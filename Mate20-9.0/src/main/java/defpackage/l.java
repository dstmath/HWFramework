package defpackage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.huawei.android.feature.install.InstallStateUpdateObserver;

/* renamed from: l  reason: default package */
public final class l extends BroadcastReceiver {
    final /* synthetic */ InstallStateUpdateObserver n;

    private l(InstallStateUpdateObserver installStateUpdateObserver) {
        this.n = installStateUpdateObserver;
    }

    public /* synthetic */ l(InstallStateUpdateObserver installStateUpdateObserver, byte b) {
        this(installStateUpdateObserver);
    }

    public final void onReceive(Context context, Intent intent) {
        Log.d(InstallStateUpdateObserver.TAG, "receive broadcast");
        this.n.handleStateUpdate(intent);
    }
}
