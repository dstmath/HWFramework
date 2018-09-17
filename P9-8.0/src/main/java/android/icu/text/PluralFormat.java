package android.icu.text;

import android.icu.impl.Utility;
import android.icu.text.MessagePattern.Part;
import android.icu.text.MessagePattern.Part.Type;
import android.icu.text.PluralRules.FixedDecimal;
import android.icu.text.PluralRules.PluralType;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Map;

public class PluralFormat extends UFormat {
    static final /* synthetic */ boolean -assertionsDisabled = (PluralFormat.class.desiredAssertionStatus() ^ 1);
    private static final long serialVersionUID = 1;
    private transient MessagePattern msgPattern;
    private NumberFormat numberFormat;
    private transient double offset;
    private Map<String, String> parsedValues;
    private String pattern;
    private PluralRules pluralRules;
    private transient PluralSelectorAdapter pluralRulesWrapper;
    private ULocale ulocale;

    interface PluralSelector {
        String select(Object obj, double d);
    }

    private final class PluralSelectorAdapter implements PluralSelector {
        static final /* synthetic */ boolean -assertionsDisabled = (PluralSelectorAdapter.class.desiredAssertionStatus() ^ 1);
        final /* synthetic */ boolean $assertionsDisabled;

        /* synthetic */ PluralSelectorAdapter(PluralFormat this$0, PluralSelectorAdapter -this1) {
            this();
        }

        private PluralSelectorAdapter() {
        }

        public String select(Object context, double number) {
            FixedDecimal dec = (FixedDecimal) context;
            if (!-assertionsDisabled) {
                double d = dec.source;
                if (dec.isNegative) {
                    number = -number;
                }
                if ((d == number ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            return PluralFormat.this.pluralRules.select(dec);
        }
    }

    public PluralFormat() {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter(this, null);
        init(null, PluralType.CARDINAL, ULocale.getDefault(Category.FORMAT), null);
    }

    public PluralFormat(ULocale ulocale) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter(this, null);
        init(null, PluralType.CARDINAL, ulocale, null);
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
        this.pluralRulesWrapper = new PluralSelectorAdapter(this, null);
        init(rules, PluralType.CARDINAL, ULocale.getDefault(Category.FORMAT), null);
    }

    public PluralFormat(ULocale ulocale, PluralRules rules) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter(this, null);
        init(rules, PluralType.CARDINAL, ulocale, null);
    }

    public PluralFormat(Locale locale, PluralRules rules) {
        this(ULocale.forLocale(locale), rules);
    }

    public PluralFormat(ULocale ulocale, PluralType type) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter(this, null);
        init(null, type, ulocale, null);
    }

    public PluralFormat(Locale locale, PluralType type) {
        this(ULocale.forLocale(locale), type);
    }

    public PluralFormat(String pattern) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter(this, null);
        init(null, PluralType.CARDINAL, ULocale.getDefault(Category.FORMAT), null);
        applyPattern(pattern);
    }

    public PluralFormat(ULocale ulocale, String pattern) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter(this, null);
        init(null, PluralType.CARDINAL, ulocale, null);
        applyPattern(pattern);
    }

    public PluralFormat(PluralRules rules, String pattern) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter(this, null);
        init(rules, PluralType.CARDINAL, ULocale.getDefault(Category.FORMAT), null);
        applyPattern(pattern);
    }

    public PluralFormat(ULocale ulocale, PluralRules rules, String pattern) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter(this, null);
        init(rules, PluralType.CARDINAL, ulocale, null);
        applyPattern(pattern);
    }

    public PluralFormat(ULocale ulocale, PluralType type, String pattern) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter(this, null);
        init(null, type, ulocale, null);
        applyPattern(pattern);
    }

    PluralFormat(ULocale ulocale, PluralType type, String pattern, NumberFormat numberFormat) {
        this.ulocale = null;
        this.pluralRules = null;
        this.pattern = null;
        this.parsedValues = null;
        this.numberFormat = null;
        this.offset = 0.0d;
        this.pluralRulesWrapper = new PluralSelectorAdapter(this, null);
        init(null, type, ulocale, numberFormat);
        applyPattern(pattern);
    }

    private void init(PluralRules rules, PluralType type, ULocale locale, NumberFormat numberFormat) {
        this.ulocale = locale;
        if (rules == null) {
            rules = PluralRules.forLocale(this.ulocale, type);
        }
        this.pluralRules = rules;
        resetPattern();
        if (numberFormat == null) {
            numberFormat = NumberFormat.getInstance(this.ulocale);
        }
        this.numberFormat = numberFormat;
    }

    private void resetPattern() {
        this.pattern = null;
        if (this.msgPattern != null) {
            this.msgPattern.clear();
        }
        this.offset = 0.0d;
    }

    public void applyPattern(String pattern) {
        this.pattern = pattern;
        if (this.msgPattern == null) {
            this.msgPattern = new MessagePattern();
        }
        try {
            this.msgPattern.parsePluralStyle(pattern);
            this.offset = this.msgPattern.getPluralOffset(0);
        } catch (RuntimeException e) {
            resetPattern();
            throw e;
        }
    }

    public String toPattern() {
        return this.pattern;
    }

    static int findSubMessage(MessagePattern pattern, int partIndex, PluralSelector selector, Object context, double number) {
        double offset;
        int count = pattern.countParts();
        Part part = pattern.getPart(partIndex);
        if (part.getType().hasNumericValue()) {
            offset = pattern.getNumericValue(part);
            partIndex++;
        } else {
            offset = 0.0d;
        }
        String keyword = null;
        boolean haveKeywordMatch = false;
        int msgStart = 0;
        while (true) {
            int partIndex2 = partIndex + 1;
            part = pattern.getPart(partIndex);
            Type type = part.getType();
            if (type == Type.ARG_LIMIT) {
                partIndex = partIndex2;
                break;
            } else if (-assertionsDisabled || type == Type.ARG_SELECTOR) {
                if (pattern.getPartType(partIndex2).hasNumericValue()) {
                    partIndex = partIndex2 + 1;
                    if (number == pattern.getNumericValue(pattern.getPart(partIndex2))) {
                        return partIndex;
                    }
                }
                if (!haveKeywordMatch) {
                    if (!pattern.partSubstringMatches(part, "other")) {
                        if (keyword == null) {
                            keyword = selector.select(context, number - offset);
                            if (msgStart != 0 && keyword.equals("other")) {
                                haveKeywordMatch = true;
                            }
                        }
                        if (haveKeywordMatch) {
                            partIndex = partIndex2;
                        } else if (pattern.partSubstringMatches(part, keyword)) {
                            msgStart = partIndex2;
                            haveKeywordMatch = true;
                            partIndex = partIndex2;
                        }
                    } else if (msgStart == 0) {
                        msgStart = partIndex2;
                        if (keyword == null) {
                            partIndex = partIndex2;
                        } else if (keyword.equals("other")) {
                            haveKeywordMatch = true;
                            partIndex = partIndex2;
                        }
                    }
                }
                partIndex = partIndex2;
                partIndex = pattern.getLimitPartIndex(partIndex) + 1;
                if (partIndex >= count) {
                    break;
                }
            } else {
                throw new AssertionError();
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
        if (this.msgPattern == null || this.msgPattern.countParts() == 0) {
            return this.numberFormat.format(numberObject);
        }
        String numberString;
        FixedDecimal dec;
        int index;
        double numberMinusOffset = number - this.offset;
        if (this.offset == 0.0d) {
            numberString = this.numberFormat.format(numberObject);
        } else {
            numberString = this.numberFormat.format(numberMinusOffset);
        }
        if (this.numberFormat instanceof DecimalFormat) {
            dec = ((DecimalFormat) this.numberFormat).getFixedDecimal(numberMinusOffset);
        } else {
            dec = new FixedDecimal(numberMinusOffset);
        }
        int partIndex = findSubMessage(this.msgPattern, 0, this.pluralRulesWrapper, dec, number);
        StringBuilder result = null;
        int prevIndex = this.msgPattern.getPart(partIndex).getLimit();
        while (true) {
            partIndex++;
            Part part = this.msgPattern.getPart(partIndex);
            Type type = part.getType();
            index = part.getIndex();
            if (type == Type.MSG_LIMIT) {
                break;
            } else if (type == Type.REPLACE_NUMBER || (type == Type.SKIP_SYNTAX && this.msgPattern.jdkAposMode())) {
                if (result == null) {
                    result = new StringBuilder();
                }
                result.append(this.pattern, prevIndex, index);
                if (type == Type.REPLACE_NUMBER) {
                    result.append(numberString);
                }
                prevIndex = part.getLimit();
            } else if (type == Type.ARG_START) {
                if (result == null) {
                    result = new StringBuilder();
                }
                result.append(this.pattern, prevIndex, index);
                prevIndex = index;
                partIndex = this.msgPattern.getLimitPartIndex(partIndex);
                index = this.msgPattern.getPart(partIndex).getLimit();
                MessagePattern.appendReducedApostrophes(this.pattern, prevIndex, index, result);
                prevIndex = index;
            }
        }
        if (result == null) {
            return this.pattern.substring(prevIndex, index);
        }
        return result.append(this.pattern, prevIndex, index).toString();
    }

    public Number parse(String text, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }

    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }

    String parseType(String source, RbnfLenientScanner scanner, FieldPosition pos) {
        if (this.msgPattern == null || this.msgPattern.countParts() == 0) {
            pos.setBeginIndex(-1);
            pos.setEndIndex(-1);
            return null;
        }
        int count = this.msgPattern.countParts();
        int startingAt = pos.getBeginIndex();
        if (startingAt < 0) {
            startingAt = 0;
        }
        String keyword = null;
        String matchedWord = null;
        int matchedIndex = -1;
        int partIndex = 0;
        while (partIndex < count) {
            int partIndex2 = partIndex + 1;
            if (this.msgPattern.getPart(partIndex).getType() != Type.ARG_SELECTOR) {
                partIndex = partIndex2;
            } else {
                partIndex = partIndex2 + 1;
                Part partStart = this.msgPattern.getPart(partIndex2);
                if (partStart.getType() == Type.MSG_START) {
                    partIndex2 = partIndex + 1;
                    Part partLimit = this.msgPattern.getPart(partIndex);
                    if (partLimit.getType() != Type.MSG_LIMIT) {
                        partIndex = partIndex2;
                    } else {
                        int currMatchIndex;
                        String currArg = this.pattern.substring(partStart.getLimit(), partLimit.getIndex());
                        if (scanner != null) {
                            currMatchIndex = scanner.findText(source, currArg, startingAt)[0];
                        } else {
                            currMatchIndex = source.indexOf(currArg, startingAt);
                        }
                        if (currMatchIndex >= 0 && currMatchIndex >= matchedIndex && (matchedWord == null || currArg.length() > matchedWord.length())) {
                            matchedIndex = currMatchIndex;
                            matchedWord = currArg;
                            keyword = this.pattern.substring(partStart.getLimit(), partLimit.getIndex());
                        }
                        partIndex = partIndex2;
                    }
                }
            }
        }
        if (keyword != null) {
            pos.setBeginIndex(matchedIndex);
            pos.setEndIndex(matchedWord.length() + matchedIndex);
            return keyword;
        }
        pos.setBeginIndex(-1);
        pos.setEndIndex(-1);
        return null;
    }

    @Deprecated
    public void setLocale(ULocale ulocale) {
        if (ulocale == null) {
            ulocale = ULocale.getDefault(Category.FORMAT);
        }
        init(null, PluralType.CARDINAL, ulocale, null);
    }

    public void setNumberFormat(NumberFormat format) {
        this.numberFormat = format;
    }

    public boolean equals(Object rhs) {
        boolean z = false;
        if (this == rhs) {
            return true;
        }
        if (rhs == null || getClass() != rhs.getClass()) {
            return false;
        }
        PluralFormat pf = (PluralFormat) rhs;
        if (Utility.objectEquals(this.ulocale, pf.ulocale) && Utility.objectEquals(this.pluralRules, pf.pluralRules) && Utility.objectEquals(this.msgPattern, pf.msgPattern)) {
            z = Utility.objectEquals(this.numberFormat, pf.numberFormat);
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
        StringBuilder buf = new StringBuilder();
        buf.append("locale=").append(this.ulocale);
        buf.append(", rules='").append(this.pluralRules).append("'");
        buf.append(", pattern='").append(this.pattern).append("'");
        buf.append(", format='").append(this.numberFormat).append("'");
        return buf.toString();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.pluralRulesWrapper = new PluralSelectorAdapter(this, null);
        this.parsedValues = null;
        if (this.pattern != null) {
            applyPattern(this.pattern);
        }
    }
}
