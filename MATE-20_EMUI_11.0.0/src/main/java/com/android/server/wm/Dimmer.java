package com.android.server.wm;

import android.graphics.Rect;
import android.util.Log;
import android.util.proto.ProtoOutputStream;
import android.view.Surface;
import android.view.SurfaceControl;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wm.Dimmer;
import com.android.server.wm.LocalAnimationAdapter;
import com.android.server.wm.SurfaceAnimator;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public class Dimmer {
    private static final int DEFAULT_DIM_ANIM_DURATION = 200;
    private static final String TAG = "WindowManager";
    @VisibleForTesting
    DimState mDimState;
    WindowContainer mHost;
    private WindowContainer mLastRequestedDimContainer;
    private final SurfaceAnimatorStarter mSurfaceAnimatorStarter;

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public interface SurfaceAnimatorStarter {
        void startAnimation(SurfaceAnimator surfaceAnimator, SurfaceControl.Transaction transaction, AnimationAdapter animationAdapter, boolean z);
    }

    /* access modifiers changed from: private */
    public class DimAnimatable implements SurfaceAnimator.Animatable {
        private SurfaceControl mDimLayer;

        private DimAnimatable(SurfaceControl dimLayer) {
            this.mDimLayer = dimLayer;
        }

        @Override // com.android.server.wm.SurfaceAnimator.Animatable
        public SurfaceControl.Transaction getPendingTransaction() {
            return Dimmer.this.mHost.getPendingTransaction();
        }

        @Override // com.android.server.wm.SurfaceAnimator.Animatable
        public void commitPendingTransaction() {
            Dimmer.this.mHost.commitPendingTransaction();
        }

        @Override // com.android.server.wm.SurfaceAnimator.Animatable
        public void onAnimationLeashCreated(SurfaceControl.Transaction t, SurfaceControl leash) {
        }

        @Override // com.android.server.wm.SurfaceAnimator.Animatable
        public void onAnimationLeashLost(SurfaceControl.Transaction t) {
        }

        @Override // com.android.server.wm.SurfaceAnimator.Animatable
        public SurfaceControl.Builder makeAnimationLeash() {
            return Dimmer.this.mHost.makeAnimationLeash();
        }

        @Override // com.android.server.wm.SurfaceAnimator.Animatable
        public SurfaceControl getAnimationLeashParent() {
            return Dimmer.this.mHost.getSurfaceControl();
        }

        @Override // com.android.server.wm.SurfaceAnimator.Animatable
        public SurfaceControl getSurfaceControl() {
            return this.mDimLayer;
        }

        @Override // com.android.server.wm.SurfaceAnimator.Animatable
        public SurfaceControl getParentSurfaceControl() {
            return Dimmer.this.mHost.getSurfaceControl();
        }

        @Override // com.android.server.wm.SurfaceAnimator.Animatable
        public int getSurfaceWidth() {
            return Dimmer.this.mHost.getSurfaceWidth();
        }

        @Override // com.android.server.wm.SurfaceAnimator.Animatable
        public int getSurfaceHeight() {
            return Dimmer.this.mHost.getSurfaceHeight();
        }

        /* access modifiers changed from: package-private */
        public void removeSurface() {
            SurfaceControl surfaceControl = this.mDimLayer;
            if (surfaceControl != null && surfaceControl.isValid()) {
                getPendingTransaction().remove(this.mDimLayer);
            }
            this.mDimLayer = null;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public class DimState {
        boolean isVisible;
        boolean mAnimateExit = true;
        SurfaceControl mDimLayer;
        boolean mDimming;
        boolean mDontReset;
        boolean mHideFreeFormFlag = false;
        SurfaceAnimator mSurfaceAnimator;

        DimState(SurfaceControl dimLayer) {
            this.mDimLayer = dimLayer;
            this.mDimming = true;
            DimAnimatable dimAnimatable = new DimAnimatable(dimLayer);
            this.mSurfaceAnimator = new SurfaceAnimator(dimAnimatable, new Runnable(dimAnimatable) {
                /* class com.android.server.wm.$$Lambda$Dimmer$DimState$QYvwJex5H10MFMe0LEzEUs1b2G0 */
                private final /* synthetic */ Dimmer.DimAnimatable f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    Dimmer.DimState.this.lambda$new$0$Dimmer$DimState(this.f$1);
                }
            }, Dimmer.this.mHost.mWmService);
        }

        public /* synthetic */ void lambda$new$0$Dimmer$DimState(DimAnimatable dimAnimatable) {
            if (!this.mDimming) {
                dimAnimatable.removeSurface();
            }
        }
    }

    Dimmer(WindowContainer host) {
        this(host, $$Lambda$yACUZqn1AkGL14Nu3kHUSaLX0.INSTANCE);
    }

    Dimmer(WindowContainer host, SurfaceAnimatorStarter surfaceAnimatorStarter) {
        this.mHost = host;
        this.mSurfaceAnimatorStarter = surfaceAnimatorStarter;
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl makeDimLayer() {
        SurfaceControl.Builder colorLayer = this.mHost.makeChildSurface(null).setParent(this.mHost.getSurfaceControl()).setColorLayer();
        return colorLayer.setName("Dim Layer for - " + this.mHost.getName()).build();
    }

    private DimState getDimState(WindowContainer container) {
        if (this.mDimState == null) {
            try {
                this.mDimState = new DimState(makeDimLayer());
                if (container == null) {
                    this.mDimState.mDontReset = true;
                }
            } catch (Surface.OutOfResourcesException e) {
                Log.w(TAG, "OutOfResourcesException creating dim surface");
            }
        }
        this.mLastRequestedDimContainer = container;
        return this.mDimState;
    }

    private void dim(SurfaceControl.Transaction t, WindowContainer container, int relativeLayer, float alpha) {
        DimState d = getDimState(container);
        if (d != null) {
            if (container != null) {
                t.setRelativeLayer(d.mDimLayer, container.getSurfaceControl(), relativeLayer);
            } else {
                t.setLayer(d.mDimLayer, Integer.MAX_VALUE);
            }
            t.setAlpha(d.mDimLayer, alpha);
            d.mDimming = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void stopDim(SurfaceControl.Transaction t) {
        DimState dimState = this.mDimState;
        if (dimState != null) {
            t.hide(dimState.mDimLayer);
            DimState dimState2 = this.mDimState;
            dimState2.isVisible = false;
            dimState2.mDontReset = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void dimAbove(SurfaceControl.Transaction t, float alpha) {
        dim(t, null, 1, alpha);
    }

    /* access modifiers changed from: package-private */
    public void dimAbove(SurfaceControl.Transaction t, WindowContainer container, float alpha) {
        dim(t, container, 1, alpha);
    }

    /* access modifiers changed from: package-private */
    public void dimBelow(SurfaceControl.Transaction t, WindowContainer container, float alpha) {
        dim(t, container, -1, alpha);
    }

    /* access modifiers changed from: package-private */
    public void resetDimStates() {
        DimState dimState = this.mDimState;
        if (dimState != null && !dimState.mDontReset) {
            this.mDimState.mDimming = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void dontAnimateExit() {
        DimState dimState = this.mDimState;
        if (dimState != null) {
            dimState.mAnimateExit = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void setHideFreeFormFlag(boolean flag) {
        DimState dimState = this.mDimState;
        if (dimState != null) {
            dimState.mHideFreeFormFlag = flag;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateDims(SurfaceControl.Transaction t, Rect bounds) {
        DimState dimState = this.mDimState;
        if (dimState == null) {
            return false;
        }
        if (!dimState.mDimming) {
            if (this.mDimState.mAnimateExit) {
                startDimExit(this.mLastRequestedDimContainer, this.mDimState.mSurfaceAnimator, t);
            } else if (this.mDimState.mDimLayer.isValid()) {
                t.remove(this.mDimState.mDimLayer);
            }
            this.mDimState.mSurfaceAnimator = null;
            this.mDimState = null;
            return false;
        }
        WindowContainer windowContainer = this.mHost;
        if (windowContainer != null && windowContainer.mWmService != null && this.mHost.mWmService.mHwWMSEx != null && !this.mHost.mWmService.mHwWMSEx.isShowDimForPCMode(this.mHost, bounds)) {
            return false;
        }
        t.setPosition(this.mDimState.mDimLayer, (float) bounds.left, (float) bounds.top);
        t.setWindowCrop(this.mDimState.mDimLayer, bounds.width(), bounds.height());
        WindowContainer windowContainer2 = this.mHost;
        if (!(windowContainer2 == null || !windowContainer2.inHwFreeFormWindowingMode() || this.mHost.mWmService == null)) {
            if (this.mHost.isAppAnimating()) {
                return false;
            }
            float hwFreeFormScale = 1.0f;
            WindowContainer windowContainer3 = this.mHost;
            if (windowContainer3 instanceof Task) {
                TaskStack stack = ((Task) windowContainer3).mStack;
                hwFreeFormScale = stack == null ? 1.0f : stack.mHwStackScale;
            } else if (windowContainer3 instanceof TaskStack) {
                hwFreeFormScale = ((TaskStack) windowContainer3).mHwStackScale;
            }
            if (this.mHost.getDisplayContent() != null && this.mHost.getDisplayContent().getDisplayId() == 0) {
                if (this.mHost.mWmService.getLazyMode() != 0) {
                    float scale = this.mHost.getLazyScale();
                    t.setMatrix(this.mDimState.mDimLayer, scale * hwFreeFormScale, 0.0f, 0.0f, scale * hwFreeFormScale);
                } else if (this.mHost.mWmService.isInSubFoldScaleMode()) {
                    t.setMatrix(this.mDimState.mDimLayer, this.mHost.mWmService.mSubFoldModeScale * hwFreeFormScale, 0.0f, 0.0f, this.mHost.mWmService.mSubFoldModeScale * hwFreeFormScale);
                } else {
                    t.setMatrix(this.mDimState.mDimLayer, hwFreeFormScale, 0.0f, 0.0f, hwFreeFormScale);
                }
            }
        }
        if (!this.mDimState.isVisible && !this.mDimState.mHideFreeFormFlag) {
            DimState dimState2 = this.mDimState;
            dimState2.isVisible = true;
            t.show(dimState2.mDimLayer);
            startDimEnter(this.mLastRequestedDimContainer, this.mDimState.mSurfaceAnimator, t);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void updateDimsCornerRadius(SurfaceControl.Transaction t, float cornerRadius) {
        t.setCornerRadius(this.mDimState.mDimLayer, cornerRadius);
    }

    private void startDimEnter(WindowContainer container, SurfaceAnimator animator, SurfaceControl.Transaction t) {
        startAnim(container, animator, t, 0.0f, 1.0f);
    }

    private void startDimExit(WindowContainer container, SurfaceAnimator animator, SurfaceControl.Transaction t) {
        startAnim(container, animator, t, 1.0f, 0.0f);
    }

    private void startAnim(WindowContainer container, SurfaceAnimator animator, SurfaceControl.Transaction t, float startAlpha, float endAlpha) {
        this.mSurfaceAnimatorStarter.startAnimation(animator, t, new LocalAnimationAdapter(new AlphaAnimationSpec(startAlpha, endAlpha, getDimDuration(container)), this.mHost.mWmService.mSurfaceAnimationRunner), false);
    }

    private long getDimDuration(WindowContainer container) {
        if (container == null) {
            return 0;
        }
        AnimationAdapter animationAdapter = container.mSurfaceAnimator.getAnimation();
        if (animationAdapter == null) {
            return 200;
        }
        return animationAdapter.getDurationHint();
    }

    /* access modifiers changed from: private */
    public static class AlphaAnimationSpec implements LocalAnimationAdapter.AnimationSpec {
        private final long mDuration;
        private final float mFromAlpha;
        private final float mToAlpha;

        AlphaAnimationSpec(float fromAlpha, float toAlpha, long duration) {
            this.mFromAlpha = fromAlpha;
            this.mToAlpha = toAlpha;
            this.mDuration = duration;
        }

        @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
        public long getDuration() {
            return this.mDuration;
        }

        @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
        public void apply(SurfaceControl.Transaction t, SurfaceControl sc, long currentPlayTime) {
            float duration = ((float) currentPlayTime) / ((float) getDuration());
            float f = this.mToAlpha;
            float f2 = this.mFromAlpha;
            t.setAlpha(sc, (duration * (f - f2)) + f2);
        }

        @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
        public void dump(PrintWriter pw, String prefix) {
            pw.print(prefix);
            pw.print("from=");
            pw.print(this.mFromAlpha);
            pw.print(" to=");
            pw.print(this.mToAlpha);
            pw.print(" duration=");
            pw.println(this.mDuration);
        }

        @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
        public void writeToProtoInner(ProtoOutputStream proto) {
            long token = proto.start(1146756268035L);
            proto.write(1108101562369L, this.mFromAlpha);
            proto.write(1108101562370L, this.mToAlpha);
            proto.write(1112396529667L, this.mDuration);
            proto.end(token);
        }
    }
}
