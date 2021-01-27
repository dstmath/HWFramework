package com.huawei.lcagent.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.lcagent.client.ILogCollect;

public class LogCollectManager {
    public static final int ALREADY_DONE = 1;
    public static final int ERROR_OTHER = -2;
    public static final int ERROR_SERVICE_NOT_CONNECTED = -1;
    public static final int FAIL = -3;
    public static final int SUCCESS = 0;
    private static final String TAG = "LogCollectManager";
    private CallBack mCallerCallback = null;
    private Context mContext = null;
    protected ILogCollect mLogCollect = null;
    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class com.huawei.lcagent.client.LogCollectManager.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(LogCollectManager.TAG, "service is connected");
            LogCollectManager.this.mLogCollect = ILogCollect.Stub.asInterface(service);
            if (LogCollectManager.this.mCallerCallback != null) {
                LogCollectManager.this.mCallerCallback.serviceConnected();
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(LogCollectManager.TAG, "service is disconnceted");
            LogCollectManager.this.mLogCollect = null;
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
        bindToServiceInternal(this.mContext);
    }

    public LogCollectManager(Context context, CallBack callBack) {
        this.mCallerCallback = callBack;
        this.mContext = context.getApplicationContext();
        bindToServiceInternal(this.mContext);
    }

    public LogCollectManager(Context context, boolean isFramework) {
        if (isFramework) {
            this.mContext = context;
            bindToServiceInternal(this.mContext);
            return;
        }
        this.mContext = context.getApplicationContext();
        bindToServiceInternal(this.mContext);
    }

    public int setMetricStoargeHeader(int metricId, byte[] payloadBytes, int payloadLen) throws RemoteException {
        ILogCollect iLogCollect;
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.setMetricStoargeHeader(metricId, payloadBytes, payloadLen);
        }
        return -1;
    }

    public int setMetricStoargeHeader(int metricId, byte[] payloadBytes) throws RemoteException {
        if (payloadBytes == null) {
            return -2;
        }
        return setMetricStoargeHeader(metricId, payloadBytes, payloadBytes.length);
    }

    public int setMetricStoargeTail(int metricId, byte[] payloadBytes, int payloadLen) throws RemoteException {
        ILogCollect iLogCollect;
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.setMetricStoargeTail(metricId, payloadBytes, payloadLen);
        }
        return -1;
    }

    public int setMetricStoargeTail(int metricId, byte[] payloadBytes) throws RemoteException {
        if (payloadBytes == null) {
            return -2;
        }
        return setMetricStoargeTail(metricId, payloadBytes, payloadBytes.length);
    }

    public int setMetricCommonHeader(int metricId, byte[] payloadBytes, int payloadLen) throws RemoteException {
        ILogCollect iLogCollect;
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.setMetricCommonHeader(metricId, payloadBytes, payloadLen);
        }
        return -1;
    }

    public int setMetricCommonHeader(int metricId, byte[] payloadBytes) throws RemoteException {
        if (payloadBytes == null) {
            return -2;
        }
        return setMetricCommonHeader(metricId, payloadBytes, payloadBytes.length);
    }

    public int submitMetric(int metricId, int level, byte[] payloadBytes, int payloadLen) throws RemoteException {
        ILogCollect iLogCollect;
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.submitMetric(metricId, level, payloadBytes, payloadLen);
        }
        return -1;
    }

    public int submitMetric(int metricId, int level, byte[] payloadBytes) throws RemoteException {
        if (payloadBytes == null) {
            return -2;
        }
        return submitMetric(metricId, level, payloadBytes, payloadBytes.length);
    }

    public boolean shouldSubmitMetric(int metricId, int level) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.shouldSubmitMetric(metricId, level);
        }
        return false;
    }

    public LogMetricInfo captureLogMetric(int metricId) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.captureLogMetric(metricId);
        }
        return null;
    }

    public LogMetricInfo captureLogMetricWithParameters(int metricId, String keyValuePairs) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.captureLogMetricWithParameters(metricId, keyValuePairs);
        }
        return null;
    }

    public LogMetricInfo captureAllLog() throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.captureAllLog();
        }
        return null;
    }

    public void clearLogMetric(long id) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            iLogCollect.clearLogMetric(id);
        }
    }

    public int allowUploadInMobileNetwork(boolean isAllow) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.allowUploadInMobileNetwork(isAllow);
        }
        return -1;
    }

    public int allowUploadAlways(boolean isAllow) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.allowUploadAlways(isAllow);
        }
        return -1;
    }

    public int configureUserType(int type) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.configureUserType(type);
        }
        return -1;
    }

    public int forceUpload() throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.forceUpload();
        }
        return -1;
    }

    public int feedbackUploadResult(long hashId, int status) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.feedbackUploadResult(hashId, status);
        }
        return -1;
    }

    public int configure(String strCommand) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.configure(strCommand);
        }
        return -1;
    }

    public int getUserType() throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.getUserType();
        }
        return -1;
    }

    private boolean bindToServiceInternal(Context context) {
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
        boolean isSuccessed = this.mContext.bindService(tent, this.mServiceConnection, 1);
        Log.i(TAG, "bind result:" + isSuccessed);
        return isSuccessed;
    }

    public boolean bindToService(Context context) {
        return bindToServiceInternal(context);
    }

    private boolean isPkgInstalled(Context context, String pkgName) {
        PackageInfo packageInfo;
        if (context == null) {
            Log.i(TAG, "isPkgInstalled context = null");
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            Log.i(TAG, "isPkgInstalled context getPacageManager = null");
            return false;
        }
        try {
            packageInfo = packageManager.getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, pkgName + " is not installed");
            packageInfo = null;
        }
        if (packageInfo == null) {
            return false;
        }
        return true;
    }

    private void unbindToService() {
        ServiceConnection serviceConnection;
        Context context = this.mContext;
        if (context == null || (serviceConnection = this.mServiceConnection) == null) {
            Log.e(TAG, "mContext == null || mServiceConnection == null");
        } else {
            context.unbindService(serviceConnection);
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        unbindToService();
        try {
            super.finalize();
        } catch (Throwable e) {
            Log.e(TAG, "finalize error: " + e.getMessage());
        }
    }

    public long getFirstErrorTime() throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.getFirstErrorTime();
        }
        return -1;
    }

    public int resetFirstErrorTime() throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.resetFirstErrorTime();
        }
        return -1;
    }

    public String getFirstErrorType() throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.getFirstErrorType();
        }
        return null;
    }

    public int configureModemlogcat(int mode, String parameters) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.configureModemlogcat(mode, parameters);
        }
        return -1;
    }

    public int configureBluetoothlogcat(int enable, String parameters) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.configureBluetoothlogcat(enable, parameters);
        }
        return -1;
    }

    public int configureLogcat(int enable, String parameters) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.configureLogcat(enable, parameters);
        }
        return -1;
    }

    public int configureAPlogs(int enable) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.configureAPlogs(enable);
        }
        return -1;
    }

    public int configureCoredump(int enable) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.configureCoredump(enable);
        }
        return -1;
    }

    public int configureGPS(int enable) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.configureGPS(enable);
        }
        return -1;
    }

    public CompressInfo getCompressInfo() throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.getCompressInfo();
        }
        return null;
    }

    public LogMetricInfo captureLogMetricWithModule(int metricId, String module) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.captureLogMetricWithModule(metricId, module);
        }
        return null;
    }

    public int setMetricStoargeHeaderWithMcc(int metricId, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
        ILogCollect iLogCollect;
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.setMetricStoargeHeaderWithMcc(metricId, payloadBytes, payloadLen, mcc);
        }
        return -1;
    }

    public int setMetricCommonHeaderWithMcc(int metricId, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
        ILogCollect iLogCollect;
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.setMetricCommonHeaderWithMcc(metricId, payloadBytes, payloadLen, mcc);
        }
        return -1;
    }

    public int setMetricStoargeTailWithMcc(int metricId, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
        ILogCollect iLogCollect;
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.setMetricStoargeTailWithMcc(metricId, payloadBytes, payloadLen, mcc);
        }
        return -1;
    }

    public int submitMetricWithMcc(int metricId, int level, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
        ILogCollect iLogCollect;
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.submitMetricWithMcc(metricId, level, payloadBytes, payloadLen, mcc);
        }
        return -1;
    }

    public int getUploadType(String mcc) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.getUploadType(mcc);
        }
        return -1;
    }

    public String doEncrypt(String src) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.doEncrypt(src);
        }
        return null;
    }

    public int postRemoteDebugCmd(String msg) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.postRemoteDebugCmd(msg);
        }
        return -1;
    }

    public int closeRemoteDebug(int reason) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.closeRemoteDebug(reason);
        }
        return -1;
    }

    public int captureRemoteDebugLog(ICaptureLogCallback callback, String remarkPath, String patchFilespath) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.captureRemoteDebugLogWithRemark(callback, remarkPath, patchFilespath);
        }
        return -1;
    }

    public int captureRemoteDebugLog(ICaptureLogCallback callback) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.captureRemoteDebugLog(callback);
        }
        return -1;
    }

    public int uploadLogFile(String filename, int filetype, int uploadtime, IUploadLogCallback callback) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.uploadLogFile(filename, filetype, uploadtime, callback);
        }
        return -1;
    }

    public int getMaxSizeOfLogFile() throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.getMaxSizeOfLogFile();
        }
        return -1;
    }

    public int cancelRdebugProcess() throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.cancelRdebugProcess();
        }
        return -1;
    }

    public long getValueByType(int datatype) throws RemoteException {
        ILogCollect iLogCollect;
        if ((this.mLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect = this.mLogCollect) != null) {
            return iLogCollect.getValueByType(datatype);
        }
        return -1;
    }
}
