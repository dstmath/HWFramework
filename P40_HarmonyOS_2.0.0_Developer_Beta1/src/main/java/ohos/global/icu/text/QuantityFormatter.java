package ohos.global.icu.text;

import java.text.FieldPosition;
import ohos.global.icu.impl.SimpleFormatterImpl;
import ohos.global.icu.impl.StandardPlural;

class QuantityFormatter {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final SimpleFormatter[] templates = new SimpleFormatter[StandardPlural.COUNT];

    public void addIfAbsent(CharSequence charSequence, String str) {
        int indexFromString = StandardPlural.indexFromString(charSequence);
        SimpleFormatter[] simpleFormatterArr = this.templates;
        if (simpleFormatterArr[indexFromString] == null) {
            simpleFormatterArr[indexFromString] = SimpleFormatter.compileMinMaxArguments(str, 0, 1);
        }
    }

    public boolean isValid() {
        return this.templates[StandardPlural.OTHER_INDEX] != null;
    }

    public String format(double d, NumberFormat numberFormat, PluralRules pluralRules) {
        String format = numberFormat.format(d);
        SimpleFormatter simpleFormatter = this.templates[selectPlural(d, numberFormat, pluralRules).ordinal()];
        if (simpleFormatter == null) {
            simpleFormatter = this.templates[StandardPlural.OTHER_INDEX];
        }
        return simpleFormatter.format(format);
    }

    public SimpleFormatter getByVariant(CharSequence charSequence) {
        int indexOrOtherIndexFromString = StandardPlural.indexOrOtherIndexFromString(charSequence);
        SimpleFormatter simpleFormatter = this.templates[indexOrOtherIndexFromString];
        return (simpleFormatter != null || indexOrOtherIndexFromString == StandardPlural.OTHER_INDEX) ? simpleFormatter : this.templates[StandardPlural.OTHER_INDEX];
    }

    public static StandardPlural selectPlural(double d, NumberFormat numberFormat, PluralRules pluralRules) {
        String str;
        if (numberFormat instanceof DecimalFormat) {
            str = pluralRules.select(((DecimalFormat) numberFormat).getFixedDecimal(d));
        } else {
            str = pluralRules.select(d);
        }
        return StandardPlural.orOtherFromString(str);
    }

    public static StringBuilder format(String str, CharSequence charSequence, StringBuilder sb, FieldPosition fieldPosition) {
        int[] iArr = new int[1];
        SimpleFormatterImpl.formatAndAppend(str, sb, iArr, new CharSequence[]{charSequence});
        if (!(fieldPosition.getBeginIndex() == 0 && fieldPosition.getEndIndex() == 0)) {
            if (iArr[0] >= 0) {
                fieldPosition.setBeginIndex(fieldPosition.getBeginIndex() + iArr[0]);
                fieldPosition.setEndIndex(fieldPosition.getEndIndex() + iArr[0]);
            } else {
                fieldPosition.setBeginIndex(0);
                fieldPosition.setEndIndex(0);
            }
        }
        return sb;
    }
}
