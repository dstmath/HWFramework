package com.android.server.wifi.util;

import android.net.wifi.WifiConfiguration;
import android.util.Log;
import com.android.server.wifi.WifiNative;
import java.util.ArrayList;
import java.util.Random;

public class ApConfigUtil {
    public static final int DEFAULT_AP_BAND = 0;
    public static final int DEFAULT_AP_CHANNEL = 6;
    public static final int ERROR_GENERIC = 2;
    public static final int ERROR_NO_CHANNEL = 1;
    public static final int SUCCESS = 0;
    private static final String TAG = "ApConfigUtil";
    private static final Random sRandom = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.util.ApConfigUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.util.ApConfigUtil.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.util.ApConfigUtil.<clinit>():void");
    }

    public static int convertFrequencyToChannel(int frequency) {
        if (frequency >= 2412 && frequency <= 2472) {
            return ((frequency - 2412) / 5) + ERROR_NO_CHANNEL;
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
        if (apBand != 0 && apBand != ERROR_NO_CHANNEL) {
            Log.e(TAG, "Invalid band: " + apBand);
            return -1;
        } else if (apBand == 0) {
            if (allowed2GChannels != null && allowed2GChannels.size() != 0) {
                return ((Integer) allowed2GChannels.get(sRandom.nextInt(allowed2GChannels.size()))).intValue();
            }
            Log.d(TAG, "2GHz allowed channel list not specified");
            return DEFAULT_AP_CHANNEL;
        } else if (allowed5GFreqList != null && allowed5GFreqList.length > 0) {
            return convertFrequencyToChannel(allowed5GFreqList[sRandom.nextInt(allowed5GFreqList.length)]);
        } else {
            Log.e(TAG, "No available channels on 5GHz band");
            return -1;
        }
    }

    public static int updateApChannelConfig(WifiNative wifiNative, String countryCode, ArrayList<Integer> allowed2GChannels, WifiConfiguration config) {
        if (!wifiNative.isHalStarted()) {
            config.apBand = SUCCESS;
            config.apChannel = DEFAULT_AP_CHANNEL;
            return SUCCESS;
        } else if (config.apBand == ERROR_NO_CHANNEL && countryCode == null) {
            Log.e(TAG, "5GHz band is not allowed without country code");
            return ERROR_GENERIC;
        } else {
            if (config.apChannel == 0) {
                config.apChannel = chooseApChannel(config.apBand, allowed2GChannels, wifiNative.getChannelsForBand(ERROR_GENERIC));
                if (config.apChannel == -1) {
                    if (wifiNative.isGetChannelsForBandSupported()) {
                        Log.e(TAG, "Failed to get available channel.");
                        return ERROR_NO_CHANNEL;
                    }
                    config.apBand = SUCCESS;
                    config.apChannel = DEFAULT_AP_CHANNEL;
                }
            }
            return SUCCESS;
        }
    }
}
