package main.java.gui;

import javax.swing.*;

public class Frame extends JFrame {

    public static final int WIDTH = Panel.GUESS_LIST_START[0] + 400;
    public static final int HEIGHT = Panel.GO_BUTTON_START[1] + Panel.GO_BUTTON_SIZE[1] + Panel.SQUARE_BORDER_SIZE + 34;

    public Frame() {
        super("Nerdle helper");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(new Panel());
        setVisible(true);
    }

    public static void main(String[] args) {
        new Frame();
    }
}
