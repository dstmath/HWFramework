package defpackage;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import com.huawei.android.feature.install.RemoteServiceConnector;

/* renamed from: t  reason: default package */
public final class t implements ServiceConnection {
    final /* synthetic */ RemoteServiceConnector u;

    public t(RemoteServiceConnector remoteServiceConnector) {
        this.u = remoteServiceConnector;
    }

    public final void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d(RemoteServiceConnector.TAG, "onServiceConnect");
        this.u.handleServiceConnected(iBinder);
    }

    public final void onServiceDisconnected(ComponentName componentName) {
        Log.d(RemoteServiceConnector.TAG, "onServiceDisconnected");
        this.u.handleServiceDisconnected();
    }
}
