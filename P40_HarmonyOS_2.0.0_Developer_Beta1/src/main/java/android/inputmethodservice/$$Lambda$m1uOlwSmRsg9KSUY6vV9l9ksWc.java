package android.inputmethodservice;

import android.inputmethodservice.MultiClientInputMethodClientCallbackAdaptor;
import android.os.ResultReceiver;
import com.android.internal.util.function.TriConsumer;

/* renamed from: android.inputmethodservice.-$$Lambda$m1uOlwS-mRsg9KSUY6vV9l9ksWc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$m1uOlwSmRsg9KSUY6vV9l9ksWc implements TriConsumer {
    public static final /* synthetic */ $$Lambda$m1uOlwSmRsg9KSUY6vV9l9ksWc INSTANCE = new $$Lambda$m1uOlwSmRsg9KSUY6vV9l9ksWc();

    private /* synthetic */ $$Lambda$m1uOlwSmRsg9KSUY6vV9l9ksWc() {
    }

    @Override // com.android.internal.util.function.TriConsumer
    public final void accept(Object obj, Object obj2, Object obj3) {
        ((MultiClientInputMethodClientCallbackAdaptor.CallbackImpl) obj).showSoftInput(((Integer) obj2).intValue(), (ResultReceiver) obj3);
    }
}
