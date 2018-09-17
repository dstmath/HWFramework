package java.time.format;

public enum SignStyle {
    NORMAL,
    ALWAYS,
    NEVER,
    NOT_NEGATIVE,
    EXCEEDS_PAD;

    boolean parse(boolean positive, boolean strict, boolean fixedWidth) {
        boolean z = true;
        switch (ordinal()) {
            case 0:
                if (positive) {
                    z = strict ^ 1;
                }
                return z;
            case 1:
            case 4:
                return true;
            default:
                if (strict) {
                    z = false;
                } else {
                    z = fixedWidth ^ 1;
                }
                return z;
        }
    }
}
