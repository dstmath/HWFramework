package com.huawei.hwwifiproservice;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class HwDualBandManager {
    private static HwDualBandManager sHwDualBandManager = null;
    private static HwDualBandStateMachine sHwDualBandStateMachine = null;

    private HwDualBandManager(Context context, IDualBandManagerCallback dbCallBack) {
        sHwDualBandStateMachine = new HwDualBandStateMachine(context, dbCallBack);
    }

    public static HwDualBandStateMachine getHwDualBandStateMachine() {
        return sHwDualBandStateMachine;
    }

    public static HwDualBandManager createInstance(Context context, IDualBandManagerCallback dbCallBack) {
        if (sHwDualBandManager == null) {
            sHwDualBandManager = new HwDualBandManager(context, dbCallBack);
        }
        Log.i(HwDualBandMessageUtil.TAG, "HwDualBandManager init Complete!");
        return sHwDualBandManager;
    }

    public static HwDualBandManager getInstance() {
        return sHwDualBandManager;
    }

    public boolean startDualBandManger() {
        sHwDualBandStateMachine.onStart();
        return true;
    }

    public boolean stopDualBandManger() {
        sHwDualBandStateMachine.onStop();
        return true;
    }

    public boolean isDualbandScanning() {
        HwDualBandStateMachine hwDualBandStateMachine = sHwDualBandStateMachine;
        if (hwDualBandStateMachine == null) {
            return false;
        }
        return hwDualBandStateMachine.isDualbandScanning();
    }

    public boolean startMonitor(ArrayList<HwDualBandMonitorInfo> apList) {
        if (apList.size() == 0) {
            Log.e(HwDualBandMessageUtil.TAG, "startMonitor apList.size() == 0");
            return false;
        }
        Handler mHandler = sHwDualBandStateMachine.getStateMachineHandler();
        Bundle data = new Bundle();
        data.putParcelableArrayList(HwDualBandMessageUtil.MSG_KEY_APLIST, (ArrayList) apList.clone());
        Message msg = Message.obtain();
        msg.what = 102;
        msg.setData(data);
        mHandler.sendMessage(msg);
        return true;
    }

    public boolean stopMonitor() {
        sHwDualBandStateMachine.getStateMachineHandler().sendEmptyMessage(103);
        return true;
    }

    public void updateCurrentRssi(int rssi) {
        Bundle data = new Bundle();
        data.putInt(HwDualBandMessageUtil.MSG_KEY_RSSI, rssi);
        Message msg = Message.obtain();
        msg.what = 18;
        msg.setData(data);
        sHwDualBandStateMachine.getStateMachineHandler().sendMessage(msg);
    }

    public boolean isDualBandSignalApSameSsid(int type, List<HwDualBandMonitorInfo> apList) {
        HwDualBandRelationManager hwDualBandRelationManager = HwDualBandRelationManager.getInstance();
        if (hwDualBandRelationManager == null) {
            return false;
        }
        return hwDualBandRelationManager.isDualBandSignalApSameSsid(type, apList);
    }
}
