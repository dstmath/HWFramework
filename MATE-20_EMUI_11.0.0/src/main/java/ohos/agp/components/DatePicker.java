package ohos.agp.components;

import ohos.agp.components.element.Element;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.DatePickerAttrsConstants;
import ohos.agp.utils.Color;
import ohos.app.Context;

public class DatePicker extends StackLayout {
    private DateChangedListener mDateChangedListener;
    private Element mDisplayedDPBottomElement;
    private Element mDisplayedDPTopElement;

    public interface DateChangedListener {
        void onDateChanged(DatePicker datePicker, int i, int i2, int i3);
    }

    public interface DateOrder {
        public static final int DAY = 10;
        public static final int DM = 4;
        public static final int DMY = 0;
        public static final int MD = 5;
        public static final int MDY = 1;
        public static final int MONTH = 9;
        public static final int MY = 7;
        public static final int YDM = 3;
        public static final int YEAR = 8;
        public static final int YM = 6;
        public static final int YMD = 2;
    }

    private native void nativeDatePickerUpdateDate(long j, int i, int i2, int i3);

    private native int nativeGetDatePickerDateOrder(long j);

    private native boolean nativeGetDatePickerDayFixed(long j);

    private native int nativeGetDatePickerDayOfMonth(long j);

    private native long nativeGetDatePickerHandle();

    private native long nativeGetDatePickerMaxDate(long j);

    private native long nativeGetDatePickerMinDate(long j);

    private native int nativeGetDatePickerMonth(long j);

    private native boolean nativeGetDatePickerMonthFixed(long j);

    private native int nativeGetDatePickerNormalTextColor(long j);

    private native int nativeGetDatePickerNormalTextSize(long j);

    private native int nativeGetDatePickerOperatedTextColor(long j);

    private native float nativeGetDatePickerSelectedNormalTextMarginRatio(long j);

    private native int nativeGetDatePickerSelectedTextColor(long j);

    private native int nativeGetDatePickerSelectedTextSize(long j);

    private native int nativeGetDatePickerSelectorWheelItemNum(long j);

    private native int nativeGetDatePickerShaderColor(long j);

    private native boolean nativeGetDatePickerWrapSelectorWheel(long j);

    private native int nativeGetDatePickerYear(long j);

    private native boolean nativeGetDatePickerYearFixed(long j);

    private native void nativeSetDatePickerDateChangedListener(long j, DateChangedListener dateChangedListener);

    private native void nativeSetDatePickerDateOrder(long j, int i);

    private native void nativeSetDatePickerDayFixed(long j, boolean z);

    private native void nativeSetDatePickerDisplayedLinesDrawables(long j, long j2, long j3);

    private native void nativeSetDatePickerMaxDate(long j, long j2);

    private native void nativeSetDatePickerMinDate(long j, long j2);

    private native void nativeSetDatePickerMonthFixed(long j, boolean z);

    private native void nativeSetDatePickerNormalTextColor(long j, int i);

    private native void nativeSetDatePickerNormalTextSize(long j, int i);

    private native void nativeSetDatePickerOperatedTextColor(long j, int i);

    private native void nativeSetDatePickerSelectedNormalTextMarginRatio(long j, float f);

    private native void nativeSetDatePickerSelectedTextColor(long j, int i);

    private native void nativeSetDatePickerSelectedTextSize(long j, int i);

    private native void nativeSetDatePickerSelectorWheelItemNum(long j, int i);

    private native void nativeSetDatePickerShaderColor(long j, int i);

    private native void nativeSetDatePickerWrapSelectorWheel(long j, boolean z);

    private native void nativeSetDatePickerYearFixed(long j, boolean z);

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.StackLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        this.mNativeViewPtr = nativeGetDatePickerHandle();
    }

    public DatePicker(Context context) {
        this(context, null);
    }

    public DatePicker(Context context, AttrSet attrSet) {
        this(context, attrSet, "DatePickerDefaultStyle");
    }

    public DatePicker(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.StackLayout, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new DatePickerAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style.hasProperty("top_line_element")) {
            setDisplayedLinesDrawables(style.getPropertyValue("top_line_element").asElement(), getDisplayedBottomElement());
        }
        if (style.hasProperty("bottom_line_element")) {
            setDisplayedLinesDrawables(getDisplayedTopElement(), style.getPropertyValue("bottom_line_element").asElement());
        }
    }

    public int getYear() {
        return nativeGetDatePickerYear(this.mNativeViewPtr);
    }

    public int getMonth() {
        return nativeGetDatePickerMonth(this.mNativeViewPtr);
    }

    public int getDayOfMonth() {
        return nativeGetDatePickerDayOfMonth(this.mNativeViewPtr);
    }

    public long getMaxDate() {
        return nativeGetDatePickerMaxDate(this.mNativeViewPtr);
    }

    public long getMinDate() {
        return nativeGetDatePickerMinDate(this.mNativeViewPtr);
    }

    public void setMaxDate(long j) {
        nativeSetDatePickerMaxDate(this.mNativeViewPtr, j);
    }

    public void setMinDate(long j) {
        nativeSetDatePickerMinDate(this.mNativeViewPtr, j);
    }

    public void updateDate(int i, int i2, int i3) {
        if (i < 0 || i2 < 1 || i3 < 1) {
            throw new IllegalArgumentException("year must be >= 0, month >= 1, dayOfMonth >= 1");
        }
        nativeDatePickerUpdateDate(this.mNativeViewPtr, i, i2, i3);
    }

    public void setDateChangedListener(DateChangedListener dateChangedListener) {
        this.mDateChangedListener = dateChangedListener;
        nativeSetDatePickerDateChangedListener(this.mNativeViewPtr, dateChangedListener);
    }

    public void setDateOrder(int i) {
        nativeSetDatePickerDateOrder(this.mNativeViewPtr, i);
    }

    public int getDateOrder() {
        return nativeGetDatePickerDateOrder(this.mNativeViewPtr);
    }

    public void setNormalTextSize(int i) {
        nativeSetDatePickerNormalTextSize(this.mNativeViewPtr, i);
    }

    public int getNormalTextSize() {
        return nativeGetDatePickerNormalTextSize(this.mNativeViewPtr);
    }

    public void setSelectedTextSize(int i) {
        nativeSetDatePickerSelectedTextSize(this.mNativeViewPtr, i);
    }

    public int getSelectedTextSize() {
        return nativeGetDatePickerSelectedTextSize(this.mNativeViewPtr);
    }

    public void init(int i, int i2, int i3, DateChangedListener dateChangedListener) {
        updateDate(i, i2, i3);
        setDateChangedListener(dateChangedListener);
    }

    public void setYearFixed(boolean z) {
        nativeSetDatePickerYearFixed(this.mNativeViewPtr, z);
    }

    public boolean isYearFixed() {
        return nativeGetDatePickerYearFixed(this.mNativeViewPtr);
    }

    public void setMonthFixed(boolean z) {
        nativeSetDatePickerMonthFixed(this.mNativeViewPtr, z);
    }

    public boolean isMonthFixed() {
        return nativeGetDatePickerMonthFixed(this.mNativeViewPtr);
    }

    public void setDayFixed(boolean z) {
        nativeSetDatePickerDayFixed(this.mNativeViewPtr, z);
    }

    public boolean isDayFixed() {
        return nativeGetDatePickerDayFixed(this.mNativeViewPtr);
    }

    public void setNormalTextColor(Color color) {
        nativeSetDatePickerNormalTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getNormalTextColor() {
        return new Color(nativeGetDatePickerNormalTextColor(this.mNativeViewPtr));
    }

    public void setSelectedTextColor(Color color) {
        nativeSetDatePickerSelectedTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getSelectedTextColor() {
        return new Color(nativeGetDatePickerSelectedTextColor(this.mNativeViewPtr));
    }

    public void setOperatedTextColor(Color color) {
        nativeSetDatePickerOperatedTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getOperatedTextColor() {
        return new Color(nativeGetDatePickerOperatedTextColor(this.mNativeViewPtr));
    }

    public void setSelectedNormalTextMarginRatio(float f) {
        nativeSetDatePickerSelectedNormalTextMarginRatio(this.mNativeViewPtr, f);
    }

    public float getSelectedNormalTextMarginRatio() {
        return nativeGetDatePickerSelectedNormalTextMarginRatio(this.mNativeViewPtr);
    }

    @Deprecated
    public void setDisplayedLinesDrawables(Element element, Element element2) {
        long j;
        this.mDisplayedDPTopElement = element;
        this.mDisplayedDPBottomElement = element2;
        long j2 = this.mNativeViewPtr;
        if (element == null) {
            j = 0;
        } else {
            j = element.getNativeElementPtr();
        }
        nativeSetDatePickerDisplayedLinesDrawables(j2, j, element2 == null ? 0 : element2.getNativeElementPtr());
    }

    public void setDisplayedLinesElements(Element element, Element element2) {
        long j;
        this.mDisplayedDPTopElement = element;
        this.mDisplayedDPBottomElement = element2;
        long j2 = this.mNativeViewPtr;
        if (element == null) {
            j = 0;
        } else {
            j = element.getNativeElementPtr();
        }
        nativeSetDatePickerDisplayedLinesDrawables(j2, j, element2 == null ? 0 : element2.getNativeElementPtr());
    }

    @Deprecated
    public Element getDisplayedTopDrawable() {
        return this.mDisplayedDPTopElement;
    }

    public Element getDisplayedTopElement() {
        return this.mDisplayedDPTopElement;
    }

    @Deprecated
    public Element getDisplayedBottomDrawable() {
        return this.mDisplayedDPBottomElement;
    }

    public Element getDisplayedBottomElement() {
        return this.mDisplayedDPBottomElement;
    }

    public void setWrapSelectorWheel(boolean z) {
        nativeSetDatePickerWrapSelectorWheel(this.mNativeViewPtr, z);
    }

    public boolean getWrapSelectorWheel() {
        return nativeGetDatePickerWrapSelectorWheel(this.mNativeViewPtr);
    }

    public void setSelectorWheelItemNum(int i) {
        if (i > 0) {
            nativeSetDatePickerSelectorWheelItemNum(this.mNativeViewPtr, i);
            return;
        }
        throw new IllegalArgumentException("selectorWheelItemCnt must be > 0");
    }

    public int getSelectorWheelItemNum() {
        return nativeGetDatePickerSelectorWheelItemNum(this.mNativeViewPtr);
    }

    public void setShaderColor(Color color) {
        nativeSetDatePickerShaderColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getShaderColor() {
        return new Color(nativeGetDatePickerShaderColor(this.mNativeViewPtr));
    }
}
