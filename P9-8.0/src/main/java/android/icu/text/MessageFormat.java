package android.icu.text;

import android.icu.impl.PatternProps;
import android.icu.impl.Utility;
import android.icu.text.MessagePattern.ApostropheMode;
import android.icu.text.MessagePattern.ArgType;
import android.icu.text.MessagePattern.Part;
import android.icu.text.MessagePattern.Part.Type;
import android.icu.text.PluralRules.PluralType;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.text.ChoiceFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MessageFormat extends UFormat {
    static final /* synthetic */ boolean -assertionsDisabled = (MessageFormat.class.desiredAssertionStatus() ^ 1);
    private static final char CURLY_BRACE_LEFT = '{';
    private static final char CURLY_BRACE_RIGHT = '}';
    private static final int DATE_MODIFIER_EMPTY = 0;
    private static final int DATE_MODIFIER_FULL = 4;
    private static final int DATE_MODIFIER_LONG = 3;
    private static final int DATE_MODIFIER_MEDIUM = 2;
    private static final int DATE_MODIFIER_SHORT = 1;
    private static final int MODIFIER_CURRENCY = 1;
    private static final int MODIFIER_EMPTY = 0;
    private static final int MODIFIER_INTEGER = 3;
    private static final int MODIFIER_PERCENT = 2;
    private static final char SINGLE_QUOTE = '\'';
    private static final int STATE_INITIAL = 0;
    private static final int STATE_IN_QUOTE = 2;
    private static final int STATE_MSG_ELEMENT = 3;
    private static final int STATE_SINGLE_QUOTE = 1;
    private static final int TYPE_DATE = 1;
    private static final int TYPE_DURATION = 5;
    private static final int TYPE_NUMBER = 0;
    private static final int TYPE_ORDINAL = 4;
    private static final int TYPE_SPELLOUT = 3;
    private static final int TYPE_TIME = 2;
    private static final String[] dateModifierList = new String[]{"", "short", "medium", "long", "full"};
    private static final String[] modifierList = new String[]{"", "currency", "percent", "integer"};
    private static final Locale rootLocale = new Locale("");
    static final long serialVersionUID = 7136212545847378652L;
    private static final String[] typeList = new String[]{"number", "date", "time", "spellout", "ordinal", "duration"};
    private transient Map<Integer, Format> cachedFormatters;
    private transient Set<Integer> customFormatArgStarts;
    private transient MessagePattern msgPattern;
    private transient PluralSelectorProvider ordinalProvider;
    private transient PluralSelectorProvider pluralProvider;
    private transient DateFormat stockDateFormatter;
    private transient NumberFormat stockNumberFormatter;
    private transient ULocale ulocale;

    private static final class AppendableWrapper {
        private Appendable app;
        private List<AttributeAndPosition> attributes = null;
        private int length;

        public AppendableWrapper(StringBuilder sb) {
            this.app = sb;
            this.length = sb.length();
        }

        public AppendableWrapper(StringBuffer sb) {
            this.app = sb;
            this.length = sb.length();
        }

        public void useAttributes() {
            this.attributes = new ArrayList();
        }

        public void append(CharSequence s) {
            try {
                this.app.append(s);
                this.length += s.length();
            } catch (Throwable e) {
                throw new ICUUncheckedIOException(e);
            }
        }

        public void append(CharSequence s, int start, int limit) {
            try {
                this.app.append(s, start, limit);
                this.length += limit - start;
            } catch (Throwable e) {
                throw new ICUUncheckedIOException(e);
            }
        }

        public void append(CharacterIterator iterator) {
            this.length += append(this.app, iterator);
        }

        public static int append(Appendable result, CharacterIterator iterator) {
            try {
                int start = iterator.getBeginIndex();
                int limit = iterator.getEndIndex();
                int length = limit - start;
                if (start < limit) {
                    result.append(iterator.first());
                    while (true) {
                        start++;
                        if (start >= limit) {
                            break;
                        }
                        result.append(iterator.next());
                    }
                }
                return length;
            } catch (Throwable e) {
                throw new ICUUncheckedIOException(e);
            }
        }

        public void formatAndAppend(Format formatter, Object arg) {
            if (this.attributes == null) {
                append(formatter.format(arg));
                return;
            }
            CharacterIterator formattedArg = formatter.formatToCharacterIterator(arg);
            int prevLength = this.length;
            append(formattedArg);
            formattedArg.first();
            int start = formattedArg.getIndex();
            int limit = formattedArg.getEndIndex();
            int offset = prevLength - start;
            while (start < limit) {
                Map<Attribute, Object> map = formattedArg.getAttributes();
                int runLimit = formattedArg.getRunLimit();
                if (map.size() != 0) {
                    for (Entry<Attribute, Object> entry : map.entrySet()) {
                        this.attributes.add(new AttributeAndPosition((Attribute) entry.getKey(), entry.getValue(), offset + start, offset + runLimit));
                    }
                }
                start = runLimit;
                formattedArg.setIndex(runLimit);
            }
        }

        public void formatAndAppend(Format formatter, Object arg, String argString) {
            if (this.attributes != null || argString == null) {
                formatAndAppend(formatter, arg);
            } else {
                append((CharSequence) argString);
            }
        }
    }

    private static final class AttributeAndPosition {
        private Attribute key;
        private int limit;
        private int start;
        private Object value;

        public AttributeAndPosition(Object fieldValue, int startIndex, int limitIndex) {
            init(Field.ARGUMENT, fieldValue, startIndex, limitIndex);
        }

        public AttributeAndPosition(Attribute field, Object fieldValue, int startIndex, int limitIndex) {
            init(field, fieldValue, startIndex, limitIndex);
        }

        public void init(Attribute field, Object fieldValue, int startIndex, int limitIndex) {
            this.key = field;
            this.value = fieldValue;
            this.start = startIndex;
            this.limit = limitIndex;
        }
    }

    public static class Field extends java.text.Format.Field {
        public static final Field ARGUMENT = new Field("message argument field");
        private static final long serialVersionUID = 7510380454602616157L;

        protected Field(String name) {
            super(name);
        }

        protected Object readResolve() throws InvalidObjectException {
            if (getClass() != Field.class) {
                throw new InvalidObjectException("A subclass of MessageFormat.Field must implement readResolve.");
            } else if (getName().equals(ARGUMENT.getName())) {
                return ARGUMENT;
            } else {
                throw new InvalidObjectException("Unknown attribute name.");
            }
        }
    }

    private static final class PluralSelectorContext {
        String argName;
        boolean forReplaceNumber;
        Format formatter;
        Number number;
        int numberArgIndex;
        String numberString;
        double offset;
        int startIndex;

        /* synthetic */ PluralSelectorContext(int start, String name, Number num, double off, PluralSelectorContext -this4) {
            this(start, name, num, off);
        }

        private PluralSelectorContext(int start, String name, Number num, double off) {
            this.startIndex = start;
            this.argName = name;
            if (off == 0.0d) {
                this.number = num;
            } else {
                this.number = Double.valueOf(num.doubleValue() - off);
            }
            this.offset = off;
        }

        public String toString() {
            throw new AssertionError("PluralSelectorContext being formatted, rather than its number");
        }
    }

    private static final class PluralSelectorProvider implements PluralSelector {
        static final /* synthetic */ boolean -assertionsDisabled = (PluralSelectorProvider.class.desiredAssertionStatus() ^ 1);
        private MessageFormat msgFormat;
        private PluralRules rules;
        private PluralType type;

        public PluralSelectorProvider(MessageFormat mf, PluralType type) {
            this.msgFormat = mf;
            this.type = type;
        }

        public String select(Object ctx, double number) {
            if (this.rules == null) {
                this.rules = PluralRules.forLocale(this.msgFormat.ulocale, this.type);
            }
            PluralSelectorContext context = (PluralSelectorContext) ctx;
            context.numberArgIndex = this.msgFormat.findFirstPluralNumberArg(this.msgFormat.findOtherSubMessage(context.startIndex), context.argName);
            if (context.numberArgIndex > 0 && this.msgFormat.cachedFormatters != null) {
                context.formatter = (Format) this.msgFormat.cachedFormatters.get(Integer.valueOf(context.numberArgIndex));
            }
            if (context.formatter == null) {
                context.formatter = this.msgFormat.getStockNumberFormatter();
                context.forReplaceNumber = true;
            }
            if (!-assertionsDisabled) {
                if (!(context.number.doubleValue() == number)) {
                    throw new AssertionError();
                }
            }
            context.numberString = context.formatter.format(context.number);
            if (!(context.formatter instanceof DecimalFormat)) {
                return this.rules.select(number);
            }
            return this.rules.select(((DecimalFormat) context.formatter).getFixedDecimal(number));
        }
    }

    public MessageFormat(String pattern) {
        this.ulocale = ULocale.getDefault(Category.FORMAT);
        applyPattern(pattern);
    }

    public MessageFormat(String pattern, Locale locale) {
        this(pattern, ULocale.forLocale(locale));
    }

    public MessageFormat(String pattern, ULocale locale) {
        this.ulocale = locale;
        applyPattern(pattern);
    }

    public void setLocale(Locale locale) {
        setLocale(ULocale.forLocale(locale));
    }

    public void setLocale(ULocale locale) {
        String existingPattern = toPattern();
        this.ulocale = locale;
        this.stockDateFormatter = null;
        this.stockNumberFormatter = null;
        this.pluralProvider = null;
        this.ordinalProvider = null;
        applyPattern(existingPattern);
    }

    public Locale getLocale() {
        return this.ulocale.toLocale();
    }

    public ULocale getULocale() {
        return this.ulocale;
    }

    public void applyPattern(String pttrn) {
        try {
            if (this.msgPattern == null) {
                this.msgPattern = new MessagePattern(pttrn);
            } else {
                this.msgPattern.parse(pttrn);
            }
            cacheExplicitFormats();
        } catch (RuntimeException e) {
            resetPattern();
            throw e;
        }
    }

    public void applyPattern(String pattern, ApostropheMode aposMode) {
        if (this.msgPattern == null) {
            this.msgPattern = new MessagePattern(aposMode);
        } else if (aposMode != this.msgPattern.getApostropheMode()) {
            this.msgPattern.clearPatternAndSetApostropheMode(aposMode);
        }
        applyPattern(pattern);
    }

    public ApostropheMode getApostropheMode() {
        if (this.msgPattern == null) {
            this.msgPattern = new MessagePattern();
        }
        return this.msgPattern.getApostropheMode();
    }

    public String toPattern() {
        if (this.customFormatArgStarts != null) {
            throw new IllegalStateException("toPattern() is not supported after custom Format objects have been set via setFormat() or similar APIs");
        } else if (this.msgPattern == null) {
            return "";
        } else {
            String originalPattern = this.msgPattern.getPatternString();
            if (originalPattern == null) {
                originalPattern = "";
            }
            return originalPattern;
        }
    }

    private int nextTopLevelArgStart(int partIndex) {
        if (partIndex != 0) {
            partIndex = this.msgPattern.getLimitPartIndex(partIndex);
        }
        Type type;
        do {
            partIndex++;
            type = this.msgPattern.getPartType(partIndex);
            if (type == Type.ARG_START) {
                return partIndex;
            }
        } while (type != Type.MSG_LIMIT);
        return -1;
    }

    private boolean argNameMatches(int partIndex, String argName, int argNumber) {
        Part part = this.msgPattern.getPart(partIndex);
        if (part.getType() == Type.ARG_NAME) {
            return this.msgPattern.partSubstringMatches(part, argName);
        }
        return part.getValue() == argNumber;
    }

    private String getArgName(int partIndex) {
        Part part = this.msgPattern.getPart(partIndex);
        if (part.getType() == Type.ARG_NAME) {
            return this.msgPattern.getSubstring(part);
        }
        return Integer.toString(part.getValue());
    }

    public void setFormatsByArgumentIndex(Format[] newFormats) {
        if (this.msgPattern.hasNamedArguments()) {
            throw new IllegalArgumentException("This method is not available in MessageFormat objects that use alphanumeric argument names.");
        }
        int partIndex = 0;
        while (true) {
            partIndex = nextTopLevelArgStart(partIndex);
            if (partIndex >= 0) {
                int argNumber = this.msgPattern.getPart(partIndex + 1).getValue();
                if (argNumber < newFormats.length) {
                    setCustomArgStartFormat(partIndex, newFormats[argNumber]);
                }
            } else {
                return;
            }
        }
    }

    public void setFormatsByArgumentName(Map<String, Format> newFormats) {
        int partIndex = 0;
        while (true) {
            partIndex = nextTopLevelArgStart(partIndex);
            if (partIndex >= 0) {
                String key = getArgName(partIndex + 1);
                if (newFormats.containsKey(key)) {
                    setCustomArgStartFormat(partIndex, (Format) newFormats.get(key));
                }
            } else {
                return;
            }
        }
    }

    public void setFormats(Format[] newFormats) {
        int formatNumber = 0;
        int partIndex = 0;
        while (formatNumber < newFormats.length) {
            partIndex = nextTopLevelArgStart(partIndex);
            if (partIndex >= 0) {
                setCustomArgStartFormat(partIndex, newFormats[formatNumber]);
                formatNumber++;
            } else {
                return;
            }
        }
    }

    public void setFormatByArgumentIndex(int argumentIndex, Format newFormat) {
        if (this.msgPattern.hasNamedArguments()) {
            throw new IllegalArgumentException("This method is not available in MessageFormat objects that use alphanumeric argument names.");
        }
        int partIndex = 0;
        while (true) {
            partIndex = nextTopLevelArgStart(partIndex);
            if (partIndex < 0) {
                return;
            }
            if (this.msgPattern.getPart(partIndex + 1).getValue() == argumentIndex) {
                setCustomArgStartFormat(partIndex, newFormat);
            }
        }
    }

    public void setFormatByArgumentName(String argumentName, Format newFormat) {
        int argNumber = MessagePattern.validateArgumentName(argumentName);
        if (argNumber >= -1) {
            int partIndex = 0;
            while (true) {
                partIndex = nextTopLevelArgStart(partIndex);
                if (partIndex < 0) {
                    return;
                }
                if (argNameMatches(partIndex + 1, argumentName, argNumber)) {
                    setCustomArgStartFormat(partIndex, newFormat);
                }
            }
        }
    }

    public void setFormat(int formatElementIndex, Format newFormat) {
        int formatNumber = 0;
        int partIndex = 0;
        while (true) {
            partIndex = nextTopLevelArgStart(partIndex);
            if (partIndex < 0) {
                throw new ArrayIndexOutOfBoundsException(formatElementIndex);
            } else if (formatNumber == formatElementIndex) {
                setCustomArgStartFormat(partIndex, newFormat);
                return;
            } else {
                formatNumber++;
            }
        }
    }

    public Format[] getFormatsByArgumentIndex() {
        if (this.msgPattern.hasNamedArguments()) {
            throw new IllegalArgumentException("This method is not available in MessageFormat objects that use alphanumeric argument names.");
        }
        ArrayList<Format> list = new ArrayList();
        int partIndex = 0;
        while (true) {
            partIndex = nextTopLevelArgStart(partIndex);
            if (partIndex < 0) {
                return (Format[]) list.toArray(new Format[list.size()]);
            }
            Object obj;
            int argNumber = this.msgPattern.getPart(partIndex + 1).getValue();
            while (argNumber >= list.size()) {
                list.add(null);
            }
            if (this.cachedFormatters == null) {
                obj = null;
            } else {
                Format obj2 = (Format) this.cachedFormatters.get(Integer.valueOf(partIndex));
            }
            list.set(argNumber, obj2);
        }
    }

    public Format[] getFormats() {
        ArrayList<Format> list = new ArrayList();
        int partIndex = 0;
        while (true) {
            partIndex = nextTopLevelArgStart(partIndex);
            if (partIndex < 0) {
                return (Format[]) list.toArray(new Format[list.size()]);
            }
            Object obj;
            if (this.cachedFormatters == null) {
                obj = null;
            } else {
                Format obj2 = (Format) this.cachedFormatters.get(Integer.valueOf(partIndex));
            }
            list.add(obj2);
        }
    }

    public Set<String> getArgumentNames() {
        Set<String> result = new HashSet();
        int partIndex = 0;
        while (true) {
            partIndex = nextTopLevelArgStart(partIndex);
            if (partIndex < 0) {
                return result;
            }
            result.add(getArgName(partIndex + 1));
        }
    }

    public Format getFormatByArgumentName(String argumentName) {
        if (this.cachedFormatters == null) {
            return null;
        }
        int argNumber = MessagePattern.validateArgumentName(argumentName);
        if (argNumber < -1) {
            return null;
        }
        int partIndex = 0;
        do {
            partIndex = nextTopLevelArgStart(partIndex);
            if (partIndex < 0) {
                return null;
            }
        } while (!argNameMatches(partIndex + 1, argumentName, argNumber));
        return (Format) this.cachedFormatters.get(Integer.valueOf(partIndex));
    }

    public final StringBuffer format(Object[] arguments, StringBuffer result, FieldPosition pos) {
        format(arguments, null, new AppendableWrapper(result), pos);
        return result;
    }

    public final StringBuffer format(Map<String, Object> arguments, StringBuffer result, FieldPosition pos) {
        format(null, arguments, new AppendableWrapper(result), pos);
        return result;
    }

    public static String format(String pattern, Object... arguments) {
        return new MessageFormat(pattern).format(arguments);
    }

    public static String format(String pattern, Map<String, Object> arguments) {
        return new MessageFormat(pattern).format(arguments);
    }

    public boolean usesNamedArguments() {
        return this.msgPattern.hasNamedArguments();
    }

    public final StringBuffer format(Object arguments, StringBuffer result, FieldPosition pos) {
        format(arguments, new AppendableWrapper(result), pos);
        return result;
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object arguments) {
        if (arguments == null) {
            throw new NullPointerException("formatToCharacterIterator must be passed non-null object");
        }
        StringBuilder result = new StringBuilder();
        AppendableWrapper wrapper = new AppendableWrapper(result);
        wrapper.useAttributes();
        format(arguments, wrapper, null);
        AttributedString as = new AttributedString(result.toString());
        for (AttributeAndPosition a : wrapper.attributes) {
            as.addAttribute(a.key, a.value, a.start, a.limit);
        }
        return as.getIterator();
    }

    public Object[] parse(String source, ParsePosition pos) {
        if (this.msgPattern.hasNamedArguments()) {
            throw new IllegalArgumentException("This method is not available in MessageFormat objects that use named argument.");
        }
        int maxArgId = -1;
        int partIndex = 0;
        while (true) {
            partIndex = nextTopLevelArgStart(partIndex);
            if (partIndex < 0) {
                break;
            }
            int argNumber = this.msgPattern.getPart(partIndex + 1).getValue();
            if (argNumber > maxArgId) {
                maxArgId = argNumber;
            }
        }
        Object[] resultArray = new Object[(maxArgId + 1)];
        int backupStartPos = pos.getIndex();
        parse(0, source, pos, resultArray, null);
        if (pos.getIndex() == backupStartPos) {
            return null;
        }
        return resultArray;
    }

    public Map<String, Object> parseToMap(String source, ParsePosition pos) {
        Map<String, Object> result = new HashMap();
        int backupStartPos = pos.getIndex();
        parse(0, source, pos, null, result);
        if (pos.getIndex() == backupStartPos) {
            return null;
        }
        return result;
    }

    public Object[] parse(String source) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Object[] result = parse(source, pos);
        if (pos.getIndex() != 0) {
            return result;
        }
        throw new ParseException("MessageFormat parse error!", pos.getErrorIndex());
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x0157  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0157  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parse(int msgStart, String source, ParsePosition pos, Object[] args, Map<String, Object> argsMap) {
        if (source != null) {
            String msgString = this.msgPattern.getPatternString();
            int prevIndex = this.msgPattern.getPart(msgStart).getLimit();
            int sourceOffset = pos.getIndex();
            ParsePosition parsePosition = new ParsePosition(0);
            int i = msgStart + 1;
            while (true) {
                Part part = this.msgPattern.getPart(i);
                Type type = part.getType();
                int len = part.getIndex() - prevIndex;
                if (len == 0 || msgString.regionMatches(prevIndex, source, sourceOffset, len)) {
                    sourceOffset += len;
                    prevIndex += len;
                    if (type == Type.MSG_LIMIT) {
                        pos.setIndex(sourceOffset);
                        return;
                    }
                    if (type == Type.SKIP_SYNTAX || type == Type.INSERT_CHAR) {
                        prevIndex = part.getLimit();
                    } else if (-assertionsDisabled || type == Type.ARG_START) {
                        Object argId;
                        int argLimit = this.msgPattern.getLimitPartIndex(i);
                        ArgType argType = part.getArgType();
                        i++;
                        part = this.msgPattern.getPart(i);
                        int argNumber = 0;
                        String key = null;
                        if (args != null) {
                            argNumber = part.getValue();
                            argId = Integer.valueOf(argNumber);
                        } else {
                            if (part.getType() == Type.ARG_NAME) {
                                key = this.msgPattern.getSubstring(part);
                            } else {
                                key = Integer.toString(part.getValue());
                            }
                            String argId2 = key;
                        }
                        i++;
                        boolean haveArgResult = false;
                        Object argResult = null;
                        if (this.cachedFormatters != null) {
                            Format formatter = (Format) this.cachedFormatters.get(Integer.valueOf(i - 2));
                            if (formatter != null) {
                                parsePosition.setIndex(sourceOffset);
                                argResult = formatter.parseObject(source, parsePosition);
                                if (parsePosition.getIndex() == sourceOffset) {
                                    pos.setErrorIndex(sourceOffset);
                                    return;
                                }
                                haveArgResult = true;
                                sourceOffset = parsePosition.getIndex();
                                if (haveArgResult) {
                                    if (args != null) {
                                        args[argNumber] = argResult;
                                    } else if (argsMap != null) {
                                        argsMap.put(key, argResult);
                                    }
                                }
                                prevIndex = this.msgPattern.getPart(argLimit).getLimit();
                                i = argLimit;
                            }
                        }
                        if (argType == ArgType.NONE || (this.cachedFormatters != null && this.cachedFormatters.containsKey(Integer.valueOf(i - 2)))) {
                            int next;
                            String stringAfterArgument = getLiteralStringUntilNextArgument(argLimit);
                            if (stringAfterArgument.length() != 0) {
                                next = source.indexOf(stringAfterArgument, sourceOffset);
                            } else {
                                next = source.length();
                            }
                            if (next < 0) {
                                pos.setErrorIndex(sourceOffset);
                                return;
                            }
                            String strValue = source.substring(sourceOffset, next);
                            if (!strValue.equals("{" + argId2.toString() + "}")) {
                                haveArgResult = true;
                                argResult = strValue;
                            }
                            sourceOffset = next;
                            if (haveArgResult) {
                            }
                            prevIndex = this.msgPattern.getPart(argLimit).getLimit();
                            i = argLimit;
                        } else if (argType == ArgType.CHOICE) {
                            parsePosition.setIndex(sourceOffset);
                            double choiceResult = parseChoiceArgument(this.msgPattern, i, source, parsePosition);
                            if (parsePosition.getIndex() == sourceOffset) {
                                pos.setErrorIndex(sourceOffset);
                                return;
                            }
                            argResult = Double.valueOf(choiceResult);
                            haveArgResult = true;
                            sourceOffset = parsePosition.getIndex();
                            if (haveArgResult) {
                            }
                            prevIndex = this.msgPattern.getPart(argLimit).getLimit();
                            i = argLimit;
                        } else if (argType.hasPluralStyle() || argType == ArgType.SELECT) {
                            throw new UnsupportedOperationException("Parsing of plural/select/selectordinal argument is not supported.");
                        } else {
                            throw new IllegalStateException("unexpected argType " + argType);
                        }
                    } else {
                        throw new AssertionError("Unexpected Part " + part + " in parsed message.");
                    }
                    i++;
                } else {
                    pos.setErrorIndex(sourceOffset);
                    return;
                }
            }
        }
    }

    public Map<String, Object> parseToMap(String source) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Map<String, Object> result = new HashMap();
        parse(0, source, pos, null, result);
        if (pos.getIndex() != 0) {
            return result;
        }
        throw new ParseException("MessageFormat parse error!", pos.getErrorIndex());
    }

    public Object parseObject(String source, ParsePosition pos) {
        if (this.msgPattern.hasNamedArguments()) {
            return parseToMap(source, pos);
        }
        return parse(source, pos);
    }

    public Object clone() {
        MessageFormat other = (MessageFormat) super.clone();
        if (this.customFormatArgStarts != null) {
            other.customFormatArgStarts = new HashSet();
            for (Integer key : this.customFormatArgStarts) {
                other.customFormatArgStarts.add(key);
            }
        } else {
            other.customFormatArgStarts = null;
        }
        if (this.cachedFormatters != null) {
            other.cachedFormatters = new HashMap();
            for (Entry<Integer, Format> entry : this.cachedFormatters.entrySet()) {
                other.cachedFormatters.put((Integer) entry.getKey(), (Format) entry.getValue());
            }
        } else {
            other.cachedFormatters = null;
        }
        other.msgPattern = this.msgPattern == null ? null : (MessagePattern) this.msgPattern.clone();
        other.stockDateFormatter = this.stockDateFormatter == null ? null : (DateFormat) this.stockDateFormatter.clone();
        other.stockNumberFormatter = this.stockNumberFormatter == null ? null : (NumberFormat) this.stockNumberFormatter.clone();
        other.pluralProvider = null;
        other.ordinalProvider = null;
        return other;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MessageFormat other = (MessageFormat) obj;
        if (Utility.objectEquals(this.ulocale, other.ulocale) && Utility.objectEquals(this.msgPattern, other.msgPattern) && Utility.objectEquals(this.cachedFormatters, other.cachedFormatters)) {
            z = Utility.objectEquals(this.customFormatArgStarts, other.customFormatArgStarts);
        }
        return z;
    }

    public int hashCode() {
        return this.msgPattern.getPatternString().hashCode();
    }

    private DateFormat getStockDateFormatter() {
        if (this.stockDateFormatter == null) {
            this.stockDateFormatter = DateFormat.getDateTimeInstance(3, 3, this.ulocale);
        }
        return this.stockDateFormatter;
    }

    private NumberFormat getStockNumberFormatter() {
        if (this.stockNumberFormatter == null) {
            this.stockNumberFormatter = NumberFormat.getInstance(this.ulocale);
        }
        return this.stockNumberFormatter;
    }

    private void format(int msgStart, PluralSelectorContext pluralNumber, Object[] args, Map<String, Object> argsMap, AppendableWrapper dest, FieldPosition fp) {
        String msgString = this.msgPattern.getPatternString();
        int prevIndex = this.msgPattern.getPart(msgStart).getLimit();
        int i = msgStart + 1;
        while (true) {
            Part part = this.msgPattern.getPart(i);
            Type type = part.getType();
            dest.append(msgString, prevIndex, part.getIndex());
            if (type != Type.MSG_LIMIT) {
                prevIndex = part.getLimit();
                if (type == Type.REPLACE_NUMBER) {
                    if (pluralNumber.forReplaceNumber) {
                        dest.formatAndAppend(pluralNumber.formatter, pluralNumber.number, pluralNumber.numberString);
                    } else {
                        dest.formatAndAppend(getStockNumberFormatter(), pluralNumber.number);
                    }
                } else if (type == Type.ARG_START) {
                    Number arg;
                    int argLimit = this.msgPattern.getLimitPartIndex(i);
                    ArgType argType = part.getArgType();
                    i++;
                    part = this.msgPattern.getPart(i);
                    boolean noArg = false;
                    Object argId = null;
                    String argName = this.msgPattern.getSubstring(part);
                    if (args != null) {
                        int argNumber = part.getValue();
                        if (dest.attributes != null) {
                            argId = Integer.valueOf(argNumber);
                        }
                        if (argNumber < 0 || argNumber >= args.length) {
                            arg = null;
                            noArg = true;
                        } else {
                            arg = args[argNumber];
                        }
                    } else {
                        String argId2 = argName;
                        if (argsMap == null || !argsMap.containsKey(argName)) {
                            arg = null;
                            noArg = true;
                        } else {
                            arg = argsMap.get(argName);
                        }
                    }
                    i++;
                    int prevDestLength = dest.length;
                    if (noArg) {
                        dest.append("{" + argName + "}");
                    } else if (arg == null) {
                        dest.append((CharSequence) "null");
                    } else if (pluralNumber == null || pluralNumber.numberArgIndex != i - 2) {
                        if (this.cachedFormatters != null) {
                            Format formatter = (Format) this.cachedFormatters.get(Integer.valueOf(i - 2));
                            if (formatter != null) {
                                if ((formatter instanceof ChoiceFormat) || (formatter instanceof PluralFormat) || (formatter instanceof SelectFormat)) {
                                    String subMsgString = formatter.format(arg);
                                    if (subMsgString.indexOf(123) >= 0 || (subMsgString.indexOf(39) >= 0 && (this.msgPattern.jdkAposMode() ^ 1) != 0)) {
                                        new MessageFormat(subMsgString, this.ulocale).format(0, null, args, argsMap, dest, null);
                                    } else if (dest.attributes == null) {
                                        dest.append((CharSequence) subMsgString);
                                    } else {
                                        dest.formatAndAppend(formatter, arg);
                                    }
                                } else {
                                    dest.formatAndAppend(formatter, arg);
                                }
                            }
                        }
                        if (argType == ArgType.NONE || (this.cachedFormatters != null && this.cachedFormatters.containsKey(Integer.valueOf(i - 2)))) {
                            if (arg instanceof Number) {
                                dest.formatAndAppend(getStockNumberFormatter(), arg);
                            } else if (arg instanceof Date) {
                                dest.formatAndAppend(getStockDateFormatter(), arg);
                            } else {
                                dest.append(arg.toString());
                            }
                        } else if (argType == ArgType.CHOICE) {
                            if (arg instanceof Number) {
                                formatComplexSubMessage(findChoiceSubMessage(this.msgPattern, i, arg.doubleValue()), null, args, argsMap, dest);
                            } else {
                                throw new IllegalArgumentException("'" + arg + "' is not a Number");
                            }
                        } else if (argType.hasPluralStyle()) {
                            if (arg instanceof Number) {
                                PluralSelectorProvider selector;
                                if (argType == ArgType.PLURAL) {
                                    if (this.pluralProvider == null) {
                                        this.pluralProvider = new PluralSelectorProvider(this, PluralType.CARDINAL);
                                    }
                                    selector = this.pluralProvider;
                                } else {
                                    if (this.ordinalProvider == null) {
                                        this.ordinalProvider = new PluralSelectorProvider(this, PluralType.ORDINAL);
                                    }
                                    selector = this.ordinalProvider;
                                }
                                Number number = arg;
                                PluralSelectorContext context = new PluralSelectorContext(i, argName, number, this.msgPattern.getPluralOffset(i), null);
                                formatComplexSubMessage(PluralFormat.findSubMessage(this.msgPattern, i, selector, context, number.doubleValue()), context, args, argsMap, dest);
                            } else {
                                throw new IllegalArgumentException("'" + arg + "' is not a Number");
                            }
                        } else if (argType == ArgType.SELECT) {
                            formatComplexSubMessage(SelectFormat.findSubMessage(this.msgPattern, i, arg.toString()), null, args, argsMap, dest);
                        } else {
                            throw new IllegalStateException("unexpected argType " + argType);
                        }
                    } else if (pluralNumber.offset == 0.0d) {
                        dest.formatAndAppend(pluralNumber.formatter, pluralNumber.number, pluralNumber.numberString);
                    } else {
                        dest.formatAndAppend(pluralNumber.formatter, arg);
                    }
                    fp = updateMetaData(dest, prevDestLength, fp, argId);
                    prevIndex = this.msgPattern.getPart(argLimit).getLimit();
                    i = argLimit;
                } else {
                    continue;
                }
                i++;
            } else {
                return;
            }
        }
    }

    private void formatComplexSubMessage(int msgStart, PluralSelectorContext pluralNumber, Object[] args, Map<String, Object> argsMap, AppendableWrapper dest) {
        if (this.msgPattern.jdkAposMode()) {
            int index;
            CharSequence subMsgString;
            String msgString = this.msgPattern.getPatternString();
            StringBuilder sb = null;
            int prevIndex = this.msgPattern.getPart(msgStart).getLimit();
            int i = msgStart;
            while (true) {
                i++;
                Part part = this.msgPattern.getPart(i);
                Type type = part.getType();
                index = part.getIndex();
                if (type == Type.MSG_LIMIT) {
                    break;
                } else if (type == Type.REPLACE_NUMBER || type == Type.SKIP_SYNTAX) {
                    if (sb == null) {
                        sb = new StringBuilder();
                    }
                    sb.append(msgString, prevIndex, index);
                    if (type == Type.REPLACE_NUMBER) {
                        if (pluralNumber.forReplaceNumber) {
                            sb.append(pluralNumber.numberString);
                        } else {
                            sb.append(getStockNumberFormatter().format(pluralNumber.number));
                        }
                    }
                    prevIndex = part.getLimit();
                } else if (type == Type.ARG_START) {
                    if (sb == null) {
                        sb = new StringBuilder();
                    }
                    sb.append(msgString, prevIndex, index);
                    prevIndex = index;
                    i = this.msgPattern.getLimitPartIndex(i);
                    index = this.msgPattern.getPart(i).getLimit();
                    MessagePattern.appendReducedApostrophes(msgString, prevIndex, index, sb);
                    prevIndex = index;
                }
            }
            if (sb == null) {
                subMsgString = msgString.substring(prevIndex, index);
            } else {
                subMsgString = sb.append(msgString, prevIndex, index).toString();
            }
            if (subMsgString.indexOf(123) >= 0) {
                MessageFormat subMsgFormat = new MessageFormat("", this.ulocale);
                subMsgFormat.applyPattern(subMsgString, ApostropheMode.DOUBLE_REQUIRED);
                subMsgFormat.format(0, null, args, argsMap, dest, null);
            } else {
                dest.append(subMsgString);
            }
            return;
        }
        format(msgStart, pluralNumber, args, argsMap, dest, null);
    }

    private String getLiteralStringUntilNextArgument(int from) {
        StringBuilder b = new StringBuilder();
        String msgString = this.msgPattern.getPatternString();
        int prevIndex = this.msgPattern.getPart(from).getLimit();
        int i = from + 1;
        while (true) {
            Part part = this.msgPattern.getPart(i);
            Type type = part.getType();
            b.append(msgString, prevIndex, part.getIndex());
            if (type != Type.ARG_START && type != Type.MSG_LIMIT) {
                if (-assertionsDisabled || type == Type.SKIP_SYNTAX || type == Type.INSERT_CHAR) {
                    prevIndex = part.getLimit();
                    i++;
                } else {
                    throw new AssertionError("Unexpected Part " + part + " in parsed message.");
                }
            }
        }
        return b.toString();
    }

    private FieldPosition updateMetaData(AppendableWrapper dest, int prevLength, FieldPosition fp, Object argId) {
        if (dest.attributes != null && prevLength < dest.length) {
            dest.attributes.add(new AttributeAndPosition(argId, prevLength, dest.length));
        }
        if (fp == null || !Field.ARGUMENT.equals(fp.getFieldAttribute())) {
            return fp;
        }
        fp.setBeginIndex(prevLength);
        fp.setEndIndex(dest.length);
        return null;
    }

    private static int findChoiceSubMessage(MessagePattern pattern, int partIndex, double number) {
        int msgStart;
        int count = pattern.countParts();
        partIndex += 2;
        int i;
        do {
            msgStart = partIndex;
            partIndex = pattern.getLimitPartIndex(partIndex) + 1;
            if (partIndex >= count) {
                break;
            }
            int partIndex2 = partIndex + 1;
            Part part = pattern.getPart(partIndex);
            Type type = part.getType();
            if (type == Type.ARG_LIMIT) {
                partIndex = partIndex2;
                break;
            } else if (-assertionsDisabled || type.hasNumericValue()) {
                double boundary = pattern.getNumericValue(part);
                partIndex = partIndex2 + 1;
                i = pattern.getPatternString().charAt(pattern.getPatternIndex(partIndex2)) == '<' ? number > boundary ? 1 : 0 : number >= boundary ? 1 : 0;
            } else {
                throw new AssertionError();
            }
        } while ((i ^ 1) == 0);
        return msgStart;
    }

    private static double parseChoiceArgument(MessagePattern pattern, int partIndex, String source, ParsePosition pos) {
        int start = pos.getIndex();
        int furthest = start;
        double bestNumber = Double.NaN;
        while (pattern.getPartType(partIndex) != Type.ARG_LIMIT) {
            double tempNumber = pattern.getNumericValue(pattern.getPart(partIndex));
            partIndex += 2;
            int msgLimit = pattern.getLimitPartIndex(partIndex);
            int len = matchStringUntilLimitPart(pattern, partIndex, msgLimit, source, start);
            if (len >= 0) {
                int newIndex = start + len;
                if (newIndex > furthest) {
                    furthest = newIndex;
                    bestNumber = tempNumber;
                    if (newIndex == source.length()) {
                        break;
                    }
                } else {
                    continue;
                }
            }
            partIndex = msgLimit + 1;
        }
        if (furthest == start) {
            pos.setErrorIndex(start);
        } else {
            pos.setIndex(furthest);
        }
        return bestNumber;
    }

    private static int matchStringUntilLimitPart(MessagePattern pattern, int partIndex, int limitPartIndex, String source, int sourceOffset) {
        int matchingSourceLength = 0;
        String msgString = pattern.getPatternString();
        int prevIndex = pattern.getPart(partIndex).getLimit();
        while (true) {
            partIndex++;
            Part part = pattern.getPart(partIndex);
            if (partIndex == limitPartIndex || part.getType() == Type.SKIP_SYNTAX) {
                int length = part.getIndex() - prevIndex;
                if (length != 0 && (source.regionMatches(sourceOffset, msgString, prevIndex, length) ^ 1) != 0) {
                    return -1;
                }
                matchingSourceLength += length;
                if (partIndex == limitPartIndex) {
                    return matchingSourceLength;
                }
                prevIndex = part.getLimit();
            }
        }
    }

    private int findOtherSubMessage(int partIndex) {
        int count = this.msgPattern.countParts();
        if (this.msgPattern.getPart(partIndex).getType().hasNumericValue()) {
            partIndex++;
        }
        while (true) {
            int partIndex2 = partIndex + 1;
            Part part = this.msgPattern.getPart(partIndex);
            Type type = part.getType();
            if (type == Type.ARG_LIMIT) {
                partIndex = partIndex2;
                break;
            } else if (-assertionsDisabled || type == Type.ARG_SELECTOR) {
                if (!this.msgPattern.partSubstringMatches(part, "other")) {
                    if (this.msgPattern.getPartType(partIndex2).hasNumericValue()) {
                        partIndex = partIndex2 + 1;
                    } else {
                        partIndex = partIndex2;
                    }
                    partIndex = this.msgPattern.getLimitPartIndex(partIndex) + 1;
                    if (partIndex >= count) {
                        break;
                    }
                } else {
                    return partIndex2;
                }
            } else {
                throw new AssertionError();
            }
        }
        return 0;
    }

    private int findFirstPluralNumberArg(int msgStart, String argName) {
        int i = msgStart + 1;
        while (true) {
            Part part = this.msgPattern.getPart(i);
            Type type = part.getType();
            if (type == Type.MSG_LIMIT) {
                return 0;
            }
            if (type == Type.REPLACE_NUMBER) {
                return -1;
            }
            if (type == Type.ARG_START) {
                ArgType argType = part.getArgType();
                if (argName.length() != 0 && (argType == ArgType.NONE || argType == ArgType.SIMPLE)) {
                    if (this.msgPattern.partSubstringMatches(this.msgPattern.getPart(i + 1), argName)) {
                        return i;
                    }
                }
                i = this.msgPattern.getLimitPartIndex(i);
            }
            i++;
        }
    }

    private void format(Object arguments, AppendableWrapper result, FieldPosition fp) {
        if (arguments == null || (arguments instanceof Map)) {
            format(null, (Map) arguments, result, fp);
        } else {
            format((Object[]) arguments, null, result, fp);
        }
    }

    private void format(Object[] arguments, Map<String, Object> argsMap, AppendableWrapper dest, FieldPosition fp) {
        if (arguments == null || !this.msgPattern.hasNamedArguments()) {
            format(0, null, arguments, argsMap, dest, fp);
            return;
        }
        throw new IllegalArgumentException("This method is not available in MessageFormat objects that use alphanumeric argument names.");
    }

    private void resetPattern() {
        if (this.msgPattern != null) {
            this.msgPattern.clear();
        }
        if (this.cachedFormatters != null) {
            this.cachedFormatters.clear();
        }
        this.customFormatArgStarts = null;
    }

    private Format createAppropriateFormat(String type, String style) {
        Format rbnf;
        String ruleset;
        switch (findKeyword(type, typeList)) {
            case 0:
                switch (findKeyword(style, modifierList)) {
                    case 0:
                        return NumberFormat.getInstance(this.ulocale);
                    case 1:
                        return NumberFormat.getCurrencyInstance(this.ulocale);
                    case 2:
                        return NumberFormat.getPercentInstance(this.ulocale);
                    case 3:
                        return NumberFormat.getIntegerInstance(this.ulocale);
                    default:
                        return new DecimalFormat(style, new DecimalFormatSymbols(this.ulocale));
                }
            case 1:
                switch (findKeyword(style, dateModifierList)) {
                    case 0:
                        return DateFormat.getDateInstance(2, this.ulocale);
                    case 1:
                        return DateFormat.getDateInstance(3, this.ulocale);
                    case 2:
                        return DateFormat.getDateInstance(2, this.ulocale);
                    case 3:
                        return DateFormat.getDateInstance(1, this.ulocale);
                    case 4:
                        return DateFormat.getDateInstance(0, this.ulocale);
                    default:
                        return new SimpleDateFormat(style, this.ulocale);
                }
            case 2:
                switch (findKeyword(style, dateModifierList)) {
                    case 0:
                        return DateFormat.getTimeInstance(2, this.ulocale);
                    case 1:
                        return DateFormat.getTimeInstance(3, this.ulocale);
                    case 2:
                        return DateFormat.getTimeInstance(2, this.ulocale);
                    case 3:
                        return DateFormat.getTimeInstance(1, this.ulocale);
                    case 4:
                        return DateFormat.getTimeInstance(0, this.ulocale);
                    default:
                        return new SimpleDateFormat(style, this.ulocale);
                }
            case 3:
                rbnf = new RuleBasedNumberFormat(this.ulocale, 1);
                ruleset = style.trim();
                if (ruleset.length() != 0) {
                    try {
                        rbnf.setDefaultRuleSet(ruleset);
                    } catch (Exception e) {
                    }
                }
                return rbnf;
            case 4:
                rbnf = new RuleBasedNumberFormat(this.ulocale, 2);
                ruleset = style.trim();
                if (ruleset.length() != 0) {
                    try {
                        rbnf.setDefaultRuleSet(ruleset);
                    } catch (Exception e2) {
                    }
                }
                return rbnf;
            case 5:
                rbnf = new RuleBasedNumberFormat(this.ulocale, 3);
                ruleset = style.trim();
                if (ruleset.length() != 0) {
                    try {
                        rbnf.setDefaultRuleSet(ruleset);
                    } catch (Exception e3) {
                    }
                }
                return rbnf;
            default:
                throw new IllegalArgumentException("Unknown format type \"" + type + "\"");
        }
    }

    private static final int findKeyword(String s, String[] list) {
        s = PatternProps.trimWhiteSpace(s).toLowerCase(rootLocale);
        for (int i = 0; i < list.length; i++) {
            if (s.equals(list[i])) {
                return i;
            }
        }
        return -1;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(this.ulocale.toLanguageTag());
        if (this.msgPattern == null) {
            this.msgPattern = new MessagePattern();
        }
        out.writeObject(this.msgPattern.getApostropheMode());
        out.writeObject(this.msgPattern.getPatternString());
        if (this.customFormatArgStarts != null && !this.customFormatArgStarts.isEmpty()) {
            out.writeInt(this.customFormatArgStarts.size());
            int formatIndex = 0;
            int partIndex = 0;
            while (true) {
                partIndex = nextTopLevelArgStart(partIndex);
                if (partIndex < 0) {
                    break;
                }
                if (this.customFormatArgStarts.contains(Integer.valueOf(partIndex))) {
                    out.writeInt(formatIndex);
                    out.writeObject(this.cachedFormatters.get(Integer.valueOf(partIndex)));
                }
                formatIndex++;
            }
        } else {
            out.writeInt(0);
        }
        out.writeInt(0);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.ulocale = ULocale.forLanguageTag((String) in.readObject());
        ApostropheMode aposMode = (ApostropheMode) in.readObject();
        if (this.msgPattern == null || aposMode != this.msgPattern.getApostropheMode()) {
            this.msgPattern = new MessagePattern(aposMode);
        }
        String msg = (String) in.readObject();
        if (msg != null) {
            applyPattern(msg);
        }
        for (int numFormatters = in.readInt(); numFormatters > 0; numFormatters--) {
            setFormat(in.readInt(), (Format) in.readObject());
        }
        for (int numPairs = in.readInt(); numPairs > 0; numPairs--) {
            in.readInt();
            in.readObject();
        }
    }

    private void cacheExplicitFormats() {
        if (this.cachedFormatters != null) {
            this.cachedFormatters.clear();
        }
        this.customFormatArgStarts = null;
        int limit = this.msgPattern.countParts() - 2;
        int i = 1;
        while (i < limit) {
            Part part = this.msgPattern.getPart(i);
            if (part.getType() == Type.ARG_START && part.getArgType() == ArgType.SIMPLE) {
                int index = i;
                i += 2;
                int i2 = i + 1;
                String explicitType = this.msgPattern.getSubstring(this.msgPattern.getPart(i));
                String style = "";
                part = this.msgPattern.getPart(i2);
                if (part.getType() == Type.ARG_STYLE) {
                    style = this.msgPattern.getSubstring(part);
                    i = i2 + 1;
                } else {
                    i = i2;
                }
                setArgStartFormat(index, createAppropriateFormat(explicitType, style));
            }
            i++;
        }
    }

    private void setArgStartFormat(int argStart, Format formatter) {
        if (this.cachedFormatters == null) {
            this.cachedFormatters = new HashMap();
        }
        this.cachedFormatters.put(Integer.valueOf(argStart), formatter);
    }

    private void setCustomArgStartFormat(int argStart, Format formatter) {
        setArgStartFormat(argStart, formatter);
        if (this.customFormatArgStarts == null) {
            this.customFormatArgStarts = new HashSet();
        }
        this.customFormatArgStarts.add(Integer.valueOf(argStart));
    }

    public static String autoQuoteApostrophe(String pattern) {
        StringBuilder buf = new StringBuilder(pattern.length() * 2);
        int state = 0;
        int braceCount = 0;
        int j = pattern.length();
        for (int i = 0; i < j; i++) {
            char c = pattern.charAt(i);
            switch (state) {
                case 0:
                    switch (c) {
                        case '\'':
                            state = 1;
                            break;
                        case '{':
                            state = 3;
                            braceCount++;
                            break;
                        default:
                            break;
                    }
                case 1:
                    switch (c) {
                        case '\'':
                            state = 0;
                            break;
                        case '{':
                        case '}':
                            state = 2;
                            break;
                        default:
                            buf.append('\'');
                            state = 0;
                            break;
                    }
                case 2:
                    switch (c) {
                        case '\'':
                            state = 0;
                            break;
                        default:
                            break;
                    }
                case 3:
                    switch (c) {
                        case '{':
                            braceCount++;
                            break;
                        case '}':
                            braceCount--;
                            if (braceCount != 0) {
                                break;
                            }
                            state = 0;
                            break;
                        default:
                            break;
                    }
                default:
                    break;
            }
            buf.append(c);
        }
        if (state == 1 || state == 2) {
            buf.append('\'');
        }
        return new String(buf);
    }
}
