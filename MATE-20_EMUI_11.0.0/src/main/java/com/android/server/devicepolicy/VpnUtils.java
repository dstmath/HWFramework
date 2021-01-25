package com.android.server.devicepolicy;

import android.os.Bundle;
import android.text.TextUtils;
import com.android.internal.net.VpnProfile;
import java.util.ArrayList;
import java.util.List;

class VpnUtils {
    private static final String KEY = "key";
    private static final String TAG = "VpnUtils";

    VpnUtils() {
    }

    public static VpnProfile getProfile(Bundle vpnBundle) {
        if (vpnBundle == null) {
            return null;
        }
        VpnProfile profile = new VpnProfile(vpnBundle.getString(KEY));
        profile.name = vpnBundle.getString("name");
        try {
            profile.type = Integer.parseInt(vpnBundle.getString("type"));
        } catch (NumberFormatException e) {
            HwLog.e(TAG, "proxyPort : NumberFormatException");
        }
        profile.server = vpnBundle.getString("server");
        profile.username = vpnBundle.getString("username");
        profile.password = vpnBundle.getString("password");
        switch (profile.type) {
            case 0:
                profile.mppe = Boolean.parseBoolean(vpnBundle.getString("mppe"));
                break;
            case 1:
                profile.l2tpSecret = vpnBundle.getString("l2tpSecret");
                break;
            case 2:
                profile.l2tpSecret = vpnBundle.getString("l2tpSecret");
                profile.ipsecIdentifier = vpnBundle.getString("ipsecIdentifier");
                profile.ipsecSecret = vpnBundle.getString("ipsecSecret");
                break;
            case 3:
                profile.l2tpSecret = vpnBundle.getString("l2tpSecret");
                profile.ipsecUserCert = vpnBundle.getString("ipsecUserCert");
                profile.ipsecCaCert = vpnBundle.getString("ipsecCaCert");
                profile.ipsecServerCert = vpnBundle.getString("ipsecServerCert");
                break;
            case HwDevicePolicyManagerService.SD_CRYPT_STATE_DECRYPTING /* 4 */:
                profile.ipsecIdentifier = vpnBundle.getString("ipsecIdentifier");
                profile.ipsecSecret = vpnBundle.getString("ipsecSecret");
                break;
            case HwDevicePolicyManagerService.SD_CRYPT_STATE_MISMATCH /* 5 */:
                profile.ipsecUserCert = vpnBundle.getString("ipsecUserCert");
                profile.ipsecCaCert = vpnBundle.getString("ipsecCaCert");
                profile.ipsecServerCert = vpnBundle.getString("ipsecServerCert");
                break;
            case HwDevicePolicyManagerService.SD_CRYPT_STATE_WAIT_UNLOCK /* 6 */:
                profile.ipsecCaCert = vpnBundle.getString("ipsecCaCert");
                profile.ipsecServerCert = vpnBundle.getString("ipsecServerCert");
                break;
        }
        return profile;
    }

    public static boolean isValidVpnConfig(Bundle para) {
        if (para == null) {
            return false;
        }
        boolean isKeyAndNameEmpty = TextUtils.isEmpty(para.getString(KEY)) || TextUtils.isEmpty(para.getString("name"));
        boolean isTypeAndServerEmpty = TextUtils.isEmpty(para.getString("type")) || TextUtils.isEmpty(para.getString("server"));
        if (isKeyAndNameEmpty || isTypeAndServerEmpty) {
            return false;
        }
        try {
            int type = Integer.parseInt(para.getString("type"));
            if (type >= 0) {
                if (type <= 6) {
                    if (type != 2) {
                        if (type != 3) {
                            if (type != 4) {
                                if (type != 5) {
                                    return true;
                                }
                            }
                        }
                        return !TextUtils.isEmpty(para.getString("ipsecUserCert"));
                    }
                    return !TextUtils.isEmpty(para.getString("ipsecSecret"));
                }
            }
            return false;
        } catch (NumberFormatException e) {
            HwLog.e(TAG, "proxyPort : NumberFormatException");
            return false;
        }
    }

    public static void filterVpnKeyList(List<Bundle> vpnProviderlist, ArrayList<String> vpnKeyList) {
        if (!(vpnProviderlist == null || vpnKeyList == null)) {
            for (Bundle provider : vpnProviderlist) {
                if (provider != null && !TextUtils.isEmpty(provider.getString(KEY)) && !vpnKeyList.contains(provider.getString(KEY))) {
                    vpnKeyList.add(provider.getString(KEY));
                }
            }
        }
    }
}
