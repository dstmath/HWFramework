package android.inputmethodservice;

import android.inputmethodservice.MultiClientInputMethodClientCallbackAdaptor;
import android.view.inputmethod.CursorAnchorInfo;
import java.util.function.BiConsumer;

/* renamed from: android.inputmethodservice.-$$Lambda$BAvs3tw1MzE4gOJqYOA5MCJasPE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BAvs3tw1MzE4gOJqYOA5MCJasPE implements BiConsumer {
    public static final /* synthetic */ $$Lambda$BAvs3tw1MzE4gOJqYOA5MCJasPE INSTANCE = new $$Lambda$BAvs3tw1MzE4gOJqYOA5MCJasPE();

    private /* synthetic */ $$Lambda$BAvs3tw1MzE4gOJqYOA5MCJasPE() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((MultiClientInputMethodClientCallbackAdaptor.CallbackImpl) obj).updateCursorAnchorInfo((CursorAnchorInfo) obj2);
    }
}
