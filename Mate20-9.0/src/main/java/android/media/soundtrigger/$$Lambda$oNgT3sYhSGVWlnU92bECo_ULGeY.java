package android.media.soundtrigger;

import android.os.Bundle;
import com.android.internal.util.function.QuintConsumer;
import java.util.UUID;

/* renamed from: android.media.soundtrigger.-$$Lambda$oNgT3sYhSGVWlnU92bECo_ULGeY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$oNgT3sYhSGVWlnU92bECo_ULGeY implements QuintConsumer {
    public static final /* synthetic */ $$Lambda$oNgT3sYhSGVWlnU92bECo_ULGeY INSTANCE = new $$Lambda$oNgT3sYhSGVWlnU92bECo_ULGeY();

    private /* synthetic */ $$Lambda$oNgT3sYhSGVWlnU92bECo_ULGeY() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
        ((SoundTriggerDetectionService) obj).onError((UUID) obj2, (Bundle) obj3, ((Integer) obj4).intValue(), ((Integer) obj5).intValue());
    }
}
