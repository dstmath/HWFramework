package ohos.agp.components;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.element.Element;
import ohos.agp.styles.Style;
import ohos.agp.text.Font;
import ohos.agp.utils.Color;
import ohos.agp.utils.ErrorHandler;
import ohos.agp.utils.TextTool;
import ohos.app.Context;
import ohos.hiviewdfx.HiLogLabel;

public class Picker extends DirectionalLayout {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_COMPONENT");
    private Element mDisplayedBottomElement;
    private Element mDisplayedTopElement;
    private ElementFormatter mElementFormatter;
    private Formatter mFormatter;
    private Font mNormalTextFont;
    private ScrolledListener mScrolledListener;
    private Element mSelectedTextBackgroundElement;
    private Font mSelectedTextFont;
    private ValueChangedListener mValueChangedListener;

    public interface ElementFormatter {
        Element leftElement(int i);

        Element rightElement(int i);
    }

    public interface Formatter {
        String format(int i);
    }

    public interface ScrolledListener {
        public static final int STATE_DRAGGING = 1;
        public static final int STATE_FLING = 2;
        public static final int STATE_HALT = 0;

        void onScrollStateUpdated(Picker picker, int i);
    }

    public interface ValueChangedListener {
        void onValueChanged(Picker picker, int i, int i2);
    }

    private native int nativeGetCompoundDrawablesPadding(long j);

    private native String[] nativeGetDisplayedData(long j);

    private native int nativeGetMaxValue(long j);

    private native int nativeGetMinValue(long j);

    private native int nativeGetNormalTextColor(long j);

    private native int nativeGetNormalTextSize(long j);

    private native long nativeGetNumberPickerHandle();

    private native float nativeGetSelectedNormalTextMarginRatio(long j);

    private native int nativeGetSelectedTextColor(long j);

    private native int nativeGetSelectedTextSize(long j);

    private native int nativeGetSelectorItemNum(long j);

    private native int nativeGetShaderColor(long j);

    private native int nativeGetValue(long j);

    private native boolean nativeIsWheelModeEnabled(long j);

    private native void nativeSetCompoundDrawablesPadding(long j, int i);

    private native void nativeSetDisplayedData(long j, String[] strArr);

    private native void nativeSetDisplayedLinesDrawables(long j, long j2, long j3);

    private native void nativeSetDrawablesFormatter(long j, ElementFormatter elementFormatter);

    private native void nativeSetFormatter(long j, Formatter formatter);

    private native void nativeSetMaxValue(long j, int i);

    private native void nativeSetMinValue(long j, int i);

    private native void nativeSetNormalTextColor(long j, int i);

    private native void nativeSetNormalTextFont(long j, long j2);

    private native void nativeSetNormalTextSize(long j, int i);

    private native void nativeSetOnScrollListener(long j, ScrolledListener scrolledListener);

    private native void nativeSetOnValueChangedListener(long j, ValueChangedListener valueChangedListener);

    private native void nativeSetSelectedNormalTextMarginRatio(long j, float f);

    private native void nativeSetSelectedTextBackground(long j, long j2);

    private native void nativeSetSelectedTextColor(long j, int i);

    private native void nativeSetSelectedTextFont(long j, long j2);

    private native void nativeSetSelectedTextSize(long j, int i);

    private native void nativeSetSelectorItemNum(long j, int i);

    private native void nativeSetShaderColor(long j, int i);

    private native void nativeSetValue(long j, int i);

    private native boolean nativeSetWheelModeEnabled(long j, boolean z);

    public Picker(Context context) {
        this(context, null);
    }

    public Picker(Context context, AttrSet attrSet) {
        this(context, attrSet, "NumberPickerDefaultStyle");
    }

    public Picker(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mNormalTextFont = Font.DEFAULT;
        this.mSelectedTextFont = Font.DEFAULT;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.DirectionalLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        this.mNativeViewPtr = nativeGetNumberPickerHandle();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.DirectionalLayout, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getPickerAttrsConstants();
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
        if (style.hasProperty("selected_text_background")) {
            setSelectedTextBackground(style.getPropertyValue("selected_text_background").asElement());
        }
    }

    static /* synthetic */ boolean lambda$setSelectorItemNum$0(Integer num) {
        return num.intValue() > 0;
    }

    public void setSelectorItemNum(int i) {
        validateParam(Integer.valueOf(i), $$Lambda$Picker$1k41Qi5a_ApZp4tj1bD8WrM6axk.INSTANCE, "setSelectorItemNum must be > 0");
        nativeSetSelectorItemNum(this.mNativeViewPtr, i);
    }

    public void setValue(int i) {
        ErrorHandler.validateParamNonNegative(i);
        nativeSetValue(this.mNativeViewPtr, i);
    }

    public void setMaxValue(int i) {
        ErrorHandler.validateParamNonNegative(i);
        nativeSetMaxValue(this.mNativeViewPtr, i);
    }

    public void setMinValue(int i) {
        ErrorHandler.validateParamNonNegative(i);
        nativeSetMinValue(this.mNativeViewPtr, i);
    }

    public void setNormalTextSize(int i) {
        TextTool.validateTextSizeParam(i);
        nativeSetNormalTextSize(this.mNativeViewPtr, i);
    }

    public void setSelectedTextSize(int i) {
        TextTool.validateTextSizeParam(i);
        nativeSetSelectedTextSize(this.mNativeViewPtr, i);
    }

    public void setNormalTextColor(Color color) {
        ErrorHandler.validateParamNotNull(color);
        nativeSetNormalTextColor(this.mNativeViewPtr, color.getValue());
    }

    public void setSelectedTextColor(Color color) {
        ErrorHandler.validateParamNotNull(color);
        nativeSetSelectedTextColor(this.mNativeViewPtr, color.getValue());
    }

    public void setNormalTextFont(Font font) {
        TextTool.validateFontParam(font);
        if (!font.convertToTypeface().equals(this.mNormalTextFont.convertToTypeface())) {
            nativeSetNormalTextFont(this.mNativeViewPtr, font.convertToTypeface().getNativeTypefacePtr());
            this.mNormalTextFont = font;
        }
    }

    public Font getNormalTextFont() {
        if (this.mNormalTextFont == null) {
            this.mNormalTextFont = Font.DEFAULT;
        }
        return this.mNormalTextFont;
    }

    public void setSelectedTextFont(Font font) {
        TextTool.validateFontParam(font);
        if (!font.convertToTypeface().equals(this.mSelectedTextFont.convertToTypeface())) {
            nativeSetSelectedTextFont(this.mNativeViewPtr, font.convertToTypeface().getNativeTypefacePtr());
            this.mSelectedTextFont = font;
        }
    }

    public Font getSelectedTextFont() {
        if (this.mSelectedTextFont == null) {
            this.mSelectedTextFont = Font.DEFAULT;
        }
        return this.mSelectedTextFont;
    }

    public void setSelectedNormalTextMarginRatio(float f) {
        ErrorHandler.validateParamNonNegative(f);
        nativeSetSelectedNormalTextMarginRatio(this.mNativeViewPtr, f);
    }

    public void setShaderColor(Color color) {
        ErrorHandler.validateParamNotNull(color);
        nativeSetShaderColor(this.mNativeViewPtr, color.getValue());
    }

    public boolean setWheelModeEnabled(boolean z) {
        return nativeSetWheelModeEnabled(this.mNativeViewPtr, z);
    }

    public void setDisplayedData(String[] strArr) {
        nativeSetDisplayedData(this.mNativeViewPtr, strArr);
    }

    public void setFormatter(Formatter formatter) {
        if (!Objects.equals(formatter, this.mFormatter)) {
            this.mFormatter = formatter;
            nativeSetFormatter(this.mNativeViewPtr, formatter);
        }
    }

    public Formatter getFormatter() {
        return this.mFormatter;
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

    public Element[] getDisplayedLinesElements() {
        return new Element[]{this.mDisplayedTopElement, this.mDisplayedBottomElement};
    }

    public void setDisplayedLinesTopElement(Element element) {
        setDisplayedLinesElements(element, getDisplayedBottomElement());
    }

    public void setDisplayedLinesBottomElement(Element element) {
        setDisplayedLinesElements(getDisplayedTopElement(), element);
    }

    public void setElementFormatter(ElementFormatter elementFormatter) {
        if (!Objects.equals(elementFormatter, this.mElementFormatter)) {
            this.mElementFormatter = elementFormatter;
            nativeSetDrawablesFormatter(this.mNativeViewPtr, elementFormatter);
        }
    }

    public ElementFormatter getElementFormatter() {
        return this.mElementFormatter;
    }

    public void setSelectedTextBackground(Element element) {
        if (!Objects.equals(this.mSelectedTextBackgroundElement, element)) {
            this.mSelectedTextBackgroundElement = element;
            nativeSetSelectedTextBackground(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
        }
    }

    public Element getSelectedTextBackgroundElement() {
        return this.mSelectedTextBackgroundElement;
    }

    public void setCompoundElementPadding(int i) {
        nativeSetCompoundDrawablesPadding(this.mNativeViewPtr, i);
    }

    public void setValueChangedListener(ValueChangedListener valueChangedListener) {
        this.mValueChangedListener = valueChangedListener;
        nativeSetOnValueChangedListener(this.mNativeViewPtr, valueChangedListener);
    }

    public ValueChangedListener getValueChangedListener() {
        return this.mValueChangedListener;
    }

    public void setScrollListener(ScrolledListener scrolledListener) {
        this.mScrolledListener = scrolledListener;
        nativeSetOnScrollListener(this.mNativeViewPtr, scrolledListener);
    }

    public ScrolledListener getScrollListener() {
        return this.mScrolledListener;
    }

    public int getSelectorItemNum() {
        return nativeGetSelectorItemNum(this.mNativeViewPtr);
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

    public boolean isWheelModeEnabled() {
        return nativeIsWheelModeEnabled(this.mNativeViewPtr);
    }

    public String[] getDisplayedData() {
        return nativeGetDisplayedData(this.mNativeViewPtr);
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

    private String formatNumber(int i) {
        return NumberFormat.getInstance(Locale.getDefault(Locale.Category.FORMAT)).format((long) i);
    }
}
