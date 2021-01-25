package com.android.server.wm;

import android.app.StatusBarManager;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.HwPCUtils;
import android.util.proto.ProtoOutputStream;
import com.android.server.LocalServices;
import com.android.server.UiThread;
import com.android.server.statusbar.StatusBarManagerInternal;
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
    protected final int mDisplayId;
    protected final Handler mHandler;
    private long mLastTranslucent;
    private boolean mNoAnimationOnNextShow;
    private boolean mPendingShow;
    private final Object mServiceAquireLock = new Object();
    private boolean mSetUnHideFlagWhenNextTransparent;
    private boolean mShowTransparent;
    private int mState = 0;
    private StatusBarManagerInternal mStatusBarInternal;
    private final int mStatusBarManagerId;
    protected final String mTag;
    private int mTransientBarState;
    private final int mTransientFlag;
    private final int mTranslucentFlag;
    private final int mTranslucentWmFlag;
    private final int mTransparentFlag;
    private final int mUnhideFlag;
    private OnBarVisibilityChangedListener mVisibilityChangeListener;
    protected WindowState mWin;

    /* access modifiers changed from: package-private */
    public interface OnBarVisibilityChangedListener {
        void onBarVisibilityChanged(boolean z);
    }

    BarController(String tag, int displayId, int transientFlag, int unhideFlag, int translucentFlag, int statusBarManagerId, int translucentWmFlag, int transparentFlag) {
        this.mTag = "BarController." + tag;
        this.mDisplayId = displayId;
        this.mTransientFlag = transientFlag;
        this.mUnhideFlag = unhideFlag;
        this.mTranslucentFlag = translucentFlag;
        this.mStatusBarManagerId = statusBarManagerId;
        this.mTranslucentWmFlag = translucentWmFlag;
        this.mTransparentFlag = transparentFlag;
        this.mHandler = new BarHandler();
    }

    /* access modifiers changed from: package-private */
    public void setWindow(WindowState win) {
        this.mWin = win;
    }

    /* access modifiers changed from: package-private */
    public void setContentFrame(Rect frame) {
        this.mContentFrame.set(frame);
    }

    /* access modifiers changed from: package-private */
    public void setShowTransparent(boolean transparent) {
        if (transparent != this.mShowTransparent) {
            this.mShowTransparent = transparent;
            this.mSetUnHideFlagWhenNextTransparent = transparent;
            this.mNoAnimationOnNextShow = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void showTransient() {
        if (this.mWin != null) {
            setTransientBarState(1);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isTransientShowing() {
        return this.mTransientBarState == 2;
    }

    /* access modifiers changed from: package-private */
    public boolean isTransientShowRequested() {
        return this.mTransientBarState == 1;
    }

    /* access modifiers changed from: package-private */
    public boolean wasRecentlyTranslucent() {
        return SystemClock.uptimeMillis() - this.mLastTranslucent < 1000;
    }

    /* access modifiers changed from: package-private */
    public void adjustSystemUiVisibilityLw(int oldVis, int vis) {
        if (this.mWin != null && this.mTransientBarState == 2 && (this.mTransientFlag & vis) == 0) {
            setTransientBarState(3);
            setBarShowingLw(false);
        } else if (this.mWin != null) {
            int i = this.mUnhideFlag;
            if ((oldVis & i) != 0 && (i & vis) == 0) {
                setBarShowingLw(true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int applyTranslucentFlagLw(WindowState win, int vis, int oldVis) {
        int vis2;
        if (this.mWin == null) {
            return vis;
        }
        if (win == null || (win.getAttrs().privateFlags & 512) != 0) {
            int i = this.mTranslucentFlag;
            int vis3 = ((~i) & vis) | (i & oldVis);
            int i2 = this.mTransparentFlag;
            return ((~i2) & vis3) | (i2 & oldVis);
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
    public boolean isTransparentAllowed(WindowState win) {
        return win == null || !win.isLetterboxedOverlappingWith(this.mContentFrame);
    }

    /* access modifiers changed from: package-private */
    public boolean setBarShowingLw(boolean show) {
        boolean change;
        int state;
        if (this.mWin == null) {
            return false;
        }
        if (!show || this.mTransientBarState != 3) {
            if (this.mWin.mAttrs.type == 2000) {
                this.mWin.mWmService.mAtmService.mHwATMSEx.adjustHwFreeformPosIfNeed(this.mWin.mDisplayContent, show && this.mTransientBarState != 2 && (this.mWin.mAttrs.privateFlags & 4096) == 0);
            }
            WindowManagerInternal wmi = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && !wmi.isKeyguardLocked() && this.mWin.mAttrs.type == 2000) {
                return false;
            }
            boolean isCoverOpen = wmi.isCoverOpen();
            boolean wasVis = this.mWin.isVisibleLw();
            boolean wasAnim = this.mWin.isAnimatingLw();
            if (isCoverOpen && wmi.isKeyguardLocked()) {
                this.mNoAnimationOnNextShow = true;
            }
            boolean skipAnim = skipAnimation();
            if (show) {
                change = this.mWin.showLw(!this.mNoAnimationOnNextShow && !skipAnim);
            } else {
                change = this.mWin.hideLw(!this.mNoAnimationOnNextShow && !skipAnim);
            }
            this.mNoAnimationOnNextShow = false;
            if (isCoverOpen) {
                state = computeStateLw(wasVis, wasAnim, this.mWin, change);
            } else {
                state = computeStateNoAnimLw(wasVis, wasAnim, this.mWin, change);
            }
            boolean stateChanged = updateStateLw(state);
            if (change && this.mVisibilityChangeListener != null) {
                this.mHandler.obtainMessage(1, show ? 1 : 0, 0).sendToTarget();
            }
            if (change || stateChanged) {
                return true;
            }
            return false;
        }
        this.mPendingShow = true;
        return false;
    }

    private int computeStateNoAnimLw(boolean wasVis, boolean wasAnim, WindowState win, boolean change) {
        if (win.hasDrawnLw()) {
            boolean vis = win.isVisibleLw();
            win.isAnimatingLw();
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
        return !this.mWin.isDrawnLw();
    }

    private int computeStateLw(boolean wasVis, boolean wasAnim, WindowState win, boolean change) {
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
        if (this.mWin == null || state == this.mState) {
            return false;
        }
        this.mState = state;
        this.mHandler.post(new Runnable() {
            /* class com.android.server.wm.BarController.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                StatusBarManagerInternal statusbar = BarController.this.getStatusBarInternal();
                if (statusbar != null) {
                    statusbar.setWindowState(BarController.this.mDisplayId, BarController.this.mStatusBarManagerId, state);
                }
            }
        });
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean checkHiddenLw() {
        WindowState windowState = this.mWin;
        if (windowState != null && windowState.isDrawnLw()) {
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

    /* access modifiers changed from: package-private */
    public boolean checkShowTransientBarLw() {
        WindowState windowState;
        int i = this.mTransientBarState;
        if (i == 2 || i == 1 || (windowState = this.mWin) == null || windowState.isDisplayedLw()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public int updateVisibilityLw(boolean transientAllowed, int oldVis, int vis) {
        if (this.mWin == null) {
            return vis;
        }
        if (isTransientShowing() || isTransientShowRequested()) {
            if (transientAllowed) {
                int i = this.mTransientFlag;
                vis |= i;
                if ((i & oldVis) == 0) {
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
        int i2 = this.mTranslucentFlag;
        if (!((vis & i2) == 0 && (i2 & oldVis) == 0 && ((vis | oldVis) & this.mTransparentFlag) == 0)) {
            this.mLastTranslucent = SystemClock.uptimeMillis();
        }
        return vis;
    }

    private void setTransientBarState(int state) {
        int i;
        if (this.mWin != null && state != (i = this.mTransientBarState)) {
            if (i == 2 || state == 2) {
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

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1159641169921L, this.mState);
        proto.write(1159641169922L, this.mTransientBarState);
        proto.end(token);
    }

    /* access modifiers changed from: package-private */
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

    private class BarHandler extends Handler {
        BarHandler() {
            super(UiThread.getHandler().getLooper());
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            boolean visible = true;
            if (msg.what == 1) {
                if (msg.arg1 == 0) {
                    visible = false;
                }
                if (BarController.this.mVisibilityChangeListener != null) {
                    BarController.this.mVisibilityChangeListener.onBarVisibilityChanged(visible);
                }
            }
        }
    }
}
