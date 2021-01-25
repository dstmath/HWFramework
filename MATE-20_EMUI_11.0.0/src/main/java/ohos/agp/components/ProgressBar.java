package ohos.agp.components;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.element.Element;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.ProgressBarAttrsConstants;
import ohos.agp.utils.Color;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ProgressBar extends Component {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_PROGRESSBAR");
    private Element mBackElement;
    private Element mIndeterminateElement;
    private Element mProgressElement;
    private Element mSecondaryProgressElement;

    private native void nativeEnableDividerLines(long j, boolean z);

    private native int nativeGetColor(long j);

    private native int[] nativeGetColors(long j);

    private native int nativeGetDividerLineColor(long j);

    private native int nativeGetDividerLineThickness(long j);

    private native int nativeGetDividerLinesNumber(long j);

    private native String nativeGetHintText(long j);

    private native int nativeGetHintTextColor(long j);

    private native int nativeGetOrientation(long j);

    private native long nativeGetProgressBarHandle();

    private native boolean nativeGetProgressBarIndeterminate(long j);

    private native int nativeGetProgressBarMax(long j);

    private native int nativeGetProgressBarMaxHeight(long j);

    private native int nativeGetProgressBarMaxWidth(long j);

    private native int nativeGetProgressBarMin(long j);

    private native int nativeGetProgressBarProgress(long j);

    private native int nativeGetProgressBarProgressWidth(long j);

    private native int nativeGetProgressBarSecondaryProgress(long j);

    private native int nativeGetProgressBarStep(long j);

    private native int nativeGetTextAlignment(long j);

    private native boolean nativeIsDividerLinesEnabled(long j);

    private native void nativeSetColor(long j, int i);

    private native void nativeSetColors(long j, int[] iArr);

    private native void nativeSetDividerLineColor(long j, int i);

    private native void nativeSetDividerLineThickness(long j, int i);

    private native void nativeSetDividerLinesNumber(long j, int i);

    private native void nativeSetHintText(long j, String str);

    private native void nativeSetHintTextColor(long j, int i);

    private native void nativeSetOrientation(long j, int i);

    private native void nativeSetProgressBackgroundDrawable(long j, long j2);

    private native void nativeSetProgressBarIndeterminate(long j, boolean z);

    private native void nativeSetProgressBarIndeterminateDrawable(long j, long j2);

    private native void nativeSetProgressBarMax(long j, int i);

    private native void nativeSetProgressBarMaxHeight(long j, int i);

    private native void nativeSetProgressBarMaxWidth(long j, int i);

    private native void nativeSetProgressBarMin(long j, int i);

    private native void nativeSetProgressBarProgress(long j, int i);

    private native void nativeSetProgressBarProgressWidth(long j, int i);

    private native void nativeSetProgressBarSecondaryProgress(long j, int i);

    private native void nativeSetProgressBarStep(long j, int i);

    private native void nativeSetProgressDrawable(long j, long j2);

    private native void nativeSetSecondaryProgressDrawable(long j, long j2);

    private native void nativeSetTextAlignment(long j, int i);

    public ProgressBar(Context context) {
        this(context, null);
    }

    public ProgressBar(Context context, AttrSet attrSet) {
        this(context, attrSet, "ProgressBarDefaultStyle");
    }

    public ProgressBar(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new ProgressBarAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetProgressBarHandle();
        }
    }

    public void setOrientation(int i) {
        nativeSetOrientation(this.mNativeViewPtr, i);
    }

    public int getOrientation() {
        return nativeGetOrientation(this.mNativeViewPtr);
    }

    @Override // ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        applyStyleImplementation(style);
    }

    private void applyStyleImplementation(Style style) {
        String[] strArr = {ProgressBarAttrsConstants.INDETERMINATE_ELEMENT, ProgressBarAttrsConstants.PROGRESS_BACKGROUND_ELEMENT, ProgressBarAttrsConstants.PROGRESS_ELEMENT, ProgressBarAttrsConstants.SECONDARY_PROGRESS_ELEMENT};
        for (String str : strArr) {
            if (style.hasProperty(str)) {
                char c = 65535;
                switch (str.hashCode()) {
                    case -853765547:
                        if (str.equals(ProgressBarAttrsConstants.SECONDARY_PROGRESS_ELEMENT)) {
                            c = 3;
                            break;
                        }
                        break;
                    case -528547779:
                        if (str.equals(ProgressBarAttrsConstants.PROGRESS_BACKGROUND_ELEMENT)) {
                            c = 1;
                            break;
                        }
                        break;
                    case 931186552:
                        if (str.equals(ProgressBarAttrsConstants.INDETERMINATE_ELEMENT)) {
                            c = 0;
                            break;
                        }
                        break;
                    case 2008305913:
                        if (str.equals(ProgressBarAttrsConstants.PROGRESS_ELEMENT)) {
                            c = 2;
                            break;
                        }
                        break;
                }
                if (c == 0) {
                    setIndeterminateElement(style.getPropertyValue(str).asElement());
                } else if (c == 1) {
                    setProgressBackgroundElement(style.getPropertyValue(str).asElement());
                } else if (c == 2) {
                    setProgressElement(style.getPropertyValue(str).asElement());
                } else if (c == 3) {
                    setSecondaryProgressElement(style.getPropertyValue(str).asElement());
                }
            }
        }
    }

    public void setMin(int i) {
        nativeSetProgressBarMin(this.mNativeViewPtr, i);
    }

    public void setMax(int i) {
        nativeSetProgressBarMax(this.mNativeViewPtr, i);
    }

    public void setProgress(int i) {
        nativeSetProgressBarProgress(this.mNativeViewPtr, i);
    }

    public void setSecondaryProgress(int i) {
        nativeSetProgressBarSecondaryProgress(this.mNativeViewPtr, i);
    }

    public void setStep(int i) {
        nativeSetProgressBarStep(this.mNativeViewPtr, i);
    }

    public void setIndeterminate(boolean z) {
        nativeSetProgressBarIndeterminate(this.mNativeViewPtr, z);
    }

    @Deprecated
    public void setIndeterminateDrawable(Element element) {
        long j;
        this.mIndeterminateElement = element;
        long j2 = this.mNativeViewPtr;
        if (element == null) {
            j = 0;
        } else {
            j = element.getNativeElementPtr();
        }
        nativeSetProgressBarIndeterminateDrawable(j2, j);
    }

    public void setIndeterminateElement(Element element) {
        long j;
        this.mIndeterminateElement = element;
        long j2 = this.mNativeViewPtr;
        if (element == null) {
            j = 0;
        } else {
            j = element.getNativeElementPtr();
        }
        nativeSetProgressBarIndeterminateDrawable(j2, j);
    }

    public void setMaxWidth(int i) {
        nativeSetProgressBarMaxWidth(this.mNativeViewPtr, i);
    }

    public void setMaxHeight(int i) {
        nativeSetProgressBarMaxHeight(this.mNativeViewPtr, i);
    }

    @Deprecated
    public Element getIndeterminateDrawable() {
        return this.mIndeterminateElement;
    }

    public Element getIndeterminateElement() {
        return this.mIndeterminateElement;
    }

    public int getMin() {
        return nativeGetProgressBarMin(this.mNativeViewPtr);
    }

    public int getMax() {
        return nativeGetProgressBarMax(this.mNativeViewPtr);
    }

    public int getProgress() {
        return nativeGetProgressBarProgress(this.mNativeViewPtr);
    }

    public int getSecondaryProgress() {
        return nativeGetProgressBarSecondaryProgress(this.mNativeViewPtr);
    }

    public int getStep() {
        return nativeGetProgressBarStep(this.mNativeViewPtr);
    }

    public boolean isIndeterminate() {
        return nativeGetProgressBarIndeterminate(this.mNativeViewPtr);
    }

    public int getMaxWidth() {
        return nativeGetProgressBarMaxWidth(this.mNativeViewPtr);
    }

    public int getMaxHeight() {
        return nativeGetProgressBarMaxHeight(this.mNativeViewPtr);
    }

    @Deprecated
    public void setProgressBackgroundDrawable(Element element) {
        this.mBackElement = element;
        nativeSetProgressBackgroundDrawable(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    public void setProgressBackgroundElement(Element element) {
        this.mBackElement = element;
        nativeSetProgressBackgroundDrawable(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    @Deprecated
    public void setProgressDrawable(Element element) {
        this.mProgressElement = element;
        nativeSetProgressDrawable(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    public void setProgressElement(Element element) {
        this.mProgressElement = element;
        nativeSetProgressDrawable(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    @Deprecated
    public void setSecondaryProgressDrawable(Element element) {
        this.mSecondaryProgressElement = element;
        nativeSetSecondaryProgressDrawable(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    public void setSecondaryProgressElement(Element element) {
        this.mSecondaryProgressElement = element;
        nativeSetSecondaryProgressDrawable(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    @Deprecated
    public Element getProgressBackgroundDrawable() {
        return this.mBackElement;
    }

    public Element getProgressBackgroundElement() {
        return this.mBackElement;
    }

    @Deprecated
    public Element getProgressDrawable() {
        return this.mProgressElement;
    }

    public Element getProgressElement() {
        return this.mProgressElement;
    }

    @Deprecated
    public Element getSecondaryProgressDrawable() {
        return this.mSecondaryProgressElement;
    }

    public Element getSecondaryProgressElement() {
        return this.mSecondaryProgressElement;
    }

    public String getProgressHintText() {
        return nativeGetHintText(this.mNativeViewPtr);
    }

    public void setProgressHintText(String str) {
        nativeSetHintText(this.mNativeViewPtr, str);
    }

    public void setProgressHintTextColor(Color color) {
        nativeSetHintTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getProgressHintTextColor() {
        return new Color(nativeGetHintTextColor(this.mNativeViewPtr));
    }

    public void setProgressHintTextAlignment(int i) {
        nativeSetTextAlignment(this.mNativeViewPtr, i);
    }

    public int getProgressHintTextAlignment() {
        return nativeGetTextAlignment(this.mNativeViewPtr);
    }

    public void enableDividerLines(boolean z) {
        nativeEnableDividerLines(this.mNativeViewPtr, z);
    }

    public boolean isDividerLinesEnabled() {
        return nativeIsDividerLinesEnabled(this.mNativeViewPtr);
    }

    public void setDividerLinesNumber(int i) {
        if (i >= 0) {
            nativeSetDividerLinesNumber(this.mNativeViewPtr, i);
        } else {
            HiLog.error(TAG, "setDividerLinesNumber fail! Invalid number.", new Object[0]);
        }
    }

    public int getDividerLinesNumber() {
        return nativeGetDividerLinesNumber(this.mNativeViewPtr);
    }

    public void setDividerLineThickness(int i) {
        nativeSetDividerLineThickness(this.mNativeViewPtr, i);
    }

    public int getDividerLineThickness() {
        return nativeGetDividerLineThickness(this.mNativeViewPtr);
    }

    public void setDividerLineColor(Color color) {
        nativeSetDividerLineColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getDividerLineColor() {
        return new Color(nativeGetDividerLineColor(this.mNativeViewPtr));
    }

    public void setProgressWidth(int i) {
        nativeSetProgressBarProgressWidth(this.mNativeViewPtr, i);
    }

    public int getProgressWidth() {
        return nativeGetProgressBarProgressWidth(this.mNativeViewPtr);
    }

    public void setProgressColor(Color color) {
        nativeSetColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getProgressColor() {
        return new Color(nativeGetColor(this.mNativeViewPtr));
    }

    public void setProgressColors(int[] iArr) {
        nativeSetColors(this.mNativeViewPtr, iArr);
    }

    public int[] getProgressColors() {
        return nativeGetColors(this.mNativeViewPtr);
    }
}
