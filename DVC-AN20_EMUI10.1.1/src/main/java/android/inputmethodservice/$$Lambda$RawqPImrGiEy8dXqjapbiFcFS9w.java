package android.inputmethodservice;

import android.inputmethodservice.MultiClientInputMethodClientCallbackAdaptor;
import android.view.inputmethod.CompletionInfo;
import java.util.function.BiConsumer;

/* renamed from: android.inputmethodservice.-$$Lambda$RawqPImrGiEy8dXqjapbiFcFS9w  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RawqPImrGiEy8dXqjapbiFcFS9w implements BiConsumer {
    public static final /* synthetic */ $$Lambda$RawqPImrGiEy8dXqjapbiFcFS9w INSTANCE = new $$Lambda$RawqPImrGiEy8dXqjapbiFcFS9w();

    private /* synthetic */ $$Lambda$RawqPImrGiEy8dXqjapbiFcFS9w() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((MultiClientInputMethodClientCallbackAdaptor.CallbackImpl) obj).displayCompletions((CompletionInfo[]) obj2);
    }
}
