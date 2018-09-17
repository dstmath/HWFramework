package android.hardware.fingerprint;

import android.hardware.fingerprint.FingerprintManager.AnonymousClass2;
import android.hardware.fingerprint.FingerprintManager.LockoutResetCallback;
import android.os.PowerManager.WakeLock;

final /* synthetic */ class -$Lambda$mAn4tgSaQL9G4z96dzSedXu3vvs implements Runnable {
    private final /* synthetic */ Object -$f0;
    private final /* synthetic */ Object -$f1;

    private final /* synthetic */ void $m$0() {
        AnonymousClass2.lambda$-android_hardware_fingerprint_FingerprintManager$2_32064((WakeLock) this.-$f0, (LockoutResetCallback) this.-$f1);
    }

    public /* synthetic */ -$Lambda$mAn4tgSaQL9G4z96dzSedXu3vvs(Object obj, Object obj2) {
        this.-$f0 = obj;
        this.-$f1 = obj2;
    }

    public final void run() {
        $m$0();
    }
}
