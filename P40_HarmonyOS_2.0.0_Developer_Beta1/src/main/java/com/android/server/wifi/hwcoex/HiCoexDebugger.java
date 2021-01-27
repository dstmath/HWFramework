package com.android.server.wifi.hwcoex;

import android.os.SystemProperties;
import android.text.TextUtils;

public class HiCoexDebugger {
    private static final String EMPTY = "";
    private static final String PROP_CELL_SCORE_CONFIG = "runtime.hicoex.cellscoreconfig";
    private static final String PROP_SOFTAP_CHANNEL = "runtime.softap.channel";
    private static final String PROP_WIFI_SCORE_CONFIG = "runtime.hicoex.wifiscoreconfig";
    private static final String SPLIT_CHAR = ",";
    private static final String TAG = "HiCoexDebugger";
    private static HiCoexDebugger mDebugger = null;

    private HiCoexDebugger() {
    }

    public static synchronized HiCoexDebugger getInstance() {
        HiCoexDebugger hiCoexDebugger;
        synchronized (HiCoexDebugger.class) {
            if (mDebugger == null) {
                mDebugger = new HiCoexDebugger();
            }
            hiCoexDebugger = mDebugger;
        }
        return hiCoexDebugger;
    }

    public static int[] loadScoreConfiguration(int type) {
        int[] scores = null;
        String strScoreConfig = "";
        if (type == 0) {
            strScoreConfig = SystemProperties.get(PROP_CELL_SCORE_CONFIG, "");
        } else if (type == 1) {
            strScoreConfig = SystemProperties.get(PROP_WIFI_SCORE_CONFIG, "");
        } else {
            HiCoexUtils.logD(TAG, "loadScoreConfiguration invalid type:" + type);
        }
        HiCoexUtils.logD(TAG, "scoreConfig:" + strScoreConfig);
        if (TextUtils.isEmpty(strScoreConfig)) {
            return null;
        }
        String[] scoreConfigs = strScoreConfig.split(SPLIT_CHAR);
        if (scoreConfigs.length > 0) {
            scores = new int[scoreConfigs.length];
            for (int i = 0; i < scoreConfigs.length; i++) {
                try {
                    scores[i] = Integer.valueOf(scoreConfigs[i]).intValue();
                } catch (NumberFormatException e) {
                    HiCoexUtils.logE(TAG, "error:" + scoreConfigs[i]);
                    return null;
                }
            }
        }
        return scores;
    }

    public static int getRecommendWiFiChannel() {
        return SystemProperties.getInt(PROP_SOFTAP_CHANNEL, 0);
    }
}
