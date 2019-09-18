package android.text;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.style.CharacterStyle;
import android.text.style.ImageSpan;
import android.text.style.MetricAffectingSpan;
import android.text.style.ReplacementSpan;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import java.util.ArrayList;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public class TextLine {
    private static final boolean DEBUG = false;
    private static final int TAB_INCREMENT = 20;
    private static final TextLine[] sCached = new TextLine[3];
    private final TextPaint mActivePaint = new TextPaint();
    private float mAddedWidth;
    private final SpanSet<CharacterStyle> mCharacterStyleSpanSet = new SpanSet<>(CharacterStyle.class);
    private char[] mChars;
    private boolean mCharsValid;
    private PrecomputedText mComputed;
    private final DecorationInfo mDecorationInfo = new DecorationInfo();
    private final ArrayList<DecorationInfo> mDecorations = new ArrayList<>();
    private int mDir;
    private Layout.Directions mDirections;
    private boolean mHasTabs;
    private int mLen;
    private final SpanSet<MetricAffectingSpan> mMetricAffectingSpanSpanSet = new SpanSet<>(MetricAffectingSpan.class);
    private TextPaint mPaint;
    private final SpanSet<ReplacementSpan> mReplacementSpanSpanSet = new SpanSet<>(ReplacementSpan.class);
    private Spanned mSpanned;
    private int mStart;
    private Layout.TabStops mTabs;
    private CharSequence mText;
    private final TextPaint mWorkPaint = new TextPaint();

    private static final class DecorationInfo {
        public int end;
        public boolean isStrikeThruText;
        public boolean isUnderlineText;
        public int start;
        public int underlineColor;
        public float underlineThickness;

        private DecorationInfo() {
            this.start = -1;
            this.end = -1;
        }

        public boolean hasDecoration() {
            return this.isStrikeThruText || this.isUnderlineText || this.underlineColor != 0;
        }

        public DecorationInfo copyInfo() {
            DecorationInfo copy = new DecorationInfo();
            copy.isStrikeThruText = this.isStrikeThruText;
            copy.isUnderlineText = this.isUnderlineText;
            copy.underlineColor = this.underlineColor;
            copy.underlineThickness = this.underlineThickness;
            return copy;
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public static TextLine obtain() {
        synchronized (sCached) {
            int i = sCached.length;
            do {
                i--;
                if (i < 0) {
                    return new TextLine();
                }
            } while (sCached[i] == null);
            TextLine tl = sCached[i];
            sCached[i] = null;
            return tl;
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public static TextLine recycle(TextLine tl) {
        tl.mText = null;
        tl.mPaint = null;
        tl.mDirections = null;
        tl.mSpanned = null;
        tl.mTabs = null;
        tl.mChars = null;
        tl.mComputed = null;
        tl.mMetricAffectingSpanSpanSet.recycle();
        tl.mCharacterStyleSpanSet.recycle();
        tl.mReplacementSpanSpanSet.recycle();
        synchronized (sCached) {
            int i = 0;
            while (true) {
                if (i >= sCached.length) {
                    break;
                } else if (sCached[i] == null) {
                    sCached[i] = tl;
                    break;
                } else {
                    i++;
                }
            }
        }
        return null;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void set(TextPaint paint, CharSequence text, int start, int limit, int dir, Layout.Directions directions, boolean hasTabs, Layout.TabStops tabStops) {
        TextPaint textPaint = paint;
        CharSequence charSequence = text;
        int i = start;
        int i2 = limit;
        Layout.Directions directions2 = directions;
        boolean z = hasTabs;
        this.mPaint = textPaint;
        this.mText = charSequence;
        this.mStart = i;
        this.mLen = i2 - i;
        this.mDir = dir;
        this.mDirections = directions2;
        if (this.mDirections != null) {
            this.mHasTabs = z;
            this.mSpanned = null;
            boolean hasReplacement = false;
            if (charSequence instanceof Spanned) {
                this.mSpanned = (Spanned) charSequence;
                this.mReplacementSpanSpanSet.init(this.mSpanned, i, i2);
                hasReplacement = this.mReplacementSpanSpanSet.numberOfSpans > 0;
            }
            this.mComputed = null;
            if (charSequence instanceof PrecomputedText) {
                this.mComputed = (PrecomputedText) charSequence;
                if (!this.mComputed.getParams().getTextPaint().equalsForTextMeasurement(textPaint)) {
                    this.mComputed = null;
                }
            }
            this.mCharsValid = hasReplacement || z || directions2 != Layout.DIRS_ALL_LEFT_TO_RIGHT;
            if (this.mCharsValid) {
                if (this.mChars == null || this.mChars.length < this.mLen) {
                    this.mChars = ArrayUtils.newUnpaddedCharArray(this.mLen);
                }
                TextUtils.getChars(charSequence, i, i2, this.mChars, 0);
                if (hasReplacement) {
                    char[] chars = this.mChars;
                    int i3 = i;
                    while (i3 < i2) {
                        int inext = this.mReplacementSpanSpanSet.getNextTransition(i3, i2);
                        if (this.mReplacementSpanSpanSet.hasSpansIntersecting(i3, inext)) {
                            chars[i3 - i] = 65532;
                            int e = inext - i;
                            for (int j = (i3 - i) + 1; j < e; j++) {
                                chars[j] = 65279;
                            }
                        }
                        i3 = inext;
                    }
                }
            }
            this.mTabs = tabStops;
            this.mAddedWidth = 0.0f;
            return;
        }
        Layout.TabStops tabStops2 = tabStops;
        throw new IllegalArgumentException("Directions cannot be null");
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void justify(float justifyWidth) {
        int end = this.mLen;
        while (end > 0 && isLineEndSpace(this.mText.charAt((this.mStart + end) - 1))) {
            end--;
        }
        int spaces = countStretchableSpaces(0, end);
        if (spaces != 0) {
            this.mAddedWidth = (justifyWidth - Math.abs(measure(end, false, null))) / ((float) spaces);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v15, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v16, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v31, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v32, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v33, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v34, resolved type: char} */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    public void draw(Canvas c, float x, int top, int y, int bottom) {
        int runLimit;
        if (!this.mHasTabs) {
            if (this.mDirections == Layout.DIRS_ALL_LEFT_TO_RIGHT) {
                drawRun(c, 0, this.mLen, false, x, top, y, bottom, false);
                return;
            } else if (this.mDirections == Layout.DIRS_ALL_RIGHT_TO_LEFT) {
                drawRun(c, 0, this.mLen, true, x, top, y, bottom, false);
                return;
            }
        }
        int[] runs = this.mDirections.mDirections;
        int lastRunIndex = runs.length - 2;
        float h = 0.0f;
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < runs.length) {
                int runStart = runs[i2];
                int runLimit2 = (runs[i2 + 1] & 67108863) + runStart;
                if (runLimit2 > this.mLen) {
                    runLimit2 = this.mLen;
                }
                int runLimit3 = runLimit2;
                boolean runIsRtl = (runs[i2 + 1] & 67108864) != 0;
                int segstart = runStart;
                int j = this.mHasTabs ? runStart : runLimit3;
                int segstart2 = segstart;
                float h2 = h;
                while (true) {
                    int j2 = j;
                    if (j2 > runLimit3) {
                        break;
                    }
                    int codept = 0;
                    if (this.mHasTabs && j2 < runLimit3) {
                        char codept2 = this.mChars[j2];
                        codept = codept2;
                        if (codept2 >= 55296) {
                            codept = codept2;
                            if (codept2 < 56320) {
                                codept = codept2;
                                if (j2 + 1 < runLimit3) {
                                    int codept3 = Character.codePointAt(this.mChars, j2);
                                    codept = codept3;
                                    if (codept3 > 65535) {
                                        j2++;
                                        runLimit = runLimit3;
                                        j = j2 + 1;
                                        runLimit3 = runLimit;
                                    }
                                }
                            }
                        }
                    }
                    int codept4 = codept;
                    if (j2 == runLimit3 || codept4 == 9) {
                        int codept5 = codept4;
                        int j3 = j2;
                        runLimit = runLimit3;
                        h2 += drawRun(c, segstart2, j2, runIsRtl, x + h2, top, y, bottom, (i2 == lastRunIndex && j2 == this.mLen) ? false : true);
                        if (codept5 == 9) {
                            h2 = ((float) this.mDir) * nextTab(((float) this.mDir) * h2);
                        }
                        segstart2 = j3 + 1;
                        j2 = j3;
                        j = j2 + 1;
                        runLimit3 = runLimit;
                    }
                    runLimit = runLimit3;
                    j = j2 + 1;
                    runLimit3 = runLimit;
                }
                i = i2 + 2;
                h = h2;
            } else {
                return;
            }
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public float metrics(Paint.FontMetricsInt fmi) {
        return measure(this.mLen, false, fmi);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v17, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v18, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v44, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v57, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v58, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v59, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v60, resolved type: char} */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    public float measure(int offset, boolean trailing, Paint.FontMetricsInt fmi) {
        float h;
        int runLimit;
        boolean runIsRtl;
        int j;
        int codept = offset;
        int target = trailing ? codept - 1 : codept;
        if (target < 0) {
            return 0.0f;
        }
        float h2 = 0.0f;
        if (!this.mHasTabs) {
            if (this.mDirections == Layout.DIRS_ALL_LEFT_TO_RIGHT) {
                return measureRun(0, codept, this.mLen, false, fmi);
            } else if (this.mDirections == Layout.DIRS_ALL_RIGHT_TO_LEFT) {
                return measureRun(0, codept, this.mLen, true, fmi);
            }
        }
        char[] chars = this.mChars;
        int[] runs = this.mDirections.mDirections;
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < runs.length) {
                int runStart = runs[i2];
                int runLimit2 = (runs[i2 + 1] & 67108863) + runStart;
                if (runLimit2 > this.mLen) {
                    runLimit2 = this.mLen;
                }
                int runLimit3 = runLimit2;
                boolean runIsRtl2 = (runs[i2 + 1] & 67108864) != 0;
                int segstart = runStart;
                int j2 = this.mHasTabs ? runStart : runLimit3;
                h = h2;
                int segstart2 = segstart;
                while (true) {
                    int j3 = j2;
                    if (j3 > runLimit3) {
                        break;
                    }
                    int codept2 = 0;
                    if (this.mHasTabs && j3 < runLimit3) {
                        char codept3 = chars[j3];
                        codept2 = codept3;
                        if (codept3 >= 55296) {
                            codept2 = codept3;
                            if (codept3 < 56320) {
                                codept2 = codept3;
                                if (j3 + 1 < runLimit3) {
                                    int codept4 = Character.codePointAt(chars, j3);
                                    codept2 = codept4;
                                    if (codept4 > 65535) {
                                        j3++;
                                        runIsRtl = runIsRtl2;
                                        runLimit = runLimit3;
                                        j2 = j3 + 1;
                                        runIsRtl2 = runIsRtl;
                                        runLimit3 = runLimit;
                                    }
                                }
                            }
                        }
                    }
                    int runLimit4 = codept2;
                    if (j3 == runLimit3 || runLimit4 == 9) {
                        boolean inSegment = target >= segstart2 && target < j3;
                        boolean advance = (this.mDir == -1) == runIsRtl2;
                        if (!inSegment || !advance) {
                            int j4 = j3;
                            runIsRtl = runIsRtl2;
                            runLimit = runLimit3;
                            int codept5 = runLimit4;
                            float w = measureRun(segstart2, j4, j4, runIsRtl, fmi);
                            h += advance ? w : -w;
                            if (inSegment) {
                                float f = w;
                                return h + measureRun(segstart2, codept, j4, runIsRtl, null);
                            }
                            if (codept5 == 9) {
                                j = j4;
                                if (codept == j) {
                                    return h;
                                }
                                float h3 = ((float) this.mDir) * nextTab(((float) this.mDir) * h);
                                if (target == j) {
                                    return h3;
                                }
                                h = h3;
                            } else {
                                j = j4;
                            }
                            segstart2 = j + 1;
                            j3 = j;
                            j2 = j3 + 1;
                            runIsRtl2 = runIsRtl;
                            runLimit3 = runLimit;
                        } else {
                            int i3 = runLimit4;
                            int i4 = j3;
                            boolean z = runIsRtl2;
                            int i5 = runLimit3;
                            return h + measureRun(segstart2, codept, j3, runIsRtl2, fmi);
                        }
                    }
                    runIsRtl = runIsRtl2;
                    runLimit = runLimit3;
                    j2 = j3 + 1;
                    runIsRtl2 = runIsRtl;
                    runLimit3 = runLimit;
                }
            } else {
                return h2;
            }
            i = i2 + 2;
            h2 = h;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v23, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v0, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v1, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v27, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v2, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v29, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v59, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v60, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v72, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v73, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v74, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v75, resolved type: char} */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    public float[] measureAllOffsets(boolean[] trailing, Paint.FontMetricsInt fmi) {
        int runLimit;
        boolean runIsRtl;
        int j;
        int j2;
        float w;
        int j3;
        int offset;
        int i = 1;
        float[] measurement = new float[(this.mLen + 1)];
        int[] target = new int[(this.mLen + 1)];
        int offset2 = 0;
        for (int offset3 = 0; offset3 < target.length; offset3++) {
            target[offset3] = trailing[offset3] ? offset3 - 1 : offset3;
        }
        if (target[0] < 0) {
            measurement[0] = 0.0f;
        }
        float h = 0.0f;
        if (!this.mHasTabs) {
            if (this.mDirections == Layout.DIRS_ALL_LEFT_TO_RIGHT) {
                while (true) {
                    int offset4 = offset2;
                    if (offset4 > this.mLen) {
                        return measurement;
                    }
                    measurement[offset4] = measureRun(0, offset4, this.mLen, false, fmi);
                    offset2 = offset4 + 1;
                }
            } else if (this.mDirections == Layout.DIRS_ALL_RIGHT_TO_LEFT) {
                while (true) {
                    int offset5 = offset2;
                    if (offset5 > this.mLen) {
                        return measurement;
                    }
                    measurement[offset5] = measureRun(0, offset5, this.mLen, true, fmi);
                    offset2 = offset5 + 1;
                }
            }
        }
        char[] chars = this.mChars;
        int[] runs = this.mDirections.mDirections;
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 >= runs.length) {
                break;
            }
            int runStart = runs[i3];
            int runLimit2 = (runs[i3 + 1] & 67108863) + runStart;
            if (runLimit2 > this.mLen) {
                runLimit2 = this.mLen;
            }
            int runLimit3 = runLimit2;
            boolean runIsRtl2 = (runs[i3 + 1] & 67108864) != 0 ? i : offset2;
            int segstart = runStart;
            int j4 = this.mHasTabs ? runStart : runLimit3;
            float h2 = h;
            int segstart2 = segstart;
            while (true) {
                int j5 = j4;
                if (j5 > runLimit3) {
                    break;
                }
                int codept = 0;
                if (this.mHasTabs && j5 < runLimit3) {
                    char codept2 = chars[j5];
                    codept = codept2;
                    if (codept2 >= 55296) {
                        codept = codept2;
                        if (codept2 < 56320) {
                            codept = codept2;
                            if (j5 + 1 < runLimit3) {
                                int codept3 = Character.codePointAt(chars, j5);
                                codept = codept3;
                                if (codept3 > 65535) {
                                    j5++;
                                    runIsRtl = runIsRtl2;
                                    runLimit = runLimit3;
                                    j4 = j5 + 1;
                                    i = 1;
                                    runIsRtl2 = runIsRtl;
                                    runLimit3 = runLimit;
                                    offset2 = 0;
                                }
                            }
                        }
                    }
                }
                int codept4 = codept;
                if (j5 == runLimit3 || codept4 == 9) {
                    float oldh = h2;
                    int i4 = (this.mDir == -1 ? i : offset2) == runIsRtl2 ? i : offset2;
                    int codept5 = codept4;
                    int offset6 = j5;
                    runIsRtl = runIsRtl2;
                    runLimit = runLimit3;
                    float w2 = measureRun(segstart2, j5, j5, runIsRtl2, fmi);
                    h2 += i4 != 0 ? w2 : -w2;
                    float baseh = i4 != 0 ? oldh : h2;
                    Paint.FontMetricsInt crtfmi = i4 != 0 ? fmi : null;
                    int offset7 = segstart2;
                    while (true) {
                        int offset8 = offset7;
                        j = offset6;
                        if (offset8 > j || offset8 > this.mLen) {
                            int j6 = j;
                            float f = w2;
                        } else {
                            if (target[offset8] < segstart2 || target[offset8] >= j) {
                                j3 = j;
                                offset = offset8;
                                w = w2;
                            } else {
                                j3 = j;
                                offset = offset8;
                                w = w2;
                                measurement[offset] = baseh + measureRun(segstart2, offset8, j3, runIsRtl, crtfmi);
                            }
                            offset7 = offset + 1;
                            offset6 = j3;
                            w2 = w;
                        }
                    }
                    int j62 = j;
                    float f2 = w2;
                    if (codept5 == 9) {
                        j2 = j62;
                        if (target[j2] == j2) {
                            measurement[j2] = h2;
                        }
                        float h3 = ((float) this.mDir) * nextTab(((float) this.mDir) * h2);
                        if (target[j2 + 1] == j2) {
                            measurement[j2 + 1] = h3;
                        }
                        h2 = h3;
                    } else {
                        j2 = j62;
                    }
                    segstart2 = j2 + 1;
                    j5 = j2;
                    j4 = j5 + 1;
                    i = 1;
                    runIsRtl2 = runIsRtl;
                    runLimit3 = runLimit;
                    offset2 = 0;
                }
                runIsRtl = runIsRtl2;
                runLimit = runLimit3;
                j4 = j5 + 1;
                i = 1;
                runIsRtl2 = runIsRtl;
                runLimit3 = runLimit;
                offset2 = 0;
            }
            int i5 = i;
            i2 = i3 + 2;
            h = h2;
            offset2 = 0;
        }
        if (target[this.mLen] == this.mLen) {
            measurement[this.mLen] = h;
        }
        return measurement;
    }

    private float drawRun(Canvas c, int start, int limit, boolean runIsRtl, float x, int top, int y, int bottom, boolean needWidth) {
        boolean z = true;
        if (this.mDir != 1) {
            z = false;
        }
        boolean z2 = runIsRtl;
        if (z != z2) {
            return handleRun(start, limit, limit, z2, c, x, top, y, bottom, null, needWidth);
        }
        int i = start;
        int i2 = limit;
        int i3 = limit;
        boolean z3 = z2;
        float w = -measureRun(i, i2, i3, z3, null);
        handleRun(i, i2, i3, z3, c, x + w, top, y, bottom, null, false);
        return w;
    }

    private float measureRun(int start, int offset, int limit, boolean runIsRtl, Paint.FontMetricsInt fmi) {
        return handleRun(start, offset, limit, runIsRtl, null, 0.0f, 0, 0, 0, fmi, true);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x0178, code lost:
        if (r13 != -1) goto L_0x0183;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x017a, code lost:
        if (r0 == false) goto L_0x0181;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x017c, code lost:
        r1 = r7.mLen + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x0181, code lost:
        r13 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x0183, code lost:
        if (r13 > r11) goto L_0x018b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x0185, code lost:
        if (r0 == false) goto L_0x0189;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:0x0187, code lost:
        r1 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x0189, code lost:
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x018a, code lost:
        r13 = r1;
     */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x0175 A[ADDED_TO_REGION, EDGE_INSN: B:125:0x0175->B:106:0x0175 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x00fe  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0100  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0103  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0105  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x010a  */
    public int getOffsetToLeftRightOf(int cursor, boolean toLeft) {
        int runLevel;
        int newCaret;
        int runIndex;
        int[] runs;
        int otherRunIndex;
        int otherRunLevel;
        int otherRunIndex2;
        int runLevel2;
        int runStart;
        boolean trailing;
        int runLimit;
        int runStart2;
        int runStart3;
        int runIndex2;
        int i = cursor;
        boolean z = toLeft;
        int lineEnd = this.mLen;
        boolean paraIsRtl = this.mDir == -1;
        int[] runs2 = this.mDirections.mDirections;
        int runLimit2 = lineEnd;
        boolean trailing2 = false;
        if (i == 0) {
            runIndex2 = -2;
        } else if (i == lineEnd) {
            runIndex2 = runs2.length;
        } else {
            int runLimit3 = runLimit2;
            int runLimit4 = 0;
            int runIndex3 = 0;
            while (true) {
                if (runIndex3 >= runs2.length) {
                    runLevel2 = 0;
                    runIndex = runIndex3;
                    runStart = runLimit4;
                    trailing = false;
                    runLimit = runLimit3;
                    break;
                }
                int runStart4 = 0 + runs2[runIndex3];
                if (i >= runStart4) {
                    int runLimit5 = (runs2[runIndex3 + 1] & 67108863) + runStart4;
                    if (runLimit5 > lineEnd) {
                        runLimit5 = lineEnd;
                    }
                    if (i < runLimit5) {
                        int runLevel3 = (runs2[runIndex3 + 1] >>> 26) & 63;
                        if (i == runStart4) {
                            int pos = i - 1;
                            int prevRunIndex = 0;
                            while (true) {
                                int prevRunIndex2 = prevRunIndex;
                                if (prevRunIndex2 >= runs2.length) {
                                    runStart = runStart4;
                                    break;
                                }
                                int prevRunStart = runs2[prevRunIndex2] + 0;
                                if (pos >= prevRunStart) {
                                    int prevRunLimit = prevRunStart + (runs2[prevRunIndex2 + 1] & 67108863);
                                    if (prevRunLimit > lineEnd) {
                                        prevRunLimit = lineEnd;
                                    }
                                    if (pos < prevRunLimit) {
                                        runStart3 = runStart4;
                                        int runStart5 = (runs2[prevRunIndex2 + 1] >>> 26) & 63;
                                        if (runStart5 < runLevel3) {
                                            runIndex3 = prevRunIndex2;
                                            runLevel3 = runStart5;
                                            runStart = prevRunStart;
                                            runLimit5 = prevRunLimit;
                                            trailing2 = true;
                                            break;
                                        }
                                        prevRunIndex = prevRunIndex2 + 2;
                                        runStart4 = runStart3;
                                    }
                                }
                                runStart3 = runStart4;
                                prevRunIndex = prevRunIndex2 + 2;
                                runStart4 = runStart3;
                            }
                            runLevel2 = runLevel3;
                            runIndex = runIndex3;
                            runLimit = runLimit5;
                            trailing = trailing2;
                        } else {
                            runLevel2 = runLevel3;
                            runIndex = runIndex3;
                            runLimit = runLimit5;
                            runStart = runStart4;
                            trailing = false;
                        }
                    } else {
                        runStart2 = runStart4;
                        runLimit3 = runLimit5;
                    }
                } else {
                    runStart2 = runStart4;
                }
                runIndex3 += 2;
                runLimit4 = runStart2;
            }
            if (runIndex != runs2.length) {
                boolean runIsRtl = (runLevel2 & 1) != 0;
                boolean advance = z == runIsRtl;
                if (i == (advance ? runLimit : runStart) && advance == trailing) {
                    boolean z2 = trailing;
                    runs = runs2;
                } else {
                    boolean advance2 = advance;
                    boolean z3 = runIsRtl;
                    boolean z4 = trailing;
                    int i2 = i;
                    runs = runs2;
                    int newCaret2 = getOffsetBeforeAfter(runIndex, runStart, runLimit, runIsRtl, i2, advance2);
                    if (newCaret2 != (advance2 ? runLimit : runStart)) {
                        return newCaret2;
                    }
                    int i3 = runLimit;
                    runLevel = runLevel2;
                    newCaret = newCaret2;
                    while (true) {
                        boolean advance3 = z == paraIsRtl;
                        otherRunIndex = runIndex + (advance3 ? 2 : -2);
                        if (otherRunIndex >= 0 || otherRunIndex >= runs.length) {
                            int i4 = -1;
                        } else {
                            int otherRunStart = 0 + runs[otherRunIndex];
                            int otherRunLimit = otherRunStart + (runs[otherRunIndex + 1] & 67108863);
                            if (otherRunLimit > lineEnd) {
                                otherRunLimit = lineEnd;
                            }
                            int otherRunLimit2 = otherRunLimit;
                            int otherRunLevel2 = (runs[otherRunIndex + 1] >>> 26) & 63;
                            boolean otherRunIsRtl = (otherRunLevel2 & 1) != 0;
                            boolean advance4 = z == otherRunIsRtl;
                            if (newCaret == -1) {
                                boolean z5 = otherRunIsRtl;
                                otherRunLevel = otherRunLevel2;
                                otherRunIndex2 = otherRunIndex;
                                newCaret = getOffsetBeforeAfter(otherRunIndex, otherRunStart, otherRunLimit2, otherRunIsRtl, advance4 ? otherRunStart : otherRunLimit2, advance4);
                                if (newCaret != (advance4 ? otherRunLimit2 : otherRunStart)) {
                                    break;
                                }
                                runIndex = otherRunIndex2;
                                runLevel = otherRunLevel;
                            } else {
                                int i5 = otherRunIndex;
                                if (otherRunLevel2 < runLevel) {
                                    newCaret = advance4 ? otherRunStart : otherRunLimit2;
                                }
                            }
                        }
                    }
                    return newCaret;
                }
            } else {
                runs = runs2;
            }
            int i6 = runLimit;
            runLevel = runLevel2;
            newCaret = -1;
            int newCaret3 = i6;
            while (true) {
                if (z == paraIsRtl) {
                }
                otherRunIndex = runIndex + (advance3 ? 2 : -2);
                if (otherRunIndex >= 0) {
                    break;
                }
                break;
                runIndex = otherRunIndex2;
                runLevel = otherRunLevel;
            }
            return newCaret;
        }
        runLevel = 0;
        runIndex = runIndex2;
        runs = runs2;
        newCaret = -1;
        while (true) {
            if (z == paraIsRtl) {
            }
            otherRunIndex = runIndex + (advance3 ? 2 : -2);
            if (otherRunIndex >= 0) {
            }
            runIndex = otherRunIndex2;
            runLevel = otherRunLevel;
        }
        return newCaret;
    }

    private int getOffsetBeforeAfter(int runIndex, int runStart, int runLimit, boolean runIsRtl, int offset, boolean after) {
        int spanLimit;
        int spanStart;
        int spanLimit2;
        int i = offset;
        if (runIndex >= 0) {
            int i2 = 0;
            if (i != (after ? this.mLen : 0)) {
                TextPaint wp = this.mWorkPaint;
                wp.set(this.mPaint);
                wp.setWordSpacing(this.mAddedWidth);
                int spanStart2 = runStart;
                if (this.mSpanned == null) {
                    spanStart = spanStart2;
                    spanLimit = runLimit;
                } else {
                    int target = after ? i + 1 : i;
                    int limit = this.mStart + runLimit;
                    while (true) {
                        spanLimit2 = this.mSpanned.nextSpanTransition(this.mStart + spanStart2, limit, MetricAffectingSpan.class) - this.mStart;
                        if (spanLimit2 >= target) {
                            break;
                        }
                        spanStart2 = spanLimit2;
                    }
                    MetricAffectingSpan[] spans = (MetricAffectingSpan[]) TextUtils.removeEmptySpans((MetricAffectingSpan[]) this.mSpanned.getSpans(this.mStart + spanStart2, this.mStart + spanLimit2, MetricAffectingSpan.class), this.mSpanned, MetricAffectingSpan.class);
                    if (spans.length > 0) {
                        ReplacementSpan replacement = null;
                        for (MetricAffectingSpan span : spans) {
                            if (span instanceof ReplacementSpan) {
                                replacement = (ReplacementSpan) span;
                            } else {
                                span.updateMeasureState(wp);
                            }
                        }
                        if (replacement != null) {
                            return after ? spanLimit2 : spanStart2;
                        }
                    }
                    spanStart = spanStart2;
                    spanLimit = spanLimit2;
                }
                int spanLimit3 = runIsRtl;
                if (!after) {
                    i2 = 2;
                }
                int cursorOpt = i2;
                if (this.mCharsValid) {
                    return wp.getTextRunCursor(this.mChars, spanStart, spanLimit - spanStart, (int) spanLimit3, i, cursorOpt);
                }
                TextPaint textPaint = wp;
                return wp.getTextRunCursor(this.mText, this.mStart + spanStart, this.mStart + spanLimit, (int) spanLimit3, this.mStart + i, cursorOpt) - this.mStart;
            }
        }
        if (after) {
            return TextUtils.getOffsetAfter(this.mText, this.mStart + i) - this.mStart;
        }
        return TextUtils.getOffsetBefore(this.mText, this.mStart + i) - this.mStart;
    }

    private static void expandMetricsFromPaint(Paint.FontMetricsInt fmi, TextPaint wp) {
        int previousTop = fmi.top;
        int previousAscent = fmi.ascent;
        int previousDescent = fmi.descent;
        int previousBottom = fmi.bottom;
        int previousLeading = fmi.leading;
        wp.getFontMetricsInt(fmi);
        updateMetrics(fmi, previousTop, previousAscent, previousDescent, previousBottom, previousLeading);
    }

    static void updateMetrics(Paint.FontMetricsInt fmi, int previousTop, int previousAscent, int previousDescent, int previousBottom, int previousLeading) {
        fmi.top = Math.min(fmi.top, previousTop);
        fmi.ascent = Math.min(fmi.ascent, previousAscent);
        fmi.descent = Math.max(fmi.descent, previousDescent);
        fmi.bottom = Math.max(fmi.bottom, previousBottom);
        fmi.leading = Math.max(fmi.leading, previousLeading);
    }

    private static void drawStroke(TextPaint wp, Canvas c, int color, float position, float thickness, float xleft, float xright, float baseline) {
        TextPaint textPaint = wp;
        float strokeTop = baseline + ((float) textPaint.baselineShift) + position;
        int previousColor = textPaint.getColor();
        Paint.Style previousStyle = textPaint.getStyle();
        boolean previousAntiAlias = textPaint.isAntiAlias();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);
        textPaint.setColor(color);
        c.drawRect(xleft, strokeTop, xright, strokeTop + thickness, textPaint);
        textPaint.setStyle(previousStyle);
        textPaint.setColor(previousColor);
        textPaint.setAntiAlias(previousAntiAlias);
    }

    private float getRunAdvance(TextPaint wp, int start, int end, int contextStart, int contextEnd, boolean runIsRtl, int offset) {
        if (this.mCharsValid) {
            return wp.getRunAdvance(this.mChars, start, end, contextStart, contextEnd, runIsRtl, offset);
        }
        int delta = this.mStart;
        if (this.mComputed != null) {
            return this.mComputed.getWidth(start + delta, end + delta);
        }
        return wp.getRunAdvance(this.mText, delta + start, delta + end, delta + contextStart, delta + contextEnd, runIsRtl, delta + offset);
    }

    private float handleText(TextPaint wp, int start, int end, int contextStart, int contextEnd, boolean runIsRtl, Canvas c, float x, int top, int y, int bottom, Paint.FontMetricsInt fmi, boolean needWidth, int offset, ArrayList<DecorationInfo> decorations) {
        int numDecorations;
        float totalWidth;
        float leftX;
        float rightX;
        float decorationXLeft;
        float f;
        int numDecorations2;
        float totalWidth2;
        int i;
        DecorationInfo info;
        TextPaint textPaint = wp;
        int i2 = start;
        int i3 = y;
        Paint.FontMetricsInt fontMetricsInt = fmi;
        ArrayList<DecorationInfo> arrayList = decorations;
        TextLine textLine = this;
        textPaint.setWordSpacing(textLine.mAddedWidth);
        if (fontMetricsInt != null) {
            expandMetricsFromPaint(fontMetricsInt, textPaint);
        }
        int i4 = end;
        if (i4 == i2) {
            return 0.0f;
        }
        float totalWidth3 = 0.0f;
        int decorationStart = 0;
        int numDecorations3 = arrayList == null ? 0 : decorations.size();
        if (needWidth || !(c == null || (textPaint.bgColor == 0 && numDecorations3 == 0 && !runIsRtl))) {
            numDecorations = numDecorations3;
            totalWidth3 = textLine.getRunAdvance(textPaint, i2, i4, contextStart, contextEnd, runIsRtl, offset);
        } else {
            numDecorations = numDecorations3;
        }
        if (c != null) {
            if (runIsRtl) {
                leftX = x - totalWidth3;
                rightX = x;
            } else {
                leftX = x;
                rightX = x + totalWidth3;
            }
            float leftX2 = leftX;
            float rightX2 = rightX;
            if (textPaint.bgColor != 0) {
                int previousColor = wp.getColor();
                Paint.Style previousStyle = wp.getStyle();
                textPaint.setColor(textPaint.bgColor);
                textPaint.setStyle(Paint.Style.FILL);
                c.drawRect(leftX2, (float) top, rightX2, (float) bottom, textPaint);
                textPaint.setStyle(previousStyle);
                textPaint.setColor(previousColor);
            }
            if (numDecorations != 0) {
                while (true) {
                    int i5 = decorationStart;
                    if (i5 >= numDecorations) {
                        break;
                    }
                    DecorationInfo info2 = arrayList.get(i5);
                    int decorationStart2 = Math.max(info2.start, i2);
                    int decorationEnd = Math.min(info2.end, offset);
                    TextLine textLine2 = textLine;
                    TextPaint textPaint2 = textPaint;
                    int i6 = i2;
                    int i7 = i4;
                    int i8 = contextStart;
                    int i9 = contextEnd;
                    int numDecorations4 = numDecorations;
                    DecorationInfo info3 = info2;
                    boolean z = runIsRtl;
                    int i10 = i5;
                    float decorationStartAdvance = textLine2.getRunAdvance(textPaint2, i6, i7, i8, i9, z, decorationStart2);
                    float decorationEndAdvance = textLine2.getRunAdvance(textPaint2, i6, i7, i8, i9, z, decorationEnd);
                    if (runIsRtl) {
                        decorationXLeft = rightX2 - decorationEndAdvance;
                        f = rightX2 - decorationStartAdvance;
                    } else {
                        decorationXLeft = leftX2 + decorationStartAdvance;
                        f = leftX2 + decorationEndAdvance;
                    }
                    float decorationXLeft2 = decorationXLeft;
                    float decorationXRight = f;
                    if (info3.underlineColor != 0) {
                        drawStroke(textPaint, c, info3.underlineColor, wp.getUnderlinePosition(), info3.underlineThickness, decorationXLeft2, decorationXRight, (float) i3);
                    }
                    if (info3.isUnderlineText) {
                        totalWidth2 = totalWidth3;
                        info = info3;
                        numDecorations2 = numDecorations4;
                        i = i3;
                        drawStroke(textPaint, c, wp.getColor(), wp.getUnderlinePosition(), Math.max(wp.getUnderlineThickness(), 1.0f), decorationXLeft2, decorationXRight, (float) i3);
                    } else {
                        totalWidth2 = totalWidth3;
                        info = info3;
                        i = i3;
                        numDecorations2 = numDecorations4;
                    }
                    if (info.isStrikeThruText) {
                        drawStroke(textPaint, c, wp.getColor(), wp.getStrikeThruPosition(), Math.max(wp.getStrikeThruThickness(), 1.0f), decorationXLeft2, decorationXRight, (float) i);
                    }
                    decorationStart = i10 + 1;
                    textLine = this;
                    i2 = start;
                    i4 = end;
                    arrayList = decorations;
                    i3 = i;
                    totalWidth3 = totalWidth2;
                    numDecorations = numDecorations2;
                }
            }
            int i11 = numDecorations;
            totalWidth = totalWidth3;
            drawTextRun(c, textPaint, start, end, contextStart, contextEnd, runIsRtl, leftX2, i3 + textPaint.baselineShift);
        } else {
            totalWidth = totalWidth3;
            int i12 = numDecorations;
        }
        return runIsRtl ? -totalWidth : totalWidth;
    }

    private float handleReplacement(ReplacementSpan replacement, TextPaint wp, int start, int limit, boolean runIsRtl, Canvas c, float x, int top, int y, int bottom, Paint.FontMetricsInt fmi, boolean needWidth) {
        float ret;
        float x2;
        Paint.FontMetricsInt fontMetricsInt = fmi;
        float ret2 = 0.0f;
        int textStart = this.mStart + start;
        int textLimit = this.mStart + limit;
        if (needWidth || (c != null && runIsRtl)) {
            int previousTop = 0;
            int previousAscent = 0;
            int previousDescent = 0;
            int previousBottom = 0;
            int previousLeading = 0;
            boolean needUpdateMetrics = fontMetricsInt != null;
            if (needUpdateMetrics) {
                previousTop = fontMetricsInt.top;
                previousAscent = fontMetricsInt.ascent;
                previousDescent = fontMetricsInt.descent;
                previousBottom = fontMetricsInt.bottom;
                previousLeading = fontMetricsInt.leading;
            }
            int previousTop2 = previousTop;
            int previousAscent2 = previousAscent;
            int previousDescent2 = previousDescent;
            int previousBottom2 = previousBottom;
            int previousLeading2 = previousLeading;
            ret2 = (float) replacement.getSize(wp, this.mText, textStart, textLimit, fontMetricsInt);
            if (needUpdateMetrics) {
                updateMetrics(fontMetricsInt, previousTop2, previousAscent2, previousDescent2, previousBottom2, previousLeading2);
            }
        }
        float ret3 = ret2;
        if (c != null) {
            if (runIsRtl) {
                x2 = x - ret3;
            } else {
                x2 = x;
            }
            ret = ret3;
            replacement.draw(c, this.mText, textStart, textLimit, x2, top, y, bottom, wp);
            float f = x2;
        } else {
            ret = ret3;
            float f2 = x;
        }
        return runIsRtl ? -ret : ret;
    }

    private int adjustHyphenEdit(int start, int limit, int hyphenEdit) {
        int result = hyphenEdit;
        if (start > 0) {
            result &= -25;
        }
        if (limit < this.mLen) {
            return result & -8;
        }
        return result;
    }

    private void extractDecorationInfo(TextPaint paint, DecorationInfo info) {
        info.isStrikeThruText = paint.isStrikeThruText();
        if (info.isStrikeThruText) {
            paint.setStrikeThruText(false);
        }
        info.isUnderlineText = paint.isUnderlineText();
        if (info.isUnderlineText) {
            paint.setUnderlineText(false);
        }
        info.underlineColor = paint.underlineColor;
        info.underlineThickness = paint.underlineThickness;
        paint.setUnderlineText(0, 0.0f);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v25, resolved type: android.text.style.MetricAffectingSpan[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v26, resolved type: android.text.style.ImageSpan} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x023b  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x024d  */
    private float handleRun(int start, int measureLimit, int limit, boolean runIsRtl, Canvas c, float x, int top, int y, int bottom, Paint.FontMetricsInt fmi, boolean needWidth) {
        boolean needsSpanMeasurement;
        int inext;
        int mlimit;
        boolean z;
        DecorationInfo decorationInfo;
        int j;
        int mlimit2;
        int jnext;
        int i;
        TextPaint wp;
        TextPaint activePaint;
        TextLine textLine;
        int jnext2;
        int i2;
        boolean z2;
        TextPaint wp2;
        int i3 = start;
        int i4 = measureLimit;
        int i5 = limit;
        Paint.FontMetricsInt fontMetricsInt = fmi;
        if (i4 < i3 || i4 > i5) {
            throw new IndexOutOfBoundsException("measureLimit (" + measureLimit + ") is out of start (" + start + ") and limit (" + limit + ") bounds");
        } else if (i3 == i4) {
            TextPaint wp3 = this.mWorkPaint;
            wp3.set(this.mPaint);
            if (fontMetricsInt != null) {
                expandMetricsFromPaint(fontMetricsInt, wp3);
            }
            return 0.0f;
        } else {
            if (this.mSpanned == null) {
                needsSpanMeasurement = false;
            } else {
                this.mMetricAffectingSpanSpanSet.init(this.mSpanned, this.mStart + i3, this.mStart + i5);
                this.mCharacterStyleSpanSet.init(this.mSpanned, this.mStart + i3, this.mStart + i5);
                needsSpanMeasurement = (this.mMetricAffectingSpanSpanSet.numberOfSpans == 0 && this.mCharacterStyleSpanSet.numberOfSpans == 0) ? false : true;
            }
            if (!needsSpanMeasurement) {
                TextPaint wp4 = this.mWorkPaint;
                wp4.set(this.mPaint);
                wp4.setHyphenEdit(adjustHyphenEdit(i3, i5, wp4.getHyphenEdit()));
                TextPaint textPaint = wp4;
                return handleText(wp4, i3, i5, i3, i5, runIsRtl, c, x, top, y, bottom, fmi, needWidth, measureLimit, null);
            }
            float originalX = x;
            float x2 = x;
            int i6 = start;
            while (true) {
                int i7 = i6;
                int i8 = measureLimit;
                if (i7 >= i8) {
                    return x2 - originalX;
                }
                TextPaint wp5 = this.mWorkPaint;
                wp5.set(this.mPaint);
                int inext2 = this.mMetricAffectingSpanSpanSet.getNextTransition(this.mStart + i7, this.mStart + limit) - this.mStart;
                int mlimit3 = Math.min(inext2, i8);
                ReplacementSpan replacement = null;
                for (int j2 = 0; j2 < this.mMetricAffectingSpanSpanSet.numberOfSpans; j2++) {
                    if (this.mMetricAffectingSpanSpanSet.spanStarts[j2] < this.mStart + mlimit3 && this.mMetricAffectingSpanSpanSet.spanEnds[j2] > this.mStart + i7) {
                        ImageSpan replacement2 = ((MetricAffectingSpan[]) this.mMetricAffectingSpanSpanSet.spans)[j2];
                        if (replacement2 instanceof ReplacementSpan) {
                            replacement = replacement2;
                        } else {
                            replacement2.updateDrawState(wp5);
                        }
                    }
                }
                if (replacement != null) {
                    boolean z3 = needWidth || mlimit3 < i8;
                    int i9 = mlimit3;
                    inext = inext2;
                    TextPaint textPaint2 = wp5;
                    x2 += handleReplacement(replacement, wp5, i7, mlimit3, runIsRtl, c, x2, top, y, bottom, fmi, z3);
                    int i10 = i7;
                } else {
                    int mlimit4 = mlimit3;
                    inext = inext2;
                    TextPaint wp6 = wp5;
                    TextLine textLine2 = this;
                    TextPaint activePaint2 = textLine2.mActivePaint;
                    activePaint2.set(textLine2.mPaint);
                    int activeStart = i7;
                    DecorationInfo decorationInfo2 = textLine2.mDecorationInfo;
                    textLine2.mDecorations.clear();
                    int activeEnd = mlimit4;
                    float x3 = x2;
                    int activeStart2 = activeStart;
                    while (true) {
                        int j3 = activeStart;
                        mlimit = mlimit4;
                        if (j3 >= mlimit) {
                            break;
                        }
                        int jnext3 = textLine2.mCharacterStyleSpanSet.getNextTransition(textLine2.mStart + j3, textLine2.mStart + inext) - textLine2.mStart;
                        int offset = Math.min(jnext3, mlimit);
                        TextPaint wp7 = wp6;
                        wp7.set(textLine2.mPaint);
                        for (int k = 0; k < textLine2.mCharacterStyleSpanSet.numberOfSpans; k++) {
                            if (textLine2.mCharacterStyleSpanSet.spanStarts[k] < textLine2.mStart + offset && textLine2.mCharacterStyleSpanSet.spanEnds[k] > textLine2.mStart + j3) {
                                textLine2.mCharacterStyleSpanSet.spans[k].updateDrawState(wp7);
                            }
                        }
                        textLine2.extractDecorationInfo(wp7, decorationInfo2);
                        if (j3 == i7) {
                            activePaint2.set(wp7);
                            jnext = jnext3;
                            mlimit2 = mlimit;
                            j = j3;
                            int i11 = activeEnd;
                            decorationInfo = decorationInfo2;
                            i2 = activeStart2;
                            i = i7;
                            wp = wp7;
                            TextLine textLine3 = textLine2;
                            activePaint = activePaint2;
                            textLine = textLine3;
                        } else if (!wp7.hasEqualAttributes(activePaint2)) {
                            activePaint2.setHyphenEdit(textLine2.adjustHyphenEdit(activeStart2, activeEnd, textLine2.mPaint.getHyphenEdit()));
                            if (!needWidth) {
                                wp2 = wp7;
                                if (activeEnd >= measureLimit) {
                                    z2 = false;
                                    jnext = jnext3;
                                    mlimit2 = mlimit;
                                    j = j3;
                                    int i12 = activeEnd;
                                    decorationInfo = decorationInfo2;
                                    int i13 = activeStart2;
                                    i = i7;
                                    x3 += textLine2.handleText(activePaint2, activeStart2, activeEnd, i7, inext, runIsRtl, c, x3, top, y, bottom, fmi, z2, Math.min(activeEnd, mlimit), textLine2.mDecorations);
                                    activeStart2 = j;
                                    wp = wp2;
                                    activePaint = activePaint2;
                                    activePaint.set(wp);
                                    textLine = this;
                                    textLine.mDecorations.clear();
                                    activeEnd = jnext;
                                    decorationInfo2 = decorationInfo;
                                    if (decorationInfo2.hasDecoration()) {
                                        DecorationInfo copy = decorationInfo2.copyInfo();
                                        copy.start = j;
                                        jnext2 = jnext;
                                        copy.end = jnext2;
                                        textLine.mDecorations.add(copy);
                                    } else {
                                        jnext2 = jnext;
                                        int i14 = j;
                                    }
                                    activeStart = jnext2;
                                    wp6 = wp;
                                    i7 = i;
                                    mlimit4 = mlimit2;
                                    TextPaint textPaint3 = activePaint;
                                    textLine2 = textLine;
                                    activePaint2 = textPaint3;
                                }
                            } else {
                                wp2 = wp7;
                                int i15 = measureLimit;
                            }
                            z2 = true;
                            jnext = jnext3;
                            mlimit2 = mlimit;
                            j = j3;
                            int i122 = activeEnd;
                            decorationInfo = decorationInfo2;
                            int i132 = activeStart2;
                            i = i7;
                            x3 += textLine2.handleText(activePaint2, activeStart2, activeEnd, i7, inext, runIsRtl, c, x3, top, y, bottom, fmi, z2, Math.min(activeEnd, mlimit), textLine2.mDecorations);
                            activeStart2 = j;
                            wp = wp2;
                            activePaint = activePaint2;
                            activePaint.set(wp);
                            textLine = this;
                            textLine.mDecorations.clear();
                            activeEnd = jnext;
                            decorationInfo2 = decorationInfo;
                            if (decorationInfo2.hasDecoration()) {
                            }
                            activeStart = jnext2;
                            wp6 = wp;
                            i7 = i;
                            mlimit4 = mlimit2;
                            TextPaint textPaint32 = activePaint;
                            textLine2 = textLine;
                            activePaint2 = textPaint32;
                        } else {
                            jnext = jnext3;
                            mlimit2 = mlimit;
                            j = j3;
                            int i16 = activeEnd;
                            decorationInfo = decorationInfo2;
                            i2 = activeStart2;
                            i = i7;
                            wp = wp7;
                            TextLine textLine4 = textLine2;
                            activePaint = activePaint2;
                            textLine = textLine4;
                        }
                        activeStart2 = i2;
                        activeEnd = jnext;
                        decorationInfo2 = decorationInfo;
                        if (decorationInfo2.hasDecoration()) {
                        }
                        activeStart = jnext2;
                        wp6 = wp;
                        i7 = i;
                        mlimit4 = mlimit2;
                        TextPaint textPaint322 = activePaint;
                        textLine2 = textLine;
                        activePaint2 = textPaint322;
                    }
                    int mlimit5 = mlimit;
                    int i17 = activeEnd;
                    int i18 = activeStart2;
                    int i19 = i7;
                    TextPaint wp8 = wp6;
                    TextLine textLine5 = textLine2;
                    TextPaint activePaint3 = activePaint2;
                    TextLine textLine6 = textLine5;
                    activePaint3.setHyphenEdit(textLine6.adjustHyphenEdit(activeStart2, activeEnd, textLine6.mPaint.getHyphenEdit()));
                    if (needWidth) {
                        int i20 = measureLimit;
                    } else if (activeEnd >= measureLimit) {
                        z = false;
                        int mlimit6 = mlimit5;
                        int i21 = mlimit6;
                        int i22 = activeEnd;
                        DecorationInfo decorationInfo3 = decorationInfo2;
                        int i23 = activeStart2;
                        boolean z4 = z;
                        TextPaint textPaint4 = activePaint3;
                        TextPaint textPaint5 = wp8;
                        x2 = x3 + textLine6.handleText(activePaint3, activeStart2, activeEnd, i19, inext, runIsRtl, c, x3, top, y, bottom, fmi, z4, Math.min(activeEnd, mlimit6), textLine6.mDecorations);
                    }
                    z = true;
                    int mlimit62 = mlimit5;
                    int i212 = mlimit62;
                    int i222 = activeEnd;
                    DecorationInfo decorationInfo32 = decorationInfo2;
                    int i232 = activeStart2;
                    boolean z42 = z;
                    TextPaint textPaint42 = activePaint3;
                    TextPaint textPaint52 = wp8;
                    x2 = x3 + textLine6.handleText(activePaint3, activeStart2, activeEnd, i19, inext, runIsRtl, c, x3, top, y, bottom, fmi, z42, Math.min(activeEnd, mlimit62), textLine6.mDecorations);
                }
                i6 = inext;
            }
        }
    }

    private void drawTextRun(Canvas c, TextPaint wp, int start, int end, int contextStart, int contextEnd, boolean runIsRtl, float x, int y) {
        int i = y;
        if (this.mCharsValid) {
            c.drawTextRun(this.mChars, start, end - start, contextStart, contextEnd - contextStart, x, (float) i, runIsRtl, wp);
            return;
        }
        int delta = this.mStart;
        c.drawTextRun(this.mText, delta + start, delta + end, delta + contextStart, delta + contextEnd, x, (float) i, runIsRtl, wp);
    }

    /* access modifiers changed from: package-private */
    public float nextTab(float h) {
        if (this.mTabs != null) {
            return this.mTabs.nextTab(h);
        }
        return Layout.TabStops.nextDefaultStop(h, 20);
    }

    private boolean isStretchableWhitespace(int ch) {
        return ch == 32;
    }

    private int countStretchableSpaces(int start, int end) {
        int count = 0;
        for (int i = start; i < end; i++) {
            if (isStretchableWhitespace(this.mCharsValid ? this.mChars[i] : this.mText.charAt(this.mStart + i))) {
                count++;
            }
        }
        return count;
    }

    public static boolean isLineEndSpace(char ch) {
        return ch == ' ' || ch == 9 || ch == 5760 || (8192 <= ch && ch <= 8202 && ch != 8199) || ch == 8287 || ch == 12288;
    }
}
