package com.android.server;

import android.content.Context;
import android.util.Log;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.IHwActivityManagerInner;
import com.android.server.am.IHwActivityManagerServiceEx;
import com.android.server.imm.IHwInputMethodManagerInner;
import com.android.server.imm.IHwInputMethodManagerServiceEx;
import com.android.server.pm.IHwPackageManagerInner;
import com.android.server.pm.IHwPackageManagerServiceEx;
import com.android.server.wm.IHwWindowManagerInner;
import com.android.server.wm.IHwWindowManagerServiceEx;
import com.huawei.server.am.IHwActivityStarterEx;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HwServiceExFactory {
    private static final String TAG = "HwServiceExFactory";
    private static final Object mLock = new Object();
    private static Factory obj = null;

    public interface Factory {
        IHwActivityManagerServiceEx getHwActivityManagerServiceEx(IHwActivityManagerInner iHwActivityManagerInner, Context context);

        IHwActivityStarterEx getHwActivityStarterEx(ActivityManagerService activityManagerService);

        IHwInputMethodManagerServiceEx getHwInputMethodManagerServiceEx(IHwInputMethodManagerInner iHwInputMethodManagerInner, Context context);

        IHwPackageManagerServiceEx getHwPackageManagerServiceEx(IHwPackageManagerInner iHwPackageManagerInner, Context context);

        IHwWindowManagerServiceEx getHwWindowManagerServiceEx(IHwWindowManagerInner iHwWindowManagerInner, Context context);
    }

    private static Factory getImplObject() {
        synchronized (mLock) {
            if (obj == null) {
                try {
                    obj = (Factory) Class.forName("com.android.server.HwServiceExFactoryImpl").newInstance();
                    Log.v(TAG, "get AllImpl object = " + obj);
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "ClassNotFoundException : " + e);
                } catch (Exception e2) {
                    Log.e(TAG, ": reflection exception is " + e2);
                }
            }
        }
        return obj;
    }

    private static Object getHwInterfaceExProxy(Class<?>[] interfaces) {
        return Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Class returnType = method.getReturnType();
                if (returnType == Integer.TYPE || returnType == Long.TYPE || returnType == Byte.TYPE || returnType == Character.TYPE || returnType == Short.TYPE || returnType == Long.TYPE || returnType == Double.TYPE) {
                    return Integer.valueOf(0);
                }
                if (returnType == Float.TYPE) {
                    return Float.valueOf(0.0f);
                }
                if (returnType == Boolean.TYPE) {
                    return Boolean.valueOf(false);
                }
                return null;
            }
        });
    }

    public static IHwActivityManagerServiceEx getHwActivityManagerServiceEx(IHwActivityManagerInner ams, Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwActivityManagerServiceEx(ams, context);
        }
        return (IHwActivityManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwActivityManagerServiceEx.class});
    }

    public static IHwWindowManagerServiceEx getHwWindowManagerServiceEx(IHwWindowManagerInner wms, Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWindowManagerServiceEx(wms, context);
        }
        return (IHwWindowManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwWindowManagerServiceEx.class});
    }

    public static IHwPackageManagerServiceEx getHwPackageManagerServiceEx(IHwPackageManagerInner pms, Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPackageManagerServiceEx(pms, context);
        }
        return (IHwPackageManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwPackageManagerServiceEx.class});
    }

    public static IHwInputMethodManagerServiceEx getHwInputMethodManagerServiceEx(IHwInputMethodManagerInner ims, Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwInputMethodManagerServiceEx(ims, context);
        }
        return (IHwInputMethodManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwInputMethodManagerServiceEx.class});
    }

    public static IHwActivityStarterEx getHwActivityStarterEx(ActivityManagerService ams) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwActivityStarterEx(ams);
        }
        return (IHwActivityStarterEx) getHwInterfaceExProxy(new Class[]{IHwActivityStarterEx.class});
    }
}
