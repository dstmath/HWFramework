package ohos.miscservices.screenlock;

import android.app.KeyguardManager;
import java.util.function.Consumer;

/* renamed from: ohos.miscservices.screenlock.-$$Lambda$KeyguardManagerAdapter$4CHaMPWU20wI3Bpm26WWYVVZUcI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$KeyguardManagerAdapter$4CHaMPWU20wI3Bpm26WWYVVZUcI implements Consumer {
    public static final /* synthetic */ $$Lambda$KeyguardManagerAdapter$4CHaMPWU20wI3Bpm26WWYVVZUcI INSTANCE = new $$Lambda$KeyguardManagerAdapter$4CHaMPWU20wI3Bpm26WWYVVZUcI();

    private /* synthetic */ $$Lambda$KeyguardManagerAdapter$4CHaMPWU20wI3Bpm26WWYVVZUcI() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        KeyguardManagerAdapter.keyGuardManager = (KeyguardManager) obj;
    }
}
