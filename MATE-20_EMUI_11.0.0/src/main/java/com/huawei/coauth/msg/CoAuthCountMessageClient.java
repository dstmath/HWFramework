package com.huawei.coauth.msg;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.coauthservice.identitymgr.constants.ServicePackage;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CoAuthCountMessageClient {
    private static final String MSG_COUNT_TAG = "count_msg";
    private static final String SERVER_ACTION = "com.huawei.coauthservice.Messenger";
    private static final String SERVER_COMPONENT = "com.huawei.coauthservice.CoAuthCountService";
    private static final String SERVER_PACKAGE = "com.huawei.coauthservice";
    private static final String TAG = "CoAuthCountMessage";
    private static CoAuthCountMessageClient instance;
    private CoAuthCountServiceConnection connection = null;

    public interface IConnectCoAuthCountServiceCallback {
        void onConnectFailed();

        void onConnected();

        void onDisconnect();
    }

    private CoAuthCountMessageClient() {
    }

    public static synchronized CoAuthCountMessageClient getInstance() {
        CoAuthCountMessageClient coAuthCountMessageClient;
        synchronized (CoAuthCountMessageClient.class) {
            if (instance == null) {
                instance = new CoAuthCountMessageClient();
            }
            coAuthCountMessageClient = instance;
        }
        return coAuthCountMessageClient;
    }

    public void connectServer(Context context, IConnectCoAuthCountServiceCallback callback) {
        if (!isCoAuthPackageActive(context)) {
            Log.i(TAG, "CoAuthCountMessageClient isCoAuthPackageActive false");
            return;
        }
        CoAuthCountServiceConnection coAuthCountServiceConnection = this.connection;
        if (coAuthCountServiceConnection == null) {
            Log.i(TAG, "CoAuthCountServiceConnection init");
            this.connection = new CoAuthCountServiceConnection(callback);
        } else {
            coAuthCountServiceConnection.callback = callback;
        }
        if (this.connection.isActive()) {
            Log.i(TAG, "rebind?");
            this.connection.callback.onConnected();
        } else if (!isCoAuthPackageExist(context)) {
            Log.i(TAG, "package not exist?");
            this.connection.callback.onConnectFailed();
        } else {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.huawei.coauthservice", SERVER_COMPONENT));
            intent.setPackage("com.huawei.coauthservice");
            intent.setAction(SERVER_ACTION);
            Log.d(TAG, "CoAuthCountMessageClient start bind");
            context.bindService(intent, this.connection, 1);
        }
    }

    private boolean isCoAuthPackageExist(Context context) {
        boolean z = false;
        if (context == null) {
            Log.d(TAG, "context null");
            return false;
        }
        boolean isExist = false;
        try {
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

    private boolean isCoAuthPackageActive(Context context) {
        if (context == null) {
            Log.i(TAG, "CoAuthCountMessageClient isCoAuthPackageActive context null");
            return false;
        }
        Optional<Object> systemService = Optional.ofNullable(context.getSystemService("activity"));
        ActivityManager activityManager = null;
        if (systemService.isPresent() && !Objects.isNull(systemService.get()) && (systemService.get() instanceof ActivityManager)) {
            activityManager = (ActivityManager) systemService.get();
        }
        if (Objects.isNull(activityManager)) {
            Log.i(TAG, "CoAuthCountMessageClient isCoAuthPackageActive activityManager null");
            return false;
        }
        List<ActivityManager.RunningAppProcessInfo> runningAppInfoLst = activityManager.getRunningAppProcesses();
        if (Objects.isNull(runningAppInfoLst)) {
            Log.i(TAG, "CoAuthCountMessageClient isCoAuthPackageActive runningAppInfoLst null");
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appInfo : runningAppInfoLst) {
            if (!Objects.isNull(appInfo) && "com.huawei.coauthservice".equals(appInfo.processName)) {
                Log.i(TAG, "CoAuthCountMessageClient isCoAuthPackageActive appInfo =com.huawei.coauthservice");
                return true;
            }
        }
        Log.i(TAG, "CoAuthCountMessageClient isCoAuthPackageActive end");
        return false;
    }

    private boolean isSystemApp(ApplicationInfo info) {
        return (info.flags & 1) != 0;
    }

    private boolean isSignedWithPlatformKey(Context context, String packageName) {
        return context.getPackageManager().checkSignatures(packageName, ServicePackage.CHECK_PACKAGE) == 0;
    }

    public void disConnectServer(Context context) {
        context.unbindService(this.connection);
        this.connection.callback = null;
        this.connection.messenger = null;
    }

    private static class CoAuthCountServiceConnection implements ServiceConnection {
        private IConnectCoAuthCountServiceCallback callback;
        private Messenger messenger = null;

        CoAuthCountServiceConnection(IConnectCoAuthCountServiceCallback callback2) {
            this.callback = callback2;
        }

        /* access modifiers changed from: package-private */
        public int sendMsg(Message message) {
            try {
                if (this.messenger == null) {
                    Log.d(CoAuthCountMessageClient.TAG, "CoAuthCountMessageClient send error, messenger unexpected null");
                    return 1;
                }
                this.messenger.send(message);
                return 0;
            } catch (RemoteException e) {
                Log.d(CoAuthCountMessageClient.TAG, "CoAuthCountMessageClient send message remote error");
                return 1;
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isActive() {
            Messenger messenger2 = this.messenger;
            return messenger2 != null && messenger2.getBinder().isBinderAlive();
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            this.messenger = new Messenger(service);
            IConnectCoAuthCountServiceCallback iConnectCoAuthCountServiceCallback = this.callback;
            if (iConnectCoAuthCountServiceCallback != null) {
                iConnectCoAuthCountServiceCallback.onConnected();
            }
            Log.d(CoAuthCountMessageClient.TAG, "CoAuthCountMessageClient onServiceConnected");
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            IConnectCoAuthCountServiceCallback iConnectCoAuthCountServiceCallback = this.callback;
            if (iConnectCoAuthCountServiceCallback != null) {
                iConnectCoAuthCountServiceCallback.onDisconnect();
            }
            Log.d(CoAuthCountMessageClient.TAG, "CoAuthCountMessageClient onServiceDisconnected");
        }
    }

    public int sendMsgToServer(int authType) {
        CoAuthCountServiceConnection coAuthCountServiceConnection = this.connection;
        if (coAuthCountServiceConnection == null) {
            Log.d(TAG, "MessengerClient connection unexpected null");
            return 1;
        } else if (!coAuthCountServiceConnection.isActive()) {
            Log.d(TAG, "MessengerClient connection unexpected inactive");
            return 1;
        } else {
            Message message = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putInt(MSG_COUNT_TAG, authType);
            message.setData(bundle);
            Log.d(TAG, "MessengerClient send msg to server");
            return this.connection.sendMsg(message);
        }
    }
}
