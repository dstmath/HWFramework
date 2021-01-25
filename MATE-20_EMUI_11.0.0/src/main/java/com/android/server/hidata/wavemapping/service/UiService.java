package com.android.server.hidata.wavemapping.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.android.server.hidata.wavemapping.cons.ContextManager;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.entity.UiInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;

public class UiService {
    private static final String LOCATION_KEY = "LOCATION";
    public static final String MSG_FWK = "WAVEMAPPING.FWK.MSG";
    public static final String MSG_UI_1 = "WAVEMAPPING.UI.MSG.isAddWmpRegularPlace";
    public static final String MSG_UI_2 = "WAVEMAPPING.UI.MSG.isForceTrainModel";
    public static final String MSG_UI_4 = "WAVEMAPPING.UI.MSG.isLeaveAction";
    private static final String SSID_KEY = "ssid";
    private static final String TAG = ("WMapping." + UiService.class.getSimpleName());
    private static final String UI_INFO_KEY = "uiInfo";
    public static final String WAVEMAPPING_BROADCATST_PERMISSION = "com.huawei.hidata.permission.BROADCAST_WAVEMAPPING";
    private static UiInfo uiInfo = new UiInfo();
    private IntentFilter filter;
    private ParameterInfo param = ParamManager.getInstance().getParameterInfo();
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        /* class com.android.server.hidata.wavemapping.service.UiService.AnonymousClass1 */
        public String ssid;

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action;
            if (intent != null && (action = intent.getAction()) != null) {
                char c = 65535;
                int hashCode = action.hashCode();
                if (hashCode != -791682084) {
                    if (hashCode != -439425963) {
                        if (hashCode == -186521644 && action.equals(UiService.MSG_UI_2)) {
                            c = 1;
                        }
                    } else if (action.equals(UiService.MSG_UI_4)) {
                        c = 2;
                    }
                } else if (action.equals(UiService.MSG_UI_1)) {
                    c = 0;
                }
                if (c == 0) {
                    if (intent.getExtras() != null) {
                        this.ssid = intent.getExtras().getString(UiService.SSID_KEY);
                    }
                    String str = this.ssid;
                    if (str != null) {
                        UiService.this.isAddWmpRegularPlace(str);
                    }
                } else if (c == 1) {
                    UiService.this.isForceTrainModel();
                } else if (c == 2) {
                    UiService.this.isLeaveAction();
                }
            }
        }
    };
    private Handler wmStateMachineHandler;

    public UiService() {
    }

    public UiService(Handler smHandler) {
        this.wmStateMachineHandler = smHandler;
        this.filter = new IntentFilter();
        this.filter.addAction(MSG_UI_1);
        this.filter.addAction(MSG_UI_2);
        this.filter.addAction(MSG_UI_4);
        ContextManager.getInstance().getContext().registerReceiver(this.receiver, this.filter, WAVEMAPPING_BROADCATST_PERMISSION, null);
    }

    public ParameterInfo getParam() {
        return this.param;
    }

    public void setParam(ParameterInfo param2) {
        this.param = param2;
    }

    public boolean setParameter(ParameterInfo param2) {
        setParam(param2);
        return true;
    }

    public static UiInfo getUiInfo() {
        return uiInfo;
    }

    public boolean isAddWmpRegularPlace(String place) {
        LogUtil.i(false, "isAddWmpRegularPlace :%{private}s", place);
        Message msg = Message.obtain(this.wmStateMachineHandler, 20);
        Bundle bundle = new Bundle();
        bundle.putCharSequence(LOCATION_KEY, place);
        msg.setData(bundle);
        this.wmStateMachineHandler.sendMessage(msg);
        return true;
    }

    public boolean isForceTrainModel() {
        this.wmStateMachineHandler.sendEmptyMessage(60);
        return true;
    }

    public boolean isLeaveAction() {
        this.wmStateMachineHandler.sendEmptyMessage(55);
        return true;
    }

    public static void sendMsgToUi() {
        if (LogUtil.getDebugFlag()) {
            sendBroadcast(MSG_FWK, uiInfo.toJsonStr());
        }
    }

    public static void sendBroadcast(String action, String uiInfo2) {
        Intent intent = null;
        if (((action.hashCode() == -1061096524 && action.equals(MSG_FWK)) ? (char) 0 : 65535) == 0) {
            intent = new Intent(MSG_FWK);
            Bundle bundle3 = new Bundle();
            bundle3.putString(UI_INFO_KEY, uiInfo2);
            intent.putExtras(bundle3);
        }
        LogUtil.i(false, "%{public}s", uiInfo2);
        Context context = ContextManager.getInstance().getContext();
        if (context != null && intent != null) {
            context.sendBroadcast(intent, WAVEMAPPING_BROADCATST_PERMISSION);
        }
    }
}
