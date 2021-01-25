package android.media;

import android.media.audiopolicy.AudioMixingRule;
import java.util.function.ToIntFunction;

/* renamed from: android.media.-$$Lambda$AudioPlaybackCaptureConfiguration$JTl6zvocylA2c1D_zvOeuHFBuXc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AudioPlaybackCaptureConfiguration$JTl6zvocylA2c1D_zvOeuHFBuXc implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$AudioPlaybackCaptureConfiguration$JTl6zvocylA2c1D_zvOeuHFBuXc INSTANCE = new $$Lambda$AudioPlaybackCaptureConfiguration$JTl6zvocylA2c1D_zvOeuHFBuXc();

    private /* synthetic */ $$Lambda$AudioPlaybackCaptureConfiguration$JTl6zvocylA2c1D_zvOeuHFBuXc() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((AudioMixingRule.AudioMixMatchCriterion) obj).getAudioAttributes().getUsage();
    }
}
