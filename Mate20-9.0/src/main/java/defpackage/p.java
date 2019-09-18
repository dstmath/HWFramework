package defpackage;

import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.feature.install.RemoteRequest;
import com.huawei.android.feature.install.RemoteRequestStrategy;
import com.huawei.android.feature.tasks.TaskHolder;
import java.util.List;

/* renamed from: p  reason: default package */
public final class p extends RemoteRequest {
    final /* synthetic */ TaskHolder p;
    final /* synthetic */ RemoteRequestStrategy q;
    final /* synthetic */ List s;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public p(RemoteRequestStrategy remoteRequestStrategy, TaskHolder taskHolder, List list, TaskHolder taskHolder2) {
        super(taskHolder);
        this.q = remoteRequestStrategy;
        this.s = list;
        this.p = taskHolder2;
    }

    /* access modifiers changed from: protected */
    public final void excute() {
        try {
            this.q.mRemoteConnector.getRemoteProxy().deferredUninstall(this.q.mPackageName, this.q.createModuleNameBundle(this.s), this.q.createExtraInfoBundle(), this.q.createDeferredUnInsallCallback(this.q.mRemoteConnector, this.p));
        } catch (RemoteException e) {
            Log.e(RemoteRequestStrategy.TAG, e.getMessage());
        }
    }
}
