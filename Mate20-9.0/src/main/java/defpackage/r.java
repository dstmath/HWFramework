package defpackage;

import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.feature.install.RemoteRequest;
import com.huawei.android.feature.install.RemoteRequestStrategy;
import com.huawei.android.feature.tasks.TaskHolder;

/* renamed from: r  reason: default package */
public final class r extends RemoteRequest {
    final /* synthetic */ TaskHolder p;
    final /* synthetic */ RemoteRequestStrategy q;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public r(RemoteRequestStrategy remoteRequestStrategy, TaskHolder taskHolder, TaskHolder taskHolder2) {
        super(taskHolder);
        this.q = remoteRequestStrategy;
        this.p = taskHolder2;
    }

    /* access modifiers changed from: protected */
    public final void excute() {
        try {
            this.q.mRemoteConnector.getRemoteProxy().getSessionStates(this.q.mPackageName, this.q.createGetSessionStatesCallback(this.q.mRemoteConnector, this.p));
        } catch (RemoteException e) {
            Log.e(RemoteRequestStrategy.TAG, e.getMessage());
        }
    }
}
