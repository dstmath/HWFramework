package com.android.server.wifi.wifipro;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.util.ArrayList;

public class HwDualBandManager {
    private static HwDualBandManager mHwDualBandManager = null;
    private HwDualBandStateMachine mHwDualBandStateMachine = null;

    private HwDualBandManager(Context context, IDualBandManagerCallback callBack) {
        this.mHwDualBandStateMachine = new HwDualBandStateMachine(context, callBack);
    }

    public static HwDualBandManager createInstance(Context context, IDualBandManagerCallback callBack) {
        if (mHwDualBandManager == null) {
            mHwDualBandManager = new HwDualBandManager(context, callBack);
        }
        Log.d(HwDualBandMessageUtil.TAG, "HwDualBandManager init Complete!");
        return mHwDualBandManager;
    }

    public static HwDualBandManager getInstance() {
        return mHwDualBandManager;
    }

    public boolean startDualBandManger() {
        this.mHwDualBandStateMachine.onStart();
        return true;
    }

    public boolean stopDualBandManger() {
        this.mHwDualBandStateMachine.onStop();
        return true;
    }

    public boolean isDualbandScanning() {
        if (this.mHwDualBandStateMachine == null) {
            return false;
        }
        return this.mHwDualBandStateMachine.isDualbandScanning();
    }

    public boolean startMonitor(ArrayList<HwDualBandMonitorInfo> apList) {
        if (apList.size() == 0) {
            Log.e(HwDualBandMessageUtil.TAG, "startMonitor apList.size() == 0");
            return false;
        }
        Handler mHandler = this.mHwDualBandStateMachine.getStateMachineHandler();
        Bundle data = new Bundle();
        data.putParcelableArrayList(HwDualBandMessageUtil.MSG_KEY_APLIST, (ArrayList) apList.clone());
        Message msg = new Message();
        msg.what = 102;
        msg.setData(data);
        mHandler.sendMessage(msg);
        return true;
    }

    public boolean stopMonitor() {
        this.mHwDualBandStateMachine.getStateMachineHandler().sendEmptyMessage(103);
        return true;
    }

    public void updateCurrentRssi(int rssi) {
        Bundle data = new Bundle();
        data.putInt(HwDualBandMessageUtil.MSG_KEY_RSSI, rssi);
        Message msg = new Message();
        msg.what = 18;
        msg.setData(data);
        this.mHwDualBandStateMachine.getStateMachineHandler().sendMessage(msg);
    }
}
