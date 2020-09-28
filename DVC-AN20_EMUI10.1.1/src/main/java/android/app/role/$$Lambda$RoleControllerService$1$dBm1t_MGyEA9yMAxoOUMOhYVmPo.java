package android.app.role;

import android.os.RemoteCallback;
import com.android.internal.util.function.QuadConsumer;

/* renamed from: android.app.role.-$$Lambda$RoleControllerService$1$dBm1t_MGyEA9yMAxoOUMOhYVmPo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RoleControllerService$1$dBm1t_MGyEA9yMAxoOUMOhYVmPo implements QuadConsumer {
    public static final /* synthetic */ $$Lambda$RoleControllerService$1$dBm1t_MGyEA9yMAxoOUMOhYVmPo INSTANCE = new $$Lambda$RoleControllerService$1$dBm1t_MGyEA9yMAxoOUMOhYVmPo();

    private /* synthetic */ $$Lambda$RoleControllerService$1$dBm1t_MGyEA9yMAxoOUMOhYVmPo() {
    }

    @Override // com.android.internal.util.function.QuadConsumer
    public final void accept(Object obj, Object obj2, Object obj3, Object obj4) {
        ((RoleControllerService) obj).onClearRoleHolders((String) obj2, ((Integer) obj3).intValue(), (RemoteCallback) obj4);
    }
}
