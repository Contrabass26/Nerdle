package main.java.util;

import main.java.log.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;

public class Collector<T> extends HashSet<T> {

    private static final Logger LOGGER = new Logger("Collector");

    private final long maxCollectionSize;
    private final Output<T> output;

    public Collector(long maxCollectionSize, Output<T> output) {
        this.maxCollectionSize = maxCollectionSize;
        this.output = output;
    }

    public boolean add(T addition) {
        super.add(addition);
        if (size() == maxCollectionSize) {
            flush();
        }
        return true;
    }

    public void flush() {
        output.writeAll(this);
        clear();
        LOGGER.log("FLUSHED COLLECTION", 2);
    }

    public interface Output<T> {

        void write(T value);

        default void writeAll(Collection<T> values) {
            values.forEach(this::write);
        }
    }

    public static class FileOutput<T> implements Output<T> {

        private static final Logger LOGGER = new Logger("FileOutput");

        private final String path;
        private final String extension;
        private int number = 0;

        public FileOutput(String path) {
            int dotIndex = path.lastIndexOf('.');
            this.extension = path.substring(dotIndex);
            this.path = path.substring(0, dotIndex);
        }

        @Override
        public void write(T value) {
            try {
                tryFlush();
                BufferedWriter writer = new BufferedWriter(new FileWriter(path));
                writer.write(value.toString());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void writeAll(Collection<T> values) {
            try {
                tryFlush();
                BufferedWriter writer = new BufferedWriter(new FileWriter(getPath(), true));
                for (T value : values) {
                    writer.write(value.toString() + "\n");
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void tryFlush() {
            try {
                if (Files.size(Paths.get(getPath())) > 1073741824L) {
                    number++;
                    LOGGER.log("STARTED NEW FILE", 2);
                }
            } catch (IOException e) {
                if (e instanceof NoSuchFileException) {
                    LOGGER.log("No file found: " + getPath() + " - creating one now!", 1);
                }
            }
        }

        private String getPath() {
            return path + number + extension;
        }
    }
}
