package com.huawei.hiai.awareness.service;

import android.os.RemoteException;
import com.huawei.hiai.awareness.common.ThreadPoolManager;
import com.huawei.hiai.awareness.common.Utils;
import com.huawei.hiai.awareness.common.log.LogUtil;
import com.huawei.opcollect.collector.receivercollection.SysEventUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceBindingManager {
    /* access modifiers changed from: private */
    public static final String TAG = ServiceBindingManager.class.getName();
    private static ConcurrentHashMap<String, ConcurrentHashMap<String, IRequestCallBack>> packageCallbackMap = new ConcurrentHashMap<>();
    private static ServiceBindingManager serviceBindingManager = null;
    private Map<Integer, String> statusConfigMap = new HashMap();

    private static class RequestRunnable implements Runnable {
        private IRequestCallBack iRequestCallBack;
        RequestResult result;

        private RequestRunnable(IRequestCallBack iRequestCallBack2, RequestResult result2) {
            this.iRequestCallBack = iRequestCallBack2;
            this.result = result2;
        }

        public void run() {
            try {
                LogUtil.d(ServiceBindingManager.TAG, "RequestRunnable run() start");
                this.iRequestCallBack.onRequestResult(this.result);
            } catch (RemoteException e) {
                LogUtil.d(ServiceBindingManager.TAG, "RequestRunnable run() RemoteException ");
            }
        }
    }

    private ServiceBindingManager() {
        this.statusConfigMap.put(1, SysEventUtil.ON);
        this.statusConfigMap.put(3, "3");
        this.statusConfigMap.put(6, "6");
    }

    public static ServiceBindingManager getInstance() {
        ServiceBindingManager serviceBindingManager2;
        synchronized (ServiceBindingManager.class) {
            if (serviceBindingManager == null) {
                serviceBindingManager = new ServiceBindingManager();
            }
            serviceBindingManager2 = serviceBindingManager;
        }
        return serviceBindingManager2;
    }

    public boolean isFenceFunctionSupported(int type) {
        switch (type) {
            case 1:
            case 3:
                if (!Utils.checkMsdpInstalled(ConnectServiceManager.getInstance().getConnectServiceManagerContext())) {
                    return false;
                }
                break;
        }
        return true;
    }

    private RequestResult buildRequestResultFromAwareness(AwarenessFence awarenessFence, int resultType, int triggerStatus) {
        LogUtil.d(TAG, "buildRequestResultFromAwareness() awarenessFence :  " + awarenessFence + " resultType : " + resultType + " triggerStatus : " + triggerStatus);
        RequestResult result = null;
        if (awarenessFence != null) {
            int i = triggerStatus;
            result = new RequestResult(awarenessFence.getType(), awarenessFence.getStatus(), awarenessFence.getAction(), null, System.currentTimeMillis(), System.currentTimeMillis(), 100);
            result.setRegisterTopKey(awarenessFence.getTopKey());
            result.setContent(null);
            result.setResultType(resultType);
            result.setTriggerStatus(triggerStatus);
        }
        LogUtil.d(TAG, "buildRequestResultFromAwareness() result : " + result);
        return result;
    }

    public void registerResultCallback(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, int triggerStatus, int resultCode) {
        LogUtil.d(TAG, "registerResultCallback awarenessFence : " + awarenessFence);
        if (iRequestCallBack != null && awarenessFence != null) {
            RequestResult result = buildRequestResultFromAwareness(awarenessFence, 1, triggerStatus);
            if (200009 == resultCode) {
                result.setErrorCode(resultCode);
                result.setErrorResult("Unsupport action: " + awarenessFence.getAction());
            }
            ThreadPoolManager.getInstance().startInCacheChildThread(new RequestRunnable(iRequestCallBack, result));
        }
    }
}
