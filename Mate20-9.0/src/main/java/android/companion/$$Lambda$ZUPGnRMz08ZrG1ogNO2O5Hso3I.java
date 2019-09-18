package android.companion;

import android.companion.CompanionDeviceManager;
import java.util.function.BiConsumer;

/* renamed from: android.companion.-$$Lambda$ZUPGnRMz08ZrG1ogNO-2O5Hso3I  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ZUPGnRMz08ZrG1ogNO2O5Hso3I implements BiConsumer {
    public static final /* synthetic */ $$Lambda$ZUPGnRMz08ZrG1ogNO2O5Hso3I INSTANCE = new $$Lambda$ZUPGnRMz08ZrG1ogNO2O5Hso3I();

    private /* synthetic */ $$Lambda$ZUPGnRMz08ZrG1ogNO2O5Hso3I() {
    }

    public final void accept(Object obj, Object obj2) {
        ((CompanionDeviceManager.Callback) obj).onFailure((CharSequence) obj2);
    }
}
