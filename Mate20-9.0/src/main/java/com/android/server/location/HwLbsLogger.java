package com.android.server.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import java.util.ArrayList;
import java.util.List;

public class HwLbsLogger implements IHwLbsLogger {
    private static final String BUNDLE_PARAMETER_LOCATION = "location";
    private static final String BUNDLE_PARAMETER_LOCATION_RECEIVERS = "receivers";
    private static final String BUNDLE_PARAMETER_SESSION_ACTION = "action";
    private static final String BUNDLE_PARAMETER_SESSION_INTERVAL = "interval";
    private static final String BUNDLE_PARAMETER_SESSION_PACKAGE_NAME = "pkgName";
    private static final String BUNDLE_PARAMETER_SESSION_PROVIDER = "provider";
    private static final String BUNDLE_PARAMETER_SESSION_RECEIVER = "receiver";
    private static final String BUNDLE_PARAMETER_TIMESTAMP = "timestamp";
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String SESSION_START = "start";
    private static final String SESSION_STOP = "stop";
    private static final String TAG = "HwLbsLogger";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private static HwLbsLogger mHwLbsLogger;
    private final HwLbsLoggerHandler mHandler;
    private HwLogRecordManager mHwLogManager = HwLogRecordManager.getInstance();
    private HandlerThread mThread = new HandlerThread(TAG);

    private final class HwLbsLoggerHandler extends Handler {
        public HwLbsLoggerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                case 102:
                case 103:
                case 104:
                case 105:
                case 106:
                case 107:
                case HwAPPQoEUtils.MSG_APP_STATE_UNKNOW:
                case 109:
                case 110:
                case 112:
                    break;
                case 101:
                    Bundle sessionBundle = (Bundle) msg.obj;
                    if (sessionBundle != null) {
                        SessionRecord sessionRecord = null;
                        String action = sessionBundle.getString("action");
                        if (action.equals(HwLbsLogger.SESSION_START)) {
                            SessionRecord sessionRecord2 = new SessionRecord(sessionBundle.getLong(HwLbsLogger.BUNDLE_PARAMETER_TIMESTAMP), action, sessionBundle.getString("pkgName"), sessionBundle.getString(HwLbsLogger.BUNDLE_PARAMETER_SESSION_PROVIDER), sessionBundle.getInt("interval"), sessionBundle.getInt("receiver"));
                            sessionRecord = sessionRecord2;
                        } else if (action.equals(HwLbsLogger.SESSION_STOP)) {
                            SessionRecord sessionRecord3 = new SessionRecord(sessionBundle.getLong(HwLbsLogger.BUNDLE_PARAMETER_TIMESTAMP), action, sessionBundle.getString("pkgName"), sessionBundle.getInt("receiver"));
                            sessionRecord = sessionRecord3;
                        }
                        HwLbsLogger.this.saveSessionRecord(sessionRecord);
                        break;
                    } else {
                        Log.d(HwLbsLogger.TAG, "LOCATION_SESSION_ACTION Parameter bundle is null");
                        return;
                    }
                case 111:
                    Bundle posBundle = (Bundle) msg.obj;
                    if (posBundle != null) {
                        HwLbsLogger.this.saveLocationResultRecord(new LocationResultRecord((Location) posBundle.getParcelable(HwLbsLogger.BUNDLE_PARAMETER_LOCATION), posBundle.getLong(HwLbsLogger.BUNDLE_PARAMETER_TIMESTAMP), posBundle.getIntegerArrayList(HwLbsLogger.BUNDLE_PARAMETER_LOCATION_RECEIVERS)));
                        break;
                    } else {
                        Log.d(HwLbsLogger.TAG, "LOCATION_POS_REPORT Parameter bundle is null");
                        return;
                    }
                default:
                    return;
            }
        }
    }

    public static class LocationEventRecord {
        private int mEventId = 0;
        private int mParameter = 0;
        private int mReceiver = 0;
        private long mTimestamp = 0;

        public LocationEventRecord(long timestamp, int eventId, int parameter, int receiver) {
            this.mTimestamp = timestamp;
            this.mEventId = eventId;
            this.mParameter = parameter;
            this.mReceiver = receiver;
        }

        public long getTimestamp() {
            return this.mTimestamp;
        }

        public int getEventId() {
            return this.mEventId;
        }

        public int getParameter() {
            return this.mParameter;
        }

        public int getReceiver() {
            return this.mReceiver;
        }
    }

    public static class LocationResultRecord {
        private Location mLocation;
        private List<Integer> mReceivers = new ArrayList();
        private long mTimestamp;

        public LocationResultRecord(Location location, long timestamp, List<Integer> receivers) {
            this.mLocation = location;
            this.mTimestamp = timestamp;
            if (receivers != null) {
                int size = receivers.size();
                for (int i = 0; i < size; i++) {
                    this.mReceivers.add(receivers.get(i));
                }
            }
        }

        public Location getLocation() {
            return this.mLocation;
        }

        public long getTimestamp() {
            return this.mTimestamp;
        }

        public List<Integer> getReceivers() {
            return this.mReceivers;
        }
    }

    public static class SessionRecord {
        private String mAction;
        private int mInterval;
        private String mPkgName;
        private String mProvider;
        private int mReceiver;
        private long mTimestamp;

        public SessionRecord(long timestamp, String action, String pkgName, String provider, int interval, int receiver) {
            this.mTimestamp = timestamp;
            this.mAction = action;
            this.mPkgName = pkgName;
            this.mProvider = provider;
            this.mInterval = interval;
            this.mReceiver = receiver;
        }

        public SessionRecord(long timestamp, String action, String pkgName, int receiver) {
            this.mTimestamp = timestamp;
            this.mAction = action;
            this.mPkgName = pkgName;
            this.mReceiver = receiver;
        }

        public String toString() {
            return "SessionRecord: " + "mTimestamp " + this.mTimestamp + ", mAction " + this.mAction + ", mPkgName " + this.mPkgName + ", mProvider " + this.mProvider + ", mInterval " + this.mInterval + ", mReceiver " + this.mReceiver;
        }

        public long getTimestamp() {
            return this.mTimestamp;
        }

        public String getAction() {
            return this.mAction;
        }

        public String getPackageName() {
            return this.mPkgName;
        }

        public String getProvider() {
            return this.mProvider;
        }

        public long getInterval() {
            return (long) this.mInterval;
        }

        public int getReceiver() {
            return this.mReceiver;
        }
    }

    private HwLbsLogger(Context context) {
        Log.d(TAG, "enter HwLbsLogger");
        this.mThread.start();
        this.mHandler = new HwLbsLoggerHandler(this.mThread.getLooper());
    }

    public static synchronized HwLbsLogger getInstance(Context context) {
        HwLbsLogger hwLbsLogger;
        synchronized (HwLbsLogger.class) {
            if (mHwLbsLogger == null) {
                mHwLbsLogger = new HwLbsLogger(context);
            }
            hwLbsLogger = mHwLbsLogger;
        }
        return hwLbsLogger;
    }

    public static synchronized HwLbsLogger getHwLbsLogger() {
        HwLbsLogger hwLbsLogger;
        synchronized (HwLbsLogger.class) {
            hwLbsLogger = mHwLbsLogger;
        }
        return hwLbsLogger;
    }

    /* access modifiers changed from: private */
    public void saveSessionRecord(SessionRecord sr) {
        if (this.mHwLogManager == null || sr == null) {
            Log.d(TAG, "save sesstion record failed");
        } else {
            this.mHwLogManager.writeSession(sr);
        }
    }

    /* access modifiers changed from: private */
    public void saveLocationResultRecord(LocationResultRecord locationRecord) {
        if (this.mHwLogManager == null || locationRecord == null) {
            Log.d(TAG, "save location result record failed.");
        } else {
            this.mHwLogManager.writePosition(locationRecord);
        }
    }

    public void loggerEvent(int eventId, Bundle parameter) {
        Message msg = Message.obtain();
        msg.what = eventId;
        msg.obj = parameter;
        this.mHandler.sendMessage(msg);
    }
}
