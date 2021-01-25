package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public enum CoreAnimationPlaybackState {
    STOP(0),
    PLAY(1),
    PAUSE(2);
    
    private final int swigValue;

    /* access modifiers changed from: package-private */
    public final int swigValue() {
        return this.swigValue;
    }

    static CoreAnimationPlaybackState swigToEnum(int i) {
        CoreAnimationPlaybackState[] coreAnimationPlaybackStateArr = (CoreAnimationPlaybackState[]) CoreAnimationPlaybackState.class.getEnumConstants();
        if (i < coreAnimationPlaybackStateArr.length && i >= 0 && coreAnimationPlaybackStateArr[i].swigValue == i) {
            return coreAnimationPlaybackStateArr[i];
        }
        for (CoreAnimationPlaybackState coreAnimationPlaybackState : coreAnimationPlaybackStateArr) {
            if (coreAnimationPlaybackState.swigValue == i) {
                return coreAnimationPlaybackState;
            }
        }
        throw new IllegalArgumentException("No enum " + CoreAnimationPlaybackState.class + " with value " + i);
    }

    private CoreAnimationPlaybackState() {
        this(SwigNext.next);
    }

    private CoreAnimationPlaybackState(int i) {
        this.swigValue = i;
        int unused = SwigNext.next = i + 1;
    }

    private CoreAnimationPlaybackState(CoreAnimationPlaybackState coreAnimationPlaybackState) {
        this(coreAnimationPlaybackState.swigValue);
    }

    private static class SwigNext {
        private static int next;

        private SwigNext() {
        }
    }
}
