package com.android.server.hidata.appqoe;

import android.content.Context;
import android.emcom.EmcomManager;
import android.os.Handler;
import android.os.Parcel;
import android.rms.iaware.IAwareSdkCore;
import com.android.server.hidata.appqoe.contentaware.HwAPPQoEFICallback;
import com.android.server.hidata.arbitration.HwArbitrationCommonUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class HwAPPQoEContentAware {
    private static final String FI_GAME_REGISTER_MODULE = "module";
    private static final String FI_GAME_REGISTER_MODULE_APPQOE = "com.android.server.hidata.appqoe";
    private static final String FI_GAME_REGISTER_TYPE = "listenAppType";
    private static final int GAME_REG_MAX_RETRY_CNT = 3;
    private static final int GAME_REG_RETRY_STEP = 10000;
    private static final int REGISTER_HICOM_DELAY_TIME = 5000;
    private static final String TAG = "HiData_HwAPPQoEContentAware";
    private static HwAPPQoEContentAware mHwAPPQoEContentAware = null;
    private static Handler stmHandler = null;
    private int gameRegRetryCnt = 0;
    private HwAPPQoEFICallback mFICallback = null;
    private HwAPPQoEGameCallback mGameCallback = new HwAPPQoEGameCallback();

    private HwAPPQoEContentAware(Context context, Handler handler) {
        HwAPPQoEActivityMonitor.createHwAPPQoEActivityMonitor(context);
        registerAllGameCallbacks();
        if (HwArbitrationCommonUtils.IS_HIDATA2_ENABLED) {
            initFIHiComCallback(context);
        }
    }

    protected static HwAPPQoEContentAware createHwAPPQoEContentAware(Context context, Handler handler) {
        stmHandler = handler;
        if (mHwAPPQoEContentAware == null) {
            mHwAPPQoEContentAware = new HwAPPQoEContentAware(context, handler);
        }
        return mHwAPPQoEContentAware;
    }

    private void initFIHiComCallback(Context context) {
        this.mFICallback = HwAPPQoEFICallback.createHwAPPQoEFICallback(context, this.mGameCallback);
        stmHandler.sendEmptyMessageDelayed(HwAPPQoEUtils.MSG_REGISTER_HICOM, 5000);
    }

    public void registerFIHiComCallback() {
        if (this.mFICallback == null) {
            HwAPPQoEUtils.logE(TAG, false, "mFICallback, error", new Object[0]);
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(FI_GAME_REGISTER_MODULE, FI_GAME_REGISTER_MODULE_APPQOE);
            jsonObject.put(FI_GAME_REGISTER_TYPE, 16777216);
            String listenInfo = jsonObject.toString();
            EmcomManager.getInstance().listenHiCom(this.mFICallback, listenInfo);
            HwAPPQoEUtils.logD(TAG, false, "listenFIHiCom: %{public}s", listenInfo);
        } catch (JSONException e) {
            HwAPPQoEUtils.logE(TAG, false, "JSONException: %{public}s", e.getMessage());
        }
    }

    public void reRegisterAllGameCallbacks() {
        registerAllGameCallbacks();
    }

    private void registerAllGameCallbacks() {
        int i;
        boolean isGameRegSucc = registerGameCallback("RES:com.netease.hyxd*->1|7|10000|10001;com.tencent.tmgp.sgame->4|12|10000|10001;com.tencent.tmgp.pubgmhd->1|7|10000|10001", this.mGameCallback);
        HwAPPQoEUtils.logD(TAG, false, "game register result is: %{public}s", String.valueOf(isGameRegSucc));
        if (!isGameRegSucc && (i = this.gameRegRetryCnt) <= 3) {
            this.gameRegRetryCnt = i + 1;
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
            HwAPPQoEUtils.logD(TAG, false, "sentNotifacationToSTM, stmHandler is null", new Object[0]);
            return;
        }
        HwAPPQoEUtils.logD(TAG, false, "sentNotifacationToSTM: %{public}s, action:%{public}d", monitorApp.toString(), Integer.valueOf(action));
        HwAPPStateInfo tempAPPStateInfo = new HwAPPStateInfo();
        tempAPPStateInfo.copyObjectValue(monitorApp);
        stmHandler.sendMessage(stmHandler.obtainMessage(action, tempAPPStateInfo));
    }
}
