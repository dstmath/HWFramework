package defpackage;

import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import com.huawei.android.pushagent.PushService;

/* renamed from: b */
public class b extends NetworkCallback {
    final /* synthetic */ PushService k;

    public b(PushService pushService) {
        this.k = pushService;
    }

    public void onAvailable(Network network) {
        super.onAvailable(network);
        aw.i(PushService.TAG, "onAvailable");
        this.k.a(this.k.context, network, true);
    }

    public void onLost(Network network) {
        super.onLost(network);
        aw.i(PushService.TAG, "onLost");
        this.k.a(this.k.context, network, false);
    }
}
