package android.text;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.Layout;
import android.text.TextUtils;
import android.text.style.ParagraphStyle;
import java.util.Locale;

public class BoringLayout extends Layout implements TextUtils.EllipsizeCallback {
    private static final int ROWHEIGHTOFFSET_BO = 7;
    private static final int ROWHEIGHTOFFSET_MY = 3;
    int mBottom;
    private int mBottomPadding;
    int mDesc;
    private String mDirect;
    private int mEllipsizedCount;
    private int mEllipsizedStart;
    private int mEllipsizedWidth;
    private float mMax;
    private Paint mPaint;
    private int mTopPadding;

    public static class Metrics extends Paint.FontMetricsInt {
        public int width;

        public String toString() {
            return super.toString() + " width=" + this.width;
        }

        /* access modifiers changed from: private */
        public void reset() {
            this.top = 0;
            this.bottom = 0;
            this.ascent = 0;
            this.descent = 0;
            this.width = 0;
            this.leading = 0;
        }
    }

    public static BoringLayout make(CharSequence source, TextPaint paint, int outerWidth, Layout.Alignment align, float spacingMult, float spacingAdd, Metrics metrics, boolean includePad) {
        BoringLayout boringLayout = new BoringLayout(source, paint, outerWidth, align, spacingMult, spacingAdd, metrics, includePad);
        return boringLayout;
    }

    public static BoringLayout make(CharSequence source, TextPaint paint, int outerWidth, Layout.Alignment align, float spacingmult, float spacingadd, Metrics metrics, boolean includePad, TextUtils.TruncateAt ellipsize, int ellipsizedWidth) {
        BoringLayout boringLayout = new BoringLayout(source, paint, outerWidth, align, spacingmult, spacingadd, metrics, includePad, ellipsize, ellipsizedWidth);
        return boringLayout;
    }

    public BoringLayout replaceOrMake(CharSequence source, TextPaint paint, int outerwidth, Layout.Alignment align, float spacingMult, float spacingAdd, Metrics metrics, boolean includePad) {
        replaceWith(source, paint, outerwidth, align, spacingMult, spacingAdd);
        this.mEllipsizedWidth = outerwidth;
        this.mEllipsizedStart = 0;
        this.mEllipsizedCount = 0;
        init(source, paint, align, metrics, includePad, true);
        return this;
    }

    public BoringLayout replaceOrMake(CharSequence source, TextPaint paint, int outerWidth, Layout.Alignment align, float spacingMult, float spacingAdd, Metrics metrics, boolean includePad, TextUtils.TruncateAt ellipsize, int ellipsizedWidth) {
        boolean trust;
        TextUtils.TruncateAt truncateAt = ellipsize;
        int i = ellipsizedWidth;
        if (truncateAt == null || truncateAt == TextUtils.TruncateAt.MARQUEE) {
            replaceWith(source, paint, outerWidth, align, spacingMult, spacingAdd);
            this.mEllipsizedWidth = outerWidth;
            this.mEllipsizedStart = 0;
            this.mEllipsizedCount = 0;
            trust = true;
        } else {
            replaceWith(TextUtils.ellipsize(source, paint, (float) i, truncateAt, true, this), paint, outerWidth, align, spacingMult, spacingAdd);
            this.mEllipsizedWidth = i;
            int i2 = outerWidth;
            trust = false;
        }
        init(getText(), paint, align, metrics, includePad, trust);
        return this;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public BoringLayout(CharSequence source, TextPaint paint, int outerwidth, Layout.Alignment align, float spacingMult, float spacingAdd, Metrics metrics, boolean includePad) {
        super(source, paint, outerwidth, align, spacingMult, spacingAdd);
        this.mEllipsizedWidth = outerwidth;
        this.mEllipsizedStart = 0;
        this.mEllipsizedCount = 0;
        init(source, paint, align, metrics, includePad, true);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public BoringLayout(CharSequence source, TextPaint paint, int outerWidth, Layout.Alignment align, float spacingMult, float spacingAdd, Metrics metrics, boolean includePad, TextUtils.TruncateAt ellipsize, int ellipsizedWidth) {
        super(source, paint, outerWidth, align, spacingMult, spacingAdd);
        boolean trust;
        TextUtils.TruncateAt truncateAt = ellipsize;
        int i = ellipsizedWidth;
        if (truncateAt == null || truncateAt == TextUtils.TruncateAt.MARQUEE) {
            this.mEllipsizedWidth = outerWidth;
            this.mEllipsizedStart = 0;
            this.mEllipsizedCount = 0;
            trust = true;
        } else {
            replaceWith(TextUtils.ellipsize(source, paint, (float) i, truncateAt, true, this), paint, outerWidth, align, spacingMult, spacingAdd);
            this.mEllipsizedWidth = i;
            int i2 = outerWidth;
            trust = false;
        }
        init(getText(), paint, align, metrics, includePad, trust);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x006d  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0074  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00a1  */
    /* JADX WARNING: Removed duplicated region for block: B:25:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0022  */
    public void init(CharSequence source, TextPaint paint, Layout.Alignment align, Metrics metrics, boolean includePad, boolean trustWidth) {
        int spacing;
        Metrics metrics2 = metrics;
        CharSequence charSequence = source;
        if (!(charSequence instanceof String)) {
            Layout.Alignment alignment = align;
        } else if (align == Layout.Alignment.ALIGN_NORMAL) {
            this.mDirect = source.toString();
            TextPaint textPaint = paint;
            this.mPaint = textPaint;
            if (!includePad) {
                spacing = metrics2.bottom - metrics2.top;
                this.mDesc = metrics2.bottom;
            } else {
                spacing = metrics2.descent - metrics2.ascent;
                this.mDesc = metrics2.descent;
            }
            if (!"my".equals(Locale.getDefault().getLanguage())) {
                spacing += 3;
                this.mDesc += 3;
            } else if ("bo".equals(Locale.getDefault().getLanguage())) {
                spacing += 7;
                this.mDesc += 7;
            }
            int spacing2 = spacing;
            this.mBottom = spacing2;
            if (!trustWidth) {
                this.mMax = (float) metrics2.width;
                int i = spacing2;
            } else {
                TextLine line = TextLine.obtain();
                TextLine line2 = line;
                int i2 = spacing2;
                line.set(textPaint, charSequence, 0, source.length(), 1, Layout.DIRS_ALL_LEFT_TO_RIGHT, false, null);
                this.mMax = (float) ((int) Math.ceil((double) line2.metrics(null)));
                TextLine.recycle(line2);
            }
            if (!includePad) {
                this.mTopPadding = metrics2.top - metrics2.ascent;
                this.mBottomPadding = metrics2.bottom - metrics2.descent;
                return;
            }
            return;
        }
        this.mDirect = null;
        TextPaint textPaint2 = paint;
        this.mPaint = textPaint2;
        if (!includePad) {
        }
        if (!"my".equals(Locale.getDefault().getLanguage())) {
        }
        int spacing22 = spacing;
        this.mBottom = spacing22;
        if (!trustWidth) {
        }
        if (!includePad) {
        }
    }

    public static Metrics isBoring(CharSequence text, TextPaint paint) {
        return isBoring(text, paint, TextDirectionHeuristics.FIRSTSTRONG_LTR, null);
    }

    public static Metrics isBoring(CharSequence text, TextPaint paint, Metrics metrics) {
        return isBoring(text, paint, TextDirectionHeuristics.FIRSTSTRONG_LTR, metrics);
    }

    /* JADX INFO: finally extract failed */
    private static boolean hasAnyInterestingChars(CharSequence text, int textLength) {
        char[] buffer = TextUtils.obtain(500);
        int start = 0;
        while (start < textLength) {
            try {
                int end = Math.min(start + 500, textLength);
                TextUtils.getChars(text, start, end, buffer, 0);
                int len = end - start;
                for (int i = 0; i < len; i++) {
                    char c = buffer[i];
                    if (c == 10 || c == 9 || TextUtils.couldAffectRtl(c)) {
                        TextUtils.recycle(buffer);
                        return true;
                    }
                }
                start += 500;
            } catch (Throwable th) {
                TextUtils.recycle(buffer);
                throw th;
            }
        }
        TextUtils.recycle(buffer);
        return false;
    }

    public static Metrics isBoring(CharSequence text, TextPaint paint, TextDirectionHeuristic textDir, Metrics metrics) {
        int textLength = text.length();
        if (hasAnyInterestingChars(text, textLength)) {
            return null;
        }
        if (textDir != null && textDir.isRtl(text, 0, textLength)) {
            return null;
        }
        if ((text instanceof Spanned) && ((Spanned) text).getSpans(0, textLength, ParagraphStyle.class).length > 0) {
            return null;
        }
        Metrics fm = metrics;
        if (fm == null) {
            fm = new Metrics();
        } else {
            fm.reset();
        }
        Metrics fm2 = fm;
        TextLine line = TextLine.obtain();
        line.set(paint, text, 0, textLength, 1, Layout.DIRS_ALL_LEFT_TO_RIGHT, false, null);
        fm2.width = (int) Math.ceil((double) line.metrics(fm2));
        TextLine.recycle(line);
        return fm2;
    }

    public int getHeight() {
        return this.mBottom;
    }

    public int getLineCount() {
        return 1;
    }

    public int getLineTop(int line) {
        if (line == 0) {
            return 0;
        }
        return this.mBottom;
    }

    public int getLineDescent(int line) {
        return adjustLineDescentForComplexScripts(this.mDesc);
    }

    private int adjustLineDescentForComplexScripts(int descent) {
        if ("th".equals(Locale.getDefault().getLanguage())) {
            return descent + 3;
        }
        return descent;
    }

    public int getLineStart(int line) {
        if (line == 0) {
            return 0;
        }
        return getText().length();
    }

    public int getParagraphDirection(int line) {
        return 1;
    }

    public boolean getLineContainsTab(int line) {
        return false;
    }

    public float getLineMax(int line) {
        return this.mMax;
    }

    public float getLineWidth(int line) {
        if (line == 0) {
            return this.mMax;
        }
        return 0.0f;
    }

    public final Layout.Directions getLineDirections(int line) {
        return Layout.DIRS_ALL_LEFT_TO_RIGHT;
    }

    public int getTopPadding() {
        return this.mTopPadding;
    }

    public int getBottomPadding() {
        return this.mBottomPadding;
    }

    public int getEllipsisCount(int line) {
        return this.mEllipsizedCount;
    }

    public int getEllipsisStart(int line) {
        return this.mEllipsizedStart;
    }

    public int getEllipsizedWidth() {
        return this.mEllipsizedWidth;
    }

    public void draw(Canvas c, Path highlight, Paint highlightpaint, int cursorOffset) {
        if (this.mDirect == null || highlight != null) {
            super.draw(c, highlight, highlightpaint, cursorOffset);
        } else {
            c.drawText(this.mDirect, 0.0f, (float) (this.mBottom - this.mDesc), this.mPaint);
        }
    }

    public void ellipsized(int start, int end) {
        this.mEllipsizedStart = start;
        this.mEllipsizedCount = end - start;
    }
}
