package android.app.admin;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/* renamed from: android.app.admin.-$$Lambda$pWaRScwKTZTgGW4Wa_v5R_pKBDU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$pWaRScwKTZTgGW4Wa_v5R_pKBDU implements BiConsumer {
    public static final /* synthetic */ $$Lambda$pWaRScwKTZTgGW4Wa_v5R_pKBDU INSTANCE = new $$Lambda$pWaRScwKTZTgGW4Wa_v5R_pKBDU();

    private /* synthetic */ $$Lambda$pWaRScwKTZTgGW4Wa_v5R_pKBDU() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((CompletableFuture) obj).complete((Boolean) obj2);
    }
}
