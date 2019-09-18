package android.net;

import android.util.SparseIntArray;
import java.util.Map;

public interface HwInnerConnectivityManager {
    boolean checkHwFeature(String str, NetworkCapabilities networkCapabilities, int i);

    String[] getFeature(String str);

    String getNetworkTypeNameEx(int i);

    Map<String, Integer> inferLegacyTypeForNetworkCapabilitiesEx(NetworkCapabilities networkCapabilities, HwCustConnectivityManager hwCustConnectivityManager, SparseIntArray sparseIntArray, SparseIntArray sparseIntArray2);

    boolean isHwFeature(String str);

    boolean isNetworkTypeMobileEx(int i);

    boolean isNetworkTypeWifiEx(int i);

    int legacyTypeForNetworkCapabilitiesEx(NetworkCapabilities networkCapabilities, HwCustConnectivityManager hwCustConnectivityManager);

    NetworkCapabilities networkCapabilitiesForFeatureEx(HwCustConnectivityManager hwCustConnectivityManager, String str);

    void setLegacyTypeToCapabilityEx(SparseIntArray sparseIntArray);

    void setLegacyTypeToTransportEx(SparseIntArray sparseIntArray);
}
