package android.inputmethodservice;

import android.inputmethodservice.MultiClientInputMethodClientCallbackAdaptor;
import com.android.internal.os.SomeArgs;
import java.util.function.BiConsumer;

/* renamed from: android.inputmethodservice.-$$Lambda$zVy_pAXuQfncxA_AL_8DWyVpYXc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$zVy_pAXuQfncxA_AL_8DWyVpYXc implements BiConsumer {
    public static final /* synthetic */ $$Lambda$zVy_pAXuQfncxA_AL_8DWyVpYXc INSTANCE = new $$Lambda$zVy_pAXuQfncxA_AL_8DWyVpYXc();

    private /* synthetic */ $$Lambda$zVy_pAXuQfncxA_AL_8DWyVpYXc() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((MultiClientInputMethodClientCallbackAdaptor.CallbackImpl) obj).updateSelection((SomeArgs) obj2);
    }
}
