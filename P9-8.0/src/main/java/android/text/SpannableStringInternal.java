package android.text;

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

    SpannableStringInternal(CharSequence source, int start, int end) {
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
            copySpans((SpannableStringInternal) source, start, end);
        } else {
            copySpans((Spanned) source, start, end);
        }
    }

    private final void copySpans(Spanned src, int start, int end) {
        Object[] spans = src.getSpans(start, end, Object.class);
        for (int i = 0; i < spans.length; i++) {
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

    private final void copySpans(SpannableStringInternal src, int start, int end) {
        if (start == 0 && end == src.length()) {
            this.mSpans = ArrayUtils.newUnpaddedObjectArray(src.mSpans.length);
            this.mSpanData = new int[src.mSpanData.length];
            this.mSpanCount = src.mSpanCount;
            System.arraycopy(src.mSpans, 0, this.mSpans, 0, src.mSpans.length);
            System.arraycopy(src.mSpanData, 0, this.mSpanData, 0, this.mSpanData.length);
        } else {
            int i;
            int count = 0;
            int[] srcData = src.mSpanData;
            int limit = src.mSpanCount;
            for (i = 0; i < limit; i++) {
                if (!isOutOfCopyRange(start, end, srcData[(i * 3) + 0], srcData[(i * 3) + 1])) {
                    count++;
                }
            }
            if (count != 0) {
                Object[] srcSpans = src.mSpans;
                this.mSpanCount = count;
                this.mSpans = ArrayUtils.newUnpaddedObjectArray(this.mSpanCount);
                this.mSpanData = new int[(this.mSpans.length * 3)];
                int j = 0;
                for (i = 0; i < limit; i++) {
                    int spanStart = srcData[(i * 3) + 0];
                    int spanEnd = srcData[(i * 3) + 1];
                    if (!isOutOfCopyRange(start, end, spanStart, spanEnd)) {
                        if (spanStart < start) {
                            spanStart = start;
                        }
                        if (spanEnd > end) {
                            spanEnd = end;
                        }
                        this.mSpans[j] = srcSpans[i];
                        this.mSpanData[(j * 3) + 0] = spanStart - start;
                        this.mSpanData[(j * 3) + 1] = spanEnd - start;
                        this.mSpanData[(j * 3) + 2] = srcData[(i * 3) + 2];
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

    void setSpan(Object what, int start, int end, int flags) {
        setSpan(what, start, end, flags, true);
    }

    private boolean isIndexFollowsNextLine(int index) {
        return (index == 0 || index == length() || charAt(index - 1) == 10) ? false : true;
    }

    private void setSpan(Object what, int start, int end, int flags, boolean enforceParagraph) {
        int nstart = start;
        int nend = end;
        checkRange("setSpan", start, end);
        if ((flags & 51) == 51) {
            if (isIndexFollowsNextLine(start)) {
                if (enforceParagraph) {
                    throw new RuntimeException("PARAGRAPH span must start at paragraph boundary (" + start + " follows " + charAt(start - 1) + ")");
                }
                return;
            } else if (isIndexFollowsNextLine(end)) {
                if (enforceParagraph) {
                    throw new RuntimeException("PARAGRAPH span must end at paragraph boundary (" + end + " follows " + charAt(end - 1) + ")");
                }
                return;
            }
        }
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        for (int i = 0; i < count; i++) {
            if (spans[i] == what) {
                int ostart = data[(i * 3) + 0];
                int oend = data[(i * 3) + 1];
                data[(i * 3) + 0] = start;
                data[(i * 3) + 1] = end;
                data[(i * 3) + 2] = flags;
                sendSpanChanged(what, ostart, oend, start, end);
                return;
            }
        }
        if (this.mSpanCount + 1 >= this.mSpans.length) {
            Object[] newtags = ArrayUtils.newUnpaddedObjectArray(GrowingArrayUtils.growSize(this.mSpanCount));
            int[] newdata = new int[(newtags.length * 3)];
            System.arraycopy(this.mSpans, 0, newtags, 0, this.mSpanCount);
            System.arraycopy(this.mSpanData, 0, newdata, 0, this.mSpanCount * 3);
            this.mSpans = newtags;
            this.mSpanData = newdata;
        }
        this.mSpans[this.mSpanCount] = what;
        this.mSpanData[(this.mSpanCount * 3) + 0] = start;
        this.mSpanData[(this.mSpanCount * 3) + 1] = end;
        this.mSpanData[(this.mSpanCount * 3) + 2] = flags;
        this.mSpanCount++;
        if (this instanceof Spannable) {
            sendSpanAdded(what, start, end);
        }
    }

    void removeSpan(Object what) {
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
                sendSpanRemoved(what, ostart, oend);
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

    public <T> T[] getSpans(int queryStart, int queryEnd, Class<T> kind) {
        int spanCount = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        Object ret = null;
        Object ret1 = null;
        int i = 0;
        int count = 0;
        while (i < spanCount) {
            int count2;
            int spanStart = data[(i * 3) + 0];
            int spanEnd = data[(i * 3) + 1];
            if (spanStart > queryEnd) {
                count2 = count;
            } else if (spanEnd < queryStart) {
                count2 = count;
            } else {
                if (!(spanStart == spanEnd || queryStart == queryEnd)) {
                    if (spanStart == queryEnd) {
                        count2 = count;
                    } else if (spanEnd == queryStart) {
                        count2 = count;
                    }
                }
                if (kind != null && kind != Object.class && (kind.isInstance(spans[i]) ^ 1) != 0) {
                    count2 = count;
                } else if (count == 0) {
                    ret1 = spans[i];
                    count2 = count + 1;
                } else {
                    if (count == 1) {
                        ret = (Object[]) Array.newInstance(kind, (spanCount - i) + 1);
                        ret[0] = ret1;
                    }
                    int prio = data[(i * 3) + 2] & Spanned.SPAN_PRIORITY;
                    if (prio != 0) {
                        int j = 0;
                        while (j < count && prio <= (getSpanFlags(ret[j]) & Spanned.SPAN_PRIORITY)) {
                            j++;
                        }
                        System.arraycopy(ret, j, ret, j + 1, count - j);
                        ret[j] = spans[i];
                        count2 = count + 1;
                    } else {
                        count2 = count + 1;
                        ret[count] = spans[i];
                    }
                }
            }
            i++;
            count = count2;
        }
        if (count == 0) {
            return ArrayUtils.emptyArray(kind);
        }
        if (count == 1) {
            Object[] ret2 = (Object[]) Array.newInstance(kind, 1);
            ret2[0] = ret1;
            return ret2;
        } else if (count == ret.length) {
            return ret;
        } else {
            Object[] nret = (Object[]) Array.newInstance(kind, count);
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
        int i = 0;
        while (i < count) {
            int st = data[(i * 3) + 0];
            int en = data[(i * 3) + 1];
            if (st > start && st < limit && kind.isInstance(spans[i])) {
                limit = st;
            }
            if (en > start && en < limit && kind.isInstance(spans[i])) {
                limit = en;
            }
            i++;
        }
        return limit;
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
        if (end < start) {
            throw new IndexOutOfBoundsException(operation + " " + region(start, end) + " has end before start");
        }
        int len = length();
        if (start > len || end > len) {
            throw new IndexOutOfBoundsException(operation + " " + region(start, end) + " ends beyond length " + len);
        } else if (start < 0 || end < 0) {
            throw new IndexOutOfBoundsException(operation + " " + region(start, end) + " starts before 0");
        }
    }

    public boolean equals(Object o) {
        if ((o instanceof Spanned) && toString().equals(o.toString())) {
            Spanned other = (Spanned) o;
            Object[] otherSpans = other.getSpans(0, other.length(), Object.class);
            if (this.mSpanCount == otherSpans.length) {
                for (int i = 0; i < this.mSpanCount; i++) {
                    SpannableStringInternal thisSpan = this.mSpans[i];
                    Spanned otherSpan = otherSpans[i];
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
            SpannableStringInternal span = this.mSpans[i];
            if (span != this) {
                hash = (hash * 31) + span.hashCode();
            }
            hash = (((((hash * 31) + getSpanStart(span)) * 31) + getSpanEnd(span)) * 31) + getSpanFlags(span);
        }
        return hash;
    }
}
