package ohos.global.icu.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.AttributedCharacterIterator;
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
import java.util.Objects;
import java.util.Set;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.global.icu.impl.PatternProps;
import ohos.global.icu.number.NumberFormatter;
import ohos.global.icu.text.MessagePattern;
import ohos.global.icu.text.PluralFormat;
import ohos.global.icu.text.PluralRules;
import ohos.global.icu.util.ICUUncheckedIOException;
import ohos.global.icu.util.ULocale;

public class MessageFormat extends UFormat {
    static final /* synthetic */ boolean $assertionsDisabled = false;
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
    private static final String[] dateModifierList = {"", SchemaSymbols.ATTVAL_SHORT, "medium", "long", "full"};
    private static final String[] modifierList = {"", "currency", Constants.ATTRNAME_PERCENT, "integer"};
    private static final Locale rootLocale = new Locale("");
    static final long serialVersionUID = 7136212545847378652L;
    private static final String[] typeList = {"number", SchemaSymbols.ATTVAL_DATE, "time", "spellout", "ordinal", SchemaSymbols.ATTVAL_DURATION};
    private transient Map<Integer, Format> cachedFormatters;
    private transient Set<Integer> customFormatArgStarts;
    private transient MessagePattern msgPattern;
    private transient PluralSelectorProvider ordinalProvider;
    private transient PluralSelectorProvider pluralProvider;
    private transient DateFormat stockDateFormatter;
    private transient NumberFormat stockNumberFormatter;
    private transient ULocale ulocale;

    public MessageFormat(String str) {
        this.ulocale = ULocale.getDefault(ULocale.Category.FORMAT);
        applyPattern(str);
    }

    public MessageFormat(String str, Locale locale) {
        this(str, ULocale.forLocale(locale));
    }

    public MessageFormat(String str, ULocale uLocale) {
        this.ulocale = uLocale;
        applyPattern(str);
    }

    public void setLocale(Locale locale) {
        setLocale(ULocale.forLocale(locale));
    }

    public void setLocale(ULocale uLocale) {
        String pattern = toPattern();
        this.ulocale = uLocale;
        this.stockDateFormatter = null;
        this.stockNumberFormatter = null;
        this.pluralProvider = null;
        this.ordinalProvider = null;
        applyPattern(pattern);
    }

    public Locale getLocale() {
        return this.ulocale.toLocale();
    }

    public ULocale getULocale() {
        return this.ulocale;
    }

    public void applyPattern(String str) {
        try {
            if (this.msgPattern == null) {
                this.msgPattern = new MessagePattern(str);
            } else {
                this.msgPattern.parse(str);
            }
            cacheExplicitFormats();
        } catch (RuntimeException e) {
            resetPattern();
            throw e;
        }
    }

    public void applyPattern(String str, MessagePattern.ApostropheMode apostropheMode) {
        MessagePattern messagePattern = this.msgPattern;
        if (messagePattern == null) {
            this.msgPattern = new MessagePattern(apostropheMode);
        } else if (apostropheMode != messagePattern.getApostropheMode()) {
            this.msgPattern.clearPatternAndSetApostropheMode(apostropheMode);
        }
        applyPattern(str);
    }

    public MessagePattern.ApostropheMode getApostropheMode() {
        if (this.msgPattern == null) {
            this.msgPattern = new MessagePattern();
        }
        return this.msgPattern.getApostropheMode();
    }

    public String toPattern() {
        if (this.customFormatArgStarts == null) {
            MessagePattern messagePattern = this.msgPattern;
            if (messagePattern == null) {
                return "";
            }
            String patternString = messagePattern.getPatternString();
            if (patternString == null) {
                return "";
            }
            return patternString;
        }
        throw new IllegalStateException("toPattern() is not supported after custom Format objects have been set via setFormat() or similar APIs");
    }

    private int nextTopLevelArgStart(int i) {
        MessagePattern.Part.Type partType;
        if (i != 0) {
            i = this.msgPattern.getLimitPartIndex(i);
        }
        do {
            i++;
            partType = this.msgPattern.getPartType(i);
            if (partType == MessagePattern.Part.Type.ARG_START) {
                return i;
            }
        } while (partType != MessagePattern.Part.Type.MSG_LIMIT);
        return -1;
    }

    private boolean argNameMatches(int i, String str, int i2) {
        MessagePattern.Part part = this.msgPattern.getPart(i);
        if (part.getType() == MessagePattern.Part.Type.ARG_NAME) {
            return this.msgPattern.partSubstringMatches(part, str);
        }
        return part.getValue() == i2;
    }

    private String getArgName(int i) {
        MessagePattern.Part part = this.msgPattern.getPart(i);
        if (part.getType() == MessagePattern.Part.Type.ARG_NAME) {
            return this.msgPattern.getSubstring(part);
        }
        return Integer.toString(part.getValue());
    }

    public void setFormatsByArgumentIndex(Format[] formatArr) {
        if (!this.msgPattern.hasNamedArguments()) {
            int i = 0;
            while (true) {
                i = nextTopLevelArgStart(i);
                if (i >= 0) {
                    int value = this.msgPattern.getPart(i + 1).getValue();
                    if (value < formatArr.length) {
                        setCustomArgStartFormat(i, formatArr[value]);
                    }
                } else {
                    return;
                }
            }
        } else {
            throw new IllegalArgumentException("This method is not available in MessageFormat objects that use alphanumeric argument names.");
        }
    }

    public void setFormatsByArgumentName(Map<String, Format> map) {
        int i = 0;
        while (true) {
            i = nextTopLevelArgStart(i);
            if (i >= 0) {
                String argName = getArgName(i + 1);
                if (map.containsKey(argName)) {
                    setCustomArgStartFormat(i, map.get(argName));
                }
            } else {
                return;
            }
        }
    }

    public void setFormats(Format[] formatArr) {
        int i = 0;
        for (int i2 = 0; i2 < formatArr.length && (i = nextTopLevelArgStart(i)) >= 0; i2++) {
            setCustomArgStartFormat(i, formatArr[i2]);
        }
    }

    public void setFormatByArgumentIndex(int i, Format format) {
        if (!this.msgPattern.hasNamedArguments()) {
            int i2 = 0;
            while (true) {
                i2 = nextTopLevelArgStart(i2);
                if (i2 < 0) {
                    return;
                }
                if (this.msgPattern.getPart(i2 + 1).getValue() == i) {
                    setCustomArgStartFormat(i2, format);
                }
            }
        } else {
            throw new IllegalArgumentException("This method is not available in MessageFormat objects that use alphanumeric argument names.");
        }
    }

    public void setFormatByArgumentName(String str, Format format) {
        int validateArgumentName = MessagePattern.validateArgumentName(str);
        if (validateArgumentName >= -1) {
            int i = 0;
            while (true) {
                i = nextTopLevelArgStart(i);
                if (i < 0) {
                    return;
                }
                if (argNameMatches(i + 1, str, validateArgumentName)) {
                    setCustomArgStartFormat(i, format);
                }
            }
        }
    }

    public void setFormat(int i, Format format) {
        int i2 = 0;
        int i3 = 0;
        while (true) {
            i2 = nextTopLevelArgStart(i2);
            if (i2 < 0) {
                throw new ArrayIndexOutOfBoundsException(i);
            } else if (i3 == i) {
                setCustomArgStartFormat(i2, format);
                return;
            } else {
                i3++;
            }
        }
    }

    public Format[] getFormatsByArgumentIndex() {
        Format format;
        if (!this.msgPattern.hasNamedArguments()) {
            ArrayList arrayList = new ArrayList();
            int i = 0;
            while (true) {
                i = nextTopLevelArgStart(i);
                if (i < 0) {
                    return (Format[]) arrayList.toArray(new Format[arrayList.size()]);
                }
                int value = this.msgPattern.getPart(i + 1).getValue();
                while (true) {
                    format = null;
                    if (value < arrayList.size()) {
                        break;
                    }
                    arrayList.add(null);
                }
                Map<Integer, Format> map = this.cachedFormatters;
                if (map != null) {
                    format = map.get(Integer.valueOf(i));
                }
                arrayList.set(value, format);
            }
        } else {
            throw new IllegalArgumentException("This method is not available in MessageFormat objects that use alphanumeric argument names.");
        }
    }

    public Format[] getFormats() {
        ArrayList arrayList = new ArrayList();
        int i = 0;
        while (true) {
            i = nextTopLevelArgStart(i);
            if (i < 0) {
                return (Format[]) arrayList.toArray(new Format[arrayList.size()]);
            }
            Map<Integer, Format> map = this.cachedFormatters;
            arrayList.add(map == null ? null : map.get(Integer.valueOf(i)));
        }
    }

    public Set<String> getArgumentNames() {
        HashSet hashSet = new HashSet();
        int i = 0;
        while (true) {
            i = nextTopLevelArgStart(i);
            if (i < 0) {
                return hashSet;
            }
            hashSet.add(getArgName(i + 1));
        }
    }

    public Format getFormatByArgumentName(String str) {
        int validateArgumentName;
        if (this.cachedFormatters == null || (validateArgumentName = MessagePattern.validateArgumentName(str)) < -1) {
            return null;
        }
        int i = 0;
        do {
            i = nextTopLevelArgStart(i);
            if (i < 0) {
                return null;
            }
        } while (!argNameMatches(i + 1, str, validateArgumentName));
        return this.cachedFormatters.get(Integer.valueOf(i));
    }

    public final StringBuffer format(Object[] objArr, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        format(objArr, null, new AppendableWrapper(stringBuffer), fieldPosition);
        return stringBuffer;
    }

    public final StringBuffer format(Map<String, Object> map, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        format(null, map, new AppendableWrapper(stringBuffer), fieldPosition);
        return stringBuffer;
    }

    public static String format(String str, Object... objArr) {
        return new MessageFormat(str).format(objArr);
    }

    public static String format(String str, Map<String, Object> map) {
        return new MessageFormat(str).format(map);
    }

    public boolean usesNamedArguments() {
        return this.msgPattern.hasNamedArguments();
    }

    @Override // java.text.Format
    public final StringBuffer format(Object obj, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        format(obj, new AppendableWrapper(stringBuffer), fieldPosition);
        return stringBuffer;
    }

    @Override // java.text.Format
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        if (obj != null) {
            StringBuilder sb = new StringBuilder();
            AppendableWrapper appendableWrapper = new AppendableWrapper(sb);
            appendableWrapper.useAttributes();
            format(obj, appendableWrapper, (FieldPosition) null);
            AttributedString attributedString = new AttributedString(sb.toString());
            for (AttributeAndPosition attributeAndPosition : appendableWrapper.attributes) {
                attributedString.addAttribute(attributeAndPosition.key, attributeAndPosition.value, attributeAndPosition.start, attributeAndPosition.limit);
            }
            return attributedString.getIterator();
        }
        throw new NullPointerException("formatToCharacterIterator must be passed non-null object");
    }

    public Object[] parse(String str, ParsePosition parsePosition) {
        if (!this.msgPattern.hasNamedArguments()) {
            int i = -1;
            int i2 = 0;
            while (true) {
                i2 = nextTopLevelArgStart(i2);
                if (i2 < 0) {
                    break;
                }
                int value = this.msgPattern.getPart(i2 + 1).getValue();
                if (value > i) {
                    i = value;
                }
            }
            Object[] objArr = new Object[(i + 1)];
            int index = parsePosition.getIndex();
            parse(0, str, parsePosition, objArr, null);
            if (parsePosition.getIndex() == index) {
                return null;
            }
            return objArr;
        }
        throw new IllegalArgumentException("This method is not available in MessageFormat objects that use named argument.");
    }

    public Map<String, Object> parseToMap(String str, ParsePosition parsePosition) {
        HashMap hashMap = new HashMap();
        int index = parsePosition.getIndex();
        parse(0, str, parsePosition, null, hashMap);
        if (parsePosition.getIndex() == index) {
            return null;
        }
        return hashMap;
    }

    public Object[] parse(String str) throws ParseException {
        ParsePosition parsePosition = new ParsePosition(0);
        Object[] parse = parse(str, parsePosition);
        if (parsePosition.getIndex() != 0) {
            return parse;
        }
        throw new ParseException("MessageFormat parse error!", parsePosition.getErrorIndex());
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r14v4, types: [java.lang.Integer] */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0168  */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void parse(int i, String str, ParsePosition parsePosition, Object[] objArr, Map<String, Object> map) {
        String str2;
        int i2;
        String str3;
        int i3;
        boolean z;
        Object obj;
        int i4;
        Map<Integer, Format> map2;
        int index;
        Format format;
        if (str != null) {
            String patternString = this.msgPattern.getPatternString();
            int limit = this.msgPattern.getPart(i).getLimit();
            int index2 = parsePosition.getIndex();
            int i5 = 0;
            ParsePosition parsePosition2 = new ParsePosition(0);
            int i6 = i + 1;
            while (true) {
                MessagePattern.Part part = this.msgPattern.getPart(i6);
                MessagePattern.Part.Type type = part.getType();
                int index3 = part.getIndex() - limit;
                if (index3 == 0 || patternString.regionMatches(limit, str, index2, index3)) {
                    index2 += index3;
                    if (type == MessagePattern.Part.Type.MSG_LIMIT) {
                        parsePosition.setIndex(index2);
                        return;
                    }
                    if (type == MessagePattern.Part.Type.SKIP_SYNTAX || type == MessagePattern.Part.Type.INSERT_CHAR) {
                        limit = part.getLimit();
                    } else {
                        int limitPartIndex = this.msgPattern.getLimitPartIndex(i6);
                        MessagePattern.ArgType argType = part.getArgType();
                        int i7 = i6 + 1;
                        MessagePattern.Part part2 = this.msgPattern.getPart(i7);
                        if (objArr != null) {
                            int value = part2.getValue();
                            str2 = null;
                            i2 = value;
                            str3 = Integer.valueOf(value);
                        } else {
                            if (part2.getType() == MessagePattern.Part.Type.ARG_NAME) {
                                str3 = this.msgPattern.getSubstring(part2);
                            } else {
                                str3 = Integer.toString(part2.getValue());
                            }
                            i2 = i5;
                            str2 = str3;
                        }
                        int i8 = i7 + 1;
                        Map<Integer, Format> map3 = this.cachedFormatters;
                        if (map3 != null && (format = map3.get(Integer.valueOf(i8 - 2))) != null) {
                            parsePosition2.setIndex(index2);
                            obj = format.parseObject(str, parsePosition2);
                            if (parsePosition2.getIndex() == index2) {
                                parsePosition.setErrorIndex(index2);
                                return;
                            }
                            index = parsePosition2.getIndex();
                        } else if (argType == MessagePattern.ArgType.NONE || ((map2 = this.cachedFormatters) != null && map2.containsKey(Integer.valueOf(i8 - 2)))) {
                            String literalStringUntilNextArgument = getLiteralStringUntilNextArgument(limitPartIndex);
                            if (literalStringUntilNextArgument.length() != 0) {
                                i4 = str.indexOf(literalStringUntilNextArgument, index2);
                            } else {
                                i4 = str.length();
                            }
                            if (i4 < 0) {
                                parsePosition.setErrorIndex(index2);
                                return;
                            }
                            String substring = str.substring(index2, i4);
                            boolean equals = substring.equals("{" + str3.toString() + "}");
                            if (equals) {
                                substring = null;
                            }
                            z = !equals;
                            i3 = i4;
                            obj = substring;
                            if (z) {
                                if (objArr != null) {
                                    objArr[i2] = obj;
                                } else if (map != null) {
                                    map.put(str2, obj);
                                }
                            }
                            index2 = i3;
                            limit = this.msgPattern.getPart(limitPartIndex).getLimit();
                            i6 = limitPartIndex;
                        } else if (argType == MessagePattern.ArgType.CHOICE) {
                            parsePosition2.setIndex(index2);
                            double parseChoiceArgument = parseChoiceArgument(this.msgPattern, i8, str, parsePosition2);
                            if (parsePosition2.getIndex() == index2) {
                                parsePosition.setErrorIndex(index2);
                                return;
                            } else {
                                obj = Double.valueOf(parseChoiceArgument);
                                index = parsePosition2.getIndex();
                            }
                        } else if (argType.hasPluralStyle() || argType == MessagePattern.ArgType.SELECT) {
                            throw new UnsupportedOperationException("Parsing of plural/select/selectordinal argument is not supported.");
                        } else {
                            throw new IllegalStateException("unexpected argType " + argType);
                        }
                        i3 = index;
                        z = true;
                        if (z) {
                        }
                        index2 = i3;
                        limit = this.msgPattern.getPart(limitPartIndex).getLimit();
                        i6 = limitPartIndex;
                    }
                    i6++;
                    i5 = 0;
                } else {
                    parsePosition.setErrorIndex(index2);
                    return;
                }
            }
        }
    }

    public Map<String, Object> parseToMap(String str) throws ParseException {
        ParsePosition parsePosition = new ParsePosition(0);
        HashMap hashMap = new HashMap();
        parse(0, str, parsePosition, null, hashMap);
        if (parsePosition.getIndex() != 0) {
            return hashMap;
        }
        throw new ParseException("MessageFormat parse error!", parsePosition.getErrorIndex());
    }

    @Override // java.text.Format
    public Object parseObject(String str, ParsePosition parsePosition) {
        if (!this.msgPattern.hasNamedArguments()) {
            return parse(str, parsePosition);
        }
        return parseToMap(str, parsePosition);
    }

    @Override // java.text.Format, java.lang.Object
    public Object clone() {
        MessageFormat messageFormat = (MessageFormat) super.clone();
        if (this.customFormatArgStarts != null) {
            messageFormat.customFormatArgStarts = new HashSet();
            for (Integer num : this.customFormatArgStarts) {
                messageFormat.customFormatArgStarts.add(num);
            }
        } else {
            messageFormat.customFormatArgStarts = null;
        }
        if (this.cachedFormatters != null) {
            messageFormat.cachedFormatters = new HashMap();
            for (Map.Entry<Integer, Format> entry : this.cachedFormatters.entrySet()) {
                messageFormat.cachedFormatters.put(entry.getKey(), entry.getValue());
            }
        } else {
            messageFormat.cachedFormatters = null;
        }
        MessagePattern messagePattern = this.msgPattern;
        messageFormat.msgPattern = messagePattern == null ? null : (MessagePattern) messagePattern.clone();
        DateFormat dateFormat = this.stockDateFormatter;
        messageFormat.stockDateFormatter = dateFormat == null ? null : (DateFormat) dateFormat.clone();
        NumberFormat numberFormat = this.stockNumberFormatter;
        messageFormat.stockNumberFormatter = numberFormat == null ? null : (NumberFormat) numberFormat.clone();
        messageFormat.pluralProvider = null;
        messageFormat.ordinalProvider = null;
        return messageFormat;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MessageFormat messageFormat = (MessageFormat) obj;
        return Objects.equals(this.ulocale, messageFormat.ulocale) && Objects.equals(this.msgPattern, messageFormat.msgPattern) && Objects.equals(this.cachedFormatters, messageFormat.cachedFormatters) && Objects.equals(this.customFormatArgStarts, messageFormat.customFormatArgStarts);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.msgPattern.getPatternString().hashCode();
    }

    public static class Field extends Format.Field {
        public static final Field ARGUMENT = new Field("message argument field");
        private static final long serialVersionUID = 7510380454602616157L;

        protected Field(String str) {
            super(str);
        }

        /* access modifiers changed from: protected */
        @Override // java.text.AttributedCharacterIterator.Attribute
        public Object readResolve() throws InvalidObjectException {
            if (getClass() != Field.class) {
                throw new InvalidObjectException("A subclass of MessageFormat.Field must implement readResolve.");
            } else if (getName().equals(ARGUMENT.getName())) {
                return ARGUMENT;
            } else {
                throw new InvalidObjectException("Unknown attribute name.");
            }
        }
    }

    private DateFormat getStockDateFormatter() {
        if (this.stockDateFormatter == null) {
            this.stockDateFormatter = DateFormat.getDateTimeInstance(3, 3, this.ulocale);
        }
        return this.stockDateFormatter;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private NumberFormat getStockNumberFormatter() {
        if (this.stockNumberFormatter == null) {
            this.stockNumberFormatter = NumberFormat.getInstance(this.ulocale);
        }
        return this.stockNumberFormatter;
    }

    private void format(int i, PluralSelectorContext pluralSelectorContext, Object[] objArr, Map<String, Object> map, AppendableWrapper appendableWrapper, FieldPosition fieldPosition) {
        int i2;
        AppendableWrapper appendableWrapper2;
        String str;
        Integer num;
        String str2;
        FieldPosition fieldPosition2;
        int i3;
        String str3;
        int i4;
        Map<Integer, Format> map2;
        PluralSelectorProvider pluralSelectorProvider;
        Format format;
        Map<String, Object> map3 = map;
        AppendableWrapper appendableWrapper3 = appendableWrapper;
        String patternString = this.msgPattern.getPatternString();
        int limit = this.msgPattern.getPart(i).getLimit();
        int i5 = i + 1;
        FieldPosition fieldPosition3 = fieldPosition;
        while (true) {
            MessagePattern.Part part = this.msgPattern.getPart(i5);
            MessagePattern.Part.Type type = part.getType();
            appendableWrapper3.append(patternString, limit, part.getIndex());
            if (type != MessagePattern.Part.Type.MSG_LIMIT) {
                limit = part.getLimit();
                if (type == MessagePattern.Part.Type.REPLACE_NUMBER) {
                    if (pluralSelectorContext.forReplaceNumber) {
                        appendableWrapper3.formatAndAppend(pluralSelectorContext.formatter, pluralSelectorContext.number, pluralSelectorContext.numberString);
                    } else {
                        appendableWrapper3.formatAndAppend(getStockNumberFormatter(), pluralSelectorContext.number);
                    }
                } else if (type == MessagePattern.Part.Type.ARG_START) {
                    int limitPartIndex = this.msgPattern.getLimitPartIndex(i5);
                    MessagePattern.ArgType argType = part.getArgType();
                    int i6 = i5 + 1;
                    MessagePattern.Part part2 = this.msgPattern.getPart(i6);
                    boolean z = false;
                    String substring = this.msgPattern.getSubstring(part2);
                    Object obj = null;
                    if (objArr != null) {
                        int value = part2.getValue();
                        Integer valueOf = appendableWrapper.attributes != null ? Integer.valueOf(value) : null;
                        if (value < 0 || value >= objArr.length) {
                            z = true;
                        } else {
                            obj = objArr[value];
                        }
                        num = valueOf;
                    } else if (map3 == null || !map3.containsKey(substring)) {
                        num = substring;
                        z = true;
                    } else {
                        obj = map3.get(substring);
                        num = substring;
                    }
                    int i7 = i6 + 1;
                    int i8 = appendableWrapper.length;
                    if (z) {
                        appendableWrapper3.append("{" + substring + "}");
                    } else if (obj == null) {
                        appendableWrapper3.append("null");
                    } else if (pluralSelectorContext == null || pluralSelectorContext.numberArgIndex != i7 - 2) {
                        Map<Integer, Format> map4 = this.cachedFormatters;
                        if (map4 == null || (format = map4.get(Integer.valueOf(i7 - 2))) == null) {
                            i4 = i8;
                            str3 = num;
                            if (argType == MessagePattern.ArgType.NONE || ((map2 = this.cachedFormatters) != null && map2.containsKey(Integer.valueOf(i7 - 2)))) {
                                i2 = limitPartIndex;
                                fieldPosition2 = fieldPosition3;
                                str = patternString;
                                appendableWrapper2 = appendableWrapper3;
                                if (obj instanceof Number) {
                                    appendableWrapper2.formatAndAppend(getStockNumberFormatter(), obj);
                                } else if (obj instanceof Date) {
                                    appendableWrapper2.formatAndAppend(getStockDateFormatter(), obj);
                                } else {
                                    appendableWrapper2.append(obj.toString());
                                }
                            } else if (argType != MessagePattern.ArgType.CHOICE) {
                                i2 = limitPartIndex;
                                str = patternString;
                                if (!argType.hasPluralStyle()) {
                                    fieldPosition2 = fieldPosition3;
                                    appendableWrapper2 = appendableWrapper3;
                                    if (argType == MessagePattern.ArgType.SELECT) {
                                        formatComplexSubMessage(SelectFormat.findSubMessage(this.msgPattern, i7, obj.toString()), null, objArr, map, appendableWrapper);
                                    } else {
                                        throw new IllegalStateException("unexpected argType " + argType);
                                    }
                                } else if (obj instanceof Number) {
                                    if (argType == MessagePattern.ArgType.PLURAL) {
                                        if (this.pluralProvider == null) {
                                            this.pluralProvider = new PluralSelectorProvider(this, PluralRules.PluralType.CARDINAL);
                                        }
                                        pluralSelectorProvider = this.pluralProvider;
                                    } else {
                                        if (this.ordinalProvider == null) {
                                            this.ordinalProvider = new PluralSelectorProvider(this, PluralRules.PluralType.ORDINAL);
                                        }
                                        pluralSelectorProvider = this.ordinalProvider;
                                    }
                                    Number number = (Number) obj;
                                    PluralSelectorContext pluralSelectorContext2 = new PluralSelectorContext(i7, substring, number, this.msgPattern.getPluralOffset(i7));
                                    fieldPosition2 = fieldPosition3;
                                    formatComplexSubMessage(PluralFormat.findSubMessage(this.msgPattern, i7, pluralSelectorProvider, pluralSelectorContext2, number.doubleValue()), pluralSelectorContext2, objArr, map, appendableWrapper);
                                    appendableWrapper2 = appendableWrapper3;
                                } else {
                                    throw new IllegalArgumentException("'" + obj + "' is not a Number");
                                }
                            } else if (obj instanceof Number) {
                                i2 = limitPartIndex;
                                str = patternString;
                                formatComplexSubMessage(findChoiceSubMessage(this.msgPattern, i7, ((Number) obj).doubleValue()), null, objArr, map, appendableWrapper);
                                appendableWrapper2 = appendableWrapper3;
                                i3 = i4;
                                str2 = str3;
                                fieldPosition2 = fieldPosition3;
                                FieldPosition updateMetaData = updateMetaData(appendableWrapper2, i3, fieldPosition2, str2);
                                limit = this.msgPattern.getPart(i2).getLimit();
                                fieldPosition3 = updateMetaData;
                                i5 = i2 + 1;
                                map3 = map;
                                patternString = str;
                                appendableWrapper3 = appendableWrapper2;
                            } else {
                                throw new IllegalArgumentException("'" + obj + "' is not a Number");
                            }
                        } else if ((format instanceof ChoiceFormat) || (format instanceof PluralFormat) || (format instanceof SelectFormat)) {
                            String format2 = format.format(obj);
                            if (format2.indexOf(123) >= 0 || (format2.indexOf(39) >= 0 && !this.msgPattern.jdkAposMode())) {
                                i4 = i8;
                                str3 = num;
                                new MessageFormat(format2, this.ulocale).format(0, null, objArr, map, appendableWrapper, null);
                            } else {
                                if (appendableWrapper.attributes == null) {
                                    appendableWrapper3.append(format2);
                                } else {
                                    appendableWrapper3.formatAndAppend(format, obj);
                                }
                                i4 = i8;
                                str3 = num;
                            }
                            i2 = limitPartIndex;
                            fieldPosition2 = fieldPosition3;
                            str = patternString;
                            appendableWrapper2 = appendableWrapper3;
                        } else {
                            appendableWrapper3.formatAndAppend(format, obj);
                        }
                        i3 = i4;
                        str2 = str3;
                        FieldPosition updateMetaData2 = updateMetaData(appendableWrapper2, i3, fieldPosition2, str2);
                        limit = this.msgPattern.getPart(i2).getLimit();
                        fieldPosition3 = updateMetaData2;
                        i5 = i2 + 1;
                        map3 = map;
                        patternString = str;
                        appendableWrapper3 = appendableWrapper2;
                    } else if (pluralSelectorContext.offset == XPath.MATCH_SCORE_QNAME) {
                        appendableWrapper3.formatAndAppend(pluralSelectorContext.formatter, pluralSelectorContext.number, pluralSelectorContext.numberString);
                    } else {
                        appendableWrapper3.formatAndAppend(pluralSelectorContext.formatter, obj);
                    }
                    fieldPosition2 = fieldPosition3;
                    str = patternString;
                    appendableWrapper2 = appendableWrapper3;
                    i3 = i8;
                    str2 = num;
                    i2 = limitPartIndex;
                    FieldPosition updateMetaData22 = updateMetaData(appendableWrapper2, i3, fieldPosition2, str2);
                    limit = this.msgPattern.getPart(i2).getLimit();
                    fieldPosition3 = updateMetaData22;
                    i5 = i2 + 1;
                    map3 = map;
                    patternString = str;
                    appendableWrapper3 = appendableWrapper2;
                }
                i2 = i5;
                str = patternString;
                appendableWrapper2 = appendableWrapper3;
                i5 = i2 + 1;
                map3 = map;
                patternString = str;
                appendableWrapper3 = appendableWrapper2;
            } else {
                return;
            }
        }
    }

    private void formatComplexSubMessage(int i, PluralSelectorContext pluralSelectorContext, Object[] objArr, Map<String, Object> map, AppendableWrapper appendableWrapper) {
        int index;
        String str;
        if (!this.msgPattern.jdkAposMode()) {
            format(i, pluralSelectorContext, objArr, map, appendableWrapper, null);
            return;
        }
        String patternString = this.msgPattern.getPatternString();
        StringBuilder sb = null;
        int limit = this.msgPattern.getPart(i).getLimit();
        while (true) {
            i++;
            MessagePattern.Part part = this.msgPattern.getPart(i);
            MessagePattern.Part.Type type = part.getType();
            index = part.getIndex();
            if (type == MessagePattern.Part.Type.MSG_LIMIT) {
                break;
            } else if (type == MessagePattern.Part.Type.REPLACE_NUMBER || type == MessagePattern.Part.Type.SKIP_SYNTAX) {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append((CharSequence) patternString, limit, index);
                if (type == MessagePattern.Part.Type.REPLACE_NUMBER) {
                    if (pluralSelectorContext.forReplaceNumber) {
                        sb.append(pluralSelectorContext.numberString);
                    } else {
                        sb.append(getStockNumberFormatter().format(pluralSelectorContext.number));
                    }
                }
                limit = part.getLimit();
            } else if (type == MessagePattern.Part.Type.ARG_START) {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append((CharSequence) patternString, limit, index);
                i = this.msgPattern.getLimitPartIndex(i);
                limit = this.msgPattern.getPart(i).getLimit();
                MessagePattern.appendReducedApostrophes(patternString, index, limit, sb);
            }
        }
        if (sb == null) {
            str = patternString.substring(limit, index);
        } else {
            sb.append((CharSequence) patternString, limit, index);
            str = sb.toString();
        }
        if (str.indexOf(123) >= 0) {
            MessageFormat messageFormat = new MessageFormat("", this.ulocale);
            messageFormat.applyPattern(str, MessagePattern.ApostropheMode.DOUBLE_REQUIRED);
            messageFormat.format(0, null, objArr, map, appendableWrapper, null);
            return;
        }
        appendableWrapper.append(str);
    }

    private String getLiteralStringUntilNextArgument(int i) {
        StringBuilder sb = new StringBuilder();
        String patternString = this.msgPattern.getPatternString();
        int limit = this.msgPattern.getPart(i).getLimit();
        while (true) {
            i++;
            MessagePattern.Part part = this.msgPattern.getPart(i);
            MessagePattern.Part.Type type = part.getType();
            sb.append((CharSequence) patternString, limit, part.getIndex());
            if (type == MessagePattern.Part.Type.ARG_START || type == MessagePattern.Part.Type.MSG_LIMIT) {
                break;
            }
            limit = part.getLimit();
        }
        return sb.toString();
    }

    private FieldPosition updateMetaData(AppendableWrapper appendableWrapper, int i, FieldPosition fieldPosition, Object obj) {
        if (appendableWrapper.attributes != null && i < appendableWrapper.length) {
            appendableWrapper.attributes.add(new AttributeAndPosition(obj, i, appendableWrapper.length));
        }
        if (fieldPosition == null || !Field.ARGUMENT.equals(fieldPosition.getFieldAttribute())) {
            return fieldPosition;
        }
        fieldPosition.setBeginIndex(i);
        fieldPosition.setEndIndex(appendableWrapper.length);
        return null;
    }

    private static int findChoiceSubMessage(MessagePattern messagePattern, int i, double d) {
        int countParts = messagePattern.countParts();
        int i2 = i + 2;
        while (true) {
            int limitPartIndex = messagePattern.getLimitPartIndex(i2) + 1;
            if (limitPartIndex >= countParts) {
                break;
            }
            int i3 = limitPartIndex + 1;
            MessagePattern.Part part = messagePattern.getPart(limitPartIndex);
            if (part.getType() == MessagePattern.Part.Type.ARG_LIMIT) {
                break;
            }
            double numericValue = messagePattern.getNumericValue(part);
            int i4 = i3 + 1;
            if (messagePattern.getPatternString().charAt(messagePattern.getPatternIndex(i3)) == '<') {
                if (d <= numericValue) {
                    break;
                }
            } else if (d < numericValue) {
                break;
            }
            i2 = i4;
        }
        return i2;
    }

    private static double parseChoiceArgument(MessagePattern messagePattern, int i, String str, ParsePosition parsePosition) {
        int i2;
        int index = parsePosition.getIndex();
        double d = Double.NaN;
        int i3 = index;
        while (true) {
            if (messagePattern.getPartType(i) == MessagePattern.Part.Type.ARG_LIMIT) {
                i2 = i3;
                break;
            }
            double numericValue = messagePattern.getNumericValue(messagePattern.getPart(i));
            int i4 = i + 2;
            int limitPartIndex = messagePattern.getLimitPartIndex(i4);
            int matchStringUntilLimitPart = matchStringUntilLimitPart(messagePattern, i4, limitPartIndex, str, index);
            if (matchStringUntilLimitPart >= 0 && (i2 = matchStringUntilLimitPart + index) > i3) {
                if (i2 == str.length()) {
                    d = numericValue;
                    break;
                }
                i3 = i2;
                d = numericValue;
            }
            i = limitPartIndex + 1;
        }
        if (i2 == index) {
            parsePosition.setErrorIndex(index);
        } else {
            parsePosition.setIndex(i2);
        }
        return d;
    }

    private static int matchStringUntilLimitPart(MessagePattern messagePattern, int i, int i2, String str, int i3) {
        String patternString = messagePattern.getPatternString();
        int limit = messagePattern.getPart(i).getLimit();
        int i4 = 0;
        while (true) {
            i++;
            MessagePattern.Part part = messagePattern.getPart(i);
            if (i == i2 || part.getType() == MessagePattern.Part.Type.SKIP_SYNTAX) {
                int index = part.getIndex() - limit;
                if (index != 0 && !str.regionMatches(i3, patternString, limit, index)) {
                    return -1;
                }
                i4 += index;
                if (i == i2) {
                    return i4;
                }
                limit = part.getLimit();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int findOtherSubMessage(int i) {
        int countParts = this.msgPattern.countParts();
        if (this.msgPattern.getPart(i).getType().hasNumericValue()) {
            i++;
        }
        do {
            int i2 = i + 1;
            MessagePattern.Part part = this.msgPattern.getPart(i);
            if (part.getType() == MessagePattern.Part.Type.ARG_LIMIT) {
                return 0;
            }
            if (this.msgPattern.partSubstringMatches(part, "other")) {
                return i2;
            }
            if (this.msgPattern.getPartType(i2).hasNumericValue()) {
                i2++;
            }
            i = this.msgPattern.getLimitPartIndex(i2) + 1;
        } while (i < countParts);
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int findFirstPluralNumberArg(int i, String str) {
        while (true) {
            i++;
            MessagePattern.Part part = this.msgPattern.getPart(i);
            MessagePattern.Part.Type type = part.getType();
            if (type == MessagePattern.Part.Type.MSG_LIMIT) {
                return 0;
            }
            if (type == MessagePattern.Part.Type.REPLACE_NUMBER) {
                return -1;
            }
            if (type == MessagePattern.Part.Type.ARG_START) {
                MessagePattern.ArgType argType = part.getArgType();
                if (str.length() != 0 && (argType == MessagePattern.ArgType.NONE || argType == MessagePattern.ArgType.SIMPLE)) {
                    if (this.msgPattern.partSubstringMatches(this.msgPattern.getPart(i + 1), str)) {
                        return i;
                    }
                }
                i = this.msgPattern.getLimitPartIndex(i);
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class PluralSelectorContext {
        String argName;
        boolean forReplaceNumber;
        Format formatter;
        Number number;
        int numberArgIndex;
        String numberString;
        double offset;
        int startIndex;

        private PluralSelectorContext(int i, String str, Number number2, double d) {
            this.startIndex = i;
            this.argName = str;
            if (d == XPath.MATCH_SCORE_QNAME) {
                this.number = number2;
            } else {
                this.number = Double.valueOf(number2.doubleValue() - d);
            }
            this.offset = d;
        }

        public String toString() {
            throw new AssertionError("PluralSelectorContext being formatted, rather than its number");
        }
    }

    /* access modifiers changed from: private */
    public static final class PluralSelectorProvider implements PluralFormat.PluralSelector {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private MessageFormat msgFormat;
        private PluralRules rules;
        private PluralRules.PluralType type;

        public PluralSelectorProvider(MessageFormat messageFormat, PluralRules.PluralType pluralType) {
            this.msgFormat = messageFormat;
            this.type = pluralType;
        }

        @Override // ohos.global.icu.text.PluralFormat.PluralSelector
        public String select(Object obj, double d) {
            if (this.rules == null) {
                this.rules = PluralRules.forLocale(this.msgFormat.ulocale, this.type);
            }
            PluralSelectorContext pluralSelectorContext = (PluralSelectorContext) obj;
            pluralSelectorContext.numberArgIndex = this.msgFormat.findFirstPluralNumberArg(this.msgFormat.findOtherSubMessage(pluralSelectorContext.startIndex), pluralSelectorContext.argName);
            if (pluralSelectorContext.numberArgIndex > 0 && this.msgFormat.cachedFormatters != null) {
                pluralSelectorContext.formatter = (Format) this.msgFormat.cachedFormatters.get(Integer.valueOf(pluralSelectorContext.numberArgIndex));
            }
            if (pluralSelectorContext.formatter == null) {
                pluralSelectorContext.formatter = this.msgFormat.getStockNumberFormatter();
                pluralSelectorContext.forReplaceNumber = true;
            }
            pluralSelectorContext.numberString = pluralSelectorContext.formatter.format(pluralSelectorContext.number);
            if (!(pluralSelectorContext.formatter instanceof DecimalFormat)) {
                return this.rules.select(d);
            }
            return this.rules.select(((DecimalFormat) pluralSelectorContext.formatter).getFixedDecimal(d));
        }
    }

    private void format(Object obj, AppendableWrapper appendableWrapper, FieldPosition fieldPosition) {
        if (obj == null || (obj instanceof Map)) {
            format(null, (Map) obj, appendableWrapper, fieldPosition);
        } else {
            format((Object[]) obj, null, appendableWrapper, fieldPosition);
        }
    }

    private void format(Object[] objArr, Map<String, Object> map, AppendableWrapper appendableWrapper, FieldPosition fieldPosition) {
        if (objArr == null || !this.msgPattern.hasNamedArguments()) {
            format(0, null, objArr, map, appendableWrapper, fieldPosition);
            return;
        }
        throw new IllegalArgumentException("This method is not available in MessageFormat objects that use alphanumeric argument names.");
    }

    private void resetPattern() {
        MessagePattern messagePattern = this.msgPattern;
        if (messagePattern != null) {
            messagePattern.clear();
        }
        Map<Integer, Format> map = this.cachedFormatters;
        if (map != null) {
            map.clear();
        }
        this.customFormatArgStarts = null;
    }

    /* access modifiers changed from: package-private */
    public Format dateTimeFormatForPatternOrSkeleton(String str) {
        int skipWhiteSpace = PatternProps.skipWhiteSpace(str, 0);
        if (str.regionMatches(skipWhiteSpace, "::", 0, 2)) {
            return DateFormat.getInstanceForSkeleton(str.substring(skipWhiteSpace + 2), this.ulocale);
        }
        return new SimpleDateFormat(str, this.ulocale);
    }

    private Format createAppropriateFormat(String str, String str2) {
        Format format;
        int findKeyword = findKeyword(str, typeList);
        if (findKeyword == 0) {
            int findKeyword2 = findKeyword(str2, modifierList);
            if (findKeyword2 == 0) {
                format = NumberFormat.getInstance(this.ulocale);
            } else if (findKeyword2 == 1) {
                format = NumberFormat.getCurrencyInstance(this.ulocale);
            } else if (findKeyword2 == 2) {
                format = NumberFormat.getPercentInstance(this.ulocale);
            } else if (findKeyword2 != 3) {
                int skipWhiteSpace = PatternProps.skipWhiteSpace(str2, 0);
                if (!str2.regionMatches(skipWhiteSpace, "::", 0, 2)) {
                    return new DecimalFormat(str2, new DecimalFormatSymbols(this.ulocale));
                }
                format = NumberFormatter.forSkeleton(str2.substring(skipWhiteSpace + 2)).locale(this.ulocale).toFormat();
            } else {
                format = NumberFormat.getIntegerInstance(this.ulocale);
            }
            return format;
        } else if (findKeyword == 1) {
            int findKeyword3 = findKeyword(str2, dateModifierList);
            if (findKeyword3 == 0) {
                return DateFormat.getDateInstance(2, this.ulocale);
            }
            if (findKeyword3 == 1) {
                return DateFormat.getDateInstance(3, this.ulocale);
            }
            if (findKeyword3 == 2) {
                return DateFormat.getDateInstance(2, this.ulocale);
            }
            if (findKeyword3 == 3) {
                return DateFormat.getDateInstance(1, this.ulocale);
            }
            if (findKeyword3 != 4) {
                return dateTimeFormatForPatternOrSkeleton(str2);
            }
            return DateFormat.getDateInstance(0, this.ulocale);
        } else if (findKeyword == 2) {
            int findKeyword4 = findKeyword(str2, dateModifierList);
            if (findKeyword4 == 0) {
                return DateFormat.getTimeInstance(2, this.ulocale);
            }
            if (findKeyword4 == 1) {
                return DateFormat.getTimeInstance(3, this.ulocale);
            }
            if (findKeyword4 == 2) {
                return DateFormat.getTimeInstance(2, this.ulocale);
            }
            if (findKeyword4 == 3) {
                return DateFormat.getTimeInstance(1, this.ulocale);
            }
            if (findKeyword4 != 4) {
                return dateTimeFormatForPatternOrSkeleton(str2);
            }
            return DateFormat.getTimeInstance(0, this.ulocale);
        } else if (findKeyword == 3) {
            RuleBasedNumberFormat ruleBasedNumberFormat = new RuleBasedNumberFormat(this.ulocale, 1);
            String trim = str2.trim();
            if (trim.length() == 0) {
                return ruleBasedNumberFormat;
            }
            ruleBasedNumberFormat.setDefaultRuleSet(trim);
            return ruleBasedNumberFormat;
        } else if (findKeyword == 4) {
            RuleBasedNumberFormat ruleBasedNumberFormat2 = new RuleBasedNumberFormat(this.ulocale, 2);
            String trim2 = str2.trim();
            if (trim2.length() == 0) {
                return ruleBasedNumberFormat2;
            }
            ruleBasedNumberFormat2.setDefaultRuleSet(trim2);
            return ruleBasedNumberFormat2;
        } else if (findKeyword == 5) {
            RuleBasedNumberFormat ruleBasedNumberFormat3 = new RuleBasedNumberFormat(this.ulocale, 3);
            String trim3 = str2.trim();
            if (trim3.length() == 0) {
                return ruleBasedNumberFormat3;
            }
            try {
                ruleBasedNumberFormat3.setDefaultRuleSet(trim3);
                return ruleBasedNumberFormat3;
            } catch (Exception unused) {
                return ruleBasedNumberFormat3;
            }
        } else {
            throw new IllegalArgumentException("Unknown format type \"" + str + "\"");
        }
    }

    private static final int findKeyword(String str, String[] strArr) {
        String lowerCase = PatternProps.trimWhiteSpace(str).toLowerCase(rootLocale);
        for (int i = 0; i < strArr.length; i++) {
            if (lowerCase.equals(strArr[i])) {
                return i;
            }
        }
        return -1;
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeObject(this.ulocale.toLanguageTag());
        if (this.msgPattern == null) {
            this.msgPattern = new MessagePattern();
        }
        objectOutputStream.writeObject(this.msgPattern.getApostropheMode());
        objectOutputStream.writeObject(this.msgPattern.getPatternString());
        Set<Integer> set = this.customFormatArgStarts;
        if (set != null && !set.isEmpty()) {
            objectOutputStream.writeInt(this.customFormatArgStarts.size());
            int i = 0;
            int i2 = 0;
            while (true) {
                i = nextTopLevelArgStart(i);
                if (i < 0) {
                    break;
                }
                if (this.customFormatArgStarts.contains(Integer.valueOf(i))) {
                    objectOutputStream.writeInt(i2);
                    objectOutputStream.writeObject(this.cachedFormatters.get(Integer.valueOf(i)));
                }
                i2++;
            }
        } else {
            objectOutputStream.writeInt(0);
        }
        objectOutputStream.writeInt(0);
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.ulocale = ULocale.forLanguageTag((String) objectInputStream.readObject());
        MessagePattern.ApostropheMode apostropheMode = (MessagePattern.ApostropheMode) objectInputStream.readObject();
        MessagePattern messagePattern = this.msgPattern;
        if (messagePattern == null || apostropheMode != messagePattern.getApostropheMode()) {
            this.msgPattern = new MessagePattern(apostropheMode);
        }
        String str = (String) objectInputStream.readObject();
        if (str != null) {
            applyPattern(str);
        }
        for (int readInt = objectInputStream.readInt(); readInt > 0; readInt--) {
            setFormat(objectInputStream.readInt(), (Format) objectInputStream.readObject());
        }
        for (int readInt2 = objectInputStream.readInt(); readInt2 > 0; readInt2--) {
            objectInputStream.readInt();
            objectInputStream.readObject();
        }
    }

    private void cacheExplicitFormats() {
        String str;
        Map<Integer, Format> map = this.cachedFormatters;
        if (map != null) {
            map.clear();
        }
        this.customFormatArgStarts = null;
        int countParts = this.msgPattern.countParts() - 2;
        int i = 1;
        while (i < countParts) {
            MessagePattern.Part part = this.msgPattern.getPart(i);
            if (part.getType() == MessagePattern.Part.Type.ARG_START && part.getArgType() == MessagePattern.ArgType.SIMPLE) {
                int i2 = i + 2;
                MessagePattern messagePattern = this.msgPattern;
                int i3 = i2 + 1;
                String substring = messagePattern.getSubstring(messagePattern.getPart(i2));
                MessagePattern.Part part2 = this.msgPattern.getPart(i3);
                if (part2.getType() == MessagePattern.Part.Type.ARG_STYLE) {
                    str = this.msgPattern.getSubstring(part2);
                    i3++;
                } else {
                    str = "";
                }
                setArgStartFormat(i, createAppropriateFormat(substring, str));
                i = i3;
            }
            i++;
        }
    }

    private void setArgStartFormat(int i, Format format) {
        if (this.cachedFormatters == null) {
            this.cachedFormatters = new HashMap();
        }
        this.cachedFormatters.put(Integer.valueOf(i), format);
    }

    private void setCustomArgStartFormat(int i, Format format) {
        setArgStartFormat(i, format);
        if (this.customFormatArgStarts == null) {
            this.customFormatArgStarts = new HashSet();
        }
        this.customFormatArgStarts.add(Integer.valueOf(i));
    }

    public static String autoQuoteApostrophe(String str) {
        StringBuilder sb = new StringBuilder(str.length() * 2);
        int length = str.length();
        boolean z = false;
        int i = 0;
        for (int i2 = 0; i2 < length; i2++) {
            char charAt = str.charAt(i2);
            if (z) {
                if (!z) {
                    if (!z) {
                        if (z) {
                            if (charAt == '{') {
                                i++;
                            } else if (charAt == '}') {
                                i--;
                                if (i != 0) {
                                }
                            }
                        }
                    } else if (charAt != '\'') {
                    }
                } else if (charAt != '\'') {
                    if (charAt == '{' || charAt == '}') {
                        z = true;
                    } else {
                        sb.append('\'');
                    }
                }
                z = false;
            } else if (charAt == '\'') {
                z = true;
            } else if (charAt == '{') {
                i++;
                z = true;
            }
            sb.append(charAt);
        }
        if (z || z) {
            sb.append('\'');
        }
        return new String(sb);
    }

    /* access modifiers changed from: private */
    public static final class AppendableWrapper {
        private Appendable app;
        private List<AttributeAndPosition> attributes = null;
        private int length;

        public AppendableWrapper(StringBuilder sb) {
            this.app = sb;
            this.length = sb.length();
        }

        public AppendableWrapper(StringBuffer stringBuffer) {
            this.app = stringBuffer;
            this.length = stringBuffer.length();
        }

        public void useAttributes() {
            this.attributes = new ArrayList();
        }

        public void append(CharSequence charSequence) {
            try {
                this.app.append(charSequence);
                this.length += charSequence.length();
            } catch (IOException e) {
                throw new ICUUncheckedIOException(e);
            }
        }

        public void append(CharSequence charSequence, int i, int i2) {
            try {
                this.app.append(charSequence, i, i2);
                this.length += i2 - i;
            } catch (IOException e) {
                throw new ICUUncheckedIOException(e);
            }
        }

        public void append(CharacterIterator characterIterator) {
            this.length += append(this.app, characterIterator);
        }

        public static int append(Appendable appendable, CharacterIterator characterIterator) {
            try {
                int beginIndex = characterIterator.getBeginIndex();
                int endIndex = characterIterator.getEndIndex();
                int i = endIndex - beginIndex;
                if (beginIndex < endIndex) {
                    appendable.append(characterIterator.first());
                    while (true) {
                        beginIndex++;
                        if (beginIndex >= endIndex) {
                            break;
                        }
                        appendable.append(characterIterator.next());
                    }
                }
                return i;
            } catch (IOException e) {
                throw new ICUUncheckedIOException(e);
            }
        }

        public void formatAndAppend(Format format, Object obj) {
            if (this.attributes == null) {
                append(format.format(obj));
                return;
            }
            AttributedCharacterIterator formatToCharacterIterator = format.formatToCharacterIterator(obj);
            int i = this.length;
            append(formatToCharacterIterator);
            formatToCharacterIterator.first();
            int index = formatToCharacterIterator.getIndex();
            int endIndex = formatToCharacterIterator.getEndIndex();
            int i2 = i - index;
            while (index < endIndex) {
                Map<AttributedCharacterIterator.Attribute, Object> attributes2 = formatToCharacterIterator.getAttributes();
                int runLimit = formatToCharacterIterator.getRunLimit();
                if (attributes2.size() != 0) {
                    for (Map.Entry<AttributedCharacterIterator.Attribute, Object> entry : attributes2.entrySet()) {
                        this.attributes.add(new AttributeAndPosition(entry.getKey(), entry.getValue(), i2 + index, i2 + runLimit));
                    }
                }
                formatToCharacterIterator.setIndex(runLimit);
                index = runLimit;
            }
        }

        public void formatAndAppend(Format format, Object obj, String str) {
            if (this.attributes != null || str == null) {
                formatAndAppend(format, obj);
            } else {
                append(str);
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class AttributeAndPosition {
        private AttributedCharacterIterator.Attribute key;
        private int limit;
        private int start;
        private Object value;

        public AttributeAndPosition(Object obj, int i, int i2) {
            init(Field.ARGUMENT, obj, i, i2);
        }

        public AttributeAndPosition(AttributedCharacterIterator.Attribute attribute, Object obj, int i, int i2) {
            init(attribute, obj, i, i2);
        }

        public void init(AttributedCharacterIterator.Attribute attribute, Object obj, int i, int i2) {
            this.key = attribute;
            this.value = obj;
            this.start = i;
            this.limit = i2;
        }
    }
}
