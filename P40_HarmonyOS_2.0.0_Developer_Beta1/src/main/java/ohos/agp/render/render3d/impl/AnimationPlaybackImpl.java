package ohos.agp.render.render3d.impl;

import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.render.render3d.Engine;
import ohos.agp.render.render3d.SceneNode;
import ohos.agp.render.render3d.resources.AnimationPlayback;
import ohos.agp.render.render3d.resources.ResourceHandle;
import ohos.hiviewdfx.HiLogLabel;

class AnimationPlaybackImpl implements AnimationPlayback {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "core: AnimationPlaybackImpl");
    private CoreAnimationSystem mAnimationSystem;
    private Engine mEngine;
    private CoreAnimationPlayback mPlayback;

    private AnimationPlaybackImpl(Engine engine, CoreAnimationSystem coreAnimationSystem, CoreAnimationPlayback coreAnimationPlayback) {
        this.mEngine = engine;
        this.mAnimationSystem = coreAnimationSystem;
        this.mPlayback = coreAnimationPlayback;
    }

    static Optional<AnimationPlayback> createPlayback(Engine engine, CoreAnimationSystem coreAnimationSystem, ResourceHandle resourceHandle, SceneNode sceneNode) {
        if (coreAnimationSystem == null) {
            throw new NullPointerException();
        } else if (resourceHandle == null) {
            throw new NullPointerException();
        } else if (sceneNode != null) {
            engine.requireRenderThread();
            Optional<CoreSceneNode> nativeSceneNode = SceneImpl.getNativeSceneNode(sceneNode);
            return (!resourceHandle.isValid() || !nativeSceneNode.isPresent()) ? Optional.empty() : Optional.of(new AnimationPlaybackImpl(engine, coreAnimationSystem, coreAnimationSystem.createPlayback(ResourceHandleImpl.getNativeHandle(resourceHandle), nativeSceneNode.get())));
        } else {
            throw new NullPointerException();
        }
    }

    @Override // ohos.agp.render.render3d.resources.AnimationPlayback
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

    @Override // ohos.agp.render.render3d.resources.AnimationPlayback
    public void setPlaybackState(AnimationPlayback.State state) {
        int i = AnonymousClass1.$SwitchMap$ohos$agp$render$render3d$resources$AnimationPlayback$State[state.ordinal()];
        if (i == 1) {
            this.mPlayback.setPlaybackState(CoreAnimationPlaybackState.STOP);
        } else if (i == 2) {
            this.mPlayback.setPlaybackState(CoreAnimationPlaybackState.PLAY);
        } else if (i == 3) {
            this.mPlayback.setPlaybackState(CoreAnimationPlaybackState.PAUSE);
        } else {
            throw new IllegalStateException();
        }
    }

    /* renamed from: ohos.agp.render.render3d.impl.AnimationPlaybackImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$render$render3d$impl$CoreAnimationPlaybackState = new int[CoreAnimationPlaybackState.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$agp$render$render3d$resources$AnimationPlayback$State = new int[AnimationPlayback.State.values().length];

        static {
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreAnimationPlaybackState[CoreAnimationPlaybackState.STOP.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreAnimationPlaybackState[CoreAnimationPlaybackState.PLAY.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$impl$CoreAnimationPlaybackState[CoreAnimationPlaybackState.PAUSE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$resources$AnimationPlayback$State[AnimationPlayback.State.STOP.ordinal()] = 1;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$resources$AnimationPlayback$State[AnimationPlayback.State.PLAY.ordinal()] = 2;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$ohos$agp$render$render3d$resources$AnimationPlayback$State[AnimationPlayback.State.PAUSE.ordinal()] = 3;
            } catch (NoSuchFieldError unused6) {
            }
        }
    }

    @Override // ohos.agp.render.render3d.resources.AnimationPlayback
    public AnimationPlayback.State getPlaybackState() {
        int i = AnonymousClass1.$SwitchMap$ohos$agp$render$render3d$impl$CoreAnimationPlaybackState[this.mPlayback.getPlaybackState().ordinal()];
        if (i == 1) {
            return AnimationPlayback.State.STOP;
        }
        if (i == 2) {
            return AnimationPlayback.State.PLAY;
        }
        if (i == 3) {
            return AnimationPlayback.State.PAUSE;
        }
        throw new IllegalStateException();
    }

    @Override // ohos.agp.render.render3d.resources.AnimationPlayback
    public void setRepeatCount(int i) {
        if (i == Integer.MAX_VALUE) {
            this.mPlayback.setRepeatCount(Core.getAnimationRepeatCountInfinite());
        } else {
            this.mPlayback.setRepeatCount((long) i);
        }
    }

    @Override // ohos.agp.render.render3d.resources.AnimationPlayback
    public int getRepeatCount() {
        long repeatCount = this.mPlayback.getRepeatCount();
        if (repeatCount == Core.getAnimationRepeatCountInfinite()) {
            return Integer.MAX_VALUE;
        }
        return (int) repeatCount;
    }

    @Override // ohos.agp.render.render3d.resources.AnimationPlayback
    public void setWeight(float f) {
        this.mPlayback.setWeight(f);
    }

    @Override // ohos.agp.render.render3d.resources.AnimationPlayback
    public float getWeight() {
        return this.mPlayback.getWeight();
    }

    @Override // ohos.agp.render.render3d.resources.AnimationPlayback
    public boolean isCompleted() {
        return this.mPlayback.isCompleted();
    }

    @Override // ohos.agp.render.render3d.resources.AnimationPlayback
    public void setSpeed(float f) {
        this.mPlayback.setSpeed(f);
    }

    @Override // ohos.agp.render.render3d.resources.AnimationPlayback
    public float getSpeed() {
        return this.mPlayback.getSpeed();
    }
}
