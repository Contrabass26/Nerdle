package main.java.out;

import com.fasterxml.jackson.databind.JsonNode;
import main.java.config.Config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static main.java.out.PossibilityWriter.CHAR_SET;

public abstract class PossibilityReader {

    public PossibilityReader() {}

    public abstract void readAll(PrintStream out) throws IOException;

    public static PossibilityReader create() {
        try {
            switch (Config.CONFIG.length) {
                case 8: return new LengthEight();
            }
            throw new IllegalStateException("No PossibilityReader for length " + Config.CONFIG.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static byte getLeft(byte b) {
        return (byte) ((b & 0xF0) >> 4);
    }

    protected static byte getRight(byte b) {
        return (byte) (b & 0x0F);
    }

    public static class LengthEight extends PossibilityReader {

        private final long count;

        public LengthEight() throws IOException {
            super();
            count = Files.size(Paths.get((String) Config.CONFIG.getOption("path", JsonNode::asText))) / 4;
        }

        // TODO: StandardOpenOption.append?

        @Override
        public void readAll(PrintStream out) throws IOException {
            byte[] bytes = Files.readAllBytes(Paths.get((String) Config.CONFIG.getOption("path", JsonNode::asText)));
            int num = 0;
            for (int i = 0; i < count; i++) {
                StringBuilder possibility = new StringBuilder();
                for (int j = 0; j < 4; j++) {
                    byte b = bytes[i * 4 + j];
                    possibility.append(CHAR_SET.charAt(getLeft(b)));
                    possibility.append(CHAR_SET.charAt(getRight(b)));
                }
                out.println(possibility);
                num++;
                System.out.println(num);
            }
        }
    }
}
