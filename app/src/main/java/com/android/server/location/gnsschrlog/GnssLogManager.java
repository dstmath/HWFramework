package com.android.server.location.gnsschrlog;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.chrfile.client.NcLogCollectManager;

public class GnssLogManager extends Handler {
    private static final int EVENT_REPORT_EXCEPTION = 1;
    static final String LOG_TAG = "hwGnssLog_gnsschrlog";
    private static final int USER_TYPE_BETA = 2;
    private static final int USER_TYPE_COMMERCIAL = 1;
    private static final int USER_TYPE_UNKNOWN = 0;
    private static NcLogCollectManager mClient;
    private static Context mContext;
    private static GnssLogManager sInstance;
    private int mUserType;

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

    private static synchronized Context getContext() {
        Context context;
        synchronized (GnssLogManager.class) {
            context = mContext;
        }
        return context;
    }

    public static synchronized void init(Context context) {
        synchronized (GnssLogManager.class) {
            if (context == null) {
                Log.d(LOG_TAG, "GnssLogManager init, context is null!");
                return;
            }
            if (mContext == null) {
                mContext = context;
                mClient = new NcLogCollectManager(mContext, true);
            } else if (mContext != context) {
                Log.d(LOG_TAG, "Detect difference context while do init");
            }
            return;
        }
    }

    public static synchronized GnssLogManager getInstance() {
        GnssLogManager gnssLogManager;
        synchronized (GnssLogManager.class) {
            if (sInstance == null) {
                Log.d(LOG_TAG, "start GnssLogManager init  !");
                HandlerThread thread = new HandlerThread("Base4in1LogManager");
                thread.start();
                sInstance = new GnssLogManager(thread.getLooper());
            }
            gnssLogManager = sInstance;
        }
        return gnssLogManager;
    }

    public void handleMessage(Message msg) {
        if (msg == null) {
            Log.d(LOG_TAG, "msg is null, return");
            return;
        }
        Log.d(LOG_TAG, "The event is: " + msg.what);
        switch (msg.what) {
            case USER_TYPE_COMMERCIAL /*1*/:
                reportBase4in1Exception((Base4in1Log) msg.obj);
                break;
        }
    }

    public void reportAbnormalEvent(ChrLogModel logInfo, int metricID, int level) {
        if (logInfo == null || metricID < 0 || level < USER_TYPE_COMMERCIAL) {
            Log.e(LOG_TAG, "illegal Parameter");
        } else {
            sendMessage(logInfo, metricID, level);
        }
    }

    public void reportAbnormalLevelA(ChrLogModel logInfo) {
        if (logInfo == null) {
            Log.e(LOG_TAG, "logInfo is null, return");
        } else {
            sendMessage(logInfo, USER_TYPE_COMMERCIAL, USER_TYPE_COMMERCIAL);
        }
    }

    public void reportAbnormalLevelB(ChrLogModel logInfo) {
        if (logInfo == null) {
            Log.e(LOG_TAG, "logInfo is null, return");
        } else {
            sendMessage(logInfo, USER_TYPE_COMMERCIAL, 16);
        }
    }

    private void sendMessage(ChrLogModel logInfo, int metricID, int level) {
        sendMessage(obtainMessage(USER_TYPE_COMMERCIAL, new Base4in1Log(logInfo, metricID, level)));
    }

    public boolean isCommercialUser() {
        int userType = USER_TYPE_COMMERCIAL;
        try {
            if (mClient != null) {
                userType = mClient.getUserType();
            }
        } catch (RemoteException e) {
            Log.d(LOG_TAG, "get user type fail !!!");
        } catch (NullPointerException e2) {
            e2.printStackTrace();
        }
        if (3 == userType || 4 == userType) {
            this.mUserType = USER_TYPE_BETA;
            return false;
        }
        this.mUserType = USER_TYPE_COMMERCIAL;
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
        if (this.mUserType != USER_TYPE_BETA) {
            return true;
        }
        return false;
    }

    private void reportBase4in1Exception(Base4in1Log base4in1Log) {
        ChrLogModel logInfo = base4in1Log.mLogInfo;
        try {
            int userType = mClient.getUserType();
            if (!(userType == USER_TYPE_COMMERCIAL || userType == USER_TYPE_BETA || userType == 5)) {
                Log.d(LOG_TAG, "UserType = " + userType);
                GnssChrCommonInfo chrComInfo = new GnssChrCommonInfo();
                if (chrComInfo != null) {
                    logInfo.chrLogComHeadModel = chrComInfo.getChrComHead(getContext(), false);
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

    private GnssLogManager(Looper looper) {
        super(looper);
        this.mUserType = 0;
    }
}
