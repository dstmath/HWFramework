package com.android.server.wifi.hwUtil;

import android.util.Log;
import com.android.server.wifi.util.ApConfigUtil;
import java.util.Random;

public class HwApConfigUtilEx {
    private static int AP_CHANNEL_165_20MHZ = 165;
    public static final int DEFAULT_AP_CHANNEL_5G = 149;
    private static final String TAG = "ApConfigUtil";
    private static Random sRandom = new Random();

    /* JADX INFO: Multiple debug info for r1v3 int[]: [D('i' int), D('allowed5GFreqListNew' int[])] */
    public static int getSelected5GChannel(int[] allowed5GFreqList) {
        if (allowed5GFreqList == null || allowed5GFreqList.length == 0) {
            return -1;
        }
        for (int i : allowed5GFreqList) {
            if (ApConfigUtil.convertFrequencyToChannel(i) == 149) {
                Log.d(TAG, "5G Channel is 149");
                return DEFAULT_AP_CHANNEL_5G;
            }
        }
        int len = allowed5GFreqList.length;
        for (int i2 = 0; i2 < allowed5GFreqList.length; i2++) {
            if (ApConfigUtil.convertFrequencyToChannel(allowed5GFreqList[i2]) == AP_CHANNEL_165_20MHZ) {
                Log.w(TAG, "updateApChannelConfig exclude AP_CHANNEL_165_20MHZ " + allowed5GFreqList[i2]);
                len += -1;
            }
        }
        int[] allowed5GFreqListNew = new int[len];
        int j = 0;
        for (int i3 = 0; i3 < allowed5GFreqList.length; i3++) {
            if (ApConfigUtil.convertFrequencyToChannel(allowed5GFreqList[i3]) != AP_CHANNEL_165_20MHZ) {
                allowed5GFreqListNew[j] = allowed5GFreqList[i3];
                j++;
            }
        }
        if (allowed5GFreqListNew.length > 0) {
            allowed5GFreqList = allowed5GFreqListNew;
        }
        int select5GChannel = ApConfigUtil.convertFrequencyToChannel(allowed5GFreqList[sRandom.nextInt(allowed5GFreqList.length)]);
        Log.d(TAG, "5G Channel is " + select5GChannel);
        return select5GChannel;
    }
}
