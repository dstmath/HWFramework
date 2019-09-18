package defpackage;

import android.content.ServiceConnection;
import android.util.Log;
import com.huawei.android.feature.IDynamicInstall;
import com.huawei.android.feature.install.RemoteServiceConnector;

/* renamed from: v  reason: default package */
public final class v implements Runnable {
    final /* synthetic */ RemoteServiceConnector u;

    public v(RemoteServiceConnector remoteServiceConnector) {
        this.u = remoteServiceConnector;
    }

    public final void run() {
        if (this.u.mRemoteProxy != null) {
            Log.d(RemoteServiceConnector.TAG, "unbindService");
            this.u.mRemoteProxy.asBinder().unlinkToDeath(RemoteServiceConnector.mDeathRecipient, 0);
            this.u.mContext.unbindService(this.u.mServiceConnection);
            boolean unused = this.u.mIsWaitingConnect = false;
            IDynamicInstall unused2 = this.u.mRemoteProxy = null;
            ServiceConnection unused3 = this.u.mServiceConnection = null;
        }
    }
}
