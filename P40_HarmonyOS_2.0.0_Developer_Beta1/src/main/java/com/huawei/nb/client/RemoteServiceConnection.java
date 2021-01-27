package com.huawei.nb.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import com.huawei.nb.utils.logger.DSLog;

public class RemoteServiceConnection {
    private IBinder binder;
    private OnConnectListener connectListener;
    private ServiceConnection connection = new ServiceConnection() {
        /* class com.huawei.nb.client.RemoteServiceConnection.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            DSLog.i("onServiceConnected invoke.", new Object[0]);
            RemoteServiceConnection.this.binder = iBinder;
            if (RemoteServiceConnection.this.connectListener != null) {
                RemoteServiceConnection.this.connectListener.onConnect(RemoteServiceConnection.this.binder);
            } else {
                DSLog.i("Not process callback: connectListener is null.", new Object[0]);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            if (RemoteServiceConnection.this.binder != null) {
                RemoteServiceConnection.this.binder = null;
                if (RemoteServiceConnection.this.connectListener != null) {
                    RemoteServiceConnection.this.connectListener.onDisconnect();
                }
                DSLog.e("Connection to data service is disconnected unexpectedly.", new Object[0]);
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
    }

    public RemoteServiceConnection(Context context2, String str) {
        this.context = context2;
        this.serviceAction = str;
        this.servicePackageName = null;
        this.serviceClassName = null;
        this.binder = null;
    }

    public boolean open(OnConnectListener onConnectListener) {
        Intent intent;
        String str;
        try {
            close();
        } catch (IllegalArgumentException unused) {
            DSLog.i("Service not registered.", new Object[0]);
        }
        this.connectListener = onConnectListener;
        String str2 = this.servicePackageName;
        if (str2 == null || (str = this.serviceClassName) == null) {
            intent = createImplicitIntent(this.serviceAction);
        } else {
            intent = createExplicitIntent(str2, str);
        }
        if (intent == null) {
            DSLog.e("Failed to find the target service.", new Object[0]);
            return false;
        }
        try {
            if (this.context.bindService(intent, this.connection, 1)) {
                return true;
            }
            DSLog.e("Failed to bind to the target service.", new Object[0]);
            return false;
        } catch (SecurityException e) {
            DSLog.e("Failed to bind service, error: %s.", e.getMessage());
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
