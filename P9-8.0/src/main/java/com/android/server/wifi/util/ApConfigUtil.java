package com.android.server.wifi.util;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ScanListener;
import android.net.wifi.WifiScanner.ScanSettings;
import android.net.wifi.WifiScanner.ScanSettings.HiddenNetwork;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.wifi.WifiNative;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ApConfigUtil {
    public static final int DEFAULT_AP_BAND = 0;
    public static final int DEFAULT_AP_CHANNEL = 6;
    public static final int ERROR_GENERIC = 2;
    public static final int ERROR_NO_CHANNEL = 1;
    public static final int SUCCESS = 0;
    private static final String TAG = "ApConfigUtil";
    private static final Random sRandom = new Random();

    public static int convertFrequencyToChannel(int frequency) {
        if (frequency >= 2412 && frequency <= 2472) {
            return ((frequency - 2412) / 5) + 1;
        }
        if (frequency == 2484) {
            return 14;
        }
        if (frequency < 5170 || frequency > 5825) {
            return -1;
        }
        return ((frequency - 5170) / 5) + 34;
    }

    public static int chooseApChannel(int apBand, ArrayList<Integer> allowed2GChannels, int[] allowed5GFreqList) {
        if (apBand != 0 && apBand != 1) {
            Log.e(TAG, "Invalid band: " + apBand);
            return -1;
        } else if (apBand == 0) {
            if (allowed2GChannels != null && allowed2GChannels.size() != 0) {
                return ((Integer) allowed2GChannels.get(sRandom.nextInt(allowed2GChannels.size()))).intValue();
            }
            Log.d(TAG, "2GHz allowed channel list not specified");
            return 6;
        } else if (allowed5GFreqList == null || allowed5GFreqList.length <= 0) {
            Log.e(TAG, "No available channels on 5GHz band");
            return -1;
        } else {
            int i;
            int len = allowed5GFreqList.length;
            for (i = 0; i < allowed5GFreqList.length; i++) {
                if (convertFrequencyToChannel(allowed5GFreqList[i]) == 165) {
                    Log.w(TAG, "updateApChannelConfig exclude AP_CHANNEL_165_20MHZ " + allowed5GFreqList[i]);
                    len--;
                }
            }
            int[] allowed5GFreqListNew = new int[len];
            int j = 0;
            for (i = 0; i < allowed5GFreqList.length; i++) {
                if (convertFrequencyToChannel(allowed5GFreqList[i]) != 165) {
                    int j2 = j + 1;
                    allowed5GFreqListNew[j] = allowed5GFreqList[i];
                    j = j2;
                }
            }
            if (allowed5GFreqListNew.length > 0) {
                allowed5GFreqList = allowed5GFreqListNew;
            }
            return convertFrequencyToChannel(allowed5GFreqList[sRandom.nextInt(allowed5GFreqList.length)]);
        }
    }

    public static int updateApChannelConfig(WifiNative wifiNative, String countryCode, ArrayList<Integer> allowed2GChannels, WifiConfiguration config) {
        if (!wifiNative.isHalStarted()) {
            config.apBand = 0;
            config.apChannel = 6;
            return 0;
        } else if (config.apBand == 1 && countryCode == null) {
            Log.e(TAG, "5GHz band is not allowed without country code");
            return 2;
        } else {
            if (config.apChannel == 0) {
                config.apChannel = chooseApChannel(config.apBand, allowed2GChannels, wifiNative.getChannelsForBand(2));
                if (config.apChannel == -1) {
                    if (wifiNative.isGetChannelsForBandSupported()) {
                        Log.e(TAG, "Failed to get available channel.");
                        return 1;
                    }
                    config.apBand = 0;
                    config.apChannel = 6;
                }
            }
            return 0;
        }
    }

    public static void startScanForHiddenNetwork(WifiScanner scanner, ScanListener listener, WifiConfiguration config) {
        int i = 0;
        List<HiddenNetwork> hiddenNetworkList = new ArrayList();
        String[] charsets = SystemProperties.getBoolean("ro.config.wifi_use_euc-kr", false) ? new String[]{"UTF-8", "EUC-KR", "KSC5601"} : new String[]{"UTF-8", "GBK"};
        int length = charsets.length;
        while (i < length) {
            String hex = NativeUtil.quotedAsciiStringToHex(config.SSID, charsets[i]);
            if (hex != null) {
                hiddenNetworkList.add(new HiddenNetwork(hex));
            }
            i++;
        }
        ScanSettings settings = new ScanSettings();
        settings.band = 7;
        settings.reportEvents = 3;
        settings.hiddenNetworks = (HiddenNetwork[]) hiddenNetworkList.toArray(new HiddenNetwork[hiddenNetworkList.size()]);
        Log.d(TAG, "startScanForHiddenNetwork");
        scanner.startScan(settings, listener);
    }
}
