package main.java.util;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;

public enum Operator {

    ADDITION(Double::sum, '+', 1),
    SUBTRACTION((a, b) -> a - b, '-', 1),
    MULTIPLICATION((a, b) -> a * b, '*', 0),
    DIVISION((a, b) -> a / b, '/', 0);

    private final DoubleBinaryOperator function;
    private final boolean constructive;
    private final char symbol;
    private final int order;

    Operator(DoubleBinaryOperator function, char symbol, int order) {
        this.function = function;
        this.symbol = symbol;
        this.order = order;
        constructive = function.applyAsDouble(1, 2) > 1;
    }

    public double apply(double a, double b) {
        return function.applyAsDouble(a, b);
    }

    public double getMaxResult(Range range1, Range range2) {
        double o1 = range1.max;
        double o2 = isConstructive() ? range2.max : range2.min;
        return apply(o1, o2);
    }

    public double getMinResult(Range range1, Range range2) {
        double o1 = range1.min;
        double o2 = isConstructive() ? range2.min : range2.max;
        return apply(o1, o2);
    }

    public Range getResultRange(Range range1, Range range2) {
        return new Range(getMinResult(range1, range2), getMaxResult(range1, range2));
    }

    private static int maximise(int length) {
        return Integer.parseInt("9".repeat(length));
    }

    private static int minimise(int length) {
        return Integer.parseInt("1" + "0".repeat(length - 1));
    }

    public boolean isConstructive() {
        return constructive;
    }

    public int getOrder() {
        return order;
    }

    public static Operator get(char symbol) {
        for (Operator operator : Operator.values()) {
            if (operator.symbol == symbol) {
                return operator;
            }
        }
        return null;
    }

    public static boolean isConfigImpossible(String config) {
        // Get position and type of each operator
        OrderedMap<Integer, Operator> operators = new OrderedMap<>((e1, e2) -> e1.getValue().getOrder() != e2.getValue().getOrder() ? Double.compare(e1.getValue().getOrder(), e2.getValue().getOrder()) : Integer.compare(e1.getKey(), e2.getKey()));
        for (int i = 0; i < config.length(); i++) {
            Operator operator = get(config.charAt(i));
            if (operator != null) {
                operators.put(i, operator);
            }
        }
        // Get ranges for each operation
        Map<Integer, Range> replacements = new HashMap<>();
        int lastReplacement = -1;
        for (Map.Entry<Integer, Operator> entry : operators.sortedEntryList()) {
            int index = entry.getKey();
            Operator operator = entry.getValue();
            // First operand
            int lastOperatorIndex = StringUtil.lastIndexOf(config.substring(0, index), "+-*/");
            Range firstRange = replacements.get(lastOperatorIndex);
            if (firstRange == null) {
                int firstOperandLength = index - lastOperatorIndex - 1;
                firstRange = new Range(minimise(firstOperandLength), maximise(firstOperandLength));
            }
            // Second operand
            int nextOperatorIndex = StringUtil.indexOf(config.substring(index + 1), "+-*/=") + index + 1;
            Range secondRange = replacements.get(nextOperatorIndex);
            if (secondRange == null) {
                int secondOperandLength = nextOperatorIndex - index - 1;
                secondRange = new Range(minimise(secondOperandLength), maximise(secondOperandLength));
            }
            // Result range
            Range resultRange = operator.getResultRange(firstRange, secondRange);
            replacements.put(index, resultRange);
            lastReplacement = index;
        }
        Range leftRange = replacements.get(lastReplacement);
        if (leftRange == null) return true;
        int rightLength = config.length() - config.indexOf('=') - 1;
        Range rightRange = new Range(minimise(rightLength), maximise(rightLength));
        return !leftRange.intersects(rightRange);
    }

    @Override
    public String toString() {
        return String.valueOf(symbol);
    }

    private record Range(double min, double max) implements Iterable<Double> {

        public boolean contains(double d) {
            return d >= min && d <= max;
        }

        public boolean intersects(Range other) {
            return contains(other.min) || contains(other.max);
        }

        @Override
        public String toString() {
            return "Range{%s to %s}".formatted(min, max);
        }

        @NotNull
        @Override
        public Iterator<Double> iterator() {
            return new Iterator<>() {
                private double next = min;

                @Override
                public boolean hasNext() {
                    return this.next <= max;
                }

                @Override
                public Double next() {
                    next++;
                    return next - 1;
                }
            };
        }
    }
}
