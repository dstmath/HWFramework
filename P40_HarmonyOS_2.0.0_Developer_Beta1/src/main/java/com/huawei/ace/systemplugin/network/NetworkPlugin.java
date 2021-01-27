package com.huawei.ace.systemplugin.network;

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
import ohos.event.commonevent.CommonEventManager;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.MatchingSkills;
import ohos.rpc.RemoteException;
import ohos.utils.fastjson.JSONObject;

public class NetworkPlugin implements ModuleGroup.ModuleGroupHandler, EventGroup.EventGroupHandler {
    private static final String ACCESS_NETWORK_PERMISSION = "ohos.permission.GET_NETWORK_INFO";
    private static final String ACCESS_WIFI_PERMISSION = "ohos.permission.GET_WIFI_INFO";
    private static final String COMMON_EVENT_CONNECTIVITY_CHANGE = "usual.event.CONNECTIVITY_CHANGE";
    private static final String LACK_OF_AUTHORITY_ERROR = "lack of permissions error";
    private static final String LOG_TAG = "NetworkPlugin#";
    private static final String SUBSCRIBE_FAILURE = "Network type subscribe failed";
    private static final String SUBSCRIBE_SUCCESS = "Network type subscribe success";
    private static final String UNSUBSCRIBE_FAILURE = "Network type unsubscribe failed";
    private static final String UNSUBSCRIBE_SUCCESS = "Network type unsubscribe success";
    private static NetworkPlugin networkPlugin;
    private Context applicationContext;
    private NetworkSubscriber commonEventSubscriber;
    private EventNotifier eventNotifier;
    private boolean hasCheckPermission = false;
    private boolean hasPermission = false;
    private boolean isSubscribeSuccess = false;

    public static String getJsCode() {
        return "var catching = global.systemplugin.catching;var network = {    networkModuleGroup: null,    networkEventGroup: null,    isSubscribe: false,    onModuleInit: function onModuleInit() {        if (network.networkModuleGroup == null) {            network.networkModuleGroup = ModuleGroup.getGroup(\"AceModuleGroup/Network\");        }    },    onEventInit: function onEventInit() {        if (network.networkEventGroup == null) {            network.networkEventGroup = EventGroup.getGroup(\"AceEventGroup/Network\");        }    },    getType: async function getType(param) {        network.onModuleInit();        return await catching(network.networkModuleGroup.callNative(\"getNetworkType\"), param);    },    subscribe: async function subscribe(param) {        network.onEventInit();        try {            if (network.isSubscribe) {                await network.networkEventGroup.unsubscribe();            }            var result = await network.networkEventGroup.subscribe(function (networkInfo) {                var networkInfoObj = JSON.parse(networkInfo);                commonCallback(param.success, 'success', networkInfoObj.data);            });            var retJson = JSON.parse(result);            if (retJson.code == 0) {                network.isSubscribe = true;            } else {                commonCallback(param.fail, 'fail', retJson.data, retJson.code);            }        } catch (pluginError) {            var pluginErrorJson = JSON.parse(pluginError);            commonCallback(param.fail, 'fail', pluginErrorJson.data, pluginErrorJson.code);        }    },    unsubscribe: async function unsubscribe() {        network.onEventInit();        if (network.isSubscribe) {            var result = await network.networkEventGroup.unsubscribe();            var retJson = JSON.parse(result);            if (retJson.code == 0) {                network.isSubscribe = false;            } else {                console.error('Network unsubscribe error, result:' + result);            }        }    }};global.systemplugin.network = network;";
    }

    public static void register(Context context) {
        networkPlugin = new NetworkPlugin();
        networkPlugin.onRegister(context);
        Integer valueOf = context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null;
        ModuleGroup.registerModuleGroup("AceModuleGroup/Network", networkPlugin, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/Network", networkPlugin, valueOf);
    }

    public static void deregister(Context context) {
        networkPlugin.onDeregister(context);
        Integer valueOf = context instanceof AceAbility ? Integer.valueOf(((AceAbility) context).getAbilityId()) : null;
        ModuleGroup.registerModuleGroup("AceModuleGroup/Network", null, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/Network", null, valueOf);
    }

    private void onRegister(Context context) {
        this.applicationContext = context;
        if (hasPremission()) {
            MatchingSkills matchingSkills = new MatchingSkills();
            matchingSkills.addEvent("usual.event.CONNECTIVITY_CHANGE");
            this.commonEventSubscriber = new NetworkSubscriber(new CommonEventSubscribeInfo(matchingSkills), context);
            try {
                CommonEventManager.subscribeCommonEvent(this.commonEventSubscriber);
                this.isSubscribeSuccess = true;
            } catch (RemoteException unused) {
                this.isSubscribeSuccess = false;
                LogUtil.error(LOG_TAG, "subscribeCommonEvent error");
            }
        }
    }

    private void onDeregister(Context context) {
        try {
            CommonEventManager.unsubscribeCommonEvent(this.commonEventSubscriber);
        } catch (RemoteException unused) {
            LogUtil.error(LOG_TAG, "unsubscribeCommonEvent error");
        }
        this.commonEventSubscriber = null;
    }

    public static Set<String> getPluginGroup() {
        HashSet hashSet = new HashSet();
        hashSet.add("AceModuleGroup/Network");
        hashSet.add("AceEventGroup/Network");
        return hashSet;
    }

    private boolean hasPremission() {
        if (!this.hasCheckPermission) {
            this.hasPermission = this.applicationContext.verifySelfPermission(ACCESS_NETWORK_PERMISSION) == 0 && this.applicationContext.verifySelfPermission(ACCESS_WIFI_PERMISSION) == 0;
            this.hasCheckPermission = true;
        }
        return this.hasPermission;
    }

    @Override // com.huawei.ace.plugin.ModuleGroup.ModuleGroupHandler
    public void onFunctionCall(Function function, Result result) {
        if (!"getNetworkType".equals(function.name)) {
            result.notExistFunction();
        } else if (!hasPremission()) {
            result.error(602, LACK_OF_AUTHORITY_ERROR);
        } else {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("type", this.commonEventSubscriber.getNetworkType());
            jSONObject.put("metered", Boolean.valueOf(this.commonEventSubscriber.isMetered()));
            result.success(jSONObject);
        }
    }

    @Override // com.huawei.ace.plugin.EventGroup.EventGroupHandler
    public void onSubscribe(List<Object> list, EventNotifier eventNotifier2, Result result) {
        if (!hasPremission()) {
            result.error(602, LACK_OF_AUTHORITY_ERROR);
            return;
        }
        this.commonEventSubscriber.setNotifier(eventNotifier2);
        if (this.isSubscribeSuccess) {
            result.success(SUBSCRIBE_SUCCESS);
        } else {
            result.error(200, SUBSCRIBE_FAILURE);
        }
    }

    @Override // com.huawei.ace.plugin.EventGroup.EventGroupHandler
    public void onUnsubscribe(List<Object> list, Result result) {
        if (this.commonEventSubscriber.getNotifier() != null) {
            this.commonEventSubscriber.setNotifier(null);
            result.success(UNSUBSCRIBE_SUCCESS);
            return;
        }
        LogUtil.warn(LOG_TAG, "event notifier is not exist");
        result.success(UNSUBSCRIBE_SUCCESS);
    }
}
