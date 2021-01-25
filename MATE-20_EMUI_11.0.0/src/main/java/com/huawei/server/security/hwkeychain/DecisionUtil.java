package com.huawei.server.security.hwkeychain;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.common.service.IDecision;
import com.huawei.hwpartsecurityservices.BuildConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DecisionUtil {
    private static final String CATEGORY_KEY = "category";
    private static final String DECISION_ACTION = "com.huawei.securitymgr.intent.action.KeychainAutoFillEvent";
    private static final String ID_KEY = "id";
    private static final String TAG = DecisionUtil.class.getSimpleName();
    private static ConcurrentHashMap<String, DecisionCallback> mCallbackList = new ConcurrentHashMap<>();
    private static IDecision mDecisionApi = null;
    private static ServiceConnection mDecisionConnection = new ServiceConnection() {
        /* class com.huawei.server.security.hwkeychain.DecisionUtil.AnonymousClass3 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(DecisionUtil.TAG, "service connected.");
            IDecision unused = DecisionUtil.mDecisionApi = IDecision.Stub.asInterface(service);
            if (DecisionUtil.sExtras != null) {
                Log.d(DecisionUtil.TAG, "execute again.");
                if (DecisionUtil.executeEvent(DecisionUtil.DECISION_ACTION, DecisionUtil.sExtras)) {
                    Log.d(DecisionUtil.TAG, "unbind service.");
                    DecisionUtil.unbindService(ActivityThreadEx.currentActivityThread().getSystemUiContext());
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            IDecision unused = DecisionUtil.mDecisionApi = null;
            Log.i(DecisionUtil.TAG, "service disconnect.");
        }
    };
    private static Handler mHander = null;
    private static Map<String, Object> sExtras;

    public static void bindService(Context context) {
        if (context == null || mDecisionApi != null) {
            Log.i(TAG, "service already binded");
            return;
        }
        if (mHander == null) {
            mHander = new Handler(context.getMainLooper());
        }
        Intent actionService = new Intent("com.huawei.recsys.decision.action.BIND_DECISION_SERVICE");
        actionService.setPackage("com.huawei.recsys");
        try {
            context.bindService(actionService, mDecisionConnection, 1);
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e(TAG, "DecisionUtil bind service error");
        }
    }

    public static void unbindService(Context context) {
        if (context != null) {
            try {
                context.unbindService(mDecisionConnection);
            } catch (IllegalArgumentException | SecurityException e) {
                Log.e(TAG, "unbind service error");
            }
            mDecisionApi = null;
            sExtras = null;
        }
    }

    public static boolean executeEvent(String eventName, Map<String, Object> extras) {
        return executeEvent(eventName, null, extras, null);
    }

    public static boolean executeEvent(String eventName, String dataId, Map<String, Object> extras, DecisionCallback callback) {
        return executeEvent(eventName, dataId, extras, callback, -1);
    }

    public static boolean executeEvent(String eventName, String dataId, Map<String, Object> extras, DecisionCallback callback, long timeout) {
        if (mDecisionApi == null) {
            return false;
        }
        ArrayMap<String, Object> extra2 = new ArrayMap<>();
        if (extras != null) {
            extra2.putAll(extras);
        }
        extra2.put(ID_KEY, dataId != null ? dataId : BuildConfig.FLAVOR);
        if (eventName != null && (dataId == null || !eventName.equals(dataId))) {
            extra2.put(CATEGORY_KEY, eventName);
        }
        final String key = callback != null ? callback.toString() : null;
        final DecisionCallback innerCallback = new DecisionCallback() {
            /* class com.huawei.server.security.hwkeychain.DecisionUtil.AnonymousClass1 */

            @Override // com.huawei.server.security.hwkeychain.DecisionCallback, com.huawei.common.service.IDecisionCallback
            public void onResult(Map result) throws RemoteException {
                if (key != null) {
                    DecisionUtil.mCallbackList.remove(key);
                }
                if (this.mReversed1 != null) {
                    this.mReversed1.onResult(result);
                }
            }
        };
        innerCallback.setReversed1(callback);
        if (callback != null && timeout > 0) {
            if (key != null) {
                mCallbackList.put(key, callback);
            }
            mHander.postDelayed(new Runnable() {
                /* class com.huawei.server.security.hwkeychain.DecisionUtil.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    DecisionCallback userCallback = key != null ? (DecisionCallback) DecisionUtil.mCallbackList.remove(key) : null;
                    if (userCallback != null) {
                        innerCallback.clearReversed1();
                        try {
                            userCallback.onTimeout();
                        } catch (Exception e) {
                        }
                    }
                }
            }, timeout);
        }
        try {
            mDecisionApi.executeEvent(extra2, innerCallback);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class DecisionAyncTask extends AsyncTask<Void, Void, Void> {
        private DecisionAyncTask() {
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... voids) {
            DecisionUtil.bindService(ActivityThreadEx.currentActivityThread().getSystemUiContext());
            Log.d(DecisionUtil.TAG, "execute event.");
            if (!DecisionUtil.executeEvent(DecisionUtil.DECISION_ACTION, DecisionUtil.sExtras)) {
                return null;
            }
            Log.d(DecisionUtil.TAG, "unbind service.");
            DecisionUtil.unbindService(ActivityThreadEx.currentActivityThread().getSystemUiContext());
            return null;
        }
    }

    public static void autoExecuteEvent(Map<String, Object> extras) {
        sExtras = extras;
        if (sExtras != null) {
            new DecisionAyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }
}
