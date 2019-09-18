package android.net;

import android.content.Context;
import android.os.SystemProperties;

public class HwCustConnectivityManager {
    private static final boolean DISABLE_SIM2_DATA = SystemProperties.getBoolean("ro.config.hw_disable_sim2_data", false);
    protected static final int INVALID_SUBSCRIPTION_ID = -1;
    private static final int SIM_2 = 1;

    public boolean enforceStartUsingNetworkFeaturePermissionFail(Context context, int usedNetworkType) {
        return false;
    }

    public boolean canHandleEimsNetworkCapabilities(NetworkCapabilities nc) {
        return false;
    }

    public NetworkCapabilities networkCapabilitiesForEimsType(int type) {
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean isSubInactiveFromNetworkCapabilities(NetworkCapabilities need) {
        return false;
    }

    public boolean isDisableRequestBySIM2(NetworkCapabilities need) {
        int subId = -1;
        if (!DISABLE_SIM2_DATA || need == null || !need.hasTransport(0) || need.getTransportTypes().length != 1) {
            return false;
        }
        NetworkSpecifier specifier = need.getNetworkSpecifier();
        if (specifier != null && (specifier instanceof StringNetworkSpecifier)) {
            try {
                subId = Integer.parseInt(((StringNetworkSpecifier) specifier).specifier);
            } catch (NumberFormatException e) {
                subId = -1;
            }
        }
        if (subId == 1) {
            return true;
        }
        return false;
    }

    public boolean isBlockNetworkRequest(NetworkCapabilities need) {
        return false;
    }
}
