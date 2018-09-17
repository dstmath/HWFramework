package android.text;

public class SpannableString extends SpannableStringInternal implements CharSequence, GetChars, Spannable {
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

    public SpannableString(CharSequence source) {
        super(source, 0, source.length());
    }

    private SpannableString(CharSequence source, int start, int end) {
        super(source, start, end);
    }

    public static SpannableString valueOf(CharSequence source) {
        if (source instanceof SpannableString) {
            return (SpannableString) source;
        }
        return new SpannableString(source);
    }

    public void setSpan(Object what, int start, int end, int flags) {
        super.setSpan(what, start, end, flags);
    }

    public void removeSpan(Object what) {
        super.removeSpan(what);
    }

    public final CharSequence subSequence(int start, int end) {
        return new SpannableString(this, start, end);
    }
}
