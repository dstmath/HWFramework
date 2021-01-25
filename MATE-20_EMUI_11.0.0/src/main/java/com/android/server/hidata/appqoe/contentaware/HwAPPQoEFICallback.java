package com.android.server.hidata.appqoe.contentaware;

import android.content.Context;
import android.emcom.IListenDataCallback;
import android.os.RemoteException;
import com.android.server.hidata.appqoe.HwAPPQoEGameCallback;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import huawei.android.net.hwmplink.MpLinkCommonUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class HwAPPQoEFICallback extends IListenDataCallback.Stub {
    private static final String FI_GAME_INDEX_PKNAME = "pkgName";
    private static final String FI_GAME_INDEX_RTT = "currRtt";
    private static final String FI_GAME_INDEX_STATUS = "currAppStatus";
    private static final int FI_GAME_IN_WAR = 1;
    private static final int FI_GAME_NOT_IN_WAR = 0;
    private static final String TAG = "HiData_HwAPPQoEFICallback";
    private static HwAPPQoEFICallback mAPPQoEFICallback = null;
    private Context mContext;
    private HwAPPQoEGameCallback mGameCallback;

    private HwAPPQoEFICallback(Context context, HwAPPQoEGameCallback gameCallback) {
        this.mContext = context;
        this.mGameCallback = gameCallback;
    }

    public static synchronized HwAPPQoEFICallback createHwAPPQoEFICallback(Context context, HwAPPQoEGameCallback gameCallback) {
        HwAPPQoEFICallback hwAPPQoEFICallback;
        synchronized (HwAPPQoEFICallback.class) {
            if (mAPPQoEFICallback == null) {
                mAPPQoEFICallback = new HwAPPQoEFICallback(context, gameCallback);
            }
            hwAPPQoEFICallback = mAPPQoEFICallback;
        }
        return hwAPPQoEFICallback;
    }

    public void onListenHiComDataChanged(String sdStr) throws RemoteException {
        handleHiComCallback(sdStr);
    }

    private void handleHiComCallback(String jsonStr) {
        if (this.mContext == null || this.mGameCallback == null) {
            HwAPPQoEUtils.logE(TAG, false, "handleHiComCallback ERROR", new Object[0]);
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            String pkgName = jsonObject.optString(FI_GAME_INDEX_PKNAME, HwAPPQoEUtils.INVALID_STRING_VALUE);
            HwAPPQoEGameCallback.GameStateInfo mHiComGameStateInfo = this.mGameCallback.getGameStateByName(pkgName);
            if (mHiComGameStateInfo == null) {
                HwAPPQoEUtils.logD(TAG, false, "hiComCallBackData game not configed", new Object[0]);
            } else if (mHiComGameStateInfo.getGameSpecialInfoSources() != 2) {
                HwAPPQoEUtils.logE(TAG, false, "not FI game", new Object[0]);
            } else {
                mHiComGameStateInfo.prevScence = mHiComGameStateInfo.curScence;
                mHiComGameStateInfo.prevState = mHiComGameStateInfo.curState;
                int currAppStatus = jsonObject.optInt(FI_GAME_INDEX_STATUS, -1);
                if (currAppStatus == 1) {
                    mHiComGameStateInfo.curScence = 200002;
                } else if (currAppStatus == 0) {
                    mHiComGameStateInfo.curScence = 200001;
                } else {
                    HwAPPQoEUtils.logE(TAG, false, "AppStatus is invalid", new Object[0]);
                    return;
                }
                mHiComGameStateInfo.curRTT = jsonObject.optInt(FI_GAME_INDEX_RTT, -1);
                mHiComGameStateInfo.curState = 100;
                mHiComGameStateInfo.curUID = MpLinkCommonUtils.getAppUid(this.mContext, pkgName);
                this.mGameCallback.handleGameStateChange(mHiComGameStateInfo);
            }
        } catch (JSONException e) {
            HwAPPQoEUtils.logE(TAG, false, "JSONException: %{public}s", e.getMessage());
        }
    }
}
