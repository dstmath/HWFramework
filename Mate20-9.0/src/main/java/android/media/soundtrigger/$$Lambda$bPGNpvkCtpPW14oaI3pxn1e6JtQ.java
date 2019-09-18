package android.media.soundtrigger;

import android.os.Bundle;
import com.android.internal.util.function.QuadConsumer;
import java.util.UUID;

/* renamed from: android.media.soundtrigger.-$$Lambda$bPGNpvkCtpPW14oaI3pxn1e6JtQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$bPGNpvkCtpPW14oaI3pxn1e6JtQ implements QuadConsumer {
    public static final /* synthetic */ $$Lambda$bPGNpvkCtpPW14oaI3pxn1e6JtQ INSTANCE = new $$Lambda$bPGNpvkCtpPW14oaI3pxn1e6JtQ();

    private /* synthetic */ $$Lambda$bPGNpvkCtpPW14oaI3pxn1e6JtQ() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4) {
        ((SoundTriggerDetectionService) obj).onStopOperation((UUID) obj2, (Bundle) obj3, ((Integer) obj4).intValue());
    }
}
