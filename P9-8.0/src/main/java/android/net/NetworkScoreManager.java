package android.net;

import android.content.Context;
import android.net.INetworkScoreService.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceManager.ServiceNotFoundException;
import java.util.List;

public class NetworkScoreManager {
    public static final String ACTION_CHANGE_ACTIVE = "android.net.scoring.CHANGE_ACTIVE";
    public static final String ACTION_CUSTOM_ENABLE = "android.net.scoring.CUSTOM_ENABLE";
    public static final String ACTION_RECOMMEND_NETWORKS = "android.net.action.RECOMMEND_NETWORKS";
    public static final String ACTION_SCORER_CHANGED = "android.net.scoring.SCORER_CHANGED";
    public static final String ACTION_SCORE_NETWORKS = "android.net.scoring.SCORE_NETWORKS";
    public static final int CACHE_FILTER_CURRENT_NETWORK = 1;
    public static final int CACHE_FILTER_NONE = 0;
    public static final int CACHE_FILTER_SCAN_RESULTS = 2;
    public static final String EXTRA_NETWORKS_TO_SCORE = "networksToScore";
    public static final String EXTRA_NEW_SCORER = "newScorer";
    public static final String EXTRA_PACKAGE_NAME = "packageName";
    public static final String NETWORK_AVAILABLE_NOTIFICATION_CHANNEL_ID_META_DATA = "android.net.wifi.notification_channel_id_network_available";
    public static final int RECOMMENDATIONS_ENABLED_FORCED_OFF = -1;
    public static final int RECOMMENDATIONS_ENABLED_OFF = 0;
    public static final int RECOMMENDATIONS_ENABLED_ON = 1;
    public static final String RECOMMENDATION_SERVICE_LABEL_META_DATA = "android.net.scoring.recommendation_service_label";
    public static final String USE_OPEN_WIFI_PACKAGE_META_DATA = "android.net.wifi.use_open_wifi_package";
    private final Context mContext;
    private final INetworkScoreService mService = Stub.asInterface(ServiceManager.getServiceOrThrow(Context.NETWORK_SCORE_SERVICE));

    public NetworkScoreManager(Context context) throws ServiceNotFoundException {
        this.mContext = context;
    }

    public String getActiveScorerPackage() {
        try {
            return this.mService.getActiveScorerPackage();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public NetworkScorerAppData getActiveScorer() {
        try {
            return this.mService.getActiveScorer();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<NetworkScorerAppData> getAllValidScorers() {
        try {
            return this.mService.getAllValidScorers();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
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
        try {
            return this.mService.requestScores(networks);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void registerNetworkScoreCache(int networkType, INetworkScoreCache scoreCache) {
        registerNetworkScoreCache(networkType, scoreCache, 0);
    }

    public void registerNetworkScoreCache(int networkType, INetworkScoreCache scoreCache, int filterType) {
        try {
            this.mService.registerNetworkScoreCache(networkType, scoreCache, filterType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unregisterNetworkScoreCache(int networkType, INetworkScoreCache scoreCache) {
        try {
            this.mService.unregisterNetworkScoreCache(networkType, scoreCache);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isCallerActiveScorer(int callingUid) {
        try {
            return this.mService.isCallerActiveScorer(callingUid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
