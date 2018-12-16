package se.kth.sabinaha.id1212.hw2.client.view;

import se.kth.sabinaha.id1212.hw2.shared.GameActionFeedback;
import se.kth.sabinaha.id1212.hw2.shared.GameInfo;

public interface GameView {
    void displayGameFeedback(GameActionFeedback gameActionFeedback);
    void displayGameInfo (GameInfo gameInfo);
    void displayTechnicalFeedback (String string);
}
