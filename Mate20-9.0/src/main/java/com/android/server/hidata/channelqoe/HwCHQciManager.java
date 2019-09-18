package com.android.server.hidata.channelqoe;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class HwCHQciManager {
    private static final String TAG = "HiDATA_ChannelQoE_QciManager";
    private static HwCHQciManager mCHQciManager;
    private List<HwCHQciConfig> mConfigList = new ArrayList();

    private HwCHQciManager() {
    }

    public static void log(String info) {
        Log.e(TAG, info);
    }

    public static HwCHQciManager getInstance() {
        if (mCHQciManager == null) {
            mCHQciManager = new HwCHQciManager();
        }
        return mCHQciManager;
    }

    public void addConfig(HwCHQciConfig config) {
        this.mConfigList.add(config);
    }

    public HwCHQciConfig getChQciConfig(int qci) {
        for (HwCHQciConfig config : this.mConfigList) {
            if (config.mQci == qci) {
                log("find QCI, RTT is " + config.mRtt + " CHLOAD is " + config.mChload);
                return config;
            }
        }
        log("Couldn't find QCI " + qci);
        return HwCHQciConfig.getDefalultQci();
    }
}
