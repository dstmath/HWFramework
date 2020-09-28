package android.text;

import android.annotation.UnsupportedAppUsage;

public final class SpannedString extends SpannableStringInternal implements CharSequence, GetChars, Spanned {
    @Override // android.text.SpannableStringInternal
    public /* bridge */ /* synthetic */ boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // android.text.Spanned, android.text.SpannableStringInternal
    @UnsupportedAppUsage
    public /* bridge */ /* synthetic */ int getSpanEnd(Object obj) {
        return super.getSpanEnd(obj);
    }

    @Override // android.text.Spanned, android.text.SpannableStringInternal
    @UnsupportedAppUsage
    public /* bridge */ /* synthetic */ int getSpanFlags(Object obj) {
        return super.getSpanFlags(obj);
    }

    @Override // android.text.Spanned, android.text.SpannableStringInternal
    @UnsupportedAppUsage
    public /* bridge */ /* synthetic */ int getSpanStart(Object obj) {
        return super.getSpanStart(obj);
    }

    @Override // android.text.Spanned, android.text.SpannableStringInternal
    @UnsupportedAppUsage
    public /* bridge */ /* synthetic */ Object[] getSpans(int i, int i2, Class cls) {
        return super.getSpans(i, i2, cls);
    }

    @Override // android.text.SpannableStringInternal
    public /* bridge */ /* synthetic */ int hashCode() {
        return super.hashCode();
    }

    @Override // android.text.Spanned, android.text.SpannableStringInternal
    @UnsupportedAppUsage
    public /* bridge */ /* synthetic */ int nextSpanTransition(int i, int i2, Class cls) {
        return super.nextSpanTransition(i, i2, cls);
    }

    @Override // android.text.SpannableStringInternal
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
