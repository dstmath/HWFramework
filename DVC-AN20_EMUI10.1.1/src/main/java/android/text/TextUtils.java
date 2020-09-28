package android.text;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Resources;
import android.icu.lang.UCharacter;
import android.icu.text.CaseMap;
import android.icu.text.Edits;
import android.icu.util.ULocale;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.sysprop.DisplayProperties;
import android.telecom.Logging.Session;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AccessibilityClickableSpan;
import android.text.style.AccessibilityURLSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.EasyEditSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.LineHeightSpan;
import android.text.style.LocaleSpan;
import android.text.style.ParagraphStyle;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ReplacementSpan;
import android.text.style.ScaleXSpan;
import android.text.style.SpellCheckSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuggestionRangeSpan;
import android.text.style.SuggestionSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TtsSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.text.style.UpdateAppearance;
import android.util.Log;
import android.util.Printer;
import com.android.internal.R;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class TextUtils {
    public static final int ABSOLUTE_SIZE_SPAN = 16;
    public static final int ACCESSIBILITY_CLICKABLE_SPAN = 25;
    public static final int ACCESSIBILITY_URL_SPAN = 26;
    public static final int ALIGNMENT_SPAN = 1;
    public static final int ANNOTATION = 18;
    public static final int BACKGROUND_COLOR_SPAN = 12;
    public static final int BULLET_SPAN = 8;
    public static final int CAP_MODE_CHARACTERS = 4096;
    public static final int CAP_MODE_SENTENCES = 16384;
    public static final int CAP_MODE_WORDS = 8192;
    public static final Parcelable.Creator<CharSequence> CHAR_SEQUENCE_CREATOR = new Parcelable.Creator<CharSequence>() {
        /* class android.text.TextUtils.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CharSequence createFromParcel(Parcel p) {
            int kind = p.readInt();
            String string = p.readString();
            if (string == null) {
                return null;
            }
            if (kind == 1) {
                return string;
            }
            SpannableString sp = new SpannableString(string);
            while (true) {
                int kind2 = p.readInt();
                if (kind2 == 0) {
                    return sp;
                }
                switch (kind2) {
                    case 1:
                        TextUtils.readSpan(p, sp, new AlignmentSpan.Standard(p));
                        break;
                    case 2:
                        TextUtils.readSpan(p, sp, new ForegroundColorSpan(p));
                        break;
                    case 3:
                        TextUtils.readSpan(p, sp, new RelativeSizeSpan(p));
                        break;
                    case 4:
                        TextUtils.readSpan(p, sp, new ScaleXSpan(p));
                        break;
                    case 5:
                        TextUtils.readSpan(p, sp, new StrikethroughSpan(p));
                        break;
                    case 6:
                        TextUtils.readSpan(p, sp, new UnderlineSpan(p));
                        break;
                    case 7:
                        TextUtils.readSpan(p, sp, new StyleSpan(p));
                        break;
                    case 8:
                        TextUtils.readSpan(p, sp, new BulletSpan(p));
                        break;
                    case 9:
                        TextUtils.readSpan(p, sp, new QuoteSpan(p));
                        break;
                    case 10:
                        TextUtils.readSpan(p, sp, new LeadingMarginSpan.Standard(p));
                        break;
                    case 11:
                        TextUtils.readSpan(p, sp, new URLSpan(p));
                        break;
                    case 12:
                        TextUtils.readSpan(p, sp, new BackgroundColorSpan(p));
                        break;
                    case 13:
                        TextUtils.readSpan(p, sp, new TypefaceSpan(p));
                        break;
                    case 14:
                        TextUtils.readSpan(p, sp, new SuperscriptSpan(p));
                        break;
                    case 15:
                        TextUtils.readSpan(p, sp, new SubscriptSpan(p));
                        break;
                    case 16:
                        TextUtils.readSpan(p, sp, new AbsoluteSizeSpan(p));
                        break;
                    case 17:
                        TextUtils.readSpan(p, sp, new TextAppearanceSpan(p));
                        break;
                    case 18:
                        TextUtils.readSpan(p, sp, new Annotation(p));
                        break;
                    case 19:
                        TextUtils.readSpan(p, sp, new SuggestionSpan(p));
                        break;
                    case 20:
                        TextUtils.readSpan(p, sp, new SpellCheckSpan(p));
                        break;
                    case 21:
                        TextUtils.readSpan(p, sp, new SuggestionRangeSpan(p));
                        break;
                    case 22:
                        TextUtils.readSpan(p, sp, new EasyEditSpan(p));
                        break;
                    case 23:
                        TextUtils.readSpan(p, sp, new LocaleSpan(p));
                        break;
                    case 24:
                        TextUtils.readSpan(p, sp, new TtsSpan(p));
                        break;
                    case 25:
                        TextUtils.readSpan(p, sp, new AccessibilityClickableSpan(p));
                        break;
                    case 26:
                        TextUtils.readSpan(p, sp, new AccessibilityURLSpan(p));
                        break;
                    case 27:
                        TextUtils.readSpan(p, sp, new LineBackgroundSpan.Standard(p));
                        break;
                    case 28:
                        TextUtils.readSpan(p, sp, new LineHeightSpan.Standard(p));
                        break;
                    default:
                        throw new RuntimeException("bogus span encoding " + kind2);
                }
            }
        }

        @Override // android.os.Parcelable.Creator
        public CharSequence[] newArray(int size) {
            return new CharSequence[size];
        }
    };
    public static final int EASY_EDIT_SPAN = 22;
    static final char ELLIPSIS_FILLER = 65279;
    private static final String ELLIPSIS_NORMAL = "…";
    private static final String ELLIPSIS_TWO_DOTS = "‥";
    private static String[] EMPTY_STRING_ARRAY = new String[0];
    public static final int FIRST_SPAN = 1;
    public static final int FOREGROUND_COLOR_SPAN = 2;
    public static final int LAST_SPAN = 28;
    public static final int LEADING_MARGIN_SPAN = 10;
    public static final int LINE_BACKGROUND_SPAN = 27;
    private static final int LINE_FEED_CODE_POINT = 10;
    public static final int LINE_HEIGHT_SPAN = 28;
    public static final int LOCALE_SPAN = 23;
    private static final int NBSP_CODE_POINT = 160;
    private static final int PARCEL_SAFE_TEXT_LENGTH = 100000;
    public static final int QUOTE_SPAN = 9;
    public static final int RELATIVE_SIZE_SPAN = 3;
    public static final int SAFE_STRING_FLAG_FIRST_LINE = 4;
    public static final int SAFE_STRING_FLAG_SINGLE_LINE = 2;
    public static final int SAFE_STRING_FLAG_TRIM = 1;
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
    private static Object sLock = new Object();
    private static char[] sTemp = null;

    public interface EllipsizeCallback {
        void ellipsized(int i, int i2);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SafeStringFlags {
    }

    public interface StringSplitter extends Iterable<String> {
        void setString(String str);
    }

    public enum TruncateAt {
        START,
        MIDDLE,
        END,
        MARQUEE,
        END_SMALL
    }

    public static String getEllipsisString(TruncateAt method) {
        return method == TruncateAt.END_SMALL ? ELLIPSIS_TWO_DOTS : ELLIPSIS_NORMAL;
    }

    private TextUtils() {
    }

    public static void getChars(CharSequence s, int start, int end, char[] dest, int destoff) {
        Class<?> cls = s.getClass();
        if (cls == String.class) {
            ((String) s).getChars(start, end, dest, destoff);
        } else if (cls == StringBuffer.class) {
            ((StringBuffer) s).getChars(start, end, dest, destoff);
        } else if (cls == StringBuilder.class) {
            ((StringBuilder) s).getChars(start, end, dest, destoff);
        } else if (s instanceof GetChars) {
            ((GetChars) s).getChars(start, end, dest, destoff);
        } else {
            int i = start;
            while (i < end) {
                dest[destoff] = s.charAt(i);
                i++;
                destoff++;
            }
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
        Class<?> cls = s.getClass();
        if ((s instanceof GetChars) || cls == StringBuffer.class || cls == StringBuilder.class || cls == String.class) {
            char[] temp = obtain(500);
            while (start < end) {
                int segend = start + 500;
                if (segend > end) {
                    segend = end;
                }
                getChars(s, start, segend, temp, 0);
                int count = segend - start;
                for (int i = 0; i < count; i++) {
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
        for (int i2 = start; i2 < end; i2++) {
            if (s.charAt(i2) == ch) {
                return i2;
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
        int end = last + 1;
        Class<?> cls = s.getClass();
        if ((s instanceof GetChars) || cls == StringBuffer.class || cls == StringBuilder.class || cls == String.class) {
            char[] temp = obtain(500);
            while (start < end) {
                int segstart = end - 500;
                if (segstart < start) {
                    segstart = start;
                }
                getChars(s, segstart, end, temp, 0);
                for (int i = (end - segstart) - 1; i >= 0; i--) {
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
        for (int i2 = end - 1; i2 >= start; i2--) {
            if (s.charAt(i2) == ch) {
                return i2;
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
            int start2 = indexOf(s, c, start);
            if (start2 > end - nlen || start2 < 0) {
                return -1;
            }
            if (regionMatches(s, start2, needle, 0, nlen)) {
                return start2;
            }
            start = start2 + 1;
        }
    }

    public static boolean regionMatches(CharSequence one, int toffset, CharSequence two, int ooffset, int len) {
        int tempLen = len * 2;
        if (tempLen >= len) {
            char[] temp = obtain(tempLen);
            getChars(one, toffset, toffset + len, temp, 0);
            getChars(two, ooffset, ooffset + len, temp, len);
            boolean match = true;
            int i = 0;
            while (true) {
                if (i >= len) {
                    break;
                } else if (temp[i] != temp[i + len]) {
                    match = false;
                    break;
                } else {
                    i++;
                }
            }
            recycle(temp);
            return match;
        }
        throw new IndexOutOfBoundsException();
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
        int length = tokens.length;
        if (length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(tokens[0]);
        for (int i = 1; i < length; i++) {
            sb.append(delimiter);
            sb.append(tokens[i]);
        }
        return sb.toString();
    }

    public static String join(CharSequence delimiter, Iterable tokens) {
        Iterator<?> it = tokens.iterator();
        if (!it.hasNext()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append(delimiter);
            sb.append(it.next());
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

    public static class SimpleStringSplitter implements StringSplitter, Iterator<String> {
        private char mDelimiter;
        private int mLength;
        private int mPosition;
        private String mString;

        public SimpleStringSplitter(char delimiter) {
            this.mDelimiter = delimiter;
        }

        @Override // android.text.TextUtils.StringSplitter
        public void setString(String string) {
            this.mString = string;
            this.mPosition = 0;
            this.mLength = this.mString.length();
        }

        @Override // java.lang.Iterable
        public Iterator<String> iterator() {
            return this;
        }

        public boolean hasNext() {
            return this.mPosition < this.mLength;
        }

        @Override // java.util.Iterator
        public String next() {
            int end = this.mString.indexOf(this.mDelimiter, this.mPosition);
            if (end == -1) {
                end = this.mLength;
            }
            String nextString = this.mString.substring(this.mPosition, end);
            this.mPosition = end + 1;
            return nextString;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
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
        return str == null || str.length() == 0;
    }

    public static String nullIfEmpty(String str) {
        if (isEmpty(str)) {
            return null;
        }
        return str;
    }

    public static String emptyIfNull(String str) {
        return str == null ? "" : str;
    }

    public static String firstNotEmpty(String a, String b) {
        return !isEmpty(a) ? a : (String) Preconditions.checkStringNotEmpty(b);
    }

    public static int length(String s) {
        if (s != null) {
            return s.length();
        }
        return 0;
    }

    public static String safeIntern(String s) {
        if (s != null) {
            return s.intern();
        }
        return null;
    }

    public static int getTrimmedLength(CharSequence s) {
        int len = s.length();
        int start = 0;
        while (start < len && s.charAt(start) <= ' ') {
            start++;
        }
        int end = len;
        while (end > start && s.charAt(end - 1) <= ' ') {
            end--;
        }
        return end - start;
    }

    public static boolean equals(CharSequence a, CharSequence b) {
        int length;
        if (a == b) {
            return true;
        }
        if (a == null || b == null || (length = a.length()) != b.length()) {
            return false;
        }
        if ((a instanceof String) && (b instanceof String)) {
            return a.equals(b);
        }
        for (int i = 0; i < length; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    @Deprecated
    public static CharSequence getReverse(CharSequence source, int start, int end) {
        return new Reverser(source, start, end);
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
            return (char) UCharacter.getMirror(this.mSource.charAt((this.mEnd - 1) - off));
        }

        @Override // android.text.GetChars
        public void getChars(int start, int end, char[] dest, int destoff) {
            CharSequence charSequence = this.mSource;
            int i = this.mStart;
            TextUtils.getChars(charSequence, start + i, i + end, dest, destoff);
            AndroidCharacter.mirror(dest, 0, end - start);
            int len = end - start;
            int n = (end - start) / 2;
            for (int i2 = 0; i2 < n; i2++) {
                char tmp = dest[destoff + i2];
                dest[destoff + i2] = dest[((destoff + len) - i2) - 1];
                dest[((destoff + len) - i2) - 1] = tmp;
            }
        }
    }

    public static void writeToParcel(CharSequence cs, Parcel p, int parcelableFlags) {
        if (cs instanceof Spanned) {
            p.writeInt(0);
            p.writeString(cs.toString());
            Spanned sp = (Spanned) cs;
            Object[] os = sp.getSpans(0, cs.length(), Object.class);
            for (int i = 0; i < os.length; i++) {
                Object o = os[i];
                Object prop = os[i];
                if (prop instanceof CharacterStyle) {
                    prop = ((CharacterStyle) prop).getUnderlying();
                }
                if (prop instanceof ParcelableSpan) {
                    ParcelableSpan ps = (ParcelableSpan) prop;
                    int spanTypeId = ps.getSpanTypeIdInternal();
                    if (spanTypeId < 1 || spanTypeId > 28) {
                        Log.e(TAG, "External class \"" + ps.getClass().getSimpleName() + "\" is attempting to use the frameworks-only ParcelableSpan interface");
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
        p.writeInt(1);
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
            for (Object o : os) {
                printer.println(prefix + ((Object) cs.subSequence(sp.getSpanStart(o), sp.getSpanEnd(o))) + ": " + Integer.toHexString(System.identityHashCode(o)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + o.getClass().getCanonicalName() + " (" + sp.getSpanStart(o) + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + sp.getSpanEnd(o) + ") fl=#" + sp.getSpanFlags(o));
            }
            return;
        }
        printer.println(prefix + ((Object) cs) + ": (no spans)");
    }

    public static CharSequence replace(CharSequence template, String[] sources, CharSequence[] destinations) {
        SpannableStringBuilder tb = new SpannableStringBuilder(template);
        for (int i = 0; i < sources.length; i++) {
            int where = indexOf(tb, sources[i]);
            if (where >= 0) {
                tb.setSpan(sources[i], where, sources[i].length() + where, 33);
            }
        }
        for (int i2 = 0; i2 < sources.length; i2++) {
            int start = tb.getSpanStart(sources[i2]);
            int end = tb.getSpanEnd(sources[i2]);
            if (start >= 0) {
                tb.replace(start, end, destinations[i2]);
            }
        }
        return tb;
    }

    public static CharSequence expandTemplate(CharSequence template, CharSequence... values) {
        if (values.length <= 9) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(template);
            int i = 0;
            while (i < ssb.length()) {
                try {
                    if (ssb.charAt(i) == '^') {
                        char next = ssb.charAt(i + 1);
                        if (next == '^') {
                            ssb.delete(i + 1, i + 2);
                            i++;
                        } else if (Character.isDigit(next)) {
                            int which = Character.getNumericValue(next) - 1;
                            if (which < 0) {
                                throw new IllegalArgumentException("template requests value ^" + (which + 1));
                            } else if (which < values.length) {
                                ssb.replace(i, i + 2, values[which]);
                                i += values[which].length();
                            } else {
                                throw new IllegalArgumentException("template requests value ^" + (which + 1) + "; only " + values.length + " provided");
                            }
                        }
                    }
                    i++;
                } catch (IndexOutOfBoundsException e) {
                }
            }
            return ssb;
        }
        throw new IllegalArgumentException("max of 9 values are supported");
    }

    public static int getOffsetBefore(CharSequence text, int offset) {
        int offset2;
        if (offset == 0 || offset == 1) {
            return 0;
        }
        char c = text.charAt(offset - 1);
        if (c < 56320 || c > 57343) {
            offset2 = offset - 1;
        } else {
            char c1 = text.charAt(offset - 2);
            offset2 = (c1 < 55296 || c1 > 56319) ? offset - 1 : offset - 2;
        }
        if (text instanceof Spanned) {
            ReplacementSpan[] spans = (ReplacementSpan[]) ((Spanned) text).getSpans(offset2, offset2, ReplacementSpan.class);
            for (int i = 0; i < spans.length; i++) {
                int start = ((Spanned) text).getSpanStart(spans[i]);
                int end = ((Spanned) text).getSpanEnd(spans[i]);
                if (start < offset2 && end > offset2) {
                    offset2 = start;
                }
            }
        }
        return offset2;
    }

    public static int getOffsetAfter(CharSequence text, int offset) {
        int offset2;
        int len = text.length();
        if (offset == len || offset == len - 1) {
            return len;
        }
        char c = text.charAt(offset);
        if (c < 55296 || c > 56319) {
            offset2 = offset + 1;
        } else {
            char c1 = text.charAt(offset + 1);
            if (c1 < 56320 || c1 > 57343) {
                offset2 = offset + 1;
            } else {
                offset2 = offset + 2;
            }
        }
        if (text instanceof Spanned) {
            ReplacementSpan[] spans = (ReplacementSpan[]) ((Spanned) text).getSpans(offset2, offset2, ReplacementSpan.class);
            for (int i = 0; i < spans.length; i++) {
                int start = ((Spanned) text).getSpanStart(spans[i]);
                int end = ((Spanned) text).getSpanEnd(spans[i]);
                if (start < offset2 && end > offset2) {
                    offset2 = end;
                }
            }
        }
        return offset2;
    }

    /* access modifiers changed from: private */
    public static void readSpan(Parcel p, Spannable sp, Object o) {
        sp.setSpan(o, p.readInt(), p.readInt(), p.readInt());
    }

    public static void copySpansFrom(Spanned source, int start, int end, Class kind, Spannable dest, int destoff) {
        if (kind == null) {
            kind = Object.class;
        }
        Object[] spans = source.getSpans(start, end, kind);
        for (int i = 0; i < spans.length; i++) {
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

    public static CharSequence toUpperCase(Locale locale, CharSequence source, boolean copySpans) {
        int destStart;
        int destEnd;
        Edits edits = new Edits();
        if (!copySpans) {
            return edits.hasChanges() ? (StringBuilder) CaseMap.toUpper().apply(locale, source, new StringBuilder(), edits) : source;
        }
        SpannableStringBuilder result = (SpannableStringBuilder) CaseMap.toUpper().apply(locale, source, new SpannableStringBuilder(), edits);
        if (!edits.hasChanges()) {
            return source;
        }
        Edits.Iterator iterator = edits.getFineIterator();
        int sourceLength = source.length();
        Spanned spanned = (Spanned) source;
        Object[] spans = spanned.getSpans(0, sourceLength, Object.class);
        for (Object span : spans) {
            int sourceStart = spanned.getSpanStart(span);
            int sourceEnd = spanned.getSpanEnd(span);
            int flags = spanned.getSpanFlags(span);
            if (sourceStart == sourceLength) {
                destStart = result.length();
            } else {
                destStart = toUpperMapToDest(iterator, sourceStart);
            }
            if (sourceEnd == sourceLength) {
                destEnd = result.length();
            } else {
                destEnd = toUpperMapToDest(iterator, sourceEnd);
            }
            result.setSpan(span, destStart, destEnd, flags);
        }
        return result;
    }

    private static int toUpperMapToDest(Edits.Iterator iterator, int sourceIndex) {
        iterator.findSourceIndex(sourceIndex);
        if (sourceIndex == iterator.sourceIndex()) {
            return iterator.destinationIndex();
        }
        if (iterator.hasChange()) {
            return iterator.destinationIndex() + iterator.newLength();
        }
        return iterator.destinationIndex() + (sourceIndex - iterator.sourceIndex());
    }

    public static CharSequence ellipsize(CharSequence text, TextPaint p, float avail, TruncateAt where) {
        return ellipsize(text, p, avail, where, false, null);
    }

    public static CharSequence ellipsize(CharSequence text, TextPaint paint, float avail, TruncateAt where, boolean preserveLength, EllipsizeCallback callback) {
        return ellipsize(text, paint, avail, where, preserveLength, callback, TextDirectionHeuristics.FIRSTSTRONG_LTR, getEllipsisString(where));
    }

    /* JADX WARNING: Removed duplicated region for block: B:67:0x0143  */
    public static CharSequence ellipsize(CharSequence text, TextPaint paint, float avail, TruncateAt where, boolean preserveLength, EllipsizeCallback callback, TextDirectionHeuristic textDir, String ellipsis) {
        int right;
        int left;
        int len = text.length();
        MeasuredParagraph mt = null;
        try {
            mt = MeasuredParagraph.buildForMeasurement(paint, text, 0, text.length(), textDir, null);
            if (mt.getWholeWidth() <= avail) {
                if (callback != null) {
                    callback.ellipsized(0, 0);
                }
                mt.recycle();
                return text;
            }
            try {
                float avail2 = avail - paint.measureText(ellipsis);
                int left2 = 0;
                if (avail2 < 0.0f) {
                    right = len;
                } else {
                    try {
                        if (where == TruncateAt.START) {
                            right = len - mt.breakText(len, false, avail2);
                        } else {
                            if (where != TruncateAt.END) {
                                if (where != TruncateAt.END_SMALL) {
                                    int right2 = len - mt.breakText(len, false, avail2 / 2.0f);
                                    left2 = mt.breakText(right2, true, avail2 - mt.measure(right2, len));
                                    right = right2;
                                }
                            }
                            left2 = mt.breakText(len, true, avail2);
                            right = len;
                        }
                    } catch (Throwable th) {
                        th = th;
                        if (mt != null) {
                        }
                        throw th;
                    }
                }
                if (callback != null) {
                    try {
                        callback.ellipsized(left2, right);
                    } catch (Throwable th2) {
                        th = th2;
                        if (mt != null) {
                            mt.recycle();
                        }
                        throw th;
                    }
                }
                char[] buf = mt.getChars();
                Spanned sp = text instanceof Spanned ? (Spanned) text : null;
                int removed = right - left2;
                int remaining = len - removed;
                if (preserveLength) {
                    if (remaining <= 0 || removed < ellipsis.length()) {
                        left = left2;
                    } else {
                        ellipsis.getChars(0, ellipsis.length(), buf, left2);
                        left = left2 + ellipsis.length();
                    }
                    for (int i = left; i < right; i++) {
                        buf[i] = ELLIPSIS_FILLER;
                    }
                    String s = new String(buf, 0, len);
                    if (sp == null) {
                        mt.recycle();
                        return s;
                    }
                    SpannableString ss = new SpannableString(s);
                    copySpansFrom(sp, 0, len, Object.class, ss, 0);
                    mt.recycle();
                    return ss;
                } else if (remaining == 0) {
                    mt.recycle();
                    return "";
                } else if (sp == null) {
                    StringBuilder sb = new StringBuilder(remaining + ellipsis.length());
                    sb.append(buf, 0, left2);
                    sb.append(ellipsis);
                    sb.append(buf, right, len - right);
                    String sb2 = sb.toString();
                    mt.recycle();
                    return sb2;
                } else {
                    SpannableStringBuilder ssb = new SpannableStringBuilder();
                    ssb.append(text, 0, left2);
                    ssb.append((CharSequence) ellipsis);
                    ssb.append(text, right, len);
                    mt.recycle();
                    return ssb;
                }
            } catch (Throwable th3) {
                th = th3;
                if (mt != null) {
                }
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            if (mt != null) {
            }
            throw th;
        }
    }

    public static CharSequence listEllipsize(Context context, List<CharSequence> elements, String separator, TextPaint paint, float avail, int moreId) {
        int totalLen;
        BidiFormatter bidiFormatter;
        Resources res;
        CharSequence morePiece;
        if (elements == null || (totalLen = elements.size()) == 0) {
            return "";
        }
        if (context == null) {
            res = null;
            bidiFormatter = BidiFormatter.getInstance();
        } else {
            res = context.getResources();
            bidiFormatter = BidiFormatter.getInstance(res.getConfiguration().getLocales().get(0));
        }
        SpannableStringBuilder output = new SpannableStringBuilder();
        int[] endIndexes = new int[totalLen];
        for (int i = 0; i < totalLen; i++) {
            output.append(bidiFormatter.unicodeWrap(elements.get(i)));
            if (i != totalLen - 1) {
                output.append((CharSequence) separator);
            }
            endIndexes[i] = output.length();
        }
        for (int i2 = totalLen - 1; i2 >= 0; i2--) {
            output.delete(endIndexes[i2], output.length());
            int remainingElements = (totalLen - i2) - 1;
            if (remainingElements > 0) {
                if (res == null) {
                    morePiece = ELLIPSIS_NORMAL;
                } else {
                    morePiece = res.getQuantityString(moreId, remainingElements, Integer.valueOf(remainingElements));
                }
                output.append(bidiFormatter.unicodeWrap(morePiece));
            }
            if (paint.measureText(output, 0, output.length()) <= avail) {
                return output;
            }
        }
        return "";
    }

    @Deprecated
    public static CharSequence commaEllipsize(CharSequence text, TextPaint p, float avail, String oneMore, String more) {
        return commaEllipsize(text, p, avail, oneMore, more, TextDirectionHeuristics.FIRSTSTRONG_LTR);
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x00f4  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00f9  */
    @Deprecated
    public static CharSequence commaEllipsize(CharSequence text, TextPaint p, float avail, String oneMore, String more, TextDirectionHeuristic textDir) {
        char c;
        float width;
        int len;
        String format;
        MeasuredParagraph mt = null;
        MeasuredParagraph tempMt = null;
        try {
            int len2 = text.length();
            mt = MeasuredParagraph.buildForMeasurement(p, text, 0, len2, textDir, null);
            float width2 = mt.getWholeWidth();
            if (width2 <= avail) {
                mt.recycle();
                if (0 != 0) {
                    tempMt.recycle();
                }
                return text;
            }
            char[] buf = mt.getChars();
            int commaCount = 0;
            int i = 0;
            while (true) {
                c = ',';
                if (i >= len2) {
                    break;
                }
                if (buf[i] == ',') {
                    commaCount++;
                }
                i++;
            }
            int remaining = commaCount + 1;
            int ok = 0;
            String okFormat = "";
            int w = 0;
            int count = 0;
            float[] widths = mt.getWidths().getRawArray();
            int i2 = 0;
            while (i2 < len2) {
                w = (int) (((float) w) + widths[i2]);
                if (buf[i2] == c) {
                    count++;
                    remaining--;
                    if (remaining == 1) {
                        try {
                            StringBuilder sb = new StringBuilder();
                            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                            sb.append(oneMore);
                            format = sb.toString();
                            width = width2;
                        } catch (Throwable th) {
                            th = th;
                            if (mt != null) {
                                mt.recycle();
                            }
                            if (tempMt != null) {
                                tempMt.recycle();
                            }
                            throw th;
                        }
                    } else {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                        width = width2;
                        try {
                            sb2.append(String.format(more, Integer.valueOf(remaining)));
                            format = sb2.toString();
                        } catch (Throwable th2) {
                            th = th2;
                            if (mt != null) {
                            }
                            if (tempMt != null) {
                            }
                            throw th;
                        }
                    }
                    tempMt = MeasuredParagraph.buildForMeasurement(p, format, 0, format.length(), textDir, tempMt);
                    len = len2;
                    if (((float) w) + tempMt.getWholeWidth() <= avail) {
                        okFormat = format;
                        ok = i2 + 1;
                    }
                } else {
                    len = len2;
                    width = width2;
                }
                i2++;
                len2 = len;
                width2 = width;
                c = ',';
            }
            SpannableStringBuilder out = new SpannableStringBuilder(okFormat);
            out.insert(0, text, 0, ok);
            mt.recycle();
            if (tempMt != null) {
                tempMt.recycle();
            }
            return out;
        } catch (Throwable th3) {
            th = th3;
            if (mt != null) {
            }
            if (tempMt != null) {
            }
            throw th;
        }
    }

    static boolean couldAffectRtl(char c) {
        return (1424 <= c && c <= 2303) || c == 8206 || c == 8207 || (8234 <= c && c <= 8238) || ((8294 <= c && c <= 8297) || ((55296 <= c && c <= 57343) || ((64285 <= c && c <= 65023) || (65136 <= c && c <= 65278))));
    }

    static boolean doesNotNeedBidi(char[] text, int start, int len) {
        int end = start + len;
        for (int i = start; i < end; i++) {
            if (couldAffectRtl(text[i])) {
                return false;
            }
        }
        return true;
    }

    static char[] obtain(int len) {
        char[] buf;
        synchronized (sLock) {
            buf = sTemp;
            sTemp = null;
        }
        if (buf == null || buf.length < len) {
            return ArrayUtils.newUnpaddedCharArray(len);
        }
        return buf;
    }

    static void recycle(char[] temp) {
        if (temp.length <= 1000) {
            synchronized (sLock) {
                sTemp = temp;
            }
        }
    }

    public static String htmlEncode(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\"') {
                sb.append("&quot;");
            } else if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '&') {
                sb.append("&amp;");
            } else if (c != '\'') {
                sb.append(c);
            } else {
                sb.append("&#39;");
            }
        }
        return sb.toString();
    }

    public static CharSequence concat(CharSequence... text) {
        if (text.length == 0) {
            return "";
        }
        int i = 0;
        if (text.length == 1) {
            return text[0];
        }
        boolean spanned = false;
        int length = text.length;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                break;
            } else if (text[i2] instanceof Spanned) {
                spanned = true;
                break;
            } else {
                i2++;
            }
        }
        if (spanned) {
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            int length2 = text.length;
            while (i < length2) {
                CharSequence piece = text[i];
                ssb.append(piece == null ? "null" : piece);
                i++;
            }
            return new SpannedString(ssb);
        }
        StringBuilder sb = new StringBuilder();
        int length3 = text.length;
        while (i < length3) {
            sb.append(text[i]);
            i++;
        }
        return sb.toString();
    }

    public static boolean isGraphic(CharSequence str) {
        int len = str.length();
        int i = 0;
        while (i < len) {
            int cp = Character.codePointAt(str, i);
            int gc = Character.getType(cp);
            if (gc != 15 && gc != 16 && gc != 19 && gc != 0 && gc != 13 && gc != 14 && gc != 12) {
                return true;
            }
            i += Character.charCount(cp);
        }
        return false;
    }

    @Deprecated
    public static boolean isGraphic(char c) {
        int gc = Character.getType(c);
        return (gc == 15 || gc == 16 || gc == 19 || gc == 0 || gc == 13 || gc == 14 || gc == 12) ? false : true;
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
        return (' ' <= c && c <= '~') || c == '\r' || c == '\n';
    }

    @UnsupportedAppUsage
    public static boolean isPrintableAsciiOnly(CharSequence str) {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (!isPrintableAscii(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static int getCapsMode(CharSequence cs, int off, int reqModes) {
        char c;
        if (off < 0) {
            return 0;
        }
        int mode = 0;
        if ((reqModes & 4096) != 0) {
            mode = 0 | 4096;
        }
        if ((reqModes & 24576) == 0) {
            return mode;
        }
        int i = off;
        while (i > 0 && ((c = cs.charAt(i - 1)) == '\"' || c == '\'' || Character.getType(c) == 21)) {
            i--;
        }
        int j = i;
        while (j > 0) {
            char c2 = cs.charAt(j - 1);
            if (!(c2 == ' ' || c2 == '\t')) {
                break;
            }
            j--;
        }
        if (j == 0 || cs.charAt(j - 1) == '\n') {
            return mode | 8192;
        }
        if ((reqModes & 16384) == 0) {
            if (i != j) {
                return mode | 8192;
            }
            return mode;
        } else if (i == j) {
            return mode;
        } else {
            while (j > 0) {
                char c3 = cs.charAt(j - 1);
                if (!(c3 == '\"' || c3 == '\'' || Character.getType(c3) == 22)) {
                    break;
                }
                j--;
            }
            if (j <= 0 || !((c = cs.charAt(j - 1)) == '.' || c == '?' || c == '!')) {
                return mode;
            }
            if (c == '.') {
                for (int k = j - 2; k >= 0; k--) {
                    char c4 = cs.charAt(k);
                    if (c4 == '.') {
                        return mode;
                    }
                    if (!Character.isLetter(c4)) {
                        break;
                    }
                }
            }
            return mode | 16384;
        }
    }

    public static boolean delimitedStringContains(String delimitedString, char delimiter, String item) {
        int expectedDelimiterPos;
        if (isEmpty(delimitedString) || isEmpty(item)) {
            return false;
        }
        int pos = -1;
        int length = delimitedString.length();
        while (true) {
            int indexOf = delimitedString.indexOf(item, pos + 1);
            pos = indexOf;
            if (indexOf == -1) {
                return false;
            }
            if ((pos <= 0 || delimitedString.charAt(pos - 1) == delimiter) && ((expectedDelimiterPos = item.length() + pos) == length || delimitedString.charAt(expectedDelimiterPos) == delimiter)) {
                return true;
            }
        }
    }

    public static <T> T[] removeEmptySpans(T[] spans, Spanned spanned, Class<T> klass) {
        Object[] objArr = null;
        int count = 0;
        for (int i = 0; i < spans.length; i++) {
            T span = spans[i];
            if (spanned.getSpanStart(span) == spanned.getSpanEnd(span)) {
                if (objArr == null) {
                    objArr = (Object[]) Array.newInstance((Class<?>) klass, spans.length - 1);
                    System.arraycopy(spans, 0, objArr, 0, i);
                    count = i;
                }
            } else if (objArr != null) {
                objArr[count] = span;
                count++;
            }
        }
        if (objArr == null) {
            return spans;
        }
        T[] result = (T[]) ((Object[]) Array.newInstance((Class<?>) klass, count));
        System.arraycopy(objArr, 0, result, 0, count);
        return result;
    }

    @UnsupportedAppUsage
    public static long packRangeInLong(int start, int end) {
        return (((long) start) << 32) | ((long) end);
    }

    @UnsupportedAppUsage
    public static int unpackRangeStartFromLong(long range) {
        return (int) (range >>> 32);
    }

    @UnsupportedAppUsage
    public static int unpackRangeEndFromLong(long range) {
        return (int) (4294967295L & range);
    }

    public static int getLayoutDirectionFromLocale(Locale locale) {
        if ((locale == null || locale.equals(Locale.ROOT) || !ULocale.forLocale(locale).isRightToLeft()) && !DisplayProperties.debug_force_rtl().orElse(false).booleanValue()) {
            return 0;
        }
        return 1;
    }

    public static CharSequence formatSelectedCount(int count) {
        return Resources.getSystem().getQuantityString(R.plurals.selected_count, count, Integer.valueOf(count));
    }

    public static boolean hasStyleSpan(Spanned spanned) {
        Preconditions.checkArgument(spanned != null);
        for (Class<?> clazz : new Class[]{CharacterStyle.class, ParagraphStyle.class, UpdateAppearance.class}) {
            if (spanned.nextSpanTransition(-1, spanned.length(), clazz) < spanned.length()) {
                return true;
            }
        }
        return false;
    }

    public static CharSequence trimNoCopySpans(CharSequence charSequence) {
        if (charSequence == null || !(charSequence instanceof Spanned)) {
            return charSequence;
        }
        return new SpannableStringBuilder(charSequence);
    }

    public static void wrap(StringBuilder builder, String start, String end) {
        builder.insert(0, start);
        builder.append(end);
    }

    public static <T extends CharSequence> T trimToParcelableSize(T text) {
        return (T) trimToSize(text, 100000);
    }

    public static <T extends CharSequence> T trimToSize(T text, int size) {
        Preconditions.checkArgument(size > 0);
        if (isEmpty(text) || text.length() <= size) {
            return text;
        }
        if (Character.isHighSurrogate(text.charAt(size - 1)) && Character.isLowSurrogate(text.charAt(size))) {
            size--;
        }
        return (T) text.subSequence(0, size);
    }

    public static <T extends CharSequence> T trimToLengthWithEllipsis(T text, int size) {
        T trimmed = (T) trimToSize(text, size);
        if (trimmed.length() >= text.length()) {
            return trimmed;
        }
        return trimmed.toString() + Session.TRUNCATE_STRING;
    }

    private static boolean isNewline(int codePoint) {
        int type = Character.getType(codePoint);
        return type == 14 || type == 13 || codePoint == 10;
    }

    private static boolean isWhiteSpace(int codePoint) {
        return Character.isWhitespace(codePoint) || codePoint == 160;
    }

    public static String withoutPrefix(String prefix, String str) {
        if (prefix == null || str == null) {
            return str;
        }
        return str.startsWith(prefix) ? str.substring(prefix.length()) : str;
    }

    public static CharSequence makeSafeForPresentation(String unclean, int maxCharactersToConsider, float ellipsizeDip, int flags) {
        String shortString;
        boolean z = true;
        boolean onlyKeepFirstLine = (flags & 4) != 0;
        boolean forceSingleLine = (flags & 2) != 0;
        boolean trim = (flags & 1) != 0;
        Preconditions.checkNotNull(unclean);
        Preconditions.checkArgumentNonnegative(maxCharactersToConsider);
        Preconditions.checkArgumentNonNegative(ellipsizeDip, "ellipsizeDip");
        Preconditions.checkFlagsArgument(flags, 7);
        if (onlyKeepFirstLine && forceSingleLine) {
            z = false;
        }
        Preconditions.checkArgument(z, "Cannot set SAFE_STRING_FLAG_SINGLE_LINE and SAFE_STRING_FLAG_FIRST_LINE at thesame time");
        if (maxCharactersToConsider > 0) {
            shortString = unclean.substring(0, Math.min(unclean.length(), maxCharactersToConsider));
        } else {
            shortString = unclean;
        }
        StringWithRemovedChars gettingCleaned = new StringWithRemovedChars(Html.fromHtml(shortString).toString());
        int firstNonWhiteSpace = -1;
        int firstTrailingWhiteSpace = -1;
        int uncleanLength = gettingCleaned.length();
        int offset = 0;
        while (true) {
            if (offset >= uncleanLength) {
                break;
            }
            int codePoint = gettingCleaned.codePointAt(offset);
            int type = Character.getType(codePoint);
            int codePointLen = Character.charCount(codePoint);
            boolean isNewline = isNewline(codePoint);
            if (onlyKeepFirstLine && isNewline) {
                gettingCleaned.removeAllCharAfter(offset);
                break;
            }
            if (forceSingleLine && isNewline) {
                gettingCleaned.removeRange(offset, offset + codePointLen);
            } else if (type == 15 && !isNewline) {
                gettingCleaned.removeRange(offset, offset + codePointLen);
            } else if (trim && !isWhiteSpace(codePoint)) {
                if (firstNonWhiteSpace == -1) {
                    firstNonWhiteSpace = offset;
                }
                firstTrailingWhiteSpace = offset + codePointLen;
            }
            offset += codePointLen;
        }
        if (trim) {
            if (firstNonWhiteSpace == -1) {
                gettingCleaned.removeAllCharAfter(0);
            } else {
                if (firstNonWhiteSpace > 0) {
                    gettingCleaned.removeAllCharBefore(firstNonWhiteSpace);
                }
                if (firstTrailingWhiteSpace < uncleanLength) {
                    gettingCleaned.removeAllCharAfter(firstTrailingWhiteSpace);
                }
            }
        }
        if (ellipsizeDip == 0.0f) {
            return gettingCleaned.toString();
        }
        TextPaint paint = new TextPaint();
        paint.setTextSize(42.0f);
        return ellipsize(gettingCleaned.toString(), paint, ellipsizeDip, TruncateAt.END);
    }

    private static class StringWithRemovedChars {
        private final String mOriginal;
        private BitSet mRemovedChars;

        StringWithRemovedChars(String original) {
            this.mOriginal = original;
        }

        /* access modifiers changed from: package-private */
        public void removeRange(int firstRemoved, int firstNonRemoved) {
            if (this.mRemovedChars == null) {
                this.mRemovedChars = new BitSet(this.mOriginal.length());
            }
            this.mRemovedChars.set(firstRemoved, firstNonRemoved);
        }

        /* access modifiers changed from: package-private */
        public void removeAllCharBefore(int firstNonRemoved) {
            if (this.mRemovedChars == null) {
                this.mRemovedChars = new BitSet(this.mOriginal.length());
            }
            this.mRemovedChars.set(0, firstNonRemoved);
        }

        /* access modifiers changed from: package-private */
        public void removeAllCharAfter(int firstRemoved) {
            if (this.mRemovedChars == null) {
                this.mRemovedChars = new BitSet(this.mOriginal.length());
            }
            this.mRemovedChars.set(firstRemoved, this.mOriginal.length());
        }

        public String toString() {
            if (this.mRemovedChars == null) {
                return this.mOriginal;
            }
            StringBuilder sb = new StringBuilder(this.mOriginal.length());
            for (int i = 0; i < this.mOriginal.length(); i++) {
                if (!this.mRemovedChars.get(i)) {
                    sb.append(this.mOriginal.charAt(i));
                }
            }
            return sb.toString();
        }

        /* access modifiers changed from: package-private */
        public int length() {
            return this.mOriginal.length();
        }

        /* access modifiers changed from: package-private */
        public int codePointAt(int offset) {
            return this.mOriginal.codePointAt(offset);
        }
    }
}
