package android.media.soundtrigger;

import android.os.Bundle;
import com.android.internal.util.function.QuadConsumer;
import java.util.UUID;

/* renamed from: android.media.soundtrigger.-$$Lambda$SoundTriggerDetectionService$1$LlOo7TiZplZCgGhS07DqYHocFcw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$SoundTriggerDetectionService$1$LlOo7TiZplZCgGhS07DqYHocFcw implements QuadConsumer {
    public static final /* synthetic */ $$Lambda$SoundTriggerDetectionService$1$LlOo7TiZplZCgGhS07DqYHocFcw INSTANCE = new $$Lambda$SoundTriggerDetectionService$1$LlOo7TiZplZCgGhS07DqYHocFcw();

    private /* synthetic */ $$Lambda$SoundTriggerDetectionService$1$LlOo7TiZplZCgGhS07DqYHocFcw() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4) {
        ((SoundTriggerDetectionService) ((SoundTriggerDetectionService) obj)).setClient((UUID) obj2, (Bundle) obj3, (ISoundTriggerDetectionServiceClient) obj4);
    }
}
