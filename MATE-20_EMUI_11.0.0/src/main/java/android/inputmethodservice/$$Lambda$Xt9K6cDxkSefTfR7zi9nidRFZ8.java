package android.inputmethodservice;

import android.inputmethodservice.MultiClientInputMethodClientCallbackAdaptor;
import com.android.internal.os.SomeArgs;
import java.util.function.BiConsumer;

/* renamed from: android.inputmethodservice.-$$Lambda$Xt9K6cDxkSefTfR7zi9ni-dRFZ8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Xt9K6cDxkSefTfR7zi9nidRFZ8 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$Xt9K6cDxkSefTfR7zi9nidRFZ8 INSTANCE = new $$Lambda$Xt9K6cDxkSefTfR7zi9nidRFZ8();

    private /* synthetic */ $$Lambda$Xt9K6cDxkSefTfR7zi9nidRFZ8() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((MultiClientInputMethodClientCallbackAdaptor.CallbackImpl) obj).startInputOrWindowGainedFocus((SomeArgs) obj2);
    }
}
