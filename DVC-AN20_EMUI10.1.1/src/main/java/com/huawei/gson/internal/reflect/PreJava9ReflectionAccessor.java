package com.huawei.gson.internal.reflect;

import java.lang.reflect.AccessibleObject;

final class PreJava9ReflectionAccessor extends ReflectionAccessor {
    PreJava9ReflectionAccessor() {
    }

    @Override // com.huawei.gson.internal.reflect.ReflectionAccessor
    public void makeAccessible(AccessibleObject ao) {
        ao.setAccessible(true);
    }
}
