package com.huawei.gson.internal.reflect;

import com.huawei.gson.JsonIOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

final class UnsafeReflectionAccessor extends ReflectionAccessor {
    private static Class unsafeClass;
    private final Field overrideField = getOverrideField();
    private final Object theUnsafe = getUnsafeInstance();

    UnsafeReflectionAccessor() {
    }

    @Override // com.huawei.gson.internal.reflect.ReflectionAccessor
    public void makeAccessible(AccessibleObject ao) {
        if (!makeAccessibleWithUnsafe(ao)) {
            try {
                ao.setAccessible(true);
            } catch (SecurityException e) {
                throw new JsonIOException("Gson couldn't modify fields for " + ao + "\nand sun.misc.Unsafe not found.\nEither write a custom type adapter, or make fields accessible, or include sun.misc.Unsafe.", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean makeAccessibleWithUnsafe(AccessibleObject ao) {
        if (!(this.theUnsafe == null || this.overrideField == null)) {
            try {
                long overrideOffset = ((Long) unsafeClass.getMethod("objectFieldOffset", Field.class).invoke(this.theUnsafe, this.overrideField)).longValue();
                unsafeClass.getMethod("putBoolean", Object.class, Long.TYPE, Boolean.TYPE).invoke(this.theUnsafe, ao, Long.valueOf(overrideOffset), true);
                return true;
            } catch (Exception e) {
            }
        }
        return false;
    }

    private static Object getUnsafeInstance() {
        try {
            unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return unsafeField.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static Field getOverrideField() {
        try {
            return AccessibleObject.class.getDeclaredField("override");
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
