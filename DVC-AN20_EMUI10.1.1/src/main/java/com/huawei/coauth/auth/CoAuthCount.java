package com.huawei.coauth.auth;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.huawei.coauth.msg.CoAuthCountMessageClient;

public class CoAuthCount {
    private static final Object LOCK = new Object();
    private static final String TAG = "CoAuthCount";
    private static CoAuthCountMessageClient coAuthCountMessageClient;
    private static CoAuthCount instance;
    private static Handler mainHandler;
    private CoAuthType mainCoAuthType;
    private Context mainContext;

    public interface IConnectServiceCallback {
        void onConnectFailed();

        void onConnected();

        void onDisconnect();
    }

    private CoAuthCount() {
    }

    public static synchronized CoAuthCount getInstance(Context context) {
        CoAuthCount coAuthCount;
        synchronized (CoAuthCount.class) {
            if (instance == null) {
                instance = new CoAuthCount();
                instance.setContext(context);
                synchronized (LOCK) {
                    if (mainHandler == null) {
                        mainHandler = new Handler(Looper.getMainLooper());
                    }
                }
            }
            coAuthCount = instance;
        }
        return coAuthCount;
    }

    private void setContext(Context context) {
        this.mainContext = context;
    }

    /* access modifiers changed from: private */
    public static void coAuthCountServiceConnection(Context context, IConnectServiceCallback callback) {
        Log.i(TAG, "resetCount start connection");
        coAuthCountMessageClient = CoAuthCountMessageClient.getInstance();
        coAuthCountMessageClient.connectServer(context, new ConnectCoAuthCountServiceCallback(callback));
    }

    public void resetCount(CoAuthType coAuthType) {
        if (coAuthType != null) {
            Log.i(TAG, "resetCount begin, coAuthType = " + coAuthType.getValue());
            this.mainCoAuthType = coAuthType;
            synchronized (LOCK) {
                mainHandler.post(new Runnable() {
                    /* class com.huawei.coauth.auth.CoAuthCount.AnonymousClass1 */

                    public void run() {
                        CoAuthCount.coAuthCountServiceConnection(CoAuthCount.this.mainContext, new ConnectCoAuthCountServiceCallbackProcess(CoAuthCount.this.mainContext, CoAuthCount.this.mainCoAuthType));
                    }
                });
            }
            return;
        }
        Log.e(TAG, "resetCount begin, coAuthType not expect null");
    }

    private static class ConnectCoAuthCountServiceCallbackProcess implements IConnectServiceCallback {
        CoAuthType coAuthType;
        Context context;

        ConnectCoAuthCountServiceCallbackProcess(Context context2, CoAuthType coAuthType2) {
            this.context = context2;
            this.coAuthType = coAuthType2;
        }

        @Override // com.huawei.coauth.auth.CoAuthCount.IConnectServiceCallback
        public void onConnected() {
            Log.i(CoAuthCount.TAG, "ConnectCoAuthCountService onConnected");
            int resultCode = CoAuthCount.coAuthCountMessageClient.sendMsgToServer(this.coAuthType.getValue());
            if (resultCode != 0) {
                Log.e(CoAuthCount.TAG, "resetCount error, resultCode = " + resultCode);
            }
            CoAuthCount.coAuthCountMessageClient.disConnectServer(this.context);
        }

        @Override // com.huawei.coauth.auth.CoAuthCount.IConnectServiceCallback
        public void onConnectFailed() {
            Log.i(CoAuthCount.TAG, "ConnectCoAuthCountService onConnectFailed");
        }

        @Override // com.huawei.coauth.auth.CoAuthCount.IConnectServiceCallback
        public void onDisconnect() {
            Log.i(CoAuthCount.TAG, "ConnectCoAuthCountService onDisconnect");
        }
    }
}
