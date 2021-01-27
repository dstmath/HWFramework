package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hwcontrol.HwWidgetFactory;
import android.hwgallerycache.HwGalleryCacheManager;
import android.media.TtmlUtils;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewHierarchyEncoder;
import android.view.accessibility.AccessibilityEvent;
import android.view.inspector.InspectionCompanion;
import android.view.inspector.PropertyMapper;
import android.view.inspector.PropertyReader;
import android.widget.RemoteViews;
import com.android.internal.R;
import huawei.android.widget.HwOnZoomEventListener;
import java.io.IOException;

@RemoteViews.RemoteView
public class ImageView extends View {
    private static final String LOG_TAG = "ImageView";
    private static boolean sCompatAdjustViewBounds;
    private static boolean sCompatDone;
    private static boolean sCompatDrawableVisibilityDispatch;
    private static boolean sCompatUseCorrectStreamDensity;
    private static final Matrix.ScaleToFit[] sS2FArray = {Matrix.ScaleToFit.FILL, Matrix.ScaleToFit.START, Matrix.ScaleToFit.CENTER, Matrix.ScaleToFit.END};
    private static final ScaleType[] sScaleTypeArray = {ScaleType.MATRIX, ScaleType.FIT_XY, ScaleType.FIT_START, ScaleType.FIT_CENTER, ScaleType.FIT_END, ScaleType.CENTER, ScaleType.CENTER_CROP, ScaleType.CENTER_INSIDE};
    @UnsupportedAppUsage
    private boolean mAdjustViewBounds;
    @UnsupportedAppUsage
    private int mAlpha;
    private int mBaseline;
    private boolean mBaselineAlignBottom;
    private ColorFilter mColorFilter;
    @UnsupportedAppUsage
    private boolean mCropToPadding;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124051687)
    private Matrix mDrawMatrix;
    @UnsupportedAppUsage
    private Drawable mDrawable;
    private BlendMode mDrawableBlendMode;
    @UnsupportedAppUsage
    private int mDrawableHeight;
    private ColorStateList mDrawableTintList;
    @UnsupportedAppUsage
    private int mDrawableWidth;
    private boolean mHasAlpha;
    private boolean mHasColorFilter;
    private boolean mHasDrawableBlendMode;
    private boolean mHasDrawableTint;
    private boolean mHasXfermode;
    private boolean mHaveFrame;
    private HwCompoundEventDetector mHwCompoundEventDetector;
    private HwImageViewZoom mHwImageViewZoom;
    public int mInBigView;
    private int mLevel;
    private Matrix mMatrix;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private int mMaxHeight;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private int mMaxWidth;
    private boolean mMergeState;
    private HwOnZoomEventListener mOnZoomListener;
    @UnsupportedAppUsage
    private BitmapDrawable mRecycleableBitmapDrawable;
    @UnsupportedAppUsage
    private int mResource;
    private ScaleType mScaleType;
    private int[] mState;
    private final RectF mTempDst;
    private final RectF mTempSrc;
    @UnsupportedAppUsage
    private Uri mUri;
    private final int mViewAlphaScale;
    private Xfermode mXfermode;

    public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<ImageView> {
        private int mAdjustViewBoundsId;
        private int mBaselineAlignBottomId;
        private int mBaselineId;
        private int mBlendModeId;
        private int mCropToPaddingId;
        private int mMaxHeightId;
        private int mMaxWidthId;
        private boolean mPropertiesMapped = false;
        private int mScaleTypeId;
        private int mSrcId;
        private int mTintId;
        private int mTintModeId;

        @Override // android.view.inspector.InspectionCompanion
        public void mapProperties(PropertyMapper propertyMapper) {
            this.mAdjustViewBoundsId = propertyMapper.mapBoolean("adjustViewBounds", 16843038);
            this.mBaselineId = propertyMapper.mapInt("baseline", 16843548);
            this.mBaselineAlignBottomId = propertyMapper.mapBoolean("baselineAlignBottom", 16843042);
            this.mBlendModeId = propertyMapper.mapObject("blendMode", 9);
            this.mCropToPaddingId = propertyMapper.mapBoolean("cropToPadding", 16843043);
            this.mMaxHeightId = propertyMapper.mapInt("maxHeight", 16843040);
            this.mMaxWidthId = propertyMapper.mapInt("maxWidth", 16843039);
            this.mScaleTypeId = propertyMapper.mapObject("scaleType", 16843037);
            this.mSrcId = propertyMapper.mapObject("src", 16843033);
            this.mTintId = propertyMapper.mapObject("tint", 16843041);
            this.mTintModeId = propertyMapper.mapObject("tintMode", 16843771);
            this.mPropertiesMapped = true;
        }

        public void readProperties(ImageView node, PropertyReader propertyReader) {
            if (this.mPropertiesMapped) {
                propertyReader.readBoolean(this.mAdjustViewBoundsId, node.getAdjustViewBounds());
                propertyReader.readInt(this.mBaselineId, node.getBaseline());
                propertyReader.readBoolean(this.mBaselineAlignBottomId, node.getBaselineAlignBottom());
                propertyReader.readObject(this.mBlendModeId, node.getImageTintBlendMode());
                propertyReader.readBoolean(this.mCropToPaddingId, node.getCropToPadding());
                propertyReader.readInt(this.mMaxHeightId, node.getMaxHeight());
                propertyReader.readInt(this.mMaxWidthId, node.getMaxWidth());
                propertyReader.readObject(this.mScaleTypeId, node.getScaleType());
                propertyReader.readObject(this.mSrcId, node.getDrawable());
                propertyReader.readObject(this.mTintId, node.getImageTintList());
                propertyReader.readObject(this.mTintModeId, node.getImageTintMode());
                return;
            }
            throw new InspectionCompanion.UninitializedPropertyMapException();
        }
    }

    public ImageView(Context context) {
        super(context);
        this.mResource = 0;
        this.mHaveFrame = false;
        this.mAdjustViewBounds = false;
        this.mMaxWidth = Integer.MAX_VALUE;
        this.mMaxHeight = Integer.MAX_VALUE;
        this.mColorFilter = null;
        this.mHasColorFilter = false;
        this.mHasXfermode = false;
        this.mAlpha = 255;
        this.mHasAlpha = false;
        this.mViewAlphaScale = 256;
        this.mDrawable = null;
        this.mRecycleableBitmapDrawable = null;
        this.mDrawableTintList = null;
        this.mDrawableBlendMode = null;
        this.mHasDrawableTint = false;
        this.mHasDrawableBlendMode = false;
        this.mState = null;
        this.mMergeState = false;
        this.mLevel = 0;
        this.mDrawMatrix = null;
        this.mTempSrc = new RectF();
        this.mTempDst = new RectF();
        this.mBaseline = -1;
        this.mBaselineAlignBottom = false;
        this.mInBigView = -1;
        this.mHwCompoundEventDetector = null;
        this.mHwImageViewZoom = null;
        this.mOnZoomListener = new HwOnZoomEventListener() {
            /* class android.widget.ImageView.AnonymousClass1 */

            @Override // huawei.android.widget.HwOnZoomEventListener
            public boolean onZoom(float value, MotionEvent event) {
                if (ImageView.this.mHwImageViewZoom == null) {
                    return false;
                }
                ImageView.this.mHwImageViewZoom.zoom(event, value);
                return true;
            }
        };
        initImageView();
    }

    public ImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mResource = 0;
        this.mHaveFrame = false;
        this.mAdjustViewBounds = false;
        this.mMaxWidth = Integer.MAX_VALUE;
        this.mMaxHeight = Integer.MAX_VALUE;
        this.mColorFilter = null;
        this.mHasColorFilter = false;
        this.mHasXfermode = false;
        this.mAlpha = 255;
        this.mHasAlpha = false;
        this.mViewAlphaScale = 256;
        this.mDrawable = null;
        this.mRecycleableBitmapDrawable = null;
        this.mDrawableTintList = null;
        this.mDrawableBlendMode = null;
        this.mHasDrawableTint = false;
        this.mHasDrawableBlendMode = false;
        this.mState = null;
        this.mMergeState = false;
        this.mLevel = 0;
        this.mDrawMatrix = null;
        this.mTempSrc = new RectF();
        this.mTempDst = new RectF();
        this.mBaseline = -1;
        this.mBaselineAlignBottom = false;
        this.mInBigView = -1;
        this.mHwCompoundEventDetector = null;
        this.mHwImageViewZoom = null;
        this.mOnZoomListener = new HwOnZoomEventListener() {
            /* class android.widget.ImageView.AnonymousClass1 */

            @Override // huawei.android.widget.HwOnZoomEventListener
            public boolean onZoom(float value, MotionEvent event) {
                if (ImageView.this.mHwImageViewZoom == null) {
                    return false;
                }
                ImageView.this.mHwImageViewZoom.zoom(event, value);
                return true;
            }
        };
        initImageView();
        if (getImportantForAutofill() == 0) {
            setImportantForAutofill(2);
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageView, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.ImageView, attrs, a, defStyleAttr, defStyleRes);
        Drawable d = a.getDrawable(0);
        if (d != null) {
            setImageDrawable(d);
        }
        this.mBaselineAlignBottom = a.getBoolean(6, false);
        this.mBaseline = a.getDimensionPixelSize(8, -1);
        setAdjustViewBounds(a.getBoolean(2, false));
        setMaxWidth(a.getDimensionPixelSize(3, Integer.MAX_VALUE));
        setMaxHeight(a.getDimensionPixelSize(4, Integer.MAX_VALUE));
        int index = a.getInt(1, -1);
        if (index >= 0) {
            setScaleType(sScaleTypeArray[index]);
        }
        if (a.hasValue(5)) {
            this.mDrawableTintList = a.getColorStateList(5);
            this.mHasDrawableTint = true;
            this.mDrawableBlendMode = BlendMode.SRC_ATOP;
            this.mHasDrawableBlendMode = true;
        }
        if (a.hasValue(9)) {
            this.mDrawableBlendMode = Drawable.parseBlendMode(a.getInt(9, -1), this.mDrawableBlendMode);
            this.mHasDrawableBlendMode = true;
        }
        applyImageTint();
        int alpha = a.getInt(10, 255);
        if (alpha != 255) {
            setImageAlpha(alpha);
        }
        this.mCropToPadding = a.getBoolean(7, false);
        a.recycle();
    }

    private void initImageView() {
        this.mMatrix = new Matrix();
        this.mScaleType = ScaleType.FIT_CENTER;
        if (!sCompatDone) {
            int targetSdkVersion = this.mContext.getApplicationInfo().targetSdkVersion;
            boolean z = false;
            sCompatAdjustViewBounds = targetSdkVersion <= 17;
            sCompatUseCorrectStreamDensity = targetSdkVersion > 23;
            if (targetSdkVersion < 24) {
                z = true;
            }
            sCompatDrawableVisibilityDispatch = z;
            sCompatDone = true;
        }
        this.mHwCompoundEventDetector = HwWidgetFactory.getCompoundEventDetector(this.mContext);
        this.mHwImageViewZoom = HwWidgetFactory.getImageViewZoom(this.mContext, this);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean verifyDrawable(Drawable dr) {
        return this.mDrawable == dr || super.verifyDrawable(dr);
    }

    @Override // android.view.View
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        Drawable drawable = this.mDrawable;
        if (drawable != null) {
            drawable.jumpToCurrentState();
        }
    }

    @Override // android.view.View, android.graphics.drawable.Drawable.Callback
    public void invalidateDrawable(Drawable dr) {
        if (dr == this.mDrawable) {
            if (dr != null) {
                int w = dr.getIntrinsicWidth();
                int h = dr.getIntrinsicHeight();
                if (!(w == this.mDrawableWidth && h == this.mDrawableHeight)) {
                    this.mDrawableWidth = w;
                    this.mDrawableHeight = h;
                    configureBounds();
                }
            }
            invalidate();
            return;
        }
        super.invalidateDrawable(dr);
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return (getBackground() == null || getBackground().getCurrent() == null) ? false : true;
    }

    @Override // android.view.View
    public void onPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        super.onPopulateAccessibilityEventInternal(event);
        CharSequence contentDescription = getContentDescription();
        if (!TextUtils.isEmpty(contentDescription)) {
            event.getText().add(contentDescription);
        }
    }

    public boolean getAdjustViewBounds() {
        return this.mAdjustViewBounds;
    }

    @RemotableViewMethod
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        this.mAdjustViewBounds = adjustViewBounds;
        if (adjustViewBounds) {
            setScaleType(ScaleType.FIT_CENTER);
        }
    }

    public int getMaxWidth() {
        return this.mMaxWidth;
    }

    @RemotableViewMethod
    public void setMaxWidth(int maxWidth) {
        this.mMaxWidth = maxWidth;
    }

    public int getMaxHeight() {
        return this.mMaxHeight;
    }

    @RemotableViewMethod
    public void setMaxHeight(int maxHeight) {
        this.mMaxHeight = maxHeight;
    }

    public Drawable getDrawable() {
        if (this.mDrawable == this.mRecycleableBitmapDrawable) {
            this.mRecycleableBitmapDrawable = null;
        }
        return this.mDrawable;
    }

    private class ImageDrawableCallback implements Runnable {
        private final Drawable drawable;
        private final int resource;
        private final Uri uri;

        ImageDrawableCallback(Drawable drawable2, Uri uri2, int resource2) {
            this.drawable = drawable2;
            this.uri = uri2;
            this.resource = resource2;
        }

        @Override // java.lang.Runnable
        public void run() {
            ImageView.this.setImageDrawable(this.drawable);
            ImageView.this.mUri = this.uri;
            ImageView.this.mResource = this.resource;
        }
    }

    @RemotableViewMethod(asyncImpl = "setImageResourceAsync")
    public void setImageResource(int resId) {
        int oldWidth = this.mDrawableWidth;
        int oldHeight = this.mDrawableHeight;
        updateDrawable(null);
        this.mResource = resId;
        this.mUri = null;
        resolveUri();
        if (!(oldWidth == this.mDrawableWidth && oldHeight == this.mDrawableHeight)) {
            requestLayout();
        }
        invalidate();
    }

    @UnsupportedAppUsage
    public Runnable setImageResourceAsync(int resId) {
        Drawable d = null;
        if (resId != 0) {
            try {
                d = getContext().getDrawable(resId);
            } catch (Exception e) {
                Log.w(LOG_TAG, "Unable to find resource: " + resId, e);
                resId = 0;
            }
        }
        return new ImageDrawableCallback(d, null, resId);
    }

    @RemotableViewMethod(asyncImpl = "setImageURIAsync")
    public void setImageURI(Uri uri) {
        if (this.mResource == 0) {
            Uri uri2 = this.mUri;
            if (uri2 == uri) {
                return;
            }
            if (!(uri == null || uri2 == null || !uri.equals(uri2))) {
                return;
            }
        }
        updateDrawable(null);
        this.mResource = 0;
        this.mUri = uri;
        int oldWidth = this.mDrawableWidth;
        int oldHeight = this.mDrawableHeight;
        resolveUri();
        if (!(oldWidth == this.mDrawableWidth && oldHeight == this.mDrawableHeight)) {
            requestLayout();
        }
        invalidate();
    }

    @UnsupportedAppUsage
    public Runnable setImageURIAsync(Uri uri) {
        Uri uri2;
        Drawable d = null;
        if (this.mResource == 0 && ((uri2 = this.mUri) == uri || (uri != null && uri2 != null && uri.equals(uri2)))) {
            return null;
        }
        if (uri != null) {
            d = getDrawableFromUri(uri);
        }
        if (d == null) {
            uri = null;
        }
        return new ImageDrawableCallback(d, uri, 0);
    }

    public void setImageDrawable(Drawable drawable) {
        if (this.mDrawable != drawable) {
            this.mResource = 0;
            this.mUri = null;
            int oldWidth = this.mDrawableWidth;
            int oldHeight = this.mDrawableHeight;
            updateDrawable(drawable);
            if (!(oldWidth == this.mDrawableWidth && oldHeight == this.mDrawableHeight)) {
                requestLayout();
            }
            invalidate();
        }
    }

    @RemotableViewMethod(asyncImpl = "setImageIconAsync")
    public void setImageIcon(Icon icon) {
        setImageDrawable(icon == null ? null : icon.loadDrawable(this.mContext));
    }

    public Runnable setImageIconAsync(Icon icon) {
        return new ImageDrawableCallback(icon == null ? null : icon.loadDrawable(this.mContext), null, 0);
    }

    public void setImageTintList(ColorStateList tint) {
        this.mDrawableTintList = tint;
        this.mHasDrawableTint = true;
        applyImageTint();
    }

    public ColorStateList getImageTintList() {
        return this.mDrawableTintList;
    }

    public void setImageTintMode(PorterDuff.Mode tintMode) {
        setImageTintBlendMode(tintMode != null ? BlendMode.fromValue(tintMode.nativeInt) : null);
    }

    public void setImageTintBlendMode(BlendMode blendMode) {
        this.mDrawableBlendMode = blendMode;
        this.mHasDrawableBlendMode = true;
        applyImageTint();
    }

    public PorterDuff.Mode getImageTintMode() {
        BlendMode blendMode = this.mDrawableBlendMode;
        if (blendMode != null) {
            return BlendMode.blendModeToPorterDuffMode(blendMode);
        }
        return null;
    }

    public BlendMode getImageTintBlendMode() {
        return this.mDrawableBlendMode;
    }

    private void applyImageTint() {
        if (this.mDrawable == null) {
            return;
        }
        if (this.mHasDrawableTint || this.mHasDrawableBlendMode) {
            this.mDrawable = this.mDrawable.mutate();
            if (this.mHasDrawableTint) {
                this.mDrawable.setTintList(this.mDrawableTintList);
            }
            if (this.mHasDrawableBlendMode) {
                this.mDrawable.setTintBlendMode(this.mDrawableBlendMode);
            }
            if (this.mDrawable.isStateful()) {
                this.mDrawable.setState(getDrawableState());
            }
        }
    }

    @RemotableViewMethod
    public void setImageBitmap(Bitmap bm) {
        this.mDrawable = null;
        if (!HwGalleryCacheManager.isGalleryCacheEffect() || !HwGalleryCacheManager.revertWechatThumb(this, bm)) {
            BitmapDrawable bitmapDrawable = this.mRecycleableBitmapDrawable;
            if (bitmapDrawable == null) {
                this.mRecycleableBitmapDrawable = new BitmapDrawable(this.mContext.getResources(), bm);
            } else {
                bitmapDrawable.setBitmap(bm);
            }
            setImageDrawable(this.mRecycleableBitmapDrawable);
        }
    }

    public void setImageState(int[] state, boolean merge) {
        this.mState = state;
        this.mMergeState = merge;
        if (this.mDrawable != null) {
            refreshDrawableState();
            resizeFromDrawable();
        }
    }

    @Override // android.view.View
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        resizeFromDrawable();
    }

    @RemotableViewMethod
    public void setImageLevel(int level) {
        this.mLevel = level;
        Drawable drawable = this.mDrawable;
        if (drawable != null) {
            drawable.setLevel(level);
            resizeFromDrawable();
        }
    }

    public enum ScaleType {
        MATRIX(0),
        FIT_XY(1),
        FIT_START(2),
        FIT_CENTER(3),
        FIT_END(4),
        CENTER(5),
        CENTER_CROP(6),
        CENTER_INSIDE(7);
        
        final int nativeInt;

        private ScaleType(int ni) {
            this.nativeInt = ni;
        }
    }

    public void setScaleType(ScaleType scaleType) {
        if (scaleType == null) {
            throw new NullPointerException();
        } else if (this.mScaleType != scaleType) {
            this.mScaleType = scaleType;
            requestLayout();
            invalidate();
        }
    }

    public ScaleType getScaleType() {
        return this.mScaleType;
    }

    public Matrix getImageMatrix() {
        Matrix matrix = this.mDrawMatrix;
        if (matrix == null) {
            return new Matrix(Matrix.IDENTITY_MATRIX);
        }
        return matrix;
    }

    public void setImageMatrix(Matrix matrix) {
        if (matrix != null && matrix.isIdentity()) {
            matrix = null;
        }
        if ((matrix == null && !this.mMatrix.isIdentity()) || (matrix != null && !this.mMatrix.equals(matrix))) {
            this.mMatrix.set(matrix);
            configureBounds();
            invalidate();
        }
    }

    public boolean getCropToPadding() {
        return this.mCropToPadding;
    }

    public void setCropToPadding(boolean cropToPadding) {
        if (this.mCropToPadding != cropToPadding) {
            this.mCropToPadding = cropToPadding;
            requestLayout();
            invalidate();
        }
    }

    @UnsupportedAppUsage
    private void resolveUri() {
        if (this.mDrawable == null && getResources() != null) {
            Drawable d = null;
            if (this.mResource != 0) {
                try {
                    d = this.mContext.getDrawable(this.mResource);
                } catch (Exception e) {
                    Log.w(LOG_TAG, "Unable to find resource: " + this.mResource, e);
                    this.mResource = 0;
                }
            } else {
                Uri uri = this.mUri;
                if (uri != null) {
                    d = getDrawableFromUri(uri);
                    if (d == null) {
                        Log.w(LOG_TAG, "resolveUri failed on bad bitmap uri: " + this.mUri);
                        this.mUri = null;
                    }
                } else {
                    return;
                }
            }
            updateDrawable(d);
        }
    }

    private Drawable getDrawableFromUri(Uri uri) {
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            try {
                ContentResolver.OpenResourceIdResult r = this.mContext.getContentResolver().getResourceId(uri);
                return r.r.getDrawable(r.id, this.mContext.getTheme());
            } catch (Exception e) {
                Log.w(LOG_TAG, "Unable to open content: " + uri, e);
                return null;
            }
        } else if (!"content".equals(scheme) && !ContentResolver.SCHEME_FILE.equals(scheme)) {
            return Drawable.createFromPath(uri.toString());
        } else {
            try {
                return ImageDecoder.decodeDrawable(ImageDecoder.createSource(this.mContext.getContentResolver(), uri, sCompatUseCorrectStreamDensity ? getResources() : null), $$Lambda$ImageView$GWf2ZLHjSbTbrFI3WzfR0LeM.INSTANCE);
            } catch (IOException e2) {
                Log.w(LOG_TAG, "Unable to open content: " + uri, e2);
                return null;
            }
        }
    }

    @Override // android.view.View
    public int[] onCreateDrawableState(int extraSpace) {
        int[] iArr = this.mState;
        if (iArr == null) {
            return super.onCreateDrawableState(extraSpace);
        }
        if (!this.mMergeState) {
            return iArr;
        }
        return mergeDrawableStates(super.onCreateDrawableState(iArr.length + extraSpace), this.mState);
    }

    @UnsupportedAppUsage
    private void updateDrawable(Drawable d) {
        BitmapDrawable bitmapDrawable = this.mRecycleableBitmapDrawable;
        if (!(d == bitmapDrawable || bitmapDrawable == null)) {
            bitmapDrawable.setBitmap(null);
        }
        boolean sameDrawable = false;
        Drawable drawable = this.mDrawable;
        boolean visible = false;
        if (drawable != null) {
            sameDrawable = drawable == d;
            this.mDrawable.setCallback(null);
            unscheduleDrawable(this.mDrawable);
            if (!sCompatDrawableVisibilityDispatch && !sameDrawable && isAttachedToWindow()) {
                this.mDrawable.setVisible(false, false);
            }
        }
        this.mDrawable = d;
        if (d != null) {
            d.setCallback(this);
            d.setLayoutDirection(getLayoutDirection());
            if (d.isStateful()) {
                d.setState(getDrawableState());
            }
            if (!sameDrawable || sCompatDrawableVisibilityDispatch) {
                if (sCompatDrawableVisibilityDispatch) {
                    if (getVisibility() == 0) {
                        visible = true;
                    }
                } else if (isAttachedToWindow() && getWindowVisibility() == 0 && isShown()) {
                    visible = true;
                }
                d.setVisible(visible, true);
            }
            d.setLevel(this.mLevel);
            this.mDrawableWidth = d.getIntrinsicWidth();
            this.mDrawableHeight = d.getIntrinsicHeight();
            applyImageTint();
            applyColorFilter();
            applyAlpha();
            applyXfermode();
            configureBounds();
            return;
        }
        this.mDrawableHeight = -1;
        this.mDrawableWidth = -1;
    }

    @UnsupportedAppUsage
    private void resizeFromDrawable() {
        Drawable d = this.mDrawable;
        if (d != null) {
            int w = d.getIntrinsicWidth();
            if (w < 0) {
                w = this.mDrawableWidth;
            }
            int h = d.getIntrinsicHeight();
            if (h < 0) {
                h = this.mDrawableHeight;
            }
            if (w != this.mDrawableWidth || h != this.mDrawableHeight) {
                this.mDrawableWidth = w;
                this.mDrawableHeight = h;
                requestLayout();
            }
        }
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        Drawable drawable = this.mDrawable;
        if (drawable != null) {
            drawable.setLayoutDirection(layoutDirection);
        }
    }

    @UnsupportedAppUsage
    private static Matrix.ScaleToFit scaleTypeToScaleToFit(ScaleType st) {
        return sS2FArray[st.nativeInt - 1];
    }

    /* JADX INFO: Multiple debug info for r8v4 'w'  int: [D('h' int), D('w' int)] */
    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int h;
        int h2;
        int widthSize;
        int heightSize;
        boolean done;
        resolveUri();
        float desiredAspect = 0.0f;
        boolean resizeWidth = false;
        boolean resizeHeight = false;
        int widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec);
        if (this.mDrawable == null) {
            this.mDrawableWidth = -1;
            this.mDrawableHeight = -1;
            h = 0;
            h2 = 0;
        } else {
            h2 = this.mDrawableWidth;
            h = this.mDrawableHeight;
            if (h2 <= 0) {
                h2 = 1;
            }
            if (h <= 0) {
                h = 1;
            }
            if (this.mAdjustViewBounds) {
                boolean z = true;
                resizeWidth = widthSpecMode != 1073741824;
                if (heightSpecMode == 1073741824) {
                    z = false;
                }
                resizeHeight = z;
                desiredAspect = ((float) h2) / ((float) h);
            }
        }
        int pleft = this.mPaddingLeft;
        int pright = this.mPaddingRight;
        int ptop = this.mPaddingTop;
        int pbottom = this.mPaddingBottom;
        if (resizeWidth || resizeHeight) {
            widthSize = resolveAdjustedSize(h2 + pleft + pright, this.mMaxWidth, widthMeasureSpec);
            heightSize = resolveAdjustedSize(h + ptop + pbottom, this.mMaxHeight, heightMeasureSpec);
            if (desiredAspect != 0.0f) {
                if (((double) Math.abs((((float) ((widthSize - pleft) - pright)) / ((float) ((heightSize - ptop) - pbottom))) - desiredAspect)) > 1.0E-7d) {
                    if (resizeWidth) {
                        int newWidth = ((int) (((float) ((heightSize - ptop) - pbottom)) * desiredAspect)) + pleft + pright;
                        if (resizeHeight || sCompatAdjustViewBounds) {
                            done = false;
                        } else {
                            done = false;
                            widthSize = resolveAdjustedSize(newWidth, this.mMaxWidth, widthMeasureSpec);
                        }
                        if (newWidth <= widthSize) {
                            widthSize = newWidth;
                            done = true;
                        }
                    } else {
                        done = false;
                    }
                    if (!done && resizeHeight) {
                        int newHeight = ((int) (((float) ((widthSize - pleft) - pright)) / desiredAspect)) + ptop + pbottom;
                        if (!resizeWidth && !sCompatAdjustViewBounds) {
                            heightSize = resolveAdjustedSize(newHeight, this.mMaxHeight, heightMeasureSpec);
                        }
                        if (newHeight <= heightSize) {
                            heightSize = newHeight;
                        }
                    }
                }
            }
        } else {
            int w = Math.max(h2 + pleft + pright, getSuggestedMinimumWidth());
            int h3 = Math.max(h + ptop + pbottom, getSuggestedMinimumHeight());
            widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
            heightSize = resolveSizeAndState(h3, heightMeasureSpec, 0);
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    private int resolveAdjustedSize(int desiredSize, int maxSize, int measureSpec) {
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);
        if (specMode == Integer.MIN_VALUE) {
            return Math.min(Math.min(desiredSize, specSize), maxSize);
        }
        if (specMode != 0) {
            return specMode != 1073741824 ? desiredSize : specSize;
        }
        return Math.min(desiredSize, maxSize);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        this.mHaveFrame = true;
        configureBounds();
        return changed;
    }

    private void configureBounds() {
        float scale;
        float scale2;
        if (this.mDrawable != null && this.mHaveFrame) {
            int dwidth = this.mDrawableWidth;
            int dheight = this.mDrawableHeight;
            int vwidth = (getWidth() - this.mPaddingLeft) - this.mPaddingRight;
            int vheight = (getHeight() - this.mPaddingTop) - this.mPaddingBottom;
            boolean fits = (dwidth < 0 || vwidth == dwidth) && (dheight < 0 || vheight == dheight);
            if (dwidth <= 0 || dheight <= 0 || ScaleType.FIT_XY == this.mScaleType) {
                this.mDrawable.setBounds(0, 0, vwidth, vheight);
                this.mDrawMatrix = null;
                return;
            }
            this.mDrawable.setBounds(0, 0, dwidth, dheight);
            if (ScaleType.MATRIX == this.mScaleType) {
                if (this.mMatrix.isIdentity()) {
                    this.mDrawMatrix = null;
                } else {
                    this.mDrawMatrix = this.mMatrix;
                }
            } else if (fits) {
                this.mDrawMatrix = null;
            } else if (ScaleType.CENTER == this.mScaleType) {
                this.mDrawMatrix = this.mMatrix;
                this.mDrawMatrix.setTranslate((float) Math.round(((float) (vwidth - dwidth)) * 0.5f), (float) Math.round(((float) (vheight - dheight)) * 0.5f));
            } else if (ScaleType.CENTER_CROP == this.mScaleType) {
                this.mDrawMatrix = this.mMatrix;
                float dx = 0.0f;
                float dy = 0.0f;
                if (dwidth * vheight > vwidth * dheight) {
                    scale2 = ((float) vheight) / ((float) dheight);
                    dx = (((float) vwidth) - (((float) dwidth) * scale2)) * 0.5f;
                } else {
                    scale2 = ((float) vwidth) / ((float) dwidth);
                    dy = (((float) vheight) - (((float) dheight) * scale2)) * 0.5f;
                }
                this.mDrawMatrix.setScale(scale2, scale2);
                this.mDrawMatrix.postTranslate((float) Math.round(dx), (float) Math.round(dy));
            } else if (ScaleType.CENTER_INSIDE == this.mScaleType) {
                this.mDrawMatrix = this.mMatrix;
                if (dwidth > vwidth || dheight > vheight) {
                    scale = Math.min(((float) vwidth) / ((float) dwidth), ((float) vheight) / ((float) dheight));
                } else {
                    scale = 1.0f;
                }
                this.mDrawMatrix.setScale(scale, scale);
                this.mDrawMatrix.postTranslate((float) Math.round((((float) vwidth) - (((float) dwidth) * scale)) * 0.5f), (float) Math.round((((float) vheight) - (((float) dheight) * scale)) * 0.5f));
            } else {
                this.mTempSrc.set(0.0f, 0.0f, (float) dwidth, (float) dheight);
                this.mTempDst.set(0.0f, 0.0f, (float) vwidth, (float) vheight);
                this.mDrawMatrix = this.mMatrix;
                this.mDrawMatrix.setRectToRect(this.mTempSrc, this.mTempDst, scaleTypeToScaleToFit(this.mScaleType));
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable drawable = this.mDrawable;
        if (drawable != null && drawable.isStateful() && drawable.setState(getDrawableState())) {
            invalidateDrawable(drawable);
        }
    }

    @Override // android.view.View
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        Drawable drawable = this.mDrawable;
        if (drawable != null) {
            drawable.setHotspot(x, y);
        }
    }

    public void animateTransform(Matrix matrix) {
        Drawable drawable = this.mDrawable;
        if (drawable != null) {
            if (matrix == null) {
                this.mDrawable.setBounds(0, 0, (getWidth() - this.mPaddingLeft) - this.mPaddingRight, (getHeight() - this.mPaddingTop) - this.mPaddingBottom);
                this.mDrawMatrix = null;
            } else {
                drawable.setBounds(0, 0, this.mDrawableWidth, this.mDrawableHeight);
                if (this.mDrawMatrix == null) {
                    this.mDrawMatrix = new Matrix();
                }
                this.mDrawMatrix.set(matrix);
            }
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mDrawable != null && this.mDrawableWidth != 0 && this.mDrawableHeight != 0) {
            if (this.mDrawMatrix == null && this.mPaddingTop == 0 && this.mPaddingLeft == 0) {
                this.mDrawable.draw(canvas);
                return;
            }
            int saveCount = canvas.getSaveCount();
            canvas.save();
            if (this.mCropToPadding) {
                int scrollX = this.mScrollX;
                int scrollY = this.mScrollY;
                canvas.clipRect(this.mPaddingLeft + scrollX, this.mPaddingTop + scrollY, ((this.mRight + scrollX) - this.mLeft) - this.mPaddingRight, ((this.mBottom + scrollY) - this.mTop) - this.mPaddingBottom);
            }
            canvas.translate((float) this.mPaddingLeft, (float) this.mPaddingTop);
            Matrix matrix = this.mDrawMatrix;
            if (matrix != null) {
                canvas.concat(matrix);
            }
            this.mDrawable.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    @Override // android.view.View
    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
    public int getBaseline() {
        if (this.mBaselineAlignBottom) {
            return getMeasuredHeight();
        }
        return this.mBaseline;
    }

    public void setBaseline(int baseline) {
        if (this.mBaseline != baseline) {
            this.mBaseline = baseline;
            requestLayout();
        }
    }

    public void setBaselineAlignBottom(boolean aligned) {
        if (this.mBaselineAlignBottom != aligned) {
            this.mBaselineAlignBottom = aligned;
            requestLayout();
        }
    }

    public boolean getBaselineAlignBottom() {
        return this.mBaselineAlignBottom;
    }

    public final void setColorFilter(int color, PorterDuff.Mode mode) {
        setColorFilter(new PorterDuffColorFilter(color, mode));
    }

    @RemotableViewMethod
    public final void setColorFilter(int color) {
        setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public final void clearColorFilter() {
        setColorFilter((ColorFilter) null);
    }

    public final void setXfermode(Xfermode mode) {
        if (this.mXfermode != mode) {
            this.mXfermode = mode;
            this.mHasXfermode = true;
            applyXfermode();
            invalidate();
        }
    }

    public ColorFilter getColorFilter() {
        return this.mColorFilter;
    }

    public void setColorFilter(ColorFilter cf) {
        if (this.mColorFilter != cf) {
            this.mColorFilter = cf;
            this.mHasColorFilter = true;
            applyColorFilter();
            invalidate();
        }
    }

    public int getImageAlpha() {
        return this.mAlpha;
    }

    @RemotableViewMethod
    public void setImageAlpha(int alpha) {
        setAlpha(alpha);
    }

    @RemotableViewMethod
    @Deprecated
    public void setAlpha(int alpha) {
        int alpha2 = alpha & 255;
        if (this.mAlpha != alpha2) {
            this.mAlpha = alpha2;
            this.mHasAlpha = true;
            applyAlpha();
            invalidate();
        }
    }

    private void applyXfermode() {
        Drawable drawable = this.mDrawable;
        if (drawable != null && this.mHasXfermode) {
            this.mDrawable = drawable.mutate();
            this.mDrawable.setXfermode(this.mXfermode);
        }
    }

    private void applyColorFilter() {
        Drawable drawable = this.mDrawable;
        if (drawable != null && this.mHasColorFilter) {
            this.mDrawable = drawable.mutate();
            this.mDrawable.setColorFilter(this.mColorFilter);
        }
    }

    private void applyAlpha() {
        Drawable drawable = this.mDrawable;
        if (drawable != null && this.mHasAlpha) {
            this.mDrawable = drawable.mutate();
            this.mDrawable.setAlpha((this.mAlpha * 256) >> 8);
        }
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        HwCompoundEventDetector hwCompoundEventDetector = this.mHwCompoundEventDetector;
        if (hwCompoundEventDetector == null || !hwCompoundEventDetector.onKeyEvent(keyCode, event)) {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        HwCompoundEventDetector hwCompoundEventDetector = this.mHwCompoundEventDetector;
        if (hwCompoundEventDetector == null || !hwCompoundEventDetector.onKeyEvent(keyCode, event)) {
            return super.onKeyUp(keyCode, event);
        }
        return true;
    }

    @Override // android.view.View
    public boolean onGenericMotionEvent(MotionEvent event) {
        HwCompoundEventDetector hwCompoundEventDetector = this.mHwCompoundEventDetector;
        if (hwCompoundEventDetector == null || !hwCompoundEventDetector.onGenericMotionEvent(event)) {
            return super.onGenericMotionEvent(event);
        }
        return true;
    }

    public void setOnZoomEnabled(boolean isEnabled) {
        HwCompoundEventDetector hwCompoundEventDetector = this.mHwCompoundEventDetector;
        if (hwCompoundEventDetector != null) {
            if (isEnabled) {
                hwCompoundEventDetector.setOnZoomEventListener(this, this.mOnZoomListener);
            } else {
                hwCompoundEventDetector.setOnZoomEventListener(this, null);
            }
        }
    }

    public boolean isOnZoomEnabled() {
        HwCompoundEventDetector hwCompoundEventDetector = this.mHwCompoundEventDetector;
        if (hwCompoundEventDetector == null || hwCompoundEventDetector.getOnZoomEventListener() == null) {
            return false;
        }
        return true;
    }

    public void setOnZoomListener(HwOnZoomEventListener listener) {
        HwCompoundEventDetector hwCompoundEventDetector = this.mHwCompoundEventDetector;
        if (hwCompoundEventDetector != null) {
            this.mOnZoomListener = listener;
            hwCompoundEventDetector.setOnZoomEventListener(this, this.mOnZoomListener);
        }
    }

    public HwOnZoomEventListener getOnZoomListener() {
        return this.mOnZoomListener;
    }

    @Override // android.view.View
    public boolean isOpaque() {
        Drawable drawable;
        return super.isOpaque() || ((drawable = this.mDrawable) != null && this.mXfermode == null && drawable.getOpacity() == -1 && ((this.mAlpha * 256) >> 8) == 255 && isFilledByImage());
    }

    private boolean isFilledByImage() {
        Drawable drawable = this.mDrawable;
        if (drawable == null) {
            return false;
        }
        Rect bounds = drawable.getBounds();
        Matrix matrix = this.mDrawMatrix;
        if (matrix == null) {
            if (bounds.left > 0 || bounds.top > 0 || bounds.right < getWidth() || bounds.bottom < getHeight()) {
                return false;
            }
            return true;
        } else if (!matrix.rectStaysRect()) {
            return false;
        } else {
            RectF boundsSrc = this.mTempSrc;
            RectF boundsDst = this.mTempDst;
            boundsSrc.set(bounds);
            matrix.mapRect(boundsDst, boundsSrc);
            if (boundsDst.left > 0.0f || boundsDst.top > 0.0f || boundsDst.right < ((float) getWidth()) || boundsDst.bottom < ((float) getHeight())) {
                return false;
            }
            return true;
        }
    }

    @Override // android.view.View
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        Drawable drawable = this.mDrawable;
        if (drawable != null && !sCompatDrawableVisibilityDispatch) {
            drawable.setVisible(isVisible, false);
        }
    }

    @Override // android.view.View
    @RemotableViewMethod
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        Drawable drawable = this.mDrawable;
        if (drawable != null && sCompatDrawableVisibilityDispatch) {
            drawable.setVisible(visibility == 0, false);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Drawable drawable = this.mDrawable;
        if (drawable != null && sCompatDrawableVisibilityDispatch) {
            drawable.setVisible(getVisibility() == 0, false);
        }
        setOnZoomEnabled(isOnZoomEnabled());
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Drawable drawable = this.mDrawable;
        if (drawable != null && sCompatDrawableVisibilityDispatch) {
            drawable.setVisible(false, false);
        }
        HwCompoundEventDetector hwCompoundEventDetector = this.mHwCompoundEventDetector;
        if (hwCompoundEventDetector != null) {
            hwCompoundEventDetector.onDetachedFromWindow();
        }
    }

    @Override // android.view.View
    public CharSequence getAccessibilityClassName() {
        return ImageView.class.getName();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void encodeProperties(ViewHierarchyEncoder stream) {
        super.encodeProperties(stream);
        stream.addProperty("layout:baseline", getBaseline());
    }

    @Override // android.view.View
    public boolean isDefaultFocusHighlightNeeded(Drawable background, Drawable foreground) {
        Drawable drawable = this.mDrawable;
        return super.isDefaultFocusHighlightNeeded(background, foreground) && (drawable == null || !drawable.isStateful() || !this.mDrawable.hasFocusStateSpecified());
    }
}
