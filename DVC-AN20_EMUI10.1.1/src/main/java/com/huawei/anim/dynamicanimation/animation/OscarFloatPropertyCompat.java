package com.huawei.anim.dynamicanimation.animation;

public abstract class OscarFloatPropertyCompat<T> {
    final String mPropertyName;

    public abstract float getValue(T t);

    public abstract void setValue(T t, float f);

    public OscarFloatPropertyCompat(String name) {
        this.mPropertyName = name;
    }
}
