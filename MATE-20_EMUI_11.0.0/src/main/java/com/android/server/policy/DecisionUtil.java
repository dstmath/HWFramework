package com.android.server.policy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Log;
import com.huawei.common.service.IDecision;
import com.huawei.common.service.IDecisionCallback;

public final class DecisionUtil {
    private static final String ACTION_COMMON_SERVICE_NAME = "com.huawei.recsys.decision.action.BIND_DECISION_SERVICE";
    private static final String ACTION_PACKAGE_NAME = "com.huawei.recsys";
    private static final int ARRAY_NUM = 2;
    private static final String CATEGORY_KEY = "category";
    private static final String ID_KEY = "id";
    private static final int OPER_SUCCESS = 0;
    private static final String TAG = DecisionUtil.class.getSimpleName();
    private static Context mContext;
    private static IDecision mDecisionApi = null;
    private static ServiceConnection mDecisionConnection = new ServiceConnection() {
        /* class com.android.server.policy.DecisionUtil.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            IDecision unused = DecisionUtil.mDecisionApi = IDecision.Stub.asInterface(service);
            DecisionUtil.executeEvent(DecisionUtil.mEventName);
            DecisionUtil.unbindService(DecisionUtil.mContext);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            IDecision unused = DecisionUtil.mDecisionApi = null;
            Log.i(DecisionUtil.TAG, "service disconnect.");
        }
    };
    private static String mEventName = null;

    private DecisionUtil() {
    }

    public static boolean bindServiceToAidsEngine(Context context, String eventName) {
        String str = TAG;
        Log.i(str, "bindServiceToAidsEngine begin:  " + eventName);
        mEventName = eventName;
        if (context == null || mDecisionApi != null || mEventName == null) {
            Log.i(TAG, "service already binded");
            return false;
        }
        mContext = context;
        Intent actionService = new Intent(ACTION_COMMON_SERVICE_NAME);
        actionService.setPackage(ACTION_PACKAGE_NAME);
        Log.i(TAG, "context.bindService");
        context.bindService(actionService, mDecisionConnection, 1);
        return true;
    }

    /* access modifiers changed from: private */
    public static void unbindService(Context context) {
        if (context != null) {
            context.unbindService(mDecisionConnection);
            mDecisionApi = null;
        }
    }

    /* access modifiers changed from: private */
    public static boolean executeEvent(String eventName) {
        return executeEvent(eventName, null);
    }

    private static boolean executeEvent(String eventName, String dataId) {
        if (mDecisionApi == null) {
            return false;
        }
        ArrayMap<String, Object> extra2 = new ArrayMap<>(2);
        extra2.put(ID_KEY, dataId != null ? dataId : "");
        if (eventName != null) {
            extra2.put(CATEGORY_KEY, eventName);
        }
        try {
            String str = TAG;
            Log.i(str, "executeEvent " + mEventName);
            mDecisionApi.executeEvent(extra2, (IDecisionCallback) null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
