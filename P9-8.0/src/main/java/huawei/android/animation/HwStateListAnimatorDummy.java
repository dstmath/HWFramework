package huawei.android.animation;

import android.animation.Animator;
import android.content.res.ConstantState;

public class HwStateListAnimatorDummy implements HwStateListAnimator {
    private static volatile HwStateListAnimator sHwStateListAnimator = null;

    private HwStateListAnimatorDummy() {
    }

    public static HwStateListAnimator getDefault() {
        if (sHwStateListAnimator == null) {
            sHwStateListAnimator = new HwStateListAnimatorDummy();
        }
        return sHwStateListAnimator;
    }

    public void addState(int[] specs, Animator animator) {
    }

    public Animator getRunningAnimator() {
        return null;
    }

    public Object getTarget() {
        return null;
    }

    public void setTarget(Object object) {
    }

    public void setState(int[] state) {
    }

    public void jumpToCurrentState() {
    }

    public int getChangingConfigurations() {
        return 0;
    }

    public void setChangingConfigurations(int configs) {
    }

    public void appendChangingConfigurations(int configs) {
    }

    public ConstantState<HwStateListAnimator> createConstantState() {
        return null;
    }

    public void setMode(int mode) {
    }

    public void setAnimatorEnable(boolean enable) {
    }
}
