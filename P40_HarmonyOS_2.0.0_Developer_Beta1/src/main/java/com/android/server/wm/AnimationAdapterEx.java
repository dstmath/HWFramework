package com.android.server.wm;

public class AnimationAdapterEx {
    public static final int TYPE_LAYER_MULTIPLIER = 10000;
    public static final int WINDOW_FREEZE_LAYER = 2000000;
    private AnimationAdapter mAnimationAdapter;

    public AnimationAdapter getAnimationAdapter() {
        return this.mAnimationAdapter;
    }

    public void setAnimationAdapter(AnimationAdapter animationAdapter) {
        this.mAnimationAdapter = animationAdapter;
    }

    public static AnimationAdapterEx getAnimationAdapterEx(AnimationSpecEx specEx, AppWindowTokenExt appTokenEx) {
        if (specEx == null || appTokenEx == null || appTokenEx.getAppWindowToken() == null || appTokenEx.getAppWindowToken().mWmService == null) {
            return null;
        }
        AnimationAdapterEx animationAdaterEx = new AnimationAdapterEx();
        animationAdaterEx.setAnimationAdapter(new LocalAnimationAdapter(specEx.getAnimationSpec(), appTokenEx.getAppWindowToken().mWmService.mSurfaceAnimationRunner));
        return animationAdaterEx;
    }

    public static AnimationAdapterEx getAnimationAdapterEx(AnimationSpecEx specEx, TaskEx taskEx) {
        if (specEx == null || taskEx == null || taskEx.getTask() == null || taskEx.getTask().mWmService == null) {
            return null;
        }
        AnimationAdapterEx animationAdaterEx = new AnimationAdapterEx();
        animationAdaterEx.setAnimationAdapter(new LocalAnimationAdapter(specEx.getAnimationSpec(), taskEx.getTask().mWmService.mSurfaceAnimationRunner));
        return animationAdaterEx;
    }
}
