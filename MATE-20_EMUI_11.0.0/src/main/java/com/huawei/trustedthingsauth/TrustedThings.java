package com.huawei.trustedthingsauth;

import android.content.Context;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import com.huawei.trustedthingsauth.ITrustedThingsCallback;
import com.huawei.trustedthingsauth.MonitorServiceTask;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TrustedThings {
    private static final int MAP_SIZE = 6;
    private static final String TAG = "TrustedThings";
    private static Map<String, HwTrustedThingsCallback> trustedThingsCallbackMap = new HashMap(6);
    private Context context;
    private String deviceId;
    private final Object lock = new Object();
    private MonitorServiceTask.TimeoutCallback timeoutCallback = new MonitorServiceTask.TimeoutCallback() {
        /* class com.huawei.trustedthingsauth.TrustedThings.AnonymousClass1 */

        @Override // com.huawei.trustedthingsauth.MonitorServiceTask.TimeoutCallback
        public void onResult() {
            LogUtil.info(TrustedThings.TAG, "MonitorServiceTask timeout callback.");
        }
    };

    public interface ResultCallback {
        void onResult(int i);
    }

    public TrustedThings(@NonNull Context context2, @NonNull String deviceId2) {
        this.context = context2;
        this.deviceId = deviceId2;
    }

    public void isFeatureSupported(final ResultCallback callback) {
        if (callback != null && this.context != null) {
            synchronized (this.lock) {
                LogUtil.info(TAG, "Receive client's isFeatureSupported request.");
                TrustedThingsServiceManager.connectService(this.context).ifPresent(new Consumer<ITrustedThings>() {
                    /* class com.huawei.trustedthingsauth.TrustedThings.AnonymousClass2 */

                    public void accept(ITrustedThings trustedThingsService) {
                        MonitorServiceTask.getInstance().startOrRestartTimerTask(TrustedThings.this.timeoutCallback);
                        try {
                            trustedThingsService.isFeatureSupported(TrustedThings.this.deviceId, new HwTrustedThingsCallback(callback));
                        } catch (RemoteException e) {
                            LogUtil.error(TrustedThings.TAG, "RemoteException occurred when invoking isFeatureSupported.");
                        }
                    }
                });
            }
        }
    }

    public void notifySetUp(final ResultCallback callback) {
        if (callback != null && this.context != null) {
            synchronized (this.lock) {
                LogUtil.info(TAG, "Receive client's notifySetUp request.");
                TrustedThingsServiceManager.connectService(this.context).ifPresent(new Consumer<ITrustedThings>() {
                    /* class com.huawei.trustedthingsauth.TrustedThings.AnonymousClass3 */

                    public void accept(ITrustedThings trustedThingsService) {
                        MonitorServiceTask.getInstance().startOrRestartTimerTask(TrustedThings.this.timeoutCallback);
                        try {
                            trustedThingsService.notifySetUp(TrustedThings.this.deviceId, new HwTrustedThingsCallback(callback));
                        } catch (RemoteException e) {
                            LogUtil.error(TrustedThings.TAG, "RemoteException occurred when invoking notifySetUp.");
                        }
                    }
                });
            }
        }
    }

    public void startAuth(final ResultCallback callback) {
        if (callback != null && this.context != null) {
            synchronized (this.lock) {
                LogUtil.info(TAG, "Receive client's startAuth request.");
                TrustedThingsServiceManager.connectService(this.context).ifPresent(new Consumer<ITrustedThings>() {
                    /* class com.huawei.trustedthingsauth.TrustedThings.AnonymousClass4 */

                    public void accept(ITrustedThings trustedThingsService) {
                        MonitorServiceTask.getInstance().startOrRestartTimerTask(TrustedThings.this.timeoutCallback);
                        HwTrustedThingsCallback trustedThingsCallback = new HwTrustedThingsCallback(callback);
                        TrustedThings.trustedThingsCallbackMap.put(TrustedThings.this.deviceId, trustedThingsCallback);
                        try {
                            trustedThingsService.startAuth(TrustedThings.this.deviceId, trustedThingsCallback);
                        } catch (RemoteException e) {
                            LogUtil.error(TrustedThings.TAG, "RemoteException occurred when invoking startAuth.");
                        }
                    }
                });
            }
        }
    }

    public void stopAuth() {
        if (this.context != null) {
            synchronized (this.lock) {
                LogUtil.info(TAG, "Receive client's stopAuth request.");
                TrustedThingsServiceManager.connectService(this.context).ifPresent(new Consumer<ITrustedThings>() {
                    /* class com.huawei.trustedthingsauth.TrustedThings.AnonymousClass5 */

                    public void accept(ITrustedThings trustedThingsService) {
                        MonitorServiceTask.getInstance().startOrRestartTimerTask(TrustedThings.this.timeoutCallback);
                        TrustedThings.trustedThingsCallbackMap.remove(TrustedThings.this.deviceId);
                        try {
                            trustedThingsService.stopAuth(TrustedThings.this.deviceId);
                        } catch (RemoteException e) {
                            LogUtil.error(TrustedThings.TAG, "RemoteException occurred when invoking stopAuth.");
                        }
                    }
                });
            }
        }
    }

    static Map<String, HwTrustedThingsCallback> getTrustedThingsCallbackMap() {
        return trustedThingsCallbackMap;
    }

    /* access modifiers changed from: package-private */
    public static class HwTrustedThingsCallback extends ITrustedThingsCallback.Stub {
        ResultCallback callback;

        HwTrustedThingsCallback(ResultCallback callback2) {
            this.callback = callback2;
        }

        @Override // com.huawei.trustedthingsauth.ITrustedThingsCallback
        public void onResult(int result) throws RemoteException {
            LogUtil.info(TrustedThings.TAG, "HwTrustedThingsCallback onResult");
            this.callback.onResult(result);
        }
    }
}
