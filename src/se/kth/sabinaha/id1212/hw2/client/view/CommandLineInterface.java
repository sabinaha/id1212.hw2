package se.kth.sabinaha.id1212.hw2.client.view;

import se.kth.sabinaha.id1212.hw2.client.controller.CommandController;
import se.kth.sabinaha.id1212.hw2.shared.GameActionFeedback;
import se.kth.sabinaha.id1212.hw2.shared.GameInfo;
import se.kth.sabinaha.id1212.hw2.shared.GameState;

import java.util.Scanner;

/**
 * This class takes input and output represented as a command line
 */
public class CommandLineInterface implements Runnable, GameView{
    private final Scanner in;
    private final CommandController commandController;
    private volatile boolean running;

    private final String PROMPT = ">> ";

    /**
     * Creates the command line interface with the specified commandController
     * @param commandController the view controller
     */
    public CommandLineInterface (CommandController commandController) {
        this.commandController = commandController;
        this.commandController.setGameView(this);
        this.running = true;
        this.in = new Scanner(System.in);
    }

    /**
     * Will continouosly run and collect what the user writes and sends the inout to the controller
     */
    @Override
    public void run() {
        String input;
        while (running) {
            System.out.print(PROMPT);
            input = this.in.nextLine();
            commandController.handleInput(input);
        }
    }

    public void displayGameFeedback (GameActionFeedback gameActionFeedback) {
        switch (gameActionFeedback) {
            case DUPLICATE_GUESS:
                System.out.println("You already guessed this!");
                break;
            case GAME_LOST:
                System.out.println("YOU LOST!");
                break;
            case GAME_WON:
                System.out.println("YOU WON! :D");
                break;
            case GAME_RESTARTED:
                System.out.println("Game restarted");
                break;
            case GAME_STARTED:
                System.out.println("Game started!");
                break;
            case NO_GAME_STARTED:
                System.out.println("You must start a game before you can issue that command");
                break;
            case INVALID_COMMAND:
                System.out.println("That is not a recognized command");
                break;
            case HELP:
                String help = "To start a game, use the \"start\" command.\n" +
                        "Guess by entering either \"guess [letter]\" or \"guess [entire word]\"\n" +
                        "To guess one letter, just type it alone\n" +
                        "If you give up on your current word, use \"restart\"\n" +
                        "Or to quit, type \"exit\"";
                System.out.println(help);
                break;
            case GAME_QUIT:
                System.out.println("Bye bye!");
                break;
        }
    }

    /**
     * Writes the progress of the current game to the client screen.
     * @param gameInfo The game info object containing data of the current state of the game to print
     */
    @Override
    public void displayGameInfo (GameInfo gameInfo) {
        StringBuilder sb = new StringBuilder();
        char[] wordProgress = gameInfo.getWordProgress();
        for (char progress : wordProgress) {
            if (progress == '\u0000')
                sb.append("_");
            else
                sb.append(progress);
            sb.append(" ");
        }
        sb.trimToSize();
        System.out.printf("Progress: %s | Remaining guesses: %d | Score: %d\n%s", sb.toString(),
                gameInfo.getRemainingGuesses(), gameInfo.getScore(), PROMPT);
        if (gameInfo.getGameState() == GameState.GAME_LOST)
            System.out.printf("The word was: %s\n", gameInfo.getSecretWord());
    }

    @Override
    public void displayTechnicalFeedback (String string) {
        System.out.println(string + "\n" + PROMPT);
    }
}
