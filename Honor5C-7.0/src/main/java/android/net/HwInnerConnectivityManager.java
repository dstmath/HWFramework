package android.net;

public interface HwInnerConnectivityManager {
    boolean checkHwFeature(String str, NetworkCapabilities networkCapabilities, int i);

    String[] getFeature(String str);

    boolean isHwFeature(String str);
}
