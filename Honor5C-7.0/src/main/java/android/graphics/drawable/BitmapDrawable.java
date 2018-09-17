package android.graphics.drawable;

import android.bluetooth.BluetoothAssignedNumbers;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Insets;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable.ConstantState;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.os.Process;
import android.speech.tts.TextToSpeech.Engine;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import com.android.internal.R;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
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
    private BitmapState mBitmapState;
    private int mBitmapWidth;
    private final Rect mDstRect;
    private boolean mDstRectAndInsetsDirty;
    private Matrix mMirrorMatrix;
    private boolean mMutated;
    private Insets mOpticalInsets;
    private int mTargetDensity;
    private PorterDuffColorFilter mTintFilter;

    static final class BitmapState extends ConstantState {
        boolean mAutoMirrored;
        float mBaseAlpha;
        Bitmap mBitmap;
        int mChangingConfigurations;
        int mGravity;
        final Paint mPaint;
        boolean mRebuildShader;
        int mTargetDensity;
        int[] mThemeAttrs;
        TileMode mTileModeX;
        TileMode mTileModeY;
        ColorStateList mTint;
        Mode mTintMode;

        BitmapState(Bitmap bitmap) {
            this.mThemeAttrs = null;
            this.mBitmap = null;
            this.mTint = null;
            this.mTintMode = BitmapDrawable.DEFAULT_TINT_MODE;
            this.mGravity = BluetoothAssignedNumbers.LAIRD_TECHNOLOGIES;
            this.mBaseAlpha = Engine.DEFAULT_VOLUME;
            this.mTileModeX = null;
            this.mTileModeY = null;
            this.mTargetDensity = Const.CODE_G3_RANGE_START;
            this.mAutoMirrored = false;
            this.mBitmap = bitmap;
            this.mPaint = new Paint((int) BitmapDrawable.DEFAULT_PAINT_FLAGS);
        }

        BitmapState(BitmapState bitmapState) {
            this.mThemeAttrs = null;
            this.mBitmap = null;
            this.mTint = null;
            this.mTintMode = BitmapDrawable.DEFAULT_TINT_MODE;
            this.mGravity = BluetoothAssignedNumbers.LAIRD_TECHNOLOGIES;
            this.mBaseAlpha = Engine.DEFAULT_VOLUME;
            this.mTileModeX = null;
            this.mTileModeY = null;
            this.mTargetDensity = Const.CODE_G3_RANGE_START;
            this.mAutoMirrored = false;
            this.mBitmap = bitmapState.mBitmap;
            this.mTint = bitmapState.mTint;
            this.mTintMode = bitmapState.mTintMode;
            this.mThemeAttrs = bitmapState.mThemeAttrs;
            this.mChangingConfigurations = bitmapState.mChangingConfigurations;
            this.mGravity = bitmapState.mGravity;
            this.mTileModeX = bitmapState.mTileModeX;
            this.mTileModeY = bitmapState.mTileModeY;
            this.mTargetDensity = bitmapState.mTargetDensity;
            this.mBaseAlpha = bitmapState.mBaseAlpha;
            this.mPaint = new Paint(bitmapState.mPaint);
            this.mRebuildShader = bitmapState.mRebuildShader;
            this.mAutoMirrored = bitmapState.mAutoMirrored;
        }

        public boolean canApplyTheme() {
            if (this.mThemeAttrs == null) {
                return this.mTint != null ? this.mTint.canApplyTheme() : false;
            } else {
                return true;
            }
        }

        public int addAtlasableBitmaps(Collection<Bitmap> atlasList) {
            if (isAtlasable(this.mBitmap) && atlasList.add(this.mBitmap)) {
                return this.mBitmap.getWidth() * this.mBitmap.getHeight();
            }
            return BitmapDrawable.TILE_MODE_CLAMP;
        }

        public Drawable newDrawable() {
            return new BitmapDrawable(null, null);
        }

        public Drawable newDrawable(Resources res) {
            return new BitmapDrawable(res, null);
        }

        public int getChangingConfigurations() {
            return (this.mTint != null ? this.mTint.getChangingConfigurations() : BitmapDrawable.TILE_MODE_CLAMP) | this.mChangingConfigurations;
        }
    }

    @Deprecated
    public BitmapDrawable() {
        this.mDstRect = new Rect();
        this.mTargetDensity = Const.CODE_G3_RANGE_START;
        this.mDstRectAndInsetsDirty = true;
        this.mOpticalInsets = Insets.NONE;
        this.mBitmapState = new BitmapState((Bitmap) null);
    }

    @Deprecated
    public BitmapDrawable(Resources res) {
        this.mDstRect = new Rect();
        this.mTargetDensity = Const.CODE_G3_RANGE_START;
        this.mDstRectAndInsetsDirty = true;
        this.mOpticalInsets = Insets.NONE;
        this.mBitmapState = new BitmapState((Bitmap) null);
        this.mBitmapState.mTargetDensity = this.mTargetDensity;
    }

    @Deprecated
    public BitmapDrawable(Bitmap bitmap) {
        this(new BitmapState(bitmap), null);
    }

    public BitmapDrawable(Resources res, Bitmap bitmap) {
        this(new BitmapState(bitmap), res);
        this.mBitmapState.mTargetDensity = this.mTargetDensity;
    }

    @Deprecated
    public BitmapDrawable(String filepath) {
        this(new BitmapState(BitmapFactory.decodeFile(filepath)), null);
        if (this.mBitmapState.mBitmap == null) {
            Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + filepath);
        }
    }

    public BitmapDrawable(Resources res, String filepath) {
        this(new BitmapState(BitmapFactory.decodeFile(filepath)), null);
        this.mBitmapState.mTargetDensity = this.mTargetDensity;
        if (this.mBitmapState.mBitmap == null) {
            Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + filepath);
        }
    }

    @Deprecated
    public BitmapDrawable(InputStream is) {
        this(new BitmapState(BitmapFactory.decodeStream(is)), null);
        if (this.mBitmapState.mBitmap == null) {
            Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + is);
        }
    }

    public BitmapDrawable(Resources res, InputStream is) {
        this(new BitmapState(BitmapFactory.decodeStream(is)), null);
        this.mBitmapState.mTargetDensity = this.mTargetDensity;
        if (this.mBitmapState.mBitmap == null) {
            Log.w("BitmapDrawable", "BitmapDrawable cannot decode " + is);
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
        this.mBitmapHeight = TILE_MODE_DISABLED;
        this.mBitmapWidth = TILE_MODE_DISABLED;
    }

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
            if (density == 0) {
                density = Const.CODE_G3_RANGE_START;
            }
            this.mTargetDensity = density;
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
        return this.mBitmapState.mBitmap != null ? this.mBitmapState.mBitmap.hasMipMap() : false;
    }

    public void setAntiAlias(boolean aa) {
        this.mBitmapState.mPaint.setAntiAlias(aa);
        invalidateSelf();
    }

    public boolean hasAntiAlias() {
        return this.mBitmapState.mPaint.isAntiAlias();
    }

    public void setFilterBitmap(boolean filter) {
        this.mBitmapState.mPaint.setFilterBitmap(filter);
        invalidateSelf();
    }

    public boolean isFilterBitmap() {
        return this.mBitmapState.mPaint.isFilterBitmap();
    }

    public void setDither(boolean dither) {
        this.mBitmapState.mPaint.setDither(dither);
        invalidateSelf();
    }

    public TileMode getTileModeX() {
        return this.mBitmapState.mTileModeX;
    }

    public TileMode getTileModeY() {
        return this.mBitmapState.mTileModeY;
    }

    public void setTileModeX(TileMode mode) {
        setTileModeXY(mode, this.mBitmapState.mTileModeY);
    }

    public final void setTileModeY(TileMode mode) {
        setTileModeXY(this.mBitmapState.mTileModeX, mode);
    }

    public void setTileModeXY(TileMode xmode, TileMode ymode) {
        BitmapState state = this.mBitmapState;
        if (state.mTileModeX != xmode || state.mTileModeY != ymode) {
            state.mTileModeX = xmode;
            state.mTileModeY = ymode;
            state.mRebuildShader = true;
            this.mDstRectAndInsetsDirty = true;
            invalidateSelf();
        }
    }

    public void setAutoMirrored(boolean mirrored) {
        if (this.mBitmapState.mAutoMirrored != mirrored) {
            this.mBitmapState.mAutoMirrored = mirrored;
            invalidateSelf();
        }
    }

    public final boolean isAutoMirrored() {
        return this.mBitmapState.mAutoMirrored;
    }

    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mBitmapState.getChangingConfigurations();
    }

    private boolean needMirroring() {
        return isAutoMirrored() && getLayoutDirection() == TILE_MODE_REPEAT;
    }

    private void updateMirrorMatrix(float dx) {
        if (this.mMirrorMatrix == null) {
            this.mMirrorMatrix = new Matrix();
        }
        this.mMirrorMatrix.setTranslate(dx, 0.0f);
        this.mMirrorMatrix.preScale(ScaledLayoutParams.SCALE_UNSPECIFIED, Engine.DEFAULT_VOLUME);
    }

    protected void onBoundsChange(Rect bounds) {
        this.mDstRectAndInsetsDirty = true;
        Shader shader = this.mBitmapState.mPaint.getShader();
        if (shader == null) {
            return;
        }
        if (needMirroring()) {
            updateMirrorMatrix((float) (bounds.right - bounds.left));
            shader.setLocalMatrix(this.mMirrorMatrix);
            this.mBitmapState.mPaint.setShader(shader);
        } else if (this.mMirrorMatrix != null) {
            this.mMirrorMatrix = null;
            shader.setLocalMatrix(Matrix.IDENTITY_MATRIX);
            this.mBitmapState.mPaint.setShader(shader);
        }
    }

    public void draw(Canvas canvas) {
        Bitmap bitmap = this.mBitmapState.mBitmap;
        if (bitmap != null) {
            int restoreAlpha;
            boolean clearColorFilter;
            BitmapState state = this.mBitmapState;
            Paint paint = state.mPaint;
            if (state.mRebuildShader) {
                TileMode tmx = state.mTileModeX;
                TileMode tmy = state.mTileModeY;
                if (tmx == null && tmy == null) {
                    paint.setShader(null);
                } else {
                    if (tmx == null) {
                        tmx = TileMode.CLAMP;
                    }
                    if (tmy == null) {
                        tmy = TileMode.CLAMP;
                    }
                    paint.setShader(new BitmapShader(bitmap, tmx, tmy));
                }
                state.mRebuildShader = false;
            }
            if (state.mBaseAlpha != Engine.DEFAULT_VOLUME) {
                Paint p = getPaint();
                restoreAlpha = p.getAlpha();
                p.setAlpha((int) ((((float) restoreAlpha) * state.mBaseAlpha) + NetworkHistoryUtils.RECOVERY_PERCENTAGE));
            } else {
                restoreAlpha = TILE_MODE_DISABLED;
            }
            if (this.mTintFilter == null || paint.getColorFilter() != null) {
                clearColorFilter = false;
            } else {
                paint.setColorFilter(this.mTintFilter);
                clearColorFilter = true;
            }
            updateDstRectAndInsetsIfDirty();
            Shader shader = paint.getShader();
            boolean needMirroring = needMirroring();
            if (shader == null) {
                if (needMirroring) {
                    canvas.save();
                    canvas.translate((float) (this.mDstRect.right - this.mDstRect.left), 0.0f);
                    canvas.scale(ScaledLayoutParams.SCALE_UNSPECIFIED, Engine.DEFAULT_VOLUME);
                }
                try {
                    canvas.drawBitmap(bitmap, null, this.mDstRect, paint);
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
                if (needMirroring) {
                    updateMirrorMatrix((float) (this.mDstRect.right - this.mDstRect.left));
                    shader.setLocalMatrix(this.mMirrorMatrix);
                    paint.setShader(shader);
                } else if (this.mMirrorMatrix != null) {
                    this.mMirrorMatrix = null;
                    shader.setLocalMatrix(Matrix.IDENTITY_MATRIX);
                    paint.setShader(shader);
                }
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

    public Insets getOpticalInsets() {
        updateDstRectAndInsetsIfDirty();
        return this.mOpticalInsets;
    }

    public void getOutline(Outline outline) {
        float alpha;
        boolean opaqueOverShape = false;
        updateDstRectAndInsetsIfDirty();
        outline.setRect(this.mDstRect);
        if (!(this.mBitmapState.mBitmap == null || this.mBitmapState.mBitmap.hasAlpha())) {
            opaqueOverShape = true;
        }
        if (opaqueOverShape) {
            alpha = ((float) getAlpha()) / 255.0f;
        } else {
            alpha = 0.0f;
        }
        outline.setAlpha(alpha);
    }

    public void setAlpha(int alpha) {
        if (alpha != this.mBitmapState.mPaint.getAlpha()) {
            this.mBitmapState.mPaint.setAlpha(alpha);
            invalidateSelf();
        }
    }

    public int getAlpha() {
        return this.mBitmapState.mPaint.getAlpha();
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mBitmapState.mPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    public ColorFilter getColorFilter() {
        return this.mBitmapState.mPaint.getColorFilter();
    }

    public void setTintList(ColorStateList tint) {
        BitmapState state = this.mBitmapState;
        if (state.mTint != tint) {
            state.mTint = tint;
            this.mTintFilter = updateTintFilter(this.mTintFilter, tint, this.mBitmapState.mTintMode);
            invalidateSelf();
        }
    }

    public void setTintMode(Mode tintMode) {
        BitmapState state = this.mBitmapState;
        if (state.mTintMode != tintMode) {
            state.mTintMode = tintMode;
            this.mTintFilter = updateTintFilter(this.mTintFilter, this.mBitmapState.mTint, tintMode);
            invalidateSelf();
        }
    }

    public ColorStateList getTint() {
        return this.mBitmapState.mTint;
    }

    public Mode getTintMode() {
        return this.mBitmapState.mTintMode;
    }

    public void setXfermode(Xfermode xfermode) {
        this.mBitmapState.mPaint.setXfermode(xfermode);
        invalidateSelf();
    }

    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mBitmapState = new BitmapState(this.mBitmapState);
            this.mMutated = true;
        }
        return this;
    }

    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
    }

    protected boolean onStateChange(int[] stateSet) {
        BitmapState state = this.mBitmapState;
        if (state.mTint == null || state.mTintMode == null) {
            return false;
        }
        this.mTintFilter = updateTintFilter(this.mTintFilter, state.mTint, state.mTintMode);
        return true;
    }

    public boolean isStateful() {
        if (this.mBitmapState.mTint == null || !this.mBitmapState.mTint.isStateful()) {
            return super.isStateful();
        }
        return true;
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        TypedArray a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.BitmapDrawable);
        updateStateFromTypedArray(a);
        verifyRequiredAttributes(a);
        a.recycle();
        updateLocalState(r);
    }

    private void verifyRequiredAttributes(TypedArray a) throws XmlPullParserException {
        BitmapState state = this.mBitmapState;
        if (state.mBitmap != null) {
            return;
        }
        if (state.mThemeAttrs == null || state.mThemeAttrs[TILE_MODE_REPEAT] == 0) {
            throw new XmlPullParserException(a.getPositionDescription() + ": <bitmap> requires a valid 'src' attribute");
        }
    }

    private void updateStateFromTypedArray(TypedArray a) throws XmlPullParserException {
        Resources r = a.getResources();
        BitmapState state = this.mBitmapState;
        state.mChangingConfigurations |= a.getChangingConfigurations();
        state.mThemeAttrs = a.extractThemeAttrs();
        int srcResId = a.getResourceId(TILE_MODE_REPEAT, TILE_MODE_CLAMP);
        if (srcResId != 0) {
            Bitmap bitmap = BitmapFactory.decodeResource(r, srcResId);
            if (bitmap == null) {
                throw new XmlPullParserException(a.getPositionDescription() + ": <bitmap> requires a valid 'src' attribute");
            }
            state.mBitmap = bitmap;
        }
        state.mTargetDensity = r.getDisplayMetrics().densityDpi;
        setMipMap(a.getBoolean(8, state.mBitmap != null ? state.mBitmap.hasMipMap() : false));
        state.mAutoMirrored = a.getBoolean(9, state.mAutoMirrored);
        state.mBaseAlpha = a.getFloat(7, state.mBaseAlpha);
        int tintMode = a.getInt(10, TILE_MODE_DISABLED);
        if (tintMode != TILE_MODE_DISABLED) {
            state.mTintMode = Drawable.parseTintMode(tintMode, Mode.SRC_IN);
        }
        ColorStateList tint = a.getColorStateList(5);
        if (tint != null) {
            state.mTint = tint;
        }
        Paint paint = this.mBitmapState.mPaint;
        paint.setAntiAlias(a.getBoolean(TILE_MODE_MIRROR, paint.isAntiAlias()));
        paint.setFilterBitmap(a.getBoolean(3, paint.isFilterBitmap()));
        paint.setDither(a.getBoolean(4, paint.isDither()));
        setGravity(a.getInt(TILE_MODE_CLAMP, state.mGravity));
        int tileMode = a.getInt(DEFAULT_PAINT_FLAGS, TILE_MODE_UNDEFINED);
        if (tileMode != TILE_MODE_UNDEFINED) {
            TileMode mode = parseTileMode(tileMode);
            setTileModeXY(mode, mode);
        }
        int tileModeX = a.getInt(11, TILE_MODE_UNDEFINED);
        if (tileModeX != TILE_MODE_UNDEFINED) {
            setTileModeX(parseTileMode(tileModeX));
        }
        int tileModeY = a.getInt(12, TILE_MODE_UNDEFINED);
        if (tileModeY != TILE_MODE_UNDEFINED) {
            setTileModeY(parseTileMode(tileModeY));
        }
        state.mTargetDensity = Drawable.resolveDensity(r, TILE_MODE_CLAMP);
    }

    public void applyTheme(Theme t) {
        super.applyTheme(t);
        BitmapState state = this.mBitmapState;
        if (state != null) {
            if (state.mThemeAttrs != null) {
                TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.BitmapDrawable);
                try {
                    updateStateFromTypedArray(a);
                } catch (XmlPullParserException e) {
                    Drawable.rethrowAsRuntimeException(e);
                } finally {
                    a.recycle();
                }
            }
            if (state.mTint != null && state.mTint.canApplyTheme()) {
                state.mTint = state.mTint.obtainForTheme(t);
            }
            updateLocalState(t.getResources());
        }
    }

    private static TileMode parseTileMode(int tileMode) {
        switch (tileMode) {
            case TILE_MODE_CLAMP /*0*/:
                return TileMode.CLAMP;
            case TILE_MODE_REPEAT /*1*/:
                return TileMode.REPEAT;
            case TILE_MODE_MIRROR /*2*/:
                return TileMode.MIRROR;
            default:
                return null;
        }
    }

    public boolean canApplyTheme() {
        return this.mBitmapState != null ? this.mBitmapState.canApplyTheme() : false;
    }

    public int getIntrinsicWidth() {
        return this.mBitmapWidth;
    }

    public int getIntrinsicHeight() {
        return this.mBitmapHeight;
    }

    public int getOpacity() {
        int i = -3;
        if (this.mBitmapState.mGravity != BluetoothAssignedNumbers.LAIRD_TECHNOLOGIES) {
            return -3;
        }
        Bitmap bitmap = this.mBitmapState.mBitmap;
        if (!(bitmap == null || bitmap.hasAlpha() || this.mBitmapState.mPaint.getAlpha() < Process.PROC_TERM_MASK)) {
            i = TILE_MODE_DISABLED;
        }
        return i;
    }

    public final ConstantState getConstantState() {
        BitmapState bitmapState = this.mBitmapState;
        bitmapState.mChangingConfigurations |= getChangingConfigurations();
        return this.mBitmapState;
    }

    private BitmapDrawable(BitmapState state, Resources res) {
        this.mDstRect = new Rect();
        this.mTargetDensity = Const.CODE_G3_RANGE_START;
        this.mDstRectAndInsetsDirty = true;
        this.mOpticalInsets = Insets.NONE;
        this.mBitmapState = state;
        updateLocalState(res);
    }

    private void updateLocalState(Resources res) {
        this.mTargetDensity = Drawable.resolveDensity(res, this.mBitmapState.mTargetDensity);
        this.mTintFilter = updateTintFilter(this.mTintFilter, this.mBitmapState.mTint, this.mBitmapState.mTintMode);
        computeBitmapSize();
    }
}
