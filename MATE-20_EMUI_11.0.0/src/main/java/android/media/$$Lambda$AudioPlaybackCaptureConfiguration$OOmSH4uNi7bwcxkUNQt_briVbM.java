package android.media;

import android.media.audiopolicy.AudioMixingRule;
import java.util.function.ToIntFunction;

/* renamed from: android.media.-$$Lambda$AudioPlaybackCaptureConfiguration$OOmSH4uNi7bw-cxkUNQt_briVbM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AudioPlaybackCaptureConfiguration$OOmSH4uNi7bwcxkUNQt_briVbM implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$AudioPlaybackCaptureConfiguration$OOmSH4uNi7bwcxkUNQt_briVbM INSTANCE = new $$Lambda$AudioPlaybackCaptureConfiguration$OOmSH4uNi7bwcxkUNQt_briVbM();

    private /* synthetic */ $$Lambda$AudioPlaybackCaptureConfiguration$OOmSH4uNi7bwcxkUNQt_briVbM() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((AudioMixingRule.AudioMixMatchCriterion) obj).getIntProp();
    }
}
