package se.kth.sabinaha.id1212.hw2.server.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * The class for accepting client requests and handling them.
 */
public class ConnectionHandler {

    private Selector selector;
    private final int port;
    private GameHandler gameHandler;

    /**
     * Creates a server socket that listens for connections and accepts them as well as
     * delegating the connections to new threads.
     * @param port The port to listen on.
     */
    public ConnectionHandler(int port, GameHandler gameHandler) {
        // This listens for incoming connections and handles them in a separate thread in the net layer.
        this.port = port;
        this.gameHandler = gameHandler;
    }

    /**
     * Initializes the server and starts listening for incoming connections.
     * @throws IOException If there is a I/O problem, this is thrown.
     */
    public void startServer() throws IOException {
        InetSocketAddress myAddress = new InetSocketAddress(port);
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(myAddress);

        this.selector = Selector.open();
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        listen();
    }

    /**
     * Starts a non blocking listener for incoming or outgoing I/O requests and calls their handlers
     * @throws IOException If there is a I/O problem, this is thrown.
     */
    private void listen() throws IOException {
        while (true) {
            selector.select();
            Iterator<SelectionKey> keySet = selector.selectedKeys().iterator();
            while (keySet.hasNext()) {
                SelectionKey key = keySet.next();
                keySet.remove();
                selector.selectedKeys().remove(key);

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    System.out.println("Accepting " + key.channel());
                    acceptClient(key);
                } else if (key.isWritable() || key.isReadable()) {
                    startHandler(key);
                }
            }
        }
    }

    /**
     * Accepts a client whom requested to connect.
     * @param key The key which was selected with the client attempted to connect.
     * @throws IOException If there is a I/O problem, this is thrown.
     */
    private void acceptClient(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel clientConnection = server.accept();
        clientConnection.configureBlocking(false);
        clientConnection.register(selector, SelectionKey.OP_READ, new ClientHandler(this.selector, clientConnection, this.gameHandler));
    }

    /**
     * Starts a read or write handler for a key.
     * @param key The key which was selected
     */
    private void startHandler(SelectionKey key) {
        ClientHandler clientHandler = (ClientHandler) key.attachment();
        if (key.isReadable()) {
            clientHandler.receiveMessage(key);
        } else if (key.isWritable()) {
            clientHandler.sendMessages(key);
        }
    }
}
