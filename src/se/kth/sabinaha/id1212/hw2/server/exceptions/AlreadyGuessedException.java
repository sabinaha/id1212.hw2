package se.kth.sabinaha.id1212.hw2.server.exceptions;

/**
 * If a user guesses a word or a letter which is already guessed-
 */
public class AlreadyGuessedException extends Throwable {
    public AlreadyGuessedException(String s) {
        super(s);
    }
}
