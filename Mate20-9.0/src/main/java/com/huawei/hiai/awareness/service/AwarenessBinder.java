package com.huawei.hiai.awareness.service;

import android.app.PendingIntent;
import android.os.Bundle;
import android.os.RemoteException;
import com.huawei.hiai.awareness.AwarenessConstants;
import com.huawei.hiai.awareness.common.log.LogUtil;
import com.huawei.hiai.awareness.movement.MovementController;

public class AwarenessBinder {
    private static final String TAG = "AwarenessBinder";
    private static AwarenessBinder sInstance;

    private AwarenessBinder() {
        LogUtil.d(TAG, "AwarenessBinder()");
    }

    public static synchronized AwarenessBinder getInstance() {
        AwarenessBinder awarenessBinder;
        synchronized (AwarenessBinder.class) {
            if (sInstance == null) {
                sInstance = new AwarenessBinder();
            }
            awarenessBinder = sInstance;
        }
        return awarenessBinder;
    }

    public boolean unRegisterFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) {
        boolean unRegisterSuccess;
        LogUtil.d(TAG, "unRegisterFence() awarenessFence : " + awarenessFence);
        if (callback == null || awarenessFence == null || operationPI == null) {
            LogUtil.e(TAG, "unRegisterFence() null == callback || null == awarenessFence || null == operationPI");
            return false;
        }
        LogUtil.d(TAG, "unRegisterFence() operationPI.hashCode : " + operationPI.hashCode());
        boolean isSuccess = false;
        if (1 == awarenessFence.getType()) {
            isSuccess = MovementController.getInstance().doSensorUnRegister(awarenessFence);
        }
        if (isSuccess) {
            RequestResult result = new RequestResult();
            result.setRegisterTopKey(awarenessFence.getTopKey());
            result.setResultType(1);
            result.setTriggerStatus(5);
            try {
                callback.onRequestResult(result);
            } catch (RemoteException e) {
                LogUtil.d(TAG, "unRegisterFence()  catch exception : isSuccess = " + isSuccess);
            }
            unRegisterSuccess = true;
        } else {
            RequestResult result2 = new RequestResult();
            result2.setRegisterTopKey(awarenessFence.getTopKey());
            result2.setResultType(1);
            result2.setTriggerStatus(6);
            try {
                callback.onRequestResult(result2);
            } catch (RemoteException e2) {
                LogUtil.d(TAG, "unRegisterFence()  catch exception : isSuccess = " + isSuccess);
            }
            unRegisterSuccess = false;
        }
        LogUtil.d(TAG, "unRegisterFence()  unRegisterSuccess : " + unRegisterSuccess);
        return unRegisterSuccess;
    }

    public String getAwarenessApiVersion() throws RemoteException {
        LogUtil.d(TAG, "getAwarenessApiVersion() version : " + AwarenessInnerConstants.AWARENESS_VERSION_CODE);
        return AwarenessInnerConstants.AWARENESS_VERSION_CODE;
    }

    private boolean registerFence(int fenceType, int fenceAction, IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) {
        LogUtil.d(TAG, "registerFence() fenceType : " + fenceType + " fenceAction : " + fenceAction);
        if (callback == null || awarenessFence == null || operationPI == null) {
            LogUtil.e(TAG, "registerFence() null == callback || null == awarenessFence || null == operationPI");
            return false;
        }
        LogUtil.d(TAG, "registerFence() operationPI.hashCode : " + operationPI.hashCode());
        RequestResult result = new RequestResult();
        if (!ServiceBindingManager.getInstance().isFenceFunctionSupported(fenceType)) {
            RequestResult result2 = new RequestResult(AwarenessConstants.ERROR_FUNCTION_NOT_SUPPORTED_CODE, AwarenessConstants.ERROR_FUNCTION_NOT_SUPPORTED);
            result2.setResultType(1);
            result2.setTriggerStatus(4);
            try {
                callback.onRequestResult(result2);
                return false;
            } catch (RemoteException e) {
                LogUtil.e(TAG, "registerFence() RemoteException   ");
                return false;
            }
        } else if (isFenceParameterError(fenceType, fenceAction, awarenessFence)) {
            LogUtil.e(TAG, "registerFence() parameter error");
            result.setResultType(1);
            result.setTriggerStatus(4);
            result.setErrorCode(AwarenessConstants.ERROR_PARAMETER_CODE);
            result.setErrorResult(AwarenessConstants.ERROR_PARAMETER);
            try {
                callback.onRequestResult(result);
                return false;
            } catch (RemoteException e2) {
                LogUtil.e(TAG, "registerFence() RemoteException   ");
                return false;
            }
        } else {
            awarenessFence.setOperationPI(operationPI);
            switch (awarenessFence.getType()) {
                case 1:
                    return MovementController.getInstance().doSensorRegister(awarenessFence, callback, operationPI);
                default:
                    return false;
            }
        }
    }

    private boolean isFenceParameterError(int fenceType, int fenceAction, AwarenessFence awarenessFence) {
        LogUtil.d(TAG, "isFenceParameterError() fenceType : " + fenceType + " fenceAction : " + fenceAction);
        boolean isParameterError = false;
        if (awarenessFence == null) {
            LogUtil.e(TAG, "isFenceParameterError() null == awarenessFence");
            return true;
        }
        int action = awarenessFence.getAction();
        int status = awarenessFence.getStatus();
        switch (fenceType) {
            case 1:
                if (1 != awarenessFence.getType()) {
                    LogUtil.e(TAG, "isFenceParameterError() MOVEMENT_TYPE type error");
                    isParameterError = true;
                    break;
                }
                break;
            case 3:
                if (3 != awarenessFence.getType()) {
                    LogUtil.e(TAG, "isFenceParameterError() DEVICE_STATUS_TYPE type error");
                    isParameterError = true;
                    break;
                }
                break;
            case 9:
                if (!(9 == awarenessFence.getType() && 4 == awarenessFence.getStatus() && fenceAction == awarenessFence.getAction())) {
                    LogUtil.e(TAG, "isFenceParameterError() DEVICE_USE_TYPE type or status or action error");
                    isParameterError = true;
                    break;
                }
            case 10:
                if (10 == awarenessFence.getType() && 4 == awarenessFence.getStatus() && fenceAction == awarenessFence.getAction()) {
                    if (action > 16383 || action < 1) {
                        LogUtil.e(TAG, "isFenceParameterError() broadcast event action  error");
                        isParameterError = true;
                        break;
                    }
                } else {
                    LogUtil.e(TAG, "isFenceParameterError() COMMON_SYSTEM_EVENT_TRIGGER_TYPE type or status or action error");
                    isParameterError = true;
                    break;
                }
                break;
            default:
                LogUtil.e(TAG, "isFenceParameterError() unknown type error");
                isParameterError = true;
                break;
        }
        LogUtil.d(TAG, "isFenceParameterError() isParameterError : " + isParameterError);
        return isParameterError;
    }

    public RequestResult getSupportAwarenessCapability(int type) {
        LogUtil.d(TAG, "getSupportAwarenessCapability() type : " + type);
        RequestResult result = new RequestResult();
        switch (type) {
            case 1:
                int capability = ConnectServiceManager.getInstance().getMovementCapability();
                if (capability >= 0) {
                    result.setAction(capability);
                    result.setResultType(5);
                } else {
                    result = new RequestResult(AwarenessConstants.ERROR_SERVICE_NOT_CONNECTED_CODE, AwarenessConstants.ERROR_SERVICE_NOT_CONNECTED);
                    result.setResultType(4);
                }
                LogUtil.d(TAG, "getSupportAwarenessCapability() MOVEMENT_TYPE result : " + result);
                break;
            default:
                result = new RequestResult(AwarenessConstants.ERROR_PARAMETER_CODE, AwarenessConstants.ERROR_PARAMETER);
                result.setResultType(3);
                break;
        }
        result.setType(type);
        LogUtil.d(TAG, "getSupportAwarenessCapability() result : " + result);
        return result;
    }

    public boolean registerMovementFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) {
        LogUtil.d(TAG, "registerMovementFence() awarenessFence : " + awarenessFence);
        boolean registerSuccess = registerFence(1, -1, callback, awarenessFence, null, operationPI);
        LogUtil.d(TAG, "registerMovementFence()  registerSuccess : " + registerSuccess);
        return registerSuccess;
    }

    public boolean registerDeviceStatusFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) {
        LogUtil.d(TAG, "registerDeviceStatusFence() awarenessFence : " + awarenessFence);
        boolean registerSuccess = registerFence(3, -1, callback, awarenessFence, null, operationPI);
        LogUtil.d(TAG, "registerDeviceStatusFence()  registerSuccess : " + registerSuccess);
        return registerSuccess;
    }

    public boolean isIntegrateSensorHub() {
        boolean sensorHubIsIntegrated = ConnectServiceManager.getInstance().isIntegradeSensorHub();
        LogUtil.d(TAG, "isIntegrateSensorHub() sensorHubIsIntegrated : " + sensorHubIsIntegrated);
        return sensorHubIsIntegrated;
    }
}
