package android.graphics.drawable;

import android.annotation.UnsupportedAppUsage;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ImageDecoder;
import android.graphics.Insets;
import android.graphics.NinePatch;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import com.android.internal.R;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class NinePatchDrawable extends Drawable {
    private static final boolean DEFAULT_DITHER = false;
    private int mBitmapHeight;
    private int mBitmapWidth;
    private BlendModeColorFilter mBlendModeFilter;
    private boolean mMutated;
    @UnsupportedAppUsage
    private NinePatchState mNinePatchState;
    private Insets mOpticalInsets;
    private Rect mOutlineInsets;
    private float mOutlineRadius;
    private Rect mPadding;
    private Paint mPaint;
    private int mTargetDensity;
    private Rect mTempRect;

    NinePatchDrawable() {
        this.mOpticalInsets = Insets.NONE;
        this.mTargetDensity = 160;
        this.mBitmapWidth = -1;
        this.mBitmapHeight = -1;
        this.mNinePatchState = new NinePatchState();
    }

    @Deprecated
    public NinePatchDrawable(Bitmap bitmap, byte[] chunk, Rect padding, String srcName) {
        this(new NinePatchState(new NinePatch(bitmap, chunk, srcName), padding), (Resources) null);
    }

    public NinePatchDrawable(Resources res, Bitmap bitmap, byte[] chunk, Rect padding, String srcName) {
        this(new NinePatchState(new NinePatch(bitmap, chunk, srcName), padding), res);
    }

    public NinePatchDrawable(Resources res, Bitmap bitmap, byte[] chunk, Rect padding, Rect opticalInsets, String srcName) {
        this(new NinePatchState(new NinePatch(bitmap, chunk, srcName), padding, opticalInsets), res);
    }

    @Deprecated
    public NinePatchDrawable(NinePatch patch) {
        this(new NinePatchState(patch, new Rect()), (Resources) null);
    }

    public NinePatchDrawable(Resources res, NinePatch patch) {
        this(new NinePatchState(patch, new Rect()), res);
    }

    public void setTargetDensity(Canvas canvas) {
        setTargetDensity(canvas.getDensity());
    }

    public void setTargetDensity(DisplayMetrics metrics) {
        setTargetDensity(metrics.densityDpi);
    }

    public void setTargetDensity(int density) {
        if (density == 0) {
            density = 160;
        }
        if (this.mTargetDensity != density) {
            this.mTargetDensity = density;
            computeBitmapSize();
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        boolean clearColorFilter;
        int restoreAlpha;
        NinePatchState state = this.mNinePatchState;
        Rect bounds = getBounds();
        int restoreToCount = -1;
        if (this.mBlendModeFilter == null || getPaint().getColorFilter() != null) {
            clearColorFilter = false;
        } else {
            this.mPaint.setColorFilter(this.mBlendModeFilter);
            clearColorFilter = true;
        }
        if (state.mBaseAlpha != 1.0f) {
            restoreAlpha = getPaint().getAlpha();
            this.mPaint.setAlpha((int) ((((float) restoreAlpha) * state.mBaseAlpha) + 0.5f));
        } else {
            restoreAlpha = -1;
        }
        if (canvas.getDensity() == 0 && state.mNinePatch.getDensity() != 0) {
            restoreToCount = -1 >= 0 ? -1 : canvas.save();
            float scale = ((float) this.mTargetDensity) / ((float) state.mNinePatch.getDensity());
            canvas.scale(scale, scale, (float) bounds.left, (float) bounds.top);
            if (this.mTempRect == null) {
                this.mTempRect = new Rect();
            }
            Rect scaledBounds = this.mTempRect;
            scaledBounds.left = bounds.left;
            scaledBounds.top = bounds.top;
            scaledBounds.right = bounds.left + Math.round(((float) bounds.width()) / scale);
            scaledBounds.bottom = bounds.top + Math.round(((float) bounds.height()) / scale);
            bounds = scaledBounds;
        }
        if (needsMirroring()) {
            restoreToCount = restoreToCount >= 0 ? restoreToCount : canvas.save();
            canvas.scale(-1.0f, 1.0f, ((float) (bounds.left + bounds.right)) / 2.0f, ((float) (bounds.top + bounds.bottom)) / 2.0f);
        }
        state.mNinePatch.draw(canvas, bounds, this.mPaint);
        if (restoreToCount >= 0) {
            canvas.restoreToCount(restoreToCount);
        }
        if (clearColorFilter) {
            this.mPaint.setColorFilter(null);
        }
        if (restoreAlpha >= 0) {
            this.mPaint.setAlpha(restoreAlpha);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mNinePatchState.getChangingConfigurations();
    }

    @Override // android.graphics.drawable.Drawable
    public boolean getPadding(Rect padding) {
        Rect rect = this.mPadding;
        if (rect == null) {
            return super.getPadding(padding);
        }
        padding.set(rect);
        return (((padding.left | padding.top) | padding.right) | padding.bottom) != 0;
    }

    @Override // android.graphics.drawable.Drawable
    public void getOutline(Outline outline) {
        NinePatch.InsetStruct insets;
        Rect bounds = getBounds();
        if (!bounds.isEmpty()) {
            NinePatchState ninePatchState = this.mNinePatchState;
            if (ninePatchState == null || this.mOutlineInsets == null || (insets = ninePatchState.mNinePatch.getBitmap().getNinePatchInsets()) == null) {
                super.getOutline(outline);
                return;
            }
            outline.setRoundRect(bounds.left + this.mOutlineInsets.left, bounds.top + this.mOutlineInsets.top, bounds.right - this.mOutlineInsets.right, bounds.bottom - this.mOutlineInsets.bottom, this.mOutlineRadius);
            outline.setAlpha(insets.outlineAlpha * (((float) getAlpha()) / 255.0f));
        }
    }

    @Override // android.graphics.drawable.Drawable
    public Insets getOpticalInsets() {
        Insets opticalInsets = this.mOpticalInsets;
        if (needsMirroring()) {
            return Insets.of(opticalInsets.right, opticalInsets.top, opticalInsets.left, opticalInsets.bottom);
        }
        return opticalInsets;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        if (this.mPaint != null || alpha != 255) {
            getPaint().setAlpha(alpha);
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        if (this.mPaint == null) {
            return 255;
        }
        return getPaint().getAlpha();
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        if (this.mPaint != null || colorFilter != null) {
            getPaint().setColorFilter(colorFilter);
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintList(ColorStateList tint) {
        NinePatchState ninePatchState = this.mNinePatchState;
        ninePatchState.mTint = tint;
        this.mBlendModeFilter = updateBlendModeFilter(this.mBlendModeFilter, tint, ninePatchState.mBlendMode);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintBlendMode(BlendMode blendMode) {
        NinePatchState ninePatchState = this.mNinePatchState;
        ninePatchState.mBlendMode = blendMode;
        this.mBlendModeFilter = updateBlendModeFilter(this.mBlendModeFilter, ninePatchState.mTint, blendMode);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void setDither(boolean dither) {
        if (this.mPaint != null || dither) {
            getPaint().setDither(dither);
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setAutoMirrored(boolean mirrored) {
        this.mNinePatchState.mAutoMirrored = mirrored;
    }

    private boolean needsMirroring() {
        return isAutoMirrored() && getLayoutDirection() == 1;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isAutoMirrored() {
        return this.mNinePatchState.mAutoMirrored;
    }

    @Override // android.graphics.drawable.Drawable
    public void setFilterBitmap(boolean filter) {
        getPaint().setFilterBitmap(filter);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isFilterBitmap() {
        return this.mPaint != null && getPaint().isFilterBitmap();
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.NinePatchDrawable);
        updateStateFromTypedArray(a);
        a.recycle();
        updateLocalState(r);
    }

    private void updateStateFromTypedArray(TypedArray a) throws XmlPullParserException {
        Resources r = a.getResources();
        NinePatchState state = this.mNinePatchState;
        state.mChangingConfigurations |= a.getChangingConfigurations();
        state.mThemeAttrs = a.extractThemeAttrs();
        state.mDither = a.getBoolean(1, state.mDither);
        int srcResId = a.getResourceId(0, 0);
        if (srcResId != 0) {
            Rect padding = new Rect();
            Rect opticalInsets = new Rect();
            Bitmap bitmap = HwThemeManager.getThemeBitmap(r, srcResId, padding);
            if (bitmap == null || bitmap.getNinePatchChunk() == null) {
                try {
                    TypedValue value = new TypedValue();
                    InputStream is = r.openRawResource(srcResId, value);
                    int density = 0;
                    if (value.density == 0) {
                        density = 160;
                    } else if (value.density != 65535) {
                        density = value.density;
                    }
                    bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(r, is, density), new ImageDecoder.OnHeaderDecodedListener() {
                        /* class android.graphics.drawable.$$Lambda$NinePatchDrawable$yQvfm7FAkslD5wdGFysjgwt8cLE */

                        @Override // android.graphics.ImageDecoder.OnHeaderDecodedListener
                        public final void onHeaderDecoded(ImageDecoder imageDecoder, ImageDecoder.ImageInfo imageInfo, ImageDecoder.Source source) {
                            NinePatchDrawable.lambda$updateStateFromTypedArray$0(Rect.this, imageDecoder, imageInfo, source);
                        }
                    });
                    is.close();
                } catch (IOException e) {
                }
            }
            if (bitmap == null) {
                throw new XmlPullParserException(a.getPositionDescription() + ": <nine-patch> requires a valid src attribute");
            } else if (bitmap.getNinePatchChunk() != null) {
                bitmap.getOpticalInsets(opticalInsets);
                state.mNinePatch = new NinePatch(bitmap, bitmap.getNinePatchChunk());
                state.mPadding = padding;
                state.mOpticalInsets = Insets.of(opticalInsets);
            } else {
                throw new XmlPullParserException(a.getPositionDescription() + ": <nine-patch> requires a valid 9-patch source image");
            }
        }
        state.mAutoMirrored = a.getBoolean(4, state.mAutoMirrored);
        state.mBaseAlpha = a.getFloat(3, state.mBaseAlpha);
        int tintMode = a.getInt(5, -1);
        if (tintMode != -1) {
            state.mBlendMode = Drawable.parseBlendMode(tintMode, BlendMode.SRC_IN);
        }
        ColorStateList tint = a.getColorStateList(2);
        if (tint != null) {
            state.mTint = tint;
        }
    }

    static /* synthetic */ void lambda$updateStateFromTypedArray$0(Rect padding, ImageDecoder decoder, ImageDecoder.ImageInfo info, ImageDecoder.Source src) {
        decoder.setOutPaddingRect(padding);
        decoder.setAllocator(1);
    }

    @Override // android.graphics.drawable.Drawable
    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        NinePatchState state = this.mNinePatchState;
        if (state != null) {
            if (state.mThemeAttrs != null) {
                TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.NinePatchDrawable);
                try {
                    updateStateFromTypedArray(a);
                } catch (XmlPullParserException e) {
                    rethrowAsRuntimeException(e);
                } catch (Throwable th) {
                    a.recycle();
                    throw th;
                }
                a.recycle();
            }
            if (state.mTint != null && state.mTint.canApplyTheme()) {
                state.mTint = state.mTint.obtainForTheme(t);
            }
            updateLocalState(t.getResources());
        }
    }

    @Override // android.graphics.drawable.Drawable
    public boolean canApplyTheme() {
        NinePatchState ninePatchState = this.mNinePatchState;
        return ninePatchState != null && ninePatchState.canApplyTheme();
    }

    public Paint getPaint() {
        if (this.mPaint == null) {
            this.mPaint = new Paint();
            this.mPaint.setDither(false);
        }
        return this.mPaint;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mBitmapWidth;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mBitmapHeight;
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        Paint paint;
        return (this.mNinePatchState.mNinePatch.hasAlpha() || ((paint = this.mPaint) != null && paint.getAlpha() < 255)) ? -3 : -1;
    }

    @Override // android.graphics.drawable.Drawable
    public Region getTransparentRegion() {
        return this.mNinePatchState.mNinePatch.getTransparentRegion(getBounds());
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable.ConstantState getConstantState() {
        this.mNinePatchState.mChangingConfigurations = getChangingConfigurations();
        return this.mNinePatchState;
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mNinePatchState = new NinePatchState(this.mNinePatchState);
            this.mMutated = true;
        }
        return this;
    }

    @Override // android.graphics.drawable.Drawable
    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public boolean onStateChange(int[] stateSet) {
        NinePatchState state = this.mNinePatchState;
        if (state.mTint == null || state.mBlendMode == null) {
            return false;
        }
        this.mBlendModeFilter = updateBlendModeFilter(this.mBlendModeFilter, state.mTint, state.mBlendMode);
        return true;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isStateful() {
        NinePatchState s = this.mNinePatchState;
        return super.isStateful() || (s.mTint != null && s.mTint.isStateful());
    }

    @Override // android.graphics.drawable.Drawable
    public boolean hasFocusStateSpecified() {
        return this.mNinePatchState.mTint != null && this.mNinePatchState.mTint.hasFocusStateSpecified();
    }

    /* access modifiers changed from: package-private */
    public static final class NinePatchState extends Drawable.ConstantState {
        boolean mAutoMirrored;
        float mBaseAlpha;
        BlendMode mBlendMode;
        int mChangingConfigurations;
        boolean mDither;
        @UnsupportedAppUsage
        NinePatch mNinePatch;
        Insets mOpticalInsets;
        Rect mPadding;
        int[] mThemeAttrs;
        ColorStateList mTint;

        NinePatchState() {
            this.mNinePatch = null;
            this.mTint = null;
            this.mBlendMode = Drawable.DEFAULT_BLEND_MODE;
            this.mPadding = null;
            this.mOpticalInsets = Insets.NONE;
            this.mBaseAlpha = 1.0f;
            this.mDither = false;
            this.mAutoMirrored = false;
        }

        NinePatchState(NinePatch ninePatch, Rect padding) {
            this(ninePatch, padding, null, false, false);
        }

        NinePatchState(NinePatch ninePatch, Rect padding, Rect opticalInsets) {
            this(ninePatch, padding, opticalInsets, false, false);
        }

        NinePatchState(NinePatch ninePatch, Rect padding, Rect opticalInsets, boolean dither, boolean autoMirror) {
            this.mNinePatch = null;
            this.mTint = null;
            this.mBlendMode = Drawable.DEFAULT_BLEND_MODE;
            this.mPadding = null;
            this.mOpticalInsets = Insets.NONE;
            this.mBaseAlpha = 1.0f;
            this.mDither = false;
            this.mAutoMirrored = false;
            this.mNinePatch = ninePatch;
            this.mPadding = padding;
            this.mOpticalInsets = Insets.of(opticalInsets);
            this.mDither = dither;
            this.mAutoMirrored = autoMirror;
        }

        NinePatchState(NinePatchState orig) {
            this.mNinePatch = null;
            this.mTint = null;
            this.mBlendMode = Drawable.DEFAULT_BLEND_MODE;
            this.mPadding = null;
            this.mOpticalInsets = Insets.NONE;
            this.mBaseAlpha = 1.0f;
            this.mDither = false;
            this.mAutoMirrored = false;
            this.mChangingConfigurations = orig.mChangingConfigurations;
            this.mNinePatch = orig.mNinePatch;
            this.mTint = orig.mTint;
            this.mBlendMode = orig.mBlendMode;
            this.mPadding = orig.mPadding;
            this.mOpticalInsets = orig.mOpticalInsets;
            this.mBaseAlpha = orig.mBaseAlpha;
            this.mDither = orig.mDither;
            this.mAutoMirrored = orig.mAutoMirrored;
            this.mThemeAttrs = orig.mThemeAttrs;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public boolean canApplyTheme() {
            ColorStateList colorStateList;
            return this.mThemeAttrs != null || ((colorStateList = this.mTint) != null && colorStateList.canApplyTheme()) || super.canApplyTheme();
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new NinePatchDrawable(this, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res) {
            return new NinePatchDrawable(this, res);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public int getChangingConfigurations() {
            int i = this.mChangingConfigurations;
            ColorStateList colorStateList = this.mTint;
            return i | (colorStateList != null ? colorStateList.getChangingConfigurations() : 0);
        }
    }

    private void computeBitmapSize() {
        NinePatch ninePatch = this.mNinePatchState.mNinePatch;
        if (ninePatch != null) {
            int targetDensity = this.mTargetDensity;
            int sourceDensity = ninePatch.getDensity() == 0 ? targetDensity : ninePatch.getDensity();
            Insets sourceOpticalInsets = this.mNinePatchState.mOpticalInsets;
            if (sourceOpticalInsets != Insets.NONE) {
                this.mOpticalInsets = Insets.of(Drawable.scaleFromDensity(sourceOpticalInsets.left, sourceDensity, targetDensity, true), Drawable.scaleFromDensity(sourceOpticalInsets.top, sourceDensity, targetDensity, true), Drawable.scaleFromDensity(sourceOpticalInsets.right, sourceDensity, targetDensity, true), Drawable.scaleFromDensity(sourceOpticalInsets.bottom, sourceDensity, targetDensity, true));
            } else {
                this.mOpticalInsets = Insets.NONE;
            }
            Rect sourcePadding = this.mNinePatchState.mPadding;
            if (sourcePadding != null) {
                if (this.mPadding == null) {
                    this.mPadding = new Rect();
                }
                this.mPadding.left = Drawable.scaleFromDensity(sourcePadding.left, sourceDensity, targetDensity, true);
                this.mPadding.top = Drawable.scaleFromDensity(sourcePadding.top, sourceDensity, targetDensity, true);
                this.mPadding.right = Drawable.scaleFromDensity(sourcePadding.right, sourceDensity, targetDensity, true);
                this.mPadding.bottom = Drawable.scaleFromDensity(sourcePadding.bottom, sourceDensity, targetDensity, true);
            } else {
                this.mPadding = null;
            }
            this.mBitmapHeight = Drawable.scaleFromDensity(ninePatch.getHeight(), sourceDensity, targetDensity, true);
            this.mBitmapWidth = Drawable.scaleFromDensity(ninePatch.getWidth(), sourceDensity, targetDensity, true);
            NinePatch.InsetStruct insets = ninePatch.getBitmap().getNinePatchInsets();
            if (insets != null) {
                Rect outlineRect = insets.outlineRect;
                this.mOutlineInsets = NinePatch.InsetStruct.scaleInsets(outlineRect.left, outlineRect.top, outlineRect.right, outlineRect.bottom, ((float) targetDensity) / ((float) sourceDensity));
                this.mOutlineRadius = Drawable.scaleFromDensity(insets.outlineRadius, sourceDensity, targetDensity);
                return;
            }
            this.mOutlineInsets = null;
        }
    }

    private NinePatchDrawable(NinePatchState state, Resources res) {
        this.mOpticalInsets = Insets.NONE;
        this.mTargetDensity = 160;
        this.mBitmapWidth = -1;
        this.mBitmapHeight = -1;
        this.mNinePatchState = state;
        updateLocalState(res);
    }

    private void updateLocalState(Resources res) {
        NinePatchState state = this.mNinePatchState;
        if (state.mDither) {
            setDither(state.mDither);
        }
        if (res != null || state.mNinePatch == null) {
            this.mTargetDensity = Drawable.resolveDensity(res, this.mTargetDensity);
        } else {
            this.mTargetDensity = state.mNinePatch.getDensity();
        }
        this.mBlendModeFilter = updateBlendModeFilter(this.mBlendModeFilter, state.mTint, state.mBlendMode);
        computeBitmapSize();
    }
}
