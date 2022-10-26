package main.java.compute;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import main.java.config.Config;
import main.java.log.Logger;
import main.java.util.MathUtil;
import main.java.util.Operator;
import main.java.util.OrderedMap;
import main.java.util.StringUtil;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static main.java.config.Config.CONFIG;

public class Computer {

    private static final Logger LOGGER = new Logger("Computer");
    private static final DoubleEvaluator DOUBLE_EVALUATOR = new DoubleEvaluator();

    public static double evaluate(String expression) {
        try {
            return DOUBLE_EVALUATOR.evaluate(expression);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return 0;
    }

    public static double getEntropy(String guess, List<String> possibilities) {
        List<String> subset = new ArrayList<>(possibilities);
        Collections.shuffle(subset);
        final List<String> finalSubset = subset.subList(0, subset.size() / CONFIG.filterThreshold);
        FrequencyMap<String, String> frequencyMap = new FrequencyMap<>(finalSubset) {
            @Override
            public void incorporate(String toIncorporate) {
                increment(getFeedback(toIncorporate, guess));
            }
        };
        return frequencyMap.frequencies().stream().mapToDouble(f -> {
            double probability = (double) f / finalSubset.size();
            return probability * MathUtil.getInformation(probability);
        }).sum();
    }

    public static String getFeedback(String answer, String guess) {
        Map<Character, Integer> quota = new HashMap<>();
        char[] feedback = answer.toCharArray();
        for (int i = 0; i < feedback.length; ++i) {
            quota.merge(answer.charAt(i), 1, Integer::sum);
        }
        feedback = new char[Config.CONFIG.length];
        Arrays.fill(feedback, ' ');
        for (int i = 0; i < Config.CONFIG.length; ++i) {
            char c = guess.charAt(i);
            if (!quota.containsKey(c)) {
                feedback[i] = 'B';
            } else if (c == answer.charAt(i)) {
                feedback[i] = 'G';
                quota.merge(c, -1, Integer::sum);
            }
        }
        for (int i = 0; i < Config.CONFIG.length; ++i) {
            char c = guess.charAt(i);
            if (!quota.containsKey(c) || feedback[i] != ' ') continue;
            if (quota.get(c) == 0) {
                feedback[i] = 'B';
            } else {
                feedback[i] = 'Y';
                quota.merge(c, -1, Integer::sum);
            }
        }
        return new String(feedback);
    }

    public static List<Map.Entry<String, Double>> getPossibilities(Guess... guesses) {
        // Limitations
        String[] charPossibilities = getBaseCharPossibilities(); // What the character at each index could be
        Set<Integer> greenOperatorCounts = new HashSet<>(); // How many operators in the right place there are in each guess
        List<Predicate<String>> charCountPredicates = new ArrayList<>();
        for (Guess guess : guesses) {
            if (guess == null) break; // No more guesses
            int greenOperatorCount = 0;
            // Modify charPossibilities based on feedback from previous guess
            Map<Character, Integer> minCharCounts = new HashMap<>(); // The minimum number of each character that there must be
            Map<Character, Boolean> capped = new HashMap<>(); // true if there can be no more of this character than its minCharCounts value
            Set<Character> uniqueGuessChars = new HashSet<>(); // All unique characters in the guess
            for (int i = 0; i < CONFIG.length; i++) {
                char guessChar = guess.guess().charAt(i);
                String guessCharStr = String.valueOf(guessChar);
                switch (guess.feedback().charAt(i)) {
                    case 'G' -> {
                        charPossibilities[i] = guessCharStr;
                        minCharCounts.merge(guessChar, 1, Integer::sum);
                        if (guessChar == '=') {
                            // No other characters can be an equals sign - modify charPossibilities accordingly
                            for (int j = 0; j < charPossibilities.length; j++) {
                                if (j != i) {
                                    charPossibilities[j] = charPossibilities[j].replace("=", "");
                                }
                            }
                        }
                        if ("+-*/".contains(guessCharStr)) {
                            greenOperatorCount++;
                        }
                    }
                    case 'Y' -> {
                        minCharCounts.merge(guessChar, 1, Integer::sum);
                        charPossibilities[i] = charPossibilities[i].replace(guessCharStr, ""); // Can't be this character at this index
                    }
                    case 'B' -> capped.put(guessChar, true); // No more of this character
                }
                uniqueGuessChars.add(guessChar);
            }
            // Get counts for each character
            for (char c : uniqueGuessChars) {
                int minCount = minCharCounts.getOrDefault(c, 0);
                boolean isCapped = capped.getOrDefault(c, false);
                if (minCount == 0 && isCapped) {
                    // None of this character in answer - remove from all charPossibilities
                    for (int i = 0; i < charPossibilities.length; i++) {
                        charPossibilities[i] = charPossibilities[i].replace(String.valueOf(c), "");
                    }
                } else {
                    // Add predicate to ensure that minCount is satisfied
                    charCountPredicates.add(s -> {
                        int count = StringUtil.count(s, c);
                        if (isCapped) {
                            // Must be exactly right
                            return count == minCount;
                        }
                        return count >= minCount;
                    });
                }
            }
            greenOperatorCounts.add(greenOperatorCount);
        }
        // Largest of the values for each guess (or 0)
        int minOperatorCount = greenOperatorCounts.size() == 0 ? 0 : Collections.max(greenOperatorCounts);
        // TODO: Predicate optimisations/remove them entirely?
        List<Predicate<String>> predicates = getPredicates(new HashSet<>());
        // Must conform to charPossibilities
        predicates.add(s -> {
            for (int i = 0; i < Math.min(charPossibilities.length, s.length()); i++) {
                if (!charPossibilities[i].contains(String.valueOf(s.charAt(i)))) {
                    return false;
                }
            }
            return true;
        });
        predicates.addAll(charCountPredicates);
        // Iterate through each equals position
        List<String> possibilities = new ArrayList<>();
        for (int equalsPos = 0; equalsPos < charPossibilities.length; equalsPos++) {
            LOGGER.log("Equals position: " + equalsPos, 2);
            // Continue if this index can't be an equals sign
            if (!charPossibilities[equalsPos].contains("=")) {
                continue;
            }
            // Operator configurations
            String[] configs = generateOperatorConfigurations(new int[0], equalsPos);
            configLoop:
            for (int i = 0; i < configs.length; i++) {
                String config = configs[i];
                LOGGER.log(String.format("Config %s of %s: %s", i + 1, configs.length, config), 2);
                // Continue if it doesn't have enough operators;
                if (StringUtil.count(config, '+') < minOperatorCount) {
                    continue;
                }
                // Get operator positions
                List<Integer> operatorPositions = new ArrayList<>();
                for (int j = 0; j < config.length(); j++) {
                    if (config.charAt(j) == '+') {
                        if (j != 0 && j < equalsPos) { // Checks will throw ArrayIndexOutOfBoundsException otherwise
                            // Continue if there are multiple operators next to each other
                            if (config.charAt(j - 1) == '+' || config.charAt(j + 1) == '+') {
                                continue configLoop;
                            }
                        }
                        // Continue if this index can't be an operator
                        if (charPossibilities[j].replaceAll("[1234567890]", "").length() == 0) {
                            continue configLoop;
                        }
                        operatorPositions.add(j);
                    }
                }
                // Iterate through operators for each position
                int numOperators = operatorPositions.size();
                // TODO: Is the loop system completely unnecessary without concurrency?
                Loop[] loops = new Loop[numOperators + 1];
                // First loops should just add another operator to the array and call the next loop
                for (int j = 0; j < loops.length - 1; j++) {
                    final int j_ = j;
                    loops[j] = previousOperators -> {
                        char[] newArgs = new char[previousOperators.length + 1];
                        System.arraycopy(previousOperators, 0, newArgs, 0, previousOperators.length);
                        String allowedOperators = StringUtil.filterAllowed(charPossibilities[operatorPositions.get(j_)], "+-*/");
                        for (int k = 0; k < allowedOperators.length(); k++) {
                            newArgs[newArgs.length - 1] = allowedOperators.charAt(k);
                            loops[j_ + 1].run(newArgs);
                        }
                    };
                }
                // Last loop receives the complete array of operators, checks it, then does the final iteration
                final int equalsPos_ = equalsPos;
                loops[loops.length - 1] = operators -> {
                    String[] charSets = new String[equalsPos_]; // Only need charSets for indices before the equals sign - the result is calculated afterwards to minimise iteration
                    int[] maximums = new int[charSets.length]; // The size of the charSet for each index
                    // Modify charSets according to where operators are
                    int operatorsPlaced = 0;
                    for (int k = 0; k < charSets.length; k++) {
                        if (operatorPositions.contains(k)) {
                            // Must be the operator
                            charSets[k] = String.valueOf(operators[operatorsPlaced]);
                            operatorsPlaced++;
                        } else {
                            // Can't be an operator
                            String charSet = StringUtil.filterDisallowed(charPossibilities[k], "+-*/=");
                            if (operatorPositions.contains(k - 1)) {
                                // Can't be a leading zero after an operator
                                charSet = charSet.replace("0", "");
                            }
                            charSets[k] = charSet;
                        }
                        maximums[k] = charSets[k].length();
                    }
                    // Return immediately if any of the charSets are empty - no possibilities
                    if (Arrays.stream(maximums).anyMatch(n -> n == 0)) {
                        return;
                    }
                    // Assemble configuration with operators
                    String operatorConfig = Arrays.stream(charSets).map(s -> s.substring(0, 1)).collect(Collectors.joining()) + "=" + "1".repeat(Config.CONFIG.length - equalsPos_ - 1);
                    if (Operator.isConfigImpossible(operatorConfig)) {
                        LOGGER.log("Skipped " + operatorConfig, 3);
                        return;
                    }
                    LOGGER.log("ITERATING " + operatorConfig, 2);
                    Counter counter = new Counter(maximums);
                    while (counter.isRunning()) {
                        String left = counter.substitute(charSets); // Before equals sign
                        String right = MathUtil.truncateDecimals(String.valueOf(evaluate(left))); // After equals sign
                        String possibility = left + "=" + right;
                        // Check predicates
                        boolean succeededPredicates = true;
                        for (Predicate<String> predicate : predicates) {
                            if (!predicate.test(possibility)) {
                                succeededPredicates = false;
                            }
                        }
                        if (succeededPredicates) {
                            possibilities.add(possibility);
                        }
                        counter.increment();
                    }
                };
                loops[0].run();
                LOGGER.log("Finished config %s of %s: %s".formatted(i + 1, configs.length, config), 2);
            }
        }
        LOGGER.log("Collected " + possibilities.size() + " possibilities", 2);
        // Sort and return
        ThreadPoolExecutor sortingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        List<Future<Map.Entry<String, Double>>> entropyFutures = new ArrayList<>();
        for (String possibility : possibilities) {
            entropyFutures.add(sortingExecutor.submit(() -> Map.entry(possibility, getEntropy(possibility, possibilities))));
        }
        LOGGER.log("Submitted sorting tasks", 2);
        LOGGER.log("Collecting...", 2);
        OrderedMap<String, Double> entropyMap = new OrderedMap<>(OrderedMap.reverseComparator(Comparator.comparingDouble(Map.Entry::getValue)));
        entropyFutures.forEach(f -> {
            try {
                Map.Entry<String, Double> entry = f.get();
                entropyMap.put(entry);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        sortingExecutor.shutdown();
        LOGGER.log("Collected entropies", 2);
        return entropyMap.sortedEntryList();
    }

    public static String[] generateOperatorConfigurations(int[] given, int equalsPos) {
        if (given.length == 0) {
            return generateOperatorConfigurations(new int[]{-1}, equalsPos);
        } else {
            List<String> configs = new ArrayList<>();
            // No more operators
            if (given.length != 1) {
                char[] charArray = new char[CONFIG.length];
                Arrays.fill(charArray, '1');
                charArray[equalsPos] = '=';
                for (int i = 1; i < given.length; i++) {
                    charArray[given[i]] = '+';
                }
                configs.add(new String(charArray));
            }
            // An operator in each other valid position
            for (int i = given[given.length - 1] + 2; i < equalsPos - 1; i++) {
                int[] newGiven = new int[given.length + 1];
                System.arraycopy(given, 0, newGiven, 0, given.length);
                newGiven[newGiven.length - 1] = i;
                configs.addAll(Arrays.asList(generateOperatorConfigurations(newGiven, equalsPos)));
            }
            return configs.toArray(new String[0]);
        }
    }

    public static List<Predicate<String>> getPredicates(Set<Character> mustContain) {
        List<Predicate<String>> predicates = new ArrayList<>();
        // Correct length
        predicates.add(s -> s.length() == CONFIG.length);
        // Must contain characters
        predicates.add(s -> {
            for (char c : mustContain) {
                if (!s.contains(String.valueOf(c))) {
                    return false;
                }
            }
            return true;
        });
        // Negative result
        predicates.add(s -> s.charAt(s.indexOf('=') + 1) != '-');
        // Zeros
        predicates.add(s -> {
            for (int i = 1; i < s.length() - 1; i++) {
                if (s.charAt(i) == '0' && "+-*/=".contains(String.valueOf(s.charAt(i - 1)))) {
                    return false;
                }
            }
            return true;
        });
        // Decimal result
        predicates.add(s -> !s.contains("."));
        return predicates;
    }

    public static String[] getBaseCharPossibilities() {
        String[] charSets = new String[CONFIG.length];
        for (int i = 0; i < charSets.length; i++) {
            if (i == 0) {
                charSets[i] = "123456789";
            } else if (i == CONFIG.length - 1) {
                charSets[i] = "0123456789";
            } else {
                charSets[i] = "0123456789+-*/=";
            }
        }
        return charSets;
    }

    public interface Loop {

        void run(char... previousOperators);
    }
}
