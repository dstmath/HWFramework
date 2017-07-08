package huawei.cust;

import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public final class HwCustUtils {
    static final String CUST_CLS_NULL_REPLACE = "-";
    static final String CUST_CLS_SUFFIX_DEF = "Impl:-";
    static final String CUST_CLS_SUFFIX_SEP = ":";
    static boolean CUST_VERSION = false;
    static final boolean DEBUG_I = true;
    static final boolean EXCEPTION_WHEN_ERROR = true;
    static final String[] FACTORY_ARRAY = null;
    static String FILE_ONLY_IN_CUST = null;
    static final String HWCUST_PREFIX = "HwCust";
    static final String PROP_CUST_CLS_SUFFIX = "cust.cls.suffixes";
    static final String TAG = "HwCust";
    private static HashMap<String, ClassInfo> mClassCache;
    private static HashMap<String, Constructor<?>> mConstructorCache;
    private static HashMap<Class<?>, Class<?>> mPrimitiveMap;

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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.cust.HwCustUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.cust.HwCustUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.cust.HwCustUtils.<clinit>():void");
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
            Log.d(TAG, "Create obj success use " + clsInfo.mCls);
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

    static synchronized ClassInfo getClassByName(String className, ClassLoader cl, String[] allSuffix) {
        synchronized (HwCustUtils.class) {
            ClassInfo clsInfo = (ClassInfo) mClassCache.get(className);
            if (clsInfo != null) {
                return clsInfo;
            }
            if (className != null) {
                if (className.length() != 0) {
                    if (!className.contains("$") && className.contains(".HwCust")) {
                        int i = 0;
                        ClassInfo clsInfo2 = clsInfo;
                        while (i < allSuffix.length) {
                            try {
                                Class<?> dstClass = Class.forName(className + allSuffix[i], EXCEPTION_WHEN_ERROR, cl);
                                clsInfo = new ClassInfo(className, dstClass);
                                try {
                                    mClassCache.put(className, clsInfo);
                                    if (!CUST_VERSION || i != allSuffix.length - 1) {
                                        if (!(CUST_VERSION || i == allSuffix.length - 1)) {
                                        }
                                    }
                                    Log.w(TAG, "CUST VERSION = " + CUST_VERSION + ", use class = " + dstClass);
                                    break;
                                } catch (ClassNotFoundException e) {
                                }
                            } catch (ClassNotFoundException e2) {
                                clsInfo = clsInfo2;
                                i++;
                                clsInfo2 = clsInfo;
                            }
                        }
                        clsInfo = clsInfo2;
                        if (clsInfo == null) {
                            handle_exception("Class / custClass not found for: " + className, new ClassNotFoundException());
                        }
                        return clsInfo;
                    }
                }
            }
            handle_exception("createCustImpl obj, className invalid: " + className, new Exception());
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static synchronized Constructor<?> findConstructor(ClassInfo info, Object... args) {
        synchronized (HwCustUtils.class) {
            String tag = getArgsType(info.mOrgClsName, args);
            Constructor<?> useConstructor = (Constructor) mConstructorCache.get(tag);
            if (useConstructor != null) {
                return useConstructor;
            }
            loop0:
            for (Constructor<?> c : info.mCs) {
                Class<?>[] ptcs = c.getParameterTypes();
                if (!Modifier.isPrivate(c.getModifiers()) && ptcs.length == args.length) {
                    if (ptcs.length == 0) {
                        useConstructor = c;
                    } else {
                        int i = 0;
                        while (true) {
                            if (i >= args.length) {
                                break;
                            }
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
                            i++;
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
        StringBuilder sb = new StringBuilder(clsName + CUST_CLS_SUFFIX_SEP + CUST_CLS_NULL_REPLACE);
        for (Object arg : args) {
            if (arg == null) {
                sb.append(":null");
            } else {
                sb.append(CUST_CLS_SUFFIX_SEP).append(arg.getClass());
            }
        }
        return sb.toString();
    }

    static void log_info(String msg) {
        Log.i(TAG, msg);
    }

    static void handle_exception(String msg, Throwable th) {
        throw new RuntimeException(msg, th);
    }
}
