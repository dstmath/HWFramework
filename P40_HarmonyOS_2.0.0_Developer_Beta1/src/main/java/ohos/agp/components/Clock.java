package ohos.agp.components;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.styles.Style;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class Clock extends Text {
    private static final int CLOCK_UPDATE_COUNT_LOG = 60;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_Clock");
    private int mClockUpateCount;
    private OnTimeChangedListener mOnTimeChangedListener;

    /* access modifiers changed from: private */
    public interface OnTimeChangedListener {
        void onTimeChanged(Clock clock, long j);
    }

    private native String nativeGetFormat12Hour(long j);

    private native String nativeGetFormat24Hour(long j);

    private native long nativeGetHandle();

    private native long nativeGetTime(long j);

    private native String nativeGetTimeZone(long j);

    private native boolean nativeIs24HourModeEnabled(long j);

    private native void nativeSet24HourModeEnabled(long j, boolean z);

    private native void nativeSetFormat12Hour(long j, String str);

    private native void nativeSetFormat24Hour(long j, String str);

    private native void nativeSetOnTimeChangedListener(long j, OnTimeChangedListener onTimeChangedListener);

    private native void nativeSetTime(long j, long j2);

    private native void nativeSetTimeZone(long j, String str);

    public Clock(Context context) {
        this(context, null);
    }

    public Clock(Context context, AttrSet attrSet) {
        this(context, attrSet, null);
    }

    public Clock(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mClockUpateCount = 0;
        setOnTimeChangedListener(new OnTimeChangedListener() {
            /* class ohos.agp.components.$$Lambda$Clock$9lxjcRw_5P3Ym7sV9uxNuFXQqw */

            @Override // ohos.agp.components.Clock.OnTimeChangedListener
            public final void onTimeChanged(Clock clock, long j) {
                Clock.this.lambda$new$0$Clock(clock, j);
            }
        });
    }

    public /* synthetic */ void lambda$new$0$Clock(Clock clock, long j) {
        CharSequence charSequence;
        if (is24HourMode()) {
            charSequence = Formatter.format(getFormatIn24HourMode(), j * 1000);
        } else {
            charSequence = Formatter.format(getFormatIn12HourMode(), j * 1000);
        }
        this.mClockUpateCount++;
        if (this.mClockUpateCount >= 60) {
            HiLog.debug(TAG, "Time: %{public}s, Count: %{public}d", new Object[]{charSequence.toString(), Integer.valueOf(this.mClockUpateCount)});
            this.mClockUpateCount = 0;
        }
        setText(charSequence.toString());
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Text, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getClockAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Text, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetHandle();
        }
    }

    public void setFormatIn12HourMode(CharSequence charSequence) {
        nativeSetFormat12Hour(this.mNativeViewPtr, charSequence.toString());
    }

    public CharSequence getFormatIn12HourMode() {
        return nativeGetFormat12Hour(this.mNativeViewPtr);
    }

    public void setFormatIn24HourMode(CharSequence charSequence) {
        nativeSetFormat24Hour(this.mNativeViewPtr, charSequence.toString());
    }

    public CharSequence getFormatIn24HourMode() {
        return nativeGetFormat24Hour(this.mNativeViewPtr);
    }

    public void setTimeZone(String str) {
        nativeSetTimeZone(this.mNativeViewPtr, str);
    }

    public String getTimeZone() {
        return nativeGetTimeZone(this.mNativeViewPtr);
    }

    public void setTime(long j) {
        nativeSetTime(this.mNativeViewPtr, j);
    }

    public long getTime() {
        return nativeGetTime(this.mNativeViewPtr);
    }

    public void set24HourModeEnabled(boolean z) {
        nativeSet24HourModeEnabled(this.mNativeViewPtr, z);
    }

    public boolean is24HourMode() {
        return nativeIs24HourModeEnabled(this.mNativeViewPtr);
    }

    private void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        this.mOnTimeChangedListener = onTimeChangedListener;
        nativeSetOnTimeChangedListener(this.mNativeViewPtr, onTimeChangedListener);
    }

    /* access modifiers changed from: private */
    public static class Formatter {
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
            if (length <= i3 || sb.charAt(i3) != '\'') {
                int i4 = 0;
                sb.delete(i, i3);
                int i5 = length - 1;
                while (true) {
                    if (i >= i5) {
                        break;
                    } else if (sb.charAt(i) == '\'') {
                        i2 = i + 1;
                        if (i5 <= i2 || sb.charAt(i2) != '\'') {
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
            return new SimpleDateFormat(sb.toString(), Locale.getDefault(Locale.Category.FORMAT)).format(new Date(j));
        }
    }
}
