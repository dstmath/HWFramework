package ohos.agp.components;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import ohos.agp.components.element.Element;
import ohos.agp.styles.Style;
import ohos.agp.text.Font;
import ohos.agp.utils.Color;
import ohos.agp.utils.ErrorHandler;
import ohos.agp.utils.TextTool;
import ohos.app.Context;
import ohos.dmsdp.sdk.DMSDPConfig;

public class DatePicker extends StackLayout {
    private Element mDisplayedDPBottomElement;
    private Element mDisplayedDPTopElement;
    private Font mNormalTextFont;
    private Element mOperatedTextBackgroundElement;
    private Element mSelectedTextBackgroundElement;
    private Font mSelectedTextFont;
    private ValueChangedListener mValueChangedListener;

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

    public interface ValueChangedListener {
        void onValueChanged(DatePicker datePicker, int i, int i2, int i3);
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

    private native int nativeGetDatePickerSelectorItemNum(long j);

    private native int nativeGetDatePickerShaderColor(long j);

    private native int nativeGetDatePickerYear(long j);

    private native boolean nativeGetDatePickerYearFixed(long j);

    private native boolean nativeIsWheelModeEnabled(long j);

    private native void nativeSetDatePickerDateChangedListener(long j, ValueChangedListener valueChangedListener);

    private native void nativeSetDatePickerDateOrder(long j, int i);

    private native void nativeSetDatePickerDayFixed(long j, boolean z);

    private native void nativeSetDatePickerDisplayedLinesDrawables(long j, long j2, long j3);

    private native void nativeSetDatePickerMaxDate(long j, long j2);

    private native void nativeSetDatePickerMinDate(long j, long j2);

    private native void nativeSetDatePickerMonthFixed(long j, boolean z);

    private native void nativeSetDatePickerNormalTextColor(long j, int i);

    private native void nativeSetDatePickerNormalTextFont(long j, long j2);

    private native void nativeSetDatePickerNormalTextSize(long j, int i);

    private native void nativeSetDatePickerOperatedTextColor(long j, int i);

    private native void nativeSetDatePickerSelectedNormalTextMarginRatio(long j, float f);

    private native void nativeSetDatePickerSelectedTextColor(long j, int i);

    private native void nativeSetDatePickerSelectedTextFont(long j, long j2);

    private native void nativeSetDatePickerSelectedTextSize(long j, int i);

    private native void nativeSetDatePickerSelectorItemNum(long j, int i);

    private native void nativeSetDatePickerShaderColor(long j, int i);

    private native void nativeSetDatePickerYearFixed(long j, boolean z);

    private native void nativeSetOperatedTextBackground(long j, long j2);

    private native void nativeSetSelectedTextBackground(long j, long j2);

    private native void nativeSetWheelModeEnabled(long j, boolean z);

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
        this.mNormalTextFont = Font.DEFAULT;
        this.mSelectedTextFont = Font.DEFAULT;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.StackLayout, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getDatePickerAttrsConstants();
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
        if (style.hasProperty("operated_text_background")) {
            setOperatedTextBackground(style.getPropertyValue("operated_text_background").asElement());
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
        ErrorHandler.validateParamNonNegative(i);
        ErrorHandler.validateParamIsNaturalNumber(i2);
        ErrorHandler.validateParamIsNaturalNumber(i3);
        nativeDatePickerUpdateDate(this.mNativeViewPtr, i, i2, i3);
    }

    public void setValueChangedListener(ValueChangedListener valueChangedListener) {
        this.mValueChangedListener = valueChangedListener;
        nativeSetDatePickerDateChangedListener(this.mNativeViewPtr, valueChangedListener);
    }

    public ValueChangedListener getValueChangedListener() {
        return this.mValueChangedListener;
    }

    static /* synthetic */ boolean lambda$setDateOrder$0(Integer num) {
        return num.intValue() >= 0 && num.intValue() <= 10;
    }

    public void setDateOrder(int i) {
        validateParam(Integer.valueOf(i), $$Lambda$DatePicker$Xb4hlY8qMwoYBagXtOwrHFoNy6Y.INSTANCE, "please use one of the defined values from DateOrder interface");
        nativeSetDatePickerDateOrder(this.mNativeViewPtr, i);
    }

    public int getDateOrder() {
        return nativeGetDatePickerDateOrder(this.mNativeViewPtr);
    }

    public void setNormalTextSize(int i) {
        TextTool.validateTextSizeParam(i);
        nativeSetDatePickerNormalTextSize(this.mNativeViewPtr, i);
    }

    public int getNormalTextSize() {
        return nativeGetDatePickerNormalTextSize(this.mNativeViewPtr);
    }

    public void setSelectedTextSize(int i) {
        TextTool.validateTextSizeParam(i);
        nativeSetDatePickerSelectedTextSize(this.mNativeViewPtr, i);
    }

    public int getSelectedTextSize() {
        return nativeGetDatePickerSelectedTextSize(this.mNativeViewPtr);
    }

    public void init(int i, int i2, int i3, ValueChangedListener valueChangedListener) {
        updateDate(i, i2, i3);
        setValueChangedListener(valueChangedListener);
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
        ErrorHandler.validateParamNotNull(color);
        nativeSetDatePickerNormalTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getNormalTextColor() {
        return new Color(nativeGetDatePickerNormalTextColor(this.mNativeViewPtr));
    }

    public void setSelectedTextColor(Color color) {
        ErrorHandler.validateParamNotNull(color);
        nativeSetDatePickerSelectedTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getSelectedTextColor() {
        return new Color(nativeGetDatePickerSelectedTextColor(this.mNativeViewPtr));
    }

    public void setOperatedTextColor(Color color) {
        ErrorHandler.validateParamNotNull(color);
        nativeSetDatePickerOperatedTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getOperatedTextColor() {
        return new Color(nativeGetDatePickerOperatedTextColor(this.mNativeViewPtr));
    }

    public void setNormalTextFont(Font font) {
        TextTool.validateFontParam(font);
        if (!font.convertToTypeface().equals(this.mNormalTextFont.convertToTypeface())) {
            nativeSetDatePickerNormalTextFont(this.mNativeViewPtr, font.convertToTypeface().getNativeTypefacePtr());
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
            nativeSetDatePickerSelectedTextFont(this.mNativeViewPtr, font.convertToTypeface().getNativeTypefacePtr());
            this.mSelectedTextFont = font;
        }
    }

    public Font getSelectedTextFont() {
        if (this.mSelectedTextFont == null) {
            this.mSelectedTextFont = Font.DEFAULT;
        }
        return this.mSelectedTextFont;
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

    public void setOperatedTextBackground(Element element) {
        if (!Objects.equals(this.mSelectedTextBackgroundElement, element)) {
            this.mOperatedTextBackgroundElement = element;
            nativeSetOperatedTextBackground(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
        }
    }

    public Element getOperatedTextBackgroundElement() {
        return this.mOperatedTextBackgroundElement;
    }

    public void setSelectedNormalTextMarginRatio(float f) {
        ErrorHandler.validateParamNonNegative(f);
        nativeSetDatePickerSelectedNormalTextMarginRatio(this.mNativeViewPtr, f);
    }

    public float getSelectedNormalTextMarginRatio() {
        return nativeGetDatePickerSelectedNormalTextMarginRatio(this.mNativeViewPtr);
    }

    public void setDisplayedLinesElements(Element element, Element element2) {
        long j;
        long j2;
        this.mDisplayedDPTopElement = element;
        this.mDisplayedDPBottomElement = element2;
        long j3 = this.mNativeViewPtr;
        if (element == null) {
            j = 0;
        } else {
            j = element.getNativeElementPtr();
        }
        if (element2 == null) {
            j2 = 0;
        } else {
            j2 = element2.getNativeElementPtr();
        }
        nativeSetDatePickerDisplayedLinesDrawables(j3, j, j2);
    }

    public Element getDisplayedTopElement() {
        return this.mDisplayedDPTopElement;
    }

    public Element getDisplayedBottomElement() {
        return this.mDisplayedDPBottomElement;
    }

    public void setWheelModeEnabled(boolean z) {
        nativeSetWheelModeEnabled(this.mNativeViewPtr, z);
    }

    public boolean isWheelModeEnabled() {
        return nativeIsWheelModeEnabled(this.mNativeViewPtr);
    }

    public void setSelectorItemNum(int i) {
        ErrorHandler.validateParamIsNaturalNumber(i);
        nativeSetDatePickerSelectorItemNum(this.mNativeViewPtr, i);
    }

    public int getSelectorItemNum() {
        return nativeGetDatePickerSelectorItemNum(this.mNativeViewPtr);
    }

    public void setShaderColor(Color color) {
        ErrorHandler.validateParamNotNull(color);
        nativeSetDatePickerShaderColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getShaderColor() {
        return new Color(nativeGetDatePickerShaderColor(this.mNativeViewPtr));
    }

    private String formatNumber(int i) {
        NumberFormat instance = NumberFormat.getInstance(Locale.getDefault(Locale.Category.FORMAT));
        if (instance instanceof DecimalFormat) {
            DecimalFormat decimalFormat = (DecimalFormat) instance;
            decimalFormat.applyPattern(DMSDPConfig.SPLIT);
            return decimalFormat.format((long) i);
        }
        throw new ClassCastException("Object type conversion  failed.");
    }

    private String formatMonth(int i) {
        return (i < 1 || i > 12) ? "" : DateFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT)).getShortMonths()[i - 1];
    }
}
