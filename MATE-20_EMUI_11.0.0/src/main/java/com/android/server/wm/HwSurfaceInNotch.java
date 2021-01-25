package com.android.server.wm;

import android.graphics.Rect;
import android.os.SystemProperties;
import android.util.Slog;
import android.view.SurfaceControl;
import com.huawei.server.HwBasicPlatformFactory;

public class HwSurfaceInNotch {
    private static final Rect EMPTY_RECT = new Rect();
    private static final Object LOCK = new Object();
    private static final int PROP_LENGTH = 4;
    private InsetSurface mCoverInNotch = new InsetSurface("surfaceInNotch");
    private DisplayContent mDc;
    private int mDisplayHeight = 0;
    private int mDisplayWidth = 0;
    private boolean mIsAddCover = false;
    private Rect mRect = new Rect();

    public HwSurfaceInNotch(DisplayContent dc) {
        this.mDc = dc;
        this.mCoverInNotch.setDisplayContent(dc);
    }

    public void show(int rotation, SurfaceControl surfaceControl) {
        synchronized (LOCK) {
            if (!this.mIsAddCover) {
                if (isNaviBarMini()) {
                    calDisplaySize(rotation);
                    String[] props = SystemProperties.get("ro.config.hw_notch_size", "").split(",");
                    if (props != null) {
                        if (props.length >= 4) {
                            int notchWidth = Integer.parseInt(props[1]);
                            if (rotation == 1) {
                                this.mRect.set(0, 0, notchWidth, this.mDisplayHeight);
                            } else if (rotation == 3) {
                                this.mRect.set(this.mDisplayWidth - notchWidth, 0, this.mDisplayWidth, this.mDisplayHeight);
                            } else {
                                return;
                            }
                            if (this.mCoverInNotch != null) {
                                this.mCoverInNotch.layout(this.mRect.left, this.mRect.top, this.mRect.right, this.mRect.bottom);
                                this.mCoverInNotch.show(surfaceControl);
                            }
                            this.mIsAddCover = true;
                            return;
                        }
                    }
                    Slog.i("HwSurfaceInNotch", "hw_notch_size props is null or length less than four");
                }
            }
        }
    }

    public void remove() {
        synchronized (LOCK) {
            if (this.mIsAddCover) {
                this.mRect.setEmpty();
                this.mCoverInNotch.layout(EMPTY_RECT.left, EMPTY_RECT.top, EMPTY_RECT.right, EMPTY_RECT.bottom);
                this.mCoverInNotch.remove();
                this.mIsAddCover = false;
            }
        }
    }

    private void calDisplaySize(int rotation) {
        if (rotation == 1 || rotation == 3) {
            this.mDisplayHeight = this.mDc.getDisplayInfo().logicalHeight;
            this.mDisplayWidth = this.mDc.getDisplayInfo().logicalWidth;
            return;
        }
        this.mDisplayWidth = this.mDc.getDisplayInfo().logicalHeight;
        this.mDisplayHeight = this.mDc.getDisplayInfo().logicalWidth;
    }

    private boolean isNaviBarMini() {
        if (this.mDc.getDisplayPolicy().mHwDisplayPolicyEx.isNaviBarMini()) {
            return true;
        }
        return !HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getGestureNavConst().getGestureNavEnabled(this.mDc.mWmService.mContext, -2);
    }
}
