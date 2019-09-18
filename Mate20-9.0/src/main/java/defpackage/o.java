package defpackage;

import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.feature.install.RemoteRequest;
import com.huawei.android.feature.install.RemoteRequestStrategy;
import com.huawei.android.feature.tasks.TaskHolder;
import java.util.List;

/* renamed from: o  reason: default package */
public final class o extends RemoteRequest {
    final /* synthetic */ TaskHolder p;
    final /* synthetic */ RemoteRequestStrategy q;
    final /* synthetic */ List s;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public o(RemoteRequestStrategy remoteRequestStrategy, TaskHolder taskHolder, List list, TaskHolder taskHolder2) {
        super(taskHolder);
        this.q = remoteRequestStrategy;
        this.s = list;
        this.p = taskHolder2;
    }

    /* access modifiers changed from: protected */
    public final void excute() {
        try {
            this.q.mRemoteConnector.getRemoteProxy().deferredInstall(this.q.mPackageName, this.q.createVersionModuleNameBundle(this.s), this.q.createExtraInfoBundle(), this.q.createDeferredInstallCallback(this.q.mRemoteConnector, this.p));
        } catch (RemoteException e) {
            Log.e(RemoteRequestStrategy.TAG, e.getMessage());
        }
    }
}
