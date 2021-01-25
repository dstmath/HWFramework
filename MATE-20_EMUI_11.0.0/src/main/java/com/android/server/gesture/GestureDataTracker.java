package com.android.server.gesture;

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
import android.util.ArrayMap;
import android.util.Log;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.content.ContextEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.common.service.IDecision;
import com.huawei.common.service.IDecisionCallback;
import java.io.PrintWriter;
import java.time.LocalDate;

public class GestureDataTracker {
    private static final String CATEGORY_KEY = "category";
    private static final long DAY_MS = 86400000;
    private static final int DEFAULT_TIMES = 0;
    private static final String ID_KEY = "id";
    private static final String KEY_BACK_TIMES_PER_DAY = "back_gesture_times";
    private static final String KEY_SUGGESTION_STATE = "gesture_suggestion_event_status";
    private static final String LEFT_BACK_FAIL_EVENT_NAME = "com.huawei.gusture.intent.action.LeftBackFail";
    private static final String LEFT_BACK_SUCCESS_EVENT_NAME = "com.huawei.gusture.intent.action.LeftBackSuccess";
    private static final int MSG_DATE_CHANGED = 1;
    private static final String RIGHT_BACK_FAIL_EVENT_NAME = "com.huawei.gusture.intent.action.RightBackFail";
    private static final String RIGHT_BACK_SUCCESS_EVENT_NAME = "com.huawei.gusture.intent.action.RightBackSuccess";
    private static final int SUCCESS_BACK_TIMES_GOALS = 30;
    private static IDecision decisionApi = null;
    private static ServiceConnection decisionConnection = new ServiceConnection() {
        /* class com.android.server.gesture.GestureDataTracker.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (GestureNavConst.DEBUG) {
                Log.d(GestureNavConst.TAG_GESTURE_TRACKER, "decision service connected");
            }
            IDecision unused = GestureDataTracker.decisionApi = IDecision.Stub.asInterface(service);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            IDecision unused = GestureDataTracker.decisionApi = null;
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "decision service disconnected");
            }
        }
    };
    private static GestureDataTracker instance;
    private static int sSuccessBackTimes;
    private Context mContext;
    private int mCurrentUserId = 0;
    private DateChangeReceiver mDateChangeReceiver;
    private String mDateValue;
    private TrackerHandler mHandler;
    private boolean mIsTracking;
    private final Object mLock = new Object();
    private boolean mShouldDecision;
    private long mStartTime;

    private GestureDataTracker(Context context) {
        this.mContext = context;
        this.mStartTime = System.currentTimeMillis();
    }

    public static GestureDataTracker getInstance(Context context) {
        GestureDataTracker gestureDataTracker;
        synchronized (GestureDataTracker.class) {
            if (instance == null) {
                instance = new GestureDataTracker(context);
            }
            gestureDataTracker = instance;
        }
        return gestureDataTracker;
    }

    public boolean checkStartTrackerIfNeed() {
        this.mCurrentUserId = ActivityManagerEx.getCurrentUser();
        this.mShouldDecision = shouldReportDecision(this.mContext);
        if (hasBackAchieveGoals(this.mContext)) {
            return false;
        }
        return startDataTracking();
    }

    public void gestureBackEvent(int navId, boolean isSuccess) {
        reportDecisionIfNeed(navId, isSuccess);
        if (isSuccess) {
            trackBackSuccessData();
        }
    }

    private void trackBackSuccessData() {
        if (this.mIsTracking && this.mCurrentUserId == 0) {
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
        putBackTimsToDb(this.mContext, sSuccessBackTimes);
    }

    private boolean startDataTracking() {
        synchronized (this.mLock) {
            if (this.mIsTracking) {
                return false;
            }
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "startDataTracking");
            }
            this.mIsTracking = true;
            this.mDateValue = LocalDate.now().toString();
            this.mHandler = new TrackerHandler(Looper.myLooper());
            this.mDateChangeReceiver = new DateChangeReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.DATE_CHANGED");
            filter.addAction("android.intent.action.TIME_SET");
            ContextEx.registerReceiverAsUser(this.mContext, this.mDateChangeReceiver, UserHandleEx.ALL, filter, (String) null, this.mHandler);
            return true;
        }
    }

    private boolean stopDataTracking() {
        synchronized (this.mLock) {
            if (!this.mIsTracking) {
                return false;
            }
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "stopDataTracking");
            }
            this.mIsTracking = false;
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
    public final class DateChangeReceiver extends BroadcastReceiver {
        private DateChangeReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "DateChangeReceiver, intent=" + intent);
            }
            if (GestureDataTracker.this.mHandler != null) {
                GestureDataTracker.this.mHandler.sendEmptyMessage(1);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class TrackerHandler extends Handler {
        TrackerHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                GestureDataTracker.this.handleDateChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDateChanged() {
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

    private static void putBackTimsToDb(Context context, int times) {
        if (context != null) {
            SettingsEx.Secure.putIntForUser(context.getContentResolver(), KEY_BACK_TIMES_PER_DAY, times, 0);
        }
    }

    private static int getBackTimesFromDb(Context context) {
        if (context == null) {
            return 0;
        }
        return SettingsEx.Secure.getIntForUser(context.getContentResolver(), KEY_BACK_TIMES_PER_DAY, 0, 0);
    }

    private static boolean hasBackAchieveGoals(Context context) {
        int times = getBackTimesFromDb(context);
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "DB back times:" + times);
        }
        return backTimesAchieveGoals(times);
    }

    private static boolean backTimesAchieveGoals(int times) {
        return times > SUCCESS_BACK_TIMES_GOALS;
    }

    private static String getDecisionEventName(int navId, boolean isSuccess) {
        if (navId == 1) {
            return isSuccess ? LEFT_BACK_SUCCESS_EVENT_NAME : LEFT_BACK_FAIL_EVENT_NAME;
        } else if (navId != 2) {
            return null;
        } else {
            return isSuccess ? RIGHT_BACK_SUCCESS_EVENT_NAME : RIGHT_BACK_FAIL_EVENT_NAME;
        }
    }

    private static boolean shouldReportDecision(Context context) {
        if (context == null || SettingsEx.Secure.getIntForUser(context.getContentResolver(), KEY_SUGGESTION_STATE, -1, 0) == 0) {
            return false;
        }
        return true;
    }

    private void stopReportDecision(Context context) {
        Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "stop report decision");
        this.mShouldDecision = false;
        SettingsEx.Secure.putIntForUser(context.getContentResolver(), KEY_SUGGESTION_STATE, 0, 0);
        unbindService(context);
    }

    private boolean checkReportDecision(Context context) {
        if (!this.mShouldDecision) {
            return false;
        }
        if (System.currentTimeMillis() - this.mStartTime <= DAY_MS) {
            return true;
        }
        stopReportDecision(context);
        return false;
    }

    private void reportDecisionIfNeed(int navId, boolean isSuccess) {
        if (checkReportDecision(this.mContext)) {
            String eventName = getDecisionEventName(navId, isSuccess);
            bindService(this.mContext);
            executeEvent(eventName);
        }
    }

    private void bindService(Context context) {
        if (decisionApi == null) {
            Intent actionService = new Intent("com.huawei.recsys.decision.action.BIND_DECISION_SERVICE");
            actionService.setPackage("com.huawei.recsys");
            boolean isBound = false;
            try {
                isBound = ContextEx.bindServiceAsUser(context, actionService, decisionConnection, 1, UserHandleEx.CURRENT);
            } catch (IllegalArgumentException e) {
                Log.e(GestureNavConst.TAG_GESTURE_TRACKER, "bind decision fail, catch IllegalArgumentException");
            } catch (Exception e2) {
                Log.e(GestureNavConst.TAG_GESTURE_TRACKER, "bind decisioin fail, catch Exception");
            }
            Log.d(GestureNavConst.TAG_GESTURE_TRACKER, "service bound:" + isBound);
        }
    }

    private static void unbindService(Context context) {
        if (decisionApi != null) {
            try {
                context.unbindService(decisionConnection);
            } catch (IllegalArgumentException e) {
                Log.e(GestureNavConst.TAG_GESTURE_TRACKER, "unbind decisioin fail, catch IllegalArgumentException");
            } catch (Exception e2) {
                Log.e(GestureNavConst.TAG_GESTURE_TRACKER, "unbind decisioin fail, catch Exception");
            }
            decisionApi = null;
            Log.d(GestureNavConst.TAG_GESTURE_TRACKER, "service unbind");
        }
    }

    private static void executeEvent(String eventName) {
        IDecision decision = decisionApi;
        if (decision != null && eventName != null) {
            try {
                ArrayMap<String, String> extras = new ArrayMap<>();
                extras.put(ID_KEY, "");
                extras.put(CATEGORY_KEY, eventName);
                decision.executeEvent(extras, (IDecisionCallback) null);
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_TRACKER, "report eventName:" + eventName);
                }
            } catch (IllegalArgumentException e) {
                Log.e(GestureNavConst.TAG_GESTURE_TRACKER, "executeEvent IllegalArgumentException");
            } catch (Exception e2) {
                Log.e(GestureNavConst.TAG_GESTURE_TRACKER, "executeEvent exception");
            }
        }
    }

    public void dump(String prefix, PrintWriter pw, String[] args) {
        pw.print(prefix);
        pw.print("mTracking=" + this.mIsTracking);
        pw.print(" sSuccessBackTimes=" + sSuccessBackTimes);
        pw.println(" mBackTimesInDB=" + getBackTimesFromDb(this.mContext));
    }
}
