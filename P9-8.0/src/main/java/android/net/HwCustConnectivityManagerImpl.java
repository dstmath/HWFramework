package android.net;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import java.util.ArrayList;
import java.util.List;

public class HwCustConnectivityManagerImpl extends HwCustConnectivityManager {
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    protected static final boolean NEED_TO_HANDLE_EMERGENCY_APN = SystemProperties.getBoolean("ro.config.emergency_apn_handle", false);

    public boolean enforceStartUsingNetworkFeaturePermissionFail(Context context, int usedNetworkType) {
        if (!IS_DOCOMO) {
            return false;
        }
        int[] protectedNetworks = context.getResources().getIntArray(17236025);
        List protectedNetworkList = new ArrayList();
        for (int p : protectedNetworks) {
            if (protectedNetworkList.contains(Integer.valueOf(p))) {
                log("Ignoring protectedNetwork " + p);
            } else {
                protectedNetworkList.add(Integer.valueOf(p));
            }
        }
        log("[enter] cs.enforceStartUsingNetworkFeaturePermission usedNetworkType =" + usedNetworkType);
        if (protectedNetworkList.contains(Integer.valueOf(usedNetworkType))) {
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
        if (NEED_TO_HANDLE_EMERGENCY_APN) {
            z = nc.hasCapability(10);
        }
        return z;
    }

    public NetworkCapabilities networkCapabilitiesForEimsType(int type) {
        if (NEED_TO_HANDLE_EMERGENCY_APN) {
            return ConnectivityManager.networkCapabilitiesForType(type);
        }
        return null;
    }
}
