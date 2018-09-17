package android.text;

public final class SpannedString extends SpannableStringInternal implements CharSequence, GetChars, Spanned {
    public SpannedString(CharSequence source) {
        super(source, 0, source.length());
    }

    private SpannedString(CharSequence source, int start, int end) {
        super(source, start, end);
    }

    public CharSequence subSequence(int start, int end) {
        return new SpannedString(this, start, end);
    }

    public static SpannedString valueOf(CharSequence source) {
        if (source instanceof SpannedString) {
            return (SpannedString) source;
        }
        return new SpannedString(source);
    }
}
