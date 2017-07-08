package com.android.server.policy;

import android.app.StatusBarManager;
import android.common.HwFrameworkFactory;
import android.os.Handler;
import android.os.SystemClock;
import android.view.WindowManagerPolicy.WindowState;
import com.android.server.LocalServices;
import com.android.server.statusbar.StatusBarManagerInternal;
import java.io.PrintWriter;

public class BarController {
    private static final boolean DEBUG = false;
    private static final int TRANSIENT_BAR_HIDING = 3;
    private static final int TRANSIENT_BAR_NONE = 0;
    private static final int TRANSIENT_BAR_SHOWING = 2;
    private static final int TRANSIENT_BAR_SHOW_REQUESTED = 1;
    private static final int TRANSLUCENT_ANIMATION_DELAY_MS = 1000;
    protected final Handler mHandler;
    private long mLastTranslucent;
    private boolean mNoAnimationOnNextShow;
    private boolean mPendingShow;
    private final Object mServiceAquireLock;
    private boolean mSetUnHideFlagWhenNextTransparent;
    private boolean mShowTransparent;
    private int mState;
    protected StatusBarManagerInternal mStatusBarInternal;
    private final int mStatusBarManagerId;
    protected final String mTag;
    private int mTransientBarState;
    private final int mTransientFlag;
    private final int mTranslucentFlag;
    private final int mTranslucentWmFlag;
    private final int mTransparentFlag;
    private final int mUnhideFlag;
    protected WindowState mWin;

    /* renamed from: com.android.server.policy.BarController.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ int val$state;

        AnonymousClass1(int val$state) {
            this.val$state = val$state;
        }

        public void run() {
            StatusBarManagerInternal statusbar = BarController.this.getStatusBarInternal();
            if (statusbar != null) {
                statusbar.setWindowState(BarController.this.mStatusBarManagerId, this.val$state);
            }
        }
    }

    public int applyTranslucentFlagLw(android.view.WindowManagerPolicy.WindowState r1, int r2, int r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.policy.BarController.applyTranslucentFlagLw(android.view.WindowManagerPolicy$WindowState, int, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.BarController.applyTranslucentFlagLw(android.view.WindowManagerPolicy$WindowState, int, int):int");
    }

    public BarController(String tag, int transientFlag, int unhideFlag, int translucentFlag, int statusBarManagerId, int translucentWmFlag, int transparentFlag) {
        this.mServiceAquireLock = new Object();
        this.mState = TRANSIENT_BAR_NONE;
        this.mTag = "BarController." + tag;
        this.mTransientFlag = transientFlag;
        this.mUnhideFlag = unhideFlag;
        this.mTranslucentFlag = translucentFlag;
        this.mStatusBarManagerId = statusBarManagerId;
        this.mTranslucentWmFlag = translucentWmFlag;
        this.mTransparentFlag = transparentFlag;
        this.mHandler = new Handler();
    }

    public void setWindow(WindowState win) {
        this.mWin = win;
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
            setTransientBarState(TRANSIENT_BAR_SHOW_REQUESTED);
        }
    }

    public boolean isTransientShowing() {
        return this.mTransientBarState == TRANSIENT_BAR_SHOWING ? true : DEBUG;
    }

    public boolean isTransientShowRequested() {
        return this.mTransientBarState == TRANSIENT_BAR_SHOW_REQUESTED ? true : DEBUG;
    }

    public boolean wasRecentlyTranslucent() {
        return SystemClock.uptimeMillis() - this.mLastTranslucent < 1000 ? true : DEBUG;
    }

    public void adjustSystemUiVisibilityLw(int oldVis, int vis) {
        if (this.mWin != null && this.mTransientBarState == TRANSIENT_BAR_SHOWING && (this.mTransientFlag & vis) == 0) {
            setTransientBarState(TRANSIENT_BAR_HIDING);
            setBarShowingLw(DEBUG);
        } else if (this.mWin != null && (this.mUnhideFlag & oldVis) != 0 && (this.mUnhideFlag & vis) == 0) {
            setBarShowingLw(true);
        }
    }

    public boolean setBarShowingLw(boolean show) {
        boolean z = true;
        if (this.mWin == null) {
            return DEBUG;
        }
        if (show && this.mTransientBarState == TRANSIENT_BAR_HIDING) {
            this.mPendingShow = true;
            return DEBUG;
        }
        boolean change;
        int state;
        boolean isCoverOpen = HwFrameworkFactory.getCoverManager().isCoverOpen();
        boolean wasVis = this.mWin.isVisibleLw();
        boolean wasAnim = this.mWin.isAnimatingLw();
        if (isCoverOpen) {
            this.mNoAnimationOnNextShow = true;
        }
        WindowState windowState;
        boolean z2;
        if (show) {
            windowState = this.mWin;
            if (this.mNoAnimationOnNextShow || skipAnimation()) {
                z2 = DEBUG;
            } else {
                z2 = true;
            }
            change = windowState.showLw(z2);
        } else {
            windowState = this.mWin;
            z2 = (this.mNoAnimationOnNextShow || skipAnimation()) ? DEBUG : true;
            change = windowState.hideLw(z2);
        }
        this.mNoAnimationOnNextShow = DEBUG;
        if (isCoverOpen) {
            state = computeStateLw(wasVis, wasAnim, this.mWin, change);
        } else {
            state = computeStateNoAnimLw(wasVis, wasAnim, this.mWin, change);
        }
        boolean stateChanged = updateStateLw(state);
        if (!change) {
            z = stateChanged;
        }
        return z;
    }

    private int computeStateNoAnimLw(boolean wasVis, boolean wasAnim, WindowState win, boolean change) {
        if (win.hasDrawnLw()) {
            boolean vis = win.isVisibleLw();
            boolean anim = win.isAnimatingLw();
            if (this.mState == 0 && !vis) {
                return TRANSIENT_BAR_SHOWING;
            }
            if (this.mState == TRANSIENT_BAR_SHOWING && vis) {
                return TRANSIENT_BAR_NONE;
            }
        }
        return this.mState;
    }

    protected boolean skipAnimation() {
        return DEBUG;
    }

    private int computeStateLw(boolean wasVis, boolean wasAnim, WindowState win, boolean change) {
        if (win.isDrawnLw()) {
            boolean vis = win.isVisibleLw();
            boolean anim = win.isAnimatingLw();
            if (this.mState == TRANSIENT_BAR_SHOW_REQUESTED && !change && !vis) {
                return TRANSIENT_BAR_SHOWING;
            }
            if (this.mState == TRANSIENT_BAR_SHOWING && vis) {
                return TRANSIENT_BAR_NONE;
            }
            if (change) {
                return (wasVis && vis && !wasAnim && anim) ? TRANSIENT_BAR_SHOW_REQUESTED : TRANSIENT_BAR_NONE;
            }
        }
        return this.mState;
    }

    private boolean updateStateLw(int state) {
        if (state == this.mState) {
            return DEBUG;
        }
        this.mState = state;
        this.mHandler.post(new AnonymousClass1(state));
        return true;
    }

    public boolean checkHiddenLw() {
        if (this.mWin != null && this.mWin.isDrawnLw()) {
            if (!(this.mWin.isVisibleLw() || this.mWin.isAnimatingLw())) {
                updateStateLw(TRANSIENT_BAR_SHOWING);
            }
            if (this.mTransientBarState == TRANSIENT_BAR_HIDING && !this.mWin.isVisibleLw()) {
                setTransientBarState(TRANSIENT_BAR_NONE);
                if (this.mPendingShow) {
                    setBarShowingLw(true);
                    this.mPendingShow = DEBUG;
                }
                return true;
            }
        }
        return DEBUG;
    }

    public boolean checkShowTransientBarLw() {
        return (this.mTransientBarState == TRANSIENT_BAR_SHOWING || this.mTransientBarState == TRANSIENT_BAR_SHOW_REQUESTED || this.mWin == null || this.mWin.isDisplayedLw()) ? DEBUG : true;
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
                setTransientBarState(TRANSIENT_BAR_SHOWING);
            } else {
                setTransientBarState(TRANSIENT_BAR_NONE);
            }
        }
        if (this.mShowTransparent) {
            vis |= this.mTransparentFlag;
            if (this.mSetUnHideFlagWhenNextTransparent) {
                vis |= this.mUnhideFlag;
                this.mSetUnHideFlagWhenNextTransparent = DEBUG;
            }
        }
        if (this.mTransientBarState != 0) {
            vis = (vis | this.mTransientFlag) & -2;
        }
        if ((this.mTranslucentFlag & vis) == 0 && (this.mTranslucentFlag & oldVis) == 0) {
            if (((vis | oldVis) & this.mTransparentFlag) != 0) {
            }
            return vis;
        }
        this.mLastTranslucent = SystemClock.uptimeMillis();
        return vis;
    }

    private void setTransientBarState(int state) {
        if (this.mWin != null && state != this.mTransientBarState) {
            if (this.mTransientBarState == TRANSIENT_BAR_SHOWING || state == TRANSIENT_BAR_SHOWING) {
                this.mLastTranslucent = SystemClock.uptimeMillis();
            }
            this.mTransientBarState = state;
        }
    }

    protected StatusBarManagerInternal getStatusBarInternal() {
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
        if (state == TRANSIENT_BAR_HIDING) {
            return "TRANSIENT_BAR_HIDING";
        }
        if (state == TRANSIENT_BAR_SHOWING) {
            return "TRANSIENT_BAR_SHOWING";
        }
        if (state == TRANSIENT_BAR_SHOW_REQUESTED) {
            return "TRANSIENT_BAR_SHOW_REQUESTED";
        }
        if (state == 0) {
            return "TRANSIENT_BAR_NONE";
        }
        throw new IllegalArgumentException("Unknown state " + state);
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
        }
    }

    public boolean isTransientHiding() {
        return this.mTransientBarState == TRANSIENT_BAR_HIDING ? true : DEBUG;
    }

    public void sethwTransientBarState(int state) {
        setTransientBarState(state);
    }
}
