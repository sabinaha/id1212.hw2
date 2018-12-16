package se.kth.sabinaha.id1212.hw2.server.controller;

import se.kth.sabinaha.id1212.hw2.server.exceptions.AlreadyGuessedException;
import se.kth.sabinaha.id1212.hw2.server.exceptions.OngoingGameException;
import se.kth.sabinaha.id1212.hw2.server.model.Game;
import se.kth.sabinaha.id1212.hw2.server.model.GameCommand;
import se.kth.sabinaha.id1212.hw2.server.model.ParsedCommand;
import se.kth.sabinaha.id1212.hw2.server.net.GameHandler;
import se.kth.sabinaha.id1212.hw2.shared.GameActionFeedback;
import se.kth.sabinaha.id1212.hw2.shared.GameInfo;
import se.kth.sabinaha.id1212.hw2.shared.GameState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class controls the flow of the game, and what should be sent to the client.
 */
public class GameController implements GameHandler {

    private Map<Integer, Game> games;

    /**
     * Creates a new game controller
     */
    public GameController() {
        this.games = new HashMap<>();
    }

    /**
     * Makes a word or a letter guess in the hangman game.
     * @param guess The word or letter to guess.
     * @return The the <code>GameActionFeedback</code> which indicates the status to the client.
     */
    private GameActionFeedback makeGuess(String guess, int sessionID) {
        Game game;
        synchronized (this) {
            game = this.games.get(sessionID);
        }
        try {
            if (game.getGameState() == GameState.GAME_LOST || game.getGameState() == GameState.GAME_WON)
                return GameActionFeedback.NO_GAME_STARTED;
            else
                game.makeGuess(guess);
        } catch (AlreadyGuessedException e) {
            return GameActionFeedback.DUPLICATE_GUESS;
        }
        if (game.getGameState() == GameState.GAME_LOST) {
            return GameActionFeedback.GAME_LOST;
        } else if (game.getGameState() == GameState.GAME_WON) {
            return GameActionFeedback.GAME_WON;
        } else {
            return GameActionFeedback.GAME_INFO;
        }
    }

    /**
     * Starts a game session for the player.
     * If a game is already ongoing an exception will be thrown
     * @throws OngoingGameException Exception indicating that a game is already ongoing, and no new game will
     * be started.
     */
    private void startGame(int withID) throws OngoingGameException {
        synchronized (this) {
            if (this.games.containsKey(withID)) {
                if (this.games.get(withID).getGameState() == GameState.GAME_ONGOING) {
                    throw new OngoingGameException("There is alreadya a game ongoing");
                } else {
                    this.games.put(withID, new Game(this.games.get(withID)));
                }
            } else {
                this.games.put(withID, new Game());
            }
        }
    }

    /**
     * Restarts the game, which results in -1 points. Or starts a new game if there's no ongoing game.
     */
    private void restartGame(int gameID) {
        synchronized (this) {
            if (this.games.containsKey(gameID)) {
                Game oldGame = this.games.get(gameID);
                if (oldGame.getGameState() == GameState.GAME_ONGOING) {
                    oldGame.concede();
                }
                this.games.put(gameID, new Game(oldGame));
            } else {
                try {
                    startGame(gameID);
                } catch (OngoingGameException e) {
                    e.printStackTrace(); // Won't happen though
                }
            }
        }
    }

    /**
     * Returns the game info of the game.
     * @return A <code>GameInfo</code> object that indicates the current state of the game.
     */
    private GameInfo getGameInfo(int sessionID) {
        Game game;
        synchronized (this) {
            game = this.games.get(sessionID);
        }
        return game.getGameInfo();
    }


    /**
     * Parses the command and performs the requested action.
     * @param command The raw command from the client.
     * @param clientIO The client handler to use for sending back responses.
     */
    @Override
    public void parseCommand(String command, ClientIO clientIO) {
        ParsedCommand parsedCommand = getParsedCommand(command);
        GameCommand gc = parsedCommand.getGameCommand();
        int sessionID = clientIO.getSessionID();
        // Take action on the command
        switch (gc) {
            case START_GAME:
                try {
                    startGame(sessionID);
                    clientIO.addObjectToWrite(getGameInfo(sessionID));
                } catch (OngoingGameException e) {
                    clientIO.addObjectToWrite(GameActionFeedback.GAME_ONGOING);
                    e.printStackTrace();
                }
                break;
            case RESTART:
                restartGame(sessionID);
                clientIO.addObjectToWrite(GameActionFeedback.GAME_RESTARTED);
                clientIO.addObjectToWrite(getGameInfo(sessionID));
                break;
            case MAKE_GUESS:
                GameActionFeedback gaf = makeGuess(parsedCommand.getArguments()[0], sessionID);
                clientIO.addObjectToWrite(getGameInfo(sessionID));
                clientIO.addObjectToWrite(gaf);
                break;
            case FETCH_INFO:
                // The boolean is introduced to avoid deadlocking since getGameInfo uses "this" as lock as well.
                boolean gameOngoing = false;
                synchronized (this) {
                    if (this.games.get(sessionID) == null) {
                        clientIO.addObjectToWrite(GameActionFeedback.NO_GAME_STARTED);
                    } else {
                        gameOngoing = true;
                    }
                }
                if (gameOngoing)
                    clientIO.addObjectToWrite(getGameInfo(sessionID));
                break;
            case EXIT:
                System.err.println("Quitting session #" + sessionID);
                synchronized (this) {
                    this.games.remove(sessionID);
                }
                clientIO.disconnect();
                break;
            case INVALID_COMMAND:
                System.err.println("Invalid command received");
                clientIO.addObjectToWrite(GameActionFeedback.INVALID_COMMAND);
                break;
        }
    }

    /**
     * Parses the a raw String sent to the server and creates a ParsedCommand which may be
     * used for performing the requested actions.
     * @param command The raw string command.
     * @return A ParsedCommand object which includes the Command and any arguments.
     */
    private ParsedCommand getParsedCommand(String command) {
        String[] commandsArray = command.trim().split(" ");
        String[] arguments = null;
        GameCommand gc;
        if (commandsArray.length == 1 && commandsArray[0].length() == 1) {
            gc = GameCommand.MAKE_GUESS;
            arguments = new String[]{commandsArray[0]};
        } else if (commandsArray[0].equalsIgnoreCase("start")) {
            gc = GameCommand.START_GAME;
        } else if (commandsArray[0].equalsIgnoreCase("guess")) {
            gc = GameCommand.MAKE_GUESS;
            arguments = Arrays.copyOfRange(commandsArray, 1, commandsArray.length);
        } else if (commandsArray[0].equalsIgnoreCase("exit")) {
            gc = GameCommand.EXIT;
        } else if (commandsArray[0].equalsIgnoreCase("info")) {
            gc = GameCommand.FETCH_INFO;
        } else if(commandsArray[0].equalsIgnoreCase("restart")) {
            gc = GameCommand.RESTART;
        } else {
            gc = GameCommand.INVALID_COMMAND;
        }
        return new ParsedCommand(gc, arguments);
    }
}
