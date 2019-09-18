package android.companion;

import android.companion.CompanionDeviceManager;
import android.content.IntentSender;
import java.util.function.BiConsumer;

/* renamed from: android.companion.-$$Lambda$OThxsns9MAD5QsKURFQAFbt-3qc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$OThxsns9MAD5QsKURFQAFbt3qc implements BiConsumer {
    public static final /* synthetic */ $$Lambda$OThxsns9MAD5QsKURFQAFbt3qc INSTANCE = new $$Lambda$OThxsns9MAD5QsKURFQAFbt3qc();

    private /* synthetic */ $$Lambda$OThxsns9MAD5QsKURFQAFbt3qc() {
    }

    public final void accept(Object obj, Object obj2) {
        ((CompanionDeviceManager.Callback) obj).onDeviceFound((IntentSender) obj2);
    }
}
