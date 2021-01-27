package ohos.agp.components;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import ohos.agp.styles.Style;
import ohos.app.Context;

public class TickTimer extends Text {
    private static final String DEFAULT_TIME_FORMAT = "mm:ss";
    private Localization mLocalization;
    protected TickListener mTickListener;
    private String mTimeFormat;

    /* access modifiers changed from: private */
    public interface Localization {
        void simpleDateFormat(long j);
    }

    public interface TickListener {
        void onTickTimerUpdate(TickTimer tickTimer);
    }

    private native String nativeChronometerGetFormat(long j);

    private native boolean nativeChronometerIsCountDown(long j);

    private native void nativeChronometerSetBase(long j, long j2);

    private native void nativeChronometerSetCountDown(long j, boolean z);

    private native void nativeChronometerSetFormat(long j, String str);

    private native void nativeChronometerSetTickCallback(long j, TickListener tickListener);

    private native void nativeChronometerStart(long j);

    private native void nativeChronometerStop(long j);

    private native long nativeGetChronometerHandle();

    private native void nativeSetCurrentTime(long j, String str);

    private native void nativeSetSimpleDateFormat(long j);

    public TickTimer(Context context) {
        this(context, null);
    }

    public TickTimer(Context context, AttrSet attrSet) {
        this(context, attrSet, "TickTimerDefaultStyle");
    }

    public TickTimer(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mTimeFormat = DEFAULT_TIME_FORMAT;
        this.mTickListener = null;
        this.mLocalization = new Localization() {
            /* class ohos.agp.components.$$Lambda$TickTimer$D6Zz1yxMgRqnEizluh1aUw2aZg */

            @Override // ohos.agp.components.TickTimer.Localization
            public final void simpleDateFormat(long j) {
                TickTimer.this.lambda$new$1$TickTimer(j);
            }
        };
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Text, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getChronometerAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Text, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetChronometerHandle();
        }
        if (this.mNativeViewPtr != 0) {
            nativeChronometerSetFormat(this.mNativeViewPtr, DEFAULT_TIME_FORMAT);
            nativeSetSimpleDateFormat(this.mNativeViewPtr);
        }
    }

    static /* synthetic */ boolean lambda$setBaseTime$0(Long l) {
        return l.longValue() >= 0;
    }

    public void setBaseTime(long j) {
        validateParam(Long.valueOf(j), $$Lambda$TickTimer$BQ_klZDyO2v8vR9vZJ2F94LBIa4.INSTANCE, "base time must be greater than 0");
        nativeChronometerSetBase(this.mNativeViewPtr, j);
    }

    public void setCountDown(boolean z) {
        nativeChronometerSetCountDown(this.mNativeViewPtr, z);
    }

    public boolean isCountDown() {
        return nativeChronometerIsCountDown(this.mNativeViewPtr);
    }

    public void setFormat(String str) {
        if (str == null) {
            str = DEFAULT_TIME_FORMAT;
        }
        this.mTimeFormat = str;
        nativeChronometerSetFormat(this.mNativeViewPtr, this.mTimeFormat);
    }

    public String getFormat() {
        return nativeChronometerGetFormat(this.mNativeViewPtr);
    }

    public void start() {
        nativeChronometerStart(this.mNativeViewPtr);
    }

    public void stop() {
        nativeChronometerStop(this.mNativeViewPtr);
    }

    public void setTickListener(TickListener tickListener) {
        this.mTickListener = tickListener;
        nativeChronometerSetTickCallback(this.mNativeViewPtr, this.mTickListener);
    }

    public /* synthetic */ void lambda$new$1$TickTimer(long j) {
        nativeSetCurrentTime(this.mNativeViewPtr, new SimpleDateFormat(this.mTimeFormat, Locale.getDefault(Locale.Category.FORMAT)).format(new Date(j)));
    }
}
