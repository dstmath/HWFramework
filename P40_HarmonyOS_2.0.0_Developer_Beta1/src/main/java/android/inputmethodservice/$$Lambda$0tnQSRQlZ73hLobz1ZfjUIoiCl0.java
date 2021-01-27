package android.inputmethodservice;

import android.inputmethodservice.MultiClientInputMethodClientCallbackAdaptor;
import android.os.ResultReceiver;
import com.android.internal.util.function.TriConsumer;

/* renamed from: android.inputmethodservice.-$$Lambda$0tnQSRQlZ73hLobz1ZfjUIoiCl0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$0tnQSRQlZ73hLobz1ZfjUIoiCl0 implements TriConsumer {
    public static final /* synthetic */ $$Lambda$0tnQSRQlZ73hLobz1ZfjUIoiCl0 INSTANCE = new $$Lambda$0tnQSRQlZ73hLobz1ZfjUIoiCl0();

    private /* synthetic */ $$Lambda$0tnQSRQlZ73hLobz1ZfjUIoiCl0() {
    }

    @Override // com.android.internal.util.function.TriConsumer
    public final void accept(Object obj, Object obj2, Object obj3) {
        ((MultiClientInputMethodClientCallbackAdaptor.CallbackImpl) obj).hideSoftInput(((Integer) obj2).intValue(), (ResultReceiver) obj3);
    }
}
