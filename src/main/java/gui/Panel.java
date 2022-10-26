package main.java.gui;

import main.java.compute.Computer;
import main.java.compute.Guess;
import main.java.log.Logger;
import main.java.util.ColourUtil;
import main.java.util.MathUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.*;

import static main.java.config.Config.CONFIG;
import static main.java.util.MathUtil.meetsBounds;

public class Panel extends JPanel {

    private static final ColourUtil BACKGROUND = new ColourUtil(18, 18, 18);
    private static final ColourUtil TEXT = new ColourUtil(255, 255, 255);
    private static final ColourUtil SQUARES_LINE = new ColourUtil(255, 255, 255);
    public static final ColourUtil SQUARES_FILL = new ColourUtil(18, 18, 18);
    public static final ColourUtil GREEN = new ColourUtil(83, 141, 78);
    public static final ColourUtil YELLOW = new ColourUtil(181, 159, 58);
    public static final ColourUtil BLANK = new ColourUtil(58, 58, 58);

    private static final Font NERDLE_FONT = new Font("nerdle_font", Font.PLAIN, 26);
    private static final BasicStroke SQUARES_LINE_STROKE = new BasicStroke(3);
    public static final BasicStroke TITLE_UNDERLINE_STROKE = new BasicStroke(2);
    private static final BasicStroke NORMAL_STROKE = new BasicStroke(1);

    static final int STRING_HEIGHT = 22;
    static final int[] SQUARE_START = {10, 10};
    static final int[] SQUARE_SIZE = {400 / CONFIG.length, 50};
    static final int SQUARE_BORDER_SIZE = 5;
    static final int[] GUESS_LIST_START = {SQUARE_START[0] + (SQUARE_SIZE[0] + SQUARE_BORDER_SIZE) * CONFIG.length + 30, SQUARE_START[1]};
    static final int COLOUR_MENU_BORDER_SIZE = 2;
    static final int COLOUR_MENU_SQUARE_SIZE = 30;
    static final int[] COLOUR_MENU_SIZE = {COLOUR_MENU_SQUARE_SIZE * 4 + COLOUR_MENU_BORDER_SIZE * 5, COLOUR_MENU_SQUARE_SIZE + COLOUR_MENU_BORDER_SIZE * 2};
    static final int[] GO_BUTTON_START = {SQUARE_START[0], SQUARE_START[1] + (SQUARE_SIZE[1] + SQUARE_BORDER_SIZE) * 6 + 5};
    static final int[] GO_BUTTON_SIZE = {(SQUARE_SIZE[0] + SQUARE_BORDER_SIZE) * CONFIG.length - SQUARE_BORDER_SIZE, 40};
    private static final String NO_SOLUTIONS_TEXT = "No solutions found";

    private static final Logger LOGGER = new Logger("Panel");

    private int[] selectedSquare = {0, 0};
    private final char[][] squareValues;
    private final ColourUtil[][] squareColours;
    private boolean showColourMenu = false;
    private Rectangle colourMenuPos = new Rectangle(0, 0, 0, 0);
    private List<Map.Entry<String, Double>> possibilities = new ArrayList<>();
    private String bestPossibility = "";

    public Panel() {
        super();
        loadFont();
        setFocusable(true);
        requestFocusInWindow();
        setFocusTraversalKeysEnabled(false);
        // Set up square values to be all empty
        squareValues = new char[CONFIG.length][6];
        for (int i = 0; i < squareValues.length; i++) {
            char[] inner = new char[squareValues[i].length];
            Arrays.fill(inner, ' ');
            squareValues[i] = inner;
        }
        // Set up square colours to be all normal
        squareColours = new ColourUtil[CONFIG.length][6];
        for (int i = 0; i < squareColours.length; i++) {
            ColourUtil[] inner = new ColourUtil[squareColours[i].length];
            Arrays.fill(inner, SQUARES_FILL);
            squareColours[i] = inner;
        }
        // Listeners for squares
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = (e.getX() - SQUARE_START[0]) / (SQUARE_SIZE[0] + SQUARE_BORDER_SIZE);
                int y = (e.getY() - SQUARE_START[1]) / (SQUARE_SIZE[1] + SQUARE_BORDER_SIZE);
                switch (e.getButton()) {
                    case MouseEvent.BUTTON1 -> {
                        Point clickPoint = new Point(e.getX(), e.getY());
                        if (showColourMenu) {
                            if (colourMenuPos.contains(clickPoint)) {
                                int xWithinMenu = e.getX() - colourMenuPos.x;
                                int squareIndex = (xWithinMenu - COLOUR_MENU_BORDER_SIZE) / (COLOUR_MENU_SQUARE_SIZE + COLOUR_MENU_BORDER_SIZE);
                                ColourUtil colour = new ColourUtil[]{BLANK, YELLOW, GREEN, SQUARES_FILL}[squareIndex];
                                int[] squarePos = realToSquare(colourMenuPos.x, colourMenuPos.y);
                                squareColours[squarePos[0]][squarePos[1]] = colour;
                            }
                            showColourMenu = false;
                            repaint();
                        } else if (new Rectangle(GO_BUTTON_START[0], GO_BUTTON_START[1], GO_BUTTON_SIZE[0], GO_BUTTON_SIZE[1]).contains(clickPoint)) {
                            new Thread(() -> {
                                generatePossibilities();
                                repaint();
                            }).start();
                        } else {
                            if (meetsBounds(x, 0, CONFIG.length - 1) && meetsBounds(y, 0, 5)) {
                                selectedSquare = new int[]{x, y};
                            }
                        }
                        repaint();
                    }
                    case MouseEvent.BUTTON3 -> {
                        if (meetsBounds(x, 0, CONFIG.length - 1) && meetsBounds(y, 0, 5)) {
                            showColourMenu = true;
                            colourMenuPos = new Rectangle(
                                    MathUtil.cap(e.getX(), SQUARE_BORDER_SIZE, getWidth() - SQUARE_BORDER_SIZE - COLOUR_MENU_SIZE[0]),
                                    MathUtil.cap(e.getY(), SQUARE_BORDER_SIZE, getHeight() - SQUARE_BORDER_SIZE - COLOUR_MENU_SIZE[1]),
                                    COLOUR_MENU_SIZE[0],
                                    COLOUR_MENU_SIZE[1]
                            );
                            repaint();
                        }
                    }
                }
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!(Arrays.compare(selectedSquare, new int[]{-1, -1}) == 0)) {
                    char toInsert = e.getKeyChar();
                    if ("1234567890+-*/=".contains(String.valueOf(toInsert))) {
                        squareValues[selectedSquare[0]][selectedSquare[1]] = toInsert;
                        advanceSelectedSquare();
                        repaint();
                    } else if ("byg".contains(String.valueOf(toInsert).toLowerCase(Locale.ROOT))) {
                        switch (e.getKeyChar()) {
                            case 'b' -> squareColours[selectedSquare[0]][selectedSquare[1]] = BLANK;
                            case 'y' -> squareColours[selectedSquare[0]][selectedSquare[1]] = YELLOW;
                            case 'g' -> squareColours[selectedSquare[0]][selectedSquare[1]] = GREEN;
                        }
                        advanceSelectedSquare();
                        repaint();
                    } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
                        squareValues[selectedSquare[0]][selectedSquare[1]] = ' ';
                        if (selectedSquare[0] > 0) {
                            selectedSquare[0]--;
                        } else if (selectedSquare[1] > 0) {
                            selectedSquare[1]--;
                            selectedSquare[0] = CONFIG.length - 1;
                        }
                        repaint();
                    } else if (e.getKeyCode() == KeyEvent.VK_TAB) {
                        if (bestPossibility.length() == CONFIG.length) {
                            for (int i = 0; i < CONFIG.length; i++) {
                                squareValues[i][selectedSquare[1]] = bestPossibility.charAt(i);
                            }
                        }
                        repaint();
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        new Thread(() -> {
                            generatePossibilities();
                            repaint();
                        }).start();
                    } else {
                        int[] transform = {0, 0};
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_UP -> transform = new int[]{0, -1};
                            case KeyEvent.VK_DOWN -> transform = new int[]{0, 1};
                            case KeyEvent.VK_LEFT -> transform = new int[]{-1, 0};
                            case KeyEvent.VK_RIGHT -> transform = new int[]{1, 0};
                        }
                        int[] potential = new int[]{selectedSquare[0] + transform[0], selectedSquare[1] + transform[1]};
                        if (meetsBounds(potential[0], 0, CONFIG.length - 1) && meetsBounds(potential[1], 0, 5)) {
                            selectedSquare = potential;
                            repaint();
                        }
                    }
                }
            }
        });
    }

    public static void loadFont() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Panel.class.getClassLoader().getResourceAsStream("nerdle_font.ttf"))));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    private void generatePossibilities() {
        List<Guess> guesses = new ArrayList<>();
        outer:
        for (int y = 0; y < 6; y++) {
            StringBuilder guess = new StringBuilder();
            StringBuilder feedback = new StringBuilder();
            for (int x = 0; x < CONFIG.length; x++) {
                char value = squareValues[x][y];
                char colour = squareColours[x][y].toString().charAt(0);
                if (value == ' ' || !"BGY".contains(String.valueOf(colour))) {
                    break outer;
                }
                guess.append(value);
                feedback.append(colour);
            }
            guesses.add(new Guess(guess.toString(), feedback.toString()));
        }
        long startTime = System.currentTimeMillis();
        possibilities = Computer.getPossibilities(guesses.toArray(new Guess[0]));
        long duration = System.currentTimeMillis() - startTime;
        LOGGER.log("Operation completed in " + duration + "ms", 2);
        if (possibilities.size() == 0) {
            possibilities.add(Map.entry(NO_SOLUTIONS_TEXT, 0d));
        }
        bestPossibility = possibilities.get(0).getKey();
    }

    private void advanceSelectedSquare() {
        if (selectedSquare[0] < CONFIG.length - 1) {
            selectedSquare[0]++;
        } else if (selectedSquare[1] < 5) {
            selectedSquare[1]++;
            selectedSquare[0] = 0;
        }
    }

    private static int[] realToSquare(int x, int y) {
        return new int[]{
                (x - SQUARE_START[0]) / (SQUARE_BORDER_SIZE + SQUARE_SIZE[0]),
                (y - SQUARE_START[1]) / (SQUARE_BORDER_SIZE + SQUARE_SIZE[1])
        };
    }

    @Override
    public void paint(Graphics graphics) {
        // Cast to Graphics2D
        Graphics2D g = (Graphics2D) graphics;
        // Use correct font
        g.setFont(NERDLE_FONT);
        // Background
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, getWidth(), getHeight());
        // Guess squares
        paintGuessSquares(g);
        // Colour menu
        if (showColourMenu) {
            paintColourMenu(g);
        }
        // Go button
        paintGoButton(g);
        // List of guesses
        paintGuessList(g);
    }

    private void paintGuessSquares(Graphics2D g) {
        int selectionInset = 4;
        for (int x = SQUARE_START[0]; x < SQUARE_START[0] + (SQUARE_SIZE[0] + SQUARE_BORDER_SIZE) * CONFIG.length; x += SQUARE_SIZE[0] + SQUARE_BORDER_SIZE) {
            for (int y = SQUARE_START[1]; y < SQUARE_START[1] + (SQUARE_SIZE[1] + SQUARE_BORDER_SIZE) * 6; y += SQUARE_SIZE[1] + SQUARE_BORDER_SIZE) {
                int xSquare = (x - SQUARE_START[0]) / (SQUARE_SIZE[0] + SQUARE_BORDER_SIZE);
                int ySquare = (y - SQUARE_START[1]) / (SQUARE_SIZE[1] + SQUARE_BORDER_SIZE);
                ColourUtil fillColour = squareColours[xSquare][ySquare];
                if (xSquare == selectedSquare[0] && ySquare == selectedSquare[1]) {
                    g.setColor(fillColour);
                    Rectangle rectangle = new Rectangle(x + selectionInset / 2, y + selectionInset / 2, SQUARE_SIZE[0] - selectionInset, SQUARE_SIZE[1] - selectionInset);
                    g.fillRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, 5, 5);
                    g.setColor(SQUARES_LINE);
                    g.setStroke(SQUARES_LINE_STROKE);
                    g.drawRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, 5, 5);
                    g.setStroke(NORMAL_STROKE);
                } else {
                    g.setColor(fillColour);
                    g.fillRoundRect(x, y, SQUARE_SIZE[0], SQUARE_SIZE[1], 5, 5);
                    g.setColor(SQUARES_LINE);
                    g.drawRoundRect(x, y, SQUARE_SIZE[0], SQUARE_SIZE[1], 5, 5);
                }
                // Contents
                String contents = String.valueOf(squareValues[xSquare][ySquare]);
                int width = g.getFontMetrics().stringWidth(contents);
                g.setColor(TEXT);
                g.drawString(contents, x + SQUARE_SIZE[0] / 2 - width / 2, y + SQUARE_SIZE[1] - (SQUARE_SIZE[1] - STRING_HEIGHT) / 2);
            }
        }
    }

    private void paintColourMenu(Graphics2D g) {
        g.setColor(SQUARES_FILL);
        g.fillRoundRect(colourMenuPos.x, colourMenuPos.y, colourMenuPos.width, colourMenuPos.height, 5, 5);
        g.setColor(SQUARES_LINE);
        g.drawRoundRect(colourMenuPos.x, colourMenuPos.y, colourMenuPos.width, colourMenuPos.height, 5, 5);
        int y = colourMenuPos.y + COLOUR_MENU_BORDER_SIZE;
        g.setColor(BLANK);
        g.fillRect(colourMenuPos.x + COLOUR_MENU_BORDER_SIZE, y, COLOUR_MENU_SQUARE_SIZE, COLOUR_MENU_SQUARE_SIZE);
        g.setColor(YELLOW);
        g.fillRect(colourMenuPos.x + COLOUR_MENU_BORDER_SIZE * 2 + COLOUR_MENU_SQUARE_SIZE, y, COLOUR_MENU_SQUARE_SIZE, COLOUR_MENU_SQUARE_SIZE);
        g.setColor(GREEN);
        g.fillRect(colourMenuPos.x + COLOUR_MENU_BORDER_SIZE * 3 + COLOUR_MENU_SQUARE_SIZE * 2, y, COLOUR_MENU_SQUARE_SIZE, COLOUR_MENU_SQUARE_SIZE);
        g.setColor(SQUARES_FILL);
        g.fillRect(colourMenuPos.x + COLOUR_MENU_BORDER_SIZE * 4 + COLOUR_MENU_SQUARE_SIZE * 3, y, COLOUR_MENU_SQUARE_SIZE, COLOUR_MENU_SQUARE_SIZE);
    }

    private void paintGoButton(Graphics2D g) {
        g.setColor(SQUARES_LINE);
        g.setStroke(SQUARES_LINE_STROKE);
        g.drawRoundRect(GO_BUTTON_START[0], GO_BUTTON_START[1], GO_BUTTON_SIZE[0], GO_BUTTON_SIZE[1], 10, 10);
        g.setStroke(NORMAL_STROKE);
        String text = "Generate guesses";
        g.setColor(TEXT);
        g.drawString(text, GO_BUTTON_START[0] + GO_BUTTON_SIZE[0] / 2 - g.getFontMetrics().stringWidth(text) / 2, GO_BUTTON_START[1] + 30);
    }

    private void paintGuessList(Graphics2D g) {
        g.setColor(TEXT);
        int size;
        if (possibilities.size() == 0) {
            size = 0;
        } else if (possibilities.get(0).getKey().equals(NO_SOLUTIONS_TEXT)) {
            size = 0;
        } else {
            size = possibilities.size();
        }
        String text = "Best guesses: (of " + size + ")";
        g.drawString(text, GUESS_LIST_START[0], GUESS_LIST_START[1] + STRING_HEIGHT);
        g.setStroke(TITLE_UNDERLINE_STROKE);
        int y = GUESS_LIST_START[1] + STRING_HEIGHT + 3;
        g.drawLine(GUESS_LIST_START[0], y, GUESS_LIST_START[0] + g.getFontMetrics().stringWidth(text), y);
        g.setStroke(NORMAL_STROKE);
        y += 30;
        for (Map.Entry<String, Double> possibility : possibilities.subList(0, Math.min(10, possibilities.size()))) {
            g.drawString(possibility.getKey(), GUESS_LIST_START[0], y);
            if (!possibility.getKey().equals(NO_SOLUTIONS_TEXT)) {
                double entropy = possibility.getValue();
                String entropyStr = String.format("%.2f", entropy);
                int stringWidth = g.getFontMetrics().stringWidth(entropyStr);
                g.drawString(entropyStr, getWidth() - stringWidth - 10, y);
            }
            y += 35;
        }
    }
}
