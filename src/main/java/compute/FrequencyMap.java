package main.java.compute;

import java.util.*;

public abstract class FrequencyMap<T, R> {

    private final Map<T, Integer> map = new HashMap<>();

    public FrequencyMap(Collection<R> toIncorporate) {
        toIncorporate.forEach(this::incorporate);
    }

    protected void increment(T category) {
        map.merge(category, 1, Integer::sum);
    }

    public abstract void incorporate(R toIncorporate);

    public Collection<Integer> frequencies() {
        return map.values();
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("{");
        List<T> keySet = new ArrayList<>(map.keySet());
        keySet.sort((o1, o2) -> Integer.compare(map.get(o2), map.get(o1)));
        for (T key : keySet) {
            string.append(String.format("'%s': %s, ", key, map.get(key)));
        }
        return string.substring(0, string.length() - 2) + "}";
    }
}
