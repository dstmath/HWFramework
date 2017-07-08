package android.net.wifi.nan;

import android.util.Log;

public class WifiNanSession {
    private static final boolean DBG = false;
    private static final String TAG = "WifiNanSession";
    private static final boolean VDBG = false;
    private boolean mDestroyed;
    protected WifiNanManager mManager;
    protected int mSessionId;

    public WifiNanSession(WifiNanManager manager, int sessionId) {
        this.mManager = manager;
        this.mSessionId = sessionId;
        this.mDestroyed = DBG;
    }

    public void stop() {
        this.mManager.stopSession(this.mSessionId);
    }

    public void destroy() {
        this.mManager.destroySession(this.mSessionId);
        this.mDestroyed = true;
    }

    protected void finalize() throws Throwable {
        if (!this.mDestroyed) {
            Log.w(TAG, "WifiNanSession mSessionId=" + this.mSessionId + " was not explicitly destroyed. The session may use resources until " + "destroyed so step should be done explicitly");
        }
        destroy();
    }

    public void sendMessage(int peerId, byte[] message, int messageLength, int messageId) {
        this.mManager.sendMessage(this.mSessionId, peerId, message, messageLength, messageId);
    }
}
