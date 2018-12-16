package se.kth.sabinaha.id1212.hw2.server.exceptions;

/**
 * Thrown if a user prompts to start a game, but there is already a ongoing game
 */
public class OngoingGameException extends Exception {
    public OngoingGameException(String s) {
        super(s);
    }
}
