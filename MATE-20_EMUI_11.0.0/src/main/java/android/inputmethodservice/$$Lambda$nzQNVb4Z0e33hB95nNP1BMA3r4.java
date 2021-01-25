package android.inputmethodservice;

import android.inputmethodservice.MultiClientInputMethodClientCallbackAdaptor;
import android.os.Bundle;
import com.android.internal.util.function.TriConsumer;

/* renamed from: android.inputmethodservice.-$$Lambda$nzQNVb4Z0e33hB95nNP1BM-A3r4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$nzQNVb4Z0e33hB95nNP1BMA3r4 implements TriConsumer {
    public static final /* synthetic */ $$Lambda$nzQNVb4Z0e33hB95nNP1BMA3r4 INSTANCE = new $$Lambda$nzQNVb4Z0e33hB95nNP1BMA3r4();

    private /* synthetic */ $$Lambda$nzQNVb4Z0e33hB95nNP1BMA3r4() {
    }

    @Override // com.android.internal.util.function.TriConsumer
    public final void accept(Object obj, Object obj2, Object obj3) {
        ((MultiClientInputMethodClientCallbackAdaptor.CallbackImpl) obj).appPrivateCommand((String) obj2, (Bundle) obj3);
    }
}
