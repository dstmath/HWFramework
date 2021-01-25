package android.text;

public interface Spanned extends CharSequence {
    public static final int SPAN_COMPOSING = 256;
    public static final int SPAN_EXCLUSIVE_EXCLUSIVE = 33;
    public static final int SPAN_EXCLUSIVE_INCLUSIVE = 34;
    public static final int SPAN_INCLUSIVE_EXCLUSIVE = 17;
    public static final int SPAN_INCLUSIVE_INCLUSIVE = 18;
    public static final int SPAN_INTERMEDIATE = 512;
    public static final int SPAN_MARK_MARK = 17;
    public static final int SPAN_MARK_POINT = 18;
    public static final int SPAN_PARAGRAPH = 51;
    public static final int SPAN_POINT_MARK = 33;
    public static final int SPAN_POINT_MARK_MASK = 51;
    public static final int SPAN_POINT_POINT = 34;
    public static final int SPAN_PRIORITY = 16711680;
    public static final int SPAN_PRIORITY_SHIFT = 16;
    public static final int SPAN_USER = -16777216;
    public static final int SPAN_USER_SHIFT = 24;

    int getSpanEnd(Object obj);

    int getSpanFlags(Object obj);

    int getSpanStart(Object obj);

    <T> T[] getSpans(int i, int i2, Class<T> cls);

    int nextSpanTransition(int i, int i2, Class cls);
}
