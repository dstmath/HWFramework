package android.graphics.drawable;

import android.annotation.UnsupportedAppUsage;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ImageDecoder;
import android.graphics.Insets;
import android.graphics.NinePatch;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Xfermode;
import android.os.Trace;
import android.util.AttributeSet;
import android.util.Log;
import android.util.StateSet;
import android.util.TypedValue;
import android.util.Xml;
import com.android.internal.R;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class Drawable {
    static final BlendMode DEFAULT_BLEND_MODE = BlendMode.SRC_IN;
    static final PorterDuff.Mode DEFAULT_TINT_MODE = PorterDuff.Mode.SRC_IN;
    private static final Rect ZERO_BOUNDS_RECT = new Rect();
    private Rect mBounds = ZERO_BOUNDS_RECT;
    @UnsupportedAppUsage
    private WeakReference<Callback> mCallback = null;
    private int mChangingConfigurations = 0;
    public Drawable mHwShader;
    private int mLayoutDirection;
    private int mLevel = 0;
    private boolean mSetBlendModeInvoked = false;
    private boolean mSetTintModeInvoked = false;
    @UnsupportedAppUsage
    protected int mSrcDensityOverride = 0;
    private int[] mStateSet = StateSet.WILD_CARD;
    private boolean mVisible = true;

    public interface Callback {
        void invalidateDrawable(Drawable drawable);

        void scheduleDrawable(Drawable drawable, Runnable runnable, long j);

        void unscheduleDrawable(Drawable drawable, Runnable runnable);
    }

    public abstract void draw(Canvas canvas);

    @Deprecated
    public abstract int getOpacity();

    public abstract void setAlpha(int i);

    public abstract void setColorFilter(ColorFilter colorFilter);

    public void setBounds(int left, int top, int right, int bottom) {
        Rect oldBounds = this.mBounds;
        if (oldBounds == ZERO_BOUNDS_RECT) {
            Rect rect = new Rect();
            this.mBounds = rect;
            oldBounds = rect;
        }
        if (oldBounds.left != left || oldBounds.top != top || oldBounds.right != right || oldBounds.bottom != bottom) {
            if (!oldBounds.isEmpty()) {
                invalidateSelf();
            }
            this.mBounds.set(left, top, right, bottom);
            onBoundsChange(this.mBounds);
        }
    }

    public void setBounds(Rect bounds) {
        setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
    }

    public void setShader(Drawable drawable) {
        this.mHwShader = drawable;
        Drawable drawable2 = this.mHwShader;
        if (drawable2 != null) {
            drawable2.setBounds(copyBounds());
        }
    }

    public final void copyBounds(Rect bounds) {
        bounds.set(this.mBounds);
    }

    public final Rect copyBounds() {
        return new Rect(this.mBounds);
    }

    public final Rect getBounds() {
        if (this.mBounds == ZERO_BOUNDS_RECT) {
            this.mBounds = new Rect();
        }
        return this.mBounds;
    }

    public Rect getDirtyBounds() {
        return getBounds();
    }

    public void setChangingConfigurations(int configs) {
        this.mChangingConfigurations = configs;
    }

    public int getChangingConfigurations() {
        return this.mChangingConfigurations;
    }

    @Deprecated
    public void setDither(boolean dither) {
    }

    public void setFilterBitmap(boolean filter) {
    }

    public boolean isFilterBitmap() {
        return false;
    }

    public final void setCallback(Callback cb) {
        this.mCallback = cb != null ? new WeakReference<>(cb) : null;
    }

    public Callback getCallback() {
        WeakReference<Callback> weakReference = this.mCallback;
        if (weakReference != null) {
            return weakReference.get();
        }
        return null;
    }

    public void invalidateSelf() {
        Callback callback = getCallback();
        if (callback != null) {
            callback.invalidateDrawable(this);
        }
    }

    public void scheduleSelf(Runnable what, long when) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.scheduleDrawable(this, what, when);
        }
    }

    public void unscheduleSelf(Runnable what) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.unscheduleDrawable(this, what);
        }
    }

    public int getLayoutDirection() {
        return this.mLayoutDirection;
    }

    public final boolean setLayoutDirection(int layoutDirection) {
        if (this.mLayoutDirection == layoutDirection) {
            return false;
        }
        this.mLayoutDirection = layoutDirection;
        return onLayoutDirectionChanged(layoutDirection);
    }

    public boolean onLayoutDirectionChanged(int layoutDirection) {
        return false;
    }

    public int getAlpha() {
        return 255;
    }

    public void setXfermode(Xfermode mode) {
    }

    @Deprecated
    public void setColorFilter(int color, PorterDuff.Mode mode) {
        if (getColorFilter() instanceof PorterDuffColorFilter) {
            PorterDuffColorFilter existing = (PorterDuffColorFilter) getColorFilter();
            if (existing.getColor() == color && existing.getMode() == mode) {
                return;
            }
        }
        setColorFilter(new PorterDuffColorFilter(color, mode));
    }

    public void setTint(int tintColor) {
        setTintList(ColorStateList.valueOf(tintColor));
    }

    public void setTintList(ColorStateList tint) {
    }

    public void setTintMode(PorterDuff.Mode tintMode) {
        if (!this.mSetTintModeInvoked) {
            this.mSetTintModeInvoked = true;
            BlendMode mode = tintMode != null ? BlendMode.fromValue(tintMode.nativeInt) : null;
            setTintBlendMode(mode != null ? mode : DEFAULT_BLEND_MODE);
            this.mSetTintModeInvoked = false;
        }
    }

    public void setTintBlendMode(BlendMode blendMode) {
        if (!this.mSetBlendModeInvoked) {
            this.mSetBlendModeInvoked = true;
            PorterDuff.Mode mode = BlendMode.blendModeToPorterDuffMode(blendMode);
            setTintMode(mode != null ? mode : DEFAULT_TINT_MODE);
            this.mSetBlendModeInvoked = false;
        }
    }

    public ColorFilter getColorFilter() {
        return null;
    }

    public void clearColorFilter() {
        setColorFilter(null);
    }

    public void setHotspot(float x, float y) {
    }

    public void setHotspotBounds(int left, int top, int right, int bottom) {
    }

    public void getHotspotBounds(Rect outRect) {
        outRect.set(getBounds());
    }

    public boolean isProjected() {
        return false;
    }

    public boolean isStateful() {
        return false;
    }

    public boolean hasFocusStateSpecified() {
        return false;
    }

    public boolean setState(int[] stateSet) {
        if (Arrays.equals(this.mStateSet, stateSet)) {
            return false;
        }
        this.mStateSet = stateSet;
        return onStateChange(stateSet);
    }

    public int[] getState() {
        return this.mStateSet;
    }

    public void jumpToCurrentState() {
    }

    public Drawable getCurrent() {
        return this;
    }

    public final boolean setLevel(int level) {
        if (this.mLevel == level) {
            return false;
        }
        this.mLevel = level;
        return onLevelChange(level);
    }

    public final int getLevel() {
        return this.mLevel;
    }

    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = this.mVisible != visible;
        if (changed) {
            this.mVisible = visible;
            invalidateSelf();
        }
        return changed;
    }

    public final boolean isVisible() {
        return this.mVisible;
    }

    public void setAutoMirrored(boolean mirrored) {
    }

    public boolean isAutoMirrored() {
        return false;
    }

    public void applyTheme(Resources.Theme t) {
    }

    public boolean canApplyTheme() {
        return false;
    }

    public static int resolveOpacity(int op1, int op2) {
        if (op1 == op2) {
            return op1;
        }
        if (op1 == 0 || op2 == 0) {
            return 0;
        }
        if (op1 == -3 || op2 == -3) {
            return -3;
        }
        if (op1 == -2 || op2 == -2) {
            return -2;
        }
        return -1;
    }

    public Region getTransparentRegion() {
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean onStateChange(int[] state) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean onLevelChange(int level) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void onBoundsChange(Rect bounds) {
    }

    public int getIntrinsicWidth() {
        return -1;
    }

    public int getIntrinsicHeight() {
        return -1;
    }

    public int getMinimumWidth() {
        int intrinsicWidth = getIntrinsicWidth();
        if (intrinsicWidth > 0) {
            return intrinsicWidth;
        }
        return 0;
    }

    public int getMinimumHeight() {
        int intrinsicHeight = getIntrinsicHeight();
        if (intrinsicHeight > 0) {
            return intrinsicHeight;
        }
        return 0;
    }

    public boolean getPadding(Rect padding) {
        padding.set(0, 0, 0, 0);
        return false;
    }

    public Insets getOpticalInsets() {
        return Insets.NONE;
    }

    public void getOutline(Outline outline) {
        outline.setRect(getBounds());
        outline.setAlpha(0.0f);
    }

    public Drawable mutate() {
        return this;
    }

    public void clearMutated() {
    }

    public static Drawable createFromStream(InputStream is, String srcName) {
        Trace.traceBegin(8192, srcName != null ? srcName : "Unknown drawable");
        try {
            return createFromResourceStream(null, null, is, srcName);
        } finally {
            Trace.traceEnd(8192);
        }
    }

    public static Drawable createFromResourceStream(Resources res, TypedValue value, InputStream is, String srcName) {
        Trace.traceBegin(8192, srcName != null ? srcName : "Unknown drawable");
        try {
            return createFromResourceStream(res, value, is, srcName, null);
        } finally {
            Trace.traceEnd(8192);
        }
    }

    public static Drawable createFromResourceStream(Resources res, TypedValue value, InputStream is, String srcName, BitmapFactory.Options opts) {
        if (is == null) {
            return null;
        }
        if (opts == null) {
            return getBitmapDrawable(res, value, is);
        }
        Rect pad = new Rect();
        opts.inScreenDensity = resolveDensity(res, 0);
        Bitmap bm = BitmapFactory.decodeResourceStream(res, value, is, pad, opts);
        if (bm == null) {
            return null;
        }
        byte[] np = bm.getNinePatchChunk();
        if (np == null || !NinePatch.isNinePatchChunk(np)) {
            np = null;
            pad = null;
        }
        Rect opticalInsets = new Rect();
        bm.getOpticalInsets(opticalInsets);
        return drawableFromBitmap(res, bm, np, pad, opticalInsets, srcName);
    }

    private static Drawable getBitmapDrawable(Resources res, TypedValue value, InputStream is) {
        ImageDecoder.Source source;
        if (value != null) {
            int density = 0;
            try {
                if (value.density == 0) {
                    density = 160;
                } else if (value.density != 65535) {
                    density = value.density;
                }
                source = ImageDecoder.createSource(res, is, density);
            } catch (IOException e) {
                Log.e("Drawable", "Unable to decode stream: " + e);
                return null;
            }
        } else {
            source = ImageDecoder.createSource(res, is);
        }
        return ImageDecoder.decodeDrawable(source, $$Lambda$Drawable$bbJz2VgQAwkXlE27mR8nPMYacEw.INSTANCE);
    }

    static /* synthetic */ void lambda$getBitmapDrawable$1(ImageDecoder decoder, ImageDecoder.ImageInfo info, ImageDecoder.Source src) {
        decoder.setAllocator(1);
        decoder.setOnPartialImageListener($$Lambda$Drawable$KZt6g0IxKV2yrq1V3HrWrb1kXg.INSTANCE);
    }

    static /* synthetic */ boolean lambda$getBitmapDrawable$0(ImageDecoder.DecodeException e) {
        return e.getError() == 2;
    }

    public static Drawable createFromXml(Resources r, XmlPullParser parser) throws XmlPullParserException, IOException {
        return createFromXml(r, parser, null);
    }

    public static Drawable createFromXml(Resources r, XmlPullParser parser, Resources.Theme theme) throws XmlPullParserException, IOException {
        return createFromXmlForDensity(r, parser, 0, theme);
    }

    public static Drawable createFromXmlForDensity(Resources r, XmlPullParser parser, int density, Resources.Theme theme) throws XmlPullParserException, IOException {
        int type;
        AttributeSet attrs = Xml.asAttributeSet(parser);
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type == 2) {
            Drawable drawable = createFromXmlInnerForDensity(r, parser, attrs, density, theme);
            if (drawable != null) {
                return drawable;
            }
            throw new RuntimeException("Unknown initial tag: " + parser.getName());
        }
        throw new XmlPullParserException("No start tag found");
    }

    public static Drawable createFromXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
        return createFromXmlInner(r, parser, attrs, null);
    }

    public static Drawable createFromXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        return createFromXmlInnerForDensity(r, parser, attrs, 0, theme);
    }

    static Drawable createFromXmlInnerForDensity(Resources r, XmlPullParser parser, AttributeSet attrs, int density, Resources.Theme theme) throws XmlPullParserException, IOException {
        return r.getDrawableInflater().inflateFromXmlForDensity(parser.getName(), parser, attrs, density, theme);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001b, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0020, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0021, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0024, code lost:
        throw r5;
     */
    public static Drawable createFromPath(String pathName) {
        if (pathName == null) {
            return null;
        }
        Trace.traceBegin(8192, pathName);
        try {
            FileInputStream stream = new FileInputStream(pathName);
            Drawable bitmapDrawable = getBitmapDrawable(null, null, stream);
            stream.close();
            return bitmapDrawable;
        } catch (IOException e) {
            return null;
        } finally {
            Trace.traceEnd(8192);
        }
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
        inflate(r, parser, attrs, null);
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.Drawable);
        this.mVisible = a.getBoolean(0, this.mVisible);
        a.recycle();
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void inflateWithAttributes(Resources r, XmlPullParser parser, TypedArray attrs, int visibleAttr) throws XmlPullParserException, IOException {
        this.mVisible = attrs.getBoolean(visibleAttr, this.mVisible);
    }

    /* access modifiers changed from: package-private */
    public final void setSrcDensityOverride(int density) {
        this.mSrcDensityOverride = density;
    }

    public static abstract class ConstantState {
        public abstract int getChangingConfigurations();

        public abstract Drawable newDrawable();

        public Drawable newDrawable(Resources res) {
            return newDrawable();
        }

        public Drawable newDrawable(Resources res, Resources.Theme theme) {
            return newDrawable(res);
        }

        public boolean canApplyTheme() {
            return false;
        }
    }

    public ConstantState getConstantState() {
        return null;
    }

    private static Drawable drawableFromBitmap(Resources res, Bitmap bm, byte[] np, Rect pad, Rect layoutBounds, String srcName) {
        if (np != null) {
            return new NinePatchDrawable(res, bm, np, pad, layoutBounds, srcName);
        }
        return new BitmapDrawable(res, bm);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public PorterDuffColorFilter updateTintFilter(PorterDuffColorFilter tintFilter, ColorStateList tint, PorterDuff.Mode tintMode) {
        if (tint == null || tintMode == null) {
            return null;
        }
        int color = tint.getColorForState(getState(), 0);
        if (tintFilter != null && tintFilter.getColor() == color && tintFilter.getMode() == tintMode) {
            return tintFilter;
        }
        return new PorterDuffColorFilter(color, tintMode);
    }

    /* access modifiers changed from: package-private */
    public BlendModeColorFilter updateBlendModeFilter(BlendModeColorFilter blendFilter, ColorStateList tint, BlendMode blendMode) {
        if (tint == null || blendMode == null) {
            return null;
        }
        int color = tint.getColorForState(getState(), 0);
        if (blendFilter != null && blendFilter.getColor() == color && blendFilter.getMode() == blendMode) {
            return blendFilter;
        }
        return new BlendModeColorFilter(color, blendMode);
    }

    protected static TypedArray obtainAttributes(Resources res, Resources.Theme theme, AttributeSet set, int[] attrs) {
        if (theme == null) {
            return res.obtainAttributes(set, attrs);
        }
        return theme.obtainStyledAttributes(set, attrs, 0, 0);
    }

    static float scaleFromDensity(float pixels, int sourceDensity, int targetDensity) {
        return (((float) targetDensity) * pixels) / ((float) sourceDensity);
    }

    static int scaleFromDensity(int pixels, int sourceDensity, int targetDensity, boolean isSize) {
        if (pixels == 0 || sourceDensity == targetDensity) {
            return pixels;
        }
        float result = ((float) (pixels * targetDensity)) / ((float) sourceDensity);
        if (!isSize) {
            return (int) result;
        }
        int rounded = Math.round(result);
        if (rounded != 0) {
            return rounded;
        }
        if (pixels > 0) {
            return 1;
        }
        return -1;
    }

    static int resolveDensity(Resources r, int parentDensity) {
        int densityDpi = r == null ? parentDensity : r.getDisplayMetrics().densityDpi;
        if (densityDpi == 0) {
            return 160;
        }
        return densityDpi;
    }

    static void rethrowAsRuntimeException(Exception cause) throws RuntimeException {
        RuntimeException e = new RuntimeException(cause);
        e.setStackTrace(new StackTraceElement[0]);
        throw e;
    }

    @UnsupportedAppUsage
    public static PorterDuff.Mode parseTintMode(int value, PorterDuff.Mode defaultMode) {
        if (value == 3) {
            return PorterDuff.Mode.SRC_OVER;
        }
        if (value == 5) {
            return PorterDuff.Mode.SRC_IN;
        }
        if (value == 9) {
            return PorterDuff.Mode.SRC_ATOP;
        }
        switch (value) {
            case 14:
                return PorterDuff.Mode.MULTIPLY;
            case 15:
                return PorterDuff.Mode.SCREEN;
            case 16:
                return PorterDuff.Mode.ADD;
            default:
                return defaultMode;
        }
    }

    @UnsupportedAppUsage
    public static BlendMode parseBlendMode(int value, BlendMode defaultMode) {
        if (value == 3) {
            return BlendMode.SRC_OVER;
        }
        if (value == 5) {
            return BlendMode.SRC_IN;
        }
        if (value == 9) {
            return BlendMode.SRC_ATOP;
        }
        switch (value) {
            case 14:
                return BlendMode.MODULATE;
            case 15:
                return BlendMode.SCREEN;
            case 16:
                return BlendMode.PLUS;
            default:
                return defaultMode;
        }
    }
}
