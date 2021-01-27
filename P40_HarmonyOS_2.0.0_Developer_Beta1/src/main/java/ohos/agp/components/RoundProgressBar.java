package ohos.agp.components;

import ohos.agp.styles.Style;
import ohos.app.Context;

public class RoundProgressBar extends ProgressBar {
    private native float nativeGetMaxAngle(long j);

    private native long nativeGetRoundProgressBarHandle();

    private native float nativeGetStartAngle(long j);

    private native void nativeSetMaxAngle(long j, float f);

    private native void nativeSetStartAngle(long j, float f);

    public RoundProgressBar(Context context) {
        this(context, null);
    }

    public RoundProgressBar(Context context, AttrSet attrSet) {
        this(context, attrSet, "RoundProgressBarDefaultStyle");
    }

    public RoundProgressBar(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ProgressBar, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getRoundProgressBarAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    public void setStartAngle(float f) {
        nativeSetStartAngle(this.mNativeViewPtr, f);
    }

    public float getStartAngle() {
        return nativeGetStartAngle(this.mNativeViewPtr);
    }

    public void setMaxAngle(float f) {
        nativeSetMaxAngle(this.mNativeViewPtr, f);
    }

    public float getMaxAngle() {
        return nativeGetMaxAngle(this.mNativeViewPtr);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ProgressBar, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetRoundProgressBarHandle();
        }
    }
}
