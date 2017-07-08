package com.huawei.utils.reflect;

import android.net.Uri;
import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class HwReflectUtils {
    private static final String TAG = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.utils.reflect.HwReflectUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.utils.reflect.HwReflectUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.utils.reflect.HwReflectUtils.<clinit>():void");
    }

    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "className not found:" + className);
            return null;
        }
    }

    public static Method getMethod(Class<?> targetClass, String name, Class<?>... parameterTypes) {
        if (targetClass == null || isEmpty(name)) {
            return null;
        }
        try {
            return targetClass.getMethod(name, parameterTypes);
        } catch (SecurityException e) {
            Log.w(TAG, e.getCause());
            return null;
        } catch (NoSuchMethodException e2) {
            Log.w(TAG, name + ", not such method.");
            return null;
        }
    }

    public static Field getField(Class<?> targetClass, String name) {
        if (targetClass == null || isEmpty(name)) {
            return null;
        }
        try {
            return targetClass.getField(name);
        } catch (SecurityException e) {
            Log.w(TAG, e.getCause());
            return null;
        } catch (NoSuchFieldException e2) {
            Log.w(TAG, name + ", no such field.");
            return null;
        }
    }

    public static Field getDeclaredField(Class<?> targetClass, String name) {
        if (targetClass == null || isEmpty(name)) {
            return null;
        }
        try {
            return targetClass.getDeclaredField(name);
        } catch (SecurityException e) {
            Log.w(TAG, e.getCause());
            return null;
        } catch (NoSuchFieldException e2) {
            Log.w(TAG, name + ", no such field.");
            return null;
        }
    }

    public static Constructor<?> getConstructor(Class<?> targetClass, Class<?>... types) {
        Constructor<?> constructor = null;
        if (targetClass == null || types == null) {
            return constructor;
        }
        try {
            return targetClass.getConstructor(types);
        } catch (SecurityException e) {
            return constructor;
        } catch (NoSuchMethodException e2) {
            return constructor;
        }
    }

    public static Object newInstance(Constructor<?> constructor, Object... args) {
        if (constructor == null) {
            return null;
        }
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            Log.e(TAG, "Exception in newInstance: " + e.getClass().getSimpleName());
            return null;
        }
    }

    public static Object invoke(Object receiver, Method method, Object... args) {
        if (method == null) {
            throw new UnsupportedOperationException();
        }
        try {
            return method.invoke(receiver, args);
        } catch (RuntimeException re) {
            Log.e(TAG, "Exception in invoke: " + re.getClass().getSimpleName());
            if ("com.huawei.android.util.NoExtAPIException".equals(re.getClass().getName())) {
                throw new UnsupportedOperationException();
            }
            throw new UnsupportedOperationException();
        } catch (Exception e) {
            Log.e(TAG, "Exception in invoke: " + e.getCause() + "; method=" + method.getName());
            throw new UnsupportedOperationException();
        }
    }

    public static Object getFieldValue(Object receiver, Field field) {
        if (field == null) {
            return null;
        }
        try {
            return field.get(receiver);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getFieldValue: " + e.getClass().getSimpleName());
            throw new UnsupportedOperationException();
        }
    }

    public static void setFieldValue(Object receiver, Field field, Object value) {
        if (field != null) {
            try {
                field.set(receiver, value);
            } catch (Exception e) {
                Log.e(TAG, "Exception in setFieldValue: " + e.getClass().getSimpleName());
            }
        }
    }

    private static boolean isEmpty(String str) {
        boolean z = true;
        if (str == null || str.length() == 0) {
            return true;
        }
        if (str.trim().length() != 0) {
            z = false;
        }
        return z;
    }

    public static int objectToInt(Object o) {
        if (o == null) {
            return -1;
        }
        return ((Integer) o).intValue();
    }

    public static int objectToInt(Object o, int def) {
        if (o == null) {
            return def;
        }
        return ((Integer) o).intValue();
    }

    public static String objectToString(Object o) {
        if (o == null) {
            return null;
        }
        return (String) o;
    }

    public static Uri objectToUri(Object o) {
        if (o == null) {
            return null;
        }
        return (Uri) o;
    }

    public static boolean objectToBoolean(Object o) {
        if (o == null) {
            return false;
        }
        return ((Boolean) o).booleanValue();
    }
}
