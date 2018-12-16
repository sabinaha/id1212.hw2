package se.kth.sabinaha.id1212.hw2.client.main;

import se.kth.sabinaha.id1212.hw2.client.controller.CommandController;
import se.kth.sabinaha.id1212.hw2.client.net.ServerHandler;
import se.kth.sabinaha.id1212.hw2.client.view.CommandLineInterface;

import java.io.IOException;

/**
 * This class contains the main method, which will start the whole program
 */
public class ClientMain {
    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 54321;

        ServerHandler serverHandler = new ServerHandler(host, port);
        CommandController commandController = null;

        commandController = new CommandController(serverHandler);

        new Thread(new CommandLineInterface(commandController)).start();
    }
}
