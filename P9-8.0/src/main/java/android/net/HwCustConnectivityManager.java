package android.net;

import android.content.Context;

public class HwCustConnectivityManager {
    public boolean enforceStartUsingNetworkFeaturePermissionFail(Context context, int usedNetworkType) {
        return false;
    }

    public boolean canHandleEimsNetworkCapabilities(NetworkCapabilities nc) {
        return false;
    }

    public NetworkCapabilities networkCapabilitiesForEimsType(int type) {
        return null;
    }
}
