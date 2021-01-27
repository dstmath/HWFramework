package ohos.global.icu.number;

public class IntegerWidth {
    static final IntegerWidth DEFAULT = new IntegerWidth(1, -1);
    final int maxInt;
    final int minInt;

    private IntegerWidth(int i, int i2) {
        this.minInt = i;
        this.maxInt = i2;
    }

    public static IntegerWidth zeroFillTo(int i) {
        if (i == 1) {
            return DEFAULT;
        }
        if (i >= 0 && i <= 999) {
            return new IntegerWidth(i, -1);
        }
        throw new IllegalArgumentException("Integer digits must be between 0 and 999 (inclusive)");
    }

    public IntegerWidth truncateAt(int i) {
        int i2;
        if (i == this.maxInt) {
            return this;
        }
        if (i >= 0 && i <= 999 && i >= (i2 = this.minInt)) {
            return new IntegerWidth(i2, i);
        }
        if (this.minInt == 1 && i == -1) {
            return DEFAULT;
        }
        if (i == -1) {
            return new IntegerWidth(this.minInt, -1);
        }
        throw new IllegalArgumentException("Integer digits must be between -1 and 999 (inclusive)");
    }
}
