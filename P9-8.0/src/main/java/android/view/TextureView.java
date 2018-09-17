package android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import com.huawei.pgmng.log.LogPower;

public class TextureView extends View {
    private static final String LOG_TAG = "TextureView";
    private Canvas mCanvas;
    private boolean mHadSurface;
    private HardwareLayer mLayer;
    private SurfaceTextureListener mListener;
    private final Object[] mLock;
    private final Matrix mMatrix;
    private boolean mMatrixChanged;
    private long mNativeWindow;
    private final Object[] mNativeWindowLock;
    private boolean mOpaque;
    private int mSaveCount;
    private SurfaceTexture mSurface;
    private boolean mUpdateLayer;
    private final OnFrameAvailableListener mUpdateListener;
    private boolean mUpdateSurface;

    public interface SurfaceTextureListener {
        void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2);

        boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture);

        void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2);

        void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture);
    }

    private native void nCreateNativeWindow(SurfaceTexture surfaceTexture);

    private native void nDestroyNativeWindow();

    private static native boolean nLockCanvas(long j, Canvas canvas, Rect rect);

    private static native void nUnlockCanvasAndPost(long j, Canvas canvas);

    public TextureView(Context context) {
        super(context);
        this.mOpaque = true;
        this.mMatrix = new Matrix();
        this.mLock = new Object[0];
        this.mNativeWindowLock = new Object[0];
        this.mUpdateListener = new OnFrameAvailableListener() {
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                TextureView.this.updateLayer();
                TextureView.this.invalidate();
            }
        };
    }

    public TextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mOpaque = true;
        this.mMatrix = new Matrix();
        this.mLock = new Object[0];
        this.mNativeWindowLock = new Object[0];
        this.mUpdateListener = /* anonymous class already generated */;
    }

    public TextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mOpaque = true;
        this.mMatrix = new Matrix();
        this.mLock = new Object[0];
        this.mNativeWindowLock = new Object[0];
        this.mUpdateListener = /* anonymous class already generated */;
    }

    public TextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mOpaque = true;
        this.mMatrix = new Matrix();
        this.mLock = new Object[0];
        this.mNativeWindowLock = new Object[0];
        this.mUpdateListener = /* anonymous class already generated */;
    }

    public boolean isOpaque() {
        return this.mOpaque;
    }

    public void setOpaque(boolean opaque) {
        if (opaque != this.mOpaque) {
            this.mOpaque = opaque;
            if (this.mLayer != null) {
                updateLayerAndInvalidate();
            }
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isHardwareAccelerated()) {
            Log.w(LOG_TAG, "A TextureView or a subclass can only be used with hardware acceleration enabled.");
        }
        if (this.mHadSurface) {
            invalidate(true);
            this.mHadSurface = false;
        }
    }

    protected void onDetachedFromWindowInternal() {
        destroyHardwareLayer();
        releaseSurfaceTexture();
        super.onDetachedFromWindowInternal();
    }

    protected void destroyHardwareResources() {
        super.destroyHardwareResources();
        destroyHardwareLayer();
    }

    private void destroyHardwareLayer() {
        if (this.mLayer != null) {
            this.mLayer.detachSurfaceTexture();
            this.mLayer.destroy();
            this.mLayer = null;
            this.mMatrixChanged = true;
        }
    }

    private void releaseSurfaceTexture() {
        if (this.mSurface != null) {
            boolean shouldRelease = true;
            if (this.mListener != null) {
                shouldRelease = this.mListener.onSurfaceTextureDestroyed(this.mSurface);
            }
            synchronized (this.mNativeWindowLock) {
                nDestroyNativeWindow();
                LogPower.push(184);
            }
            if (shouldRelease) {
                this.mSurface.release();
            }
            this.mSurface = null;
            this.mHadSurface = true;
        }
    }

    public void setLayerType(int layerType, Paint paint) {
        setLayerPaint(paint);
    }

    public void setLayerPaint(Paint paint) {
        if (paint != this.mLayerPaint) {
            this.mLayerPaint = paint;
            invalidate();
        }
    }

    public int getLayerType() {
        return 2;
    }

    public void buildLayer() {
    }

    public void setForeground(Drawable foreground) {
        if (foreground != null && (sTextureViewIgnoresDrawableSetters ^ 1) != 0) {
            throw new UnsupportedOperationException("TextureView doesn't support displaying a foreground drawable");
        }
    }

    public void setBackgroundDrawable(Drawable background) {
        if (background != null && (sTextureViewIgnoresDrawableSetters ^ 1) != 0) {
            throw new UnsupportedOperationException("TextureView doesn't support displaying a background drawable");
        }
    }

    public final void draw(Canvas canvas) {
        this.mPrivateFlags = (this.mPrivateFlags & -6291457) | 32;
        if (canvas.isHardwareAccelerated()) {
            DisplayListCanvas displayListCanvas = (DisplayListCanvas) canvas;
            HardwareLayer layer = getHardwareLayer();
            if (layer != null) {
                applyUpdate();
                applyTransformMatrix();
                this.mLayer.setLayerPaint(this.mLayerPaint);
                displayListCanvas.drawHardwareLayer(layer);
            }
        }
    }

    protected final void onDraw(Canvas canvas) {
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.mSurface != null) {
            this.mSurface.setDefaultBufferSize(getWidth(), getHeight());
            updateLayer();
            if (this.mListener != null) {
                this.mListener.onSurfaceTextureSizeChanged(this.mSurface, getWidth(), getHeight());
            }
        }
    }

    HardwareLayer getHardwareLayer() {
        if (this.mLayer == null) {
            if (this.mAttachInfo == null || this.mAttachInfo.mThreadedRenderer == null) {
                return null;
            }
            this.mLayer = this.mAttachInfo.mThreadedRenderer.createTextureLayer();
            boolean createNewSurface = this.mSurface == null;
            if (createNewSurface) {
                this.mSurface = new SurfaceTexture(false);
                nCreateNativeWindow(this.mSurface);
                LogPower.push(183);
            }
            this.mLayer.setSurfaceTexture(this.mSurface);
            this.mSurface.setDefaultBufferSize(getWidth(), getHeight());
            this.mSurface.setOnFrameAvailableListener(this.mUpdateListener, this.mAttachInfo.mHandler);
            if (this.mListener != null && createNewSurface) {
                this.mListener.onSurfaceTextureAvailable(this.mSurface, getWidth(), getHeight());
            }
            this.mLayer.setLayerPaint(this.mLayerPaint);
        }
        if (this.mUpdateSurface) {
            this.mUpdateSurface = false;
            updateLayer();
            this.mMatrixChanged = true;
            this.mLayer.setSurfaceTexture(this.mSurface);
            this.mSurface.setDefaultBufferSize(getWidth(), getHeight());
        }
        return this.mLayer;
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (this.mSurface == null) {
            return;
        }
        if (visibility == 0) {
            if (this.mLayer != null) {
                this.mSurface.setOnFrameAvailableListener(this.mUpdateListener, this.mAttachInfo.mHandler);
            }
            updateLayerAndInvalidate();
            return;
        }
        this.mSurface.setOnFrameAvailableListener(null);
    }

    private void updateLayer() {
        synchronized (this.mLock) {
            this.mUpdateLayer = true;
        }
    }

    private void updateLayerAndInvalidate() {
        synchronized (this.mLock) {
            this.mUpdateLayer = true;
        }
        invalidate();
    }

    /* JADX WARNING: Missing block: B:10:0x0010, code:
            r4.mLayer.prepare(getWidth(), getHeight(), r4.mOpaque);
            r4.mLayer.updateSurfaceTexture();
     */
    /* JADX WARNING: Missing block: B:11:0x0026, code:
            if (r4.mListener == null) goto L_0x002f;
     */
    /* JADX WARNING: Missing block: B:12:0x0028, code:
            r4.mListener.onSurfaceTextureUpdated(r4.mSurface);
     */
    /* JADX WARNING: Missing block: B:13:0x002f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void applyUpdate() {
        if (this.mLayer != null) {
            synchronized (this.mLock) {
                if (this.mUpdateLayer) {
                    this.mUpdateLayer = false;
                }
            }
        }
    }

    public void setTransform(Matrix transform) {
        this.mMatrix.set(transform);
        this.mMatrixChanged = true;
        invalidateParentIfNeeded();
    }

    public Matrix getTransform(Matrix transform) {
        if (transform == null) {
            transform = new Matrix();
        }
        transform.set(this.mMatrix);
        return transform;
    }

    private void applyTransformMatrix() {
        if (this.mMatrixChanged && this.mLayer != null) {
            this.mLayer.setTransform(this.mMatrix);
            this.mMatrixChanged = false;
        }
    }

    public Bitmap getBitmap() {
        return getBitmap(getWidth(), getHeight());
    }

    public Bitmap getBitmap(int width, int height) {
        if (!isAvailable() || width <= 0 || height <= 0) {
            return null;
        }
        return getBitmap(Bitmap.createBitmap(getResources().getDisplayMetrics(), width, height, Config.ARGB_8888));
    }

    public Bitmap getBitmap(Bitmap bitmap) {
        if (bitmap != null && isAvailable()) {
            applyUpdate();
            applyTransformMatrix();
            if (this.mLayer == null && this.mUpdateSurface) {
                getHardwareLayer();
            }
            if (this.mLayer != null) {
                this.mLayer.copyInto(bitmap);
            }
        }
        return bitmap;
    }

    public boolean isAvailable() {
        return this.mSurface != null;
    }

    public Canvas lockCanvas() {
        return lockCanvas(null);
    }

    public Canvas lockCanvas(Rect dirty) {
        if (!isAvailable()) {
            return null;
        }
        if (this.mCanvas == null) {
            this.mCanvas = new Canvas();
        }
        synchronized (this.mNativeWindowLock) {
            if (nLockCanvas(this.mNativeWindow, this.mCanvas, dirty)) {
                this.mSaveCount = this.mCanvas.save();
                return this.mCanvas;
            }
            return null;
        }
    }

    public void unlockCanvasAndPost(Canvas canvas) {
        if (this.mCanvas != null && canvas == this.mCanvas) {
            canvas.restoreToCount(this.mSaveCount);
            this.mSaveCount = 0;
            synchronized (this.mNativeWindowLock) {
                nUnlockCanvasAndPost(this.mNativeWindow, this.mCanvas);
            }
        }
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.mSurface;
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        if (surfaceTexture == null) {
            throw new NullPointerException("surfaceTexture must not be null");
        } else if (surfaceTexture == this.mSurface) {
            throw new IllegalArgumentException("Trying to setSurfaceTexture to the same SurfaceTexture that's already set.");
        } else if (surfaceTexture.isReleased()) {
            throw new IllegalArgumentException("Cannot setSurfaceTexture to a released SurfaceTexture");
        } else {
            if (this.mSurface != null) {
                nDestroyNativeWindow();
                this.mSurface.release();
            }
            this.mSurface = surfaceTexture;
            nCreateNativeWindow(this.mSurface);
            if ((this.mViewFlags & 12) == 0 && this.mLayer != null) {
                this.mSurface.setOnFrameAvailableListener(this.mUpdateListener, this.mAttachInfo.mHandler);
            }
            this.mUpdateSurface = true;
            invalidateParentIfNeeded();
        }
    }

    public SurfaceTextureListener getSurfaceTextureListener() {
        return this.mListener;
    }

    public void setSurfaceTextureListener(SurfaceTextureListener listener) {
        this.mListener = listener;
    }
}
