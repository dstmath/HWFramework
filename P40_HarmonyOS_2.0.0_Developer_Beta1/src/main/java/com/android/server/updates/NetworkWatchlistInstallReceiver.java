package com.android.server.updates;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkWatchlistManager;
import android.util.Slog;

public class NetworkWatchlistInstallReceiver extends ConfigUpdateInstallReceiver {
    public NetworkWatchlistInstallReceiver() {
        super("/data/misc/network_watchlist/", "network_watchlist.xml", "metadata/", "version");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.updates.ConfigUpdateInstallReceiver
    public void postInstall(Context context, Intent intent) {
        try {
            ((NetworkWatchlistManager) context.getSystemService(NetworkWatchlistManager.class)).reloadWatchlist();
        } catch (Exception e) {
            Slog.wtf("NetworkWatchlistInstallReceiver", "Unable to reload watchlist");
        }
    }
}
