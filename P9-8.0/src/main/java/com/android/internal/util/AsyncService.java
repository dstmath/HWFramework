package com.android.internal.util;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Slog;

public abstract class AsyncService extends Service {
    public static final int CMD_ASYNC_SERVICE_DESTROY = 16777216;
    public static final int CMD_ASYNC_SERVICE_ON_START_INTENT = 16777215;
    protected static final boolean DBG = true;
    private static final String TAG = "AsyncService";
    AsyncServiceInfo mAsyncServiceInfo;
    Handler mHandler;
    protected Messenger mMessenger;

    public static final class AsyncServiceInfo {
        public Handler mHandler;
        public int mRestartFlags;
    }

    public abstract AsyncServiceInfo createHandler();

    public Handler getHandler() {
        return this.mHandler;
    }

    public void onCreate() {
        super.onCreate();
        this.mAsyncServiceInfo = createHandler();
        this.mHandler = this.mAsyncServiceInfo.mHandler;
        this.mMessenger = new Messenger(this.mHandler);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Slog.d(TAG, "onStartCommand");
        Message msg = this.mHandler.obtainMessage();
        msg.what = 16777215;
        msg.arg1 = flags;
        msg.arg2 = startId;
        msg.obj = intent;
        this.mHandler.sendMessage(msg);
        return this.mAsyncServiceInfo.mRestartFlags;
    }

    public void onDestroy() {
        Slog.d(TAG, "onDestroy");
        Message msg = this.mHandler.obtainMessage();
        msg.what = 16777216;
        this.mHandler.sendMessage(msg);
    }

    public IBinder onBind(Intent intent) {
        return this.mMessenger.getBinder();
    }
}
