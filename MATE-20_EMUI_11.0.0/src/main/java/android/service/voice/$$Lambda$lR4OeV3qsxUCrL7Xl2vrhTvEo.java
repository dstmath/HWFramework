package android.service.voice;

import android.service.voice.VoiceInteractionSession;
import java.util.function.BiConsumer;

/* renamed from: android.service.voice.-$$Lambda$lR4OeV3qsxUC-rL-7Xl2vrhTvEo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$lR4OeV3qsxUCrL7Xl2vrhTvEo implements BiConsumer {
    public static final /* synthetic */ $$Lambda$lR4OeV3qsxUCrL7Xl2vrhTvEo INSTANCE = new $$Lambda$lR4OeV3qsxUCrL7Xl2vrhTvEo();

    private /* synthetic */ $$Lambda$lR4OeV3qsxUCrL7Xl2vrhTvEo() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((VoiceInteractionSession) obj).onDirectActionsInvalidated((VoiceInteractionSession.ActivityId) obj2);
    }
}
