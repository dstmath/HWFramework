package android.net.util;

import android.content.Context;
import android.content.res.Resources;
import android.net.NetworkCapabilities;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import com.android.internal.R;

public final class KeepaliveUtils {
    public static final String TAG = "KeepaliveUtils";

    public static class KeepaliveDeviceConfigurationException extends AndroidRuntimeException {
        public KeepaliveDeviceConfigurationException(String msg) {
            super(msg);
        }
    }

    public static int[] getSupportedKeepalives(Context context) {
        String[] res = null;
        try {
            res = context.getResources().getStringArray(R.array.config_networkSupportedKeepaliveCount);
        } catch (Resources.NotFoundException e) {
        }
        if (res != null) {
            int[] ret = new int[8];
            for (String row : res) {
                if (!TextUtils.isEmpty(row)) {
                    String[] arr = row.split(SmsManager.REGEX_PREFIX_DELIMITER);
                    if (arr.length == 2) {
                        try {
                            int transport = Integer.parseInt(arr[0]);
                            int supported = Integer.parseInt(arr[1]);
                            if (!NetworkCapabilities.isValidTransport(transport)) {
                                throw new KeepaliveDeviceConfigurationException("Invalid transport " + transport);
                            } else if (supported >= 0) {
                                ret[transport] = supported;
                            } else {
                                throw new KeepaliveDeviceConfigurationException("Invalid supported count " + supported + " for " + NetworkCapabilities.transportNameOf(transport));
                            }
                        } catch (NumberFormatException e2) {
                            throw new KeepaliveDeviceConfigurationException("Invalid number format");
                        }
                    } else {
                        throw new KeepaliveDeviceConfigurationException("Invalid parameter length");
                    }
                } else {
                    throw new KeepaliveDeviceConfigurationException("Empty string");
                }
            }
            return ret;
        }
        throw new KeepaliveDeviceConfigurationException("invalid resource");
    }

    public static int getSupportedKeepalivesForNetworkCapabilities(int[] supportedKeepalives, NetworkCapabilities nc) {
        int[] transports = nc.getTransportTypes();
        if (transports.length == 0) {
            return 0;
        }
        int supportedCount = supportedKeepalives[transports[0]];
        for (int transport : transports) {
            if (supportedCount > supportedKeepalives[transport]) {
                supportedCount = supportedKeepalives[transport];
            }
        }
        return supportedCount;
    }
}
