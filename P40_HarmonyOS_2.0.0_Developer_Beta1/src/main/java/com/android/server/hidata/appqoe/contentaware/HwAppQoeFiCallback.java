package com.android.server.hidata.appqoe.contentaware;

import android.content.Context;
import android.emcom.IListenDataCallback;
import android.os.RemoteException;
import com.android.server.hidata.appqoe.HwAppQoeGameCallback;
import com.android.server.hidata.appqoe.HwAppQoeUtils;
import huawei.android.net.hwmplink.MpLinkCommonUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class HwAppQoeFiCallback extends IListenDataCallback.Stub {
    private static final String FI_GAME_INDEX_PKNAME = "pkgName";
    private static final String FI_GAME_INDEX_RTT = "currRtt";
    private static final String FI_GAME_INDEX_STATUS = "currAppStatus";
    private static final int FI_GAME_IN_WAR = 1;
    private static final int FI_GAME_NOT_IN_WAR = 0;
    private static final String TAG = "HiData_HwAPPQoEFICallback";
    private static HwAppQoeFiCallback sAppQoeFiCallback = null;
    private Context mContext;
    private HwAppQoeGameCallback mGameCallback;

    private HwAppQoeFiCallback(Context context, HwAppQoeGameCallback gameCallback) {
        this.mContext = context;
        this.mGameCallback = gameCallback;
    }

    public static synchronized HwAppQoeFiCallback createHwAppQoeFiCallback(Context context, HwAppQoeGameCallback gameCallback) {
        HwAppQoeFiCallback hwAppQoeFiCallback;
        synchronized (HwAppQoeFiCallback.class) {
            if (sAppQoeFiCallback == null) {
                sAppQoeFiCallback = new HwAppQoeFiCallback(context, gameCallback);
            }
            hwAppQoeFiCallback = sAppQoeFiCallback;
        }
        return hwAppQoeFiCallback;
    }

    public void onListenHiComDataChanged(String sdStr) throws RemoteException {
        handleHiComCallback(sdStr);
    }

    private void handleHiComCallback(String jsonStr) {
        if (this.mContext == null || this.mGameCallback == null) {
            HwAppQoeUtils.logE(TAG, false, "handleHiComCallback ERROR", new Object[0]);
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            String pkgName = jsonObject.optString(FI_GAME_INDEX_PKNAME, HwAppQoeUtils.INVALID_STRING_VALUE);
            HwAppQoeGameCallback.GameStateInfo mHiComGameStateInfo = this.mGameCallback.getGameStateByName(pkgName);
            if (mHiComGameStateInfo == null) {
                HwAppQoeUtils.logD(TAG, false, "hiComCallBackData game not configed", new Object[0]);
            } else if (mHiComGameStateInfo.getGameSpecialInfoSources() != 2) {
                HwAppQoeUtils.logE(TAG, false, "not FI game", new Object[0]);
            } else {
                mHiComGameStateInfo.prevScenes = mHiComGameStateInfo.curScenes;
                mHiComGameStateInfo.prevState = mHiComGameStateInfo.curState;
                int currAppStatus = jsonObject.optInt(FI_GAME_INDEX_STATUS, -1);
                if (currAppStatus == 1) {
                    mHiComGameStateInfo.curScenes = 200002;
                } else if (currAppStatus == 0) {
                    mHiComGameStateInfo.curScenes = 200001;
                } else {
                    HwAppQoeUtils.logE(TAG, false, "AppStatus is invalid", new Object[0]);
                    return;
                }
                mHiComGameStateInfo.curRtt = jsonObject.optInt(FI_GAME_INDEX_RTT, -1);
                mHiComGameStateInfo.curState = 100;
                mHiComGameStateInfo.curUid = MpLinkCommonUtils.getAppUid(this.mContext, pkgName);
                this.mGameCallback.handleGameStateChange(mHiComGameStateInfo);
            }
        } catch (JSONException e) {
            HwAppQoeUtils.logE(TAG, false, "JSONException: %{public}s", e.getMessage());
        }
    }
}
