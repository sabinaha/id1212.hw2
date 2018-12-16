package se.kth.sabinaha.id1212.hw2.server.net;
import se.kth.sabinaha.id1212.hw2.server.controller.ClientIO;
import se.kth.sabinaha.id1212.hw2.shared.Serializer;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * This is the main class for handling a single persistent connection.
 */
public class ClientHandler implements ClientIO, Runnable {

    private static int SESSION_ID = 0;

    private final GameHandler gameHandler;
    private final Queue<Object> objectsToSend;
    private final Queue<String> receivedCommands;
    private final int sessionID = SESSION_ID++;
    private final Selector globalSelector;
    private final SocketChannel channel;

    /**
     * Creates a client handler that orchestrates the communication network communication with the client against
     * a GameController.
     */
    ClientHandler(Selector selector, SocketChannel channel, GameHandler gameHandler) {
        this.channel = channel;
        this.globalSelector = selector;
        this.gameHandler = gameHandler;
        objectsToSend = new ArrayDeque<>();
        receivedCommands = new ArrayDeque<>();
    }

    /**
     * Gets a message from the client, adds it to the queue and starts a handler for the command.
     */
    public void receiveMessage(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        String message = "";
        try {
            message = readFromChannel(clientChannel);
        } catch (IOException e) {
            key.cancel();
            System.out.println("Client disconnected");
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        receivedCommands.add(message);
        new Thread(this).start();
    }

    /**
     * Reads the data from a channel.
     * @param clientChannel The channel to read from.
     * @return The string command from the client.
     * @throws IOException If there is a I/O problem, this is thrown.
     * @throws ClassNotFoundException A unknown class was retrieved.
     */
    private String readFromChannel(SocketChannel clientChannel) throws IOException, ClassNotFoundException {
        ByteBuffer content = ByteBuffer.allocate(1024);
        clientChannel.read(content);
        return ((String) Serializer.deserialize(content.array())).trim();
    }

    /**
     * Writes an object to the client containing game information. If the server can't write to the client
     * it disconnects.
     */
    public void sendMessages(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        Object nextToSend;
        while ( (nextToSend = this.objectsToSend.poll()) != null) {
            try {
                writeToChannel(clientChannel, nextToSend);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.objectsToSend.size() == 0)
            key.interestOps(SelectionKey.OP_READ);
        else
            key.interestOps(SelectionKey.OP_WRITE | key.interestOps());
    }

    /**
     * Adds a object to write to the client into the internal buffer, which will be sent soon.
     * @param objectToWrite The object to write.
     */
    @Override
    public void addObjectToWrite(Object objectToWrite) {
        this.objectsToSend.add(objectToWrite);
        this.channel.keyFor(globalSelector).interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ );
        this.globalSelector.wakeup();
    }

    /**
     * Serializes and writes a object to the connected client.
     * @param clientChannel The channel to write to.
     * @param obj The object to write
     * @throws IOException Is thrown if there are I/O issues.
     */
    private void writeToChannel(SocketChannel clientChannel, Object obj) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(Serializer.serializeObject(obj));
        while (buffer.hasRemaining()) {
            clientChannel.write(buffer);
        }
    }

    /**
     * Disconnects the client.
     */
    @Override
    public void disconnect() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getSessionID() {
        return this.sessionID;
    }

    @Override
    public void run() {
        gameHandler.parseCommand(this.receivedCommands.poll(), this);
    }
}