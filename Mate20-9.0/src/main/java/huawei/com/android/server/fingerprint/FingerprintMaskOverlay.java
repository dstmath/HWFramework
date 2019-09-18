package huawei.com.android.server.fingerprint;

import android.graphics.Canvas;
import android.os.SystemProperties;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import com.android.server.gesture.GestureNavConst;

public class FingerprintMaskOverlay {
    private static final String ELLE_TITLE = "screenon_fingerprint_alpha_layer";
    private static final float MIN_ALPHA = 0.004f;
    private static String TAG = "FingerprintMaskOverlay";
    private static final String VOGUE_TITLE = "fingerprint_alpha_layer";
    private final int DEFAULT_SCREEN_SIZE_STRING = 2;
    private float mAlpha;
    private int mHeight;
    private boolean mIsCreate;
    private int mLayer;
    private Surface mSurface;
    private SurfaceControl mSurfaceControl;
    private SurfaceSession mSurfaceSession;
    private String mTitle;
    private boolean mVisible;
    private int mWidth;

    public void create(int width, int height, int type) {
        String str = TAG;
        Log.i(str, "create width = " + width + " ,height = " + height + " ,type = " + type);
        this.mWidth = width;
        this.mHeight = height;
        this.mTitle = type == 0 ? VOGUE_TITLE : ELLE_TITLE;
        this.mIsCreate = createSurface();
        drawIfNeeded();
    }

    public void show(float alpha) {
        this.mAlpha = alpha;
        if (this.mSurfaceControl != null && !this.mVisible) {
            this.mSurfaceControl.setAlpha(alpha);
            this.mSurfaceControl.show();
            this.mVisible = true;
        }
    }

    public void show() {
        if (this.mSurfaceControl != null && !this.mVisible) {
            Log.i(TAG, "FingerprintMaskOverlay show");
            this.mSurfaceControl.show();
            this.mVisible = true;
        }
    }

    public void setAlpha(float alpha) {
        String str = TAG;
        Log.i(str, "FingerprintMaskOverlay alpha =" + alpha);
        this.mAlpha = alpha;
        if (this.mAlpha == GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            this.mAlpha = MIN_ALPHA;
            Log.i(TAG, "FingerprintMaskOverlay change alpha to min alpha");
        }
        if (this.mSurfaceControl != null) {
            this.mSurfaceControl.setAlpha(this.mAlpha);
        }
    }

    public void setLayer(int layer) {
        this.mLayer = layer;
        if (this.mSurfaceControl != null) {
            this.mSurfaceControl.setLayer(layer);
        }
    }

    public void hide() {
        if (this.mSurfaceControl != null && this.mVisible) {
            Log.i(TAG, "FingerprintMaskOverlay hide");
            this.mSurfaceControl.hide();
            this.mVisible = false;
        }
    }

    public void destroy() {
        if (this.mSurfaceControl != null) {
            Log.i(TAG, "destroy mFingerprintMaskOverlay");
            this.mSurfaceControl.destroy();
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

    private boolean createSurface() {
        String str = TAG;
        Log.i(str, "FingerprintMaskOverlay createSurface mTitle = " + this.mTitle);
        if (this.mSurfaceSession == null) {
            this.mSurfaceSession = new SurfaceSession();
        }
        if ((this.mWidth == 0 || this.mHeight == 0) && !getDefaultScreenSize()) {
            Log.i(TAG, "cannot get size of screen, return");
            return false;
        }
        SurfaceControl.openTransaction();
        try {
            if (this.mSurfaceControl == null) {
                this.mSurfaceControl = new SurfaceControl.Builder(this.mSurfaceSession).setName(this.mTitle).setSize(this.mWidth, this.mHeight).setFlags(4).setParent(null).setFormat(-3).build();
                this.mSurfaceControl.setPosition(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
            }
            this.mSurface = new Surface();
            this.mSurface.copyFrom(this.mSurfaceControl);
            SurfaceControl.closeTransaction();
            return true;
        } catch (Surface.OutOfResourcesException e) {
            Log.e(TAG, "Unable to create mSurfaceControl");
            SurfaceControl.closeTransaction();
            return false;
        } catch (IllegalArgumentException e2) {
            String str2 = TAG;
            Log.e(str2, "mWidth = " + this.mWidth + "mHeight = " + this.mHeight);
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
        Log.i(TAG, "FingerprintMaskOverlay drawIfNeeded");
        Canvas c = null;
        try {
            c = this.mSurface.lockCanvas(null);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException cannot get Canvas");
        } catch (Surface.OutOfResourcesException e2) {
            Log.e(TAG, "OutOfResourcesException cannot get Canvas");
        }
        if (c != null) {
            c.drawColor(-16777216);
            this.mSurface.unlockCanvasAndPost(c);
        }
    }

    private boolean getDefaultScreenSize() {
        String defaultScreenSize = SystemProperties.get("ro.config.default_screensize");
        if (defaultScreenSize != null && !defaultScreenSize.equals("")) {
            String[] array = defaultScreenSize.split(",");
            if (array.length == 2) {
                try {
                    this.mWidth = Integer.parseInt(array[0]);
                    this.mHeight = Integer.parseInt(array[1]);
                    String str = TAG;
                    Log.i(str, "defaultScreenSizePoint get from prop : mInitDisplayWidth=" + this.mWidth + ",mInitDisplayHeight=" + this.mHeight);
                    return true;
                } catch (NumberFormatException e) {
                    Log.i(TAG, "defaultScreenSizePoint: NumberFormatException");
                    return false;
                }
            } else {
                String str2 = TAG;
                Log.i(str2, "defaultScreenSizePoint the defaultScreenSize prop is error,defaultScreenSize=" + defaultScreenSize);
            }
        }
        return false;
    }
}
