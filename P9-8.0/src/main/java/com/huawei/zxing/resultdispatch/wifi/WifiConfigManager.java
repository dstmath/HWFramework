package com.huawei.zxing.resultdispatch.wifi;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import com.huawei.zxing.client.result.WifiParsedResult;
import java.util.List;
import java.util.regex.Pattern;

public final class WifiConfigManager extends AsyncTask<WifiParsedResult, Object, Object> {
    private static final Pattern HEX_DIGITS = Pattern.compile("[0-9A-Fa-f]+");
    private static final String TAG = WifiConfigManager.class.getSimpleName();
    public static final int WIFI_ERROR_CONNECT_TIMEOUT = 1;
    public static final int WIFI_ERROR_ENABLED = 0;
    public static final int WIFI_ERROR_SAVE = 2;
    private static SaveConfigurationListener mListener = null;
    private final WifiManager wifiManager;

    public interface SaveConfigurationListener {
        void onSaveConfigurationFailed(int i);

        void onSaveConfigurationSucceed(WifiConfiguration wifiConfiguration);
    }

    public WifiConfigManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    protected Object doInBackground(WifiParsedResult... args) {
        WifiParsedResult theWifiResult = args[0];
        if (!this.wifiManager.isWifiEnabled()) {
            Log.i(TAG, "Enabling wi-fi...");
            if (this.wifiManager.setWifiEnabled(true)) {
                Log.i(TAG, "Wi-fi enabled");
                int count = 0;
                while (!this.wifiManager.isWifiEnabled()) {
                    if (count >= 10) {
                        Log.i(TAG, "Took too long to enable wi-fi, quitting");
                        if (mListener != null) {
                            mListener.onSaveConfigurationFailed(1);
                        }
                        return null;
                    }
                    Log.i(TAG, "Still waiting for wi-fi to enable...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    count++;
                }
            } else {
                Log.w(TAG, "Wi-fi could not be enabled!");
                if (mListener != null) {
                    mListener.onSaveConfigurationFailed(0);
                }
                return null;
            }
        }
        try {
            NetworkType networkType = NetworkType.forIntentValue(theWifiResult.getNetworkEncryption());
            if (networkType == NetworkType.NO_PASSWORD) {
                changeNetworkUnEncrypted(this.wifiManager, theWifiResult);
            } else {
                String password = theWifiResult.getPassword();
                if (!(password == null || password.length() == 0)) {
                    if (networkType == NetworkType.WEP) {
                        changeNetworkWEP(this.wifiManager, theWifiResult);
                    } else if (networkType == NetworkType.WPA) {
                        changeNetworkWPA(this.wifiManager, theWifiResult);
                    }
                }
            }
            return null;
        } catch (IllegalArgumentException e2) {
            if (mListener != null) {
                mListener.onSaveConfigurationFailed(2);
            }
            return null;
        }
    }

    private static void updateNetwork(WifiManager wifiManager, WifiConfiguration config) {
        Integer foundNetworkID = findNetworkInExistingConfig(wifiManager, config.SSID);
        if (foundNetworkID != null) {
            Log.i(TAG, "Removing old configuration for network " + config.SSID);
            wifiManager.removeNetwork(foundNetworkID.intValue());
            wifiManager.saveConfiguration();
        }
        int networkId = wifiManager.addNetwork(config);
        if (networkId < 0) {
            if (mListener != null) {
                mListener.onSaveConfigurationFailed(1);
            }
            Log.w(TAG, "Unable to add network " + config.SSID);
        } else if (wifiManager.enableNetwork(networkId, true)) {
            Log.i(TAG, "Associating to network " + config.SSID);
            wifiManager.saveConfiguration();
            if (mListener != null) {
                mListener.onSaveConfigurationSucceed(config);
            }
        } else {
            if (mListener != null) {
                mListener.onSaveConfigurationFailed(1);
            }
            Log.w(TAG, "Failed to enable network " + config.SSID);
        }
    }

    private static WifiConfiguration changeNetworkCommon(WifiParsedResult wifiResult) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = quoteNonHex(wifiResult.getSsid(), new int[0]);
        config.hiddenSSID = wifiResult.isHidden();
        return config;
    }

    private static void changeNetworkWEP(WifiManager wifiManager, WifiParsedResult wifiResult) {
        WifiConfiguration config = changeNetworkCommon(wifiResult);
        config.wepKeys[0] = quoteNonHex(wifiResult.getPassword(), 10, 26, 58);
        config.wepTxKeyIndex = 0;
        config.allowedAuthAlgorithms.set(1);
        config.allowedKeyManagement.set(0);
        config.allowedGroupCiphers.set(2);
        config.allowedGroupCiphers.set(3);
        config.allowedGroupCiphers.set(0);
        config.allowedGroupCiphers.set(1);
        updateNetwork(wifiManager, config);
    }

    private static void changeNetworkWPA(WifiManager wifiManager, WifiParsedResult wifiResult) {
        WifiConfiguration config = changeNetworkCommon(wifiResult);
        config.preSharedKey = quoteNonHex(wifiResult.getPassword(), 64);
        config.allowedAuthAlgorithms.set(0);
        config.allowedProtocols.set(0);
        config.allowedProtocols.set(1);
        config.allowedKeyManagement.set(1);
        config.allowedKeyManagement.set(2);
        config.allowedPairwiseCiphers.set(1);
        config.allowedPairwiseCiphers.set(2);
        config.allowedGroupCiphers.set(2);
        config.allowedGroupCiphers.set(3);
        updateNetwork(wifiManager, config);
    }

    private static void changeNetworkUnEncrypted(WifiManager wifiManager, WifiParsedResult wifiResult) {
        WifiConfiguration config = changeNetworkCommon(wifiResult);
        config.allowedKeyManagement.set(0);
        updateNetwork(wifiManager, config);
    }

    private static Integer findNetworkInExistingConfig(WifiManager wifiManager, String ssid) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        if (existingConfigs != null) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals(ssid)) {
                    return Integer.valueOf(existingConfig.networkId);
                }
            }
        }
        return null;
    }

    private static String quoteNonHex(String value, int... allowedLengths) {
        return isHexOfLength(value, allowedLengths) ? value : convertToQuotedString(value);
    }

    private static String convertToQuotedString(String string) {
        if (string == null || string.length() == 0) {
            return null;
        }
        if (string.charAt(0) == '\"' && string.charAt(string.length() - 1) == '\"') {
            return string;
        }
        return '\"' + string + '\"';
    }

    private static boolean isHexOfLength(CharSequence value, int... allowedLengths) {
        if (value == null || (HEX_DIGITS.matcher(value).matches() ^ 1) != 0) {
            return false;
        }
        if (allowedLengths.length == 0) {
            return true;
        }
        for (int length : allowedLengths) {
            if (value.length() == length) {
                return true;
            }
        }
        return false;
    }

    public void registerListener(SaveConfigurationListener listener) {
        mListener = listener;
    }
}
