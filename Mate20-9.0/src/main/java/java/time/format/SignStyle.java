package java.time.format;

public enum SignStyle {
    NORMAL,
    ALWAYS,
    NEVER,
    NOT_NEGATIVE,
    EXCEEDS_PAD;

    /* access modifiers changed from: package-private */
    public boolean parse(boolean positive, boolean strict, boolean fixedWidth) {
        int ordinal = ordinal();
        if (ordinal != 4) {
            boolean z = false;
            switch (ordinal) {
                case 0:
                    if (!positive || !strict) {
                        z = true;
                    }
                    return z;
                case 1:
                    break;
                default:
                    if (!strict && !fixedWidth) {
                        z = true;
                    }
                    return z;
            }
        }
        return true;
    }
}
