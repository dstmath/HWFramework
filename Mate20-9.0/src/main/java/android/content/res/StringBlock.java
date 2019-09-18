package android.content.res;

import android.app.slice.Slice;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Annotation;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineHeightSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.SparseArray;

final class StringBlock {
    private static final String TAG = "AssetManager";
    private static final boolean localLOGV = false;
    private final long mNative;
    private final boolean mOwnsNative;
    private SparseArray<CharSequence> mSparseStrings;
    private CharSequence[] mStrings;
    StyleIDs mStyleIDs = null;
    private final boolean mUseSparse;

    private static class Height implements LineHeightSpan.WithDensity {
        private static float sProportion = 0.0f;
        private int mSize;

        public Height(int size) {
            this.mSize = size;
        }

        public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm) {
            chooseHeight(text, start, end, spanstartv, v, fm, null);
        }

        public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm, TextPaint paint) {
            int size = this.mSize;
            if (paint != null) {
                size = (int) (((float) size) * paint.density);
            }
            if (fm.bottom - fm.top < size) {
                fm.top = fm.bottom - size;
                fm.ascent -= size;
                return;
            }
            if (sProportion == 0.0f) {
                Paint p = new Paint();
                p.setTextSize(100.0f);
                Rect r = new Rect();
                p.getTextBounds("ABCDEFG", 0, 7, r);
                sProportion = ((float) r.top) / p.ascent();
            }
            int need = (int) Math.ceil((double) (((float) (-fm.top)) * sProportion));
            if (size - fm.descent >= need) {
                fm.top = fm.bottom - size;
                fm.ascent = fm.descent - size;
            } else if (size >= need) {
                int i = -need;
                fm.ascent = i;
                fm.top = i;
                int i2 = fm.top + size;
                fm.descent = i2;
                fm.bottom = i2;
            } else {
                int i3 = -size;
                fm.ascent = i3;
                fm.top = i3;
                fm.descent = 0;
                fm.bottom = 0;
            }
        }
    }

    static final class StyleIDs {
        /* access modifiers changed from: private */
        public int bigId = -1;
        /* access modifiers changed from: private */
        public int boldId = -1;
        /* access modifiers changed from: private */
        public int italicId = -1;
        /* access modifiers changed from: private */
        public int listItemId = -1;
        /* access modifiers changed from: private */
        public int marqueeId = -1;
        /* access modifiers changed from: private */
        public int smallId = -1;
        /* access modifiers changed from: private */
        public int strikeId = -1;
        /* access modifiers changed from: private */
        public int subId = -1;
        /* access modifiers changed from: private */
        public int supId = -1;
        /* access modifiers changed from: private */
        public int ttId = -1;
        /* access modifiers changed from: private */
        public int underlineId = -1;

        StyleIDs() {
        }
    }

    private static native long nativeCreate(byte[] bArr, int i, int i2);

    private static native void nativeDestroy(long j);

    private static native int nativeGetSize(long j);

    private static native String nativeGetString(long j, int i);

    private static native int[] nativeGetStyle(long j, int i);

    public StringBlock(byte[] data, boolean useSparse) {
        this.mNative = nativeCreate(data, 0, data.length);
        this.mUseSparse = useSparse;
        this.mOwnsNative = true;
    }

    public StringBlock(byte[] data, int offset, int size, boolean useSparse) {
        this.mNative = nativeCreate(data, offset, size);
        this.mUseSparse = useSparse;
        this.mOwnsNative = true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0177, code lost:
        return r1;
     */
    public CharSequence get(int idx) {
        synchronized (this) {
            if (this.mStrings != null) {
                CharSequence res = this.mStrings[idx];
                if (res != null) {
                    return res;
                }
            } else if (this.mSparseStrings != null) {
                CharSequence res2 = this.mSparseStrings.get(idx);
                if (res2 != null) {
                    return res2;
                }
            } else {
                int num = nativeGetSize(this.mNative);
                if (!this.mUseSparse || num <= 250) {
                    this.mStrings = new CharSequence[num];
                } else {
                    this.mSparseStrings = new SparseArray<>();
                }
            }
            String str = nativeGetString(this.mNative, idx);
            CharSequence res3 = str;
            int[] style = nativeGetStyle(this.mNative, idx);
            if (style != null) {
                if (this.mStyleIDs == null) {
                    this.mStyleIDs = new StyleIDs();
                }
                for (int styleIndex = 0; styleIndex < style.length; styleIndex += 3) {
                    int styleId = style[styleIndex];
                    if (!(styleId == this.mStyleIDs.boldId || styleId == this.mStyleIDs.italicId || styleId == this.mStyleIDs.underlineId || styleId == this.mStyleIDs.ttId || styleId == this.mStyleIDs.bigId || styleId == this.mStyleIDs.smallId || styleId == this.mStyleIDs.subId || styleId == this.mStyleIDs.supId || styleId == this.mStyleIDs.strikeId || styleId == this.mStyleIDs.listItemId)) {
                        if (styleId != this.mStyleIDs.marqueeId) {
                            String styleTag = nativeGetString(this.mNative, styleId);
                            if (styleTag.equals("b")) {
                                int unused = this.mStyleIDs.boldId = styleId;
                            } else if (styleTag.equals("i")) {
                                int unused2 = this.mStyleIDs.italicId = styleId;
                            } else if (styleTag.equals("u")) {
                                int unused3 = this.mStyleIDs.underlineId = styleId;
                            } else if (styleTag.equals(TtmlUtils.TAG_TT)) {
                                int unused4 = this.mStyleIDs.ttId = styleId;
                            } else if (styleTag.equals("big")) {
                                int unused5 = this.mStyleIDs.bigId = styleId;
                            } else if (styleTag.equals("small")) {
                                int unused6 = this.mStyleIDs.smallId = styleId;
                            } else if (styleTag.equals("sup")) {
                                int unused7 = this.mStyleIDs.supId = styleId;
                            } else if (styleTag.equals("sub")) {
                                int unused8 = this.mStyleIDs.subId = styleId;
                            } else if (styleTag.equals("strike")) {
                                int unused9 = this.mStyleIDs.strikeId = styleId;
                            } else if (styleTag.equals("li")) {
                                int unused10 = this.mStyleIDs.listItemId = styleId;
                            } else if (styleTag.equals("marquee")) {
                                int unused11 = this.mStyleIDs.marqueeId = styleId;
                            }
                        }
                    }
                }
                res3 = applyStyles(str, style, this.mStyleIDs);
            }
            if (this.mStrings != null) {
                this.mStrings[idx] = res3;
            } else {
                this.mSparseStrings.put(idx, res3);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            super.finalize();
        } finally {
            if (this.mOwnsNative) {
                nativeDestroy(this.mNative);
            }
        }
    }

    private CharSequence applyStyles(String str, int[] style, StyleIDs ids) {
        String str2 = str;
        int[] iArr = style;
        if (iArr.length == 0) {
            return str2;
        }
        SpannableString buffer = new SpannableString(str2);
        boolean z = false;
        int i = 0;
        while (i < iArr.length) {
            int type = iArr[i];
            if (type == ids.boldId) {
                buffer.setSpan(new StyleSpan(1), iArr[i + 1], iArr[i + 2] + 1, 33);
            } else if (type == ids.italicId) {
                buffer.setSpan(new StyleSpan(2), iArr[i + 1], iArr[i + 2] + 1, 33);
            } else if (type == ids.underlineId) {
                buffer.setSpan(new UnderlineSpan(), iArr[i + 1], iArr[i + 2] + 1, 33);
            } else if (type == ids.ttId) {
                buffer.setSpan(new TypefaceSpan("monospace"), iArr[i + 1], iArr[i + 2] + 1, 33);
            } else if (type == ids.bigId) {
                buffer.setSpan(new RelativeSizeSpan(1.25f), iArr[i + 1], iArr[i + 2] + 1, 33);
            } else if (type == ids.smallId) {
                buffer.setSpan(new RelativeSizeSpan(0.8f), iArr[i + 1], iArr[i + 2] + 1, 33);
            } else if (type == ids.subId) {
                buffer.setSpan(new SubscriptSpan(), iArr[i + 1], iArr[i + 2] + 1, 33);
            } else if (type == ids.supId) {
                buffer.setSpan(new SuperscriptSpan(), iArr[i + 1], iArr[i + 2] + 1, 33);
            } else if (type == ids.strikeId) {
                buffer.setSpan(new StrikethroughSpan(), iArr[i + 1], iArr[i + 2] + 1, 33);
            } else if (type == ids.listItemId) {
                addParagraphSpan(buffer, new BulletSpan(10), iArr[i + 1], iArr[i + 2] + 1);
            } else if (type == ids.marqueeId) {
                buffer.setSpan(TextUtils.TruncateAt.MARQUEE, iArr[i + 1], iArr[i + 2] + 1, 18);
            } else {
                String tag = nativeGetString(this.mNative, type);
                if (tag.startsWith("font;")) {
                    String sub = subtag(tag, ";height=");
                    if (sub != null) {
                        addParagraphSpan(buffer, new Height(Integer.parseInt(sub)), iArr[i + 1], iArr[i + 2] + 1);
                    }
                    String sub2 = subtag(tag, ";size=");
                    if (sub2 != null) {
                        buffer.setSpan(new AbsoluteSizeSpan(Integer.parseInt(sub2), true), iArr[i + 1], iArr[i + 2] + 1, 33);
                    }
                    String sub3 = subtag(tag, ";fgcolor=");
                    if (sub3 != null) {
                        buffer.setSpan(getColor(sub3, true), iArr[i + 1], iArr[i + 2] + 1, 33);
                    }
                    String sub4 = subtag(tag, ";color=");
                    if (sub4 != null) {
                        buffer.setSpan(getColor(sub4, true), iArr[i + 1], iArr[i + 2] + 1, 33);
                    }
                    String sub5 = subtag(tag, ";bgcolor=");
                    if (sub5 != null) {
                        buffer.setSpan(getColor(sub5, z), iArr[i + 1], iArr[i + 2] + 1, 33);
                    }
                    String sub6 = subtag(tag, ";face=");
                    if (sub6 != null) {
                        buffer.setSpan(new TypefaceSpan(sub6), iArr[i + 1], iArr[i + 2] + 1, 33);
                    }
                } else if (tag.startsWith("a;")) {
                    String sub7 = subtag(tag, ";href=");
                    if (sub7 != null) {
                        buffer.setSpan(new URLSpan(sub7), iArr[i + 1], iArr[i + 2] + 1, 33);
                    }
                } else if (tag.startsWith("annotation;")) {
                    int len = tag.length();
                    int i2 = 59;
                    int t = tag.indexOf(59);
                    while (t < len) {
                        int eq = tag.indexOf(61, t);
                        if (eq < 0) {
                            break;
                        }
                        int next = tag.indexOf(i2, eq);
                        if (next < 0) {
                            next = len;
                        }
                        buffer.setSpan(new Annotation(tag.substring(t + 1, eq), tag.substring(eq + 1, next)), iArr[i + 1], iArr[i + 2] + 1, 33);
                        t = next;
                        String str3 = str;
                        i2 = 59;
                    }
                }
                i += 3;
                String str4 = str;
                z = false;
            }
            i += 3;
            String str42 = str;
            z = false;
        }
        return new SpannedString(buffer);
    }

    private static CharacterStyle getColor(String color, boolean foreground) {
        int c = -16777216;
        if (!TextUtils.isEmpty(color)) {
            if (color.startsWith("@")) {
                Resources res = Resources.getSystem();
                int colorRes = res.getIdentifier(color.substring(1), Slice.SUBTYPE_COLOR, "android");
                if (colorRes != 0) {
                    ColorStateList colors = res.getColorStateList(colorRes, null);
                    if (foreground) {
                        TextAppearanceSpan textAppearanceSpan = new TextAppearanceSpan(null, 0, 0, colors, null);
                        return textAppearanceSpan;
                    }
                    c = colors.getDefaultColor();
                }
            } else {
                try {
                    c = Color.parseColor(color);
                } catch (IllegalArgumentException e) {
                    c = -16777216;
                }
            }
        }
        if (foreground) {
            return new ForegroundColorSpan(c);
        }
        return new BackgroundColorSpan(c);
    }

    private static void addParagraphSpan(Spannable buffer, Object what, int start, int end) {
        int len = buffer.length();
        if (start != 0 && start != len && buffer.charAt(start - 1) != 10) {
            do {
                start--;
                if (start <= 0) {
                    break;
                }
            } while (buffer.charAt(start - 1) != 10);
        }
        if (end != 0 && end != len && buffer.charAt(end - 1) != 10) {
            do {
                end++;
                if (end >= len) {
                    break;
                }
            } while (buffer.charAt(end - 1) != 10);
        }
        buffer.setSpan(what, start, end, 51);
    }

    private static String subtag(String full, String attribute) {
        int start = full.indexOf(attribute);
        if (start < 0) {
            return null;
        }
        int start2 = start + attribute.length();
        int end = full.indexOf(59, start2);
        if (end < 0) {
            return full.substring(start2);
        }
        return full.substring(start2, end);
    }

    StringBlock(long obj, boolean useSparse) {
        this.mNative = obj;
        this.mUseSparse = useSparse;
        this.mOwnsNative = false;
    }
}
