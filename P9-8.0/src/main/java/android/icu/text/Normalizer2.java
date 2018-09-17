package android.icu.text;

import android.icu.impl.ICUBinary;
import android.icu.impl.Norm2AllModes;
import android.icu.text.Normalizer.QuickCheckResult;
import android.icu.util.ICUUncheckedIOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public abstract class Normalizer2 {
    private static final /* synthetic */ int[] -android-icu-text-Normalizer2$ModeSwitchesValues = null;

    public enum Mode {
        COMPOSE,
        DECOMPOSE,
        FCD,
        COMPOSE_CONTIGUOUS
    }

    private static /* synthetic */ int[] -getandroid-icu-text-Normalizer2$ModeSwitchesValues() {
        if (-android-icu-text-Normalizer2$ModeSwitchesValues != null) {
            return -android-icu-text-Normalizer2$ModeSwitchesValues;
        }
        int[] iArr = new int[Mode.values().length];
        try {
            iArr[Mode.COMPOSE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Mode.COMPOSE_CONTIGUOUS.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Mode.DECOMPOSE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Mode.FCD.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -android-icu-text-Normalizer2$ModeSwitchesValues = iArr;
        return iArr;
    }

    public abstract StringBuilder append(StringBuilder stringBuilder, CharSequence charSequence);

    public abstract String getDecomposition(int i);

    public abstract boolean hasBoundaryAfter(int i);

    public abstract boolean hasBoundaryBefore(int i);

    public abstract boolean isInert(int i);

    public abstract boolean isNormalized(CharSequence charSequence);

    public abstract Appendable normalize(CharSequence charSequence, Appendable appendable);

    public abstract StringBuilder normalize(CharSequence charSequence, StringBuilder stringBuilder);

    public abstract StringBuilder normalizeSecondAndAppend(StringBuilder stringBuilder, CharSequence charSequence);

    public abstract QuickCheckResult quickCheck(CharSequence charSequence);

    public abstract int spanQuickCheckYes(CharSequence charSequence);

    public static Normalizer2 getNFCInstance() {
        return Norm2AllModes.getNFCInstance().comp;
    }

    public static Normalizer2 getNFDInstance() {
        return Norm2AllModes.getNFCInstance().decomp;
    }

    public static Normalizer2 getNFKCInstance() {
        return Norm2AllModes.getNFKCInstance().comp;
    }

    public static Normalizer2 getNFKDInstance() {
        return Norm2AllModes.getNFKCInstance().decomp;
    }

    public static Normalizer2 getNFKCCasefoldInstance() {
        return Norm2AllModes.getNFKC_CFInstance().comp;
    }

    public static Normalizer2 getInstance(InputStream data, String name, Mode mode) {
        ByteBuffer bytes = null;
        if (data != null) {
            try {
                bytes = ICUBinary.getByteBufferFromInputStreamAndCloseStream(data);
            } catch (Throwable e) {
                throw new ICUUncheckedIOException(e);
            }
        }
        Norm2AllModes all2Modes = Norm2AllModes.getInstance(bytes, name);
        switch (-getandroid-icu-text-Normalizer2$ModeSwitchesValues()[mode.ordinal()]) {
            case 1:
                return all2Modes.comp;
            case 2:
                return all2Modes.fcc;
            case 3:
                return all2Modes.decomp;
            case 4:
                return all2Modes.fcd;
            default:
                return null;
        }
    }

    public String normalize(CharSequence src) {
        if (!(src instanceof String)) {
            return normalize(src, new StringBuilder(src.length())).toString();
        }
        int spanLength = spanQuickCheckYes(src);
        if (spanLength == src.length()) {
            return (String) src;
        }
        return normalizeSecondAndAppend(new StringBuilder(src.length()).append(src, 0, spanLength), src.subSequence(spanLength, src.length())).toString();
    }

    public String getRawDecomposition(int c) {
        return null;
    }

    public int composePair(int a, int b) {
        return -1;
    }

    public int getCombiningClass(int c) {
        return 0;
    }

    @Deprecated
    protected Normalizer2() {
    }
}
