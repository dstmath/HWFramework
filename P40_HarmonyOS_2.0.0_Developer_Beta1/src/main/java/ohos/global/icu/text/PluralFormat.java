package ohos.global.icu.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import ohos.global.icu.number.FormattedNumber;
import ohos.global.icu.number.LocalizedNumberFormatter;
import ohos.global.icu.text.MessagePattern;
import ohos.global.icu.text.PluralRules;
import ohos.global.icu.util.ULocale;

public class PluralFormat extends UFormat {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final long serialVersionUID = 1;
    private transient MessagePattern msgPattern;
    private NumberFormat numberFormat;
    private transient double offset;
    private Map<String, String> parsedValues;
    private String pattern;
    private PluralRules pluralRules;
    private transient PluralSelectorAdapter pluralRulesWrapper;
    private ULocale ulocale;

    /* access modifiers changed from: package-private */
    public interface PluralSelector {
        String select(Object obj, double d);
    }

    public PluralFormat() {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(null, PluralRules.PluralType.CARDINAL, ULocale.getDefault(ULocale.Category.FORMAT), null);
    }

    public PluralFormat(ULocale uLocale) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(null, PluralRules.PluralType.CARDINAL, uLocale, null);
    }

    public PluralFormat(Locale locale) {
        this(ULocale.forLocale(locale));
    }

    public PluralFormat(PluralRules pluralRules2) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(pluralRules2, PluralRules.PluralType.CARDINAL, ULocale.getDefault(ULocale.Category.FORMAT), null);
    }

    public PluralFormat(ULocale uLocale, PluralRules pluralRules2) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(pluralRules2, PluralRules.PluralType.CARDINAL, uLocale, null);
    }

    public PluralFormat(Locale locale, PluralRules pluralRules2) {
        this(ULocale.forLocale(locale), pluralRules2);
    }

    public PluralFormat(ULocale uLocale, PluralRules.PluralType pluralType) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(null, pluralType, uLocale, null);
    }

    public PluralFormat(Locale locale, PluralRules.PluralType pluralType) {
        this(ULocale.forLocale(locale), pluralType);
    }

    public PluralFormat(String str) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(null, PluralRules.PluralType.CARDINAL, ULocale.getDefault(ULocale.Category.FORMAT), null);
        applyPattern(str);
    }

    public PluralFormat(ULocale uLocale, String str) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(null, PluralRules.PluralType.CARDINAL, uLocale, null);
        applyPattern(str);
    }

    public PluralFormat(PluralRules pluralRules2, String str) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(pluralRules2, PluralRules.PluralType.CARDINAL, ULocale.getDefault(ULocale.Category.FORMAT), null);
        applyPattern(str);
    }

    public PluralFormat(ULocale uLocale, PluralRules pluralRules2, String str) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(pluralRules2, PluralRules.PluralType.CARDINAL, uLocale, null);
        applyPattern(str);
    }

    public PluralFormat(ULocale uLocale, PluralRules.PluralType pluralType, String str) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(null, pluralType, uLocale, null);
        applyPattern(str);
    }

    PluralFormat(ULocale uLocale, PluralRules.PluralType pluralType, String str, NumberFormat numberFormat2) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(null, pluralType, uLocale, numberFormat2);
        applyPattern(str);
    }

    private void init(PluralRules pluralRules2, PluralRules.PluralType pluralType, ULocale uLocale, NumberFormat numberFormat2) {
        this.ulocale = uLocale;
        if (pluralRules2 == null) {
            pluralRules2 = PluralRules.forLocale(this.ulocale, pluralType);
        }
        this.pluralRules = pluralRules2;
        resetPattern();
        if (numberFormat2 == null) {
            numberFormat2 = NumberFormat.getInstance(this.ulocale);
        }
        this.numberFormat = numberFormat2;
    }

    private void resetPattern() {
        this.pattern = null;
        MessagePattern messagePattern = this.msgPattern;
        if (messagePattern != null) {
            messagePattern.clear();
        }
        this.offset = 0.0d;
    }

    public void applyPattern(String str) {
        this.pattern = str;
        if (this.msgPattern == null) {
            this.msgPattern = new MessagePattern();
        }
        try {
            this.msgPattern.parsePluralStyle(str);
            this.offset = this.msgPattern.getPluralOffset(0);
        } catch (RuntimeException e) {
            resetPattern();
            throw e;
        }
    }

    public String toPattern() {
        return this.pattern;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0091, code lost:
        if (r15.partSubstringMatches(r4, r7) != false) goto L_0x0093;
     */
    static int findSubMessage(MessagePattern messagePattern, int i, PluralSelector pluralSelector, Object obj, double d) {
        int i2;
        double d2;
        int countParts = messagePattern.countParts();
        MessagePattern.Part part = messagePattern.getPart(i);
        if (part.getType().hasNumericValue()) {
            d2 = messagePattern.getNumericValue(part);
            i2 = i + 1;
        } else {
            d2 = 0.0d;
            i2 = i;
        }
        boolean z = false;
        String str = null;
        int i3 = 0;
        do {
            int i4 = i2 + 1;
            MessagePattern.Part part2 = messagePattern.getPart(i2);
            if (part2.getType() == MessagePattern.Part.Type.ARG_LIMIT) {
                break;
            }
            if (messagePattern.getPartType(i4).hasNumericValue()) {
                int i5 = i4 + 1;
                if (d == messagePattern.getNumericValue(messagePattern.getPart(i4))) {
                    return i5;
                }
                i4 = i5;
            } else if (!z) {
                if (!messagePattern.partSubstringMatches(part2, "other")) {
                    if (str == null) {
                        str = pluralSelector.select(obj, d - d2);
                        if (i3 != 0 && str.equals("other")) {
                            z = true;
                        }
                    }
                    if (!z) {
                    }
                } else if (i3 == 0) {
                    if (str == null || !str.equals("other")) {
                        i3 = i4;
                    }
                }
                i3 = i4;
                z = true;
            }
            i2 = messagePattern.getLimitPartIndex(i4) + 1;
        } while (i2 < countParts);
        return i3;
    }

    /* access modifiers changed from: private */
    public final class PluralSelectorAdapter implements PluralSelector {
        private PluralSelectorAdapter() {
        }

        @Override // ohos.global.icu.text.PluralFormat.PluralSelector
        public String select(Object obj, double d) {
            return PluralFormat.this.pluralRules.select((PluralRules.IFixedDecimal) obj);
        }
    }

    public final String format(double d) {
        return format(Double.valueOf(d), d);
    }

    @Override // java.text.Format
    public StringBuffer format(Object obj, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        if (obj instanceof Number) {
            Number number = (Number) obj;
            stringBuffer.append(format(number, number.doubleValue()));
            return stringBuffer;
        }
        throw new IllegalArgumentException("'" + obj + "' is not a Number");
    }

    private String format(Number number, double d) {
        Object obj;
        String str;
        int index;
        String str2;
        FormattedNumber formattedNumber;
        MessagePattern messagePattern = this.msgPattern;
        if (messagePattern == null || messagePattern.countParts() == 0) {
            return this.numberFormat.format(number);
        }
        double d2 = this.offset;
        double d3 = d - d2;
        NumberFormat numberFormat2 = this.numberFormat;
        if (numberFormat2 instanceof DecimalFormat) {
            LocalizedNumberFormatter numberFormatter = ((DecimalFormat) numberFormat2).toNumberFormatter();
            if (this.offset == 0.0d) {
                formattedNumber = numberFormatter.format(number);
            } else {
                formattedNumber = numberFormatter.format(d3);
            }
            str = formattedNumber.toString();
            obj = formattedNumber.getFixedDecimal();
        } else {
            if (d2 == 0.0d) {
                str2 = numberFormat2.format(number);
            } else {
                str2 = numberFormat2.format(d3);
            }
            str = str2;
            obj = new PluralRules.FixedDecimal(d3);
        }
        int findSubMessage = findSubMessage(this.msgPattern, 0, this.pluralRulesWrapper, obj, d);
        StringBuilder sb = null;
        int limit = this.msgPattern.getPart(findSubMessage).getLimit();
        while (true) {
            findSubMessage++;
            MessagePattern.Part part = this.msgPattern.getPart(findSubMessage);
            MessagePattern.Part.Type type = part.getType();
            index = part.getIndex();
            if (type == MessagePattern.Part.Type.MSG_LIMIT) {
                break;
            } else if (type == MessagePattern.Part.Type.REPLACE_NUMBER || (type == MessagePattern.Part.Type.SKIP_SYNTAX && this.msgPattern.jdkAposMode())) {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append((CharSequence) this.pattern, limit, index);
                if (type == MessagePattern.Part.Type.REPLACE_NUMBER) {
                    sb.append(str);
                }
                limit = part.getLimit();
            } else if (type == MessagePattern.Part.Type.ARG_START) {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append((CharSequence) this.pattern, limit, index);
                findSubMessage = this.msgPattern.getLimitPartIndex(findSubMessage);
                limit = this.msgPattern.getPart(findSubMessage).getLimit();
                MessagePattern.appendReducedApostrophes(this.pattern, index, limit, sb);
            }
        }
        if (sb == null) {
            return this.pattern.substring(limit, index);
        }
        sb.append((CharSequence) this.pattern, limit, index);
        return sb.toString();
    }

    public Number parse(String str, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }

    @Override // java.text.Format
    public Object parseObject(String str, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public String parseType(String str, RbnfLenientScanner rbnfLenientScanner, FieldPosition fieldPosition) {
        int i;
        MessagePattern messagePattern = this.msgPattern;
        if (messagePattern == null || messagePattern.countParts() == 0) {
            fieldPosition.setBeginIndex(-1);
            fieldPosition.setEndIndex(-1);
            return null;
        }
        int countParts = this.msgPattern.countParts();
        int beginIndex = fieldPosition.getBeginIndex();
        char c = 0;
        if (beginIndex < 0) {
            beginIndex = 0;
        }
        int i2 = 0;
        String str2 = null;
        int i3 = -1;
        String str3 = null;
        while (i2 < countParts) {
            int i4 = i2 + 1;
            if (this.msgPattern.getPart(i2).getType() != MessagePattern.Part.Type.ARG_SELECTOR) {
                i2 = i4;
            } else {
                int i5 = i4 + 1;
                MessagePattern.Part part = this.msgPattern.getPart(i4);
                if (part.getType() != MessagePattern.Part.Type.MSG_START) {
                    i2 = i5;
                } else {
                    int i6 = i5 + 1;
                    MessagePattern.Part part2 = this.msgPattern.getPart(i5);
                    if (part2.getType() != MessagePattern.Part.Type.MSG_LIMIT) {
                        i2 = i6;
                    } else {
                        String substring = this.pattern.substring(part.getLimit(), part2.getIndex());
                        if (rbnfLenientScanner != null) {
                            i = rbnfLenientScanner.findText(str, substring, beginIndex)[c];
                        } else {
                            i = str.indexOf(substring, beginIndex);
                        }
                        if (i >= 0 && i >= i3 && (str3 == null || substring.length() > str3.length())) {
                            str3 = substring;
                            i3 = i;
                            str2 = this.pattern.substring(part.getLimit(), part2.getIndex());
                        }
                        i2 = i6;
                        c = 0;
                    }
                }
            }
        }
        if (str2 != null) {
            fieldPosition.setBeginIndex(i3);
            fieldPosition.setEndIndex(i3 + str3.length());
            return str2;
        }
        fieldPosition.setBeginIndex(-1);
        fieldPosition.setEndIndex(-1);
        return null;
    }

    @Deprecated
    public void setLocale(ULocale uLocale) {
        if (uLocale == null) {
            uLocale = ULocale.getDefault(ULocale.Category.FORMAT);
        }
        init(null, PluralRules.PluralType.CARDINAL, uLocale, null);
    }

    public void setNumberFormat(NumberFormat numberFormat2) {
        this.numberFormat = numberFormat2;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PluralFormat pluralFormat = (PluralFormat) obj;
        return Objects.equals(this.ulocale, pluralFormat.ulocale) && Objects.equals(this.pluralRules, pluralFormat.pluralRules) && Objects.equals(this.msgPattern, pluralFormat.msgPattern) && Objects.equals(this.numberFormat, pluralFormat.numberFormat);
    }

    public boolean equals(PluralFormat pluralFormat) {
        return equals((Object) pluralFormat);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.parsedValues.hashCode() ^ this.pluralRules.hashCode();
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("locale=" + this.ulocale);
        sb.append(", rules='" + this.pluralRules + "'");
        sb.append(", pattern='" + this.pattern + "'");
        sb.append(", format='" + this.numberFormat + "'");
        return sb.toString();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        this.parsedValues = null;
        String str = this.pattern;
        if (str != null) {
            applyPattern(str);
        }
    }
}
