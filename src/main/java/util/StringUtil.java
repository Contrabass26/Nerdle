package main.java.util;

public final class StringUtil {

    private StringUtil() {}

    public static int count(String s, char c) {
        int count = 0;
        for (char sChar : s.toCharArray()) {
            if (sChar == c) {
                count++;
            }
        }
        return count;
    }

    public static String filterAllowed(String s, String allowed) {
        StringBuilder result = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (allowed.contains(String.valueOf(c))) {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static String filterDisallowed(String s, String disallowed) {
        StringBuilder result = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (!disallowed.contains(String.valueOf(c))) {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static int indexOf(String string, String search) {
        for (int i = 0; i < string.length(); i++) {
            if (search.contains(String.valueOf(string.charAt(i)))) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(String string, String search) {
        for (int i = string.length() - 1; i >= 0; i--) {
            if (search.contains(String.valueOf(string.charAt(i)))) {
                return i;
            }
        }
        return -1;
    }
}
