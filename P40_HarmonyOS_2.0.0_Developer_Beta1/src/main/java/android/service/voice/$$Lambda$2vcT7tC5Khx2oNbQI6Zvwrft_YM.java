package android.service.voice;

import java.util.function.Consumer;

/* renamed from: android.service.voice.-$$Lambda$2vcT7tC5Khx2oNbQI6Zvwrft_YM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$2vcT7tC5Khx2oNbQI6Zvwrft_YM implements Consumer {
    public static final /* synthetic */ $$Lambda$2vcT7tC5Khx2oNbQI6Zvwrft_YM INSTANCE = new $$Lambda$2vcT7tC5Khx2oNbQI6Zvwrft_YM();

    private /* synthetic */ $$Lambda$2vcT7tC5Khx2oNbQI6Zvwrft_YM() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((VoiceInteractionService) obj).onLaunchVoiceAssistFromKeyguard();
    }
}
