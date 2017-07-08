package java.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.regex.Pattern;

public class MessageFormat extends Format {
    private static final int[] DATE_TIME_MODIFIERS = null;
    private static final String[] DATE_TIME_MODIFIER_KEYWORDS = null;
    private static final int INITIAL_FORMATS = 10;
    private static final int MODIFIER_CURRENCY = 1;
    private static final int MODIFIER_DEFAULT = 0;
    private static final int MODIFIER_FULL = 4;
    private static final int MODIFIER_INTEGER = 3;
    private static final int MODIFIER_LONG = 3;
    private static final int MODIFIER_MEDIUM = 2;
    private static final int MODIFIER_PERCENT = 2;
    private static final int MODIFIER_SHORT = 1;
    private static final String[] NUMBER_MODIFIER_KEYWORDS = null;
    private static final int SEG_INDEX = 1;
    private static final int SEG_MODIFIER = 3;
    private static final int SEG_RAW = 0;
    private static final int SEG_TYPE = 2;
    private static final int TYPE_CHOICE = 4;
    private static final int TYPE_DATE = 2;
    private static final String[] TYPE_KEYWORDS = null;
    private static final int TYPE_NULL = 0;
    private static final int TYPE_NUMBER = 1;
    private static final int TYPE_TIME = 3;
    private static final long serialVersionUID = 6479157306784022952L;
    private int[] argumentNumbers;
    private Format[] formats;
    private Locale locale;
    private int maxOffset;
    private int[] offsets;
    private String pattern;

    public static class Field extends java.text.Format.Field {
        public static final Field ARGUMENT = null;
        private static final long serialVersionUID = 7899943957617360810L;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.text.MessageFormat.Field.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.text.MessageFormat.Field.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.text.MessageFormat.Field.<clinit>():void");
        }

        protected Field(String name) {
            super(name);
        }

        protected Object readResolve() throws InvalidObjectException {
            if (getClass() == Field.class) {
                return ARGUMENT;
            }
            throw new InvalidObjectException("subclass didn't correctly implement readResolve");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.text.MessageFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.text.MessageFormat.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.text.MessageFormat.<clinit>():void");
    }

    public MessageFormat(String pattern) {
        this.pattern = "";
        this.formats = new Format[INITIAL_FORMATS];
        this.offsets = new int[INITIAL_FORMATS];
        this.argumentNumbers = new int[INITIAL_FORMATS];
        this.maxOffset = -1;
        this.locale = Locale.getDefault(Category.FORMAT);
        applyPattern(pattern);
    }

    public MessageFormat(String pattern, Locale locale) {
        this.pattern = "";
        this.formats = new Format[INITIAL_FORMATS];
        this.offsets = new int[INITIAL_FORMATS];
        this.argumentNumbers = new int[INITIAL_FORMATS];
        this.maxOffset = -1;
        this.locale = locale;
        applyPattern(pattern);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void applyPattern(String pattern) {
        StringBuilder[] segments = new StringBuilder[TYPE_CHOICE];
        segments[TYPE_NULL] = new StringBuilder();
        int part = TYPE_NULL;
        int formatNumber = TYPE_NULL;
        boolean inQuote = false;
        int braceStack = TYPE_NULL;
        this.maxOffset = -1;
        int i = TYPE_NULL;
        while (i < pattern.length()) {
            char ch = pattern.charAt(i);
            if (part == 0) {
                if (ch == '\'') {
                    if (i + TYPE_NUMBER >= pattern.length() || pattern.charAt(i + TYPE_NUMBER) != '\'') {
                        inQuote = !inQuote;
                    } else {
                        segments[part].append(ch);
                        i += TYPE_NUMBER;
                    }
                } else if (ch != '{' || inQuote) {
                    segments[part].append(ch);
                } else {
                    part = TYPE_NUMBER;
                    if (segments[TYPE_NUMBER] == null) {
                        segments[TYPE_NUMBER] = new StringBuilder();
                    }
                }
            } else if (inQuote) {
                segments[part].append(ch);
                if (ch == '\'') {
                    inQuote = false;
                }
            } else {
                switch (ch) {
                    case Pattern.DOTALL /*32*/:
                        if (part != TYPE_DATE || segments[TYPE_DATE].length() > 0) {
                            segments[part].append(ch);
                            break;
                        }
                        continue;
                        break;
                    case '\'':
                        inQuote = true;
                        break;
                    case ',':
                        if (part >= TYPE_TIME) {
                            segments[part].append(ch);
                            break;
                        }
                        part += TYPE_NUMBER;
                        if (segments[part] == null) {
                            segments[part] = new StringBuilder();
                            break;
                        }
                        continue;
                    case '{':
                        braceStack += TYPE_NUMBER;
                        segments[part].append(ch);
                        continue;
                    case '}':
                        if (braceStack != 0) {
                            braceStack--;
                            segments[part].append(ch);
                            break;
                        }
                        part = TYPE_NULL;
                        makeFormat(i, formatNumber, segments);
                        formatNumber += TYPE_NUMBER;
                        segments[TYPE_NUMBER] = null;
                        segments[TYPE_DATE] = null;
                        segments[TYPE_TIME] = null;
                        continue;
                }
                segments[part].append(ch);
            }
            i += TYPE_NUMBER;
        }
        if (braceStack != 0 || part == 0) {
            this.pattern = segments[TYPE_NULL].toString();
        } else {
            this.maxOffset = -1;
            throw new IllegalArgumentException("Unmatched braces in the pattern.");
        }
    }

    public String toPattern() {
        int lastOffset = TYPE_NULL;
        StringBuilder result = new StringBuilder();
        for (int i = TYPE_NULL; i <= this.maxOffset; i += TYPE_NUMBER) {
            copyAndFixQuotes(this.pattern, lastOffset, this.offsets[i], result);
            lastOffset = this.offsets[i];
            result.append('{').append(this.argumentNumbers[i]);
            Format fmt = this.formats[i];
            if (fmt != null) {
                if (fmt instanceof NumberFormat) {
                    if (fmt.equals(NumberFormat.getInstance(this.locale))) {
                        result.append(",number");
                    } else if (fmt.equals(NumberFormat.getCurrencyInstance(this.locale))) {
                        result.append(",number,currency");
                    } else if (fmt.equals(NumberFormat.getPercentInstance(this.locale))) {
                        result.append(",number,percent");
                    } else if (fmt.equals(NumberFormat.getIntegerInstance(this.locale))) {
                        result.append(",number,integer");
                    } else if (fmt instanceof DecimalFormat) {
                        result.append(",number,").append(((DecimalFormat) fmt).toPattern());
                    } else if (fmt instanceof ChoiceFormat) {
                        result.append(",choice,").append(((ChoiceFormat) fmt).toPattern());
                    }
                } else if (fmt instanceof DateFormat) {
                    int index = TYPE_NULL;
                    while (index < DATE_TIME_MODIFIERS.length) {
                        if (fmt.equals(DateFormat.getDateInstance(DATE_TIME_MODIFIERS[index], this.locale))) {
                            result.append(",date");
                            break;
                        } else if (fmt.equals(DateFormat.getTimeInstance(DATE_TIME_MODIFIERS[index], this.locale))) {
                            result.append(",time");
                            break;
                        } else {
                            index += TYPE_NUMBER;
                        }
                    }
                    if (index >= DATE_TIME_MODIFIERS.length) {
                        if (fmt instanceof SimpleDateFormat) {
                            result.append(",date,").append(((SimpleDateFormat) fmt).toPattern());
                        }
                    } else if (index != 0) {
                        result.append(',').append(DATE_TIME_MODIFIER_KEYWORDS[index]);
                    }
                }
            }
            result.append('}');
        }
        copyAndFixQuotes(this.pattern, lastOffset, this.pattern.length(), result);
        return result.toString();
    }

    public void setFormatsByArgumentIndex(Format[] newFormats) {
        for (int i = TYPE_NULL; i <= this.maxOffset; i += TYPE_NUMBER) {
            int j = this.argumentNumbers[i];
            if (j < newFormats.length) {
                this.formats[i] = newFormats[j];
            }
        }
    }

    public void setFormats(Format[] newFormats) {
        int runsToCopy = newFormats.length;
        if (runsToCopy > this.maxOffset + TYPE_NUMBER) {
            runsToCopy = this.maxOffset + TYPE_NUMBER;
        }
        for (int i = TYPE_NULL; i < runsToCopy; i += TYPE_NUMBER) {
            this.formats[i] = newFormats[i];
        }
    }

    public void setFormatByArgumentIndex(int argumentIndex, Format newFormat) {
        for (int j = TYPE_NULL; j <= this.maxOffset; j += TYPE_NUMBER) {
            if (this.argumentNumbers[j] == argumentIndex) {
                this.formats[j] = newFormat;
            }
        }
    }

    public void setFormat(int formatElementIndex, Format newFormat) {
        if (formatElementIndex > this.maxOffset) {
            throw new ArrayIndexOutOfBoundsException(this.maxOffset, formatElementIndex);
        }
        this.formats[formatElementIndex] = newFormat;
    }

    public Format[] getFormatsByArgumentIndex() {
        int i;
        int maximumArgumentNumber = -1;
        for (i = TYPE_NULL; i <= this.maxOffset; i += TYPE_NUMBER) {
            if (this.argumentNumbers[i] > maximumArgumentNumber) {
                maximumArgumentNumber = this.argumentNumbers[i];
            }
        }
        Format[] resultArray = new Format[(maximumArgumentNumber + TYPE_NUMBER)];
        for (i = TYPE_NULL; i <= this.maxOffset; i += TYPE_NUMBER) {
            resultArray[this.argumentNumbers[i]] = this.formats[i];
        }
        return resultArray;
    }

    public Format[] getFormats() {
        Object resultArray = new Format[(this.maxOffset + TYPE_NUMBER)];
        System.arraycopy(this.formats, (int) TYPE_NULL, resultArray, (int) TYPE_NULL, this.maxOffset + TYPE_NUMBER);
        return resultArray;
    }

    public final StringBuffer format(Object[] arguments, StringBuffer result, FieldPosition pos) {
        return subformat(arguments, result, pos, null);
    }

    public static String format(String pattern, Object... arguments) {
        return new MessageFormat(pattern).format(arguments);
    }

    public final StringBuffer format(Object arguments, StringBuffer result, FieldPosition pos) {
        return subformat((Object[]) arguments, result, pos, null);
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object arguments) {
        StringBuffer result = new StringBuffer();
        ArrayList iterators = new ArrayList();
        if (arguments == null) {
            throw new NullPointerException("formatToCharacterIterator must be passed non-null object");
        }
        subformat((Object[]) arguments, result, null, iterators);
        if (iterators.size() == 0) {
            return createAttributedCharacterIterator("");
        }
        return createAttributedCharacterIterator((AttributedCharacterIterator[]) iterators.toArray(new AttributedCharacterIterator[iterators.size()]));
    }

    public Object[] parse(String source, ParsePosition pos) {
        if (source == null) {
            return new Object[TYPE_NULL];
        }
        int i;
        int len;
        int maximumArgumentNumber = -1;
        for (i = TYPE_NULL; i <= this.maxOffset; i += TYPE_NUMBER) {
            if (this.argumentNumbers[i] > maximumArgumentNumber) {
                maximumArgumentNumber = this.argumentNumbers[i];
            }
        }
        Object[] resultArray = new Object[(maximumArgumentNumber + TYPE_NUMBER)];
        int patternOffset = TYPE_NULL;
        int sourceOffset = pos.index;
        ParsePosition tempStatus = new ParsePosition(TYPE_NULL);
        i = TYPE_NULL;
        while (i <= this.maxOffset) {
            len = this.offsets[i] - patternOffset;
            if (len == 0 || this.pattern.regionMatches(patternOffset, source, sourceOffset, len)) {
                sourceOffset += len;
                patternOffset += len;
                if (this.formats[i] == null) {
                    int next;
                    int tempLength = i != this.maxOffset ? this.offsets[i + TYPE_NUMBER] : this.pattern.length();
                    if (patternOffset >= tempLength) {
                        next = source.length();
                    } else {
                        next = source.indexOf(this.pattern.substring(patternOffset, tempLength), sourceOffset);
                    }
                    if (next < 0) {
                        pos.errorIndex = sourceOffset;
                        return null;
                    }
                    if (!source.substring(sourceOffset, next).equals("{" + this.argumentNumbers[i] + "}")) {
                        resultArray[this.argumentNumbers[i]] = source.substring(sourceOffset, next);
                    }
                    sourceOffset = next;
                } else {
                    tempStatus.index = sourceOffset;
                    resultArray[this.argumentNumbers[i]] = this.formats[i].parseObject(source, tempStatus);
                    if (tempStatus.index == sourceOffset) {
                        pos.errorIndex = sourceOffset;
                        return null;
                    }
                    sourceOffset = tempStatus.index;
                }
                i += TYPE_NUMBER;
            } else {
                pos.errorIndex = sourceOffset;
                return null;
            }
        }
        len = this.pattern.length() - patternOffset;
        if (len == 0 || this.pattern.regionMatches(patternOffset, source, sourceOffset, len)) {
            pos.index = sourceOffset + len;
            return resultArray;
        }
        pos.errorIndex = sourceOffset;
        return null;
    }

    public Object[] parse(String source) throws ParseException {
        ParsePosition pos = new ParsePosition(TYPE_NULL);
        Object[] result = parse(source, pos);
        if (pos.index != 0) {
            return result;
        }
        throw new ParseException("MessageFormat parse error!", pos.errorIndex);
    }

    public Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }

    public Object clone() {
        MessageFormat other = (MessageFormat) super.clone();
        other.formats = (Format[]) this.formats.clone();
        for (int i = TYPE_NULL; i < this.formats.length; i += TYPE_NUMBER) {
            if (this.formats[i] != null) {
                other.formats[i] = (Format) this.formats[i].clone();
            }
        }
        other.offsets = (int[]) this.offsets.clone();
        other.argumentNumbers = (int[]) this.argumentNumbers.clone();
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
        if (this.maxOffset == other.maxOffset && this.pattern.equals(other.pattern) && (((this.locale != null && this.locale.equals(other.locale)) || (this.locale == null && other.locale == null)) && Arrays.equals(this.offsets, other.offsets) && Arrays.equals(this.argumentNumbers, other.argumentNumbers))) {
            z = Arrays.equals(this.formats, other.formats);
        }
        return z;
    }

    public int hashCode() {
        return this.pattern.hashCode();
    }

    private StringBuffer subformat(Object[] arguments, StringBuffer result, FieldPosition fp, List characterIterators) {
        int lastOffset = TYPE_NULL;
        int last = result.length();
        for (int i = TYPE_NULL; i <= this.maxOffset; i += TYPE_NUMBER) {
            result.append(this.pattern.substring(lastOffset, this.offsets[i]));
            lastOffset = this.offsets[i];
            int argumentNumber = this.argumentNumbers[i];
            if (arguments == null || argumentNumber >= arguments.length) {
                result.append('{').append(argumentNumber).append('}');
            } else {
                Object obj;
                String obj2 = arguments[argumentNumber];
                String arg = null;
                Format subFormatter = null;
                if (obj2 == null) {
                    arg = "null";
                } else if (this.formats[i] != null) {
                    subFormatter = this.formats[i];
                    if (subFormatter instanceof ChoiceFormat) {
                        arg = this.formats[i].format(obj2);
                        if (arg.indexOf(123) >= 0) {
                            subFormatter = new MessageFormat(arg, this.locale);
                            obj = arguments;
                            arg = null;
                        }
                    }
                } else if (obj2 instanceof Number) {
                    subFormatter = NumberFormat.getInstance(this.locale);
                } else if (obj2 instanceof Date) {
                    subFormatter = DateFormat.getDateTimeInstance(TYPE_TIME, TYPE_TIME, this.locale);
                } else if (obj2 instanceof String) {
                    arg = obj2;
                } else {
                    arg = obj2.toString();
                    if (arg == null) {
                        arg = "null";
                    }
                }
                if (characterIterators != null) {
                    if (last != result.length()) {
                        characterIterators.add(createAttributedCharacterIterator(result.substring(last)));
                        last = result.length();
                    }
                    if (subFormatter != null) {
                        AttributedCharacterIterator subIterator = subFormatter.formatToCharacterIterator(obj);
                        append(result, subIterator);
                        if (last != result.length()) {
                            characterIterators.add(createAttributedCharacterIterator(subIterator, Field.ARGUMENT, (Object) Integer.valueOf(argumentNumber)));
                            last = result.length();
                        }
                        arg = null;
                    }
                    if (arg != null && arg.length() > 0) {
                        result.append(arg);
                        characterIterators.add(createAttributedCharacterIterator(arg, Field.ARGUMENT, (Object) Integer.valueOf(argumentNumber)));
                        last = result.length();
                    }
                } else {
                    if (subFormatter != null) {
                        arg = subFormatter.format(obj);
                    }
                    last = result.length();
                    result.append(arg);
                    if (i == 0 && fp != null && Field.ARGUMENT.equals(fp.getFieldAttribute())) {
                        fp.setBeginIndex(last);
                        fp.setEndIndex(result.length());
                    }
                    last = result.length();
                }
            }
        }
        result.append(this.pattern.substring(lastOffset, this.pattern.length()));
        if (!(characterIterators == null || last == result.length())) {
            characterIterators.add(createAttributedCharacterIterator(result.substring(last)));
        }
        return result;
    }

    private void append(StringBuffer result, CharacterIterator iterator) {
        if (iterator.first() != CharacterIterator.DONE) {
            result.append(iterator.first());
            while (true) {
                char aChar = iterator.next();
                if (aChar != CharacterIterator.DONE) {
                    result.append(aChar);
                } else {
                    return;
                }
            }
        }
    }

    private void makeFormat(int position, int offsetNumber, StringBuilder[] textSegments) {
        String[] segments = new String[textSegments.length];
        int i = TYPE_NULL;
        while (true) {
            int length = textSegments.length;
            if (i >= r0) {
                break;
            }
            StringBuilder oneseg = textSegments[i];
            segments[i] = oneseg != null ? oneseg.toString() : "";
            i += TYPE_NUMBER;
        }
        try {
            int argumentNumber = Integer.parseInt(segments[TYPE_NUMBER]);
            if (argumentNumber < 0) {
                throw new IllegalArgumentException("negative argument number: " + argumentNumber);
            }
            if (offsetNumber >= this.formats.length) {
                int newLength = this.formats.length * TYPE_DATE;
                Object newFormats = new Format[newLength];
                int[] newOffsets = new int[newLength];
                int[] newArgumentNumbers = new int[newLength];
                System.arraycopy(this.formats, TYPE_NULL, newFormats, TYPE_NULL, this.maxOffset + TYPE_NUMBER);
                System.arraycopy(this.offsets, TYPE_NULL, newOffsets, TYPE_NULL, this.maxOffset + TYPE_NUMBER);
                System.arraycopy(this.argumentNumbers, TYPE_NULL, newArgumentNumbers, TYPE_NULL, this.maxOffset + TYPE_NUMBER);
                this.formats = newFormats;
                this.offsets = newOffsets;
                this.argumentNumbers = newArgumentNumbers;
            }
            int oldMaxOffset = this.maxOffset;
            this.maxOffset = offsetNumber;
            this.offsets[offsetNumber] = segments[TYPE_NULL].length();
            this.argumentNumbers[offsetNumber] = argumentNumber;
            DecimalFormat decimalFormat = null;
            if (segments[TYPE_DATE].length() != 0) {
                int type = findKeyword(segments[TYPE_DATE], TYPE_KEYWORDS);
                switch (type) {
                    case TYPE_NULL /*0*/:
                        break;
                    case TYPE_NUMBER /*1*/:
                        switch (findKeyword(segments[TYPE_TIME], NUMBER_MODIFIER_KEYWORDS)) {
                            case TYPE_NULL /*0*/:
                                decimalFormat = NumberFormat.getInstance(this.locale);
                                break;
                            case TYPE_NUMBER /*1*/:
                                decimalFormat = NumberFormat.getCurrencyInstance(this.locale);
                                break;
                            case TYPE_DATE /*2*/:
                                decimalFormat = NumberFormat.getPercentInstance(this.locale);
                                break;
                            case TYPE_TIME /*3*/:
                                decimalFormat = NumberFormat.getIntegerInstance(this.locale);
                                break;
                            default:
                                try {
                                    decimalFormat = new DecimalFormat(segments[TYPE_TIME], DecimalFormatSymbols.getInstance(this.locale));
                                    break;
                                } catch (IllegalArgumentException e) {
                                    this.maxOffset = oldMaxOffset;
                                    throw e;
                                }
                        }
                    case TYPE_DATE /*2*/:
                    case TYPE_TIME /*3*/:
                        int mod = findKeyword(segments[TYPE_TIME], DATE_TIME_MODIFIER_KEYWORDS);
                        if (mod >= 0) {
                            length = DATE_TIME_MODIFIER_KEYWORDS.length;
                            if (mod < r0) {
                                if (type != TYPE_DATE) {
                                    decimalFormat = DateFormat.getTimeInstance(DATE_TIME_MODIFIERS[mod], this.locale);
                                    break;
                                } else {
                                    decimalFormat = DateFormat.getDateInstance(DATE_TIME_MODIFIERS[mod], this.locale);
                                    break;
                                }
                            }
                        }
                        try {
                            decimalFormat = new SimpleDateFormat(segments[TYPE_TIME], this.locale);
                            break;
                        } catch (IllegalArgumentException e2) {
                            this.maxOffset = oldMaxOffset;
                            throw e2;
                        }
                    case TYPE_CHOICE /*4*/:
                        try {
                            decimalFormat = new ChoiceFormat(segments[TYPE_TIME]);
                            break;
                        } catch (Exception e3) {
                            this.maxOffset = oldMaxOffset;
                            throw new IllegalArgumentException("Choice Pattern incorrect: " + segments[TYPE_TIME], e3);
                        }
                    default:
                        this.maxOffset = oldMaxOffset;
                        throw new IllegalArgumentException("unknown format type: " + segments[TYPE_DATE]);
                }
            }
            this.formats[offsetNumber] = decimalFormat;
        } catch (NumberFormatException e4) {
            throw new IllegalArgumentException("can't parse argument number: " + segments[TYPE_NUMBER], e4);
        }
    }

    private static final int findKeyword(String s, String[] list) {
        int i;
        for (i = TYPE_NULL; i < list.length; i += TYPE_NUMBER) {
            if (s.equals(list[i])) {
                return i;
            }
        }
        String ls = s.trim().toLowerCase(Locale.ROOT);
        if (ls != s) {
            for (i = TYPE_NULL; i < list.length; i += TYPE_NUMBER) {
                if (ls.equals(list[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static final void copyAndFixQuotes(String source, int start, int end, StringBuilder target) {
        boolean quoted = false;
        for (int i = start; i < end; i += TYPE_NUMBER) {
            char ch = source.charAt(i);
            if (ch == '{') {
                if (!quoted) {
                    target.append('\'');
                    quoted = true;
                }
                target.append(ch);
            } else if (ch == '\'') {
                target.append("''");
            } else {
                if (quoted) {
                    target.append('\'');
                    quoted = false;
                }
                target.append(ch);
            }
        }
        if (quoted) {
            target.append('\'');
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        boolean isValid;
        boolean z = false;
        in.defaultReadObject();
        if (this.maxOffset < -1 || this.formats.length <= this.maxOffset || this.offsets.length <= this.maxOffset) {
            isValid = false;
        } else {
            if (this.argumentNumbers.length > this.maxOffset) {
                z = true;
            }
            isValid = z;
        }
        if (isValid) {
            int lastOffset = this.pattern.length() + TYPE_NUMBER;
            int i = this.maxOffset;
            while (i >= 0) {
                if (this.offsets[i] < 0 || this.offsets[i] > lastOffset) {
                    isValid = false;
                    break;
                } else {
                    lastOffset = this.offsets[i];
                    i--;
                }
            }
        }
        if (!isValid) {
            throw new InvalidObjectException("Could not reconstruct MessageFormat from corrupt stream.");
        }
    }
}
