package android.graphics;

import android.graphics.fonts.FontVariationAxis;
import android.hardware.camera2.params.TonemapCurve;
import android.net.ProxyInfo;
import android.os.LocaleList;
import android.text.GraphicsOperations;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;
import com.android.internal.annotations.GuardedBy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import libcore.util.NativeAllocationRegistry;

public class Paint {
    public static final int ANTI_ALIAS_FLAG = 1;
    public static final int AUTO_HINTING_TEXT_FLAG = 2048;
    public static final int BIDI_DEFAULT_LTR = 2;
    public static final int BIDI_DEFAULT_RTL = 3;
    private static final int BIDI_FLAG_MASK = 7;
    public static final int BIDI_FORCE_LTR = 4;
    public static final int BIDI_FORCE_RTL = 5;
    public static final int BIDI_LTR = 0;
    private static final int BIDI_MAX_FLAG_VALUE = 5;
    public static final int BIDI_RTL = 1;
    public static final int CURSOR_AFTER = 0;
    public static final int CURSOR_AT = 4;
    public static final int CURSOR_AT_OR_AFTER = 1;
    public static final int CURSOR_AT_OR_BEFORE = 3;
    public static final int CURSOR_BEFORE = 2;
    private static final int CURSOR_OPT_MAX_VALUE = 4;
    public static final int DEV_KERN_TEXT_FLAG = 256;
    public static final int DIRECTION_LTR = 0;
    public static final int DIRECTION_RTL = 1;
    public static final int DITHER_FLAG = 4;
    public static final int EMBEDDED_BITMAP_TEXT_FLAG = 1024;
    public static final int FAKE_BOLD_TEXT_FLAG = 32;
    public static final int FILTER_BITMAP_FLAG = 2;
    static final int HIDDEN_DEFAULT_PAINT_FLAGS = 1280;
    public static final int HINTING_OFF = 0;
    public static final int HINTING_ON = 1;
    public static final int HYPHENEDIT_MASK_END_OF_LINE = 7;
    public static final int HYPHENEDIT_MASK_START_OF_LINE = 24;
    public static final int LCD_RENDER_TEXT_FLAG = 512;
    public static final int LINEAR_TEXT_FLAG = 64;
    private static final long NATIVE_PAINT_SIZE = 98;
    public static final int STRIKE_THRU_TEXT_FLAG = 16;
    public static final int SUBPIXEL_TEXT_FLAG = 128;
    public static final int UNDERLINE_TEXT_FLAG = 8;
    public static final int VERTICAL_TEXT_FLAG = 4096;
    static final Align[] sAlignArray = new Align[]{Align.LEFT, Align.CENTER, Align.RIGHT};
    private static final Object sCacheLock = new Object();
    static final Cap[] sCapArray = new Cap[]{Cap.BUTT, Cap.ROUND, Cap.SQUARE};
    static final Join[] sJoinArray = new Join[]{Join.MITER, Join.ROUND, Join.BEVEL};
    @GuardedBy("sCacheLock")
    private static final HashMap<String, Integer> sMinikinLangListIdCache = new HashMap();
    static final Style[] sStyleArray = new Style[]{Style.FILL, Style.STROKE, Style.FILL_AND_STROKE};
    public int mBidiFlags;
    private ColorFilter mColorFilter;
    private float mCompatScaling;
    private String mFontFeatureSettings;
    private String mFontVariationSettings;
    private boolean mHasCompatScaling;
    private float mInvCompatScaling;
    private LocaleList mLocales;
    private MaskFilter mMaskFilter;
    private long mNativeColorFilter;
    private long mNativePaint;
    private long mNativeShader;
    public long mNativeTypeface;
    private PathEffect mPathEffect;
    private Shader mShader;
    private Typeface mTypeface;
    private Xfermode mXfermode;

    public enum Align {
        LEFT(0),
        CENTER(1),
        RIGHT(2);
        
        final int nativeInt;

        private Align(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    public enum Cap {
        BUTT(0),
        ROUND(1),
        SQUARE(2);
        
        final int nativeInt;

        private Cap(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    public static class FontMetrics {
        public float ascent;
        public float bottom;
        public float descent;
        public float leading;
        public float top;
    }

    public static class FontMetricsInt {
        public int ascent;
        public int bottom;
        public int descent;
        public int leading;
        public int top;

        public String toString() {
            return "FontMetricsInt: top=" + this.top + " ascent=" + this.ascent + " descent=" + this.descent + " bottom=" + this.bottom + " leading=" + this.leading;
        }
    }

    public enum Join {
        MITER(0),
        ROUND(1),
        BEVEL(2);
        
        final int nativeInt;

        private Join(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    private static class NoImagePreloadHolder {
        public static final NativeAllocationRegistry sRegistry = new NativeAllocationRegistry(Paint.class.getClassLoader(), Paint.nGetNativeFinalizer(), Paint.NATIVE_PAINT_SIZE);

        private NoImagePreloadHolder() {
        }
    }

    public enum Style {
        FILL(0),
        STROKE(1),
        FILL_AND_STROKE(2);
        
        final int nativeInt;

        private Style(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    private static native float nAscent(long j, long j2);

    private static native int nBreakText(long j, long j2, String str, boolean z, float f, int i, float[] fArr);

    private static native int nBreakText(long j, long j2, char[] cArr, int i, int i2, float f, int i3, float[] fArr);

    private static native float nDescent(long j, long j2);

    private static native int nGetAlpha(long j);

    private static native void nGetCharArrayBounds(long j, long j2, char[] cArr, int i, int i2, int i3, Rect rect);

    private static native int nGetColor(long j);

    private static native boolean nGetFillPath(long j, long j2, long j3);

    private static native int nGetFlags(long j);

    private static native float nGetFontMetrics(long j, long j2, FontMetrics fontMetrics);

    private static native int nGetFontMetricsInt(long j, long j2, FontMetricsInt fontMetricsInt);

    private static native int nGetHinting(long j);

    private static native int nGetHyphenEdit(long j);

    private static native float nGetLetterSpacing(long j);

    private static native long nGetNativeFinalizer();

    private static native int nGetOffsetForAdvance(long j, long j2, char[] cArr, int i, int i2, int i3, int i4, boolean z, float f);

    private static native float nGetRunAdvance(long j, long j2, char[] cArr, int i, int i2, int i3, int i4, boolean z, int i5);

    private static native void nGetStringBounds(long j, long j2, String str, int i, int i2, int i3, Rect rect);

    private static native int nGetStrokeCap(long j);

    private static native int nGetStrokeJoin(long j);

    private static native float nGetStrokeMiter(long j);

    private static native float nGetStrokeWidth(long j);

    private static native int nGetStyle(long j);

    private static native float nGetTextAdvances(long j, long j2, String str, int i, int i2, int i3, int i4, int i5, float[] fArr, int i6);

    private static native float nGetTextAdvances(long j, long j2, char[] cArr, int i, int i2, int i3, int i4, int i5, float[] fArr, int i6);

    private static native int nGetTextAlign(long j);

    private static native void nGetTextPath(long j, long j2, int i, String str, int i2, int i3, float f, float f2, long j3);

    private static native void nGetTextPath(long j, long j2, int i, char[] cArr, int i2, int i3, float f, float f2, long j3);

    private native int nGetTextRunCursor(long j, long j2, String str, int i, int i2, int i3, int i4, int i5);

    private native int nGetTextRunCursor(long j, long j2, char[] cArr, int i, int i2, int i3, int i4, int i5);

    private static native float nGetTextScaleX(long j);

    private static native float nGetTextSize(long j);

    private static native float nGetTextSkewX(long j);

    private static native float nGetWordSpacing(long j);

    private static native boolean nHasGlyph(long j, long j2, int i, String str);

    private static native boolean nHasShadowLayer(long j);

    private static native long nInit();

    private static native long nInitWithPaint(long j);

    private static native boolean nIsElegantTextHeight(long j);

    private static native void nReset(long j);

    private static native void nSet(long j, long j2);

    private static native void nSetAlpha(long j, int i);

    private static native void nSetAntiAlias(long j, boolean z);

    private static native void nSetColor(long j, int i);

    private static native long nSetColorFilter(long j, long j2);

    private static native void nSetDither(long j, boolean z);

    private static native void nSetElegantTextHeight(long j, boolean z);

    private static native void nSetFakeBoldText(long j, boolean z);

    private static native void nSetFilterBitmap(long j, boolean z);

    private static native void nSetFlags(long j, int i);

    private static native void nSetFontFeatureSettings(long j, String str);

    private static native void nSetHinting(long j, int i);

    private static native void nSetHyphenEdit(long j, int i);

    private static native void nSetLetterSpacing(long j, float f);

    private static native void nSetLinearText(long j, boolean z);

    private static native long nSetMaskFilter(long j, long j2);

    private static native long nSetPathEffect(long j, long j2);

    private static native long nSetShader(long j, long j2);

    private static native void nSetShadowLayer(long j, float f, float f2, float f3, int i);

    private static native void nSetStrikeThruText(long j, boolean z);

    private static native void nSetStrokeCap(long j, int i);

    private static native void nSetStrokeJoin(long j, int i);

    private static native void nSetStrokeMiter(long j, float f);

    private static native void nSetStrokeWidth(long j, float f);

    private static native void nSetStyle(long j, int i);

    private static native void nSetSubpixelText(long j, boolean z);

    private static native void nSetTextAlign(long j, int i);

    private static native int nSetTextLocales(long j, String str);

    private static native void nSetTextLocalesByMinikinLangListId(long j, int i);

    private static native void nSetTextScaleX(long j, float f);

    private static native void nSetTextSize(long j, float f);

    private static native void nSetTextSkewX(long j, float f);

    private static native long nSetTypeface(long j, long j2);

    private static native void nSetUnderlineText(long j, boolean z);

    private static native void nSetWordSpacing(long j, float f);

    private static native void nSetXfermode(long j, int i);

    public Paint() {
        this(0);
    }

    public Paint(int flags) {
        this.mBidiFlags = 2;
        this.mNativePaint = nInit();
        NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativePaint);
        setFlags(flags | 1280);
        this.mInvCompatScaling = 1.0f;
        this.mCompatScaling = 1.0f;
        setTextLocales(LocaleList.getAdjustedDefault());
    }

    public Paint(Paint paint) {
        this.mBidiFlags = 2;
        this.mNativePaint = nInitWithPaint(paint.getNativeInstance());
        NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativePaint);
        setClassVariablesFrom(paint);
    }

    public void reset() {
        nReset(this.mNativePaint);
        setFlags(1280);
        this.mColorFilter = null;
        this.mMaskFilter = null;
        this.mPathEffect = null;
        this.mShader = null;
        this.mNativeShader = 0;
        this.mTypeface = null;
        this.mNativeTypeface = 0;
        this.mXfermode = null;
        this.mHasCompatScaling = false;
        this.mCompatScaling = 1.0f;
        this.mInvCompatScaling = 1.0f;
        this.mBidiFlags = 2;
        setTextLocales(LocaleList.getAdjustedDefault());
        setElegantTextHeight(false);
        this.mFontFeatureSettings = null;
        this.mFontVariationSettings = null;
    }

    public void set(Paint src) {
        if (this != src) {
            nSet(this.mNativePaint, src.mNativePaint);
            setClassVariablesFrom(src);
        }
    }

    private void setClassVariablesFrom(Paint paint) {
        this.mColorFilter = paint.mColorFilter;
        this.mMaskFilter = paint.mMaskFilter;
        this.mPathEffect = paint.mPathEffect;
        this.mShader = paint.mShader;
        this.mNativeShader = paint.mNativeShader;
        this.mTypeface = paint.mTypeface;
        this.mNativeTypeface = paint.mNativeTypeface;
        this.mXfermode = paint.mXfermode;
        this.mHasCompatScaling = paint.mHasCompatScaling;
        this.mCompatScaling = paint.mCompatScaling;
        this.mInvCompatScaling = paint.mInvCompatScaling;
        this.mBidiFlags = paint.mBidiFlags;
        this.mLocales = paint.mLocales;
        this.mFontFeatureSettings = paint.mFontFeatureSettings;
        this.mFontVariationSettings = paint.mFontVariationSettings;
    }

    public void setCompatibilityScaling(float factor) {
        if (((double) factor) == 1.0d) {
            this.mHasCompatScaling = false;
            this.mInvCompatScaling = 1.0f;
            this.mCompatScaling = 1.0f;
            return;
        }
        this.mHasCompatScaling = true;
        this.mCompatScaling = factor;
        this.mInvCompatScaling = 1.0f / factor;
    }

    public long getNativeInstance() {
        long newNativeShader = this.mShader == null ? 0 : this.mShader.getNativeInstance();
        if (newNativeShader != this.mNativeShader) {
            this.mNativeShader = newNativeShader;
            nSetShader(this.mNativePaint, this.mNativeShader);
        }
        long newNativeColorFilter = this.mColorFilter == null ? 0 : this.mColorFilter.getNativeInstance();
        if (newNativeColorFilter != this.mNativeColorFilter) {
            this.mNativeColorFilter = newNativeColorFilter;
            nSetColorFilter(this.mNativePaint, this.mNativeColorFilter);
        }
        return this.mNativePaint;
    }

    public int getBidiFlags() {
        return this.mBidiFlags;
    }

    public void setBidiFlags(int flags) {
        flags &= 7;
        if (flags > 5) {
            throw new IllegalArgumentException("unknown bidi flag: " + flags);
        }
        this.mBidiFlags = flags;
    }

    public int getFlags() {
        return nGetFlags(this.mNativePaint);
    }

    public void setFlags(int flags) {
        nSetFlags(this.mNativePaint, flags);
    }

    public int getHinting() {
        return nGetHinting(this.mNativePaint);
    }

    public void setHinting(int mode) {
        nSetHinting(this.mNativePaint, mode);
    }

    public final boolean isAntiAlias() {
        return (getFlags() & 1) != 0;
    }

    public void setAntiAlias(boolean aa) {
        nSetAntiAlias(this.mNativePaint, aa);
    }

    public final boolean isDither() {
        return (getFlags() & 4) != 0;
    }

    public void setDither(boolean dither) {
        nSetDither(this.mNativePaint, dither);
    }

    public final boolean isLinearText() {
        return (getFlags() & 64) != 0;
    }

    public void setLinearText(boolean linearText) {
        nSetLinearText(this.mNativePaint, linearText);
    }

    public final boolean isSubpixelText() {
        return (getFlags() & 128) != 0;
    }

    public void setSubpixelText(boolean subpixelText) {
        nSetSubpixelText(this.mNativePaint, subpixelText);
    }

    public final boolean isUnderlineText() {
        return (getFlags() & 8) != 0;
    }

    public void setUnderlineText(boolean underlineText) {
        nSetUnderlineText(this.mNativePaint, underlineText);
    }

    public final boolean isStrikeThruText() {
        return (getFlags() & 16) != 0;
    }

    public void setStrikeThruText(boolean strikeThruText) {
        nSetStrikeThruText(this.mNativePaint, strikeThruText);
    }

    public final boolean isFakeBoldText() {
        return (getFlags() & 32) != 0;
    }

    public void setFakeBoldText(boolean fakeBoldText) {
        nSetFakeBoldText(this.mNativePaint, fakeBoldText);
    }

    public final boolean isFilterBitmap() {
        return (getFlags() & 2) != 0;
    }

    public void setFilterBitmap(boolean filter) {
        nSetFilterBitmap(this.mNativePaint, filter);
    }

    public Style getStyle() {
        return sStyleArray[nGetStyle(this.mNativePaint)];
    }

    public void setStyle(Style style) {
        nSetStyle(this.mNativePaint, style.nativeInt);
    }

    public int getColor() {
        return nGetColor(this.mNativePaint);
    }

    public void setColor(int color) {
        nSetColor(this.mNativePaint, color);
    }

    public int getAlpha() {
        return nGetAlpha(this.mNativePaint);
    }

    public void setAlpha(int a) {
        nSetAlpha(this.mNativePaint, a);
    }

    public void setARGB(int a, int r, int g, int b) {
        setColor((((a << 24) | (r << 16)) | (g << 8)) | b);
    }

    public float getStrokeWidth() {
        return nGetStrokeWidth(this.mNativePaint);
    }

    public void setStrokeWidth(float width) {
        nSetStrokeWidth(this.mNativePaint, width);
    }

    public float getStrokeMiter() {
        return nGetStrokeMiter(this.mNativePaint);
    }

    public void setStrokeMiter(float miter) {
        nSetStrokeMiter(this.mNativePaint, miter);
    }

    public Cap getStrokeCap() {
        return sCapArray[nGetStrokeCap(this.mNativePaint)];
    }

    public void setStrokeCap(Cap cap) {
        nSetStrokeCap(this.mNativePaint, cap.nativeInt);
    }

    public Join getStrokeJoin() {
        return sJoinArray[nGetStrokeJoin(this.mNativePaint)];
    }

    public void setStrokeJoin(Join join) {
        nSetStrokeJoin(this.mNativePaint, join.nativeInt);
    }

    public boolean getFillPath(Path src, Path dst) {
        return nGetFillPath(this.mNativePaint, src.readOnlyNI(), dst.mutateNI());
    }

    public Shader getShader() {
        return this.mShader;
    }

    public Shader setShader(Shader shader) {
        if (this.mShader != shader) {
            this.mNativeShader = -1;
            nSetShader(this.mNativePaint, 0);
        }
        this.mShader = shader;
        return shader;
    }

    public ColorFilter getColorFilter() {
        return this.mColorFilter;
    }

    public ColorFilter setColorFilter(ColorFilter filter) {
        if (this.mColorFilter != filter) {
            this.mNativeColorFilter = -1;
        }
        this.mColorFilter = filter;
        return filter;
    }

    public Xfermode getXfermode() {
        return this.mXfermode;
    }

    public Xfermode setXfermode(Xfermode xfermode) {
        int newMode = xfermode != null ? xfermode.porterDuffMode : Xfermode.DEFAULT;
        if (newMode != (this.mXfermode != null ? this.mXfermode.porterDuffMode : Xfermode.DEFAULT)) {
            nSetXfermode(this.mNativePaint, newMode);
        }
        this.mXfermode = xfermode;
        return xfermode;
    }

    public PathEffect getPathEffect() {
        return this.mPathEffect;
    }

    public PathEffect setPathEffect(PathEffect effect) {
        long effectNative = 0;
        if (effect != null) {
            effectNative = effect.native_instance;
        }
        nSetPathEffect(this.mNativePaint, effectNative);
        this.mPathEffect = effect;
        return effect;
    }

    public MaskFilter getMaskFilter() {
        return this.mMaskFilter;
    }

    public MaskFilter setMaskFilter(MaskFilter maskfilter) {
        long maskfilterNative = 0;
        if (maskfilter != null) {
            maskfilterNative = maskfilter.native_instance;
        }
        nSetMaskFilter(this.mNativePaint, maskfilterNative);
        this.mMaskFilter = maskfilter;
        return maskfilter;
    }

    public Typeface getTypeface() {
        return this.mTypeface;
    }

    public Typeface setTypeface(Typeface typeface) {
        long typefaceNative = 0;
        if (typeface != null) {
            typefaceNative = typeface.native_instance;
        }
        nSetTypeface(this.mNativePaint, typefaceNative);
        this.mTypeface = typeface;
        this.mNativeTypeface = typefaceNative;
        return typeface;
    }

    @Deprecated
    public Rasterizer getRasterizer() {
        return null;
    }

    @Deprecated
    public Rasterizer setRasterizer(Rasterizer rasterizer) {
        return rasterizer;
    }

    public void setShadowLayer(float radius, float dx, float dy, int shadowColor) {
        nSetShadowLayer(this.mNativePaint, radius, dx, dy, shadowColor);
    }

    public void clearShadowLayer() {
        setShadowLayer(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, 0);
    }

    public boolean hasShadowLayer() {
        return nHasShadowLayer(this.mNativePaint);
    }

    public Align getTextAlign() {
        return sAlignArray[nGetTextAlign(this.mNativePaint)];
    }

    public void setTextAlign(Align align) {
        nSetTextAlign(this.mNativePaint, align.nativeInt);
    }

    public Locale getTextLocale() {
        return this.mLocales.get(0);
    }

    public LocaleList getTextLocales() {
        return this.mLocales;
    }

    public void setTextLocale(Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException("locale cannot be null");
        } else if (this.mLocales == null || this.mLocales.size() != 1 || !locale.equals(this.mLocales.get(0))) {
            this.mLocales = new LocaleList(locale);
            syncTextLocalesWithMinikin();
        }
    }

    public void setTextLocales(LocaleList locales) {
        if (locales == null || locales.isEmpty()) {
            throw new IllegalArgumentException("locales cannot be null or empty");
        } else if (!locales.equals(this.mLocales)) {
            this.mLocales = locales;
            syncTextLocalesWithMinikin();
        }
    }

    private void syncTextLocalesWithMinikin() {
        String languageTags = this.mLocales.toLanguageTags();
        synchronized (sCacheLock) {
            Integer minikinLangListId = (Integer) sMinikinLangListIdCache.get(languageTags);
            if (minikinLangListId == null) {
                sMinikinLangListIdCache.put(languageTags, Integer.valueOf(nSetTextLocales(this.mNativePaint, languageTags)));
                return;
            }
            nSetTextLocalesByMinikinLangListId(this.mNativePaint, minikinLangListId.intValue());
        }
    }

    public boolean isElegantTextHeight() {
        return nIsElegantTextHeight(this.mNativePaint);
    }

    public void setElegantTextHeight(boolean elegant) {
        nSetElegantTextHeight(this.mNativePaint, elegant);
    }

    public float getTextSize() {
        return nGetTextSize(this.mNativePaint);
    }

    public void setTextSize(float textSize) {
        nSetTextSize(this.mNativePaint, textSize);
    }

    public float getTextScaleX() {
        return nGetTextScaleX(this.mNativePaint);
    }

    public void setTextScaleX(float scaleX) {
        nSetTextScaleX(this.mNativePaint, scaleX);
    }

    public float getTextSkewX() {
        return nGetTextSkewX(this.mNativePaint);
    }

    public void setTextSkewX(float skewX) {
        nSetTextSkewX(this.mNativePaint, skewX);
    }

    public float getLetterSpacing() {
        return nGetLetterSpacing(this.mNativePaint);
    }

    public void setLetterSpacing(float letterSpacing) {
        nSetLetterSpacing(this.mNativePaint, letterSpacing);
    }

    public float getWordSpacing() {
        return nGetWordSpacing(this.mNativePaint);
    }

    public void setWordSpacing(float wordSpacing) {
        nSetWordSpacing(this.mNativePaint, wordSpacing);
    }

    public String getFontFeatureSettings() {
        return this.mFontFeatureSettings;
    }

    public void setFontFeatureSettings(String settings) {
        if (settings != null && settings.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
            settings = null;
        }
        if (!(settings == null && this.mFontFeatureSettings == null) && (settings == null || !settings.equals(this.mFontFeatureSettings))) {
            this.mFontFeatureSettings = settings;
            nSetFontFeatureSettings(this.mNativePaint, settings);
        }
    }

    public String getFontVariationSettings() {
        return this.mFontVariationSettings;
    }

    public boolean setFontVariationSettings(String fontVariationSettings) {
        String settings = TextUtils.nullIfEmpty(fontVariationSettings);
        if (settings == this.mFontVariationSettings || (settings != null && settings.equals(this.mFontVariationSettings))) {
            return true;
        }
        if (settings == null || settings.length() == 0) {
            this.mFontVariationSettings = null;
            setTypeface(Typeface.createFromTypefaceWithVariation(this.mTypeface, Collections.emptyList()));
            return true;
        }
        Typeface targetTypeface = this.mTypeface == null ? Typeface.DEFAULT : this.mTypeface;
        FontVariationAxis[] axes = FontVariationAxis.fromFontVariationSettings(settings);
        ArrayList<FontVariationAxis> filteredAxes = new ArrayList();
        for (FontVariationAxis axis : axes) {
            if (targetTypeface.isSupportedAxes(axis.getOpenTypeTagValue())) {
                filteredAxes.add(axis);
            }
        }
        if (filteredAxes.isEmpty()) {
            return false;
        }
        this.mFontVariationSettings = settings;
        setTypeface(Typeface.createFromTypefaceWithVariation(targetTypeface, filteredAxes));
        return true;
    }

    public int getHyphenEdit() {
        return nGetHyphenEdit(this.mNativePaint);
    }

    public void setHyphenEdit(int hyphen) {
        nSetHyphenEdit(this.mNativePaint, hyphen);
    }

    public float ascent() {
        return nAscent(this.mNativePaint, this.mNativeTypeface);
    }

    public float descent() {
        return nDescent(this.mNativePaint, this.mNativeTypeface);
    }

    public float getFontMetrics(FontMetrics metrics) {
        return nGetFontMetrics(this.mNativePaint, this.mNativeTypeface, metrics);
    }

    public FontMetrics getFontMetrics() {
        FontMetrics fm = new FontMetrics();
        getFontMetrics(fm);
        return fm;
    }

    public int getFontMetricsInt(FontMetricsInt fmi) {
        return nGetFontMetricsInt(this.mNativePaint, this.mNativeTypeface, fmi);
    }

    public FontMetricsInt getFontMetricsInt() {
        FontMetricsInt fm = new FontMetricsInt();
        getFontMetricsInt(fm);
        return fm;
    }

    public float getFontSpacing() {
        return getFontMetrics(null);
    }

    public float measureText(char[] text, int index, int count) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if ((index | count) < 0 || index + count > text.length) {
            throw new ArrayIndexOutOfBoundsException();
        } else if (text.length == 0 || count == 0) {
            return TonemapCurve.LEVEL_BLACK;
        } else {
            if (this.mHasCompatScaling) {
                float oldSize = getTextSize();
                setTextSize(this.mCompatScaling * oldSize);
                float w = nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, index, count, index, count, this.mBidiFlags, null, 0);
                setTextSize(oldSize);
                return (float) Math.ceil((double) (this.mInvCompatScaling * w));
            }
            return (float) Math.ceil((double) nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, index, count, index, count, this.mBidiFlags, null, 0));
        }
    }

    public float measureText(String text, int start, int end) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if ((((start | end) | (end - start)) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (text.length() == 0 || start == end) {
            return TonemapCurve.LEVEL_BLACK;
        } else {
            if (this.mHasCompatScaling) {
                float oldSize = getTextSize();
                setTextSize(this.mCompatScaling * oldSize);
                float w = nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, start, end, start, end, this.mBidiFlags, null, 0);
                setTextSize(oldSize);
                return (float) Math.ceil((double) (this.mInvCompatScaling * w));
            }
            return (float) Math.ceil((double) nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, start, end, start, end, this.mBidiFlags, null, 0));
        }
    }

    public float measureText(String text) {
        if (text != null) {
            return measureText(text, 0, text.length());
        }
        throw new IllegalArgumentException("text cannot be null");
    }

    public float measureText(CharSequence text, int start, int end) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if ((((start | end) | (end - start)) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (text.length() == 0 || start == end) {
            return TonemapCurve.LEVEL_BLACK;
        } else {
            if (text instanceof String) {
                return measureText((String) text, start, end);
            }
            if ((text instanceof SpannedString) || (text instanceof SpannableString)) {
                return measureText(text.toString(), start, end);
            }
            if (text instanceof GraphicsOperations) {
                return ((GraphicsOperations) text).measureText(start, end, this);
            }
            char[] buf = TemporaryBuffer.obtain(end - start);
            TextUtils.getChars(text, start, end, buf, 0);
            float result = measureText(buf, 0, end - start);
            TemporaryBuffer.recycle(buf);
            return result;
        }
    }

    public int breakText(char[] text, int index, int count, float maxWidth, float[] measuredWidth) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if (index < 0 || text.length - index < Math.abs(count)) {
            throw new ArrayIndexOutOfBoundsException();
        } else if (text.length == 0 || count == 0) {
            return 0;
        } else {
            if (this.mHasCompatScaling) {
                float oldSize = getTextSize();
                setTextSize(this.mCompatScaling * oldSize);
                int res = nBreakText(this.mNativePaint, this.mNativeTypeface, text, index, count, maxWidth * this.mCompatScaling, this.mBidiFlags, measuredWidth);
                setTextSize(oldSize);
                if (measuredWidth != null) {
                    measuredWidth[0] = measuredWidth[0] * this.mInvCompatScaling;
                }
                return res;
            }
            return nBreakText(this.mNativePaint, this.mNativeTypeface, text, index, count, maxWidth, this.mBidiFlags, measuredWidth);
        }
    }

    public int breakText(CharSequence text, int start, int end, boolean measureForwards, float maxWidth, float[] measuredWidth) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if ((((start | end) | (end - start)) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (text.length() == 0 || start == end) {
            return 0;
        } else {
            if (start == 0 && (text instanceof String) && end == text.length()) {
                return breakText((String) text, measureForwards, maxWidth, measuredWidth);
            }
            int result;
            char[] buf = TemporaryBuffer.obtain(end - start);
            TextUtils.getChars(text, start, end, buf, 0);
            if (measureForwards) {
                result = breakText(buf, 0, end - start, maxWidth, measuredWidth);
            } else {
                result = breakText(buf, 0, -(end - start), maxWidth, measuredWidth);
            }
            TemporaryBuffer.recycle(buf);
            return result;
        }
    }

    public int breakText(String text, boolean measureForwards, float maxWidth, float[] measuredWidth) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if (text.length() == 0) {
            return 0;
        } else {
            if (this.mHasCompatScaling) {
                float oldSize = getTextSize();
                setTextSize(this.mCompatScaling * oldSize);
                int res = nBreakText(this.mNativePaint, this.mNativeTypeface, text, measureForwards, maxWidth * this.mCompatScaling, this.mBidiFlags, measuredWidth);
                setTextSize(oldSize);
                if (measuredWidth != null) {
                    measuredWidth[0] = measuredWidth[0] * this.mInvCompatScaling;
                }
                return res;
            }
            return nBreakText(this.mNativePaint, this.mNativeTypeface, text, measureForwards, maxWidth, this.mBidiFlags, measuredWidth);
        }
    }

    public int getTextWidths(char[] text, int index, int count, float[] widths) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if ((index | count) < 0 || index + count > text.length || count > widths.length) {
            throw new ArrayIndexOutOfBoundsException();
        } else if (text.length == 0 || count == 0) {
            return 0;
        } else {
            if (this.mHasCompatScaling) {
                float oldSize = getTextSize();
                setTextSize(this.mCompatScaling * oldSize);
                nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, index, count, index, count, this.mBidiFlags, widths, 0);
                setTextSize(oldSize);
                for (int i = 0; i < count; i++) {
                    widths[i] = widths[i] * this.mInvCompatScaling;
                }
                return count;
            }
            nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, index, count, index, count, this.mBidiFlags, widths, 0);
            return count;
        }
    }

    public int getTextWidths(CharSequence text, int start, int end, float[] widths) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if ((((start | end) | (end - start)) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (end - start > widths.length) {
            throw new ArrayIndexOutOfBoundsException();
        } else if (text.length() == 0 || start == end) {
            return 0;
        } else {
            if (text instanceof String) {
                return getTextWidths((String) text, start, end, widths);
            }
            if ((text instanceof SpannedString) || (text instanceof SpannableString)) {
                return getTextWidths(text.toString(), start, end, widths);
            }
            if (text instanceof GraphicsOperations) {
                return ((GraphicsOperations) text).getTextWidths(start, end, widths, this);
            }
            char[] buf = TemporaryBuffer.obtain(end - start);
            TextUtils.getChars(text, start, end, buf, 0);
            int result = getTextWidths(buf, 0, end - start, widths);
            TemporaryBuffer.recycle(buf);
            return result;
        }
    }

    public int getTextWidths(String text, int start, int end, float[] widths) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if ((((start | end) | (end - start)) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (end - start > widths.length) {
            throw new ArrayIndexOutOfBoundsException();
        } else if (text.length() == 0 || start == end) {
            return 0;
        } else {
            if (this.mHasCompatScaling) {
                float oldSize = getTextSize();
                setTextSize(this.mCompatScaling * oldSize);
                nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, start, end, start, end, this.mBidiFlags, widths, 0);
                setTextSize(oldSize);
                for (int i = 0; i < end - start; i++) {
                    widths[i] = widths[i] * this.mInvCompatScaling;
                }
                return end - start;
            }
            nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, start, end, start, end, this.mBidiFlags, widths, 0);
            return end - start;
        }
    }

    public int getTextWidths(String text, float[] widths) {
        return getTextWidths(text, 0, text.length(), widths);
    }

    public float getTextRunAdvances(char[] chars, int index, int count, int contextIndex, int contextCount, boolean isRtl, float[] advances, int advancesIndex) {
        if (chars == null) {
            throw new IllegalArgumentException("text cannot be null");
        }
        int i;
        int length = (chars.length - (contextIndex + contextCount)) | (((((((index | count) | contextIndex) | contextCount) | advancesIndex) | (index - contextIndex)) | (contextCount - count)) | ((contextIndex + contextCount) - (index + count)));
        if (advances == null) {
            i = 0;
        } else {
            i = advances.length - (advancesIndex + count);
        }
        if ((i | length) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (chars.length == 0 || count == 0) {
            return TonemapCurve.LEVEL_BLACK;
        } else {
            if (this.mHasCompatScaling) {
                float oldSize = getTextSize();
                setTextSize(this.mCompatScaling * oldSize);
                float res = nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, chars, index, count, contextIndex, contextCount, isRtl ? 5 : 4, advances, advancesIndex);
                setTextSize(oldSize);
                if (advances != null) {
                    int e = advancesIndex + count;
                    for (int i2 = advancesIndex; i2 < e; i2++) {
                        advances[i2] = advances[i2] * this.mInvCompatScaling;
                    }
                }
                return this.mInvCompatScaling * res;
            }
            return nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, chars, index, count, contextIndex, contextCount, isRtl ? 5 : 4, advances, advancesIndex);
        }
    }

    public float getTextRunAdvances(CharSequence text, int start, int end, int contextStart, int contextEnd, boolean isRtl, float[] advances, int advancesIndex) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        }
        int i;
        int length = (text.length() - contextEnd) | (((((((start | end) | contextStart) | contextEnd) | advancesIndex) | (end - start)) | (start - contextStart)) | (contextEnd - end));
        if (advances == null) {
            i = 0;
        } else {
            i = (advances.length - advancesIndex) - (end - start);
        }
        if ((i | length) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (text instanceof String) {
            return getTextRunAdvances((String) text, start, end, contextStart, contextEnd, isRtl, advances, advancesIndex);
        } else if ((text instanceof SpannedString) || (text instanceof SpannableString)) {
            return getTextRunAdvances(text.toString(), start, end, contextStart, contextEnd, isRtl, advances, advancesIndex);
        } else if (text instanceof GraphicsOperations) {
            return ((GraphicsOperations) text).getTextRunAdvances(start, end, contextStart, contextEnd, isRtl, advances, advancesIndex, this);
        } else {
            if (text.length() == 0 || end == start) {
                return TonemapCurve.LEVEL_BLACK;
            }
            int contextLen = contextEnd - contextStart;
            int len = end - start;
            char[] buf = TemporaryBuffer.obtain(contextLen);
            TextUtils.getChars(text, contextStart, contextEnd, buf, 0);
            float result = getTextRunAdvances(buf, start - contextStart, len, 0, contextLen, isRtl, advances, advancesIndex);
            TemporaryBuffer.recycle(buf);
            return result;
        }
    }

    public float getTextRunAdvances(String text, int start, int end, int contextStart, int contextEnd, boolean isRtl, float[] advances, int advancesIndex) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        }
        int i;
        int length = (text.length() - contextEnd) | (((((((start | end) | contextStart) | contextEnd) | advancesIndex) | (end - start)) | (start - contextStart)) | (contextEnd - end));
        if (advances == null) {
            i = 0;
        } else {
            i = (advances.length - advancesIndex) - (end - start);
        }
        if ((i | length) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (text.length() == 0 || start == end) {
            return TonemapCurve.LEVEL_BLACK;
        } else {
            if (this.mHasCompatScaling) {
                float oldSize = getTextSize();
                setTextSize(this.mCompatScaling * oldSize);
                float totalAdvance = nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, start, end, contextStart, contextEnd, isRtl ? 5 : 4, advances, advancesIndex);
                setTextSize(oldSize);
                if (advances != null) {
                    int e = advancesIndex + (end - start);
                    for (int i2 = advancesIndex; i2 < e; i2++) {
                        advances[i2] = advances[i2] * this.mInvCompatScaling;
                    }
                }
                return this.mInvCompatScaling * totalAdvance;
            }
            return nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, start, end, contextStart, contextEnd, isRtl ? 5 : 4, advances, advancesIndex);
        }
    }

    public int getTextRunCursor(char[] text, int contextStart, int contextLength, int dir, int offset, int cursorOpt) {
        int contextEnd = contextStart + contextLength;
        if ((((((((contextStart | contextEnd) | offset) | (contextEnd - contextStart)) | (offset - contextStart)) | (contextEnd - offset)) | (text.length - contextEnd)) | cursorOpt) < 0 || cursorOpt > 4) {
            throw new IndexOutOfBoundsException();
        }
        return nGetTextRunCursor(this.mNativePaint, this.mNativeTypeface, text, contextStart, contextLength, dir, offset, cursorOpt);
    }

    public int getTextRunCursor(CharSequence text, int contextStart, int contextEnd, int dir, int offset, int cursorOpt) {
        if ((text instanceof String) || (text instanceof SpannedString) || (text instanceof SpannableString)) {
            return getTextRunCursor(text.toString(), contextStart, contextEnd, dir, offset, cursorOpt);
        } else if (text instanceof GraphicsOperations) {
            return ((GraphicsOperations) text).getTextRunCursor(contextStart, contextEnd, dir, offset, cursorOpt, this);
        } else {
            int contextLen = contextEnd - contextStart;
            char[] buf = TemporaryBuffer.obtain(contextLen);
            TextUtils.getChars(text, contextStart, contextEnd, buf, 0);
            int relPos = getTextRunCursor(buf, 0, contextLen, dir, offset - contextStart, cursorOpt);
            TemporaryBuffer.recycle(buf);
            return relPos == -1 ? -1 : relPos + contextStart;
        }
    }

    public int getTextRunCursor(String text, int contextStart, int contextEnd, int dir, int offset, int cursorOpt) {
        if ((((((((contextStart | contextEnd) | offset) | (contextEnd - contextStart)) | (offset - contextStart)) | (contextEnd - offset)) | (text.length() - contextEnd)) | cursorOpt) < 0 || cursorOpt > 4) {
            throw new IndexOutOfBoundsException();
        }
        return nGetTextRunCursor(this.mNativePaint, this.mNativeTypeface, text, contextStart, contextEnd, dir, offset, cursorOpt);
    }

    public void getTextPath(char[] text, int index, int count, float x, float y, Path path) {
        if ((index | count) < 0 || index + count > text.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        nGetTextPath(this.mNativePaint, this.mNativeTypeface, this.mBidiFlags, text, index, count, x, y, path.mutateNI());
    }

    public void getTextPath(String text, int start, int end, float x, float y, Path path) {
        if ((((start | end) | (end - start)) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        }
        nGetTextPath(this.mNativePaint, this.mNativeTypeface, this.mBidiFlags, text, start, end, x, y, path.mutateNI());
    }

    public void getTextBounds(String text, int start, int end, Rect bounds) {
        if ((((start | end) | (end - start)) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (bounds == null) {
            throw new NullPointerException("need bounds Rect");
        } else {
            nGetStringBounds(this.mNativePaint, this.mNativeTypeface, text, start, end, this.mBidiFlags, bounds);
        }
    }

    public void getTextBounds(CharSequence text, int start, int end, Rect bounds) {
        if ((((start | end) | (end - start)) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (bounds == null) {
            throw new NullPointerException("need bounds Rect");
        } else {
            char[] buf = TemporaryBuffer.obtain(end - start);
            TextUtils.getChars(text, start, end, buf, 0);
            getTextBounds(buf, 0, end - start, bounds);
            TemporaryBuffer.recycle(buf);
        }
    }

    public void getTextBounds(char[] text, int index, int count, Rect bounds) {
        if ((index | count) < 0 || index + count > text.length) {
            throw new ArrayIndexOutOfBoundsException();
        } else if (bounds == null) {
            throw new NullPointerException("need bounds Rect");
        } else {
            nGetCharArrayBounds(this.mNativePaint, this.mNativeTypeface, text, index, count, this.mBidiFlags, bounds);
        }
    }

    public boolean hasGlyph(String string) {
        return nHasGlyph(this.mNativePaint, this.mNativeTypeface, this.mBidiFlags, string);
    }

    public float getRunAdvance(char[] text, int start, int end, int contextStart, int contextEnd, boolean isRtl, int offset) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if ((((((((((contextStart | start) | offset) | end) | contextEnd) | (start - contextStart)) | (offset - start)) | (end - offset)) | (contextEnd - end)) | (text.length - contextEnd)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (end == start) {
            return TonemapCurve.LEVEL_BLACK;
        } else {
            return nGetRunAdvance(this.mNativePaint, this.mNativeTypeface, text, start, end, contextStart, contextEnd, isRtl, offset);
        }
    }

    public float getRunAdvance(CharSequence text, int start, int end, int contextStart, int contextEnd, boolean isRtl, int offset) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if ((((((((((contextStart | start) | offset) | end) | contextEnd) | (start - contextStart)) | (offset - start)) | (end - offset)) | (contextEnd - end)) | (text.length() - contextEnd)) < 0) {
            throw new IndexOutOfBoundsException("contextStart : " + contextStart + ", start : " + start + ", offset : " + offset + ", end : " + end + ", contextEnd : " + contextEnd + ", text.length() : " + text.length() + ", isRtl : " + isRtl + ", text : " + text);
        } else if (end == start) {
            return TonemapCurve.LEVEL_BLACK;
        } else {
            char[] buf = TemporaryBuffer.obtain(contextEnd - contextStart);
            TextUtils.getChars(text, contextStart, contextEnd, buf, 0);
            float result = getRunAdvance(buf, start - contextStart, end - contextStart, 0, contextEnd - contextStart, isRtl, offset - contextStart);
            TemporaryBuffer.recycle(buf);
            return result;
        }
    }

    public int getOffsetForAdvance(char[] text, int start, int end, int contextStart, int contextEnd, boolean isRtl, float advance) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if ((((((((contextStart | start) | end) | contextEnd) | (start - contextStart)) | (end - start)) | (contextEnd - end)) | (text.length - contextEnd)) >= 0) {
            return nGetOffsetForAdvance(this.mNativePaint, this.mNativeTypeface, text, start, end, contextStart, contextEnd, isRtl, advance);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public int getOffsetForAdvance(CharSequence text, int start, int end, int contextStart, int contextEnd, boolean isRtl, float advance) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if ((((((((contextStart | start) | end) | contextEnd) | (start - contextStart)) | (end - start)) | (contextEnd - end)) | (text.length() - contextEnd)) < 0) {
            throw new IndexOutOfBoundsException();
        } else {
            char[] buf = TemporaryBuffer.obtain(contextEnd - contextStart);
            TextUtils.getChars(text, contextStart, contextEnd, buf, 0);
            int result = getOffsetForAdvance(buf, start - contextStart, end - contextStart, 0, contextEnd - contextStart, isRtl, advance) + contextStart;
            TemporaryBuffer.recycle(buf);
            return result;
        }
    }
}
