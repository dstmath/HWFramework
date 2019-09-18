package defpackage;

import android.os.IBinder;
import android.util.Log;
import com.huawei.android.feature.install.RemoteServiceConnector;

/* renamed from: u  reason: default package */
public final class u implements IBinder.DeathRecipient {
    public final void binderDied() {
        Log.d(RemoteServiceConnector.TAG, "binderDied");
    }
}
