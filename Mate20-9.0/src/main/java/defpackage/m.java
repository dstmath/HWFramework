package defpackage;

import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.feature.install.RemoteRequest;
import com.huawei.android.feature.install.RemoteRequestStrategy;
import com.huawei.android.feature.tasks.TaskHolder;
import java.util.Collection;

/* renamed from: m  reason: default package */
public final class m extends RemoteRequest {
    final /* synthetic */ Collection o;
    final /* synthetic */ TaskHolder p;
    final /* synthetic */ RemoteRequestStrategy q;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public m(RemoteRequestStrategy remoteRequestStrategy, TaskHolder taskHolder, Collection collection, TaskHolder taskHolder2) {
        super(taskHolder);
        this.q = remoteRequestStrategy;
        this.o = collection;
        this.p = taskHolder2;
    }

    /* access modifiers changed from: protected */
    public final void excute() {
        try {
            this.q.mRemoteConnector.getRemoteProxy().startInstall(this.q.mPackageName, this.q.createVersionModuleNameBundle(this.o), this.q.createExtraInfoBundle(), this.q.createStartInstallCallback(this.q.mRemoteConnector, this.p));
        } catch (RemoteException e) {
            Log.e(RemoteRequestStrategy.TAG, e.getMessage());
            this.p.notifyException(new RuntimeException("start installFeatureFromUnverifyIfNeed error"));
        }
    }
}
