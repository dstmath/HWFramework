package com.android.server.hidata.appqoe;

import android.content.Context;
import android.emcom.EmcomManager;
import android.os.Handler;
import android.os.Parcel;
import android.rms.iaware.IAwareSdkCore;
import com.android.server.hidata.appqoe.contentaware.HwAppQoeFiCallback;
import com.android.server.hidata.arbitration.HwArbitrationCommonUtils;
import com.android.server.hidata.arbitration.HwArbitrationDefs;
import com.android.server.swing.HwSwingMotionGestureConstant;
import org.json.JSONException;
import org.json.JSONObject;

public class HwAppQoeContentAware {
    private static final String FI_GAME_REGISTER_MODULE = "module";
    private static final String FI_GAME_REGISTER_MODULE_APPQOE = "com.android.server.hidata.appqoe";
    private static final String FI_GAME_REGISTER_TYPE = "listenAppType";
    private static final int GAME_REG_MAX_RETRY_CNT = 3;
    private static final int GAME_REG_RETRY_STEP = 10000;
    private static final int REGISTER_HICOM_DELAY_TIME = 5000;
    private static final String TAG = (HwArbitrationDefs.BASE_TAG + HwAppQoeContentAware.class.getSimpleName());
    private static Handler sHandler = null;
    private static HwAppQoeContentAware sHwAppQoeContentAware = null;
    private int gameRegRetryCnt = 0;
    private HwAppQoeFiCallback mHwAppQoeFiCallback = null;
    private HwAppQoeGameCallback mHwAppQoeGameCallback = new HwAppQoeGameCallback();

    private HwAppQoeContentAware(Context context, Handler handler) {
        HwAppQoeActivityMonitor.createHwAppQoeActivityMonitor(context);
        registerAllGameCallbacks();
        if (HwArbitrationCommonUtils.IS_HIDATA2_ENABLED) {
            initFiHiComCallback(context);
        }
    }

    protected static HwAppQoeContentAware createHwAppQoeContentAware(Context context, Handler handler) {
        sHandler = handler;
        if (sHwAppQoeContentAware == null) {
            sHwAppQoeContentAware = new HwAppQoeContentAware(context, handler);
        }
        return sHwAppQoeContentAware;
    }

    private void initFiHiComCallback(Context context) {
        this.mHwAppQoeFiCallback = HwAppQoeFiCallback.createHwAppQoeFiCallback(context, this.mHwAppQoeGameCallback);
        sHandler.sendEmptyMessageDelayed(HwAppQoeUtils.MSG_REGISTER_HICOM, HwSwingMotionGestureConstant.HOVER_SCREEN_OFF_THRESHOLD);
    }

    public void registerFiHiComCallback() {
        if (this.mHwAppQoeFiCallback == null) {
            HwAppQoeUtils.logE(TAG, false, "mFICallback, error", new Object[0]);
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(FI_GAME_REGISTER_MODULE, FI_GAME_REGISTER_MODULE_APPQOE);
            jsonObject.put(FI_GAME_REGISTER_TYPE, 16777216);
            String listenInfo = jsonObject.toString();
            EmcomManager.getInstance().listenHiCom(this.mHwAppQoeFiCallback, listenInfo);
            HwAppQoeUtils.logD(TAG, false, "listenFIHiCom: %{public}s", listenInfo);
        } catch (JSONException e) {
            HwAppQoeUtils.logE(TAG, false, "JSONException: %{public}s", e.getMessage());
        }
    }

    public void reRegisterAllGameCallbacks() {
        registerAllGameCallbacks();
    }

    private void registerAllGameCallbacks() {
        int i;
        boolean isGameRegSucc = registerGameCallback("RES:com.netease.hyxd*->1|7|10000|10001;com.tencent.tmgp.sgame->4|12|10000|10001;com.tencent.tmgp.pubgmhd->1|7|10000|10001", this.mHwAppQoeGameCallback);
        HwAppQoeUtils.logD(TAG, false, "game register result is: %{public}s", String.valueOf(isGameRegSucc));
        if (!isGameRegSucc && (i = this.gameRegRetryCnt) <= 3) {
            this.gameRegRetryCnt = i + 1;
            sHandler.sendEmptyMessageDelayed(202, (long) (this.gameRegRetryCnt * 10000));
        }
    }

    private boolean registerGameCallback(String packageName, HwAppQoeGameCallback mGameCallback) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeString(packageName);
        data.writeStrongBinder(mGameCallback);
        IAwareSdkCore.handleEvent(4, data, reply);
        int ret = reply.readInt();
        reply.recycle();
        data.recycle();
        return ret > 0;
    }

    public static void sentNotificationToStm(HwAppStateInfo monitorApp, int action) {
        if (sHandler == null || monitorApp == null) {
            HwAppQoeUtils.logD(TAG, false, "sentNotificationToSTM, stmHandler is null", new Object[0]);
            return;
        }
        HwAppQoeUtils.logD(TAG, false, "sentNotificationToSTM: %{public}s, action:%{public}d", monitorApp.toString(), Integer.valueOf(action));
        HwAppStateInfo tempAPPStateInfo = new HwAppStateInfo();
        tempAPPStateInfo.copyObjectValue(monitorApp);
        sHandler.sendMessage(sHandler.obtainMessage(action, tempAPPStateInfo));
    }
}
