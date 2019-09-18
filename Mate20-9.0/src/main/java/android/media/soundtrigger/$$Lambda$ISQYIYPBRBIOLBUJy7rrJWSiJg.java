package android.media.soundtrigger;

import android.hardware.soundtrigger.SoundTrigger;
import android.os.Bundle;
import com.android.internal.util.function.QuintConsumer;
import java.util.UUID;

/* renamed from: android.media.soundtrigger.-$$Lambda$ISQYIYPBRBIOLBUJy7rrJW-SiJg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ISQYIYPBRBIOLBUJy7rrJWSiJg implements QuintConsumer {
    public static final /* synthetic */ $$Lambda$ISQYIYPBRBIOLBUJy7rrJWSiJg INSTANCE = new $$Lambda$ISQYIYPBRBIOLBUJy7rrJWSiJg();

    private /* synthetic */ $$Lambda$ISQYIYPBRBIOLBUJy7rrJWSiJg() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
        ((SoundTriggerDetectionService) obj).onGenericRecognitionEvent((UUID) obj2, (Bundle) obj3, ((Integer) obj4).intValue(), (SoundTrigger.GenericRecognitionEvent) obj5);
    }
}
