package android.net.wifi.aware;

import android.util.Log;

public class PublishDiscoverySession extends DiscoverySession {
    private static final String TAG = "PublishDiscoverySession";

    public PublishDiscoverySession(WifiAwareManager manager, int clientId, int sessionId) {
        super(manager, clientId, sessionId);
    }

    public void updatePublish(PublishConfig publishConfig) {
        if (this.mTerminated) {
            Log.w(TAG, "updatePublish: called on terminated session");
            return;
        }
        WifiAwareManager mgr = (WifiAwareManager) this.mMgr.get();
        if (mgr == null) {
            Log.w(TAG, "updatePublish: called post GC on WifiAwareManager");
        } else {
            mgr.updatePublish(this.mClientId, this.mSessionId, publishConfig);
        }
    }
}
