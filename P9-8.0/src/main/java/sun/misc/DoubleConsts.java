package sun.misc;

public class DoubleConsts {
    static final /* synthetic */ boolean -assertionsDisabled = (DoubleConsts.class.desiredAssertionStatus() ^ 1);
    public static final int EXP_BIAS = 1023;
    public static final long EXP_BIT_MASK = 9218868437227405312L;
    public static final int MAX_EXPONENT = 1023;
    public static final double MAX_VALUE = Double.MAX_VALUE;
    public static final int MIN_EXPONENT = -1022;
    public static final double MIN_NORMAL = Double.MIN_NORMAL;
    public static final int MIN_SUB_EXPONENT = -1074;
    public static final double MIN_VALUE = Double.MIN_VALUE;
    public static final double NEGATIVE_INFINITY = Double.NEGATIVE_INFINITY;
    public static final double NaN = Double.NaN;
    public static final double POSITIVE_INFINITY = Double.POSITIVE_INFINITY;
    public static final int SIGNIFICAND_WIDTH = 53;
    public static final long SIGNIF_BIT_MASK = 4503599627370495L;
    public static final long SIGN_BIT_MASK = Long.MIN_VALUE;

    private DoubleConsts() {
    }

    static {
        boolean z = -assertionsDisabled;
    }
}
