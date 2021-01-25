package com.android.server.intellicom.common;

import android.app.ActivityManager;
import android.common.HwPartBaseTelephonyFactory;
import android.content.Context;
import android.net.NetworkCapabilities;
import android.util.Log;
import com.android.internal.telephony.HwPartTelephonyFactory;
import com.huawei.android.net.NetworkCapabilitiesEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.List;

public class IntellicomUtils {
    private static final int INVALID_UID = -1;
    private static final String TAG = "IntellicomUtils";

    private IntellicomUtils() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x001d  */
    public static int getForegroundAppUid(Context context) {
        List<ActivityManager.RunningAppProcessInfo> lr;
        if (context == null || (lr = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses()) == null) {
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

    public static boolean isMultiSimEnabled() {
        return TelephonyManagerEx.isMultiSimEnabled(TelephonyManagerEx.getDefault());
    }

    public static String getOperator(int slotId) {
        String operator = HwPartBaseTelephonyFactory.loadFactory().createHwInnerTelephonyManager().getCTOperator(slotId, TelephonyManagerEx.getSimOperatorNumericForPhone(slotId));
        if (isMultiSimEnabled()) {
            if (HwPartTelephonyFactory.loadFactory("android.telephony.HwPartTelephonyFactoryImpl").createHwPhoneManagerImpl().isRoamingBrokerActivated(Integer.valueOf(slotId))) {
                return HwPartTelephonyFactory.loadFactory("android.telephony.HwPartTelephonyFactoryImpl").createHwPhoneManagerImpl().getRoamingBrokerOperatorNumeric(Integer.valueOf(slotId));
            }
            return operator;
        } else if (HwPartTelephonyFactory.loadFactory("android.telephony.HwPartTelephonyFactoryImpl").createHwPhoneManagerImpl().isRoamingBrokerActivated()) {
            return HwPartTelephonyFactory.loadFactory("android.telephony.HwPartTelephonyFactoryImpl").createHwPhoneManagerImpl().getRoamingBrokerOperatorNumeric();
        } else {
            return operator;
        }
    }

    public static int getSubId(int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds != null && subIds.length > 0) {
            return subIds[0];
        }
        logw("can not get subId for slotId =" + slotId);
        return -1;
    }

    public static int getApnTypeFromNetworkCapabilities(NetworkCapabilities nc) {
        if (!isNetworkCapabilitiesAvailable(nc)) {
            return 0;
        }
        int apnType = 0;
        if (nc.hasCapability(12)) {
            apnType = 17;
        }
        if (nc.hasCapability(0)) {
            apnType = 2;
        }
        if (nc.hasCapability(1)) {
            apnType = 4;
        }
        if (nc.hasCapability(2)) {
            apnType = 8;
        }
        if (nc.hasCapability(3)) {
            apnType = 32;
        }
        if (nc.hasCapability(4)) {
            apnType = 64;
        }
        if (nc.hasCapability(5)) {
            apnType = 128;
        }
        if (nc.hasCapability(7)) {
            apnType = 256;
        }
        if (nc.hasCapability(8)) {
            Log.e(TAG, "RCS APN type not yet supported");
        }
        if (nc.hasCapability(9)) {
            apnType = HwGlobalActionsData.FLAG_KEYCOMBINATION_INIT_STATE;
        }
        if (nc.hasCapability(10)) {
            apnType = 512;
        }
        if (nc.hasCapability(23)) {
            apnType = 1024;
        }
        int apnTypeOther = getApnTypeFromNetworkCapabilitiesOther(nc);
        if (apnTypeOther != 0) {
            return apnTypeOther;
        }
        return apnType;
    }

    private static boolean isNetworkCapabilitiesAvailable(NetworkCapabilities nc) {
        if (nc == null) {
            return false;
        }
        if (NetworkCapabilitiesEx.getTransportTypes(nc).length <= 0 || nc.hasTransport(0)) {
            return true;
        }
        logw("nc.getTransportTypes().length = " + NetworkCapabilitiesEx.getTransportTypes(nc).length + ",nc = " + nc);
        return false;
    }

    private static int getApnTypeFromNetworkCapabilitiesOther(NetworkCapabilities nc) {
        if (nc == null) {
            return 0;
        }
        int apnType = 0;
        if (nc.hasCapability(25)) {
            apnType = 32768;
        }
        if (nc.hasCapability(26)) {
            apnType = 65536;
        }
        if (nc.hasCapability(27)) {
            apnType = 131072;
        }
        if (nc.hasCapability(28)) {
            apnType = 262144;
        }
        if (nc.hasCapability(29)) {
            apnType = 524288;
        }
        if (nc.hasCapability(30)) {
            apnType = 1048576;
        }
        if (nc.hasCapability(31)) {
            apnType = 2097152;
        }
        if (nc.hasCapability(32)) {
            apnType = 8388608;
        }
        if (NetworkCapabilitiesEx.hasSnssaiCapability(nc)) {
            apnType = 33554432;
        }
        if (apnType == 0) {
            Log.e(TAG, "Unsupported NetworkRequest in Telephony: nc = " + nc);
        }
        return apnType;
    }

    private static void logw(String msg) {
        Log.w(TAG, msg);
    }
}
