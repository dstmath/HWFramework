package defpackage;

import android.os.IBinder.DeathRecipient;
import com.huawei.bd.Reporter;

/* renamed from: cb */
public final class cb implements DeathRecipient {
    private cb() {
    }

    public void binderDied() {
        Reporter.sService = null;
    }
}
