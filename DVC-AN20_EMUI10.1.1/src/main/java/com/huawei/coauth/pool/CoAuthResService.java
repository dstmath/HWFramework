package com.huawei.coauth.pool;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.coauthservice.pool.ExecutorCallback;
import com.huawei.coauthservice.pool.SecureRegCallBack;
import java.util.List;
import java.util.Objects;

public class CoAuthResService extends Service {
    private static final String HW_COAUTH_RESOURCE_CLIENT_ACTION = "huawei.security.startreg";
    private static final String HW_COAUTH_RESOURCE_POOL_ACTION = "huawei.security.coauthregister";
    private static final String HW_COAUTH_SERVICE = "com.huawei.coauthservice";
    public static final String KEY_DATA = "data";
    public static final String KEY_SIGN = "sign";
    public static final int RESULT_FAIL = -1;
    public static final int RESULT_SUCCESS = 0;
    private static final String TAG = CoAuthResService.class.getName();
    private SecureRegCallBack mHwAuthResPoolService = null;
    private HwCoAuthResClientService mHwCoAuthResClientService = new HwCoAuthResClientService();
    private boolean mIsBindSucc = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class com.huawei.coauth.pool.CoAuthResService.AnonymousClass1 */

        public void onServiceConnected(ComponentName name, IBinder binder) {
            CoAuthResService.this.mHwAuthResPoolService = SecureRegCallBack.Stub.asInterface(binder);
            Log.i(CoAuthResService.TAG, "onServiceConnected");
            try {
                binder.linkToDeath(new IBinder.DeathRecipient() {
                    /* class com.huawei.coauth.pool.CoAuthResService.AnonymousClass1.AnonymousClass1 */

                    public void binderDied() {
                        CoAuthResService.this.mHwAuthResPoolService = null;
                        CoAuthResService.this.onDisConnected();
                    }
                }, 0);
            } catch (RemoteException e) {
                Log.e(CoAuthResService.TAG, "linkToDeath bind fail!");
            }
            CoAuthResService.this.onConnected();
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.i(CoAuthResService.TAG, "onServiceDisconnected");
            CoAuthResService.this.mHwAuthResPoolService = null;
            CoAuthResService.this.onDisConnected();
        }
    };

    public void onCreate() {
        super.onCreate();
    }

    public void onDestroy() {
        if (this.mIsBindSucc) {
            unbindService(this.mServiceConnection);
            this.mHwAuthResPoolService = null;
        }
        super.onDestroy();
    }

    private final class HwCoAuthResClientService extends ExecutorCallback.Stub {
        private HwCoAuthResClientService() {
        }

        @Override // com.huawei.coauthservice.pool.ExecutorCallback
        public int beginExecute(long sessionId, byte[] publicKey, Bundle param) throws RemoteException {
            return CoAuthResService.this.onPrepare(sessionId, publicKey, param);
        }

        @Override // com.huawei.coauthservice.pool.ExecutorCallback
        public int endExecute(long sessionId) throws RemoteException {
            return CoAuthResService.this.onFinish(sessionId);
        }

        @Override // com.huawei.coauthservice.pool.ExecutorCallback
        public int onReceiveData(long sessionId, long transNum, int srcType, int dstType, Bundle params) throws RemoteException {
            return CoAuthResService.this.onReceiveMessage(sessionId, transNum, srcType, dstType, params);
        }

        @Override // com.huawei.coauthservice.pool.ExecutorCallback
        public int setProperty(byte[] property, byte[] value) throws RemoteException {
            return CoAuthResService.this.onSetProperty(property, value);
        }

        @Override // com.huawei.coauthservice.pool.ExecutorCallback
        public byte[] getProperty(byte[] property) throws RemoteException {
            return CoAuthResService.this.onGetProperty(property);
        }
    }

    public IBinder onBind(Intent intent) {
        if (HW_COAUTH_RESOURCE_CLIENT_ACTION.equals(intent.getAction()) && this.mHwAuthResPoolService == null) {
            this.mIsBindSucc = bindCoauthRes();
        }
        return new Binder();
    }

    private Intent getExplicitIntent(Intent implicitIntent) {
        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentServices(implicitIntent, 0);
        if (resolveInfoList.size() <= 0) {
            return implicitIntent;
        }
        ResolveInfo serviceInfo = resolveInfoList.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        String str = TAG;
        Log.i(str, packageName + " | " + className);
        ComponentName component = new ComponentName(packageName, className);
        Intent explicitIntent = new Intent(implicitIntent);
        explicitIntent.setComponent(component);
        return explicitIntent;
    }

    private boolean bindCoauthRes() {
        Intent intent = new Intent(HW_COAUTH_RESOURCE_POOL_ACTION);
        intent.setPackage(HW_COAUTH_SERVICE);
        return bindService(getExplicitIntent(intent), this.mServiceConnection, 0);
    }

    public int onPrepare(long sessionId, byte[] publicKey, Bundle param) {
        return 0;
    }

    public int onFinish(long sessionId) {
        return 0;
    }

    public int onReceiveMessage(long sessionId, long transNum, int srcType, int dstType, Bundle params) {
        return 0;
    }

    public int onSetProperty(byte[] property, byte[] value) {
        return 0;
    }

    public byte[] onGetProperty(byte[] property) {
        return new byte[0];
    }

    public void onConnected() {
    }

    public void onDisConnected() {
    }

    public int registerToHwCoAuth(int executorType, String gid, List<String> executors, byte[] publicKey) {
        if (Objects.isNull(this.mHwAuthResPoolService)) {
            return -1;
        }
        try {
            return this.mHwAuthResPoolService.executorSecureRegister(executorType, gid, executors, publicKey, this.mHwCoAuthResClientService);
        } catch (RemoteException e) {
            Log.e(TAG, "registerToHwCoAuth RemoteException!");
            return -1;
        }
    }

    public int registerToHwCoAuth(int executorType, String gid, List<String> executors, byte[] publicKey, ExecutorCallback callback) {
        if (Objects.isNull(this.mHwAuthResPoolService)) {
            return -1;
        }
        try {
            return this.mHwAuthResPoolService.executorSecureRegister(executorType, gid, executors, publicKey, callback);
        } catch (RemoteException e) {
            Log.e(TAG, "registerToHwCoAuth RemoteException!");
            return -1;
        }
    }

    public int unRegisterFromHwCoAuth(List<String> executors) {
        if (Objects.isNull(this.mHwAuthResPoolService)) {
            return -1;
        }
        try {
            return this.mHwAuthResPoolService.executorSecureUnregister(executors);
        } catch (RemoteException e) {
            Log.e(TAG, "unRegisterFromHwCoAuth RemoteException!");
            return -1;
        }
    }

    public int notifyHwCoAuthToFinish(long sessionId, int resultCode, Bundle params) {
        if (Objects.isNull(this.mHwAuthResPoolService)) {
            return -1;
        }
        try {
            return this.mHwAuthResPoolService.executeFinish(sessionId, resultCode, params);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyHwCoAuthToFinish RemoteException!");
            return -1;
        }
    }

    public int sendMessage(long sessionId, long transNum, int srcType, int dstType, Bundle params) {
        if (Objects.isNull(this.mHwAuthResPoolService)) {
            return -1;
        }
        try {
            return this.mHwAuthResPoolService.executeSendData(sessionId, transNum, srcType, dstType, params);
        } catch (RemoteException e) {
            Log.e(TAG, "sendMessage RemoteException!");
            return -1;
        }
    }
}
