package com.huawei.anim.dynamicanimation.animation;

public final class OscarFloatValueHolder {
    private float mValue = 0.0f;

    public OscarFloatValueHolder() {
    }

    public OscarFloatValueHolder(float value) {
        setValue(value);
    }

    public void setValue(float value) {
        this.mValue = value;
    }

    public float getValue() {
        return this.mValue;
    }
}
