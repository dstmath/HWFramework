package huawei.android.animation;

import android.animation.Animator;
import android.content.res.ConstantState;

public interface HwStateListAnimator {
    void addState(int[] iArr, Animator animator);

    void appendChangingConfigurations(int i);

    ConstantState<HwStateListAnimator> createConstantState();

    int getChangingConfigurations();

    Animator getRunningAnimator();

    Object getTarget();

    void jumpToCurrentState();

    void setAnimatorEnable(boolean z);

    void setChangingConfigurations(int i);

    void setMode(int i);

    void setState(int[] iArr);

    void setTarget(Object obj);
}
