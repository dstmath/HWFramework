package android.service.voice;

import com.android.internal.app.IVoiceActionCheckCallback;
import com.android.internal.util.function.TriConsumer;
import java.util.List;

/* renamed from: android.service.voice.-$$Lambda$VoiceInteractionService$1$gKwKkiuvnPnBCMXtKcZDpBR3098  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$VoiceInteractionService$1$gKwKkiuvnPnBCMXtKcZDpBR3098 implements TriConsumer {
    public static final /* synthetic */ $$Lambda$VoiceInteractionService$1$gKwKkiuvnPnBCMXtKcZDpBR3098 INSTANCE = new $$Lambda$VoiceInteractionService$1$gKwKkiuvnPnBCMXtKcZDpBR3098();

    private /* synthetic */ $$Lambda$VoiceInteractionService$1$gKwKkiuvnPnBCMXtKcZDpBR3098() {
    }

    @Override // com.android.internal.util.function.TriConsumer
    public final void accept(Object obj, Object obj2, Object obj3) {
        ((VoiceInteractionService) obj).onHandleVoiceActionCheck((List) obj2, (IVoiceActionCheckCallback) obj3);
    }
}
