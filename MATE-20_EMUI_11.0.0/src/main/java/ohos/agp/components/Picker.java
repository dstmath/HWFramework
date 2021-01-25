package ohos.agp.components;

import java.util.Objects;
import ohos.agp.components.element.Element;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.PickerAttrsConstants;
import ohos.agp.utils.Color;
import ohos.app.Context;

public class Picker extends DirectionalLayout {
    private Element mDisplayedBottomElement;
    private Element mDisplayedTopElement;
    private ElementFormatter mElementFormatter;
    private Formatter mFormatter;
    private ScrolledListener mScrolledListener;
    private ValueChangedListener mValueChangedListener;

    public interface ElementFormatter {
        Element leftElement(int i);

        Element rightElement(int i);
    }

    public interface Formatter {
        String format(int i);
    }

    public interface ScrolledListener {
        public static final int SCROLL_STATE_FLING = 2;
        public static final int SCROLL_STATE_IDLE = 0;
        public static final int SCROLL_STATE_TOUCH_SCROLL = 1;

        void onScrollStateChanged(Picker picker, int i);
    }

    public interface ValueChangedListener {
        void onValueChange(Picker picker, int i, int i2);
    }

    private native int nativeGetCompoundDrawablesPadding(long j);

    private native String[] nativeGetDisplayedValues(long j);

    private native int nativeGetMaxValue(long j);

    private native int nativeGetMinValue(long j);

    private native int nativeGetNormalTextColor(long j);

    private native int nativeGetNormalTextSize(long j);

    private native long nativeGetNumberPickerHandle();

    private native float nativeGetSelectedNormalTextMarginRatio(long j);

    private native int nativeGetSelectedTextColor(long j);

    private native int nativeGetSelectedTextSize(long j);

    private native int nativeGetSelectorWheelItemNum(long j);

    private native int nativeGetShaderColor(long j);

    private native int nativeGetValue(long j);

    private native boolean nativeGetWrapSelectorWheel(long j);

    private native void nativeSetCompoundDrawablesPadding(long j, int i);

    private native void nativeSetDisplayedLinesDrawables(long j, long j2, long j3);

    private native void nativeSetDisplayedValues(long j, String[] strArr);

    private native void nativeSetDrawablesFormatter(long j, ElementFormatter elementFormatter);

    private native void nativeSetFormatter(long j, Formatter formatter);

    private native void nativeSetMaxValue(long j, int i);

    private native void nativeSetMinValue(long j, int i);

    private native void nativeSetNormalTextColor(long j, int i);

    private native void nativeSetNormalTextSize(long j, int i);

    private native void nativeSetOnScrollListener(long j, ScrolledListener scrolledListener);

    private native void nativeSetOnValueChangedListener(long j, ValueChangedListener valueChangedListener);

    private native void nativeSetSelectedNormalTextMarginRatio(long j, float f);

    private native void nativeSetSelectedTextColor(long j, int i);

    private native void nativeSetSelectedTextSize(long j, int i);

    private native void nativeSetSelectorWheelItemNum(long j, int i);

    private native void nativeSetShaderColor(long j, int i);

    private native void nativeSetValue(long j, int i);

    private native boolean nativeSetWrapSelectorWheel(long j, boolean z);

    public Picker(Context context) {
        this(context, null);
    }

    public Picker(Context context, AttrSet attrSet) {
        this(context, attrSet, "NumberPickerDefaultStyle");
    }

    public Picker(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.LinearLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        this.mNativeViewPtr = nativeGetNumberPickerHandle();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.DirectionalLayout, ohos.agp.components.LinearLayout, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new PickerAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style.hasProperty("top_line_element")) {
            setDisplayedLinesElements(style.getPropertyValue("top_line_element").asElement(), getDisplayedBottomElement());
        }
        if (style.hasProperty("bottom_line_element")) {
            setDisplayedLinesElements(getDisplayedTopElement(), style.getPropertyValue("bottom_line_element").asElement());
        }
    }

    public void setSelectorWheelItemNum(int i) {
        if (i > 0) {
            nativeSetSelectorWheelItemNum(this.mNativeViewPtr, i);
            return;
        }
        throw new IllegalArgumentException("selectorWheelItemCnt must be > 0");
    }

    public void setValue(int i) {
        nativeSetValue(this.mNativeViewPtr, i);
    }

    public void setMaxValue(int i) {
        if (i >= 0) {
            nativeSetMaxValue(this.mNativeViewPtr, i);
        }
    }

    public void setMinValue(int i) {
        if (i >= 0) {
            nativeSetMinValue(this.mNativeViewPtr, i);
        }
    }

    public void setNormalTextSize(int i) {
        nativeSetNormalTextSize(this.mNativeViewPtr, i);
    }

    public void setSelectedTextSize(int i) {
        nativeSetSelectedTextSize(this.mNativeViewPtr, i);
    }

    public void setNormalTextColor(Color color) {
        nativeSetNormalTextColor(this.mNativeViewPtr, color.getValue());
    }

    public void setSelectedTextColor(Color color) {
        nativeSetSelectedTextColor(this.mNativeViewPtr, color.getValue());
    }

    public void setSelectedNormalTextMarginRatio(float f) {
        nativeSetSelectedNormalTextMarginRatio(this.mNativeViewPtr, f);
    }

    public void setShaderColor(Color color) {
        nativeSetShaderColor(this.mNativeViewPtr, color.getValue());
    }

    public boolean setWrapSelectorWheel(boolean z) {
        return nativeSetWrapSelectorWheel(this.mNativeViewPtr, z);
    }

    public void setDisplayedValues(String[] strArr) {
        nativeSetDisplayedValues(this.mNativeViewPtr, strArr);
    }

    public void setFormatter(Formatter formatter) {
        if (!Objects.equals(formatter, this.mFormatter)) {
            this.mFormatter = formatter;
            nativeSetFormatter(this.mNativeViewPtr, formatter);
        }
    }

    public void setDisplayedLinesElements(Element element, Element element2) {
        long j;
        this.mDisplayedTopElement = element;
        this.mDisplayedBottomElement = element2;
        long j2 = this.mNativeViewPtr;
        if (element == null) {
            j = 0;
        } else {
            j = element.getNativeElementPtr();
        }
        nativeSetDisplayedLinesDrawables(j2, j, element2 == null ? 0 : element2.getNativeElementPtr());
    }

    public void setElementFormatter(ElementFormatter elementFormatter) {
        if (!Objects.equals(elementFormatter, this.mElementFormatter)) {
            this.mElementFormatter = elementFormatter;
            nativeSetDrawablesFormatter(this.mNativeViewPtr, elementFormatter);
        }
    }

    public void setCompoundElementPadding(int i) {
        nativeSetCompoundDrawablesPadding(this.mNativeViewPtr, i);
    }

    public void setValueChangedListener(ValueChangedListener valueChangedListener) {
        this.mValueChangedListener = valueChangedListener;
        nativeSetOnValueChangedListener(this.mNativeViewPtr, valueChangedListener);
    }

    public void setScrollListener(ScrolledListener scrolledListener) {
        this.mScrolledListener = scrolledListener;
        nativeSetOnScrollListener(this.mNativeViewPtr, scrolledListener);
    }

    public int getSelectorWheelItemNum() {
        return nativeGetSelectorWheelItemNum(this.mNativeViewPtr);
    }

    public int getValue() {
        return nativeGetValue(this.mNativeViewPtr);
    }

    public int getMaxValue() {
        return nativeGetMaxValue(this.mNativeViewPtr);
    }

    public int getMinValue() {
        return nativeGetMinValue(this.mNativeViewPtr);
    }

    public int getNormalTextSize() {
        return nativeGetNormalTextSize(this.mNativeViewPtr);
    }

    public int getSelectedTextSize() {
        return nativeGetSelectedTextSize(this.mNativeViewPtr);
    }

    public Color getNormalTextColor() {
        return new Color(nativeGetNormalTextColor(this.mNativeViewPtr));
    }

    public Color getSelectedTextColor() {
        return new Color(nativeGetSelectedTextColor(this.mNativeViewPtr));
    }

    public float getSelectedNormalTextMarginRatio() {
        return nativeGetSelectedNormalTextMarginRatio(this.mNativeViewPtr);
    }

    public Color getShaderColor() {
        return new Color(nativeGetShaderColor(this.mNativeViewPtr));
    }

    public boolean getWrapSelectorWheel() {
        return nativeGetWrapSelectorWheel(this.mNativeViewPtr);
    }

    public String[] getDisplayedValues() {
        return nativeGetDisplayedValues(this.mNativeViewPtr);
    }

    public Element getDisplayedTopElement() {
        return this.mDisplayedTopElement;
    }

    public Element getDisplayedBottomElement() {
        return this.mDisplayedBottomElement;
    }

    public int getCompoundElementPadding() {
        return nativeGetCompoundDrawablesPadding(this.mNativeViewPtr);
    }
}
