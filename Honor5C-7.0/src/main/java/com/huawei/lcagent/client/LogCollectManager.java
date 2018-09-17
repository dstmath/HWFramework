package com.huawei.lcagent.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.lcagent.client.ILogCollect.Stub;

public class LogCollectManager {
    public static final int ALREADY_DONE = 1;
    public static final int ERROR_OTHER = -2;
    public static final int ERROR_SERVICE_NOT_CONNECTED = -1;
    public static final int FAIL = -3;
    public static final int SUCCESS = 0;
    private static final String TAG = "LogCollectManager";
    ILogCollect iLogCollect;
    private CallBack mCallerCallback;
    private Context mContext;
    protected ServiceConnection scLogCollect;

    public interface CallBack {
        void serviceConnected();
    }

    public LogCollectManager(Context context) {
        this.mContext = null;
        this.mCallerCallback = null;
        this.iLogCollect = null;
        this.scLogCollect = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(LogCollectManager.TAG, "service is connected");
                LogCollectManager.this.iLogCollect = Stub.asInterface(service);
                if (LogCollectManager.this.mCallerCallback != null) {
                    LogCollectManager.this.mCallerCallback.serviceConnected();
                }
            }

            public void onServiceDisconnected(ComponentName arg0) {
                Log.i(LogCollectManager.TAG, "service is disconnceted");
                LogCollectManager.this.iLogCollect = null;
            }
        };
        this.mContext = context.getApplicationContext();
        if (this.mContext == null) {
            this.mContext = context;
        }
        bindToService(this.mContext);
    }

    public LogCollectManager(Context context, CallBack callBack) {
        this.mContext = null;
        this.mCallerCallback = null;
        this.iLogCollect = null;
        this.scLogCollect = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(LogCollectManager.TAG, "service is connected");
                LogCollectManager.this.iLogCollect = Stub.asInterface(service);
                if (LogCollectManager.this.mCallerCallback != null) {
                    LogCollectManager.this.mCallerCallback.serviceConnected();
                }
            }

            public void onServiceDisconnected(ComponentName arg0) {
                Log.i(LogCollectManager.TAG, "service is disconnceted");
                LogCollectManager.this.iLogCollect = null;
            }
        };
        this.mCallerCallback = callBack;
        this.mContext = context.getApplicationContext();
        bindToService(this.mContext);
    }

    public LogCollectManager(Context context, boolean isFramework) {
        this.mContext = null;
        this.mCallerCallback = null;
        this.iLogCollect = null;
        this.scLogCollect = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(LogCollectManager.TAG, "service is connected");
                LogCollectManager.this.iLogCollect = Stub.asInterface(service);
                if (LogCollectManager.this.mCallerCallback != null) {
                    LogCollectManager.this.mCallerCallback.serviceConnected();
                }
            }

            public void onServiceDisconnected(ComponentName arg0) {
                Log.i(LogCollectManager.TAG, "service is disconnceted");
                LogCollectManager.this.iLogCollect = null;
            }
        };
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
            return ERROR_OTHER;
        }
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.setMetricStoargeHeader(metricID, payloadBytes, payloadLen);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int setMetricStoargeHeader(int metricID, byte[] payloadBytes) throws RemoteException {
        return setMetricStoargeHeader(metricID, payloadBytes, payloadBytes.length);
    }

    public int setMetricStoargeTail(int metricID, byte[] payloadBytes, int payloadLen) throws RemoteException {
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return ERROR_OTHER;
        }
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.setMetricStoargeTail(metricID, payloadBytes, payloadLen);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int setMetricStoargeTail(int metricID, byte[] payloadBytes) throws RemoteException {
        return setMetricStoargeTail(metricID, payloadBytes, payloadBytes.length);
    }

    public int setMetricCommonHeader(int metricID, byte[] payloadBytes, int payloadLen) throws RemoteException {
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return ERROR_OTHER;
        }
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.setMetricCommonHeader(metricID, payloadBytes, payloadLen);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int setMetricCommonHeader(int metricID, byte[] payloadBytes) throws RemoteException {
        return setMetricCommonHeader(metricID, payloadBytes, payloadBytes.length);
    }

    public int submitMetric(int metricID, int level, byte[] payloadBytes, int payloadLen) throws RemoteException {
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return ERROR_OTHER;
        }
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.submitMetric(metricID, level, payloadBytes, payloadLen);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int submitMetric(int metricID, int level, byte[] payloadBytes) throws RemoteException {
        return submitMetric(metricID, level, payloadBytes, payloadBytes.length);
    }

    public boolean shouldSubmitMetric(int metricID, int level) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.shouldSubmitMetric(metricID, level);
        }
        return false;
    }

    public LogMetricInfo captureLogMetric(int metricID) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.captureLogMetric(metricID);
        }
        return null;
    }

    public LogMetricInfo captureLogMetricWithParameters(int metricID, String keyValuePairs) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.captureLogMetricWithParameters(metricID, keyValuePairs);
        }
        return null;
    }

    public LogMetricInfo captureAllLog() throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.captureAllLog();
        }
        return null;
    }

    public void clearLogMetric(long id) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            this.iLogCollect.clearLogMetric(id);
        }
    }

    public int allowUploadInMobileNetwork(boolean allow) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.allowUploadInMobileNetwork(allow);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int allowUploadAlways(boolean allow) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.allowUploadAlways(allow);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int configureUserType(int type) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.configureUserType(type);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int forceUpload() throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.forceUpload();
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int feedbackUploadResult(long hashId, int status) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.feedbackUploadResult(hashId, status);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int configure(String strCommand) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.configure(strCommand);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int getUserType() throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.getUserType();
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public boolean bindToService(Context context) {
        Log.i(TAG, "start to bind to Log Collect service");
        if (this.mContext == null) {
            Log.e(TAG, "mContext == null");
            return false;
        }
        Intent serviceIntent = new Intent("com.huawei.lcagent.service.ILogCollect");
        serviceIntent.setClassName("com.huawei.imonitor", "com.huawei.lcagent.service.LogCollectService");
        context.startService(serviceIntent);
        Intent tent = new Intent("com.huawei.lcagent.service.ILogCollect");
        tent.setClassName("com.huawei.imonitor", "com.huawei.lcagent.service.LogCollectService");
        return this.mContext.bindService(tent, this.scLogCollect, ALREADY_DONE);
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

    public long getFirstErrorTime() throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.getFirstErrorTime();
        }
        return -1;
    }

    public int resetFirstErrorTime() throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.resetFirstErrorTime();
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public String getFirstErrorType() throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.getFirstErrorType();
        }
        return null;
    }

    public int configureModemlogcat(int mode, String parameters) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.configureModemlogcat(mode, parameters);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int configureBluetoothlogcat(int enable, String parameters) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.configureBluetoothlogcat(enable, parameters);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int configureLogcat(int enable, String parameters) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.configureLogcat(enable, parameters);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int configureAPlogs(int enable) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.configureAPlogs(enable);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int configureCoredump(int enable) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.configureCoredump(enable);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int configureGPS(int enable) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.configureGPS(enable);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public CompressInfo getCompressInfo() throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.getCompressInfo();
        }
        return null;
    }

    public LogMetricInfo captureLogMetricWithModule(int metricID, String module) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.captureLogMetricWithModule(metricID, module);
        }
        return null;
    }

    public int setMetricStoargeHeaderWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return ERROR_OTHER;
        }
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.setMetricStoargeHeaderWithMcc(metricID, payloadBytes, payloadLen, mcc);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int setMetricCommonHeaderWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return ERROR_OTHER;
        }
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.setMetricCommonHeaderWithMcc(metricID, payloadBytes, payloadLen, mcc);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int setMetricStoargeTailWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return ERROR_OTHER;
        }
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.setMetricStoargeTailWithMcc(metricID, payloadBytes, payloadLen, mcc);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int submitMetricWithMcc(int metricID, int level, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return ERROR_OTHER;
        }
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.submitMetricWithMcc(metricID, level, payloadBytes, payloadLen, mcc);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public int getUploadType(String mcc) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.getUploadType(mcc);
        }
        return ERROR_SERVICE_NOT_CONNECTED;
    }

    public String doEncrypt(String src) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.doEncrypt(src);
        }
        return null;
    }
}
