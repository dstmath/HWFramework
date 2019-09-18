package android.net;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.net.INetworkWatchlistManager;
import com.android.internal.util.Preconditions;

public class NetworkWatchlistManager {
    private static final String SHARED_MEMORY_TAG = "NETWORK_WATCHLIST_SHARED_MEMORY";
    private static final String TAG = "NetworkWatchlistManager";
    private final Context mContext;
    private final INetworkWatchlistManager mNetworkWatchlistManager;

    public NetworkWatchlistManager(Context context, INetworkWatchlistManager manager) {
        this.mContext = context;
        this.mNetworkWatchlistManager = manager;
    }

    public NetworkWatchlistManager(Context context) {
        this.mContext = (Context) Preconditions.checkNotNull(context, "missing context");
        this.mNetworkWatchlistManager = INetworkWatchlistManager.Stub.asInterface(ServiceManager.getService(Context.NETWORK_WATCHLIST_SERVICE));
    }

    public void reportWatchlistIfNecessary() {
        try {
            this.mNetworkWatchlistManager.reportWatchlistIfNecessary();
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot report records", e);
            e.rethrowFromSystemServer();
        }
    }

    public void reloadWatchlist() {
        try {
            this.mNetworkWatchlistManager.reloadWatchlist();
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to reload watchlist");
            e.rethrowFromSystemServer();
        }
    }

    public byte[] getWatchlistConfigHash() {
        try {
            return this.mNetworkWatchlistManager.getWatchlistConfigHash();
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to get watchlist config hash");
            throw e.rethrowFromSystemServer();
        }
    }
}
