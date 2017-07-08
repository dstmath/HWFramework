package com.android.server.rms.utils;

import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Utils {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final long DATE_TIME_24HOURS = 86400000;
    public static final boolean DEBUG = false;
    public static final int FLAG_BIGDATA_STATISTIC = 1;
    public static final int FLAG_CRASH_MONITOR = 16;
    public static final int FLAG_IO_STATISTIC = 8;
    public static final int FLAG_SCREENOFF_TRIM = 4;
    public static final int FLAG_TIMING_TRIM = 2;
    public static final boolean HWFLOW = false;
    public static final boolean HWLOGW_E = true;
    private static final String PARAM_SPLIT = ":";
    public static final int RMSVERSION = 0;
    public static final String TAG = "RMS";

    /* renamed from: com.android.server.rms.utils.Utils.1 */
    static class AnonymousClass1 implements PrivilegedAction<Object> {
        final /* synthetic */ Method val$methodResult;

        AnonymousClass1(Method val$methodResult) {
            this.val$methodResult = val$methodResult;
        }

        public Void run() {
            this.val$methodResult.setAccessible(Utils.HWLOGW_E);
            return null;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.utils.Utils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.utils.Utils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.utils.Utils.<clinit>():void");
    }

    public static final boolean writeFile(String path, String data) {
        IOException e;
        FileOutputStream fos = null;
        try {
            FileOutputStream fos2 = new FileOutputStream(path);
            try {
                fos2.write(data.getBytes("UTF-8"));
                if (fos2 != null) {
                    try {
                        fos2.close();
                    } catch (IOException e2) {
                    }
                }
                return HWLOGW_E;
            } catch (IOException e3) {
                e = e3;
                fos = fos2;
                try {
                    Log.w(TAG, "Unable to write " + path + " msg=" + e.getMessage());
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e4) {
                        }
                    }
                    return HWFLOW;
                } catch (Throwable th) {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e5) {
                        }
                    }
                    return HWLOGW_E;
                }
            } catch (Throwable th2) {
                fos = fos2;
                if (fos != null) {
                    fos.close();
                }
                return HWLOGW_E;
            }
        } catch (IOException e6) {
            e = e6;
            Log.w(TAG, "Unable to write " + path + " msg=" + e.getMessage());
            if (fos != null) {
                fos.close();
            }
            return HWFLOW;
        }
    }

    public static final void wait(int ms) {
        try {
            Thread.sleep((long) ms);
        } catch (InterruptedException e) {
        }
    }

    public static boolean scanArgs(String[] args, String value) {
        if (args != null) {
            int length = args.length;
            for (int i = RMSVERSION; i < length; i += FLAG_BIGDATA_STATISTIC) {
                if (value.equals(args[i])) {
                    return HWLOGW_E;
                }
            }
        }
        return HWFLOW;
    }

    public static String scanArgsWithParam(String[] args, String key) {
        if (args == null || key == null) {
            Log.e(TAG, "scanArgsWithParam,neither args or key is null");
            return null;
        }
        String result = null;
        int length = args.length;
        for (int i = RMSVERSION; i < length; i += FLAG_BIGDATA_STATISTIC) {
            String arg = args[i];
            if (arg != null && arg.contains(key)) {
                String[] splitsArray = arg.split(PARAM_SPLIT);
                if (splitsArray.length < FLAG_TIMING_TRIM) {
                    break;
                }
                result = splitsArray[FLAG_BIGDATA_STATISTIC];
            }
        }
        return result;
    }

    public static Object invokeMethod(Object instance, String methodName, Class[] parameterType, Object... argsValues) {
        if (instance == null) {
            Log.e(TAG, "invokeMethod,instance is null");
            return null;
        }
        Object resultObj = null;
        try {
            Method method;
            Class<?> classObj = instance.getClass();
            if (parameterType != null) {
                method = classObj.getDeclaredMethod(methodName, parameterType);
            } else {
                method = classObj.getDeclaredMethod(methodName, new Class[RMSVERSION]);
            }
            AccessController.doPrivileged(new AnonymousClass1(method));
            resultObj = method.invoke(instance, argsValues);
        } catch (RuntimeException e) {
            Log.e(TAG, "invokeMethod,RuntimeException method:" + methodName + ",msg:" + e.getMessage());
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "invokeMethod,no such method:" + methodName + ",msg:" + e2.getMessage());
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "invokeMethod,IllegalAccessException,method:" + methodName + ",msg:" + e3.getMessage());
        } catch (Exception ex) {
            Log.e(TAG, "invokeMethod,Exception,method:" + methodName + ",msg:" + ex.getMessage());
        }
        return resultObj;
    }

    public static String getDateFormatValue(long time) {
        return new SimpleDateFormat(DATE_FORMAT, Locale.US).format(new Date(time));
    }

    public static long getShortDateFormatValue(long time) {
        SimpleDateFormat sdFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        try {
            return sdFormatter.parse(sdFormatter.format(new Date(time))).getTime();
        } catch (Exception e) {
            Log.e(TAG, "getShortDateFormatValue:" + e.getMessage());
            return 0;
        }
    }

    public static long getDifferencesByDay(long time1, long time2) {
        return (time1 - time2) / DATE_TIME_24HOURS;
    }

    public static long getSizeOfDirectory(File directory) {
        long totalSizeInDirectory = 0;
        try {
            if (directory.exists()) {
                String[] subFiles = directory.list();
                for (int i = RMSVERSION; i < subFiles.length; i += FLAG_BIGDATA_STATISTIC) {
                    totalSizeInDirectory += new File(directory, subFiles[i]).length();
                }
                return totalSizeInDirectory;
            }
            Log.e(TAG, "getSizeOfDirectory," + directory.getCanonicalPath() + " not exists");
            return 0;
        } catch (IOException ex) {
            Log.e(TAG, "getSizeOfDirectory,IOException occurs:" + ex.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "getSizeOfDirectory,Exception occurs:" + e.getMessage());
        }
    }
}
