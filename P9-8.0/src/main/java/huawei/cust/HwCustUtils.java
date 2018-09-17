package huawei.cust;

import android.os.SystemProperties;
import android.util.Log;
import android.util.LogException;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public final class HwCustUtils {
    static final String CUST_CLS_NULL_REPLACE = "-";
    static final String CUST_CLS_SUFFIX_DEF = "Impl:-";
    static final String CUST_CLS_SUFFIX_SEP = ":";
    static boolean CUST_VERSION = new File(FILE_ONLY_IN_CUST).exists();
    static final boolean DEBUG_I = true;
    static final boolean EXCEPTION_WHEN_ERROR = true;
    static final String[] FACTORY_ARRAY = SystemProperties.get(PROP_CUST_CLS_SUFFIX, CUST_CLS_SUFFIX_DEF).split(":");
    static String FILE_ONLY_IN_CUST = "/system/etc/permissions/hwcustframework.xml";
    static final String HWCUST_PREFIX = "HwCust";
    static final String PROP_CUST_CLS_SUFFIX = "cust.cls.suffixes";
    static final String TAG = "HwCust";
    private static HashMap<String, ClassInfo> mClassCache = new HashMap();
    private static HashMap<String, Constructor<?>> mConstructorCache = new HashMap();
    private static HashMap<Class<?>, Class<?>> mPrimitiveMap = new HashMap();

    static class ClassInfo {
        Class<?> mCls;
        Constructor<?>[] mCs;
        String mOrgClsName;

        ClassInfo(String orgName, Class<?> cls) {
            this.mOrgClsName = orgName;
            this.mCls = cls;
            this.mCs = cls.getDeclaredConstructors();
        }
    }

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
        while (i < FACTORY_ARRAY.length) {
            if (FACTORY_ARRAY[i] == null || FACTORY_ARRAY[i].equals("-")) {
                FACTORY_ARRAY[i] = LogException.NO_VALUE;
            }
            i++;
        }
    }

    public static Object createObj(String className, ClassLoader cl, Object... args) {
        Throwable ex;
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
            Object obj = useConstructor.newInstance(args);
            Log.d("HwCust", "Create obj success use " + clsInfo.mCls);
            return obj;
        } catch (Throwable e) {
            ex = e;
            handle_exception("create cust obj fail. Class = " + clsInfo.mCls + ", constructor = " + useConstructor, ex);
            return null;
        } catch (Throwable e2) {
            ex = e2;
            handle_exception("create cust obj fail. Class = " + clsInfo.mCls + ", constructor = " + useConstructor, ex);
            return null;
        } catch (Throwable e3) {
            ex = e3;
            handle_exception("create cust obj fail. Class = " + clsInfo.mCls + ", constructor = " + useConstructor, ex);
            return null;
        } catch (Throwable e4) {
            ex = e4;
            handle_exception("create cust obj fail. Class = " + clsInfo.mCls + ", constructor = " + useConstructor, ex);
            return null;
        } catch (Throwable e5) {
            ex = e5;
            handle_exception("create cust obj fail. Class = " + clsInfo.mCls + ", constructor = " + useConstructor, ex);
            return null;
        }
    }

    public static Object createObj(Class<?> classClass, Object... args) {
        return createObj(classClass.getName(), classClass.getClassLoader(), args);
    }

    /* JADX WARNING: Missing block: B:34:0x00c0, code:
            return r0;
     */
    /* JADX WARNING: Missing block: B:40:0x00c8, code:
            if (r4 != (r11.length - 1)) goto L_0x007a;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static synchronized ClassInfo getClassByName(String className, ClassLoader cl, String[] allSuffix) {
        synchronized (HwCustUtils.class) {
            ClassInfo clsInfo = (ClassInfo) mClassCache.get(className);
            if (clsInfo != null) {
                return clsInfo;
            }
            if (className != null) {
                if (className.length() != 0) {
                    if (!className.contains("$") && (className.contains(".HwCust") ^ 1) == 0) {
                        Class<?> dstClass;
                        int i = 0;
                        while (true) {
                            ClassInfo clsInfo2 = clsInfo;
                            if (i >= allSuffix.length) {
                                clsInfo = clsInfo2;
                                break;
                            }
                            try {
                                dstClass = Class.forName(className + allSuffix[i], true, cl);
                                clsInfo = new ClassInfo(className, dstClass);
                                try {
                                    mClassCache.put(className, clsInfo);
                                    if (CUST_VERSION && i == allSuffix.length - 1) {
                                        break;
                                    } else if (CUST_VERSION) {
                                    }
                                } catch (ClassNotFoundException e) {
                                }
                            } catch (ClassNotFoundException e2) {
                                clsInfo = clsInfo2;
                            }
                            i++;
                        }
                        Log.w("HwCust", "CUST VERSION = " + CUST_VERSION + ", use class = " + dstClass);
                        if (clsInfo == null) {
                            handle_exception("Class / custClass not found for: " + className, new ClassNotFoundException());
                        }
                    }
                }
            }
            handle_exception("createCustImpl obj, className invalid: " + className, new Exception());
            return null;
        }
    }

    static synchronized Constructor<?> findConstructor(ClassInfo info, Object... args) {
        synchronized (HwCustUtils.class) {
            String tag = getArgsType(info.mOrgClsName, args);
            Constructor<?> useConstructor = (Constructor) mConstructorCache.get(tag);
            if (useConstructor != null) {
                return useConstructor;
            }
            for (Constructor<?> c : info.mCs) {
                Class<?>[] ptcs = c.getParameterTypes();
                if (!Modifier.isPrivate(c.getModifiers()) && ptcs.length == args.length) {
                    if (ptcs.length == 0) {
                        useConstructor = c;
                    } else {
                        for (int i = 0; i < args.length; i++) {
                            if (args[i] == null) {
                                if (ptcs[i].isPrimitive()) {
                                    break;
                                }
                            }
                            Class<?> argCls = args[i].getClass();
                            Class<?> ptcCls = ptcs[i];
                            if (argCls.isPrimitive()) {
                                argCls = (Class) mPrimitiveMap.get(argCls);
                            }
                            if (ptcCls.isPrimitive()) {
                                ptcCls = (Class) mPrimitiveMap.get(ptcCls);
                            }
                            if (!ptcCls.isAssignableFrom(argCls)) {
                                break;
                            }
                            if (i == args.length - 1) {
                                useConstructor = c;
                            }
                        }
                    }
                    if (useConstructor != null) {
                        log_info("Constructor found for " + info.mCls);
                        break;
                    }
                }
            }
            mConstructorCache.put(tag, useConstructor);
            return useConstructor;
        }
    }

    static String getArgsType(String clsName, Object... args) {
        StringBuilder sb = new StringBuilder(clsName + ":" + "-");
        for (Object arg : args) {
            if (arg == null) {
                sb.append(":null");
            } else {
                sb.append(":").append(arg.getClass());
            }
        }
        return sb.toString();
    }

    static void log_info(String msg) {
        Log.i("HwCust", msg);
    }

    static void handle_exception(String msg, Throwable th) {
        throw new RuntimeException(msg, th);
    }
}
