package android.icu.text;

import android.icu.impl.SimpleFormatterImpl;
import android.icu.impl.StandardPlural;
import android.icu.text.PluralRules.FixedDecimal;
import java.text.FieldPosition;

class QuantityFormatter {
    static final /* synthetic */ boolean -assertionsDisabled = (QuantityFormatter.class.desiredAssertionStatus() ^ 1);
    private final SimpleFormatter[] templates = new SimpleFormatter[StandardPlural.COUNT];

    public void addIfAbsent(CharSequence variant, String template) {
        int idx = StandardPlural.indexFromString(variant);
        if (this.templates[idx] == null) {
            this.templates[idx] = SimpleFormatter.compileMinMaxArguments(template, 0, 1);
        }
    }

    public boolean isValid() {
        return this.templates[StandardPlural.OTHER_INDEX] != null;
    }

    public String format(double number, NumberFormat numberFormat, PluralRules pluralRules) {
        String formatStr = numberFormat.format(number);
        SimpleFormatter formatter = this.templates[selectPlural(number, numberFormat, pluralRules).ordinal()];
        if (formatter == null) {
            formatter = this.templates[StandardPlural.OTHER_INDEX];
            if (!-assertionsDisabled && formatter == null) {
                throw new AssertionError();
            }
        }
        return formatter.format(formatStr);
    }

    public SimpleFormatter getByVariant(CharSequence variant) {
        if (-assertionsDisabled || isValid()) {
            int idx = StandardPlural.indexOrOtherIndexFromString(variant);
            SimpleFormatter template = this.templates[idx];
            if (template != null || idx == StandardPlural.OTHER_INDEX) {
                return template;
            }
            return this.templates[StandardPlural.OTHER_INDEX];
        }
        throw new AssertionError();
    }

    public static StandardPlural selectPlural(double number, NumberFormat numberFormat, PluralRules rules) {
        String pluralKeyword;
        if (numberFormat instanceof DecimalFormat) {
            pluralKeyword = rules.select(((DecimalFormat) numberFormat).getFixedDecimal(number));
        } else {
            pluralKeyword = rules.select(number);
        }
        return StandardPlural.orOtherFromString(pluralKeyword);
    }

    public static StandardPlural selectPlural(Number number, NumberFormat fmt, PluralRules rules, StringBuffer formattedNumber, FieldPosition pos) {
        FieldPosition fpos = new UFieldPosition(pos.getFieldAttribute(), pos.getField());
        fmt.format((Object) number, formattedNumber, fpos);
        String pluralKeyword = rules.select(new FixedDecimal(number.doubleValue(), fpos.getCountVisibleFractionDigits(), fpos.getFractionDigits()));
        pos.setBeginIndex(fpos.getBeginIndex());
        pos.setEndIndex(fpos.getEndIndex());
        return StandardPlural.orOtherFromString(pluralKeyword);
    }

    public static StringBuilder format(String compiledPattern, CharSequence value, StringBuilder appendTo, FieldPosition pos) {
        int[] offsets = new int[1];
        SimpleFormatterImpl.formatAndAppend(compiledPattern, appendTo, offsets, value);
        if (!(pos.getBeginIndex() == 0 && pos.getEndIndex() == 0)) {
            if (offsets[0] >= 0) {
                pos.setBeginIndex(pos.getBeginIndex() + offsets[0]);
                pos.setEndIndex(pos.getEndIndex() + offsets[0]);
            } else {
                pos.setBeginIndex(0);
                pos.setEndIndex(0);
            }
        }
        return appendTo;
    }
}
