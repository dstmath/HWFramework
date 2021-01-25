package com.huawei.ace.systemplugin.geolocation;

import com.huawei.ace.plugin.ErrorCode;
import com.huawei.ace.plugin.EventGroup;
import com.huawei.ace.plugin.EventNotifier;
import com.huawei.ace.plugin.Function;
import com.huawei.ace.plugin.ModuleGroup;
import com.huawei.ace.plugin.RequestCodeMapping;
import com.huawei.ace.plugin.Result;
import com.huawei.ace.systemplugin.LogUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ohos.ace.ability.AceAbility;
import ohos.agp.render.opengl.GLES20;
import ohos.app.Context;
import ohos.app.dispatcher.task.Revocable;
import ohos.location.Location;
import ohos.location.Locator;
import ohos.location.LocatorCallback;
import ohos.location.RequestParam;
import ohos.utils.fastjson.JSONObject;

public class GeolocationPlugin implements ModuleGroup.ModuleGroupHandler, EventGroup.EventGroupHandler, ErrorCode {
    private static final int DEFAULT_TIMEOUT = 30000;
    private static final String LOCATION_PERMISSION = "ohos.permission.LOCATION";
    private static final String[] LOCATION_TYPES = {"gps", "wifi"};
    private static final String TAG = "GeolocationPlugin#";
    private static GeolocationPlugin geolocationPlugin;
    private Context context;
    private EventNotifier eventNotifier = null;
    private Result eventResult = null;
    private boolean isGetLocation = false;
    private boolean isReported = false;
    private boolean isSubscribe = false;
    private LocationCallback locationCallback = new LocationCallback();
    private Locator locator = null;
    private Result moduleResult = null;
    private RequestParam request = new RequestParam((int) GLES20.GL_LEQUAL, 1, 0);
    private Revocable revocable = null;
    private int timeout = 30000;

    public static String getJsCode() {
        return "var catching = global.systemplugin.catching;var geolocation = {    moduleGroup: null,    eventGroup: null,    isSubscribe: false,    locationType: {types: ['gps','network']},    supportedCoordTypes: ['wgs84'],    getLocation: async function getLocation(param) {        if (typeof param.timeout !== 'undefined' &&            Math.floor(param.timeout) !== param.timeout) {            commonCallback(param.fail, 'fail', 'timeout is not available', 202);            commonCallback(param.complete, 'complete');            return;        }        if (typeof param.coordType !== 'undefined' && param.coordType !== 'wgs84') {            commonCallback(param.fail, 'fail', 'coordType is not available', 202);            commonCallback(param.complete, 'complete');            return;        }        if (geolocation.moduleGroup == null) {            geolocation.moduleGroup = ModuleGroup.getGroup(\"AceModuleGroup/Geolocation\");        }        return await catching(geolocation.moduleGroup.callNative(\"getLocation\", param.timeout),                   param);    },    subscribe: async function subscribe(param) {        if (typeof param.coordType !== 'undefined' && param.coordType !== 'wgs84') {            commonCallback(param.fail, 'fail', 'coordType is not an available', 202);            return;        }        if (typeof param.success !== 'function') {            commonCallback(param.fail, 'fail', 'success is not an available function', 202);            return;        }        if (geolocation.eventGroup == null) {            geolocation.eventGroup = EventGroup.getGroup(\"AceEventGroup/Geolocation\");        }        if (geolocation.isSubscribe) {            geolocation.eventGroup.unsubscribe();        }        try {            var ret = await geolocation.eventGroup.subscribe(function (callback) {                var val = JSON.parse(callback);                commonCallback(param.success, 'success', val.data);            });        } catch (pluginError) {            var pluginErrorJson = JSON.parse(pluginError);            commonCallback(param.fail, 'fail', pluginErrorJson.data, pluginErrorJson.code);        }        var val = JSON.parse(ret);        if (val.code !== 0) {            commonCallback(param.fail, 'fail', val.data, val.code);        } else {            geolocation.isSubscribe = true;        }    },    unsubscribe: async function unsubscribe() {        if (geolocation.eventGroup == null) {            return;        }        if (geolocation.isSubscribe) {            await geolocation.eventGroup.unsubscribe();            geolocation.isSubscribe = false;        }    },    getLocationType: async function getLocationType(param) {        if (geolocation.moduleGroup == null) {            geolocation.moduleGroup = ModuleGroup.getGroup(\"AceModuleGroup/Geolocation\");        }        return await catching(geolocation.moduleGroup.callNative(\"getLocationType\"), param);    },    getSupportedCoordTypes: function getSupportedCoordTypes() {        return geolocation.supportedCoordTypes;    }};global.systemplugin.geolocation = {    getLocation: geolocation.getLocation,    subscribe: geolocation.subscribe,    unsubscribe: geolocation.unsubscribe,    getLocationType: geolocation.getLocationType,    getSupportedCoordTypes: geolocation.getSupportedCoordTypes};";
    }

    @Override // com.huawei.ace.plugin.ModuleGroup.ModuleGroupHandler
    public void onFunctionCall(Function function, Result result) {
        LogUtil.info(TAG, "into onFunctionCall");
        if ("getLocationType".equals(function.name)) {
            getLocationType(result);
        } else if (!"getLocation".equals(function.name)) {
            result.notExistFunction();
            LogUtil.info(TAG, "geoLocation onFunctioncall's name is error");
        } else {
            Result result2 = this.moduleResult;
            if (result2 != null) {
                result2.error(ErrorCode.REPEAT_GETLOCATION_ERROR_CODE, "new geolocation request initiated but this request terminated");
            }
            if (function.arguments.size() <= 0) {
                this.timeout = 30000;
            } else if (function.arguments.get(0) instanceof Integer) {
                this.timeout = ((Integer) function.arguments.get(0)).intValue();
                if (this.timeout <= 0) {
                    this.timeout = 30000;
                }
            } else {
                result.error(202, "value is not an available integer");
                return;
            }
            this.moduleResult = result;
            if (this.context.verifySelfPermission(LOCATION_PERMISSION) != 0) {
                LogUtil.info(TAG, "permission has got");
                if (this.context.canRequestPermission(LOCATION_PERMISSION)) {
                    this.context.requestPermissionsFromUser(new String[]{LOCATION_PERMISSION}, RequestCodeMapping.GEOLOCATIONSTART.getCode());
                } else {
                    result.error(602, "user rejects the perssion request and forbids to again");
                }
            } else {
                getLocation();
            }
        }
    }

    private void getLocationType(Result result) {
        LogUtil.info(TAG, "into getLocationType");
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("types", LOCATION_TYPES);
        result.success(jSONObject);
    }

    private void getLocation() {
        LogUtil.info(TAG, "into getLocation");
        this.isReported = false;
        Revocable revocable2 = this.revocable;
        if (revocable2 != null) {
            revocable2.revoke();
        }
        if (!this.locator.isLocationSwitchOn()) {
            this.moduleResult.error(ErrorCode.LOCATION_SWITCH_CLOSE_ERROR_CODE, "location switch is close");
            return;
        }
        this.revocable = this.context.getUITaskDispatcher().delayDispatch(new Runnable() {
            /* class com.huawei.ace.systemplugin.geolocation.$$Lambda$GeolocationPlugin$gtbnBA7u1ZoaE96IMYb7UFwyu6c */

            @Override // java.lang.Runnable
            public final void run() {
                GeolocationPlugin.this.lambda$getLocation$0$GeolocationPlugin();
            }
        }, (long) this.timeout);
        if (!this.isGetLocation && !this.isSubscribe) {
            this.locator.startLocating(this.request, this.locationCallback);
        }
        this.isGetLocation = true;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkIsReported */
    public void lambda$getLocation$0$GeolocationPlugin() {
        if (!this.isReported) {
            this.moduleResult.error(ErrorCode.TIMEOUT_ERROR_CODE, "timeout but not reported");
            this.moduleResult = null;
            this.isGetLocation = false;
            if (!this.isSubscribe) {
                this.locator.stopLocating(this.locationCallback);
            }
        }
    }

    @Override // com.huawei.ace.plugin.EventGroup.EventGroupHandler
    public void onSubscribe(List<Object> list, EventNotifier eventNotifier2, Result result) {
        LogUtil.info(TAG, "into onSubscribe");
        this.eventResult = result;
        this.eventNotifier = eventNotifier2;
        if (this.context.verifySelfPermission(LOCATION_PERMISSION) == 0) {
            subscribe();
        } else if (this.context.canRequestPermission(LOCATION_PERMISSION)) {
            this.context.requestPermissionsFromUser(new String[]{LOCATION_PERMISSION}, RequestCodeMapping.GEOLOCATIONSTART.getCode());
        } else {
            result.error(602, "user rejects the perssion request and forbids to again");
        }
    }

    @Override // com.huawei.ace.plugin.EventGroup.EventGroupHandler
    public void onUnsubscribe(List<Object> list, Result result) {
        LogUtil.info(TAG, "into onUnSubscribe");
        if (!this.isGetLocation) {
            this.locator.stopLocating(this.locationCallback);
        }
        this.isSubscribe = false;
    }

    private void subscribe() {
        LogUtil.info(TAG, "into subscribe");
        if (!this.locator.isLocationSwitchOn()) {
            this.eventResult.error(ErrorCode.LOCATION_SWITCH_CLOSE_ERROR_CODE, "location switch is close");
            return;
        }
        if (!this.isGetLocation && !this.isSubscribe) {
            this.locator.startLocating(this.request, this.locationCallback);
        }
        this.isSubscribe = true;
        this.eventResult.success("subscribe success");
    }

    public static void register(Context context2) {
        LogUtil.info(TAG, "register");
        geolocationPlugin = new GeolocationPlugin();
        geolocationPlugin.onRegister(context2);
        Integer valueOf = context2 instanceof AceAbility ? Integer.valueOf(((AceAbility) context2).getAbilityId()) : null;
        ModuleGroup.registerModuleGroup("AceModuleGroup/Geolocation", geolocationPlugin, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/Geolocation", geolocationPlugin, valueOf);
    }

    public static void deregister(Context context2) {
        Integer valueOf = context2 instanceof AceAbility ? Integer.valueOf(((AceAbility) context2).getAbilityId()) : null;
        ModuleGroup.registerModuleGroup("AceModuleGroup/Geolocation", null, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/Geolocation", null, valueOf);
    }

    private void onRegister(Context context2) {
        LogUtil.info(TAG, "onRegister");
        this.context = context2;
        this.locator = new Locator(context2);
    }

    public static Set<String> getPluginGroup() {
        HashSet hashSet = new HashSet();
        hashSet.add("AceModuleGroup/Geolocation");
        hashSet.add("AceEventGroup/Geolocation");
        return hashSet;
    }

    /* access modifiers changed from: private */
    public class LocationCallback implements LocatorCallback {
        public void onErrorReport(int i) {
        }

        public void onStatusChanged(int i) {
        }

        public LocationCallback() {
        }

        public void onLocationReport(Location location) {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("longitude", Double.valueOf(location.getLongitude()));
            jSONObject.put("latitude", Double.valueOf(location.getLatitude()));
            jSONObject.put("accuracy", Float.valueOf(location.getAccuracy()));
            jSONObject.put("time", Long.valueOf(location.getTimeStamp()));
            if (GeolocationPlugin.this.isGetLocation) {
                GeolocationPlugin.this.moduleResult.success(jSONObject);
                GeolocationPlugin.this.moduleResult = null;
                GeolocationPlugin.this.isReported = true;
                GeolocationPlugin.this.isGetLocation = false;
            }
            if (GeolocationPlugin.this.isSubscribe) {
                GeolocationPlugin.this.eventNotifier.success(jSONObject);
            }
            if (!GeolocationPlugin.this.isGetLocation && !GeolocationPlugin.this.isSubscribe) {
                GeolocationPlugin.this.locator.stopLocating(GeolocationPlugin.this.locationCallback);
            }
        }
    }

    public static void permissionResult(int[] iArr) {
        if (iArr.length <= 0 || iArr[0] != 0) {
            LogUtil.info(TAG, "permission denied");
            GeolocationPlugin geolocationPlugin2 = geolocationPlugin;
            Result result = geolocationPlugin2.moduleResult;
            if (result != null) {
                result.error(601, "user rejects the perssion request");
            } else {
                geolocationPlugin2.eventResult.error(601, "user rejects the perssion request");
            }
        } else {
            LogUtil.info(TAG, "permission approved");
            GeolocationPlugin geolocationPlugin3 = geolocationPlugin;
            if (geolocationPlugin3.moduleResult != null) {
                geolocationPlugin3.getLocation();
            } else {
                geolocationPlugin3.subscribe();
            }
        }
    }
}
