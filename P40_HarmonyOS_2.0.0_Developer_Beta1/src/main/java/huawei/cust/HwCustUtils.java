package huawei.cust;

import android.os.SystemProperties;
import android.util.Log;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public final class HwCustUtils {
    static final String CUST_CLS_NULL_REPLACE = "-";
    static final String CUST_CLS_SUFFIX_DEF = "Impl:-";
    static final String CUST_CLS_SUFFIX_SEP = ":";
    static boolean CUST_VERSION = new File(FILE_ONLY_IN_CUST).exists();
    static final boolean DEBUG_I = false;
    static final boolean EXCEPTION_WHEN_ERROR = true;
    static final String[] FACTORY_ARRAY = SystemProperties.get(PROP_CUST_CLS_SUFFIX, CUST_CLS_SUFFIX_DEF).split(":");
    static String FILE_ONLY_IN_CUST = "/system/etc/permissions/hwcustframework.xml";
    static final String HWCUST_PREFIX = "HwCust";
    static final String PROP_CUST_CLS_SUFFIX = "cust.cls.suffixes";
    static final String TAG = "HwCust";
    private static HashMap<String, ClassInfo> mClassCache = new HashMap<>();
    private static HashMap<String, Constructor<?>> mConstructorCache = new HashMap<>();
    private static HashMap<Class<?>, Class<?>> mPrimitiveMap = new HashMap<>();
    private static String mRegion;

    static {
        mPrimitiveMap.put(Boolean.TYPE, Boolean.class);
        mPrimitiveMap.put(Byte.TYPE, Byte.class);
        mPrimitiveMap.put(Character.TYPE, Character.class);
        mPrimitiveMap.put(Short.TYPE, Short.class);
        mPrimitiveMap.put(Integer.TYPE, Integer.class);
        mPrimitiveMap.put(Long.TYPE, Long.class);
        mPrimitiveMap.put(Float.TYPE, Float.class);
        mPrimitiveMap.put(Double.TYPE, Double.class);
        int i = 0;
        while (true) {
            String[] strArr = FACTORY_ARRAY;
            if (i < strArr.length) {
                if (strArr[i] == null || strArr[i].equals("-")) {
                    FACTORY_ARRAY[i] = "";
                }
                i++;
            } else {
                initRegionInfo();
                return;
            }
        }
    }

    public static Object createObj(String className, ClassLoader cl, Object... args) {
        ClassInfo clsInfo = getClassByName(className, cl, FACTORY_ARRAY);
        if (clsInfo == null) {
            return null;
        }
        Constructor<?> useConstructor = findConstructor(clsInfo, args);
        if (useConstructor == null) {
            handle_exception("constructor not found for " + clsInfo.mCls, new NullPointerException());
            return null;
        }
        try {
            return useConstructor.newInstance(args);
        } catch (ExceptionInInitializerError | IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException ex) {
            handle_exception("create cust obj fail. Class = " + clsInfo.mCls + ", constructor = " + useConstructor, ex);
            return null;
        }
    }

    public static Object createObj(Class<?> classClass, Object... args) {
        return createObj(classClass.getName(), classClass.getClassLoader(), args);
    }

    public static String getVersionRegion() {
        return mRegion;
    }

    private static void initRegionInfo() {
        String optb = SystemProperties.get("ro.config.hw_optb", "");
        String vendor2 = SystemProperties.get("ro.hw.vendor", "");
        String country = SystemProperties.get("ro.hw.country", "");
        if (optb.equals("156")) {
            mRegion = "cn";
        } else if (optb.equals("376")) {
            mRegion = "il";
        } else if (((vendor2.equals("orange") || vendor2.equals("altice")) && country.equals("all")) || (vendor2.equals("tef") && country.equals("normal"))) {
            mRegion = "eu";
        } else if (!vendor2.equals("orange") || !country.equals("btob")) {
            mRegion = country;
        } else {
            mRegion = "fr";
        }
    }

    static synchronized ClassInfo getClassByName(String className, ClassLoader cl, String[] allSuffix) {
        synchronized (HwCustUtils.class) {
            ClassInfo clsInfo = mClassCache.get(className);
            if (clsInfo != null) {
                return clsInfo;
            }
            if (className == null || className.length() == 0 || className.contains("$") || !className.contains(".HwCust")) {
                handle_exception("createCustImpl obj, className invalid: " + className, new Exception());
                return null;
            }
            int i = 0;
            while (true) {
                if (i >= allSuffix.length) {
                    break;
                }
                try {
                    Class<?> dstClass = Class.forName(className + allSuffix[i], true, cl);
                    clsInfo = new ClassInfo(className, dstClass);
                    mClassCache.put(className, clsInfo);
                    if ((CUST_VERSION && i == allSuffix.length - 1) || (!CUST_VERSION && i != allSuffix.length - 1)) {
                        Log.w("HwCust", "CUST VERSION = " + CUST_VERSION + ", use class = " + dstClass);
                    }
                } catch (ClassNotFoundException e) {
                    i++;
                }
            }
            if (clsInfo == null) {
                handle_exception("Class / custClass not found for: " + className, new ClassNotFoundException());
            }
            return clsInfo;
        }
    }

    static synchronized Constructor<?> findConstructor(ClassInfo info, Object... args) {
        synchronized (HwCustUtils.class) {
            String tag = getArgsType(info.mOrgClsName, args);
            Constructor<?> useConstructor = mConstructorCache.get(tag);
            if (useConstructor != null) {
                return useConstructor;
            }
            Constructor<?>[] constructorArr = info.mCs;
            Constructor<?> useConstructor2 = useConstructor;
            for (Constructor<?> c : constructorArr) {
                Class<?>[] ptcs = c.getParameterTypes();
                if (!Modifier.isPrivate(c.getModifiers()) && ptcs.length == args.length) {
                    if (ptcs.length == 0) {
                        useConstructor2 = c;
                    } else {
                        for (int i = 0; i < args.length; i++) {
                            if (args[i] == null) {
                                if (ptcs[i].isPrimitive()) {
                                    break;
                                }
                            } else {
                                Class<?> argCls = args[i].getClass();
                                Class<?> ptcCls = ptcs[i];
                                if (!(ptcCls.isPrimitive() ? mPrimitiveMap.get(ptcCls) : ptcCls).isAssignableFrom(argCls.isPrimitive() ? mPrimitiveMap.get(argCls) : argCls)) {
                                    break;
                                }
                            }
                            if (i == args.length - 1) {
                                useConstructor2 = c;
                            }
                        }
                    }
                    if (useConstructor2 != null) {
                        break;
                    }
                }
            }
            mConstructorCache.put(tag, useConstructor2);
            return useConstructor2;
        }
    }

    static String getArgsType(String clsName, Object... args) {
        StringBuilder sb = new StringBuilder(clsName + ":-");
        for (Object arg : args) {
            if (arg == null) {
                sb.append(":null");
            } else {
                sb.append(":" + arg.getClass());
            }
        }
        return sb.toString();
    }

    static void log_info(String msg) {
    }

    static void handle_exception(String msg, Throwable th) {
        throw new RuntimeException(msg, th);
    }

    /* access modifiers changed from: package-private */
    public static class ClassInfo {
        Class<?> mCls;
        Constructor<?>[] mCs;
        String mOrgClsName;

        ClassInfo(String orgName, Class<?> cls) {
            this.mOrgClsName = orgName;
            this.mCls = cls;
            this.mCs = cls.getDeclaredConstructors();
        }
    }
}
