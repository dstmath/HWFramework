package android.app;

import android.os.CancellationSignal;
import android.os.IBinder;
import android.os.RemoteCallback;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.util.function.QuintConsumer;

/* renamed from: android.app.-$$Lambda$ActivityThread$ApplicationThread$uR_ee-5oPoxu4U_by7wU55jwtdU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ActivityThread$ApplicationThread$uR_ee5oPoxu4U_by7wU55jwtdU implements QuintConsumer {
    public static final /* synthetic */ $$Lambda$ActivityThread$ApplicationThread$uR_ee5oPoxu4U_by7wU55jwtdU INSTANCE = new $$Lambda$ActivityThread$ApplicationThread$uR_ee5oPoxu4U_by7wU55jwtdU();

    private /* synthetic */ $$Lambda$ActivityThread$ApplicationThread$uR_ee5oPoxu4U_by7wU55jwtdU() {
    }

    @Override // com.android.internal.util.function.QuintConsumer
    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
        ((ActivityThread) obj).handleRequestDirectActions((IBinder) obj2, (IVoiceInteractor) obj3, (CancellationSignal) obj4, (RemoteCallback) obj5);
    }
}
