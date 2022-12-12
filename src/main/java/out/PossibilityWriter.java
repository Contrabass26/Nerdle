package main.java.out;

import com.fasterxml.jackson.databind.JsonNode;
import main.java.config.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class PossibilityWriter {

    static final String CHAR_SET = "0123456789+-*/=";

    private long count = 0;
    private final List<Byte> bytes = new ArrayList<>();

    protected PossibilityWriter() {}

    public void addPossibility(String possibility) throws IOException {
        for (byte b : format(possibility)) {
            bytes.add(b);
        }
        count++;
    }

    public long getCount() {
        return count;
    }

    private byte[] getByteArray() {
        byte[] bytes = new byte[this.bytes.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = this.bytes.get(i);
        }
        return bytes;
    }

    public void close() throws IOException {
        Files.write(Paths.get((String) Config.CONFIG.getOption("path", JsonNode::asText)), getByteArray());
    }

    protected abstract byte[] format(String possibility);

    public static PossibilityWriter create() {
        try {
            switch (Config.CONFIG.length) {
                case 8: return new LengthEight();
            }
            throw new IllegalStateException("No PossibilityWriter for length " + Config.CONFIG.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static byte merge(byte b1, byte b2) {
        assert b1 < 16;
        assert b2 < 16;
        b1 = (byte) (b1 << 4);
        return (byte) (b1 | b2);
    }

    public static class LengthEight extends PossibilityWriter {

        public LengthEight() throws IOException {
            super();
        }

        @Override
        protected byte[] format(String possibility) {
            byte[] bytes = new byte[4];
            for (int i = 0; i < 8; i += 2) {
                byte b1 = (byte) CHAR_SET.indexOf(possibility.charAt(i));
                byte b2 = (byte) CHAR_SET.indexOf(possibility.charAt(i + 1));
                bytes[i / 2] = merge(b1, b2);
            }
            return bytes;
        }
    }
}
