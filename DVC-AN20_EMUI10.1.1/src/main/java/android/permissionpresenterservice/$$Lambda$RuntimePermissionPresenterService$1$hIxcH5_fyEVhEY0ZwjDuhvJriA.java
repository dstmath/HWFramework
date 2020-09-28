package android.permissionpresenterservice;

import android.os.RemoteCallback;
import com.android.internal.util.function.TriConsumer;

/* renamed from: android.permissionpresenterservice.-$$Lambda$RuntimePermissionPresenterService$1$hIxcH5_fyEVhEY0Z-wjDuhvJriA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RuntimePermissionPresenterService$1$hIxcH5_fyEVhEY0ZwjDuhvJriA implements TriConsumer {
    public static final /* synthetic */ $$Lambda$RuntimePermissionPresenterService$1$hIxcH5_fyEVhEY0ZwjDuhvJriA INSTANCE = new $$Lambda$RuntimePermissionPresenterService$1$hIxcH5_fyEVhEY0ZwjDuhvJriA();

    private /* synthetic */ $$Lambda$RuntimePermissionPresenterService$1$hIxcH5_fyEVhEY0ZwjDuhvJriA() {
    }

    @Override // com.android.internal.util.function.TriConsumer
    public final void accept(Object obj, Object obj2, Object obj3) {
        ((RuntimePermissionPresenterService) obj).getAppPermissions((String) obj2, (RemoteCallback) obj3);
    }
}
