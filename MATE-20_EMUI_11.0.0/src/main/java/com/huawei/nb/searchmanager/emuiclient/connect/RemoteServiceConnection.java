package com.huawei.nb.searchmanager.emuiclient.connect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.util.Log;
import com.huawei.sidetouch.TpCommandConstant;

public class RemoteServiceConnection {
    private static final String COMMAND_TYPE_EMUI = "EMUI";
    private static final String TAG = "RemoteServiceConnection";
    private IBinder binder;
    private OnConnectListener connectListener;
    private ServiceConnection connection = new ServiceConnection() {
        /* class com.huawei.nb.searchmanager.emuiclient.connect.RemoteServiceConnection.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            RemoteServiceConnection.this.binder = iBinder;
            if (RemoteServiceConnection.this.connectListener != null) {
                RemoteServiceConnection.this.connectListener.onConnect(RemoteServiceConnection.this.binder);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName className) {
            if (RemoteServiceConnection.this.binder != null) {
                RemoteServiceConnection.this.binder = null;
                if (RemoteServiceConnection.this.connectListener != null) {
                    RemoteServiceConnection.this.connectListener.onDisconnect();
                }
                Log.e(RemoteServiceConnection.TAG, "Connection to data service is disconnected unexpectedly.");
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

    public RemoteServiceConnection(Context base, String servicePackageName2, String serviceClassName2) {
        this.context = base;
        this.serviceAction = null;
        this.servicePackageName = servicePackageName2;
        this.serviceClassName = serviceClassName2;
        this.binder = null;
        Log.i(TAG, TAG);
    }

    public RemoteServiceConnection(Context base, String serviceAction2) {
        this.context = base;
        this.serviceAction = serviceAction2;
        this.servicePackageName = null;
        this.serviceClassName = null;
        this.binder = null;
        Log.i(TAG, TAG);
    }

    public boolean open(OnConnectListener listener) {
        Intent remoteServiceIntent;
        String str;
        this.connectListener = listener;
        String str2 = this.servicePackageName;
        if (str2 == null || (str = this.serviceClassName) == null) {
            remoteServiceIntent = createImplicitIntent(this.serviceAction);
        } else {
            remoteServiceIntent = createExplicitIntent(str2, str);
        }
        if (remoteServiceIntent == null) {
            Log.e(TAG, "Failed to find the given data service action.");
            return false;
        }
        remoteServiceIntent.setType(COMMAND_TYPE_EMUI);
        try {
            if (this.context.bindService(remoteServiceIntent, this.connection, 1)) {
                return true;
            }
            Log.e(TAG, "Failed to connect to data service.");
            return false;
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to do bind service, error: %s.");
            return false;
        }
    }

    public boolean close() {
        this.context.unbindService(this.connection);
        this.binder = null;
        this.connectListener = null;
        return true;
    }

    private Intent createImplicitIntent(String action) {
        Intent intent;
        ResolveInfo info;
        PackageManager pm = this.context.getPackageManager();
        if (pm == null || (info = pm.resolveService((intent = new Intent(action)), TpCommandConstant.TSA_EVENT_RIGHT_VOLUME_TOUCH)) == null) {
            return null;
        }
        intent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));
        return intent;
    }

    private Intent createExplicitIntent(String packageName, String className) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, className));
        return intent;
    }
}
