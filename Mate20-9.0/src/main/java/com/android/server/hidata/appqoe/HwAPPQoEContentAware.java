package com.android.server.hidata.appqoe;

import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.rms.iaware.IAwareSdkCore;

public class HwAPPQoEContentAware {
    private static final int GAME_REG_MAX_RETRY_CNT = 3;
    private static final int GAME_REG_RETRY_STEP = 10000;
    private static String TAG = "HiData_HwAPPQoEContentAware";
    private static HwAPPQoEContentAware mHwAPPQoEContentAware = null;
    private static Handler stmHandler = null;
    private int gameRegRetryCnt = 0;
    private HwAPPQoEGameCallback mGameCallback = new HwAPPQoEGameCallback();

    private HwAPPQoEContentAware(Context context, Handler handler) {
        HwAPPQoEActivityMonitor.createHwAPPQoEActivityMonitor(context);
        registerAllGameCallbacks();
    }

    protected static HwAPPQoEContentAware createHwAPPQoEContentAware(Context context, Handler handler) {
        stmHandler = handler;
        if (mHwAPPQoEContentAware == null) {
            mHwAPPQoEContentAware = new HwAPPQoEContentAware(context, handler);
        }
        return mHwAPPQoEContentAware;
    }

    public void reRegisterAllGameCallbacks() {
        registerAllGameCallbacks();
    }

    private void registerAllGameCallbacks() {
        boolean isGameRegSucc = registerGameCallback("RES:com.netease.hyxd*->1|7|10000|10001;com.tencent.tmgp.sgame->4|12|10000|10001;com.tencent.tmgp.pubgmhd->1|7|10000|10001", this.mGameCallback);
        HwAPPQoEUtils.logD(TAG, "game register result is: " + isGameRegSucc);
        if (!isGameRegSucc && this.gameRegRetryCnt <= 3) {
            this.gameRegRetryCnt++;
            stmHandler.sendEmptyMessageDelayed(202, (long) (this.gameRegRetryCnt * 10000));
        }
    }

    private boolean registerGameCallback(String packageName, HwAPPQoEGameCallback mGameCallback2) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeString(packageName);
        data.writeStrongBinder(mGameCallback2);
        IAwareSdkCore.handleEvent(4, data, reply);
        int ret = reply.readInt();
        reply.recycle();
        data.recycle();
        return ret > 0;
    }

    public static void sentNotificationToSTM(HwAPPStateInfo monitorApp, int action) {
        if (stmHandler == null || monitorApp == null) {
            HwAPPQoEUtils.logD(TAG, "sentNotifacationToSTM, stmHandler is null");
            return;
        }
        String str = TAG;
        HwAPPQoEUtils.logD(str, "sentNotifacationToSTM: " + monitorApp.toString() + ", action:" + action);
        HwAPPStateInfo tempAPPStateInfo = new HwAPPStateInfo();
        tempAPPStateInfo.copyObjectValue(monitorApp);
        stmHandler.sendMessage(stmHandler.obtainMessage(action, tempAPPStateInfo));
    }
}
