package android.text;

import android.content.res.Resources;
import android.icu.util.ULocale;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.text.format.DateFormat;
import android.text.style.CharacterStyle;
import android.text.style.MetricAffectingSpan;
import android.text.style.ReplacementSpan;
import android.util.Log;
import android.util.Printer;
import android.widget.ExpandableListView;
import com.android.internal.R;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.telephony.AbstractRILConstants;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.ArrayUtils;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Pattern;

public class TextUtils {
    public static final int ABSOLUTE_SIZE_SPAN = 16;
    public static final int ALIGNMENT_SPAN = 1;
    public static final int ANNOTATION = 18;
    public static final int BACKGROUND_COLOR_SPAN = 12;
    public static final int BULLET_SPAN = 8;
    public static final int CAP_MODE_CHARACTERS = 4096;
    public static final int CAP_MODE_SENTENCES = 16384;
    public static final int CAP_MODE_WORDS = 8192;
    public static final Creator<CharSequence> CHAR_SEQUENCE_CREATOR = null;
    public static final int EASY_EDIT_SPAN = 22;
    static final char[] ELLIPSIS_NORMAL = null;
    public static final String ELLIPSIS_STRING = null;
    static final char[] ELLIPSIS_TWO_DOTS = null;
    private static final String ELLIPSIS_TWO_DOTS_STRING = null;
    private static String[] EMPTY_STRING_ARRAY = null;
    private static final char FIRST_RIGHT_TO_LEFT = '\u0590';
    public static final int FIRST_SPAN = 1;
    public static final int FOREGROUND_COLOR_SPAN = 2;
    public static final int LAST_SPAN = 24;
    public static final int LEADING_MARGIN_SPAN = 10;
    public static final int LOCALE_SPAN = 23;
    public static final int QUOTE_SPAN = 9;
    public static final int RELATIVE_SIZE_SPAN = 3;
    public static final int SCALE_X_SPAN = 4;
    public static final int SPELL_CHECK_SPAN = 20;
    public static final int STRIKETHROUGH_SPAN = 5;
    public static final int STYLE_SPAN = 7;
    public static final int SUBSCRIPT_SPAN = 15;
    public static final int SUGGESTION_RANGE_SPAN = 21;
    public static final int SUGGESTION_SPAN = 19;
    public static final int SUPERSCRIPT_SPAN = 14;
    private static final String TAG = "TextUtils";
    public static final int TEXT_APPEARANCE_SPAN = 17;
    public static final int TTS_SPAN = 24;
    public static final int TYPEFACE_SPAN = 13;
    public static final int UNDERLINE_SPAN = 6;
    public static final int URL_SPAN = 11;
    private static final char ZWNBS_CHAR = '\ufeff';
    private static Object sLock;
    private static char[] sTemp;

    public interface EllipsizeCallback {
        void ellipsized(int i, int i2);
    }

    private static class Reverser implements CharSequence, GetChars {
        private int mEnd;
        private CharSequence mSource;
        private int mStart;

        public Reverser(CharSequence source, int start, int end) {
            this.mSource = source;
            this.mStart = start;
            this.mEnd = end;
        }

        public int length() {
            return this.mEnd - this.mStart;
        }

        public CharSequence subSequence(int start, int end) {
            char[] buf = new char[(end - start)];
            getChars(start, end, buf, 0);
            return new String(buf);
        }

        public String toString() {
            return subSequence(0, length()).toString();
        }

        public char charAt(int off) {
            return AndroidCharacter.getMirror(this.mSource.charAt((this.mEnd - 1) - off));
        }

        public void getChars(int start, int end, char[] dest, int destoff) {
            TextUtils.getChars(this.mSource, this.mStart + start, this.mStart + end, dest, destoff);
            AndroidCharacter.mirror(dest, 0, end - start);
            int len = end - start;
            int n = (end - start) / TextUtils.FOREGROUND_COLOR_SPAN;
            for (int i = 0; i < n; i += TextUtils.FIRST_SPAN) {
                char tmp = dest[destoff + i];
                dest[destoff + i] = dest[((destoff + len) - i) - 1];
                dest[((destoff + len) - i) - 1] = tmp;
            }
        }
    }

    public interface StringSplitter extends Iterable<String> {
        void setString(String str);
    }

    public static class SimpleStringSplitter implements StringSplitter, Iterator<String> {
        private char mDelimiter;
        private int mLength;
        private int mPosition;
        private String mString;

        public SimpleStringSplitter(char delimiter) {
            this.mDelimiter = delimiter;
        }

        public void setString(String string) {
            this.mString = string;
            this.mPosition = 0;
            this.mLength = this.mString.length();
        }

        public Iterator<String> iterator() {
            return this;
        }

        public boolean hasNext() {
            return this.mPosition < this.mLength;
        }

        public String next() {
            int end = this.mString.indexOf(this.mDelimiter, this.mPosition);
            if (end == -1) {
                end = this.mLength;
            }
            String nextString = this.mString.substring(this.mPosition, end);
            this.mPosition = end + TextUtils.FIRST_SPAN;
            return nextString;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public enum TruncateAt {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.TextUtils.TruncateAt.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.TextUtils.TruncateAt.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.text.TextUtils.TruncateAt.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.TextUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.TextUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.TextUtils.<clinit>():void");
    }

    private TextUtils() {
    }

    public static void getChars(CharSequence s, int start, int end, char[] dest, int destoff) {
        Class<? extends CharSequence> c = s.getClass();
        if (c == String.class) {
            ((String) s).getChars(start, end, dest, destoff);
        } else if (c == StringBuffer.class) {
            ((StringBuffer) s).getChars(start, end, dest, destoff);
        } else if (c == StringBuilder.class) {
            ((StringBuilder) s).getChars(start, end, dest, destoff);
        } else if (s instanceof GetChars) {
            ((GetChars) s).getChars(start, end, dest, destoff);
        } else {
            int i = start;
            int destoff2 = destoff;
            while (i < end) {
                destoff = destoff2 + FIRST_SPAN;
                dest[destoff2] = s.charAt(i);
                i += FIRST_SPAN;
                destoff2 = destoff;
            }
            destoff = destoff2;
        }
    }

    public static int indexOf(CharSequence s, char ch) {
        return indexOf(s, ch, 0);
    }

    public static int indexOf(CharSequence s, char ch, int start) {
        if (s.getClass() == String.class) {
            return ((String) s).indexOf(ch, start);
        }
        return indexOf(s, ch, start, s.length());
    }

    public static int indexOf(CharSequence s, char ch, int start, int end) {
        Class<? extends CharSequence> c = s.getClass();
        int i;
        if ((s instanceof GetChars) || c == StringBuffer.class || c == StringBuilder.class || c == String.class) {
            char[] temp = obtain(AbstractRILConstants.HW_RIL_REQUEST_BASE);
            while (start < end) {
                int segend = start + AbstractRILConstants.HW_RIL_REQUEST_BASE;
                if (segend > end) {
                    segend = end;
                }
                getChars(s, start, segend, temp, 0);
                int count = segend - start;
                for (i = 0; i < count; i += FIRST_SPAN) {
                    if (temp[i] == ch) {
                        recycle(temp);
                        return i + start;
                    }
                }
                start = segend;
            }
            recycle(temp);
            return -1;
        }
        for (i = start; i < end; i += FIRST_SPAN) {
            if (s.charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(CharSequence s, char ch) {
        return lastIndexOf(s, ch, s.length() - 1);
    }

    public static int lastIndexOf(CharSequence s, char ch, int last) {
        if (s.getClass() == String.class) {
            return ((String) s).lastIndexOf(ch, last);
        }
        return lastIndexOf(s, ch, 0, last);
    }

    public static int lastIndexOf(CharSequence s, char ch, int start, int last) {
        if (last < 0) {
            return -1;
        }
        if (last >= s.length()) {
            last = s.length() - 1;
        }
        int end = last + FIRST_SPAN;
        Class<? extends CharSequence> c = s.getClass();
        int i;
        if ((s instanceof GetChars) || c == StringBuffer.class || c == StringBuilder.class || c == String.class) {
            char[] temp = obtain(AbstractRILConstants.HW_RIL_REQUEST_BASE);
            while (start < end) {
                int segstart = end - 500;
                if (segstart < start) {
                    segstart = start;
                }
                getChars(s, segstart, end, temp, 0);
                for (i = (end - segstart) - 1; i >= 0; i--) {
                    if (temp[i] == ch) {
                        recycle(temp);
                        return i + segstart;
                    }
                }
                end = segstart;
            }
            recycle(temp);
            return -1;
        }
        for (i = end - 1; i >= start; i--) {
            if (s.charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(CharSequence s, CharSequence needle) {
        return indexOf(s, needle, 0, s.length());
    }

    public static int indexOf(CharSequence s, CharSequence needle, int start) {
        return indexOf(s, needle, start, s.length());
    }

    public static int indexOf(CharSequence s, CharSequence needle, int start, int end) {
        int nlen = needle.length();
        if (nlen == 0) {
            return start;
        }
        char c = needle.charAt(0);
        while (true) {
            start = indexOf(s, c, start);
            if (start > end - nlen || start < 0) {
                return -1;
            }
            if (regionMatches(s, start, needle, 0, nlen)) {
                return start;
            }
            start += FIRST_SPAN;
        }
    }

    public static boolean regionMatches(CharSequence one, int toffset, CharSequence two, int ooffset, int len) {
        int tempLen = len * FOREGROUND_COLOR_SPAN;
        if (tempLen < len) {
            throw new IndexOutOfBoundsException();
        }
        char[] temp = obtain(tempLen);
        getChars(one, toffset, toffset + len, temp, 0);
        getChars(two, ooffset, ooffset + len, temp, len);
        boolean match = true;
        for (int i = 0; i < len; i += FIRST_SPAN) {
            if (temp[i] != temp[i + len]) {
                match = false;
                break;
            }
        }
        recycle(temp);
        return match;
    }

    public static String substring(CharSequence source, int start, int end) {
        if (source instanceof String) {
            return ((String) source).substring(start, end);
        }
        if (source instanceof StringBuilder) {
            return ((StringBuilder) source).substring(start, end);
        }
        if (source instanceof StringBuffer) {
            return ((StringBuffer) source).substring(start, end);
        }
        char[] temp = obtain(end - start);
        getChars(source, start, end, temp, 0);
        String ret = new String(temp, 0, end - start);
        recycle(temp);
        return ret;
    }

    public static String join(CharSequence delimiter, Object[] tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        int length = tokens.length;
        for (int i = 0; i < length; i += FIRST_SPAN) {
            Object token = tokens[i];
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }

    public static String join(CharSequence delimiter, Iterable tokens) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> it = tokens.iterator();
        if (it.hasNext()) {
            sb.append(it.next());
            while (it.hasNext()) {
                sb.append(delimiter);
                sb.append(it.next());
            }
        }
        return sb.toString();
    }

    public static String[] split(String text, String expression) {
        if (text.length() == 0) {
            return EMPTY_STRING_ARRAY;
        }
        return text.split(expression, -1);
    }

    public static String[] split(String text, Pattern pattern) {
        if (text.length() == 0) {
            return EMPTY_STRING_ARRAY;
        }
        return pattern.split(text, -1);
    }

    public static CharSequence stringOrSpannedString(CharSequence source) {
        if (source == null) {
            return null;
        }
        if (source instanceof SpannedString) {
            return source;
        }
        if (source instanceof Spanned) {
            return new SpannedString(source);
        }
        return source.toString();
    }

    public static boolean isEmpty(CharSequence str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        return false;
    }

    public static String nullIfEmpty(String str) {
        return isEmpty(str) ? null : str;
    }

    public static int getTrimmedLength(CharSequence s) {
        int len = s.length();
        int start = 0;
        while (start < len && s.charAt(start) <= ' ') {
            start += FIRST_SPAN;
        }
        int end = len;
        while (end > start && s.charAt(end - 1) <= ' ') {
            end--;
        }
        return end - start;
    }

    public static boolean equals(CharSequence a, CharSequence b) {
        if (a == b) {
            return true;
        }
        if (!(a == null || b == null)) {
            int length = a.length();
            if (length == b.length()) {
                if ((a instanceof String) && (b instanceof String)) {
                    return a.equals(b);
                }
                for (int i = 0; i < length; i += FIRST_SPAN) {
                    if (a.charAt(i) != b.charAt(i)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public static CharSequence getReverse(CharSequence source, int start, int end) {
        return new Reverser(source, start, end);
    }

    public static void writeToParcel(CharSequence cs, Parcel p, int parcelableFlags) {
        if (cs instanceof Spanned) {
            p.writeInt(0);
            p.writeString(cs.toString());
            Spanned sp = (Spanned) cs;
            Object[] os = sp.getSpans(0, cs.length(), Object.class);
            for (int i = 0; i < os.length; i += FIRST_SPAN) {
                Object o = os[i];
                ParcelableSpan prop = os[i];
                if (prop instanceof CharacterStyle) {
                    prop = ((CharacterStyle) prop).getUnderlying();
                }
                if (prop instanceof ParcelableSpan) {
                    ParcelableSpan ps = prop;
                    int spanTypeId = ps.getSpanTypeIdInternal();
                    if (spanTypeId < FIRST_SPAN || spanTypeId > TTS_SPAN) {
                        Log.e(TAG, "External class \"" + ps.getClass().getSimpleName() + "\" is attempting to use the frameworks-only ParcelableSpan" + " interface");
                    } else {
                        p.writeInt(spanTypeId);
                        ps.writeToParcelInternal(p, parcelableFlags);
                        writeWhere(p, sp, o);
                    }
                }
            }
            p.writeInt(0);
            return;
        }
        p.writeInt(FIRST_SPAN);
        if (cs != null) {
            p.writeString(cs.toString());
        } else {
            p.writeString(null);
        }
    }

    private static void writeWhere(Parcel p, Spanned sp, Object o) {
        p.writeInt(sp.getSpanStart(o));
        p.writeInt(sp.getSpanEnd(o));
        p.writeInt(sp.getSpanFlags(o));
    }

    public static void dumpSpans(CharSequence cs, Printer printer, String prefix) {
        if (cs instanceof Spanned) {
            Spanned sp = (Spanned) cs;
            Object[] os = sp.getSpans(0, cs.length(), Object.class);
            for (int i = 0; i < os.length; i += FIRST_SPAN) {
                Object o = os[i];
                printer.println(prefix + cs.subSequence(sp.getSpanStart(o), sp.getSpanEnd(o)) + ": " + Integer.toHexString(System.identityHashCode(o)) + " " + o.getClass().getCanonicalName() + " (" + sp.getSpanStart(o) + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + sp.getSpanEnd(o) + ") fl=#" + sp.getSpanFlags(o));
            }
            return;
        }
        printer.println(prefix + cs + ": (no spans)");
    }

    public static CharSequence replace(CharSequence template, String[] sources, CharSequence[] destinations) {
        int i;
        CharSequence tb = new SpannableStringBuilder(template);
        for (i = 0; i < sources.length; i += FIRST_SPAN) {
            int where = indexOf(tb, sources[i]);
            if (where >= 0) {
                tb.setSpan(sources[i], where, sources[i].length() + where, 33);
            }
        }
        for (i = 0; i < sources.length; i += FIRST_SPAN) {
            int start = tb.getSpanStart(sources[i]);
            int end = tb.getSpanEnd(sources[i]);
            if (start >= 0) {
                tb.replace(start, end, destinations[i]);
            }
        }
        return tb;
    }

    public static CharSequence expandTemplate(CharSequence template, CharSequence... values) {
        if (values.length > QUOTE_SPAN) {
            throw new IllegalArgumentException("max of 9 values are supported");
        }
        SpannableStringBuilder ssb = new SpannableStringBuilder(template);
        int i = 0;
        while (i < ssb.length()) {
            try {
                if (ssb.charAt(i) == '^') {
                    char next = ssb.charAt(i + FIRST_SPAN);
                    if (next == '^') {
                        ssb.delete(i + FIRST_SPAN, i + FOREGROUND_COLOR_SPAN);
                        i += FIRST_SPAN;
                    } else if (Character.isDigit(next)) {
                        int which = Character.getNumericValue(next) - 1;
                        if (which < 0) {
                            throw new IllegalArgumentException("template requests value ^" + (which + FIRST_SPAN));
                        } else if (which >= values.length) {
                            throw new IllegalArgumentException("template requests value ^" + (which + FIRST_SPAN) + "; only " + values.length + " provided");
                        } else {
                            ssb.replace(i, i + FOREGROUND_COLOR_SPAN, values[which]);
                            i += values[which].length();
                        }
                    }
                }
                i += FIRST_SPAN;
            } catch (IndexOutOfBoundsException e) {
            }
        }
        return ssb;
    }

    public static int getOffsetBefore(CharSequence text, int offset) {
        if (offset == 0 || offset == FIRST_SPAN) {
            return 0;
        }
        char c = text.charAt(offset - 1);
        if (c < '\udc00' || c > '\udfff') {
            offset--;
        } else {
            char c1 = text.charAt(offset - 2);
            if (c1 < '\ud800' || c1 > '\udbff') {
                offset--;
            } else {
                offset -= 2;
            }
        }
        if (text instanceof Spanned) {
            ReplacementSpan[] spans = (ReplacementSpan[]) ((Spanned) text).getSpans(offset, offset, ReplacementSpan.class);
            for (int i = 0; i < spans.length; i += FIRST_SPAN) {
                int start = ((Spanned) text).getSpanStart(spans[i]);
                int end = ((Spanned) text).getSpanEnd(spans[i]);
                if (start < offset && end > offset) {
                    offset = start;
                }
            }
        }
        return offset;
    }

    public static int getOffsetAfter(CharSequence text, int offset) {
        int len = text.length();
        if (offset == len || offset == len - 1) {
            return len;
        }
        char c = text.charAt(offset);
        if (c < '\ud800' || c > '\udbff') {
            offset += FIRST_SPAN;
        } else {
            char c1 = text.charAt(offset + FIRST_SPAN);
            if (c1 < '\udc00' || c1 > '\udfff') {
                offset += FIRST_SPAN;
            } else {
                offset += FOREGROUND_COLOR_SPAN;
            }
        }
        if (text instanceof Spanned) {
            ReplacementSpan[] spans = (ReplacementSpan[]) ((Spanned) text).getSpans(offset, offset, ReplacementSpan.class);
            for (int i = 0; i < spans.length; i += FIRST_SPAN) {
                int start = ((Spanned) text).getSpanStart(spans[i]);
                int end = ((Spanned) text).getSpanEnd(spans[i]);
                if (start < offset && end > offset) {
                    offset = end;
                }
            }
        }
        return offset;
    }

    private static void readSpan(Parcel p, Spannable sp, Object o) {
        sp.setSpan(o, p.readInt(), p.readInt(), p.readInt());
    }

    public static void copySpansFrom(Spanned source, int start, int end, Class kind, Spannable dest, int destoff) {
        if (kind == null) {
            kind = Object.class;
        }
        Object[] spans = source.getSpans(start, end, kind);
        for (int i = 0; i < spans.length; i += FIRST_SPAN) {
            int st = source.getSpanStart(spans[i]);
            int en = source.getSpanEnd(spans[i]);
            int fl = source.getSpanFlags(spans[i]);
            if (st < start) {
                st = start;
            }
            if (en > end) {
                en = end;
            }
            dest.setSpan(spans[i], (st - start) + destoff, (en - start) + destoff, fl);
        }
    }

    public static CharSequence ellipsize(CharSequence text, TextPaint p, float avail, TruncateAt where) {
        return ellipsize(text, p, avail, where, false, null);
    }

    public static CharSequence ellipsize(CharSequence text, TextPaint paint, float avail, TruncateAt where, boolean preserveLength, EllipsizeCallback callback) {
        return ellipsize(text, paint, avail, where, preserveLength, callback, TextDirectionHeuristics.FIRSTSTRONG_LTR, where == TruncateAt.END_SMALL ? ELLIPSIS_TWO_DOTS_STRING : ELLIPSIS_STRING);
    }

    public static CharSequence ellipsize(CharSequence text, TextPaint paint, float avail, TruncateAt where, boolean preserveLength, EllipsizeCallback callback, TextDirectionHeuristic textDir, String ellipsis) {
        int len = text.length();
        MeasuredText mt = MeasuredText.obtain();
        try {
            if (setPara(mt, paint, text, 0, text.length(), textDir) <= avail) {
                if (callback != null) {
                    callback.ellipsized(0, 0);
                }
                MeasuredText.recycle(mt);
                return text;
            }
            int left;
            avail -= paint.measureText(ellipsis);
            int right = len;
            if (avail < 0.0f) {
                left = 0;
            } else if (where == TruncateAt.START) {
                right = len - mt.breakText(len, false, avail);
                left = 0;
            } else if (where == TruncateAt.END || where == TruncateAt.END_SMALL) {
                left = mt.breakText(len, true, avail);
            } else {
                right = len - mt.breakText(len, false, avail / 2.0f);
                left = mt.breakText(right, true, avail - mt.measure(right, len));
            }
            if (callback != null) {
                callback.ellipsized(left, right);
            }
            char[] buf = mt.mChars;
            Spanned spanned = text instanceof Spanned ? (Spanned) text : null;
            int remaining = len - (right - left);
            if (preserveLength) {
                int left2;
                if (remaining > 0) {
                    left2 = left + FIRST_SPAN;
                    buf[left] = ellipsis.charAt(0);
                } else {
                    left2 = left;
                }
                for (int i = left2; i < right; i += FIRST_SPAN) {
                    buf[i] = ZWNBS_CHAR;
                }
                String str = new String(buf, 0, len);
                if (spanned == null) {
                    MeasuredText.recycle(mt);
                    return str;
                }
                SpannableString ss = new SpannableString(str);
                copySpansFrom(spanned, 0, len, Object.class, ss, 0);
                MeasuredText.recycle(mt);
                return ss;
            } else if (remaining == 0) {
                r5 = "";
                MeasuredText.recycle(mt);
                return r5;
            } else if (spanned == null) {
                StringBuilder stringBuilder = new StringBuilder(ellipsis.length() + remaining);
                stringBuilder.append(buf, 0, left);
                stringBuilder.append(ellipsis);
                stringBuilder.append(buf, right, len - right);
                r5 = stringBuilder.toString();
                MeasuredText.recycle(mt);
                return r5;
            } else {
                SpannableStringBuilder ssb = new SpannableStringBuilder();
                ssb.append(text, 0, left);
                ssb.append((CharSequence) ellipsis);
                ssb.append(text, right, len);
                MeasuredText.recycle(mt);
                return ssb;
            }
        } catch (Throwable th) {
            MeasuredText.recycle(mt);
        }
    }

    public static CharSequence commaEllipsize(CharSequence text, TextPaint p, float avail, String oneMore, String more) {
        return commaEllipsize(text, p, avail, oneMore, more, TextDirectionHeuristics.FIRSTSTRONG_LTR);
    }

    public static CharSequence commaEllipsize(CharSequence text, TextPaint p, float avail, String oneMore, String more, TextDirectionHeuristic textDir) {
        MeasuredText mt = MeasuredText.obtain();
        try {
            int len = text.length();
            if (setPara(mt, p, text, 0, len, textDir) <= avail) {
                return text;
            }
            int i;
            char[] buf = mt.mChars;
            int commaCount = 0;
            for (i = 0; i < len; i += FIRST_SPAN) {
                if (buf[i] == PhoneNumberUtils.PAUSE) {
                    commaCount += FIRST_SPAN;
                }
            }
            int remaining = commaCount + FIRST_SPAN;
            int ok = 0;
            String okFormat = "";
            int w = 0;
            int count = 0;
            float[] widths = mt.mWidths;
            MeasuredText tempMt = MeasuredText.obtain();
            for (i = 0; i < len; i += FIRST_SPAN) {
                w = (int) (((float) w) + widths[i]);
                if (buf[i] == PhoneNumberUtils.PAUSE) {
                    String format;
                    count += FIRST_SPAN;
                    remaining--;
                    if (remaining == FIRST_SPAN) {
                        format = " " + oneMore;
                    } else {
                        StringBuilder append = new StringBuilder().append(" ");
                        Object[] objArr = new Object[FIRST_SPAN];
                        objArr[0] = Integer.valueOf(remaining);
                        format = append.append(String.format(more, objArr)).toString();
                    }
                    tempMt.setPara(format, 0, format.length(), textDir, null);
                    if (((float) w) + tempMt.addStyleRun(p, tempMt.mLen, null) <= avail) {
                        ok = i + FIRST_SPAN;
                        okFormat = format;
                    }
                }
            }
            MeasuredText.recycle(tempMt);
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(okFormat);
            spannableStringBuilder.insert(0, text, 0, ok);
            MeasuredText.recycle(mt);
            return spannableStringBuilder;
        } finally {
            MeasuredText.recycle(mt);
        }
    }

    private static float setPara(MeasuredText mt, TextPaint paint, CharSequence text, int start, int end, TextDirectionHeuristic textDir) {
        mt.setPara(text, start, end, textDir, null);
        Spanned sp = text instanceof Spanned ? (Spanned) text : null;
        int len = end - start;
        if (sp == null) {
            return mt.addStyleRun(paint, len, null);
        }
        float width = 0.0f;
        int spanStart = 0;
        while (spanStart < len) {
            int spanEnd = sp.nextSpanTransition(spanStart, len, MetricAffectingSpan.class);
            width += mt.addStyleRun(paint, (MetricAffectingSpan[]) removeEmptySpans((MetricAffectingSpan[]) sp.getSpans(spanStart, spanEnd, MetricAffectingSpan.class), sp, MetricAffectingSpan.class), spanEnd - spanStart, null);
            spanStart = spanEnd;
        }
        return width;
    }

    static boolean doesNotNeedBidi(CharSequence s, int start, int end) {
        for (int i = start; i < end; i += FIRST_SPAN) {
            if (s.charAt(i) >= FIRST_RIGHT_TO_LEFT) {
                return false;
            }
        }
        return true;
    }

    static boolean doesNotNeedBidi(char[] text, int start, int len) {
        int e = start + len;
        for (int i = start; i < e; i += FIRST_SPAN) {
            if (text[i] >= FIRST_RIGHT_TO_LEFT) {
                return false;
            }
        }
        return true;
    }

    static char[] obtain(int len) {
        synchronized (sLock) {
            char[] buf = sTemp;
            sTemp = null;
        }
        if (buf == null || buf.length < len) {
            return ArrayUtils.newUnpaddedCharArray(len);
        }
        return buf;
    }

    static void recycle(char[] temp) {
        if (temp.length <= RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED) {
            synchronized (sLock) {
                sTemp = temp;
            }
        }
    }

    public static String htmlEncode(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i += FIRST_SPAN) {
            char c = s.charAt(i);
            switch (c) {
                case StatisticalConstant.TYPE_MULTIWINDOW_FRAME_SIZE_CHANGED /*34*/:
                    sb.append("&quot;");
                    break;
                case PerfHub.PERF_TAG_CTRL_TYPE_NEW /*38*/:
                    sb.append("&amp;");
                    break;
                case PerfHub.PERF_TAG_MAX /*39*/:
                    sb.append("&#39;");
                    break;
                case StatisticalConstant.TYPE_MEDIA_FOUNCTION_STATICS /*60*/:
                    sb.append("&lt;");
                    break;
                case RILConstants.RIL_REQUEST_SET_SUPP_SVC_NOTIFICATION /*62*/:
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    public static CharSequence concat(CharSequence... text) {
        if (text.length == 0) {
            return "";
        }
        if (text.length == FIRST_SPAN) {
            return text[0];
        }
        int i;
        boolean spanned = false;
        for (i = 0; i < text.length; i += FIRST_SPAN) {
            if (text[i] instanceof Spanned) {
                spanned = true;
                break;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (i = 0; i < text.length; i += FIRST_SPAN) {
            sb.append(text[i]);
        }
        if (!spanned) {
            return sb.toString();
        }
        SpannableString ss = new SpannableString(sb);
        int off = 0;
        for (i = 0; i < text.length; i += FIRST_SPAN) {
            int len = text[i].length();
            if (text[i] instanceof Spanned) {
                copySpansFrom((Spanned) text[i], 0, len, Object.class, ss, off);
            }
            off += len;
        }
        return new SpannedString(ss);
    }

    public static boolean isGraphic(CharSequence str) {
        int len = str.length();
        int i = 0;
        while (i < len) {
            int cp = Character.codePointAt(str, i);
            int gc = Character.getType(cp);
            if (gc != SUBSCRIPT_SPAN && gc != ABSOLUTE_SIZE_SPAN && gc != SUGGESTION_SPAN && gc != 0 && gc != TYPEFACE_SPAN && gc != SUPERSCRIPT_SPAN && gc != BACKGROUND_COLOR_SPAN) {
                return true;
            }
            i += Character.charCount(cp);
        }
        return false;
    }

    @Deprecated
    public static boolean isGraphic(char c) {
        int gc = Character.getType(c);
        if (gc == SUBSCRIPT_SPAN || gc == ABSOLUTE_SIZE_SPAN || gc == SUGGESTION_SPAN || gc == 0 || gc == TYPEFACE_SPAN || gc == SUPERSCRIPT_SPAN || gc == BACKGROUND_COLOR_SPAN) {
            return false;
        }
        return true;
    }

    public static boolean isDigitsOnly(CharSequence str) {
        int len = str.length();
        int i = 0;
        while (i < len) {
            int cp = Character.codePointAt(str, i);
            if (!Character.isDigit(cp)) {
                return false;
            }
            i += Character.charCount(cp);
        }
        return true;
    }

    public static boolean isPrintableAscii(char c) {
        if ((' ' <= c && c <= '~') || c == '\r' || c == '\n') {
            return true;
        }
        return false;
    }

    public static boolean isPrintableAsciiOnly(CharSequence str) {
        int len = str.length();
        for (int i = 0; i < len; i += FIRST_SPAN) {
            if (!isPrintableAscii(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static int getCapsMode(CharSequence cs, int off, int reqModes) {
        if (off < 0) {
            return 0;
        }
        int mode = 0;
        if ((reqModes & CAP_MODE_CHARACTERS) != 0) {
            mode = CAP_MODE_CHARACTERS;
        }
        if ((reqModes & 24576) == 0) {
            return mode;
        }
        int i = off;
        while (i > 0) {
            char c = cs.charAt(i - 1);
            if (c != '\"' && c != DateFormat.QUOTE && Character.getType(c) != SUGGESTION_RANGE_SPAN) {
                break;
            }
            i--;
        }
        int j = i;
        while (j > 0) {
            c = cs.charAt(j - 1);
            if (c != ' ' && c != '\t') {
                break;
            }
            j--;
        }
        if (j == 0 || cs.charAt(j - 1) == '\n') {
            return mode | CAP_MODE_WORDS;
        }
        if ((reqModes & CAP_MODE_SENTENCES) == 0) {
            if (i != j) {
                mode |= CAP_MODE_WORDS;
            }
            return mode;
        } else if (i == j) {
            return mode;
        } else {
            while (j > 0) {
                c = cs.charAt(j - 1);
                if (c != '\"' && c != DateFormat.QUOTE && Character.getType(c) != EASY_EDIT_SPAN) {
                    break;
                }
                j--;
            }
            if (j > 0) {
                c = cs.charAt(j - 1);
                if (c == '.' || c == '?' || c == '!') {
                    if (c == '.') {
                        for (int k = j - 2; k >= 0; k--) {
                            c = cs.charAt(k);
                            if (c == '.') {
                                return mode;
                            }
                            if (!Character.isLetter(c)) {
                                break;
                            }
                        }
                    }
                    return mode | CAP_MODE_SENTENCES;
                }
            }
            return mode;
        }
    }

    public static boolean delimitedStringContains(String delimitedString, char delimiter, String item) {
        if (isEmpty(delimitedString) || isEmpty(item)) {
            return false;
        }
        int pos = -1;
        int length = delimitedString.length();
        while (true) {
            pos = delimitedString.indexOf(item, pos + FIRST_SPAN);
            if (pos == -1) {
                return false;
            }
            if (pos <= 0 || delimitedString.charAt(pos - 1) == delimiter) {
                int expectedDelimiterPos = pos + item.length();
                if (expectedDelimiterPos == length || delimitedString.charAt(expectedDelimiterPos) == delimiter) {
                    return true;
                }
            }
        }
    }

    public static <T> T[] removeEmptySpans(T[] spans, Spanned spanned, Class<T> klass) {
        T[] copy = null;
        int count = 0;
        for (int i = 0; i < spans.length; i += FIRST_SPAN) {
            T span = spans[i];
            if (spanned.getSpanStart(span) == spanned.getSpanEnd(span)) {
                if (copy == null) {
                    copy = (Object[]) Array.newInstance(klass, spans.length - 1);
                    System.arraycopy(spans, 0, copy, 0, i);
                    count = i;
                }
            } else if (copy != null) {
                copy[count] = span;
                count += FIRST_SPAN;
            }
        }
        if (copy == null) {
            return spans;
        }
        Object[] result = (Object[]) Array.newInstance(klass, count);
        System.arraycopy(copy, 0, result, 0, count);
        return result;
    }

    public static long packRangeInLong(int start, int end) {
        return (((long) start) << 32) | ((long) end);
    }

    public static int unpackRangeStartFromLong(long range) {
        return (int) (range >>> 32);
    }

    public static int unpackRangeEndFromLong(long range) {
        return (int) (ExpandableListView.PACKED_POSITION_VALUE_NULL & range);
    }

    public static int getLayoutDirectionFromLocale(Locale locale) {
        if (!(locale == null || locale.equals(Locale.ROOT))) {
            if (!ULocale.forLocale(locale).isRightToLeft()) {
            }
            return FIRST_SPAN;
        }
        if (!SystemProperties.getBoolean("debug.force_rtl", false)) {
            return 0;
        }
        return FIRST_SPAN;
    }

    public static CharSequence formatSelectedCount(int count) {
        Resources system = Resources.getSystem();
        Object[] objArr = new Object[FIRST_SPAN];
        objArr[0] = Integer.valueOf(count);
        return system.getQuantityString(R.plurals.selected_count, count, objArr);
    }
}
