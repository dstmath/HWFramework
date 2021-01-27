package com.android.server;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import com.huawei.common.service.IDecision;
import java.lang.Thread;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DecisionUtil {
    private static final String CATEGORY_KEY = "category";
    private static final String ID_KEY = "id";
    private static final int OPER_SUCCESS = 0;
    private static final String TAG = DecisionUtil.class.getSimpleName();
    private static ConcurrentHashMap<String, DecisionCallback> sCallbackList = new ConcurrentHashMap<>();
    private static IDecision sDecisionApi = null;
    private static ServiceConnection sDecisionConnection = new ServiceConnection() {
        /* class com.android.server.DecisionUtil.AnonymousClass5 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(DecisionUtil.TAG, "service connected.");
            IDecision unused = DecisionUtil.sDecisionApi = IDecision.Stub.asInterface(service);
            Thread callbackThread = new Thread(new Runnable() {
                /* class com.android.server.DecisionUtil.AnonymousClass5.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    DecisionUtil.sServiceConnectionCallback.onServiceConnected();
                }
            }, "DecisionConnectionThread");
            callbackThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                /* class com.android.server.DecisionUtil.AnonymousClass5.AnonymousClass2 */

                @Override // java.lang.Thread.UncaughtExceptionHandler
                public void uncaughtException(Thread t, Throwable e) {
                    String str = DecisionUtil.TAG;
                    Log.e(str, t.getName() + " : " + e.getMessage());
                }
            });
            callbackThread.start();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            IDecision unused = DecisionUtil.sDecisionApi = null;
            Log.i(DecisionUtil.TAG, "service disconnect.");
        }
    };
    private static Handler sHander = null;
    private static ServiceConnectionCallback sServiceConnectionCallback = null;

    public static void bindService(Context context, ServiceConnectionCallback callback) {
        sServiceConnectionCallback = callback;
        if (context == null || sDecisionApi != null) {
            Log.i(TAG, "service already binded");
            Thread callbackThread = new Thread(new Runnable() {
                /* class com.android.server.DecisionUtil.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    DecisionUtil.sServiceConnectionCallback.onServiceConnected();
                }
            }, "DecisionUtilThread");
            callbackThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                /* class com.android.server.DecisionUtil.AnonymousClass2 */

                @Override // java.lang.Thread.UncaughtExceptionHandler
                public void uncaughtException(Thread t, Throwable e) {
                    String str = DecisionUtil.TAG;
                    Log.e(str, t.getName() + " : " + e.getMessage());
                }
            });
            callbackThread.start();
            return;
        }
        if (sHander == null) {
            sHander = new Handler(context.getMainLooper());
        }
        Intent actionService = new Intent("com.huawei.recsys.decision.action.BIND_DECISION_SERVICE");
        actionService.setPackage("com.huawei.recsys");
        try {
            context.bindService(actionService, sDecisionConnection, 1);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void unbindService(Context context) {
        sServiceConnectionCallback = null;
        if (context != null) {
            try {
                context.unbindService(sDecisionConnection);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            sDecisionApi = null;
        }
    }

    public static boolean executeEvent(String eventName) {
        return executeEvent(eventName, (String) null, (Map<String, Object>) null, (DecisionCallback) null);
    }

    public static boolean executeEvent(String eventName, Map<String, Object> extras) {
        return executeEvent(eventName, (String) null, extras, (DecisionCallback) null);
    }

    public static boolean executeEvent(String eventName, DecisionCallback callback) {
        return executeEvent(eventName, (String) null, (Map<String, Object>) null, callback);
    }

    public static boolean executeEvent(String eventName, DecisionCallback callback, long timeout) {
        return executeEvent(eventName, null, null, callback, timeout);
    }

    public static boolean executeEvent(String eventName, Map<String, Object> extras, DecisionCallback callback) {
        return executeEvent(eventName, (String) null, extras, callback);
    }

    public static boolean executeEvent(String eventName, Map<String, Object> extras, DecisionCallback callback, long timeout) {
        return executeEvent(eventName, null, extras, callback, timeout);
    }

    public static boolean executeEvent(String eventName, String dataId) {
        return executeEvent(eventName, dataId, (Map<String, Object>) null, (DecisionCallback) null);
    }

    public static boolean executeEvent(String eventName, String dataId, Map<String, Object> extras) {
        return executeEvent(eventName, dataId, extras, (DecisionCallback) null);
    }

    public static boolean executeEvent(String eventName, String dataId, DecisionCallback callback) {
        return executeEvent(eventName, dataId, (Map<String, Object>) null, callback);
    }

    public static boolean executeEvent(String eventName, String dataId, DecisionCallback callback, long timeout) {
        return executeEvent(eventName, dataId, null, callback, timeout);
    }

    public static boolean executeEvent(String eventName, String dataId, Map<String, Object> extras, DecisionCallback callback) {
        return executeEvent(eventName, dataId, extras, callback, -1);
    }

    public static boolean executeEvent(String eventName, String dataId, Map<String, Object> extras, DecisionCallback callback, long timeout) {
        final String key;
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
        if (callback != null) {
            key = callback.toString();
        } else {
            key = null;
        }
        final DecisionCallback innerCallback = new DecisionCallback() {
            /* class com.android.server.DecisionUtil.AnonymousClass3 */

            @Override // com.android.server.DecisionCallback
            public void onResult(Map result) throws RemoteException {
                if (key != null) {
                    DecisionUtil.sCallbackList.remove(key);
                }
                if (this.mReversed1 != null) {
                    try {
                        this.mReversed1.onResult(result);
                    } catch (Exception e) {
                    }
                }
            }
        };
        innerCallback.setReversed1(callback);
        if (callback != null && timeout > 0) {
            sCallbackList.put(key, callback);
            sHander.postDelayed(new Runnable() {
                /* class com.android.server.DecisionUtil.AnonymousClass4 */

                @Override // java.lang.Runnable
                public void run() {
                    DecisionCallback userCallback = (DecisionCallback) DecisionUtil.sCallbackList.remove(key);
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
            sDecisionApi.executeEvent(extra2, innerCallback);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean insertBusinessData(String category) {
        return insertBusinessData(category, null, null);
    }

    public static boolean insertBusinessData(String category, String dataId) {
        return insertBusinessData(category, dataId, null);
    }

    public static boolean insertBusinessData(String category, Map<String, Object> extras) {
        return insertBusinessData(category, null, extras);
    }

    public static boolean insertBusinessData(String category, String dataId, Map<String, Object> extras) {
        if (sDecisionApi == null) {
            return false;
        }
        ArrayMap<String, Object> extra2 = new ArrayMap<>();
        if (extras != null) {
            extra2.putAll(extras);
        }
        extra2.put(ID_KEY, dataId != null ? dataId : "");
        if (category != null && !category.equals(dataId)) {
            extra2.put(CATEGORY_KEY, category);
        }
        try {
            if (sDecisionApi.insertBusinessData(extra2) == 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean removeBusinessData(String category) {
        return removeBusinessData(category, null);
    }

    public static boolean removeBusinessData(String category, String dataId) {
        IDecision iDecision = sDecisionApi;
        if (iDecision == null) {
            return false;
        }
        try {
            if (iDecision.removeBusinessData(category, dataId) == 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
