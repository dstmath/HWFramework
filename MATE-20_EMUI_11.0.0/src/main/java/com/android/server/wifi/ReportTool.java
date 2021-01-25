package com.android.server.wifi;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.wifi.hwUtil.IHwLogCollectManagerEx;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Method;

public class ReportTool {
    private static final boolean DBG = false;
    private static final String REPORTERINTERFACE_LIB_PATH = "/system/framework/com.huawei.report.jar";
    private static final String REPORT_CLS = "com.huawei.report.ReporterInterface";
    private static final String REPORT_METHOD_E = "e";
    private static final String TAG = "ReportTools";
    private static Context mContext = null;
    private static IHwLogCollectManagerEx mHwLogCollectManagerEx = null;
    private static Method sReportMethod = null;
    private static Class<?> sReporterClazz = null;
    private static ReportTool sSingleInstance = null;

    public static ReportTool getInstance(Context context) {
        if (sSingleInstance == null) {
            sSingleInstance = new ReportTool(context.getApplicationContext());
        }
        return sSingleInstance;
    }

    private ReportTool(Context context) {
        initReporter(context);
    }

    private void initReporter(Context context) {
        try {
            sReporterClazz = new PathClassLoader(REPORTERINTERFACE_LIB_PATH, context.getClassLoader()).loadClass(REPORT_CLS);
            sReportMethod = sReporterClazz.getDeclaredMethod(REPORT_METHOD_E, Context.class, Integer.TYPE, String.class);
            mHwLogCollectManagerEx = HwWifiServiceFactory.getHwLogCollectManager(context);
            mContext = context;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Can't find sReporterClazz");
            sReporterClazz = null;
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "Can't find sReportMethod");
            sReportMethod = null;
        }
    }

    public boolean report(int eventID, String eventMsg) {
        if (isBetaUser()) {
            try {
                if (!(sReportMethod == null || sReporterClazz == null)) {
                    return ((Boolean) sReportMethod.invoke(sReporterClazz, mContext, Integer.valueOf(eventID), eventMsg)).booleanValue();
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception happened in report()");
            }
        } else {
            Log.e(TAG, "This is not beta user build");
        }
        return false;
    }

    private boolean isBetaUser() {
        if (HwWifiServiceFactory.getHwConstantUtils() == null || HwWifiServiceFactory.getHwConstantUtils().getConfigurationBetaUserVal() != getUserType()) {
            return false;
        }
        return true;
    }

    private int getUserType() {
        try {
            if (mHwLogCollectManagerEx != null) {
                return mHwLogCollectManagerEx.getUserType();
            }
            return -1;
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        } catch (NullPointerException e2) {
            return -1;
        }
    }
}
