package android.net.wifi.aware;

import android.util.Log;

public class SubscribeDiscoverySession extends DiscoverySession {
    private static final String TAG = "SubscribeDiscSession";

    public SubscribeDiscoverySession(WifiAwareManager manager, int clientId, int sessionId) {
        super(manager, clientId, sessionId);
    }

    public void updateSubscribe(SubscribeConfig subscribeConfig) {
        if (this.mTerminated) {
            Log.w(TAG, "updateSubscribe: called on terminated session");
            return;
        }
        WifiAwareManager mgr = (WifiAwareManager) this.mMgr.get();
        if (mgr == null) {
            Log.w(TAG, "updateSubscribe: called post GC on WifiAwareManager");
        } else {
            mgr.updateSubscribe(this.mClientId, this.mSessionId, subscribeConfig);
        }
    }
}
