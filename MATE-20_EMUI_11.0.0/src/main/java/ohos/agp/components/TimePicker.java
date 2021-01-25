package ohos.agp.components;

import ohos.agp.components.element.Element;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.TimePickerAttrsConstants;
import ohos.agp.utils.Color;
import ohos.app.Context;

public class TimePicker extends StackLayout {
    private Element mDisplayedTPBottomElement;
    private Element mDisplayedTPTopElement;
    protected TimeChangedListener mTimeChangedListener;

    public enum AmPmOrder {
        START,
        END,
        LEFT,
        RIGHT
    }

    public interface TimeChangedListener {
        void onTimeChanged(TimePicker timePicker, int i, int i2, int i3);
    }

    private native int nativeGetTimePickerAmPmOrder(long j);

    private native String[] nativeGetTimePickerAmPmStrings(long j);

    private native long nativeGetTimePickerHandle();

    private native int nativeGetTimePickerHour(long j);

    private native int nativeGetTimePickerMinute(long j);

    private native int nativeGetTimePickerNormalTextColor(long j);

    private native int nativeGetTimePickerNormalTextSize(long j);

    private native int nativeGetTimePickerOperatedTextColor(long j);

    private native void nativeGetTimePickerRange(long j, int[] iArr);

    private native int nativeGetTimePickerSecond(long j);

    private native float nativeGetTimePickerSelectedNormalTextMarginRatio(long j);

    private native int nativeGetTimePickerSelectedTextColor(long j);

    private native int nativeGetTimePickerSelectedTextSize(long j);

    private native int nativeGetTimePickerSelectorWheelItemNum(long j);

    private native int nativeGetTimePickerShaderColor(long j);

    private native boolean nativeGetTimePickerWrapSelectorWheel(long j);

    private native boolean nativeIsTimePicker24Hour(long j);

    private native boolean nativeIsTimePickerHourEnabled(long j);

    private native boolean nativeIsTimePickerHourShown(long j);

    private native boolean nativeIsTimePickerMinuteEnabled(long j);

    private native boolean nativeIsTimePickerMinuteShown(long j);

    private native boolean nativeIsTimePickerSecondEnabled(long j);

    private native boolean nativeIsTimePickerSecondShown(long j);

    private native void nativeSetTimePicker24Hour(long j, boolean z);

    private native void nativeSetTimePickerAmPmOrder(long j, int i);

    private native void nativeSetTimePickerAmPmStrings(long j, String str, String str2);

    private native void nativeSetTimePickerDisplayedLinesDrawables(long j, long j2, long j3);

    private native void nativeSetTimePickerHour(long j, int i);

    private native void nativeSetTimePickerMinute(long j, int i);

    private native void nativeSetTimePickerNormalTextColor(long j, int i);

    private native void nativeSetTimePickerNormalTextSize(long j, int i);

    private native void nativeSetTimePickerOnTimeChangedCallback(long j, TimeChangedListener timeChangedListener);

    private native void nativeSetTimePickerOperatedTextColor(long j, int i);

    private native void nativeSetTimePickerRange(long j, int[] iArr);

    private native void nativeSetTimePickerSecond(long j, int i);

    private native void nativeSetTimePickerSelectedNormalTextMarginRatio(long j, float f);

    private native void nativeSetTimePickerSelectedTextColor(long j, int i);

    private native void nativeSetTimePickerSelectedTextSize(long j, int i);

    private native void nativeSetTimePickerSelectorWheelItemNum(long j, int i);

    private native void nativeSetTimePickerShaderColor(long j, int i);

    private native void nativeSetTimePickerWrapSelectorWheel(long j, boolean z);

    private native void nativeTimePickerEnableHour(long j, boolean z);

    private native void nativeTimePickerEnableMinute(long j, boolean z);

    private native void nativeTimePickerEnableSecond(long j, boolean z);

    private native void nativeTimePickerShowHour(long j, boolean z);

    private native void nativeTimePickerShowMinute(long j, boolean z);

    private native void nativeTimePickerShowSecond(long j, boolean z);

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.StackLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetTimePickerHandle();
        }
    }

    public TimePicker(Context context) {
        this(context, null);
    }

    public TimePicker(Context context, AttrSet attrSet) {
        this(context, attrSet, "TimePickerDefaultStyle");
    }

    public TimePicker(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mTimeChangedListener = null;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.StackLayout, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new TimePickerAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style.hasProperty("top_line_element")) {
            setDisplayedLinesDrawables(style.getPropertyValue("top_line_element").asElement(), getDisplayedBottomDrawable());
        }
        if (style.hasProperty("bottom_line_element")) {
            setDisplayedLinesDrawables(getDisplayedTopDrawable(), style.getPropertyValue("bottom_line_element").asElement());
        }
    }

    public boolean isHourEnabled() {
        return nativeIsTimePickerHourEnabled(this.mNativeViewPtr);
    }

    public void enableHour(boolean z) {
        nativeTimePickerEnableHour(this.mNativeViewPtr, z);
    }

    public void showHour(boolean z) {
        nativeTimePickerShowHour(this.mNativeViewPtr, z);
    }

    public boolean isHourShown() {
        return nativeIsTimePickerHourShown(this.mNativeViewPtr);
    }

    public boolean isMinuteEnabled() {
        return nativeIsTimePickerMinuteEnabled(this.mNativeViewPtr);
    }

    public void enableMinute(boolean z) {
        nativeTimePickerEnableMinute(this.mNativeViewPtr, z);
    }

    public void showMinute(boolean z) {
        nativeTimePickerShowMinute(this.mNativeViewPtr, z);
    }

    public boolean isMinuteShown() {
        return nativeIsTimePickerMinuteShown(this.mNativeViewPtr);
    }

    public boolean isSecondEnabled() {
        return nativeIsTimePickerSecondEnabled(this.mNativeViewPtr);
    }

    public void enableSecond(boolean z) {
        nativeTimePickerEnableSecond(this.mNativeViewPtr, z);
    }

    public void showSecond(boolean z) {
        nativeTimePickerShowSecond(this.mNativeViewPtr, z);
    }

    public boolean isSecondShown() {
        return nativeIsTimePickerSecondShown(this.mNativeViewPtr);
    }

    public int getHour() {
        return nativeGetTimePickerHour(this.mNativeViewPtr);
    }

    public void setHour(int i) {
        nativeSetTimePickerHour(this.mNativeViewPtr, i);
    }

    public int getMinute() {
        return nativeGetTimePickerMinute(this.mNativeViewPtr);
    }

    public void setMinute(int i) {
        nativeSetTimePickerMinute(this.mNativeViewPtr, i);
    }

    public int getSecond() {
        return nativeGetTimePickerSecond(this.mNativeViewPtr);
    }

    public void setSecond(int i) {
        nativeSetTimePickerSecond(this.mNativeViewPtr, i);
    }

    public void set24Hour(boolean z) {
        nativeSetTimePicker24Hour(this.mNativeViewPtr, z);
    }

    @Deprecated
    public void set24HourView(boolean z) {
        nativeSetTimePicker24Hour(this.mNativeViewPtr, z);
    }

    public boolean is24Hour() {
        return nativeIsTimePicker24Hour(this.mNativeViewPtr);
    }

    @Deprecated
    public boolean is24HourView() {
        return nativeIsTimePicker24Hour(this.mNativeViewPtr);
    }

    public void setTimeChangedListener(TimeChangedListener timeChangedListener) {
        this.mTimeChangedListener = timeChangedListener;
        nativeSetTimePickerOnTimeChangedCallback(this.mNativeViewPtr, this.mTimeChangedListener);
    }

    public void setRange(int[] iArr) {
        if (iArr != null) {
            nativeSetTimePickerRange(this.mNativeViewPtr, iArr);
        }
    }

    public void getRange(int[] iArr) {
        if (iArr != null) {
            nativeGetTimePickerRange(this.mNativeViewPtr, iArr);
        }
    }

    @Deprecated
    public void setTextSize(int i) {
        setNormalTextSize(i);
        setSelectedTextSize(i);
    }

    @Deprecated
    public int getTextSize() {
        return getNormalTextSize();
    }

    public void setNormalTextSize(int i) {
        nativeSetTimePickerNormalTextSize(this.mNativeViewPtr, i);
    }

    public int getNormalTextSize() {
        return nativeGetTimePickerNormalTextSize(this.mNativeViewPtr);
    }

    public void setSelectedTextSize(int i) {
        nativeSetTimePickerSelectedTextSize(this.mNativeViewPtr, i);
    }

    public int getSelectedTextSize() {
        return nativeGetTimePickerSelectedTextSize(this.mNativeViewPtr);
    }

    public void setNormalTextColor(Color color) {
        nativeSetTimePickerNormalTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getNormalTextColor() {
        return new Color(nativeGetTimePickerNormalTextColor(this.mNativeViewPtr));
    }

    public void setSelectedTextColor(Color color) {
        nativeSetTimePickerSelectedTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getSelectedTextColor() {
        return new Color(nativeGetTimePickerSelectedTextColor(this.mNativeViewPtr));
    }

    public void setOperatedTextColor(Color color) {
        nativeSetTimePickerOperatedTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getOperatedTextColor() {
        return new Color(nativeGetTimePickerOperatedTextColor(this.mNativeViewPtr));
    }

    public void setSelectedNormalTextMarginRatio(float f) {
        nativeSetTimePickerSelectedNormalTextMarginRatio(this.mNativeViewPtr, f);
    }

    public float getSelectedNormalTextMarginRatio() {
        return nativeGetTimePickerSelectedNormalTextMarginRatio(this.mNativeViewPtr);
    }

    public void setDisplayedLinesDrawables(Element element, Element element2) {
        long j;
        this.mDisplayedTPTopElement = element;
        this.mDisplayedTPBottomElement = element2;
        long j2 = this.mNativeViewPtr;
        if (element == null) {
            j = 0;
        } else {
            j = element.getNativeElementPtr();
        }
        nativeSetTimePickerDisplayedLinesDrawables(j2, j, element2 == null ? 0 : element2.getNativeElementPtr());
    }

    public Element getDisplayedTopDrawable() {
        return this.mDisplayedTPTopElement;
    }

    public Element getDisplayedBottomDrawable() {
        return this.mDisplayedTPBottomElement;
    }

    public void setAmPmStrings(String str, String str2) {
        nativeSetTimePickerAmPmStrings(this.mNativeViewPtr, str, str2);
    }

    public String[] getAmPmStrings() {
        return nativeGetTimePickerAmPmStrings(this.mNativeViewPtr);
    }

    public void setWrapSelectorWheel(boolean z) {
        nativeSetTimePickerWrapSelectorWheel(this.mNativeViewPtr, z);
    }

    public boolean getWrapSelectorWheel() {
        return nativeGetTimePickerWrapSelectorWheel(this.mNativeViewPtr);
    }

    public void setShaderColor(Color color) {
        nativeSetTimePickerShaderColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getShaderColor() {
        return new Color(nativeGetTimePickerShaderColor(this.mNativeViewPtr));
    }

    public void setSelectorWheelItemNum(int i) {
        if (i > 0) {
            nativeSetTimePickerSelectorWheelItemNum(this.mNativeViewPtr, i);
            return;
        }
        throw new IllegalArgumentException("selectorWheelItemCnt must be > 0");
    }

    public int getSelectorWheelItemNum() {
        return nativeGetTimePickerSelectorWheelItemNum(this.mNativeViewPtr);
    }

    public void setAmPmOrder(AmPmOrder amPmOrder) {
        nativeSetTimePickerAmPmOrder(this.mNativeViewPtr, amPmOrder.ordinal());
    }

    public AmPmOrder getAmPmOrder() {
        int nativeGetTimePickerAmPmOrder = nativeGetTimePickerAmPmOrder(this.mNativeViewPtr);
        if (nativeGetTimePickerAmPmOrder == 0) {
            return AmPmOrder.START;
        }
        if (nativeGetTimePickerAmPmOrder == 1) {
            return AmPmOrder.END;
        }
        if (nativeGetTimePickerAmPmOrder != 2) {
            return AmPmOrder.RIGHT;
        }
        return AmPmOrder.LEFT;
    }
}
