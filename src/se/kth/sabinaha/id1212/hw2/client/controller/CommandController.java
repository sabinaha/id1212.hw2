package se.kth.sabinaha.id1212.hw2.client.controller;

import se.kth.sabinaha.id1212.hw2.client.net.ServerHandler;
import se.kth.sabinaha.id1212.hw2.client.view.GameView;

import java.io.IOException;

/**
 * This is a controller for the commands issued by the view.
 */
public class CommandController {
    private ServerHandler serverHandler;

    /**
     * Creates a controller, which in turn creates and sets up the connection to the server
     * @throws IOException If there are issues with establishing a connection to the server, this will be thrown.
     */
    public CommandController (ServerHandler serverHandler) throws IOException {
        this.serverHandler = serverHandler;
        this.serverHandler.connect();
    }

    /**
     * Called for the input to be handled, or in this case sent to the server
     * @param input The command/input issued.
     */
    public void handleInput (String input) {
        this.serverHandler.sendMessage(input);
    }

    /**
     * Used to give network a way to return the data to the view.
     * @param gameView The view to be used.
     */
    public void setGameView (GameView gameView) {
        this.serverHandler.setGameView(gameView);
    }
}
