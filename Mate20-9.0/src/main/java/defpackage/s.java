package defpackage;

import com.huawei.android.feature.install.RemoteRequest;
import com.huawei.android.feature.install.RemoteServiceConnector;

/* renamed from: s  reason: default package */
public final class s implements Runnable {
    final /* synthetic */ RemoteRequest t;
    final /* synthetic */ RemoteServiceConnector u;

    public s(RemoteServiceConnector remoteServiceConnector, RemoteRequest remoteRequest) {
        this.u = remoteServiceConnector;
        this.t = remoteRequest;
    }

    public final void run() {
        this.u.doCommand(this.t);
    }
}
