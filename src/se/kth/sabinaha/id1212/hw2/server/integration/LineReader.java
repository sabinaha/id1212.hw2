package se.kth.sabinaha.id1212.hw2.server.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

/**
 * A class that is used to read words from a text file.
 */
public class LineReader {
    public static String getRandomLineFromFile() {
        int length = 0;
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get("res/words.txt"));
            length = lines.size();
        } catch (IOException e) {
            System.out.println("Could not read word from file");
            e.printStackTrace();
        }
        return lines.get(new Random().nextInt(length));
    }
}
