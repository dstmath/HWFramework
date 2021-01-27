package com.android.server.wifi.p2p;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.wifi.HwHiLog;
import com.huawei.common.service.IDecision;
import com.huawei.common.service.IDecisionCallback;
import java.util.Map;

public final class DecisionUtil {
    private static final String ACTION_COMMON_SERVICE_NAME = "com.huawei.recsys.decision.action.BIND_DECISION_SERVICE";
    private static final String ACTION_PACKAGE_NAME = "com.huawei.recsys";
    private static final String CATEGORY_KEY = "category";
    private static final String EVENT_NAME = "com.huawei.wifi.intent.action.WifiBridgeShare";
    private static final String ID_KEY = "id";
    private static final int OPER_SUCCESS = 0;
    private static final String TAG = DecisionUtil.class.getSimpleName();
    private static Context sContext;
    private static IDecision sDecisionApi = null;
    private static ServiceConnection sDecisionConnection = new ServiceConnection() {
        /* class com.android.server.wifi.p2p.DecisionUtil.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            HwHiLog.d(DecisionUtil.TAG, false, "service connected.", new Object[0]);
            IDecision unused = DecisionUtil.sDecisionApi = IDecision.Stub.asInterface(service);
            DecisionUtil.executeEvent(DecisionUtil.EVENT_NAME);
            DecisionUtil.unbindService(DecisionUtil.sContext);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            IDecision unused = DecisionUtil.sDecisionApi = null;
            Context unused2 = DecisionUtil.sContext = null;
            HwHiLog.i(DecisionUtil.TAG, false, "service disconnect.", new Object[0]);
        }
    };

    private DecisionUtil() {
    }

    public static void bindService(Context context) {
        if (context == null || sDecisionApi != null) {
            HwHiLog.i(TAG, false, "service already binded", new Object[0]);
            return;
        }
        sContext = context;
        Intent actionService = new Intent(ACTION_COMMON_SERVICE_NAME);
        actionService.setPackage(ACTION_PACKAGE_NAME);
        try {
            HwHiLog.d(TAG, false, "bindService %{public}s", new Object[]{String.valueOf(context.bindService(actionService, sDecisionConnection, 1))});
        } catch (IllegalArgumentException | SecurityException e) {
            HwHiLog.e(TAG, false, "bindService: exception happens", new Object[0]);
        }
    }

    public static void unbindService(Context context) {
        if (context != null) {
            try {
                context.unbindService(sDecisionConnection);
            } catch (IllegalArgumentException | SecurityException e) {
                HwHiLog.e(TAG, false, "unbindService fail", new Object[0]);
            }
        }
    }

    public static boolean executeEvent(String eventName) {
        return executeEvent(eventName, null, null, null);
    }

    public static boolean executeEvent(String eventName, String dataId, Map<String, Object> extras, IDecisionCallback callback) {
        if (sDecisionApi == null) {
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
            sDecisionApi.executeEvent(extra2, callback);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
}
