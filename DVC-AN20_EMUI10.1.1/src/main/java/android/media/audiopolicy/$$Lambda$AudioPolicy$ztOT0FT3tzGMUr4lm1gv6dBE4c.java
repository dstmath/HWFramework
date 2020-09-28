package android.media.audiopolicy;

import java.util.function.Predicate;

/* renamed from: android.media.audiopolicy.-$$Lambda$AudioPolicy$-ztOT0FT3tzGMUr4lm1gv6dBE4c  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AudioPolicy$ztOT0FT3tzGMUr4lm1gv6dBE4c implements Predicate {
    public static final /* synthetic */ $$Lambda$AudioPolicy$ztOT0FT3tzGMUr4lm1gv6dBE4c INSTANCE = new $$Lambda$AudioPolicy$ztOT0FT3tzGMUr4lm1gv6dBE4c();

    private /* synthetic */ $$Lambda$AudioPolicy$ztOT0FT3tzGMUr4lm1gv6dBE4c() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return AudioPolicy.lambda$isLoopbackRenderPolicy$0((AudioMix) obj);
    }
}
