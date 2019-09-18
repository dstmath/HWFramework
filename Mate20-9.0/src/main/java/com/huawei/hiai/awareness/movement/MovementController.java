package com.huawei.hiai.awareness.movement;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import com.huawei.hiai.awareness.AwarenessConstants;
import com.huawei.hiai.awareness.awareness.Event;
import com.huawei.hiai.awareness.common.Utils;
import com.huawei.hiai.awareness.common.log.LogUtil;
import com.huawei.hiai.awareness.service.AwarenessFence;
import com.huawei.hiai.awareness.service.ConnectServiceManager;
import com.huawei.hiai.awareness.service.ExtendAwarenessFence;
import com.huawei.hiai.awareness.service.IRequestCallBack;
import com.huawei.hiai.awareness.service.RequestResult;
import com.huawei.hiai.awareness.service.ServiceBindingManager;
import com.huawei.msdp.movement.HwMSDPMovementChangeEvent;
import com.huawei.msdp.movement.HwMSDPMovementEvent;
import com.huawei.msdp.movement.HwMSDPOtherParameters;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MovementController {
    private static String TAG = ("sdk_" + MovementController.class.getSimpleName());
    private static Event mMovenentStatusEvent = new Event();
    private static final ConcurrentHashMap<String, Integer> sDefaultMovementActionConfigMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, Integer> sDefaultMovementStatusConfigMap = new ConcurrentHashMap<>();
    private static MovementController sInstance;
    private ConcurrentHashMap<String, PendingIntent> mActionCallbackMap = new ConcurrentHashMap<>();
    private String[] mEmptyStringArray = new String[0];
    private int mMSDPSupportModule = -1;
    private String[] mMSDPSupportedEnvironments = this.mEmptyStringArray;
    private String[] mSupportedActivities = this.mEmptyStringArray;

    static {
        sDefaultMovementStatusConfigMap.put(1, 1);
        sDefaultMovementStatusConfigMap.put(2, 2);
        sDefaultMovementActionConfigMap.put("android.activity_recognition.in_vehicle", 1);
        sDefaultMovementActionConfigMap.put("android.activity_recognition.on_bicycle", 2);
        sDefaultMovementActionConfigMap.put("android.activity_recognition.walking", 4);
        sDefaultMovementActionConfigMap.put("android.activity_recognition.running", 8);
        sDefaultMovementActionConfigMap.put("android.activity_recognition.still", 16);
        sDefaultMovementActionConfigMap.put("android.activity_recognition.fast_walking", 32);
        sDefaultMovementActionConfigMap.put("android.activity_recognition.high_speed_rail", 64);
        sDefaultMovementActionConfigMap.put("android.activity_recognition.on_foot", 128);
        sDefaultMovementActionConfigMap.put("android.activity_recognition.elevator", 256);
        sDefaultMovementActionConfigMap.put("android.activity_recognition.relative_still", 512);
        sDefaultMovementActionConfigMap.put("android.activity_recognition.env_home", Integer.valueOf(AwarenessConstants.MSDP_ENVIRONMENT_TYPE_HOME));
        sDefaultMovementActionConfigMap.put("android.activity_recognition.env_office", Integer.valueOf(AwarenessConstants.MSDP_ENVIRONMENT_TYPE_OFFICE));
        sDefaultMovementActionConfigMap.put("android.activity_recognition.env_way_home", Integer.valueOf(AwarenessConstants.MSDP_ENVIRONMENT_TYPE_WAY_HOME));
        sDefaultMovementActionConfigMap.put("android.activity_recognition.env_way_office", Integer.valueOf(AwarenessConstants.MSDP_ENVIRONMENT_TYPE_WAY_OFFICE));
    }

    public static ConcurrentHashMap<String, Integer> getDefaultMovementActionConfigMap() {
        return sDefaultMovementActionConfigMap;
    }

    public Event getMovenentStatusEvent() {
        return mMovenentStatusEvent;
    }

    public static synchronized MovementController getInstance() {
        MovementController movementController;
        synchronized (MovementController.class) {
            if (sInstance == null) {
                sInstance = new MovementController();
            }
            movementController = sInstance;
        }
        return movementController;
    }

    public String getAction(int action) {
        for (Map.Entry<String, Integer> entry : sDefaultMovementActionConfigMap.entrySet()) {
            if (entry.getValue().intValue() == action) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean doSensorRegister(AwarenessFence awarenessFence, IRequestCallBack iRequestCallBack, PendingIntent pendingIntent) {
        String[] supportAction;
        LogUtil.d(TAG, "doSensorUnRegister ");
        if (awarenessFence == null) {
            LogUtil.e(TAG, "doSensorRegister illegal!");
            return false;
        }
        if (-1 == this.mMSDPSupportModule) {
            this.mMSDPSupportModule = ConnectServiceManager.getInstance().getmMSDPSupportModule();
            LogUtil.d(TAG, "doSensorRegister, mMSDPSupportModule: " + this.mMSDPSupportModule + "\t action: 0x" + Integer.toHexString(awarenessFence.getAction()));
            if (1 == (this.mMSDPSupportModule & 1) && this.mSupportedActivities != null && this.mSupportedActivities.length == 0) {
                this.mSupportedActivities = ConnectServiceManager.getInstance().getmMSDPMovementSupportedActivities();
                LogUtil.d(TAG, "doSensorRegister, printHashMap mSupportedActivities");
            }
            if (2 == (this.mMSDPSupportModule & 2) && this.mMSDPSupportedEnvironments != null && this.mMSDPSupportedEnvironments.length == 0) {
                this.mMSDPSupportedEnvironments = ConnectServiceManager.getInstance().getmMSDPSupportedEnvironments();
                LogUtil.d(TAG, "doSensorRegister, printHashMap mMSDPSupportedEnvironments");
            }
            if (this.mSupportedActivities != null && this.mSupportedActivities.length == 0) {
                LogUtil.e(TAG, "awareness doSensorRegister, get movenent mSupportedActivities failure!");
                this.mMSDPSupportModule = -1;
            }
        }
        if (!(awarenessFence instanceof ExtendAwarenessFence)) {
            return false;
        }
        int action = awarenessFence.getAction();
        Bundle bundle = ((ExtendAwarenessFence) awarenessFence).getRegisterBundle();
        if (bundle == null) {
            return false;
        }
        Long screenOnReportPeriod = Long.valueOf(bundle.getLong(AwarenessConstants.SENSORHUB_CONTROL_REPORT_PERIOD, 200000000000L));
        String strHwMSDPOtherParameters = null;
        if (Build.VERSION.SDK_INT >= 12) {
            strHwMSDPOtherParameters = bundle.getString(AwarenessConstants.HwMSDPOtherParams, null);
        }
        HwMSDPOtherParameters hwMSDPOtherParameters = Utils.getHwMSDPOtherParametersbyString(strHwMSDPOtherParameters);
        LogUtil.d(TAG, "doSensorRegister, action: 0x" + Integer.toHexString(awarenessFence.getAction()) + "\t screenOnReportPeriod: " + screenOnReportPeriod + "\t HwMSDPOtherParameters:" + hwMSDPOtherParameters);
        ConcurrentHashMap<String, Integer> map = sDefaultMovementActionConfigMap;
        boolean isRegistered = false;
        Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Map.Entry<String, Integer> entry = it.next();
            if ((entry.getValue().intValue() & awarenessFence.getAction()) != 0) {
                isRegistered = true;
                if (entry.getValue().intValue() < 65536) {
                }
                if (entry.getValue().intValue() < 65536) {
                    supportAction = this.mSupportedActivities;
                } else {
                    supportAction = this.mMSDPSupportedEnvironments;
                }
                String activity = Utils.actionSupport(entry.getValue().intValue(), map, supportAction);
                if (entry.getValue().intValue() != (entry.getValue().intValue() & awarenessFence.getAction()) || activity == null) {
                    LogUtil.e(TAG, "doSensorRegister unsupport register type: entry.getValue(): 0x" + Integer.toHexString(entry.getValue().intValue()));
                    isRegistered = false;
                    action = entry.getValue().intValue();
                }
            }
        }
        if (!isRegistered) {
            awarenessFence.setAction(action);
            ServiceBindingManager.getInstance().registerResultCallback(iRequestCallBack, awarenessFence, 4, AwarenessConstants.ERROR_FUNCTION_NOT_SUPPORTED_CODE);
            return false;
        }
        if (this.mActionCallbackMap == null) {
            this.mActionCallbackMap = new ConcurrentHashMap<>();
        }
        if (!this.mActionCallbackMap.containsKey(Integer.valueOf(awarenessFence.getAction()))) {
            this.mActionCallbackMap.put(awarenessFence.getTopKey(), pendingIntent);
        }
        for (Map.Entry<String, Integer> entry2 : map.entrySet()) {
            if ((entry2.getValue().intValue() & awarenessFence.getAction()) != 0) {
                ConnectServiceManager.getInstance().enableMovementEvent(Utils.actionSupport(entry2.getValue().intValue(), map, entry2.getValue().intValue() < 65536 ? this.mSupportedActivities : this.mMSDPSupportedEnvironments), entry2.getValue().intValue() < 65536 ? 0 : 2, screenOnReportPeriod, hwMSDPOtherParameters);
            }
        }
        ServiceBindingManager.getInstance().registerResultCallback(iRequestCallBack, awarenessFence, 3, AwarenessConstants.REGISTER_SUCCESS_CODE);
        return true;
    }

    public boolean doSensorUnRegister(AwarenessFence fence) {
        LogUtil.d(TAG, "doSensorUnRegister ");
        if (fence == null) {
            LogUtil.e(TAG, "doSensorUnRegister illegal!");
            return false;
        }
        if (this.mSupportedActivities == null || this.mSupportedActivities.length == 0) {
            this.mSupportedActivities = ConnectServiceManager.getInstance().getmMSDPMovementSupportedActivities();
        }
        if (this.mMSDPSupportedEnvironments == null || this.mMSDPSupportedEnvironments.length == 0) {
            this.mMSDPSupportedEnvironments = ConnectServiceManager.getInstance().getmMSDPSupportedEnvironments();
        }
        ConcurrentHashMap<String, Integer> map = sDefaultMovementActionConfigMap;
        boolean flag = true;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if ((entry.getValue().intValue() & fence.getAction()) != 0) {
                int movementType = entry.getValue().intValue() < 65536 ? 0 : 2;
                String activity = Utils.actionSupport(entry.getValue().intValue(), map, entry.getValue().intValue() < 65536 ? this.mSupportedActivities : this.mMSDPSupportedEnvironments);
                if (entry.getValue().intValue() == (entry.getValue().intValue() & fence.getAction()) && activity != null) {
                    LogUtil.d(TAG, "doSensorUnRegister success 2, entry.getValue():0x" + Integer.toHexString(entry.getValue().intValue()));
                    ConnectServiceManager.getInstance().diableMovementEvent(activity, movementType);
                }
            }
        }
        if (!doRealUnRegisterSensor(fence)) {
            flag = false;
        }
        return flag;
    }

    private boolean doRealUnRegisterSensor(AwarenessFence fence) {
        LogUtil.d(TAG, "doRealUnRegisterSensor ");
        if (fence != null) {
            LogUtil.d(TAG, "prepareUnregister action:" + fence.getAction());
            if (this.mActionCallbackMap != null && this.mActionCallbackMap.containsKey(fence.getTopKey())) {
                this.mActionCallbackMap.remove(fence.getTopKey());
                return true;
            }
        }
        return false;
    }

    public void onMovementStatusChanged(HwMSDPMovementChangeEvent var1) {
        LogUtil.d(TAG, "HwMSDPMovementStatusChangedCallBack onMovementStatusChanged()");
        Iterable<HwMSDPMovementEvent> eventIterable = var1.getMovementEvents();
        if (eventIterable == null) {
            LogUtil.e(TAG, "onMovementStatusChanged() eventIterable == null");
            return;
        }
        int i = 0;
        for (HwMSDPMovementEvent event : eventIterable) {
            i++;
            String Movement = event.getMovement();
            LogUtil.d(TAG, "onMovementStatusChanged() i = " + i + " Movement : " + Movement);
            if (Utils.isEmpty(Movement) || sDefaultMovementActionConfigMap.containsKey(Movement)) {
                String eventTypeString = "";
                int eventTypeInt = event.getEventType();
                if (1 == eventTypeInt) {
                    eventTypeString = "In";
                    eventTypeInt = 1;
                } else if (2 == eventTypeInt) {
                    eventTypeString = "Out";
                    eventTypeInt = 2;
                }
                long timeNS = event.getTimestampNs();
                int confidence = event.getConfidence();
                LogUtil.d(TAG, "onMovementChanged() timeNS : " + timeNS + ",eventTypeString:" + ",eventTypeString:" + eventTypeString + ",confidence: " + confidence);
                prepareQueryMovementStatus(Movement, eventTypeInt, timeNS, confidence);
                executeSensorCallback(event);
            } else {
                LogUtil.e(TAG, "onMovementStatusChanged() sDefaultMovementActionConfigMap.containsKey(Movement) false");
            }
        }
    }

    private void prepareQueryMovementStatus(String Movement, int eventType, long timeNS, int confidence) {
        LogUtil.d(TAG, "prepareQueryMovementStatus() Movement : " + Movement + " eventType : " + eventType + " timeNS : " + timeNS + " confidence : " + confidence);
        int status = -1;
        if (sDefaultMovementStatusConfigMap.containsKey(Integer.valueOf(eventType))) {
            status = sDefaultMovementStatusConfigMap.get(Integer.valueOf(eventType)).intValue();
        }
        LogUtil.d(TAG, "prepareQueryMovementStatus() status : " + status + " action : " + -1);
        mMovenentStatusEvent.setEventConfidence(confidence);
        mMovenentStatusEvent.setEventCurAction(-1);
        mMovenentStatusEvent.setEventCurStatus(status);
        mMovenentStatusEvent.setEventCurType(1);
        mMovenentStatusEvent.setEventSensorTime(timeNS);
        mMovenentStatusEvent.setEventTime(System.currentTimeMillis());
        mMovenentStatusEvent.setEventTriggerStatus(1);
    }

    private void executeSensorCallback(HwMSDPMovementEvent event) {
        LogUtil.d(TAG, "enter executeSensorCallback()");
        int action = sDefaultMovementActionConfigMap.get(event.getMovement()).intValue();
        long timeNS = event.getTimestampNs();
        int eventTypeInt = event.getEventType();
        for (Map.Entry<String, PendingIntent> entry : this.mActionCallbackMap.entrySet()) {
            ArrayList arrayList = Utils.getArrayListFromTopKey(entry.getKey());
            if (arrayList == null) {
                LogUtil.e(TAG, "executeSensorCallback() arrayList == null");
                return;
            }
            LogUtil.d(TAG, "executeSensorCallback() arrayList:" + arrayList);
            if (1 != arrayList.size()) {
                LogUtil.e(TAG, "executeSensorCallback() arrayList size is not 1");
                return;
            }
            int registerAction = ((Integer) arrayList.get(0)).intValue();
            if (-1 == registerAction) {
                LogUtil.e(TAG, "executeSensorCallback() registerAction == -1");
                return;
            }
            LogUtil.d(TAG, "executeSensorCallback() registerAction : " + registerAction);
            if (action == (action & registerAction)) {
                PendingIntent pendingIntent = entry.getValue();
                if (pendingIntent != null) {
                    Intent intent = new Intent();
                    intent.putExtra(AwarenessConstants.DATA_SENSOR_TIME_STAMP, timeNS);
                    intent.putExtra(AwarenessConstants.DATA_EVENT_TYPE, eventTypeInt);
                    intent.putExtra(AwarenessConstants.DATA_ACTION_TYPE, action);
                    intent.putExtra(AwarenessConstants.DATA_ACTION_STRING_TYPE, event.getMovement());
                    LogUtil.d(TAG, "executeSensorCallback() timeNS : " + timeNS + ",eventTypeInt : " + eventTypeInt + ",DATA_ACTION_TYPE:" + action + ",intent.toString():" + intent.toString());
                    try {
                        pendingIntent.send(ConnectServiceManager.getInstance().getConnectServiceManagerContext(), 0, intent);
                    } catch (PendingIntent.CanceledException e) {
                        LogUtil.e(TAG, "executeSensorCallback() send failure " + intent.toString());
                    }
                } else {
                    return;
                }
            }
        }
    }

    public RequestResult doSetReportPeriod(ExtendAwarenessFence awarenessFence) {
        String[] supportAction;
        if (awarenessFence == null || awarenessFence.getRegisterBundle() == null) {
            LogUtil.d(TAG, "doSetReportPeriod(): illegal parameters!");
            RequestResult result = new RequestResult(AwarenessConstants.ERROR_UNKNOWN_CODE, AwarenessConstants.ERROR_UNKNOWN);
            result.setResultType(7);
            return result;
        }
        if (-1 == this.mMSDPSupportModule) {
            this.mMSDPSupportModule = ConnectServiceManager.getInstance().getmMSDPSupportModule();
            LogUtil.d(TAG, "doSetReportPeriod, mMSDPSupportModule: " + this.mMSDPSupportModule + "\t action: 0x" + Integer.toHexString(awarenessFence.getAction()));
            if (1 == (this.mMSDPSupportModule & 1) && this.mSupportedActivities != null && this.mSupportedActivities.length == 0) {
                this.mSupportedActivities = ConnectServiceManager.getInstance().getmMSDPMovementSupportedActivities();
                LogUtil.d(TAG, "doSetReportPeriod, printHashMap mSupportedActivities");
            }
            if (2 == (this.mMSDPSupportModule & 2) && this.mMSDPSupportedEnvironments != null && this.mMSDPSupportedEnvironments.length == 0) {
                this.mMSDPSupportedEnvironments = ConnectServiceManager.getInstance().getmMSDPSupportedEnvironments();
                LogUtil.d(TAG, "doSetReportPeriod, printHashMap mMSDPSupportedEnvironments");
            }
            if (this.mSupportedActivities != null && this.mSupportedActivities.length == 0) {
                LogUtil.e(TAG, "doSetReportPeriod, get movenent mSupportedActivities failure!");
                this.mMSDPSupportModule = -1;
            }
        }
        Bundle bundle = awarenessFence.getRegisterBundle();
        Long screenOnReportPeriod = Long.valueOf(bundle.getLong(AwarenessConstants.SENSORHUB_CONTROL_REPORT_PERIOD, 200000000000L));
        String strHwMSDPOtherParameters = null;
        if (Build.VERSION.SDK_INT >= 12) {
            strHwMSDPOtherParameters = bundle.getString(AwarenessConstants.HwMSDPOtherParams, null);
        }
        HwMSDPOtherParameters hwMSDPOtherParameters = Utils.getHwMSDPOtherParametersbyString(strHwMSDPOtherParameters);
        LogUtil.d(TAG, "doSetReportPeriod, action: 0x" + Integer.toHexString(awarenessFence.getAction()) + "\t screenOnReportPeriod: " + screenOnReportPeriod + "\t HwMSDPOtherParameters:" + hwMSDPOtherParameters);
        ConcurrentHashMap<String, Integer> map = sDefaultMovementActionConfigMap;
        boolean isRegistered = false;
        Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Map.Entry<String, Integer> entry = it.next();
            if ((entry.getValue().intValue() & awarenessFence.getAction()) != 0) {
                isRegistered = true;
                if (entry.getValue().intValue() < 65536) {
                }
                if (entry.getValue().intValue() < 65536) {
                    supportAction = this.mSupportedActivities;
                } else {
                    supportAction = this.mMSDPSupportedEnvironments;
                }
                String activity = Utils.actionSupport(entry.getValue().intValue(), map, supportAction);
                if (entry.getValue().intValue() != (entry.getValue().intValue() & awarenessFence.getAction()) || activity == null) {
                    LogUtil.e(TAG, "doSetReportPeriod unsupport register type: entry.getValue(): 0x" + Integer.toHexString(entry.getValue().intValue()));
                    isRegistered = false;
                }
            }
        }
        if (!isRegistered) {
            RequestResult result2 = new RequestResult(AwarenessConstants.ERROR_PARAMETER_CODE, null);
            result2.setResultType(7);
            return result2;
        }
        for (Map.Entry<String, Integer> entry2 : map.entrySet()) {
            if ((entry2.getValue().intValue() & awarenessFence.getAction()) != 0) {
                ConnectServiceManager.getInstance().enableMovementEvent(Utils.actionSupport(entry2.getValue().intValue(), map, entry2.getValue().intValue() < 65536 ? this.mSupportedActivities : this.mMSDPSupportedEnvironments), entry2.getValue().intValue() < 65536 ? 0 : 2, screenOnReportPeriod, hwMSDPOtherParameters);
            }
        }
        RequestResult result3 = new RequestResult(AwarenessConstants.REGISTER_SUCCESS_CODE, null);
        result3.setResultType(6);
        return result3;
    }
}
