package sun.misc;

public class FloatConsts {
    static final /* synthetic */ boolean -assertionsDisabled = (FloatConsts.class.desiredAssertionStatus() ^ 1);
    public static final int EXP_BIAS = 127;
    public static final int EXP_BIT_MASK = 2139095040;
    public static final int MAX_EXPONENT = 127;
    public static final float MAX_VALUE = Float.MAX_VALUE;
    public static final int MIN_EXPONENT = -126;
    public static final float MIN_NORMAL = Float.MIN_NORMAL;
    public static final int MIN_SUB_EXPONENT = -149;
    public static final float MIN_VALUE = Float.MIN_VALUE;
    public static final float NEGATIVE_INFINITY = Float.NEGATIVE_INFINITY;
    public static final float NaN = Float.NaN;
    public static final float POSITIVE_INFINITY = Float.POSITIVE_INFINITY;
    public static final int SIGNIFICAND_WIDTH = 24;
    public static final int SIGNIF_BIT_MASK = 8388607;
    public static final int SIGN_BIT_MASK = Integer.MIN_VALUE;

    private FloatConsts() {
    }

    static {
        boolean z = -assertionsDisabled;
    }
}
