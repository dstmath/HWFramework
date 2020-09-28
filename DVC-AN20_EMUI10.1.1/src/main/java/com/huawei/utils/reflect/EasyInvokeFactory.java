package com.huawei.utils.reflect;

import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class EasyInvokeFactory {
    private static Map<Class<?>, EasyInvokeUtils> invokeUtilsMap = new HashMap();

    public static synchronized <T extends EasyInvokeUtils> T getInvokeUtils(Class<T> clazz) {
        T t;
        synchronized (EasyInvokeFactory.class) {
            EasyInvokeUtils invokeUtil = (T) invokeUtilsMap.get(clazz);
            if (invokeUtil == null) {
                try {
                    EasyInvokeUtils invokeUtil2 = clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
                    invokeUtil = (T) invokeUtil2;
                    invokeUtilsMap.put(clazz, invokeUtil);
                } catch (SecurityException e) {
                    Log.e("EasyInvokeFactory", "getInvokeUtils catch SecurityException : " + e.toString());
                } catch (NoSuchMethodException e2) {
                    Log.e("EasyInvokeFactory", "getInvokeUtils catch NoSuchMethodException : " + e2.toString());
                } catch (IllegalArgumentException e3) {
                    Log.e("EasyInvokeFactory", "getInvokeUtils catch IllegalArgumentException : " + e3.toString());
                } catch (InstantiationException e4) {
                    Log.e("EasyInvokeFactory", "getInvokeUtils catch InstantiationException : " + e4.toString());
                } catch (IllegalAccessException e5) {
                    Log.e("EasyInvokeFactory", "getInvokeUtils catch IllegalAccessException : " + e5.toString());
                } catch (InvocationTargetException e6) {
                    Log.e("EasyInvokeFactory", "getInvokeUtils catch InvocationTargetException : " + e6.toString());
                }
                if (invokeUtil == null) {
                    Log.e("EasyInvokeFactory", "create instance error clazz[" + clazz + "]");
                }
            }
        }
        return t;
    }
}
