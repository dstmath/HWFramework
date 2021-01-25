package ohos.agp.components;

import java.text.SimpleDateFormat;
import java.util.Date;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.TextClockAttrsConstants;
import ohos.app.Context;

public class TextClock extends Text {
    private OnTimeChangeListener mOnTimeChangeListener;

    /* access modifiers changed from: private */
    public interface OnTimeChangeListener {
        void onTimeChange(TextClock textClock, long j);
    }

    private native String nativeGetTextClockFormat12Hour(long j);

    private native String nativeGetTextClockFormat24Hour(long j);

    private native long nativeGetTextClockHandle();

    private native long nativeGetTextClockTime(long j);

    private native String nativeGetTextClockTimeZone(long j);

    private native boolean nativeIsTextClock24HourModeEnabled(long j);

    private native void nativeSetOnTimeChangedListener(long j, OnTimeChangeListener onTimeChangeListener);

    private native void nativeSetTextClock24HourModeEnabled(long j, boolean z);

    private native void nativeSetTextClockFormat12Hour(long j, String str);

    private native void nativeSetTextClockFormat24Hour(long j, String str);

    private native void nativeSetTextClockTime(long j, long j2);

    private native void nativeSetTextClockTimeZone(long j, String str);

    public TextClock(Context context) {
        this(context, null);
    }

    public TextClock(Context context, AttrSet attrSet) {
        this(context, attrSet, null);
    }

    public TextClock(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        setOnTimeChangedListener(new OnTimeChangeListener() {
            /* class ohos.agp.components.$$Lambda$TextClock$x_K0xgM39UYaxIHli53ShcWRRCk */

            @Override // ohos.agp.components.TextClock.OnTimeChangeListener
            public final void onTimeChange(TextClock textClock, long j) {
                TextClock.this.lambda$new$0$TextClock(textClock, j);
            }
        });
    }

    public /* synthetic */ void lambda$new$0$TextClock(TextClock textClock, long j) {
        CharSequence charSequence;
        if (is24HourModeEnabled()) {
            charSequence = Formatter.format(getFormat24Hour(), j * 1000);
        } else {
            charSequence = Formatter.format(getFormat12Hour(), j * 1000);
        }
        setText(charSequence.toString());
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Text, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new TextClockAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Text, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetTextClockHandle();
        }
    }

    public void setFormat12Hour(CharSequence charSequence) {
        nativeSetTextClockFormat12Hour(this.mNativeViewPtr, charSequence.toString());
    }

    public CharSequence getFormat12Hour() {
        return nativeGetTextClockFormat12Hour(this.mNativeViewPtr);
    }

    public void setFormat24Hour(CharSequence charSequence) {
        nativeSetTextClockFormat24Hour(this.mNativeViewPtr, charSequence.toString());
    }

    public CharSequence getFormat24Hour() {
        return nativeGetTextClockFormat24Hour(this.mNativeViewPtr);
    }

    public void setTimeZone(String str) {
        nativeSetTextClockTimeZone(this.mNativeViewPtr, str);
    }

    public String getTimeZone() {
        return nativeGetTextClockTimeZone(this.mNativeViewPtr);
    }

    public void setTime(long j) {
        nativeSetTextClockTime(this.mNativeViewPtr, j);
    }

    public long getTime() {
        return nativeGetTextClockTime(this.mNativeViewPtr);
    }

    public void set24HourModeEnabled(boolean z) {
        nativeSetTextClock24HourModeEnabled(this.mNativeViewPtr, z);
    }

    public boolean is24HourModeEnabled() {
        return nativeIsTextClock24HourModeEnabled(this.mNativeViewPtr);
    }

    private void setOnTimeChangedListener(OnTimeChangeListener onTimeChangeListener) {
        this.mOnTimeChangeListener = onTimeChangeListener;
        nativeSetOnTimeChangedListener(this.mNativeViewPtr, onTimeChangeListener);
    }

    private static class Formatter {
        private static final String FORMAT_SYMBOLS = "GyMdhHmsSEDFwWakKz";
        private static final char QUOTE = '\'';

        private Formatter() {
        }

        /* access modifiers changed from: private */
        public static CharSequence format(CharSequence charSequence, long j) {
            int i;
            int i2;
            int i3;
            StringBuilder sb = new StringBuilder(charSequence);
            int length = charSequence.length();
            int i4 = 0;
            while (i4 < length) {
                char charAt = sb.charAt(i4);
                if (charAt == '\'') {
                    i = appendQuotedText(sb, i4);
                    i2 = sb.length();
                } else {
                    int i5 = 1;
                    while (true) {
                        i3 = i4 + i5;
                        if (i3 >= length || sb.charAt(i3) != charAt) {
                            break;
                        }
                        i5++;
                    }
                    if (FORMAT_SYMBOLS.indexOf(charAt) != -1) {
                        String tinyFormat = tinyFormat((char) charAt, i5, j);
                        sb.replace(i4, i3, tinyFormat);
                        i = tinyFormat.length();
                        i2 = sb.length();
                    } else {
                        i2 = length;
                        i = i5;
                    }
                }
                i4 += i;
                length = i2;
            }
            return sb.toString();
        }

        private static int appendQuotedText(StringBuilder sb, int i) {
            int i2;
            int length = sb.length();
            int i3 = i + 1;
            if (i3 >= length || sb.charAt(i3) != '\'') {
                int i4 = 0;
                sb.delete(i, i3);
                int i5 = length - 1;
                while (true) {
                    if (i >= i5) {
                        break;
                    } else if (sb.charAt(i) == '\'') {
                        i2 = i + 1;
                        if (i2 >= i5 || sb.charAt(i2) != '\'') {
                            break;
                        }
                        sb.delete(i, i2);
                        i5--;
                        i4++;
                        i = i2;
                    } else {
                        i++;
                        i4++;
                    }
                }
                sb.delete(i, i2);
                return i4;
            }
            sb.delete(i, i3);
            return 1;
        }

        private static String tinyFormat(char c, int i, long j) {
            StringBuilder sb = new StringBuilder();
            for (int i2 = 0; i2 < i; i2++) {
                sb.append(c);
            }
            return new SimpleDateFormat(sb.toString()).format(new Date(j));
        }
    }
}
