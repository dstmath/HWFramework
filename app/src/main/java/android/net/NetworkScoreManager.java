package android.net;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.net.INetworkScoreService.Stub;
import android.net.NetworkScorerAppManager.NetworkScorerAppData;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.UserHandle;

public class NetworkScoreManager {
    public static final String ACTION_CHANGE_ACTIVE = "android.net.scoring.CHANGE_ACTIVE";
    public static final String ACTION_CUSTOM_ENABLE = "android.net.scoring.CUSTOM_ENABLE";
    public static final String ACTION_SCORER_CHANGED = "android.net.scoring.SCORER_CHANGED";
    public static final String ACTION_SCORE_NETWORKS = "android.net.scoring.SCORE_NETWORKS";
    public static final String EXTRA_NETWORKS_TO_SCORE = "networksToScore";
    public static final String EXTRA_NEW_SCORER = "newScorer";
    public static final String EXTRA_PACKAGE_NAME = "packageName";
    private final Context mContext;
    private final INetworkScoreService mService;

    public NetworkScoreManager(Context context) {
        this.mContext = context;
        this.mService = Stub.asInterface(ServiceManager.getService(Context.NETWORK_SCORE_SERVICE));
    }

    public String getActiveScorerPackage() {
        NetworkScorerAppData app = NetworkScorerAppManager.getActiveScorer(this.mContext);
        if (app == null) {
            return null;
        }
        return app.mPackageName;
    }

    public boolean updateScores(ScoredNetwork[] networks) throws SecurityException {
        try {
            return this.mService.updateScores(networks);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean clearScores() throws SecurityException {
        try {
            return this.mService.clearScores();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setActiveScorer(String packageName) throws SecurityException {
        try {
            return this.mService.setActiveScorer(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void disableScoring() throws SecurityException {
        try {
            this.mService.disableScoring();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean requestScores(NetworkKey[] networks) throws SecurityException {
        String activeScorer = getActiveScorerPackage();
        if (activeScorer == null) {
            return false;
        }
        Intent intent = new Intent(ACTION_SCORE_NETWORKS);
        intent.setPackage(activeScorer);
        intent.setFlags(StrictMode.PENALTY_DEATH_ON_FILE_URI_EXPOSURE);
        intent.putExtra(EXTRA_NETWORKS_TO_SCORE, (Parcelable[]) networks);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.SYSTEM, permission.SCORE_NETWORKS);
        return true;
    }

    public void registerNetworkScoreCache(int networkType, INetworkScoreCache scoreCache) {
        try {
            this.mService.registerNetworkScoreCache(networkType, scoreCache);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
