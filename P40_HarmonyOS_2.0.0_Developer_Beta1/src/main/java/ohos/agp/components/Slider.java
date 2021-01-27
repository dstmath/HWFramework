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
        void onProgressUpdated(Slider slider, int i, boolean z);

        void onTouchEnd(Slider slider);

        void onTouchStart(Slider slider);
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

    public ValueChangedListener getValueChangedListener() {
        return this.mValueChangedListener;
    }

    public void setIncreaseButton(Element element, Component.ClickedListener clickedListener) {
        this.mIncreaseElement = element;
        this.mIncreaseClickListener = clickedListener;
        nativeSetIncreaseButton(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr(), clickedListener);
    }

    public void setIncreaseButtonElement(Element element) {
        setIncreaseButton(element, this.mIncreaseClickListener);
    }

    public void setIncreaseButtonClickedListener(Component.ClickedListener clickedListener) {
        setIncreaseButton(this.mIncreaseElement, clickedListener);
    }

    public Component.ClickedListener getIncreaseButtonClickedListener() {
        return this.mIncreaseClickListener;
    }

    public Element getIncreaseButton() {
        return this.mIncreaseElement;
    }

    public void setDecreaseButton(Element element, Component.ClickedListener clickedListener) {
        this.mDecreaseElement = element;
        this.mDecreaseClickListener = clickedListener;
        nativeSetDecreaseButton(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr(), clickedListener);
    }

    public void setDecreaseButtonElement(Element element) {
        setDecreaseButton(element, this.mDecreaseClickListener);
    }

    public void setDecreaseButtonClickedListener(Component.ClickedListener clickedListener) {
        setDecreaseButton(this.mDecreaseElement, clickedListener);
    }

    public Component.ClickedListener getDecreaseButtonClickedListener() {
        return this.mDecreaseClickListener;
    }

    public Element getDecreaseButton() {
        return this.mDecreaseElement;
    }

    public void setAdjustButtons(Element element, Component.ClickedListener clickedListener, Element element2, Component.ClickedListener clickedListener2) {
        setIncreaseButton(element, clickedListener);
        setDecreaseButton(element2, clickedListener2);
    }

    public Element[] getAdjustButtonsElements() {
        return new Element[]{this.mIncreaseElement, this.mDecreaseElement};
    }
}
