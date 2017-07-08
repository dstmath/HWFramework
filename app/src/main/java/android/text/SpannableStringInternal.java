package android.text;

import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.lang.reflect.Array;
import libcore.util.EmptyArray;

abstract class SpannableStringInternal {
    private static final int COLUMNS = 3;
    static final Object[] EMPTY = null;
    private static final int END = 1;
    private static final int FLAGS = 2;
    private static final int START = 0;
    private int mSpanCount;
    private int[] mSpanData;
    private Object[] mSpans;
    private String mText;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.SpannableStringInternal.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.SpannableStringInternal.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.text.SpannableStringInternal.<clinit>():void");
    }

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
        for (int i = 0; i < spans.length; i += END) {
            int st = src.getSpanStart(spans[i]);
            int en = src.getSpanEnd(spans[i]);
            int fl = src.getSpanFlags(spans[i]);
            if (st < start) {
                st = start;
            }
            if (en > end) {
                en = end;
            }
            setSpan(spans[i], st - start, en - start, fl);
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
            for (i = 0; i < limit; i += END) {
                if (!isOutOfCopyRange(start, end, srcData[(i * COLUMNS) + 0], srcData[(i * COLUMNS) + END])) {
                    count += END;
                }
            }
            if (count != 0) {
                Object[] srcSpans = src.mSpans;
                this.mSpanCount = count;
                this.mSpans = ArrayUtils.newUnpaddedObjectArray(this.mSpanCount);
                this.mSpanData = new int[(this.mSpans.length * COLUMNS)];
                int j = 0;
                for (i = 0; i < limit; i += END) {
                    int spanStart = srcData[(i * COLUMNS) + 0];
                    int spanEnd = srcData[(i * COLUMNS) + END];
                    if (!isOutOfCopyRange(start, end, spanStart, spanEnd)) {
                        if (spanStart < start) {
                            spanStart = start;
                        }
                        if (spanEnd > end) {
                            spanEnd = end;
                        }
                        this.mSpans[j] = srcSpans[i];
                        this.mSpanData[(j * COLUMNS) + 0] = spanStart - start;
                        this.mSpanData[(j * COLUMNS) + END] = spanEnd - start;
                        this.mSpanData[(j * COLUMNS) + FLAGS] = srcData[(i * COLUMNS) + FLAGS];
                        j += END;
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
        int nstart = start;
        int nend = end;
        checkRange("setSpan", start, end);
        if ((flags & 51) == 51) {
            char c;
            if (!(start == 0 || start == length())) {
                c = charAt(start - 1);
                if (c != '\n') {
                    throw new RuntimeException("PARAGRAPH span must start at paragraph boundary (" + start + " follows " + c + ")");
                }
            }
            if (!(end == 0 || end == length())) {
                c = charAt(end - 1);
                if (c != '\n') {
                    throw new RuntimeException("PARAGRAPH span must end at paragraph boundary (" + end + " follows " + c + ")");
                }
            }
        }
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        for (int i = 0; i < count; i += END) {
            if (spans[i] == what) {
                int ostart = data[(i * COLUMNS) + 0];
                int oend = data[(i * COLUMNS) + END];
                data[(i * COLUMNS) + 0] = start;
                data[(i * COLUMNS) + END] = end;
                data[(i * COLUMNS) + FLAGS] = flags;
                sendSpanChanged(what, ostart, oend, start, end);
                return;
            }
        }
        if (this.mSpanCount + END >= this.mSpans.length) {
            Object[] newtags = ArrayUtils.newUnpaddedObjectArray(GrowingArrayUtils.growSize(this.mSpanCount));
            int[] newdata = new int[(newtags.length * COLUMNS)];
            System.arraycopy(this.mSpans, 0, newtags, 0, this.mSpanCount);
            System.arraycopy(this.mSpanData, 0, newdata, 0, this.mSpanCount * COLUMNS);
            this.mSpans = newtags;
            this.mSpanData = newdata;
        }
        this.mSpans[this.mSpanCount] = what;
        this.mSpanData[(this.mSpanCount * COLUMNS) + 0] = start;
        this.mSpanData[(this.mSpanCount * COLUMNS) + END] = end;
        this.mSpanData[(this.mSpanCount * COLUMNS) + FLAGS] = flags;
        this.mSpanCount += END;
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
                int ostart = data[(i * COLUMNS) + 0];
                int oend = data[(i * COLUMNS) + END];
                int c = count - (i + END);
                System.arraycopy(spans, i + END, spans, i, c);
                System.arraycopy(data, (i + END) * COLUMNS, data, i * COLUMNS, c * COLUMNS);
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
                return data[(i * COLUMNS) + 0];
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
                return data[(i * COLUMNS) + END];
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
                return data[(i * COLUMNS) + FLAGS];
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
            int spanStart = data[(i * COLUMNS) + 0];
            int spanEnd = data[(i * COLUMNS) + END];
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
                if (!(kind == null || kind == Object.class)) {
                    if (!kind.isInstance(spans[i])) {
                        count2 = count;
                    }
                }
                if (count == 0) {
                    ret1 = spans[i];
                    count2 = count + END;
                } else {
                    if (count == END) {
                        ret = (Object[]) Array.newInstance(kind, (spanCount - i) + END);
                        ret[0] = ret1;
                    }
                    int prio = data[(i * COLUMNS) + FLAGS] & Spanned.SPAN_PRIORITY;
                    if (prio != 0) {
                        int j = 0;
                        while (j < count) {
                            if (prio > (getSpanFlags(ret[j]) & Spanned.SPAN_PRIORITY)) {
                                break;
                            }
                            j += END;
                        }
                        System.arraycopy(ret, j, ret, j + END, count - j);
                        ret[j] = spans[i];
                        count2 = count + END;
                    } else {
                        count2 = count + END;
                        ret[count] = spans[i];
                    }
                }
            }
            i += END;
            count = count2;
        }
        if (count == 0) {
            return ArrayUtils.emptyArray(kind);
        }
        if (count == END) {
            Object[] ret2 = (Object[]) Array.newInstance(kind, END);
            ret2[0] = ret1;
            return ret2;
        }
        int length = ret.length;
        if (count == r0) {
            return ret;
        }
        Object[] nret = (Object[]) Array.newInstance(kind, count);
        System.arraycopy(ret, 0, nret, 0, count);
        return nret;
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
            int st = data[(i * COLUMNS) + 0];
            int en = data[(i * COLUMNS) + END];
            if (st > start && st < limit && kind.isInstance(spans[i])) {
                limit = st;
            }
            if (en > start && en < limit && kind.isInstance(spans[i])) {
                limit = en;
            }
            i += END;
        }
        return limit;
    }

    private void sendSpanAdded(Object what, int start, int end) {
        SpanWatcher[] recip = (SpanWatcher[]) getSpans(start, end, SpanWatcher.class);
        int n = recip.length;
        for (int i = 0; i < n; i += END) {
            recip[i].onSpanAdded((Spannable) this, what, start, end);
        }
    }

    private void sendSpanRemoved(Object what, int start, int end) {
        SpanWatcher[] recip = (SpanWatcher[]) getSpans(start, end, SpanWatcher.class);
        int n = recip.length;
        for (int i = 0; i < n; i += END) {
            recip[i].onSpanRemoved((Spannable) this, what, start, end);
        }
    }

    private void sendSpanChanged(Object what, int s, int e, int st, int en) {
        SpanWatcher[] recip = (SpanWatcher[]) getSpans(Math.min(s, st), Math.max(e, en), SpanWatcher.class);
        int n = recip.length;
        for (int i = 0; i < n; i += END) {
            recip[i].onSpanChanged((Spannable) this, what, s, e, st, en);
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
                for (int i = 0; i < this.mSpanCount; i += END) {
                    SpannableStringInternal thisSpan = this.mSpans[i];
                    Spanned otherSpan = otherSpans[i];
                    if (thisSpan != this) {
                        if (thisSpan.equals(otherSpan) && getSpanStart(thisSpan) == other.getSpanStart(otherSpan) && getSpanEnd(thisSpan) == other.getSpanEnd(otherSpan)) {
                            if (getSpanFlags(thisSpan) != other.getSpanFlags(otherSpan)) {
                            }
                        }
                        return false;
                    } else if (other != otherSpan || getSpanStart(thisSpan) != other.getSpanStart(otherSpan) || getSpanEnd(thisSpan) != other.getSpanEnd(otherSpan) || getSpanFlags(thisSpan) != other.getSpanFlags(otherSpan)) {
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
        for (int i = 0; i < this.mSpanCount; i += END) {
            SpannableStringInternal span = this.mSpans[i];
            if (span != this) {
                hash = (hash * 31) + span.hashCode();
            }
            hash = (((((hash * 31) + getSpanStart(span)) * 31) + getSpanEnd(span)) * 31) + getSpanFlags(span);
        }
        return hash;
    }
}
