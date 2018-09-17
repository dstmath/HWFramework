package com.huawei.chrfile.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.chrfile.client.INcLogCollect.Stub;

public class NcLogCollectManager {
    public static final int ALREADY_DONE = 1;
    public static final int ERROR_OTHER = -2;
    public static final int ERROR_SERVICE_NOT_CONNECTED = -1;
    public static final int FAIL = -3;
    public static final int SUCCESS = 0;
    private static final String TAG = "NcLogCollectManager";
    protected INcLogCollect iLogCollect = null;
    private CallBack mCallerCallback = null;
    private Context mContext = null;
    protected ServiceConnection scLogCollect = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(NcLogCollectManager.TAG, "service is connected");
            NcLogCollectManager.this.iLogCollect = Stub.asInterface(service);
            if (NcLogCollectManager.this.mCallerCallback != null) {
                NcLogCollectManager.this.mCallerCallback.serviceConnected();
            }
        }

        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(NcLogCollectManager.TAG, "service is disconnceted");
            NcLogCollectManager.this.iLogCollect = null;
        }
    };

    public interface CallBack {
        void serviceConnected();
    }

    public NcLogCollectManager(Context context) {
        this.mContext = context.getApplicationContext();
        if (this.mContext == null) {
            this.mContext = context;
        }
        bindToService(this.mContext);
    }

    public NcLogCollectManager(Context context, CallBack callBack) {
        this.mCallerCallback = callBack;
        this.mContext = context.getApplicationContext();
        bindToService(this.mContext);
    }

    public NcLogCollectManager(Context context, boolean isFramework) {
        if (isFramework) {
            this.mContext = context;
            bindToService(this.mContext);
            return;
        }
        this.mContext = context.getApplicationContext();
        bindToService(this.mContext);
    }

    public int setMetricStoargeHeader(int metricID, byte[] payloadBytes, int payloadLen) throws RemoteException {
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.setMetricStoargeHeader(metricID, payloadBytes, payloadLen);
        }
        return -1;
    }

    public int setMetricStoargeHeader(int metricID, byte[] payloadBytes) throws RemoteException {
        return setMetricStoargeHeader(metricID, payloadBytes, payloadBytes.length);
    }

    public int setMetricCommonHeader(int metricID, byte[] payloadBytes, int payloadLen) throws RemoteException {
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.setMetricCommonHeader(metricID, payloadBytes, payloadLen);
        }
        return -1;
    }

    public int setMetricCommonHeader(int metricID, byte[] payloadBytes) throws RemoteException {
        return setMetricCommonHeader(metricID, payloadBytes, payloadBytes.length);
    }

    public int submitMetric(int metricID, int level, byte[] payloadBytes, int payloadLen) throws RemoteException {
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.submitMetric(metricID, level, payloadBytes, payloadLen);
        }
        return -1;
    }

    public int submitMetric(int metricID, int level, byte[] payloadBytes) throws RemoteException {
        return submitMetric(metricID, level, payloadBytes, payloadBytes.length);
    }

    public int getUserType() throws RemoteException {
        if (this.iLogCollect == null) {
            Log.d(TAG, "getUserType not bind to Log Collect service.");
            if (!bindToService(this.mContext)) {
                return -1;
            }
        }
        if (this.iLogCollect != null) {
            return this.iLogCollect.getUserType();
        }
        Log.e(TAG, "getUserType bind to Log Collect service failed.");
        return -1;
    }

    public boolean bindToService(Context context) {
        Log.i(TAG, "start to bind to Log Collect service");
        if (context == null) {
            Log.e(TAG, "mContext == null");
            return false;
        }
        Intent serviceIntent = new Intent("com.huawei.chrfile.service.INcLogCollect");
        serviceIntent.setClassName("com.huawei.imonitor", "com.huawei.chrfile.service.NcLogCollectService");
        context.startService(serviceIntent);
        Intent tent = new Intent("com.huawei.chrfile.service.INcLogCollect");
        tent.setClassName("com.huawei.imonitor", "com.huawei.chrfile.service.NcLogCollectService");
        return context.bindService(tent, this.scLogCollect, 1);
    }

    private void unbindToService() {
        if (this.mContext == null || this.scLogCollect == null) {
            Log.e(TAG, "mContext == null || scLogCollect == null");
        } else {
            this.mContext.unbindService(this.scLogCollect);
        }
    }

    protected void finalize() {
        unbindToService();
        try {
            super.finalize();
        } catch (Throwable th) {
        }
    }
}
