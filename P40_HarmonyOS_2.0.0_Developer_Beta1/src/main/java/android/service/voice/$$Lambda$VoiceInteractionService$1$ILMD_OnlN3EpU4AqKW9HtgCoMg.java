package android.service.voice;

import java.util.function.Consumer;

/* renamed from: android.service.voice.-$$Lambda$VoiceInteractionService$1$ILMD_OnlN3EpU4AqKW9H-tgCoMg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$VoiceInteractionService$1$ILMD_OnlN3EpU4AqKW9HtgCoMg implements Consumer {
    public static final /* synthetic */ $$Lambda$VoiceInteractionService$1$ILMD_OnlN3EpU4AqKW9HtgCoMg INSTANCE = new $$Lambda$VoiceInteractionService$1$ILMD_OnlN3EpU4AqKW9HtgCoMg();

    private /* synthetic */ $$Lambda$VoiceInteractionService$1$ILMD_OnlN3EpU4AqKW9HtgCoMg() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((VoiceInteractionService) obj).onShutdownInternal();
    }
}
