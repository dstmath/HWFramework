package android.app.role;

import android.os.RemoteCallback;
import java.util.function.BiConsumer;

/* renamed from: android.app.role.-$$Lambda$RoleControllerService$1$-fmj7uDKaG3BoLl6bhtrA675gRI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RoleControllerService$1$fmj7uDKaG3BoLl6bhtrA675gRI implements BiConsumer {
    public static final /* synthetic */ $$Lambda$RoleControllerService$1$fmj7uDKaG3BoLl6bhtrA675gRI INSTANCE = new $$Lambda$RoleControllerService$1$fmj7uDKaG3BoLl6bhtrA675gRI();

    private /* synthetic */ $$Lambda$RoleControllerService$1$fmj7uDKaG3BoLl6bhtrA675gRI() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((RoleControllerService) obj).grantDefaultRoles((RemoteCallback) obj2);
    }
}
