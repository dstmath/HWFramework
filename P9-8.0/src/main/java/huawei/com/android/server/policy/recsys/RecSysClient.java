package huawei.com.android.server.policy.recsys;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.recsys.aidl.HwHighLightRecResult;
import com.huawei.recsys.aidl.HwObjectContainer;
import com.huawei.recsys.aidl.HwRecSysAidlInterface;
import com.huawei.recsys.aidl.IHwRecSysCallBack.Stub;

public class RecSysClient {
    private static final String BIND_ACTION = "com.huawei.recsys.action.THIRD_REQUEST_ENGINE";
    public static final String BUSINESS_NAME = "AutoPowerOnOff";
    private static final String SERVER_PAKAGE_NAME = "com.huawei.recsys";
    private static final String TAG = ("[ShutdownRecommend]" + RecSysClient.class.getSimpleName());
    private Context mContext;
    private HwRecSysAidlInterface mHwRecSysAidlInterface;
    private Stub mHwRecSysCallBack = new Stub() {
        public void onRecResult(HwObjectContainer hwObjectContainer) throws RemoteException {
            HwLog.d(RecSysClient.TAG, "HwRecSysCallBack onRecResult");
            for (HwHighLightRecResult hwHighLightRecResult : hwObjectContainer.get()) {
                RecSysClient.this.mRequestResult = hwHighLightRecResult != null;
            }
            HwLog.i(RecSysClient.TAG, "shutdown recommend request result: " + RecSysClient.this.mRequestResult);
        }

        public void onConfigResult(int resCode, String message) throws RemoteException {
            HwLog.e(RecSysClient.TAG, "onConfigResult resCode:" + resCode + "  message:" + message);
        }
    };
    private boolean mRequestResult = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecSysClient.this.mHwRecSysAidlInterface = HwRecSysAidlInterface.Stub.asInterface(service);
            HwLog.i(RecSysClient.TAG, "onServiceConnected");
            if (RecSysClient.this.mHwRecSysAidlInterface != null) {
                try {
                    RecSysClient.this.mHwRecSysAidlInterface.registerCallBack(RecSysClient.this.mHwRecSysCallBack, RecSysClient.this.mContext.getPackageName());
                } catch (RemoteException e) {
                    HwLog.d(RecSysClient.TAG, e.getMessage());
                }
                HwLog.d(RecSysClient.TAG, "ServiceConnection.onServiceConnected is registerCallBack ok");
                return;
            }
            HwLog.w(RecSysClient.TAG, "ServiceConnection.onServiceConnected mRequestWeather is null");
        }

        public void onServiceDisconnected(ComponentName name) {
            if (RecSysClient.this.mHwRecSysAidlInterface != null) {
                try {
                    RecSysClient.this.mHwRecSysAidlInterface.unregisterCallBack(RecSysClient.this.mHwRecSysCallBack, RecSysClient.this.mContext.getPackageName());
                } catch (RemoteException e) {
                    HwLog.d(RecSysClient.TAG, e.getMessage());
                }
            } else {
                HwLog.w(RecSysClient.TAG, "ServiceConnection.onServiceConnected mRequestWeather is null");
            }
            RecSysClient.this.mHwRecSysAidlInterface = null;
            HwLog.i(RecSysClient.TAG, "onServiceDisconnected");
        }
    };

    public RecSysClient(Context context) {
        if (context == null) {
            HwLog.e(TAG, "context is null!");
        } else {
            this.mContext = context;
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public void bindRecService() {
        HwLog.i(TAG, "bindRecService()");
        if (this.mContext != null) {
            try {
                Intent intent = new Intent();
                intent.setAction(BIND_ACTION);
                intent.setPackage(SERVER_PAKAGE_NAME);
                this.mContext.bindService(intent, this.mServiceConnection, 1);
            } catch (Exception e) {
                HwLog.e(TAG, "bind recommend service fail");
            }
        }
    }

    public void unbindRecService() {
        HwLog.i(TAG, "unbindRecService()");
        if (!(this.mContext == null || this.mServiceConnection == null)) {
            try {
                this.mContext.unbindService(this.mServiceConnection);
            } catch (Exception e) {
                HwLog.e(TAG, "release exception");
            }
        }
        this.mHwRecSysAidlInterface = null;
        this.mContext = null;
    }

    public void requestRecRes(String jobName) {
        HwLog.d(TAG, "requestRecRes");
        try {
            if (this.mHwRecSysCallBack == null || this.mHwRecSysAidlInterface == null) {
                HwLog.w(TAG, "warning !!! mHwRecSysCallBack or mHwRecSysAidlInterface is null, requestRecRes() can not be called back, make sure the recsystem service has connected!!");
            } else {
                this.mHwRecSysAidlInterface.requestRecRes(this.mHwRecSysCallBack, jobName);
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, e.getMessage());
        }
    }

    public boolean getRequestResult() {
        return this.mRequestResult;
    }

    public void resetRequestResult() {
        this.mRequestResult = false;
    }
}
