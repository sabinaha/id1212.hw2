package se.kth.sabinaha.id1212.hw2.client.net;

import se.kth.sabinaha.id1212.hw2.client.view.GameView;
import se.kth.sabinaha.id1212.hw2.shared.GameActionFeedback;
import se.kth.sabinaha.id1212.hw2.shared.GameInfo;
import se.kth.sabinaha.id1212.hw2.shared.Serializer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

/**
 * This class handles all the communication between the client and the server. For example when the client
 * connects and disconnects from the server
 */
public class ServerHandler {
    private final int port;
    private final String host;
    private Selector selector;
    private Communicator communicator;
    private SocketChannel serverChannel;

    private boolean connected = false;

    /**
     * Creates a server handler that will connect to the specific host and port
     * @param host which host the client should use
     * @param port which port the client should connect to on the host
     */
    public ServerHandler(String host, int port) {
        this.port = port;
        this.host = host;
    }

    /**
     * Connects the client with the server
     * @throws IOException If there is a I/O problem, this is thrown.
     */
    public void connect() throws IOException {
        this.communicator = new Communicator();
        this.serverChannel = SocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.connect(new InetSocketAddress(host, port));

        this.selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_CONNECT);

        this.connected = true;
        new Thread(this.communicator).start();
    }

    /**
     * Will be used when the client has typed in a command and then send it to the server
     * @param msg
     */
    public void sendMessage(String msg) {
        this.communicator.addMessageToWrite(msg);
    }

    /**
     * Specifies which view to pass the information received from the server
     * @param gameView The game view to display information in.
     */
    public void setGameView(GameView gameView) {
        this.communicator.setGameView(gameView);
    }

    private class Communicator implements Runnable {
        private GameView gameView;
        private final Queue<String> writeBuffer;

        /**
         * Creates a new communicator
         */
        Communicator() {
            this.writeBuffer = new ArrayDeque<>();
        }

        @Override
        public void run() {
            try {
                while(connected) {
                    if (this.writeBuffer.size() > 0) {
                        serverChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    }
                    int numSelected = selector.select();

                    if (numSelected == 0)
                        continue;

                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while(keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();

                        if (!key.isValid())
                            continue;

                        if (key.isConnectable()) {
                            ((SocketChannel) key.channel()).finishConnect();
                        } else if (key.isReadable()) {
                            readFromServer(key);
                        } else if (key.isWritable()) {
                            writeToServer(key);
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    }
                }
            } catch (IOException e) {
                gameView.displayTechnicalFeedback("Lost connection to the server. Try to restart");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                gameView.displayTechnicalFeedback("Could not find that class. Try again.");
                e.printStackTrace();
            }
        }

        /**
         * Reads data from the server and passes it onto the GameView
         * @param key The SelectionKey that was chosen from the select call
         * @throws IOException If there is a I/O problem, this is thrown
         * @throws ClassNotFoundException If a unkown class is retrieved, this is thrown
         */
        private void readFromServer(SelectionKey key) throws IOException, ClassNotFoundException {
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            channel.read(buffer);
            buffer.flip();

            Object receivedObj = Serializer.deserialize(buffer.array());
            if (receivedObj instanceof GameInfo)
                gameView.displayGameInfo((GameInfo) receivedObj);
            else if (receivedObj instanceof GameActionFeedback)
                gameView.displayGameFeedback((GameActionFeedback) receivedObj);
        }

        /**
         * Write the data from the class buffer to the channel of the specified key.
         * @param key The SelectionKey that was chosen from the select call.
         * @throws IOException If there is a I/O problem, this is thrown.
         */
        private void writeToServer(SelectionKey key) throws IOException {
            SocketChannel channel = (SocketChannel) key.channel();
            Object nextToSend;
            while ((nextToSend = this.writeBuffer.poll()) != null) {
                ByteBuffer buffer = ByteBuffer.wrap(Serializer.serializeObject(nextToSend));
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
            }
        }

        /**
         * Specifies which view to pass the information received from the server.
         * @param gameView The view to pass received data to
         */
        void setGameView(GameView gameView) {
            this.gameView = gameView;
        }

        /**
         * Adds a message in the queue of messages to send to the server.
         * @param message The message to write to the server.
         */
        void addMessageToWrite (String message) {
            synchronized (this.writeBuffer){
                this.writeBuffer.add(message);
            }
            selector.wakeup();
        }

    }
}
