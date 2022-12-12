package main.java.util;

import main.java.gui.Panel;
import main.java.out.Logger;

import java.awt.*;
import java.lang.reflect.Field;

public class ColourUtil extends Color {

    private static final Logger LOGGER = new Logger("ColourUtil");

    private static final String[] OPTIONS = new String[]{
            "SQUARES_FILL",
            "BLANK",
            "YELLOW",
            "GREEN"
    };

    public ColourUtil(int r, int g, int b) {
        super(r, g, b);
    }

    @Override
    public String toString() {
        for (String fieldName : OPTIONS) {
            try {
                Field field = Panel.class.getField(fieldName);
                ColourUtil colour = (ColourUtil) field.get(null);
                if (this.equals(colour)) {
                    return fieldName;
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOGGER.log("Exception when fieldName = " + fieldName, 1);
                e.printStackTrace();
            }
        }
        return super.toString();
    }
}
