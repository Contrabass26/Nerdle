package main.java.compute;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PossibilityComparator implements Comparator<String> {

    private final List<String> possibilities;
    private final Map<String, Double> entropyMap = new HashMap<>();

    public PossibilityComparator(List<String> possibilities) {
        this.possibilities = possibilities;
    }

    private double evaluate(String possibility) {
        Double fromMap = entropyMap.get(possibility);
        if (fromMap != null) {
            return fromMap;
        }
        double entropy = Computer.getEntropy(possibility, possibilities);
        entropyMap.put(possibility, entropy);
        return entropy;
    }

    @Override
    public int compare(String o1, String o2) {
        return Double.compare(evaluate(o2), evaluate(o1));
    }
}
