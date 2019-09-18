package com.android.server.policy;

import android.app.StatusBarManager;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.server.LocalServices;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.wm.WindowManagerInternal;
import java.io.PrintWriter;

public class BarController {
    private static final boolean DEBUG = false;
    private static final int MSG_NAV_BAR_VISIBILITY_CHANGED = 1;
    private static final int TRANSIENT_BAR_HIDING = 3;
    private static final int TRANSIENT_BAR_NONE = 0;
    private static final int TRANSIENT_BAR_SHOWING = 2;
    private static final int TRANSIENT_BAR_SHOW_REQUESTED = 1;
    private static final int TRANSLUCENT_ANIMATION_DELAY_MS = 1000;
    private final Rect mContentFrame = new Rect();
    protected final Handler mHandler;
    private long mLastTranslucent;
    private boolean mNoAnimationOnNextShow;
    private boolean mPendingShow;
    private final Object mServiceAquireLock = new Object();
    private boolean mSetUnHideFlagWhenNextTransparent;
    private boolean mShowTransparent;
    private int mState = 0;
    protected StatusBarManagerInternal mStatusBarInternal;
    /* access modifiers changed from: private */
    public final int mStatusBarManagerId;
    protected final String mTag;
    private int mTransientBarState;
    private final int mTransientFlag;
    private final int mTranslucentFlag;
    private final int mTranslucentWmFlag;
    private final int mTransparentFlag;
    private final int mUnhideFlag;
    /* access modifiers changed from: private */
    public OnBarVisibilityChangedListener mVisibilityChangeListener;
    protected WindowManagerPolicy.WindowState mWin;

    private class BarHandler extends Handler {
        private BarHandler() {
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            if (msg.what == 1) {
                if (msg.arg1 == 0) {
                    z = false;
                }
                boolean visible = z;
                if (BarController.this.mVisibilityChangeListener != null) {
                    BarController.this.mVisibilityChangeListener.onBarVisibilityChanged(visible);
                }
            }
        }
    }

    interface OnBarVisibilityChangedListener {
        void onBarVisibilityChanged(boolean z);
    }

    public BarController(String tag, int transientFlag, int unhideFlag, int translucentFlag, int statusBarManagerId, int translucentWmFlag, int transparentFlag) {
        this.mTag = "BarController." + tag;
        this.mTransientFlag = transientFlag;
        this.mUnhideFlag = unhideFlag;
        this.mTranslucentFlag = translucentFlag;
        this.mStatusBarManagerId = statusBarManagerId;
        this.mTranslucentWmFlag = translucentWmFlag;
        this.mTransparentFlag = transparentFlag;
        this.mHandler = new BarHandler();
    }

    public void setWindow(WindowManagerPolicy.WindowState win) {
        this.mWin = win;
    }

    public void setContentFrame(Rect frame) {
        this.mContentFrame.set(frame);
    }

    public void setShowTransparent(boolean transparent) {
        if (transparent != this.mShowTransparent) {
            this.mShowTransparent = transparent;
            this.mSetUnHideFlagWhenNextTransparent = transparent;
            this.mNoAnimationOnNextShow = true;
        }
    }

    public void showTransient() {
        if (this.mWin != null) {
            setTransientBarState(1);
        }
    }

    public boolean isTransientShowing() {
        return this.mTransientBarState == 2;
    }

    public boolean isTransientShowRequested() {
        return this.mTransientBarState == 1;
    }

    public boolean wasRecentlyTranslucent() {
        return SystemClock.uptimeMillis() - this.mLastTranslucent < 1000;
    }

    public void adjustSystemUiVisibilityLw(int oldVis, int vis) {
        if (this.mWin != null && this.mTransientBarState == 2 && (this.mTransientFlag & vis) == 0) {
            setTransientBarState(3);
            setBarShowingLw(false);
        } else if (this.mWin != null && (this.mUnhideFlag & oldVis) != 0 && (this.mUnhideFlag & vis) == 0) {
            setBarShowingLw(true);
        }
    }

    public int applyTranslucentFlagLw(WindowManagerPolicy.WindowState win, int vis, int oldVis) {
        int vis2;
        if (this.mWin == null) {
            return vis;
        }
        if (win == null || (win.getAttrs().privateFlags & 512) != 0) {
            return ((~this.mTransparentFlag) & (((~this.mTranslucentFlag) & vis) | (this.mTranslucentFlag & oldVis))) | (this.mTransparentFlag & oldVis);
        }
        int fl = PolicyControl.getWindowFlags(win, null);
        if ((this.mTranslucentWmFlag & fl) != 0) {
            vis2 = vis | this.mTranslucentFlag;
        } else {
            vis2 = vis & (~this.mTranslucentFlag);
        }
        if ((Integer.MIN_VALUE & fl) == 0 || !isTransparentAllowed(win)) {
            return vis2 & (~this.mTransparentFlag);
        }
        return vis2 | this.mTransparentFlag;
    }

    /* access modifiers changed from: package-private */
    public boolean isTransparentAllowed(WindowManagerPolicy.WindowState win) {
        return win == null || !win.isLetterboxedOverlappingWith(this.mContentFrame);
    }

    public boolean setBarShowingLw(boolean show) {
        boolean change;
        int state;
        if (this.mWin == null) {
            return false;
        }
        boolean z = true;
        if (!show || this.mTransientBarState != 3) {
            WindowManagerInternal wmi = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            boolean isCoverOpen = wmi.isCoverOpen();
            boolean wasVis = this.mWin.isVisibleLw();
            boolean wasAnim = this.mWin.isAnimatingLw();
            if (isCoverOpen && wmi.isKeyguardLocked()) {
                this.mNoAnimationOnNextShow = true;
            }
            if (show) {
                change = this.mWin.showLw(!this.mNoAnimationOnNextShow && !skipAnimation());
            } else {
                change = this.mWin.hideLw(!this.mNoAnimationOnNextShow && !skipAnimation());
            }
            this.mNoAnimationOnNextShow = false;
            if (isCoverOpen) {
                state = computeStateLw(wasVis, wasAnim, this.mWin, change);
            } else {
                state = computeStateNoAnimLw(wasVis, wasAnim, this.mWin, change);
            }
            boolean stateChanged = updateStateLw(state);
            if (change && this.mVisibilityChangeListener != null) {
                this.mHandler.obtainMessage(1, show, 0).sendToTarget();
            } else if (PhoneWindowManager.NAV_TAG == this.mTag) {
                Slog.i(this.mTag, "show:" + show + ",change:" + change + ",mVisibilityChangeListener:" + this.mVisibilityChangeListener + ",mTransientBarState:" + this.mTransientBarState);
            }
            if (!change && !stateChanged) {
                z = false;
            }
            return z;
        }
        this.mPendingShow = true;
        return false;
    }

    private int computeStateNoAnimLw(boolean wasVis, boolean wasAnim, WindowManagerPolicy.WindowState win, boolean change) {
        if (win.hasDrawnLw()) {
            boolean vis = win.isVisibleLw();
            boolean isAnimatingLw = win.isAnimatingLw();
            if (this.mState == 0 && !vis) {
                return 2;
            }
            if (this.mState == 2 && vis) {
                return 0;
            }
        }
        return this.mState;
    }

    /* access modifiers changed from: package-private */
    public void setOnBarVisibilityChangedListener(OnBarVisibilityChangedListener listener, boolean invokeWithState) {
        this.mVisibilityChangeListener = listener;
        if (invokeWithState) {
            this.mHandler.obtainMessage(1, this.mState == 0 ? 1 : 0, 0).sendToTarget();
        }
    }

    /* access modifiers changed from: protected */
    public boolean skipAnimation() {
        return false;
    }

    private int computeStateLw(boolean wasVis, boolean wasAnim, WindowManagerPolicy.WindowState win, boolean change) {
        if (win.isDrawnLw()) {
            boolean vis = win.isVisibleLw();
            boolean anim = win.isAnimatingLw();
            if (this.mState == 1 && !change && !vis) {
                return 2;
            }
            if (this.mState == 2 && vis) {
                return 0;
            }
            if (change) {
                if (!wasVis || !vis || wasAnim || !anim) {
                    return 0;
                }
                return 1;
            }
        }
        return this.mState;
    }

    private boolean updateStateLw(final int state) {
        if (state == this.mState) {
            return false;
        }
        this.mState = state;
        this.mHandler.post(new Runnable() {
            public void run() {
                StatusBarManagerInternal statusbar = BarController.this.getStatusBarInternal();
                if (statusbar != null) {
                    statusbar.setWindowState(BarController.this.mStatusBarManagerId, state);
                }
            }
        });
        return true;
    }

    public boolean checkHiddenLw() {
        if (this.mWin != null && this.mWin.isDrawnLw()) {
            if (!this.mWin.isVisibleLw() && !this.mWin.isAnimatingLw()) {
                updateStateLw(2);
            }
            if (this.mTransientBarState == 3 && !this.mWin.isVisibleLw()) {
                setTransientBarState(0);
                if (this.mPendingShow) {
                    setBarShowingLw(true);
                    this.mPendingShow = false;
                }
                return true;
            }
        }
        return false;
    }

    public boolean checkShowTransientBarLw() {
        if (this.mTransientBarState == 2 || this.mTransientBarState == 1 || this.mWin == null || this.mWin.isDisplayedLw()) {
            return false;
        }
        return true;
    }

    public int updateVisibilityLw(boolean transientAllowed, int oldVis, int vis) {
        if (this.mWin == null) {
            return vis;
        }
        if (isTransientShowing() || isTransientShowRequested()) {
            if (transientAllowed) {
                vis |= this.mTransientFlag;
                if ((this.mTransientFlag & oldVis) == 0) {
                    vis |= this.mUnhideFlag;
                }
                setTransientBarState(2);
            } else {
                setTransientBarState(0);
            }
        }
        if (this.mShowTransparent) {
            vis |= this.mTransparentFlag;
            if (this.mSetUnHideFlagWhenNextTransparent) {
                vis |= this.mUnhideFlag;
                this.mSetUnHideFlagWhenNextTransparent = false;
            }
        }
        if (this.mTransientBarState != 0) {
            vis = (vis | this.mTransientFlag) & -2;
        }
        if (!((this.mTranslucentFlag & vis) == 0 && (this.mTranslucentFlag & oldVis) == 0 && ((vis | oldVis) & this.mTransparentFlag) == 0)) {
            this.mLastTranslucent = SystemClock.uptimeMillis();
        }
        return vis;
    }

    private void setTransientBarState(int state) {
        if (PhoneWindowManager.NAV_TAG == this.mTag) {
            String str = this.mTag;
            Slog.i(str, "setTransientBarState mWin:" + this.mWin + ",state:" + state + ",mTransientBarState:" + this.mTransientBarState);
        }
        if (this.mWin != null && state != this.mTransientBarState) {
            if (this.mTransientBarState == 2 || state == 2) {
                this.mLastTranslucent = SystemClock.uptimeMillis();
            }
            this.mTransientBarState = state;
        }
    }

    /* access modifiers changed from: protected */
    public StatusBarManagerInternal getStatusBarInternal() {
        StatusBarManagerInternal statusBarManagerInternal;
        synchronized (this.mServiceAquireLock) {
            if (this.mStatusBarInternal == null) {
                this.mStatusBarInternal = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
            }
            statusBarManagerInternal = this.mStatusBarInternal;
        }
        return statusBarManagerInternal;
    }

    private static String transientBarStateToString(int state) {
        if (state == 3) {
            return "TRANSIENT_BAR_HIDING";
        }
        if (state == 2) {
            return "TRANSIENT_BAR_SHOWING";
        }
        if (state == 1) {
            return "TRANSIENT_BAR_SHOW_REQUESTED";
        }
        if (state == 0) {
            return "TRANSIENT_BAR_NONE";
        }
        throw new IllegalArgumentException("Unknown state " + state);
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1159641169921L, this.mState);
        proto.write(1159641169922L, this.mTransientBarState);
        proto.end(token);
    }

    public void dump(PrintWriter pw, String prefix) {
        if (this.mWin != null) {
            pw.print(prefix);
            pw.println(this.mTag);
            pw.print(prefix);
            pw.print("  ");
            pw.print("mState");
            pw.print('=');
            pw.println(StatusBarManager.windowStateToString(this.mState));
            pw.print(prefix);
            pw.print("  ");
            pw.print("mTransientBar");
            pw.print('=');
            pw.println(transientBarStateToString(this.mTransientBarState));
            pw.print(prefix);
            pw.print("  mContentFrame=");
            pw.println(this.mContentFrame);
        }
    }

    public boolean isTransientHiding() {
        return this.mTransientBarState == 3;
    }

    public void sethwTransientBarState(int state) {
        setTransientBarState(state);
    }
}
