package ohos.agp.components;

import ohos.agp.components.Component;
import ohos.agp.components.element.Element;
import ohos.app.Context;

public class Slider extends AbsSlider {
    private Component.ClickedListener mDecreaseClickListener;
    private Element mDecreaseElement;
    private Component.ClickedListener mIncreaseClickListener;
    private Element mIncreaseElement;
    protected ValueChangedListener mValueChangedListener;

    public interface ValueChangedListener {
        void onProgressChanged(Slider slider, int i, boolean z);

        void onStartTrackingTouch(Slider slider);

        void onStopTrackingTouch(Slider slider);
    }

    private native long nativeGetSliderHandle();

    private native void nativeSetDecreaseButton(long j, long j2, Component.ClickedListener clickedListener);

    private native void nativeSetIncreaseButton(long j, long j2, Component.ClickedListener clickedListener);

    private native void nativeSetValueChangedListener(long j, ValueChangedListener valueChangedListener);

    public Slider(Context context) {
        this(context, null);
    }

    public Slider(Context context, AttrSet attrSet) {
        this(context, attrSet, "SeekBarDefaultStyle");
    }

    public Slider(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mValueChangedListener = null;
        this.mIncreaseElement = null;
        this.mDecreaseElement = null;
        this.mIncreaseClickListener = null;
        this.mDecreaseClickListener = null;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ProgressBar, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetSliderHandle();
        }
    }

    public void setValueChangedListener(ValueChangedListener valueChangedListener) {
        this.mValueChangedListener = valueChangedListener;
        nativeSetValueChangedListener(this.mNativeViewPtr, valueChangedListener);
    }

    public void setIncreaseButton(Element element, Component.ClickedListener clickedListener) {
        this.mIncreaseElement = element;
        this.mIncreaseClickListener = clickedListener;
        nativeSetIncreaseButton(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr(), clickedListener);
    }

    public Element getIncreaseButton() {
        return this.mIncreaseElement;
    }

    public void setDecreaseButton(Element element, Component.ClickedListener clickedListener) {
        this.mDecreaseElement = element;
        this.mDecreaseClickListener = clickedListener;
        nativeSetDecreaseButton(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr(), clickedListener);
    }

    public Element getDecreaseButton() {
        return this.mDecreaseElement;
    }
}
