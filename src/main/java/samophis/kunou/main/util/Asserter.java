package samophis.kunou.main.util;

public class Asserter {
    private Asserter() {}
    public static int requireNonNegative(int number) {
        if (number < 0)
            throw new IllegalArgumentException("Number must be positive!");
        return number;
    }
}
