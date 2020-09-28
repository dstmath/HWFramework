package android.text;

import android.annotation.UnsupportedAppUsage;
import android.net.wifi.WifiEnterpriseConfig;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.lang.reflect.Array;
import libcore.util.EmptyArray;

/* access modifiers changed from: package-private */
public abstract class SpannableStringInternal {
    @UnsupportedAppUsage
    private static final int COLUMNS = 3;
    @UnsupportedAppUsage
    static final Object[] EMPTY = new Object[0];
    @UnsupportedAppUsage
    private static final int END = 1;
    @UnsupportedAppUsage
    private static final int FLAGS = 2;
    @UnsupportedAppUsage
    private static final int START = 0;
    @UnsupportedAppUsage
    private int mSpanCount;
    @UnsupportedAppUsage
    private int[] mSpanData;
    @UnsupportedAppUsage
    private int mSpanIndex;
    @UnsupportedAppUsage
    private Object[] mSpans;
    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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
        int count = 0;
        int[] srcData = src.mSpanData;
        Object[] srcSpans = src.mSpans;
        int limit = src.mSpanCount;
        boolean hasNoCopySpan = false;
        for (int i = 0; i < limit; i++) {
            if (!isOutOfCopyRange(start, end, srcData[(i * 3) + 0], srcData[(i * 3) + 1])) {
                if (srcSpans[i] instanceof NoCopySpan) {
                    hasNoCopySpan = true;
                    if (ignoreNoCopySpan) {
                    }
                }
                count++;
            }
        }
        if (count != 0) {
            if (!hasNoCopySpan && start == 0 && end == src.length()) {
                this.mSpans = ArrayUtils.newUnpaddedObjectArray(src.mSpans.length);
                this.mSpanData = new int[src.mSpanData.length];
                this.mSpanCount = src.mSpanCount;
                Object[] objArr = src.mSpans;
                System.arraycopy(objArr, 0, this.mSpans, 0, objArr.length);
                int[] iArr = src.mSpanData;
                int[] iArr2 = this.mSpanData;
                System.arraycopy(iArr, 0, iArr2, 0, iArr2.length);
                return;
            }
            this.mSpanCount = count;
            this.mSpans = ArrayUtils.newUnpaddedObjectArray(this.mSpanCount);
            this.mSpanData = new int[(this.mSpans.length * 3)];
            int j = 0;
            for (int i2 = 0; i2 < limit; i2++) {
                int spanStart = srcData[(i2 * 3) + 0];
                int spanEnd = srcData[(i2 * 3) + 1];
                if (!isOutOfCopyRange(start, end, spanStart, spanEnd) && (!ignoreNoCopySpan || !(srcSpans[i2] instanceof NoCopySpan))) {
                    if (spanStart < start) {
                        spanStart = start;
                    }
                    if (spanEnd > end) {
                        spanEnd = end;
                    }
                    this.mSpans[j] = srcSpans[i2];
                    int[] iArr3 = this.mSpanData;
                    iArr3[(j * 3) + 0] = spanStart - start;
                    iArr3[(j * 3) + 1] = spanEnd - start;
                    iArr3[(j * 3) + 2] = srcData[(i2 * 3) + 2];
                    j++;
                }
            }
        }
    }

    @UnsupportedAppUsage
    private final boolean isOutOfCopyRange(int start, int end, int spanStart, int spanEnd) {
        if (spanStart > end || spanEnd < start) {
            return true;
        }
        if (spanStart == spanEnd || start == end) {
            return false;
        }
        if (spanStart == end || spanEnd == start) {
            return true;
        }
        return false;
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
    @UnsupportedAppUsage
    public void setSpan(Object what, int start, int end, int flags) {
        setSpan(what, start, end, flags, true);
    }

    @UnsupportedAppUsage
    private boolean isIndexFollowsNextLine(int index) {
        return (index == 0 || index == length() || charAt(index + -1) == '\n') ? false : true;
    }

    @UnsupportedAppUsage
    private void setSpan(Object what, int start, int end, int flags, boolean enforceParagraph) {
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
        int i2 = this.mSpanCount;
        if (i2 + 1 >= this.mSpans.length) {
            Object[] newtags = ArrayUtils.newUnpaddedObjectArray(GrowingArrayUtils.growSize(i2));
            int[] newdata = new int[(newtags.length * 3)];
            System.arraycopy(this.mSpans, 0, newtags, 0, this.mSpanCount);
            System.arraycopy(this.mSpanData, 0, newdata, 0, this.mSpanCount * 3);
            this.mSpans = newtags;
            this.mSpanData = newdata;
        }
        Object[] newtags2 = this.mSpans;
        int i3 = this.mSpanCount;
        newtags2[i3] = what;
        int[] iArr = this.mSpanData;
        iArr[(i3 * 3) + 0] = start;
        iArr[(i3 * 3) + 1] = end;
        iArr[(i3 * 3) + 2] = flags;
        this.mSpanCount = i3 + 1;
        if (this instanceof Spannable) {
            sendSpanAdded(what, start, end);
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
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
                    return;
                }
                return;
            }
        }
    }

    @UnsupportedAppUsage
    public int getSpanStart(Object what) {
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        int index = getSpanIndex(what);
        if (index != -1) {
            return data[(index * 3) + 0];
        }
        for (int i = count - 1; i >= 0; i--) {
            if (spans[i] == what) {
                this.mSpanIndex = i;
                return data[(i * 3) + 0];
            }
        }
        return -1;
    }

    @UnsupportedAppUsage
    public int getSpanEnd(Object what) {
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        int index = getSpanIndex(what);
        if (index != -1) {
            return data[(index * 3) + 1];
        }
        for (int i = count - 1; i >= 0; i--) {
            if (spans[i] == what) {
                this.mSpanIndex = i;
                return data[(i * 3) + 1];
            }
        }
        return -1;
    }

    @UnsupportedAppUsage
    public int getSpanFlags(Object what) {
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        int index = getSpanIndex(what);
        if (index != -1) {
            return data[(index * 3) + 2];
        }
        for (int i = count - 1; i >= 0; i--) {
            if (spans[i] == what) {
                this.mSpanIndex = i;
                return data[(i * 3) + 2];
            }
        }
        return 0;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:44:0x0089 */
    /* JADX DEBUG: Multi-variable search result rejected for r8v1, resolved type: T[] */
    /* JADX DEBUG: Multi-variable search result rejected for r1v6, resolved type: T[] */
    /* JADX DEBUG: Multi-variable search result rejected for r8v3, resolved type: T[] */
    /* JADX DEBUG: Multi-variable search result rejected for r1v12, resolved type: java.lang.Object[] */
    /* JADX DEBUG: Multi-variable search result rejected for r8v5, resolved type: T[] */
    /* JADX DEBUG: Multi-variable search result rejected for r8v8, resolved type: T[] */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r8v9 */
    @UnsupportedAppUsage
    public <T> T[] getSpans(int queryStart, int queryEnd, Class<T> kind) {
        T[] tArr;
        int i = queryStart;
        int count = 0;
        int spanCount = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        Object[] ret = (T[]) null;
        Object ret1 = null;
        int i2 = 0;
        while (i2 < spanCount) {
            int spanStart = data[(i2 * 3) + 0];
            int spanEnd = data[(i2 * 3) + 1];
            if (spanStart <= queryEnd && spanEnd >= i && ((spanStart == spanEnd || i == queryEnd || !(spanStart == queryEnd || spanEnd == i)) && (kind == null || kind == Object.class || kind.isInstance(spans[i2])))) {
                if (count == 0) {
                    ret1 = spans[i2];
                    count++;
                } else {
                    if (count == 1) {
                        tArr = (T[]) ((Object[]) Array.newInstance((Class<?>) kind, (spanCount - i2) + 1));
                        tArr[0] = ret1;
                    }
                    int prio = data[(i2 * 3) + 2] & 16711680;
                    if (prio != 0) {
                        int j = 0;
                        while (j < count && prio <= (getSpanFlags(tArr[j]) & 16711680)) {
                            j++;
                        }
                        System.arraycopy(tArr, j, tArr, j + 1, count - j);
                        tArr[j] = spans[i2];
                        count++;
                    } else {
                        tArr[count] = spans[i2];
                        count++;
                    }
                }
            }
            i2++;
            i = queryStart;
        }
        if (count == 0) {
            return (T[]) ArrayUtils.emptyArray(kind);
        }
        if (count == 1) {
            T[] tArr2 = (T[]) ((Object[]) Array.newInstance((Class<?>) kind, 1));
            tArr2[0] = ret1;
            return tArr2;
        } else if (count == tArr.length) {
            return tArr;
        } else {
            T[] tArr3 = (T[]) ((Object[]) Array.newInstance((Class<?>) kind, count));
            System.arraycopy(tArr, 0, tArr3, 0, count);
            return tArr3;
        }
    }

    @UnsupportedAppUsage
    public int nextSpanTransition(int start, int limit, Class kind) {
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        if (kind == null) {
            kind = Object.class;
        }
        for (int i = 0; i < count; i++) {
            int st = data[(i * 3) + 0];
            int en = data[(i * 3) + 1];
            if (st > start && st < limit && kind.isInstance(spans[i])) {
                limit = st;
            }
            if (en > start && en < limit && kind.isInstance(spans[i])) {
                limit = en;
            }
        }
        return limit;
    }

    @UnsupportedAppUsage
    private void sendSpanAdded(Object what, int start, int end) {
        for (SpanWatcher spanWatcher : (SpanWatcher[]) getSpans(start, end, SpanWatcher.class)) {
            spanWatcher.onSpanAdded((Spannable) this, what, start, end);
        }
    }

    @UnsupportedAppUsage
    private void sendSpanRemoved(Object what, int start, int end) {
        for (SpanWatcher spanWatcher : (SpanWatcher[]) getSpans(start, end, SpanWatcher.class)) {
            spanWatcher.onSpanRemoved((Spannable) this, what, start, end);
        }
    }

    @UnsupportedAppUsage
    private void sendSpanChanged(Object what, int s, int e, int st, int en) {
        for (SpanWatcher spanWatcher : (SpanWatcher[]) getSpans(Math.min(s, st), Math.max(e, en), SpanWatcher.class)) {
            spanWatcher.onSpanChanged((Spannable) this, what, s, e, st, en);
        }
    }

    @UnsupportedAppUsage
    private static String region(int start, int end) {
        return "(" + start + " ... " + end + ")";
    }

    @UnsupportedAppUsage
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
            Object[] thisSpans = getSpans(0, length(), Object.class);
            if (this.mSpanCount == otherSpans.length) {
                for (int i = 0; i < this.mSpanCount; i++) {
                    Object thisSpan = thisSpans[i];
                    Object otherSpan = otherSpans[i];
                    if (thisSpan == this) {
                        if (!(other == otherSpan && getSpanStart(thisSpan) == other.getSpanStart(otherSpan) && getSpanEnd(thisSpan) == other.getSpanEnd(otherSpan) && getSpanFlags(thisSpan) == other.getSpanFlags(otherSpan))) {
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

    @UnsupportedAppUsage
    private void copySpans(Spanned src, int start, int end) {
        copySpans(src, start, end, false);
    }

    @UnsupportedAppUsage
    private void copySpans(SpannableStringInternal src, int start, int end) {
        copySpans(src, start, end, false);
    }

    @UnsupportedAppUsage
    private int getSpanIndex(Object what) {
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        if (count <= 0) {
            return -1;
        }
        int lastIndex = this.mSpanIndex % count;
        if (spans[lastIndex] == what) {
            return lastIndex;
        }
        int lastIndex2 = lastIndex + 1;
        if (lastIndex2 >= count || spans[lastIndex2] != what) {
            return -1;
        }
        this.mSpanIndex = lastIndex2;
        return lastIndex2;
    }
}
