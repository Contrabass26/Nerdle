package main.java;

import main.java.compute.Computer;
import main.java.compute.Guess;
import main.java.compute.PossibilityComparator;
import main.java.config.Config;
import main.java.log.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Benchmark {

    private static final Logger LOGGER = new Logger("Benchmark");

    public static void main(String[] args) {
        possibilities12();
    }

    private static void possibilities12() {
        if (Config.CONFIG.length != 12) throw new AssertionError("Length in config must be 12");
        runWithDuration(() -> Computer.getPossibilities(new Guess("9+8*7-3/1=62", "YGBBYYYBBGYG")));
    }

    private static void sorting() {
        try {
            List<String> temp = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader("generated_data/possibilities8.txt"));
            reader.lines().forEach(temp::add);
            reader.close();
            List<String> possibilities = temp.subList(0, (int) Math.ceil(temp.size() / 7d));
            // Sort
            LOGGER.log("Starting...", 2);
            runWithDuration(() -> {
                PossibilityComparator comparator = new PossibilityComparator(possibilities);
                possibilities.sort(comparator);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void feedback() {
        runWithDuration(() -> {
            for (int i = 0; i < 17438; i++) {
                Computer.getFeedback("111+999=1110", "9+8*7-3/1=62");
            }
        });
    }

    private static void entropy() {
        try {
            List<String> possibilities = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader("generated_data/possibilities8.txt"));
            reader.lines().forEach(possibilities::add);
            reader.close();
            List<String> subList = possibilities.subList(0, possibilities.size());
            // Sort
            LOGGER.log("Starting...", 2);
            runWithDuration(() -> Computer.getEntropy(subList.get(0), subList));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runWithDuration(Runnable operation) {
        long startTime = System.currentTimeMillis();
        operation.run();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        LOGGER.log("Operation completed in " + duration + "ms", 2);
    }
}
