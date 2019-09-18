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
    protected static final boolean NEED_TO_HANDLE_EMERGENCY_APN = SystemProperties.getBoolean("ro.config.emergency_apn_handle", false);
    private HwTelephonyManagerInner mHwTelephonyManager = HwTelephonyManagerInner.getDefault();

    public boolean enforceStartUsingNetworkFeaturePermissionFail(Context context, int usedNetworkType) {
        if (!IS_DOCOMO) {
            return false;
        }
        int[] protectedNetworks = context.getResources().getIntArray(17236028);
        List mProtectedNetworks = new ArrayList();
        for (int p : protectedNetworks) {
            if (!mProtectedNetworks.contains(Integer.valueOf(p))) {
                mProtectedNetworks.add(Integer.valueOf(p));
            } else {
                log("Ignoring protectedNetwork " + p);
            }
        }
        log("[enter] cs.enforceStartUsingNetworkFeaturePermission usedNetworkType =" + usedNetworkType);
        if (mProtectedNetworks.contains(Integer.valueOf(usedNetworkType))) {
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

    private static void log(String s) {
        Slog.d("HwCustConnectivityManagerImpl", s);
    }

    public boolean canHandleEimsNetworkCapabilities(NetworkCapabilities nc) {
        boolean z = false;
        if (nc == null) {
            return false;
        }
        if (NEED_TO_HANDLE_EMERGENCY_APN && nc.hasCapability(10)) {
            z = true;
        }
        return z;
    }

    public NetworkCapabilities networkCapabilitiesForEimsType(int type) {
        if (NEED_TO_HANDLE_EMERGENCY_APN) {
            return ConnectivityManager.networkCapabilitiesForType(type);
        }
        return null;
    }

    private int getSubIdFromNetworkCapbilities(NetworkCapabilities need) {
        int subId;
        if (need == null || need.getTransportTypes().length != 1 || !need.hasTransport(0)) {
            return -1;
        }
        StringNetworkSpecifier networkSpecifier = need.getNetworkSpecifier();
        if (!(networkSpecifier instanceof StringNetworkSpecifier)) {
            return -1;
        }
        try {
            subId = Integer.parseInt(networkSpecifier.specifier);
        } catch (NumberFormatException e) {
            subId = -1;
        }
        return subId;
    }

    /* access modifiers changed from: protected */
    public boolean isSubInactiveFromNetworkCapabilities(NetworkCapabilities need) {
        int slotId = SubscriptionManager.getSlotIndex(getSubIdFromNetworkCapbilities(need));
        boolean z = false;
        if (!SubscriptionManager.isValidSlotIndex(slotId)) {
            return false;
        }
        int subState = this.mHwTelephonyManager.getSubState((long) slotId);
        log("isSubInactiveFromNetworkCapabilities: subState= " + subState);
        if (subState == 0) {
            z = true;
        }
        return z;
    }

    public boolean isBlockNetworkRequest(NetworkCapabilities need) {
        return isBlockNetworkRequestByNonAis(need);
    }

    private boolean isBlockNetworkRequestByNonAis(NetworkCapabilities need) {
        int subId = -1;
        if (need != null) {
            NetworkSpecifier specifier = need.getNetworkSpecifier();
            if (specifier != null && (specifier instanceof StringNetworkSpecifier)) {
                try {
                    subId = Integer.parseInt(((StringNetworkSpecifier) specifier).specifier);
                } catch (NumberFormatException e) {
                    subId = -1;
                }
            }
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            log("isBlockNetworkRequestByNonAis, INVALID_SUBSCRIPTION_ID");
            return false;
        } else if (!HwTelephonyManagerInner.getDefault().isCustomAis() || HwTelephonyManagerInner.getDefault().isAISCard(subId) || HwTelephonyManagerInner.getDefault().isAisCustomDisable()) {
            return false;
        } else {
            log("isBlockNetworkRequestByNonAis, ais custom version but not ais card. block network");
            return true;
        }
    }
}
