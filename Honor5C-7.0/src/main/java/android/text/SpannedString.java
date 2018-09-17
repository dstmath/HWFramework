package android.text;

public final class SpannedString extends SpannableStringInternal implements CharSequence, GetChars, Spanned {
    public /* bridge */ /* synthetic */ boolean equals(Object o) {
        return super.equals(o);
    }

    public /* bridge */ /* synthetic */ int getSpanEnd(Object what) {
        return super.getSpanEnd(what);
    }

    public /* bridge */ /* synthetic */ int getSpanFlags(Object what) {
        return super.getSpanFlags(what);
    }

    public /* bridge */ /* synthetic */ int getSpanStart(Object what) {
        return super.getSpanStart(what);
    }

    public /* bridge */ /* synthetic */ Object[] getSpans(int queryStart, int queryEnd, Class kind) {
        return super.getSpans(queryStart, queryEnd, kind);
    }

    public /* bridge */ /* synthetic */ int hashCode() {
        return super.hashCode();
    }

    public /* bridge */ /* synthetic */ int nextSpanTransition(int start, int limit, Class kind) {
        return super.nextSpanTransition(start, limit, kind);
    }

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
