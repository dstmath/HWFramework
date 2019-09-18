package com.android.server.wm;

import android.common.HwFrameworkFactory;
import android.os.Debug;
import android.os.IBinder;
import android.rms.HwSysResource;
import android.util.Log;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.server.pm.DumpState;
import java.io.PrintWriter;
import java.lang.annotation.RCUnownedThisRef;
import java.util.Comparator;

class WindowToken extends WindowContainer<WindowState> {
    private static final String TAG = "WindowManager";
    boolean hasVisible;
    private HwSysResource mActivityResource;
    protected DisplayContent mDisplayContent;
    private boolean mHidden;
    final boolean mOwnerCanManageAppTokens;
    boolean mPersistOnEmpty;
    final boolean mRoundedCornerOverlay;
    private final Comparator<WindowState> mWindowComparator;
    boolean paused;
    boolean sendingToBottom;
    String stringName;
    final IBinder token;
    boolean waitingToShow;
    final int windowType;

    WindowToken(WindowManagerService service, IBinder _token, int type, boolean persistOnEmpty, DisplayContent dc, boolean ownerCanManageAppTokens) {
        this(service, _token, type, persistOnEmpty, dc, ownerCanManageAppTokens, false);
    }

    WindowToken(WindowManagerService service, IBinder _token, int type, boolean persistOnEmpty, DisplayContent dc, boolean ownerCanManageAppTokens, boolean roundedCornerOverlay) {
        super(service);
        this.paused = false;
        this.mWindowComparator = new Comparator<WindowState>() {
            @RCUnownedThisRef
            public int compare(WindowState newWindow, WindowState existingWindow) {
                WindowToken token = WindowToken.this;
                if (newWindow.mToken != token) {
                    throw new IllegalArgumentException("newWindow=" + newWindow + " is not a child of token=" + token);
                } else if (existingWindow.mToken == token) {
                    return WindowToken.this.isFirstChildWindowGreaterThanSecond(newWindow, existingWindow) ? 1 : -1;
                } else {
                    throw new IllegalArgumentException("existingWindow=" + existingWindow + " is not a child of token=" + token);
                }
            }
        };
        this.token = _token;
        this.windowType = type;
        this.mPersistOnEmpty = persistOnEmpty;
        this.mOwnerCanManageAppTokens = ownerCanManageAppTokens;
        this.mRoundedCornerOverlay = roundedCornerOverlay;
        onDisplayChanged(dc);
    }

    /* access modifiers changed from: package-private */
    public void setHidden(boolean hidden) {
        if (hidden != this.mHidden) {
            this.mHidden = hidden;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isHidden() {
        return this.mHidden;
    }

    /* access modifiers changed from: package-private */
    public void removeAllWindowsIfPossible() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            try {
                ((WindowState) this.mChildren.get(i)).removeIfPossible();
            } catch (IndexOutOfBoundsException e) {
                Slog.e(TAG, "removeAllWindowsIfPossible IndexOutOfBoundsException");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setExiting() {
        if (this.mChildren.size() == 0) {
            super.removeImmediately();
            return;
        }
        this.mPersistOnEmpty = false;
        if (!this.mHidden) {
            int count = this.mChildren.size();
            boolean delayed = false;
            boolean changed = false;
            for (int i = 0; i < count; i++) {
                WindowState win = (WindowState) this.mChildren.get(i);
                if (win.mWinAnimator.isAnimationSet()) {
                    delayed = true;
                }
                changed |= win.onSetAppExiting();
            }
            setHidden(true);
            if (changed) {
                this.mService.mWindowPlacerLocked.performSurfacePlacement();
                this.mService.updateFocusedWindowLocked(0, false);
            }
            if (delayed) {
                this.mDisplayContent.mExitingTokens.add(this);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isFirstChildWindowGreaterThanSecond(WindowState newWindow, WindowState existingWindow) {
        return newWindow.mBaseLayer >= existingWindow.mBaseLayer;
    }

    /* access modifiers changed from: package-private */
    public void addWindow(WindowState win) {
        if (WindowManagerDebugConfig.DEBUG_FOCUS) {
            Slog.d(TAG, "addWindow: win=" + win + " Callers=" + Debug.getCallers(5));
        }
        if (!win.isChildWindow() && !this.mChildren.contains(win)) {
            if (this.mActivityResource == null) {
                this.mActivityResource = HwFrameworkFactory.getHwResource(30);
            }
            if (!(this.mActivityResource == null || win.mAttrs.packageName == null)) {
                if (Log.HWINFO) {
                    Slog.d(TAG, "ACTIVITY check resid: " + win.mAttrs.packageName + ", size=" + win.mToken.mChildren.size());
                }
                this.mActivityResource.acquire(win.mOwnerUid, win.mAttrs.packageName, -1, win.mToken.mChildren.size());
            }
            addChild(win, this.mWindowComparator);
            this.mService.mWindowsChanged = true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isEmpty() {
        return this.mChildren.isEmpty();
    }

    /* access modifiers changed from: package-private */
    public WindowState getReplacingWindow() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowState replacing = ((WindowState) this.mChildren.get(i)).getReplacingWindow();
            if (replacing != null) {
                return replacing;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean windowsCanBeWallpaperTarget() {
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            if ((((WindowState) this.mChildren.get(j)).mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public int getHighestAnimLayer() {
        int highest = -1;
        for (int j = 0; j < this.mChildren.size(); j++) {
            int wLayer = ((WindowState) this.mChildren.get(j)).getHighestAnimLayer();
            if (wLayer > highest) {
                highest = wLayer;
            }
        }
        return highest;
    }

    /* access modifiers changed from: package-private */
    public AppWindowToken asAppWindowToken() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public DisplayContent getDisplayContent() {
        return this.mDisplayContent;
    }

    /* access modifiers changed from: package-private */
    public void removeImmediately() {
        if (this.mDisplayContent != null) {
            this.mDisplayContent.removeWindowToken(this.token);
        }
        super.removeImmediately();
    }

    /* access modifiers changed from: package-private */
    public void onDisplayChanged(DisplayContent dc) {
        dc.reParentWindowToken(this);
        this.mDisplayContent = dc;
        if (this.mRoundedCornerOverlay) {
            this.mDisplayContent.reparentToOverlay(this.mPendingTransaction, this.mSurfaceControl);
        }
        super.onDisplayChanged(dc);
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId, boolean trim) {
        long token2 = proto.start(fieldId);
        super.writeToProto(proto, 1146756268033L, trim);
        proto.write(1120986464258L, System.identityHashCode(this));
        for (int i = 0; i < this.mChildren.size(); i++) {
            ((WindowState) this.mChildren.get(i)).writeToProto(proto, 2246267895811L, trim);
        }
        proto.write(1133871366148L, this.mHidden);
        proto.write(1133871366149L, this.waitingToShow);
        proto.write(1133871366150L, this.paused);
        proto.end(token2);
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        super.dump(pw, prefix, dumpAll);
        pw.print(prefix);
        pw.print("windows=");
        pw.println(this.mChildren);
        pw.print(prefix);
        pw.print("windowType=");
        pw.print(this.windowType);
        pw.print(" hidden=");
        pw.print(this.mHidden);
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
            this.stringName = "WindowToken{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.token + '}';
        }
        return this.stringName;
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return toString();
    }

    /* access modifiers changed from: package-private */
    public boolean okToDisplay() {
        return this.mDisplayContent != null && this.mDisplayContent.okToDisplay();
    }

    /* access modifiers changed from: package-private */
    public boolean okToAnimate() {
        return this.mDisplayContent != null && this.mDisplayContent.okToAnimate();
    }

    /* access modifiers changed from: package-private */
    public boolean canLayerAboveSystemBars() {
        return this.mOwnerCanManageAppTokens && this.mService.mPolicy.getWindowLayerFromTypeLw(this.windowType, this.mOwnerCanManageAppTokens) > this.mService.mPolicy.getWindowLayerFromTypeLw(2019, this.mOwnerCanManageAppTokens);
    }
}
