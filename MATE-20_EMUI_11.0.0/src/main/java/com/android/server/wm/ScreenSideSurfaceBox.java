package com.android.server.wm;

import android.graphics.Rect;
import android.os.IBinder;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceControl;

public class ScreenSideSurfaceBox {
    private static final int DEF_PHYSICAL_HEIGHT = 2400;
    private static final Rect EMPTY_RECT = new Rect();
    private static final int FIRST_INDEX = 0;
    private static final int FORTH_INDEX = 3;
    private static final float ROUND_VALUE = 0.5f;
    private static final int SECOND_INDEX = 1;
    private static final String SIDE_PROP = SystemProperties.get("ro.config.hw_curved_side_disp", "");
    private static final int SIDE_PROP_LENGTH = 4;
    public static final int SURFACE_HIDE = 2;
    public static final int SURFACE_INIT = 0;
    public static final int SURFACE_SHOW = 1;
    private static final String TAG = "ScreenSideSurfaceBox";
    private static final int THIRD_INDEX = 2;
    private static int sBaseValue;
    private static Rect sInnerLand = new Rect();
    private static Rect sInnerPort = new Rect();
    private static Rect sOuterLand = new Rect();
    private static Rect sOuterPort = new Rect();
    private static int[] sSideParams;
    private final InsetSurface mBottom = new InsetSurface("bottom");
    private DisplayContent mDc;
    private Rect mInner = new Rect();
    private final InsetSurface mLeft = new InsetSurface("left");
    private Rect mOuter = new Rect();
    private int mPhysicalHeight = 0;
    private final InsetSurface mRight = new InsetSurface("right");
    int mSurfaceState = 0;
    private final InsetSurface[] mSurfaces = {this.mLeft, this.mTop, this.mRight, this.mBottom};
    private final SurfaceControl.Transaction mTmpTransaction = new SurfaceControl.Transaction();
    private final InsetSurface mTop = new InsetSurface("top");

    static {
        sBaseValue = 24;
        try {
            if (!TextUtils.isEmpty(SIDE_PROP)) {
                String[] params = SIDE_PROP.split(",");
                int length = params.length;
                if (length < 4) {
                    sSideParams = null;
                    return;
                }
                sSideParams = new int[length];
                for (int i = 0; i < length; i++) {
                    sSideParams[i] = Integer.parseInt(params[i]);
                }
                sOuterPort.set(0, 0, sSideParams[0] + sSideParams[1] + sSideParams[2], sSideParams[3]);
                sOuterLand.set(0, 0, sSideParams[3], sSideParams[0] + sSideParams[1] + sSideParams[2]);
                sInnerPort.set(sSideParams[0], 0, sSideParams[1] + sSideParams[2], sSideParams[3]);
                sInnerLand.set(0, sSideParams[0], sSideParams[3], sSideParams[1] + sSideParams[2]);
                sBaseValue = sSideParams[0];
            }
        } catch (NumberFormatException e) {
            sSideParams = null;
        } catch (Exception e2) {
            sSideParams = null;
        }
    }

    public ScreenSideSurfaceBox(DisplayContent dc) {
        this.mDc = dc;
        this.mTop.setDisplayContent(dc);
        this.mLeft.setDisplayContent(dc);
        this.mBottom.setDisplayContent(dc);
        this.mRight.setDisplayContent(dc);
        this.mSurfaceState = 2;
    }

    public void layout(Rect outer, Rect inner) {
        this.mTop.layout(outer.left, outer.top, outer.right, inner.top);
        this.mLeft.layout(outer.left, outer.top, inner.left, outer.bottom);
        this.mBottom.layout(outer.left, inner.bottom, outer.right, outer.bottom);
        this.mRight.layout(inner.right, outer.top, outer.right, outer.bottom);
    }

    public void hide(SurfaceControl.Transaction t) {
        Rect rect = EMPTY_RECT;
        layout(rect, rect);
        for (InsetSurface surface : this.mSurfaces) {
            surface.hide(t);
        }
        this.mSurfaceState = 2;
    }

    private int getPhysicalHeight() {
        SurfaceControl.PhysicalDisplayInfo[] configs;
        if (this.mPhysicalHeight == 0) {
            IBinder displayToken = SurfaceControl.getInternalDisplayToken();
            if (displayToken == null || (configs = SurfaceControl.getDisplayConfigs(displayToken)) == null || configs.length == 0) {
                return DEF_PHYSICAL_HEIGHT;
            }
            this.mPhysicalHeight = configs[0].height;
        }
        return this.mPhysicalHeight;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0066 A[LOOP:0: B:11:0x0064->B:12:0x0066, LOOP_END] */
    public void show() {
        int rotation = this.mDc.mWmService.getDefaultDisplayRotation();
        int curHeight = this.mDc.getBaseDisplayHeight();
        if (rotation != 0) {
            if (rotation != 1) {
                if (rotation != 2) {
                    if (rotation != 3) {
                        this.mOuter.set(sOuterPort);
                        this.mInner.set(sInnerPort);
                        float ratio = (float) ((((double) curHeight) * 1.0d) / ((double) getPhysicalHeight()));
                        layout(scale(this.mOuter, ratio), scale(this.mInner, ratio));
                        for (InsetSurface surface : this.mSurfaces) {
                            surface.show(null);
                        }
                        this.mSurfaceState = 1;
                    }
                }
            }
            this.mOuter.set(sOuterLand);
            this.mInner.set(sInnerLand);
            float ratio2 = (float) ((((double) curHeight) * 1.0d) / ((double) getPhysicalHeight()));
            layout(scale(this.mOuter, ratio2), scale(this.mInner, ratio2));
            while (r6 < r5) {
            }
            this.mSurfaceState = 1;
        }
        this.mOuter.set(sOuterPort);
        this.mInner.set(sInnerPort);
        float ratio22 = (float) ((((double) curHeight) * 1.0d) / ((double) getPhysicalHeight()));
        layout(scale(this.mOuter, ratio22), scale(this.mInner, ratio22));
        while (r6 < r5) {
        }
        this.mSurfaceState = 1;
    }

    public void destroy() {
        this.mOuter.setEmpty();
        this.mInner.setEmpty();
        Rect rect = EMPTY_RECT;
        layout(rect, rect);
        for (InsetSurface surface : this.mSurfaces) {
            surface.remove();
        }
        this.mSurfaceState = 0;
    }

    private Rect scale(Rect r, float scale) {
        Rect ret = new Rect(r);
        if (scale != 1.0f) {
            ret.left = (int) ((((float) r.left) * scale) + 0.5f);
            ret.top = (int) ((((float) r.top) * scale) + 0.5f);
            ret.right = (int) ((((float) r.right) * scale) + 0.5f);
            ret.bottom = (int) ((((float) r.bottom) * scale) + 0.5f);
        }
        return ret;
    }

    public void hideSideBox() {
        hideSideBox(false);
    }

    public void hideSideBox(boolean force) {
        if (this.mSurfaceState == 1 || force) {
            Rect rect = EMPTY_RECT;
            layout(rect, rect);
            for (InsetSurface surface : this.mSurfaces) {
                surface.hide(this.mTmpTransaction);
            }
            this.mTmpTransaction.apply();
            this.mSurfaceState = 2;
        }
    }

    public void showSideBox() {
        WindowState win;
        if (this.mSurfaceState != 1) {
            for (int tokenNdx = this.mDc.mAboveAppWindowsContainers.getChildCount() - 1; tokenNdx >= 0; tokenNdx--) {
                WindowToken token = (WindowToken) this.mDc.mAboveAppWindowsContainers.getChildAt(tokenNdx);
                if (token != null && (win = (WindowState) token.getTopChild()) != null && "com.huawei.android.extdisplay".equals(win.getOwningPackage()) && win.mWinAnimator != null && win.mWinAnimator.getShown()) {
                    Log.i(TAG, "skip showSideBox because L-R key");
                    return;
                }
            }
            showSideBoxInner();
            this.mSurfaceState = 1;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0066 A[LOOP:0: B:12:0x0064->B:13:0x0066, LOOP_END] */
    private void showSideBoxInner() {
        int rotation = this.mDc.mWmService.getDefaultDisplayRotation();
        int curHeight = this.mDc.getBaseDisplayHeight();
        if (rotation != 0) {
            if (rotation != 1) {
                if (rotation != 2) {
                    if (rotation != 3) {
                        this.mOuter.set(sOuterPort);
                        this.mInner.set(sInnerPort);
                        float ratio = (float) ((((double) curHeight) * 1.0d) / ((double) getPhysicalHeight()));
                        layout(scale(this.mOuter, ratio), scale(this.mInner, ratio));
                        for (InsetSurface surface : this.mSurfaces) {
                            surface.show(null, this.mTmpTransaction);
                        }
                        this.mTmpTransaction.apply();
                    }
                }
            }
            this.mOuter.set(sOuterLand);
            this.mInner.set(sInnerLand);
            float ratio2 = (float) ((((double) curHeight) * 1.0d) / ((double) getPhysicalHeight()));
            layout(scale(this.mOuter, ratio2), scale(this.mInner, ratio2));
            while (r5 < r4) {
            }
            this.mTmpTransaction.apply();
        }
        this.mOuter.set(sOuterPort);
        this.mInner.set(sInnerPort);
        float ratio22 = (float) ((((double) curHeight) * 1.0d) / ((double) getPhysicalHeight()));
        layout(scale(this.mOuter, ratio22), scale(this.mInner, ratio22));
        while (r5 < r4) {
        }
        this.mTmpTransaction.apply();
    }

    public void updateSideBox() {
        if (this.mSurfaceState == 1) {
            showSideBoxInner();
        }
    }

    public boolean isShowing() {
        return this.mSurfaceState == 1;
    }

    public void setLayer(SurfaceControl.Transaction transaction, SurfaceControl relativeTo, int layer) {
        if (this.mSurfaceState == 1) {
            for (InsetSurface surface : this.mSurfaces) {
                surface.setLayer(transaction, relativeTo, layer);
            }
        }
    }
}
