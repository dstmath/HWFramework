package android.net.wifi.aware;

import android.net.NetworkSpecifier;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import dalvik.system.CloseGuard;
import java.lang.ref.WeakReference;

public class WifiAwareSession implements AutoCloseable {
    private static final boolean DBG = false;
    private static final String TAG = "WifiAwareSession";
    private static final boolean VDBG = false;
    private final Binder mBinder;
    private final int mClientId;
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final WeakReference<WifiAwareManager> mMgr;
    private boolean mTerminated = true;

    public WifiAwareSession(WifiAwareManager manager, Binder binder, int clientId) {
        this.mMgr = new WeakReference(manager);
        this.mBinder = binder;
        this.mClientId = clientId;
        this.mTerminated = false;
        this.mCloseGuard.open("destroy");
    }

    public void close() {
        WifiAwareManager mgr = (WifiAwareManager) this.mMgr.get();
        if (mgr == null) {
            Log.w(TAG, "destroy: called post GC on WifiAwareManager");
            return;
        }
        mgr.disconnect(this.mClientId, this.mBinder);
        this.mTerminated = true;
        this.mMgr.clear();
        this.mCloseGuard.close();
    }

    protected void finalize() throws Throwable {
        try {
            if (!this.mTerminated) {
                this.mCloseGuard.warnIfOpen();
                close();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public void publish(PublishConfig publishConfig, DiscoverySessionCallback callback, Handler handler) {
        WifiAwareManager mgr = (WifiAwareManager) this.mMgr.get();
        if (mgr == null) {
            Log.e(TAG, "publish: called post GC on WifiAwareManager");
        } else if (this.mTerminated) {
            Log.e(TAG, "publish: called after termination");
        } else {
            mgr.publish(this.mClientId, handler == null ? Looper.getMainLooper() : handler.getLooper(), publishConfig, callback);
        }
    }

    public void subscribe(SubscribeConfig subscribeConfig, DiscoverySessionCallback callback, Handler handler) {
        WifiAwareManager mgr = (WifiAwareManager) this.mMgr.get();
        if (mgr == null) {
            Log.e(TAG, "publish: called post GC on WifiAwareManager");
        } else if (this.mTerminated) {
            Log.e(TAG, "publish: called after termination");
        } else {
            mgr.subscribe(this.mClientId, handler == null ? Looper.getMainLooper() : handler.getLooper(), subscribeConfig, callback);
        }
    }

    public NetworkSpecifier createNetworkSpecifierOpen(int role, byte[] peer) {
        WifiAwareManager mgr = (WifiAwareManager) this.mMgr.get();
        if (mgr == null) {
            Log.e(TAG, "createNetworkSpecifierOpen: called post GC on WifiAwareManager");
            return null;
        } else if (!this.mTerminated) {
            return mgr.createNetworkSpecifier(this.mClientId, role, peer, null, null);
        } else {
            Log.e(TAG, "createNetworkSpecifierOpen: called after termination");
            return null;
        }
    }

    public NetworkSpecifier createNetworkSpecifierPassphrase(int role, byte[] peer, String passphrase) {
        WifiAwareManager mgr = (WifiAwareManager) this.mMgr.get();
        if (mgr == null) {
            Log.e(TAG, "createNetworkSpecifierPassphrase: called post GC on WifiAwareManager");
            return null;
        } else if (this.mTerminated) {
            Log.e(TAG, "createNetworkSpecifierPassphrase: called after termination");
            return null;
        } else if (passphrase != null && passphrase.length() != 0) {
            return mgr.createNetworkSpecifier(this.mClientId, role, peer, null, passphrase);
        } else {
            throw new IllegalArgumentException("Passphrase must not be null or empty");
        }
    }

    public NetworkSpecifier createNetworkSpecifierPmk(int role, byte[] peer, byte[] pmk) {
        WifiAwareManager mgr = (WifiAwareManager) this.mMgr.get();
        if (mgr == null) {
            Log.e(TAG, "createNetworkSpecifierPmk: called post GC on WifiAwareManager");
            return null;
        } else if (this.mTerminated) {
            Log.e(TAG, "createNetworkSpecifierPmk: called after termination");
            return null;
        } else if (pmk != null && pmk.length != 0) {
            return mgr.createNetworkSpecifier(this.mClientId, role, peer, pmk, null);
        } else {
            throw new IllegalArgumentException("PMK must not be null or empty");
        }
    }
}
