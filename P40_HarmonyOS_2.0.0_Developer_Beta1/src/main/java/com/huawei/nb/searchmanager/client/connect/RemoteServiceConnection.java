package com.huawei.nb.searchmanager.client.connect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import com.huawei.nb.searchmanager.utils.logger.DSLog;

public class RemoteServiceConnection {
    private static final String COMMAND_TYPE_APP = "APP";
    private static final String TAG = "RemoteServiceConnection";
    private IBinder binder;
    private OnConnectListener connectListener;
    private ServiceConnection connection = new ServiceConnection() {
        /* class com.huawei.nb.searchmanager.client.connect.RemoteServiceConnection.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            RemoteServiceConnection.this.binder = iBinder;
            if (RemoteServiceConnection.this.connectListener != null) {
                RemoteServiceConnection.this.connectListener.onConnect(RemoteServiceConnection.this.binder);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            if (RemoteServiceConnection.this.binder != null) {
                RemoteServiceConnection.this.binder = null;
                if (RemoteServiceConnection.this.connectListener != null) {
                    RemoteServiceConnection.this.connectListener.onDisconnect();
                }
                DSLog.et(RemoteServiceConnection.TAG, "Connection to search service is disconnected unexpectedly.", new Object[0]);
            }
        }
    };
    private final Context context;
    private final String serviceAction;
    private final String serviceClassName;
    private final String servicePackageName;

    public interface OnConnectListener {
        void onConnect(IBinder iBinder);

        void onDisconnect();
    }

    public RemoteServiceConnection(Context context2, String str, String str2) {
        this.context = context2;
        this.serviceAction = null;
        this.servicePackageName = str;
        this.serviceClassName = str2;
        this.binder = null;
        DSLog.it(TAG, "create search ServiceConnection with package name", new Object[0]);
    }

    public RemoteServiceConnection(Context context2, String str) {
        this.context = context2;
        this.serviceAction = str;
        this.servicePackageName = null;
        this.serviceClassName = null;
        this.binder = null;
        DSLog.it(TAG, "create search ServiceConnection with action", new Object[0]);
    }

    public boolean open(OnConnectListener onConnectListener) {
        Intent intent;
        String str;
        this.connectListener = onConnectListener;
        String str2 = this.servicePackageName;
        if (str2 == null || (str = this.serviceClassName) == null) {
            intent = createImplicitIntent(this.serviceAction);
        } else {
            intent = createExplicitIntent(str2, str);
        }
        if (intent == null) {
            DSLog.et(TAG, "Failed to find the given search service action.", new Object[0]);
            return false;
        }
        intent.setType(COMMAND_TYPE_APP);
        try {
            if (this.context.bindService(intent, this.connection, 1)) {
                return true;
            }
            DSLog.et(TAG, "Failed to connect to search service.", new Object[0]);
            return false;
        } catch (SecurityException unused) {
            DSLog.et(TAG, "Failed to bind search service.", new Object[0]);
            return false;
        }
    }

    public boolean close() {
        this.context.unbindService(this.connection);
        this.binder = null;
        this.connectListener = null;
        return true;
    }

    private Intent createImplicitIntent(String str) {
        Intent intent;
        ResolveInfo resolveService;
        PackageManager packageManager = this.context.getPackageManager();
        if (packageManager == null || (resolveService = packageManager.resolveService((intent = new Intent(str)), 131072)) == null) {
            return null;
        }
        intent.setComponent(new ComponentName(resolveService.serviceInfo.packageName, resolveService.serviceInfo.name));
        return intent;
    }

    private Intent createExplicitIntent(String str, String str2) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(str, str2));
        return intent;
    }
}
