package ohos.global.icu.text;

public interface UnicodeMatcher {
    public static final char ETHER = 65535;
    public static final int U_MATCH = 2;
    public static final int U_MISMATCH = 0;
    public static final int U_PARTIAL_MATCH = 1;

    void addMatchSetTo(UnicodeSet unicodeSet);

    int matches(Replaceable replaceable, int[] iArr, int i, boolean z);

    boolean matchesIndexValue(int i);

    String toPattern(boolean z);
}
