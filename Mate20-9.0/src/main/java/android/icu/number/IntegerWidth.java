package android.icu.number;

public class IntegerWidth {
    static final IntegerWidth DEFAULT = new IntegerWidth(1, -1);
    final int maxInt;
    final int minInt;

    private IntegerWidth(int minInt2, int maxInt2) {
        this.minInt = minInt2;
        this.maxInt = maxInt2;
    }

    public static IntegerWidth zeroFillTo(int minInt2) {
        if (minInt2 == 1) {
            return DEFAULT;
        }
        if (minInt2 >= 0 && minInt2 < 100) {
            return new IntegerWidth(minInt2, -1);
        }
        throw new IllegalArgumentException("Integer digits must be between 0 and 100");
    }

    public IntegerWidth truncateAt(int maxInt2) {
        if (maxInt2 == this.maxInt) {
            return this;
        }
        if (maxInt2 >= 0 && maxInt2 < 100) {
            return new IntegerWidth(this.minInt, maxInt2);
        }
        if (maxInt2 == -1) {
            return new IntegerWidth(this.minInt, maxInt2);
        }
        throw new IllegalArgumentException("Integer digits must be between 0 and 100");
    }
}
