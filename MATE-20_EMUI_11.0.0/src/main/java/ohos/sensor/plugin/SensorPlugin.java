package ohos.sensor.plugin;

import com.huawei.ace.plugin.EventGroup;
import com.huawei.ace.plugin.EventNotifier;
import com.huawei.ace.plugin.Function;
import com.huawei.ace.plugin.ModuleGroup;
import com.huawei.ace.plugin.Result;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentFilter;
import ohos.ace.ability.AceAbility;
import ohos.app.Context;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.sensor.agent.CategoryBodyAgent;
import ohos.sensor.agent.CategoryEnvironmentAgent;
import ohos.sensor.agent.CategoryLightAgent;
import ohos.sensor.agent.CategoryMotionAgent;
import ohos.sensor.agent.CategoryOrientationAgent;
import ohos.sensor.bean.CategoryBody;
import ohos.sensor.bean.CategoryEnvironment;
import ohos.sensor.bean.CategoryLight;
import ohos.sensor.bean.CategoryMotion;
import ohos.sensor.bean.CategoryOrientation;
import ohos.sensor.data.CategoryBodyData;
import ohos.sensor.data.CategoryEnvironmentData;
import ohos.sensor.data.CategoryLightData;
import ohos.sensor.data.CategoryMotionData;
import ohos.sensor.data.CategoryOrientationData;
import ohos.sensor.listener.ICategoryBodyDataCallback;
import ohos.sensor.listener.ICategoryEnvironmentDataCallback;
import ohos.sensor.listener.ICategoryLightDataCallback;
import ohos.sensor.listener.ICategoryMotionDataCallback;
import ohos.sensor.listener.ICategoryOrientationDataCallback;
import ohos.utils.fastjson.JSONObject;

public class SensorPlugin extends AceAbility implements ModuleGroup.ModuleGroupHandler, EventGroup.EventGroupHandler {
    private static final float BODY_STATE_EXPECT_DATA = 0.0f;
    private static final int DOMAIN_ID = 218113829;
    private static final String EVENT_ACCELEROMETER_DATA_CHANGED = "event.sensor.ACCELEROMETER_DATA_CHANGED";
    private static final String EVENT_BAROMETER_DATA_CHANGED = "event.sensor.BAROMETER_DATA_CHANGED";
    private static final String EVENT_BODY_STATE_DATA_CHANGED = "event.sensor.BODY_STATE_DATA_CHANGED";
    private static final String EVENT_COMPASS_DATA_CHANGED = "event.sensor.COMPASS_DATA_CHANGED";
    private static final String EVENT_HEART_RATE_DATA_CHANGED = "event.sensor.HEART_RATE_DATA_CHANGED";
    private static final String EVENT_LIGHT_DATA_CHANGED = "event.sensor.LIGHT_DATA_CHANGED";
    private static final String EVENT_PROXIMITY_DATA_CHANGED = "event.sensor.PROXIMITY_DATA_CHANGED";
    private static final String EVENT_STEP_COUNTER_DATA_CHANGED = "event.sensor.STEP_COUNTER_DATA_CHANGED";
    private static final int FORBID_PERMISSION_ERROR_CODE = 602;
    private static final String HEART_RATE_PERMISSION = "ohos.permission.READ_HEALTH_DATA";
    private static final int HEART_RATE_REQUEST_CODE = 2;
    private static final int INPUT_ERROR_CODE = 202;
    private static final int INPUT_PARAMETER_INDEX = 0;
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, DOMAIN_ID, "SensorLevel");
    private static final String LOG_FORMAT = "%{public}s: %{public}s";
    private static final int SENSOR_ACCELEROMETER = 0;
    private static final int SENSOR_BAROMETER = 5;
    private static final int SENSOR_BODY_STATE = 7;
    private static final int SENSOR_COMPASS = 1;
    private static final int SENSOR_DATA_INDEX_ONE = 0;
    private static final int SENSOR_DATA_INDEX_THREE = 2;
    private static final int SENSOR_DATA_INDEX_TWO = 1;
    private static final int SENSOR_HEART_RATE = 6;
    private static final int SENSOR_LIGHT = 3;
    private static final int SENSOR_NOT_EXIST_ERROR_CODE = 900;
    private static final int SENSOR_PROXIMITY = 2;
    private static final int SENSOR_STEP_COUNTER = 4;
    private static final int SENSOR_SUBSCRIBE_FAILURE = 1001;
    private static final int SENSOR_TYPE_INDEX = 1;
    private static final int SENSOR_UNSUBSCRIBE_FAILURE = 1002;
    private static final String STEP_COUNTER_PERMISSION = "ohos.permission.ACTIVITY_MOTION";
    private static final int STEP_COUNTER_REQUEST_CODE = 1;
    private static final String TAG = "SensorPlugin#";
    private static final float THRESHOLD = 1.0E-6f;
    private static SensorPlugin instance;
    private CategoryMotion accelerometer;
    private CategoryMotionAgent accelerometerAgent = new CategoryMotionAgent();
    private ICategoryMotionDataCallback accelerometerDataCallback;
    private CommonEventSubscriber accelerometerEventSubscriber;
    private CategoryEnvironment barometer;
    private CategoryEnvironmentAgent barometerAgent = new CategoryEnvironmentAgent();
    private ICategoryEnvironmentDataCallback barometerDataCallback;
    private CommonEventSubscriber barometerEventSubscriber;
    private CategoryBody bodyState;
    private float bodyStateActualData = -1.0f;
    private CategoryBodyAgent bodyStateAgent = new CategoryBodyAgent();
    private ICategoryBodyDataCallback bodyStateDataCallback;
    private CommonEventSubscriber bodyStateEventSubscriber;
    private CommonEventSubscriber compassEventSubscriber;
    private Context context;
    private long delay = 0;
    private CategoryBody heartRate;
    private CategoryBodyAgent heartRateAgent = new CategoryBodyAgent();
    private ICategoryBodyDataCallback heartRateDataCallback;
    private CommonEventSubscriber heartRateEventSubscriber;
    private CategoryLight light;
    private CategoryLightAgent lightAgent = new CategoryLightAgent();
    private ICategoryLightDataCallback lightDataCallback;
    private CommonEventSubscriber lightEventSubscriber;
    private CategoryOrientation orientation;
    private CategoryOrientationAgent orientationAgent = new CategoryOrientationAgent();
    private ICategoryOrientationDataCallback orientationDataCallback;
    private CategoryLight proximity;
    private CategoryLightAgent proximityAgent = new CategoryLightAgent();
    private ICategoryLightDataCallback proximityDataCallback;
    private CommonEventSubscriber proximityEventSubscriber;
    private int samplingJson = 0;
    private CategoryMotion stepCounter;
    private CategoryMotionAgent stepCounterAgent = new CategoryMotionAgent();
    private ICategoryMotionDataCallback stepCounterDataCallback;
    private CommonEventSubscriber stepCounterEventSubscriber;
    private int subscribeIndex = 0;

    public static String getJsCode() {
        return "var sensor = {\n    sensorModuleGroup: null,\n    accelerometerEventGroup: null,\n    compassEventGroup: null,\n    proximityEventGroup: null,\n    lightEventGroup: null,\n    stepCounterEventGroup: null,\n    barometerEventGroup: null,\n    heartRateEventGroup: null,\n    bodyStateEventGroup: null,\n    intervalMode: [\"normal\", \"ui\", \"game\"],\n    samplingPeriod: [200000000, 60000000, 20000000],\n    isAccSubscribe: false,\n    isCompassSubscribe: false,\n    isProximitySubscribe: false,\n    isLightSubscribe: false,\n    isStepCounterSubscribe: false,\n    isBarometerSubscribe: false,\n    isHeartRateSubscribe: false,\n    isBodyStateSubscribe: false,\n    getOnBodyState: async function getOnBodyState(param) {\n        console.info(\"into getOnBodyState\");\n        if (sensor.sensorModuleGroup == null) {\n            sensor.sensorModuleGroup = ModuleGroup.getGroup(\"AceModuleGroup/SensorBody\");\n        }\n        return await catching(sensor.sensorModuleGroup.callNative(\"getBodyState\"), param);\n    },\n    subscribeAccelerometer: async function subscribeAccelerometer(param) {\n        var index = sensor.intervalMode.indexOf(param.interval);\n        if (index === -1) {\n            console.error(\"input accelerometer interval parameter error\");\n            return;\n        }\n        console.info(\"into subscribeAccelerometer\");\n        try {\n            if (typeof param.success !== 'function') {\n                commonCallback(param.fail, 'fail', 'The success function is not an available', 202);\n                return;\n            }\n            if (sensor.isAccSubscribe) {\n                await sensor.accelerometerEventGroup.unsubscribe(0);\n                sensor.isAccSubscribe = false;\n            } else {\n                sensor.accelerometerEventGroup = EventGroup.getGroup(\"AceEventGroup/Accelerometer\");\n            }\n            var result = await\n            sensor.accelerometerEventGroup.subscribe(function (callback) {\n                var val = JSON.parse(callback);\n                commonCallback(param.success, 'success', val.data);\n            }, sensor.samplingPeriod[index], 0);\n            var retAcc = JSON.parse(result);\n            if (retAcc.code === 0) {\n                sensor.isAccSubscribe = true;\n            } else {\n                commonCallback(param.fail, 'fail', retAcc, retAcc.code);\n            }\n        } catch (pluginError) {\n            var accError = JSON.parse(pluginError);\n            commonCallback(param.fail, 'fail', accError.data, accError.code);\n        }\n    },\n    unsubscribeAccelerometer: async function unsubscribeAccelerometer() {\n        if (sensor.accelerometerEventGroup == null) {\n            return;\n        }\n        if (sensor.isAccSubscribe) {\n            await sensor.accelerometerEventGroup.unsubscribe(0);\n            sensor.isAccSubscribe = false;\n        }\n    },\n    subscribeCompass: async function subscribeCompass(param) {\n        console.info(\"into subscribeCompass\");\n        try {\n            if (typeof param.success !== 'function') {\n                commonCallback(param.fail, 'fail', 'The success function is not an available', 202);\n                return;\n            }\n            if (sensor.isCompassSubscribe) {\n                await sensor.compassEventGroup.unsubscribe(1);\n                sensor.isCompassSubscribe = false;\n            } else {\n                sensor.compassEventGroup = EventGroup.getGroup(\"AceEventGroup/Compass\");\n            }\n            var result = await\n            sensor.compassEventGroup.subscribe(function (callback) {\n                var val = JSON.parse(callback);\n                commonCallback(param.success, 'success', val.data);\n            }, sensor.samplingPeriod[0], 1);\n            var retCompass = JSON.parse(result);\n            if (retCompass.code === 0) {\n                sensor.isCompassSubscribe = true;\n            } else {\n                commonCallback(param.fail, 'fail', retCompass, retCompass.code);\n            }\n        } catch (pluginError) {\n            var compassError = JSON.parse(pluginError);\n            commonCallback(param.fail, 'fail', compassError.data, compassError.code);\n        }\n    },\n    unsubscribeCompass: async function unsubscribeCompass() {\n        if (sensor.compassEventGroup == null) {\n            return;\n        }\n        if (sensor.isCompassSubscribe) {\n            await sensor.compassEventGroup.unsubscribe(1);\n            sensor.isCompassSubscribe = false;\n        }\n    },\n    subscribeProximity: async function subscribeProximity(param) {\n        console.info(\"into subscribeProximity\");\n        try {\n            if (typeof param.success !== 'function') {\n                commonCallback(param.fail, 'fail', 'The success function is not an available', 202);\n                return;\n            }\n            if (sensor.isProximitySubscribe) {\n                await sensor.proximityEventGroup.unsubscribe(2);\n                sensor.isProximitySubscribe = false;\n            } else {\n                sensor.proximityEventGroup = EventGroup.getGroup(\"AceEventGroup/Proximity\");\n            }\n            var result = await\n            sensor.proximityEventGroup.subscribe(function (callback) {\n                var val = JSON.parse(callback);\n                commonCallback(param.success, 'success', val.data);\n            }, sensor.samplingPeriod[0], 2);\n            var retProximity = JSON.parse(result);\n            if (retProximity.code === 0) {\n                sensor.isProximitySubscribe = true;\n            } else {\n                commonCallback(param.fail, 'fail', retProximity, retProximity.code);\n            }\n        } catch (pluginError) {\n            var proximityError = JSON.parse(pluginError);\n            commonCallback(param.fail, 'fail', proximityError.data, proximityError.code);\n        }\n    },\n    unsubscribeProximity: async function unsubscribeProximity() {\n        if (sensor.proximityEventGroup == null) {\n            return;\n        }\n        if (sensor.isProximitySubscribe) {\n            await sensor.proximityEventGroup.unsubscribe(2);\n            sensor.isProximitySubscribe = false;\n        }\n    },\n    subscribeLight: async function subscribeLight(param) {\n        console.info(\"into subscribeLight\");\n        try {\n            if (typeof param.success !== 'function') {\n                commonCallback(param.fail, 'fail', 'The success function is not an available', 202);\n                return;\n            }\n            if (sensor.isLightSubscribe) {\n                await sensor.lightEventGroup.unsubscribe(3);\n                sensor.isLightSubscribe = false;\n            } else {\n                sensor.lightEventGroup = EventGroup.getGroup(\"AceEventGroup/Light\");\n            }\n            var result = await\n            sensor.lightEventGroup.subscribe(function (callback) {\n                var val = JSON.parse(callback);\n                commonCallback(param.success, 'success', val.data);\n            }, sensor.samplingPeriod[0], 3);\n            var retLight = JSON.parse(result);\n            if (retLight.code === 0) {\n                sensor.isLightSubscribe = true;\n            } else {\n                commonCallback(param.fail, 'fail', retLight, retLight.code);\n            }\n        } catch (pluginError) {\n            var lightError = JSON.parse(pluginError);\n            commonCallback(param.fail, 'fail', lightError.data, lightError.code);\n        }\n    },\n    unsubscribeLight: async function unsubscribeLight() {\n        if (sensor.lightEventGroup == null) {\n            return;\n        }\n        if (sensor.isLightSubscribe) {\n            await sensor.lightEventGroup.unsubscribe(3);\n            sensor.isLightSubscribe = false;\n        }\n    },\n    subscribeStepCounter: async function subscribeStepCounter(param) {\n        console.info(\"into subscribeStepCounter\");\n        try {\n            if (typeof param.success !== 'function') {\n                commonCallback(param.fail, 'fail', 'The success function is not an available', 202);\n                return;\n            }\n            if (sensor.isStepCounterSubscribe) {\n                await sensor.stepCounterEventGroup.unsubscribe(4);\n                sensor.isStepCounterSubscribe = false;\n            } else {\n                sensor.stepCounterEventGroup = EventGroup.getGroup(\"AceEventGroup/StepCounter\");\n            }\n            var result = await\n            sensor.stepCounterEventGroup.subscribe(function (callback) {\n                var val = JSON.parse(callback);\n                commonCallback(param.success, 'success', val.data);\n            }, sensor.samplingPeriod[0], 4);\n            var retStep = JSON.parse(result);\n            if (retStep.code === 0) {\n                sensor.isStepCounterSubscribe = true;\n            } else {\n                commonCallback(param.fail, 'fail', retStep, retStep.code);\n            }\n        } catch (pluginError) {\n            var stepError = JSON.parse(pluginError);\n            commonCallback(param.fail, 'fail', stepError.data, stepError.code);\n        }\n    },\n    unsubscribeStepCounter: async function unsubscribeStepCounter() {\n        if (sensor.stepCounterEventGroup == null) {\n            return;\n        }\n        if (sensor.isStepCounterSubscribe) {\n            await sensor.stepCounterEventGroup.unsubscribe(4);\n            sensor.isStepCounterSubscribe = false;\n        }\n    },\n    subscribeBarometer: async function subscribeBarometer(param) {\n        console.info(\"into subscribeBarometer\");\n        try {\n            if (typeof param.success !== 'function') {\n                commonCallback(param.fail, 'fail', 'The success function is not an available', 202);\n                return;\n            }\n            if (sensor.isBarometerSubscribe) {\n                await sensor.barometerEventGroup.unsubscribe(5);\n                sensor.isBarometerSubscribe = false;\n            } else {\n                sensor.barometerEventGroup = EventGroup.getGroup(\"AceEventGroup/Barometer\");\n            }\n            var result = await\n            sensor.barometerEventGroup.subscribe(function (callback) {\n                var val = JSON.parse(callback);\n                commonCallback(param.success, 'success', val.data);\n            }, sensor.samplingPeriod[0], 5);\n            var retBarometer = JSON.parse(result);\n            if (retBarometer.code === 0) {\n                sensor.isBarometerSubscribe = true;\n            } else {\n                commonCallback(param.fail, 'fail', retBarometer, retBarometer.code);\n            }\n        } catch (pluginError) {\n            var barometerError = JSON.parse(pluginError);\n            commonCallback(param.fail, 'fail', barometerError.data, barometerError.code);\n        }\n    },\n    unsubscribeBarometer: async function unsubscribeBarometer() {\n        if (sensor.barometerEventGroup == null) {\n            return;\n        }\n        if (sensor.isBarometerSubscribe) {\n            await sensor.barometerEventGroup.unsubscribe(5);\n            sensor.isBarometerSubscribe = false;\n        }\n    },\n    subscribeHeartRate: async function subscribeHeartRate(param) {\n        console.info(\"into subscribeHeartRate\");\n        try {\n            if (typeof param.success !== 'function') {\n                commonCallback(param.fail, 'fail', 'The success function is not an available', 202);\n                return;\n            }\n            if (sensor.isHeartRateSubscribe) {\n                await sensor.heartRateEventGroup.unsubscribe(6);\n                sensor.isHeartRateSubscribe = false;\n            } else {\n                sensor.heartRateEventGroup = EventGroup.getGroup(\"AceEventGroup/HeartRate\");\n            }\n            var result = await\n            sensor.heartRateEventGroup.subscribe(function (callback) {\n                var val = JSON.parse(callback);\n                commonCallback(param.success, 'success', val.data);\n            }, sensor.samplingPeriod[0], 6);\n            var retHeartRate = JSON.parse(result);\n            if (retHeartRate.code === 0) {\n                sensor.isHeartRateSubscribe = true;\n            } else {\n                commonCallback(param.fail, 'fail', retHeartRate, retHeartRate.code);\n            }\n        } catch (pluginError) {\n            var heartError = JSON.parse(pluginError);\n            commonCallback(param.fail, 'fail', heartError.data, heartError.code);\n        }\n    },\n    unsubscribeHeartRate: async function unsubscribeHeartRate() {\n        if (sensor.heartRateEventGroup == null) {\n            return;\n        }\n        if (sensor.isHeartRateSubscribe) {\n            await sensor.heartRateEventGroup.unsubscribe(6);\n            sensor.isHeartRateSubscribe = false;\n        }\n    },\n    subscribeOnBodyState: async function subscribeOnBodyState(param) {\n        console.info(\"into subscribeOnBodyState\");\n        try {\n            if (typeof param.success !== 'function') {\n                commonCallback(param.fail, 'fail', 'The success function is not an available', 202);\n                return;\n            }\n            if (sensor.isBodyStateSubscribe) {\n                await sensor.bodyStateEventGroup.unsubscribe(7);\n                sensor.isBodyStateSubscribe = false;\n            } else {\n                sensor.bodyStateEventGroup = EventGroup.getGroup(\"AceEventGroup/BodyState\");\n            }\n            var result = await\n            sensor.bodyStateEventGroup.subscribe(function (callback) {\n                var val = JSON.parse(callback);\n                commonCallback(param.success, 'success', val.data);\n            }, sensor.samplingPeriod[0], 7);\n            var retState = JSON.parse(result);\n            if (retState.code === 0) {\n                sensor.isBodyStateSubscribe = true;\n            } else {\n                commonCallback(param.fail, 'fail', retState, retState.code);\n            }\n        } catch (pluginError) {\n            var stateError = JSON.parse(pluginError);\n            commonCallback(param.fail, 'fail', stateError.data, stateError.code);\n        }\n    },\n    unsubscribeOnBodyState: async function unsubscribeOnBodyState() {\n        if (sensor.bodyStateEventGroup == null) {\n            return;\n        }\n        if (sensor.isBodyStateSubscribe) {\n            await sensor.bodyStateEventGroup.unsubscribe(7);\n            sensor.isBodyStateSubscribe = false;\n        }\n    }\n};\nglobal.systemplugin.sensor = {\n    getOnBodyState: sensor.getOnBodyState,\n    subscribeAccelerometer: sensor.subscribeAccelerometer,\n    unsubscribeAccelerometer: sensor.unsubscribeAccelerometer,\n    subscribeCompass: sensor.subscribeCompass,\n    unsubscribeCompass: sensor.unsubscribeCompass,\n    subscribeProximity: sensor.subscribeProximity,\n    unsubscribeProximity: sensor.unsubscribeProximity,\n    subscribeLight: sensor.subscribeLight,\n    unsubscribeLight: sensor.unsubscribeLight,\n    subscribeStepCounter: sensor.subscribeStepCounter,\n    unsubscribeStepCounter: sensor.unsubscribeStepCounter,\n    subscribeBarometer: sensor.subscribeBarometer,\n    unsubscribeBarometer: sensor.unsubscribeBarometer,\n    subscribeHeartRate: sensor.subscribeHeartRate,\n    unsubscribeHeartRate: sensor.unsubscribeHeartRate,\n    subscribeOnBodyState: sensor.subscribeOnBodyState,\n    unsubscribeOnBodyState: sensor.unsubscribeOnBodyState\n};";
    }

    public static void register(Context context2) {
        instance = new SensorPlugin();
        instance.onRegister(context2);
        Integer valueOf = context2 instanceof AceAbility ? Integer.valueOf(((AceAbility) context2).getAbilityId()) : null;
        ModuleGroup.registerModuleGroup("AceModuleGroup/SensorBody", instance, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/Accelerometer", instance, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/Compass", instance, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/Proximity", instance, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/Light", instance, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/StepCounter", instance, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/Barometer", instance, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/HeartRate", instance, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/BodyState", instance, valueOf);
    }

    private void onRegister(Context context2) {
        this.context = context2;
    }

    public static void deregister(Context context2) {
        Integer valueOf = context2 instanceof AceAbility ? Integer.valueOf(((AceAbility) context2).getAbilityId()) : null;
        ModuleGroup.registerModuleGroup("AceModuleGroup/SensorBody", (ModuleGroup.ModuleGroupHandler) null, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/Accelerometer", (EventGroup.EventGroupHandler) null, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/Compass", (EventGroup.EventGroupHandler) null, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/Proximity", (EventGroup.EventGroupHandler) null, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/Light", (EventGroup.EventGroupHandler) null, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/StepCounter", (EventGroup.EventGroupHandler) null, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/Barometer", (EventGroup.EventGroupHandler) null, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/HeartRate", (EventGroup.EventGroupHandler) null, valueOf);
        EventGroup.registerEventGroup("AceEventGroup/BodyState", (EventGroup.EventGroupHandler) null, valueOf);
    }

    public static Set<String> getPluginGroup() {
        HashSet hashSet = new HashSet();
        hashSet.add("AceModuleGroup/SensorBody");
        hashSet.add("AceEventGroup/Accelerometer");
        hashSet.add("AceEventGroup/Compass");
        hashSet.add("AceEventGroup/Proximity");
        hashSet.add("AceEventGroup/Light");
        hashSet.add("AceEventGroup/StepCounter");
        hashSet.add("AceEventGroup/Barometer");
        hashSet.add("AceEventGroup/HeartRate");
        hashSet.add("AceEventGroup/BodyState");
        return hashSet;
    }

    public void onFunctionCall(Function function, Result result) {
        boolean z = false;
        HiLog.info(LABEL_LOG, LOG_FORMAT, TAG, "onFunctionCall");
        if (function.name.equals("getBodyState")) {
            if (Math.abs(this.bodyStateActualData - 0.0f) < THRESHOLD) {
                z = true;
            }
            result.success(Boolean.valueOf(z));
            return;
        }
        result.notExistFunction();
    }

    public void onSubscribe(List<Object> list, EventNotifier eventNotifier, Result result) {
        HiLog.info(LABEL_LOG, LOG_FORMAT, TAG, "onSubscribe");
        if (!isValidParameter(list)) {
            result.error((int) INPUT_ERROR_CODE, "Input parameter is invalid.");
            return;
        }
        long j = (long) this.samplingJson;
        switch (this.subscribeIndex) {
            case 0:
                subscribeAccelerometer(eventNotifier, result, j, this.delay);
                return;
            case 1:
                subscribeCompass(eventNotifier, result, j, this.delay);
                return;
            case 2:
                subscribeProximity(eventNotifier, result, j, this.delay);
                return;
            case 3:
                subscribeLight(eventNotifier, result, j, this.delay);
                return;
            case 4:
                subscribeStepCounter(eventNotifier, result, j, this.delay);
                return;
            case 5:
                subscribeBarometer(eventNotifier, result, j, this.delay);
                return;
            case 6:
                subscribeHeartRate(eventNotifier, result, j, this.delay);
                return;
            case 7:
                subscribeBodyState(eventNotifier, result, j, this.delay);
                return;
            default:
                HiLog.error(LABEL_LOG, LOG_FORMAT, TAG, "subscribe index is not exist.");
                return;
        }
    }

    private boolean isValidParameter(List<Object> list) {
        if (list.get(0) instanceof Integer) {
            this.samplingJson = ((Integer) list.get(0)).intValue();
            if (list.get(1) instanceof Integer) {
                this.subscribeIndex = ((Integer) list.get(1)).intValue();
                return true;
            }
        }
        return false;
    }

    private void subscribeAccelerometer(final EventNotifier eventNotifier, Result result, final long j, final long j2) {
        this.accelerometer = this.accelerometerAgent.getSingleSensor(0);
        if (this.accelerometer == null) {
            result.error(900, "Accelerometer sensor is not exist!");
            HiLog.error(LABEL_LOG, LOG_FORMAT, TAG, "Get accelerometer sensor failed");
            return;
        }
        this.accelerometerEventSubscriber = new CommonEventSubscriber(addSensorDataEvent(EVENT_ACCELEROMETER_DATA_CHANGED)) {
            /* class ohos.sensor.plugin.SensorPlugin.AnonymousClass1 */

            public void onReceiveEvent(CommonEventData commonEventData) {
                SensorPlugin.this.accelerometerDataCallback = new ICategoryMotionDataCallback() {
                    /* class ohos.sensor.plugin.SensorPlugin.AnonymousClass1.AnonymousClass1 */

                    public void onAccuracyDataModified(CategoryMotion categoryMotion, int i) {
                    }

                    public void onCommandCompleted(CategoryMotion categoryMotion) {
                    }

                    public void onSensorDataModified(CategoryMotionData categoryMotionData) {
                        if (categoryMotionData.getValues().length > 2) {
                            JSONObject jSONObject = new JSONObject();
                            jSONObject.put("x", (Object) Float.valueOf(categoryMotionData.getValues()[0]));
                            jSONObject.put("y", (Object) Float.valueOf(categoryMotionData.getValues()[1]));
                            jSONObject.put("z", (Object) Float.valueOf(categoryMotionData.getValues()[2]));
                            eventNotifier.success(jSONObject);
                        }
                    }
                };
                SensorPlugin.this.accelerometerAgent.setSensorDataCallback(SensorPlugin.this.accelerometerDataCallback, SensorPlugin.this.accelerometer, j, j2);
            }
        };
        channelSubscribeResult(result, this.accelerometerEventSubscriber, "Subscribe accelerometer sensor success");
        publishSensorDataEvent(EVENT_ACCELEROMETER_DATA_CHANGED);
    }

    private void subscribeCompass(final EventNotifier eventNotifier, Result result, final long j, final long j2) {
        this.orientation = this.orientationAgent.getSingleSensor(3);
        if (this.orientation == null) {
            result.error(900, "Compass sensor is not exist!");
            HiLog.error(LABEL_LOG, LOG_FORMAT, TAG, "Get compass sensor failed");
            return;
        }
        this.compassEventSubscriber = new CommonEventSubscriber(addSensorDataEvent(EVENT_COMPASS_DATA_CHANGED)) {
            /* class ohos.sensor.plugin.SensorPlugin.AnonymousClass2 */

            public void onReceiveEvent(CommonEventData commonEventData) {
                SensorPlugin.this.orientationDataCallback = new ICategoryOrientationDataCallback() {
                    /* class ohos.sensor.plugin.SensorPlugin.AnonymousClass2.AnonymousClass1 */

                    public void onAccuracyDataModified(CategoryOrientation categoryOrientation, int i) {
                    }

                    public void onCommandCompleted(CategoryOrientation categoryOrientation) {
                    }

                    public void onSensorDataModified(CategoryOrientationData categoryOrientationData) {
                        if (categoryOrientationData.getValues().length > 0) {
                            JSONObject jSONObject = new JSONObject();
                            jSONObject.put("direction", (Object) Float.valueOf(categoryOrientationData.getValues()[0]));
                            eventNotifier.success(jSONObject);
                        }
                    }
                };
                SensorPlugin.this.orientationAgent.setSensorDataCallback(SensorPlugin.this.orientationDataCallback, SensorPlugin.this.orientation, j, j2);
            }
        };
        channelSubscribeResult(result, this.compassEventSubscriber, "Subscribe compass sensor success");
        publishSensorDataEvent(EVENT_COMPASS_DATA_CHANGED);
    }

    private void subscribeProximity(final EventNotifier eventNotifier, Result result, final long j, final long j2) {
        this.proximity = this.proximityAgent.getSingleSensor(0);
        if (this.proximity == null) {
            result.error(900, "Proximity sensor is not exist!");
            HiLog.error(LABEL_LOG, LOG_FORMAT, TAG, "Get proximity sensor failed");
            return;
        }
        this.proximityEventSubscriber = new CommonEventSubscriber(addSensorDataEvent(EVENT_PROXIMITY_DATA_CHANGED)) {
            /* class ohos.sensor.plugin.SensorPlugin.AnonymousClass3 */

            public void onReceiveEvent(CommonEventData commonEventData) {
                SensorPlugin.this.proximityDataCallback = new ICategoryLightDataCallback() {
                    /* class ohos.sensor.plugin.SensorPlugin.AnonymousClass3.AnonymousClass1 */

                    public void onAccuracyDataModified(CategoryLight categoryLight, int i) {
                    }

                    public void onCommandCompleted(CategoryLight categoryLight) {
                    }

                    public void onSensorDataModified(CategoryLightData categoryLightData) {
                        if (categoryLightData.getValues().length > 0) {
                            JSONObject jSONObject = new JSONObject();
                            jSONObject.put("distance", (Object) Float.valueOf(categoryLightData.getValues()[0]));
                            eventNotifier.success(jSONObject);
                        }
                    }
                };
                SensorPlugin.this.proximityAgent.setSensorDataCallback(SensorPlugin.this.proximityDataCallback, SensorPlugin.this.proximity, j, j2);
            }
        };
        channelSubscribeResult(result, this.proximityEventSubscriber, "Subscribe proximity sensor success");
        publishSensorDataEvent(EVENT_PROXIMITY_DATA_CHANGED);
    }

    private void subscribeLight(final EventNotifier eventNotifier, Result result, final long j, final long j2) {
        this.light = this.lightAgent.getSingleSensor(2);
        if (this.light == null) {
            result.error(900, "Ambient light sensor is not exist!");
            HiLog.error(LABEL_LOG, LOG_FORMAT, TAG, "Get ambient light sensor failed");
            return;
        }
        this.lightEventSubscriber = new CommonEventSubscriber(addSensorDataEvent(EVENT_LIGHT_DATA_CHANGED)) {
            /* class ohos.sensor.plugin.SensorPlugin.AnonymousClass4 */

            public void onReceiveEvent(CommonEventData commonEventData) {
                SensorPlugin.this.lightDataCallback = new ICategoryLightDataCallback() {
                    /* class ohos.sensor.plugin.SensorPlugin.AnonymousClass4.AnonymousClass1 */

                    public void onAccuracyDataModified(CategoryLight categoryLight, int i) {
                    }

                    public void onCommandCompleted(CategoryLight categoryLight) {
                    }

                    public void onSensorDataModified(CategoryLightData categoryLightData) {
                        if (categoryLightData.getValues().length > 0) {
                            JSONObject jSONObject = new JSONObject();
                            jSONObject.put("intensity", (Object) Float.valueOf(categoryLightData.getValues()[0]));
                            eventNotifier.success(jSONObject);
                        }
                    }
                };
                SensorPlugin.this.lightAgent.setSensorDataCallback(SensorPlugin.this.lightDataCallback, SensorPlugin.this.light, j, j2);
            }
        };
        channelSubscribeResult(result, this.lightEventSubscriber, "Subscribe ambient light sensor success");
        publishSensorDataEvent(EVENT_LIGHT_DATA_CHANGED);
    }

    private void subscribeStepCounter(EventNotifier eventNotifier, Result result, long j, long j2) {
        this.stepCounter = this.stepCounterAgent.getSingleSensor(9);
        if (this.stepCounter == null) {
            result.error(900, "Step counter sensor is not exist!");
            HiLog.error(LABEL_LOG, LOG_FORMAT, TAG, "Get step counter sensor failed");
        } else if (this.context.verifySelfPermission("ohos.permission.ACTIVITY_MOTION") != 0) {
            HiLog.info(LABEL_LOG, LOG_FORMAT, TAG, "start request permission");
            if (this.context.canRequestPermission("ohos.permission.ACTIVITY_MOTION")) {
                this.context.requestPermissionsFromUser(new String[]{"ohos.permission.ACTIVITY_MOTION"}, 1);
            } else {
                result.error((int) FORBID_PERMISSION_ERROR_CODE, "user rejects the step counter permission request and forbids to again");
            }
        } else {
            subscribeStepCounterData(eventNotifier, result, j, j2);
        }
    }

    private void subscribeStepCounterData(final EventNotifier eventNotifier, Result result, final long j, final long j2) {
        this.stepCounterEventSubscriber = new CommonEventSubscriber(addSensorDataEvent(EVENT_STEP_COUNTER_DATA_CHANGED)) {
            /* class ohos.sensor.plugin.SensorPlugin.AnonymousClass5 */

            public void onReceiveEvent(CommonEventData commonEventData) {
                SensorPlugin.this.stepCounterDataCallback = new ICategoryMotionDataCallback() {
                    /* class ohos.sensor.plugin.SensorPlugin.AnonymousClass5.AnonymousClass1 */

                    public void onAccuracyDataModified(CategoryMotion categoryMotion, int i) {
                    }

                    public void onCommandCompleted(CategoryMotion categoryMotion) {
                    }

                    public void onSensorDataModified(CategoryMotionData categoryMotionData) {
                        if (categoryMotionData.getValues().length > 0) {
                            JSONObject jSONObject = new JSONObject();
                            jSONObject.put("steps", (Object) Float.valueOf(categoryMotionData.getValues()[0]));
                            eventNotifier.success(jSONObject);
                        }
                    }
                };
                SensorPlugin.this.stepCounterAgent.setSensorDataCallback(SensorPlugin.this.stepCounterDataCallback, SensorPlugin.this.stepCounter, j, j2);
            }
        };
        channelSubscribeResult(result, this.stepCounterEventSubscriber, "Subscribe step counter sensor success");
        publishSensorDataEvent(EVENT_STEP_COUNTER_DATA_CHANGED);
    }

    private void subscribeBarometer(final EventNotifier eventNotifier, Result result, final long j, final long j2) {
        this.barometer = this.barometerAgent.getSingleSensor(4);
        if (this.barometer == null) {
            result.error(900, "Barometer sensor is not exist!");
            HiLog.error(LABEL_LOG, LOG_FORMAT, TAG, "Get barometer sensor failed");
            return;
        }
        this.barometerEventSubscriber = new CommonEventSubscriber(addSensorDataEvent(EVENT_BAROMETER_DATA_CHANGED)) {
            /* class ohos.sensor.plugin.SensorPlugin.AnonymousClass6 */

            public void onReceiveEvent(CommonEventData commonEventData) {
                SensorPlugin.this.barometerDataCallback = new ICategoryEnvironmentDataCallback() {
                    /* class ohos.sensor.plugin.SensorPlugin.AnonymousClass6.AnonymousClass1 */

                    public void onAccuracyDataModified(CategoryEnvironment categoryEnvironment, int i) {
                    }

                    public void onCommandCompleted(CategoryEnvironment categoryEnvironment) {
                    }

                    public void onSensorDataModified(CategoryEnvironmentData categoryEnvironmentData) {
                        if (categoryEnvironmentData.getValues().length > 0) {
                            JSONObject jSONObject = new JSONObject();
                            jSONObject.put("pressure", (Object) Float.valueOf(categoryEnvironmentData.getValues()[0]));
                            eventNotifier.success(jSONObject);
                        }
                    }
                };
                SensorPlugin.this.barometerAgent.setSensorDataCallback(SensorPlugin.this.barometerDataCallback, SensorPlugin.this.barometer, j, j2);
            }
        };
        channelSubscribeResult(result, this.barometerEventSubscriber, "Subscribe barometer sensor success");
        publishSensorDataEvent(EVENT_BAROMETER_DATA_CHANGED);
    }

    private void subscribeHeartRate(EventNotifier eventNotifier, Result result, long j, long j2) {
        this.heartRate = this.heartRateAgent.getSingleSensor(0);
        if (this.heartRate == null) {
            result.error(900, "Heart rate sensor is not exist!");
            HiLog.error(LABEL_LOG, LOG_FORMAT, TAG, "Get heart rate sensor failed");
        } else if (this.context.verifySelfPermission("ohos.permission.READ_HEALTH_DATA") != 0) {
            HiLog.info(LABEL_LOG, LOG_FORMAT, TAG, "start request permission");
            if (this.context.canRequestPermission("ohos.permission.READ_HEALTH_DATA")) {
                this.context.requestPermissionsFromUser(new String[]{"ohos.permission.READ_HEALTH_DATA"}, 2);
            } else {
                result.error((int) FORBID_PERMISSION_ERROR_CODE, "user rejects the heart rate permission request and forbids to again");
            }
        } else {
            subscribeHeartRateData(eventNotifier, result, j, j2);
        }
    }

    private void subscribeHeartRateData(final EventNotifier eventNotifier, Result result, final long j, final long j2) {
        this.heartRateEventSubscriber = new CommonEventSubscriber(addSensorDataEvent(EVENT_HEART_RATE_DATA_CHANGED)) {
            /* class ohos.sensor.plugin.SensorPlugin.AnonymousClass7 */

            public void onReceiveEvent(CommonEventData commonEventData) {
                SensorPlugin.this.heartRateDataCallback = new ICategoryBodyDataCallback() {
                    /* class ohos.sensor.plugin.SensorPlugin.AnonymousClass7.AnonymousClass1 */

                    public void onAccuracyDataModified(CategoryBody categoryBody, int i) {
                    }

                    public void onCommandCompleted(CategoryBody categoryBody) {
                    }

                    public void onSensorDataModified(CategoryBodyData categoryBodyData) {
                        if (categoryBodyData.getValues().length > 0) {
                            JSONObject jSONObject = new JSONObject();
                            jSONObject.put("heartRate", (Object) Float.valueOf(categoryBodyData.getValues()[0]));
                            eventNotifier.success(jSONObject);
                        }
                    }
                };
                SensorPlugin.this.heartRateAgent.setSensorDataCallback(SensorPlugin.this.heartRateDataCallback, SensorPlugin.this.heartRate, j, j2);
            }
        };
        channelSubscribeResult(result, this.heartRateEventSubscriber, "Subscribe heart rate sensor success");
        publishSensorDataEvent(EVENT_HEART_RATE_DATA_CHANGED);
    }

    private void subscribeBodyState(final EventNotifier eventNotifier, Result result, final long j, final long j2) {
        this.bodyState = this.bodyStateAgent.getSingleSensor(1);
        if (this.bodyState == null) {
            result.error(900, "wear detection sensor is not exist!");
            HiLog.error(LABEL_LOG, LOG_FORMAT, TAG, "Get body state sensor failed");
            return;
        }
        this.bodyStateEventSubscriber = new CommonEventSubscriber(addSensorDataEvent(EVENT_BODY_STATE_DATA_CHANGED)) {
            /* class ohos.sensor.plugin.SensorPlugin.AnonymousClass8 */

            public void onReceiveEvent(CommonEventData commonEventData) {
                SensorPlugin.this.bodyStateDataCallback = new ICategoryBodyDataCallback() {
                    /* class ohos.sensor.plugin.SensorPlugin.AnonymousClass8.AnonymousClass1 */

                    public void onAccuracyDataModified(CategoryBody categoryBody, int i) {
                    }

                    public void onCommandCompleted(CategoryBody categoryBody) {
                    }

                    public void onSensorDataModified(CategoryBodyData categoryBodyData) {
                        SensorPlugin.this.bodyStateActualData = categoryBodyData.getValues()[0];
                        JSONObject jSONObject = new JSONObject();
                        if (Math.abs(SensorPlugin.this.bodyStateActualData - 0.0f) < SensorPlugin.THRESHOLD) {
                            jSONObject.put("value", (Object) true);
                            eventNotifier.success(jSONObject);
                            return;
                        }
                        jSONObject.put("value", (Object) false);
                        eventNotifier.success(jSONObject);
                    }
                };
                SensorPlugin.this.bodyStateAgent.setSensorDataCallback(SensorPlugin.this.bodyStateDataCallback, SensorPlugin.this.bodyState, j, j2);
            }
        };
        channelSubscribeResult(result, this.bodyStateEventSubscriber, "Subscribe body state sensor success");
        publishSensorDataEvent(EVENT_BODY_STATE_DATA_CHANGED);
    }

    private CommonEventSubscribeInfo addSensorDataEvent(String str) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(str);
        return new CommonEventSubscribeInfo(intentFilter);
    }

    private void channelSubscribeResult(Result result, CommonEventSubscriber commonEventSubscriber, String str) {
        try {
            CommonEventManager.subscribeCommonEvent(commonEventSubscriber);
            result.success(str);
        } catch (RemoteException unused) {
            result.error(1001, "Sensor Subscribe failed!");
            HiLog.error(LABEL_LOG, LOG_FORMAT, "subscribe error!");
        }
    }

    private void publishSensorDataEvent(String str) {
        Intent intent = new Intent();
        intent.setAction(str);
        try {
            CommonEventManager.publishCommonEvent(new CommonEventData(intent));
        } catch (RemoteException unused) {
            HiLog.error(LABEL_LOG, LOG_FORMAT, "publish sensor data event failed!");
        }
    }

    public void onRequestPermissionsFromUserResult(int i, String[] strArr, int[] iArr) {
        if (i != 1) {
            if (i != 2) {
                HiLog.error(LABEL_LOG, LOG_FORMAT, "the request code is invalid!");
            } else if (iArr.length <= 0 || iArr[0] != 0) {
                HiLog.error(LABEL_LOG, LOG_FORMAT, "the heart rate permission is forbidden!");
            } else {
                HiLog.info(LABEL_LOG, LOG_FORMAT, "the heart rate permission is granted!");
            }
        } else if (iArr.length <= 0 || iArr[0] != 0) {
            HiLog.error(LABEL_LOG, LOG_FORMAT, "the step counter permission is forbidden!");
        } else {
            HiLog.info(LABEL_LOG, LOG_FORMAT, "the step counter permission is granted!");
        }
    }

    public void onUnsubscribe(List<Object> list, Result result) {
        HiLog.info(LABEL_LOG, LOG_FORMAT, TAG, "onUnsubscribe");
        switch (((Integer) list.get(0)).intValue()) {
            case 0:
                unsubscribeAccelerometer(result);
                return;
            case 1:
                unsubscribeCompass(result);
                return;
            case 2:
                unsubscribeProximity(result);
                return;
            case 3:
                unsubscribeLight(result);
                return;
            case 4:
                unsubscribeStepCounter(result);
                return;
            case 5:
                unsubscribeBarometer(result);
                return;
            case 6:
                unsubscribeHeartRate(result);
                return;
            case 7:
                unsubscribeBodyState(result);
                return;
            default:
                HiLog.error(LABEL_LOG, LOG_FORMAT, TAG, "unsubscribe index is not exist.");
                return;
        }
    }

    private void unsubscribeAccelerometer(Result result) {
        this.accelerometerAgent.releaseSensorDataCallback(this.accelerometerDataCallback, this.accelerometer);
        channelUnsubscribeResult(result, this.accelerometerEventSubscriber, "Unsubscribe accelerometer sensor success");
        this.accelerometerEventSubscriber = null;
    }

    private void unsubscribeCompass(Result result) {
        this.orientationAgent.releaseSensorDataCallback(this.orientationDataCallback, this.orientation);
        channelUnsubscribeResult(result, this.compassEventSubscriber, "Unsubscribe compass sensor success");
        this.compassEventSubscriber = null;
    }

    private void unsubscribeProximity(Result result) {
        this.proximityAgent.releaseSensorDataCallback(this.proximityDataCallback, this.proximity);
        channelUnsubscribeResult(result, this.proximityEventSubscriber, "Unsubscribe proximity sensor success");
        this.proximityEventSubscriber = null;
    }

    private void unsubscribeLight(Result result) {
        this.lightAgent.releaseSensorDataCallback(this.lightDataCallback, this.light);
        channelUnsubscribeResult(result, this.lightEventSubscriber, "Unsubscribe ambient light sensor success");
        this.lightEventSubscriber = null;
    }

    private void unsubscribeStepCounter(Result result) {
        this.stepCounterAgent.releaseSensorDataCallback(this.stepCounterDataCallback, this.stepCounter);
        channelUnsubscribeResult(result, this.stepCounterEventSubscriber, "Unsubscribe step counter sensor success");
        this.stepCounterEventSubscriber = null;
    }

    private void unsubscribeBarometer(Result result) {
        this.barometerAgent.releaseSensorDataCallback(this.barometerDataCallback, this.barometer);
        channelUnsubscribeResult(result, this.barometerEventSubscriber, "Unsubscribe barometer sensor success");
        this.barometerEventSubscriber = null;
    }

    private void unsubscribeHeartRate(Result result) {
        this.heartRateAgent.releaseSensorDataCallback(this.heartRateDataCallback, this.heartRate);
        channelUnsubscribeResult(result, this.heartRateEventSubscriber, "Unsubscribe heart rate sensor success");
        this.heartRateEventSubscriber = null;
    }

    private void unsubscribeBodyState(Result result) {
        this.bodyStateAgent.releaseSensorDataCallback(this.bodyStateDataCallback, this.bodyState);
        channelUnsubscribeResult(result, this.bodyStateEventSubscriber, "Unsubscribe body state sensor success");
        this.bodyStateEventSubscriber = null;
    }

    private void channelUnsubscribeResult(Result result, CommonEventSubscriber commonEventSubscriber, String str) {
        try {
            CommonEventManager.unsubscribeCommonEvent(commonEventSubscriber);
            result.success(str);
        } catch (RemoteException unused) {
            result.error(1002, "Sensor Unsubscribe failed!");
            HiLog.error(LABEL_LOG, LOG_FORMAT, "unsubscribe error!");
        }
    }
}
