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
    protected ILogCollect iLogCollect = null;
    private CallBack mCallerCallback = null;
    private Context mContext = null;
    protected ServiceConnection scLogCollect = new ServiceConnection() {
        /* class com.huawei.lcagent.client.LogCollectManager.AnonymousClass1 */

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(LogCollectManager.TAG, "service is connected");
            LogCollectManager.this.iLogCollect = ILogCollect.Stub.asInterface(service);
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

    public int setMetricStoargeHeader(int metricID, byte[] payloadBytes, int payloadLen) throws RemoteException {
        ILogCollect iLogCollect2;
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.setMetricStoargeHeader(metricID, payloadBytes, payloadLen);
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
        ILogCollect iLogCollect2;
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.setMetricStoargeTail(metricID, payloadBytes, payloadLen);
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
        ILogCollect iLogCollect2;
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.setMetricCommonHeader(metricID, payloadBytes, payloadLen);
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
        ILogCollect iLogCollect2;
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.submitMetric(metricID, level, payloadBytes, payloadLen);
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
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.shouldSubmitMetric(metricID, level);
        }
        return false;
    }

    public LogMetricInfo captureLogMetric(int metricID) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.captureLogMetric(metricID);
        }
        return null;
    }

    public LogMetricInfo captureLogMetricWithParameters(int metricID, String keyValuePairs) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.captureLogMetricWithParameters(metricID, keyValuePairs);
        }
        return null;
    }

    public LogMetricInfo captureAllLog() throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.captureAllLog();
        }
        return null;
    }

    public void clearLogMetric(long id) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            iLogCollect2.clearLogMetric(id);
        }
    }

    public int allowUploadInMobileNetwork(boolean allow) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.allowUploadInMobileNetwork(allow);
        }
        return -1;
    }

    public int allowUploadAlways(boolean allow) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.allowUploadAlways(allow);
        }
        return -1;
    }

    public int configureUserType(int type) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.configureUserType(type);
        }
        return -1;
    }

    public int forceUpload() throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.forceUpload();
        }
        return -1;
    }

    public int feedbackUploadResult(long hashId, int status) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.feedbackUploadResult(hashId, status);
        }
        return -1;
    }

    public int configure(String strCommand) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.configure(strCommand);
        }
        return -1;
    }

    public int getUserType() throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.getUserType();
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
        boolean bRet = this.mContext.bindService(tent, this.scLogCollect, 1);
        Log.i(TAG, "bind result:" + bRet);
        return bRet;
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
            Log.i(TAG, pkgName + " is not installed");
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
        if (context == null || (serviceConnection = this.scLogCollect) == null) {
            Log.e(TAG, "mContext == null || scLogCollect == null");
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
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.getFirstErrorTime();
        }
        return -1;
    }

    public int resetFirstErrorTime() throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.resetFirstErrorTime();
        }
        return -1;
    }

    public String getFirstErrorType() throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.getFirstErrorType();
        }
        return null;
    }

    public int configureModemlogcat(int mode, String parameters) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.configureModemlogcat(mode, parameters);
        }
        return -1;
    }

    public int configureBluetoothlogcat(int enable, String parameters) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.configureBluetoothlogcat(enable, parameters);
        }
        return -1;
    }

    public int configureLogcat(int enable, String parameters) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.configureLogcat(enable, parameters);
        }
        return -1;
    }

    public int configureAPlogs(int enable) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.configureAPlogs(enable);
        }
        return -1;
    }

    public int configureCoredump(int enable) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.configureCoredump(enable);
        }
        return -1;
    }

    public int configureGPS(int enable) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.configureGPS(enable);
        }
        return -1;
    }

    public CompressInfo getCompressInfo() throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.getCompressInfo();
        }
        return null;
    }

    public LogMetricInfo captureLogMetricWithModule(int metricID, String module) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.captureLogMetricWithModule(metricID, module);
        }
        return null;
    }

    public int setMetricStoargeHeaderWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
        ILogCollect iLogCollect2;
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.setMetricStoargeHeaderWithMcc(metricID, payloadBytes, payloadLen, mcc);
        }
        return -1;
    }

    public int setMetricCommonHeaderWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
        ILogCollect iLogCollect2;
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.setMetricCommonHeaderWithMcc(metricID, payloadBytes, payloadLen, mcc);
        }
        return -1;
    }

    public int setMetricStoargeTailWithMcc(int metricID, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
        ILogCollect iLogCollect2;
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.setMetricStoargeTailWithMcc(metricID, payloadBytes, payloadLen, mcc);
        }
        return -1;
    }

    public int submitMetricWithMcc(int metricID, int level, byte[] payloadBytes, int payloadLen, String mcc) throws RemoteException {
        ILogCollect iLogCollect2;
        if (payloadBytes == null || payloadBytes.length < payloadLen) {
            return -2;
        }
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.submitMetricWithMcc(metricID, level, payloadBytes, payloadLen, mcc);
        }
        return -1;
    }

    public int getUploadType(String mcc) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.getUploadType(mcc);
        }
        return -1;
    }

    public String doEncrypt(String src) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.doEncrypt(src);
        }
        return null;
    }

    public int postRemoteDebugCmd(String msg) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.postRemoteDebugCmd(msg);
        }
        return -1;
    }

    public int closeRemoteDebug(int reason) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.closeRemoteDebug(reason);
        }
        return -1;
    }

    public int captureRemoteDebugLog(ICaptureLogCallback callback, String remarkPath, String patchFilespath) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.captureRemoteDebugLogWithRemark(callback, remarkPath, patchFilespath);
        }
        return -1;
    }

    public int captureRemoteDebugLog(ICaptureLogCallback callback) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.captureRemoteDebugLog(callback);
        }
        return -1;
    }

    public int uploadLogFile(String filename, int filetype, int uploadtime, IUploadLogCallback callback) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.uploadLogFile(filename, filetype, uploadtime, callback);
        }
        return -1;
    }

    public int getMaxSizeOfLogFile() throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.getMaxSizeOfLogFile();
        }
        return -1;
    }

    public int cancelRdebugProcess() throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.cancelRdebugProcess();
        }
        return -1;
    }

    public long getValueByType(int datatype) throws RemoteException {
        ILogCollect iLogCollect2;
        if ((this.iLogCollect != null || bindToServiceInternal(this.mContext)) && (iLogCollect2 = this.iLogCollect) != null) {
            return iLogCollect2.getValueByType(datatype);
        }
        return -1;
    }
}
