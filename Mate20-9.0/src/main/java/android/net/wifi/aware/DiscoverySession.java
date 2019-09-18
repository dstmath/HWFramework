package android.net.wifi.aware;

import android.annotation.SystemApi;
import android.net.NetworkSpecifier;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import dalvik.system.CloseGuard;
import java.lang.ref.WeakReference;

public class DiscoverySession implements AutoCloseable {
    private static final boolean DBG = false;
    private static final int MAX_SEND_RETRY_COUNT = 5;
    private static final String TAG = "DiscoverySession";
    private static final boolean VDBG = false;
    protected final int mClientId;
    private final CloseGuard mCloseGuard = CloseGuard.get();
    protected WeakReference<WifiAwareManager> mMgr;
    protected final int mSessionId;
    protected boolean mTerminated = false;

    public static int getMaxSendRetryCount() {
        return 5;
    }

    public DiscoverySession(WifiAwareManager manager, int clientId, int sessionId) {
        this.mMgr = new WeakReference<>(manager);
        this.mClientId = clientId;
        this.mSessionId = sessionId;
        this.mCloseGuard.open("close");
    }

    public void close() {
        WifiAwareManager mgr = (WifiAwareManager) this.mMgr.get();
        if (mgr == null) {
            Log.w(TAG, "destroy: called post GC on WifiAwareManager");
            return;
        }
        mgr.terminateSession(this.mClientId, this.mSessionId);
        this.mTerminated = true;
        this.mMgr.clear();
        this.mCloseGuard.close();
    }

    public void setTerminated() {
        if (this.mTerminated) {
            Log.w(TAG, "terminate: already terminated.");
            return;
        }
        this.mTerminated = true;
        this.mMgr.clear();
        this.mCloseGuard.close();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            if (!this.mTerminated) {
                close();
            }
        } finally {
            super.finalize();
        }
    }

    @VisibleForTesting
    public int getClientId() {
        return this.mClientId;
    }

    @VisibleForTesting
    public int getSessionId() {
        return this.mSessionId;
    }

    public void sendMessage(PeerHandle peerHandle, int messageId, byte[] message, int retryCount) {
        if (this.mTerminated) {
            Log.w(TAG, "sendMessage: called on terminated session");
            return;
        }
        WifiAwareManager mgr = (WifiAwareManager) this.mMgr.get();
        if (mgr == null) {
            Log.w(TAG, "sendMessage: called post GC on WifiAwareManager");
            return;
        }
        mgr.sendMessage(this.mClientId, this.mSessionId, peerHandle, message, messageId, retryCount);
    }

    public void sendMessage(PeerHandle peerHandle, int messageId, byte[] message) {
        sendMessage(peerHandle, messageId, message, 0);
    }

    public NetworkSpecifier createNetworkSpecifierOpen(PeerHandle peerHandle) {
        int role;
        if (this.mTerminated) {
            Log.w(TAG, "createNetworkSpecifierOpen: called on terminated session");
            return null;
        }
        WifiAwareManager mgr = (WifiAwareManager) this.mMgr.get();
        if (mgr == null) {
            Log.w(TAG, "createNetworkSpecifierOpen: called post GC on WifiAwareManager");
            return null;
        }
        if (this instanceof SubscribeDiscoverySession) {
            role = 0;
        } else {
            role = 1;
        }
        return mgr.createNetworkSpecifier(this.mClientId, role, this.mSessionId, peerHandle, null, null);
    }

    public NetworkSpecifier createNetworkSpecifierPassphrase(PeerHandle peerHandle, String passphrase) {
        int role;
        if (!WifiAwareUtils.validatePassphrase(passphrase)) {
            throw new IllegalArgumentException("Passphrase must meet length requirements");
        } else if (this.mTerminated) {
            Log.w(TAG, "createNetworkSpecifierPassphrase: called on terminated session");
            return null;
        } else {
            WifiAwareManager mgr = (WifiAwareManager) this.mMgr.get();
            if (mgr == null) {
                Log.w(TAG, "createNetworkSpecifierPassphrase: called post GC on WifiAwareManager");
                return null;
            }
            if (this instanceof SubscribeDiscoverySession) {
                role = 0;
            } else {
                role = 1;
            }
            return mgr.createNetworkSpecifier(this.mClientId, role, this.mSessionId, peerHandle, null, passphrase);
        }
    }

    @SystemApi
    public NetworkSpecifier createNetworkSpecifierPmk(PeerHandle peerHandle, byte[] pmk) {
        int role;
        if (!WifiAwareUtils.validatePmk(pmk)) {
            throw new IllegalArgumentException("PMK must 32 bytes");
        } else if (this.mTerminated) {
            Log.w(TAG, "createNetworkSpecifierPmk: called on terminated session");
            return null;
        } else {
            WifiAwareManager mgr = (WifiAwareManager) this.mMgr.get();
            if (mgr == null) {
                Log.w(TAG, "createNetworkSpecifierPmk: called post GC on WifiAwareManager");
                return null;
            }
            if (this instanceof SubscribeDiscoverySession) {
                role = 0;
            } else {
                role = 1;
            }
            return mgr.createNetworkSpecifier(this.mClientId, role, this.mSessionId, peerHandle, pmk, null);
        }
    }
}
