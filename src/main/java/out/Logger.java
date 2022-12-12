package main.java.out;

import com.contrabass.tools.Printer;
import main.java.config.Config;
import org.jetbrains.annotations.Range;

import java.util.function.BiConsumer;

public class Logger {

    // Logging levels:
    // 1 = error
    // 2 = info
    // 3 = debug

    private final String sender;

    public Logger(String sender) {
        this.sender = sender;
    }

    public void log(Object message, @Range(from = 1, to = 3) int level) {
        log(message, sender, level);
    }

    public static void log(Object message, String sender, int level) {
        if (level <= Config.CONFIG.logLevel) {
            // Should log this message
            BiConsumer<Object, String> printerMethod;
            switch (level) {
                case 1 -> printerMethod = Printer::error;
                case 3 -> printerMethod = Printer::debug;
                default -> printerMethod = Printer::info;
            }
            printerMethod.accept(message, sender);
        }
    }
}
