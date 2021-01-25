package android.net.shared;

import android.net.NetworkCapabilities;

public class NetworkMonitorUtils {
    public static final String ACTION_NETWORK_CONDITIONS_MEASURED = "android.net.conn.NETWORK_CONDITIONS_MEASURED";
    public static final String EXTRA_BSSID = "extra_bssid";
    public static final String EXTRA_CELL_ID = "extra_cellid";
    public static final String EXTRA_CONNECTIVITY_TYPE = "extra_connectivity_type";
    public static final String EXTRA_IS_CAPTIVE_PORTAL = "extra_is_captive_portal";
    public static final String EXTRA_NETWORK_TYPE = "extra_network_type";
    public static final String EXTRA_REQUEST_TIMESTAMP_MS = "extra_request_timestamp_ms";
    public static final String EXTRA_RESPONSE_RECEIVED = "extra_response_received";
    public static final String EXTRA_RESPONSE_TIMESTAMP_MS = "extra_response_timestamp_ms";
    public static final String EXTRA_SSID = "extra_ssid";
    public static final String PERMISSION_ACCESS_NETWORK_CONDITIONS = "android.permission.ACCESS_NETWORK_CONDITIONS";

    public static boolean isPrivateDnsValidationRequired(NetworkCapabilities nc) {
        return nc != null && nc.hasCapability(12) && nc.hasCapability(13) && nc.hasCapability(14);
    }

    public static boolean isValidationRequired(NetworkCapabilities nc) {
        return isPrivateDnsValidationRequired(nc) && nc.hasCapability(15);
    }
}
