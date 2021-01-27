package com.android.server.hidata.arbitration;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.android.server.hidata.wavemapping.IWaveMappingCallback;
import java.util.HashMap;

public class HwArbitrationHistoryQoeManager {
    private static final String TAG = "HiDATA_HwArbitrationHistoryQoeManager";
    private static HwArbitrationHistoryQoeManager sInstance = null;
    private HashMap<Integer, String> mPreferWifiMap = new HashMap<>();
    private Handler mWmpStateMachineHandler;

    private HwArbitrationHistoryQoeManager(Handler handler) {
        this.mWmpStateMachineHandler = handler;
        HwArbitrationCommonUtils.logI(TAG, false, "HwArbitrationHistoryQoeManager init success", new Object[0]);
    }

    public static synchronized HwArbitrationHistoryQoeManager getInstance(Handler handler) {
        HwArbitrationHistoryQoeManager hwArbitrationHistoryQoeManager;
        synchronized (HwArbitrationHistoryQoeManager.class) {
            if (sInstance == null) {
                sInstance = new HwArbitrationHistoryQoeManager(handler);
            }
            hwArbitrationHistoryQoeManager = sInstance;
        }
        return hwArbitrationHistoryQoeManager;
    }

    public void savePreferListForWifi(HashMap<Integer, String> list) {
        HwArbitrationCommonUtils.logI(TAG, false, "savePreferListForWifi:%{public}s", list.toString());
        this.mPreferWifiMap = (HashMap) list.clone();
    }

    public HashMap<Integer, String> queryHistoryPreference(int networkType, IWaveMappingCallback callback) {
        HwArbitrationCommonUtils.logI(TAG, false, "queryHistoryPreference enter, nw=%{public}d", Integer.valueOf(networkType));
        if (this.mPreferWifiMap.isEmpty()) {
            HwArbitrationCommonUtils.logI(TAG, false, "ask WM futhur", new Object[0]);
            Bundle data = new Bundle();
            data.putInt("NW", networkType);
            Message msg = Message.obtain(this.mWmpStateMachineHandler, 121);
            msg.setData(data);
            msg.obj = callback;
            this.mWmpStateMachineHandler.sendMessage(msg);
        } else {
            HwArbitrationCommonUtils.logI(TAG, false, "directly get prefered NW:%{public}s", this.mPreferWifiMap.toString());
        }
        return this.mPreferWifiMap;
    }
}
