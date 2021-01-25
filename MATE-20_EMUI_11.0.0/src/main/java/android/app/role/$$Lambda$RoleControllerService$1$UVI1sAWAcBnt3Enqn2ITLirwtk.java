package android.app.role;

import android.os.RemoteCallback;
import com.android.internal.util.function.QuintConsumer;

/* renamed from: android.app.role.-$$Lambda$RoleControllerService$1$UVI1sAWAcBnt3Enqn2IT-Lirwtk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RoleControllerService$1$UVI1sAWAcBnt3Enqn2ITLirwtk implements QuintConsumer {
    public static final /* synthetic */ $$Lambda$RoleControllerService$1$UVI1sAWAcBnt3Enqn2ITLirwtk INSTANCE = new $$Lambda$RoleControllerService$1$UVI1sAWAcBnt3Enqn2ITLirwtk();

    private /* synthetic */ $$Lambda$RoleControllerService$1$UVI1sAWAcBnt3Enqn2ITLirwtk() {
    }

    @Override // com.android.internal.util.function.QuintConsumer
    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
        ((RoleControllerService) obj).onAddRoleHolder((String) obj2, (String) obj3, ((Integer) obj4).intValue(), (RemoteCallback) obj5);
    }
}
