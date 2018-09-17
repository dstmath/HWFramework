package com.huawei.connectivitylog;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.chrfile.client.NcLogCollectManager;
import com.huawei.device.connectivitychrlog.ChrLogBaseModel;
import com.huawei.device.connectivitychrlog.ChrLogModel;

public class LogManager extends Handler {
    private static final int EVENT_REPORT_EXCEPTION = 1;
    static final String LOG_TAG = "BASE4IN1_LOG";
    private static final int USER_TYPE_BETA = 2;
    private static final int USER_TYPE_COMMERCIAL = 1;
    private static final int USER_TYPE_UNKNOWN = 0;
    private static NcLogCollectManager mClient;
    private static Context mContext;
    private static LogManager sInstance;
    private int mUserType = 0;

    private class Base4in1Log {
        public int mLevel;
        public ChrLogModel mLogInfo;
        public int mMetricID;

        Base4in1Log(ChrLogModel logInfo, int metricID, int level) {
            this.mLogInfo = logInfo;
            this.mMetricID = metricID;
            this.mLevel = level;
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0021, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void init(Context context) {
        synchronized (LogManager.class) {
            if (context == null) {
                Log.d(LOG_TAG, "LogManager init, context is null!");
            } else if (mContext == null) {
                mContext = context;
                mClient = new NcLogCollectManager(mContext, true);
            } else if (mContext != context) {
                Log.d(LOG_TAG, "Detect difference context while do init");
            }
        }
    }

    public static synchronized LogManager getInstance() {
        LogManager logManager;
        synchronized (LogManager.class) {
            if (sInstance == null) {
                Log.d(LOG_TAG, "start LogManager init  !");
                HandlerThread thread = new HandlerThread("Base4in1LogManager");
                thread.start();
                sInstance = new LogManager(thread.getLooper());
            }
            logManager = sInstance;
        }
        return logManager;
    }

    public void handleMessage(Message msg) {
        if (msg == null) {
            Log.d(LOG_TAG, "msg is null, return");
            return;
        }
        Log.d(LOG_TAG, "The event is: " + msg.what);
        switch (msg.what) {
            case 1:
                reportBase4in1Exception((Base4in1Log) msg.obj);
                break;
        }
    }

    public void reportAbnormalEvent(ChrLogModel logInfo, int metricID, int level) {
        if (logInfo == null || metricID < 0 || level < 1) {
            Log.e(LOG_TAG, "illegal Parameter");
        } else {
            sendMessage(logInfo, metricID, level);
        }
    }

    public void reportAbnormalLevelA(ChrLogModel logInfo) {
        if (logInfo == null) {
            Log.e(LOG_TAG, "logInfo is null, return");
        } else {
            sendMessage(logInfo, 1, 1);
        }
    }

    public void reportAbnormalLevelB(ChrLogModel logInfo) {
        if (logInfo == null) {
            Log.e(LOG_TAG, "logInfo is null, return");
        } else {
            sendMessage(logInfo, 1, 16);
        }
    }

    private void sendMessage(ChrLogModel logInfo, int metricID, int level) {
        sendMessage(obtainMessage(1, new Base4in1Log(logInfo, metricID, level)));
    }

    public boolean isCommercialUser() {
        int userType = 1;
        try {
            if (mClient != null) {
                userType = mClient.getUserType();
            }
            Log.d(LOG_TAG, "the user type is :  " + userType);
        } catch (RemoteException e) {
            Log.d(LOG_TAG, "get user type fail !!!");
        } catch (NullPointerException e2) {
            e2.printStackTrace();
        }
        if (3 == userType || 4 == userType) {
            this.mUserType = 2;
            return false;
        }
        this.mUserType = 1;
        return true;
    }

    public boolean isOverseaCommercialUser() {
        int userType = 6;
        try {
            if (mClient != null) {
                userType = mClient.getUserType();
            }
            Log.d(LOG_TAG, "the user type is :  " + userType);
        } catch (RemoteException e) {
            Log.d(LOG_TAG, "get user type fail !!!");
        } catch (NullPointerException e2) {
            e2.printStackTrace();
        }
        return userType == 6;
    }

    public boolean isCommercialUserFromCache() {
        if (this.mUserType == 0) {
            isCommercialUser();
        }
        if (this.mUserType != 2) {
            return true;
        }
        return false;
    }

    private void reportBase4in1Exception(Base4in1Log base4in1Log) {
        ChrLogModel logInfo = base4in1Log.mLogInfo;
        try {
            int userType = mClient.getUserType();
            if (!(userType == 1 || userType == 2 || userType == 5)) {
                Log.d(LOG_TAG, "UserType = " + userType);
                ChrCommonInfo chrComInfo = new ChrCommonInfo();
                if (chrComInfo != null) {
                    logInfo.chrLogComHeadModel = chrComInfo.getChrComHead(mContext, false);
                    if (logInfo.chrLogComHeadModel == null) {
                        Log.e(LOG_TAG, "getChrComHead is null, return");
                        return;
                    }
                }
            }
            byte[] header = logInfo.chrLogFileHeadModel.toByteArray();
            mClient.setMetricStoargeHeader(base4in1Log.mMetricID, header, header.length);
            header = logInfo.chrLogComHeadModel.toByteArray();
            mClient.setMetricCommonHeader(base4in1Log.mMetricID, header, header.length);
            if (logInfo.logEvents.isEmpty()) {
                Log.e(LOG_TAG, "log events is empty, not submit metric");
            } else if (logInfo.logEvents.get(0) == null) {
                Log.e(LOG_TAG, "log event is null, not submit metric");
            } else {
                byte[] payload = ((ChrLogBaseModel) logInfo.logEvents.get(0)).toByteArray();
                mClient.submitMetric(base4in1Log.mMetricID, base4in1Log.mLevel, payload, payload.length);
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Call LogCollectService fail, did you install it?");
            e.printStackTrace();
        }
    }

    private LogManager(Looper looper) {
        super(looper);
    }
}
