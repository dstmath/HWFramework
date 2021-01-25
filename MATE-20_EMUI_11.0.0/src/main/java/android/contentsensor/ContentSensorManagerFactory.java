package android.contentsensor;

import android.app.Activity;
import android.app.AppGlobals;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ContentSensorManagerFactory {
    private static final String SENSOR_MANAGER_CLASS_NAME = "com.huawei.contentsensor.agent.ContentSensorManager";
    private static final String SENSOR_MANAGER_PACKAGE_NAME = "com.huawei.contentsensor";
    private static final String TAG = "ContentSensorFactory";
    private static Class<IContentSensorManager> sContentSensorClz = null;
    private static volatile PackageInfo sPackageInfo = null;

    public static IContentSensorManager createContentSensorManager(int token, Activity activity) {
        System.currentTimeMillis();
        IContentSensorManager sensor = null;
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            Class<IContentSensorManager> clz = getContentSensorManagerClass();
            if (clz != null) {
                Constructor constructor = clz.getConstructor(Integer.TYPE, Activity.class);
                constructor.setAccessible(true);
                sensor = constructor.newInstance(Integer.valueOf(token), activity);
            }
        } catch (ClassNotFoundException e) {
            LogUtil.e("ContentSensorFactory", "ClassNotFoundExceptione:" + e);
        } catch (NoSuchMethodException e2) {
            LogUtil.e("ContentSensorFactory", "NoSuchMethodExceptione:" + e2);
        } catch (InstantiationException e3) {
            LogUtil.e("ContentSensorFactory", "InstantiationExceptione:" + e3);
        } catch (IllegalAccessException e4) {
            LogUtil.e("ContentSensorFactory", "IllegalAccessExceptione:" + e4);
        } catch (InvocationTargetException e5) {
            LogUtil.e("ContentSensorFactory", "InvocationTargetExceptione:" + e5);
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(oldPolicy);
            throw th;
        }
        StrictMode.setThreadPolicy(oldPolicy);
        if (sensor != null) {
            return sensor;
        }
        LogUtil.w("ContentSensorFactory", "DefaultContentSensorManager is created");
        return new DefaultContentSensorManager();
    }

    /* JADX DEBUG: Type inference failed for r6v2. Raw type applied. Possible types: java.lang.Class<?>, java.lang.Class<android.contentsensor.IContentSensorManager> */
    private static synchronized Class<IContentSensorManager> getContentSensorManagerClass() throws ClassNotFoundException {
        synchronized (ContentSensorManagerFactory.class) {
            if (sContentSensorClz != null) {
                return sContentSensorClz;
            }
            PackageInfo packageInfo = fetchPackageInfo();
            if (packageInfo == null) {
                return null;
            }
            try {
                sContentSensorClz = Class.forName(SENSOR_MANAGER_CLASS_NAME, true, AppGlobals.getInitialApplication().createPackageContext(packageInfo.packageName, 3).getClassLoader());
                return sContentSensorClz;
            } catch (PackageManager.NameNotFoundException e) {
                LogUtil.e("ContentSensorFactory", "can not find class com.huawei.contentsensor.agent.ContentSensorManager");
                return null;
            }
        }
    }

    private static PackageInfo fetchPackageInfo() {
        PackageManager pm;
        if (sPackageInfo != null) {
            return sPackageInfo;
        }
        Application app = AppGlobals.getInitialApplication();
        if (app == null || (pm = app.getPackageManager()) == null) {
            return null;
        }
        try {
            sPackageInfo = pm.getPackageInfo(SENSOR_MANAGER_PACKAGE_NAME, 128);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.e("ContentSensorFactory", "can not find package com.huawei.contentsensor");
        }
        return sPackageInfo;
    }

    public static class DefaultContentSensorManager implements IContentSensorManager {
        @Override // android.contentsensor.IContentSensorManager
        public void updateToken(int token, Activity activity) {
        }

        @Override // android.contentsensor.IContentSensorManager
        public void copyNode(Bundle data) {
        }

        @Override // android.contentsensor.IContentSensorManager
        public void processImageAndWebView(Bundle data) {
        }
    }

    /* access modifiers changed from: package-private */
    public static class LogUtil {
        public static final String TAG = "ContentSensorFactory";
        private static boolean mIsDLogCanPrint;
        private static boolean mIsELogCanPrint;
        private static boolean mIsILogCanPrint;
        private static boolean mIsVLogCanPrint;
        private static boolean mIsWLogCanPrint;

        LogUtil() {
        }

        static {
            mIsVLogCanPrint = false;
            mIsDLogCanPrint = true;
            mIsILogCanPrint = true;
            mIsWLogCanPrint = true;
            mIsELogCanPrint = true;
            mIsVLogCanPrint = isNormalLogCanPrint("ContentSensorFactory", 2);
            mIsDLogCanPrint = isNormalLogCanPrint("ContentSensorFactory", 3);
            mIsILogCanPrint = isNormalLogCanPrint("ContentSensorFactory", 4);
            mIsWLogCanPrint = isNormalLogCanPrint("ContentSensorFactory", 5);
            mIsELogCanPrint = isNormalLogCanPrint("ContentSensorFactory", 6);
        }

        private static boolean isNormalLogCanPrint(String tag, int level) {
            return Log.isLoggable(tag, level);
        }

        public static void v(String className, String msg) {
            if (mIsVLogCanPrint) {
                Log.v("ContentSensorFactory", className + ": " + msg);
            }
        }

        public static void d(String className, String msg) {
            if (mIsDLogCanPrint) {
                Log.d("ContentSensorFactory", className + ": " + msg);
            }
        }

        public static void i(String className, String msg) {
            if (mIsILogCanPrint) {
                Log.i("ContentSensorFactory", className + ": " + msg);
            }
        }

        public static void w(String className, String msg) {
            if (mIsWLogCanPrint) {
                Log.w("ContentSensorFactory", className + ": " + msg);
            }
        }

        public static void e(String className, String msg) {
            if (mIsELogCanPrint) {
                Log.e("ContentSensorFactory", className + ": " + msg);
            }
        }

        public static void logException(String className, String msg, Exception e) {
            if (mIsELogCanPrint) {
                if (!TextUtils.isEmpty(msg)) {
                    if (e != null) {
                        Log.e("ContentSensorFactory", className + ": " + msg + e.getMessage(), e);
                        return;
                    }
                    Log.e("ContentSensorFactory", className + ": " + msg);
                } else if (e != null) {
                    Log.e("ContentSensorFactory", className + ": " + msg);
                }
            }
        }
    }
}
