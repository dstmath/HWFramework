package com.android.server.wm;

import android.app.ActivityManager.StackId;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Slog;
import android.view.IApplicationToken;
import android.view.View;
import com.android.server.input.InputApplicationHandle;
import com.android.server.wm.WindowManagerService.H;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;

class AppWindowToken extends WindowToken {
    private static final String TAG = null;
    final WindowList allAppWindows;
    boolean allDrawn;
    boolean allDrawnExcludingSaved;
    boolean appFullscreen;
    final IApplicationToken appToken;
    boolean clientHidden;
    boolean deferClearAllDrawn;
    boolean firstWindowDrawn;
    boolean hiddenRequested;
    boolean inPendingTransaction;
    long inputDispatchingTimeoutNanos;
    long lastTransactionSequence;
    boolean layoutConfigChanges;
    boolean mAlwaysFocusable;
    final AppWindowAnimator mAppAnimator;
    boolean mAppStopped;
    boolean mEnteringAnimation;
    ArrayDeque<Rect> mFrozenBounds;
    ArrayDeque<Configuration> mFrozenMergedConfig;
    final InputApplicationHandle mInputApplicationHandle;
    boolean mIsExiting;
    boolean mLaunchTaskBehind;
    int mPendingRelaunchCount;
    private ArrayList<SurfaceControlWithBackground> mSurfaceViewBackgrounds;
    Task mTask;
    boolean navigationBarHide;
    int numDrawnWindows;
    int numDrawnWindowsExclusingSaved;
    int numInterestingWindows;
    int numInterestingWindowsExcludingSaved;
    boolean removed;
    boolean reportedDrawn;
    boolean reportedVisible;
    int requestedOrientation;
    boolean showForAllUsers;
    StartingData startingData;
    boolean startingDisplayed;
    boolean startingMoved;
    View startingView;
    WindowState startingWindow;
    int targetSdk;
    final boolean voiceInteraction;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.AppWindowToken.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.AppWindowToken.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.AppWindowToken.<clinit>():void");
    }

    AppWindowToken(WindowManagerService _service, IApplicationToken _token, boolean _voiceInteraction) {
        super(_service, _token.asBinder(), 2, true);
        this.allAppWindows = new WindowList();
        this.requestedOrientation = -1;
        this.lastTransactionSequence = Long.MIN_VALUE;
        this.mSurfaceViewBackgrounds = new ArrayList();
        this.mFrozenBounds = new ArrayDeque();
        this.mFrozenMergedConfig = new ArrayDeque();
        this.appWindowToken = this;
        this.appToken = _token;
        this.voiceInteraction = _voiceInteraction;
        this.mInputApplicationHandle = new InputApplicationHandle(this);
        this.mAppAnimator = new AppWindowAnimator(this);
    }

    void sendAppVisibilityToClients() {
        int N = this.allAppWindows.size();
        for (int i = 0; i < N; i++) {
            WindowState win = (WindowState) this.allAppWindows.get(i);
            if (win != this.startingWindow || !this.clientHidden) {
                try {
                    win.mClient.dispatchAppVisibility(!this.clientHidden);
                } catch (RemoteException e) {
                }
            }
        }
    }

    void setVisibleBeforeClientHidden() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            ((WindowState) this.allAppWindows.get(i)).setVisibleBeforeClientHidden();
        }
    }

    void onFirstWindowDrawn(WindowState win, WindowStateAnimator winAnimator) {
        this.firstWindowDrawn = true;
        removeAllDeadWindows();
        if (this.startingData != null) {
            winAnimator.clearAnimation();
            winAnimator.mService.mFinishedStarting.add(this);
            winAnimator.mService.mH.sendEmptyMessage(7);
        }
        updateReportedVisibilityLocked();
    }

    void updateReportedVisibilityLocked() {
        int i = 1;
        if (this.appToken != null) {
            int numInteresting = 0;
            int numVisible = 0;
            int numDrawn = 0;
            boolean nowGone = true;
            int N = this.allAppWindows.size();
            for (int i2 = 0; i2 < N; i2++) {
                WindowState win = (WindowState) this.allAppWindows.get(i2);
                if (!(win == this.startingWindow || win.mAppFreezing || win.mViewVisibility != 0 || win.mAttrs.type == 3 || win.mDestroying)) {
                    numInteresting++;
                    if (win.isDrawnLw()) {
                        numDrawn++;
                        if (!win.mWinAnimator.isAnimationSet()) {
                            numVisible++;
                        }
                        nowGone = false;
                    } else if (win.mWinAnimator.isAnimationSet()) {
                        nowGone = false;
                    }
                }
            }
            boolean z = numInteresting > 0 && numDrawn >= numInteresting;
            boolean nowVisible = numInteresting > 0 && numVisible >= numInteresting;
            if (!nowGone) {
                if (!z) {
                    z = this.reportedDrawn;
                }
                if (!nowVisible) {
                    nowVisible = this.reportedVisible;
                }
            }
            if (z != this.reportedDrawn) {
                if (z) {
                    this.service.mH.sendMessage(this.service.mH.obtainMessage(9, this));
                }
                this.reportedDrawn = z;
            }
            if (nowVisible != this.reportedVisible) {
                int i3;
                this.reportedVisible = nowVisible;
                H h = this.service.mH;
                if (nowVisible) {
                    i3 = 1;
                } else {
                    i3 = 0;
                }
                if (!nowGone) {
                    i = 0;
                }
                this.service.mH.sendMessage(h.obtainMessage(8, i3, i, this));
            }
        }
    }

    WindowState findMainWindow() {
        WindowState candidate = null;
        int j = this.windows.size();
        while (j > 0) {
            j--;
            WindowState win = (WindowState) this.windows.get(j);
            if (win.mAttrs.type == 1 || win.mAttrs.type == 3) {
                if (!win.mAnimatingExit) {
                    return win;
                }
                candidate = win;
            }
        }
        return candidate;
    }

    boolean windowsAreFocusable() {
        return !StackId.canReceiveKeys(this.mTask.mStack.mStackId) ? this.mAlwaysFocusable : true;
    }

    boolean isVisible() {
        int N = this.allAppWindows.size();
        for (int i = 0; i < N; i++) {
            WindowState win = (WindowState) this.allAppWindows.get(i);
            if (!win.mAppFreezing && ((win.mViewVisibility == 0 || win.isAnimatingWithSavedSurface() || (win.mWinAnimator.isAnimationSet() && !this.service.mAppTransition.isTransitionSet())) && !win.mDestroying && win.isDrawnLw())) {
                return true;
            }
        }
        return false;
    }

    void removeAppFromTaskLocked() {
        this.mIsExiting = false;
        removeAllWindows();
        Task task = this.mTask;
        if (task != null) {
            if (!task.removeAppToken(this)) {
                Slog.e(TAG, "removeAppFromTaskLocked: token=" + this + " not found.");
            }
            task.mStack.mExitingAppTokens.remove(this);
        }
    }

    void destroySurfaces() {
        int i;
        ArrayList<WindowState> allWindows = (ArrayList) this.allAppWindows.clone();
        DisplayContentList displayList = new DisplayContentList();
        for (i = allWindows.size() - 1; i >= 0; i--) {
            boolean z;
            DisplayContent displayContent;
            WindowState win = (WindowState) allWindows.get(i);
            if (this.mAppStopped) {
                z = true;
            } else {
                z = win.mWindowRemovalAllowed;
            }
            if (z) {
                win.mWinAnimator.destroyPreservedSurfaceLocked();
                if (win.mDestroying) {
                    win.destroyOrSaveSurface();
                    if (win.mRemoveOnExit) {
                        this.service.removeWindowInnerLocked(win);
                    }
                    displayContent = win.getDisplayContent();
                    if (!(displayContent == null || displayList.contains(displayContent))) {
                        displayList.add(displayContent);
                    }
                    win.mDestroying = false;
                }
            }
        }
        for (i = 0; i < displayList.size(); i++) {
            displayContent = (DisplayContent) displayList.get(i);
            this.service.mLayersController.assignLayersLocked(displayContent.getWindowList());
            displayContent.layoutNeeded = true;
        }
    }

    void notifyAppStopped(boolean stopped) {
        this.mAppStopped = stopped;
        if (stopped) {
            destroySurfaces();
            this.mTask.mService.scheduleRemoveStartingWindowLocked(this);
        }
    }

    boolean shouldSaveSurface() {
        return this.allDrawn;
    }

    boolean canRestoreSurfaces() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            if (((WindowState) this.allAppWindows.get(i)).canRestoreSurface()) {
                return true;
            }
        }
        return false;
    }

    void clearVisibleBeforeClientHidden() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            ((WindowState) this.allAppWindows.get(i)).clearVisibleBeforeClientHidden();
        }
    }

    boolean isAnimatingInvisibleWithSavedSurface() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            if (((WindowState) this.allAppWindows.get(i)).isAnimatingInvisibleWithSavedSurface()) {
                return true;
            }
        }
        return false;
    }

    void stopUsingSavedSurfaceLocked() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            WindowState w = (WindowState) this.allAppWindows.get(i);
            if (w.isAnimatingInvisibleWithSavedSurface()) {
                w.clearAnimatingWithSavedSurface();
                w.mDestroying = true;
                w.mWinAnimator.hide("stopUsingSavedSurfaceLocked");
                w.mWinAnimator.mWallpaperControllerLocked.hideWallpapers(w);
            }
        }
        destroySurfaces();
    }

    void markSavedSurfaceExiting() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            WindowState w = (WindowState) this.allAppWindows.get(i);
            if (w.isAnimatingInvisibleWithSavedSurface()) {
                w.mAnimatingExit = true;
                w.mWinAnimator.mAnimating = true;
            }
        }
    }

    void restoreSavedSurfaces() {
        boolean z = false;
        if (canRestoreSurfaces()) {
            int numInteresting = 0;
            int numDrawn = 0;
            for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
                WindowState w = (WindowState) this.allAppWindows.get(i);
                if (!(w == this.startingWindow || w.mAppDied || !w.wasVisibleBeforeClientHidden() || (this.mAppAnimator.freezingScreen && w.mAppFreezing))) {
                    numInteresting++;
                    if (w.hasSavedSurface()) {
                        w.restoreSavedSurface();
                    }
                    if (w.isDrawnLw()) {
                        numDrawn++;
                    }
                }
            }
            if (!this.allDrawn) {
                if (numInteresting > 0 && numInteresting == numDrawn) {
                    z = true;
                }
                this.allDrawn = z;
                if (this.allDrawn) {
                    this.service.mH.obtainMessage(32, this.token).sendToTarget();
                }
            }
            clearVisibleBeforeClientHidden();
            return;
        }
        clearVisibleBeforeClientHidden();
    }

    void destroySavedSurfaces() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            ((WindowState) this.allAppWindows.get(i)).destroySavedSurface();
        }
    }

    void clearAllDrawn() {
        this.allDrawn = false;
        this.deferClearAllDrawn = false;
        this.allDrawnExcludingSaved = false;
    }

    void removeAllWindows() {
        int winNdx = this.allAppWindows.size() - 1;
        while (winNdx >= 0) {
            try {
                this.service.removeWindowLocked((WindowState) this.allAppWindows.get(winNdx));
            } catch (IndexOutOfBoundsException e) {
                Slog.e(TAG, "Error while removing window : " + e);
            }
            winNdx = Math.min(winNdx - 1, this.allAppWindows.size() - 1);
        }
        this.allAppWindows.clear();
        this.windows.clear();
    }

    void removeAllDeadWindows() {
        int winNdx = this.allAppWindows.size() - 1;
        while (winNdx >= 0) {
            WindowState win = (WindowState) this.allAppWindows.get(winNdx);
            if (win.mAppDied) {
                win.mDestroying = true;
                this.service.removeWindowLocked(win);
            }
            winNdx = Math.min(winNdx - 1, this.allAppWindows.size() - 1);
        }
    }

    boolean hasWindowsAlive() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            if (!((WindowState) this.allAppWindows.get(i)).mAppDied) {
                return true;
            }
        }
        return false;
    }

    void setReplacingWindows(boolean animate) {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            ((WindowState) this.allAppWindows.get(i)).setReplacing(animate);
        }
        if (animate) {
            this.mAppAnimator.setDummyAnimation();
        }
    }

    void setReplacingChildren() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            WindowState w = (WindowState) this.allAppWindows.get(i);
            if (w.shouldBeReplacedWithChildren()) {
                w.setReplacing(false);
            }
        }
    }

    void resetReplacingWindows() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            ((WindowState) this.allAppWindows.get(i)).resetReplacing();
        }
    }

    void requestUpdateWallpaperIfNeeded() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            ((WindowState) this.allAppWindows.get(i)).requestUpdateWallpaperIfNeeded();
        }
    }

    boolean isRelaunching() {
        return this.mPendingRelaunchCount > 0;
    }

    void startRelaunching() {
        if (canFreezeBounds()) {
            freezeBounds();
        }
        this.mPendingRelaunchCount++;
    }

    void finishRelaunching() {
        if (canFreezeBounds()) {
            unfreezeBounds();
        }
        if (this.mPendingRelaunchCount > 0) {
            this.mPendingRelaunchCount--;
        }
    }

    void addWindow(WindowState w) {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            WindowState candidate = (WindowState) this.allAppWindows.get(i);
            if (candidate.mWillReplaceWindow && candidate.mReplacingWindow == null && candidate.getWindowTag().toString().equals(w.getWindowTag().toString())) {
                candidate.mReplacingWindow = w;
                w.mSkipEnterAnimationForSeamlessReplacement = !candidate.mAnimateReplacingWindow;
                this.service.scheduleReplacingWindowTimeouts(this);
            }
        }
        this.allAppWindows.add(w);
    }

    boolean waitingForReplacement() {
        for (int i = this.allAppWindows.size() - 1; i >= 0; i--) {
            if (((WindowState) this.allAppWindows.get(i)).mWillReplaceWindow) {
                return true;
            }
        }
        return false;
    }

    void clearTimedoutReplacesLocked() {
        int i = this.allAppWindows.size() - 1;
        while (i >= 0) {
            WindowState candidate = (WindowState) this.allAppWindows.get(i);
            if (candidate.mWillReplaceWindow) {
                candidate.mWillReplaceWindow = false;
                if (candidate.mReplacingWindow != null) {
                    candidate.mReplacingWindow.mSkipEnterAnimationForSeamlessReplacement = false;
                }
                this.service.removeWindowInnerLocked(candidate);
            }
            i = Math.min(i - 1, this.allAppWindows.size() - 1);
        }
    }

    private boolean canFreezeBounds() {
        return (this.mTask == null || this.mTask.inFreeformWorkspace()) ? false : true;
    }

    private void freezeBounds() {
        this.mFrozenBounds.offer(new Rect(this.mTask.mPreparedFrozenBounds));
        if (this.mTask.mPreparedFrozenMergedConfig.equals(Configuration.EMPTY)) {
            Configuration config = new Configuration(this.service.mCurConfiguration);
            config.updateFrom(this.mTask.mOverrideConfig);
            this.mFrozenMergedConfig.offer(config);
        } else {
            this.mFrozenMergedConfig.offer(new Configuration(this.mTask.mPreparedFrozenMergedConfig));
        }
        this.mTask.mPreparedFrozenMergedConfig.setToDefaults();
    }

    private void unfreezeBounds() {
        this.mFrozenBounds.remove();
        this.mFrozenMergedConfig.remove();
        for (int i = this.windows.size() - 1; i >= 0; i--) {
            WindowState win = (WindowState) this.windows.get(i);
            if (win.mHasSurface) {
                win.mLayoutNeeded = true;
                win.setDisplayLayoutNeeded();
                if (!this.service.mResizingWindows.contains(win)) {
                    this.service.mResizingWindows.add(win);
                }
            }
        }
        this.service.mWindowPlacerLocked.performSurfacePlacement();
    }

    void addSurfaceViewBackground(SurfaceControlWithBackground background) {
        this.mSurfaceViewBackgrounds.add(background);
    }

    void removeSurfaceViewBackground(SurfaceControlWithBackground background) {
        this.mSurfaceViewBackgrounds.remove(background);
        updateSurfaceViewBackgroundVisibilities();
    }

    void updateSurfaceViewBackgroundVisibilities() {
        int i;
        SurfaceControlWithBackground bottom = null;
        int bottomLayer = Integer.MAX_VALUE;
        for (i = 0; i < this.mSurfaceViewBackgrounds.size(); i++) {
            SurfaceControlWithBackground sc = (SurfaceControlWithBackground) this.mSurfaceViewBackgrounds.get(i);
            if (sc.mVisible && sc.mLayer < bottomLayer) {
                bottomLayer = sc.mLayer;
                bottom = sc;
            }
        }
        for (i = 0; i < this.mSurfaceViewBackgrounds.size(); i++) {
            sc = (SurfaceControlWithBackground) this.mSurfaceViewBackgrounds.get(i);
            sc.updateBackgroundVisibility(sc != bottom);
        }
    }

    void dump(PrintWriter pw, String prefix) {
        super.dump(pw, prefix);
        if (this.appToken != null) {
            pw.print(prefix);
            pw.print("app=true voiceInteraction=");
            pw.println(this.voiceInteraction);
        }
        if (this.allAppWindows.size() > 0) {
            pw.print(prefix);
            pw.print("allAppWindows=");
            pw.println(this.allAppWindows);
        }
        pw.print(prefix);
        pw.print("task=");
        pw.println(this.mTask);
        pw.print(prefix);
        pw.print(" appFullscreen=");
        pw.print(this.appFullscreen);
        pw.print(" requestedOrientation=");
        pw.println(this.requestedOrientation);
        pw.print(prefix);
        pw.print("hiddenRequested=");
        pw.print(this.hiddenRequested);
        pw.print(" clientHidden=");
        pw.print(this.clientHidden);
        pw.print(" reportedDrawn=");
        pw.print(this.reportedDrawn);
        pw.print(" reportedVisible=");
        pw.println(this.reportedVisible);
        if (this.paused) {
            pw.print(prefix);
            pw.print("paused=");
            pw.println(this.paused);
        }
        if (this.mAppStopped) {
            pw.print(prefix);
            pw.print("mAppStopped=");
            pw.println(this.mAppStopped);
        }
        if (this.numInterestingWindows == 0 && this.numDrawnWindows == 0 && !this.allDrawn) {
            if (this.mAppAnimator.allDrawn) {
            }
            if (this.inPendingTransaction) {
                pw.print(prefix);
                pw.print("inPendingTransaction=");
                pw.println(this.inPendingTransaction);
            }
            if (this.startingData != null || this.removed || this.firstWindowDrawn || this.mIsExiting) {
                pw.print(prefix);
                pw.print("startingData=");
                pw.print(this.startingData);
                pw.print(" removed=");
                pw.print(this.removed);
                pw.print(" firstWindowDrawn=");
                pw.print(this.firstWindowDrawn);
                pw.print(" mIsExiting=");
                pw.println(this.mIsExiting);
            }
            if (this.startingWindow == null && this.startingView == null && !this.startingDisplayed) {
                if (this.startingMoved) {
                }
                if (!this.mFrozenBounds.isEmpty()) {
                    pw.print(prefix);
                    pw.print("mFrozenBounds=");
                    pw.println(this.mFrozenBounds);
                    pw.print(prefix);
                    pw.print("mFrozenMergedConfig=");
                    pw.println(this.mFrozenMergedConfig);
                }
                if (this.mPendingRelaunchCount == 0) {
                    pw.print(prefix);
                    pw.print("mPendingRelaunchCount=");
                    pw.println(this.mPendingRelaunchCount);
                }
            }
            pw.print(prefix);
            pw.print("startingWindow=");
            pw.print(this.startingWindow);
            pw.print(" startingView=");
            pw.print(this.startingView);
            pw.print(" startingDisplayed=");
            pw.print(this.startingDisplayed);
            pw.print(" startingMoved=");
            pw.println(this.startingMoved);
            if (this.mFrozenBounds.isEmpty()) {
                pw.print(prefix);
                pw.print("mFrozenBounds=");
                pw.println(this.mFrozenBounds);
                pw.print(prefix);
                pw.print("mFrozenMergedConfig=");
                pw.println(this.mFrozenMergedConfig);
            }
            if (this.mPendingRelaunchCount == 0) {
                pw.print(prefix);
                pw.print("mPendingRelaunchCount=");
                pw.println(this.mPendingRelaunchCount);
            }
        }
        pw.print(prefix);
        pw.print("numInterestingWindows=");
        pw.print(this.numInterestingWindows);
        pw.print(" numDrawnWindows=");
        pw.print(this.numDrawnWindows);
        pw.print(" inPendingTransaction=");
        pw.print(this.inPendingTransaction);
        pw.print(" allDrawn=");
        pw.print(this.allDrawn);
        pw.print(" (animator=");
        pw.print(this.mAppAnimator.allDrawn);
        pw.println(")");
        if (this.inPendingTransaction) {
            pw.print(prefix);
            pw.print("inPendingTransaction=");
            pw.println(this.inPendingTransaction);
        }
        pw.print(prefix);
        pw.print("startingData=");
        pw.print(this.startingData);
        pw.print(" removed=");
        pw.print(this.removed);
        pw.print(" firstWindowDrawn=");
        pw.print(this.firstWindowDrawn);
        pw.print(" mIsExiting=");
        pw.println(this.mIsExiting);
        if (this.startingMoved) {
            pw.print(prefix);
            pw.print("startingWindow=");
            pw.print(this.startingWindow);
            pw.print(" startingView=");
            pw.print(this.startingView);
            pw.print(" startingDisplayed=");
            pw.print(this.startingDisplayed);
            pw.print(" startingMoved=");
            pw.println(this.startingMoved);
        }
        if (this.mFrozenBounds.isEmpty()) {
            pw.print(prefix);
            pw.print("mFrozenBounds=");
            pw.println(this.mFrozenBounds);
            pw.print(prefix);
            pw.print("mFrozenMergedConfig=");
            pw.println(this.mFrozenMergedConfig);
        }
        if (this.mPendingRelaunchCount == 0) {
            pw.print(prefix);
            pw.print("mPendingRelaunchCount=");
            pw.println(this.mPendingRelaunchCount);
        }
    }

    public String toString() {
        if (this.stringName == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("AppWindowToken{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" token=");
            sb.append(this.token);
            sb.append('}');
            this.stringName = sb.toString();
        }
        return this.stringName;
    }
}
