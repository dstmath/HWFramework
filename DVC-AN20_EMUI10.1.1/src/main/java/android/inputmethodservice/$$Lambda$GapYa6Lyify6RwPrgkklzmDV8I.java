package android.inputmethodservice;

import android.inputmethodservice.MultiClientInputMethodClientCallbackAdaptor;
import com.android.internal.util.function.TriConsumer;

/* renamed from: android.inputmethodservice.-$$Lambda$GapYa6Lyify6RwP-rgkklzmDV8I  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GapYa6Lyify6RwPrgkklzmDV8I implements TriConsumer {
    public static final /* synthetic */ $$Lambda$GapYa6Lyify6RwPrgkklzmDV8I INSTANCE = new $$Lambda$GapYa6Lyify6RwPrgkklzmDV8I();

    private /* synthetic */ $$Lambda$GapYa6Lyify6RwPrgkklzmDV8I() {
    }

    @Override // com.android.internal.util.function.TriConsumer
    public final void accept(Object obj, Object obj2, Object obj3) {
        ((MultiClientInputMethodClientCallbackAdaptor.CallbackImpl) obj).toggleSoftInput(((Integer) obj2).intValue(), ((Integer) obj3).intValue());
    }
}
