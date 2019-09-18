package huawei.com.android.server.fingerprint;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import com.android.server.gesture.GestureNavConst;

public class FingerprintCircleOverlay {
    private static final int BLUR_MASK_PARAMETER = 20;
    private static final float DEFUALT_RADIUS = 95.0f;
    private static final float DEFUALT_SCALE = 1.0f;
    private static final int DIA_RADIUS_RATIO = 2;
    private static final int LENGTH_RADIUS_RATIO = 4;
    private static String TAG = "FingerprintCircleOverlay";
    private static final String TITLE = "fingerprint_circle_layer";
    private float mAlpha;
    private int mCenterX;
    private int mCenterY;
    private int mColor;
    private Context mContext;
    private int mHeight;
    private boolean mIsCreate;
    private int mLayer;
    private float mRadius = DEFUALT_RADIUS;
    private float mScale = 1.0f;
    private Surface mSurface;
    private SurfaceControl mSurfaceControl;
    private SurfaceSession mSurfaceSession;
    private boolean mVisible;
    private int mWidth;

    public FingerprintCircleOverlay(Context context) {
        this.mContext = context;
    }

    public void create(int centerX, int centerY, float scale) {
        this.mCenterX = centerX;
        this.mCenterY = centerY;
        this.mScale = scale;
        this.mIsCreate = createSurface();
        drawIfNeeded();
    }

    public void show() {
        if (this.mSurfaceControl != null && !this.mVisible) {
            Log.i(TAG, "fingerprintCircleOverlay show");
            this.mSurfaceControl.show();
            this.mVisible = true;
            FingerViewController.getInstance(this.mContext).notifyCaptureImage();
        }
    }

    public void hide() {
        if (this.mSurfaceControl != null && this.mVisible) {
            Log.i(TAG, "fingerprintCircleOverlay hide");
            this.mSurfaceControl.hide();
            this.mVisible = false;
            FingerViewController.getInstance(this.mContext).notifyDismissBlueSpot();
        }
    }

    public void destroy() {
        if (this.mSurfaceControl != null) {
            this.mSurfaceControl.destroy();
            Log.i(TAG, "fingerprintCircleOverlay destroy");
            this.mSurfaceControl = null;
            this.mVisible = false;
            this.mIsCreate = false;
        }
    }

    public boolean isVisible() {
        return this.mVisible;
    }

    public boolean isCreate() {
        return this.mIsCreate;
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public void setRadius(int radius) {
        this.mRadius = (float) radius;
    }

    public void setAlpha(float alpha) {
        this.mAlpha = alpha;
        if (this.mSurfaceControl != null) {
            this.mSurfaceControl.setAlpha(alpha);
        }
    }

    public void setScale(float scale) {
        this.mScale = scale;
        String str = TAG;
        Log.d(str, "mScale = " + this.mScale);
    }

    public void setCenterPoints(int centerX, int centerY) {
        this.mCenterX = centerX;
        this.mCenterY = centerY;
        String str = TAG;
        Log.d(str, "mCenterX = " + this.mCenterX + ",mCenterY=" + this.mCenterY);
    }

    public void setLayer(int layer) {
        this.mLayer = layer;
        if (this.mSurfaceControl != null) {
            this.mSurfaceControl.setLayer(layer);
        }
    }

    private boolean createSurface() {
        String str = TAG;
        Log.i(str, "fingerprintCircleOverlay createSurface mScale =" + this.mScale + ", mRadius = " + this.mRadius);
        if (this.mSurfaceSession == null) {
            this.mSurfaceSession = new SurfaceSession();
        }
        if (this.mScale == GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            Log.i(TAG, "scale INVALID, use defalut");
            this.mScale = 1.0f;
        }
        if (this.mRadius == GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            Log.i(TAG, "mRadius INVALID, use defalut");
            this.mRadius = DEFUALT_RADIUS;
        }
        SurfaceControl.openTransaction();
        try {
            if (this.mSurfaceControl == null) {
                this.mSurfaceControl = new SurfaceControl.Builder(this.mSurfaceSession).setName(TITLE).setSize((int) (this.mRadius * this.mScale * 4.0f), (int) (this.mRadius * this.mScale * 4.0f)).setFlags(4).setParent(null).setFormat(-3).build();
                this.mSurfaceControl.setPosition((((float) this.mCenterX) - (this.mRadius * 2.0f)) * this.mScale, (((float) this.mCenterY) - (2.0f * this.mRadius)) * this.mScale);
            }
            this.mSurface = new Surface();
            this.mSurface.copyFrom(this.mSurfaceControl);
            SurfaceControl.closeTransaction();
            return true;
        } catch (Surface.OutOfResourcesException e) {
            Log.e(TAG, "Unable to createCircleSurface1.");
            SurfaceControl.closeTransaction();
            return false;
        } catch (IllegalArgumentException e2) {
            String str2 = TAG;
            Log.e(str2, "mRadius = " + this.mRadius + "mScale = " + this.mScale);
            SurfaceControl.closeTransaction();
            return false;
        } catch (Throwable th) {
            SurfaceControl.closeTransaction();
            throw th;
        }
    }

    private void drawIfNeeded() {
        if (this.mSurface == null) {
            Log.e(TAG, "mSurface not created");
            return;
        }
        Log.i(TAG, "fingerprintCircleOverlay drawIfNeeded");
        Canvas c = null;
        try {
            c = this.mSurface.lockCanvas(null);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException cannot get Canvas");
        } catch (Surface.OutOfResourcesException e2) {
            Log.e(TAG, "OutOfResourcesException cannot get Canvas");
        }
        if (c != null) {
            c.drawColor(0);
            Paint circlePaint = new Paint(1);
            circlePaint.setDither(true);
            circlePaint.setStyle(Paint.Style.FILL);
            circlePaint.setColor(this.mColor);
            circlePaint.setMaskFilter(new BlurMaskFilter(20.0f, BlurMaskFilter.Blur.SOLID));
            c.drawCircle(this.mRadius * 2.0f * this.mScale, 2.0f * this.mRadius * this.mScale, this.mRadius * this.mScale, circlePaint);
            this.mSurface.unlockCanvasAndPost(c);
            Log.i(TAG, "drawIfNeeded mFingerprintCenterX=" + this.mCenterX + ",mFingerprintCenterY=" + this.mCenterY + ",scale=" + this.mScale + ",mHighlightSpotRadius=" + this.mRadius);
        }
    }
}
