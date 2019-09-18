package android.telecom.Logging;

import android.telecom.Logging.EventManager;
import android.util.Pair;
import java.util.function.ToLongFunction;

/* renamed from: android.telecom.Logging.-$$Lambda$EventManager$weOtitr8e1cZeiy1aDSqzNoKaY8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EventManager$weOtitr8e1cZeiy1aDSqzNoKaY8 implements ToLongFunction {
    public static final /* synthetic */ $$Lambda$EventManager$weOtitr8e1cZeiy1aDSqzNoKaY8 INSTANCE = new $$Lambda$EventManager$weOtitr8e1cZeiy1aDSqzNoKaY8();

    private /* synthetic */ $$Lambda$EventManager$weOtitr8e1cZeiy1aDSqzNoKaY8() {
    }

    public final long applyAsLong(Object obj) {
        return ((EventManager.Event) ((Pair) obj).second).time;
    }
}
