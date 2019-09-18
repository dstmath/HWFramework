package com.android.server.gesture;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import com.huawei.common.service.IDecision;
import java.io.PrintWriter;
import java.time.LocalDate;

public class GestureDataTracker {
    private static final String CATEGORY_KEY = "category";
    private static final long DAY_MS = 86400000;
    private static final int DEFAULT_TIMES = 0;
    private static final String ID_KEY = "id";
    private static final String KEY_BACK_TIMES_PER_DAY = "back_gesture_times";
    private static final String KEY_SUGGESTION_STATE = "gesture_suggestion_event_status";
    private static final int MSG_DATE_CHANGED = 1;
    private static final int SUCCESS_BACK_TIMES_GOALS = 30;
    /* access modifiers changed from: private */
    public static IDecision mDecisionApi = null;
    private static ServiceConnection mDecisionConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (GestureNavConst.DEBUG) {
                Log.d(GestureNavConst.TAG_GESTURE_TRACKER, "decision service connected");
            }
            IDecision unused = GestureDataTracker.mDecisionApi = IDecision.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName name) {
            IDecision unused = GestureDataTracker.mDecisionApi = null;
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "decision service disconnected");
            }
        }
    };
    private static GestureDataTracker mInstance;
    private static int sSuccessBackTimes;
    private Context mContext;
    private int mCurrentUserId = 0;
    private DateChangeReceiver mDateChangeReceiver;
    private String mDateValue;
    /* access modifiers changed from: private */
    public TrackerHandler mHandler;
    private final Object mLock = new Object();
    private boolean mShouldDecision;
    private long mStartTime;
    private boolean mTracking;

    private final class DateChangeReceiver extends BroadcastReceiver {
        private DateChangeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "DateChangeReceiver, intent=" + intent);
            }
            if (GestureDataTracker.this.mHandler != null) {
                GestureDataTracker.this.mHandler.sendEmptyMessage(1);
            }
        }
    }

    private final class TrackerHandler extends Handler {
        public TrackerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                GestureDataTracker.this.handleDateChanged();
            }
        }
    }

    private GestureDataTracker(Context context) {
        this.mContext = context;
        this.mStartTime = System.currentTimeMillis();
    }

    public static GestureDataTracker getInstance(Context context) {
        GestureDataTracker gestureDataTracker;
        synchronized (GestureDataTracker.class) {
            if (mInstance == null) {
                mInstance = new GestureDataTracker(context);
            }
            gestureDataTracker = mInstance;
        }
        return gestureDataTracker;
    }

    public boolean checkStartTrackerIfNeed() {
        this.mCurrentUserId = ActivityManager.getCurrentUser();
        this.mShouldDecision = shouldReportDecision(this.mContext);
        if (hasBackAchieveGoals(this.mContext)) {
            return false;
        }
        return startDataTracking();
    }

    public void gestureBackEvent(int navId, boolean success) {
        reportDecisionIfNeed(navId, success);
        if (success) {
            trackBackSuccessData();
        }
    }

    private void trackBackSuccessData() {
        if (this.mTracking && this.mCurrentUserId == 0) {
            sSuccessBackTimes++;
            if (backTimesAchieveGoals(sSuccessBackTimes)) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "back times has achieved goals, stop tracking");
                }
                saveBackTimes();
                stopDataTracking();
            }
        }
    }

    private boolean checkStopTrackerIfNeed() {
        if (sSuccessBackTimes != 0 || GestureNavConst.isGestureNavEnabled(this.mContext, -2)) {
            return false;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "back times is zero and gesture is closed now, stop stracking");
        }
        return stopDataTracking();
    }

    private void resetBackTimes() {
        sSuccessBackTimes = 0;
    }

    private void saveBackTimes() {
        putBackTimsToDB(this.mContext, sSuccessBackTimes);
    }

    private boolean startDataTracking() {
        synchronized (this.mLock) {
            if (this.mTracking) {
                return false;
            }
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "startDataTracking");
            }
            this.mTracking = true;
            this.mDateValue = LocalDate.now().toString();
            this.mHandler = new TrackerHandler(Looper.myLooper());
            this.mDateChangeReceiver = new DateChangeReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.DATE_CHANGED");
            filter.addAction("android.intent.action.TIME_SET");
            this.mContext.registerReceiverAsUser(this.mDateChangeReceiver, UserHandle.ALL, filter, null, this.mHandler);
            return true;
        }
    }

    private boolean stopDataTracking() {
        synchronized (this.mLock) {
            if (!this.mTracking) {
                return false;
            }
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "stopDataTracking");
            }
            this.mTracking = false;
            if (this.mDateChangeReceiver != null) {
                this.mContext.unregisterReceiver(this.mDateChangeReceiver);
                this.mDateChangeReceiver = null;
            }
            if (this.mHandler != null) {
                this.mHandler.removeCallbacksAndMessages(null);
                this.mHandler = null;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void handleDateChanged() {
        String now = LocalDate.now().toString();
        if (now != null && !now.equals(this.mDateValue)) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "Date changed, backTimes:" + sSuccessBackTimes + ", now:" + now);
            }
            this.mDateValue = now;
            saveBackTimes();
            checkStopTrackerIfNeed();
            resetBackTimes();
        }
    }

    private static void putBackTimsToDB(Context context, int times) {
        if (context != null) {
            Settings.Secure.putIntForUser(context.getContentResolver(), KEY_BACK_TIMES_PER_DAY, times, 0);
        }
    }

    private static int getBackTimesFromDB(Context context) {
        if (context == null) {
            return 0;
        }
        return Settings.Secure.getIntForUser(context.getContentResolver(), KEY_BACK_TIMES_PER_DAY, 0, 0);
    }

    private static boolean hasBackAchieveGoals(Context context) {
        int times = getBackTimesFromDB(context);
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "DB back times:" + times);
        }
        return backTimesAchieveGoals(times);
    }

    private static boolean backTimesAchieveGoals(int times) {
        return times > 30;
    }

    private static String getDecisionEventName(int navId, boolean success) {
        String eventName;
        String eventName2;
        switch (navId) {
            case 1:
                if (success) {
                    eventName = "com.huawei.gusture.intent.action.LeftBackSuccess";
                } else {
                    eventName = "com.huawei.gusture.intent.action.LeftBackFail";
                }
                return eventName;
            case 2:
                if (success) {
                    eventName2 = "com.huawei.gusture.intent.action.RightBackSuccess";
                } else {
                    eventName2 = "com.huawei.gusture.intent.action.RightBackFail";
                }
                return eventName2;
            default:
                return null;
        }
    }

    private static boolean shouldReportDecision(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        if (Settings.Secure.getIntForUser(context.getContentResolver(), KEY_SUGGESTION_STATE, -1, 0) != 0) {
            z = true;
        }
        return z;
    }

    private void stopReportDecision(Context context) {
        Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "stop report decision");
        this.mShouldDecision = false;
        Settings.Secure.putIntForUser(context.getContentResolver(), KEY_SUGGESTION_STATE, 0, 0);
        unbindService(context);
    }

    private boolean checkReportDecision(Context context) {
        if (!this.mShouldDecision) {
            return false;
        }
        if (System.currentTimeMillis() - this.mStartTime <= 86400000) {
            return true;
        }
        stopReportDecision(context);
        return false;
    }

    private void reportDecisionIfNeed(int navId, boolean success) {
        if (checkReportDecision(this.mContext)) {
            String eventName = getDecisionEventName(navId, success);
            bindService(this.mContext);
            executeEvent(eventName);
        }
    }

    private void bindService(Context context) {
        if (mDecisionApi == null) {
            Intent actionService = new Intent("com.huawei.recsys.decision.action.BIND_DECISION_SERVICE");
            actionService.setPackage("com.huawei.recsys");
            boolean bound = false;
            try {
                bound = context.bindServiceAsUser(actionService, mDecisionConnection, 1, UserHandle.CURRENT);
            } catch (Exception e) {
                Log.e(GestureNavConst.TAG_GESTURE_TRACKER, "bind decisioin fail," + e.getMessage());
            }
            Log.d(GestureNavConst.TAG_GESTURE_TRACKER, "service bound:" + bound);
        }
    }

    private static void unbindService(Context context) {
        if (mDecisionApi != null) {
            try {
                context.unbindService(mDecisionConnection);
            } catch (Exception e) {
                Log.e(GestureNavConst.TAG_GESTURE_TRACKER, "unbind decisioin fail," + e.getMessage());
            }
            mDecisionApi = null;
            Log.d(GestureNavConst.TAG_GESTURE_TRACKER, "service unbind");
        }
    }

    private static void executeEvent(String eventName) {
        IDecision decision = mDecisionApi;
        if (decision != null && eventName != null) {
            try {
                ArrayMap<String, String> extra = new ArrayMap<>();
                extra.put("id", "");
                extra.put("category", eventName);
                decision.executeEvent(extra, null);
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "report eventName:" + eventName);
                }
            } catch (Exception e) {
                Log.e(GestureNavConst.TAG_GESTURE_TRACKER, "executeEvent exception");
            }
        }
    }

    public void dump(String prefix, PrintWriter pw, String[] args) {
        pw.print(prefix);
        pw.print("mTracking=" + this.mTracking);
        pw.print(" sSuccessBackTimes=" + sSuccessBackTimes);
        pw.println(" mBackTimesInDB=" + getBackTimesFromDB(this.mContext));
    }
}
