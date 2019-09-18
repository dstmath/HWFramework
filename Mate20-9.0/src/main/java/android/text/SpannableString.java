package android.text;

public class SpannableString extends SpannableStringInternal implements CharSequence, GetChars, Spannable {
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

    public SpannableString(CharSequence source, boolean ignoreNoCopySpan) {
        super(source, 0, source.length(), ignoreNoCopySpan);
    }

    public SpannableString(CharSequence source) {
        this(source, false);
    }

    private SpannableString(CharSequence source, int start, int end) {
        super(source, start, end, false);
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
