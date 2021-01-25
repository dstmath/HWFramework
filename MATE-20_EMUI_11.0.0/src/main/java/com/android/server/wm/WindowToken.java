package com.android.server.wm;

import android.os.Debug;
import android.os.IBinder;
import android.util.Flog;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import java.io.PrintWriter;
import java.lang.annotation.RCUnownedThisRef;
import java.util.Comparator;

/* access modifiers changed from: package-private */
public class WindowToken extends WindowContainer<WindowState> {
    private static final String TAG = "WindowManager";
    boolean hasVisible;
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
            /* class com.android.server.wm.WindowToken.AnonymousClass1 */

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
            Flog.i(307, this + " setHidden " + hidden);
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
            boolean changed = false;
            boolean delayed = false;
            for (int i = 0; i < count; i++) {
                WindowState win = (WindowState) this.mChildren.get(i);
                if (win.isAnimating()) {
                    delayed = true;
                }
                changed |= win.onSetAppExiting();
            }
            setHidden(true);
            if (changed) {
                this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
                this.mWmService.updateFocusedWindowLocked(0, false);
            }
            if (delayed) {
                this.mDisplayContent.mExitingTokens.add(this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public float getSizeCompatScale() {
        return this.mDisplayContent.mCompatibleScreenScale;
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
            addChild((WindowToken) win, (Comparator<WindowToken>) this.mWindowComparator);
            this.mWmService.mWindowsChanged = true;
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
            if ((((WindowState) this.mChildren.get(j)).mAttrs.flags & 1048576) != 0) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public AppWindowToken asAppWindowToken() {
        return null;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void removeImmediately() {
        if (this.mDisplayContent != null) {
            this.mDisplayContent.removeWindowToken(this.token);
        }
        super.removeImmediately();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void onDisplayChanged(DisplayContent dc) {
        dc.reParentWindowToken(this);
        super.onDisplayChanged(dc);
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void writeToProto(ProtoOutputStream proto, long fieldId, int logLevel) {
        if (logLevel != 2 || isVisible()) {
            long token2 = proto.start(fieldId);
            super.writeToProto(proto, 1146756268033L, logLevel);
            proto.write(1120986464258L, System.identityHashCode(this));
            for (int i = 0; i < this.mChildren.size(); i++) {
                ((WindowState) this.mChildren.get(i)).writeToProto(proto, 2246267895811L, logLevel);
            }
            proto.write(1133871366148L, this.mHidden);
            proto.write(1133871366149L, this.waitingToShow);
            proto.write(1133871366150L, this.paused);
            proto.end(token2);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
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

    @Override // java.lang.Object
    public String toString() {
        if (this.stringName == null) {
            this.stringName = "WindowToken{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.token + '}';
        }
        return this.stringName;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.ConfigurationContainer
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
        return this.mOwnerCanManageAppTokens && this.mWmService.mPolicy.getWindowLayerFromTypeLw(this.windowType, this.mOwnerCanManageAppTokens) > this.mWmService.mPolicy.getWindowLayerFromTypeLw(2019, this.mOwnerCanManageAppTokens);
    }
}
