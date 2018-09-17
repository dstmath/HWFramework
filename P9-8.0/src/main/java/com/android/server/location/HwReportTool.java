package com.android.server.location;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
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
    private Method sReportMethod = null;
    private Class<?> sReporterClazz = null;

    public static HwReportTool getInstance(Context context) {
        if (sSingleInstance == null) {
            sSingleInstance = new HwReportTool(context.getApplicationContext());
        }
        return sSingleInstance;
    }

    private HwReportTool(Context context) {
        initReporter(context);
    }

    private void initReporter(Context context) {
        try {
            this.sReporterClazz = new PathClassLoader(REPORTERINTERFACE_LIB_PATH, context.getClassLoader()).loadClass(REPORT_CLS);
            this.sReportMethod = this.sReporterClazz.getDeclaredMethod(REPORT_METHOD_E, new Class[]{Context.class, Integer.TYPE, String.class});
            this.mClient = new LogCollectManager(context);
            this.mContext = context;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Can't find sReporterClazz");
            this.sReporterClazz = null;
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "Can't find sReportMethod");
            this.sReportMethod = null;
        } catch (NullPointerException e3) {
            e3.printStackTrace();
        }
    }

    public boolean report(int eventID, String eventMsg) {
        if (!isBetaUser()) {
            Log.e(TAG, "This is not beta user build");
        } else if (!(this.sReportMethod == null || this.sReporterClazz == null)) {
            try {
                return ((Boolean) this.sReportMethod.invoke(this.sReporterClazz, new Object[]{this.mContext, Integer.valueOf(eventID), eventMsg})).booleanValue();
            } catch (Exception e) {
                Log.e(TAG, "got exception" + e.getMessage(), e);
            }
        }
        return false;
    }

    private boolean isBetaUser() {
        return 3 == getUserType();
    }

    private int getUserType() {
        int userType = -1;
        if (this.mClient == null) {
            return userType;
        }
        try {
            userType = this.mClient.getUserType();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NullPointerException e2) {
            e2.printStackTrace();
        }
        if (DEBUG) {
            Log.d(TAG, "userType is: " + userType);
        }
        return userType;
    }
}
