package android.app.role;

import android.os.RemoteCallback;
import com.android.internal.util.function.QuintConsumer;

/* renamed from: android.app.role.-$$Lambda$RoleControllerService$1$PB6H1df6VvLzUJ3hhB_75mN3u7s  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RoleControllerService$1$PB6H1df6VvLzUJ3hhB_75mN3u7s implements QuintConsumer {
    public static final /* synthetic */ $$Lambda$RoleControllerService$1$PB6H1df6VvLzUJ3hhB_75mN3u7s INSTANCE = new $$Lambda$RoleControllerService$1$PB6H1df6VvLzUJ3hhB_75mN3u7s();

    private /* synthetic */ $$Lambda$RoleControllerService$1$PB6H1df6VvLzUJ3hhB_75mN3u7s() {
    }

    @Override // com.android.internal.util.function.QuintConsumer
    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
        ((RoleControllerService) obj).onRemoveRoleHolder((String) obj2, (String) obj3, ((Integer) obj4).intValue(), (RemoteCallback) obj5);
    }
}
