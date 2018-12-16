package se.kth.sabinaha.id1212.hw2.server.controller;

import se.kth.sabinaha.id1212.hw2.server.net.ConnectionHandler;

import java.io.IOException;

/**
 * Creates a Game Controller to orchestrate the game and a connection handler to accept client connections.
 */
public class ServerController {

    /**
     * Creates a ServerController with the specified port to listen to.
     * @param connectionHandler The connection handler for the server.
     */
    public ServerController(ConnectionHandler connectionHandler) {
        try {
            connectionHandler.startServer();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("[ERROR] Server: Couldn't set up server.");
        }
    }
}
