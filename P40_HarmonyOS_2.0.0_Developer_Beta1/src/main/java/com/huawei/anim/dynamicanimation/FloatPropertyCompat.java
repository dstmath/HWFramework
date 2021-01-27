package com.huawei.anim.dynamicanimation;

public abstract class FloatPropertyCompat<T> {
    final String c;

    public abstract float getValue(T t);

    public abstract void setValue(T t, float f);

    public FloatPropertyCompat(String str) {
        this.c = str;
    }
}
