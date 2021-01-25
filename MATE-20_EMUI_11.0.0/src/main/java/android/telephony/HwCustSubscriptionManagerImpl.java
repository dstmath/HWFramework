package android.telephony;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Binder;
import android.provider.Settings;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public class HwCustSubscriptionManagerImpl extends HwCustSubscriptionManager {
    private static final int ICCID_BEGIN = 0;
    private static final int ICCID_END = 19;
    private static final String LOG_TAG = "HwCustSubscriptionManagerImpl";
    private List<String> mWhiteAppsList = null;

    private void initWhiteAppsList(String whiteApps) {
        String[] whiteAppsArray = whiteApps.split(";");
        this.mWhiteAppsList.clear();
        int length = whiteAppsArray.length;
        for (int i = ICCID_BEGIN; i < length; i++) {
            this.mWhiteAppsList.add(whiteAppsArray[i]);
        }
    }

    public SubscriptionInfo getCustSubscriptionInfo(SubscriptionInfo subInfo, Context context) {
        if (context == null || subInfo == null || !isWhiteApp(getAppName(Binder.getCallingPid(), context))) {
            return subInfo;
        }
        subInfo.setIccId((subInfo.getIccId() == null || subInfo.getIccId().length() <= ICCID_END) ? subInfo.getIccId() : subInfo.getIccId().substring(ICCID_BEGIN, ICCID_END));
        return subInfo;
    }

    private String getAppName(int pid, Context context) {
        List<ActivityManager.RunningAppProcessInfo> appProcessLists;
        ActivityManager am = (ActivityManager) context.getSystemService("activity");
        if (am == null || (appProcessLists = am.getRunningAppProcesses()) == null || appProcessLists.size() == 0) {
            return "";
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessLists) {
            if (appProcess != null && appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }

    private boolean isWhiteApp(String pkg) {
        List<String> list;
        if (!TextUtils.isEmpty(pkg) && (list = this.mWhiteAppsList) != null) {
            return list.contains(pkg);
        }
        return false;
    }

    public boolean isSupport19BitIccid(Context context) {
        if (context == null) {
            return false;
        }
        if (this.mWhiteAppsList == null) {
            this.mWhiteAppsList = new ArrayList();
            String whiteApps = Settings.System.getString(context.getContentResolver(), "sup_19bit_iccid_white_apps");
            if (TextUtils.isEmpty(whiteApps)) {
                return false;
            }
            initWhiteAppsList(whiteApps);
        }
        if (this.mWhiteAppsList.size() > 0) {
            return true;
        }
        return false;
    }
}
