package defpackage;

import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.feature.install.RemoteRequest;
import com.huawei.android.feature.install.RemoteRequestStrategy;
import com.huawei.android.feature.tasks.TaskHolder;

/* renamed from: n  reason: default package */
public final class n extends RemoteRequest {
    final /* synthetic */ TaskHolder p;
    final /* synthetic */ RemoteRequestStrategy q;
    final /* synthetic */ int r;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public n(RemoteRequestStrategy remoteRequestStrategy, TaskHolder taskHolder, int i, TaskHolder taskHolder2) {
        super(taskHolder);
        this.q = remoteRequestStrategy;
        this.r = i;
        this.p = taskHolder2;
    }

    /* access modifiers changed from: protected */
    public final void excute() {
        try {
            this.q.mRemoteConnector.getRemoteProxy().cancelInstall(this.q.mPackageName, this.r, this.q.createExtraInfoBundle(), this.q.createCancelInstallCallback(this.q.mRemoteConnector, this.p));
        } catch (RemoteException e) {
            Log.e(RemoteRequestStrategy.TAG, e.getMessage());
        }
    }
}
