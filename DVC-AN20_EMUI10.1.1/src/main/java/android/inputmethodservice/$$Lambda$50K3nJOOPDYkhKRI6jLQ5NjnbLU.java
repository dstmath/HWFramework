package android.inputmethodservice;

import android.inputmethodservice.MultiClientInputMethodClientCallbackAdaptor;
import java.util.function.Consumer;

/* renamed from: android.inputmethodservice.-$$Lambda$50K3nJOOPDYkhKRI6jLQ5NjnbLU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$50K3nJOOPDYkhKRI6jLQ5NjnbLU implements Consumer {
    public static final /* synthetic */ $$Lambda$50K3nJOOPDYkhKRI6jLQ5NjnbLU INSTANCE = new $$Lambda$50K3nJOOPDYkhKRI6jLQ5NjnbLU();

    private /* synthetic */ $$Lambda$50K3nJOOPDYkhKRI6jLQ5NjnbLU() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((MultiClientInputMethodClientCallbackAdaptor.CallbackImpl) obj).finishSession();
    }
}
