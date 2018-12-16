package se.kth.sabinaha.id1212.hw2.shared;

/**
 * Enum that contains all possible game feedback for the client to parse.
 */
public enum GameActionFeedback {
    GAME_INFO, GAME_STARTED, GAME_RESTARTED, GAME_QUIT, DUPLICATE_GUESS, GAME_ONGOING, GAME_WON, GAME_LOST,
    NO_GAME_STARTED, INVALID_COMMAND, HELP
}
