package ohos.agp.render;

import ohos.agp.text.Font;
import ohos.agp.utils.Color;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;
import ohos.agp.utils.Point;
import ohos.agp.utils.Rect;
import ohos.agp.utils.TextTool;

public class Paint {
    static final StrokeCap[] STROKECAP_ARRAY = {StrokeCap.BUTT_CAP, StrokeCap.ROUND_CAP, StrokeCap.SQUARE_CAP};
    static final Style[] STYLE_ARRAY = {Style.FILL_STYLE, Style.STROKE_STYLE, Style.FILLANDSTROKE_STYLE};
    public int baselineOffset;
    public Color color;
    public float[] dashPathEffectIntervals;
    public Color[] gradientShaderColor;
    private BlendMode mBlendMode;
    private ColorFilter mColorFilter;
    protected ColorMatrix mColorMatrix;
    private Font mFont;
    private MaskFilter mMaskFilter;
    protected final long mNativePaintHandle;
    private long mNativeShaderHandle;
    private PathEffect mPathEffect;
    protected Shader mShader;
    private Path mTextPath;

    private native void nativeAddTextToPath(long j, String str, float f, float f2, long j2);

    private native float nativeAscent(long j);

    private native float nativeDescent(long j);

    private native float nativeGetAdvanceWidths(long j, String str, float[] fArr);

    private native float nativeGetAlpha(long j);

    private native float nativeGetCornerPathEffectRadius(long j);

    private native int nativeGetDashIntervalCount(long j);

    private native float nativeGetDashPathEffectPhase(long j);

    private native int nativeGetDisplayNumber(long j, String str, boolean z, float f, float[] fArr);

    private native boolean nativeGetDither(long j);

    private native boolean nativeGetFillPath(long j, long j2, long j3);

    private native boolean nativeGetFilterBitmap(long j);

    private native FontMetrics nativeGetFontMetrics(long j);

    private native float nativeGetLineHeightCoefficient(long j);

    private native float nativeGetLineHeightOffset(long j);

    private native int nativeGetMaxLines(long j);

    private native boolean nativeGetMultipleLine(long j);

    private native long nativeGetPaintHandle();

    private native long nativeGetPaintHandleWithPaint(long j);

    private native int nativeGetPositionX(long j);

    private native int nativeGetPositionY(long j);

    private native int nativeGetStrokeCap(long j);

    private native int nativeGetStrokeJoin(long j);

    private native float nativeGetStrokeWidth(long j);

    private native int nativeGetStyle(long j);

    private native int nativeGetTextAlign(long j);

    private native Rect nativeGetTextBounds(long j, String str);

    private native int nativeGetTextSize(long j);

    private native void nativeHorizontalTilt(long j, float f);

    private native void nativeHorizontalZoom(long j, float f);

    private native boolean nativeIsAntiAlias(long j);

    private native boolean nativeIsFakeBoldText(long j);

    private native boolean nativeIsUnderLined(long j);

    private native float nativeMeasureText(long j, String str);

    private native void nativeReset(long j);

    private native void nativeSet(long j, long j2);

    private native void nativeSetAlpha(long j, float f);

    private native void nativeSetAntiAlias(long j, boolean z);

    private native void nativeSetBlendMode(long j, int i);

    private native void nativeSetBlurDrawLooper(long j, int i, float f, float f2, float f3);

    private native void nativeSetColor(long j, int i);

    private native void nativeSetColorFilter(long j, float[] fArr);

    private native void nativeSetColorFilterWithFilterHandle(long j, long j2);

    private native void nativeSetCornerPathEffectRadius(long j, float f);

    private native void nativeSetDashPathEffectIntervals(long j, float[] fArr, int i);

    private native void nativeSetDashPathEffectPhase(long j, float f);

    private native void nativeSetDither(long j, boolean z);

    private native void nativeSetFakeBoldText(long j, boolean z);

    private native void nativeSetFilterBitmap(long j, boolean z);

    private native void nativeSetGradientShaderColor(long j, int[] iArr);

    private native void nativeSetLetterSpacing(long j, float f);

    private native void nativeSetMaskFilter(long j, long j2);

    private native void nativeSetMaxLines(long j, int i);

    private native void nativeSetMultipleLine(long j, boolean z);

    private native void nativeSetPathEffect(long j, long j2);

    private native void nativeSetPosition(long j, int i, int i2);

    private native void nativeSetShader(long j, long j2, int i);

    private native void nativeSetStrikeThrough(long j, boolean z);

    private native void nativeSetStrokeCap(long j, int i);

    private native void nativeSetStrokeJoin(long j, int i);

    private native void nativeSetStrokeMiter(long j, float f);

    private native void nativeSetStrokeWidth(long j, float f);

    private native void nativeSetStyle(long j, int i);

    private native void nativeSetSubpixel(long j, boolean z);

    private native void nativeSetTextAlign(long j, int i);

    private native void nativeSetTextSize(long j, int i);

    private native void nativeSetTypeface(long j, long j2);

    private native void nativeSetUnderLine(long j, boolean z);

    public enum MaskType {
        NONE_MASK(0),
        RECTANGLE_MASK(1),
        ROUNDEDRECTANGLE_MASK(2),
        TRIANGLE_MASK(3),
        CIRCLE_MASK(4);
        
        final int enumInt;

        private MaskType(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    public enum Style {
        FILL_STYLE(0),
        STROKE_STYLE(1),
        FILLANDSTROKE_STYLE(2);
        
        final int enumInt;

        private Style(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    public enum StrokeCap {
        BUTT_CAP(0),
        ROUND_CAP(1),
        SQUARE_CAP(2);
        
        final int enumInt;

        private StrokeCap(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    public enum ShaderType {
        LINEAR_SHADER(0),
        SWEEP_SHADER(1),
        RADIAL_SHADER(2),
        PIXELMAP_SHADER(3),
        GROUP_SHADER(4);
        
        final int enumInt;

        private ShaderType(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    public enum Join {
        MITER_JOIN(0),
        ROUND_JOIN(1),
        BEVEL_JOIN(2);
        
        final int enumInt;

        private Join(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    public static class FontMetrics {
        public float ascent;
        public float bottom;
        public float descent;
        public float leading;
        public float top;

        public FontMetrics(float f, float f2, float f3, float f4, float f5) {
            this.top = f;
            this.ascent = f2;
            this.descent = f3;
            this.bottom = f4;
            this.leading = f5;
        }
    }

    public Paint() {
        this.gradientShaderColor = new Color[]{Color.BLACK, Color.BLACK};
        this.dashPathEffectIntervals = new float[0];
        this.mNativePaintHandle = nativeGetPaintHandle();
        MemoryCleanerRegistry.getInstance().register(this, new PaintCleaner(this.mNativePaintHandle));
    }

    public Paint(Paint paint) {
        this.gradientShaderColor = new Color[]{Color.BLACK, Color.BLACK};
        this.dashPathEffectIntervals = new float[0];
        this.mNativePaintHandle = nativeGetPaintHandleWithPaint(paint.getNativeHandle());
        setParameters(paint);
        MemoryCleanerRegistry.getInstance().register(this, new PaintCleaner(this.mNativePaintHandle));
    }

    public void set(Paint paint) {
        if (this.mNativePaintHandle != paint.getNativeHandle()) {
            nativeSet(this.mNativePaintHandle, paint.getNativeHandle());
            setParameters(paint);
        }
    }

    private void setParameters(Paint paint) {
        this.color = paint.color;
        this.gradientShaderColor = (Color[]) paint.gradientShaderColor.clone();
        this.dashPathEffectIntervals = (float[]) paint.dashPathEffectIntervals.clone();
        this.mColorMatrix = paint.mColorMatrix;
        this.mShader = paint.mShader;
        this.mNativeShaderHandle = paint.mNativeShaderHandle;
        this.mFont = paint.mFont;
        this.mPathEffect = paint.mPathEffect;
        this.mBlendMode = paint.mBlendMode;
        this.mTextPath = paint.mTextPath;
        this.baselineOffset = paint.baselineOffset;
        this.mMaskFilter = paint.mMaskFilter;
        this.mColorFilter = paint.mColorFilter;
    }

    protected static class PaintCleaner extends NativeMemoryCleanerHelper {
        private native void nativePaintRelease(long j);

        public PaintCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativePaintRelease(j);
            }
        }
    }

    public long getNativeHandle() {
        return this.mNativePaintHandle;
    }

    public void setStyle(Style style) {
        nativeSetStyle(this.mNativePaintHandle, style.value());
    }

    public Style getStyle() {
        int nativeGetStyle = nativeGetStyle(this.mNativePaintHandle);
        if (nativeGetStyle >= 0) {
            Style[] styleArr = STYLE_ARRAY;
            if (nativeGetStyle < styleArr.length) {
                return styleArr[nativeGetStyle];
            }
        }
        return STYLE_ARRAY[0];
    }

    public void setDither(boolean z) {
        nativeSetDither(this.mNativePaintHandle, z);
    }

    public boolean getDither() {
        return nativeGetDither(this.mNativePaintHandle);
    }

    public void setFilterBitmap(boolean z) {
        nativeSetFilterBitmap(this.mNativePaintHandle, z);
    }

    public boolean getFilterBitmap() {
        return nativeGetFilterBitmap(this.mNativePaintHandle);
    }

    public void setStrokeCap(StrokeCap strokeCap) {
        nativeSetStrokeCap(this.mNativePaintHandle, strokeCap.value());
    }

    public StrokeCap getStrokeCap() {
        int nativeGetStrokeCap = nativeGetStrokeCap(this.mNativePaintHandle);
        StrokeCap[] strokeCapArr = STROKECAP_ARRAY;
        if (nativeGetStrokeCap < 0 || nativeGetStrokeCap >= strokeCapArr.length) {
            nativeGetStrokeCap = 0;
        }
        return strokeCapArr[nativeGetStrokeCap];
    }

    public void setStrokeWidth(float f) {
        nativeSetStrokeWidth(this.mNativePaintHandle, f);
    }

    public float getStrokeWidth() {
        return nativeGetStrokeWidth(this.mNativePaintHandle);
    }

    public void setColor(Color color2) {
        nativeSetColor(this.mNativePaintHandle, color2.getValue());
        this.color = color2;
    }

    public Color getColor() {
        return this.color;
    }

    public void setGradientShaderColor(Color[] colorArr) {
        if (colorArr.length == 2) {
            int[] iArr = {0, 0};
            iArr[0] = colorArr[0].getValue();
            iArr[1] = colorArr[1].getValue();
            nativeSetGradientShaderColor(this.mNativePaintHandle, iArr);
            this.gradientShaderColor = (Color[]) colorArr.clone();
        }
    }

    public Color[] getGradientShaderColor() {
        return (Color[]) this.gradientShaderColor.clone();
    }

    public void setDashPathEffectIntervals(float[] fArr) {
        if (fArr.length % 2 == 0 && fArr.length >= 2 && fArr.length <= 20) {
            nativeSetDashPathEffectIntervals(this.mNativePaintHandle, fArr, fArr.length);
            this.dashPathEffectIntervals = (float[]) fArr.clone();
        }
    }

    public float[] getDashPathEffectIntervals() {
        return (float[]) this.dashPathEffectIntervals.clone();
    }

    public int getDashIntervalCount() {
        return nativeGetDashIntervalCount(this.mNativePaintHandle);
    }

    public void setDashPathEffectPhase(float f) {
        nativeSetDashPathEffectPhase(this.mNativePaintHandle, f);
    }

    public float getDashPathEffectPhase() {
        return nativeGetDashPathEffectPhase(this.mNativePaintHandle);
    }

    public void setAntiAlias(boolean z) {
        nativeSetAntiAlias(this.mNativePaintHandle, z);
    }

    public final boolean isAntiAlias() {
        return nativeIsAntiAlias(this.mNativePaintHandle);
    }

    public void setFakeBoldText(boolean z) {
        nativeSetFakeBoldText(this.mNativePaintHandle, z);
    }

    public final boolean isFakeBoldText() {
        return nativeIsFakeBoldText(this.mNativePaintHandle);
    }

    public FontMetrics getFontMetrics() {
        return nativeGetFontMetrics(this.mNativePaintHandle);
    }

    public void setAlpha(float f) {
        nativeSetAlpha(this.mNativePaintHandle, f);
    }

    public float getAlpha() {
        return nativeGetAlpha(this.mNativePaintHandle);
    }

    public void setColorMatrix(ColorMatrix colorMatrix) {
        nativeSetColorFilter(this.mNativePaintHandle, colorMatrix.getMatrix());
        this.mColorMatrix = colorMatrix;
    }

    public ColorMatrix getColorMatrix() {
        return this.mColorMatrix;
    }

    public Rect getTextBounds(String str) {
        return nativeGetTextBounds(this.mNativePaintHandle, str);
    }

    public void setFont(Font font) {
        this.mFont = font;
        long j = this.mNativePaintHandle;
        Font font2 = this.mFont;
        nativeSetTypeface(j, font2 == null ? 0 : font2.convertToTypeface().getNativeTypefacePtr());
    }

    public Font getFont() {
        return this.mFont;
    }

    public void setTextAlign(int i) {
        nativeSetTextAlign(this.mNativePaintHandle, i);
    }

    public int getTextAlign() {
        return nativeGetTextAlign(this.mNativePaintHandle);
    }

    public void setTextSize(int i) {
        if (TextTool.validateTextSizeParam(i)) {
            nativeSetTextSize(this.mNativePaintHandle, i);
        }
    }

    public int getTextSize() {
        return nativeGetTextSize(this.mNativePaintHandle);
    }

    public void setMaxLines(int i) {
        nativeSetMaxLines(this.mNativePaintHandle, i);
    }

    public int getMaxLines() {
        return nativeGetMaxLines(this.mNativePaintHandle);
    }

    public void setMultipleLine(boolean z) {
        nativeSetMultipleLine(this.mNativePaintHandle, z);
    }

    public boolean isMultipleLine() {
        return nativeGetMultipleLine(this.mNativePaintHandle);
    }

    public float getLineHeightCoefficient() {
        return nativeGetLineHeightCoefficient(this.mNativePaintHandle);
    }

    public float getLineHeightOffset() {
        return nativeGetLineHeightOffset(this.mNativePaintHandle);
    }

    public void setPosition(Point point) {
        nativeSetPosition(this.mNativePaintHandle, (int) point.position[0], (int) point.position[1]);
    }

    public Point getPosition() {
        return new Point((float) nativeGetPositionX(this.mNativePaintHandle), (float) nativeGetPositionY(this.mNativePaintHandle));
    }

    public void setCornerPathEffectRadius(float f) {
        nativeSetCornerPathEffectRadius(this.mNativePaintHandle, f);
    }

    public float getCornerPathEffectRadius() {
        return nativeGetCornerPathEffectRadius(this.mNativePaintHandle);
    }

    public void setShader(Shader shader, ShaderType shaderType) {
        if (this.mShader != shader) {
            this.mShader = shader;
            int value = shaderType.value();
            Shader shader2 = this.mShader;
            this.mNativeShaderHandle = shader2 == null ? 0 : shader2.getNativeHandle();
            nativeSetShader(this.mNativePaintHandle, this.mNativeShaderHandle, value);
        }
    }

    public Shader getShader() {
        return this.mShader;
    }

    public float measureText(String str) {
        return nativeMeasureText(this.mNativePaintHandle, str);
    }

    public BlendMode setBlendMode(BlendMode blendMode) {
        BlendMode blendMode2 = this.mBlendMode;
        if (blendMode2 == null) {
            blendMode2 = BlendMode.SRC_OVER;
        }
        int value = blendMode2.value();
        int value2 = blendMode == null ? BlendMode.SRC_OVER.value() : blendMode.value();
        if (value2 != value) {
            nativeSetBlendMode(this.mNativePaintHandle, value2);
        }
        this.mBlendMode = blendMode;
        return blendMode;
    }

    public BlendMode getBlendMode() {
        return this.mBlendMode;
    }

    public void setStrokeJoin(Join join) {
        nativeSetStrokeJoin(this.mNativePaintHandle, join == null ? 0 : join.value());
    }

    public Join getStrokeJoin() {
        return getStrokeJoinValue(nativeGetStrokeJoin(this.mNativePaintHandle));
    }

    public void setBlurDrawLooper(BlurDrawLooper blurDrawLooper) {
        nativeSetBlurDrawLooper(this.mNativePaintHandle, blurDrawLooper.intColor, blurDrawLooper.offsetX, blurDrawLooper.offsetY, blurDrawLooper.shadowRadius);
    }

    public void clearBlurDrawLooper() {
        setBlurDrawLooper(new BlurDrawLooper(0.0f, 0.0f, 0.0f, new Color(0)));
    }

    public void setPathEffect(PathEffect pathEffect) {
        if (this.mPathEffect != pathEffect) {
            this.mPathEffect = pathEffect;
        }
        long j = 0;
        if (pathEffect != null) {
            j = pathEffect.getNativeHandle();
        }
        nativeSetPathEffect(this.mNativePaintHandle, j);
    }

    public PathEffect getPathEffect() {
        return this.mPathEffect;
    }

    public int getDisplayNumber(String str, boolean z, float f, float[] fArr) {
        if (str == null || str.length() == 0 || fArr == null || fArr.length != str.length()) {
            return -1;
        }
        return nativeGetDisplayNumber(this.mNativePaintHandle, str, z, f, fArr);
    }

    public int getAdvanceWidths(String str, float[] fArr) {
        if (str == null || fArr == null || fArr.length != str.length()) {
            return -1;
        }
        nativeGetAdvanceWidths(this.mNativePaintHandle, str, fArr);
        return str.length();
    }

    public float ascent() {
        return nativeAscent(this.mNativePaintHandle);
    }

    public float descent() {
        return nativeDescent(this.mNativePaintHandle);
    }

    public void horizontalZoom(float f) {
        nativeHorizontalZoom(this.mNativePaintHandle, f);
    }

    public void horizontalTilt(float f) {
        nativeHorizontalTilt(this.mNativePaintHandle, f);
    }

    public void setStrikeThrough(boolean z) {
        nativeSetStrikeThrough(this.mNativePaintHandle, z);
    }

    public void setUnderLine(boolean z) {
        nativeSetUnderLine(this.mNativePaintHandle, z);
    }

    public void setSubpixelAntiAlias(boolean z) {
        nativeSetSubpixel(this.mNativePaintHandle, z);
    }

    public void addTextToPath(String str, float f, float f2, Path path) {
        if (path != null) {
            if (this.mTextPath != path) {
                this.mTextPath = path;
            }
            nativeAddTextToPath(this.mNativePaintHandle, str, f, f2, path.getNativeHandle());
        }
    }

    public void setStrokeMiter(float f) {
        nativeSetStrokeMiter(this.mNativePaintHandle, f);
    }

    public void setMaskFilter(MaskFilter maskFilter) {
        if (this.mMaskFilter != maskFilter) {
            this.mMaskFilter = maskFilter;
        }
        if (maskFilter != null) {
            nativeSetMaskFilter(this.mNativePaintHandle, maskFilter.getNativeHandle());
        } else {
            nativeSetMaskFilter(this.mNativePaintHandle, 0);
        }
    }

    public MaskFilter getMaskFilter() {
        return this.mMaskFilter;
    }

    public void setColorFilter(ColorFilter colorFilter) {
        if (this.mColorFilter != colorFilter) {
            this.mColorFilter = colorFilter;
        }
        if (colorFilter != null) {
            nativeSetColorFilterWithFilterHandle(this.mNativePaintHandle, colorFilter.getNativeHandle());
        } else {
            nativeSetColorFilterWithFilterHandle(this.mNativePaintHandle, 0);
        }
    }

    public ColorFilter getColorFilter() {
        return this.mColorFilter;
    }

    public boolean getFillPath(Path path, Path path2) {
        return nativeGetFillPath(this.mNativePaintHandle, path.getNativeHandle(), path2.getNativeHandle());
    }

    public float getRecommendedLineSpacing() {
        FontMetrics fontMetrics = getFontMetrics();
        return (fontMetrics.descent - fontMetrics.ascent) + fontMetrics.leading;
    }

    public void setLetterSpacing(float f) {
        nativeSetLetterSpacing(this.mNativePaintHandle, f);
    }

    public final boolean isUnderLined() {
        return nativeIsUnderLined(this.mNativePaintHandle);
    }

    public void reset() {
        nativeReset(this.mNativePaintHandle);
    }

    private Join getStrokeJoinValue(int i) {
        Join[] joinArr = {Join.MITER_JOIN, Join.ROUND_JOIN, Join.BEVEL_JOIN};
        if (i < 0 || i >= joinArr.length) {
            return null;
        }
        return joinArr[i];
    }
}
