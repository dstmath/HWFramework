package com.android.server.broadcastradio.hal2;

import android.hardware.radio.ITunerCallback;
import com.android.server.broadcastradio.hal2.RadioModule;

/* renamed from: com.android.server.broadcastradio.hal2.-$$Lambda$TunerSession$RN6YGky4fEzp_y9hG2yxYfo0XPs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TunerSession$RN6YGky4fEzp_y9hG2yxYfo0XPs implements RadioModule.AidlCallbackRunnable {
    public static final /* synthetic */ $$Lambda$TunerSession$RN6YGky4fEzp_y9hG2yxYfo0XPs INSTANCE = new $$Lambda$TunerSession$RN6YGky4fEzp_y9hG2yxYfo0XPs();

    private /* synthetic */ $$Lambda$TunerSession$RN6YGky4fEzp_y9hG2yxYfo0XPs() {
    }

    @Override // com.android.server.broadcastradio.hal2.RadioModule.AidlCallbackRunnable
    public final void run(ITunerCallback iTunerCallback) {
        iTunerCallback.onBackgroundScanComplete();
    }
}
