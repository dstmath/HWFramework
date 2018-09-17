package com.android.server.wm;

import android.common.HwFrameworkFactory;
import android.os.IBinder;
import android.rms.HwSysResource;
import android.util.Log;
import android.util.Slog;
import android.view.SurfaceControl;
import java.io.PrintWriter;
import java.util.Comparator;

class WindowToken extends WindowContainer<WindowState> {
    private static final String TAG = "WindowManager";
    boolean hasVisible;
    boolean hidden;
    private HwSysResource mActivityResource;
    protected DisplayContent mDisplayContent;
    final boolean mOwnerCanManageAppTokens;
    boolean mPersistOnEmpty;
    protected final WindowManagerService mService;
    private final Comparator<WindowState> mWindowComparator = new -$Lambda$ciWQwJI1JkWGjENAc2_yhiUogoU(this);
    boolean paused = false;
    boolean sendingToBottom;
    String stringName;
    final IBinder token;
    boolean waitingToShow;
    final int windowType;

    /* synthetic */ int lambda$-com_android_server_wm_WindowToken_3754(WindowState newWindow, WindowState existingWindow) {
        if (newWindow.mToken != this) {
            throw new IllegalArgumentException("newWindow=" + newWindow + " is not a child of token=" + this);
        } else if (existingWindow.mToken == this) {
            return isFirstChildWindowGreaterThanSecond(newWindow, existingWindow) ? 1 : -1;
        } else {
            throw new IllegalArgumentException("existingWindow=" + existingWindow + " is not a child of token=" + this);
        }
    }

    WindowToken(WindowManagerService service, IBinder _token, int type, boolean persistOnEmpty, DisplayContent dc, boolean ownerCanManageAppTokens) {
        this.mService = service;
        this.token = _token;
        this.windowType = type;
        this.mPersistOnEmpty = persistOnEmpty;
        this.mOwnerCanManageAppTokens = ownerCanManageAppTokens;
        onDisplayChanged(dc);
    }

    void removeAllWindowsIfPossible() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            try {
                ((WindowState) this.mChildren.get(i)).removeIfPossible();
            } catch (IndexOutOfBoundsException e) {
                Slog.e(TAG, "removeAllWindowsIfPossible IndexOutOfBoundsException");
            }
        }
    }

    void setExiting() {
        this.mPersistOnEmpty = false;
        if (!this.hidden) {
            int count = this.mChildren.size();
            int changed = 0;
            boolean delayed = false;
            for (int i = 0; i < count; i++) {
                WindowState win = (WindowState) this.mChildren.get(i);
                if (win.mWinAnimator.isAnimationSet()) {
                    delayed = true;
                }
                changed |= win.onSetAppExiting();
            }
            this.hidden = true;
            if (changed != 0) {
                this.mService.mWindowPlacerLocked.performSurfacePlacement();
                this.mService.updateFocusedWindowLocked(0, false);
            }
            if (delayed) {
                this.mDisplayContent.mExitingTokens.add(this);
            }
        }
    }

    protected boolean isFirstChildWindowGreaterThanSecond(WindowState newWindow, WindowState existingWindow) {
        return newWindow.mBaseLayer >= existingWindow.mBaseLayer;
    }

    void addWindow(WindowState win) {
        if (!(win.isChildWindow() || this.mChildren.contains(win))) {
            if (this.mActivityResource == null) {
                this.mActivityResource = HwFrameworkFactory.getHwResource(30);
            }
            if (!(this.mActivityResource == null || win.mAttrs.packageName == null)) {
                if (Log.HWINFO) {
                    Slog.d(TAG, "ACTIVITY check resid: " + win.mAttrs.packageName + ", size=" + win.mToken.mChildren.size());
                }
                this.mActivityResource.acquire(win.mOwnerUid, win.mAttrs.packageName, -1, win.mToken.mChildren.size());
            }
            Slog.v(TAG, "Adding " + win + " to " + this);
            addChild((WindowContainer) win, this.mWindowComparator);
            this.mService.mWindowsChanged = true;
        }
    }

    boolean isEmpty() {
        return this.mChildren.isEmpty();
    }

    int getAnimLayerAdjustment() {
        return 0;
    }

    WindowState getReplacingWindow() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowState replacing = ((WindowState) this.mChildren.get(i)).getReplacingWindow();
            if (replacing != null) {
                return replacing;
            }
        }
        return null;
    }

    boolean windowsCanBeWallpaperTarget() {
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            if ((((WindowState) this.mChildren.get(j)).mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                return true;
            }
        }
        return false;
    }

    int getHighestAnimLayer() {
        int highest = -1;
        for (int j = 0; j < this.mChildren.size(); j++) {
            int wLayer = ((WindowState) this.mChildren.get(j)).getHighestAnimLayer();
            if (wLayer > highest) {
                highest = wLayer;
            }
        }
        return highest;
    }

    AppWindowToken asAppWindowToken() {
        return null;
    }

    DisplayContent getDisplayContent() {
        return this.mDisplayContent;
    }

    void removeImmediately() {
        if (this.mDisplayContent != null) {
            this.mDisplayContent.removeWindowToken(this.token);
        }
        super.removeImmediately();
    }

    void onDisplayChanged(DisplayContent dc) {
        dc.reParentWindowToken(this);
        this.mDisplayContent = dc;
        SurfaceControl.openTransaction();
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).mWinAnimator.updateLayerStackInTransaction();
        }
        SurfaceControl.closeTransaction();
        super.onDisplayChanged(dc);
    }

    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("windows=");
        pw.println(this.mChildren);
        pw.print(prefix);
        pw.print("windowType=");
        pw.print(this.windowType);
        pw.print(" hidden=");
        pw.print(this.hidden);
        pw.print(" hasVisible=");
        pw.println(this.hasVisible);
        if (this.waitingToShow || this.sendingToBottom) {
            pw.print(prefix);
            pw.print("waitingToShow=");
            pw.print(this.waitingToShow);
            pw.print(" sendingToBottom=");
            pw.print(this.sendingToBottom);
        }
    }

    public String toString() {
        if (this.stringName == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("WindowToken{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" ");
            sb.append(this.token);
            sb.append('}');
            this.stringName = sb.toString();
        }
        return this.stringName;
    }

    String getName() {
        return toString();
    }
}
