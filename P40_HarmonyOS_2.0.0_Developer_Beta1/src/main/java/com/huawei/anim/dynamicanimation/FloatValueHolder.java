package com.huawei.anim.dynamicanimation;

public final class FloatValueHolder {
    private float a = 0.0f;

    public FloatValueHolder(float f) {
        setValue(f);
    }

    public void setValue(float f) {
        this.a = f;
    }

    public float getValue() {
        return this.a;
    }
}
