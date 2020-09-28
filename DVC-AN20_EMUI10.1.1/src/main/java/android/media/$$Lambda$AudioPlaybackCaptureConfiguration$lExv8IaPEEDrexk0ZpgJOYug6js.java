package android.media;

import android.media.audiopolicy.AudioMixingRule;
import java.util.function.ToIntFunction;

/* renamed from: android.media.-$$Lambda$AudioPlaybackCaptureConfiguration$lExv8IaPEEDrexk0ZpgJOYug6js  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AudioPlaybackCaptureConfiguration$lExv8IaPEEDrexk0ZpgJOYug6js implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$AudioPlaybackCaptureConfiguration$lExv8IaPEEDrexk0ZpgJOYug6js INSTANCE = new $$Lambda$AudioPlaybackCaptureConfiguration$lExv8IaPEEDrexk0ZpgJOYug6js();

    private /* synthetic */ $$Lambda$AudioPlaybackCaptureConfiguration$lExv8IaPEEDrexk0ZpgJOYug6js() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((AudioMixingRule.AudioMixMatchCriterion) obj).getIntProp();
    }
}
