package com.huawei.agpengine.impl;

import android.util.Log;
import com.huawei.agpengine.Engine;
import com.huawei.agpengine.SceneNode;
import com.huawei.agpengine.resources.AnimationPlayback;
import com.huawei.agpengine.resources.ResourceHandle;
import java.util.Optional;

class AnimationPlaybackImpl implements AnimationPlayback {
    private static final String TAG = "core: AnimationPlaybackImpl";
    private CoreAnimationSystem mAnimationSystem;
    private Engine mEngine;
    private CoreAnimationPlayback mPlayback;

    private AnimationPlaybackImpl(Engine engine, CoreAnimationSystem animationSystem, CoreAnimationPlayback playback) {
        this.mEngine = engine;
        this.mAnimationSystem = animationSystem;
        this.mPlayback = playback;
    }

    static Optional<AnimationPlayback> createPlayback(Engine engine, CoreAnimationSystem animationSystem, ResourceHandle animationHandle, SceneNode node) {
        if (animationSystem == null) {
            throw new NullPointerException("Internal graphics engine error");
        } else if (animationHandle == null) {
            throw new NullPointerException("animationHandle must not be null.");
        } else if (node != null) {
            engine.requireRenderThread();
            Optional<CoreSceneNode> nativeNode = SceneImpl.getNativeSceneNode(node);
            return (!animationHandle.isValid() || !nativeNode.isPresent()) ? Optional.empty() : Optional.of(new AnimationPlaybackImpl(engine, animationSystem, animationSystem.createPlayback(ResourceHandleImpl.getNativeHandle(animationHandle), nativeNode.get())));
        } else {
            throw new NullPointerException("node must not be null.");
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        if (this.mEngine != null) {
            Log.w(TAG, "AnimationPlayback not released explicitly.");
            this.mEngine.runInRenderThread(new Runnable() {
                /* class com.huawei.agpengine.impl.AnimationPlaybackImpl.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    AnimationPlaybackImpl.this.release();
                }
            });
        }
        super.finalize();
    }

    @Override // com.huawei.agpengine.resources.AnimationPlayback
    public void release() {
        Engine engine = this.mEngine;
        if (engine != null) {
            engine.requireRenderThread();
        }
        CoreAnimationPlayback coreAnimationPlayback = this.mPlayback;
        if (coreAnimationPlayback != null) {
            this.mAnimationSystem.destroyPlayback(coreAnimationPlayback);
            this.mAnimationSystem = null;
            this.mPlayback = null;
        }
        this.mEngine = null;
    }

    @Override // com.huawei.agpengine.resources.AnimationPlayback
    public void setPlaybackState(AnimationPlayback.State state) {
        int i = AnonymousClass2.$SwitchMap$com$huawei$agpengine$resources$AnimationPlayback$State[state.ordinal()];
        if (i == 1) {
            this.mPlayback.setPlaybackState(CoreAnimationPlaybackState.STOP);
        } else if (i == 2) {
            this.mPlayback.setPlaybackState(CoreAnimationPlaybackState.PLAY);
        } else if (i == 3) {
            this.mPlayback.setPlaybackState(CoreAnimationPlaybackState.PAUSE);
        } else {
            throw new IllegalStateException("Internal graphics engine error");
        }
    }

    /* renamed from: com.huawei.agpengine.impl.AnimationPlaybackImpl$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$impl$CoreAnimationPlaybackState = new int[CoreAnimationPlaybackState.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$huawei$agpengine$resources$AnimationPlayback$State = new int[AnimationPlayback.State.values().length];

        static {
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreAnimationPlaybackState[CoreAnimationPlaybackState.STOP.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreAnimationPlaybackState[CoreAnimationPlaybackState.PLAY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$impl$CoreAnimationPlaybackState[CoreAnimationPlaybackState.PAUSE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$resources$AnimationPlayback$State[AnimationPlayback.State.STOP.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$resources$AnimationPlayback$State[AnimationPlayback.State.PLAY.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$huawei$agpengine$resources$AnimationPlayback$State[AnimationPlayback.State.PAUSE.ordinal()] = 3;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    @Override // com.huawei.agpengine.resources.AnimationPlayback
    public AnimationPlayback.State getPlaybackState() {
        int i = AnonymousClass2.$SwitchMap$com$huawei$agpengine$impl$CoreAnimationPlaybackState[this.mPlayback.getPlaybackState().ordinal()];
        if (i == 1) {
            return AnimationPlayback.State.STOP;
        }
        if (i == 2) {
            return AnimationPlayback.State.PLAY;
        }
        if (i == 3) {
            return AnimationPlayback.State.PAUSE;
        }
        throw new IllegalStateException("Internal graphics engine error");
    }

    @Override // com.huawei.agpengine.resources.AnimationPlayback
    public void setRepeatCount(int repeatCount) {
        if (repeatCount == Integer.MAX_VALUE) {
            this.mPlayback.setRepeatCount(Core.getAnimationRepeatCountInfinite());
        } else {
            this.mPlayback.setRepeatCount((long) repeatCount);
        }
    }

    @Override // com.huawei.agpengine.resources.AnimationPlayback
    public int getRepeatCount() {
        long repeatCount = this.mPlayback.getRepeatCount();
        if (repeatCount == Core.getAnimationRepeatCountInfinite()) {
            return Integer.MAX_VALUE;
        }
        return (int) repeatCount;
    }

    @Override // com.huawei.agpengine.resources.AnimationPlayback
    public void setWeight(float weight) {
        this.mPlayback.setWeight(weight);
    }

    @Override // com.huawei.agpengine.resources.AnimationPlayback
    public float getWeight() {
        return this.mPlayback.getWeight();
    }

    @Override // com.huawei.agpengine.resources.AnimationPlayback
    public boolean isCompleted() {
        return this.mPlayback.isCompleted();
    }

    @Override // com.huawei.agpengine.resources.AnimationPlayback
    public void setSpeed(float speed) {
        this.mPlayback.setSpeed(speed);
    }

    @Override // com.huawei.agpengine.resources.AnimationPlayback
    public float getSpeed() {
        return this.mPlayback.getSpeed();
    }
}
