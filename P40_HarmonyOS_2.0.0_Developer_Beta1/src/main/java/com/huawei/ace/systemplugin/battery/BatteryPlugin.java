package com.huawei.ace.systemplugin.battery;

import com.huawei.ace.plugin.ErrorCode;
import com.huawei.ace.plugin.EventGroup;
import com.huawei.ace.plugin.EventNotifier;
import com.huawei.ace.plugin.Function;
import com.huawei.ace.plugin.ModuleGroup;
import com.huawei.ace.plugin.Result;
import com.huawei.ace.systemplugin.LogUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ohos.ace.ability.AceAbility;
import ohos.app.Context;
import ohos.batterymanager.BatteryInfo;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.event.commonevent.CommonEventSupport;
import ohos.event.commonevent.MatchingSkills;
import ohos.rpc.RemoteException;
import ohos.utils.fastjson.JSONObject;

public class BatteryPlugin implements ModuleGroup.ModuleGroupHandler, EventGroup.EventGroupHandler, ErrorCode {
    private static final String LEVEL_NOT_AVAILABLE = "Battery level is not available";
    private static final String LOG_TAG = "BatteryPlugin#";
    private static final String SUBSCRIBE_FAILURE = "Battery subscribe failed";
    private static final String SUBSCRIBE_SUCCESS = "Battery subscribe success";
    private static final String UNSUBSCRIBE_FAILURE = "Battery unsubscribe failed";
    private static final String UNSUBSCRIBE_SUCCESS = "Battery unsubscribe success";
    private static BatteryPlugin instance;
    private Context applicationContext;
    private CommonEventSubscriber commonEventSubscriber;
    private EventNotifier eventNotifier;

    public static String getJsCode() {
        return "var catching = global.systemplugin.catching;var battery = {    batteryModuleGroup: null,    onInit: function onInit() {        if (battery.batteryModuleGroup == null) {            battery.batteryModuleGroup = ModuleGroup.getGroup(\"AceModuleGroup/Battery\");        }    },    getStatus: async function getStatus(param) {        console.log('into battery getStatus');        battery.onInit();        return await catching(battery.batteryModuleGroup.callNative(\"getBatteryLevel\"), param);    }};global.systemplugin.battery = battery;";
    }

    public static void register(Context context) {
        instance = new BatteryPlugin();
        instance.onRegister(context);
        Integer valueOf = context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null;
        ModuleGroup.registerModuleGroup("AceModuleGroup/Battery", instance, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/Battery", instance, valueOf);
    }

    public static void deregister(Context context) {
        Integer valueOf = context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null;
        ModuleGroup.registerModuleGroup("AceModuleGroup/Battery", null, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/Battery", null, valueOf);
    }

    public static Set<String> getPluginGroup() {
        HashSet hashSet = new HashSet();
        hashSet.add("AceModuleGroup/Battery");
        hashSet.add("AceEventGroup/Battery");
        return hashSet;
    }

    private void onRegister(Context context) {
        this.applicationContext = context;
    }

    @Override // com.huawei.ace.plugin.ModuleGroup.ModuleGroupHandler
    public void onFunctionCall(Function function, Result result) {
        if (function == null || function.name == null) {
            result.error(200, "param is invalidate!");
        } else if ("getBatteryLevel".equals(function.name)) {
            int batteryLevel = getBatteryLevel();
            if (batteryLevel != -1) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("level", Double.valueOf(((double) batteryLevel) / 100.0d));
                jSONObject.put("charging", Boolean.valueOf(getChargingStatus()));
                result.success(jSONObject);
                return;
            }
            result.error(200, LEVEL_NOT_AVAILABLE);
        } else {
            result.notExistFunction();
        }
    }

    private static class BatterySubscriber extends CommonEventSubscriber {
        private final EventNotifier eventNotifier;

        public BatterySubscriber(CommonEventSubscribeInfo commonEventSubscribeInfo, EventNotifier eventNotifier2) {
            super(commonEventSubscribeInfo);
            this.eventNotifier = eventNotifier2;
        }

        @Override // ohos.event.commonevent.CommonEventSubscriber
        public void onReceiveEvent(CommonEventData commonEventData) {
            this.eventNotifier.success(Double.valueOf(((double) new BatteryInfo().getCapacity()) / 100.0d));
        }
    }

    @Override // com.huawei.ace.plugin.EventGroup.EventGroupHandler
    public void onSubscribe(List<Object> list, EventNotifier eventNotifier2, Result result) {
        LogUtil.debug(LOG_TAG, "onSubscribe");
        this.eventNotifier = eventNotifier2;
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.addEvent(CommonEventSupport.COMMON_EVENT_BATTERY_CHANGED);
        this.commonEventSubscriber = new BatterySubscriber(new CommonEventSubscribeInfo(matchingSkills), eventNotifier2);
        try {
            CommonEventManager.subscribeCommonEvent(this.commonEventSubscriber);
            result.success(SUBSCRIBE_SUCCESS);
        } catch (RemoteException unused) {
            result.error(200, SUBSCRIBE_FAILURE);
            LogUtil.error(LOG_TAG, "subscribe error!");
        }
    }

    @Override // com.huawei.ace.plugin.EventGroup.EventGroupHandler
    public void onUnsubscribe(List<Object> list, Result result) {
        LogUtil.debug(LOG_TAG, "onUnsubscribe");
        try {
            CommonEventManager.unsubscribeCommonEvent(this.commonEventSubscriber);
            result.success(UNSUBSCRIBE_SUCCESS);
        } catch (RemoteException unused) {
            result.error(200, UNSUBSCRIBE_FAILURE);
            LogUtil.error(LOG_TAG, "unsubscribe error!");
        }
        this.commonEventSubscriber = null;
    }

    private int getBatteryLevel() {
        return new BatteryInfo().getCapacity();
    }

    private boolean getChargingStatus() {
        BatteryInfo.BatteryChargeState chargingStatus = new BatteryInfo().getChargingStatus();
        return chargingStatus == BatteryInfo.BatteryChargeState.ENABLE || chargingStatus == BatteryInfo.BatteryChargeState.FULL;
    }
}
