package android.service.voice;

import java.util.function.Consumer;

/* renamed from: android.service.voice.-$$Lambda$VoiceInteractionService$1$WnZueQJxACwCZWfYsmNtGrcNbEc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$VoiceInteractionService$1$WnZueQJxACwCZWfYsmNtGrcNbEc implements Consumer {
    public static final /* synthetic */ $$Lambda$VoiceInteractionService$1$WnZueQJxACwCZWfYsmNtGrcNbEc INSTANCE = new $$Lambda$VoiceInteractionService$1$WnZueQJxACwCZWfYsmNtGrcNbEc();

    private /* synthetic */ $$Lambda$VoiceInteractionService$1$WnZueQJxACwCZWfYsmNtGrcNbEc() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((VoiceInteractionService) obj).onSoundModelsChangedInternal();
    }
}
