package com.android.server.wifi.p2p;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Log;
import com.huawei.common.service.IDecision;
import com.huawei.common.service.IDecisionCallback;
import java.util.Map;

public final class DecisionUtil {
    private static final String CATEGORY_KEY = "category";
    private static final String EVENT_NAME = "com.huawei.wifi.intent.action.WifiBridgeShare";
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
            DecisionUtil.executeEvent(DecisionUtil.EVENT_NAME);
            DecisionUtil.unbindService(DecisionUtil.mContext);
        }

        public void onServiceDisconnected(ComponentName name) {
            IDecision unused = DecisionUtil.mDecisionApi = null;
            Context unused2 = DecisionUtil.mContext = null;
            Log.i(DecisionUtil.TAG, "service disconnect.");
        }
    };

    public static void bindService(Context context) {
        if (context == null || mDecisionApi != null) {
            Log.i(TAG, "service already binded");
            return;
        }
        mContext = context;
        Intent actionService = new Intent("com.huawei.recsys.decision.action.BIND_DECISION_SERVICE");
        actionService.setPackage("com.huawei.recsys");
        try {
            boolean ret = context.bindService(actionService, mDecisionConnection, 1);
            String str = TAG;
            Log.e(str, "bindService" + ret);
        } catch (SecurityException e) {
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
        }
    }

    public static boolean executeEvent(String eventName) {
        return executeEvent(eventName, null, null, null);
    }

    public static boolean executeEvent(String eventName, String dataId, Map<String, Object> extras, IDecisionCallback callback) {
        if (mDecisionApi == null) {
            return false;
        }
        ArrayMap<String, Object> extra2 = new ArrayMap<>();
        if (extras != null) {
            extra2.putAll(extras);
        }
        extra2.put(ID_KEY, dataId != null ? dataId : "");
        if (eventName != null && !eventName.equals(dataId)) {
            extra2.put(CATEGORY_KEY, eventName);
        }
        try {
            mDecisionApi.executeEvent(extra2, callback);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
