package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.os.Environment;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import com.huawei.utils.reflect.EasyInvokeFactory;

public class HwWifiConfigStore extends WifiConfigStore {
    private static final String DEFAULT_CERTIFICATE_PATH;
    private static final int HISI_WAPI = 0;
    private static final int INVALID_WAPI = -1;
    private static final int QUALCOMM_WAPI = 1;
    private static final String SUPPLICANT_CONFIG_FILE = "/data/misc/wifi/wpa_supplicant.conf";
    public static final String TAG = "HwWifiConfigStore";
    private static HwWifiConfigStoreUtils wifiConfigStoreUtils;
    private int mWapiType;

    static {
        wifiConfigStoreUtils = (HwWifiConfigStoreUtils) EasyInvokeFactory.getInvokeUtils(HwWifiConfigStoreUtils.class);
        DEFAULT_CERTIFICATE_PATH = Environment.getDataDirectory().getPath() + "/wapi_certificate";
    }

    HwWifiConfigStore(WifiNative wifiNative, KeyStore keyStore, LocalLog localLog, boolean showNetworks, boolean verboseDebug) {
        super(wifiNative, keyStore, localLog, showNetworks, verboseDebug);
    }

    public void setWifiConfigurationWapi(WifiConfiguration config, int netId) {
        String value = wifiConfigStoreUtils.getWifiNative(this).getNetworkVariable(netId, "as_cert_file");
        if (TextUtils.isEmpty(value)) {
            config.wapiAsCertQualcomm = null;
        } else {
            config.wapiAsCertQualcomm = value;
        }
        value = wifiConfigStoreUtils.getWifiNative(this).getNetworkVariable(netId, "user_cert_file");
        if (TextUtils.isEmpty(value)) {
            config.wapiUserCertQualcomm = null;
        } else {
            config.wapiUserCertQualcomm = value;
        }
        value = wifiConfigStoreUtils.getWifiNative(this).getNetworkVariable(netId, "wapi_key_type");
        if (!TextUtils.isEmpty(value)) {
            try {
                config.wapiPskTypeQualcomm = Integer.parseInt(value);
            } catch (NumberFormatException e) {
            }
        }
        value = wifiConfigStoreUtils.getWifiNative(this).getNetworkVariable(netId, "wapi_psk");
        if (TextUtils.isEmpty(value)) {
            config.wapiPskQualcomm = null;
        } else {
            config.wapiPskQualcomm = value;
        }
        if (config.allowedKeyManagement.get(6)) {
            value = wifiConfigStoreUtils.getWifiNative(this).getNetworkVariable(netId, "psk_key_type");
            config.wapiPskTypeBcm = INVALID_WAPI;
            Log.d(TAG, "***WAPI : readNetworkVariables BCM_WAPI_PSK key type " + value);
            if (!TextUtils.isEmpty(value)) {
                try {
                    config.wapiPskTypeBcm = Integer.parseInt(value);
                } catch (NumberFormatException e2) {
                }
            }
        } else if (config.allowedKeyManagement.get(7)) {
            value = wifiConfigStoreUtils.getWifiNative(this).getNetworkVariable(netId, "cert_index");
            config.wapiCertIndexBcm = INVALID_WAPI;
            Log.d(TAG, "***WAPI : readNetworkVariables BCM_WAPI_CERT index " + value);
            if (!TextUtils.isEmpty(value)) {
                try {
                    config.wapiCertIndexBcm = Integer.parseInt(value);
                } catch (NumberFormatException e3) {
                }
            }
            value = wifiConfigStoreUtils.getWifiNative(this).getNetworkVariable(netId, "wapi_as_cert");
            Log.d(TAG, "***WAPI : readNetworkVariables BCM_WAPI_CERT as cert " + value);
            if (TextUtils.isEmpty(value)) {
                config.wapiAsCertBcm = null;
            } else {
                config.wapiAsCertBcm = removeDoubleQuotes(value);
            }
            value = wifiConfigStoreUtils.getWifiNative(this).getNetworkVariable(netId, "wapi_user_cert");
            Log.d(TAG, "***WAPI : readNetworkVariables BCM_WAPI_CERT user cert " + value);
            if (TextUtils.isEmpty(value)) {
                config.wapiUserCertBcm = null;
            } else {
                config.wapiUserCertBcm = removeDoubleQuotes(value);
            }
        }
    }

    public boolean isVariablesWapi(WifiConfiguration config, int netId) {
        if (config.allowedKeyManagement.get(6)) {
            if (!wifiConfigStoreUtils.getWifiNative(this).setNetworkVariable(netId, "psk_key_type", Integer.toString(config.wapiPskTypeBcm))) {
                Log.d(TAG, config.SSID + ": failed to set BCM_WAPI_PSK key type: " + config.wapiPskTypeBcm);
                return true;
            } else if (!(config.preSharedKey == null || config.preSharedKey.equals("*") || wifiConfigStoreUtils.getWifiNative(this).setNetworkVariable(netId, "psk", config.preSharedKey))) {
                Log.d(TAG, "failed to set psk");
                return true;
            }
        } else if (config.allowedKeyManagement.get(7)) {
            if (wifiConfigStoreUtils.getWifiNative(this).setNetworkVariable(netId, "cert_index", Integer.toString(config.wapiCertIndexBcm))) {
                if (!(config.wapiAsCertBcm == null || wifiConfigStoreUtils.getWifiNative(this).setNetworkVariable(netId, "wapi_as_cert", config.wapiAsCertBcm))) {
                    if (config.wapiAsCertBcm.equals("")) {
                        Log.d(TAG, "failed to set BCM_WAPI_CERT as cert: " + config.wapiAsCertBcm);
                        return true;
                    }
                    config.wapiAsCertBcm = "\"" + config.wapiAsCertBcm + "\"";
                    if (!wifiConfigStoreUtils.getWifiNative(this).setNetworkVariable(netId, "wapi_as_cert", config.wapiAsCertBcm)) {
                        Log.d(TAG, "failed to set BCM_WAPI_CERT as cert: " + config.wapiAsCertBcm);
                        return true;
                    }
                }
                if (!(config.wapiUserCertBcm == null || wifiConfigStoreUtils.getWifiNative(this).setNetworkVariable(netId, "wapi_user_cert", config.wapiUserCertBcm))) {
                    if (config.wapiAsCertBcm == null || !config.wapiAsCertBcm.equals("")) {
                        config.wapiUserCertBcm = "\"" + config.wapiUserCertBcm + "\"";
                        if (!wifiConfigStoreUtils.getWifiNative(this).setNetworkVariable(netId, "wapi_user_cert", config.wapiUserCertBcm)) {
                            Log.d(TAG, "failed to set BCM_WAPI_CERT user cert: " + config.wapiUserCertBcm);
                            return true;
                        }
                    }
                    Log.d(TAG, "failed to set BCM_WAPI_CERT as cert: " + config.wapiAsCertBcm);
                    return true;
                }
            }
            Log.d(TAG, config.SSID + ": failed to set BCM_WAPI_CERT index: " + config.wapiCertIndexBcm);
            return true;
        }
        if (!(config.wapiAsCertQualcomm == null || config.wapiAsCertQualcomm.equals("*"))) {
            if (config.wapiAsCertQualcomm.contains("keystore://")) {
                String[] str = config.wapiAsCertQualcomm.split("/");
                config.wapiAsCertQualcomm = "\"" + DEFAULT_CERTIFICATE_PATH + "/" + str[str.length + INVALID_WAPI].substring(HISI_WAPI, str[str.length + INVALID_WAPI].length() + INVALID_WAPI) + "/" + "as.cer" + "\"";
            }
            if (!wifiConfigStoreUtils.getWifiNative(this).setNetworkVariable(netId, "as_cert_file", config.wapiAsCertQualcomm)) {
                Log.e(TAG, "failed to set as cert: " + config.wapiAsCertQualcomm);
                Log.d(TAG, "- config.wapiAsCertQualcomm = " + config.wapiAsCertQualcomm);
                return true;
            }
        }
        if (!(config.wapiUserCertQualcomm == null || config.wapiUserCertQualcomm.equals("*"))) {
            if (config.wapiUserCertQualcomm.contains("keystore://")) {
                str = config.wapiUserCertQualcomm.split("/");
                config.wapiUserCertQualcomm = "\"" + DEFAULT_CERTIFICATE_PATH + "/" + str[str.length + INVALID_WAPI].substring(HISI_WAPI, str[str.length + INVALID_WAPI].length() + INVALID_WAPI) + "/" + "user.cer" + "\"";
            }
            if (!wifiConfigStoreUtils.getWifiNative(this).setNetworkVariable(netId, "user_cert_file", config.wapiUserCertQualcomm)) {
                Log.e(TAG, "failed to set user cert.");
                return true;
            }
        }
        if (config.allowedKeyManagement.get(8)) {
            if (config.wapiPskQualcomm != null && !config.wapiPskQualcomm.equals("*") && !wifiConfigStoreUtils.getWifiNative(this).setNetworkVariable(netId, "wapi_psk", config.wapiPskQualcomm)) {
                Log.e(TAG, "failed to set wapi psk: " + config.wapiPskQualcomm);
                return true;
            } else if (!wifiConfigStoreUtils.getWifiNative(this).setNetworkVariable(netId, "wapi_key_type", Integer.toString(config.wapiPskTypeQualcomm))) {
                Log.e(TAG, "failed to set wapi key type: " + config.wapiPskTypeQualcomm);
                return true;
            }
        }
        return false;
    }

    private static String removeDoubleQuotes(String string) {
        int length = string.length();
        if (length > QUALCOMM_WAPI && string.charAt(HISI_WAPI) == '\"' && string.charAt(length + INVALID_WAPI) == '\"') {
            return string.substring(QUALCOMM_WAPI, length + INVALID_WAPI);
        }
        return string;
    }
}
