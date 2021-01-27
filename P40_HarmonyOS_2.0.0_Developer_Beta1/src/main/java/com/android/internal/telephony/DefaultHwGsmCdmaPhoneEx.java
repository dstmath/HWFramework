package com.android.internal.telephony;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.huawei.internal.telephony.PhoneExt;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultHwGsmCdmaPhoneEx implements IHwGsmCdmaPhoneEx {
    private static final int APPNAME_INDEX = 0;
    private static final int CALLINGPACKAGENAME_INDEX = 2;
    private static final boolean HW_INFO;
    private static final String LOG_TAG = "DefaultHwGsmCdmaPhoneEx";
    private static final int MAX_MAP_SIZE = 10;
    private static final int NAME_ARRAY_SIZE = 3;
    private static final int PROCESSNAME_INDEX = 1;
    private Context context;
    private Map<Integer, String[]> mMap = new HashMap();

    static {
        boolean z = false;
        if (SystemProperties.getBoolean("ro.debuggable", false) || SystemProperties.getBoolean("persist.sys.huawei.debug.on", false)) {
            z = true;
        }
        HW_INFO = z;
    }

    public DefaultHwGsmCdmaPhoneEx(Context context2) {
        this.context = context2;
    }

    public DefaultHwGsmCdmaPhoneEx(IHwGsmCdmaPhoneInner hwGsmCdmaPhoneInner, PhoneExt phoneExt) {
        this.context = phoneExt.getContext();
    }

    private Context getContext() {
        return this.context;
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneEx
    public void logForTest(String operationName, String content) {
        String processName;
        String callingPackageName;
        if (HW_INFO) {
            int pid = Binder.getCallingPid();
            String appName = PhoneConfigurationManager.SSSS;
            synchronized (this) {
                String[] name = this.mMap.get(Integer.valueOf(pid));
                if (name != null) {
                    appName = name[0];
                    processName = name[1];
                    callingPackageName = name[2];
                } else {
                    loge("pid is not exist in map");
                    if (this.mMap.size() == 10) {
                        this.mMap.clear();
                    }
                    String processName2 = getProcessName(pid);
                    String callingPackageName2 = getPackageNameForPid(pid);
                    try {
                        appName = getContext().getPackageManager().getPackageInfo(callingPackageName2, 0).applicationInfo.loadLabel(getContext().getPackageManager()).toString();
                    } catch (PackageManager.NameNotFoundException e) {
                        loge("get appname wrong");
                    }
                    this.mMap.put(Integer.valueOf(pid), new String[]{appName, processName2, callingPackageName2});
                    processName = processName2;
                    callingPackageName = callingPackageName2;
                }
            }
            Rlog.i("ctaifs <" + appName + ">[" + callingPackageName + "][" + processName + "]", "[" + operationName + "] " + content);
        }
    }

    private String getPackageNameForPid(int pid) {
        String res = null;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.app.IActivityManager");
            data.writeInt(pid);
            ActivityManagerNative.getDefault().asBinder().transact(504, data, reply, 0);
            reply.readException();
            res = reply.readString();
            data.recycle();
            reply.recycle();
            return res;
        } catch (RemoteException e) {
            logi("RemoteException");
            return res;
        } catch (Exception e2) {
            logi("getPackageNameForPid exception");
            return res;
        }
    }

    private String getProcessName(int pid) {
        String processName = PhoneConfigurationManager.SSSS;
        Object systemService = getContext().getSystemService("activity");
        if (!(systemService instanceof ActivityManager)) {
            return PhoneConfigurationManager.SSSS;
        }
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = ((ActivityManager) systemService).getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return processName;
        }
        for (ActivityManager.RunningAppProcessInfo info : runningAppProcesses) {
            if (info != null && info.pid == pid) {
                processName = info.processName;
            }
        }
        return processName;
    }

    private void logi(String s) {
        Rlog.i(LOG_TAG, s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }
}
