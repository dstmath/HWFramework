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
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.common.service.IDecision;
import com.huawei.hwpartsecurityservices.BuildConfig;
import com.huawei.trustedthingsauth.LogUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DecisionUtil {
    private static final String ACTION_COMMON_SERVICE_NAME = "com.huawei.recsys.decision.action.BIND_DECISION_SERVICE";
    private static final String ACTION_PACKAGE_NAME = "com.huawei.recsys";
    private static final String CATEGORY_KEY = "category";
    private static final String DECISION_ACTION = "com.huawei.securitymgr.intent.action.KeychainAutoFillEvent";
    private static final long EXECUTE_TIME_OUT = -1;
    private static final String ID_KEY = "id";
    private static final String TAG = "DecisionUtil";
    private static volatile DecisionUtil sInstance;
    private ConcurrentHashMap<String, DecisionCallback> mCallbackMap = new ConcurrentHashMap<>();
    private volatile IDecision mDecisionApi = null;
    private ServiceConnection mDecisionConnection = new ServiceConnection() {
        /* class com.huawei.server.security.hwkeychain.DecisionUtil.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.debug(DecisionUtil.TAG, "service connected.");
            DecisionUtil.this.mDecisionApi = IDecision.Stub.asInterface(service);
            if (DecisionUtil.this.mExtras == null) {
                LogUtils.error(DecisionUtil.TAG, "mExtras is null in onServiceConnected");
                return;
            }
            DecisionUtil decisionUtil = DecisionUtil.this;
            if (decisionUtil.executeEvent(DecisionUtil.DECISION_ACTION, decisionUtil.mExtras)) {
                synchronized (DecisionUtil.class) {
                    if (DecisionUtil.this.mIsNeededUnbindService) {
                        LogUtils.info(DecisionUtil.TAG, "unbind service in ServiceConnection.");
                        DecisionUtil.this.unbindService(ActivityThreadEx.currentActivityThread().getSystemUiContext());
                        DecisionUtil.this.mIsNeededUnbindService = false;
                    }
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            synchronized (DecisionUtil.class) {
                DecisionUtil.this.mDecisionApi = null;
            }
            LogUtils.info(DecisionUtil.TAG, "service disconnect.");
        }
    };
    private Map<String, Object> mExtras;
    private Handler mHandler = null;
    private volatile boolean mIsNeededUnbindService = true;

    private DecisionUtil() {
        LogUtil.info(TAG, "DecisionUtil construcor invoked.");
    }

    public static DecisionUtil getInstance() {
        if (sInstance == null) {
            synchronized (DecisionUtil.class) {
                if (sInstance == null) {
                    sInstance = new DecisionUtil();
                }
            }
        }
        return sInstance;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bindService(Context context) {
        if (context == null) {
            LogUtils.error(TAG, "bingService: The context is null.");
        } else if (this.mDecisionApi != null) {
            LogUtils.info(TAG, "service already start.");
        } else {
            if (this.mHandler == null) {
                this.mHandler = new Handler(context.getMainLooper());
            }
            Intent actionService = new Intent(ACTION_COMMON_SERVICE_NAME);
            actionService.setPackage(ACTION_PACKAGE_NAME);
            try {
                context.bindService(actionService, this.mDecisionConnection, 1);
            } catch (IllegalArgumentException | SecurityException e) {
                LogUtils.error(TAG, "DecisionUtil bind service error.");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unbindService(Context context) {
        if (context != null) {
            try {
                context.unbindService(this.mDecisionConnection);
            } catch (IllegalArgumentException | SecurityException e) {
                LogUtils.error(TAG, "unbind service error.");
            }
            this.mDecisionApi = null;
            this.mExtras = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean executeEvent(String eventName, Map<String, Object> extras) {
        return executeEvent(eventName, null, extras, null);
    }

    private boolean executeEvent(String eventName, String dataId, Map<String, Object> extras, DecisionCallback callback) {
        return executeEvent(eventName, dataId, extras, callback, -1);
    }

    private boolean executeEvent(String eventName, String dataId, Map<String, Object> extras, DecisionCallback callback, long timeout) {
        if (this.mDecisionApi == null) {
            return false;
        }
        Map<String, Object> eventInfoMap = getEventInfoMap(extras, dataId, eventName);
        String key = callback != null ? callback.toString() : null;
        DecisionCallback innerCallback = initDecisionCallback(key);
        innerCallback.setReversed(callback);
        if (callback != null && timeout > 0) {
            if (key != null) {
                this.mCallbackMap.put(key, callback);
            }
            postDelayHandler(key, timeout, innerCallback);
        }
        if (this.mDecisionApi == null) {
            LogUtils.info(TAG, "executeEvent: The mDecisionApi is null.");
            return false;
        }
        synchronized (DecisionUtil.class) {
            if (this.mDecisionApi == null) {
                LogUtils.error(TAG, "executeEvent: The mDecisionApi is null in synchronized block.");
                return false;
            }
            try {
                this.mDecisionApi.executeEvent(eventInfoMap, innerCallback);
                return true;
            } catch (RemoteException e) {
                LogUtils.error(TAG, "executeEvent: RemoteException occur.");
                return false;
            } catch (Exception e2) {
                LogUtils.error(TAG, "executeEvent: Unknown Exception occur.");
                return false;
            }
        }
    }

    private Map<String, Object> getEventInfoMap(Map<String, Object> extras, String dataId, String eventName) {
        Map<String, Object> eventInfoMap = new ArrayMap<>();
        if (extras != null) {
            eventInfoMap.putAll(extras);
        }
        eventInfoMap.put(ID_KEY, dataId != null ? dataId : BuildConfig.FLAVOR);
        if (eventName != null && !eventName.equals(dataId)) {
            eventInfoMap.put(CATEGORY_KEY, eventName);
        }
        return eventInfoMap;
    }

    private void postDelayHandler(String key, long timeout, DecisionCallback innerCallback) {
        this.mHandler.postDelayed(new Runnable(key, innerCallback) {
            /* class com.huawei.server.security.hwkeychain.$$Lambda$DecisionUtil$kiVZvfZK8A5zQN7YIyZp7tZjhDA */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ DecisionCallback f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                DecisionUtil.this.lambda$postDelayHandler$0$DecisionUtil(this.f$1, this.f$2);
            }
        }, timeout);
    }

    public /* synthetic */ void lambda$postDelayHandler$0$DecisionUtil(String key, DecisionCallback innerCallback) {
        DecisionCallback userCallback = key != null ? this.mCallbackMap.remove(key) : null;
        if (userCallback != null) {
            innerCallback.clearReversed();
            userCallback.onTimeout();
        }
    }

    private DecisionCallback initDecisionCallback(final String key) {
        return new DecisionCallback() {
            /* class com.huawei.server.security.hwkeychain.DecisionUtil.AnonymousClass2 */

            @Override // com.huawei.server.security.hwkeychain.DecisionCallback, com.huawei.common.service.IDecisionCallback
            public void onResult(Map result) throws RemoteException {
                if (key != null) {
                    DecisionUtil.this.mCallbackMap.remove(key);
                } else {
                    LogUtils.info(DecisionUtil.TAG, "The key is null in initDecisionCallback");
                }
                if (this.mReversed != null) {
                    this.mReversed.onResult(result);
                } else {
                    LogUtils.info(DecisionUtil.TAG, "The mReversed is null in initDecisionCallback.");
                }
            }
        };
    }

    /* access modifiers changed from: private */
    public class DecisionAyncTask extends AsyncTask<Void, Void, Void> {
        private DecisionAyncTask() {
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... voids) {
            LogUtils.info(DecisionUtil.TAG, "bind service in DecisionAyncTask.");
            DecisionUtil.this.bindService(ActivityThreadEx.currentActivityThread().getSystemUiContext());
            LogUtils.debug(DecisionUtil.TAG, "execute doInBackground in DecisionAyncTask.");
            DecisionUtil decisionUtil = DecisionUtil.this;
            if (!decisionUtil.executeEvent(DecisionUtil.DECISION_ACTION, decisionUtil.mExtras)) {
                return null;
            }
            synchronized (DecisionUtil.class) {
                if (DecisionUtil.this.mIsNeededUnbindService && DecisionUtil.this.mDecisionConnection != null) {
                    LogUtils.info(DecisionUtil.TAG, "unbind service in DecisionAyncTask.");
                    DecisionUtil.this.unbindService(ActivityThreadEx.currentActivityThread().getSystemUiContext());
                    DecisionUtil.this.mIsNeededUnbindService = false;
                }
            }
            return null;
        }
    }

    public void autoExecuteEvent(Map<String, Object> extras) {
        this.mExtras = extras;
        if (this.mExtras != null) {
            new DecisionAyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }
}
