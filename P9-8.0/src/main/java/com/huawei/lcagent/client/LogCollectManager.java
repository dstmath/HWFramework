package com.huawei.lcagent.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
    protected ILogCollect iLogCollect = null;
    private CallBack mCallerCallback = null;
    private Context mContext = null;
    protected ServiceConnection scLogCollect = new ServiceConnection() {
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

    public interface CallBack {
        void serviceConnected();
    }

    public LogCollectManager(Context context) {
        this.mContext = context.getApplicationContext();
        if (this.mContext == null) {
            this.mContext = context;
        }
        bindToService(this.mContext);
    }

    public LogCollectManager(Context context, CallBack callBack) {
        this.mCallerCallback = callBack;
        this.mContext = context.getApplicationContext();
        bindToService(this.mContext);
    }

    public LogCollectManager(Context context, boolean isFramework) {
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
        if (payloadBytes == null) {
            return -2;
        }
        return setMetricStoargeHeader(metricID, payloadBytes, payloadBytes.length);
    }

    public int setMetricStoargeTail(int metricID, byte[] payloadBytes, int payloadLen) throws RemoteException {
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.setMetricStoargeTail(metricID, payloadBytes, payloadLen);
        }
        return -1;
    }

    public int setMetricStoargeTail(int metricID, byte[] payloadBytes) throws RemoteException {
        if (payloadBytes == null) {
            return -2;
        }
        return setMetricStoargeTail(metricID, payloadBytes, payloadBytes.length);
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
        if (payloadBytes == null) {
            return -2;
        }
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
        if (payloadBytes == null) {
            return -2;
        }
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
        return -1;
    }

    public int allowUploadAlways(boolean allow) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.allowUploadAlways(allow);
        }
        return -1;
    }

    public int configureUserType(int type) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.configureUserType(type);
        }
        return -1;
    }

    public int forceUpload() throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.forceUpload();
        }
        return -1;
    }

    public int feedbackUploadResult(long hashId, int status) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.feedbackUploadResult(hashId, status);
        }
        return -1;
    }

    public int configure(String strCommand) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.configure(strCommand);
        }
        return -1;
    }

    public int getUserType() throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.getUserType();
        }
        return -1;
    }

    public boolean bindToService(Context context) {
        Log.i(TAG, "start to bind to Log Collect service");
        if (this.mContext == null) {
            Log.e(TAG, "mContext == null");
            return false;
        }
        String logModlePkgName = "com.huawei.imonitor";
        if (isPkgInstalled(context, "com.huawei.hiview")) {
            logModlePkgName = "com.huawei.hiview";
        }
        Log.i(TAG, "bind to LogCollectService from " + logModlePkgName);
        Intent serviceIntent = new Intent("com.huawei.lcagent.service.ILogCollect");
        serviceIntent.setClassName(logModlePkgName, "com.huawei.lcagent.service.LogCollectService");
        context.startService(serviceIntent);
        Intent tent = new Intent("com.huawei.lcagent.service.ILogCollect");
        tent.setClassName(logModlePkgName, "com.huawei.lcagent.service.LogCollectService");
        boolean bRet = this.mContext.bindService(tent, this.scLogCollect, 1);
        Log.i(TAG, "bind result:" + bRet);
        return bRet;
    }

    private boolean isPkgInstalled(Context context, String pkgName) {
        if (context == null) {
            Log.i(TAG, "isPkgInstalled context = null");
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            Log.i(TAG, "isPkgInstalled context getPacageManager = null");
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            packageInfo = null;
            Log.i(TAG, pkgName + " is not installed");
        }
        if (packageInfo == null) {
            return false;
        }
        return true;
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
        return -1;
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
        return -1;
    }

    public int configureBluetoothlogcat(int enable, String parameters) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.configureBluetoothlogcat(enable, parameters);
        }
        return -1;
    }

    public int configureLogcat(int enable, String parameters) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.configureLogcat(enable, parameters);
        }
        return -1;
    }

    public int configureAPlogs(int enable) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.configureAPlogs(enable);
        }
        return -1;
    }

    public int configureCoredump(int enable) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.configureCoredump(enable);
        }
        return -1;
    }

    public int configureGPS(int enable) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.configureGPS(enable);
        }
        return -1;
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
            return -2;
        }
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.setMetricStoargeHeaderWithMcc(metricID, payloadBytes, payloadLen, mcc);
        }
        return -1;
    }

    public int setMetricCommonHeaderWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.setMetricCommonHeaderWithMcc(metricID, payloadBytes, payloadLen, mcc);
        }
        return -1;
    }

    public int setMetricStoargeTailWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.setMetricStoargeTailWithMcc(metricID, payloadBytes, payloadLen, mcc);
        }
        return -1;
    }

    public int submitMetricWithMcc(int metricID, int level, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.submitMetricWithMcc(metricID, level, payloadBytes, payloadLen, mcc);
        }
        return -1;
    }

    public int getUploadType(String mcc) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.getUploadType(mcc);
        }
        return -1;
    }

    public String doEncrypt(String src) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.doEncrypt(src);
        }
        return null;
    }

    public int postRemoteDebugCmd(String msg) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.postRemoteDebugCmd(msg);
        }
        return -1;
    }

    public int closeRemoteDebug(int reason) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.closeRemoteDebug(reason);
        }
        return -1;
    }

    public int captureRemoteDebugLog(ICaptureLogCallback callback) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.captureRemoteDebugLog(callback);
        }
        return -1;
    }

    public int uploadLogFile(String filename, int filetype, int uploadtime, IUploadLogCallback callback) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.uploadLogFile(filename, filetype, uploadtime, callback);
        }
        return -1;
    }

    public int getMaxSizeOfLogFile() throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.getMaxSizeOfLogFile();
        }
        return -1;
    }

    public int cancelRdebugProcess() throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.cancelRdebugProcess();
        }
        return -1;
    }

    public long getValueByType(int datatype) throws RemoteException {
        if ((this.iLogCollect != null || bindToService(this.mContext)) && this.iLogCollect != null) {
            return this.iLogCollect.getValueByType(datatype);
        }
        return -1;
    }
}
