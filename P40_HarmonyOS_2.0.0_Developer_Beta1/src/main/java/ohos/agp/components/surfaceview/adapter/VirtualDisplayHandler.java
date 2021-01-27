package ohos.agp.components.surfaceview.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.View;
import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class VirtualDisplayHandler {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "VirtualDisplayHandler");
    private final Context mContext;
    private DisplayMetrics mDisplayMetrics;
    private final int mHeight;
    private Object mOutView;
    private PresentationHandler mPresentationHandler;
    private Surface mSurface;
    private final SurfaceTexture mSurfaceTexture;
    private VirtualDisplay mVirtualDisplay;
    private final int mWidth;

    public VirtualDisplayHandler(Context context, SurfaceTexture surfaceTexture, int i, int i2) {
        this.mContext = context;
        this.mSurfaceTexture = surfaceTexture;
        this.mWidth = i;
        this.mHeight = i2;
    }

    public boolean createPresentationShow() {
        if (!init()) {
            HiLog.error(LABEL, "fail to init the handler", new Object[0]);
            return false;
        }
        VirtualDisplay virtualDisplay = this.mVirtualDisplay;
        if (virtualDisplay == null) {
            HiLog.error(LABEL, "fail to show the presentation", new Object[0]);
            return false;
        }
        this.mPresentationHandler = new PresentationHandler(this.mContext, virtualDisplay.getDisplay(), this.mOutView);
        this.mPresentationHandler.show();
        return true;
    }

    public Optional<View> getPrententationRootView() {
        PresentationHandler presentationHandler = this.mPresentationHandler;
        if (presentationHandler != null) {
            return Optional.of(presentationHandler.getRootView());
        }
        return Optional.empty();
    }

    public void registerObject(Object obj) {
        if (obj != null) {
            this.mOutView = obj;
        }
    }

    private boolean init() {
        SurfaceTexture surfaceTexture;
        if (this.mContext == null || (surfaceTexture = this.mSurfaceTexture) == null || this.mOutView == null) {
            HiLog.error(LABEL, "invalid inputs", new Object[0]);
            return false;
        }
        surfaceTexture.setDefaultBufferSize(this.mWidth, this.mHeight);
        this.mSurface = new Surface(this.mSurfaceTexture);
        if (!initDisplayMetrics()) {
            HiLog.error(LABEL, "fail to get DensityDpi", new Object[0]);
            return false;
        } else if (!isWidthHeightValid()) {
            HiLog.error(LABEL, "invalid width and height", new Object[0]);
            return false;
        } else {
            DisplayManager displayManager = null;
            Object systemService = this.mContext.getSystemService("display");
            if (systemService instanceof DisplayManager) {
                displayManager = (DisplayManager) systemService;
            }
            if (displayManager == null) {
                HiLog.error(LABEL, "fail to get displayManager", new Object[0]);
                return false;
            }
            this.mVirtualDisplay = displayManager.createVirtualDisplay("AGP-vd", this.mWidth, this.mHeight, this.mDisplayMetrics.densityDpi, this.mSurface, 0);
            if (this.mVirtualDisplay != null) {
                return true;
            }
            HiLog.error(LABEL, "fail to get VirtualDisplay", new Object[0]);
            return false;
        }
    }

    private boolean initDisplayMetrics() {
        Resources resources = this.mContext.getResources();
        if (resources == null) {
            HiLog.error(LABEL, "fail to get resources", new Object[0]);
            return false;
        }
        this.mDisplayMetrics = resources.getDisplayMetrics();
        if (this.mDisplayMetrics != null) {
            return true;
        }
        HiLog.error(LABEL, "fail to get resources", new Object[0]);
        return false;
    }

    private boolean isWidthHeightValid() {
        int i = this.mWidth;
        if (i <= 0 || this.mHeight <= 0) {
            HiLog.error(LABEL, "invalid width or height", new Object[0]);
            return false;
        } else if (i <= this.mDisplayMetrics.widthPixels && this.mHeight <= this.mDisplayMetrics.heightPixels) {
            return true;
        } else {
            HiLog.error(LABEL, "invalid size, width:%d, height:%d, metricWidth:%d, metricHeight:%d", new Object[]{Integer.valueOf(this.mWidth), Integer.valueOf(this.mHeight), Integer.valueOf(this.mDisplayMetrics.widthPixels), Integer.valueOf(this.mDisplayMetrics.heightPixels)});
            return false;
        }
    }
}
