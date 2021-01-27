package ohos.agp.animation;

import java.util.ArrayList;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.animation.Animator;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AnimatorGroup extends Animator {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_ANIMATION");
    private final ArrayList<AnimatorParallel> mAnimatorParallelList = new ArrayList<>();
    protected Builder mBuilder = null;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeAddParallelAnimators(long j, long[] jArr);

    private native void nativeClear(long j);

    private native long nativeGetAnimatorSetHandle();

    public AnimatorGroup() {
        this.mNativeAnimatorPtr = nativeGetAnimatorSetHandle();
        initAnimator(this.mNativeAnimatorPtr);
    }

    public void runParallel(Animator... animatorArr) {
        build().addAnimators(animatorArr);
    }

    public void runSerially(Animator... animatorArr) {
        Builder build = build();
        for (Animator animator : animatorArr) {
            if (!(animator == null || animator.getNativeAnimatorPtr() == 0)) {
                build.addAnimators(animator);
            }
        }
    }

    public int getRoundCount() {
        return this.mAnimatorParallelList.size();
    }

    public ArrayList<Animator> getAnimatorsAt(int i) {
        if (i >= 0 && i < this.mAnimatorParallelList.size()) {
            return this.mAnimatorParallelList.get(i).getChildAnimations();
        }
        HiLog.error(TAG, "Get animators fail.", new Object[0]);
        return new ArrayList<>();
    }

    public void clear() {
        this.mAnimatorParallelList.clear();
        nativeClear(this.mNativeAnimatorPtr);
    }

    public Builder build() {
        clear();
        if (this.mBuilder == null) {
            this.mBuilder = new Builder();
        }
        return this.mBuilder;
    }

    public class Builder {
        public Builder() {
        }

        public Builder addAnimators(Animator... animatorArr) {
            AnimatorParallel animatorParallel = new AnimatorParallel(animatorArr);
            AnimatorGroup.this.mAnimatorParallelList.add(animatorParallel);
            AnimatorGroup animatorGroup = AnimatorGroup.this;
            animatorGroup.nativeAddParallelAnimators(animatorGroup.mNativeAnimatorPtr, animatorParallel.getNativePtrArray());
            return this;
        }
    }

    public void setDuration(long j) {
        setDurationInternal(j);
    }

    public void setDelay(long j) {
        setDelayInternal(j);
    }

    public void setLoopedCount(int i) {
        setLoopedCountInternal(i);
    }

    public void setCurveType(int i) {
        setCurveTypeInternal(i);
    }

    public void setStateChangedListener(Animator.StateChangedListener stateChangedListener) {
        setStartListenerInternal(stateChangedListener);
        setPauseListenerInternal(stateChangedListener);
    }

    public void setLoopedListener(Animator.LoopedListener loopedListener) {
        setLoopedListenerInternal(loopedListener);
    }

    /* access modifiers changed from: private */
    public static class AnimatorParallel {
        private final ArrayList<Animator> mAnimatorList = new ArrayList<>();

        public AnimatorParallel(Animator... animatorArr) {
            for (Animator animator : animatorArr) {
                if (!(animator == null || animator.getNativeAnimatorPtr() == 0)) {
                    this.mAnimatorList.add(animator);
                }
            }
        }

        public long[] getNativePtrArray() {
            long[] jArr = new long[this.mAnimatorList.size()];
            for (int i = 0; i < this.mAnimatorList.size(); i++) {
                jArr[i] = this.mAnimatorList.get(i).getNativeAnimatorPtr();
            }
            return jArr;
        }

        public ArrayList<Animator> getChildAnimations() {
            return this.mAnimatorList;
        }
    }
}
