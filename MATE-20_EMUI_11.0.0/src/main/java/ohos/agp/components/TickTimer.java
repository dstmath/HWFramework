package ohos.agp.components;

import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.ChronometerAttrsConstants;
import ohos.app.Context;

public class TickTimer extends Text {
    protected TickListener mTickListener;

    public interface TickListener {
        void onTick(TickTimer tickTimer);
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

    public TickTimer(Context context) {
        this(context, null);
    }

    public TickTimer(Context context, AttrSet attrSet) {
        this(context, attrSet, "TickTimerDefaultStyle");
    }

    public TickTimer(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mTickListener = null;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Text, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new ChronometerAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Text, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetChronometerHandle();
        }
    }

    public void setBase(long j) {
        nativeChronometerSetBase(this.mNativeViewPtr, j);
    }

    public void setCountDown(boolean z) {
        nativeChronometerSetCountDown(this.mNativeViewPtr, z);
    }

    public boolean isCountDown() {
        return nativeChronometerIsCountDown(this.mNativeViewPtr);
    }

    public void setFormat(String str) {
        nativeChronometerSetFormat(this.mNativeViewPtr, str);
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
}
