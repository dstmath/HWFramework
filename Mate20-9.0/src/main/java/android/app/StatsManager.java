package android.app;

import android.annotation.SystemApi;
import android.content.Context;
import android.os.IBinder;
import android.os.IStatsManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.AndroidException;
import android.util.Slog;

@SystemApi
public final class StatsManager {
    public static final String ACTION_STATSD_STARTED = "android.app.action.STATSD_STARTED";
    private static final boolean DEBUG = false;
    public static final String EXTRA_STATS_BROADCAST_SUBSCRIBER_COOKIES = "android.app.extra.STATS_BROADCAST_SUBSCRIBER_COOKIES";
    public static final String EXTRA_STATS_CONFIG_KEY = "android.app.extra.STATS_CONFIG_KEY";
    public static final String EXTRA_STATS_CONFIG_UID = "android.app.extra.STATS_CONFIG_UID";
    public static final String EXTRA_STATS_DIMENSIONS_VALUE = "android.app.extra.STATS_DIMENSIONS_VALUE";
    public static final String EXTRA_STATS_SUBSCRIPTION_ID = "android.app.extra.STATS_SUBSCRIPTION_ID";
    public static final String EXTRA_STATS_SUBSCRIPTION_RULE_ID = "android.app.extra.STATS_SUBSCRIPTION_RULE_ID";
    private static final String TAG = "StatsManager";
    private final Context mContext;
    /* access modifiers changed from: private */
    public IStatsManager mService;

    public static class StatsUnavailableException extends AndroidException {
        public StatsUnavailableException(String reason) {
            super("Failed to connect to statsd: " + reason);
        }

        public StatsUnavailableException(String reason, Throwable e) {
            super("Failed to connect to statsd: " + reason, e);
        }
    }

    private class StatsdDeathRecipient implements IBinder.DeathRecipient {
        private StatsdDeathRecipient() {
        }

        public void binderDied() {
            synchronized (this) {
                IStatsManager unused = StatsManager.this.mService = null;
            }
        }
    }

    public StatsManager(Context context) {
        this.mContext = context;
    }

    public void addConfig(long configKey, byte[] config) throws StatsUnavailableException {
        synchronized (this) {
            try {
                getIStatsManagerLocked().addConfiguration(configKey, config, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to connect to statsd when adding configuration");
                throw new StatsUnavailableException("could not connect", e);
            } catch (SecurityException e2) {
                throw new StatsUnavailableException(e2.getMessage(), e2);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public boolean addConfiguration(long configKey, byte[] config) {
        try {
            addConfig(configKey, config);
            return true;
        } catch (StatsUnavailableException | IllegalArgumentException e) {
            return false;
        }
    }

    public void removeConfig(long configKey) throws StatsUnavailableException {
        synchronized (this) {
            try {
                getIStatsManagerLocked().removeConfiguration(configKey, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to connect to statsd when removing configuration");
                throw new StatsUnavailableException("could not connect", e);
            } catch (SecurityException e2) {
                throw new StatsUnavailableException(e2.getMessage(), e2);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public boolean removeConfiguration(long configKey) {
        try {
            removeConfig(configKey);
            return true;
        } catch (StatsUnavailableException e) {
            return false;
        }
    }

    public void setBroadcastSubscriber(PendingIntent pendingIntent, long configKey, long subscriberId) throws StatsUnavailableException {
        synchronized (this) {
            try {
                IStatsManager service = getIStatsManagerLocked();
                if (pendingIntent != null) {
                    service.setBroadcastSubscriber(configKey, subscriberId, pendingIntent.getTarget().asBinder(), this.mContext.getOpPackageName());
                } else {
                    service.unsetBroadcastSubscriber(configKey, subscriberId, this.mContext.getOpPackageName());
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to connect to statsd when adding broadcast subscriber", e);
                throw new StatsUnavailableException("could not connect", e);
            } catch (SecurityException e2) {
                throw new StatsUnavailableException(e2.getMessage(), e2);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public boolean setBroadcastSubscriber(long configKey, long subscriberId, PendingIntent pendingIntent) {
        try {
            setBroadcastSubscriber(pendingIntent, configKey, subscriberId);
            return true;
        } catch (StatsUnavailableException e) {
            return false;
        }
    }

    public void setFetchReportsOperation(PendingIntent pendingIntent, long configKey) throws StatsUnavailableException {
        synchronized (this) {
            try {
                IStatsManager service = getIStatsManagerLocked();
                if (pendingIntent == null) {
                    service.removeDataFetchOperation(configKey, this.mContext.getOpPackageName());
                } else {
                    service.setDataFetchOperation(configKey, pendingIntent.getTarget().asBinder(), this.mContext.getOpPackageName());
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to connect to statsd when registering data listener.");
                throw new StatsUnavailableException("could not connect", e);
            } catch (SecurityException e2) {
                throw new StatsUnavailableException(e2.getMessage(), e2);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public boolean setDataFetchOperation(long configKey, PendingIntent pendingIntent) {
        try {
            setFetchReportsOperation(pendingIntent, configKey);
            return true;
        } catch (StatsUnavailableException e) {
            return false;
        }
    }

    public byte[] getReports(long configKey) throws StatsUnavailableException {
        byte[] data;
        synchronized (this) {
            try {
                data = getIStatsManagerLocked().getData(configKey, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to connect to statsd when getting data");
                throw new StatsUnavailableException("could not connect", e);
            } catch (SecurityException e2) {
                throw new StatsUnavailableException(e2.getMessage(), e2);
            } catch (Throwable service) {
                throw service;
            }
        }
        return data;
    }

    public byte[] getData(long configKey) {
        try {
            return getReports(configKey);
        } catch (StatsUnavailableException e) {
            return null;
        }
    }

    public byte[] getStatsMetadata() throws StatsUnavailableException {
        byte[] metadata;
        synchronized (this) {
            try {
                metadata = getIStatsManagerLocked().getMetadata(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to connect to statsd when getting metadata");
                throw new StatsUnavailableException("could not connect", e);
            } catch (SecurityException e2) {
                throw new StatsUnavailableException(e2.getMessage(), e2);
            } catch (Throwable service) {
                throw service;
            }
        }
        return metadata;
    }

    public byte[] getMetadata() {
        try {
            return getStatsMetadata();
        } catch (StatsUnavailableException e) {
            return null;
        }
    }

    private IStatsManager getIStatsManagerLocked() throws StatsUnavailableException {
        if (this.mService != null) {
            return this.mService;
        }
        this.mService = IStatsManager.Stub.asInterface(ServiceManager.getService(Context.STATS_MANAGER));
        if (this.mService != null) {
            try {
                this.mService.asBinder().linkToDeath(new StatsdDeathRecipient(), 0);
                return this.mService;
            } catch (RemoteException e) {
                throw new StatsUnavailableException("could not connect when linkToDeath", e);
            }
        } else {
            throw new StatsUnavailableException("could not be found");
        }
    }
}
