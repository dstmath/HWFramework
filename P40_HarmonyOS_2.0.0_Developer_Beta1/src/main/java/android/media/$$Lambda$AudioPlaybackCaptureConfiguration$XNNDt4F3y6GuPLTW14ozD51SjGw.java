package android.media;

import android.media.audiopolicy.AudioMixingRule;
import java.util.function.ToIntFunction;

/* renamed from: android.media.-$$Lambda$AudioPlaybackCaptureConfiguration$XNNDt4F3y6GuPLTW14ozD51SjGw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AudioPlaybackCaptureConfiguration$XNNDt4F3y6GuPLTW14ozD51SjGw implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$AudioPlaybackCaptureConfiguration$XNNDt4F3y6GuPLTW14ozD51SjGw INSTANCE = new $$Lambda$AudioPlaybackCaptureConfiguration$XNNDt4F3y6GuPLTW14ozD51SjGw();

    private /* synthetic */ $$Lambda$AudioPlaybackCaptureConfiguration$XNNDt4F3y6GuPLTW14ozD51SjGw() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((AudioMixingRule.AudioMixMatchCriterion) obj).getAudioAttributes().getUsage();
    }
}
