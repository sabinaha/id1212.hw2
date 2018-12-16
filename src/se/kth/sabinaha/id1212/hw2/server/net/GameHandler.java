package se.kth.sabinaha.id1212.hw2.server.net;

import se.kth.sabinaha.id1212.hw2.server.controller.ClientIO;

public interface GameHandler {
    void parseCommand(String input, ClientIO clientIO);
}
