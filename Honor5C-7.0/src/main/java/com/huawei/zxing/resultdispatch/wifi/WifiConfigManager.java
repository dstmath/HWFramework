package com.huawei.zxing.resultdispatch.wifi;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import com.huawei.zxing.client.result.WifiParsedResult;
import java.util.List;
import java.util.regex.Pattern;

public final class WifiConfigManager extends AsyncTask<WifiParsedResult, Object, Object> {
    private static final Pattern HEX_DIGITS = null;
    private static final String TAG = null;
    public static final int WIFI_ERROR_CONNECT_TIMEOUT = 1;
    public static final int WIFI_ERROR_ENABLED = 0;
    public static final int WIFI_ERROR_SAVE = 2;
    private static SaveConfigurationListener mListener;
    private final WifiManager wifiManager;

    public interface SaveConfigurationListener {
        void onSaveConfigurationFailed(int i);

        void onSaveConfigurationSucceed(WifiConfiguration wifiConfiguration);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.resultdispatch.wifi.WifiConfigManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.resultdispatch.wifi.WifiConfigManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.resultdispatch.wifi.WifiConfigManager.<clinit>():void");
    }

    public WifiConfigManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    protected Object doInBackground(WifiParsedResult... args) {
        WifiParsedResult theWifiResult = args[WIFI_ERROR_ENABLED];
        if (!this.wifiManager.isWifiEnabled()) {
            Log.i(TAG, "Enabling wi-fi...");
            if (this.wifiManager.setWifiEnabled(true)) {
                Log.i(TAG, "Wi-fi enabled");
                int count = WIFI_ERROR_ENABLED;
                while (!this.wifiManager.isWifiEnabled()) {
                    if (count >= 10) {
                        Log.i(TAG, "Took too long to enable wi-fi, quitting");
                        if (mListener != null) {
                            mListener.onSaveConfigurationFailed(WIFI_ERROR_CONNECT_TIMEOUT);
                        }
                        return null;
                    }
                    Log.i(TAG, "Still waiting for wi-fi to enable...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    count += WIFI_ERROR_CONNECT_TIMEOUT;
                }
            } else {
                Log.w(TAG, "Wi-fi could not be enabled!");
                if (mListener != null) {
                    mListener.onSaveConfigurationFailed(WIFI_ERROR_ENABLED);
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
                mListener.onSaveConfigurationFailed(WIFI_ERROR_SAVE);
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
                mListener.onSaveConfigurationFailed(WIFI_ERROR_CONNECT_TIMEOUT);
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
                mListener.onSaveConfigurationFailed(WIFI_ERROR_CONNECT_TIMEOUT);
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
        config.SSID = quoteNonHex(wifiResult.getSsid(), new int[WIFI_ERROR_ENABLED]);
        config.hiddenSSID = wifiResult.isHidden();
        return config;
    }

    private static void changeNetworkWEP(WifiManager wifiManager, WifiParsedResult wifiResult) {
        WifiConfiguration config = changeNetworkCommon(wifiResult);
        config.wepKeys[WIFI_ERROR_ENABLED] = quoteNonHex(wifiResult.getPassword(), 10, 26, 58);
        config.wepTxKeyIndex = WIFI_ERROR_ENABLED;
        config.allowedAuthAlgorithms.set(WIFI_ERROR_CONNECT_TIMEOUT);
        config.allowedKeyManagement.set(WIFI_ERROR_ENABLED);
        config.allowedGroupCiphers.set(WIFI_ERROR_SAVE);
        config.allowedGroupCiphers.set(3);
        config.allowedGroupCiphers.set(WIFI_ERROR_ENABLED);
        config.allowedGroupCiphers.set(WIFI_ERROR_CONNECT_TIMEOUT);
        updateNetwork(wifiManager, config);
    }

    private static void changeNetworkWPA(WifiManager wifiManager, WifiParsedResult wifiResult) {
        WifiConfiguration config = changeNetworkCommon(wifiResult);
        String password = wifiResult.getPassword();
        int[] iArr = new int[WIFI_ERROR_CONNECT_TIMEOUT];
        iArr[WIFI_ERROR_ENABLED] = 64;
        config.preSharedKey = quoteNonHex(password, iArr);
        config.allowedAuthAlgorithms.set(WIFI_ERROR_ENABLED);
        config.allowedProtocols.set(WIFI_ERROR_ENABLED);
        config.allowedProtocols.set(WIFI_ERROR_CONNECT_TIMEOUT);
        config.allowedKeyManagement.set(WIFI_ERROR_CONNECT_TIMEOUT);
        config.allowedKeyManagement.set(WIFI_ERROR_SAVE);
        config.allowedPairwiseCiphers.set(WIFI_ERROR_CONNECT_TIMEOUT);
        config.allowedPairwiseCiphers.set(WIFI_ERROR_SAVE);
        config.allowedGroupCiphers.set(WIFI_ERROR_SAVE);
        config.allowedGroupCiphers.set(3);
        updateNetwork(wifiManager, config);
    }

    private static void changeNetworkUnEncrypted(WifiManager wifiManager, WifiParsedResult wifiResult) {
        WifiConfiguration config = changeNetworkCommon(wifiResult);
        config.allowedKeyManagement.set(WIFI_ERROR_ENABLED);
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
        if (string.charAt(WIFI_ERROR_ENABLED) == '\"' && string.charAt(string.length() - 1) == '\"') {
            return string;
        }
        return '\"' + string + '\"';
    }

    private static boolean isHexOfLength(CharSequence value, int... allowedLengths) {
        if (value == null || !HEX_DIGITS.matcher(value).matches()) {
            return false;
        }
        if (allowedLengths.length == 0) {
            return true;
        }
        int length = allowedLengths.length;
        for (int i = WIFI_ERROR_ENABLED; i < length; i += WIFI_ERROR_CONNECT_TIMEOUT) {
            if (value.length() == allowedLengths[i]) {
                return true;
            }
        }
        return false;
    }

    public void registerListener(SaveConfigurationListener listener) {
        mListener = listener;
    }
}
