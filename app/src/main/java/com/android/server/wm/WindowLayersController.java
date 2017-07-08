package com.android.server.wm;

import android.util.Slog;
import com.android.server.power.IHwShutdownThread;
import java.io.PrintWriter;
import java.util.ArrayDeque;

public class WindowLayersController {
    private WindowState mDockDivider;
    private ArrayDeque<WindowState> mDockedWindows;
    private int mHighestApplicationLayer;
    private int mInputMethodAnimLayerAdjustment;
    private ArrayDeque<WindowState> mInputMethodWindows;
    private ArrayDeque<WindowState> mPinnedWindows;
    private ArrayDeque<WindowState> mReplacingWindows;
    private final WindowManagerService mService;

    public WindowLayersController(WindowManagerService service) {
        this.mHighestApplicationLayer = 0;
        this.mPinnedWindows = new ArrayDeque();
        this.mDockedWindows = new ArrayDeque();
        this.mInputMethodWindows = new ArrayDeque();
        this.mDockDivider = null;
        this.mReplacingWindows = new ArrayDeque();
        this.mService = service;
    }

    final void assignLayersLocked(WindowList windows) {
        clear();
        int curBaseLayer = 0;
        int curLayer = 0;
        boolean anyLayerChanged = false;
        int i = 0;
        int windowCount = windows.size();
        while (i < windowCount) {
            WindowState w = (WindowState) windows.get(i);
            boolean layerChanged = false;
            int oldLayer = w.mLayer;
            if (w.mBaseLayer == curBaseLayer || w.mIsImWindow || (i > 0 && w.mIsWallpaper)) {
                curLayer += 5;
            } else {
                curLayer = w.mBaseLayer;
                curBaseLayer = curLayer;
            }
            assignAnimLayer(w, curLayer);
            if (!(w.mLayer == oldLayer && w.mWinAnimator.mAnimLayer == oldLayer)) {
                layerChanged = true;
                anyLayerChanged = true;
            }
            if (w.mAppToken != null) {
                this.mHighestApplicationLayer = Math.max(this.mHighestApplicationLayer, w.mWinAnimator.mAnimLayer);
            }
            collectSpecialWindows(w);
            if (layerChanged) {
                w.scheduleAnimationIfDimming();
            }
            i++;
        }
        adjustSpecialWindows();
        if (this.mService.mAccessibilityController != null && anyLayerChanged && ((WindowState) windows.get(windows.size() - 1)).getDisplayId() == 0) {
            this.mService.mAccessibilityController.onWindowLayersChangedLocked();
        }
    }

    void setInputMethodAnimLayerAdjustment(int adj) {
        int i;
        this.mInputMethodAnimLayerAdjustment = adj;
        WindowState imw = this.mService.mInputMethodWindow;
        if (imw != null) {
            imw.mWinAnimator.mAnimLayer = imw.mLayer + adj;
            for (i = imw.mChildWindows.size() - 1; i >= 0; i--) {
                WindowState childWindow = (WindowState) imw.mChildWindows.get(i);
                childWindow.mWinAnimator.mAnimLayer = childWindow.mLayer + adj;
            }
        }
        for (i = this.mService.mInputMethodDialogs.size() - 1; i >= 0; i--) {
            WindowState dialog = (WindowState) this.mService.mInputMethodDialogs.get(i);
            dialog.mWinAnimator.mAnimLayer = dialog.mLayer + adj;
        }
    }

    int getSpecialWindowAnimLayerAdjustment(WindowState win) {
        if (win.mIsImWindow) {
            return this.mInputMethodAnimLayerAdjustment;
        }
        if (win.mIsWallpaper) {
            return this.mService.mWallpaperControllerLocked.getAnimLayerAdjustment();
        }
        return 0;
    }

    int getResizeDimLayer() {
        return this.mDockDivider != null ? this.mDockDivider.mLayer - 1 : 1;
    }

    private void logDebugLayers(WindowList windows) {
        int n = windows.size();
        for (int i = 0; i < n; i++) {
            String str;
            WindowState w = (WindowState) windows.get(i);
            WindowStateAnimator winAnimator = w.mWinAnimator;
            String str2 = "WindowManager";
            StringBuilder append = new StringBuilder().append("Assign layer ").append(w).append(": ").append("mBase=").append(w.mBaseLayer).append(" mLayer=").append(w.mLayer);
            if (w.mAppToken == null) {
                str = "";
            } else {
                str = " mAppLayer=" + w.mAppToken.mAppAnimator.animLayerAdjustment;
            }
            Slog.v(str2, append.append(str).append(" =mAnimLayer=").append(winAnimator.mAnimLayer).toString());
        }
    }

    private void clear() {
        this.mHighestApplicationLayer = 0;
        this.mPinnedWindows.clear();
        this.mInputMethodWindows.clear();
        this.mDockedWindows.clear();
        this.mReplacingWindows.clear();
        this.mDockDivider = null;
    }

    private void collectSpecialWindows(WindowState w) {
        if (w.mAttrs.type == 2034) {
            this.mDockDivider = w;
            return;
        }
        if (w.mWillReplaceWindow) {
            this.mReplacingWindows.add(w);
        }
        if (w.mIsImWindow) {
            this.mInputMethodWindows.add(w);
            return;
        }
        TaskStack stack = w.getStack();
        if (stack != null) {
            if (stack.mStackId == 4) {
                this.mPinnedWindows.add(w);
            } else if (stack.mStackId == 3) {
                this.mDockedWindows.add(w);
            }
        }
    }

    private void adjustSpecialWindows() {
        int layer = this.mHighestApplicationLayer + 5;
        while (!this.mDockedWindows.isEmpty()) {
            layer = assignAndIncreaseLayerIfNeeded((WindowState) this.mDockedWindows.remove(), layer);
        }
        layer = assignAndIncreaseLayerIfNeeded(this.mDockDivider, layer);
        if (this.mDockDivider != null && this.mDockDivider.isVisibleLw()) {
            while (!this.mInputMethodWindows.isEmpty()) {
                WindowState w = (WindowState) this.mInputMethodWindows.remove();
                if (layer > w.mLayer) {
                    layer = assignAndIncreaseLayerIfNeeded(w, layer);
                }
            }
        }
        while (!this.mReplacingWindows.isEmpty()) {
            layer = assignAndIncreaseLayerIfNeeded((WindowState) this.mReplacingWindows.remove(), layer);
        }
        while (!this.mPinnedWindows.isEmpty()) {
            layer = assignAndIncreaseLayerIfNeeded((WindowState) this.mPinnedWindows.remove(), layer);
        }
    }

    private int assignAndIncreaseLayerIfNeeded(WindowState win, int layer) {
        if (win == null) {
            return layer;
        }
        assignAnimLayer(win, layer);
        return layer + 5;
    }

    private void assignAnimLayer(WindowState w, int layer) {
        w.mLayer = layer;
        w.mWinAnimator.mAnimLayer = (w.mLayer + w.getAnimLayerAdjustment()) + getSpecialWindowAnimLayerAdjustment(w);
        if (w.mAttrs.type == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME && !this.mService.isCoverOpen()) {
            w.mWinAnimator.mAnimLayer = AbsWindowManagerService.TOP_LAYER;
        }
        if (w.mAppToken != null && w.mAppToken.mAppAnimator.thumbnailForceAboveLayer > 0 && w.mWinAnimator.mAnimLayer > w.mAppToken.mAppAnimator.thumbnailForceAboveLayer) {
            w.mAppToken.mAppAnimator.thumbnailForceAboveLayer = w.mWinAnimator.mAnimLayer;
        }
    }

    void dump(PrintWriter pw, String s) {
        if (this.mInputMethodAnimLayerAdjustment != 0 || this.mService.mWallpaperControllerLocked.getAnimLayerAdjustment() != 0) {
            pw.print("  mInputMethodAnimLayerAdjustment=");
            pw.print(this.mInputMethodAnimLayerAdjustment);
            pw.print("  mWallpaperAnimLayerAdjustment=");
            pw.println(this.mService.mWallpaperControllerLocked.getAnimLayerAdjustment());
        }
    }
}
