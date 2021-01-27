package android.graphics.drawable;

import android.annotation.UnsupportedAppUsage;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ImageDecoder;
import android.graphics.Insets;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import com.android.internal.R;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class BitmapDrawable extends Drawable {
    private static final int DEFAULT_PAINT_FLAGS = 6;
    private static final int TILE_MODE_CLAMP = 0;
    private static final int TILE_MODE_DISABLED = -1;
    private static final int TILE_MODE_MIRROR = 2;
    private static final int TILE_MODE_REPEAT = 1;
    private static final int TILE_MODE_UNDEFINED = -2;
    private int mBitmapHeight;
    @UnsupportedAppUsage
    private BitmapState mBitmapState;
    private int mBitmapWidth;
    private BlendModeColorFilter mBlendModeFilter;
    private final Rect mDstRect;
    private boolean mDstRectAndInsetsDirty;
    private Matrix mMirrorMatrix;
    private boolean mMutated;
    private Insets mOpticalInsets;
    @UnsupportedAppUsage
    private int mTargetDensity;

    @Deprecated
    public BitmapDrawable() {
        this.mDstRect = new Rect();
        this.mTargetDensity = 160;
        this.mDstRectAndInsetsDirty = true;
        this.mOpticalInsets = Insets.NONE;
        init(new BitmapState((Bitmap) null), null);
    }

    @Deprecated
    public BitmapDrawable(Resources res) {
        this.mDstRect = new Rect();
        this.mTargetDensity = 160;
        this.mDstRectAndInsetsDirty = true;
        this.mOpticalInsets = Insets.NONE;
        init(new BitmapState((Bitmap) null), res);
    }

    @Deprecated
    public BitmapDrawable(Bitmap bitmap) {
        this.mDstRect = new Rect();
        this.mTargetDensity = 160;
        this.mDstRectAndInsetsDirty = true;
        this.mOpticalInsets = Insets.NONE;
        init(new BitmapState(bitmap), null);
    }

    public BitmapDrawable(Resources res, Bitmap bitmap) {
        this.mDstRect = new Rect();
        this.mTargetDensity = 160;
        this.mDstRectAndInsetsDirty = true;
        this.mOpticalInsets = Insets.NONE;
        init(new BitmapState(bitmap), res);
    }

    @Deprecated
    public BitmapDrawable(String filepath) {
        this((Resources) null, filepath);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0051, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0052, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0055, code lost:
        throw r5;
     */
    public BitmapDrawable(Resources res, String filepath) {
        StringBuilder sb;
        this.mDstRect = new Rect();
        this.mTargetDensity = 160;
        this.mDstRectAndInsetsDirty = true;
        this.mOpticalInsets = Insets.NONE;
        try {
            FileInputStream stream = new FileInputStream(filepath);
            Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(res, stream), $$Lambda$BitmapDrawable$23eAuhdkgEf5MIRJCrMNbn4Pyg.INSTANCE);
            $closeResource(null, stream);
            init(new BitmapState(bitmap), res);
            if (this.mBitmapState.mBitmap == null) {
                sb = new StringBuilder();
                sb.append("BitmapDrawable cannot decode ");
                sb.append(filepath);
                Log.w("BitmapDrawable", sb.toString());
            }
        } catch (Exception e) {
            init(new BitmapState((Bitmap) null), res);
            if (this.mBitmapState.mBitmap == null) {
                sb = new StringBuilder();
            }
        } catch (Throwable th) {
            init(new BitmapState((Bitmap) null), res);
            if (this.mBitmapState.mBitmap == null) {
                Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + filepath);
            }
            throw th;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    @Deprecated
    public BitmapDrawable(InputStream is) {
        this((Resources) null, is);
    }

    public BitmapDrawable(Resources res, InputStream is) {
        StringBuilder sb;
        this.mDstRect = new Rect();
        this.mTargetDensity = 160;
        this.mDstRectAndInsetsDirty = true;
        this.mOpticalInsets = Insets.NONE;
        try {
            init(new BitmapState(ImageDecoder.decodeBitmap(ImageDecoder.createSource(res, is), $$Lambda$BitmapDrawable$T1BUUqQwU4Z6Ve8DJHFuQvYohkY.INSTANCE)), res);
            if (this.mBitmapState.mBitmap == null) {
                sb = new StringBuilder();
                sb.append("BitmapDrawable cannot decode ");
                sb.append(is);
                Log.w("BitmapDrawable", sb.toString());
            }
        } catch (Exception e) {
            init(new BitmapState((Bitmap) null), res);
            if (this.mBitmapState.mBitmap == null) {
                sb = new StringBuilder();
            }
        } catch (Throwable th) {
            init(new BitmapState((Bitmap) null), res);
            if (this.mBitmapState.mBitmap == null) {
                Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + is);
            }
            throw th;
        }
    }

    public final Paint getPaint() {
        return this.mBitmapState.mPaint;
    }

    public final Bitmap getBitmap() {
        return this.mBitmapState.mBitmap;
    }

    private void computeBitmapSize() {
        Bitmap bitmap = this.mBitmapState.mBitmap;
        if (bitmap != null) {
            this.mBitmapWidth = bitmap.getScaledWidth(this.mTargetDensity);
            this.mBitmapHeight = bitmap.getScaledHeight(this.mTargetDensity);
            return;
        }
        this.mBitmapHeight = -1;
        this.mBitmapWidth = -1;
    }

    @UnsupportedAppUsage
    public void setBitmap(Bitmap bitmap) {
        if (this.mBitmapState.mBitmap != bitmap) {
            this.mBitmapState.mBitmap = bitmap;
            computeBitmapSize();
            invalidateSelf();
        }
    }

    public void setTargetDensity(Canvas canvas) {
        setTargetDensity(canvas.getDensity());
    }

    public void setTargetDensity(DisplayMetrics metrics) {
        setTargetDensity(metrics.densityDpi);
    }

    public void setTargetDensity(int density) {
        if (this.mTargetDensity != density) {
            this.mTargetDensity = density == 0 ? 160 : density;
            if (this.mBitmapState.mBitmap != null) {
                computeBitmapSize();
            }
            invalidateSelf();
        }
    }

    public int getGravity() {
        return this.mBitmapState.mGravity;
    }

    public void setGravity(int gravity) {
        if (this.mBitmapState.mGravity != gravity) {
            this.mBitmapState.mGravity = gravity;
            this.mDstRectAndInsetsDirty = true;
            invalidateSelf();
        }
    }

    public void setMipMap(boolean mipMap) {
        if (this.mBitmapState.mBitmap != null) {
            this.mBitmapState.mBitmap.setHasMipMap(mipMap);
            invalidateSelf();
        }
    }

    public boolean hasMipMap() {
        return this.mBitmapState.mBitmap != null && this.mBitmapState.mBitmap.hasMipMap();
    }

    public void setAntiAlias(boolean aa) {
        this.mBitmapState.mPaint.setAntiAlias(aa);
        invalidateSelf();
    }

    public boolean hasAntiAlias() {
        return this.mBitmapState.mPaint.isAntiAlias();
    }

    @Override // android.graphics.drawable.Drawable
    public void setFilterBitmap(boolean filter) {
        this.mBitmapState.mPaint.setFilterBitmap(filter);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isFilterBitmap() {
        return this.mBitmapState.mPaint.isFilterBitmap();
    }

    @Override // android.graphics.drawable.Drawable
    public void setDither(boolean dither) {
        this.mBitmapState.mPaint.setDither(dither);
        invalidateSelf();
    }

    public Shader.TileMode getTileModeX() {
        return this.mBitmapState.mTileModeX;
    }

    public Shader.TileMode getTileModeY() {
        return this.mBitmapState.mTileModeY;
    }

    public void setTileModeX(Shader.TileMode mode) {
        setTileModeXY(mode, this.mBitmapState.mTileModeY);
    }

    public final void setTileModeY(Shader.TileMode mode) {
        setTileModeXY(this.mBitmapState.mTileModeX, mode);
    }

    public void setTileModeXY(Shader.TileMode xmode, Shader.TileMode ymode) {
        BitmapState state = this.mBitmapState;
        if (state.mTileModeX != xmode || state.mTileModeY != ymode) {
            state.mTileModeX = xmode;
            state.mTileModeY = ymode;
            state.mRebuildShader = true;
            this.mDstRectAndInsetsDirty = true;
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setAutoMirrored(boolean mirrored) {
        if (this.mBitmapState.mAutoMirrored != mirrored) {
            this.mBitmapState.mAutoMirrored = mirrored;
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public final boolean isAutoMirrored() {
        return this.mBitmapState.mAutoMirrored;
    }

    @Override // android.graphics.drawable.Drawable
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mBitmapState.getChangingConfigurations();
    }

    private boolean needMirroring() {
        return isAutoMirrored() && getLayoutDirection() == 1;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public void onBoundsChange(Rect bounds) {
        this.mDstRectAndInsetsDirty = true;
        Bitmap bitmap = this.mBitmapState.mBitmap;
        Shader shader = this.mBitmapState.mPaint.getShader();
        if (bitmap != null && shader != null) {
            updateShaderMatrix(bitmap, this.mBitmapState.mPaint, shader, needMirroring());
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        int restoreAlpha;
        boolean clearColorFilter;
        Bitmap bitmap = this.mBitmapState.mBitmap;
        if (bitmap != null) {
            BitmapState state = this.mBitmapState;
            Paint paint = state.mPaint;
            if (state.mRebuildShader) {
                Shader.TileMode tmx = state.mTileModeX;
                Shader.TileMode tmy = state.mTileModeY;
                if (tmx == null && tmy == null) {
                    paint.setShader(null);
                } else {
                    paint.setShader(new BitmapShader(bitmap, tmx == null ? Shader.TileMode.CLAMP : tmx, tmy == null ? Shader.TileMode.CLAMP : tmy));
                }
                state.mRebuildShader = false;
            }
            if (state.mBaseAlpha != 1.0f) {
                Paint p = getPaint();
                restoreAlpha = p.getAlpha();
                p.setAlpha((int) ((((float) restoreAlpha) * state.mBaseAlpha) + 0.5f));
            } else {
                restoreAlpha = -1;
            }
            if (this.mBlendModeFilter == null || paint.getColorFilter() != null) {
                clearColorFilter = false;
            } else {
                paint.setColorFilter(this.mBlendModeFilter);
                clearColorFilter = true;
            }
            updateDstRectAndInsetsIfDirty();
            Shader shader = paint.getShader();
            boolean needMirroring = needMirroring();
            if (shader == null) {
                if (needMirroring) {
                    canvas.save();
                    canvas.translate((float) (this.mDstRect.right - this.mDstRect.left), 0.0f);
                    canvas.scale(-1.0f, 1.0f);
                }
                try {
                    canvas.drawBitmap(bitmap, (Rect) null, this.mDstRect, paint);
                } catch (NullPointerException e) {
                    throw e;
                } catch (RuntimeException e2) {
                    Log.e("BitmapDrawable", "Canvas: trying to use a recycled bitmap");
                    e2.printStackTrace();
                }
                if (needMirroring) {
                    canvas.restore();
                }
            } else {
                updateShaderMatrix(bitmap, paint, shader, needMirroring);
                canvas.drawRect(this.mDstRect, paint);
            }
            if (clearColorFilter) {
                paint.setColorFilter(null);
            }
            if (restoreAlpha >= 0) {
                paint.setAlpha(restoreAlpha);
            }
        }
    }

    private void updateShaderMatrix(Bitmap bitmap, Paint paint, Shader shader, boolean needMirroring) {
        int sourceDensity = bitmap.getDensity();
        int targetDensity = this.mTargetDensity;
        boolean needScaling = (sourceDensity == 0 || sourceDensity == targetDensity) ? false : true;
        if (needScaling || needMirroring) {
            Matrix matrix = getOrCreateMirrorMatrix();
            matrix.reset();
            if (needMirroring) {
                matrix.setTranslate((float) (this.mDstRect.right - this.mDstRect.left), 0.0f);
                matrix.setScale(-1.0f, 1.0f);
            }
            if (needScaling) {
                float densityScale = ((float) targetDensity) / ((float) sourceDensity);
                matrix.postScale(densityScale, densityScale);
            }
            shader.setLocalMatrix(matrix);
        } else {
            this.mMirrorMatrix = null;
            shader.setLocalMatrix(Matrix.IDENTITY_MATRIX);
        }
        paint.setShader(shader);
    }

    private Matrix getOrCreateMirrorMatrix() {
        if (this.mMirrorMatrix == null) {
            this.mMirrorMatrix = new Matrix();
        }
        return this.mMirrorMatrix;
    }

    private void updateDstRectAndInsetsIfDirty() {
        if (this.mDstRectAndInsetsDirty) {
            if (this.mBitmapState.mTileModeX == null && this.mBitmapState.mTileModeY == null) {
                Rect bounds = getBounds();
                Gravity.apply(this.mBitmapState.mGravity, this.mBitmapWidth, this.mBitmapHeight, bounds, this.mDstRect, getLayoutDirection());
                this.mOpticalInsets = Insets.of(this.mDstRect.left - bounds.left, this.mDstRect.top - bounds.top, bounds.right - this.mDstRect.right, bounds.bottom - this.mDstRect.bottom);
            } else {
                copyBounds(this.mDstRect);
                this.mOpticalInsets = Insets.NONE;
            }
        }
        this.mDstRectAndInsetsDirty = false;
    }

    @Override // android.graphics.drawable.Drawable
    public Insets getOpticalInsets() {
        updateDstRectAndInsetsIfDirty();
        return this.mOpticalInsets;
    }

    @Override // android.graphics.drawable.Drawable
    public void getOutline(Outline outline) {
        updateDstRectAndInsetsIfDirty();
        outline.setRect(this.mDstRect);
        outline.setAlpha(this.mBitmapState.mBitmap != null && !this.mBitmapState.mBitmap.hasAlpha() ? ((float) getAlpha()) / 255.0f : 0.0f);
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        if (alpha != this.mBitmapState.mPaint.getAlpha()) {
            this.mBitmapState.mPaint.setAlpha(alpha);
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mBitmapState.mPaint.getAlpha();
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mBitmapState.mPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public ColorFilter getColorFilter() {
        return this.mBitmapState.mPaint.getColorFilter();
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintList(ColorStateList tint) {
        BitmapState state = this.mBitmapState;
        if (state.mTint != tint) {
            state.mTint = tint;
            this.mBlendModeFilter = updateBlendModeFilter(this.mBlendModeFilter, tint, this.mBitmapState.mBlendMode);
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintBlendMode(BlendMode blendMode) {
        BitmapState state = this.mBitmapState;
        if (state.mBlendMode != blendMode) {
            state.mBlendMode = blendMode;
            this.mBlendModeFilter = updateBlendModeFilter(this.mBlendModeFilter, this.mBitmapState.mTint, blendMode);
            invalidateSelf();
        }
    }

    @UnsupportedAppUsage
    public ColorStateList getTint() {
        return this.mBitmapState.mTint;
    }

    @UnsupportedAppUsage
    public PorterDuff.Mode getTintMode() {
        return BlendMode.blendModeToPorterDuffMode(this.mBitmapState.mBlendMode);
    }

    @Override // android.graphics.drawable.Drawable
    public void setXfermode(Xfermode xfermode) {
        this.mBitmapState.mPaint.setXfermode(xfermode);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mBitmapState = new BitmapState(this.mBitmapState);
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
        BitmapState state = this.mBitmapState;
        if (state.mTint == null || state.mBlendMode == null) {
            return false;
        }
        this.mBlendModeFilter = updateBlendModeFilter(this.mBlendModeFilter, state.mTint, state.mBlendMode);
        return true;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isStateful() {
        return (this.mBitmapState.mTint != null && this.mBitmapState.mTint.isStateful()) || super.isStateful();
    }

    @Override // android.graphics.drawable.Drawable
    public boolean hasFocusStateSpecified() {
        return this.mBitmapState.mTint != null && this.mBitmapState.mTint.hasFocusStateSpecified();
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.BitmapDrawable);
        updateStateFromTypedArray(a, this.mSrcDensityOverride);
        verifyRequiredAttributes(a);
        a.recycle();
        updateLocalState(r);
    }

    private void verifyRequiredAttributes(TypedArray a) throws XmlPullParserException {
        BitmapState state = this.mBitmapState;
        if (state.mBitmap != null) {
            return;
        }
        if (state.mThemeAttrs == null || state.mThemeAttrs[1] == 0) {
            throw new XmlPullParserException(a.getPositionDescription() + ": <bitmap> requires a valid 'src' attribute");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x007a, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007b, code lost:
        if (r7 != null) goto L_0x007d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x007d, code lost:
        $closeResource(r8, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0080, code lost:
        throw r9;
     */
    private void updateStateFromTypedArray(TypedArray a, int srcDensityOverride) throws XmlPullParserException {
        Resources r = a.getResources();
        BitmapState state = this.mBitmapState;
        state.mChangingConfigurations |= a.getChangingConfigurations();
        state.mThemeAttrs = a.extractThemeAttrs();
        state.mSrcDensityOverride = srcDensityOverride;
        state.mTargetDensity = Drawable.resolveDensity(r, 0);
        int srcResId = a.getResourceId(1, 0);
        if (srcResId != 0) {
            TypedValue value = new TypedValue();
            r.getValueForDensity(srcResId, srcDensityOverride, value, true);
            if (srcDensityOverride > 0 && value.density > 0 && value.density != 65535) {
                if (value.density == srcDensityOverride) {
                    value.density = r.getDisplayMetrics().densityDpi;
                } else {
                    value.density = (value.density * r.getDisplayMetrics().densityDpi) / srcDensityOverride;
                }
            }
            int density = 0;
            if (value.density == 0) {
                density = 160;
            } else if (value.density != 65535) {
                density = value.density;
            }
            Bitmap bitmap = null;
            try {
                InputStream is = r.openRawResource(srcResId, value);
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(r, is, density), $$Lambda$BitmapDrawable$LMqt8JvxZ4giSOIRAtlCKDg39Jw.INSTANCE);
                if (is != null) {
                    $closeResource(null, is);
                }
            } catch (Exception e) {
            }
            if (bitmap != null) {
                state.mBitmap = bitmap;
            } else {
                throw new XmlPullParserException(a.getPositionDescription() + ": <bitmap> requires a valid 'src' attribute");
            }
        }
        setMipMap(a.getBoolean(8, state.mBitmap != null ? state.mBitmap.hasMipMap() : false));
        state.mAutoMirrored = a.getBoolean(9, state.mAutoMirrored);
        state.mBaseAlpha = a.getFloat(7, state.mBaseAlpha);
        int tintMode = a.getInt(10, -1);
        if (tintMode != -1) {
            state.mBlendMode = Drawable.parseBlendMode(tintMode, BlendMode.SRC_IN);
        }
        ColorStateList tint = a.getColorStateList(5);
        if (tint != null) {
            state.mTint = tint;
        }
        Paint paint = this.mBitmapState.mPaint;
        paint.setAntiAlias(a.getBoolean(2, paint.isAntiAlias()));
        paint.setFilterBitmap(a.getBoolean(3, paint.isFilterBitmap()));
        paint.setDither(a.getBoolean(4, paint.isDither()));
        setGravity(a.getInt(0, state.mGravity));
        int tileMode = a.getInt(6, -2);
        if (tileMode != -2) {
            Shader.TileMode mode = parseTileMode(tileMode);
            setTileModeXY(mode, mode);
        }
        int tileModeX = a.getInt(11, -2);
        if (tileModeX != -2) {
            setTileModeX(parseTileMode(tileModeX));
        }
        int tileModeY = a.getInt(12, -2);
        if (tileModeY != -2) {
            setTileModeY(parseTileMode(tileModeY));
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        BitmapState state = this.mBitmapState;
        if (state != null) {
            if (state.mThemeAttrs != null) {
                TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.BitmapDrawable);
                try {
                    updateStateFromTypedArray(a, state.mSrcDensityOverride);
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

    private static Shader.TileMode parseTileMode(int tileMode) {
        if (tileMode == 0) {
            return Shader.TileMode.CLAMP;
        }
        if (tileMode == 1) {
            return Shader.TileMode.REPEAT;
        }
        if (tileMode != 2) {
            return null;
        }
        return Shader.TileMode.MIRROR;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean canApplyTheme() {
        BitmapState bitmapState = this.mBitmapState;
        return bitmapState != null && bitmapState.canApplyTheme();
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
        Bitmap bitmap;
        if (this.mBitmapState.mGravity == 119 && (bitmap = this.mBitmapState.mBitmap) != null && !bitmap.hasAlpha() && this.mBitmapState.mPaint.getAlpha() >= 255) {
            return -1;
        }
        return -3;
    }

    @Override // android.graphics.drawable.Drawable
    public final Drawable.ConstantState getConstantState() {
        this.mBitmapState.mChangingConfigurations |= getChangingConfigurations();
        return this.mBitmapState;
    }

    /* access modifiers changed from: package-private */
    public static final class BitmapState extends Drawable.ConstantState {
        boolean mAutoMirrored = false;
        float mBaseAlpha = 1.0f;
        Bitmap mBitmap = null;
        BlendMode mBlendMode = Drawable.DEFAULT_BLEND_MODE;
        int mChangingConfigurations;
        int mGravity = 119;
        final Paint mPaint;
        boolean mRebuildShader;
        int mSrcDensityOverride = 0;
        int mTargetDensity = 160;
        int[] mThemeAttrs = null;
        Shader.TileMode mTileModeX = null;
        Shader.TileMode mTileModeY = null;
        ColorStateList mTint = null;

        BitmapState(Bitmap bitmap) {
            this.mBitmap = bitmap;
            this.mPaint = new Paint(6);
        }

        BitmapState(BitmapState bitmapState) {
            this.mBitmap = bitmapState.mBitmap;
            this.mTint = bitmapState.mTint;
            this.mBlendMode = bitmapState.mBlendMode;
            this.mThemeAttrs = bitmapState.mThemeAttrs;
            this.mChangingConfigurations = bitmapState.mChangingConfigurations;
            this.mGravity = bitmapState.mGravity;
            this.mTileModeX = bitmapState.mTileModeX;
            this.mTileModeY = bitmapState.mTileModeY;
            this.mSrcDensityOverride = bitmapState.mSrcDensityOverride;
            this.mTargetDensity = bitmapState.mTargetDensity;
            this.mBaseAlpha = bitmapState.mBaseAlpha;
            this.mPaint = new Paint(bitmapState.mPaint);
            this.mRebuildShader = bitmapState.mRebuildShader;
            this.mAutoMirrored = bitmapState.mAutoMirrored;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public boolean canApplyTheme() {
            ColorStateList colorStateList;
            return this.mThemeAttrs != null || ((colorStateList = this.mTint) != null && colorStateList.canApplyTheme());
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new BitmapDrawable(this, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res) {
            return new BitmapDrawable(this, res);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public int getChangingConfigurations() {
            int i = this.mChangingConfigurations;
            ColorStateList colorStateList = this.mTint;
            return i | (colorStateList != null ? colorStateList.getChangingConfigurations() : 0);
        }
    }

    private BitmapDrawable(BitmapState state, Resources res) {
        this.mDstRect = new Rect();
        this.mTargetDensity = 160;
        this.mDstRectAndInsetsDirty = true;
        this.mOpticalInsets = Insets.NONE;
        init(state, res);
    }

    private void init(BitmapState state, Resources res) {
        this.mBitmapState = state;
        updateLocalState(res);
        BitmapState bitmapState = this.mBitmapState;
        if (bitmapState != null && res != null) {
            bitmapState.mTargetDensity = this.mTargetDensity;
        }
    }

    private void updateLocalState(Resources res) {
        this.mTargetDensity = resolveDensity(res, this.mBitmapState.mTargetDensity);
        this.mBlendModeFilter = updateBlendModeFilter(this.mBlendModeFilter, this.mBitmapState.mTint, this.mBitmapState.mBlendMode);
        computeBitmapSize();
    }
}
