package android.graphics;

import android.net.ProxyInfo;
import android.os.LocaleList;
import android.speech.tts.TextToSpeech.Engine;
import android.text.GraphicsOperations;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;
import com.android.internal.annotations.GuardedBy;
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
    public static final int LCD_RENDER_TEXT_FLAG = 512;
    public static final int LINEAR_TEXT_FLAG = 64;
    private static final long NATIVE_PAINT_SIZE = 98;
    public static final int STRIKE_THRU_TEXT_FLAG = 16;
    public static final int SUBPIXEL_TEXT_FLAG = 128;
    public static final int UNDERLINE_TEXT_FLAG = 8;
    public static final int VERTICAL_TEXT_FLAG = 4096;
    static final Align[] sAlignArray = null;
    private static final Object sCacheLock = null;
    static final Cap[] sCapArray = null;
    static final Join[] sJoinArray = null;
    @GuardedBy("sCacheLock")
    private static final HashMap<String, Integer> sMinikinLangListIdCache = null;
    static final Style[] sStyleArray = null;
    public int mBidiFlags;
    private ColorFilter mColorFilter;
    private float mCompatScaling;
    private String mFontFeatureSettings;
    private boolean mHasCompatScaling;
    private float mInvCompatScaling;
    private LocaleList mLocales;
    private MaskFilter mMaskFilter;
    private long mNativePaint;
    private long mNativeShader;
    public long mNativeTypeface;
    private PathEffect mPathEffect;
    private Rasterizer mRasterizer;
    private Shader mShader;
    private Typeface mTypeface;
    private Xfermode mXfermode;

    public enum Align {
        ;
        
        final int nativeInt;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.Paint.Align.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.Paint.Align.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.Paint.Align.<clinit>():void");
        }

        private Align(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    public enum Cap {
        ;
        
        final int nativeInt;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.Paint.Cap.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.Paint.Cap.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.Paint.Cap.<clinit>():void");
        }

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

        public FontMetrics() {
        }
    }

    public static class FontMetricsInt {
        public int ascent;
        public int bottom;
        public int descent;
        public int leading;
        public int top;

        public FontMetricsInt() {
        }

        public String toString() {
            return "FontMetricsInt: top=" + this.top + " ascent=" + this.ascent + " descent=" + this.descent + " bottom=" + this.bottom + " leading=" + this.leading;
        }
    }

    public enum Join {
        ;
        
        final int nativeInt;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.Paint.Join.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.Paint.Join.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.Paint.Join.<clinit>():void");
        }

        private Join(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    private static class NoImagePreloadHolder {
        public static final NativeAllocationRegistry sRegistry = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.Paint.NoImagePreloadHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.Paint.NoImagePreloadHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.Paint.NoImagePreloadHolder.<clinit>():void");
        }

        private NoImagePreloadHolder() {
        }
    }

    public enum Style {
        ;
        
        final int nativeInt;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.Paint.Style.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.Paint.Style.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.Paint.Style.<clinit>():void");
        }

        private Style(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.Paint.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.Paint.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.Paint.<clinit>():void");
    }

    private native float nAscent(long j, long j2);

    private static native int nBreakText(long j, long j2, String str, boolean z, float f, int i, float[] fArr);

    private static native int nBreakText(long j, long j2, char[] cArr, int i, int i2, float f, int i3, float[] fArr);

    private native float nDescent(long j, long j2);

    private native int nGetAlpha(long j);

    private static native void nGetCharArrayBounds(long j, long j2, char[] cArr, int i, int i2, int i3, Rect rect);

    private native int nGetColor(long j);

    private static native boolean nGetFillPath(long j, long j2, long j3);

    private native int nGetFlags(long j);

    private native float nGetFontMetrics(long j, long j2, FontMetrics fontMetrics);

    private native int nGetFontMetricsInt(long j, long j2, FontMetricsInt fontMetricsInt);

    private native int nGetHinting(long j);

    private static native int nGetHyphenEdit(long j);

    private static native float nGetLetterSpacing(long j);

    private static native long nGetNativeFinalizer();

    private static native int nGetOffsetForAdvance(long j, long j2, char[] cArr, int i, int i2, int i3, int i4, boolean z, float f);

    private static native float nGetRunAdvance(long j, long j2, char[] cArr, int i, int i2, int i3, int i4, boolean z, int i5);

    private static native void nGetStringBounds(long j, long j2, String str, int i, int i2, int i3, Rect rect);

    private static native int nGetStrokeCap(long j);

    private static native int nGetStrokeJoin(long j);

    private native float nGetStrokeMiter(long j);

    private native float nGetStrokeWidth(long j);

    private static native int nGetStyle(long j);

    private static native float nGetTextAdvances(long j, long j2, String str, int i, int i2, int i3, int i4, int i5, float[] fArr, int i6);

    private static native float nGetTextAdvances(long j, long j2, char[] cArr, int i, int i2, int i3, int i4, int i5, float[] fArr, int i6);

    private static native int nGetTextAlign(long j);

    private static native void nGetTextPath(long j, long j2, int i, String str, int i2, int i3, float f, float f2, long j3);

    private static native void nGetTextPath(long j, long j2, int i, char[] cArr, int i2, int i3, float f, float f2, long j3);

    private native int nGetTextRunCursor(long j, String str, int i, int i2, int i3, int i4, int i5);

    private native int nGetTextRunCursor(long j, char[] cArr, int i, int i2, int i3, int i4, int i5);

    private native float nGetTextScaleX(long j);

    private native float nGetTextSize(long j);

    private native float nGetTextSkewX(long j);

    private static native boolean nHasGlyph(long j, long j2, int i, String str);

    private static native boolean nHasShadowLayer(long j);

    private static native long nInit();

    private static native long nInitWithPaint(long j);

    private native boolean nIsElegantTextHeight(long j);

    private static native void nReset(long j);

    private static native void nSet(long j, long j2);

    private native void nSetAlpha(long j, int i);

    private native void nSetAntiAlias(long j, boolean z);

    private native void nSetColor(long j, int i);

    private static native long nSetColorFilter(long j, long j2);

    private native void nSetDither(long j, boolean z);

    private native void nSetElegantTextHeight(long j, boolean z);

    private native void nSetFakeBoldText(long j, boolean z);

    private native void nSetFilterBitmap(long j, boolean z);

    private native void nSetFlags(long j, int i);

    private static native void nSetFontFeatureSettings(long j, String str);

    private native void nSetHinting(long j, int i);

    private static native void nSetHyphenEdit(long j, int i);

    private static native void nSetLetterSpacing(long j, float f);

    private native void nSetLinearText(long j, boolean z);

    private static native long nSetMaskFilter(long j, long j2);

    private static native long nSetPathEffect(long j, long j2);

    private static native long nSetRasterizer(long j, long j2);

    private static native long nSetShader(long j, long j2);

    private static native void nSetShadowLayer(long j, float f, float f2, float f3, int i);

    private native void nSetStrikeThruText(long j, boolean z);

    private static native void nSetStrokeCap(long j, int i);

    private static native void nSetStrokeJoin(long j, int i);

    private native void nSetStrokeMiter(long j, float f);

    private native void nSetStrokeWidth(long j, float f);

    private static native void nSetStyle(long j, int i);

    private native void nSetSubpixelText(long j, boolean z);

    private static native void nSetTextAlign(long j, int i);

    private static native int nSetTextLocales(long j, String str);

    private static native void nSetTextLocalesByMinikinLangListId(long j, int i);

    private native void nSetTextScaleX(long j, float f);

    private native void nSetTextSize(long j, float f);

    private native void nSetTextSkewX(long j, float f);

    private static native long nSetTypeface(long j, long j2);

    private native void nSetUnderlineText(long j, boolean z);

    private static native long nSetXfermode(long j, long j2);

    public Paint() {
        this((int) HINTING_OFF);
    }

    public Paint(int flags) {
        this.mNativeShader = 0;
        this.mBidiFlags = FILTER_BITMAP_FLAG;
        this.mNativePaint = nInit();
        NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativePaint);
        setFlags(flags | HIDDEN_DEFAULT_PAINT_FLAGS);
        this.mInvCompatScaling = Engine.DEFAULT_VOLUME;
        this.mCompatScaling = Engine.DEFAULT_VOLUME;
        setTextLocales(LocaleList.getAdjustedDefault());
    }

    public Paint(Paint paint) {
        this.mNativeShader = 0;
        this.mBidiFlags = FILTER_BITMAP_FLAG;
        this.mNativePaint = nInitWithPaint(paint.getNativeInstance());
        NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativePaint);
        setClassVariablesFrom(paint);
    }

    public void reset() {
        nReset(this.mNativePaint);
        setFlags(HIDDEN_DEFAULT_PAINT_FLAGS);
        this.mColorFilter = null;
        this.mMaskFilter = null;
        this.mPathEffect = null;
        this.mRasterizer = null;
        this.mShader = null;
        this.mNativeShader = 0;
        this.mTypeface = null;
        this.mNativeTypeface = 0;
        this.mXfermode = null;
        this.mHasCompatScaling = false;
        this.mCompatScaling = Engine.DEFAULT_VOLUME;
        this.mInvCompatScaling = Engine.DEFAULT_VOLUME;
        this.mBidiFlags = FILTER_BITMAP_FLAG;
        setTextLocales(LocaleList.getAdjustedDefault());
        setElegantTextHeight(false);
        this.mFontFeatureSettings = null;
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
        this.mRasterizer = paint.mRasterizer;
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
    }

    public void setCompatibilityScaling(float factor) {
        if (((double) factor) == 1.0d) {
            this.mHasCompatScaling = false;
            this.mInvCompatScaling = Engine.DEFAULT_VOLUME;
            this.mCompatScaling = Engine.DEFAULT_VOLUME;
            return;
        }
        this.mHasCompatScaling = true;
        this.mCompatScaling = factor;
        this.mInvCompatScaling = Engine.DEFAULT_VOLUME / factor;
    }

    public long getNativeInstance() {
        long newNativeShader = this.mShader == null ? 0 : this.mShader.getNativeInstance();
        if (newNativeShader != this.mNativeShader) {
            this.mNativeShader = newNativeShader;
            nSetShader(this.mNativePaint, this.mNativeShader);
        }
        return this.mNativePaint;
    }

    public int getBidiFlags() {
        return this.mBidiFlags;
    }

    public void setBidiFlags(int flags) {
        flags &= BIDI_FLAG_MASK;
        if (flags > BIDI_MAX_FLAG_VALUE) {
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
        return (getFlags() & HINTING_ON) != 0;
    }

    public void setAntiAlias(boolean aa) {
        nSetAntiAlias(this.mNativePaint, aa);
    }

    public final boolean isDither() {
        return (getFlags() & DITHER_FLAG) != 0;
    }

    public void setDither(boolean dither) {
        nSetDither(this.mNativePaint, dither);
    }

    public final boolean isLinearText() {
        return (getFlags() & LINEAR_TEXT_FLAG) != 0;
    }

    public void setLinearText(boolean linearText) {
        nSetLinearText(this.mNativePaint, linearText);
    }

    public final boolean isSubpixelText() {
        return (getFlags() & SUBPIXEL_TEXT_FLAG) != 0;
    }

    public void setSubpixelText(boolean subpixelText) {
        nSetSubpixelText(this.mNativePaint, subpixelText);
    }

    public final boolean isUnderlineText() {
        return (getFlags() & UNDERLINE_TEXT_FLAG) != 0;
    }

    public void setUnderlineText(boolean underlineText) {
        nSetUnderlineText(this.mNativePaint, underlineText);
    }

    public final boolean isStrikeThruText() {
        return (getFlags() & STRIKE_THRU_TEXT_FLAG) != 0;
    }

    public void setStrikeThruText(boolean strikeThruText) {
        nSetStrikeThruText(this.mNativePaint, strikeThruText);
    }

    public final boolean isFakeBoldText() {
        return (getFlags() & FAKE_BOLD_TEXT_FLAG) != 0;
    }

    public void setFakeBoldText(boolean fakeBoldText) {
        nSetFakeBoldText(this.mNativePaint, fakeBoldText);
    }

    public final boolean isFilterBitmap() {
        return (getFlags() & FILTER_BITMAP_FLAG) != 0;
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
        setColor((((a << 24) | (r << STRIKE_THRU_TEXT_FLAG)) | (g << UNDERLINE_TEXT_FLAG)) | b);
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
        return nGetFillPath(this.mNativePaint, src.ni(), dst.ni());
    }

    public Shader getShader() {
        return this.mShader;
    }

    public Shader setShader(Shader shader) {
        if (this.mShader != shader) {
            this.mNativeShader = -1;
        }
        this.mShader = shader;
        return shader;
    }

    public ColorFilter getColorFilter() {
        return this.mColorFilter;
    }

    public ColorFilter setColorFilter(ColorFilter filter) {
        long filterNative = 0;
        if (filter != null) {
            filterNative = filter.native_instance;
        }
        nSetColorFilter(this.mNativePaint, filterNative);
        this.mColorFilter = filter;
        return filter;
    }

    public Xfermode getXfermode() {
        return this.mXfermode;
    }

    public Xfermode setXfermode(Xfermode xfermode) {
        long xfermodeNative = 0;
        if (xfermode != null) {
            xfermodeNative = xfermode.native_instance;
        }
        nSetXfermode(this.mNativePaint, xfermodeNative);
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
        return this.mRasterizer;
    }

    @Deprecated
    public Rasterizer setRasterizer(Rasterizer rasterizer) {
        long rasterizerNative = 0;
        if (rasterizer != null) {
            rasterizerNative = rasterizer.native_instance;
        }
        nSetRasterizer(this.mNativePaint, rasterizerNative);
        this.mRasterizer = rasterizer;
        return rasterizer;
    }

    public void setShadowLayer(float radius, float dx, float dy, int shadowColor) {
        nSetShadowLayer(this.mNativePaint, radius, dx, dy, shadowColor);
    }

    public void clearShadowLayer() {
        setShadowLayer(0.0f, 0.0f, 0.0f, HINTING_OFF);
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
        return this.mLocales.get(HINTING_OFF);
    }

    public LocaleList getTextLocales() {
        return this.mLocales;
    }

    public void setTextLocale(Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException("locale cannot be null");
        } else if (this.mLocales == null || this.mLocales.size() != HINTING_ON || !locale.equals(this.mLocales.get(HINTING_OFF))) {
            Locale[] localeArr = new Locale[HINTING_ON];
            localeArr[HINTING_OFF] = locale;
            this.mLocales = new LocaleList(localeArr);
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
            return 0.0f;
        } else {
            if (this.mHasCompatScaling) {
                float oldSize = getTextSize();
                setTextSize(this.mCompatScaling * oldSize);
                float w = nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, index, count, index, count, this.mBidiFlags, null, (int) HINTING_OFF);
                setTextSize(oldSize);
                return (float) Math.ceil((double) (this.mInvCompatScaling * w));
            }
            return (float) Math.ceil((double) nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, index, count, index, count, this.mBidiFlags, null, (int) HINTING_OFF));
        }
    }

    public float measureText(String text, int start, int end) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if ((((start | end) | (end - start)) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (text.length() == 0 || start == end) {
            return 0.0f;
        } else {
            if (this.mHasCompatScaling) {
                float oldSize = getTextSize();
                setTextSize(this.mCompatScaling * oldSize);
                float w = nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, start, end, start, end, this.mBidiFlags, null, (int) HINTING_OFF);
                setTextSize(oldSize);
                return (float) Math.ceil((double) (this.mInvCompatScaling * w));
            }
            return (float) Math.ceil((double) nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, start, end, start, end, this.mBidiFlags, null, (int) HINTING_OFF));
        }
    }

    public float measureText(String text) {
        if (text != null) {
            return measureText(text, (int) HINTING_OFF, text.length());
        }
        throw new IllegalArgumentException("text cannot be null");
    }

    public float measureText(CharSequence text, int start, int end) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if ((((start | end) | (end - start)) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (text.length() == 0 || start == end) {
            return 0.0f;
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
            TextUtils.getChars(text, start, end, buf, HINTING_OFF);
            float result = measureText(buf, (int) HINTING_OFF, end - start);
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
            return HINTING_OFF;
        } else {
            if (this.mHasCompatScaling) {
                float oldSize = getTextSize();
                setTextSize(this.mCompatScaling * oldSize);
                int res = nBreakText(this.mNativePaint, this.mNativeTypeface, text, index, count, maxWidth * this.mCompatScaling, this.mBidiFlags, measuredWidth);
                setTextSize(oldSize);
                if (measuredWidth != null) {
                    measuredWidth[HINTING_OFF] = measuredWidth[HINTING_OFF] * this.mInvCompatScaling;
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
            return HINTING_OFF;
        } else {
            if (start == 0 && (text instanceof String) && end == text.length()) {
                return breakText((String) text, measureForwards, maxWidth, measuredWidth);
            }
            int result;
            char[] buf = TemporaryBuffer.obtain(end - start);
            TextUtils.getChars(text, start, end, buf, HINTING_OFF);
            if (measureForwards) {
                result = breakText(buf, HINTING_OFF, end - start, maxWidth, measuredWidth);
            } else {
                result = breakText(buf, HINTING_OFF, -(end - start), maxWidth, measuredWidth);
            }
            TemporaryBuffer.recycle(buf);
            return result;
        }
    }

    public int breakText(String text, boolean measureForwards, float maxWidth, float[] measuredWidth) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if (text.length() == 0) {
            return HINTING_OFF;
        } else {
            if (this.mHasCompatScaling) {
                float oldSize = getTextSize();
                setTextSize(this.mCompatScaling * oldSize);
                int res = nBreakText(this.mNativePaint, this.mNativeTypeface, text, measureForwards, maxWidth * this.mCompatScaling, this.mBidiFlags, measuredWidth);
                setTextSize(oldSize);
                if (measuredWidth != null) {
                    measuredWidth[HINTING_OFF] = measuredWidth[HINTING_OFF] * this.mInvCompatScaling;
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
            return HINTING_OFF;
        } else {
            if (this.mHasCompatScaling) {
                float oldSize = getTextSize();
                setTextSize(this.mCompatScaling * oldSize);
                nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, index, count, index, count, this.mBidiFlags, widths, (int) HINTING_OFF);
                setTextSize(oldSize);
                for (int i = HINTING_OFF; i < count; i += HINTING_ON) {
                    widths[i] = widths[i] * this.mInvCompatScaling;
                }
                return count;
            }
            nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, index, count, index, count, this.mBidiFlags, widths, (int) HINTING_OFF);
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
            return HINTING_OFF;
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
            TextUtils.getChars(text, start, end, buf, HINTING_OFF);
            int result = getTextWidths(buf, (int) HINTING_OFF, end - start, widths);
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
            return HINTING_OFF;
        } else {
            if (this.mHasCompatScaling) {
                float oldSize = getTextSize();
                setTextSize(this.mCompatScaling * oldSize);
                nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, start, end, start, end, this.mBidiFlags, widths, (int) HINTING_OFF);
                setTextSize(oldSize);
                for (int i = HINTING_OFF; i < end - start; i += HINTING_ON) {
                    widths[i] = widths[i] * this.mInvCompatScaling;
                }
                return end - start;
            }
            nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, start, end, start, end, this.mBidiFlags, widths, (int) HINTING_OFF);
            return end - start;
        }
    }

    public int getTextWidths(String text, float[] widths) {
        return getTextWidths(text, (int) HINTING_OFF, text.length(), widths);
    }

    public float getTextRunAdvances(char[] chars, int index, int count, int contextIndex, int contextCount, boolean isRtl, float[] advances, int advancesIndex) {
        if (chars == null) {
            throw new IllegalArgumentException("text cannot be null");
        }
        int i;
        int length = (chars.length - (contextIndex + contextCount)) | (((((((index | count) | contextIndex) | contextCount) | advancesIndex) | (index - contextIndex)) | (contextCount - count)) | ((contextIndex + contextCount) - (index + count)));
        if (advances == null) {
            i = HINTING_OFF;
        } else {
            i = advances.length - (advancesIndex + count);
        }
        if ((i | length) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (chars.length == 0 || count == 0) {
            return 0.0f;
        } else {
            if (this.mHasCompatScaling) {
                float oldSize = getTextSize();
                setTextSize(this.mCompatScaling * oldSize);
                float res = nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, chars, index, count, contextIndex, contextCount, isRtl ? BIDI_MAX_FLAG_VALUE : DITHER_FLAG, advances, advancesIndex);
                setTextSize(oldSize);
                if (advances != null) {
                    int e = advancesIndex + count;
                    for (int i2 = advancesIndex; i2 < e; i2 += HINTING_ON) {
                        advances[i2] = advances[i2] * this.mInvCompatScaling;
                    }
                }
                return this.mInvCompatScaling * res;
            }
            return nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, chars, index, count, contextIndex, contextCount, isRtl ? BIDI_MAX_FLAG_VALUE : DITHER_FLAG, advances, advancesIndex);
        }
    }

    public float getTextRunAdvances(CharSequence text, int start, int end, int contextStart, int contextEnd, boolean isRtl, float[] advances, int advancesIndex) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        }
        int i;
        int length = (text.length() - contextEnd) | (((((((start | end) | contextStart) | contextEnd) | advancesIndex) | (end - start)) | (start - contextStart)) | (contextEnd - end));
        if (advances == null) {
            i = HINTING_OFF;
        } else {
            i = (advances.length - advancesIndex) - (end - start);
        }
        if ((i | length) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (text instanceof String) {
            return getTextRunAdvances((String) text, start, end, contextStart, contextEnd, isRtl, advances, advancesIndex);
        } else {
            if ((text instanceof SpannedString) || (text instanceof SpannableString)) {
                return getTextRunAdvances(text.toString(), start, end, contextStart, contextEnd, isRtl, advances, advancesIndex);
            }
            if (text instanceof GraphicsOperations) {
                return ((GraphicsOperations) text).getTextRunAdvances(start, end, contextStart, contextEnd, isRtl, advances, advancesIndex, this);
            }
            if (text.length() == 0 || end == start) {
                return 0.0f;
            }
            int contextLen = contextEnd - contextStart;
            int len = end - start;
            char[] buf = TemporaryBuffer.obtain(contextLen);
            TextUtils.getChars(text, contextStart, contextEnd, buf, HINTING_OFF);
            float result = getTextRunAdvances(buf, start - contextStart, len, (int) HINTING_OFF, contextLen, isRtl, advances, advancesIndex);
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
            i = HINTING_OFF;
        } else {
            i = (advances.length - advancesIndex) - (end - start);
        }
        if ((i | length) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (text.length() == 0 || start == end) {
            return 0.0f;
        } else {
            if (this.mHasCompatScaling) {
                float oldSize = getTextSize();
                setTextSize(this.mCompatScaling * oldSize);
                float totalAdvance = nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, start, end, contextStart, contextEnd, isRtl ? BIDI_MAX_FLAG_VALUE : DITHER_FLAG, advances, advancesIndex);
                setTextSize(oldSize);
                if (advances != null) {
                    int e = advancesIndex + (end - start);
                    for (int i2 = advancesIndex; i2 < e; i2 += HINTING_ON) {
                        advances[i2] = advances[i2] * this.mInvCompatScaling;
                    }
                }
                return this.mInvCompatScaling * totalAdvance;
            }
            return nGetTextAdvances(this.mNativePaint, this.mNativeTypeface, text, start, end, contextStart, contextEnd, isRtl ? BIDI_MAX_FLAG_VALUE : DITHER_FLAG, advances, advancesIndex);
        }
    }

    public int getTextRunCursor(char[] text, int contextStart, int contextLength, int dir, int offset, int cursorOpt) {
        int contextEnd = contextStart + contextLength;
        if ((((((((contextStart | contextEnd) | offset) | (contextEnd - contextStart)) | (offset - contextStart)) | (contextEnd - offset)) | (text.length - contextEnd)) | cursorOpt) >= 0 && cursorOpt <= DITHER_FLAG) {
            return nGetTextRunCursor(this.mNativePaint, text, contextStart, contextLength, dir, offset, cursorOpt);
        }
        throw new IndexOutOfBoundsException();
    }

    public int getTextRunCursor(CharSequence text, int contextStart, int contextEnd, int dir, int offset, int cursorOpt) {
        if ((text instanceof String) || (text instanceof SpannedString) || (text instanceof SpannableString)) {
            return getTextRunCursor(text.toString(), contextStart, contextEnd, dir, offset, cursorOpt);
        }
        if (text instanceof GraphicsOperations) {
            return ((GraphicsOperations) text).getTextRunCursor(contextStart, contextEnd, dir, offset, cursorOpt, this);
        }
        int contextLen = contextEnd - contextStart;
        char[] buf = TemporaryBuffer.obtain(contextLen);
        TextUtils.getChars(text, contextStart, contextEnd, buf, HINTING_OFF);
        int relPos = getTextRunCursor(buf, (int) HINTING_OFF, contextLen, dir, offset - contextStart, cursorOpt);
        TemporaryBuffer.recycle(buf);
        return relPos == -1 ? -1 : relPos + contextStart;
    }

    public int getTextRunCursor(String text, int contextStart, int contextEnd, int dir, int offset, int cursorOpt) {
        if ((((((((contextStart | contextEnd) | offset) | (contextEnd - contextStart)) | (offset - contextStart)) | (contextEnd - offset)) | (text.length() - contextEnd)) | cursorOpt) >= 0 && cursorOpt <= DITHER_FLAG) {
            return nGetTextRunCursor(this.mNativePaint, text, contextStart, contextEnd, dir, offset, cursorOpt);
        }
        throw new IndexOutOfBoundsException();
    }

    public void getTextPath(char[] text, int index, int count, float x, float y, Path path) {
        if ((index | count) < 0 || index + count > text.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        nGetTextPath(this.mNativePaint, this.mNativeTypeface, this.mBidiFlags, text, index, count, x, y, path.ni());
    }

    public void getTextPath(String text, int start, int end, float x, float y, Path path) {
        if ((((start | end) | (end - start)) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        }
        nGetTextPath(this.mNativePaint, this.mNativeTypeface, this.mBidiFlags, text, start, end, x, y, path.ni());
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
            return 0.0f;
        } else {
            return nGetRunAdvance(this.mNativePaint, this.mNativeTypeface, text, start, end, contextStart, contextEnd, isRtl, offset);
        }
    }

    public float getRunAdvance(CharSequence text, int start, int end, int contextStart, int contextEnd, boolean isRtl, int offset) {
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        } else if ((((((((((contextStart | start) | offset) | end) | contextEnd) | (start - contextStart)) | (offset - start)) | (end - offset)) | (contextEnd - end)) | (text.length() - contextEnd)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (end == start) {
            return 0.0f;
        } else {
            char[] buf = TemporaryBuffer.obtain(contextEnd - contextStart);
            TextUtils.getChars(text, contextStart, contextEnd, buf, HINTING_OFF);
            float result = getRunAdvance(buf, start - contextStart, end - contextStart, (int) HINTING_OFF, contextEnd - contextStart, isRtl, offset - contextStart);
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
            TextUtils.getChars(text, contextStart, contextEnd, buf, HINTING_OFF);
            int result = getOffsetForAdvance(buf, start - contextStart, end - contextStart, (int) HINTING_OFF, contextEnd - contextStart, isRtl, advance) + contextStart;
            TemporaryBuffer.recycle(buf);
            return result;
        }
    }
}
