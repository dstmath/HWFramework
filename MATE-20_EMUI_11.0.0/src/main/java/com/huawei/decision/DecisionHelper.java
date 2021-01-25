package com.huawei.decision;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.huawei.common.service.IDecision;
import com.huawei.common.service.IDecisionCallback;

public final class DecisionHelper {
    private static final String ACTION_COMMON_SERVICE_NAME = "com.huawei.recsys.decision.action.BIND_DECISION_SERVICE";
    private static final String CATEGORY_KEY = "category";
    private static final boolean DEBUG = false;
    private static final String ID_KEY = "id";
    public static final String ROLLBACK_EVENT = "com.huawei.control.intent.action.RollBackEvent";
    public static final String ROLLBACK_USED_EVENT = "com.huawei.control.intent.action.RollBackUsedEvent";
    private static final String SERVICE_PACKAGE_NAME = "com.huawei.recsys";
    private static final String TAG = DecisionHelper.class.getSimpleName();
    private IDecision mDecisionApi;
    private ServiceConnection mDecisionConnection = new ServiceConnection() {
        /* class com.huawei.decision.DecisionHelper.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            DecisionHelper.this.mDecisionApi = IDecision.Stub.asInterface(service);
            DecisionHelper.this.mIsBound = true;
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            DecisionHelper.this.mDecisionApi = null;
            DecisionHelper.this.mIsBound = DecisionHelper.DEBUG;
        }
    };
    private boolean mIsBound;

    public void bindService(Context context) {
        if (this.mIsBound) {
            Log.w(TAG, "service already binded");
        } else if (context != null && this.mDecisionApi != null) {
            Intent actionService = new Intent(ACTION_COMMON_SERVICE_NAME);
            actionService.setPackage(SERVICE_PACKAGE_NAME);
            try {
                context.bindService(actionService, this.mDecisionConnection, 1);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "bind service IllegalArgumentException");
            } catch (Exception e2) {
                Log.e(TAG, "bind service exception");
            }
        }
    }

    public void unbindService(Context context) {
        if (!this.mIsBound) {
            Log.w(TAG, "service already unbindService");
            return;
        }
        if (!(context == null || this.mDecisionApi == null)) {
            try {
                context.unbindService(this.mDecisionConnection);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "unbindService service IllegalArgumentException");
            } catch (Exception e2) {
                Log.e(TAG, "unbindService service exception");
            }
        }
        this.mDecisionApi = null;
        this.mIsBound = DEBUG;
    }

    public void executeEvent(String eventName) {
        if (this.mDecisionApi != null && !TextUtils.isEmpty(eventName)) {
            try {
                ArrayMap<String, String> extra = new ArrayMap<>();
                extra.put("id", "");
                extra.put(CATEGORY_KEY, eventName);
                this.mDecisionApi.executeEvent(extra, (IDecisionCallback) null);
            } catch (RemoteException e) {
                Log.e(TAG, "executeEvent RemoteException");
            } catch (Exception e2) {
                Log.e(TAG, "executeEvent exception");
            }
        }
    }
}
