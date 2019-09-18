package android.text;

public final class SpannedString extends SpannableStringInternal implements CharSequence, GetChars, Spanned {
    public /* bridge */ /* synthetic */ boolean equals(Object obj) {
        return super.equals(obj);
    }

    public /* bridge */ /* synthetic */ int getSpanEnd(Object obj) {
        return super.getSpanEnd(obj);
    }

    public /* bridge */ /* synthetic */ int getSpanFlags(Object obj) {
        return super.getSpanFlags(obj);
    }

    public /* bridge */ /* synthetic */ int getSpanStart(Object obj) {
        return super.getSpanStart(obj);
    }

    public /* bridge */ /* synthetic */ Object[] getSpans(int i, int i2, Class cls) {
        return super.getSpans(i, i2, cls);
    }

    public /* bridge */ /* synthetic */ int hashCode() {
        return super.hashCode();
    }

    public /* bridge */ /* synthetic */ int nextSpanTransition(int i, int i2, Class cls) {
        return super.nextSpanTransition(i, i2, cls);
    }

    public /* bridge */ /* synthetic */ void removeSpan(Object obj, int i) {
        super.removeSpan(obj, i);
    }

    public SpannedString(CharSequence source, boolean ignoreNoCopySpan) {
        super(source, 0, source.length(), ignoreNoCopySpan);
    }

    public SpannedString(CharSequence source) {
        this(source, false);
    }

    private SpannedString(CharSequence source, int start, int end) {
        super(source, start, end, false);
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
