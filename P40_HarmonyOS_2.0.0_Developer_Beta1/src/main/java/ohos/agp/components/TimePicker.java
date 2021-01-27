package ohos.agp.components;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
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

public class TimePicker extends StackLayout {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_COMPONENT");
    private Element mDisplayedTPBottomElement;
    private Element mDisplayedTPTopElement;
    private Font mNormalTextFont;
    private Element mOperatedTextBackgroundElement;
    private Element mSelectedTextBackgroundElement;
    private Font mSelectedTextFont;
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

    private native int nativeGetTimePickerSelectorItemNum(long j);

    private native int nativeGetTimePickerShaderColor(long j);

    private native boolean nativeIsTimePicker24Hour(long j);

    private native boolean nativeIsTimePickerHourEnabled(long j);

    private native boolean nativeIsTimePickerHourShown(long j);

    private native boolean nativeIsTimePickerMinuteEnabled(long j);

    private native boolean nativeIsTimePickerMinuteShown(long j);

    private native boolean nativeIsTimePickerSecondEnabled(long j);

    private native boolean nativeIsTimePickerSecondShown(long j);

    private native boolean nativeIsWheelModeEnabled(long j);

    private native void nativeSetOperatedTextBackground(long j, long j2);

    private native void nativeSetSelectedTextBackground(long j, long j2);

    private native void nativeSetTimePicker24Hour(long j, boolean z);

    private native void nativeSetTimePickerAmPmOrder(long j, int i);

    private native void nativeSetTimePickerAmPmStrings(long j, String str, String str2);

    private native void nativeSetTimePickerDisplayedLinesDrawables(long j, long j2, long j3);

    private native void nativeSetTimePickerHour(long j, int i);

    private native void nativeSetTimePickerMinute(long j, int i);

    private native void nativeSetTimePickerNormalTextColor(long j, int i);

    private native void nativeSetTimePickerNormalTextFont(long j, long j2);

    private native void nativeSetTimePickerNormalTextSize(long j, int i);

    private native void nativeSetTimePickerOnTimeChangedCallback(long j, TimeChangedListener timeChangedListener);

    private native void nativeSetTimePickerOperatedTextColor(long j, int i);

    private native void nativeSetTimePickerRange(long j, int[] iArr);

    private native void nativeSetTimePickerSecond(long j, int i);

    private native void nativeSetTimePickerSelectedNormalTextMarginRatio(long j, float f);

    private native void nativeSetTimePickerSelectedTextColor(long j, int i);

    private native void nativeSetTimePickerSelectedTextFont(long j, long j2);

    private native void nativeSetTimePickerSelectedTextSize(long j, int i);

    private native void nativeSetTimePickerSelectorItemNum(long j, int i);

    private native void nativeSetTimePickerShaderColor(long j, int i);

    private native void nativeSetWheelModeEnabled(long j, boolean z);

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
        formatAmPm();
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
        this.mNormalTextFont = Font.DEFAULT;
        this.mSelectedTextFont = Font.DEFAULT;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.StackLayout, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getTimePickerAttrsConstants();
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

    static /* synthetic */ boolean lambda$setHour$0(Integer num) {
        return num.intValue() >= 0 && num.intValue() < 24;
    }

    public void setHour(int i) {
        validateParam(Integer.valueOf(i), $$Lambda$TimePicker$Q0W0UuQ_LRqjACM10Cu0iWDEtE.INSTANCE, "hour must be in range [0,23]");
        nativeSetTimePickerHour(this.mNativeViewPtr, i);
    }

    public int getMinute() {
        return nativeGetTimePickerMinute(this.mNativeViewPtr);
    }

    static /* synthetic */ boolean lambda$setMinute$1(Integer num) {
        return num.intValue() >= 0 && num.intValue() < 60;
    }

    public void setMinute(int i) {
        validateParam(Integer.valueOf(i), $$Lambda$TimePicker$t8zQvTaUVvTJEkFImHbxjiMGEY.INSTANCE, "minute must be in range [0,59]");
        nativeSetTimePickerMinute(this.mNativeViewPtr, i);
    }

    public int getSecond() {
        return nativeGetTimePickerSecond(this.mNativeViewPtr);
    }

    static /* synthetic */ boolean lambda$setSecond$2(Integer num) {
        return num.intValue() >= 0 && num.intValue() < 60;
    }

    public void setSecond(int i) {
        validateParam(Integer.valueOf(i), $$Lambda$TimePicker$BFIprUPJ1Hte0EfAUpwfSsBARI0.INSTANCE, "second must be in range [0,59]");
        nativeSetTimePickerSecond(this.mNativeViewPtr, i);
    }

    public void set24Hour(boolean z) {
        nativeSetTimePicker24Hour(this.mNativeViewPtr, z);
    }

    public boolean is24Hour() {
        return nativeIsTimePicker24Hour(this.mNativeViewPtr);
    }

    public void setTimeChangedListener(TimeChangedListener timeChangedListener) {
        this.mTimeChangedListener = timeChangedListener;
        nativeSetTimePickerOnTimeChangedCallback(this.mNativeViewPtr, this.mTimeChangedListener);
    }

    public void setRange(int[] iArr) {
        ErrorHandler.validateParamNotNull(iArr);
        validateParam(Integer.valueOf(iArr.length), $$Lambda$TimePicker$rkNBkjsrgGUS3MRjsr4cfAQ0YGg.INSTANCE, "ranges size must be 6");
        validateParam(Integer.valueOf(iArr[0]), $$Lambda$TimePicker$L96kZdcJOlLn2sTZMeLBsYFShw.INSTANCE, "starting hour must be in range [0,23]");
        validateParam(Integer.valueOf(iArr[1]), $$Lambda$TimePicker$uyaL0CbvBEkY0OpoefDGw2Xkg.INSTANCE, "starting minute must be in range [0,59]");
        validateParam(Integer.valueOf(iArr[2]), $$Lambda$TimePicker$KO4BkyiZ2XJmqCAsZbMNzJVFVU.INSTANCE, "starting second must be in range [0,59]");
        validateParam(Integer.valueOf(iArr[3]), $$Lambda$TimePicker$wDemhWzBbKVIzf6VzhRKsPLtKE.INSTANCE, "ending hour must be in range [0,23]");
        validateParam(Integer.valueOf(iArr[4]), $$Lambda$TimePicker$E9ivxOqVjzQ8jCdnexqPYpdwQ9A.INSTANCE, "ending minute must be in range [0,59]");
        validateParam(Integer.valueOf(iArr[5]), $$Lambda$TimePicker$fDL3L4uf8idHZezRnP3CMDcjcC8.INSTANCE, "ending second must be in range [0,59]");
        nativeSetTimePickerRange(this.mNativeViewPtr, iArr);
    }

    static /* synthetic */ boolean lambda$setRange$3(Integer num) {
        return num.intValue() == 6;
    }

    static /* synthetic */ boolean lambda$setRange$4(Integer num) {
        return num.intValue() >= 0 && num.intValue() < 24;
    }

    static /* synthetic */ boolean lambda$setRange$5(Integer num) {
        return num.intValue() >= 0 && num.intValue() < 60;
    }

    static /* synthetic */ boolean lambda$setRange$6(Integer num) {
        return num.intValue() >= 0 && num.intValue() < 60;
    }

    static /* synthetic */ boolean lambda$setRange$7(Integer num) {
        return num.intValue() >= 0 && num.intValue() < 24;
    }

    static /* synthetic */ boolean lambda$setRange$8(Integer num) {
        return num.intValue() >= 0 && num.intValue() < 60;
    }

    static /* synthetic */ boolean lambda$setRange$9(Integer num) {
        return num.intValue() >= 0 && num.intValue() < 60;
    }

    public void getRange(int[] iArr) {
        ErrorHandler.validateParamNotNull(iArr);
        nativeGetTimePickerRange(this.mNativeViewPtr, iArr);
    }

    public void setNormalTextSize(int i) {
        if (TextTool.validateTextSizeParam(i)) {
            nativeSetTimePickerNormalTextSize(this.mNativeViewPtr, i);
        }
    }

    public int getNormalTextSize() {
        return nativeGetTimePickerNormalTextSize(this.mNativeViewPtr);
    }

    public void setSelectedTextSize(int i) {
        if (TextTool.validateTextSizeParam(i)) {
            nativeSetTimePickerSelectedTextSize(this.mNativeViewPtr, i);
        }
    }

    public int getSelectedTextSize() {
        return nativeGetTimePickerSelectedTextSize(this.mNativeViewPtr);
    }

    public void setNormalTextColor(Color color) {
        ErrorHandler.validateParamNotNull(color);
        nativeSetTimePickerNormalTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getNormalTextColor() {
        return new Color(nativeGetTimePickerNormalTextColor(this.mNativeViewPtr));
    }

    public void setSelectedTextColor(Color color) {
        ErrorHandler.validateParamNotNull(color);
        nativeSetTimePickerSelectedTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getSelectedTextColor() {
        return new Color(nativeGetTimePickerSelectedTextColor(this.mNativeViewPtr));
    }

    public void setOperatedTextColor(Color color) {
        ErrorHandler.validateParamNotNull(color);
        nativeSetTimePickerOperatedTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getOperatedTextColor() {
        return new Color(nativeGetTimePickerOperatedTextColor(this.mNativeViewPtr));
    }

    public void setNormalTextFont(Font font) {
        ErrorHandler.validateParamNotNull(font);
        long nativeTypefacePtr = font.convertToTypeface().getNativeTypefacePtr();
        validateParam(Long.valueOf(nativeTypefacePtr), $$Lambda$TimePicker$N3akjlRSec38laLcapfEZoGwfyE.INSTANCE, "invalid font");
        if (!font.convertToTypeface().equals(this.mNormalTextFont.convertToTypeface())) {
            nativeSetTimePickerNormalTextFont(this.mNativeViewPtr, nativeTypefacePtr);
            this.mNormalTextFont = font;
        }
    }

    static /* synthetic */ boolean lambda$setNormalTextFont$10(Long l) {
        return l.longValue() != 0;
    }

    public Font getNormalTextFont() {
        if (this.mNormalTextFont == null) {
            this.mNormalTextFont = Font.DEFAULT;
        }
        return this.mNormalTextFont;
    }

    public void setSelectedTextFont(Font font) {
        ErrorHandler.validateParamNotNull(font);
        long nativeTypefacePtr = font.convertToTypeface().getNativeTypefacePtr();
        validateParam(Long.valueOf(nativeTypefacePtr), $$Lambda$TimePicker$TQL7bGX3J7RFZJKpQoCLmpw8Ew.INSTANCE, "invalid font");
        if (!font.convertToTypeface().equals(this.mSelectedTextFont.convertToTypeface())) {
            nativeSetTimePickerSelectedTextFont(this.mNativeViewPtr, nativeTypefacePtr);
            this.mSelectedTextFont = font;
        }
    }

    static /* synthetic */ boolean lambda$setSelectedTextFont$11(Long l) {
        return l.longValue() != 0;
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

    static /* synthetic */ boolean lambda$setSelectedNormalTextMarginRatio$12(Float f) {
        return f.floatValue() >= 0.0f;
    }

    public void setSelectedNormalTextMarginRatio(float f) {
        validateParam(Float.valueOf(f), $$Lambda$TimePicker$rGLHmSLuXnSwk9Aur0rnWpR5VQ.INSTANCE, "text margin ratio must be greater or equal to 0");
        nativeSetTimePickerSelectedNormalTextMarginRatio(this.mNativeViewPtr, f);
    }

    public float getSelectedNormalTextMarginRatio() {
        return nativeGetTimePickerSelectedNormalTextMarginRatio(this.mNativeViewPtr);
    }

    public void setDisplayedLinesElements(Element element, Element element2) {
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

    public Element getDisplayedTopElement() {
        return this.mDisplayedTPTopElement;
    }

    public Element getDisplayedBottomElement() {
        return this.mDisplayedTPBottomElement;
    }

    public Element[] getDisplayedLinesElements() {
        return new Element[]{this.mDisplayedTPTopElement, this.mDisplayedTPBottomElement};
    }

    public void setDisplayedTopElement(Element element) {
        setDisplayedLinesElements(element, this.mDisplayedTPBottomElement);
    }

    public void setDisplayedBottomElement(Element element) {
        setDisplayedLinesElements(this.mDisplayedTPTopElement, element);
    }

    public void setAmPmStrings(String str, String str2) {
        nativeSetTimePickerAmPmStrings(this.mNativeViewPtr, str, str2);
    }

    public void setAmString(String str) {
        nativeSetTimePickerAmPmStrings(this.mNativeViewPtr, str, getPmString());
    }

    public void setPmString(String str) {
        nativeSetTimePickerAmPmStrings(this.mNativeViewPtr, getAmString(), str);
    }

    public String[] getAmPmStrings() {
        return nativeGetTimePickerAmPmStrings(this.mNativeViewPtr);
    }

    public String getAmString() {
        return nativeGetTimePickerAmPmStrings(this.mNativeViewPtr)[0];
    }

    public String getPmString() {
        return nativeGetTimePickerAmPmStrings(this.mNativeViewPtr)[1];
    }

    public TimeChangedListener getTimeChangedListener() {
        return this.mTimeChangedListener;
    }

    public void setWheelModeEnabled(boolean z) {
        nativeSetWheelModeEnabled(this.mNativeViewPtr, z);
    }

    public boolean isWheelModeEnabled() {
        return nativeIsWheelModeEnabled(this.mNativeViewPtr);
    }

    public void setShaderColor(Color color) {
        ErrorHandler.validateParamNotNull(color);
        nativeSetTimePickerShaderColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getShaderColor() {
        return new Color(nativeGetTimePickerShaderColor(this.mNativeViewPtr));
    }

    static /* synthetic */ boolean lambda$setSelectorItemNum$13(Integer num) {
        return num.intValue() > 0;
    }

    public void setSelectorItemNum(int i) {
        validateParam(Integer.valueOf(i), $$Lambda$TimePicker$Xq2BD6WToi6zoZ4v7u9WgjP1lyg.INSTANCE, "number of items must be greater than 0");
        nativeSetTimePickerSelectorItemNum(this.mNativeViewPtr, i);
    }

    public int getSelectorItemNum() {
        return nativeGetTimePickerSelectorItemNum(this.mNativeViewPtr);
    }

    public void setAmPmOrder(AmPmOrder amPmOrder) {
        ErrorHandler.validateParamNotNull(amPmOrder);
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

    private String formatNumber(int i) {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault(Locale.Category.FORMAT));
        decimalFormat.setMinimumIntegerDigits(2);
        return decimalFormat.format((long) i);
    }

    private void formatAmPm() {
        nativeSetTimePickerAmPmStrings(this.mNativeViewPtr, DateFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT)).getAmPmStrings()[0], DateFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT)).getAmPmStrings()[1]);
    }
}
