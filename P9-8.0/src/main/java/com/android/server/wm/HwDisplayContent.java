package com.android.server.wm;

import android.content.res.Configuration;
import android.graphics.GraphicBuffer;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.HwPCUtils;
import android.util.MutableBoolean;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import com.android.server.wm.-$Lambda$gdyonVWUyLAUEjsalC_69kEf8YE.AnonymousClass1;
import huawei.android.os.HwGeneralManager;
import java.util.ArrayList;
import java.util.List;

public class HwDisplayContent extends DisplayContent {

    @FunctionalInterface
    private interface ScreenshoterForExternalDisplay<E> {
        E screenshotForExternalDisplay(IBinder iBinder, Rect rect, int i, int i2, int i3, int i4, boolean z, int i5);
    }

    public HwDisplayContent(Display display, WindowManagerService service, WindowLayersController layersController, WallpaperController wallpaperController) {
        super(display, service, layersController, wallpaperController);
    }

    void computeScreenConfiguration(Configuration config) {
        super.computeScreenConfiguration(config);
        if (HwGeneralManager.getInstance().isSupportForce()) {
            DisplayInfo displayInfo = this.mService.getDefaultDisplayContentLocked().getDisplayInfo();
            this.mService.mInputManager.setDisplayWidthAndHeight(displayInfo.logicalWidth, displayInfo.logicalHeight);
        }
    }

    List taskIdFromTop() {
        List<Integer> tasks = new ArrayList();
        for (int stackNdx = this.mTaskStackContainers.size() - 1; stackNdx >= 0; stackNdx--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.get(stackNdx);
            for (int taskNdx = stack.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
                Task task = (Task) stack.mChildren.get(taskNdx);
                if (task.getTopVisibleAppMainWindow() != null) {
                    int taskId = task.mTaskId;
                    if (taskId != -1) {
                        tasks.add(Integer.valueOf(taskId));
                    }
                }
            }
        }
        return tasks;
    }

    public void setDisplayRotationFR(int rotation) {
        IntelliServiceManager.setDisplayRotation(rotation);
    }

    public void togglePCMode(boolean pcMode) {
        if (!pcMode && HwPCUtils.isValidExtDisplayId(this.mDisplay.getDisplayId())) {
            try {
                WindowToken topChild = (WindowToken) this.mAboveAppWindowsContainers.getTopChild();
                while (topChild != null) {
                    this.mAboveAppWindowsContainers.removeChild(topChild);
                    topChild = (WindowToken) this.mAboveAppWindowsContainers.getTopChild();
                }
            } catch (Exception e) {
                HwPCUtils.log("PCManager", "togglePCMode failed!!!");
            }
        }
    }

    GraphicBuffer screenshotApplicationsToBufferForExternalDisplay(IBinder displayToken, IBinder appToken, int width, int height, boolean includeFullDisplay, float frameScale, boolean wallpaperOnly, boolean includeDecor) {
        return (GraphicBuffer) screenshotApplicationsForExternalDisplay(displayToken, appToken, width, height, includeFullDisplay, frameScale, wallpaperOnly, includeDecor, new -$Lambda$gdyonVWUyLAUEjsalC_69kEf8YE());
    }

    private <E> E screenshotApplicationsForExternalDisplay(IBinder displayToken, IBinder appToken, int width, int height, boolean includeFullDisplay, float frameScale, boolean wallpaperOnly, boolean includeDecor, ScreenshoterForExternalDisplay<E> screenshoter) {
        int dw = this.mDisplayInfo.logicalWidth;
        int dh = this.mDisplayInfo.logicalHeight;
        if (dw == 0 || dh == 0) {
            return null;
        }
        boolean includeImeInScreenshot;
        this.mScreenshotApplicationState.reset(appToken == null ? wallpaperOnly ^ 1 : false);
        Rect frame = new Rect();
        Rect stackBounds = new Rect();
        synchronized (this.mService.mWindowMap) {
            AppWindowToken imeTargetAppToken = this.mService.mInputMethodTarget != null ? this.mService.mInputMethodTarget.mAppToken : null;
            if (imeTargetAppToken == null || imeTargetAppToken.appToken == null || imeTargetAppToken.appToken.asBinder() != appToken) {
                includeImeInScreenshot = false;
            } else {
                includeImeInScreenshot = this.mService.mInputMethodTarget.isInMultiWindowMode() ^ 1;
            }
        }
        int aboveAppLayer = ((this.mService.mPolicy.getWindowLayerFromTypeLw(2) + 1) * 10000) + 1000;
        MutableBoolean mutableIncludeFullDisplay = new MutableBoolean(includeFullDisplay);
        synchronized (this.mService.mWindowMap) {
            this.mScreenshotApplicationState.appWin = null;
            forAllWindows(new AnonymousClass1(wallpaperOnly, includeImeInScreenshot, includeDecor, aboveAppLayer, this, appToken, mutableIncludeFullDisplay, frame, stackBounds), true);
            WindowState appWin = this.mScreenshotApplicationState.appWin;
            boolean screenshotReady = this.mScreenshotApplicationState.screenshotReady;
            int maxLayer = this.mScreenshotApplicationState.maxLayer;
            int minLayer = this.mScreenshotApplicationState.minLayer;
            if (appToken != null && appWin == null) {
                return null;
            } else if (!screenshotReady) {
                Slog.i("WindowManager", "Failed to capture screenshot of " + appToken + " appWin=" + (appWin == null ? "null" : appWin + " drawState=" + appWin.mWinAnimator.mDrawState));
                return null;
            } else if (maxLayer == 0) {
                return null;
            } else {
                int rot = this.mDisplay.getRotation();
                if (rot == 1 || rot == 3) {
                    int tmp = width;
                    width = height;
                    height = tmp;
                }
                if (mutableIncludeFullDisplay.value) {
                    frame.set(0, 0, dw, dh);
                } else if (!frame.intersect(0, 0, dw, dh)) {
                    frame.setEmpty();
                }
                if (frame.isEmpty()) {
                    return null;
                }
                boolean inRotation;
                if (width < 0) {
                    width = (int) (((float) frame.width()) * frameScale);
                }
                if (height < 0) {
                    height = (int) (((float) frame.height()) * frameScale);
                }
                Rect rect = new Rect(frame);
                if (this.mService.mLazyModeOn != 0) {
                    this.mService.setCropOnSingleHandMode(this.mService.mLazyModeOn, false, dw, dh, rect);
                } else {
                    if (((float) width) / ((float) frame.width()) < ((float) height) / ((float) frame.height())) {
                        rect.right = rect.left + ((int) ((((float) width) / ((float) height)) * ((float) frame.height())));
                        if (rect.right < frame.width()) {
                            rect.right = frame.width();
                        }
                    } else {
                        rect.bottom = rect.top + ((int) ((((float) height) / ((float) width)) * ((float) frame.width())));
                    }
                }
                if (rot == 1 || rot == 3) {
                    rot = rot == 1 ? 3 : 1;
                }
                convertCropForSurfaceFlinger(rect, rot, dw, dh);
                ScreenRotationAnimation screenRotationAnimation = this.mService.mAnimator.getScreenRotationAnimationLocked(0);
                if (screenRotationAnimation != null) {
                    inRotation = screenRotationAnimation.isAnimating();
                } else {
                    inRotation = false;
                }
                SurfaceControl.openTransaction();
                SurfaceControl.closeTransactionSync();
                E bitmap = screenshoter.screenshotForExternalDisplay(displayToken, rect, width, height, minLayer, maxLayer, inRotation, rot);
                if (bitmap == null) {
                    Slog.w("WindowManager", "Screenshot failure taking screenshot for (" + dw + "x" + dh + ") to layer " + maxLayer);
                    return null;
                }
                return bitmap;
            }
        }
    }

    /* synthetic */ boolean lambda$-com_android_server_wm_HwDisplayContent_6900(int aboveAppLayer, boolean wallpaperOnly, boolean includeImeInScreenshot, IBinder appToken, MutableBoolean mutableIncludeFullDisplay, boolean includeDecor, Rect frame, Rect stackBounds, WindowState w) {
        if (!w.mHasSurface) {
            return false;
        }
        if (w.mLayer >= aboveAppLayer) {
            return false;
        }
        if (wallpaperOnly && (w.mIsWallpaper ^ 1) != 0) {
            return false;
        }
        if (w.mIsImWindow) {
            if (!includeImeInScreenshot) {
                return false;
            }
        } else if (w.mIsWallpaper) {
            if (wallpaperOnly) {
                this.mScreenshotApplicationState.appWin = w;
            }
            if (this.mScreenshotApplicationState.appWin == null) {
                return false;
            }
        } else if (appToken != null) {
            if (w.mAppToken == null || w.mAppToken.token != appToken) {
                return false;
            }
            this.mScreenshotApplicationState.appWin = w;
        }
        WindowStateAnimator winAnim = w.mWinAnimator;
        int layer = winAnim.mSurfaceController.getLayer();
        if (this.mScreenshotApplicationState.maxLayer < layer) {
            this.mScreenshotApplicationState.maxLayer = layer;
        }
        if (this.mScreenshotApplicationState.minLayer > layer) {
            this.mScreenshotApplicationState.minLayer = layer;
        }
        if (!mutableIncludeFullDisplay.value && includeDecor) {
            TaskStack stack = w.getStack();
            if (stack != null) {
                stack.getBounds(frame);
            }
            frame.intersect(w.mFrame);
        } else if (!(mutableIncludeFullDisplay.value || (w.mIsWallpaper ^ 1) == 0)) {
            Rect wf = w.mFrame;
            Rect cr = w.mContentInsets;
            frame.union(wf.left + cr.left, wf.top + cr.top, wf.right - cr.right, wf.bottom - cr.bottom);
            w.getVisibleBounds(stackBounds);
            if (!Rect.intersects(frame, stackBounds)) {
                frame.setEmpty();
            }
        }
        boolean foundTargetWs = (w.mAppToken == null || w.mAppToken.token != appToken) ? this.mScreenshotApplicationState.appWin != null ? wallpaperOnly : false : true;
        if (foundTargetWs && winAnim.getShown()) {
            this.mScreenshotApplicationState.screenshotReady = true;
        }
        if (w.isObscuringDisplay()) {
            return true;
        }
        return false;
    }

    protected boolean updateRotationUnchecked(boolean inTransaction) {
        if (HwPCUtils.isPcCastModeInServer() && (HwPCUtils.isValidExtDisplayId(this.mDisplayId) || (HwPCUtils.enabledInPad() && getRotation() == 1))) {
            return false;
        }
        return super.updateRotationUnchecked(inTransaction);
    }

    void performLayout(boolean initial, boolean updateInputWindows) {
        super.performLayout(initial, updateInputWindows);
    }
}
