package huawei.android.net.hwmplink;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.FreezeScreenScene;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.indexsearch.IndexObserverHandler;
import huawei.com.android.internal.widget.HwLockPatternUtils;
import java.util.List;

public class MpLinkCommonUtils {
    private static final int[] CERTIFIED_APPS = {1001, IndexObserverHandler.MSG_UNBOUND, HwLockPatternUtils.transaction_setActiveVisitorPasswordState, 1004, 1005, 1006, 1007};
    private static final int[] CERTIFIED_SCENCES = {100001, 100101, 100102, 100103, 100104, 100105, 100106, 100201, 100202, 100301, 100302, 100401, 100501, 100701};
    private static final int DEFAULT_VALUE = -1;
    public static final String KEY_MP_LINK_LOG = "ro.config.mplink_log";
    public static final String KEY_MP_LINK_PROPERTY = "ro.config.mplink_enable";
    public static final String KEY_MP_LINK_TEST = "ro.config.mplink_test";
    public static final String KEY_PROP_LOCALE = "ro.product.locale.region";
    private static final String KEY_TOTAL = "total";
    private static final String KEY_UNKNOWN = "unknown:";
    public static final String KEY_WIFI_PRO_PROPERTY = "ro.config.hw_wifipro_enable";
    public static final String KEY_WIFI_PRO_SWITCH = "smart_network_switching";
    public static final String SETTING_MPLINK_DB_CONDITION_VALUE = "mplink_db_condition_value";
    public static final String SETTING_MPLINK_MOBILE_SWITCH = "mobile_data";
    public static final String SETTING_MPLINK_SIMULATE_HIBRAIN_REQUEST_FOR_TEST = "mplink_simulate_hibrain_request_for_test";
    public static final String SETTING_SECURE_VPN_WORK_VALUE = "wifipro_network_vpn_state";
    public static final String TAG = "HiData_HiDATA_MpLinkCommonUtils";

    private MpLinkCommonUtils() {
    }

    public static boolean isMpLinkEnabled(Context context) {
        if (context == null || !getSettingsSystemBoolean(context.getContentResolver(), "smart_network_switching", false) || !isSupportMpLink()) {
            return false;
        }
        return true;
    }

    public static boolean isMpLinkEnabledInternal(Context context) {
        if (context == null || !getSettingsSystemBoolean(context.getContentResolver(), "mplink_db_condition_value", false)) {
            return false;
        }
        return true;
    }

    public static boolean isSupportMpLink() {
        return SystemProperties.getBoolean("ro.config.mplink_enable", true);
    }

    public static boolean isMpLinkTestMode() {
        return SystemProperties.getBoolean("ro.config.mplink_test", false);
    }

    public static String getProductLocale() {
        return SystemProperties.get("ro.product.locale.region", StorageManagerExt.INVALID_KEY_DESC);
    }

    public static boolean getSettingsSystemBoolean(ContentResolver cr, String name, boolean isDefined) {
        return Settings.System.getInt(cr, name, isDefined ? 1 : 0) == 1;
    }

    public static boolean getSettingsGlobalBoolean(ContentResolver cr, String name, boolean isDefined) {
        return Settings.Global.getInt(cr, name, isDefined ? 1 : 0) == 1;
    }

    public static void logD(String tag, boolean isPrivateFmtStr, String fmt, Object... args) {
        HwHiDataCommonUtils.logD(tag, isPrivateFmtStr, fmt, args);
    }

    public static void logI(String tag, boolean isPrivateFmtStr, String fmt, Object... args) {
        HwHiDataCommonUtils.logI(tag, isPrivateFmtStr, fmt, args);
    }

    public static boolean isAppCertified(int app) {
        return isMpLinkCertified(CERTIFIED_APPS, app);
    }

    public static boolean isScenceCertified(int scence) {
        return isMpLinkCertified(CERTIFIED_SCENCES, scence);
    }

    private static boolean isMpLinkCertified(int[] conditions, int request) {
        if (conditions == null || conditions.length == 0) {
            return false;
        }
        for (int i = conditions.length - 1; i >= 0; i--) {
            if (request == conditions[i]) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x001d  */
    public static int getForegroundAppUid(Context context) {
        List<ActivityManager.RunningAppProcessInfo> lr;
        if (context == null || (lr = ((ActivityManager) context.getSystemService(FreezeScreenScene.ACTIVITY_PARAM)).getRunningAppProcesses()) == null) {
            return -1;
        }
        for (ActivityManager.RunningAppProcessInfo ra : lr) {
            if (ra.importance == 200 || ra.importance == 100) {
                return ra.uid;
            }
            while (r3.hasNext()) {
            }
        }
        return -1;
    }

    public static String getPackageName(Context context, int uid) {
        PackageManager pm;
        if (uid == -1 || context == null || (pm = context.getPackageManager()) == null) {
            return KEY_TOTAL;
        }
        String name = pm.getNameForUid(uid);
        if (!TextUtils.isEmpty(name)) {
            return name;
        }
        return KEY_UNKNOWN + uid;
    }

    public static int getAppUid(Context context, String processName) {
        PackageManager pm;
        if (TextUtils.isEmpty(processName) || context == null || (pm = context.getPackageManager()) == null) {
            return -1;
        }
        try {
            ApplicationInfo ai = pm.getApplicationInfo(processName, 1);
            if (ai != null) {
                return ai.uid;
            }
            return -1;
        } catch (PackageManager.NameNotFoundException e) {
            logD(TAG, false, "NameNotFoundException: %{public}s", e.getMessage());
            return -1;
        }
    }

    public static int getNetworkType(Context context, int netId) {
        ConnectivityManager cm;
        Network[] networks;
        Network network;
        if (context == null || netId <= 0 || (networks = (cm = (ConnectivityManager) context.getSystemService("connectivity")).getAllNetworks()) == null || networks.length == 0) {
            return -1;
        }
        int length = networks.length;
        for (int i = 0; i < length; i++) {
            NetworkInfo netInfo = cm.getNetworkInfo(networks[i]);
            if (!(netInfo == null || (network = networks[i]) == null || network.netId != netId)) {
                return netInfo.getType();
            }
        }
        return -1;
    }
}
