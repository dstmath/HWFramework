package com.huawei.android.server.wifi;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.lcagent.client.LogCollectManager;
import dalvik.system.PathClassLoader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReportTool {
    private static final boolean DBG = false;
    private static final boolean HWLOGW_E = true;
    private static final String REPORTERINTERFACE_LIB_PATH = "/system/framework/com.huawei.report.jar";
    private static final String REPORT_CLS = "com.huawei.report.ReporterInterface";
    private static final String REPORT_METHOD_E = "e";
    private static final String TAG = "ReportTools";
    private static LogCollectManager client = null;
    private static Object lockHelper = new Object();
    private static Context mContext = null;
    private static Class<?> reportClazz = null;
    private static Method reportMethod = null;
    private static volatile ReportTool singleInstance = null;

    public static ReportTool getInstance(Context context) {
        if (singleInstance == null) {
            synchronized (lockHelper) {
                if (singleInstance == null) {
                    singleInstance = new ReportTool(context.getApplicationContext());
                }
            }
        }
        return singleInstance;
    }

    private ReportTool(Context context) {
        initReporter(context);
    }

    private void initReporter(Context context) {
        try {
            reportClazz = new PathClassLoader(REPORTERINTERFACE_LIB_PATH, context.getClassLoader()).loadClass(REPORT_CLS);
            reportMethod = reportClazz.getDeclaredMethod(REPORT_METHOD_E, Context.class, Integer.TYPE, String.class);
            client = new LogCollectManager(context);
            mContext = context;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Can't find reportClazz");
            reportClazz = null;
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "Can't find reportMethod");
            reportMethod = null;
        }
    }

    public boolean report(int eventID, String eventMsg) {
        if (isBetaUser()) {
            try {
                if (!(reportMethod == null || reportClazz == null)) {
                    return ((Boolean) reportMethod.invoke(reportClazz, mContext, Integer.valueOf(eventID), eventMsg)).booleanValue();
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                Log.e(TAG, "got exception" + e.getMessage(), e);
            }
        } else {
            Log.e(TAG, "This is not beta user build");
        }
        return DBG;
    }

    private boolean isBetaUser() {
        return 3 == getUserType() ? HWLOGW_E : DBG;
    }

    private int getUserType() {
        try {
            return client.getUserType();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException");
            return -1;
        } catch (NullPointerException e2) {
            Log.e(TAG, "NullPointerException");
            return -1;
        }
    }
}
