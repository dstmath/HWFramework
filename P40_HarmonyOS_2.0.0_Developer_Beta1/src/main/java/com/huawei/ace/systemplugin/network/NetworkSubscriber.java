package com.huawei.ace.systemplugin.network;

import com.huawei.ace.plugin.EventNotifier;
import ohos.app.Context;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.net.NetManager;
import ohos.telephony.CellularDataInfoManager;
import ohos.telephony.RadioInfoManager;
import ohos.utils.fastjson.JSONObject;
import ohos.wifi.WifiDevice;

public class NetworkSubscriber extends CommonEventSubscriber {
    private static final String CDMA_IDENTIFICATION_CODE = "3g";
    private static final String GSM_IDENTIFICATION_CODE = "2g";
    private static final String LOG_TAG = "NetworkPlugin#";
    private static final String LTE_IDENTIFICATION_CODE = "4g";
    private static final String UNKNOWN_IDENTIFICATION_CODE = "none";
    private static final String WLAN_IDENTIFICATION_CODE = "wifi";
    private final Context context;
    private boolean directCall = false;
    private EventNotifier eventNotifier = null;
    private boolean isMetered = false;
    private String networkTypeResult = UNKNOWN_IDENTIFICATION_CODE;

    public NetworkSubscriber(CommonEventSubscribeInfo commonEventSubscribeInfo, Context context2) {
        super(commonEventSubscribeInfo);
        this.context = context2;
    }

    public void setNotifier(EventNotifier eventNotifier2) {
        this.eventNotifier = eventNotifier2;
    }

    public EventNotifier getNotifier() {
        return this.eventNotifier;
    }

    public String getNetworkType() {
        if (this.networkTypeResult.equals(UNKNOWN_IDENTIFICATION_CODE) && !this.directCall) {
            this.directCall = true;
            this.isMetered = isNetMetered(this.context);
            this.networkTypeResult = getNetworkType(this.context);
        }
        return this.networkTypeResult;
    }

    public String getNetworkTypeResult() {
        return this.networkTypeResult;
    }

    public boolean isMetered() {
        return this.isMetered;
    }

    @Override // ohos.event.commonevent.CommonEventSubscriber
    public void onReceiveEvent(CommonEventData commonEventData) {
        this.networkTypeResult = getNetworkType(this.context);
        this.isMetered = isNetMetered(this.context);
        if (this.eventNotifier != null) {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("type", this.networkTypeResult);
            jSONObject.put("metered", Boolean.valueOf(this.isMetered));
            this.eventNotifier.success(jSONObject);
        }
    }

    private String getNetworkType(Context context2) {
        WifiDevice instance = WifiDevice.getInstance(context2);
        if (!(instance == null || CellularDataInfoManager.getInstance(context2) == null || RadioInfoManager.getInstance(context2) == null)) {
            if (instance.isWifiActive() && instance.isConnected()) {
                return WLAN_IDENTIFICATION_CODE;
            }
            switch (RadioInfoManager.getInstance(context2).getRadioTech(CellularDataInfoManager.getInstance(context2).getDefaultCellularDataSlotId())) {
                case 0:
                    break;
                case 1:
                case 2:
                    return GSM_IDENTIFICATION_CODE;
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    return CDMA_IDENTIFICATION_CODE;
                case 9:
                case 10:
                    return LTE_IDENTIFICATION_CODE;
                default:
                    return UNKNOWN_IDENTIFICATION_CODE;
            }
        }
        return UNKNOWN_IDENTIFICATION_CODE;
    }

    private boolean isNetMetered(Context context2) {
        return NetManager.getInstance(context2) != null && NetManager.getInstance(context2).isDefaultNetMetered();
    }
}
