package se.kth.sabinaha.id1212.hw2.server.starter;

import se.kth.sabinaha.id1212.hw2.server.controller.GameController;
import se.kth.sabinaha.id1212.hw2.server.controller.ServerController;
import se.kth.sabinaha.id1212.hw2.server.net.ConnectionHandler;

public class ServerStarter {
    private static final int SERVER_PORT = 54321;

    public static void main(String[] args) {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : SERVER_PORT;
        ConnectionHandler connectionHandler = new ConnectionHandler(port, new GameController());
        new ServerController(connectionHandler);
    }
}
