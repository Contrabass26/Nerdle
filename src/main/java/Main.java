package main.java;

import main.java.compute.Computer;
import main.java.compute.Guess;
import main.java.gui.Frame;
import main.java.out.Logger;
import main.java.out.PossibilityReader;
import main.java.out.PossibilityWriter;

import java.io.IOException;
import java.util.*;

import static main.java.config.Config.CONFIG;

public class Main {

    public static final String FIRST_GUESS = "5+5+5=15";
    private static final Logger LOGGER = new Logger("Main");

    public static void main(String[] args) {
        switch (CONFIG.configuration) {
            case "FRAME" -> new Frame();
            case "GENERATE" -> {
                PossibilityWriter writer = PossibilityWriter.create();
                assert writer != null;
                Computer.getAllPossibilities().forEach(s -> {
                    try {
                        writer.addPossibility(s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            case "READ" -> {
                PossibilityReader possibilityReader = PossibilityReader.create();
                assert possibilityReader != null;
                try {
                    possibilityReader.readAll(System.out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            case "TEST" -> {
                List<String> answers = Computer.getAllPossibilities().stream().toList();
                Map<Integer, Integer> guessCounts = new HashMap<>();
                for (int i = 0; i < answers.size(); i++) {
                    String answer = answers.get(i);
                    LOGGER.log((i + 1) + ": " + answer, 2);
                    Guess[] guesses = new Guess[6];
                    guesses[0] = new Guess(FIRST_GUESS, Computer.getFeedback(answer, FIRST_GUESS));
                    LOGGER.log("\t" + FIRST_GUESS, 2);
                    if (answer.equals(FIRST_GUESS)) {
                        guessCounts.merge(1, 1, Integer::sum);
                        continue;
                    }
                    int numGuesses;
                    for (numGuesses = 1; numGuesses < 6; numGuesses++) {
                        List<Map.Entry<String, Double>> possibilities = Computer.getPossibilities(guesses);
                        if (possibilities.size() == 1) break;
                        String best = possibilities.get(0).getKey();
                        LOGGER.log("\t" + best, 2);
                        guesses[numGuesses] = new Guess(best, Computer.getFeedback(answer, best));
                    }
                    guessCounts.merge(numGuesses + 1, 1, Integer::sum);
                }
                guessCounts.forEach((k, v) -> System.out.println(k + ": " + v));
            }
            default -> System.out.println("Unrecognised configuration: " + CONFIG.configuration);
        }
    }
}
