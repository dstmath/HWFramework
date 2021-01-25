package com.android.server.wm;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.proto.ProtoOutputStream;
import android.view.InsetsSource;
import android.view.InsetsSourceControl;
import android.view.InsetsState;
import android.view.SurfaceControl;
import android.view.ViewRootImpl;
import com.android.internal.util.function.TriConsumer;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.wm.SurfaceAnimator;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public class InsetsSourceProvider {
    private ControlAdapter mAdapter;
    private boolean mClientVisible;
    private InsetsSourceControl mControl;
    private final boolean mControllable;
    private WindowState mControllingWin;
    private final DisplayContent mDisplayContent;
    private TriConsumer<DisplayFrames, WindowState, Rect> mFrameProvider;
    private boolean mServerVisible;
    private final InsetsSource mSource;
    private final InsetsStateController mStateController;
    private final Rect mTmpRect = new Rect();
    private WindowState mWin;

    InsetsSourceProvider(InsetsSource source, InsetsStateController stateController, DisplayContent displayContent) {
        this.mClientVisible = InsetsState.getDefaultVisibility(source.getType());
        this.mSource = source;
        this.mDisplayContent = displayContent;
        this.mStateController = stateController;
        int type = source.getType();
        boolean z = false;
        if (type == 0 || type == 1) {
            this.mControllable = ViewRootImpl.sNewInsetsMode == 2 ? true : z;
        } else if (type == 10) {
            this.mControllable = ViewRootImpl.sNewInsetsMode >= 1 ? true : z;
        } else {
            this.mControllable = false;
        }
    }

    /* access modifiers changed from: package-private */
    public InsetsSource getSource() {
        return this.mSource;
    }

    /* access modifiers changed from: package-private */
    public boolean isControllable() {
        return this.mControllable;
    }

    /* access modifiers changed from: package-private */
    public void setWindow(WindowState win, TriConsumer<DisplayFrames, WindowState, Rect> frameProvider) {
        WindowState windowState = this.mWin;
        if (windowState != null) {
            windowState.setInsetProvider(null);
        }
        this.mWin = win;
        this.mFrameProvider = frameProvider;
        if (win == null) {
            setServerVisible(false);
            this.mSource.setFrame(new Rect());
            return;
        }
        this.mWin.setInsetProvider(this);
        WindowState windowState2 = this.mControllingWin;
        if (windowState2 != null) {
            updateControlForTarget(windowState2, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void onPostLayout() {
        WindowState windowState = this.mWin;
        if (windowState != null) {
            this.mTmpRect.set(windowState.getFrameLw());
            TriConsumer<DisplayFrames, WindowState, Rect> triConsumer = this.mFrameProvider;
            if (triConsumer != null) {
                triConsumer.accept(this.mWin.getDisplayContent().mDisplayFrames, this.mWin, this.mTmpRect);
            } else {
                this.mTmpRect.inset(this.mWin.mGivenContentInsets);
            }
            this.mSource.setFrame(this.mTmpRect);
            if (this.mControl != null) {
                Rect frame = this.mWin.getWindowFrames().mFrame;
                if (this.mControl.setSurfacePosition(frame.left, frame.top)) {
                    this.mStateController.notifyControlChanged(this.mControllingWin);
                }
            }
            setServerVisible(this.mWin.wouldBeVisibleIfPolicyIgnored() && this.mWin.isVisibleByPolicy() && !this.mWin.mGivenInsetsPending);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateControlForTarget(WindowState target, boolean force) {
        if (this.mWin == null) {
            this.mControllingWin = target;
        } else if (target == this.mControllingWin && !force) {
        } else {
            if (target == null) {
                this.mWin.cancelAnimation();
                return;
            }
            this.mAdapter = new ControlAdapter();
            setClientVisible(InsetsState.getDefaultVisibility(this.mSource.getType()));
            this.mWin.startAnimation(this.mDisplayContent.getPendingTransaction(), this.mAdapter, !this.mClientVisible);
            this.mControllingWin = target;
            this.mControl = new InsetsSourceControl(this.mSource.getType(), this.mAdapter.mCapturedLeash, new Point(this.mWin.getWindowFrames().mFrame.left, this.mWin.getWindowFrames().mFrame.top));
        }
    }

    /* access modifiers changed from: package-private */
    public boolean onInsetsModified(WindowState caller, InsetsSource modifiedSource) {
        if (this.mControllingWin != caller || modifiedSource.isVisible() == this.mClientVisible) {
            return false;
        }
        setClientVisible(modifiedSource.isVisible());
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setClientVisible(boolean clientVisible) {
        if (this.mClientVisible != clientVisible) {
            this.mClientVisible = clientVisible;
            this.mDisplayContent.mWmService.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$guE7h8X4ZgHSWlK6mDEaOIHG60.INSTANCE, this.mDisplayContent));
            updateVisibility();
        }
    }

    private void setServerVisible(boolean serverVisible) {
        this.mServerVisible = serverVisible;
        updateVisibility();
    }

    private void updateVisibility() {
        this.mSource.setVisible(this.mServerVisible && this.mClientVisible);
    }

    /* access modifiers changed from: package-private */
    public InsetsSourceControl getControl() {
        return this.mControl;
    }

    /* access modifiers changed from: package-private */
    public boolean isClientVisible() {
        return ViewRootImpl.sNewInsetsMode == 0 || this.mClientVisible;
    }

    /* access modifiers changed from: private */
    public class ControlAdapter implements AnimationAdapter {
        private SurfaceControl mCapturedLeash;

        private ControlAdapter() {
        }

        @Override // com.android.server.wm.AnimationAdapter
        public boolean getShowWallpaper() {
            return false;
        }

        @Override // com.android.server.wm.AnimationAdapter
        public int getBackgroundColor() {
            return 0;
        }

        @Override // com.android.server.wm.AnimationAdapter
        public void startAnimation(SurfaceControl animationLeash, SurfaceControl.Transaction t, SurfaceAnimator.OnAnimationFinishedCallback finishCallback) {
            this.mCapturedLeash = animationLeash;
            Rect frame = InsetsSourceProvider.this.mWin.getWindowFrames().mFrame;
            t.setPosition(this.mCapturedLeash, (float) frame.left, (float) frame.top);
        }

        @Override // com.android.server.wm.AnimationAdapter
        public void onAnimationCancelled(SurfaceControl animationLeash) {
            if (InsetsSourceProvider.this.mAdapter == this) {
                InsetsSourceProvider.this.mStateController.notifyControlRevoked(InsetsSourceProvider.this.mControllingWin, InsetsSourceProvider.this);
                InsetsSourceProvider insetsSourceProvider = InsetsSourceProvider.this;
                insetsSourceProvider.setClientVisible(InsetsState.getDefaultVisibility(insetsSourceProvider.mSource.getType()));
                InsetsSourceProvider.this.mControl = null;
                InsetsSourceProvider.this.mControllingWin = null;
                InsetsSourceProvider.this.mAdapter = null;
            }
        }

        @Override // com.android.server.wm.AnimationAdapter
        public long getDurationHint() {
            return 0;
        }

        @Override // com.android.server.wm.AnimationAdapter
        public long getStatusBarTransitionsStartTime() {
            return 0;
        }

        @Override // com.android.server.wm.AnimationAdapter
        public void dump(PrintWriter pw, String prefix) {
        }

        @Override // com.android.server.wm.AnimationAdapter
        public void writeToProto(ProtoOutputStream proto) {
        }
    }
}
