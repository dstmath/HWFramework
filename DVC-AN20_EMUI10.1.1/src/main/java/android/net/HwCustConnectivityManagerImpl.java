package android.net;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.SubscriptionManager;
import android.util.Slog;
import java.util.ArrayList;
import java.util.List;

public class HwCustConnectivityManagerImpl extends HwCustConnectivityManager {
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    protected static final boolean NEED_TO_HANDLE_EMERGENCY_APN = SystemProperties.getBoolean("ro.config.emergency_apn_handle", true);
    private HwTelephonyManagerInner mHwTelephonyManager = HwTelephonyManagerInner.getDefault();

    public boolean enforceStartUsingNetworkFeaturePermissionFail(Context context, int usedNetworkType) {
        if (!IS_DOCOMO) {
            return false;
        }
        int[] protectedNetworks = context.getResources().getIntArray(17236049);
        List<Integer> protectedNetworksList = new ArrayList<>();
        for (int p : protectedNetworks) {
            if (!protectedNetworksList.contains(Integer.valueOf(p))) {
                protectedNetworksList.add(Integer.valueOf(p));
            } else {
                log("Ignoring protectedNetwork " + p);
            }
        }
        log("[enter] cs.enforceStartUsingNetworkFeaturePermission usedNetworkType =" + usedNetworkType);
        if (protectedNetworksList.contains(Integer.valueOf(usedNetworkType))) {
            try {
                log("[enter] enforce permission");
                ConnectivityManager.enforceChangePermission(context);
                context.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", "ConnectivityService");
            } catch (SecurityException e) {
                log("Rejected using network " + usedNetworkType);
                return true;
            }
        }
        return false;
    }

    private static void log(String logStr) {
        Slog.d("HwCustConnectivityManagerImpl", logStr);
    }

    public boolean canHandleEimsNetworkCapabilities(NetworkCapabilities networkCapa) {
        if (networkCapa != null && NEED_TO_HANDLE_EMERGENCY_APN && networkCapa.hasCapability(10)) {
            return true;
        }
        return false;
    }

    public NetworkCapabilities networkCapabilitiesForEimsType(int type) {
        if (NEED_TO_HANDLE_EMERGENCY_APN) {
            return ConnectivityManager.networkCapabilitiesForType(type);
        }
        return null;
    }

    private int getSubIdFromNetworkCapbilities(NetworkCapabilities need) {
        if (need == null || need.getTransportTypes().length != 1 || !need.hasTransport(0)) {
            return -1;
        }
        NetworkSpecifier specifier = need.getNetworkSpecifier();
        if (!(specifier instanceof StringNetworkSpecifier)) {
            return -1;
        }
        try {
            return Integer.parseInt(((StringNetworkSpecifier) specifier).specifier);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isSubInactiveFromNetworkCapabilities(NetworkCapabilities need) {
        int slotId = SubscriptionManager.getSlotIndex(getSubIdFromNetworkCapbilities(need));
        if (!SubscriptionManager.isValidSlotIndex(slotId)) {
            return false;
        }
        int subState = this.mHwTelephonyManager.getSubState((long) slotId);
        log("isSubInactiveFromNetworkCapabilities: subState= " + subState);
        if (subState == 0) {
            return true;
        }
        return false;
    }

    public boolean isBlockNetworkRequest(NetworkCapabilities need) {
        return isBlockNetworkRequestByNonAis(need);
    }

    private boolean isBlockNetworkRequestByNonAis(NetworkCapabilities need) {
        NetworkSpecifier specifier;
        int subId = -1;
        if (!(need == null || (specifier = need.getNetworkSpecifier()) == null || !(specifier instanceof StringNetworkSpecifier))) {
            try {
                subId = Integer.parseInt(((StringNetworkSpecifier) specifier).specifier);
            } catch (NumberFormatException e) {
                subId = -1;
            }
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            log("isBlockNetworkRequestByNonAis, INVALID_SUBSCRIPTION_ID");
            return false;
        }
        int slotId = SubscriptionManager.getSlotIndex(subId);
        if (!SubscriptionManager.isValidSlotIndex(slotId)) {
            log("isBlockNetworkRequestByNonAis, invlid slot index");
            return false;
        } else if (!HwTelephonyManagerInner.getDefault().isBlockNonAisSlot(slotId)) {
            return false;
        } else {
            log("isBlockNetworkRequestByNonAis, ais custom version but not ais card. block network");
            return true;
        }
    }
}
