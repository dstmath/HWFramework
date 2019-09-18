package android.icu.impl.number;

public interface AffixPatternProvider {

    public static final class Flags {
        public static final int NEGATIVE_SUBPATTERN = 512;
        public static final int PADDING = 1024;
        public static final int PLURAL_MASK = 255;
        public static final int PREFIX = 256;
    }

    char charAt(int i, int i2);

    boolean containsSymbolType(int i);

    boolean hasCurrencySign();

    boolean hasNegativeSubpattern();

    int length(int i);

    boolean negativeHasMinusSign();

    boolean positiveHasPlusSign();
}
