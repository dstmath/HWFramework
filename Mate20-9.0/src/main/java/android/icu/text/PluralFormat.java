package android.icu.text;

import android.icu.impl.Utility;
import android.icu.text.MessagePattern;
import android.icu.text.PluralRules;
import android.icu.util.ULocale;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Map;

public class PluralFormat extends UFormat {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final long serialVersionUID = 1;
    private transient MessagePattern msgPattern;
    private NumberFormat numberFormat;
    private transient double offset;
    private Map<String, String> parsedValues;
    private String pattern;
    /* access modifiers changed from: private */
    public PluralRules pluralRules;
    private transient PluralSelectorAdapter pluralRulesWrapper;
    private ULocale ulocale;

    interface PluralSelector {
        String select(Object obj, double d);
    }

    private final class PluralSelectorAdapter implements PluralSelector {
        private PluralSelectorAdapter() {
        }

        public String select(Object context, double number) {
            return PluralFormat.this.pluralRules.select((PluralRules.IFixedDecimal) context);
        }
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

    public PluralFormat(ULocale ulocale2) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(null, PluralRules.PluralType.CARDINAL, ulocale2, null);
    }

    public PluralFormat(Locale locale) {
        this(ULocale.forLocale(locale));
    }

    public PluralFormat(PluralRules rules) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(rules, PluralRules.PluralType.CARDINAL, ULocale.getDefault(ULocale.Category.FORMAT), null);
    }

    public PluralFormat(ULocale ulocale2, PluralRules rules) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(rules, PluralRules.PluralType.CARDINAL, ulocale2, null);
    }

    public PluralFormat(Locale locale, PluralRules rules) {
        this(ULocale.forLocale(locale), rules);
    }

    public PluralFormat(ULocale ulocale2, PluralRules.PluralType type) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(null, type, ulocale2, null);
    }

    public PluralFormat(Locale locale, PluralRules.PluralType type) {
        this(ULocale.forLocale(locale), type);
    }

    public PluralFormat(String pattern2) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(null, PluralRules.PluralType.CARDINAL, ULocale.getDefault(ULocale.Category.FORMAT), null);
        applyPattern(pattern2);
    }

    public PluralFormat(ULocale ulocale2, String pattern2) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(null, PluralRules.PluralType.CARDINAL, ulocale2, null);
        applyPattern(pattern2);
    }

    public PluralFormat(PluralRules rules, String pattern2) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(rules, PluralRules.PluralType.CARDINAL, ULocale.getDefault(ULocale.Category.FORMAT), null);
        applyPattern(pattern2);
    }

    public PluralFormat(ULocale ulocale2, PluralRules rules, String pattern2) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(rules, PluralRules.PluralType.CARDINAL, ulocale2, null);
        applyPattern(pattern2);
    }

    public PluralFormat(ULocale ulocale2, PluralRules.PluralType type, String pattern2) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(null, type, ulocale2, null);
        applyPattern(pattern2);
    }

    PluralFormat(ULocale ulocale2, PluralRules.PluralType type, String pattern2, NumberFormat numberFormat2) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        init(null, type, ulocale2, numberFormat2);
        applyPattern(pattern2);
    }

    private void init(PluralRules rules, PluralRules.PluralType type, ULocale locale, NumberFormat numberFormat2) {
        PluralRules pluralRules2;
        this.ulocale = locale;
        if (rules == null) {
            pluralRules2 = PluralRules.forLocale(this.ulocale, type);
        } else {
            pluralRules2 = rules;
        }
        this.pluralRules = pluralRules2;
        resetPattern();
        this.numberFormat = numberFormat2 == null ? NumberFormat.getInstance(this.ulocale) : numberFormat2;
    }

    private void resetPattern() {
        this.pattern = null;
        if (this.msgPattern != null) {
            this.msgPattern.clear();
        }
        this.offset = 0.0d;
    }

    public void applyPattern(String pattern2) {
        this.pattern = pattern2;
        if (this.msgPattern == null) {
            this.msgPattern = new MessagePattern();
        }
        try {
            this.msgPattern.parsePluralStyle(pattern2);
            this.offset = this.msgPattern.getPluralOffset(0);
        } catch (RuntimeException e) {
            resetPattern();
            throw e;
        }
    }

    public String toPattern() {
        return this.pattern;
    }

    static int findSubMessage(MessagePattern pattern2, int partIndex, PluralSelector selector, Object context, double number) {
        int partIndex2;
        double offset2;
        MessagePattern messagePattern = pattern2;
        int count = messagePattern.countParts();
        MessagePattern.Part part = messagePattern.getPart(partIndex);
        if (part.getType().hasNumericValue()) {
            offset2 = messagePattern.getNumericValue(part);
            partIndex2 = partIndex + 1;
        } else {
            offset2 = 0.0d;
            partIndex2 = partIndex;
        }
        String keyword = null;
        boolean haveKeywordMatch = false;
        int msgStart = 0;
        while (true) {
            int partIndex3 = partIndex2 + 1;
            MessagePattern.Part part2 = messagePattern.getPart(partIndex2);
            if (part2.getType() != MessagePattern.Part.Type.ARG_LIMIT) {
                if (messagePattern.getPartType(partIndex3).hasNumericValue()) {
                    int partIndex4 = partIndex3 + 1;
                    if (number == messagePattern.getNumericValue(messagePattern.getPart(partIndex3))) {
                        return partIndex4;
                    }
                    PluralSelector pluralSelector = selector;
                    Object obj = context;
                    partIndex3 = partIndex4;
                } else {
                    if (!haveKeywordMatch) {
                        if (!messagePattern.partSubstringMatches(part2, PluralRules.KEYWORD_OTHER)) {
                            if (keyword == null) {
                                keyword = selector.select(context, number - offset2);
                                if (msgStart != 0 && keyword.equals(PluralRules.KEYWORD_OTHER)) {
                                    haveKeywordMatch = true;
                                }
                            } else {
                                PluralSelector pluralSelector2 = selector;
                                Object obj2 = context;
                            }
                            if (!haveKeywordMatch && messagePattern.partSubstringMatches(part2, keyword)) {
                                msgStart = partIndex3;
                                haveKeywordMatch = true;
                            }
                        } else if (msgStart == 0) {
                            msgStart = partIndex3;
                            if (keyword != null && keyword.equals(PluralRules.KEYWORD_OTHER)) {
                                haveKeywordMatch = true;
                            }
                        }
                    }
                    PluralSelector pluralSelector3 = selector;
                    Object obj3 = context;
                }
                partIndex2 = messagePattern.getLimitPartIndex(partIndex3) + 1;
                if (partIndex2 >= count) {
                    int i = partIndex2;
                    break;
                }
            } else {
                PluralSelector pluralSelector4 = selector;
                Object obj4 = context;
                break;
            }
        }
        return msgStart;
    }

    public final String format(double number) {
        return format(Double.valueOf(number), number);
    }

    public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
        if (number instanceof Number) {
            Number numberObject = (Number) number;
            toAppendTo.append(format(numberObject, numberObject.doubleValue()));
            return toAppendTo;
        }
        throw new IllegalArgumentException("'" + number + "' is not a Number");
    }

    private String format(Number numberObject, double number) {
        String numberString;
        PluralRules.IFixedDecimal dec;
        int index;
        if (this.msgPattern == null || this.msgPattern.countParts() == 0) {
            return this.numberFormat.format(numberObject);
        }
        double numberMinusOffset = number - this.offset;
        if (this.offset == 0.0d) {
            numberString = this.numberFormat.format(numberObject);
        } else {
            numberString = this.numberFormat.format(numberMinusOffset);
        }
        if (this.numberFormat instanceof DecimalFormat) {
            dec = ((DecimalFormat) this.numberFormat).getFixedDecimal(numberMinusOffset);
        } else {
            dec = new PluralRules.FixedDecimal(numberMinusOffset);
        }
        int partIndex = findSubMessage(this.msgPattern, 0, this.pluralRulesWrapper, dec, number);
        StringBuilder result = null;
        int prevIndex = this.msgPattern.getPart(partIndex).getLimit();
        while (true) {
            partIndex++;
            MessagePattern.Part part = this.msgPattern.getPart(partIndex);
            MessagePattern.Part.Type type = part.getType();
            index = part.getIndex();
            if (type == MessagePattern.Part.Type.MSG_LIMIT) {
                break;
            } else if (type == MessagePattern.Part.Type.REPLACE_NUMBER || (type == MessagePattern.Part.Type.SKIP_SYNTAX && this.msgPattern.jdkAposMode())) {
                if (result == null) {
                    result = new StringBuilder();
                }
                result.append(this.pattern, prevIndex, index);
                if (type == MessagePattern.Part.Type.REPLACE_NUMBER) {
                    result.append(numberString);
                }
                prevIndex = part.getLimit();
            } else if (type == MessagePattern.Part.Type.ARG_START) {
                if (result == null) {
                    result = new StringBuilder();
                }
                result.append(this.pattern, prevIndex, index);
                int prevIndex2 = index;
                partIndex = this.msgPattern.getLimitPartIndex(partIndex);
                int index2 = this.msgPattern.getPart(partIndex).getLimit();
                MessagePattern.appendReducedApostrophes(this.pattern, prevIndex2, index2, result);
                prevIndex = index2;
            }
        }
        if (result == null) {
            return this.pattern.substring(prevIndex, index);
        }
        result.append(this.pattern, prevIndex, index);
        return result.toString();
    }

    public Number parse(String text, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }

    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    public String parseType(String source, RbnfLenientScanner scanner, FieldPosition pos) {
        int currMatchIndex;
        PluralFormat pluralFormat = this;
        String str = source;
        RbnfLenientScanner rbnfLenientScanner = scanner;
        FieldPosition fieldPosition = pos;
        if (pluralFormat.msgPattern == null || pluralFormat.msgPattern.countParts() == 0) {
            fieldPosition.setBeginIndex(-1);
            fieldPosition.setEndIndex(-1);
            return null;
        }
        int count = pluralFormat.msgPattern.countParts();
        int startingAt = pos.getBeginIndex();
        if (startingAt < 0) {
            startingAt = 0;
        }
        String matchedWord = null;
        String keyword = null;
        int partIndex = 0;
        int partIndex2 = -1;
        while (partIndex < count) {
            int partIndex3 = partIndex + 1;
            if (pluralFormat.msgPattern.getPart(partIndex).getType() != MessagePattern.Part.Type.ARG_SELECTOR) {
                partIndex = partIndex3;
            } else {
                int partIndex4 = partIndex3 + 1;
                MessagePattern.Part partStart = pluralFormat.msgPattern.getPart(partIndex3);
                if (partStart.getType() != MessagePattern.Part.Type.MSG_START) {
                    partIndex = partIndex4;
                } else {
                    int partIndex5 = partIndex4 + 1;
                    MessagePattern.Part partLimit = pluralFormat.msgPattern.getPart(partIndex4);
                    if (partLimit.getType() != MessagePattern.Part.Type.MSG_LIMIT) {
                        partIndex = partIndex5;
                    } else {
                        String currArg = pluralFormat.pattern.substring(partStart.getLimit(), partLimit.getIndex());
                        if (rbnfLenientScanner != null) {
                            currMatchIndex = rbnfLenientScanner.findText(str, currArg, startingAt)[0];
                        } else {
                            currMatchIndex = str.indexOf(currArg, startingAt);
                        }
                        if (currMatchIndex >= 0 && currMatchIndex >= partIndex2 && (matchedWord == null || currArg.length() > matchedWord.length())) {
                            keyword = pluralFormat.pattern.substring(partStart.getLimit(), partLimit.getIndex());
                            matchedWord = currArg;
                            partIndex2 = currMatchIndex;
                        }
                        partIndex = partIndex5;
                        pluralFormat = this;
                        str = source;
                    }
                }
            }
        }
        if (keyword != null) {
            fieldPosition.setBeginIndex(partIndex2);
            fieldPosition.setEndIndex(matchedWord.length() + partIndex2);
            return keyword;
        }
        fieldPosition.setBeginIndex(-1);
        fieldPosition.setEndIndex(-1);
        return null;
    }

    @Deprecated
    public void setLocale(ULocale ulocale2) {
        if (ulocale2 == null) {
            ulocale2 = ULocale.getDefault(ULocale.Category.FORMAT);
        }
        init(null, PluralRules.PluralType.CARDINAL, ulocale2, null);
    }

    public void setNumberFormat(NumberFormat format) {
        this.numberFormat = format;
    }

    public boolean equals(Object rhs) {
        boolean z = true;
        if (this == rhs) {
            return true;
        }
        if (rhs == null || getClass() != rhs.getClass()) {
            return false;
        }
        PluralFormat pf = (PluralFormat) rhs;
        if (!Utility.objectEquals(this.ulocale, pf.ulocale) || !Utility.objectEquals(this.pluralRules, pf.pluralRules) || !Utility.objectEquals(this.msgPattern, pf.msgPattern) || !Utility.objectEquals(this.numberFormat, pf.numberFormat)) {
            z = false;
        }
        return z;
    }

    public boolean equals(PluralFormat rhs) {
        return equals((Object) rhs);
    }

    public int hashCode() {
        return this.pluralRules.hashCode() ^ this.parsedValues.hashCode();
    }

    public String toString() {
        return ("locale=" + this.ulocale) + (", rules='" + this.pluralRules + "'") + (", pattern='" + this.pattern + "'") + (", format='" + this.numberFormat + "'");
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.pluralRulesWrapper = new PluralSelectorAdapter();
        this.parsedValues = null;
        if (this.pattern != null) {
            applyPattern(this.pattern);
        }
    }
}
