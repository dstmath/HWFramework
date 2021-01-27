package com.huawei.server.fsm;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.IBinder;
import com.android.server.display.FoldPolicy;
import com.android.server.display.SurfaceControlExt;
import com.huawei.android.hardware.display.DisplayViewportEx;
import com.huawei.android.util.SlogEx;

public class HwOutwardFoldPolicy extends DefaultHwOutwardFoldPolicy {
    private static final int REGION_FULL = 7;
    private static final int REGION_MAIN = 1;
    private static final int REGION_SUB = 4;
    private static volatile HwOutwardFoldPolicy sInstance;
    private Rect mSubDispRect = new Rect();
    private Rect mTmpDispRect = new Rect();

    private HwOutwardFoldPolicy(Context context) {
        super(context);
        this.mMainDispRect.set(HwFoldScreenState.SCREEN_FOLD_FULL_WIDTH - HwFoldScreenState.SCREEN_FOLD_MAIN_WIDTH, 0, HwFoldScreenState.SCREEN_FOLD_FULL_WIDTH, HwFoldScreenState.SCREEN_FOLD_FULL_HEIGHT);
        this.mSubDispRect.set(0, 0, HwFoldScreenState.SCREEN_FOLD_MAIN_WIDTH, HwFoldScreenState.SCREEN_FOLD_FULL_HEIGHT);
    }

    public static HwOutwardFoldPolicy getInstance(Context context) {
        if (sInstance == null) {
            synchronized (HwOutwardFoldPolicy.class) {
                if (sInstance == null) {
                    sInstance = new HwOutwardFoldPolicy(context);
                }
            }
        }
        return sInstance;
    }

    public Rect getScreenDispRect(int orientation) {
        if (orientation < 0 || orientation > 3) {
            return null;
        }
        Rect tmpDispRect = getCurrentDispRect();
        if (tmpDispRect == null) {
            return tmpDispRect;
        }
        if (orientation == 3) {
            this.mTmpDispRect.set(tmpDispRect);
        } else if (orientation == 0) {
            this.mTmpDispRect.left = tmpDispRect.top;
            this.mTmpDispRect.right = tmpDispRect.bottom;
            this.mTmpDispRect.top = HwFoldScreenState.SCREEN_FOLD_FULL_WIDTH - tmpDispRect.right;
            this.mTmpDispRect.bottom = HwFoldScreenState.SCREEN_FOLD_FULL_WIDTH - tmpDispRect.left;
        } else if (orientation == 1) {
            this.mTmpDispRect.left = HwFoldScreenState.SCREEN_FOLD_FULL_WIDTH - tmpDispRect.right;
            this.mTmpDispRect.right = HwFoldScreenState.SCREEN_FOLD_FULL_WIDTH - tmpDispRect.left;
            this.mTmpDispRect.top = HwFoldScreenState.SCREEN_FOLD_REAL_FULL_HEIGHT - tmpDispRect.bottom;
            this.mTmpDispRect.bottom = HwFoldScreenState.SCREEN_FOLD_REAL_FULL_HEIGHT - tmpDispRect.top;
        } else if (orientation == 2) {
            this.mTmpDispRect.left = HwFoldScreenState.SCREEN_FOLD_REAL_FULL_HEIGHT - tmpDispRect.bottom;
            this.mTmpDispRect.right = HwFoldScreenState.SCREEN_FOLD_REAL_FULL_HEIGHT - tmpDispRect.top;
            this.mTmpDispRect.top = tmpDispRect.left;
            this.mTmpDispRect.bottom = tmpDispRect.right;
        }
        SlogEx.d("FoldPolicy", "getScreenDispRect=" + tmpDispRect);
        return this.mTmpDispRect;
    }

    private Rect getCurrentDispRect() {
        return getDispRect(this.mDisplayMode);
    }

    public Rect getDispRect(int mode) {
        Rect screenRect;
        if (mode == 1) {
            screenRect = this.mFullDispRect;
        } else if (mode == 2) {
            screenRect = this.mMainDispRect;
        } else if (mode == 3) {
            screenRect = this.mSubDispRect;
        } else if (mode == 4) {
            screenRect = this.mFullDispRect;
        } else {
            screenRect = this.mFullDispRect;
        }
        SlogEx.d("FoldPolicy", "getCurrentDispRect = " + screenRect);
        return screenRect;
    }

    public void adjustViewportFrame(DisplayViewportEx viewport, Rect currentLayerRect, Rect currentDisplayRect) {
        if (viewport == null) {
            SlogEx.e("FoldPolicy", "DisplayViewport is null.");
            return;
        }
        int orientation = viewport.getOrientation();
        if (currentLayerRect != null) {
            viewport.getLogicalFrame().set(currentDisplayRect);
        }
        if (!viewport.getPhysicalFrame().isEmpty()) {
            viewport.getPhysicalFrame().set(currentDisplayRect);
            if (orientation != 0 && orientation != 1) {
                if (orientation == 3) {
                    if (this.mDisplayMode == 3) {
                        int tmpOffsetWidth = HwFoldScreenState.SCREEN_FOLD_FULL_WIDTH - HwFoldScreenState.SCREEN_FOLD_MAIN_WIDTH;
                        viewport.getPhysicalFrame().left += tmpOffsetWidth;
                        viewport.getPhysicalFrame().right += tmpOffsetWidth;
                    } else if (this.mDisplayMode == 2) {
                        int tmpOffsetWidth2 = HwFoldScreenState.SCREEN_FOLD_FULL_WIDTH - HwFoldScreenState.SCREEN_FOLD_MAIN_WIDTH;
                        viewport.getPhysicalFrame().left -= tmpOffsetWidth2;
                        viewport.getPhysicalFrame().right -= tmpOffsetWidth2;
                    }
                } else if (orientation != 2) {
                } else {
                    if (this.mDisplayMode == 3) {
                        int tmpOffsetHeight = HwFoldScreenState.SCREEN_FOLD_FULL_WIDTH - HwFoldScreenState.SCREEN_FOLD_MAIN_WIDTH;
                        viewport.getPhysicalFrame().top += tmpOffsetHeight;
                        viewport.getPhysicalFrame().bottom += tmpOffsetHeight;
                    } else if (this.mDisplayMode == 2) {
                        int tmpOffsetHeight2 = HwFoldScreenState.SCREEN_FOLD_FULL_WIDTH - HwFoldScreenState.SCREEN_FOLD_MAIN_WIDTH;
                        viewport.getPhysicalFrame().top -= tmpOffsetHeight2;
                        viewport.getPhysicalFrame().bottom -= tmpOffsetHeight2;
                    }
                }
            }
        }
    }

    public int getDisplayRotation() {
        return 3;
    }

    public void setDisplayStatus(IBinder token, int displayMode, int foldState, FoldPolicy.DisplayModeChangeCallback callback) {
        int region = getDisplayRegionByMode(displayMode);
        Rect displayRect = foldState == 1 ? this.mFullDispRect : this.mMainDispRect;
        SlogEx.d("FoldPolicy", "setDisplayStatus region " + region + " foldState " + foldState + " displayRect " + displayRect + " subDisplRect " + this.mSubDispRect);
        SurfaceControlExt.setDisplayStatus(token, region, foldState, displayRect, this.mSubDispRect);
        HwOutwardFoldPolicy.super.setDisplayStatus(token, displayMode, foldState, callback);
    }

    private int getDisplayRegionByMode(int displayMode) {
        if (displayMode == 2) {
            return 1;
        }
        if (displayMode == 3) {
            return 4;
        }
        return REGION_FULL;
    }
}
