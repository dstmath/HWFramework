package com.android.server.location;

import android.content.Context;
import android.os.RemoteException;
import com.huawei.lcagent.client.LogCollectManager;
import dalvik.system.PathClassLoader;
import huawei.android.debug.HwDBGSwitchController;
import java.lang.reflect.Method;

public class HwReportTool {
    private static final boolean DEBUG = HwDBGSwitchController.getDBGSwitch();
    private static final String REPORTERINTERFACE_LIB_PATH = "/system/framework/com.huawei.report.jar";
    private static final String REPORT_CLS = "com.huawei.report.ReporterInterface";
    private static final String REPORT_METHOD_E = "e";
    private static final String TAG = "HwReportTool";
    private static volatile HwReportTool sSingleInstance = null;
    private LogCollectManager mClient = null;
    private Context mContext = null;
    private Method mReportMethod = null;
    private Class<?> mReporterClazz = null;

    private HwReportTool(Context context) {
        initReporter(context);
    }

    public static HwReportTool getInstance(Context context) {
        if (sSingleInstance == null) {
            sSingleInstance = new HwReportTool(context.getApplicationContext());
        }
        return sSingleInstance;
    }

    private void initReporter(Context context) {
        if (context != null) {
            try {
                this.mReporterClazz = new PathClassLoader(REPORTERINTERFACE_LIB_PATH, context.getClassLoader()).loadClass(REPORT_CLS);
                if (this.mReporterClazz != null) {
                    this.mReportMethod = this.mReporterClazz.getDeclaredMethod(REPORT_METHOD_E, Context.class, Integer.TYPE, String.class);
                    this.mClient = new LogCollectManager(context);
                    this.mContext = context;
                }
            } catch (ClassNotFoundException e) {
                LBSLog.e(TAG, false, "Can't find mReporterClazz", new Object[0]);
                this.mReporterClazz = null;
            } catch (NoSuchMethodException e2) {
                LBSLog.e(TAG, false, "Can't find mReportMethod", new Object[0]);
                this.mReportMethod = null;
            }
        }
    }

    public boolean report(int eventId, String eventMsg) {
        Class<?> cls;
        if (isBetaUser()) {
            Method method = this.mReportMethod;
            if (!(method == null || (cls = this.mReporterClazz) == null)) {
                try {
                    return ((Boolean) method.invoke(cls, this.mContext, Integer.valueOf(eventId), eventMsg)).booleanValue();
                } catch (Exception e) {
                    LBSLog.e(TAG, false, "got exception", new Object[0]);
                }
            }
        } else {
            LBSLog.e(TAG, false, "This is not beta user build", new Object[0]);
        }
        return false;
    }

    private boolean isBetaUser() {
        return getUserType() == 3;
    }

    private int getUserType() {
        int userType = -1;
        LogCollectManager logCollectManager = this.mClient;
        if (logCollectManager == null) {
            return -1;
        }
        try {
            userType = logCollectManager.getUserType();
        } catch (RemoteException e) {
            LBSLog.e(TAG, false, "got remote exception: %{public}s", e.getMessage());
        }
        LBSLog.i(TAG, false, "userType is: %{public}d", Integer.valueOf(userType));
        return userType;
    }
}
