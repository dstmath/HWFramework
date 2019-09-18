package com.android.internal.telephony;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Log;
import com.huawei.common.service.IDecision;

public final class DecisionUtil {
    private static final String CATEGORY_KEY = "category";
    private static final String ID_KEY = "id";
    private static final int OPER_SUCCESS = 0;
    /* access modifiers changed from: private */
    public static final String TAG = DecisionUtil.class.getSimpleName();
    /* access modifiers changed from: private */
    public static Context mContext;
    /* access modifiers changed from: private */
    public static IDecision mDecisionApi = null;
    private static ServiceConnection mDecisionConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(DecisionUtil.TAG, "service connected.");
            IDecision unused = DecisionUtil.mDecisionApi = IDecision.Stub.asInterface(service);
            DecisionUtil.executeEvent(DecisionUtil.mEventName);
            Log.d(DecisionUtil.TAG, "onServiceConnected");
            DecisionUtil.unbindService(DecisionUtil.mContext);
        }

        public void onServiceDisconnected(ComponentName name) {
            IDecision unused = DecisionUtil.mDecisionApi = null;
            Log.i(DecisionUtil.TAG, "service disconnect.");
        }
    };
    /* access modifiers changed from: private */
    public static String mEventName = null;
    private static Handler mHander = null;

    public static void bindService(Context context, String eventName) {
        mEventName = eventName;
        if (context == null || mDecisionApi != null || mEventName == null) {
            Log.i(TAG, "service already binded");
            return;
        }
        mContext = context;
        if (mHander == null) {
            mHander = new Handler(context.getMainLooper());
        }
        Intent actionService = new Intent("com.huawei.recsys.decision.action.BIND_DECISION_SERVICE");
        actionService.setPackage("com.huawei.recsys");
        try {
            context.bindService(actionService, mDecisionConnection, 1);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void unbindService(Context context) {
        if (context != null) {
            try {
                context.unbindService(mDecisionConnection);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            mDecisionApi = null;
        }
    }

    public static boolean executeEvent(String eventName) {
        return executeEvent(eventName, null);
    }

    public static boolean executeEvent(String eventName, String dataId) {
        if (mDecisionApi == null) {
            return false;
        }
        ArrayMap<String, Object> extra2 = new ArrayMap<>();
        extra2.put(ID_KEY, dataId != null ? dataId : "");
        if (eventName != null) {
            extra2.put(CATEGORY_KEY, eventName);
        }
        try {
            String str = TAG;
            Log.v(str, "executeEvent " + mEventName);
            mDecisionApi.executeEvent(extra2, null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
