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
import com.huawei.recsys.aidl.IHwRecSysCallBack;
import java.util.Iterator;

public class RecSysClient {
    private static final String BIND_ACTION = "com.huawei.recsys.action.THIRD_REQUEST_ENGINE";
    public static final String BUSINESS_NAME = "AutoPowerOnOff";
    private static final String SERVER_PAKAGE_NAME = "com.huawei.recsys";
    private static final String TAG = ("[ShutdownRecommend]" + RecSysClient.class.getSimpleName());
    private Context mContext;
    private boolean mHasRequestResult = false;
    private HwRecSysAidlInterface mHwRecSysAidlInterface;
    private IHwRecSysCallBack.Stub mHwRecSysCallBack = new IHwRecSysCallBack.Stub() {
        /* class huawei.com.android.server.policy.recsys.RecSysClient.AnonymousClass2 */

        @Override // com.huawei.recsys.aidl.IHwRecSysCallBack
        public void onRecResult(HwObjectContainer hwObjectContainer) throws RemoteException {
            HwLog.d(RecSysClient.TAG, "HwRecSysCallBack onRecResult");
            Iterator<HwHighLightRecResult> it = hwObjectContainer.get().iterator();
            while (it.hasNext()) {
                HwHighLightRecResult hwHighLightRecResult = it.next();
                RecSysClient.this.mHasRequestResult = hwHighLightRecResult != null;
            }
            String str = RecSysClient.TAG;
            HwLog.i(str, "shutdown recommend request result: " + RecSysClient.this.mHasRequestResult);
        }

        @Override // com.huawei.recsys.aidl.IHwRecSysCallBack
        public void onConfigResult(int resCode, String message) throws RemoteException {
            String str = RecSysClient.TAG;
            HwLog.e(str, "onConfigResult resCode:" + resCode + "  message:" + message);
        }
    };
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class huawei.com.android.server.policy.recsys.RecSysClient.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecSysClient.this.mHwRecSysAidlInterface = HwRecSysAidlInterface.Stub.asInterface(service);
            HwLog.i(RecSysClient.TAG, "onServiceConnected");
            if (RecSysClient.this.mHwRecSysAidlInterface != null) {
                try {
                    RecSysClient.this.mHwRecSysAidlInterface.registerCallBack(RecSysClient.this.mHwRecSysCallBack, RecSysClient.this.mContext.getPackageName());
                } catch (RemoteException e) {
                    HwLog.e(RecSysClient.TAG, "onServiceConnected RemoteException!");
                }
                HwLog.d(RecSysClient.TAG, "ServiceConnection.onServiceConnected is registerCallBack ok");
                return;
            }
            HwLog.w(RecSysClient.TAG, "ServiceConnection.onServiceConnected mRequestWeather is null");
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            if (RecSysClient.this.mHwRecSysAidlInterface != null) {
                try {
                    RecSysClient.this.mHwRecSysAidlInterface.unregisterCallBack(RecSysClient.this.mHwRecSysCallBack, RecSysClient.this.mContext.getPackageName());
                } catch (RemoteException e) {
                    HwLog.e(RecSysClient.TAG, "unregisterCallBack remote exception!");
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
            } catch (SecurityException e) {
                HwLog.e(TAG, "bind recommend service fail SecurityException!");
            } catch (Exception e2) {
                HwLog.e(TAG, "bind recommend service fail Exception!");
            }
        }
    }

    public void unbindRecService() {
        ServiceConnection serviceConnection;
        HwLog.i(TAG, "unbindRecService()");
        Context context = this.mContext;
        if (!(context == null || (serviceConnection = this.mServiceConnection) == null)) {
            try {
                context.unbindService(serviceConnection);
            } catch (SecurityException e) {
                HwLog.e(TAG, "release SecurityException!");
            } catch (Exception e2) {
                HwLog.e(TAG, "release Exception!");
            }
        }
        this.mHwRecSysAidlInterface = null;
        this.mContext = null;
    }

    public void requestRecRes(String jobName) {
        HwRecSysAidlInterface hwRecSysAidlInterface;
        HwLog.d(TAG, "requestRecRes");
        IHwRecSysCallBack.Stub stub = this.mHwRecSysCallBack;
        if (stub == null || (hwRecSysAidlInterface = this.mHwRecSysAidlInterface) == null) {
            HwLog.w(TAG, "warning !!! mHwRecSysCallBack or mHwRecSysAidlInterface is null, requestRecRes() can not be called back, make sure the recsystem service has connected!!");
            return;
        }
        try {
            hwRecSysAidlInterface.requestRecRes(stub, jobName);
        } catch (RemoteException e) {
            HwLog.e(TAG, "requestRecRes remote exception!");
        }
    }

    public boolean getRequestResult() {
        return this.mHasRequestResult;
    }

    public void resetRequestResult() {
        this.mHasRequestResult = false;
    }
}
