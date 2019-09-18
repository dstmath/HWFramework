package android.text;

import android.net.wifi.WifiEnterpriseConfig;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.lang.reflect.Array;
import libcore.util.EmptyArray;

abstract class SpannableStringInternal {
    private static final int COLUMNS = 3;
    static final Object[] EMPTY = new Object[0];
    private static final int END = 1;
    private static final int FLAGS = 2;
    private static final int START = 0;
    private int mSpanCount;
    private int[] mSpanData;
    private Object[] mSpans;
    private String mText;

    SpannableStringInternal(CharSequence source, int start, int end, boolean ignoreNoCopySpan) {
        if (start == 0 && end == source.length()) {
            this.mText = source.toString();
        } else {
            this.mText = source.toString().substring(start, end);
        }
        this.mSpans = EmptyArray.OBJECT;
        this.mSpanData = EmptyArray.INT;
        if (!(source instanceof Spanned)) {
            return;
        }
        if (source instanceof SpannableStringInternal) {
            copySpans((SpannableStringInternal) source, start, end, ignoreNoCopySpan);
        } else {
            copySpans((Spanned) source, start, end, ignoreNoCopySpan);
        }
    }

    SpannableStringInternal(CharSequence source, int start, int end) {
        this(source, start, end, false);
    }

    private void copySpans(Spanned src, int start, int end, boolean ignoreNoCopySpan) {
        Object[] spans = src.getSpans(start, end, Object.class);
        for (int i = 0; i < spans.length; i++) {
            if (!ignoreNoCopySpan || !(spans[i] instanceof NoCopySpan)) {
                int st = src.getSpanStart(spans[i]);
                int en = src.getSpanEnd(spans[i]);
                int fl = src.getSpanFlags(spans[i]);
                if (st < start) {
                    st = start;
                }
                if (en > end) {
                    en = end;
                }
                setSpan(spans[i], st - start, en - start, fl, false);
            }
        }
    }

    private void copySpans(SpannableStringInternal src, int start, int end, boolean ignoreNoCopySpan) {
        SpannableStringInternal spannableStringInternal = src;
        int i = start;
        int i2 = end;
        int[] srcData = spannableStringInternal.mSpanData;
        Object[] srcSpans = spannableStringInternal.mSpans;
        int limit = spannableStringInternal.mSpanCount;
        boolean hasNoCopySpan = false;
        int count = 0;
        for (int i3 = 0; i3 < limit; i3++) {
            if (!isOutOfCopyRange(i, i2, srcData[(i3 * 3) + 0], srcData[(i3 * 3) + 1])) {
                if (srcSpans[i3] instanceof NoCopySpan) {
                    hasNoCopySpan = true;
                    if (ignoreNoCopySpan) {
                    }
                }
                count++;
            }
        }
        if (count != 0) {
            if (!hasNoCopySpan && i == 0 && i2 == src.length()) {
                this.mSpans = ArrayUtils.newUnpaddedObjectArray(spannableStringInternal.mSpans.length);
                this.mSpanData = new int[spannableStringInternal.mSpanData.length];
                this.mSpanCount = spannableStringInternal.mSpanCount;
                System.arraycopy(spannableStringInternal.mSpans, 0, this.mSpans, 0, spannableStringInternal.mSpans.length);
                System.arraycopy(spannableStringInternal.mSpanData, 0, this.mSpanData, 0, this.mSpanData.length);
            } else {
                this.mSpanCount = count;
                this.mSpans = ArrayUtils.newUnpaddedObjectArray(this.mSpanCount);
                this.mSpanData = new int[(this.mSpans.length * 3)];
                int j = 0;
                for (int i4 = 0; i4 < limit; i4++) {
                    int spanStart = srcData[(i4 * 3) + 0];
                    int spanEnd = srcData[(i4 * 3) + 1];
                    if (!isOutOfCopyRange(i, i2, spanStart, spanEnd) && (!ignoreNoCopySpan || !(srcSpans[i4] instanceof NoCopySpan))) {
                        if (spanStart < i) {
                            spanStart = i;
                        }
                        if (spanEnd > i2) {
                            spanEnd = i2;
                        }
                        this.mSpans[j] = srcSpans[i4];
                        this.mSpanData[(j * 3) + 0] = spanStart - i;
                        this.mSpanData[(j * 3) + 1] = spanEnd - i;
                        this.mSpanData[(j * 3) + 2] = srcData[(i4 * 3) + 2];
                        j++;
                    }
                }
            }
        }
    }

    private final boolean isOutOfCopyRange(int start, int end, int spanStart, int spanEnd) {
        if (spanStart > end || spanEnd < start) {
            return true;
        }
        if (spanStart == spanEnd || start == end || (spanStart != end && spanEnd != start)) {
            return false;
        }
        return true;
    }

    public final int length() {
        return this.mText.length();
    }

    public final char charAt(int i) {
        return this.mText.charAt(i);
    }

    public final String toString() {
        return this.mText;
    }

    public final void getChars(int start, int end, char[] dest, int off) {
        this.mText.getChars(start, end, dest, off);
    }

    /* access modifiers changed from: package-private */
    public void setSpan(Object what, int start, int end, int flags) {
        setSpan(what, start, end, flags, true);
    }

    private boolean isIndexFollowsNextLine(int index) {
        return (index == 0 || index == length() || charAt(index + -1) == 10) ? false : true;
    }

    private void setSpan(Object what, int start, int end, int flags, boolean enforceParagraph) {
        Object obj = what;
        int i = start;
        int i2 = end;
        int nstart = i;
        int nend = i2;
        checkRange("setSpan", i, i2);
        if ((flags & 51) == 51) {
            if (isIndexFollowsNextLine(i)) {
                if (enforceParagraph) {
                    throw new RuntimeException("PARAGRAPH span must start at paragraph boundary (" + i + " follows " + charAt(i - 1) + ")");
                }
                return;
            } else if (isIndexFollowsNextLine(i2)) {
                if (enforceParagraph) {
                    throw new RuntimeException("PARAGRAPH span must end at paragraph boundary (" + i2 + " follows " + charAt(i2 - 1) + ")");
                }
                return;
            }
        }
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        int i3 = 0;
        while (true) {
            int i4 = i3;
            if (i4 >= count) {
                if (this.mSpanCount + 1 >= this.mSpans.length) {
                    Object[] newtags = ArrayUtils.newUnpaddedObjectArray(GrowingArrayUtils.growSize(this.mSpanCount));
                    int[] newdata = new int[(newtags.length * 3)];
                    System.arraycopy(this.mSpans, 0, newtags, 0, this.mSpanCount);
                    System.arraycopy(this.mSpanData, 0, newdata, 0, this.mSpanCount * 3);
                    this.mSpans = newtags;
                    this.mSpanData = newdata;
                }
                this.mSpans[this.mSpanCount] = obj;
                this.mSpanData[(this.mSpanCount * 3) + 0] = i;
                this.mSpanData[(this.mSpanCount * 3) + 1] = i2;
                this.mSpanData[(this.mSpanCount * 3) + 2] = flags;
                this.mSpanCount++;
                if (this instanceof Spannable) {
                    sendSpanAdded(obj, nstart, nend);
                }
                return;
            } else if (spans[i4] == obj) {
                int ostart = data[(i4 * 3) + 0];
                int oend = data[(i4 * 3) + 1];
                data[(i4 * 3) + 0] = i;
                data[(i4 * 3) + 1] = i2;
                data[(i4 * 3) + 2] = flags;
                int i5 = i4;
                int[] iArr = data;
                sendSpanChanged(obj, ostart, oend, nstart, nend);
                return;
            } else {
                int[] iArr2 = data;
                i3 = i4 + 1;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeSpan(Object what) {
        removeSpan(what, 0);
    }

    public void removeSpan(Object what, int flags) {
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        for (int i = count - 1; i >= 0; i--) {
            if (spans[i] == what) {
                int ostart = data[(i * 3) + 0];
                int oend = data[(i * 3) + 1];
                int c = count - (i + 1);
                System.arraycopy(spans, i + 1, spans, i, c);
                System.arraycopy(data, (i + 1) * 3, data, i * 3, c * 3);
                this.mSpanCount--;
                if ((flags & 512) == 0) {
                    sendSpanRemoved(what, ostart, oend);
                }
                return;
            }
        }
    }

    public int getSpanStart(Object what) {
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        for (int i = count - 1; i >= 0; i--) {
            if (spans[i] == what) {
                return data[(i * 3) + 0];
            }
        }
        return -1;
    }

    public int getSpanEnd(Object what) {
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        for (int i = count - 1; i >= 0; i--) {
            if (spans[i] == what) {
                return data[(i * 3) + 1];
            }
        }
        return -1;
    }

    public int getSpanFlags(Object what) {
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        for (int i = count - 1; i >= 0; i--) {
            if (spans[i] == what) {
                return data[(i * 3) + 2];
            }
        }
        return 0;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v5, resolved type: T[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    public <T> T[] getSpans(int queryStart, int queryEnd, Class<T> kind) {
        int j;
        int i = queryStart;
        int i2 = queryEnd;
        Class<T> cls = kind;
        int spanCount = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        int i3 = 0;
        Object ret1 = null;
        Object[] ret = null;
        int count = 0;
        int i4 = 0;
        while (i4 < spanCount) {
            int spanStart = data[(i4 * 3) + i3];
            int spanEnd = data[(i4 * 3) + 1];
            if (spanStart <= i2 && spanEnd >= i && ((spanStart == spanEnd || i == i2 || !(spanStart == i2 || spanEnd == i)) && (cls == null || cls == Object.class || cls.isInstance(spans[i4])))) {
                if (count == 0) {
                    ret1 = spans[i4];
                    count++;
                } else {
                    if (count == 1) {
                        ret = (Object[]) Array.newInstance(cls, (spanCount - i4) + 1);
                        ret[i3] = ret1;
                    }
                    int prio = data[(i4 * 3) + 2] & Spanned.SPAN_PRIORITY;
                    if (prio != 0) {
                        int j2 = i3;
                        while (true) {
                            j = j2;
                            if (j >= count || prio > (getSpanFlags(ret[j]) & Spanned.SPAN_PRIORITY)) {
                                System.arraycopy(ret, j, ret, j + 1, count - j);
                                ret[j] = spans[i4];
                                count++;
                            } else {
                                j2 = j + 1;
                                int i5 = queryStart;
                            }
                        }
                        System.arraycopy(ret, j, ret, j + 1, count - j);
                        ret[j] = spans[i4];
                        count++;
                    } else {
                        ret[count] = spans[i4];
                        count++;
                    }
                }
            }
            i4++;
            i = queryStart;
            i3 = 0;
        }
        if (count == 0) {
            return ArrayUtils.emptyArray(kind);
        }
        if (count == 1) {
            Object[] ret2 = (Object[]) Array.newInstance(cls, 1);
            ret2[0] = ret1;
            return ret2;
        } else if (count == ret.length) {
            return ret;
        } else {
            Object[] nret = (Object[]) Array.newInstance(cls, count);
            System.arraycopy(ret, 0, nret, 0, count);
            return nret;
        }
    }

    public int nextSpanTransition(int start, int limit, Class kind) {
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        if (kind == null) {
            kind = Object.class;
        }
        int limit2 = limit;
        for (int i = 0; i < count; i++) {
            int st = data[(i * 3) + 0];
            int en = data[(i * 3) + 1];
            if (st > start && st < limit2 && kind.isInstance(spans[i])) {
                limit2 = st;
            }
            if (en > start && en < limit2 && kind.isInstance(spans[i])) {
                limit2 = en;
            }
        }
        return limit2;
    }

    private void sendSpanAdded(Object what, int start, int end) {
        for (SpanWatcher onSpanAdded : (SpanWatcher[]) getSpans(start, end, SpanWatcher.class)) {
            onSpanAdded.onSpanAdded((Spannable) this, what, start, end);
        }
    }

    private void sendSpanRemoved(Object what, int start, int end) {
        for (SpanWatcher onSpanRemoved : (SpanWatcher[]) getSpans(start, end, SpanWatcher.class)) {
            onSpanRemoved.onSpanRemoved((Spannable) this, what, start, end);
        }
    }

    private void sendSpanChanged(Object what, int s, int e, int st, int en) {
        for (SpanWatcher onSpanChanged : (SpanWatcher[]) getSpans(Math.min(s, st), Math.max(e, en), SpanWatcher.class)) {
            onSpanChanged.onSpanChanged((Spannable) this, what, s, e, st, en);
        }
    }

    private static String region(int start, int end) {
        return "(" + start + " ... " + end + ")";
    }

    private void checkRange(String operation, int start, int end) {
        if (end >= start) {
            int len = length();
            if (start > len || end > len) {
                throw new IndexOutOfBoundsException(operation + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + region(start, end) + " ends beyond length " + len);
            } else if (start < 0 || end < 0) {
                throw new IndexOutOfBoundsException(operation + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + region(start, end) + " starts before 0");
            }
        } else {
            throw new IndexOutOfBoundsException(operation + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + region(start, end) + " has end before start");
        }
    }

    public boolean equals(Object o) {
        if ((o instanceof Spanned) && toString().equals(o.toString())) {
            Spanned other = (Spanned) o;
            Object[] otherSpans = other.getSpans(0, other.length(), Object.class);
            if (this.mSpanCount == otherSpans.length) {
                for (int i = 0; i < this.mSpanCount; i++) {
                    Object thisSpan = this.mSpans[i];
                    Object otherSpan = otherSpans[i];
                    if (thisSpan == this) {
                        if (other != otherSpan || getSpanStart(thisSpan) != other.getSpanStart(otherSpan) || getSpanEnd(thisSpan) != other.getSpanEnd(otherSpan) || getSpanFlags(thisSpan) != other.getSpanFlags(otherSpan)) {
                            return false;
                        }
                    } else if (!thisSpan.equals(otherSpan) || getSpanStart(thisSpan) != other.getSpanStart(otherSpan) || getSpanEnd(thisSpan) != other.getSpanEnd(otherSpan) || getSpanFlags(thisSpan) != other.getSpanFlags(otherSpan)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        int hash = (toString().hashCode() * 31) + this.mSpanCount;
        for (int i = 0; i < this.mSpanCount; i++) {
            Object span = this.mSpans[i];
            if (span != this) {
                hash = (hash * 31) + span.hashCode();
            }
            hash = (((((hash * 31) + getSpanStart(span)) * 31) + getSpanEnd(span)) * 31) + getSpanFlags(span);
        }
        return hash;
    }

    private void copySpans(Spanned src, int start, int end) {
        copySpans(src, start, end, false);
    }

    private void copySpans(SpannableStringInternal src, int start, int end) {
        copySpans(src, start, end, false);
    }
}
