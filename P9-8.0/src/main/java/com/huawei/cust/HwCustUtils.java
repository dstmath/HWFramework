package com.huawei.cust;

public final class HwCustUtils {
    public static Object createObj(String className, ClassLoader cl, Object... args) {
        return huawei.cust.HwCustUtils.createObj(className, cl, args);
    }

    public static Object createObj(Class<?> classClass, Object... args) {
        return huawei.cust.HwCustUtils.createObj(classClass, args);
    }
}
