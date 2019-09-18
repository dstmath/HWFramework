package com.huawei.wallet.sdk.business.buscard.model;

import android.content.Context;
import android.os.Handler;
import com.huawei.wallet.sdk.common.log.LogC;
import java.lang.reflect.InvocationTargetException;

public class HwInvoke {
    private static final String TAG = "HwInvoke";

    public static int getIntFiled(Class<?> cls, String filedName, int def) {
        try {
            return cls.getField(filedName).getInt(null);
        } catch (IllegalArgumentException e) {
            LogC.e("getIntFiled(" + cls + " fiedName:" + filedName + ", def:" + def + ") err:" + e.toString(), false);
            return def;
        } catch (IllegalAccessException e2) {
            LogC.e("getIntFiled(" + cls + " fiedName:" + filedName + ", def:" + def + ") err:" + e2.toString(), false);
            return def;
        } catch (NoSuchFieldException e3) {
            LogC.e("getIntFiled(" + cls + " fiedName:" + filedName + ", def:" + def + ") err:" + e3.toString(), false);
            return def;
        }
    }

    public static int getIntFiled(String className, String filedName, int def) {
        try {
            return getIntFiled(Class.forName(className), filedName, def);
        } catch (ClassNotFoundException e) {
            return def;
        }
    }

    public static Object invokeFun(Class<?> cls, Object obj, String funName, Class<?>[] paramsType, Object[] params) throws ParamsException, NoSuchMethodException {
        if (cls != null) {
            if (paramsType == null) {
                if (params != null) {
                    throw new ParamsException("paramsType is null, but params is not null");
                }
            } else if (params == null) {
                throw new ParamsException("paramsType or params should be same");
            } else if (paramsType.length != params.length) {
                throw new ParamsException("paramsType len:" + paramsType.length + " should equal params.len:" + params.length);
            }
            try {
                try {
                    return cls.getMethod(funName, paramsType).invoke(obj, params);
                } catch (IllegalAccessException e) {
                    LogC.e(e.toString(), (Throwable) e, false);
                    return null;
                } catch (IllegalArgumentException e2) {
                    LogC.e(e2.toString(), (Throwable) e2, false);
                    return null;
                } catch (InvocationTargetException e3) {
                    LogC.e(e3.toString(), (Throwable) e3, false);
                    return null;
                }
            } catch (NoSuchMethodException e4) {
                throw e4;
            }
        } else {
            throw new ParamsException("class is null in staticFun");
        }
    }

    public static Object invokeFun(String className, String funName, Class<?>[] paramsType, Object[] params) throws ParamsException, NoSuchMethodException {
        try {
            Class<?> cls = Class.forName(className);
            if (cls != null) {
                if (paramsType == null) {
                    if (params != null) {
                        throw new ParamsException("paramsType is null, but params is not null");
                    }
                } else if (params == null) {
                    throw new ParamsException("paramsType or params should be same");
                } else if (paramsType.length != params.length) {
                    throw new ParamsException("paramsType len:" + paramsType.length + " should equal params.len:" + params.length);
                }
                try {
                    return invokeFun(cls, cls.newInstance(), funName, paramsType, params);
                } catch (InstantiationException e) {
                    throw new ParamsException("class to newInstance error in invokeFun");
                } catch (IllegalAccessException e2) {
                    throw new ParamsException("class to newInstance error in invokeFun");
                }
            } else {
                throw new ParamsException("class is null in invokeFun");
            }
        } catch (ClassNotFoundException e3) {
            throw new ParamsException("class is null in invokeFun");
        }
    }

    public static Object reflectClass(String className, Context context, Handler handler) {
        try {
            Class cls = Class.forName(className);
            Class[] parameterTypes = null;
            Object[] parameters = null;
            if (!(context == null || handler == null)) {
                parameterTypes = new Class[]{Context.class, Handler.class};
                parameters = new Object[]{context, handler};
            }
            if (parameterTypes == null) {
                LogC.i("not need paramter to create instance.", false);
                return cls.newInstance();
            }
            LogC.i("need paramter to create instance.", false);
            return cls.getConstructor(parameterTypes).newInstance(parameters);
        } catch (ClassNotFoundException e) {
            LogC.e("New Instance by class Name happen classNotFoundException,the className:" + className, (Throwable) e, false);
            return null;
        } catch (Exception e2) {
            LogC.e("New Instance by class Name happen exception,the className:" + className, (Throwable) e2, false);
            return null;
        }
    }
}
