package android.icu.text;

import android.icu.impl.BMPSet;
import android.icu.impl.Norm2AllModes;
import android.icu.impl.PatternProps;
import android.icu.impl.PatternTokenizer;
import android.icu.impl.RuleCharacterIterator;
import android.icu.impl.SortedSetRelation;
import android.icu.impl.StringRange;
import android.icu.impl.UBiDiProps;
import android.icu.impl.UCaseProps;
import android.icu.impl.UCharacterProperty;
import android.icu.impl.UPropertyAliases;
import android.icu.impl.UnicodeSetStringSpan;
import android.icu.impl.Utility;
import android.icu.lang.CharSequences;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.lang.UScript;
import android.icu.util.Freezable;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.OutputInt;
import android.icu.util.ULocale;
import android.icu.util.VersionInfo;
import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;

public class UnicodeSet extends UnicodeFilter implements Iterable<String>, Comparable<UnicodeSet>, Freezable<UnicodeSet> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int ADD_CASE_MAPPINGS = 4;
    public static final UnicodeSet ALL_CODE_POINTS = new UnicodeSet(0, 1114111).freeze();
    private static final String ANY_ID = "ANY";
    private static final String ASCII_ID = "ASCII";
    private static final String ASSIGNED = "Assigned";
    public static final int CASE = 2;
    public static final int CASE_INSENSITIVE = 2;
    public static final UnicodeSet EMPTY = new UnicodeSet().freeze();
    private static final int GROW_EXTRA = 16;
    private static final int HIGH = 1114112;
    public static final int IGNORE_SPACE = 1;
    private static UnicodeSet[] INCLUSIONS = null;
    private static final int LAST0_START = 0;
    private static final int LAST1_RANGE = 1;
    private static final int LAST2_SET = 2;
    private static final int LOW = 0;
    public static final int MAX_VALUE = 1114111;
    public static final int MIN_VALUE = 0;
    private static final int MODE0_NONE = 0;
    private static final int MODE1_INBRACKET = 1;
    private static final int MODE2_OUTBRACKET = 2;
    /* access modifiers changed from: private */
    public static final VersionInfo NO_VERSION = VersionInfo.getInstance(0, 0, 0, 0);
    private static final int SETMODE0_NONE = 0;
    private static final int SETMODE1_UNICODESET = 1;
    private static final int SETMODE2_PROPERTYPAT = 2;
    private static final int SETMODE3_PREPARSED = 3;
    private static final int START_EXTRA = 16;
    private static XSymbolTable XSYMBOL_TABLE = null;
    private volatile BMPSet bmpSet;
    private int[] buffer;
    /* access modifiers changed from: private */
    public int len;
    /* access modifiers changed from: private */
    public int[] list;
    private String pat;
    private int[] rangeList;
    private volatile UnicodeSetStringSpan stringSpan;
    TreeSet<String> strings;

    public enum ComparisonStyle {
        SHORTER_FIRST,
        LEXICOGRAPHIC,
        LONGER_FIRST
    }

    public static class EntryRange {
        public int codepoint;
        public int codepointEnd;

        EntryRange() {
        }

        public String toString() {
            StringBuilder sb;
            StringBuilder b = new StringBuilder();
            if (this.codepoint == this.codepointEnd) {
                sb = (StringBuilder) UnicodeSet._appendToPat(b, this.codepoint, false);
            } else {
                StringBuilder sb2 = (StringBuilder) UnicodeSet._appendToPat(b, this.codepoint, false);
                sb2.append('-');
                sb = (StringBuilder) UnicodeSet._appendToPat(sb2, this.codepointEnd, false);
            }
            return sb.toString();
        }
    }

    private class EntryRangeIterable implements Iterable<EntryRange> {
        private EntryRangeIterable() {
        }

        public Iterator<EntryRange> iterator() {
            return new EntryRangeIterator();
        }
    }

    private class EntryRangeIterator implements Iterator<EntryRange> {
        int pos;
        EntryRange result;

        private EntryRangeIterator() {
            this.result = new EntryRange();
        }

        public boolean hasNext() {
            return this.pos < UnicodeSet.this.len - 1;
        }

        public EntryRange next() {
            if (this.pos < UnicodeSet.this.len - 1) {
                EntryRange entryRange = this.result;
                int[] access$500 = UnicodeSet.this.list;
                int i = this.pos;
                this.pos = i + 1;
                entryRange.codepoint = access$500[i];
                EntryRange entryRange2 = this.result;
                int[] access$5002 = UnicodeSet.this.list;
                int i2 = this.pos;
                this.pos = i2 + 1;
                entryRange2.codepointEnd = access$5002[i2] - 1;
                return this.result;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private interface Filter {
        boolean contains(int i);
    }

    private static class GeneralCategoryMaskFilter implements Filter {
        int mask;

        GeneralCategoryMaskFilter(int mask2) {
            this.mask = mask2;
        }

        public boolean contains(int ch) {
            return ((1 << UCharacter.getType(ch)) & this.mask) != 0;
        }
    }

    private static class IntPropertyFilter implements Filter {
        int prop;
        int value;

        IntPropertyFilter(int prop2, int value2) {
            this.prop = prop2;
            this.value = value2;
        }

        public boolean contains(int ch) {
            return UCharacter.getIntPropertyValue(ch, this.prop) == this.value;
        }
    }

    private static class NumericValueFilter implements Filter {
        double value;

        NumericValueFilter(double value2) {
            this.value = value2;
        }

        public boolean contains(int ch) {
            return UCharacter.getUnicodeNumericValue(ch) == this.value;
        }
    }

    private static class ScriptExtensionsFilter implements Filter {
        int script;

        ScriptExtensionsFilter(int script2) {
            this.script = script2;
        }

        public boolean contains(int c) {
            return UScript.hasScript(c, this.script);
        }
    }

    public enum SpanCondition {
        NOT_CONTAINED,
        CONTAINED,
        SIMPLE,
        CONDITION_COUNT
    }

    private static class UnicodeSetIterator2 implements Iterator<String> {
        private char[] buffer;
        private int current;
        private int item;
        private int len;
        private int limit;
        private int[] sourceList;
        private TreeSet<String> sourceStrings;
        private Iterator<String> stringIterator;

        UnicodeSetIterator2(UnicodeSet source) {
            this.len = source.len - 1;
            if (this.len > 0) {
                this.sourceStrings = source.strings;
                this.sourceList = source.list;
                int[] iArr = this.sourceList;
                int i = this.item;
                this.item = i + 1;
                this.current = iArr[i];
                int[] iArr2 = this.sourceList;
                int i2 = this.item;
                this.item = i2 + 1;
                this.limit = iArr2[i2];
                return;
            }
            this.stringIterator = source.strings.iterator();
            this.sourceList = null;
        }

        public boolean hasNext() {
            return this.sourceList != null || this.stringIterator.hasNext();
        }

        public String next() {
            if (this.sourceList == null) {
                return this.stringIterator.next();
            }
            int codepoint = this.current;
            this.current = codepoint + 1;
            if (this.current >= this.limit) {
                if (this.item >= this.len) {
                    this.stringIterator = this.sourceStrings.iterator();
                    this.sourceList = null;
                } else {
                    int[] iArr = this.sourceList;
                    int i = this.item;
                    this.item = i + 1;
                    this.current = iArr[i];
                    int[] iArr2 = this.sourceList;
                    int i2 = this.item;
                    this.item = i2 + 1;
                    this.limit = iArr2[i2];
                }
            }
            if (codepoint <= 65535) {
                return String.valueOf((char) codepoint);
            }
            if (this.buffer == null) {
                this.buffer = new char[2];
            }
            int offset = codepoint - 65536;
            this.buffer[0] = (char) ((offset >>> 10) + 55296);
            this.buffer[1] = (char) ((offset & Opcodes.OP_NEW_INSTANCE_JUMBO) + UTF16.TRAIL_SURROGATE_MIN_VALUE);
            return String.valueOf(this.buffer);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class VersionFilter implements Filter {
        VersionInfo version;

        VersionFilter(VersionInfo version2) {
            this.version = version2;
        }

        public boolean contains(int ch) {
            VersionInfo v = UCharacter.getAge(ch);
            return !Utility.sameObjects(v, UnicodeSet.NO_VERSION) && v.compareTo(this.version) <= 0;
        }
    }

    public static abstract class XSymbolTable implements SymbolTable {
        public UnicodeMatcher lookupMatcher(int i) {
            return null;
        }

        public boolean applyPropertyAlias(String propertyName, String propertyValue, UnicodeSet result) {
            return false;
        }

        public char[] lookup(String s) {
            return null;
        }

        public String parseReference(String text, ParsePosition pos, int limit) {
            return null;
        }
    }

    public UnicodeSet() {
        this.strings = new TreeSet<>();
        this.pat = null;
        this.list = new int[17];
        int[] iArr = this.list;
        int i = this.len;
        this.len = i + 1;
        iArr[i] = 1114112;
    }

    public UnicodeSet(UnicodeSet other) {
        this.strings = new TreeSet<>();
        this.pat = null;
        set(other);
    }

    public UnicodeSet(int start, int end) {
        this();
        complement(start, end);
    }

    public UnicodeSet(int... pairs) {
        this.strings = new TreeSet<>();
        this.pat = null;
        if ((pairs.length & 1) == 0) {
            this.list = new int[(pairs.length + 1)];
            this.len = this.list.length;
            int last = -1;
            int i = 0;
            while (i < pairs.length) {
                int start = pairs[i];
                if (last < start) {
                    int i2 = i + 1;
                    int last2 = start;
                    this.list[i] = start;
                    int end = pairs[i2] + 1;
                    if (last2 < end) {
                        last = end;
                        this.list[i2] = end;
                        i = i2 + 1;
                    } else {
                        throw new IllegalArgumentException("Must be monotonically increasing.");
                    }
                } else {
                    throw new IllegalArgumentException("Must be monotonically increasing.");
                }
            }
            this.list[i] = 1114112;
            return;
        }
        throw new IllegalArgumentException("Must have even number of integers");
    }

    public UnicodeSet(String pattern) {
        this();
        applyPattern(pattern, (ParsePosition) null, (SymbolTable) null, 1);
    }

    public UnicodeSet(String pattern, boolean ignoreWhitespace) {
        this();
        applyPattern(pattern, (ParsePosition) null, (SymbolTable) null, (int) ignoreWhitespace);
    }

    public UnicodeSet(String pattern, int options) {
        this();
        applyPattern(pattern, (ParsePosition) null, (SymbolTable) null, options);
    }

    public UnicodeSet(String pattern, ParsePosition pos, SymbolTable symbols) {
        this();
        applyPattern(pattern, pos, symbols, 1);
    }

    public UnicodeSet(String pattern, ParsePosition pos, SymbolTable symbols, int options) {
        this();
        applyPattern(pattern, pos, symbols, options);
    }

    public Object clone() {
        if (isFrozen()) {
            return this;
        }
        UnicodeSet result = new UnicodeSet(this);
        result.bmpSet = this.bmpSet;
        result.stringSpan = this.stringSpan;
        return result;
    }

    public UnicodeSet set(int start, int end) {
        checkFrozen();
        clear();
        complement(start, end);
        return this;
    }

    public UnicodeSet set(UnicodeSet other) {
        checkFrozen();
        this.list = (int[]) other.list.clone();
        this.len = other.len;
        this.pat = other.pat;
        this.strings = new TreeSet<>(other.strings);
        return this;
    }

    public final UnicodeSet applyPattern(String pattern) {
        checkFrozen();
        return applyPattern(pattern, (ParsePosition) null, (SymbolTable) null, 1);
    }

    public UnicodeSet applyPattern(String pattern, boolean ignoreWhitespace) {
        checkFrozen();
        return applyPattern(pattern, (ParsePosition) null, (SymbolTable) null, (int) ignoreWhitespace);
    }

    public UnicodeSet applyPattern(String pattern, int options) {
        checkFrozen();
        return applyPattern(pattern, (ParsePosition) null, (SymbolTable) null, options);
    }

    public static boolean resemblesPattern(String pattern, int pos) {
        if ((pos + 1 >= pattern.length() || pattern.charAt(pos) != '[') && !resemblesPropertyPattern(pattern, pos)) {
            return false;
        }
        return true;
    }

    private static void appendCodePoint(Appendable app, int c) {
        if (c <= 65535) {
            try {
                app.append((char) c);
            } catch (IOException e) {
                throw new ICUUncheckedIOException((Throwable) e);
            }
        } else {
            app.append(UTF16.getLeadSurrogate(c)).append(UTF16.getTrailSurrogate(c));
        }
    }

    private static void append(Appendable app, CharSequence s) {
        try {
            app.append(s);
        } catch (IOException e) {
            throw new ICUUncheckedIOException((Throwable) e);
        }
    }

    private static <T extends Appendable> T _appendToPat(T buf, String s, boolean escapeUnprintable) {
        int i = 0;
        while (i < s.length()) {
            int cp = s.codePointAt(i);
            _appendToPat(buf, cp, escapeUnprintable);
            i += Character.charCount(cp);
        }
        return buf;
    }

    /* access modifiers changed from: private */
    public static <T extends Appendable> T _appendToPat(T buf, int c, boolean escapeUnprintable) {
        if (escapeUnprintable) {
            try {
                if (Utility.isUnprintable(c) && Utility.escapeUnprintable(buf, c)) {
                    return buf;
                }
            } catch (IOException e) {
                throw new ICUUncheckedIOException((Throwable) e);
            }
        }
        if (!(c == 36 || c == 38 || c == 45 || c == 58 || c == 123 || c == 125)) {
            switch (c) {
                case 91:
                case 92:
                case 93:
                case 94:
                    break;
                default:
                    if (PatternProps.isWhiteSpace(c)) {
                        buf.append(PatternTokenizer.BACK_SLASH);
                        break;
                    }
                    break;
            }
        }
        buf.append(PatternTokenizer.BACK_SLASH);
        appendCodePoint(buf, c);
        return buf;
    }

    public String toPattern(boolean escapeUnprintable) {
        if (this.pat == null || escapeUnprintable) {
            return ((StringBuilder) _toPattern(new StringBuilder(), escapeUnprintable)).toString();
        }
        return this.pat;
    }

    private <T extends Appendable> T _toPattern(T result, boolean escapeUnprintable) {
        if (this.pat == null) {
            return appendNewPattern(result, escapeUnprintable, true);
        }
        if (!escapeUnprintable) {
            try {
                result.append(this.pat);
                return result;
            } catch (IOException e) {
                throw new ICUUncheckedIOException((Throwable) e);
            }
        } else {
            boolean oddNumberOfBackslashes = false;
            int i = 0;
            while (i < this.pat.length()) {
                int c = this.pat.codePointAt(i);
                i += Character.charCount(c);
                if (Utility.isUnprintable(c)) {
                    Utility.escapeUnprintable(result, c);
                    oddNumberOfBackslashes = false;
                } else if (oddNumberOfBackslashes || c != 92) {
                    if (oddNumberOfBackslashes) {
                        result.append(PatternTokenizer.BACK_SLASH);
                    }
                    appendCodePoint(result, c);
                    oddNumberOfBackslashes = false;
                } else {
                    oddNumberOfBackslashes = true;
                }
            }
            if (oddNumberOfBackslashes) {
                result.append(PatternTokenizer.BACK_SLASH);
            }
            return result;
        }
    }

    public StringBuffer _generatePattern(StringBuffer result, boolean escapeUnprintable) {
        return _generatePattern(result, escapeUnprintable, true);
    }

    public StringBuffer _generatePattern(StringBuffer result, boolean escapeUnprintable, boolean includeStrings) {
        return (StringBuffer) appendNewPattern(result, escapeUnprintable, includeStrings);
    }

    private <T extends Appendable> T appendNewPattern(T result, boolean escapeUnprintable, boolean includeStrings) {
        try {
            result.append('[');
            int count = getRangeCount();
            if (count > 1 && getRangeStart(0) == 0 && getRangeEnd(count - 1) == 1114111) {
                result.append('^');
                for (int i = 1; i < count; i++) {
                    int start = getRangeEnd(i - 1) + 1;
                    int end = getRangeStart(i) - 1;
                    _appendToPat(result, start, escapeUnprintable);
                    if (start != end) {
                        if (start + 1 != end) {
                            result.append('-');
                        }
                        _appendToPat(result, end, escapeUnprintable);
                    }
                }
            } else {
                for (int i2 = 0; i2 < count; i2++) {
                    int start2 = getRangeStart(i2);
                    int end2 = getRangeEnd(i2);
                    _appendToPat(result, start2, escapeUnprintable);
                    if (start2 != end2) {
                        if (start2 + 1 != end2) {
                            result.append('-');
                        }
                        _appendToPat(result, end2, escapeUnprintable);
                    }
                }
            }
            if (includeStrings && this.strings.size() > 0) {
                Iterator<String> it = this.strings.iterator();
                while (it.hasNext()) {
                    result.append('{');
                    _appendToPat(result, it.next(), escapeUnprintable);
                    result.append('}');
                }
            }
            result.append(']');
            return result;
        } catch (IOException e) {
            throw new ICUUncheckedIOException((Throwable) e);
        }
    }

    public int size() {
        int n = 0;
        int count = getRangeCount();
        for (int i = 0; i < count; i++) {
            n += (getRangeEnd(i) - getRangeStart(i)) + 1;
        }
        return this.strings.size() + n;
    }

    public boolean isEmpty() {
        return this.len == 1 && this.strings.size() == 0;
    }

    public boolean matchesIndexValue(int v) {
        for (int i = 0; i < getRangeCount(); i++) {
            int low = getRangeStart(i);
            int high = getRangeEnd(i);
            if ((low & -256) == (high & -256)) {
                if ((low & 255) <= v && v <= (high & 255)) {
                    return true;
                }
            } else if ((low & 255) <= v || v <= (high & 255)) {
                return true;
            }
        }
        if (this.strings.size() != 0) {
            Iterator<String> it = this.strings.iterator();
            while (it.hasNext()) {
                if ((UTF16.charAt(it.next(), 0) & 255) == v) {
                    return true;
                }
            }
        }
        return false;
    }

    public int matches(Replaceable text, int[] offset, int limit, boolean incremental) {
        int i = 2;
        if (offset[0] != limit) {
            if (this.strings.size() != 0) {
                boolean forward = offset[0] < limit;
                char firstChar = text.charAt(offset[0]);
                int highWaterLength = 0;
                Iterator<String> it = this.strings.iterator();
                while (it.hasNext()) {
                    String trial = it.next();
                    char c = trial.charAt(forward ? 0 : trial.length() - 1);
                    if (!forward || c <= firstChar) {
                        if (c == firstChar) {
                            int length = matchRest(text, offset[0], limit, trial);
                            if (incremental) {
                                if (length == (forward ? limit - offset[0] : offset[0] - limit)) {
                                    return 1;
                                }
                            }
                            if (length == trial.length()) {
                                if (length > highWaterLength) {
                                    highWaterLength = length;
                                }
                                if (forward && length < highWaterLength) {
                                    break;
                                }
                            } else {
                                continue;
                            }
                        }
                    } else {
                        break;
                    }
                }
                if (highWaterLength != 0) {
                    offset[0] = offset[0] + (forward ? highWaterLength : -highWaterLength);
                    return 2;
                }
            }
            return super.matches(text, offset, limit, incremental);
        } else if (!contains((int) DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH)) {
            return 0;
        } else {
            if (incremental) {
                i = 1;
            }
            return i;
        }
    }

    private static int matchRest(Replaceable text, int start, int limit, String s) {
        int maxLen;
        int slen = s.length();
        int i = 1;
        if (start < limit) {
            maxLen = limit - start;
            if (maxLen > slen) {
                maxLen = slen;
            }
            while (i < maxLen) {
                if (text.charAt(start + i) != s.charAt(i)) {
                    return 0;
                }
                i++;
            }
        } else {
            int maxLen2 = start - limit;
            if (maxLen2 > slen) {
                maxLen2 = slen;
            }
            int slen2 = slen - 1;
            while (i < maxLen) {
                if (text.charAt(start - i) != s.charAt(slen2 - i)) {
                    return 0;
                }
                i++;
            }
        }
        return maxLen;
    }

    @Deprecated
    public int matchesAt(CharSequence text, int offset) {
        int lastLen = -1;
        if (this.strings.size() != 0) {
            char firstChar = text.charAt(offset);
            String trial = null;
            Iterator<String> it = this.strings.iterator();
            while (true) {
                if (it.hasNext()) {
                    trial = it.next();
                    char firstStringChar = trial.charAt(0);
                    if (firstStringChar >= firstChar && firstStringChar > firstChar) {
                        break;
                    }
                } else {
                    while (true) {
                        int tempLen = matchesAt(text, offset, trial);
                        if (lastLen > tempLen) {
                            break;
                        }
                        lastLen = tempLen;
                        if (!it.hasNext()) {
                            break;
                        }
                        trial = it.next();
                    }
                }
            }
        }
        if (lastLen < 2) {
            int cp = UTF16.charAt(text, offset);
            if (contains(cp)) {
                lastLen = UTF16.getCharCount(cp);
            }
        }
        return offset + lastLen;
    }

    private static int matchesAt(CharSequence text, int offsetInText, CharSequence substring) {
        int len2 = substring.length();
        if (text.length() + offsetInText > len2) {
            return -1;
        }
        int i = 0;
        int j = offsetInText;
        while (i < len2) {
            if (substring.charAt(i) != text.charAt(j)) {
                return -1;
            }
            i++;
            j++;
        }
        return i;
    }

    public void addMatchSetTo(UnicodeSet toUnionTo) {
        toUnionTo.addAll(this);
    }

    public int indexOf(int c) {
        if (c < 0 || c > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) c, 6));
        }
        int i = 0;
        int n = 0;
        while (true) {
            int i2 = i + 1;
            int start = this.list[i];
            if (c < start) {
                return -1;
            }
            int i3 = i2 + 1;
            int limit = this.list[i2];
            if (c < limit) {
                return (n + c) - start;
            }
            n += limit - start;
            i = i3;
        }
    }

    public int charAt(int index) {
        if (index >= 0) {
            int len2 = this.len & -2;
            int i = 0;
            while (i < len2) {
                int i2 = i + 1;
                int start = this.list[i];
                int i3 = i2 + 1;
                int count = this.list[i2] - start;
                if (index < count) {
                    return start + index;
                }
                index -= count;
                i = i3;
            }
        }
        return -1;
    }

    public UnicodeSet add(int start, int end) {
        checkFrozen();
        return add_unchecked(start, end);
    }

    public UnicodeSet addAll(int start, int end) {
        checkFrozen();
        return add_unchecked(start, end);
    }

    private UnicodeSet add_unchecked(int start, int end) {
        if (start < 0 || start > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) start, 6));
        } else if (end < 0 || end > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) end, 6));
        } else {
            if (start < end) {
                add(range(start, end), 2, 0);
            } else if (start == end) {
                add(start);
            }
            return this;
        }
    }

    public final UnicodeSet add(int c) {
        checkFrozen();
        return add_unchecked(c);
    }

    private final UnicodeSet add_unchecked(int c) {
        if (c < 0 || c > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) c, 6));
        }
        int i = findCodePoint(c);
        if ((i & 1) != 0) {
            return this;
        }
        if (c == this.list[i] - 1) {
            this.list[i] = c;
            if (c == 1114111) {
                ensureCapacity(this.len + 1);
                int[] iArr = this.list;
                int i2 = this.len;
                this.len = i2 + 1;
                iArr[i2] = 1114112;
            }
            if (i > 0 && c == this.list[i - 1]) {
                System.arraycopy(this.list, i + 1, this.list, i - 1, (this.len - i) - 1);
                this.len -= 2;
            }
        } else if (i <= 0 || c != this.list[i - 1]) {
            if (this.len + 2 > this.list.length) {
                int[] temp = new int[(this.len + 2 + 16)];
                if (i != 0) {
                    System.arraycopy(this.list, 0, temp, 0, i);
                }
                System.arraycopy(this.list, i, temp, i + 2, this.len - i);
                this.list = temp;
            } else {
                System.arraycopy(this.list, i, this.list, i + 2, this.len - i);
            }
            this.list[i] = c;
            this.list[i + 1] = c + 1;
            this.len += 2;
        } else {
            int[] iArr2 = this.list;
            int i3 = i - 1;
            iArr2[i3] = iArr2[i3] + 1;
        }
        this.pat = null;
        return this;
    }

    public final UnicodeSet add(CharSequence s) {
        checkFrozen();
        int cp = getSingleCP(s);
        if (cp < 0) {
            this.strings.add(s.toString());
            this.pat = null;
        } else {
            add_unchecked(cp, cp);
        }
        return this;
    }

    private static int getSingleCP(CharSequence s) {
        if (s.length() < 1) {
            throw new IllegalArgumentException("Can't use zero-length strings in UnicodeSet");
        } else if (s.length() > 2) {
            return -1;
        } else {
            if (s.length() == 1) {
                return s.charAt(0);
            }
            int cp = UTF16.charAt(s, 0);
            if (cp > 65535) {
                return cp;
            }
            return -1;
        }
    }

    public final UnicodeSet addAll(CharSequence s) {
        checkFrozen();
        int i = 0;
        while (i < s.length()) {
            int cp = UTF16.charAt(s, i);
            add_unchecked(cp, cp);
            i += UTF16.getCharCount(cp);
        }
        return this;
    }

    public final UnicodeSet retainAll(CharSequence s) {
        return retainAll(fromAll(s));
    }

    public final UnicodeSet complementAll(CharSequence s) {
        return complementAll(fromAll(s));
    }

    public final UnicodeSet removeAll(CharSequence s) {
        return removeAll(fromAll(s));
    }

    public final UnicodeSet removeAllStrings() {
        checkFrozen();
        if (this.strings.size() != 0) {
            this.strings.clear();
            this.pat = null;
        }
        return this;
    }

    public static UnicodeSet from(CharSequence s) {
        return new UnicodeSet().add(s);
    }

    public static UnicodeSet fromAll(CharSequence s) {
        return new UnicodeSet().addAll(s);
    }

    public UnicodeSet retain(int start, int end) {
        checkFrozen();
        if (start < 0 || start > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) start, 6));
        } else if (end < 0 || end > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) end, 6));
        } else {
            if (start <= end) {
                retain(range(start, end), 2, 0);
            } else {
                clear();
            }
            return this;
        }
    }

    public final UnicodeSet retain(int c) {
        return retain(c, c);
    }

    public final UnicodeSet retain(CharSequence cs) {
        int cp = getSingleCP(cs);
        if (cp < 0) {
            String s = cs.toString();
            if (this.strings.contains(s) && size() == 1) {
                return this;
            }
            clear();
            this.strings.add(s);
            this.pat = null;
        } else {
            retain(cp, cp);
        }
        return this;
    }

    public UnicodeSet remove(int start, int end) {
        checkFrozen();
        if (start < 0 || start > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) start, 6));
        } else if (end < 0 || end > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) end, 6));
        } else {
            if (start <= end) {
                retain(range(start, end), 2, 2);
            }
            return this;
        }
    }

    public final UnicodeSet remove(int c) {
        return remove(c, c);
    }

    public final UnicodeSet remove(CharSequence s) {
        int cp = getSingleCP(s);
        if (cp < 0) {
            this.strings.remove(s.toString());
            this.pat = null;
        } else {
            remove(cp, cp);
        }
        return this;
    }

    public UnicodeSet complement(int start, int end) {
        checkFrozen();
        if (start < 0 || start > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) start, 6));
        } else if (end < 0 || end > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) end, 6));
        } else {
            if (start <= end) {
                xor(range(start, end), 2, 0);
            }
            this.pat = null;
            return this;
        }
    }

    public final UnicodeSet complement(int c) {
        return complement(c, c);
    }

    public UnicodeSet complement() {
        checkFrozen();
        if (this.list[0] == 0) {
            System.arraycopy(this.list, 1, this.list, 0, this.len - 1);
            this.len--;
        } else {
            ensureCapacity(this.len + 1);
            System.arraycopy(this.list, 0, this.list, 1, this.len);
            this.list[0] = 0;
            this.len++;
        }
        this.pat = null;
        return this;
    }

    public final UnicodeSet complement(CharSequence s) {
        checkFrozen();
        int cp = getSingleCP(s);
        if (cp < 0) {
            String s2 = s.toString();
            if (this.strings.contains(s2)) {
                this.strings.remove(s2);
            } else {
                this.strings.add(s2);
            }
            this.pat = null;
        } else {
            complement(cp, cp);
        }
        return this;
    }

    public boolean contains(int c) {
        if (c < 0 || c > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) c, 6));
        } else if (this.bmpSet != null) {
            return this.bmpSet.contains(c);
        } else {
            if (this.stringSpan != null) {
                return this.stringSpan.contains(c);
            }
            return (findCodePoint(c) & 1) != 0;
        }
    }

    private final int findCodePoint(int c) {
        if (c < this.list[0]) {
            return 0;
        }
        if (this.len >= 2 && c >= this.list[this.len - 2]) {
            return this.len - 1;
        }
        int lo = 0;
        int hi = this.len - 1;
        while (true) {
            int i = (lo + hi) >>> 1;
            if (i == lo) {
                return hi;
            }
            if (c < this.list[i]) {
                hi = i;
            } else {
                lo = i;
            }
        }
    }

    public boolean contains(int start, int end) {
        if (start < 0 || start > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) start, 6));
        } else if (end < 0 || end > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) end, 6));
        } else {
            int i = findCodePoint(start);
            return (i & 1) != 0 && end < this.list[i];
        }
    }

    public final boolean contains(CharSequence s) {
        int cp = getSingleCP(s);
        if (cp < 0) {
            return this.strings.contains(s.toString());
        }
        return contains(cp);
    }

    public boolean containsAll(UnicodeSet b) {
        UnicodeSet unicodeSet = b;
        int[] listB = unicodeSet.list;
        int startA = 0;
        int aLen = this.len - 1;
        int bLen = unicodeSet.len - 1;
        int limitA = 0;
        int limitA2 = 0;
        int bPtr = 0;
        int startB = 0;
        boolean needB = true;
        boolean needA = true;
        int limitB = 0;
        while (true) {
            if (needA) {
                if (startA < aLen) {
                    int aPtr = startA + 1;
                    int startA2 = this.list[startA];
                    limitA2 = this.list[aPtr];
                    limitA = startA2;
                    startA = aPtr + 1;
                } else if (!needB || startB < bLen) {
                    return false;
                }
            }
            if (needB) {
                if (startB >= bLen) {
                    break;
                }
                int bPtr2 = startB + 1;
                int startB2 = listB[startB];
                limitB = listB[bPtr2];
                bPtr = startB2;
                startB = bPtr2 + 1;
            }
            if (bPtr >= limitA2) {
                needA = true;
                needB = false;
            } else if (bPtr < limitA || limitB > limitA2) {
                return false;
            } else {
                needA = false;
                needB = true;
            }
        }
        if (!this.strings.containsAll(unicodeSet.strings)) {
            return false;
        }
        return true;
    }

    public boolean containsAll(String s) {
        int i = 0;
        while (i < s.length()) {
            int cp = UTF16.charAt(s, i);
            if (contains(cp)) {
                i += UTF16.getCharCount(cp);
            } else if (this.strings.size() == 0) {
                return false;
            } else {
                return containsAll(s, 0);
            }
        }
        return true;
    }

    private boolean containsAll(String s, int i) {
        if (i >= s.length()) {
            return true;
        }
        int cp = UTF16.charAt(s, i);
        if (contains(cp) && containsAll(s, UTF16.getCharCount(cp) + i)) {
            return true;
        }
        Iterator<String> it = this.strings.iterator();
        while (it.hasNext()) {
            String setStr = it.next();
            if (s.startsWith(setStr, i) && containsAll(s, setStr.length() + i)) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public String getRegexEquivalent() {
        if (this.strings.size() == 0) {
            return toString();
        }
        StringBuilder result = new StringBuilder("(?:");
        appendNewPattern(result, true, false);
        Iterator<String> it = this.strings.iterator();
        while (it.hasNext()) {
            result.append('|');
            _appendToPat(result, it.next(), true);
        }
        result.append(")");
        return result.toString();
    }

    public boolean containsNone(int start, int end) {
        if (start < 0 || start > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) start, 6));
        } else if (end < 0 || end > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) end, 6));
        } else {
            int i = -1;
            do {
                i++;
            } while (start >= this.list[i]);
            if ((i & 1) != 0 || end >= this.list[i]) {
                return false;
            }
            return true;
        }
    }

    public boolean containsNone(UnicodeSet b) {
        UnicodeSet unicodeSet = b;
        int[] listB = unicodeSet.list;
        int startA = 0;
        int aLen = this.len - 1;
        int bLen = unicodeSet.len - 1;
        int limitA = 0;
        int limitA2 = 0;
        int bPtr = 0;
        int startB = 0;
        boolean needB = true;
        boolean needA = true;
        int limitB = 0;
        while (true) {
            if (needA) {
                if (startA >= aLen) {
                    break;
                }
                int aPtr = startA + 1;
                int startA2 = this.list[startA];
                limitA2 = this.list[aPtr];
                limitA = startA2;
                startA = aPtr + 1;
            }
            if (needB) {
                if (startB >= bLen) {
                    break;
                }
                int bPtr2 = startB + 1;
                int startB2 = listB[startB];
                limitB = listB[bPtr2];
                bPtr = startB2;
                startB = bPtr2 + 1;
            }
            if (bPtr >= limitA2) {
                needA = true;
                needB = false;
            } else if (limitA < limitB) {
                return false;
            } else {
                needA = false;
                needB = true;
            }
        }
        if (!SortedSetRelation.hasRelation(this.strings, 5, unicodeSet.strings)) {
            return false;
        }
        return true;
    }

    public boolean containsNone(CharSequence s) {
        return span(s, SpanCondition.NOT_CONTAINED) == s.length();
    }

    public final boolean containsSome(int start, int end) {
        return !containsNone(start, end);
    }

    public final boolean containsSome(UnicodeSet s) {
        return !containsNone(s);
    }

    public final boolean containsSome(CharSequence s) {
        return !containsNone(s);
    }

    public UnicodeSet addAll(UnicodeSet c) {
        checkFrozen();
        add(c.list, c.len, 0);
        this.strings.addAll(c.strings);
        return this;
    }

    public UnicodeSet retainAll(UnicodeSet c) {
        checkFrozen();
        retain(c.list, c.len, 0);
        this.strings.retainAll(c.strings);
        return this;
    }

    public UnicodeSet removeAll(UnicodeSet c) {
        checkFrozen();
        retain(c.list, c.len, 2);
        this.strings.removeAll(c.strings);
        return this;
    }

    public UnicodeSet complementAll(UnicodeSet c) {
        checkFrozen();
        xor(c.list, c.len, 0);
        SortedSetRelation.doOperation(this.strings, 5, c.strings);
        return this;
    }

    public UnicodeSet clear() {
        checkFrozen();
        this.list[0] = 1114112;
        this.len = 1;
        this.pat = null;
        this.strings.clear();
        return this;
    }

    public int getRangeCount() {
        return this.len / 2;
    }

    public int getRangeStart(int index) {
        return this.list[index * 2];
    }

    public int getRangeEnd(int index) {
        return this.list[(index * 2) + 1] - 1;
    }

    public UnicodeSet compact() {
        checkFrozen();
        if (this.len != this.list.length) {
            int[] temp = new int[this.len];
            System.arraycopy(this.list, 0, temp, 0, this.len);
            this.list = temp;
        }
        this.rangeList = null;
        this.buffer = null;
        return this;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        try {
            UnicodeSet that = (UnicodeSet) o;
            if (this.len != that.len) {
                return false;
            }
            for (int i = 0; i < this.len; i++) {
                if (this.list[i] != that.list[i]) {
                    return false;
                }
            }
            if (!this.strings.equals(that.strings)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int hashCode() {
        int result = this.len;
        for (int i = 0; i < this.len; i++) {
            result = (result * 1000003) + this.list[i];
        }
        return result;
    }

    public String toString() {
        return toPattern(true);
    }

    @Deprecated
    public UnicodeSet applyPattern(String pattern, ParsePosition pos, SymbolTable symbols, int options) {
        boolean parsePositionWasNull = pos == null;
        if (parsePositionWasNull) {
            pos = new ParsePosition(0);
        }
        StringBuilder rebuiltPat = new StringBuilder();
        RuleCharacterIterator chars = new RuleCharacterIterator(pattern, symbols, pos);
        applyPattern(chars, symbols, (Appendable) rebuiltPat, options);
        if (chars.inVariable()) {
            syntaxError(chars, "Extra chars in variable value");
        }
        this.pat = rebuiltPat.toString();
        if (parsePositionWasNull) {
            int i = pos.getIndex();
            if ((options & 1) != 0) {
                i = PatternProps.skipWhiteSpace(pattern, i);
            }
            if (i != pattern.length()) {
                throw new IllegalArgumentException("Parse of \"" + pattern + "\" failed at " + i);
            }
        }
        return this;
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x00c9  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0140  */
    private void applyPattern(RuleCharacterIterator chars, SymbolTable symbols, Appendable rebuiltPat, int options) {
        boolean invert;
        boolean invert2;
        boolean z;
        int c;
        UnicodeSet scratch;
        StringBuilder buf;
        String lastString;
        UnicodeSet scratch2;
        char c2;
        RuleCharacterIterator ruleCharacterIterator = chars;
        SymbolTable symbolTable = symbols;
        Appendable appendable = rebuiltPat;
        int i = options;
        int opts = 3;
        if ((i & 1) != 0) {
            opts = 3 | 4;
        }
        int opts2 = opts;
        StringBuilder patBuf = new StringBuilder();
        UnicodeSet scratch3 = null;
        Object backup = null;
        char c3 = 0;
        int lastChar = 0;
        int mode = 0;
        char op = 0;
        boolean invert3 = false;
        clear();
        String lastString2 = null;
        boolean usePat = false;
        StringBuilder buf2 = null;
        while (true) {
            String lastString3 = lastString2;
            invert = invert3;
            if (mode == 2 || chars.atEnd()) {
                UnicodeSet scratch4 = scratch3;
                z = true;
                invert2 = invert;
                UnicodeSet unicodeSet = scratch4;
            } else {
                boolean literal = false;
                UnicodeSet nested = null;
                int setMode = 0;
                if (resemblesPropertyPattern(ruleCharacterIterator, opts2)) {
                    setMode = 2;
                    c = 0;
                } else {
                    backup = ruleCharacterIterator.getPos(backup);
                    c = ruleCharacterIterator.next(opts2);
                    literal = chars.isEscaped();
                    if (c != 91 || literal) {
                        if (symbolTable != null) {
                            UnicodeMatcher m = symbolTable.lookupMatcher(c);
                            if (m != null) {
                                try {
                                    nested = (UnicodeSet) m;
                                    setMode = 3;
                                } catch (ClassCastException e) {
                                    ClassCastException classCastException = e;
                                    ClassCastException classCastException2 = e;
                                    syntaxError(ruleCharacterIterator, "Syntax error");
                                }
                            }
                        }
                    } else if (mode == 1) {
                        ruleCharacterIterator.setPos(backup);
                        setMode = 1;
                    } else {
                        mode = 1;
                        patBuf.append('[');
                        Object backup2 = ruleCharacterIterator.getPos(backup);
                        int c4 = ruleCharacterIterator.next(opts2);
                        boolean literal2 = chars.isEscaped();
                        if (c4 != 94 || literal2) {
                            invert3 = invert;
                        } else {
                            patBuf.append('^');
                            backup2 = ruleCharacterIterator.getPos(backup2);
                            c4 = ruleCharacterIterator.next(opts2);
                            literal2 = chars.isEscaped();
                            invert3 = true;
                        }
                        int i2 = c4;
                        Object backup3 = backup2;
                        int c5 = i2;
                        boolean z2 = literal2;
                        if (c5 == 45) {
                            literal = true;
                            backup = backup3;
                            c = c5;
                            if (setMode == 0) {
                                invert2 = invert3;
                                if (c3 == 1) {
                                    if (op != 0) {
                                        syntaxError(ruleCharacterIterator, "Char expected after operator");
                                    }
                                    add_unchecked(lastChar, lastChar);
                                    _appendToPat(patBuf, lastChar, false);
                                    c3 = 0;
                                    op = 0;
                                }
                                if (op == '-' || op == '&') {
                                    patBuf.append(op);
                                }
                                if (nested == null) {
                                    if (scratch3 == null) {
                                        scratch3 = new UnicodeSet();
                                    }
                                    UnicodeSet nested2 = scratch3;
                                    scratch2 = scratch3;
                                } else {
                                    scratch2 = scratch3;
                                    scratch3 = nested;
                                }
                                switch (setMode) {
                                    case 1:
                                        c2 = c3;
                                        scratch3.applyPattern(ruleCharacterIterator, symbolTable, (Appendable) patBuf, i);
                                        break;
                                    case 2:
                                        c2 = c3;
                                        ruleCharacterIterator.skipIgnored(opts2);
                                        scratch3.applyPropertyPattern(ruleCharacterIterator, (Appendable) patBuf, symbolTable);
                                        break;
                                    case 3:
                                        c2 = c3;
                                        scratch3._toPattern(patBuf, false);
                                        break;
                                    default:
                                        c2 = c3;
                                        break;
                                }
                                usePat = true;
                                if (mode == 0) {
                                    set(scratch3);
                                    mode = 2;
                                    char c6 = c2;
                                    z = true;
                                } else {
                                    if (op == 0) {
                                        addAll(scratch3);
                                    } else if (op == '&') {
                                        retainAll(scratch3);
                                    } else if (op == '-') {
                                        removeAll(scratch3);
                                    }
                                    op = 0;
                                    c3 = 2;
                                    scratch3 = scratch2;
                                    lastString2 = lastString3;
                                    invert3 = invert2;
                                }
                            } else {
                                boolean invert4 = invert3;
                                if (mode == 0) {
                                    syntaxError(ruleCharacterIterator, "Missing '['");
                                }
                                if (!literal) {
                                    scratch = scratch3;
                                    if (c == 36) {
                                        backup = ruleCharacterIterator.getPos(backup);
                                        c = ruleCharacterIterator.next(opts2);
                                        boolean anchor = c == 93 && !chars.isEscaped();
                                        if (symbolTable == null && !anchor) {
                                            c = 36;
                                            ruleCharacterIterator.setPos(backup);
                                        } else if (!anchor || op != 0) {
                                            syntaxError(ruleCharacterIterator, "Unquoted '$'");
                                        } else {
                                            if (c3 == 1) {
                                                add_unchecked(lastChar, lastChar);
                                                _appendToPat(patBuf, lastChar, false);
                                            }
                                            add_unchecked(DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH);
                                            usePat = true;
                                            patBuf.append(SymbolTable.SYMBOL_REF);
                                            patBuf.append(']');
                                            mode = 2;
                                        }
                                    } else if (c != 38) {
                                        if (c == 45) {
                                            if (op == 0) {
                                                if (c3 != 0) {
                                                    op = (char) c;
                                                } else if (lastString3 != null) {
                                                    op = (char) c;
                                                } else {
                                                    add_unchecked(c, c);
                                                    int c7 = ruleCharacterIterator.next(opts2);
                                                    boolean literal3 = chars.isEscaped();
                                                    if (c7 != 93 || literal3) {
                                                        boolean z3 = literal3;
                                                        c = c7;
                                                    } else {
                                                        patBuf.append("-]");
                                                        mode = 2;
                                                    }
                                                }
                                            }
                                            syntaxError(ruleCharacterIterator, "'-' not after char, string, or set");
                                        } else if (c != 123) {
                                            switch (c) {
                                                case 93:
                                                    if (c3 == 1) {
                                                        add_unchecked(lastChar, lastChar);
                                                        _appendToPat(patBuf, lastChar, false);
                                                    }
                                                    if (op == '-') {
                                                        add_unchecked(op, op);
                                                        patBuf.append(op);
                                                    } else if (op == '&') {
                                                        syntaxError(ruleCharacterIterator, "Trailing '&'");
                                                    }
                                                    patBuf.append(']');
                                                    mode = 2;
                                                    break;
                                                case 94:
                                                    syntaxError(ruleCharacterIterator, "'^' not after '['");
                                                    break;
                                            }
                                        } else {
                                            if (!(op == 0 || op == '-')) {
                                                syntaxError(ruleCharacterIterator, "Missing operand after operator");
                                            }
                                            if (c3 == 1) {
                                                add_unchecked(lastChar, lastChar);
                                                _appendToPat(patBuf, lastChar, false);
                                            }
                                            c3 = 0;
                                            if (buf2 == null) {
                                                buf2 = new StringBuilder();
                                            } else {
                                                buf2.setLength(0);
                                            }
                                            boolean ok = false;
                                            while (true) {
                                                if (!chars.atEnd()) {
                                                    c = ruleCharacterIterator.next(opts2);
                                                    boolean literal4 = chars.isEscaped();
                                                    if (c != 125 || literal4) {
                                                        appendCodePoint(buf2, c);
                                                    } else {
                                                        ok = true;
                                                    }
                                                }
                                            }
                                            int c8 = c;
                                            boolean ok2 = ok;
                                            if (buf2.length() < 1 || !ok2) {
                                                syntaxError(ruleCharacterIterator, "Invalid multicharacter string");
                                            }
                                            String curString = buf2.toString();
                                            boolean z4 = ok2;
                                            if (op == '-') {
                                                String lastString4 = lastString3;
                                                buf = buf2;
                                                int lastSingle = CharSequences.getSingleCodePoint(lastString4 == null ? "" : lastString4);
                                                int i3 = c8;
                                                int curSingle = CharSequences.getSingleCodePoint(curString);
                                                if (lastSingle == Integer.MAX_VALUE || curSingle == Integer.MAX_VALUE) {
                                                    try {
                                                        int i4 = lastSingle;
                                                        try {
                                                            StringRange.expand(lastString4, curString, true, this.strings);
                                                        } catch (Exception e2) {
                                                            e = e2;
                                                        }
                                                    } catch (Exception e3) {
                                                        e = e3;
                                                        int i5 = lastSingle;
                                                        syntaxError(ruleCharacterIterator, e.getMessage());
                                                        lastString = null;
                                                        op = 0;
                                                        patBuf.append('{');
                                                        _appendToPat(patBuf, curString, false);
                                                        patBuf.append('}');
                                                        lastString2 = lastString;
                                                        invert3 = invert4;
                                                        scratch3 = scratch;
                                                        buf2 = buf;
                                                        Appendable appendable2 = rebuiltPat;
                                                    }
                                                } else {
                                                    add(lastSingle, curSingle);
                                                    int i6 = lastSingle;
                                                }
                                                lastString = null;
                                                op = 0;
                                            } else {
                                                buf = buf2;
                                                int i7 = c8;
                                                String str = lastString3;
                                                add((CharSequence) curString);
                                                lastString = curString;
                                            }
                                            patBuf.append('{');
                                            _appendToPat(patBuf, curString, false);
                                            patBuf.append('}');
                                            lastString2 = lastString;
                                            invert3 = invert4;
                                            scratch3 = scratch;
                                            buf2 = buf;
                                        }
                                    } else if (c3 == 2 && op == 0) {
                                        op = (char) c;
                                    } else {
                                        syntaxError(ruleCharacterIterator, "'&' not after set");
                                    }
                                    lastString2 = lastString3;
                                    invert3 = invert4;
                                    scratch3 = scratch;
                                } else {
                                    scratch = scratch3;
                                }
                                switch (c3) {
                                    case 0:
                                        if (op == '-' && lastString3 != null) {
                                            syntaxError(ruleCharacterIterator, "Invalid range");
                                        }
                                        lastChar = c;
                                        lastString2 = null;
                                        c3 = 1;
                                        break;
                                    case 1:
                                        if (op != '-') {
                                            add_unchecked(lastChar, lastChar);
                                            _appendToPat(patBuf, lastChar, false);
                                            lastChar = c;
                                            break;
                                        } else {
                                            if (lastString3 != null) {
                                                syntaxError(ruleCharacterIterator, "Invalid range");
                                            }
                                            if (lastChar >= c) {
                                                syntaxError(ruleCharacterIterator, "Invalid range");
                                            }
                                            add_unchecked(lastChar, c);
                                            _appendToPat(patBuf, lastChar, false);
                                            patBuf.append(op);
                                            _appendToPat(patBuf, c, false);
                                            c3 = 0;
                                            op = 0;
                                            break;
                                        }
                                    case 2:
                                        if (op != 0) {
                                            syntaxError(ruleCharacterIterator, "Set expected after operator");
                                        }
                                        lastChar = c;
                                        c3 = 1;
                                        break;
                                }
                                lastString2 = lastString3;
                                invert3 = invert4;
                                scratch3 = scratch;
                            }
                            Appendable appendable22 = rebuiltPat;
                        } else {
                            ruleCharacterIterator.setPos(backup3);
                            backup = backup3;
                            lastString2 = lastString3;
                            Appendable appendable222 = rebuiltPat;
                        }
                    }
                }
                invert3 = invert;
                if (setMode == 0) {
                }
                Appendable appendable2222 = rebuiltPat;
            }
        }
        UnicodeSet scratch42 = scratch3;
        z = true;
        invert2 = invert;
        UnicodeSet unicodeSet2 = scratch42;
        if (mode != 2) {
            syntaxError(ruleCharacterIterator, "Missing ']'");
        }
        ruleCharacterIterator.skipIgnored(opts2);
        if ((i & 2) != 0) {
            closeOver(2);
        }
        if (invert2) {
            complement();
        }
        if (usePat) {
            append(rebuiltPat, patBuf.toString());
        } else {
            appendNewPattern(rebuiltPat, false, z);
        }
    }

    private static void syntaxError(RuleCharacterIterator chars, String msg) {
        throw new IllegalArgumentException("Error: " + msg + " at \"" + Utility.escape(chars.toString()) + '\"');
    }

    public <T extends Collection<String>> T addAllTo(T target) {
        return addAllTo(this, target);
    }

    public String[] addAllTo(String[] target) {
        return (String[]) addAllTo(this, (T[]) target);
    }

    public static String[] toArray(UnicodeSet set) {
        return (String[]) addAllTo(set, (T[]) new String[set.size()]);
    }

    public UnicodeSet add(Iterable<?> source) {
        return addAll(source);
    }

    public UnicodeSet addAll(Iterable<?> source) {
        checkFrozen();
        for (Object o : source) {
            add((CharSequence) o.toString());
        }
        return this;
    }

    private void ensureCapacity(int newLen) {
        if (newLen > this.list.length) {
            int[] temp = new int[(newLen + 16)];
            System.arraycopy(this.list, 0, temp, 0, this.len);
            this.list = temp;
        }
    }

    private void ensureBufferCapacity(int newLen) {
        if (this.buffer == null || newLen > this.buffer.length) {
            this.buffer = new int[(newLen + 16)];
        }
    }

    private int[] range(int start, int end) {
        if (this.rangeList == null) {
            this.rangeList = new int[]{start, end + 1, 1114112};
        } else {
            this.rangeList[0] = start;
            this.rangeList[1] = end + 1;
        }
        return this.rangeList;
    }

    private UnicodeSet xor(int[] other, int otherLen, int polarity) {
        int j;
        int k;
        ensureBufferCapacity(this.len + otherLen);
        int j2 = 0;
        int k2 = 0;
        int i = 0 + 1;
        int a = this.list[0];
        if (polarity == 1 || polarity == 2) {
            j = 0;
            if (other[0] == 0) {
                j2 = 0 + 1;
                j = other[j2];
            }
        } else {
            j = other[0];
            j2 = 0 + 1;
        }
        while (true) {
            if (a < j) {
                k = k2 + 1;
                this.buffer[k2] = a;
                a = this.list[i];
                i++;
            } else if (j < a) {
                k = k2 + 1;
                this.buffer[k2] = j;
                j = other[j2];
                j2++;
            } else if (a != 1114112) {
                a = this.list[i];
                j = other[j2];
                j2++;
                i++;
            } else {
                this.buffer[k2] = 1114112;
                this.len = k2 + 1;
                int[] temp = this.list;
                this.list = this.buffer;
                this.buffer = temp;
                this.pat = null;
                return this;
            }
            k2 = k;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x005d, code lost:
        r4 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0083, code lost:
        r4 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0084, code lost:
        r2 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x008f, code lost:
        r11 = r11 ^ 2;
        r3 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00a4, code lost:
        r3 = r4;
     */
    private UnicodeSet add(int[] other, int otherLen, int polarity) {
        int j;
        int b;
        int a;
        int k;
        int b2;
        int k2;
        int a2;
        int i;
        int j2;
        int a3;
        int k3;
        int i2;
        ensureBufferCapacity(this.len + otherLen);
        int k4 = 0;
        int i3 = 0 + 1;
        int a4 = this.list[0];
        int j3 = 0 + 1;
        int b3 = other[0];
        while (true) {
            switch (polarity) {
                case 0:
                    if (a4 >= b3) {
                        if (b3 >= a4) {
                            if (a4 != 1114112) {
                                if (k4 <= 0 || a4 > this.buffer[k4 - 1]) {
                                    this.buffer[k4] = a4;
                                    a = this.list[i3];
                                    k = k4 + 1;
                                } else {
                                    k = k4 - 1;
                                    a = max(this.list[i3], this.buffer[k]);
                                }
                                i3++;
                                polarity ^= 1;
                                j = j3 + 1;
                                b = other[j3];
                                break;
                            } else {
                                break;
                            }
                        } else {
                            if (k4 <= 0 || b3 > this.buffer[k4 - 1]) {
                                this.buffer[k4] = b3;
                                b2 = other[j3];
                                k2 = k4 + 1;
                            } else {
                                k2 = k4 - 1;
                                b2 = max(other[j3], this.buffer[k2]);
                            }
                            j3++;
                            polarity ^= 2;
                            break;
                        }
                    } else {
                        if (k4 <= 0 || a4 > this.buffer[k4 - 1]) {
                            this.buffer[k4] = a4;
                            a2 = this.list[i3];
                            k4++;
                        } else {
                            k4--;
                            a2 = max(this.list[i3], this.buffer[k4]);
                        }
                        i3++;
                        polarity ^= 1;
                        break;
                    }
                    break;
                case 1:
                    if (a4 >= b3) {
                        if (b3 >= a4) {
                            if (a4 != 1114112) {
                                i = i3 + 1;
                                a3 = this.list[i3];
                                j2 = j3 + 1;
                                b3 = other[j3];
                                polarity = (polarity ^ 1) ^ 2;
                                break;
                            } else {
                                break;
                            }
                        } else {
                            j = j3 + 1;
                            b = other[j3];
                            break;
                        }
                    } else {
                        k3 = k4 + 1;
                        this.buffer[k4] = a4;
                        i2 = i3 + 1;
                        a4 = this.list[i3];
                        polarity ^= 1;
                        break;
                    }
                case 2:
                    if (b3 >= a4) {
                        if (a4 >= b3) {
                            if (a4 != 1114112) {
                                i = i3 + 1;
                                a3 = this.list[i3];
                                j2 = j3 + 1;
                                b3 = other[j3];
                                polarity = (polarity ^ 1) ^ 2;
                                break;
                            } else {
                                break;
                            }
                        } else {
                            i = i3 + 1;
                            a3 = this.list[i3];
                            polarity = polarity ^ 1;
                            break;
                        }
                    } else {
                        k3 = k4 + 1;
                        this.buffer[k4] = b3;
                        b3 = other[j3];
                        polarity ^= 2;
                        j3++;
                        break;
                    }
                case 3:
                    if (b3 <= a4) {
                        if (a4 == 1114112) {
                            break;
                        } else {
                            k3 = k4 + 1;
                            this.buffer[k4] = a4;
                        }
                    } else if (b3 == 1114112) {
                        break;
                    } else {
                        k3 = k4 + 1;
                        this.buffer[k4] = b3;
                    }
                    i2 = i3 + 1;
                    a4 = this.list[i3];
                    b3 = other[j3];
                    polarity = (polarity ^ 1) ^ 2;
                    j3++;
                    break;
            }
        }
        this.buffer[k4] = 1114112;
        this.len = k4 + 1;
        int[] temp = this.list;
        this.list = this.buffer;
        this.buffer = temp;
        this.pat = null;
        return this;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x005f, code lost:
        r11 = r11 ^ 2;
        r3 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x009e, code lost:
        r3 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00b2, code lost:
        r3 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00be, code lost:
        r4 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00f4, code lost:
        r3 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x002a, code lost:
        r4 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x002b, code lost:
        r2 = r6;
     */
    private UnicodeSet retain(int[] other, int otherLen, int polarity) {
        int k;
        int i;
        int j;
        int a;
        int i2;
        int b;
        int j2;
        int b2;
        int j3;
        int k2;
        int k3;
        int b3;
        ensureBufferCapacity(this.len + otherLen);
        int k4 = 0;
        int i3 = 0 + 1;
        int a2 = this.list[0];
        int j4 = 0 + 1;
        int b4 = other[0];
        while (true) {
            switch (polarity) {
                case 0:
                    if (a2 >= b4) {
                        if (b4 >= a2) {
                            if (a2 != 1114112) {
                                k = k4 + 1;
                                this.buffer[k4] = a2;
                                i = i3 + 1;
                                a = this.list[i3];
                                j = j4 + 1;
                                b = other[j4];
                                polarity = (polarity ^ 1) ^ 2;
                                break;
                            } else {
                                break;
                            }
                        } else {
                            j2 = j4 + 1;
                            b2 = other[j4];
                            break;
                        }
                    } else {
                        i2 = i3 + 1;
                        a2 = this.list[i3];
                        polarity = polarity ^ 1;
                        break;
                    }
                case 1:
                    if (a2 >= b4) {
                        if (b4 >= a2) {
                            if (a2 != 1114112) {
                                i2 = i3 + 1;
                                a2 = this.list[i3];
                                j3 = j4 + 1;
                                b3 = other[j4];
                                polarity = (polarity ^ 1) ^ 2;
                                break;
                            } else {
                                break;
                            }
                        } else {
                            k2 = k4 + 1;
                            this.buffer[k4] = b4;
                            k3 = j4 + 1;
                            b4 = other[j4];
                            polarity ^= 2;
                            break;
                        }
                    } else {
                        i2 = i3 + 1;
                        a2 = this.list[i3];
                        polarity = polarity ^ 1;
                        break;
                    }
                case 2:
                    if (b4 >= a2) {
                        if (a2 >= b4) {
                            if (a2 != 1114112) {
                                i2 = i3 + 1;
                                a2 = this.list[i3];
                                j3 = j4 + 1;
                                b3 = other[j4];
                                polarity = (polarity ^ 1) ^ 2;
                                break;
                            } else {
                                break;
                            }
                        } else {
                            k = k4 + 1;
                            this.buffer[k4] = a2;
                            i = i3 + 1;
                            a = this.list[i3];
                            polarity = polarity ^ 1;
                            break;
                        }
                    } else {
                        j2 = j4 + 1;
                        b2 = other[j4];
                        break;
                    }
                case 3:
                    if (a2 >= b4) {
                        if (b4 >= a2) {
                            if (a2 != 1114112) {
                                k = k4 + 1;
                                this.buffer[k4] = a2;
                                i = i3 + 1;
                                a = this.list[i3];
                                j = j4 + 1;
                                b = other[j4];
                                polarity = (polarity ^ 1) ^ 2;
                                break;
                            } else {
                                break;
                            }
                        } else {
                            k2 = k4 + 1;
                            this.buffer[k4] = b4;
                            k3 = j4 + 1;
                            b4 = other[j4];
                            polarity ^= 2;
                            break;
                        }
                    } else {
                        k = k4 + 1;
                        this.buffer[k4] = a2;
                        i = i3 + 1;
                        a = this.list[i3];
                        polarity = polarity ^ 1;
                        break;
                    }
            }
        }
        this.buffer[k4] = 1114112;
        this.len = k4 + 1;
        int[] temp = this.list;
        this.list = this.buffer;
        this.buffer = temp;
        this.pat = null;
        return this;
    }

    private static final int max(int a, int b) {
        return a > b ? a : b;
    }

    private static synchronized UnicodeSet getInclusions(int src) {
        UnicodeSet unicodeSet;
        synchronized (UnicodeSet.class) {
            if (INCLUSIONS == null) {
                INCLUSIONS = new UnicodeSet[12];
            }
            if (INCLUSIONS[src] == null) {
                UnicodeSet incl = new UnicodeSet();
                switch (src) {
                    case 1:
                        UCharacterProperty.INSTANCE.addPropertyStarts(incl);
                        break;
                    case 2:
                        UCharacterProperty.INSTANCE.upropsvec_addPropertyStarts(incl);
                        break;
                    case 4:
                        UCaseProps.INSTANCE.addPropertyStarts(incl);
                        break;
                    case 5:
                        UBiDiProps.INSTANCE.addPropertyStarts(incl);
                        break;
                    case 6:
                        UCharacterProperty.INSTANCE.addPropertyStarts(incl);
                        UCharacterProperty.INSTANCE.upropsvec_addPropertyStarts(incl);
                        break;
                    case 7:
                        Norm2AllModes.getNFCInstance().impl.addPropertyStarts(incl);
                        UCaseProps.INSTANCE.addPropertyStarts(incl);
                        break;
                    case 8:
                        Norm2AllModes.getNFCInstance().impl.addPropertyStarts(incl);
                        break;
                    case 9:
                        Norm2AllModes.getNFKCInstance().impl.addPropertyStarts(incl);
                        break;
                    case 10:
                        Norm2AllModes.getNFKC_CFInstance().impl.addPropertyStarts(incl);
                        break;
                    case 11:
                        Norm2AllModes.getNFCInstance().impl.addCanonIterPropertyStarts(incl);
                        break;
                    default:
                        throw new IllegalStateException("UnicodeSet.getInclusions(unknown src " + src + ")");
                }
                INCLUSIONS[src] = incl;
            }
            unicodeSet = INCLUSIONS[src];
        }
        return unicodeSet;
    }

    private UnicodeSet applyFilter(Filter filter, int src) {
        clear();
        int startHasProperty = -1;
        UnicodeSet inclusions = getInclusions(src);
        int limitRange = inclusions.getRangeCount();
        int j = 0;
        while (j < limitRange) {
            int start = inclusions.getRangeStart(j);
            int end = inclusions.getRangeEnd(j);
            int startHasProperty2 = startHasProperty;
            for (int ch = start; ch <= end; ch++) {
                if (filter.contains(ch)) {
                    if (startHasProperty2 < 0) {
                        startHasProperty2 = ch;
                    }
                } else if (startHasProperty2 >= 0) {
                    add_unchecked(startHasProperty2, ch - 1);
                    startHasProperty2 = -1;
                }
            }
            j++;
            startHasProperty = startHasProperty2;
        }
        if (startHasProperty >= 0) {
            add_unchecked(startHasProperty, 1114111);
        }
        return this;
    }

    private static String mungeCharName(String source) {
        String source2 = PatternProps.trimWhiteSpace(source);
        StringBuilder buf = null;
        for (int i = 0; i < source2.length(); i++) {
            char ch = source2.charAt(i);
            if (PatternProps.isWhiteSpace(ch)) {
                if (buf == null) {
                    buf = new StringBuilder().append(source2, 0, i);
                } else if (buf.charAt(buf.length() - 1) == ' ') {
                }
                ch = ' ';
            }
            if (buf != null) {
                buf.append(ch);
            }
        }
        return buf == null ? source2 : buf.toString();
    }

    public UnicodeSet applyIntPropertyValue(int prop, int value) {
        checkFrozen();
        if (prop == 8192) {
            applyFilter(new GeneralCategoryMaskFilter(value), 1);
        } else if (prop == 28672) {
            applyFilter(new ScriptExtensionsFilter(value), 2);
        } else {
            applyFilter(new IntPropertyFilter(prop, value), UCharacterProperty.INSTANCE.getSource(prop));
        }
        return this;
    }

    public UnicodeSet applyPropertyAlias(String propertyAlias, String valueAlias) {
        return applyPropertyAlias(propertyAlias, valueAlias, null);
    }

    public UnicodeSet applyPropertyAlias(String propertyAlias, String valueAlias, SymbolTable symbols) {
        int v;
        int p;
        int v2;
        checkFrozen();
        boolean invert = false;
        if (symbols != null && (symbols instanceof XSymbolTable) && ((XSymbolTable) symbols).applyPropertyAlias(propertyAlias, valueAlias, this)) {
            return this;
        }
        if (XSYMBOL_TABLE != null && XSYMBOL_TABLE.applyPropertyAlias(propertyAlias, valueAlias, this)) {
            return this;
        }
        if (valueAlias.length() > 0) {
            p = UCharacter.getPropertyEnum(propertyAlias);
            if (p == 4101) {
                p = 8192;
            }
            if ((p >= 0 && p < 64) || ((p >= 4096 && p < 4118) || (p >= 8192 && p < 8193))) {
                try {
                    v = UCharacter.getPropertyValueEnum(p, valueAlias);
                } catch (IllegalArgumentException e) {
                    if (p == 4098 || p == 4112 || p == 4113) {
                        v2 = Integer.parseInt(PatternProps.trimWhiteSpace(valueAlias));
                        if (v2 < 0 || v2 > 255) {
                            throw e;
                        }
                    } else {
                        throw e;
                    }
                }
            } else if (p == 12288) {
                applyFilter(new NumericValueFilter(Double.parseDouble(PatternProps.trimWhiteSpace(valueAlias))), 1);
                return this;
            } else if (p == 16384) {
                applyFilter(new VersionFilter(VersionInfo.getInstance(mungeCharName(valueAlias))), 2);
                return this;
            } else if (p == 16389) {
                int ch = UCharacter.getCharFromExtendedName(mungeCharName(valueAlias));
                if (ch != -1) {
                    clear();
                    add_unchecked(ch);
                    return this;
                }
                throw new IllegalArgumentException("Invalid character name");
            } else if (p == 16395) {
                throw new IllegalArgumentException("Unicode_1_Name (na1) not supported");
            } else if (p == 28672) {
                v = UCharacter.getPropertyValueEnum(UProperty.SCRIPT, valueAlias);
            } else {
                throw new IllegalArgumentException("Unsupported property");
            }
        } else {
            UPropertyAliases pnames = UPropertyAliases.INSTANCE;
            int p2 = 8192;
            int v3 = pnames.getPropertyValueEnum(8192, propertyAlias);
            if (v3 == -1) {
                p2 = UProperty.SCRIPT;
                v3 = pnames.getPropertyValueEnum(UProperty.SCRIPT, propertyAlias);
                if (v3 == -1) {
                    int p3 = pnames.getPropertyEnum(propertyAlias);
                    if (p3 == -1) {
                        p3 = -1;
                    }
                    if (p3 >= 0 && p3 < 64) {
                        v = 1;
                        p = p3;
                    } else if (p3 != -1) {
                        throw new IllegalArgumentException("Missing property value");
                    } else if (UPropertyAliases.compare(ANY_ID, propertyAlias) == 0) {
                        set(0, 1114111);
                        return this;
                    } else if (UPropertyAliases.compare(ASCII_ID, propertyAlias) == 0) {
                        set(0, 127);
                        return this;
                    } else if (UPropertyAliases.compare(ASSIGNED, propertyAlias) == 0) {
                        v2 = 1;
                        invert = true;
                        p = 8192;
                        v = v2;
                    } else {
                        throw new IllegalArgumentException("Invalid property alias: " + propertyAlias + "=" + valueAlias);
                    }
                }
            }
            p = p2;
            v = v3;
        }
        applyIntPropertyValue(p, v);
        if (invert) {
            complement();
        }
        return this;
    }

    private static boolean resemblesPropertyPattern(String pattern, int pos) {
        boolean z = false;
        if (pos + 5 > pattern.length()) {
            return false;
        }
        if (pattern.regionMatches(pos, "[:", 0, 2) || pattern.regionMatches(true, pos, "\\p", 0, 2) || pattern.regionMatches(pos, "\\N", 0, 2)) {
            z = true;
        }
        return z;
    }

    private static boolean resemblesPropertyPattern(RuleCharacterIterator chars, int iterOpts) {
        boolean result = false;
        int iterOpts2 = iterOpts & -3;
        Object pos = chars.getPos(null);
        int c = chars.next(iterOpts2);
        if (c == 91 || c == 92) {
            int d = chars.next(iterOpts2 & -5);
            boolean z = false;
            if (c != 91 ? d == 78 || d == 112 || d == 80 : d == 58) {
                z = true;
            }
            result = z;
        }
        chars.setPos(pos);
        return result;
    }

    private UnicodeSet applyPropertyPattern(String pattern, ParsePosition ppos, SymbolTable symbols) {
        int pos;
        String valueName;
        String propName;
        String str = pattern;
        int pos2 = ppos.getIndex();
        if (pos2 + 5 > pattern.length()) {
            return null;
        }
        boolean posix = false;
        boolean isName = false;
        boolean invert = false;
        boolean z = false;
        int i = 2;
        if (str.regionMatches(pos2, "[:", 0, 2)) {
            posix = true;
            pos = PatternProps.skipWhiteSpace(str, pos2 + 2);
            if (pos < pattern.length() && str.charAt(pos) == '^') {
                pos++;
                invert = true;
            }
        } else if (!str.regionMatches(true, pos2, "\\p", 0, 2) && !str.regionMatches(pos2, "\\N", 0, 2)) {
            return null;
        } else {
            char c = str.charAt(pos2 + 1);
            invert = c == 'P';
            if (c == 'N') {
                z = true;
            }
            isName = z;
            int pos3 = PatternProps.skipWhiteSpace(str, pos2 + 2);
            if (pos3 != pattern.length()) {
                int pos4 = pos3 + 1;
                if (str.charAt(pos3) != 123) {
                    ParsePosition parsePosition = ppos;
                    SymbolTable symbolTable = symbols;
                    int i2 = pos4;
                } else {
                    pos = pos4;
                }
            } else {
                ParsePosition parsePosition2 = ppos;
                SymbolTable symbolTable2 = symbols;
            }
            return null;
        }
        int close = str.indexOf(posix ? ":]" : "}", pos);
        if (close < 0) {
            return null;
        }
        int equals = str.indexOf(61, pos);
        if (equals < 0 || equals >= close || isName) {
            propName = str.substring(pos, close);
            valueName = "";
            if (isName) {
                valueName = propName;
                propName = "na";
            }
        } else {
            propName = str.substring(pos, equals);
            valueName = str.substring(equals + 1, close);
        }
        applyPropertyAlias(propName, valueName, symbols);
        if (invert) {
            complement();
        }
        if (!posix) {
            i = 1;
        }
        ppos.setIndex(i + close);
        return this;
    }

    private void applyPropertyPattern(RuleCharacterIterator chars, Appendable rebuiltPat, SymbolTable symbols) {
        String patStr = chars.lookahead();
        ParsePosition pos = new ParsePosition(0);
        applyPropertyPattern(patStr, pos, symbols);
        if (pos.getIndex() == 0) {
            syntaxError(chars, "Invalid property pattern");
        }
        chars.jumpahead(pos.getIndex());
        append(rebuiltPat, patStr.substring(0, pos.getIndex()));
    }

    private static final void addCaseMapping(UnicodeSet set, int result, StringBuilder full) {
        if (result < 0) {
            return;
        }
        if (result > 31) {
            set.add(result);
            return;
        }
        set.add((CharSequence) full.toString());
        full.setLength(0);
    }

    public UnicodeSet closeOver(int attribute) {
        checkFrozen();
        if ((attribute & 6) != 0) {
            UCaseProps csp = UCaseProps.INSTANCE;
            UnicodeSet foldSet = new UnicodeSet(this);
            ULocale root = ULocale.ROOT;
            if ((attribute & 2) != 0) {
                foldSet.strings.clear();
            }
            int n = getRangeCount();
            StringBuilder full = new StringBuilder();
            for (int i = 0; i < n; i++) {
                int start = getRangeStart(i);
                int end = getRangeEnd(i);
                if ((attribute & 2) != 0) {
                    for (int cp = start; cp <= end; cp++) {
                        csp.addCaseClosure(cp, foldSet);
                    }
                } else {
                    for (int cp2 = start; cp2 <= end; cp2++) {
                        addCaseMapping(foldSet, csp.toFullLower(cp2, null, full, 1), full);
                        addCaseMapping(foldSet, csp.toFullTitle(cp2, null, full, 1), full);
                        addCaseMapping(foldSet, csp.toFullUpper(cp2, null, full, 1), full);
                        addCaseMapping(foldSet, csp.toFullFolding(cp2, full, 0), full);
                    }
                }
            }
            if (!this.strings.isEmpty()) {
                if ((attribute & 2) != 0) {
                    Iterator<String> it = this.strings.iterator();
                    while (it.hasNext()) {
                        String str = UCharacter.foldCase(it.next(), 0);
                        if (!csp.addStringCaseClosure(str, foldSet)) {
                            foldSet.add((CharSequence) str);
                        }
                    }
                } else {
                    BreakIterator bi = BreakIterator.getWordInstance(root);
                    Iterator<String> it2 = this.strings.iterator();
                    while (it2.hasNext()) {
                        String str2 = it2.next();
                        foldSet.add((CharSequence) UCharacter.toLowerCase(root, str2));
                        foldSet.add((CharSequence) UCharacter.toTitleCase(root, str2, bi));
                        foldSet.add((CharSequence) UCharacter.toUpperCase(root, str2));
                        foldSet.add((CharSequence) UCharacter.foldCase(str2, 0));
                    }
                }
            }
            set(foldSet);
        }
        return this;
    }

    public boolean isFrozen() {
        return (this.bmpSet == null && this.stringSpan == null) ? false : true;
    }

    public UnicodeSet freeze() {
        if (!isFrozen()) {
            this.buffer = null;
            if (this.list.length > this.len + 16) {
                int capacity = this.len == 0 ? 1 : this.len;
                int[] oldList = this.list;
                this.list = new int[capacity];
                int i = capacity;
                while (true) {
                    int i2 = i - 1;
                    if (i <= 0) {
                        break;
                    }
                    this.list[i2] = oldList[i2];
                    i = i2;
                }
            }
            if (!this.strings.isEmpty()) {
                this.stringSpan = new UnicodeSetStringSpan(this, new ArrayList(this.strings), 127);
            }
            if (this.stringSpan == null || !this.stringSpan.needsStringSpanUTF16()) {
                this.bmpSet = new BMPSet(this.list, this.len);
            }
        }
        return this;
    }

    public int span(CharSequence s, SpanCondition spanCondition) {
        return span(s, 0, spanCondition);
    }

    public int span(CharSequence s, int start, SpanCondition spanCondition) {
        int which;
        int end = s.length();
        if (start < 0) {
            start = 0;
        } else if (start >= end) {
            return end;
        }
        if (this.bmpSet != null) {
            return this.bmpSet.span(s, start, spanCondition, null);
        }
        if (this.stringSpan != null) {
            return this.stringSpan.span(s, start, spanCondition);
        }
        if (!this.strings.isEmpty()) {
            if (spanCondition == SpanCondition.NOT_CONTAINED) {
                which = 33;
            } else {
                which = 34;
            }
            UnicodeSetStringSpan strSpan = new UnicodeSetStringSpan(this, new ArrayList(this.strings), which);
            if (strSpan.needsStringSpanUTF16()) {
                return strSpan.span(s, start, spanCondition);
            }
        }
        return spanCodePointsAndCount(s, start, spanCondition, null);
    }

    @Deprecated
    public int spanAndCount(CharSequence s, int start, SpanCondition spanCondition, OutputInt outCount) {
        int which;
        if (outCount != null) {
            int end = s.length();
            if (start < 0) {
                start = 0;
            } else if (start >= end) {
                return end;
            }
            if (this.stringSpan != null) {
                return this.stringSpan.spanAndCount(s, start, spanCondition, outCount);
            }
            if (this.bmpSet != null) {
                return this.bmpSet.span(s, start, spanCondition, outCount);
            }
            if (this.strings.isEmpty()) {
                return spanCodePointsAndCount(s, start, spanCondition, outCount);
            }
            if (spanCondition == SpanCondition.NOT_CONTAINED) {
                which = 33;
            } else {
                which = 34;
            }
            return new UnicodeSetStringSpan(this, new ArrayList(this.strings), which | 64).spanAndCount(s, start, spanCondition, outCount);
        }
        throw new IllegalArgumentException("outCount must not be null");
    }

    private int spanCodePointsAndCount(CharSequence s, int start, SpanCondition spanCondition, OutputInt outCount) {
        int count = 0;
        boolean spanContained = spanCondition != SpanCondition.NOT_CONTAINED;
        int next = start;
        int length = s.length();
        do {
            int c = Character.codePointAt(s, next);
            if (spanContained != contains(c)) {
                break;
            }
            count++;
            next += Character.charCount(c);
        } while (next < length);
        if (outCount != null) {
            outCount.value = count;
        }
        return next;
    }

    public int spanBack(CharSequence s, SpanCondition spanCondition) {
        return spanBack(s, s.length(), spanCondition);
    }

    public int spanBack(CharSequence s, int fromIndex, SpanCondition spanCondition) {
        int which;
        boolean spanContained = false;
        if (fromIndex <= 0) {
            return 0;
        }
        if (fromIndex > s.length()) {
            fromIndex = s.length();
        }
        if (this.bmpSet != null) {
            return this.bmpSet.spanBack(s, fromIndex, spanCondition);
        }
        if (this.stringSpan != null) {
            return this.stringSpan.spanBack(s, fromIndex, spanCondition);
        }
        if (!this.strings.isEmpty()) {
            if (spanCondition == SpanCondition.NOT_CONTAINED) {
                which = 17;
            } else {
                which = 18;
            }
            UnicodeSetStringSpan strSpan = new UnicodeSetStringSpan(this, new ArrayList(this.strings), which);
            if (strSpan.needsStringSpanUTF16()) {
                return strSpan.spanBack(s, fromIndex, spanCondition);
            }
        }
        if (spanCondition != SpanCondition.NOT_CONTAINED) {
            spanContained = true;
        }
        int prev = fromIndex;
        do {
            int c = Character.codePointBefore(s, prev);
            if (spanContained != contains(c)) {
                break;
            }
            prev -= Character.charCount(c);
        } while (prev > 0);
        return prev;
    }

    public UnicodeSet cloneAsThawed() {
        return new UnicodeSet(this);
    }

    private void checkFrozen() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
    }

    public Iterable<EntryRange> ranges() {
        return new EntryRangeIterable();
    }

    public Iterator<String> iterator() {
        return new UnicodeSetIterator2(this);
    }

    public <T extends CharSequence> boolean containsAll(Iterable<T> collection) {
        for (T o : collection) {
            if (!contains((CharSequence) o)) {
                return false;
            }
        }
        return true;
    }

    public <T extends CharSequence> boolean containsNone(Iterable<T> collection) {
        for (T o : collection) {
            if (contains((CharSequence) o)) {
                return false;
            }
        }
        return true;
    }

    public final <T extends CharSequence> boolean containsSome(Iterable<T> collection) {
        return !containsNone(collection);
    }

    public <T extends CharSequence> UnicodeSet addAll(T... collection) {
        checkFrozen();
        for (T str : collection) {
            add((CharSequence) str);
        }
        return this;
    }

    public <T extends CharSequence> UnicodeSet removeAll(Iterable<T> collection) {
        checkFrozen();
        for (T o : collection) {
            remove((CharSequence) o);
        }
        return this;
    }

    public <T extends CharSequence> UnicodeSet retainAll(Iterable<T> collection) {
        checkFrozen();
        UnicodeSet toRetain = new UnicodeSet();
        toRetain.addAll((Iterable<?>) collection);
        retainAll(toRetain);
        return this;
    }

    public int compareTo(UnicodeSet o) {
        return compareTo(o, ComparisonStyle.SHORTER_FIRST);
    }

    public int compareTo(UnicodeSet o, ComparisonStyle style) {
        int i = -1;
        boolean z = false;
        if (style != ComparisonStyle.LEXICOGRAPHIC) {
            int diff = size() - o.size();
            if (diff != 0) {
                boolean z2 = diff < 0;
                if (style == ComparisonStyle.SHORTER_FIRST) {
                    z = true;
                }
                if (z2 != z) {
                    i = 1;
                }
                return i;
            }
        }
        int i2 = 0;
        while (true) {
            int i3 = this.list[i2] - o.list[i2];
            int result = i3;
            if (i3 != 0) {
                if (this.list[i2] == 1114112) {
                    if (this.strings.isEmpty()) {
                        return 1;
                    }
                    return compare((CharSequence) this.strings.first(), o.list[i2]);
                } else if (o.list[i2] != 1114112) {
                    return (i2 & 1) == 0 ? result : -result;
                } else if (o.strings.isEmpty()) {
                    return -1;
                } else {
                    int compareResult = compare((CharSequence) o.strings.first(), this.list[i2]);
                    if (compareResult <= 0) {
                        i = compareResult < 0 ? 1 : 0;
                    }
                    return i;
                }
            } else if (this.list[i2] == 1114112) {
                return compare(this.strings, o.strings);
            } else {
                i2++;
            }
        }
    }

    public int compareTo(Iterable<String> other) {
        return compare(this, (UnicodeSet) other);
    }

    public static int compare(CharSequence string, int codePoint) {
        return CharSequences.compare(string, codePoint);
    }

    public static int compare(int codePoint, CharSequence string) {
        return -CharSequences.compare(string, codePoint);
    }

    public static <T extends Comparable<T>> int compare(Iterable<T> collection1, Iterable<T> collection2) {
        return compare(collection1.iterator(), collection2.iterator());
    }

    @Deprecated
    public static <T extends Comparable<T>> int compare(Iterator<T> first, Iterator<T> other) {
        while (first.hasNext()) {
            if (!other.hasNext()) {
                return 1;
            }
            int result = ((Comparable) first.next()).compareTo((Comparable) other.next());
            if (result != 0) {
                return result;
            }
        }
        return other.hasNext() ? -1 : 0;
    }

    public static <T extends Comparable<T>> int compare(Collection<T> collection1, Collection<T> collection2, ComparisonStyle style) {
        if (style != ComparisonStyle.LEXICOGRAPHIC) {
            int diff = collection1.size() - collection2.size();
            if (diff != 0) {
                boolean z = false;
                int i = 1;
                boolean z2 = diff < 0;
                if (style == ComparisonStyle.SHORTER_FIRST) {
                    z = true;
                }
                if (z2 == z) {
                    i = -1;
                }
                return i;
            }
        }
        return compare(collection1, collection2);
    }

    public static <T, U extends Collection<T>> U addAllTo(Iterable<T> source, U target) {
        for (T item : source) {
            target.add(item);
        }
        return target;
    }

    public static <T> T[] addAllTo(Iterable<T> source, T[] target) {
        int i = 0;
        for (T item : source) {
            target[i] = item;
            i++;
        }
        return target;
    }

    public Collection<String> strings() {
        return Collections.unmodifiableSortedSet(this.strings);
    }

    @Deprecated
    public static int getSingleCodePoint(CharSequence s) {
        return CharSequences.getSingleCodePoint(s);
    }

    @Deprecated
    public UnicodeSet addBridges(UnicodeSet dontCare) {
        UnicodeSetIterator it = new UnicodeSetIterator(new UnicodeSet(this).complement());
        while (it.nextRange()) {
            if (!(it.codepoint == 0 || it.codepoint == UnicodeSetIterator.IS_STRING || it.codepointEnd == 1114111 || !dontCare.contains(it.codepoint, it.codepointEnd))) {
                add(it.codepoint, it.codepointEnd);
            }
        }
        return this;
    }

    @Deprecated
    public int findIn(CharSequence value, int fromIndex, boolean findNot) {
        while (fromIndex < value.length()) {
            int cp = UTF16.charAt(value, fromIndex);
            if (contains(cp) != findNot) {
                break;
            }
            fromIndex += UTF16.getCharCount(cp);
        }
        return fromIndex;
    }

    @Deprecated
    public int findLastIn(CharSequence value, int fromIndex, boolean findNot) {
        int fromIndex2 = fromIndex - 1;
        while (fromIndex2 >= 0) {
            int cp = UTF16.charAt(value, fromIndex2);
            if (contains(cp) != findNot) {
                break;
            }
            fromIndex2 -= UTF16.getCharCount(cp);
        }
        if (fromIndex2 < 0) {
            return -1;
        }
        return fromIndex2;
    }

    @Deprecated
    public String stripFrom(CharSequence source, boolean matches) {
        StringBuilder result = new StringBuilder();
        int pos = 0;
        while (pos < source.length()) {
            int inside = findIn(source, pos, !matches);
            result.append(source.subSequence(pos, inside));
            pos = findIn(source, inside, matches);
        }
        return result.toString();
    }

    @Deprecated
    public static XSymbolTable getDefaultXSymbolTable() {
        return XSYMBOL_TABLE;
    }

    @Deprecated
    public static void setDefaultXSymbolTable(XSymbolTable xSymbolTable) {
        INCLUSIONS = null;
        XSYMBOL_TABLE = xSymbolTable;
    }
}
