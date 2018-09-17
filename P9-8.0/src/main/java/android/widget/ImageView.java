package android.widget;

import android.content.ContentResolver.OpenResourceIdResult;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hwgallerycache.HwGalleryCacheManager;
import android.net.Uri;
import android.service.voice.VoiceInteractionSession;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewHierarchyEncoder;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews.RemoteView;
import android.widget.sr.HwAISRImageViewTaskManager;
import android.widget.sr.HwAISRImageViewTaskManager.SRTaskCallback;
import android.widget.sr.HwAISRImageViewTaskManager.SRTaskInfo;
import android.widget.sr.SRInfo;
import android.widget.sr.SRUtils;
import com.android.internal.R;
import java.io.IOException;
import java.io.InputStream;

@RemoteView
public class ImageView extends View implements SRTaskCallback {
    private static final String LOG_TAG = "ImageView";
    private static final String SR_TAG = "SuperResolution";
    private static boolean sCompatAdjustViewBounds;
    private static boolean sCompatDone;
    private static boolean sCompatDrawableVisibilityDispatch;
    private static boolean sCompatUseCorrectStreamDensity;
    private static final ScaleToFit[] sS2FArray = new ScaleToFit[]{ScaleToFit.FILL, ScaleToFit.START, ScaleToFit.CENTER, ScaleToFit.END};
    private static final ScaleType[] sScaleTypeArray = new ScaleType[]{ScaleType.MATRIX, ScaleType.FIT_XY, ScaleType.FIT_START, ScaleType.FIT_CENTER, ScaleType.FIT_END, ScaleType.CENTER, ScaleType.CENTER_CROP, ScaleType.CENTER_INSIDE};
    private boolean mAdjustViewBounds;
    private int mAlpha;
    private int mBaseline;
    private boolean mBaselineAlignBottom;
    private ColorFilter mColorFilter;
    private boolean mColorMod;
    private boolean mCropToPadding;
    private Matrix mDrawMatrix;
    private Drawable mDrawable;
    private int mDrawableHeight;
    private ColorStateList mDrawableTintList;
    private Mode mDrawableTintMode;
    private int mDrawableWidth;
    private boolean mHasColorFilter;
    private boolean mHasDrawableTint;
    private boolean mHasDrawableTintMode;
    private boolean mHaveFrame;
    public int mInBigView;
    private int mLevel;
    private Matrix mMatrix;
    private int mMaxHeight;
    private int mMaxWidth;
    private boolean mMergeState;
    private BitmapDrawable mRecycleableBitmapDrawable;
    private int mResource;
    private SRInfo mSRInfo;
    private ScaleType mScaleType;
    private int[] mState;
    private final RectF mTempDst;
    private final RectF mTempSrc;
    private Uri mUri;
    private final int mViewAlphaScale;
    private Xfermode mXfermode;

    private class ImageDrawableCallback implements Runnable {
        private final Drawable drawable;
        private final int resource;
        private final Uri uri;

        ImageDrawableCallback(Drawable drawable, Uri uri, int resource) {
            this.drawable = drawable;
            this.uri = uri;
            this.resource = resource;
        }

        public void run() {
            ImageView.this.setImageDrawable(this.drawable);
            ImageView.this.mUri = this.uri;
            ImageView.this.mResource = this.resource;
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

    public ImageView(Context context) {
        super(context);
        this.mResource = 0;
        this.mHaveFrame = false;
        this.mAdjustViewBounds = false;
        this.mMaxWidth = Integer.MAX_VALUE;
        this.mMaxHeight = Integer.MAX_VALUE;
        this.mColorFilter = null;
        this.mHasColorFilter = false;
        this.mAlpha = 255;
        this.mViewAlphaScale = 256;
        this.mColorMod = false;
        this.mDrawable = null;
        this.mRecycleableBitmapDrawable = null;
        this.mDrawableTintList = null;
        this.mDrawableTintMode = null;
        this.mHasDrawableTint = false;
        this.mHasDrawableTintMode = false;
        this.mState = null;
        this.mMergeState = false;
        this.mLevel = 0;
        this.mDrawMatrix = null;
        this.mTempSrc = new RectF();
        this.mTempDst = new RectF();
        this.mBaseline = -1;
        this.mBaselineAlignBottom = false;
        this.mInBigView = -1;
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
        this.mAlpha = 255;
        this.mViewAlphaScale = 256;
        this.mColorMod = false;
        this.mDrawable = null;
        this.mRecycleableBitmapDrawable = null;
        this.mDrawableTintList = null;
        this.mDrawableTintMode = null;
        this.mHasDrawableTint = false;
        this.mHasDrawableTintMode = false;
        this.mState = null;
        this.mMergeState = false;
        this.mLevel = 0;
        this.mDrawMatrix = null;
        this.mTempSrc = new RectF();
        this.mTempDst = new RectF();
        this.mBaseline = -1;
        this.mBaselineAlignBottom = false;
        this.mInBigView = -1;
        initImageView();
        if (getImportantForAutofill() == 0) {
            setImportantForAutofill(2);
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageView, defStyleAttr, defStyleRes);
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
            this.mDrawableTintMode = Mode.SRC_ATOP;
            this.mHasDrawableTintMode = true;
        }
        if (a.hasValue(9)) {
            this.mDrawableTintMode = Drawable.parseTintMode(a.getInt(9, -1), this.mDrawableTintMode);
            this.mHasDrawableTintMode = true;
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
        boolean z = false;
        this.mMatrix = new Matrix();
        this.mScaleType = ScaleType.FIT_CENTER;
        if (!sCompatDone) {
            boolean z2;
            int targetSdkVersion = this.mContext.getApplicationInfo().targetSdkVersion;
            if (targetSdkVersion <= 17) {
                z2 = true;
            } else {
                z2 = false;
            }
            sCompatAdjustViewBounds = z2;
            if (targetSdkVersion > 23) {
                z2 = true;
            } else {
                z2 = false;
            }
            sCompatUseCorrectStreamDensity = z2;
            if (targetSdkVersion < 24) {
                z = true;
            }
            sCompatDrawableVisibilityDispatch = z;
            sCompatDone = true;
        }
        if (HwAISRImageViewTaskManager.isSuperResolutionAvailable() && SRUtils.checkIsInSRWhiteList(getContext())) {
            this.mSRInfo = new SRInfo();
            this.mSRInfo.setIsInWhiteList(true);
        }
    }

    protected boolean verifyDrawable(Drawable dr) {
        return this.mDrawable != dr ? super.verifyDrawable(dr) : true;
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mDrawable != null) {
            this.mDrawable.jumpToCurrentState();
        }
    }

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
            if (this.mSRInfo != null) {
                if (this.mSRInfo.getStatus() == 1) {
                    Log.d(SR_TAG, "invalidateDrawable: increase invalidateDrawableCount");
                    this.mSRInfo.increaseInvalidateDrawableCount();
                } else if (this.mSRInfo.getStatus() == 2) {
                    clearSRInfoWithStatus(3);
                }
            }
            invalidate();
            return;
        }
        super.invalidateDrawable(dr);
    }

    public boolean hasOverlappingRendering() {
        return (getBackground() == null || getBackground().getCurrent() == null) ? false : true;
    }

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
            if (this.mUri == uri) {
                return;
            }
            if (!(uri == null || this.mUri == null || (uri.equals(this.mUri) ^ 1) != 0)) {
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

    public Runnable setImageURIAsync(Uri uri) {
        if (this.mResource == 0 && (this.mUri == uri || (uri != null && this.mUri != null && (uri.equals(this.mUri) ^ 1) == 0))) {
            return null;
        }
        Drawable d = uri == null ? null : getDrawableFromUri(uri);
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
        Drawable drawable = null;
        if (icon != null) {
            drawable = icon.loadDrawable(this.mContext);
        }
        setImageDrawable(drawable);
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

    public void setImageTintMode(Mode tintMode) {
        this.mDrawableTintMode = tintMode;
        this.mHasDrawableTintMode = true;
        applyImageTint();
    }

    public Mode getImageTintMode() {
        return this.mDrawableTintMode;
    }

    private void applyImageTint() {
        if (this.mDrawable == null) {
            return;
        }
        if (this.mHasDrawableTint || this.mHasDrawableTintMode) {
            this.mDrawable = this.mDrawable.mutate();
            if (this.mHasDrawableTint) {
                this.mDrawable.setTintList(this.mDrawableTintList);
            }
            if (this.mHasDrawableTintMode) {
                this.mDrawable.setTintMode(this.mDrawableTintMode);
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
            if (this.mRecycleableBitmapDrawable == null) {
                this.mRecycleableBitmapDrawable = new BitmapDrawable(this.mContext.getResources(), bm);
            } else {
                this.mRecycleableBitmapDrawable.setBitmap(bm);
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

    public void setSelected(boolean selected) {
        super.setSelected(selected);
        resizeFromDrawable();
    }

    @RemotableViewMethod
    public void setImageLevel(int level) {
        this.mLevel = level;
        if (this.mDrawable != null) {
            this.mDrawable.setLevel(level);
            resizeFromDrawable();
        }
    }

    public void setScaleType(ScaleType scaleType) {
        if (scaleType == null) {
            throw new NullPointerException();
        } else if (this.mScaleType != scaleType) {
            this.mScaleType = scaleType;
            setWillNotCacheDrawing(this.mScaleType == ScaleType.CENTER);
            requestLayout();
            invalidate();
        }
    }

    public ScaleType getScaleType() {
        return this.mScaleType;
    }

    public Matrix getImageMatrix() {
        if (this.mDrawMatrix == null) {
            return new Matrix(Matrix.IDENTITY_MATRIX);
        }
        return this.mDrawMatrix;
    }

    public void setImageMatrix(Matrix matrix) {
        if (matrix != null && matrix.isIdentity()) {
            matrix = null;
        }
        if ((matrix == null && (this.mMatrix.isIdentity() ^ 1) != 0) || (matrix != null && (this.mMatrix.equals(matrix) ^ 1) != 0)) {
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
            } else if (this.mUri != null) {
                d = getDrawableFromUri(this.mUri);
                if (d == null) {
                    Log.w(LOG_TAG, "resolveUri failed on bad bitmap uri: " + this.mUri);
                    this.mUri = null;
                }
            } else {
                return;
            }
            updateDrawable(d);
        }
    }

    private Drawable getDrawableFromUri(Uri uri) {
        String scheme = uri.getScheme();
        if ("android.resource".equals(scheme)) {
            try {
                OpenResourceIdResult r = this.mContext.getContentResolver().getResourceId(uri);
                return r.r.getDrawable(r.id, this.mContext.getTheme());
            } catch (Exception e) {
                Log.w(LOG_TAG, "Unable to open content: " + uri, e);
            }
        } else if (!VoiceInteractionSession.KEY_CONTENT.equals(scheme) && !"file".equals(scheme)) {
            return Drawable.createFromPath(uri.toString());
        } else {
            InputStream inputStream = null;
            try {
                Resources resources;
                inputStream = this.mContext.getContentResolver().openInputStream(uri);
                if (sCompatUseCorrectStreamDensity) {
                    resources = getResources();
                } else {
                    resources = null;
                }
                Drawable createFromResourceStream = Drawable.createFromResourceStream(resources, null, inputStream, null);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e2) {
                        Log.w(LOG_TAG, "Unable to close content: " + uri, e2);
                    }
                }
                return createFromResourceStream;
            } catch (Exception e3) {
                Log.w(LOG_TAG, "Unable to open content: " + uri, e3);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e22) {
                        Log.w(LOG_TAG, "Unable to close content: " + uri, e22);
                    }
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e222) {
                        Log.w(LOG_TAG, "Unable to close content: " + uri, e222);
                    }
                }
            }
        }
        return null;
    }

    public int[] onCreateDrawableState(int extraSpace) {
        if (this.mState == null) {
            return super.onCreateDrawableState(extraSpace);
        }
        if (this.mMergeState) {
            return View.mergeDrawableStates(super.onCreateDrawableState(this.mState.length + extraSpace), this.mState);
        }
        return this.mState;
    }

    private void updateDrawable(Drawable d) {
        if (!(d == this.mRecycleableBitmapDrawable || this.mRecycleableBitmapDrawable == null)) {
            this.mRecycleableBitmapDrawable.setBitmap(null);
        }
        boolean sameDrawable = false;
        if (this.mDrawable != null) {
            sameDrawable = this.mDrawable == d;
            this.mDrawable.setCallback(null);
            unscheduleDrawable(this.mDrawable);
            if (!(sCompatDrawableVisibilityDispatch || (sameDrawable ^ 1) == 0 || !isAttachedToWindow())) {
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
                boolean visible = sCompatDrawableVisibilityDispatch ? getVisibility() == 0 : (isAttachedToWindow() && getWindowVisibility() == 0) ? isShown() : false;
                d.setVisible(visible, true);
            }
            d.setLevel(this.mLevel);
            this.mDrawableWidth = d.getIntrinsicWidth();
            this.mDrawableHeight = d.getIntrinsicHeight();
            applyImageTint();
            applyColorMod();
            configureBounds();
        } else {
            this.mDrawableHeight = -1;
            this.mDrawableWidth = -1;
        }
        if (this.mSRInfo != null) {
            resetSRInfo();
        }
    }

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

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        if (this.mDrawable != null) {
            this.mDrawable.setLayoutDirection(layoutDirection);
        }
    }

    private static ScaleToFit scaleTypeToScaleToFit(ScaleType st) {
        return sS2FArray[st.nativeInt - 1];
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int h;
        int w;
        int widthSize;
        int heightSize;
        resolveUri();
        float desiredAspect = 0.0f;
        boolean resizeWidth = false;
        boolean resizeHeight = false;
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        if (this.mDrawable == null) {
            this.mDrawableWidth = -1;
            this.mDrawableHeight = -1;
            h = 0;
            w = 0;
        } else {
            w = this.mDrawableWidth;
            h = this.mDrawableHeight;
            if (w <= 0) {
                w = 1;
            }
            if (h <= 0) {
                h = 1;
            }
            if (this.mAdjustViewBounds) {
                resizeWidth = widthSpecMode != 1073741824;
                resizeHeight = heightSpecMode != 1073741824;
                desiredAspect = ((float) w) / ((float) h);
            }
        }
        int pleft = this.mPaddingLeft;
        int pright = this.mPaddingRight;
        int ptop = this.mPaddingTop;
        int pbottom = this.mPaddingBottom;
        if (resizeWidth || resizeHeight) {
            widthSize = resolveAdjustedSize((w + pleft) + pright, this.mMaxWidth, widthMeasureSpec);
            heightSize = resolveAdjustedSize((h + ptop) + pbottom, this.mMaxHeight, heightMeasureSpec);
            if (desiredAspect != 0.0f && ((double) Math.abs((((float) ((widthSize - pleft) - pright)) / ((float) ((heightSize - ptop) - pbottom))) - desiredAspect)) > 1.0E-7d) {
                boolean done = false;
                if (resizeWidth) {
                    int newWidth = (((int) (((float) ((heightSize - ptop) - pbottom)) * desiredAspect)) + pleft) + pright;
                    if (!(resizeHeight || (sCompatAdjustViewBounds ^ 1) == 0)) {
                        widthSize = resolveAdjustedSize(newWidth, this.mMaxWidth, widthMeasureSpec);
                    }
                    if (newWidth <= widthSize) {
                        widthSize = newWidth;
                        done = true;
                    }
                }
                if (!done && resizeHeight) {
                    int newHeight = (((int) (((float) ((widthSize - pleft) - pright)) / desiredAspect)) + ptop) + pbottom;
                    if (!(resizeWidth || (sCompatAdjustViewBounds ^ 1) == 0)) {
                        heightSize = resolveAdjustedSize(newHeight, this.mMaxHeight, heightMeasureSpec);
                    }
                    if (newHeight <= heightSize) {
                        heightSize = newHeight;
                    }
                }
            }
        } else {
            h += ptop + pbottom;
            w = Math.max(w + (pleft + pright), getSuggestedMinimumWidth());
            h = Math.max(h, getSuggestedMinimumHeight());
            widthSize = View.resolveSizeAndState(w, widthMeasureSpec, 0);
            heightSize = View.resolveSizeAndState(h, heightMeasureSpec, 0);
        }
        -wrap6(widthSize, heightSize);
    }

    private int resolveAdjustedSize(int desiredSize, int maxSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case Integer.MIN_VALUE:
                return Math.min(Math.min(desiredSize, specSize), maxSize);
            case 0:
                return Math.min(desiredSize, maxSize);
            case 1073741824:
                return specSize;
            default:
                return result;
        }
    }

    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.-wrap13(l, t, r, b);
        this.mHaveFrame = true;
        configureBounds();
        return changed;
    }

    private void configureBounds() {
        if (this.mDrawable != null && (this.mHaveFrame ^ 1) == 0) {
            int dwidth = this.mDrawableWidth;
            int dheight = this.mDrawableHeight;
            int vwidth = (getWidth() - this.mPaddingLeft) - this.mPaddingRight;
            int vheight = (getHeight() - this.mPaddingTop) - this.mPaddingBottom;
            boolean fits = (dwidth < 0 || vwidth == dwidth) ? dheight < 0 || vheight == dheight : false;
            if (dwidth <= 0 || dheight <= 0 || ScaleType.FIT_XY == this.mScaleType) {
                this.mDrawable.setBounds(0, 0, vwidth, vheight);
                this.mDrawMatrix = null;
            } else {
                this.mDrawable.setBounds(0, 0, dwidth, dheight);
                float dx;
                float dy;
                float scale;
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
                    dx = 0.0f;
                    dy = 0.0f;
                    if (dwidth * vheight > vwidth * dheight) {
                        scale = ((float) vheight) / ((float) dheight);
                        dx = (((float) vwidth) - (((float) dwidth) * scale)) * 0.5f;
                    } else {
                        scale = ((float) vwidth) / ((float) dwidth);
                        dy = (((float) vheight) - (((float) dheight) * scale)) * 0.5f;
                    }
                    this.mDrawMatrix.setScale(scale, scale);
                    this.mDrawMatrix.postTranslate((float) Math.round(dx), (float) Math.round(dy));
                } else if (ScaleType.CENTER_INSIDE == this.mScaleType) {
                    this.mDrawMatrix = this.mMatrix;
                    if (dwidth > vwidth || dheight > vheight) {
                        scale = Math.min(((float) vwidth) / ((float) dwidth), ((float) vheight) / ((float) dheight));
                    } else {
                        scale = 1.0f;
                    }
                    dx = (float) Math.round((((float) vwidth) - (((float) dwidth) * scale)) * 0.5f);
                    dy = (float) Math.round((((float) vheight) - (((float) dheight) * scale)) * 0.5f);
                    this.mDrawMatrix.setScale(scale, scale);
                    this.mDrawMatrix.postTranslate(dx, dy);
                } else {
                    this.mTempSrc.set(0.0f, 0.0f, (float) dwidth, (float) dheight);
                    this.mTempDst.set(0.0f, 0.0f, (float) vwidth, (float) vheight);
                    this.mDrawMatrix = this.mMatrix;
                    this.mDrawMatrix.setRectToRect(this.mTempSrc, this.mTempDst, scaleTypeToScaleToFit(this.mScaleType));
                }
            }
        }
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable drawable = this.mDrawable;
        if (drawable != null && drawable.isStateful() && drawable.setState(getDrawableState())) {
            invalidateDrawable(drawable);
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (this.mDrawable != null) {
            this.mDrawable.setHotspot(x, y);
        }
    }

    public void animateTransform(Matrix matrix) {
        if (this.mDrawable != null) {
            if (matrix == null) {
                this.mDrawable.setBounds(0, 0, getWidth(), getHeight());
            } else {
                this.mDrawable.setBounds(0, 0, this.mDrawableWidth, this.mDrawableHeight);
                if (this.mDrawMatrix == null) {
                    this.mDrawMatrix = new Matrix();
                }
                this.mDrawMatrix.set(matrix);
            }
            invalidate();
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mDrawable != null && this.mDrawableWidth != 0 && this.mDrawableHeight != 0) {
            if (this.mSRInfo != null && getDrawable() != null && this.mSRInfo.getIsInWhiteList() && this.mSRInfo.getIsFullScreen() && this.mSRInfo.getStatus() == 0) {
                tryToPostNewTaskToSRManager();
            }
            if (!(this.mSRInfo == null || this.mSRInfo.getSRDrawable() == null || this.mSRInfo.getSrcDrawable() == getDrawable())) {
                Log.w(SR_TAG, "onDraw:  not match this drawable, some errors happened");
                clearSRInfoWithStatus(3);
            }
            boolean drawSRDrawable = (this.mSRInfo == null || this.mSRInfo.getSRDrawable() == null) ? false : this.mSRInfo.getSrcDrawable() == getDrawable();
            if (this.mDrawMatrix != null || this.mPaddingTop != 0 || this.mPaddingLeft != 0) {
                int saveCount = canvas.getSaveCount();
                canvas.save();
                if (this.mCropToPadding) {
                    int scrollX = this.mScrollX;
                    int scrollY = this.mScrollY;
                    canvas.clipRect(this.mPaddingLeft + scrollX, this.mPaddingTop + scrollY, ((this.mRight + scrollX) - this.mLeft) - this.mPaddingRight, ((this.mBottom + scrollY) - this.mTop) - this.mPaddingBottom);
                }
                canvas.translate((float) this.mPaddingLeft, (float) this.mPaddingTop);
                if (drawSRDrawable) {
                    Matrix matrix = new Matrix();
                    matrix.setScale(1.0f / this.mSRInfo.getScaleX(), 1.0f / this.mSRInfo.getScaleY());
                    matrix.postConcat(getImageMatrix());
                    canvas.concat(matrix);
                    this.mSRInfo.getSRDrawable().draw(canvas);
                } else {
                    if (this.mDrawMatrix != null) {
                        canvas.concat(this.mDrawMatrix);
                    }
                    this.mDrawable.draw(canvas);
                }
                canvas.restoreToCount(saveCount);
            } else if (drawSRDrawable) {
                this.mSRInfo.getSRDrawable().setBounds(getDrawable().getBounds());
                this.mSRInfo.getSRDrawable().draw(canvas);
            } else {
                this.mDrawable.draw(canvas);
            }
        }
    }

    @ExportedProperty(category = "layout")
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

    public final void setColorFilter(int color, Mode mode) {
        setColorFilter(new PorterDuffColorFilter(color, mode));
    }

    @RemotableViewMethod
    public final void setColorFilter(int color) {
        setColorFilter(color, Mode.SRC_ATOP);
    }

    public final void clearColorFilter() {
        setColorFilter(null);
    }

    public final void setXfermode(Xfermode mode) {
        if (this.mXfermode != mode) {
            this.mXfermode = mode;
            this.mColorMod = true;
            applyColorMod();
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
            this.mColorMod = true;
            applyColorMod();
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
        alpha &= 255;
        if (this.mAlpha != alpha) {
            this.mAlpha = alpha;
            this.mColorMod = true;
            applyColorMod();
            invalidate();
        }
    }

    private void applyColorMod() {
        if (this.mDrawable != null && this.mColorMod) {
            this.mDrawable = this.mDrawable.mutate();
            if (this.mHasColorFilter) {
                this.mDrawable.setColorFilter(this.mColorFilter);
            }
            this.mDrawable.setXfermode(this.mXfermode);
            this.mDrawable.setAlpha((this.mAlpha * 256) >> 8);
        }
    }

    public boolean isOpaque() {
        if (super.isOpaque()) {
            return true;
        }
        if (this.mDrawable != null && this.mXfermode == null && this.mDrawable.getOpacity() == -1 && ((this.mAlpha * 256) >> 8) == 255) {
            return isFilledByImage();
        }
        return false;
    }

    private boolean isFilledByImage() {
        boolean z = true;
        if (this.mDrawable == null) {
            return false;
        }
        Rect bounds = this.mDrawable.getBounds();
        Matrix matrix = this.mDrawMatrix;
        if (matrix == null) {
            if (bounds.left > 0 || bounds.top > 0 || bounds.right < getWidth()) {
                z = false;
            } else if (bounds.bottom < getHeight()) {
                z = false;
            }
            return z;
        } else if (!matrix.rectStaysRect()) {
            return false;
        } else {
            RectF boundsSrc = this.mTempSrc;
            RectF boundsDst = this.mTempDst;
            boundsSrc.set(bounds);
            matrix.mapRect(boundsDst, boundsSrc);
            if (boundsDst.left > 0.0f || boundsDst.top > 0.0f || boundsDst.right < ((float) getWidth())) {
                z = false;
            } else if (boundsDst.bottom < ((float) getHeight())) {
                z = false;
            }
            return z;
        }
    }

    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (!(this.mDrawable == null || (sCompatDrawableVisibilityDispatch ^ 1) == 0)) {
            this.mDrawable.setVisible(isVisible, false);
        }
        if (this.mSRInfo != null && (isVisible ^ 1) != 0) {
            resetSRInfo();
        }
    }

    @RemotableViewMethod
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (this.mDrawable != null && sCompatDrawableVisibilityDispatch) {
            boolean z;
            Drawable drawable = this.mDrawable;
            if (visibility == 0) {
                z = true;
            } else {
                z = false;
            }
            drawable.setVisible(z, false);
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mDrawable != null && sCompatDrawableVisibilityDispatch) {
            boolean z;
            Drawable drawable = this.mDrawable;
            if (getVisibility() == 0) {
                z = true;
            } else {
                z = false;
            }
            drawable.setVisible(z, false);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mDrawable != null && sCompatDrawableVisibilityDispatch) {
            this.mDrawable.setVisible(false, false);
        }
        if (this.mSRInfo != null) {
            resetSRInfo();
        }
    }

    public CharSequence getAccessibilityClassName() {
        return ImageView.class.getName();
    }

    protected void encodeProperties(ViewHierarchyEncoder stream) {
        super.encodeProperties(stream);
        stream.addProperty("layout:baseline", getBaseline());
    }

    public boolean isDefaultFocusHighlightNeeded(Drawable background, Drawable foreground) {
        boolean lackFocusState;
        if (this.mDrawable == null || (this.mDrawable.isStateful() ^ 1) != 0) {
            lackFocusState = true;
        } else {
            lackFocusState = this.mDrawable.hasFocusStateSpecified() ^ 1;
        }
        return super.isDefaultFocusHighlightNeeded(background, foreground) ? lackFocusState : false;
    }

    public void onSRTaskSuccess(final SRTaskInfo taskInfo, final Bitmap srBitmap) {
        post(new Runnable() {
            public void run() {
                ImageView.this.onSRTaskSuccessImpl(taskInfo, srBitmap);
            }
        });
    }

    public void onSRTaskSuccessImpl(SRTaskInfo taskInfo, Bitmap srBitmap) {
        Log.d(SR_TAG, "onSRTaskSuccessImpl: imageView = " + this);
        if (this.mSRInfo == null || taskInfo == null || this.mSRInfo.getTaskInfo() != taskInfo) {
            Log.w(SR_TAG, "onSRTaskSuccessImpl: SRTask error! mSRInfo = " + this.mSRInfo + " taskInfo = " + taskInfo);
        } else if (srBitmap == null) {
            Log.w(SR_TAG, "onSRTaskSuccessImpl: srBitmap is null");
            onSRTaskFail(taskInfo);
        } else if (this.mSRInfo.getInvalidateDrawableCount() > 0) {
            long elpasedTime = System.currentTimeMillis() - this.mSRInfo.getFirstTryTime();
            if (this.mSRInfo.getTryCount() < 2 || elpasedTime < 300) {
                this.mSRInfo.setStatus(0);
                if (getDrawable() != null && this.mSRInfo.getIsInWhiteList() && this.mSRInfo.getIsFullScreen() && this.mSRInfo.getStatus() == 0) {
                    Log.d(SR_TAG, "onSRTaskSuccessImpl: try to post imageView = " + this);
                    tryToPostNewTaskToSRManager();
                } else {
                    clearSRInfoWithStatus(3);
                }
            } else {
                clearSRInfoWithStatus(3);
            }
        } else if (HwAISRImageViewTaskManager.getInstance(getContext()).enoughRoomForSize(srBitmap.getByteCount())) {
            if (isAttachedToWindow()) {
                this.mSRInfo.setSRDrawable(new BitmapDrawable(getContext().getResources(), srBitmap));
                int dWidth = srBitmap.getWidth();
                int dHeight = srBitmap.getHeight();
                if (dWidth <= 0 || dHeight <= 0 || ScaleType.FIT_XY == this.mScaleType) {
                    this.mSRInfo.getSRDrawable().setBounds(0, 0, getWidth(), getHeight());
                } else {
                    this.mSRInfo.getSRDrawable().setBounds(0, 0, dWidth, dHeight);
                }
                this.mSRInfo.setScaleX(1.0f);
                this.mSRInfo.setScaleY(1.0f);
                Drawable srcDrawable = getDrawable();
                if (srcDrawable != null) {
                    int sWidth = srcDrawable.getIntrinsicWidth();
                    int sHeight = srcDrawable.getIntrinsicHeight();
                    if (sWidth <= 0 || sHeight <= 0) {
                        clearSRInfoWithStatus(3);
                        return;
                    } else {
                        this.mSRInfo.setScaleX(((float) dWidth) / ((float) sWidth));
                        this.mSRInfo.setScaleY(((float) dHeight) / ((float) sHeight));
                    }
                }
                HwAISRImageViewTaskManager.getInstance(getContext()).addMemory(srBitmap.getByteCount());
                this.mSRInfo.setStatus(2);
                this.mSRInfo.setTaskInfo(null);
                invalidate();
            } else {
                clearSRInfoWithStatus(0);
            }
        } else {
            clearSRInfoWithStatus(3);
        }
    }

    public void onSRTaskFail(final SRTaskInfo taskInfo) {
        post(new Runnable() {
            public void run() {
                ImageView.this.onSRTaskFailImpl(taskInfo);
            }
        });
    }

    public void onSRTaskFailImpl(SRTaskInfo taskInfo) {
        Log.d(SR_TAG, "onSRTaskFailImpl: ");
        if (this.mSRInfo == null || taskInfo == null || this.mSRInfo.getTaskInfo() != taskInfo) {
            Log.w(SR_TAG, "onSRTaskFailImpl: SRTask error! mSRInfo = " + this.mSRInfo + " taskInfo = " + taskInfo);
        } else {
            clearSRInfoWithStatus(3);
        }
    }

    public SRTaskInfo getCurrentSRTask() {
        return this.mSRInfo == null ? null : this.mSRInfo.getTaskInfo();
    }

    private void tryToPostNewTaskToSRManager() {
        this.mSRInfo.setMatchResolution(SRUtils.checkMatchResolution(getDrawable()));
        if (this.mSRInfo.shouldDoSRProcess()) {
            SRTaskInfo taskInfo = HwAISRImageViewTaskManager.getInstance(getContext()).postNewTask(this, getDrawable());
            if (taskInfo != null) {
                Log.d(SR_TAG, "tryToPostNewTaskToSRManager: post success imageView = " + this);
                this.mSRInfo.setTaskInfo(taskInfo);
                this.mSRInfo.setSrcDrawable(getDrawable());
                this.mSRInfo.setInvalidateDrawableCount(0);
                if (this.mSRInfo.getTryCount() == 0) {
                    this.mSRInfo.setFirstTryTime(System.currentTimeMillis());
                }
                this.mSRInfo.increaseTryCount();
                this.mSRInfo.setStatus(1);
                return;
            }
            Log.d(SR_TAG, "tryToPostNewTaskToSRManager: post fail imageView = " + this);
            clearSRInfoWithStatus(3);
        }
    }

    private void resetSRInfo() {
        if (this.mSRInfo.getTaskInfo() != null && this.mSRInfo.getStatus() == 1) {
            HwAISRImageViewTaskManager.getInstance(getContext()).postCancelTask(this.mSRInfo.getTaskInfo());
        }
        clearSRInfoWithStatus(0);
    }

    private void clearSRInfoWithStatus(int status) {
        if (this.mSRInfo != null) {
            BitmapDrawable bitmapDrawable = this.mSRInfo.getSRDrawable();
            if (!(bitmapDrawable == null || bitmapDrawable.getBitmap() == null)) {
                HwAISRImageViewTaskManager.getInstance(getContext()).removeMemory(bitmapDrawable.getBitmap().getByteCount());
            }
            this.mSRInfo.clearInfoWithStatus(status);
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.mSRInfo != null && this.mSRInfo.getIsInWhiteList()) {
            this.mSRInfo.setIsFullScreen(SRUtils.checkIsFullScreen(getContext(), getWidth(), getHeight()));
        }
    }
}
