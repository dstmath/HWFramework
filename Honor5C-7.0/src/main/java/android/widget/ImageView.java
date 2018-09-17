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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewHierarchyEncoder;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.os.HwBootFail;
import huawei.cust.HwCfgFilePolicy;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.khronos.opengles.GL10;

@RemoteView
public class ImageView extends View {
    private static final String LOG_TAG = "ImageView";
    private static final ScaleToFit[] sS2FArray = null;
    private static final ScaleType[] sScaleTypeArray = null;
    private boolean mAdjustViewBounds;
    private boolean mAdjustViewBoundsCompat;
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
    private ScaleType mScaleType;
    private int[] mState;
    private final RectF mTempDst;
    private final RectF mTempSrc;
    private Uri mUri;
    private boolean mUseCorrectStreamDensity;
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
        ;
        
        final int nativeInt;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.ImageView.ScaleType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.ImageView.ScaleType.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ImageView.ScaleType.<clinit>():void");
        }

        private ScaleType(int ni) {
            this.nativeInt = ni;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.ImageView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.ImageView.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.widget.ImageView.<clinit>():void");
    }

    public ImageView(Context context) {
        super(context);
        this.mResource = 0;
        this.mHaveFrame = false;
        this.mAdjustViewBounds = false;
        this.mMaxWidth = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mMaxHeight = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mColorFilter = null;
        this.mHasColorFilter = false;
        this.mAlpha = MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
        this.mViewAlphaScale = GL10.GL_DEPTH_BUFFER_BIT;
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
        this.mAdjustViewBoundsCompat = false;
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
        this.mMaxWidth = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mMaxHeight = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mColorFilter = null;
        this.mHasColorFilter = false;
        this.mAlpha = MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
        this.mViewAlphaScale = GL10.GL_DEPTH_BUFFER_BIT;
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
        this.mAdjustViewBoundsCompat = false;
        this.mInBigView = -1;
        initImageView();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageView, defStyleAttr, defStyleRes);
        Drawable d = a.getDrawable(0);
        if (d != null) {
            setImageDrawable(d);
        }
        this.mBaselineAlignBottom = a.getBoolean(6, false);
        this.mBaseline = a.getDimensionPixelSize(8, -1);
        setAdjustViewBounds(a.getBoolean(2, false));
        setMaxWidth(a.getDimensionPixelSize(3, HwBootFail.STAGE_BOOT_SUCCESS));
        setMaxHeight(a.getDimensionPixelSize(4, HwBootFail.STAGE_BOOT_SUCCESS));
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
        int alpha = a.getInt(10, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE);
        if (alpha != MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) {
            setImageAlpha(alpha);
        }
        this.mCropToPadding = a.getBoolean(7, false);
        a.recycle();
    }

    private void initImageView() {
        boolean z;
        boolean z2 = true;
        this.mMatrix = new Matrix();
        this.mScaleType = ScaleType.FIT_CENTER;
        int targetSdkVersion = this.mContext.getApplicationInfo().targetSdkVersion;
        if (targetSdkVersion <= 17) {
            z = true;
        } else {
            z = false;
        }
        this.mAdjustViewBoundsCompat = z;
        if (targetSdkVersion <= 23) {
            z2 = false;
        }
        this.mUseCorrectStreamDensity = z2;
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
        return new ImageDrawableCallback(getContext().getDrawable(resId), null, resId);
    }

    @RemotableViewMethod(asyncImpl = "setImageURIAsync")
    public void setImageURI(Uri uri) {
        if (this.mResource == 0) {
            if (this.mUri == uri) {
                return;
            }
            if (!(uri == null || this.mUri == null || !uri.equals(this.mUri))) {
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
        Drawable d = null;
        if (this.mResource == 0 && (this.mUri == uri || (uri != null && this.mUri != null && uri.equals(this.mUri)))) {
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

    private void resolveUri() {
        if (this.mDrawable == null && getResources() != null) {
            Drawable d = null;
            if (this.mResource != 0) {
                try {
                    d = this.mContext.getDrawable(this.mResource);
                } catch (Exception e) {
                    Log.w(LOG_TAG, "Unable to find resource: " + this.mResource, e);
                    this.mUri = null;
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
                return null;
            }
        } else if (!"content".equals(scheme) && !"file".equals(scheme)) {
            return Drawable.createFromPath(uri.toString());
        } else {
            InputStream inputStream = null;
            try {
                Resources resources;
                inputStream = this.mContext.getContentResolver().openInputStream(uri);
                if (this.mUseCorrectStreamDensity) {
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
        boolean z = false;
        if (!(d == this.mRecycleableBitmapDrawable || this.mRecycleableBitmapDrawable == null)) {
            this.mRecycleableBitmapDrawable.setBitmap(null);
        }
        if (this.mDrawable != null) {
            this.mDrawable.setCallback(null);
            unscheduleDrawable(this.mDrawable);
            if (isAttachedToWindow()) {
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
            if (isAttachedToWindow()) {
                if (getWindowVisibility() == 0) {
                    z = isShown();
                }
                d.setVisible(z, true);
            }
            d.setLevel(this.mLevel);
            this.mDrawableWidth = d.getIntrinsicWidth();
            this.mDrawableHeight = d.getIntrinsicHeight();
            applyImageTint();
            applyColorMod();
            configureBounds();
            return;
        }
        this.mDrawableHeight = -1;
        this.mDrawableWidth = -1;
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
            if (desiredAspect != 0.0f) {
                if (((double) Math.abs((((float) ((widthSize - pleft) - pright)) / ((float) ((heightSize - ptop) - pbottom))) - desiredAspect)) > 1.0E-7d) {
                    boolean done = false;
                    if (resizeWidth) {
                        int newWidth = (((int) (((float) ((heightSize - ptop) - pbottom)) * desiredAspect)) + pleft) + pright;
                        if (!(resizeHeight || this.mAdjustViewBoundsCompat)) {
                            widthSize = resolveAdjustedSize(newWidth, this.mMaxWidth, widthMeasureSpec);
                        }
                        if (newWidth <= widthSize) {
                            widthSize = newWidth;
                            done = true;
                        }
                    }
                    if (!done && resizeHeight) {
                        int newHeight = (((int) (((float) ((widthSize - pleft) - pright)) / desiredAspect)) + ptop) + pbottom;
                        if (!(resizeWidth || this.mAdjustViewBoundsCompat)) {
                            heightSize = resolveAdjustedSize(newHeight, this.mMaxHeight, heightMeasureSpec);
                        }
                        if (newHeight <= heightSize) {
                            heightSize = newHeight;
                        }
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
        setMeasuredDimension(widthSize, heightSize);
    }

    private int resolveAdjustedSize(int desiredSize, int maxSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case RtlSpacingHelper.UNDEFINED /*-2147483648*/:
                return Math.min(Math.min(desiredSize, specSize), maxSize);
            case HwCfgFilePolicy.GLOBAL /*0*/:
                return Math.min(desiredSize, maxSize);
            case EditorInfo.IME_FLAG_NO_ENTER_ACTION /*1073741824*/:
                return specSize;
            default:
                return result;
        }
    }

    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        this.mHaveFrame = true;
        configureBounds();
        return changed;
    }

    private void configureBounds() {
        if (this.mDrawable != null && this.mHaveFrame) {
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
                        scale = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
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
            if (this.mDrawMatrix == null && this.mPaddingTop == 0 && this.mPaddingLeft == 0) {
                this.mDrawable.draw(canvas);
            } else {
                int saveCount = canvas.getSaveCount();
                canvas.save();
                if (this.mCropToPadding) {
                    int scrollX = this.mScrollX;
                    int scrollY = this.mScrollY;
                    canvas.clipRect(this.mPaddingLeft + scrollX, this.mPaddingTop + scrollY, ((this.mRight + scrollX) - this.mLeft) - this.mPaddingRight, ((this.mBottom + scrollY) - this.mTop) - this.mPaddingBottom);
                }
                canvas.translate((float) this.mPaddingLeft, (float) this.mPaddingTop);
                if (this.mDrawMatrix != null) {
                    canvas.concat(this.mDrawMatrix);
                }
                this.mDrawable.draw(canvas);
                canvas.restoreToCount(saveCount);
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
        alpha &= MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
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
            this.mDrawable.setAlpha((this.mAlpha * GL10.GL_DEPTH_BUFFER_BIT) >> 8);
        }
    }

    public boolean isOpaque() {
        if (super.isOpaque()) {
            return true;
        }
        if (this.mDrawable != null && this.mXfermode == null && this.mDrawable.getOpacity() == -1 && ((this.mAlpha * GL10.GL_DEPTH_BUFFER_BIT) >> 8) == MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) {
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
        if (this.mDrawable != null) {
            this.mDrawable.setVisible(isVisible, false);
        }
    }

    public CharSequence getAccessibilityClassName() {
        return ImageView.class.getName();
    }

    protected void encodeProperties(ViewHierarchyEncoder stream) {
        super.encodeProperties(stream);
        stream.addProperty("layout:baseline", getBaseline());
    }
}
