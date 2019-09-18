package com.huawei.hiai.awareness.service;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.hiai.awareness.AwarenessConstants;
import com.huawei.hiai.awareness.awareness.Event;
import com.huawei.hiai.awareness.common.Utils;
import com.huawei.hiai.awareness.common.log.LogUtil;
import com.huawei.hiai.awareness.movement.MovementController;
import com.huawei.hiai.awareness.service.IAwarenessService;

public class AwarenessManager {
    private static final int MSDP_SERVICE_CONNECTION_CYCLE = 2000;
    private static final int MSG_MSDP_SERVICE_CONNECTION = 1;
    private static final String MSG_THREAD_MONITOR_MSDP_CON = "MonitorMSDPConThread";
    private static final String TAG = "AwarenessManager";
    private static final int TRY_CONNECT_TIMES = 10;
    private static int mTryConnectionTimes = 0;
    /* access modifiers changed from: private */
    public AwarenessServiceConnection mAwarenessServiceConnection = null;
    private Context mContext = null;
    private MsdpConMsgHandler mHandler;
    IAwarenessService mIAwarenessService;
    boolean mIsConnectedMsdpService = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            AwarenessManager.this.mIAwarenessService = IAwarenessService.Stub.asInterface(service);
            if (AwarenessManager.this.mAwarenessServiceConnection != null) {
                LogUtil.i(AwarenessManager.TAG, "service connect");
                AwarenessManager.this.mAwarenessServiceConnection.onServiceConnected();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            AwarenessManager.this.mIAwarenessService = null;
            if (AwarenessManager.this.mAwarenessServiceConnection != null) {
                LogUtil.i(AwarenessManager.TAG, "service disconnect");
                AwarenessManager.this.mAwarenessServiceConnection.onServiceDisconnected();
            }
        }
    };

    private final class MsdpConMsgHandler extends Handler {
        MsdpConMsgHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    LogUtil.d(AwarenessManager.TAG, "receive Msdp Connction request!");
                    AwarenessManager.this.dealwithMsdpConnction();
                    return;
                default:
                    Log.e(AwarenessManager.TAG, "handleMessage error msg.");
                    return;
            }
        }
    }

    public AwarenessManager(Context context) {
        LogUtil.d(TAG, "AwarenessManager()");
        if (context != null) {
            this.mContext = context;
        } else {
            LogUtil.e(TAG, "AwarenessManager() context == null");
        }
    }

    public boolean connectService(AwarenessServiceConnection awarenessServiceConnection) {
        LogUtil.d(TAG, "connectService()");
        if (this.mContext == null) {
            LogUtil.e(TAG, "connectService() mContext == null");
            return false;
        }
        boolean isConnectSuccess = false;
        if (awarenessServiceConnection != null) {
            this.mAwarenessServiceConnection = awarenessServiceConnection;
            if (this.mIAwarenessService == null) {
                isConnectSuccess = bindService();
            } else {
                LogUtil.i(TAG, "connectService() mIAwarenessService != null");
            }
        } else {
            LogUtil.e(TAG, "connectService() awarenessServiceConnection == null");
        }
        LogUtil.i(TAG, "connectService() isConnectSuccess = " + isConnectSuccess + ",getPackageName: " + this.mContext.getPackageName());
        return isConnectSuccess;
    }

    /* access modifiers changed from: private */
    public void dealwithMsdpConnction() {
        LogUtil.i(TAG, "dealwithMsdpConnction() mIsConnectedMsdpService = " + this.mIsConnectedMsdpService + ",getPackageName: " + this.mContext.getPackageName());
        if (this.mIsConnectedMsdpService) {
            LogUtil.d(TAG, "dealwithMsdpConnction()， quit handler!");
            this.mHandler.getLooper().quit();
            this.mHandler = null;
            mTryConnectionTimes = 0;
            this.mAwarenessServiceConnection.onServiceConnected();
        } else if (this.mHandler != null && mTryConnectionTimes < 10) {
            this.mIsConnectedMsdpService = ConnectServiceManager.getInstance().onStart();
            LogUtil.d(TAG, "execute dealwithMsdpConnction() handler! mTryConnectionTimes:" + mTryConnectionTimes);
            mTryConnectionTimes++;
            this.mHandler.sendEmptyMessageDelayed(1, 2000);
        }
    }

    public boolean connectMsdpService(AwarenessServiceConnection awarenessServiceConnection) {
        LogUtil.d(TAG, "connectMsdpService()");
        if (this.mContext == null) {
            LogUtil.e(TAG, "connectMsdpService() mContext == null");
            return false;
        }
        HandlerThread handleThread = new HandlerThread(MSG_THREAD_MONITOR_MSDP_CON);
        handleThread.start();
        this.mHandler = new MsdpConMsgHandler(handleThread.getLooper());
        ConnectServiceManager.getInstance().initialize(this.mContext);
        ConnectServiceManager.getInstance().setConnectServiceManagerContext(this.mContext);
        if (Utils.checkMsdpInstalled(this.mContext.getApplicationContext())) {
            this.mAwarenessServiceConnection = awarenessServiceConnection;
            dealwithMsdpConnction();
        }
        return true;
    }

    private boolean bindService() {
        LogUtil.d(TAG, "bindService()");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.huawei.hiai", AwarenessConstants.AWARENESS_SERVICE_CLASS_NAME));
        intent.setAction(AwarenessConstants.AWARENESS_SERVICE_ACTION_NAME);
        intent.putExtra(AwarenessConstants.LAUNCH_AWARENESS_PACKAGE_NAME, this.mContext.getPackageName());
        boolean isBindSuccess = false;
        try {
            return this.mContext.bindService(intent, this.mServiceConnection, 1);
        } catch (SecurityException e) {
            LogUtil.e(TAG, "bindService() Exception");
            return isBindSuccess;
        }
    }

    public boolean disconnectService() {
        LogUtil.d(TAG, "disconnectService()");
        if (this.mIsConnectedMsdpService) {
            this.mIsConnectedMsdpService = false;
            LogUtil.d(TAG, "disconnectService(), mIsConnectedMsdpService:" + this.mIsConnectedMsdpService);
            ConnectServiceManager.getInstance().stopConnectService();
            this.mAwarenessServiceConnection.onServiceDisconnected();
            return true;
        } else if (this.mIAwarenessService == null) {
            LogUtil.e(TAG, "disconnectService() mIAwarenessService == null.");
            return false;
        } else {
            this.mContext.unbindService(this.mServiceConnection);
            if (this.mAwarenessServiceConnection != null) {
                this.mAwarenessServiceConnection.onServiceDisconnected();
            }
            this.mIAwarenessService = null;
            return true;
        }
    }

    public RequestResult getCurrentMotionStatus() {
        LogUtil.d(TAG, "getCurrentMotionStatus()");
        RequestResult result = getCurrentStatus(1);
        LogUtil.d(TAG, "getCurrentMotionStatus() result : " + result);
        return result;
    }

    public RequestResult getCurrentPhoneStatus() {
        LogUtil.d(TAG, "getCurrentPhoneStatus()");
        RequestResult result = getCurrentStatus(3);
        LogUtil.d(TAG, "getCurrentPhoneStatus() result : " + result);
        return result;
    }

    public String getAwarenessApiVersion() {
        if (this.mIsConnectedMsdpService) {
            if (isMSDPIntegrationSensorHub()) {
                return AwarenessInnerConstants.AWARENESS_VERSION_CODE;
            }
            LogUtil.e(TAG, "getAwarenessApiVersion() old version!");
            return null;
        } else if (this.mIAwarenessService != null) {
            try {
                LogUtil.d(TAG, "getAwarenessApiVersion() call binder");
                String version = this.mIAwarenessService.getAwarenessApiVersion();
                LogUtil.d(TAG, "getAwarenessApiVersion() version : " + version);
                return version;
            } catch (RemoteException e) {
                LogUtil.e(TAG, "getAwarenessApiVersion() RemoteException");
                return null;
            }
        } else {
            LogUtil.e(TAG, "getAwarenessApiVersion() mIAwarenessService = null");
            return null;
        }
    }

    private boolean isMSDPIntegrationSensorHub() {
        return ConnectServiceManager.getInstance().isIntegradeSensorHub();
    }

    public RequestResult getCurrentAwareness(int type) {
        return getCurrentAwareness(type, false, null);
    }

    public RequestResult getCurrentAwareness(int type, boolean isCustom, Bundle bundle) {
        LogUtil.d(TAG, "getCurrentAwareness() type : " + type + " isCustom : " + isCustom);
        if (this.mIsConnectedMsdpService) {
            return getCurrentStatus(type);
        }
        if (this.mIAwarenessService != null) {
            try {
                LogUtil.d(TAG, "getCurrentAwareness() call binder");
                return this.mIAwarenessService.getCurrentAwareness(type, isCustom, bundle, this.mContext != null ? this.mContext.getPackageName() : null);
            } catch (RemoteException e) {
                LogUtil.e(TAG, "getCurrentAwareness() RemoteException");
                RequestResult result = new RequestResult(AwarenessConstants.ERROR_UNKNOWN_CODE, AwarenessConstants.ERROR_UNKNOWN);
                result.setResultType(3);
                return result;
            }
        } else {
            LogUtil.e(TAG, "getCurrentAwareness() mIAwarenessService = null");
            RequestResult result2 = new RequestResult(AwarenessConstants.ERROR_UNKNOWN_CODE, AwarenessConstants.ERROR_UNKNOWN);
            result2.setResultType(3);
            return result2;
        }
    }

    public boolean registerMotionFence(IRequestCallBack callback, AwarenessFence awarenessFence, PendingIntent operationPI) {
        LogUtil.d(TAG, "registerMotionFence() awarenessFence : " + awarenessFence);
        boolean registerSuccess = registerFence(1, -1, callback, awarenessFence, null, operationPI);
        LogUtil.d(TAG, "registerMotionFence()  registerSuccess : " + registerSuccess);
        return registerSuccess;
    }

    public boolean registerTimeFence(IRequestCallBack callback, AwarenessFence awarenessFence, PendingIntent operationPI) {
        LogUtil.d(TAG, "registerTimeFence() awarenessFence : " + awarenessFence);
        boolean registerSuccess = registerFence(8, -1, callback, awarenessFence, null, operationPI);
        LogUtil.d(TAG, "registerTimeFence()  registerSuccess : " + registerSuccess);
        return registerSuccess;
    }

    public boolean registerLocationFence(IRequestCallBack callback, AwarenessFence awarenessFence, PendingIntent operationPI) {
        LogUtil.d(TAG, "registerLocationFence() awarenessFence : " + awarenessFence);
        boolean registerSuccess = registerFence(6, -1, callback, awarenessFence, null, operationPI);
        LogUtil.d(TAG, "registerLocationFence()  registerSuccess : " + registerSuccess);
        return registerSuccess;
    }

    public boolean registerCustomLocationFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, PendingIntent operationPI) {
        if (callback == null || awarenessFence == null || operationPI == null) {
            LogUtil.e(TAG, "registerCustomLocationFence() null == callback || null == awarenessFence || null == operationPI");
            return false;
        }
        boolean registerSuccess = false;
        try {
            awarenessFence.build(this.mContext);
            LogUtil.d(TAG, "registerCustomLocationFence() call binder awarenessFence :" + awarenessFence);
            if (AwarenessConstants.LOCATION_CUSTOM.equals(awarenessFence.getSecondAction())) {
                registerSuccess = this.mIAwarenessService.registerCustomLocationFence(callback, awarenessFence, null, operationPI);
            }
            LogUtil.d(TAG, "registerCustomLocationFence()  registerSuccess : " + registerSuccess);
            return registerSuccess;
        } catch (RemoteException e) {
            LogUtil.e(TAG, "registerCustomLocationFence() RemoteException");
            return false;
        }
    }

    public boolean registerAppUseTotalTimeFence(IRequestCallBack callback, AwarenessFence awarenessFence, PendingIntent operationPI) {
        LogUtil.d(TAG, "registerAppUseTotalTimeFence() awarenessFence : " + awarenessFence);
        boolean registerSuccess = registerFence(9, 1, callback, awarenessFence, null, operationPI);
        LogUtil.d(TAG, "registerAppUseTotalTimeFence()  registerSuccess : " + registerSuccess);
        return registerSuccess;
    }

    public boolean registerOneAppContinuousUseTimeFence(IRequestCallBack callback, AwarenessFence awarenessFence, PendingIntent operationPI) {
        LogUtil.d(TAG, "registerOneAppContinuousUseTimeFence() awarenessFence : " + awarenessFence);
        boolean registerSuccess = registerFence(9, 2, callback, awarenessFence, null, operationPI);
        LogUtil.d(TAG, "registerOneAppContinuousUseTimeFence()  registerSuccess : " + registerSuccess);
        return registerSuccess;
    }

    public boolean registerDeviceUseTotalTimeFence(IRequestCallBack callback, AwarenessFence awarenessFence, PendingIntent operationPI) {
        LogUtil.d(TAG, "registerDeviceUseTotalTimeFence() awarenessFence : " + awarenessFence);
        boolean registerSuccess = registerFence(9, 3, callback, awarenessFence, null, operationPI);
        LogUtil.d(TAG, "registerDeviceUseTotalTimeFence()  registerSuccess : " + registerSuccess);
        return registerSuccess;
    }

    public boolean registerScreenUnlockTotalNumberFence(IRequestCallBack callback, AwarenessFence awarenessFence, PendingIntent operationPI) {
        LogUtil.d(TAG, "registerScreenUnlockTotalNumberFence() awarenessFence : " + awarenessFence);
        boolean registerSuccess = registerFence(9, 4, callback, awarenessFence, null, operationPI);
        LogUtil.d(TAG, "registerScreenUnlockTotalNumberFence()  registerSuccess : " + registerSuccess);
        return registerSuccess;
    }

    public boolean registerScreenUnlockFence(IRequestCallBack callback, AwarenessFence awarenessFence, PendingIntent operationPI) {
        LogUtil.d(TAG, "registerScreenUnlockFence() awarenessFence : " + awarenessFence);
        boolean registerSuccess = registerFence(10, 1, callback, awarenessFence, null, operationPI);
        LogUtil.d(TAG, "registerScreenUnlockFence()  registerSuccess : " + registerSuccess);
        return registerSuccess;
    }

    public boolean registerBroadcastEventFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, PendingIntent operationPI, Intent intent) throws RemoteException {
        if (callback == null || awarenessFence == null || operationPI == null) {
            LogUtil.e(TAG, "registerBroadcastEventFence() null == callback || null == awarenessFence || null == operationPI");
            return false;
        }
        Bundle intentBundle = new Bundle();
        intentBundle.putParcelable(AwarenessConstants.REGISTER_BROADCAST_FENCE_INTENT, intent);
        awarenessFence.setRegisterBundle(intentBundle);
        try {
            awarenessFence.build(this.mContext);
            LogUtil.d(TAG, "registerBroadcastEventFence() call binder awarenessFence :" + awarenessFence);
            boolean registerSuccess = this.mIAwarenessService.registerBroadcastEventFence(callback, awarenessFence, null, operationPI);
            LogUtil.d(TAG, "registerBroadcastEventFence()  registerSuccess : " + registerSuccess);
            return registerSuccess;
        } catch (RemoteException e) {
            LogUtil.e(TAG, "registerBroadcastEventFence() RemoteException");
            return false;
        }
    }

    public RequestResult getFenceTriggerResult(AwarenessFence awarenessFence, PendingIntent operationPI) {
        RequestResult result;
        if (this.mIAwarenessService != null) {
            LogUtil.d(TAG, "getFenceTriggerResult() awarenessFence : " + awarenessFence);
            if (awarenessFence == null || operationPI == null) {
                LogUtil.e(TAG, "getFenceTriggerResult() null == awarenessFence || null == operationPI");
                return null;
            }
            LogUtil.d(TAG, "getFenceTriggerResult() operationPI.hashCode : " + operationPI.hashCode());
            try {
                awarenessFence.build(this.mContext);
                LogUtil.d(TAG, "getFenceTriggerResult() call binder");
                if (awarenessFence instanceof ExtendAwarenessFence) {
                    LogUtil.d(TAG, "getFenceTriggerResult() revert to ExtendAwarenessFence");
                    result = this.mIAwarenessService.getExtendFenceTriggerResult((ExtendAwarenessFence) awarenessFence, null, operationPI);
                } else {
                    LogUtil.d(TAG, "getFenceTriggerResult() is AwarenessFence");
                    result = this.mIAwarenessService.getFenceTriggerResult(awarenessFence, null, operationPI);
                }
                LogUtil.d(TAG, "getFenceTriggerResult() result : " + result);
                return result;
            } catch (RemoteException e) {
                LogUtil.e(TAG, "getFenceTriggerResult() RemoteException");
                RequestResult result2 = new RequestResult(AwarenessConstants.ERROR_UNKNOWN_CODE, AwarenessConstants.ERROR_UNKNOWN);
                result2.setResultType(3);
                return result2;
            }
        } else {
            LogUtil.e(TAG, "getFenceTriggerResult() mIAwarenessService = null");
            RequestResult result3 = new RequestResult(AwarenessConstants.ERROR_UNKNOWN_CODE, AwarenessConstants.ERROR_UNKNOWN);
            result3.setResultType(3);
            return result3;
        }
    }

    public boolean unRegisterFence(IRequestCallBack callback, AwarenessFence awarenessFence, PendingIntent operationPI) {
        if (this.mIsConnectedMsdpService) {
            return AwarenessBinder.getInstance().unRegisterFence(callback, awarenessFence, null, operationPI);
        }
        if (this.mIAwarenessService == null) {
            LogUtil.e(TAG, "unRegisterFence() mIAwarenessService = null");
            return false;
        } else if (callback == null || awarenessFence == null || operationPI == null) {
            LogUtil.e(TAG, "unRegisterFence() null == callback || null == awarenessFence || null == operationPI");
            return false;
        } else {
            LogUtil.d(TAG, "unRegisterFence() operationPI.hashCode : " + operationPI.hashCode());
            try {
                awarenessFence.build(this.mContext);
                LogUtil.d(TAG, "unRegisterFence() call binder awarenessFence :" + awarenessFence);
                if (awarenessFence instanceof ExtendAwarenessFence) {
                    LogUtil.d(TAG, "unRegisterFence() revert to ExtendAwarenessFence");
                    return this.mIAwarenessService.unRegisterExtendFence(callback, (ExtendAwarenessFence) awarenessFence, null, operationPI);
                }
                LogUtil.d(TAG, "unRegisterFence() is AwarenessFence");
                return this.mIAwarenessService.unRegisterFence(callback, awarenessFence, null, operationPI);
            } catch (RemoteException e) {
                LogUtil.e(TAG, "unRegisterFence() RemoteException");
                return false;
            }
        }
    }

    private boolean registerFence(int fenceType, int fenceAction, IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) {
        boolean registerSuccess;
        if (this.mIAwarenessService == null) {
            LogUtil.e(TAG, "registerFence() mIAwarenessService = null");
            return false;
        } else if (callback == null || awarenessFence == null || operationPI == null) {
            LogUtil.e(TAG, "registerFence() null == callback || null == awarenessFence || null == operationPI");
            return false;
        } else {
            LogUtil.d(TAG, "registerFence() operationPI.hashCode : " + operationPI.hashCode());
            try {
                awarenessFence.build(this.mContext);
                LogUtil.d(TAG, "registerFence() call binder awarenessFence :" + awarenessFence);
                switch (fenceType) {
                    case 6:
                        registerSuccess = this.mIAwarenessService.registerLocationFence(callback, awarenessFence, null, operationPI);
                        break;
                    case 8:
                        registerSuccess = this.mIAwarenessService.registerTimeFence(callback, awarenessFence, null, operationPI);
                        break;
                    case 9:
                        registerSuccess = registerDeviceUseTypeFence(fenceAction, callback, awarenessFence, null, operationPI);
                        break;
                    case 10:
                        registerSuccess = registerSystemEventTriggerTypeFence(fenceAction, callback, awarenessFence, null, operationPI);
                        break;
                    default:
                        registerSuccess = false;
                        break;
                }
                LogUtil.d(TAG, "registerFence() registerSuccess :" + registerSuccess);
                return registerSuccess;
            } catch (RemoteException e) {
                LogUtil.e(TAG, "registerFence() RemoteException");
                return false;
            }
        }
    }

    private boolean registerDeviceUseTypeFence(int fenceAction, IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) {
        boolean registerSuccess;
        LogUtil.d(TAG, "registerDeviceUseTypeFence() fenceAction :" + fenceAction);
        switch (fenceAction) {
            case 1:
                registerSuccess = this.mIAwarenessService.registerAppUseTotalTimeFence(callback, awarenessFence, null, operationPI);
                break;
            case 2:
                registerSuccess = this.mIAwarenessService.registerOneAppContinuousUseTimeFence(callback, awarenessFence, null, operationPI);
                break;
            case 3:
                registerSuccess = this.mIAwarenessService.registerDeviceUseTotalTimeFence(callback, awarenessFence, null, operationPI);
                break;
            case 4:
                registerSuccess = this.mIAwarenessService.registerScreenUnlockTotalNumberFence(callback, awarenessFence, null, operationPI);
                break;
            default:
                registerSuccess = false;
                break;
        }
        try {
            LogUtil.d(TAG, "registerDeviceUseTypeFence() registerSuccess :" + registerSuccess);
            return registerSuccess;
        } catch (RemoteException e) {
            LogUtil.e(TAG, "registerDeviceUseTypeFence() RemoteException");
            return false;
        }
    }

    private boolean registerSystemEventTriggerTypeFence(int fenceAction, IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) {
        boolean registerSuccess;
        LogUtil.d(TAG, "registerSystemEventTriggerTypeFence() fenceAction :" + fenceAction);
        switch (fenceAction) {
            case 1:
                registerSuccess = this.mIAwarenessService.registerScreenUnlockFence(callback, awarenessFence, null, operationPI);
                break;
            default:
                registerSuccess = false;
                break;
        }
        try {
            LogUtil.d(TAG, "registerSystemEventTriggerTypeFence() registerSuccess :" + registerSuccess);
            return registerSuccess;
        } catch (RemoteException e) {
            LogUtil.e(TAG, "registerSystemEventTriggerTypeFence() RemoteException");
            return false;
        }
    }

    private RequestResult getCurrentStatus(int type) {
        RequestResult result = null;
        if (this.mIsConnectedMsdpService) {
            switch (type) {
                case 1:
                    RequestResult result2 = buildRequestResultFromEvent(MovementController.getInstance().getMovenentStatusEvent(), 2, -1);
                    LogUtil.d(TAG, "getCurrentStatus() MOVEMENT_TYPE result : " + result2);
                    return result2;
                default:
                    return null;
            }
        } else if (this.mIAwarenessService != null) {
            try {
                LogUtil.d(TAG, "getCurrentStatus() call binder");
                return this.mIAwarenessService.getCurrentStatus(type);
            } catch (RemoteException e) {
                LogUtil.e(TAG, "getCurrentStatus() RemoteException");
                return result;
            }
        } else {
            LogUtil.e(TAG, "getCurrentStatus() mIAwarenessService = null");
            return result;
        }
    }

    public RequestResult getSupportAwarenessCapability(int type) {
        RequestResult requestResult = null;
        if (this.mIsConnectedMsdpService) {
            return AwarenessBinder.getInstance().getSupportAwarenessCapability(type);
        }
        if (this.mIAwarenessService != null) {
            try {
                return this.mIAwarenessService.getSupportAwarenessCapability(type);
            } catch (RemoteException e) {
                LogUtil.e(TAG, "getSupportAwarenessCapability() RemoteException");
                return requestResult;
            }
        } else {
            LogUtil.e(TAG, "getSupportAwarenessCapability() RemoteException");
            return requestResult;
        }
    }

    public boolean registerMovementFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, PendingIntent operationPI) {
        if (callback == null || awarenessFence == null || operationPI == null) {
            LogUtil.e(TAG, "registerMovementFence() null == callback || null == awarenessFence || null == operationPI");
            return false;
        } else if (this.mIsConnectedMsdpService) {
            awarenessFence.build(this.mContext);
            LogUtil.d(TAG, "registerMovementFence() call binder awarenessFence :" + awarenessFence + ",mIsConnectedMsdpService");
            return AwarenessBinder.getInstance().registerMovementFence(callback, awarenessFence, null, operationPI);
        } else if (this.mIAwarenessService != null) {
            try {
                awarenessFence.build(this.mContext);
                LogUtil.d(TAG, "registerMovementFence() call binder awarenessFence :" + awarenessFence);
                boolean registerSuccess = this.mIAwarenessService.registerMovementFence(callback, awarenessFence, null, operationPI);
                LogUtil.d(TAG, "registerMovementFence()  registerSuccess : " + registerSuccess);
                return registerSuccess;
            } catch (RemoteException e) {
                LogUtil.e(TAG, "registerMovementFence() RemoteException");
                return false;
            }
        } else {
            LogUtil.e(TAG, "registerMovementFence() exception");
            return false;
        }
    }

    public boolean registerDeviceStatusFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, PendingIntent operationPI) {
        if (callback == null || awarenessFence == null || operationPI == null) {
            LogUtil.e(TAG, "registerDeviceStatusFence() null == callback || null == awarenessFence || null == operationPI");
            return false;
        } else if (this.mIsConnectedMsdpService) {
            awarenessFence.build(this.mContext);
            LogUtil.d(TAG, "registerDeviceStatusFence() call binder awarenessFence :" + awarenessFence + ",mIsConnectedMsdpService");
            return AwarenessBinder.getInstance().registerDeviceStatusFence(callback, awarenessFence, null, operationPI);
        } else if (this.mIAwarenessService != null) {
            try {
                awarenessFence.build(this.mContext);
                LogUtil.d(TAG, "registerDeviceStatusFence() call binder awarenessFence :" + awarenessFence);
                boolean registerSuccess = this.mIAwarenessService.registerDeviceStatusFence(callback, awarenessFence, null, operationPI);
                LogUtil.d(TAG, "registerDeviceStatusFence()  registerSuccess : " + registerSuccess);
                return registerSuccess;
            } catch (RemoteException e) {
                LogUtil.e(TAG, "registerDeviceStatusFence() RemoteException");
                return false;
            }
        } else {
            LogUtil.e(TAG, "registerDeviceStatusFence() exception");
            return false;
        }
    }

    public RequestResult setReportPeriod(ExtendAwarenessFence awarenessFence) {
        RequestResult result;
        if (awarenessFence == null || awarenessFence.getRegisterBundle() == null) {
            LogUtil.e(TAG, "setReportPeriod(): illegal parameters!");
            RequestResult result2 = new RequestResult(AwarenessConstants.ERROR_PARAMETER_CODE, AwarenessConstants.ERROR_PARAMETER);
            result2.setResultType(7);
            return result2;
        } else if (this.mIsConnectedMsdpService) {
            switch (awarenessFence.getType()) {
                case 1:
                    result = MovementController.getInstance().doSetReportPeriod(awarenessFence);
                    break;
                default:
                    LogUtil.d(TAG, "setReportPeriod(): illegal parameters!");
                    result = new RequestResult(AwarenessConstants.ERROR_PARAMETER_CODE, AwarenessConstants.ERROR_PARAMETER);
                    result.setResultType(7);
                    break;
            }
            return result;
        } else if (this.mIAwarenessService != null) {
            try {
                LogUtil.d(TAG, "setReportPeriod() call binder");
                return this.mIAwarenessService.setReportPeriod(awarenessFence);
            } catch (RemoteException e) {
                LogUtil.e(TAG, "setReportPeriod() RemoteException");
                RequestResult result3 = new RequestResult(AwarenessConstants.ERROR_UNKNOWN_CODE, AwarenessConstants.ERROR_UNKNOWN);
                result3.setResultType(7);
                return result3;
            }
        } else {
            LogUtil.e(TAG, "setReportPeriod()");
            RequestResult result4 = new RequestResult(AwarenessConstants.ERROR_UNKNOWN_CODE, AwarenessConstants.ERROR_UNKNOWN);
            result4.setResultType(7);
            return result4;
        }
    }

    public boolean isIntegrateSensorHub() {
        boolean z = false;
        LogUtil.d(TAG, "isIntegrateSensorHub() mIsConnectedMsdpService:" + this.mIsConnectedMsdpService);
        if (this.mIsConnectedMsdpService) {
            return AwarenessBinder.getInstance().isIntegrateSensorHub();
        }
        if (this.mIAwarenessService != null) {
            try {
                return this.mIAwarenessService.isIntegrateSensorHub();
            } catch (RemoteException e) {
                LogUtil.e(TAG, "isIntegrateSensorHub() RemoteException");
                return z;
            }
        } else {
            LogUtil.e(TAG, "isIntegrateSensorHub() RemoteException");
            return z;
        }
    }

    private RequestResult buildRequestResultFromEvent(Event event, int resultType, int triggerStatus) {
        LogUtil.d(TAG, "buildRequestResultFromEvent() event :  " + event + " resultType : " + resultType + " triggerStatus : " + triggerStatus);
        RequestResult result = null;
        if (event != null) {
            int eventTriggerStatus = event.getEventTriggerStatus();
            result = new RequestResult(event.getEventCurType(), event.getEventCurStatus(), event.getEventCurAction(), null, event.getEventTime(), event.getEventSensorTime(), event.getEventConfidence());
            result.setRegisterTopKey(null);
            result.setContent(null);
            result.setResultType(resultType);
            result.setTriggerStatus(triggerStatus);
        }
        LogUtil.d(TAG, "buildRequestResultFromEvent() result : " + result);
        return result;
    }

    public boolean registerAppLifeChangeFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, PendingIntent operationPI, Intent intent) throws RemoteException {
        if (callback == null || awarenessFence == null || operationPI == null) {
            LogUtil.e(TAG, "registerAppLifeChangeFence() null == callback || null == awarenessFence || null == operationPI");
            return false;
        }
        Bundle intentBundle = new Bundle();
        intentBundle.putParcelable(AwarenessConstants.REGISTER_APP_LIFE_FENCE_INTENT, intent);
        awarenessFence.setRegisterBundle(intentBundle);
        try {
            awarenessFence.build(this.mContext);
            LogUtil.d(TAG, "registerAppLifeChangeFence() call binder awarenessFence :" + awarenessFence);
            boolean registerSuccess = this.mIAwarenessService.registerAppLifeChangeFence(callback, awarenessFence, null, operationPI);
            LogUtil.d(TAG, "registerAppLifeChangeFence()  registerSuccess : " + registerSuccess);
            return registerSuccess;
        } catch (RemoteException e) {
            LogUtil.e(TAG, "registerAppLifeChangeFence() RemoteException");
            return false;
        }
    }
}
