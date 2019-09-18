package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class RippleDrawable extends LayerDrawable {
    private static final int MASK_CONTENT = 1;
    private static final int MASK_EXPLICIT = 2;
    private static final int MASK_NONE = 0;
    private static final int MASK_UNKNOWN = -1;
    private static final int MAX_RIPPLES = 10;
    public static final int RADIUS_AUTO = -1;
    public static final int TYPE_LINEAR = 0;
    public static final int TYPE_RADICAL = 1;
    public static final int TYPE_RIPPLE = 2;
    private RippleBackground mBackground;
    private int mDensity;
    private final Rect mDirtyBounds;
    private final Rect mDrawingBounds;
    private RippleForeground[] mExitingRipples;
    private int mExitingRipplesCount;
    private boolean mForceSoftware;
    private boolean mHasPending;
    private boolean mHasValidMask;
    private final Rect mHotspotBounds;
    private HwRippleForeground mHwRipple;
    private boolean mHwRippleActive;
    private Drawable mMask;
    private Bitmap mMaskBuffer;
    private Canvas mMaskCanvas;
    private PorterDuffColorFilter mMaskColorFilter;
    private Matrix mMaskMatrix;
    private BitmapShader mMaskShader;
    private boolean mOverrideBounds;
    private float mPendingX;
    private float mPendingY;
    private RippleForeground mRipple;
    private boolean mRippleActive;
    private Paint mRipplePaint;
    private boolean mRippleUnBounded;
    private RippleState mState;
    private final Rect mTempRect;

    static class RippleState extends LayerDrawable.LayerState {
        ColorStateList mColor = ColorStateList.valueOf(Color.MAGENTA);
        int mMaxRadius = -1;
        int[] mTouchThemeAttrs;
        int mType = 2;

        public RippleState(LayerDrawable.LayerState orig, RippleDrawable owner, Resources res) {
            super(orig, owner, res);
            if (orig != null && (orig instanceof RippleState)) {
                RippleState origs = (RippleState) orig;
                this.mTouchThemeAttrs = origs.mTouchThemeAttrs;
                this.mColor = origs.mColor;
                this.mMaxRadius = origs.mMaxRadius;
                this.mType = origs.mType;
                if (origs.mDensity != this.mDensity) {
                    applyDensityScaling(orig.mDensity, this.mDensity);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void onDensityChanged(int sourceDensity, int targetDensity) {
            super.onDensityChanged(sourceDensity, targetDensity);
            applyDensityScaling(sourceDensity, targetDensity);
        }

        private void applyDensityScaling(int sourceDensity, int targetDensity) {
            if (this.mMaxRadius != -1) {
                this.mMaxRadius = Drawable.scaleFromDensity(this.mMaxRadius, sourceDensity, targetDensity, true);
            }
        }

        public boolean canApplyTheme() {
            return this.mTouchThemeAttrs != null || (this.mColor != null && this.mColor.canApplyTheme()) || super.canApplyTheme();
        }

        public Drawable newDrawable() {
            return new RippleDrawable(this, (Resources) null);
        }

        public Drawable newDrawable(Resources res) {
            return new RippleDrawable(this, res);
        }

        public int getChangingConfigurations() {
            return super.getChangingConfigurations() | (this.mColor != null ? this.mColor.getChangingConfigurations() : 0);
        }
    }

    RippleDrawable() {
        this(new RippleState(null, null, null), null);
    }

    public RippleDrawable(ColorStateList color, Drawable content, Drawable mask) {
        this(new RippleState(null, null, null), null);
        if (color != null) {
            if (content != null) {
                addLayer(content, null, 0, 0, 0, 0, 0);
            }
            if (mask != null) {
                addLayer(mask, null, 16908334, 0, 0, 0, 0);
            }
            setColor(color);
            ensurePadding();
            refreshPadding();
            updateLocalState();
            return;
        }
        throw new IllegalArgumentException("RippleDrawable requires a non-null color");
    }

    public void jumpToCurrentState() {
        super.jumpToCurrentState();
        if (this.mRipple != null) {
            this.mRipple.end();
        }
        if (this.mBackground != null) {
            this.mBackground.jumpToFinal();
        }
        if (this.mHwRipple != null) {
            this.mHwRipple.end();
        }
        cancelExitingRipples();
    }

    private void cancelExitingRipples() {
        int count = this.mExitingRipplesCount;
        RippleForeground[] ripples = this.mExitingRipples;
        for (int i = 0; i < count; i++) {
            ripples[i].end();
        }
        if (ripples != null) {
            Arrays.fill(ripples, 0, count, null);
        }
        this.mExitingRipplesCount = 0;
        invalidateSelf(false);
    }

    public int getOpacity() {
        return -3;
    }

    /* access modifiers changed from: protected */
    public boolean onStateChange(int[] stateSet) {
        boolean changed = super.onStateChange(stateSet);
        boolean hovered = false;
        boolean focused = false;
        boolean pressed = false;
        boolean enabled = false;
        for (int state : stateSet) {
            if (state == 16842910) {
                enabled = true;
            } else if (state == 16842908) {
                focused = true;
            } else if (state == 16842919) {
                pressed = true;
            } else if (state == 16843623) {
                hovered = true;
            }
        }
        boolean z = true;
        setRippleActive(enabled && pressed);
        setBackgroundActive(hovered, focused, pressed);
        if (!hovered && !focused && (!enabled || !pressed)) {
            z = false;
        }
        setHwRippleActive(z);
        return changed;
    }

    private void setRippleActive(boolean active) {
        if (this.mRippleActive != active) {
            this.mRippleActive = active;
            if (active) {
                tryRippleEnter();
            } else {
                tryRippleExit();
            }
        }
    }

    private void setBackgroundActive(boolean hovered, boolean focused, boolean pressed) {
        if (this.mBackground == null && (hovered || focused)) {
            this.mBackground = new RippleBackground(this, this.mHotspotBounds, isBounded());
            this.mBackground.setup((float) this.mState.mMaxRadius, this.mDensity);
        }
        if (this.mBackground != null) {
            this.mBackground.setState(focused, hovered, pressed);
        }
    }

    private void setHwRippleActive(boolean active) {
        if (this.mHwRippleActive != active) {
            this.mHwRippleActive = active;
            if (active) {
                tryHwRippleEnter();
            } else {
                tryHwRippleExit();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        if (!this.mOverrideBounds) {
            this.mHotspotBounds.set(bounds);
            onHotspotBoundsChanged();
        }
        int count = this.mExitingRipplesCount;
        RippleForeground[] ripples = this.mExitingRipples;
        for (int i = 0; i < count; i++) {
            ripples[i].onBoundsChange();
        }
        if (this.mBackground != null) {
            this.mBackground.onBoundsChange();
        }
        if (this.mRipple != null) {
            this.mRipple.onBoundsChange();
        }
        if (this.mHwRipple != null) {
            this.mHwRipple.onBoundsChange();
        }
        invalidateSelf();
    }

    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        if (!visible) {
            clearHotspots();
        } else if (changed) {
            if (this.mRippleActive) {
                tryRippleEnter();
            }
            if (this.mHwRippleActive) {
                tryHwRippleEnter();
            }
            jumpToCurrentState();
        }
        return changed;
    }

    public boolean isProjected() {
        if (isBounded()) {
            return false;
        }
        int radius = this.mState.mMaxRadius;
        Rect drawableBounds = getBounds();
        Rect hotspotBounds = this.mHotspotBounds;
        if (radius == -1 || radius > hotspotBounds.width() / 2 || radius > hotspotBounds.height() / 2 || (!drawableBounds.equals(hotspotBounds) && !drawableBounds.contains(hotspotBounds))) {
            return true;
        }
        return false;
    }

    public void setRippleUnBounded(boolean rippleUnBounded) {
        this.mRippleUnBounded = rippleUnBounded;
    }

    public boolean getRippleUnBounded() {
        return this.mRippleUnBounded;
    }

    private boolean isBounded() {
        boolean z = false;
        if (getRippleUnBounded()) {
            return false;
        }
        if (getNumberOfLayers() > 0) {
            z = true;
        }
        return z;
    }

    public boolean isStateful() {
        return true;
    }

    public boolean hasFocusStateSpecified() {
        return true;
    }

    public void setColor(ColorStateList color) {
        this.mState.mColor = color;
        invalidateSelf(false);
    }

    public void setRadius(int radius) {
        this.mState.mMaxRadius = radius;
        invalidateSelf(false);
    }

    public int getRadius() {
        return this.mState.mMaxRadius;
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        TypedArray ahw = r.obtainAttributes(Xml.asAttributeSet(parser), new int[]{16843169});
        this.mState.mType = ahw.getInt(0, this.mState.mType);
        if (this.mState.mType == 1) {
            setRippleUnBounded(true);
        }
        ahw.recycle();
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.RippleDrawable);
        setPaddingMode(1);
        super.inflate(r, parser, attrs, theme);
        updateStateFromTypedArray(a);
        verifyRequiredAttributes(a);
        a.recycle();
        updateLocalState();
    }

    public boolean setDrawableByLayerId(int id, Drawable drawable) {
        if (!super.setDrawableByLayerId(id, drawable)) {
            return false;
        }
        if (id == 16908334) {
            this.mMask = drawable;
            this.mHasValidMask = false;
        }
        return true;
    }

    public void setPaddingMode(int mode) {
        super.setPaddingMode(mode);
    }

    private void updateStateFromTypedArray(TypedArray a) throws XmlPullParserException {
        RippleState state = this.mState;
        state.mChangingConfigurations |= a.getChangingConfigurations();
        state.mTouchThemeAttrs = a.extractThemeAttrs();
        ColorStateList color = a.getColorStateList(0);
        if (color != null) {
            this.mState.mColor = color;
        }
        this.mState.mMaxRadius = a.getDimensionPixelSize(1, this.mState.mMaxRadius);
    }

    private void verifyRequiredAttributes(TypedArray a) throws XmlPullParserException {
        if (this.mState.mColor != null) {
            return;
        }
        if (this.mState.mTouchThemeAttrs == null || this.mState.mTouchThemeAttrs[0] == 0) {
            throw new XmlPullParserException(a.getPositionDescription() + ": <ripple> requires a valid color attribute");
        }
    }

    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        RippleState state = this.mState;
        if (state != null) {
            if (state.mTouchThemeAttrs != null) {
                TypedArray a = t.resolveAttributes(state.mTouchThemeAttrs, R.styleable.RippleDrawable);
                try {
                    updateStateFromTypedArray(a);
                    verifyRequiredAttributes(a);
                } catch (XmlPullParserException e) {
                    rethrowAsRuntimeException(e);
                } catch (Throwable th) {
                    a.recycle();
                    throw th;
                }
                a.recycle();
            }
            if (state.mColor != null && state.mColor.canApplyTheme()) {
                state.mColor = state.mColor.obtainForTheme(t);
            }
            updateLocalState();
        }
    }

    public boolean canApplyTheme() {
        return (this.mState != null && this.mState.canApplyTheme()) || super.canApplyTheme();
    }

    public void setHotspot(float x, float y) {
        if (this.mRipple == null || this.mBackground == null) {
            this.mPendingX = x;
            this.mPendingY = y;
            this.mHasPending = true;
        }
        if (this.mRipple != null) {
            this.mRipple.move(x, y);
        }
    }

    private void tryHwRippleEnter() {
        if (this.mHwRipple == null) {
            this.mHwRipple = HwWidgetFactory.getHwRippleForeground(this, this.mHotspotBounds, isBounded(), this.mForceSoftware, this.mState.mType);
        }
        if (this.mHwRipple != null) {
            this.mHwRipple.setup((float) this.mState.mMaxRadius, this.mDensity);
            this.mHwRipple.enter(false);
        }
    }

    private void tryHwRippleExit() {
        if (this.mHwRipple != null) {
            this.mHwRipple.exit();
        }
    }

    private void tryRippleEnter() {
        float x;
        float y;
        if (this.mExitingRipplesCount < 10) {
            if (this.mRipple == null) {
                if (this.mHasPending) {
                    this.mHasPending = false;
                    x = this.mPendingX;
                    y = this.mPendingY;
                } else {
                    x = this.mHotspotBounds.exactCenterX();
                    y = this.mHotspotBounds.exactCenterY();
                }
                RippleForeground rippleForeground = new RippleForeground(this, this.mHotspotBounds, x, y, this.mForceSoftware);
                this.mRipple = rippleForeground;
            }
            this.mRipple.setup((float) this.mState.mMaxRadius, this.mDensity);
            this.mRipple.enter();
        }
    }

    private void tryRippleExit() {
        if (this.mRipple != null) {
            if (this.mExitingRipples == null) {
                this.mExitingRipples = new RippleForeground[10];
            }
            RippleForeground[] rippleForegroundArr = this.mExitingRipples;
            int i = this.mExitingRipplesCount;
            this.mExitingRipplesCount = i + 1;
            rippleForegroundArr[i] = this.mRipple;
            this.mRipple.exit();
            this.mRipple = null;
        }
    }

    private void clearHotspots() {
        if (this.mRipple != null) {
            this.mRipple.end();
            this.mRipple = null;
            this.mRippleActive = false;
        }
        if (this.mBackground != null) {
            this.mBackground.setState(false, false, false);
        }
        if (this.mHwRipple != null) {
            this.mHwRipple.end();
            this.mHwRipple = null;
            this.mHwRippleActive = false;
        }
        cancelExitingRipples();
    }

    public void setHotspotBounds(int left, int top, int right, int bottom) {
        this.mOverrideBounds = true;
        this.mHotspotBounds.set(left, top, right, bottom);
        onHotspotBoundsChanged();
    }

    public void getHotspotBounds(Rect outRect) {
        outRect.set(this.mHotspotBounds);
    }

    private void onHotspotBoundsChanged() {
        int count = this.mExitingRipplesCount;
        RippleForeground[] ripples = this.mExitingRipples;
        for (int i = 0; i < count; i++) {
            ripples[i].onHotspotBoundsChanged();
        }
        if (this.mRipple != null) {
            this.mRipple.onHotspotBoundsChanged();
        }
        if (this.mBackground != null) {
            this.mBackground.onHotspotBoundsChanged();
        }
        if (this.mHwRipple != null) {
            this.mHwRipple.onHotspotBoundsChanged();
        }
    }

    public void getOutline(Outline outline) {
        LayerDrawable.LayerState state = this.mLayerState;
        LayerDrawable.ChildDrawable[] children = state.mChildren;
        int N = state.mNumChildren;
        for (int i = 0; i < N; i++) {
            if (children[i].mId != 16908334) {
                children[i].mDrawable.getOutline(outline);
                if (!outline.isEmpty()) {
                    return;
                }
            }
        }
    }

    public void draw(Canvas canvas) {
        pruneRipples();
        Rect bounds = getDirtyBounds();
        int saveCount = canvas.save(2);
        if (isBounded()) {
            canvas.clipRect(bounds);
        }
        drawContent(canvas);
        if (this.mState.mType == 2 || this.mHwRipple == null) {
            drawBackgroundAndRipples(canvas);
        } else {
            drawHwRipple(canvas);
        }
        canvas.restoreToCount(saveCount);
    }

    public void invalidateSelf() {
        invalidateSelf(true);
    }

    /* access modifiers changed from: package-private */
    public void invalidateSelf(boolean invalidateMask) {
        super.invalidateSelf();
        if (invalidateMask) {
            this.mHasValidMask = false;
        }
    }

    private void pruneRipples() {
        int remaining = 0;
        RippleForeground[] ripples = this.mExitingRipples;
        int count = this.mExitingRipplesCount;
        for (int i = 0; i < count; i++) {
            if (!ripples[i].hasFinishedExit()) {
                ripples[remaining] = ripples[i];
                remaining++;
            }
        }
        for (int i2 = remaining; i2 < count; i2++) {
            ripples[i2] = null;
        }
        this.mExitingRipplesCount = remaining;
    }

    private void updateMaskShaderIfNeeded() {
        if (!this.mHasValidMask) {
            int maskType = getMaskType();
            if (maskType != -1) {
                this.mHasValidMask = true;
                Rect bounds = getBounds();
                if (maskType == 0 || bounds.isEmpty()) {
                    if (this.mMaskBuffer != null) {
                        this.mMaskBuffer.recycle();
                        this.mMaskBuffer = null;
                        this.mMaskShader = null;
                        this.mMaskCanvas = null;
                    }
                    this.mMaskMatrix = null;
                    this.mMaskColorFilter = null;
                    return;
                }
                if (this.mMaskBuffer != null && this.mMaskBuffer.getWidth() == bounds.width() && this.mMaskBuffer.getHeight() == bounds.height()) {
                    this.mMaskBuffer.eraseColor(0);
                } else {
                    if (this.mMaskBuffer != null) {
                        this.mMaskBuffer.recycle();
                    }
                    this.mMaskBuffer = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ALPHA_8);
                    this.mMaskShader = new BitmapShader(this.mMaskBuffer, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    this.mMaskCanvas = new Canvas(this.mMaskBuffer);
                }
                if (this.mMaskMatrix == null) {
                    this.mMaskMatrix = new Matrix();
                } else {
                    this.mMaskMatrix.reset();
                }
                if (this.mMaskColorFilter == null) {
                    this.mMaskColorFilter = new PorterDuffColorFilter(0, PorterDuff.Mode.SRC_IN);
                }
                int left = bounds.left;
                int top = bounds.top;
                this.mMaskCanvas.translate((float) (-left), (float) (-top));
                if (maskType == 2) {
                    drawMask(this.mMaskCanvas);
                } else if (maskType == 1) {
                    drawContent(this.mMaskCanvas);
                }
                this.mMaskCanvas.translate((float) left, (float) top);
            }
        }
    }

    private int getMaskType() {
        if (this.mRipple == null && this.mExitingRipplesCount <= 0 && (this.mBackground == null || !this.mBackground.isVisible())) {
            return -1;
        }
        if (this.mMask == null) {
            LayerDrawable.ChildDrawable[] array = this.mLayerState.mChildren;
            int count = this.mLayerState.mNumChildren;
            for (int i = 0; i < count; i++) {
                if (array[i].mDrawable.getOpacity() != -1) {
                    return 1;
                }
            }
            return 0;
        } else if (this.mMask.getOpacity() == -1) {
            return 0;
        } else {
            return 2;
        }
    }

    private void drawContent(Canvas canvas) {
        LayerDrawable.ChildDrawable[] array = this.mLayerState.mChildren;
        int count = this.mLayerState.mNumChildren;
        for (int i = 0; i < count; i++) {
            if (array[i].mId != 16908334) {
                array[i].mDrawable.draw(canvas);
            }
        }
    }

    private void drawHwRipple(Canvas canvas) {
        if (this.mHwRipple != null) {
            Paint p = getRipplePaint();
            p.setColor(this.mState.mColor.getColorForState(getState(), -16777216));
            this.mHwRipple.draw(canvas, p);
        }
    }

    private void drawBackgroundAndRipples(Canvas canvas) {
        RippleForeground active = this.mRipple;
        RippleBackground background = this.mBackground;
        int count = this.mExitingRipplesCount;
        if (active != null || count > 0 || (background != null && background.isVisible())) {
            float x = this.mHotspotBounds.exactCenterX();
            float y = this.mHotspotBounds.exactCenterY();
            canvas.translate(x, y);
            Paint p = getRipplePaint();
            if (background != null && background.isVisible()) {
                background.draw(canvas, p);
            }
            if (count > 0) {
                RippleForeground[] ripples = this.mExitingRipples;
                for (int i = 0; i < count; i++) {
                    ripples[i].draw(canvas, p);
                }
            }
            if (active != null) {
                active.draw(canvas, p);
            }
            canvas.translate(-x, -y);
        }
    }

    private void drawMask(Canvas canvas) {
        this.mMask.draw(canvas);
    }

    /* access modifiers changed from: package-private */
    public Paint getRipplePaint() {
        if (this.mRipplePaint == null) {
            this.mRipplePaint = new Paint();
            this.mRipplePaint.setAntiAlias(true);
            this.mRipplePaint.setStyle(Paint.Style.FILL);
        }
        float x = this.mHotspotBounds.exactCenterX();
        float y = this.mHotspotBounds.exactCenterY();
        updateMaskShaderIfNeeded();
        if (this.mMaskShader != null) {
            Rect bounds = getBounds();
            this.mMaskMatrix.setTranslate(((float) bounds.left) - x, ((float) bounds.top) - y);
            this.mMaskShader.setLocalMatrix(this.mMaskMatrix);
        }
        int color = this.mState.mColor.getColorForState(getState(), -16777216);
        if (Color.alpha(color) > 128) {
            color = (16777215 & color) | Integer.MIN_VALUE;
        }
        Paint p = this.mRipplePaint;
        if (this.mMaskColorFilter != null) {
            this.mMaskColorFilter.setColor(color | -16777216);
            p.setColor(-16777216 & color);
            p.setColorFilter(this.mMaskColorFilter);
            p.setShader(this.mMaskShader);
        } else {
            p.setColor(color);
            p.setColorFilter(null);
            p.setShader(null);
        }
        return p;
    }

    public Rect getDirtyBounds() {
        if (isBounded()) {
            return getBounds();
        }
        Rect drawingBounds = this.mDrawingBounds;
        Rect dirtyBounds = this.mDirtyBounds;
        dirtyBounds.set(drawingBounds);
        drawingBounds.setEmpty();
        int cX = (int) this.mHotspotBounds.exactCenterX();
        int cY = (int) this.mHotspotBounds.exactCenterY();
        Rect rippleBounds = this.mTempRect;
        RippleForeground[] activeRipples = this.mExitingRipples;
        int N = this.mExitingRipplesCount;
        for (int i = 0; i < N; i++) {
            activeRipples[i].getBounds(rippleBounds);
            rippleBounds.offset(cX, cY);
            drawingBounds.union(rippleBounds);
        }
        RippleBackground background = this.mBackground;
        if (background != null) {
            background.getBounds(rippleBounds);
            rippleBounds.offset(cX, cY);
            drawingBounds.union(rippleBounds);
        }
        if (this.mHwRipple != null) {
            this.mHwRipple.getBounds(rippleBounds);
            rippleBounds.offset(cX, cY);
            drawingBounds.union(rippleBounds);
        }
        dirtyBounds.union(drawingBounds);
        dirtyBounds.union(super.getDirtyBounds());
        return dirtyBounds;
    }

    public void setForceSoftware(boolean forceSoftware) {
        this.mForceSoftware = forceSoftware;
    }

    public Drawable.ConstantState getConstantState() {
        return this.mState;
    }

    public Drawable mutate() {
        super.mutate();
        this.mState = (RippleState) this.mLayerState;
        this.mMask = findDrawableByLayerId(16908334);
        return this;
    }

    /* access modifiers changed from: package-private */
    public RippleState createConstantState(LayerDrawable.LayerState state, Resources res) {
        return new RippleState(state, this, res);
    }

    private RippleDrawable(RippleState state, Resources res) {
        this.mTempRect = new Rect();
        this.mHotspotBounds = new Rect();
        this.mDrawingBounds = new Rect();
        this.mDirtyBounds = new Rect();
        this.mExitingRipplesCount = 0;
        this.mRippleUnBounded = false;
        this.mState = new RippleState(state, this, res);
        this.mLayerState = this.mState;
        this.mDensity = Drawable.resolveDensity(res, this.mState.mDensity);
        if (this.mState.mNumChildren > 0) {
            ensurePadding();
            refreshPadding();
        }
        updateLocalState();
    }

    private void updateLocalState() {
        this.mMask = findDrawableByLayerId(16908334);
    }
}
