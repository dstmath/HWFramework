package com.android.server.hidata.arbitration;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.android.server.hidata.wavemapping.IWaveMappingCallback;
import com.android.server.hidata.wavemapping.cons.Constant;
import java.util.HashMap;

public class HwArbitrationHistoryQoeManager {
    private static final String TAG = "HiDATA_HwArbitrationHistoryQoeManager";
    private static HwArbitrationHistoryQoeManager instance = null;
    private Handler mWmpStateMachineHandler;
    private HashMap<Integer, String> preferListWifi = new HashMap<>();

    private HwArbitrationHistoryQoeManager(Handler handler) {
        this.mWmpStateMachineHandler = handler;
        HwArbitrationCommonUtils.logI(TAG, false, "HwArbitrationHistoryQoeManager init finish.", new Object[0]);
    }

    public static synchronized HwArbitrationHistoryQoeManager getInstance(Handler handler) {
        HwArbitrationHistoryQoeManager hwArbitrationHistoryQoeManager;
        synchronized (HwArbitrationHistoryQoeManager.class) {
            if (instance == null) {
                instance = new HwArbitrationHistoryQoeManager(handler);
            }
            hwArbitrationHistoryQoeManager = instance;
        }
        return hwArbitrationHistoryQoeManager;
    }

    public void queryHistoryQoE(int UID, int appId, int scence, int networkType, IWaveMappingCallback callback, int direction) {
        int net;
        HwArbitrationCommonUtils.logI(TAG, false, "queryHistoryQoE enter, UID=%{public}d, appId=%{public}d, scence=%{public}d, nw=%{public}d, direction=%{public}d", Integer.valueOf(UID), Integer.valueOf(appId), Integer.valueOf(scence), Integer.valueOf(networkType), Integer.valueOf(direction));
        Bundle data = new Bundle();
        int fullAppId = scence;
        if (2000 < appId) {
            fullAppId = Constant.transferGameId2FullId(appId, scence);
        }
        if (800 == networkType) {
            net = 1;
        } else if (801 == networkType) {
            net = 0;
        } else {
            net = 8;
        }
        data.putInt("FULLID", fullAppId);
        data.putInt("UID", UID);
        data.putInt("NW", net);
        data.putInt("ArbNW", networkType);
        data.putInt("DIRECT", direction);
        Message msg = Message.obtain(this.mWmpStateMachineHandler, 120);
        msg.setData(data);
        msg.obj = callback;
        this.mWmpStateMachineHandler.sendMessage(msg);
    }

    public void savePreferListForWifi(HashMap<Integer, String> list) {
        HwArbitrationCommonUtils.logI(TAG, false, "savePreferListForWifi:%{public}s", list.toString());
        this.preferListWifi = (HashMap) list.clone();
    }

    public HashMap<Integer, String> queryHistoryPreference(int networkType, IWaveMappingCallback callback) {
        HwArbitrationCommonUtils.logI(TAG, false, "queryHistoryPreference enter, nw=%{public}d", Integer.valueOf(networkType));
        if (this.preferListWifi.isEmpty()) {
            HwArbitrationCommonUtils.logI(TAG, false, "ask WM futhur", new Object[0]);
            Bundle data = new Bundle();
            data.putInt("NW", networkType);
            Message msg = Message.obtain(this.mWmpStateMachineHandler, 121);
            msg.setData(data);
            msg.obj = callback;
            this.mWmpStateMachineHandler.sendMessage(msg);
        } else {
            HwArbitrationCommonUtils.logI(TAG, false, "directly get prefered NW:%{public}s", this.preferListWifi.toString());
        }
        return this.preferListWifi;
    }
}
