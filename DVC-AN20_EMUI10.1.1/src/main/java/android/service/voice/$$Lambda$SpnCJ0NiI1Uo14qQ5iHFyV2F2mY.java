package android.service.voice;

import java.util.function.Consumer;

/* renamed from: android.service.voice.-$$Lambda$SpnCJ0NiI1Uo14qQ5iHFyV2F2mY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$SpnCJ0NiI1Uo14qQ5iHFyV2F2mY implements Consumer {
    public static final /* synthetic */ $$Lambda$SpnCJ0NiI1Uo14qQ5iHFyV2F2mY INSTANCE = new $$Lambda$SpnCJ0NiI1Uo14qQ5iHFyV2F2mY();

    private /* synthetic */ $$Lambda$SpnCJ0NiI1Uo14qQ5iHFyV2F2mY() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((VoiceInteractionService) obj).onReady();
    }
}
