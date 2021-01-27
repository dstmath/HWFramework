package com.huawei.anim.dynamicanimation;

import com.huawei.anim.dynamicanimation.DynamicAnimation;

public abstract class ChainListener implements DynamicAnimation.OnAnimationUpdateListener {
    @Override // com.huawei.anim.dynamicanimation.DynamicAnimation.OnAnimationUpdateListener
    public abstract void onAnimationUpdate(DynamicAnimation dynamicAnimation, float f, float f2);
}
