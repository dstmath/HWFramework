package com.huawei.coauth.msg;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.coauthservice.identitymgr.constants.ServicePackage;

public class CoMessengerClient {
    private static final String MSG_TAG = "msg";
    private static final String SERVER_ACTION = "com.huawei.coauthservice.Messenger";
    private static final String SERVER_COMPONENT = "com.huawei.coauthservice.ScheduleService";
    private static final String SERVER_PACKAGE = "com.huawei.coauthservice";
    private static final String TAG = "MessengerClient";
    private static CoMessengerClient instance;
    private CoAuthServiceConnection connection = null;

    public interface IConnectServiceCallback {
        void onConnectFailed();

        void onConnected();

        void onDisconnect();
    }

    private CoMessengerClient() {
    }

    public static synchronized CoMessengerClient getInstance() {
        CoMessengerClient coMessengerClient;
        synchronized (CoMessengerClient.class) {
            if (instance == null) {
                instance = new CoMessengerClient();
            }
            coMessengerClient = instance;
        }
        return coMessengerClient;
    }

    public void connectServer(Context context, IConnectServiceCallback callback) {
        CoAuthServiceConnection coAuthServiceConnection = this.connection;
        if (coAuthServiceConnection == null) {
            this.connection = new CoAuthServiceConnection(callback);
        } else {
            coAuthServiceConnection.callback = callback;
        }
        Handler mainHandler = new Handler(Looper.getMainLooper());
        if (this.connection.isActive()) {
            mainHandler.post(new Runnable() {
                /* class com.huawei.coauth.msg.CoMessengerClient.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    Log.i(CoMessengerClient.TAG, "rebind?");
                    CoMessengerClient.this.connection.callback.onConnected();
                }
            });
        } else if (!isCoAuthPackageExist(context)) {
            mainHandler.post(new Runnable() {
                /* class com.huawei.coauth.msg.CoMessengerClient.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    Log.i(CoMessengerClient.TAG, "package not exist?");
                    CoMessengerClient.this.connection.callback.onConnectFailed();
                }
            });
        } else {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.huawei.coauthservice", "com.huawei.coauthservice.ScheduleService"));
            intent.setPackage("com.huawei.coauthservice");
            intent.setAction(SERVER_ACTION);
            Log.d(TAG, "MessengerClient start bind");
            context.bindService(intent, this.connection, 1);
        }
    }

    private boolean isCoAuthPackageExist(Context context) {
        boolean isExist = false;
        try {
            boolean z = false;
            ApplicationInfo info = context.getPackageManager().getApplicationInfo("com.huawei.coauthservice", 0);
            if (info != null) {
                if (isSystemApp(info) && isSignedWithPlatformKey(context, "com.huawei.coauthservice")) {
                    z = true;
                }
                isExist = z;
            }
        } catch (PackageManager.NameNotFoundException e) {
            isExist = false;
        }
        Log.d(TAG, "CoAuth package exist info=" + isExist);
        return isExist;
    }

    private boolean isSystemApp(ApplicationInfo info) {
        return (info.flags & 1) != 0;
    }

    private boolean isSignedWithPlatformKey(Context context, String packageName) {
        return context.getPackageManager().checkSignatures(packageName, ServicePackage.CHECK_PACKAGE) == 0;
    }

    public void disConnectServer(Context context) {
        context.unbindService(this.connection);
        this.connection.callback.onDisconnect();
        this.connection.callback = null;
        this.connection.messenger = null;
    }

    /* access modifiers changed from: private */
    public static class CoAuthServiceConnection implements ServiceConnection {
        private IConnectServiceCallback callback;
        private Messenger messenger = null;

        CoAuthServiceConnection(IConnectServiceCallback callback2) {
            this.callback = callback2;
        }

        /* access modifiers changed from: package-private */
        public boolean isActive() {
            Messenger messenger2 = this.messenger;
            return messenger2 != null && messenger2.getBinder().isBinderAlive();
        }

        /* access modifiers changed from: package-private */
        public int sendMsg(Message message) {
            try {
                if (this.messenger == null) {
                    Log.d(CoMessengerClient.TAG, "send error, messenger unexpected null");
                    return 1;
                }
                this.messenger.send(message);
                return 0;
            } catch (RemoteException e) {
                Log.d(CoMessengerClient.TAG, "send message remote error");
                return 1;
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            this.messenger = new Messenger(service);
            IConnectServiceCallback iConnectServiceCallback = this.callback;
            if (iConnectServiceCallback != null) {
                iConnectServiceCallback.onConnected();
            }
            Log.d(CoMessengerClient.TAG, "MessengerClient onServiceConnected");
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            IConnectServiceCallback iConnectServiceCallback = this.callback;
            if (iConnectServiceCallback != null) {
                iConnectServiceCallback.onDisconnect();
            }
            Log.d(CoMessengerClient.TAG, "MessengerClient onServiceDisconnected");
        }
    }

    public int sendMsgToServer(byte[] msgData, ICoMessageProcesser processer) {
        CoAuthServiceConnection coAuthServiceConnection = this.connection;
        if (coAuthServiceConnection == null || !coAuthServiceConnection.isActive() || processer == null) {
            return 1;
        }
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putByteArray(MSG_TAG, msgData);
        message.setData(bundle);
        message.replyTo = new Messenger(new ClientHandler(processer));
        Log.d(TAG, "MessengerClient send msg to server");
        return this.connection.sendMsg(message);
    }

    /* access modifiers changed from: private */
    public static class ClientHandler extends Handler {
        ICoMessageProcesser msgProcesser;

        ClientHandler(ICoMessageProcesser processer) {
            super(Looper.getMainLooper());
            this.msgProcesser = processer;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            if (bundle != null) {
                try {
                    byte[] msgData = bundle.getByteArray(CoMessengerClient.MSG_TAG);
                    Log.d(CoMessengerClient.TAG, "MessengerClient data");
                    if (msgData != null) {
                        int handleMsgRet = this.msgProcesser.handleMsg(msgData);
                        Log.d(CoMessengerClient.TAG, "MessengerClient handle msg result = " + handleMsgRet);
                        return;
                    }
                    Log.d(CoMessengerClient.TAG, "MessengerClient handle get msg tag null");
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(CoMessengerClient.TAG, "MessengerClient bundle get msg tag bound error");
                }
            }
        }
    }
}
