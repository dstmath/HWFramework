package com.huawei.utils.reflect;

import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class EasyInvokeFactory {
    private static Map<Class<?>, EasyInvokeUtils> invokeUtilsMap = new HashMap();

    public static synchronized <T extends EasyInvokeUtils> T getInvokeUtils(Class<T> clazz) {
        EasyInvokeUtils invokeUtil;
        synchronized (EasyInvokeFactory.class) {
            invokeUtil = (EasyInvokeUtils) invokeUtilsMap.get(clazz);
            if (invokeUtil == null) {
                try {
                    invokeUtil = (EasyInvokeUtils) clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
                    invokeUtilsMap.put(clazz, invokeUtil);
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e2) {
                    e2.printStackTrace();
                } catch (IllegalArgumentException e3) {
                    e3.printStackTrace();
                } catch (InstantiationException e4) {
                    e4.printStackTrace();
                } catch (IllegalAccessException e5) {
                    e5.printStackTrace();
                } catch (InvocationTargetException e6) {
                    e6.printStackTrace();
                }
                if (invokeUtil == null) {
                    Log.e("EasyInvokeFactory", "create instance error clazz[" + clazz + "]");
                }
            }
        }
        return invokeUtil;
    }
}
