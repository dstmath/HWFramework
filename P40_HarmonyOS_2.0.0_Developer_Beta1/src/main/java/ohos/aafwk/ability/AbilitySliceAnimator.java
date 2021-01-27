package ohos.aafwk.ability;

import ohos.agp.animation.AnimatorProperty;

public class AbilitySliceAnimator {
    private AnimatorType animatorType;
    private boolean defaultAnimator;
    private long delay;
    private long duration;
    private float fromX;
    private float fromY;
    private int repeatCount;
    private float toX;
    private float toY;

    /* access modifiers changed from: private */
    public enum AnimatorType {
        TRANSLATION,
        ROTATION
    }

    public AbilitySliceAnimator() {
        this.defaultAnimator = false;
        this.animatorType = AnimatorType.TRANSLATION;
        this.duration = 300;
        this.delay = 0;
        this.repeatCount = 0;
        this.defaultAnimator = true;
    }

    public AbilitySliceAnimator(float f, float f2, float f3, float f4) {
        this.defaultAnimator = false;
        this.animatorType = AnimatorType.TRANSLATION;
        this.duration = 300;
        this.delay = 0;
        this.repeatCount = 0;
        this.fromX = f;
        this.fromY = f2;
        this.toX = f3;
        this.toY = f4;
    }

    public AbilitySliceAnimator setDuration(long j) {
        this.duration = j;
        return this;
    }

    public AbilitySliceAnimator setDelay(long j) {
        this.delay = j;
        return this;
    }

    public AbilitySliceAnimator setRepeatCount(int i) {
        this.repeatCount = i;
        return this;
    }

    /* access modifiers changed from: package-private */
    public boolean isDefaultAnimator() {
        return this.defaultAnimator;
    }

    /* access modifiers changed from: package-private */
    public void constructDefaultAnimator(float f) {
        this.animatorType = AnimatorType.TRANSLATION;
        this.fromX = f;
        this.fromY = 0.0f;
        this.toX = 0.0f;
        this.toY = 0.0f;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.aafwk.ability.AbilitySliceAnimator$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$aafwk$ability$AbilitySliceAnimator$AnimatorType = new int[AnimatorType.values().length];

        static {
            try {
                $SwitchMap$ohos$aafwk$ability$AbilitySliceAnimator$AnimatorType[AnimatorType.TRANSLATION.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$aafwk$ability$AbilitySliceAnimator$AnimatorType[AnimatorType.ROTATION.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public AnimatorProperty buildEnterAnimator(AnimatorProperty animatorProperty) {
        animatorProperty.setDuration(this.duration).setDelay(this.delay).setLoopedCount(this.repeatCount);
        if (AnonymousClass1.$SwitchMap$ohos$aafwk$ability$AbilitySliceAnimator$AnimatorType[this.animatorType.ordinal()] == 1) {
            animatorProperty.moveFromX(this.fromX).moveFromY(this.fromY).moveToX(this.toX).moveToY(this.toY);
        }
        return animatorProperty;
    }

    /* access modifiers changed from: package-private */
    public AnimatorProperty buildExitAnimator(AnimatorProperty animatorProperty) {
        animatorProperty.setDuration(this.duration).setDelay(this.delay).setLoopedCount(this.repeatCount);
        if (AnonymousClass1.$SwitchMap$ohos$aafwk$ability$AbilitySliceAnimator$AnimatorType[this.animatorType.ordinal()] == 1) {
            animatorProperty.moveFromX((this.toX * 2.0f) - this.fromX).moveFromY((this.toY * 2.0f) - this.fromY).moveToX(this.toX).moveToY(this.toY);
        }
        return animatorProperty;
    }
}
