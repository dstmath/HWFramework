package com.android.server;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.location.ActivityRecognitionHardware;
import android.hdm.HwDeviceManager;
import android.hsm.HwSystemManager;
import android.location.Address;
import android.location.Criteria;
import android.location.GeocoderParams;
import android.location.Geofence;
import android.location.IBatchedLocationCallback;
import android.location.IGnssMeasurementsListener;
import android.location.IGnssNavigationMessageListener;
import android.location.IGnssStatusListener;
import android.location.IGnssStatusProvider;
import android.location.IGpsGeofenceHardware;
import android.location.ILocationListener;
import android.location.INetInitiatedListener;
import android.location.Location;
import android.location.LocationProvider;
import android.location.LocationRequest;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import com.android.internal.content.PackageMonitor;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.location.ActivityRecognitionProxy;
import com.android.server.location.GeocoderProxy;
import com.android.server.location.GeofenceManager;
import com.android.server.location.GeofenceProxy;
import com.android.server.location.GnssBatchingProvider;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.GnssMeasurementsProvider;
import com.android.server.location.GnssNavigationMessageProvider;
import com.android.server.location.GpsFreezeListener;
import com.android.server.location.GpsFreezeProc;
import com.android.server.location.HwQuickTTFFMonitor;
import com.android.server.location.IHwGpsActionReporter;
import com.android.server.location.IHwGpsLogServices;
import com.android.server.location.IHwLbsLogger;
import com.android.server.location.IHwLocalLocationProvider;
import com.android.server.location.IHwLocationProviderInterface;
import com.android.server.location.LocationBlacklist;
import com.android.server.location.LocationFudger;
import com.android.server.location.LocationProviderInterface;
import com.android.server.location.LocationProviderProxy;
import com.android.server.location.LocationRequestStatistics;
import com.android.server.location.MockProvider;
import com.android.server.location.PassiveProvider;
import com.android.server.net.watchlist.WatchlistLoggingHandler;
import com.android.server.pm.DumpState;
import com.android.server.rms.IHwIpcMonitor;
import com.huawei.pgmng.log.LogPower;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class LocationManagerService extends AbsLocationManagerService {
    private static final String ACCESS_LOCATION_EXTRA_COMMANDS = "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS";
    private static final String ACCESS_MOCK_LOCATION = "android.permission.ACCESS_MOCK_LOCATION";
    private static final String APKSTART = "apkstart";
    private static final String APKSTOP = "apkstop";
    protected static final long CHECK_LOCATION_INTERVAL = 300000;
    public static final boolean D = false;
    private static final long DEFAULT_BACKGROUND_THROTTLE_INTERVAL_MS = 1800000;
    private static final LocationRequest DEFAULT_LOCATION_REQUEST = new LocationRequest();
    private static final int FOREGROUND_IMPORTANCE_CUTOFF = 125;
    private static final String FUSED_LOCATION_SERVICE_ACTION = "com.android.location.service.FusedLocationProvider";
    private static final long HIGH_POWER_INTERVAL_MS = 300000;
    private static final String INSTALL_LOCATION_PROVIDER = "android.permission.INSTALL_LOCATION_PROVIDER";
    private static final int MAX_PROVIDER_SCHEDULING_JITTER_MS = 100;
    protected static final int MSG_CHECK_LOCATION = 7;
    private static final int MSG_GPSFREEZEPROC_LISTNER = 4;
    private static final int MSG_LOCATION_CHANGED = 1;
    private static final int MSG_LOCATION_REMOVE = 3;
    private static final int MSG_LOCATION_REQUEST = 2;
    private static final int MSG_SUPERVISORY_CONTROL = 6;
    private static final int MSG_WHITELIST_LISTNER = 5;
    private static final long NANOS_PER_MILLI = 1000000;
    private static final String NETWORK_LOCATION_SERVICE_ACTION = "com.android.location.service.v3.NetworkLocationProvider";
    private static final int RESOLUTION_LEVEL_COARSE = 1;
    private static final int RESOLUTION_LEVEL_FINE = 2;
    private static final int RESOLUTION_LEVEL_NONE = 0;
    private static final String TAG = "LocationManagerService";
    private static final String WAKELOCK_KEY = "*location*";
    /* access modifiers changed from: private */
    public ActivityManager mActivityManager;
    /* access modifiers changed from: private */
    public final AppOpsManager mAppOps;
    private final ArraySet<String> mBackgroundThrottlePackageWhitelist = new ArraySet<>();
    private LocationBlacklist mBlacklist;
    private boolean mCHRFirstReqFlag = false;
    protected final Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentUserId = 0;
    private int[] mCurrentUserProfiles = {0};
    private final Set<String> mDisabledProviders = new HashSet();
    private final Set<String> mEnabledProviders = new HashSet();
    private GeocoderProxy mGeocodeProvider;
    private GeofenceManager mGeofenceManager;
    private IBatchedLocationCallback mGnssBatchingCallback;
    private LinkedCallback mGnssBatchingDeathCallback;
    private boolean mGnssBatchingInProgress = false;
    private GnssBatchingProvider mGnssBatchingProvider;
    /* access modifiers changed from: private */
    public final ArrayMap<IBinder, Identity> mGnssMeasurementsListeners = new ArrayMap<>();
    /* access modifiers changed from: private */
    public GnssMeasurementsProvider mGnssMeasurementsProvider;
    private GnssLocationProvider.GnssMetricsProvider mGnssMetricsProvider;
    private final ArrayMap<IBinder, Identity> mGnssNavigationMessageListeners = new ArrayMap<>();
    private GnssNavigationMessageProvider mGnssNavigationMessageProvider;
    private IGnssStatusProvider mGnssStatusProvider;
    private GnssLocationProvider.GnssSystemInfoProvider mGnssSystemInfoProvider;
    private IGpsGeofenceHardware mGpsGeofenceProxy;
    /* access modifiers changed from: private */
    public IHwGpsActionReporter mHwGpsActionReporter;
    private IHwLbsLogger mHwLbsLogger;
    private IHwGpsLogServices mHwLocationGpsLogServices;
    /* access modifiers changed from: private */
    public HwQuickTTFFMonitor mHwQuickTTFFMonitor;
    private final HashMap<String, Location> mLastLocation = new HashMap<>();
    private final HashMap<String, Location> mLastLocationCoarseInterval = new HashMap<>();
    protected IHwLocalLocationProvider mLocalLocationProvider;
    private LocationFudger mLocationFudger;
    /* access modifiers changed from: private */
    public LocationWorkerHandler mLocationHandler;
    private IHwIpcMonitor mLocationIpcMonitor;
    HandlerThread mLocationThread;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final HashMap<String, MockProvider> mMockProviders = new HashMap<>();
    private INetInitiatedListener mNetInitiatedListener;
    private boolean mNetworkChrAllowed = true;
    private PackageManager mPackageManager;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        public void onPackageDisappeared(String packageName, int reason) {
            synchronized (LocationManagerService.this.mLock) {
                ArrayList<Receiver> deadReceivers = null;
                for (Receiver receiver : LocationManagerService.this.mReceivers.values()) {
                    if (receiver.mIdentity.mPackageName.equals(packageName)) {
                        if (deadReceivers == null) {
                            deadReceivers = new ArrayList<>();
                        }
                        deadReceivers.add(receiver);
                    }
                }
                if (deadReceivers != null) {
                    Iterator<Receiver> it = deadReceivers.iterator();
                    while (it.hasNext()) {
                        LocationManagerService.this.removeUpdatesLocked(it.next());
                    }
                }
            }
        }
    };
    private PassiveProvider mPassiveProvider;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    private final ArrayList<LocationProviderInterface> mProviders = new ArrayList<>();
    /* access modifiers changed from: private */
    public final HashMap<String, LocationProviderInterface> mProvidersByName = new HashMap<>();
    private final ArrayList<LocationProviderProxy> mProxyProviders = new ArrayList<>();
    private final HashMap<String, LocationProviderInterface> mRealProviders = new HashMap<>();
    /* access modifiers changed from: private */
    public final HashMap<Object, Receiver> mReceivers = new HashMap<>();
    /* access modifiers changed from: private */
    public final HashMap<String, ArrayList<UpdateRecord>> mRecordsByProvider = new HashMap<>();
    /* access modifiers changed from: private */
    public final LocationRequestStatistics mRequestStatistics = new LocationRequestStatistics();
    /* access modifiers changed from: private */
    public UserManager mUserManager;

    public static final class Identity {
        final String mPackageName;
        final int mPid;
        final int mUid;

        Identity(int uid, int pid, String packageName) {
            this.mUid = uid;
            this.mPid = pid;
            this.mPackageName = packageName;
        }
    }

    private class LinkedCallback implements IBinder.DeathRecipient {
        private final IBatchedLocationCallback mCallback;

        public LinkedCallback(IBatchedLocationCallback callback) {
            this.mCallback = callback;
        }

        public IBatchedLocationCallback getUnderlyingListener() {
            return this.mCallback;
        }

        public void binderDied() {
            Log.d(LocationManagerService.TAG, "Remote Batching Callback died: " + this.mCallback);
            LocationManagerService.this.stopGnssBatch();
            LocationManagerService.this.removeGnssBatchingCallback();
        }
    }

    private class LocationWorkerHandler extends Handler {
        public LocationWorkerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            boolean isFreeze;
            boolean z = false;
            switch (msg.what) {
                case 1:
                    LocationManagerService locationManagerService = LocationManagerService.this;
                    Location location = (Location) msg.obj;
                    if (msg.arg1 == 1) {
                        z = true;
                    }
                    locationManagerService.handleLocationChanged(location, z);
                    break;
                case 2:
                    LocationManagerService.this.mHwGpsActionReporter.uploadLocationAction(1, (String) msg.obj);
                    break;
                case 3:
                    LocationManagerService.this.mHwGpsActionReporter.uploadLocationAction(0, (String) msg.obj);
                    break;
                case 4:
                    boolean isFound = false;
                    String pkg = (String) msg.obj;
                    synchronized (LocationManagerService.this.mLock) {
                        ArrayList<UpdateRecord> records = (ArrayList) LocationManagerService.this.mRecordsByProvider.get("gps");
                        if (records != null) {
                            Iterator<UpdateRecord> it = records.iterator();
                            while (true) {
                                if (it.hasNext()) {
                                    if (it.next().mReceiver.mIdentity.mPackageName.equals(pkg)) {
                                        Log.i(LocationManagerService.TAG, " GpsFreezeProc pkgname in gps request:" + pkg);
                                        isFound = true;
                                    }
                                }
                            }
                        }
                        if (isFound) {
                            LocationManagerService.this.applyRequirementsLocked("gps");
                        }
                        boolean isNetworkFound = false;
                        ArrayList<UpdateRecord> networkRecords = (ArrayList) LocationManagerService.this.mRecordsByProvider.get("network");
                        if (networkRecords != null) {
                            Iterator<UpdateRecord> it2 = networkRecords.iterator();
                            while (true) {
                                if (it2.hasNext()) {
                                    if (it2.next().mReceiver.mIdentity.mPackageName.equals(pkg)) {
                                        Log.i(LocationManagerService.TAG, " GpsFreezeProc pkgname in network request:" + pkg);
                                        isNetworkFound = true;
                                    }
                                }
                            }
                        }
                        if (isNetworkFound) {
                            LocationManagerService.this.applyRequirementsLocked("network");
                        }
                        for (Map.Entry<IBinder, Identity> entry : LocationManagerService.this.mGnssMeasurementsListeners.entrySet()) {
                            if (pkg != null && pkg.equals(entry.getValue().mPackageName)) {
                                Log.d(LocationManagerService.TAG, "gnss measurements listener from " + entry.getValue().mPackageName + " is freeze = " + isFreeze);
                                if (isFreeze) {
                                    LocationManagerService.this.mGnssMeasurementsProvider.removeListener(IGnssMeasurementsListener.Stub.asInterface(entry.getKey()));
                                } else {
                                    LocationManagerService.this.mGnssMeasurementsProvider.addListener(IGnssMeasurementsListener.Stub.asInterface(entry.getKey()));
                                }
                            }
                        }
                    }
                    break;
                case 5:
                    List<String> pkgList = (List) msg.obj;
                    int type = msg.arg1;
                    HwQuickTTFFMonitor unused = LocationManagerService.this.mHwQuickTTFFMonitor;
                    if (type != 3) {
                        HwQuickTTFFMonitor unused2 = LocationManagerService.this.mHwQuickTTFFMonitor;
                        if (type != 4) {
                            if (type == 1) {
                                synchronized (LocationManagerService.this.mLock) {
                                    LocationManagerService.this.updateBackgroundThrottlingWhitelistLocked();
                                    LocationManagerService.this.updateProvidersLocked();
                                }
                                break;
                            }
                        } else {
                            LocationManagerService.this.mHwQuickTTFFMonitor.updateDisableList(pkgList);
                            break;
                        }
                    } else {
                        LocationManagerService.this.mHwQuickTTFFMonitor.updateAccWhiteList(pkgList);
                        break;
                    }
                    break;
                case 6:
                    ArrayList<String> providers = (ArrayList) msg.obj;
                    synchronized (LocationManagerService.this.mLock) {
                        Iterator<String> it3 = providers.iterator();
                        while (it3.hasNext()) {
                            LocationManagerService.this.applyRequirementsLocked(it3.next());
                        }
                    }
                    break;
                default:
                    if (!LocationManagerService.this.hwLocationHandleMessage(msg)) {
                        Log.e(LocationManagerService.TAG, "receive unexpected message");
                        break;
                    } else {
                        return;
                    }
            }
        }
    }

    public class Receiver implements IBinder.DeathRecipient, PendingIntent.OnFinished {
        long mAcquireLockTime;
        final int mAllowedResolutionLevel;
        final boolean mHideFromAppOps;
        final Identity mIdentity;
        final Object mKey;
        final ILocationListener mListener;
        boolean mOpHighPowerMonitoring;
        boolean mOpMonitoring;
        int mPendingBroadcasts;
        final PendingIntent mPendingIntent;
        long mReleaseLockTime;
        final HashMap<String, UpdateRecord> mUpdateRecords = new HashMap<>();
        PowerManager.WakeLock mWakeLock;
        final WorkSource mWorkSource;

        Receiver(ILocationListener listener, PendingIntent intent, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
            this.mListener = listener;
            this.mPendingIntent = intent;
            if (listener != null) {
                this.mKey = listener.asBinder();
            } else {
                this.mKey = intent;
            }
            this.mAllowedResolutionLevel = LocationManagerService.this.getAllowedResolutionLevel(pid, uid);
            this.mIdentity = new Identity(uid, pid, packageName);
            if (workSource != null && workSource.isEmpty()) {
                workSource = null;
            }
            this.mWorkSource = workSource;
            this.mHideFromAppOps = hideFromAppOps;
            updateMonitoring(true);
            this.mWakeLock = LocationManagerService.this.mPowerManager.newWakeLock(1, LocationManagerService.WAKELOCK_KEY);
            this.mWakeLock.setWorkSource(workSource == null ? new WorkSource(this.mIdentity.mUid, this.mIdentity.mPackageName) : workSource);
        }

        public boolean equals(Object otherObj) {
            return (otherObj instanceof Receiver) && this.mKey.equals(((Receiver) otherObj).mKey);
        }

        public int hashCode() {
            return this.mKey.hashCode();
        }

        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append("Reciever[");
            s.append(Integer.toHexString(System.identityHashCode(this)));
            if (this.mListener != null) {
                s.append(" listener");
            } else {
                s.append(" intent");
            }
            for (String p : this.mUpdateRecords.keySet()) {
                s.append(" ");
                s.append(this.mUpdateRecords.get(p).toString());
            }
            s.append("]");
            return s.toString();
        }

        public void updateMonitoring(boolean allow) {
            if (!this.mHideFromAppOps) {
                boolean requestingLocation = false;
                boolean requestingHighPowerLocation = false;
                if (allow) {
                    Iterator<UpdateRecord> it = this.mUpdateRecords.values().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        UpdateRecord updateRecord = it.next();
                        if (LocationManagerService.this.isAllowedByCurrentUserSettingsLocked(updateRecord.mProvider)) {
                            requestingLocation = true;
                            LocationProviderInterface locationProvider = (LocationProviderInterface) LocationManagerService.this.mProvidersByName.get(updateRecord.mProvider);
                            ProviderProperties properties = locationProvider != null ? locationProvider.getProperties() : null;
                            Log.i(LocationManagerService.TAG, "mPackageName=" + this.mIdentity.mPackageName + ", interval=" + updateRecord.mRequest.getInterval() + ",provider:" + updateRecord.mProvider);
                            if (properties != null && properties.mPowerRequirement == 3 && updateRecord.mRealRequest.getInterval() < BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS) {
                                requestingHighPowerLocation = true;
                                break;
                            }
                        }
                    }
                }
                Log.i(LocationManagerService.TAG, "requestingHighPowerLocation =" + requestingHighPowerLocation);
                boolean wasOpMonitoring = this.mOpMonitoring;
                this.mOpMonitoring = updateMonitoring(requestingLocation, this.mOpMonitoring, 41);
                if (this.mOpMonitoring != wasOpMonitoring) {
                    LocationManagerService.this.hwSendLocationChangedAction(LocationManagerService.this.mContext, this.mIdentity.mPackageName);
                }
                boolean wasHighPowerMonitoring = this.mOpHighPowerMonitoring;
                this.mOpHighPowerMonitoring = updateMonitoring(requestingHighPowerLocation, this.mOpHighPowerMonitoring, 42);
                if (this.mOpHighPowerMonitoring != wasHighPowerMonitoring) {
                    Intent intent = new Intent("android.location.HIGH_POWER_REQUEST_CHANGE");
                    intent.putExtra("isFrameworkBroadcast", "true");
                    LocationManagerService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                }
            }
        }

        private boolean updateMonitoring(boolean allowMonitoring, boolean currentlyMonitoring, int op) {
            boolean z = false;
            if (!currentlyMonitoring) {
                if (allowMonitoring) {
                    if (LocationManagerService.this.mAppOps.startOpNoThrow(op, this.mIdentity.mUid, this.mIdentity.mPackageName) == 0) {
                        z = true;
                    }
                    return z;
                }
            } else if (!allowMonitoring || LocationManagerService.this.mAppOps.checkOpNoThrow(op, this.mIdentity.mUid, this.mIdentity.mPackageName) != 0) {
                LocationManagerService.this.mAppOps.finishOp(op, this.mIdentity.mUid, this.mIdentity.mPackageName);
                return false;
            }
            return currentlyMonitoring;
        }

        public boolean isListener() {
            return this.mListener != null;
        }

        public boolean isPendingIntent() {
            return this.mPendingIntent != null;
        }

        public ILocationListener getListener() {
            if (this.mListener != null) {
                return this.mListener;
            }
            throw new IllegalStateException("Request for non-existent listener");
        }

        public boolean callStatusChangedLocked(String provider, int status, Bundle extras) {
            if (this.mListener != null) {
                try {
                    synchronized (this) {
                        if (LocationManagerService.this.isFreeze(this.mIdentity.mPackageName)) {
                            return true;
                        }
                        Log.i(LocationManagerService.TAG, "callStatusChangedLocked receiver " + Integer.toHexString(System.identityHashCode(this)) + " mPendingBroadcasts=" + this.mPendingBroadcasts);
                        this.mListener.onStatusChanged(provider, status, extras);
                        incrementPendingBroadcastsLocked();
                    }
                } catch (RemoteException e) {
                    return false;
                }
            } else {
                Intent statusChanged = new Intent();
                statusChanged.putExtras(new Bundle(extras));
                statusChanged.putExtra("status", status);
                try {
                    synchronized (this) {
                        if (LocationManagerService.this.isFreeze(this.mIdentity.mPackageName)) {
                            return true;
                        }
                        this.mPendingIntent.send(LocationManagerService.this.mContext, 0, statusChanged, this, LocationManagerService.this.mLocationHandler, LocationManagerService.this.getResolutionPermission(this.mAllowedResolutionLevel), PendingIntentUtils.createDontSendToRestrictedAppsBundle(null));
                        incrementPendingBroadcastsLocked();
                    }
                } catch (PendingIntent.CanceledException e2) {
                    return false;
                }
            }
            return true;
        }

        public boolean callLocationChangedLocked(Location location) {
            if (this.mListener != null) {
                try {
                    synchronized (this) {
                        if (LocationManagerService.this.isFreeze(this.mIdentity.mPackageName)) {
                            Log.i(LocationManagerService.TAG, "PackageName: " + this.mIdentity.mPackageName);
                            return true;
                        }
                        Log.i(LocationManagerService.TAG, "key of receiver: " + Integer.toHexString(System.identityHashCode(this)) + " mPendingBroadcasts=" + this.mPendingBroadcasts);
                        this.mListener.onLocationChanged(new Location(location));
                        incrementPendingBroadcastsLocked();
                    }
                } catch (RemoteException e) {
                    return false;
                }
            } else {
                Intent locationChanged = new Intent();
                locationChanged.putExtra("location", new Location(location));
                locationChanged.addHwFlags(512);
                try {
                    synchronized (this) {
                        if (LocationManagerService.this.isFreeze(this.mIdentity.mPackageName)) {
                            return true;
                        }
                        this.mPendingIntent.send(LocationManagerService.this.mContext, 0, locationChanged, this, LocationManagerService.this.mLocationHandler, LocationManagerService.this.getResolutionPermission(this.mAllowedResolutionLevel), PendingIntentUtils.createDontSendToRestrictedAppsBundle(null));
                        incrementPendingBroadcastsLocked();
                    }
                } catch (PendingIntent.CanceledException e2) {
                    return false;
                }
            }
            return true;
        }

        public boolean callProviderEnabledLocked(String provider, boolean enabled) {
            updateMonitoring(true);
            if (this.mListener != null) {
                try {
                    synchronized (this) {
                        if (LocationManagerService.this.isFreeze(this.mIdentity.mPackageName)) {
                            return true;
                        }
                        Log.i(LocationManagerService.TAG, "callProviderEnabledLocked receiver " + Integer.toHexString(System.identityHashCode(this)) + " mPendingBroadcasts=" + this.mPendingBroadcasts);
                        if (enabled) {
                            this.mListener.onProviderEnabled(provider);
                        } else {
                            this.mListener.onProviderDisabled(provider);
                        }
                        incrementPendingBroadcastsLocked();
                    }
                } catch (RemoteException e) {
                    return false;
                }
            } else {
                Intent providerIntent = new Intent();
                providerIntent.putExtra("providerEnabled", enabled);
                try {
                    synchronized (this) {
                        if (LocationManagerService.this.isFreeze(this.mIdentity.mPackageName)) {
                            return true;
                        }
                        this.mPendingIntent.send(LocationManagerService.this.mContext, 0, providerIntent, this, LocationManagerService.this.mLocationHandler, LocationManagerService.this.getResolutionPermission(this.mAllowedResolutionLevel), PendingIntentUtils.createDontSendToRestrictedAppsBundle(null));
                        incrementPendingBroadcastsLocked();
                    }
                } catch (PendingIntent.CanceledException e2) {
                    return false;
                }
            }
            return true;
        }

        public void binderDied() {
            Log.i(LocationManagerService.TAG, "Location listener died");
            synchronized (LocationManagerService.this.mLock) {
                LocationManagerService.this.removeUpdatesLocked(this);
            }
            synchronized (this) {
                clearPendingBroadcastsLocked();
            }
        }

        public void onSendFinished(PendingIntent pendingIntent, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
            synchronized (this) {
                decrementPendingBroadcastsLocked();
            }
        }

        private void incrementPendingBroadcastsLocked() {
            int i = this.mPendingBroadcasts;
            this.mPendingBroadcasts = i + 1;
            if (i == 0) {
                this.mAcquireLockTime = SystemClock.elapsedRealtime();
                if (!LocationManagerService.this.mLocationHandler.hasMessages(7)) {
                    LocationManagerService.this.mLocationHandler.sendEmptyMessageDelayed(7, BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS);
                }
                this.mWakeLock.acquire();
            }
        }

        /* access modifiers changed from: private */
        public void decrementPendingBroadcastsLocked() {
            int i = this.mPendingBroadcasts - 1;
            this.mPendingBroadcasts = i;
            if (i == 0 && this.mWakeLock.isHeld()) {
                this.mReleaseLockTime = SystemClock.elapsedRealtime();
                this.mWakeLock.release();
            }
            if (this.mPendingBroadcasts < 0) {
                this.mPendingBroadcasts = 0;
            }
        }

        public void clearPendingBroadcastsLocked() {
            if (this.mPendingBroadcasts > 0) {
                this.mPendingBroadcasts = 0;
                if (this.mWakeLock.isHeld()) {
                    this.mReleaseLockTime = SystemClock.elapsedRealtime();
                    this.mWakeLock.release();
                }
            }
        }
    }

    public class UpdateRecord {
        boolean mIsForegroundUid;
        Location mLastFixBroadcast;
        long mLastStatusBroadcast;
        String mProvider;
        final LocationRequest mRealRequest;
        final Receiver mReceiver;
        LocationRequest mRequest;

        UpdateRecord(String provider, LocationRequest request, Receiver receiver) {
            this.mProvider = provider;
            this.mRealRequest = request;
            this.mRequest = request;
            this.mReceiver = receiver;
            this.mIsForegroundUid = LocationManagerService.isImportanceForeground(LocationManagerService.this.mActivityManager.getPackageImportance(this.mReceiver.mIdentity.mPackageName));
            ArrayList<UpdateRecord> records = (ArrayList) LocationManagerService.this.mRecordsByProvider.get(provider);
            if (records == null) {
                records = new ArrayList<>();
                LocationManagerService.this.mRecordsByProvider.put(provider, records);
            }
            if (!records.contains(this)) {
                records.add(this);
            }
            LocationManagerService.this.mRequestStatistics.startRequesting(this.mReceiver.mIdentity.mPackageName, provider, request.getInterval(), this.mIsForegroundUid);
        }

        /* access modifiers changed from: package-private */
        public void updateForeground(boolean isForeground) {
            this.mIsForegroundUid = isForeground;
            LocationManagerService.this.mRequestStatistics.updateForeground(this.mReceiver.mIdentity.mPackageName, this.mProvider, isForeground);
        }

        /* access modifiers changed from: package-private */
        public void disposeLocked(boolean removeReceiver) {
            LocationManagerService.this.mRequestStatistics.stopRequesting(this.mReceiver.mIdentity.mPackageName, this.mProvider);
            ArrayList<UpdateRecord> globalRecords = (ArrayList) LocationManagerService.this.mRecordsByProvider.get(this.mProvider);
            if (globalRecords != null) {
                globalRecords.remove(this);
            }
            if (removeReceiver) {
                HashMap<String, UpdateRecord> receiverRecords = this.mReceiver.mUpdateRecords;
                if (receiverRecords != null) {
                    receiverRecords.remove(this.mProvider);
                    if (receiverRecords.size() == 0) {
                        LocationManagerService.this.removeUpdatesLocked(this.mReceiver);
                    }
                }
            }
        }

        public String toString() {
            String str;
            StringBuilder sb = new StringBuilder();
            sb.append("UpdateRecord[");
            sb.append(this.mProvider);
            sb.append(" ");
            sb.append(this.mReceiver.mIdentity.mPackageName);
            sb.append("(");
            sb.append(this.mReceiver.mIdentity.mUid);
            if (this.mIsForegroundUid) {
                str = " foreground";
            } else {
                str = " background";
            }
            sb.append(str);
            sb.append(") ");
            sb.append(this.mRealRequest);
            sb.append("]");
            return sb.toString();
        }
    }

    public LocationManagerService(Context context) {
        this.mContext = context;
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).setLocationPackagesProvider(new PackageManagerInternal.PackagesProvider() {
            public String[] getPackages(int userId) {
                return LocationManagerService.this.mContext.getResources().getStringArray(17236014);
            }
        });
        Log.i(TAG, "Constructed");
        if (this.mLocationIpcMonitor == null) {
            this.mLocationIpcMonitor = HwServiceFactory.getIHwIpcMonitor(this.mLock, "location", "location");
            if (this.mLocationIpcMonitor != null) {
                Watchdog.getInstance().addIpcMonitor(this.mLocationIpcMonitor);
            }
        }
        this.mHwLbsLogger = HwServiceFactory.getHwLbsLogger(this.mContext);
    }

    /* JADX WARNING: type inference failed for: r1v17, types: [android.app.AppOpsManager$OnOpChangedListener, com.android.server.LocationManagerService$2] */
    public void systemRunning() {
        synchronized (this.mLock) {
            Log.i(TAG, "systemReady()");
            this.mPackageManager = this.mContext.getPackageManager();
            this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
            this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
            this.mLocationThread = new HandlerThread("LocationThread");
            this.mLocationThread.start();
            this.mLocationHandler = new LocationWorkerHandler(this.mLocationThread.getLooper());
            this.mLocationFudger = new LocationFudger(this.mContext, this.mLocationHandler);
            this.mBlacklist = new LocationBlacklist(this.mContext, this.mLocationHandler);
            this.mBlacklist.init();
            this.mGeofenceManager = new GeofenceManager(this.mContext, this.mBlacklist);
            this.mAppOps.startWatchingMode(0, null, new AppOpsManager.OnOpChangedInternalListener() {
                public void onOpChanged(int op, String packageName) {
                    Log.i(LocationManagerService.TAG, "onOpChanged:" + op + " " + packageName);
                    synchronized (LocationManagerService.this.mLock) {
                        for (Receiver receiver : LocationManagerService.this.mReceivers.values()) {
                            receiver.updateMonitoring(true);
                        }
                        LocationManagerService.this.applyAllProviderRequirementsLocked();
                    }
                }
            });
            this.mPackageManager.addOnPermissionsChangeListener(new PackageManager.OnPermissionsChangedListener() {
                public void onPermissionsChanged(int uid) {
                    synchronized (LocationManagerService.this.mLock) {
                        Iterator it = LocationManagerService.this.mReceivers.values().iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                break;
                            } else if (((Receiver) it.next()).mIdentity.mUid == uid) {
                                Log.i(LocationManagerService.TAG, "onPermissionsChanged: uid=" + uid);
                                LocationManagerService.this.applyAllProviderRequirementsLocked();
                                break;
                            }
                        }
                    }
                }
            });
            this.mActivityManager.addOnUidImportanceListener(new ActivityManager.OnUidImportanceListener() {
                public void onUidImportance(final int uid, final int importance) {
                    LocationManagerService.this.mLocationHandler.post(new Runnable() {
                        public void run() {
                            LocationManagerService.this.onUidImportanceChanged(uid, importance);
                        }
                    });
                }
            }, FOREGROUND_IMPORTANCE_CUTOFF);
            this.mUserManager = (UserManager) this.mContext.getSystemService("user");
            updateUserProfiles(this.mCurrentUserId);
            updateBackgroundThrottlingWhitelistLocked();
            HwServiceFactory.getHwNLPManager().setLocationManagerService(this, this.mContext);
            HwServiceFactory.getHwNLPManager().setHwMultiNlpPolicy(this.mContext);
            initHwLocationPowerTracker(this.mContext);
            this.mHwLocationGpsLogServices = HwServiceFactory.getHwGpsLogServices(this.mContext);
            this.mLocationHandler.post(new Runnable() {
                public void run() {
                    synchronized (LocationManagerService.this.mLock) {
                        LocationManagerService.this.loadProvidersLocked();
                        LocationManagerService.this.updateProvidersLocked();
                    }
                }
            });
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("location_providers_allowed"), true, new ContentObserver(this.mLocationHandler) {
            public void onChange(boolean selfChange) {
                if (LocationManagerService.this.isGPSDisabled()) {
                    Log.d(LocationManagerService.TAG, "gps is disabled by dpm .");
                }
                synchronized (LocationManagerService.this.mLock) {
                    Log.d(LocationManagerService.TAG, "LOCATION_PROVIDERS_ALLOWED onchange");
                    LocationManagerService.this.updateProvidersLocked();
                }
            }
        }, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("location_background_throttle_interval_ms"), true, new ContentObserver(this.mLocationHandler) {
            public void onChange(boolean selfChange) {
                synchronized (LocationManagerService.this.mLock) {
                    LocationManagerService.this.updateProvidersLocked();
                }
            }
        }, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("location_background_throttle_package_whitelist"), true, new ContentObserver(this.mLocationHandler) {
            public void onChange(boolean selfChange) {
                synchronized (LocationManagerService.this.mLock) {
                    LocationManagerService.this.updateBackgroundThrottlingWhitelistLocked();
                    LocationManagerService.this.updateProvidersLocked();
                }
            }
        }, -1);
        hwQuickGpsSwitch();
        this.mPackageMonitor.register(this.mContext, this.mLocationHandler.getLooper(), true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        intentFilter.addAction("android.intent.action.USER_ADDED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    LocationManagerService.this.switchUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                } else if ("android.intent.action.MANAGED_PROFILE_ADDED".equals(action) || "android.intent.action.MANAGED_PROFILE_REMOVED".equals(action)) {
                    LocationManagerService.this.updateUserProfiles(LocationManagerService.this.mCurrentUserId);
                } else if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                    if (getSendingUserId() == -1) {
                        LocationManagerService.this.shutdownComponents();
                    }
                } else if ("android.intent.action.USER_ADDED".equals(action) || "android.intent.action.USER_REMOVED".equals(action)) {
                    int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    if (userId != LocationManagerService.this.mCurrentUserId) {
                        UserInfo ui = LocationManagerService.this.mUserManager.getUserInfo(userId);
                        if (ui != null && ui.profileGroupId == LocationManagerService.this.mCurrentUserId) {
                            LocationManagerService.this.updateUserProfiles(LocationManagerService.this.mCurrentUserId);
                            Log.i(LocationManagerService.TAG, "onReceive action:" + action + ", userId:" + userId + ", updateUserProfiles for currentUserId:" + LocationManagerService.this.mCurrentUserId);
                        }
                    }
                }
            }
        }, UserHandle.ALL, intentFilter, null, this.mLocationHandler);
        GpsFreezeProc.getInstance().registerFreezeListener(new GpsFreezeListener() {
            public void onFreezeProChange(String pkg) {
                if (!LocationManagerService.this.isAllowedByCurrentUserSettingsLocked("gps")) {
                    Log.i(LocationManagerService.TAG, "LocationManager.GPS_PROVIDER is not enable");
                    return;
                }
                LocationManagerService.this.mLocationHandler.sendMessage(Message.obtain(LocationManagerService.this.mLocationHandler, 4, pkg));
            }

            public void onWhiteListChange(int type, List<String> pkgList) {
                LocationManagerService.this.mLocationHandler.sendMessage(Message.obtain(LocationManagerService.this.mLocationHandler, 5, type, 0, pkgList));
            }
        });
    }

    /* access modifiers changed from: private */
    public void onUidImportanceChanged(int uid, int importance) {
        boolean foreground = isImportanceForeground(importance);
        HashSet<String> affectedProviders = new HashSet<>(this.mRecordsByProvider.size());
        synchronized (this.mLock) {
            for (Map.Entry<String, ArrayList<UpdateRecord>> entry : this.mRecordsByProvider.entrySet()) {
                String provider = entry.getKey();
                Iterator it = entry.getValue().iterator();
                while (it.hasNext()) {
                    UpdateRecord record = (UpdateRecord) it.next();
                    if (record.mReceiver.mIdentity.mUid == uid && record.mIsForegroundUid != foreground) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("request from uid ");
                        sb.append(uid);
                        sb.append(" is now ");
                        sb.append(foreground ? "foreground" : "background)");
                        Log.d(TAG, sb.toString());
                        record.mIsForegroundUid = foreground;
                        record.updateForeground(foreground);
                        if (!isThrottlingExemptLocked(record.mReceiver.mIdentity)) {
                            affectedProviders.add(provider);
                        }
                    }
                }
            }
            Iterator<String> it2 = affectedProviders.iterator();
            while (it2.hasNext()) {
                applyRequirementsLocked(it2.next());
            }
            for (Map.Entry<IBinder, Identity> entry2 : this.mGnssMeasurementsListeners.entrySet()) {
                if (entry2.getValue().mUid == uid) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("gnss measurements listener from uid ");
                    sb2.append(uid);
                    sb2.append(" is now ");
                    sb2.append(foreground ? "foreground" : "background)");
                    Log.d(TAG, sb2.toString());
                    if (!foreground) {
                        if (!isThrottlingExemptLocked(entry2.getValue())) {
                            this.mGnssMeasurementsProvider.removeListener(IGnssMeasurementsListener.Stub.asInterface(entry2.getKey()));
                        }
                    }
                    this.mGnssMeasurementsProvider.addListener(IGnssMeasurementsListener.Stub.asInterface(entry2.getKey()));
                }
            }
            for (Map.Entry<IBinder, Identity> entry3 : this.mGnssNavigationMessageListeners.entrySet()) {
                if (entry3.getValue().mUid == uid) {
                    if (!foreground) {
                        if (!isThrottlingExemptLocked(entry3.getValue())) {
                            this.mGnssNavigationMessageProvider.removeListener(IGnssNavigationMessageListener.Stub.asInterface(entry3.getKey()));
                        }
                    }
                    this.mGnssNavigationMessageProvider.addListener(IGnssNavigationMessageListener.Stub.asInterface(entry3.getKey()));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static boolean isImportanceForeground(int importance) {
        return importance <= FOREGROUND_IMPORTANCE_CUTOFF;
    }

    /* access modifiers changed from: private */
    public void shutdownComponents() {
        LocationProviderInterface gpsProvider = this.mProvidersByName.get("gps");
        if (gpsProvider != null && gpsProvider.isEnabled()) {
            gpsProvider.disable();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateUserProfiles(int currentUserId) {
        int[] profileIds = this.mUserManager.getProfileIdsWithDisabled(currentUserId);
        synchronized (this.mLock) {
            this.mCurrentUserProfiles = profileIds;
        }
    }

    private boolean isCurrentProfile(int userId) {
        boolean contains;
        synchronized (this.mLock) {
            contains = ArrayUtils.contains(this.mCurrentUserProfiles, userId);
        }
        return contains;
    }

    private void ensureFallbackFusedProviderPresentLocked(ArrayList<String> pkgs) {
        PackageManager pm = this.mContext.getPackageManager();
        String systemPackageName = this.mContext.getPackageName();
        ArrayList<HashSet<Signature>> sigSets = ServiceWatcher.getSignatureSets(this.mContext, pkgs);
        for (ResolveInfo rInfo : pm.queryIntentServicesAsUser(new Intent(FUSED_LOCATION_SERVICE_ACTION), 128, this.mCurrentUserId)) {
            String packageName = rInfo.serviceInfo.packageName;
            try {
                if (!ServiceWatcher.isSignatureMatch(pm.getPackageInfo(packageName, 64).signatures, sigSets)) {
                    Log.w(TAG, packageName + " resolves service " + FUSED_LOCATION_SERVICE_ACTION + ", but has wrong signature, ignoring");
                } else if (rInfo.serviceInfo.metaData == null) {
                    Log.w(TAG, "Found fused provider without metadata: " + packageName);
                } else if (rInfo.serviceInfo.metaData.getInt(ServiceWatcher.EXTRA_SERVICE_VERSION, -1) != 0) {
                    Log.i(TAG, "Fallback candidate not version 0: " + packageName);
                } else if ((rInfo.serviceInfo.applicationInfo.flags & 1) == 0) {
                    Log.i(TAG, "Fallback candidate not in /system: " + packageName);
                } else if (pm.checkSignatures(systemPackageName, packageName) != 0) {
                    Log.i(TAG, "Fallback candidate not signed the same as system: " + packageName);
                } else {
                    Log.i(TAG, "Found fallback provider: " + packageName);
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "missing package: " + packageName);
            }
        }
        throw new IllegalStateException("Unable to find a fused location provider that is in the system partition with version 0 and signed with the platform certificate. Such a package is needed to provide a default fused location provider in the event that no other fused location provider has been installed or is currently available. For example, coreOnly boot mode when decrypting the data partition. The fallback must also be marked coreApp=\"true\" in the manifest");
    }

    /* access modifiers changed from: private */
    public void loadProvidersLocked() {
        PassiveProvider passiveProvider = new PassiveProvider(this);
        addProviderLocked(passiveProvider);
        this.mEnabledProviders.add(passiveProvider.getName());
        this.mPassiveProvider = passiveProvider;
        GnssLocationProvider gnssProvider = HwServiceFactory.createHwGnssLocationProvider(this.mContext, this, this.mLocationHandler.getLooper());
        if (GnssLocationProvider.isSupported()) {
            this.mGnssSystemInfoProvider = gnssProvider.getGnssSystemInfoProvider();
            this.mGnssBatchingProvider = gnssProvider.getGnssBatchingProvider();
            this.mGnssMetricsProvider = gnssProvider.getGnssMetricsProvider();
            this.mGnssStatusProvider = gnssProvider.getGnssStatusProvider();
            this.mNetInitiatedListener = gnssProvider.getNetInitiatedListener();
            addProviderLocked(gnssProvider);
            synchronized (this.mRealProviders) {
                this.mRealProviders.put("gps", gnssProvider);
            }
            this.mGnssMeasurementsProvider = gnssProvider.getGnssMeasurementsProvider();
            this.mGnssNavigationMessageProvider = gnssProvider.getGnssNavigationMessageProvider();
            this.mGpsGeofenceProxy = gnssProvider.getGpsGeofenceProxy();
        }
        this.mHwQuickTTFFMonitor = HwQuickTTFFMonitor.getInstance(this.mContext, gnssProvider);
        this.mHwQuickTTFFMonitor.startMonitor();
        Resources resources = this.mContext.getResources();
        ArrayList arrayList = new ArrayList();
        String[] pkgs = resources.getStringArray(17236014);
        Log.i(TAG, "certificates for location providers pulled from: " + Arrays.toString(pkgs));
        if (pkgs != null) {
            arrayList.addAll(Arrays.asList(pkgs));
        }
        ensureFallbackFusedProviderPresentLocked(arrayList);
        LocationProviderProxy networkProvider = HwServiceFactory.locationProviderProxyCreateAndBind(this.mContext, "network", NETWORK_LOCATION_SERVICE_ACTION, 17956962, 17039833, 17236014, this.mLocationHandler);
        if (networkProvider != null) {
            synchronized (this.mRealProviders) {
                this.mRealProviders.put("network", networkProvider);
            }
            this.mProxyProviders.add(networkProvider);
            addProviderLocked(networkProvider);
        } else {
            Slog.w(TAG, "no network location provider found");
        }
        LocationProviderProxy fusedLocationProvider = LocationProviderProxy.createAndBind(this.mContext, "fused", FUSED_LOCATION_SERVICE_ACTION, 17956953, 17039808, 17236014, this.mLocationHandler);
        if (fusedLocationProvider != null) {
            addProviderLocked(fusedLocationProvider);
            this.mProxyProviders.add(fusedLocationProvider);
            this.mEnabledProviders.add(fusedLocationProvider.getName());
            synchronized (this.mRealProviders) {
                this.mRealProviders.put("fused", fusedLocationProvider);
            }
        } else {
            Slog.e(TAG, "no fused location provider found", new IllegalStateException("Location service needs a fused location provider"));
        }
        this.mGeocodeProvider = HwServiceFactory.geocoderProxyCreateAndBind(this.mContext, 17956954, 17039809, 17236014, this.mLocationHandler);
        if (this.mGeocodeProvider == null) {
            Slog.e(TAG, "no geocoder provider found");
        }
        checkGeoFencerEnabled(this.mPackageManager);
        GeofenceProxy provider = GeofenceProxy.createAndBind(this.mContext, 17956955, 17039810, 17236014, this.mLocationHandler, this.mGpsGeofenceProxy, null);
        if (provider == null) {
            Slog.d(TAG, "Unable to bind FLP Geofence proxy.");
        }
        this.mLocalLocationProvider = HwServiceFactory.getHwLocalLocationProvider(this.mContext, this);
        enableLocalLocationProviders(gnssProvider);
        boolean activityRecognitionHardwareIsSupported = ActivityRecognitionHardware.isSupported();
        ActivityRecognitionHardware activityRecognitionHardware = null;
        if (activityRecognitionHardwareIsSupported) {
            activityRecognitionHardware = ActivityRecognitionHardware.getInstance(this.mContext);
        } else {
            Slog.d(TAG, "Hardware Activity-Recognition not supported.");
        }
        if (ActivityRecognitionProxy.createAndBind(this.mContext, this.mLocationHandler, activityRecognitionHardwareIsSupported, activityRecognitionHardware, 17956947, 17039765, 17236014) == null) {
            Slog.d(TAG, "Unable to bind ActivityRecognitionProxy.");
        }
        String[] testProviderStrings = resources.getStringArray(17236041);
        int length = testProviderStrings.length;
        int i = 0;
        while (i < length) {
            String[] fragments = testProviderStrings[i].split(",");
            GeofenceProxy provider2 = provider;
            String name = fragments[0].trim();
            PassiveProvider passiveProvider2 = passiveProvider;
            if (this.mProvidersByName.get(name) == null) {
                ProviderProperties providerProperties = new ProviderProperties(Boolean.parseBoolean(fragments[1]), Boolean.parseBoolean(fragments[2]), Boolean.parseBoolean(fragments[3]), Boolean.parseBoolean(fragments[4]), Boolean.parseBoolean(fragments[5]), Boolean.parseBoolean(fragments[6]), Boolean.parseBoolean(fragments[7]), Integer.parseInt(fragments[8]), Integer.parseInt(fragments[9]));
                addTestProviderLocked(name, providerProperties);
                i++;
                provider = provider2;
                passiveProvider = passiveProvider2;
                gnssProvider = gnssProvider;
            } else {
                throw new IllegalArgumentException("Provider \"" + name + "\" already exists");
            }
        }
        PassiveProvider passiveProvider3 = passiveProvider;
        GnssLocationProvider gnssLocationProvider = gnssProvider;
        this.mHwGpsActionReporter = HwServiceFactory.getHwGpsActionReporter(this.mContext, this);
    }

    /* access modifiers changed from: private */
    public void switchUser(int userId) {
        if (this.mCurrentUserId != userId) {
            this.mBlacklist.switchUser(userId);
            this.mLocationHandler.removeMessages(1);
            this.mLocationHandler.removeMessages(2);
            this.mLocationHandler.removeMessages(3);
            Log.i(TAG, "switchUser:" + userId);
            synchronized (this.mLock) {
                this.mLastLocation.clear();
                this.mLastLocationCoarseInterval.clear();
                Iterator<LocationProviderInterface> it = this.mProviders.iterator();
                while (it.hasNext()) {
                    updateProviderListenersLocked(it.next().getName(), false);
                }
                this.mCurrentUserId = userId;
                updateUserProfiles(userId);
                updateProvidersLocked();
            }
        }
    }

    public void locationCallbackFinished(ILocationListener listener) {
        synchronized (this.mLock) {
            Receiver receiver = this.mReceivers.get(listener.asBinder());
            if (receiver != null) {
                synchronized (receiver) {
                    long identity = Binder.clearCallingIdentity();
                    receiver.decrementPendingBroadcastsLocked();
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    public int getGnssYearOfHardware() {
        if (this.mGnssSystemInfoProvider != null) {
            return this.mGnssSystemInfoProvider.getGnssYearOfHardware();
        }
        return 0;
    }

    public String getGnssHardwareModelName() {
        if (this.mGnssSystemInfoProvider != null) {
            return this.mGnssSystemInfoProvider.getGnssHardwareModelName();
        }
        return null;
    }

    /* JADX INFO: finally extract failed */
    private boolean hasGnssPermissions(String packageName) {
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel, "gps");
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        try {
            boolean hasLocationAccess = checkLocationAccess(pid, uid, packageName, allowedResolutionLevel);
            Binder.restoreCallingIdentity(identity);
            if (!HwSystemManager.allowOp(this.mContext, 8)) {
                return true;
            }
            return hasLocationAccess;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    public int getGnssBatchSize(String packageName) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to access hardware batching");
        if (!hasGnssPermissions(packageName) || this.mGnssBatchingProvider == null) {
            return 0;
        }
        return this.mGnssBatchingProvider.getBatchSize();
    }

    public boolean addGnssBatchingCallback(IBatchedLocationCallback callback, String packageName) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to access hardware batching");
        if (!hasGnssPermissions(packageName) || this.mGnssBatchingProvider == null) {
            return false;
        }
        this.mGnssBatchingCallback = callback;
        this.mGnssBatchingDeathCallback = new LinkedCallback(callback);
        try {
            callback.asBinder().linkToDeath(this.mGnssBatchingDeathCallback, 0);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Remote listener already died.", e);
            return false;
        }
    }

    public void removeGnssBatchingCallback() {
        try {
            this.mGnssBatchingCallback.asBinder().unlinkToDeath(this.mGnssBatchingDeathCallback, 0);
        } catch (NoSuchElementException e) {
            Log.e(TAG, "Couldn't unlink death callback.", e);
        }
        this.mGnssBatchingCallback = null;
        this.mGnssBatchingDeathCallback = null;
    }

    public boolean startGnssBatch(long periodNanos, boolean wakeOnFifoFull, String packageName) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to access hardware batching");
        if (!hasGnssPermissions(packageName) || this.mGnssBatchingProvider == null) {
            return false;
        }
        if (this.mGnssBatchingInProgress) {
            Log.e(TAG, "startGnssBatch unexpectedly called w/o stopping prior batch");
            stopGnssBatch();
        }
        this.mGnssBatchingInProgress = true;
        return this.mGnssBatchingProvider.start(periodNanos, wakeOnFifoFull);
    }

    public void flushGnssBatch(String packageName) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to access hardware batching");
        if (!hasGnssPermissions(packageName)) {
            Log.e(TAG, "flushGnssBatch called without GNSS permissions");
            return;
        }
        if (!this.mGnssBatchingInProgress) {
            Log.w(TAG, "flushGnssBatch called with no batch in progress");
        }
        if (this.mGnssBatchingProvider != null) {
            this.mGnssBatchingProvider.flush();
        }
    }

    public boolean stopGnssBatch() {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to access hardware batching");
        if (this.mGnssBatchingProvider == null) {
            return false;
        }
        this.mGnssBatchingInProgress = false;
        return this.mGnssBatchingProvider.stop();
    }

    public void reportLocationBatch(List<Location> locations) {
        checkCallerIsProvider();
        if (!isAllowedByCurrentUserSettingsLocked("gps")) {
            Slog.w(TAG, "reportLocationBatch() called without user permission, locations blocked");
        } else if (this.mGnssBatchingCallback == null) {
            Slog.e(TAG, "reportLocationBatch() called without active Callback");
        } else {
            try {
                this.mGnssBatchingCallback.onLocationBatch(locations);
            } catch (RemoteException e) {
                Slog.e(TAG, "mGnssBatchingCallback.onLocationBatch failed", e);
            }
        }
    }

    private void addProviderLocked(LocationProviderInterface provider) {
        this.mProviders.add(provider);
        this.mProvidersByName.put(provider.getName(), provider);
    }

    private void removeProviderLocked(LocationProviderInterface provider) {
        provider.disable();
        this.mProviders.remove(provider);
        this.mProvidersByName.remove(provider.getName());
    }

    /* access modifiers changed from: private */
    public boolean isAllowedByCurrentUserSettingsLocked(String provider) {
        return isAllowedByUserSettingsLockedForUser(provider, this.mCurrentUserId);
    }

    private boolean isAllowedByUserSettingsLockedForUser(String provider, int userId) {
        if (this.mEnabledProviders.contains(provider)) {
            return true;
        }
        if (this.mDisabledProviders.contains(provider)) {
            return false;
        }
        return isLocationProviderEnabledForUser(provider, userId);
    }

    private boolean isAllowedByUserSettingsLocked(String provider, int uid, int userId) {
        if (isCurrentProfile(UserHandle.getUserId(uid)) || isUidALocationProvider(uid)) {
            return isAllowedByUserSettingsLockedForUser(provider, userId);
        }
        return false;
    }

    /* access modifiers changed from: private */
    public String getResolutionPermission(int resolutionLevel) {
        switch (resolutionLevel) {
            case 1:
                return "android.permission.ACCESS_COARSE_LOCATION";
            case 2:
                return "android.permission.ACCESS_FINE_LOCATION";
            default:
                return null;
        }
    }

    /* access modifiers changed from: private */
    public int getAllowedResolutionLevel(int pid, int uid) {
        if (this.mContext.checkPermission("android.permission.ACCESS_FINE_LOCATION", pid, uid) == 0) {
            return 2;
        }
        if (this.mContext.checkPermission("android.permission.ACCESS_COARSE_LOCATION", pid, uid) == 0) {
            return 1;
        }
        return 0;
    }

    private int getCallerAllowedResolutionLevel() {
        return getAllowedResolutionLevel(Binder.getCallingPid(), Binder.getCallingUid());
    }

    private void checkResolutionLevelIsSufficientForGeofenceUse(int allowedResolutionLevel) {
        if (allowedResolutionLevel < 2) {
            throw new SecurityException("Geofence usage requires ACCESS_FINE_LOCATION permission");
        }
    }

    private int getMinimumResolutionLevelForProviderUse(String provider) {
        if ("gps".equals(provider) || "passive".equals(provider)) {
            return 2;
        }
        if ("network".equals(provider) || "fused".equals(provider)) {
            return 1;
        }
        LocationProviderInterface lp = this.mMockProviders.get(provider);
        if (lp != null) {
            ProviderProperties properties = lp.getProperties();
            if (properties == null || properties.mRequiresSatellite) {
                return 2;
            }
            if (properties.mRequiresNetwork || properties.mRequiresCell) {
                return 1;
            }
        }
        return 2;
    }

    private void checkResolutionLevelIsSufficientForProviderUse(int allowedResolutionLevel, String providerName) {
        int requiredResolutionLevel = getMinimumResolutionLevelForProviderUse(providerName);
        if (allowedResolutionLevel < requiredResolutionLevel) {
            switch (requiredResolutionLevel) {
                case 1:
                    throw new SecurityException("\"" + providerName + "\" location provider requires ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission.");
                case 2:
                    throw new SecurityException("\"" + providerName + "\" location provider requires ACCESS_FINE_LOCATION permission.");
                default:
                    throw new SecurityException("Insufficient permission for \"" + providerName + "\" location provider.");
            }
        }
    }

    private void checkDeviceStatsAllowed() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
    }

    private void checkUpdateAppOpsAllowed() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_APP_OPS_STATS", null);
    }

    public static int resolutionLevelToOp(int allowedResolutionLevel) {
        if (allowedResolutionLevel == 0) {
            return -1;
        }
        if (allowedResolutionLevel == 1) {
            return 0;
        }
        return 1;
    }

    /* access modifiers changed from: package-private */
    public boolean reportLocationAccessNoThrow(int pid, int uid, String packageName, int allowedResolutionLevel) {
        int op = resolutionLevelToOp(allowedResolutionLevel);
        boolean z = false;
        if (op >= 0 && this.mAppOps.noteOpNoThrow(op, uid, packageName) != 0) {
            return false;
        }
        if (getAllowedResolutionLevel(pid, uid) >= allowedResolutionLevel) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean checkLocationAccess(int pid, int uid, String packageName, int allowedResolutionLevel) {
        int op = resolutionLevelToOp(allowedResolutionLevel);
        boolean z = false;
        if (op >= 0 && this.mAppOps.checkOp(op, uid, packageName) != 0) {
            return false;
        }
        if (getAllowedResolutionLevel(pid, uid) >= allowedResolutionLevel) {
            z = true;
        }
        return z;
    }

    public List<String> getAllProviders() {
        ArrayList<String> out;
        synchronized (this.mLock) {
            out = new ArrayList<>(this.mProviders.size());
            Iterator<LocationProviderInterface> it = this.mProviders.iterator();
            while (it.hasNext()) {
                String name = it.next().getName();
                if (!"fused".equals(name)) {
                    if (!IHwLocalLocationProvider.LOCAL_PROVIDER.equals(name)) {
                        out.add(name);
                    }
                }
            }
        }
        ArrayList<String> out2 = out;
        Log.i(TAG, "getAllProviders()=" + out2);
        return out2;
    }

    public List<String> getProviders(Criteria criteria, boolean enabledOnly) {
        ArrayList<String> out;
        hwSendBehavior(IHwBehaviorCollectManager.BehaviorId.LOCATIONMANAGER_GETPROVIDERS);
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        Log.i(TAG, "enableOnly=" + enabledOnly + "allowedLevel=" + allowedResolutionLevel);
        try {
            synchronized (this.mLock) {
                out = new ArrayList<>(this.mProviders.size());
                Iterator<LocationProviderInterface> it = this.mProviders.iterator();
                while (it.hasNext()) {
                    LocationProviderInterface provider = it.next();
                    String name = provider.getName();
                    if (!"fused".equals(name)) {
                        if (!IHwLocalLocationProvider.LOCAL_PROVIDER.equals(name)) {
                            Log.i(TAG, "provider=" + name);
                            if (allowedResolutionLevel >= getMinimumResolutionLevelForProviderUse(name)) {
                                if (enabledOnly && !isAllowedByUserSettingsLocked(name, uid, this.mCurrentUserId)) {
                                    Log.i(TAG, "provider is not enable");
                                } else if (criteria == null || LocationProvider.propertiesMeetCriteria(name, provider.getProperties(), criteria)) {
                                    out.add(name);
                                } else {
                                    Log.i(TAG, "the criteria of provider is not matches");
                                }
                            }
                        }
                    }
                }
            }
            ArrayList<String> out2 = out;
            Binder.restoreCallingIdentity(identity);
            Log.i(TAG, "getProviders()=" + out2);
            return out2;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    public String getBestProvider(Criteria criteria, boolean enabledOnly) {
        hwSendBehavior(IHwBehaviorCollectManager.BehaviorId.LOCATIONMANAGER_GETBESTPROVIDER);
        List<String> providers = getProviders(criteria, enabledOnly);
        if (!providers.isEmpty()) {
            String result = pickBest(providers);
            Log.i(TAG, "getBestProvider(" + criteria + ", " + enabledOnly + ")=" + result);
            return result;
        }
        List<String> providers2 = getProviders(null, enabledOnly);
        if (!providers2.isEmpty()) {
            String result2 = pickBest(providers2);
            Log.i(TAG, "getBestProvider(" + criteria + ", " + enabledOnly + ")=" + result2);
            return result2;
        }
        Log.i(TAG, "getBestProvider(" + criteria + ", " + enabledOnly + ")=" + null);
        return null;
    }

    private String pickBest(List<String> providers) {
        if (providers.contains("gps")) {
            return "gps";
        }
        if (providers.contains("network")) {
            return "network";
        }
        return providers.get(0);
    }

    public boolean providerMeetsCriteria(String provider, Criteria criteria) {
        LocationProviderInterface p = this.mProvidersByName.get(provider);
        if (p != null) {
            boolean result = LocationProvider.propertiesMeetCriteria(p.getName(), p.getProperties(), criteria);
            Log.i(TAG, "providerMeetsCriteria(" + provider + ", " + criteria + ")=" + result);
            return result;
        }
        throw new IllegalArgumentException("provider=" + provider);
    }

    /* access modifiers changed from: private */
    public void updateProvidersLocked() {
        boolean changesMade = false;
        for (int i = this.mProviders.size() - 1; i >= 0; i--) {
            LocationProviderInterface p = this.mProviders.get(i);
            boolean isEnabled = p.isEnabled();
            String name = p.getName();
            boolean shouldBeEnabled = isAllowedByCurrentUserSettingsLocked(name);
            Log.d(TAG, "Provider name = " + name + " shouldbeEnabled = " + shouldBeEnabled);
            if (isEnabled && !shouldBeEnabled) {
                updateProviderListenersLocked(name, false);
                this.mLastLocation.clear();
                this.mLastLocationCoarseInterval.clear();
                changesMade = true;
            } else if (!isEnabled && shouldBeEnabled) {
                updateProviderListenersLocked(name, true);
                changesMade = true;
            }
        }
        if (changesMade) {
            this.mContext.sendBroadcastAsUser(new Intent("android.location.PROVIDERS_CHANGED"), UserHandle.ALL);
            Intent intent = new Intent("android.location.MODE_CHANGED");
            intent.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private void updateProviderListenersLocked(String provider, boolean enabled) {
        int listeners = 0;
        LocationProviderInterface p = this.mProvidersByName.get(provider);
        if (p != null) {
            ArrayList<Receiver> deadReceivers = null;
            ArrayList<UpdateRecord> records = this.mRecordsByProvider.get(provider);
            if (records != null) {
                Iterator<UpdateRecord> it = records.iterator();
                while (it.hasNext()) {
                    UpdateRecord record = it.next();
                    if (isCurrentProfile(UserHandle.getUserId(record.mReceiver.mIdentity.mUid))) {
                        if (!record.mReceiver.callProviderEnabledLocked(provider, enabled)) {
                            if (deadReceivers == null) {
                                deadReceivers = new ArrayList<>();
                            }
                            deadReceivers.add(record.mReceiver);
                        }
                        listeners++;
                    }
                }
            }
            if (deadReceivers != null) {
                for (int i = deadReceivers.size() - 1; i >= 0; i--) {
                    removeUpdatesLocked(deadReceivers.get(i));
                }
            }
            if (enabled) {
                p.enable();
                if (listeners > 0) {
                    applyRequirementsLocked(provider);
                }
            } else {
                p.disable();
            }
        }
    }

    /* access modifiers changed from: private */
    public void applyRequirementsLocked(String provider) {
        long backgroundThrottleInterval;
        ContentResolver resolver;
        boolean z;
        String str = provider;
        Log.i(TAG, "applyRequirementsLocked to " + str);
        LocationProviderInterface p = this.mProvidersByName.get(str);
        if (p == null) {
            Log.i(TAG, "LocationProviderInterface is null.");
            return;
        }
        ArrayList<UpdateRecord> records = this.mRecordsByProvider.get(str);
        WorkSource worksource = new WorkSource();
        ProviderRequest providerRequest = new ProviderRequest();
        ContentResolver resolver2 = this.mContext.getContentResolver();
        long backgroundThrottleInterval2 = Settings.Global.getLong(resolver2, "location_background_throttle_interval_ms", 1800000);
        boolean z2 = true;
        providerRequest.lowPowerMode = true;
        if (records != null) {
            Iterator<UpdateRecord> it = records.iterator();
            while (it.hasNext()) {
                UpdateRecord record = it.next();
                if (!isCurrentProfile(UserHandle.getUserId(record.mReceiver.mIdentity.mUid))) {
                    resolver = resolver2;
                    backgroundThrottleInterval = backgroundThrottleInterval2;
                    z = z2;
                } else if (checkLocationAccess(record.mReceiver.mIdentity.mPid, record.mReceiver.mIdentity.mUid, record.mReceiver.mIdentity.mPackageName, record.mReceiver.mAllowedResolutionLevel)) {
                    LocationRequest locationRequest = record.mRealRequest;
                    long interval = locationRequest.getInterval();
                    if (!"gps".equals(str) || !isFreeze(record.mReceiver.mIdentity.mPackageName)) {
                        resolver = resolver2;
                        if (!"network".equals(str) || !isFreeze(record.mReceiver.mIdentity.mPackageName)) {
                            if (!isThrottlingExemptLocked(record.mReceiver.mIdentity)) {
                                if (!record.mIsForegroundUid) {
                                    interval = Math.max(interval, backgroundThrottleInterval2);
                                }
                                if (interval != locationRequest.getInterval()) {
                                    locationRequest = new LocationRequest(locationRequest);
                                    locationRequest.setInterval(interval);
                                }
                            }
                            record.mRequest = locationRequest;
                            providerRequest.locationRequests.add(locationRequest);
                            if (!locationRequest.isLowPowerMode()) {
                                providerRequest.lowPowerMode = false;
                            }
                            backgroundThrottleInterval = backgroundThrottleInterval2;
                            if (interval < providerRequest.interval) {
                                z = true;
                                providerRequest.reportLocation = true;
                                providerRequest.interval = interval;
                            } else {
                                z = true;
                            }
                        } else {
                            Log.i(TAG, "packageName:" + record.mReceiver.mIdentity.mPackageName + " is freeze, can't start network");
                        }
                    } else {
                        StringBuilder sb = new StringBuilder();
                        resolver = resolver2;
                        sb.append("packageName:");
                        sb.append(record.mReceiver.mIdentity.mPackageName);
                        sb.append(" is freeze, can't start gps");
                        Log.i(TAG, sb.toString());
                    }
                    resolver2 = resolver;
                    z2 = true;
                } else {
                    resolver = resolver2;
                    backgroundThrottleInterval = backgroundThrottleInterval2;
                    z = true;
                }
                z2 = z;
                resolver2 = resolver;
                backgroundThrottleInterval2 = backgroundThrottleInterval;
            }
            long j = backgroundThrottleInterval2;
            if (providerRequest.reportLocation) {
                long thresholdInterval = ((providerRequest.interval + 1000) * 3) / 2;
                Iterator<UpdateRecord> it2 = records.iterator();
                while (it2.hasNext()) {
                    UpdateRecord record2 = it2.next();
                    if (isCurrentProfile(UserHandle.getUserId(record2.mReceiver.mIdentity.mUid))) {
                        LocationRequest locationRequest2 = record2.mRequest;
                        if (providerRequest.locationRequests.contains(locationRequest2) && locationRequest2.getInterval() <= thresholdInterval) {
                            if (record2.mReceiver.mWorkSource == null || !isValidWorkSource(record2.mReceiver.mWorkSource)) {
                                worksource.add(record2.mReceiver.mIdentity.mUid, record2.mReceiver.mIdentity.mPackageName);
                            } else {
                                worksource.add(record2.mReceiver.mWorkSource);
                            }
                        }
                    }
                }
            }
        } else {
            long j2 = backgroundThrottleInterval2;
            Log.i(TAG, "UpdateRecords is null.");
        }
        p.setRequest(providerRequest, worksource);
        Log.i(TAG, "provider request: " + str + " " + providerRequest + " mCHRFirstReqFlag:" + this.mCHRFirstReqFlag);
        if (this.mCHRFirstReqFlag) {
            this.mHwLocationGpsLogServices.netWorkLocation(str, providerRequest);
            this.mCHRFirstReqFlag = false;
            Log.i(TAG, "network chr update");
        }
    }

    private static boolean isValidWorkSource(WorkSource workSource) {
        boolean z = true;
        if (workSource.size() > 0) {
            if (workSource.getName(0) == null) {
                z = false;
            }
            return z;
        }
        ArrayList<WorkSource.WorkChain> workChains = workSource.getWorkChains();
        if (workChains == null || workChains.isEmpty() || workChains.get(0).getAttributionTag() == null) {
            z = false;
        }
        return z;
    }

    public String[] getBackgroundThrottlingWhitelist() {
        String[] strArr;
        synchronized (this.mLock) {
            strArr = (String[]) this.mBackgroundThrottlePackageWhitelist.toArray(new String[this.mBackgroundThrottlePackageWhitelist.size()]);
        }
        return strArr;
    }

    /* access modifiers changed from: private */
    public void updateBackgroundThrottlingWhitelistLocked() {
        String setting = Settings.Global.getString(this.mContext.getContentResolver(), "location_background_throttle_package_whitelist");
        if (setting == null) {
            setting = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        this.mBackgroundThrottlePackageWhitelist.clear();
        this.mBackgroundThrottlePackageWhitelist.addAll(SystemConfig.getInstance().getAllowUnthrottledLocation());
        this.mBackgroundThrottlePackageWhitelist.addAll(Arrays.asList(setting.split(",")));
        this.mBackgroundThrottlePackageWhitelist.addAll(getPackageWhiteList(1));
    }

    private boolean isThrottlingExemptLocked(Identity identity) {
        if (identity.mUid == 1000 || this.mBackgroundThrottlePackageWhitelist.contains(identity.mPackageName)) {
            return true;
        }
        Iterator<LocationProviderProxy> it = this.mProxyProviders.iterator();
        while (it.hasNext()) {
            if (identity.mPackageName.equals(it.next().getConnectedPackageName())) {
                return true;
            }
        }
        return false;
    }

    private Receiver getReceiverLocked(ILocationListener listener, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
        IBinder binder = listener.asBinder();
        Receiver receiver = this.mReceivers.get(binder);
        if (receiver == null) {
            Receiver receiver2 = new Receiver(listener, null, pid, uid, packageName, workSource, hideFromAppOps);
            try {
                receiver2.getListener().asBinder().linkToDeath(receiver2, 0);
                this.mReceivers.put(binder, receiver2);
                receiver = receiver2;
            } catch (RemoteException e) {
                Slog.e(TAG, "linkToDeath failed:", e);
                return null;
            }
        }
        return receiver;
    }

    private Receiver getReceiverLocked(PendingIntent intent, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
        PendingIntent pendingIntent = intent;
        Receiver receiver = this.mReceivers.get(pendingIntent);
        if (receiver != null) {
            return receiver;
        }
        Receiver receiver2 = new Receiver(null, pendingIntent, pid, uid, packageName, workSource, hideFromAppOps);
        Receiver receiver3 = receiver2;
        this.mReceivers.put(pendingIntent, receiver3);
        return receiver3;
    }

    private LocationRequest createSanitizedRequest(LocationRequest request, int resolutionLevel, boolean callerHasLocationHardwarePermission) {
        LocationRequest sanitizedRequest = new LocationRequest(request);
        if (!callerHasLocationHardwarePermission) {
            sanitizedRequest.setLowPowerMode(false);
        }
        if (resolutionLevel < 2) {
            int quality = sanitizedRequest.getQuality();
            if (quality == 100) {
                sanitizedRequest.setQuality(102);
            } else if (quality == 203) {
                sanitizedRequest.setQuality(201);
            }
            if (sanitizedRequest.getInterval() < 600000) {
                sanitizedRequest.setInterval(600000);
            }
            if (sanitizedRequest.getFastestInterval() < 600000) {
                sanitizedRequest.setFastestInterval(600000);
            }
        }
        if (sanitizedRequest.getFastestInterval() > sanitizedRequest.getInterval()) {
            request.setFastestInterval(request.getInterval());
        }
        return sanitizedRequest;
    }

    private void checkPackageName(String packageName) {
        if (packageName != null) {
            int uid = Binder.getCallingUid();
            String[] packages = this.mPackageManager.getPackagesForUid(uid);
            if (packages != null) {
                int length = packages.length;
                int i = 0;
                while (i < length) {
                    if (!packageName.equals(packages[i])) {
                        i++;
                    } else {
                        return;
                    }
                }
                throw new SecurityException("invalid package name: " + packageName);
            }
            throw new SecurityException("invalid UID " + uid);
        }
        throw new SecurityException("invalid package name: " + packageName);
    }

    private void checkPendingIntent(PendingIntent intent) {
        if (intent == null) {
            throw new IllegalArgumentException("invalid pending intent: " + intent);
        }
    }

    private Receiver checkListenerOrIntentLocked(ILocationListener listener, PendingIntent intent, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
        if (intent == null && listener == null) {
            throw new IllegalArgumentException("need either listener or intent");
        } else if (intent != null && listener != null) {
            throw new IllegalArgumentException("cannot register both listener and intent");
        } else if (intent == null) {
            return getReceiverLocked(listener, pid, uid, packageName, workSource, hideFromAppOps);
        } else {
            checkPendingIntent(intent);
            return getReceiverLocked(intent, pid, uid, packageName, workSource, hideFromAppOps);
        }
    }

    public void requestLocationUpdates(LocationRequest request, ILocationListener listener, PendingIntent intent, String packageName) {
        LocationRequest request2;
        long identity;
        Object obj;
        String str = packageName;
        hwSendBehavior(IHwBehaviorCollectManager.BehaviorId.LOCATIONMANAGER_REQUESTLOCATIONUPDATES);
        if (request == null) {
            request2 = DEFAULT_LOCATION_REQUEST;
        } else {
            request2 = request;
        }
        checkPackageName(str);
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel, request2.getProvider());
        WorkSource workSource = request2.getWorkSource();
        if (workSource != null && !workSource.isEmpty()) {
            checkDeviceStatsAllowed();
        }
        boolean hideFromAppOps = request2.getHideFromAppOps();
        if (hideFromAppOps) {
            checkUpdateAppOpsAllowed();
        }
        LocationRequest sanitizedRequest = createSanitizedRequest(request2, allowedResolutionLevel, this.mContext.checkCallingPermission("android.permission.LOCATION_HARDWARE") == 0);
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        HwSystemManager.allowOp(this.mContext, 8);
        long identity2 = Binder.clearCallingIdentity();
        try {
            boolean permission = checkLocationAccess(pid, uid, str, allowedResolutionLevel);
            try {
                Log.i(TAG, " PID: " + pid + " , uid : " + uid + " , packageName :" + str + " , allowedResolutionLevel : " + allowedResolutionLevel + " , permission : " + permission);
                this.mNetworkChrAllowed = true;
                if (!permission) {
                    try {
                        this.mNetworkChrAllowed = false;
                        this.mHwLocationGpsLogServices.permissionErr(str);
                    } catch (Throwable th) {
                        th = th;
                        boolean z = permission;
                        int i = uid;
                        int i2 = pid;
                        LocationRequest locationRequest = request2;
                        int i3 = allowedResolutionLevel;
                        identity = identity2;
                    }
                }
                Object obj2 = this.mLock;
                synchronized (obj2) {
                    try {
                        if (this.mHwQuickTTFFMonitor != null) {
                            try {
                                this.mHwQuickTTFFMonitor.setPermission(permission);
                            } catch (Throwable th2) {
                                th = th2;
                                obj = obj2;
                                boolean z2 = permission;
                                int i4 = uid;
                                int i5 = pid;
                                LocationRequest locationRequest2 = request2;
                                int i6 = allowedResolutionLevel;
                                identity = identity2;
                            }
                        }
                        obj = obj2;
                        boolean z3 = permission;
                        LocationRequest locationRequest3 = request2;
                        int i7 = allowedResolutionLevel;
                        identity = identity2;
                        int uid2 = uid;
                        int pid2 = pid;
                        try {
                            Receiver recevier = checkListenerOrIntentLocked(listener, intent, pid, uid, str, workSource, hideFromAppOps);
                            if (recevier == null) {
                                Log.e(TAG, "recevier creating failed, value is null");
                                Binder.restoreCallingIdentity(identity);
                                return;
                            }
                            hwRequestLocationUpdatesLocked(sanitizedRequest, recevier, pid2, uid2, str);
                            requestLocationUpdatesLocked(sanitizedRequest, recevier, pid2, uid2, str);
                            Binder.restoreCallingIdentity(identity);
                        } catch (Throwable th3) {
                            th = th3;
                            try {
                                throw th;
                            } catch (Throwable th4) {
                                th = th4;
                            }
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        obj = obj2;
                        boolean z4 = permission;
                        int i8 = uid;
                        int i9 = pid;
                        LocationRequest locationRequest4 = request2;
                        int i10 = allowedResolutionLevel;
                        identity = identity2;
                        throw th;
                    }
                }
            } catch (Throwable th6) {
                th = th6;
                boolean z5 = permission;
                int i11 = uid;
                int i12 = pid;
                LocationRequest locationRequest5 = request2;
                int i13 = allowedResolutionLevel;
                identity = identity2;
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        } catch (Throwable th7) {
            th = th7;
            int i14 = uid;
            int i15 = pid;
            LocationRequest locationRequest6 = request2;
            int i16 = allowedResolutionLevel;
            identity = identity2;
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    private void requestLocationUpdatesLocked(LocationRequest request, Receiver receiver, int pid, int uid, String packageName) {
        LocationRequest request2;
        Receiver receiver2 = receiver;
        int i = pid;
        int i2 = uid;
        String str = packageName;
        if (request == null) {
            request2 = DEFAULT_LOCATION_REQUEST;
        } else {
            request2 = request;
        }
        String name = request2.getProvider();
        if (name != null) {
            String name2 = getLocationProvider(i2, request2, str, name);
            if (this.mHwGpsActionReporter != null && ("gps".equals(name2) || "network".equals(name2) || "fused".equals(name2))) {
                String reportString = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + "PROVIDER:" + name2 + ",PN:" + str + ",HC:" + Integer.toHexString(System.identityHashCode(receiver));
                this.mLocationHandler.removeMessages(2, reportString);
                this.mLocationHandler.sendMessage(Message.obtain(this.mLocationHandler, 2, reportString));
            }
            Log.i(TAG, "request " + Integer.toHexString(System.identityHashCode(receiver)) + " " + name2 + " " + request2 + " from " + str + "(" + i2 + ")");
            String hashCode = Integer.toHexString(System.identityHashCode(receiver));
            this.mHwLocationGpsLogServices.updateApkName(name2, hashCode, str, APKSTART, String.valueOf(System.currentTimeMillis()));
            LocationProviderInterface provider = this.mProvidersByName.get(name2);
            if (provider != null) {
                UpdateRecord oldRecord = receiver2.mUpdateRecords.put(name2, new UpdateRecord(name2, request2, receiver2));
                boolean z = false;
                if (oldRecord != null) {
                    oldRecord.disposeLocked(false);
                }
                if ("gps".equals(name2)) {
                    String str2 = hashCode;
                    LogPower.push(202, str, Integer.toString(pid), Long.toString(request2.getInterval()), new String[]{Integer.toHexString(System.identityHashCode(receiver))});
                }
                this.mCHRFirstReqFlag = false;
                if (isAllowedByUserSettingsLocked(name2, i2, this.mCurrentUserId)) {
                    if (name2.equals("network")) {
                        if (provider instanceof IHwLocationProviderInterface) {
                            ((IHwLocationProviderInterface) provider).resetNLPFlag();
                        } else {
                            Log.d(TAG, "instanceof fail");
                        }
                    }
                    if (!isFreeze(str) && this.mNetworkChrAllowed) {
                        this.mCHRFirstReqFlag = true;
                    }
                    try {
                        Bundle requestBundle = new Bundle();
                        requestBundle.putLong(WatchlistLoggingHandler.WatchlistEventKeys.TIMESTAMP, System.currentTimeMillis());
                        requestBundle.putString(HwBroadcastRadarUtil.KEY_ACTION, "start");
                        requestBundle.putString("provider", name2);
                        requestBundle.putInt("interval", (int) request2.getInterval());
                        requestBundle.putString("pkgName", str);
                        requestBundle.putInt(HwBroadcastRadarUtil.KEY_RECEIVER, System.identityHashCode(receiver));
                        this.mHwLbsLogger.loggerEvent(101, requestBundle);
                    } catch (Exception e) {
                        Log.d(TAG, "exception occured when deliver start action");
                    }
                    HwSystemManager.notifyBackgroundMgr(str, i, i2, 2, 1);
                    this.mHwQuickTTFFMonitor.requestHwQuickTTFF(request2, str, name2, Integer.toHexString(System.identityHashCode(receiver)));
                    applyRequirementsLocked(name2);
                    printFormatLog(str, i, "requestLocationUpdatesLocked", name2);
                } else {
                    receiver2.callProviderEnabledLocked(name2, false);
                    this.mHwLocationGpsLogServices.setLocationSettingsOffErr(name2);
                    Log.i(TAG, "provider:" + name2 + " is disable");
                }
                if (this.mLocalLocationProvider != null && this.mLocalLocationProvider.isEnabled() && "gps".equals(name2)) {
                    this.mLocalLocationProvider.requestLocation();
                }
                receiver2.updateMonitoring(true);
                int quality = request2.getQuality();
                if (receiver2.mListener == null) {
                    z = true;
                }
                hwLocationPowerTrackerRecordRequest(str, quality, z);
                return;
            }
            throw new IllegalArgumentException("provider doesn't exist: " + name2);
        }
        throw new IllegalArgumentException("provider name must not be null");
    }

    public void removeUpdates(ILocationListener listener, PendingIntent intent, String packageName) {
        hwSendBehavior(IHwBehaviorCollectManager.BehaviorId.LOCATIONMANAGER_REMOVEUPDATES);
        String str = packageName;
        checkPackageName(str);
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        synchronized (this.mLock) {
            Receiver receiver = checkListenerOrIntentLocked(listener, intent, pid, uid, str, null, false);
            long identity = Binder.clearCallingIdentity();
            if (receiver != null) {
                try {
                    removeUpdatesLocked(receiver);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* access modifiers changed from: private */
    public void removeUpdatesLocked(Receiver receiver) {
        hwRemoveUpdatesLocked(receiver);
        Log.i(TAG, "remove " + Integer.toHexString(System.identityHashCode(receiver)));
        this.mHwLocationGpsLogServices.updateApkName("APKSTOPPROVIDER", Integer.toHexString(System.identityHashCode(receiver)), BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, APKSTOP, String.valueOf(System.currentTimeMillis()));
        if (this.mHwGpsActionReporter != null) {
            String reportString = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + "PROVIDER:all,PN:" + receiver.mIdentity.mPackageName + ",HC:" + Integer.toHexString(System.identityHashCode(receiver));
            this.mLocationHandler.removeMessages(3, reportString);
            this.mLocationHandler.sendMessage(Message.obtain(this.mLocationHandler, 3, reportString));
        }
        try {
            Bundle removeBundle = new Bundle();
            removeBundle.putLong(WatchlistLoggingHandler.WatchlistEventKeys.TIMESTAMP, System.currentTimeMillis());
            removeBundle.putString(HwBroadcastRadarUtil.KEY_ACTION, "stop");
            removeBundle.putString("pkgName", receiver.mIdentity.mPackageName);
            removeBundle.putInt(HwBroadcastRadarUtil.KEY_RECEIVER, System.identityHashCode(receiver));
            this.mHwLbsLogger.loggerEvent(101, removeBundle);
        } catch (Exception e) {
            Log.d(TAG, "exception occured when deliver stop session");
        }
        LogPower.push(203, receiver.mIdentity.mPackageName, Integer.toString(receiver.mIdentity.mPid), Integer.toString(-1), new String[]{Integer.toHexString(System.identityHashCode(receiver))});
        HwSystemManager.notifyBackgroundMgr(receiver.mIdentity.mPackageName, receiver.mIdentity.mPid, receiver.mIdentity.mUid, 2, 0);
        if (this.mReceivers.remove(receiver.mKey) != null && receiver.isListener()) {
            receiver.getListener().asBinder().unlinkToDeath(receiver, 0);
            synchronized (receiver) {
                receiver.clearPendingBroadcastsLocked();
            }
        }
        this.mHwQuickTTFFMonitor.removeHwQuickTTFF(receiver.mIdentity.mPackageName, Integer.toHexString(System.identityHashCode(receiver)), receiver.mUpdateRecords.containsKey("gps"));
        receiver.updateMonitoring(false);
        hwLocationPowerTrackerRemoveRequest(receiver.mIdentity.mPackageName);
        HashSet<String> providers = new HashSet<>();
        HashMap<String, UpdateRecord> oldRecords = receiver.mUpdateRecords;
        if (oldRecords != null) {
            for (UpdateRecord record : oldRecords.values()) {
                record.disposeLocked(false);
            }
            providers.addAll(oldRecords.keySet());
        }
        Iterator<String> it = providers.iterator();
        while (it.hasNext()) {
            String provider = it.next();
            Log.i(TAG, "isAllowedByCurrentUserSettingsLocked started: " + provider);
            if (isAllowedByCurrentUserSettingsLocked(provider)) {
                applyRequirementsLocked(provider);
            }
        }
    }

    /* access modifiers changed from: private */
    public void applyAllProviderRequirementsLocked() {
        Iterator<LocationProviderInterface> it = this.mProviders.iterator();
        while (it.hasNext()) {
            LocationProviderInterface p = it.next();
            if (isAllowedByCurrentUserSettingsLocked(p.getName())) {
                applyRequirementsLocked(p.getName());
            }
        }
    }

    public Location getLastLocation(LocationRequest request, String packageName) {
        Location location;
        hwSendBehavior(IHwBehaviorCollectManager.BehaviorId.LOCATIONMANAGER_GETLASTLOCATION);
        Log.i(TAG, "getLastLocation: " + request);
        if (request == null) {
            request = DEFAULT_LOCATION_REQUEST;
        }
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkPackageName(packageName);
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel, request.getProvider());
        HwSystemManager.allowOp(this.mContext, 8);
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        try {
            if (this.mBlacklist.isBlacklisted(packageName)) {
                Log.i(TAG, "not returning last loc for blacklisted app: " + packageName);
                Binder.restoreCallingIdentity(identity);
                return null;
            } else if (!reportLocationAccessNoThrow(pid, uid, packageName, allowedResolutionLevel)) {
                Log.i(TAG, "not returning last loc for no op app: " + packageName);
                Binder.restoreCallingIdentity(identity);
                return null;
            } else {
                synchronized (this.mLock) {
                    String name = request.getProvider();
                    if (name == null) {
                        name = "fused";
                    }
                    if (this.mProvidersByName.get(name) == null) {
                        Binder.restoreCallingIdentity(identity);
                        return null;
                    } else if (isAllowedByUserSettingsLocked(name, uid, this.mCurrentUserId) || isExceptionAppForUserSettingsLocked(packageName)) {
                        if (allowedResolutionLevel < 2) {
                            location = this.mLastLocationCoarseInterval.get(name);
                        } else {
                            location = this.mLastLocation.get(name);
                        }
                        if (location == null) {
                            if (isExceptionAppForUserSettingsLocked(packageName)) {
                                Location readLastLocationDataBase = readLastLocationDataBase();
                                Binder.restoreCallingIdentity(identity);
                                return readLastLocationDataBase;
                            }
                            Binder.restoreCallingIdentity(identity);
                            return null;
                        } else if (allowedResolutionLevel < 2) {
                            Location noGPSLocation = location.getExtraLocation("noGPSLocation");
                            if (noGPSLocation != null) {
                                Location location2 = new Location(this.mLocationFudger.getOrCreate(noGPSLocation));
                                Binder.restoreCallingIdentity(identity);
                                return location2;
                            }
                            Binder.restoreCallingIdentity(identity);
                            return null;
                        } else {
                            Location location3 = new Location(location);
                            Binder.restoreCallingIdentity(identity);
                            return location3;
                        }
                    } else {
                        Binder.restoreCallingIdentity(identity);
                        return null;
                    }
                }
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    public boolean injectLocation(Location location) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to inject location");
        this.mContext.enforceCallingPermission("android.permission.ACCESS_FINE_LOCATION", "Access Fine Location permission not granted to inject Location");
        if (location == null) {
            return false;
        }
        LocationProviderInterface p = null;
        String provider = location.getProvider();
        if (provider != null) {
            p = this.mProvidersByName.get(provider);
        }
        if (p == null) {
            return false;
        }
        synchronized (this.mLock) {
            if (!isAllowedByCurrentUserSettingsLocked(provider)) {
                return false;
            }
            if (this.mLastLocation.get(provider) != null) {
                return false;
            }
            updateLastLocationLocked(location, provider);
            return true;
        }
    }

    public void requestGeofence(LocationRequest request, Geofence geofence, PendingIntent intent, String packageName) {
        LocationRequest request2;
        long identity;
        PendingIntent pendingIntent = intent;
        hwSendBehavior(IHwBehaviorCollectManager.BehaviorId.LOCATIONMANAGER_REQUESTGEOFENCE);
        if (request == null) {
            request2 = DEFAULT_LOCATION_REQUEST;
        } else {
            request2 = request;
        }
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForGeofenceUse(allowedResolutionLevel);
        checkPendingIntent(pendingIntent);
        String str = packageName;
        checkPackageName(str);
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel, request2.getProvider());
        boolean callerHasLocationHardwarePermission = this.mContext.checkCallingPermission("android.permission.LOCATION_HARDWARE") == 0;
        LocationRequest sanitizedRequest = createSanitizedRequest(request2, allowedResolutionLevel, callerHasLocationHardwarePermission);
        StringBuilder sb = new StringBuilder();
        sb.append("requestGeofence: ");
        sb.append(sanitizedRequest);
        sb.append(" ");
        Geofence geofence2 = geofence;
        sb.append(geofence2);
        sb.append(" ");
        sb.append(pendingIntent);
        Log.i(TAG, sb.toString());
        int uid = Binder.getCallingUid();
        if (UserHandle.getUserId(uid) != 0) {
            Log.w(TAG, "proximity alerts are currently available only to the primary user");
            return;
        }
        Geofence geofence3 = geofence2;
        LocationRequest locationRequest = sanitizedRequest;
        int i = uid;
        long identity2 = Binder.clearCallingIdentity();
        try {
            if (!addQcmGeoFencer(geofence3, locationRequest, i, pendingIntent, str)) {
                try {
                    LocationRequest locationRequest2 = sanitizedRequest;
                    boolean z = callerHasLocationHardwarePermission;
                    int i2 = allowedResolutionLevel;
                    LocationRequest locationRequest3 = request2;
                    try {
                        this.mGeofenceManager.addFence(sanitizedRequest, geofence, pendingIntent, allowedResolutionLevel, uid, packageName);
                    } catch (Throwable th) {
                        th = th;
                        identity = identity2;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    int i3 = uid;
                    LocationRequest locationRequest4 = sanitizedRequest;
                    boolean z2 = callerHasLocationHardwarePermission;
                    int i4 = allowedResolutionLevel;
                    LocationRequest locationRequest5 = request2;
                    identity = identity2;
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            } else {
                LocationRequest locationRequest6 = sanitizedRequest;
                boolean z3 = callerHasLocationHardwarePermission;
                int i5 = allowedResolutionLevel;
                LocationRequest locationRequest7 = request2;
            }
            Binder.restoreCallingIdentity(identity2);
        } catch (Throwable th3) {
            th = th3;
            int i6 = uid;
            LocationRequest locationRequest8 = sanitizedRequest;
            boolean z4 = callerHasLocationHardwarePermission;
            int i7 = allowedResolutionLevel;
            LocationRequest locationRequest9 = request2;
            identity = identity2;
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    public void removeGeofence(Geofence geofence, PendingIntent intent, String packageName) {
        checkPendingIntent(intent);
        checkPackageName(packageName);
        Log.i(TAG, "removeGeofence: " + geofence + " " + intent);
        long identity = Binder.clearCallingIdentity();
        try {
            if (!removeQcmGeoFencer(intent)) {
                this.mGeofenceManager.removeFence(geofence, intent);
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public boolean registerGnssStatusCallback(IGnssStatusListener callback, String packageName) {
        hwSendBehavior(IHwBehaviorCollectManager.BehaviorId.LOCATIONMANAGER_REGISTERGNSSSTATUSCALLBACK);
        if (!hasGnssPermissions(packageName) || this.mGnssStatusProvider == null) {
            return false;
        }
        try {
            this.mGnssStatusProvider.registerGnssStatusCallback(callback);
            return true;
        } catch (RemoteException e) {
            Slog.e(TAG, "mGpsStatusProvider.registerGnssStatusCallback failed", e);
            return false;
        }
    }

    public void unregisterGnssStatusCallback(IGnssStatusListener callback) {
        synchronized (this.mLock) {
            try {
                this.mGnssStatusProvider.unregisterGnssStatusCallback(callback);
            } catch (Exception e) {
                Slog.e(TAG, "mGpsStatusProvider.unregisterGnssStatusCallback failed", e);
            }
        }
    }

    public boolean addGnssMeasurementsListener(IGnssMeasurementsListener listener, String packageName) {
        if (!hasGnssPermissions(packageName) || this.mGnssMeasurementsProvider == null) {
            return false;
        }
        synchronized (this.mLock) {
            Identity callerIdentity = new Identity(Binder.getCallingUid(), Binder.getCallingPid(), packageName);
            this.mGnssMeasurementsListeners.put(listener.asBinder(), callerIdentity);
            long identity = Binder.clearCallingIdentity();
            try {
                if ((isThrottlingExemptLocked(callerIdentity) || isImportanceForeground(this.mActivityManager.getPackageImportance(packageName))) && !isFreeze(packageName)) {
                    Log.i(TAG, "addGnssMeasurementsListener " + Integer.toHexString(System.identityHashCode(listener)) + " packageName " + packageName);
                    boolean addListener = this.mGnssMeasurementsProvider.addListener(listener, packageName);
                    return addListener;
                }
                Binder.restoreCallingIdentity(identity);
                return true;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void removeGnssMeasurementsListener(IGnssMeasurementsListener listener) {
        if (this.mGnssMeasurementsProvider != null) {
            Log.i(TAG, "removeGnssMeasurementsListener " + Integer.toHexString(System.identityHashCode(listener)));
            synchronized (this.mLock) {
                this.mGnssMeasurementsListeners.remove(listener.asBinder());
                this.mGnssMeasurementsProvider.removeListener(listener);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean addGnssNavigationMessageListener(IGnssNavigationMessageListener listener, String packageName) {
        if (!hasGnssPermissions(packageName) || this.mGnssNavigationMessageProvider == null) {
            return false;
        }
        synchronized (this.mLock) {
            Identity callerIdentity = new Identity(Binder.getCallingUid(), Binder.getCallingPid(), packageName);
            this.mGnssNavigationMessageListeners.put(listener.asBinder(), callerIdentity);
            long identity = Binder.clearCallingIdentity();
            try {
                if (isThrottlingExemptLocked(callerIdentity) || isImportanceForeground(this.mActivityManager.getPackageImportance(packageName))) {
                    boolean addListener = this.mGnssNavigationMessageProvider.addListener(listener, packageName);
                    Binder.restoreCallingIdentity(identity);
                    return addListener;
                }
                Binder.restoreCallingIdentity(identity);
                return true;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
    }

    public void removeGnssNavigationMessageListener(IGnssNavigationMessageListener listener) {
        if (this.mGnssNavigationMessageProvider != null) {
            synchronized (this.mLock) {
                this.mGnssNavigationMessageListeners.remove(listener.asBinder());
                this.mGnssNavigationMessageProvider.removeListener(listener);
            }
        }
    }

    public boolean sendExtraCommand(String provider, String command, Bundle extras) {
        hwSendBehavior(IHwBehaviorCollectManager.BehaviorId.LOCATIONMANAGER_SENDEXTRACOMMAND);
        if (provider != null) {
            checkResolutionLevelIsSufficientForProviderUse(getCallerAllowedResolutionLevel(), provider);
            if (this.mContext.checkCallingOrSelfPermission(ACCESS_LOCATION_EXTRA_COMMANDS) == 0) {
                HwSystemManager.allowOp(this.mContext, 8);
                synchronized (this.mLock) {
                    LocationProviderInterface p = this.mProvidersByName.get(provider);
                    if (p == null) {
                        return false;
                    }
                    boolean sendExtraCommand = p.sendExtraCommand(command, extras);
                    return sendExtraCommand;
                }
            }
            throw new SecurityException("Requires ACCESS_LOCATION_EXTRA_COMMANDS permission");
        }
        throw new NullPointerException();
    }

    public boolean sendNiResponse(int notifId, int userResponse) {
        if (Binder.getCallingUid() == Process.myUid()) {
            try {
                return this.mNetInitiatedListener.sendNiResponse(notifId, userResponse);
            } catch (RemoteException e) {
                Slog.e(TAG, "RemoteException in LocationManagerService.sendNiResponse");
                return false;
            }
        } else {
            throw new SecurityException("calling sendNiResponse from outside of the system is not allowed");
        }
    }

    public ProviderProperties getProviderProperties(String provider) {
        LocationProviderInterface p;
        if (this.mProvidersByName.get(provider) == null) {
            return null;
        }
        checkResolutionLevelIsSufficientForProviderUse(getCallerAllowedResolutionLevel(), provider);
        synchronized (this.mLock) {
            p = this.mProvidersByName.get(provider);
        }
        if (p == null) {
            return null;
        }
        return p.getProperties();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001f, code lost:
        if ((r1 instanceof com.android.server.location.LocationProviderProxy) == false) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0021, code lost:
        r0 = ((com.android.server.location.LocationProviderProxy) r1).getConnectedPackageName();
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0029, code lost:
        if (r0 == null) goto L_0x0038;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002b, code lost:
        r3 = r0.split(";");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0033, code lost:
        if (r3.length < 2) goto L_0x0038;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0035, code lost:
        r2 = r3[0];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0038, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0039, code lost:
        return null;
     */
    public String getNetworkProviderPackage() {
        synchronized (this.mLock) {
            if (this.mProvidersByName.get("network") == null) {
                return null;
            }
            LocationProviderInterface p = this.mProvidersByName.get("network");
        }
    }

    public boolean isLocationEnabledForUser(int userId) {
        HashMap<String, LocationProviderInterface> realProviders;
        checkInteractAcrossUsersPermission(userId);
        long identity = Binder.clearCallingIdentity();
        try {
            String allowedProviders = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "location_providers_allowed", userId);
            if (allowedProviders == null) {
                Binder.restoreCallingIdentity(identity);
                return false;
            }
            List<String> providerList = Arrays.asList(allowedProviders.split(","));
            synchronized (this.mRealProviders) {
                realProviders = (HashMap) this.mRealProviders.clone();
            }
            for (String provider : realProviders.keySet()) {
                if (!provider.equals("passive")) {
                    if (!provider.equals("fused")) {
                        if (providerList.contains(provider)) {
                            Binder.restoreCallingIdentity(identity);
                            return true;
                        }
                    }
                }
            }
            Binder.restoreCallingIdentity(identity);
            return false;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    public void setLocationEnabledForUser(boolean enabled, int userId) {
        this.mContext.enforceCallingPermission("android.permission.WRITE_SECURE_SETTINGS", "Requires WRITE_SECURE_SETTINGS permission");
        checkInteractAcrossUsersPermission(userId);
        long identity = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                Set<String> allRealProviders = this.mRealProviders.keySet();
                Set<String> allProvidersSet = new ArraySet<>(allRealProviders.size() + 2);
                allProvidersSet.addAll(allRealProviders);
                if (!enabled) {
                    allProvidersSet.add("gps");
                    allProvidersSet.add("network");
                }
                if (allProvidersSet.isEmpty()) {
                    Binder.restoreCallingIdentity(identity);
                    return;
                }
                String prefix = enabled ? "+" : "-";
                StringBuilder locationProvidersAllowed = new StringBuilder();
                for (String provider : allProvidersSet) {
                    if (!provider.equals("passive") && !provider.equals("fused")) {
                        if (!provider.equals(IHwLocalLocationProvider.LOCAL_PROVIDER)) {
                            locationProvidersAllowed.append(prefix);
                            locationProvidersAllowed.append(provider);
                            locationProvidersAllowed.append(",");
                        }
                    }
                }
                locationProvidersAllowed.setLength(locationProvidersAllowed.length() - 1);
                Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "location_providers_allowed", locationProvidersAllowed.toString(), userId);
                Binder.restoreCallingIdentity(identity);
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    public boolean isProviderEnabledForUser(String provider, int userId) {
        checkInteractAcrossUsersPermission(userId);
        boolean z = false;
        if ("fused".equals(provider)) {
            return false;
        }
        int uid = Binder.getCallingUid();
        synchronized (this.mLock) {
            if (this.mProvidersByName.get(provider) != null && isAllowedByUserSettingsLocked(provider, uid, userId)) {
                z = true;
            }
        }
        return z;
    }

    public boolean setProviderEnabledForUser(String provider, boolean enabled, int userId) {
        this.mContext.enforceCallingPermission("android.permission.WRITE_SECURE_SETTINGS", "Requires WRITE_SECURE_SETTINGS permission");
        checkInteractAcrossUsersPermission(userId);
        if ("fused".equals(provider)) {
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                if (!this.mProvidersByName.containsKey(provider)) {
                    Binder.restoreCallingIdentity(identity);
                    return false;
                } else if (this.mMockProviders.containsKey(provider)) {
                    setTestProviderEnabled(provider, enabled);
                    Binder.restoreCallingIdentity(identity);
                    return true;
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(enabled ? "+" : "-");
                    sb.append(provider);
                    boolean putStringForUser = Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "location_providers_allowed", sb.toString(), userId);
                    Binder.restoreCallingIdentity(identity);
                    return putStringForUser;
                }
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    private boolean isLocationProviderEnabledForUser(String provider, int userId) {
        long identity = Binder.clearCallingIdentity();
        try {
            return TextUtils.delimitedStringContains(Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "location_providers_allowed", userId), ',', provider);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void checkInteractAcrossUsersPermission(int userId) {
        int uid = Binder.getCallingUid();
        if (UserHandle.getUserId(uid) != userId && ActivityManager.checkComponentPermission("android.permission.INTERACT_ACROSS_USERS", uid, -1, true) != 0) {
            throw new SecurityException("Requires INTERACT_ACROSS_USERS permission");
        }
    }

    private boolean isUidALocationProvider(int uid) {
        if (uid == 1000) {
            return true;
        }
        if (this.mGeocodeProvider != null && doesUidHavePackage(uid, this.mGeocodeProvider.getConnectedPackageName())) {
            return true;
        }
        Iterator<LocationProviderProxy> it = this.mProxyProviders.iterator();
        while (it.hasNext()) {
            if (doesUidHavePackage(uid, it.next().getConnectedPackageName())) {
                return true;
            }
        }
        return false;
    }

    private void checkCallerIsProvider() {
        if (this.mContext.checkCallingOrSelfPermission(INSTALL_LOCATION_PROVIDER) != 0 && !isUidALocationProvider(Binder.getCallingUid())) {
            throw new SecurityException("need INSTALL_LOCATION_PROVIDER permission, or UID of a currently bound location provider");
        }
    }

    private boolean doesUidHavePackage(int uid, String packageName) {
        if (packageName == null) {
            return false;
        }
        String[] packageNames = this.mPackageManager.getPackagesForUid(uid);
        if (packageNames == null) {
            return false;
        }
        String[] pNames = packageName.split(";");
        if (pNames.length >= 2) {
            for (String pName : pNames) {
                for (String name : packageNames) {
                    if (pName.equals(name)) {
                        return true;
                    }
                }
            }
        } else {
            for (String name2 : packageNames) {
                if (packageName.equals(name2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void reportLocation(Location location, boolean passive) {
        checkCallerIsProvider();
        if (!this.mHwQuickTTFFMonitor.isReport(location)) {
            Log.d(TAG, "QuickTTFFMonitor is not running return");
            return;
        }
        this.mHwQuickTTFFMonitor.setQuickTTFFLocation(location, passive);
        if (!location.isComplete()) {
            Log.w(TAG, "Dropping incomplete location: " + location);
            return;
        }
        HwSystemManager.allowOp(this.mContext, 8);
        String provider = passive ? "passive" : location.getProvider();
        if (provider.equals("network")) {
            LocationProviderInterface p = this.mProvidersByName.get(provider);
            if (p == null || !(p instanceof IHwLocationProviderInterface)) {
                Log.d(TAG, "instanceof fail");
            } else if (!((IHwLocationProviderInterface) p).reportNLPLocation(Binder.getCallingPid())) {
                return;
            }
        }
        this.mLocationHandler.removeMessages(1, location);
        Message m = Message.obtain(this.mLocationHandler, 1, location);
        m.arg1 = passive;
        this.mLocationHandler.sendMessageAtFrontOfQueue(m);
    }

    private static boolean shouldBroadcastSafe(Location loc, Location lastLoc, UpdateRecord record, long now) {
        boolean z = true;
        if (lastLoc == null) {
            return true;
        }
        if ((loc.getElapsedRealtimeNanos() - lastLoc.getElapsedRealtimeNanos()) / NANOS_PER_MILLI < record.mRealRequest.getFastestInterval() - 100) {
            return false;
        }
        double minDistance = (double) record.mRealRequest.getSmallestDisplacement();
        if ((minDistance > 0.0d && ((double) loc.distanceTo(lastLoc)) <= minDistance) || record.mRealRequest.getNumUpdates() <= 0) {
            return false;
        }
        if (record.mRealRequest.getExpireAt() < now) {
            z = false;
        }
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:102:0x032e  */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x0343  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x0310  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x0320  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0327  */
    private void handleLocationChangedLocked(Location location, boolean passive) {
        boolean z;
        Location coarseLocation;
        ArrayList<UpdateRecord> deadUpdateRecords;
        ArrayList<Receiver> deadReceivers;
        ArrayList<UpdateRecord> records;
        Location notifyLocation;
        Bundle extras;
        ArrayList<Receiver> deadReceivers2;
        Integer stat;
        Location location2 = location;
        Log.i(TAG, "incoming location: " + location2 + "; passive:" + z + "; quickttff:" + this.mHwQuickTTFFMonitor.isQuickLocation(location2));
        long now = SystemClock.elapsedRealtime();
        String provider = z ? "passive" : location.getProvider();
        this.mHwQuickTTFFMonitor.removeAllHwQuickTTFF(provider);
        long nowNanos = SystemClock.elapsedRealtimeNanos();
        if (location.getProvider() != "gps" || !this.mHwQuickTTFFMonitor.isQuickLocation(location2)) {
            this.mHwLocationGpsLogServices.updateLocation(location2, nowNanos, provider);
        } else {
            this.mHwLocationGpsLogServices.updateLocation(location2, nowNanos, "quickgps");
        }
        updateLocalLocationDB(location2, provider);
        LocationProviderInterface p = this.mProvidersByName.get(provider);
        if (p != null) {
            updateLastLocationLocked(location2, provider);
            Location lastLocation = this.mLastLocation.get(provider);
            if (lastLocation == null) {
                Log.e(TAG, "handleLocationChangedLocked() updateLastLocation failed");
                return;
            }
            Location lastLocationCoarseInterval = this.mLastLocationCoarseInterval.get(provider);
            if (lastLocationCoarseInterval == null) {
                lastLocationCoarseInterval = new Location(location2);
                this.mLastLocationCoarseInterval.put(provider, lastLocationCoarseInterval);
            }
            Location lastLocationCoarseInterval2 = lastLocationCoarseInterval;
            long timeDiffNanos = location.getElapsedRealtimeNanos() - lastLocationCoarseInterval2.getElapsedRealtimeNanos();
            if (timeDiffNanos > 600000000000L) {
                lastLocationCoarseInterval2.set(location2);
            }
            Location noGPSLocation = lastLocationCoarseInterval2.getExtraLocation("noGPSLocation");
            ArrayList<UpdateRecord> records2 = this.mRecordsByProvider.get(provider);
            if (records2 == null) {
                LocationProviderInterface locationProviderInterface = p;
                Location location3 = lastLocation;
                Location location4 = lastLocationCoarseInterval2;
                long j = timeDiffNanos;
                Location location5 = noGPSLocation;
                ArrayList<UpdateRecord> arrayList = records2;
            } else if (records2.size() == 0) {
                long j2 = nowNanos;
                LocationProviderInterface locationProviderInterface2 = p;
                Location location6 = lastLocation;
                Location location7 = lastLocationCoarseInterval2;
                long j3 = timeDiffNanos;
                Location location8 = noGPSLocation;
                ArrayList<UpdateRecord> arrayList2 = records2;
            } else {
                if (noGPSLocation != null) {
                    coarseLocation = this.mLocationFudger.getOrCreate(noGPSLocation);
                } else {
                    coarseLocation = null;
                }
                long j4 = nowNanos;
                long nowNanos2 = p.getStatusUpdateTime();
                Location lastLocation2 = lastLocation;
                Bundle extras2 = new Bundle();
                int status = p.getStatus(extras2);
                if (provider.equals("network")) {
                    ArrayList<Integer> statusList = extras2.getIntegerArrayList("status");
                    if (statusList != null) {
                        LocationProviderInterface locationProviderInterface3 = p;
                        Iterator<Integer> it = statusList.iterator();
                        while (it.hasNext()) {
                            ArrayList<Integer> statusList2 = statusList;
                            Log.d(TAG, "list network LocationChanged,  NLP status: " + stat + " , provider : " + provider);
                            this.mHwLocationGpsLogServices.updateNLPStatus(stat.intValue());
                            statusList = statusList2;
                            it = it;
                            lastLocationCoarseInterval2 = lastLocationCoarseInterval2;
                            timeDiffNanos = timeDiffNanos;
                        }
                        Location location9 = lastLocationCoarseInterval2;
                        long j5 = timeDiffNanos;
                    } else {
                        LocationProviderInterface locationProviderInterface4 = p;
                        Location location10 = lastLocationCoarseInterval2;
                        long j6 = timeDiffNanos;
                        Log.d(TAG, " network LocationChanged,  NLP status: " + status + " , provider : " + provider);
                        this.mHwLocationGpsLogServices.updateNLPStatus(status);
                    }
                } else {
                    Location location11 = lastLocationCoarseInterval2;
                    long j7 = timeDiffNanos;
                }
                ArrayList<UpdateRecord> deadUpdateRecords2 = null;
                ArrayList<Integer> calledReceivers = new ArrayList<>();
                Iterator<UpdateRecord> it2 = records2.iterator();
                ArrayList<Receiver> deadReceivers3 = null;
                while (it2.hasNext()) {
                    UpdateRecord r = it2.next();
                    Iterator<UpdateRecord> it3 = it2;
                    Receiver receiver = r.mReceiver;
                    boolean receiverDead = false;
                    Location noGPSLocation2 = noGPSLocation;
                    int receiverUserId = UserHandle.getUserId(receiver.mIdentity.mUid);
                    if (!isCurrentProfile(receiverUserId)) {
                        int i = receiverUserId;
                        if (!isUidALocationProvider(receiver.mIdentity.mUid)) {
                            deadUpdateRecords = deadUpdateRecords2;
                            deadReceivers = deadReceivers3;
                            records = records2;
                            it2 = it3;
                            noGPSLocation = noGPSLocation2;
                            records2 = records;
                            deadReceivers3 = deadReceivers;
                            deadUpdateRecords2 = deadUpdateRecords;
                        }
                    }
                    records = records2;
                    if (this.mBlacklist.isBlacklisted(receiver.mIdentity.mPackageName)) {
                        deadUpdateRecords = deadUpdateRecords2;
                        deadReceivers = deadReceivers3;
                    } else {
                        deadReceivers = deadReceivers3;
                        deadUpdateRecords = deadUpdateRecords2;
                        if (!reportLocationAccessNoThrow(receiver.mIdentity.mPid, receiver.mIdentity.mUid, receiver.mIdentity.mPackageName, receiver.mAllowedResolutionLevel)) {
                            Log.i(TAG, "skipping loc update for no op app: " + receiver.mIdentity.mPackageName);
                        } else if (!this.mHwQuickTTFFMonitor.isLocationReportToApp(receiver.mIdentity.mPackageName, provider, location2)) {
                            Log.i(TAG, "skipping qiuckttff loc update for  app: " + receiver.mIdentity.mPackageName);
                        } else {
                            if (receiver.mAllowedResolutionLevel < 2) {
                                notifyLocation = coarseLocation;
                            } else {
                                notifyLocation = lastLocation2;
                            }
                            if (notifyLocation != null) {
                                Location lastLoc = r.mLastFixBroadcast;
                                if (lastLoc == null || shouldBroadcastSafe(notifyLocation, lastLoc, r, now)) {
                                    if (lastLoc == null) {
                                        lastLoc = new Location(notifyLocation);
                                        r.mLastFixBroadcast = lastLoc;
                                    } else {
                                        lastLoc.set(notifyLocation);
                                    }
                                    calledReceivers.add(Integer.valueOf(System.identityHashCode(receiver)));
                                    if (!receiver.callLocationChangedLocked(notifyLocation)) {
                                        StringBuilder sb = new StringBuilder();
                                        Location location12 = notifyLocation;
                                        sb.append("RemoteException calling onLocationChanged on ");
                                        sb.append(receiver);
                                        Slog.w(TAG, sb.toString());
                                        receiverDead = true;
                                    }
                                    r.mRealRequest.decrementNumUpdates();
                                    Location location13 = lastLoc;
                                    printFormatLog(receiver.mIdentity.mPackageName, receiver.mIdentity.mPid, "handleLocationChangedLocked", "report_location");
                                } else {
                                    Location location14 = notifyLocation;
                                }
                            }
                            long prevStatusUpdateTime = r.mLastStatusBroadcast;
                            if (nowNanos2 > prevStatusUpdateTime) {
                                if (prevStatusUpdateTime == 0 && status == 2) {
                                    extras = extras2;
                                    if (r.mRealRequest.getNumUpdates() > 0) {
                                    }
                                    if (deadUpdateRecords == null) {
                                    }
                                    deadUpdateRecords2.add(r);
                                    if (receiverDead) {
                                    }
                                    it2 = it3;
                                    noGPSLocation = noGPSLocation2;
                                    records2 = records;
                                    extras2 = extras;
                                } else {
                                    r.mLastStatusBroadcast = nowNanos2;
                                    if (!receiver.callStatusChangedLocked(provider, status, extras2)) {
                                        receiverDead = true;
                                        StringBuilder sb2 = new StringBuilder();
                                        extras = extras2;
                                        sb2.append("RemoteException calling onStatusChanged on ");
                                        sb2.append(receiver);
                                        Slog.w(TAG, sb2.toString());
                                        if (r.mRealRequest.getNumUpdates() > 0 || r.mRealRequest.getExpireAt() < now) {
                                            if (deadUpdateRecords == null) {
                                                deadUpdateRecords2 = new ArrayList<>();
                                            } else {
                                                deadUpdateRecords2 = deadUpdateRecords;
                                            }
                                            deadUpdateRecords2.add(r);
                                        } else {
                                            deadUpdateRecords2 = deadUpdateRecords;
                                        }
                                        if (receiverDead) {
                                            if (deadReceivers == null) {
                                                deadReceivers2 = new ArrayList<>();
                                            } else {
                                                deadReceivers2 = deadReceivers;
                                            }
                                            if (!deadReceivers2.contains(receiver)) {
                                                deadReceivers2.add(receiver);
                                            }
                                            deadReceivers3 = deadReceivers2;
                                        } else {
                                            deadReceivers3 = deadReceivers;
                                        }
                                        it2 = it3;
                                        noGPSLocation = noGPSLocation2;
                                        records2 = records;
                                        extras2 = extras;
                                    }
                                }
                            }
                            extras = extras2;
                            if (r.mRealRequest.getNumUpdates() > 0) {
                            }
                            if (deadUpdateRecords == null) {
                            }
                            deadUpdateRecords2.add(r);
                            if (receiverDead) {
                            }
                            it2 = it3;
                            noGPSLocation = noGPSLocation2;
                            records2 = records;
                            extras2 = extras;
                        }
                    }
                    it2 = it3;
                    noGPSLocation = noGPSLocation2;
                    records2 = records;
                    deadReceivers3 = deadReceivers;
                    deadUpdateRecords2 = deadUpdateRecords;
                }
                ArrayList<UpdateRecord> deadUpdateRecords3 = deadUpdateRecords2;
                ArrayList<Receiver> deadReceivers4 = deadReceivers3;
                Location location15 = noGPSLocation;
                ArrayList<UpdateRecord> arrayList3 = records2;
                try {
                    Log.d(TAG, "start deliver location");
                    Bundle locationBundle = new Bundle();
                    locationBundle.putParcelable("location", new Location(location2));
                    locationBundle.putLong(WatchlistLoggingHandler.WatchlistEventKeys.TIMESTAMP, System.currentTimeMillis());
                    locationBundle.putIntegerArrayList("receivers", calledReceivers);
                    this.mHwLbsLogger.loggerEvent(111, locationBundle);
                } catch (Exception e) {
                    Log.d(TAG, "exception occured when deliver location");
                }
                if (deadReceivers4 != null) {
                    Iterator<Receiver> it4 = deadReceivers4.iterator();
                    while (it4.hasNext()) {
                        removeUpdatesLocked(it4.next());
                    }
                }
                if (deadUpdateRecords3 != null) {
                    Iterator<UpdateRecord> it5 = deadUpdateRecords3.iterator();
                    while (it5.hasNext()) {
                        it5.next().disposeLocked(true);
                    }
                    applyRequirementsLocked(provider);
                }
            }
        }
    }

    private void updateLastLocationLocked(Location location, String provider) {
        Location noGPSLocation = location.getExtraLocation("noGPSLocation");
        Location lastLocation = this.mLastLocation.get(provider);
        if (lastLocation == null) {
            lastLocation = new Location(provider);
            this.mLastLocation.put(provider, lastLocation);
        } else {
            Location lastNoGPSLocation = lastLocation.getExtraLocation("noGPSLocation");
            if (noGPSLocation == null && lastNoGPSLocation != null) {
                location.setExtraLocation("noGPSLocation", lastNoGPSLocation);
            }
        }
        lastLocation.set(location);
    }

    private boolean isMockProvider(String provider) {
        boolean containsKey;
        synchronized (this.mLock) {
            containsKey = this.mMockProviders.containsKey(provider);
        }
        return containsKey;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0046, code lost:
        return;
     */
    public void handleLocationChanged(Location location, boolean passive) {
        Location myLocation = new Location(location);
        String provider = myLocation.getProvider();
        if (!myLocation.isFromMockProvider() && isMockProvider(provider)) {
            myLocation.setIsFromMockProvider(true);
        }
        synchronized (this.mLock) {
            if (isAllowedByCurrentUserSettingsLocked(provider)) {
                if (!passive) {
                    Location location2 = screenLocationLocked(location, provider);
                    if (location2 != null) {
                        if (!HwDeviceManager.disallowOp(101) && !this.mHwQuickTTFFMonitor.isQuickLocation(location2)) {
                            this.mPassiveProvider.updateLocation(myLocation);
                        }
                    } else {
                        return;
                    }
                }
                handleLocationChangedLocked(myLocation, passive);
            }
        }
    }

    public boolean geocoderIsPresent() {
        return this.mGeocodeProvider != null;
    }

    public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        if (this.mGeocodeProvider != null) {
            return this.mGeocodeProvider.getFromLocation(latitude, longitude, maxResults, params, addrs);
        }
        Log.i(TAG, "mGeocodeProvider is null");
        return null;
    }

    public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        if (this.mGeocodeProvider != null) {
            return this.mGeocodeProvider.getFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
        }
        Log.i(TAG, "mGeocodeProvider is null");
        return null;
    }

    private boolean canCallerAccessMockLocation(String opPackageName) {
        return this.mAppOps.noteOp(58, Binder.getCallingUid(), opPackageName) == 0;
    }

    public void addTestProvider(String name, ProviderProperties properties, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            if (!"passive".equals(name)) {
                long identity = Binder.clearCallingIdentity();
                synchronized (this.mLock) {
                    if ("gps".equals(name) || "network".equals(name) || IHwLocalLocationProvider.LOCAL_PROVIDER.equals(name) || "fused".equals(name)) {
                        LocationProviderInterface p = this.mProvidersByName.get(name);
                        if (p != null) {
                            removeProviderLocked(p);
                        }
                    }
                    setGeoFencerEnabled(false);
                    Log.d(TAG, "addTestProvider " + name + " opPackageName " + opPackageName);
                    addTestProviderLocked(name, properties);
                    updateProvidersLocked();
                }
                Binder.restoreCallingIdentity(identity);
                return;
            }
            throw new IllegalArgumentException("Cannot mock the passive location provider");
        }
    }

    private void addTestProviderLocked(String name, ProviderProperties properties) {
        if (this.mProvidersByName.get(name) == null) {
            MockProvider provider = new MockProvider(name, this, properties);
            addProviderLocked(provider);
            this.mMockProviders.put(name, provider);
            this.mLastLocation.put(name, null);
            this.mLastLocationCoarseInterval.put(name, null);
            return;
        }
        throw new IllegalArgumentException("Provider \"" + name + "\" already exists");
    }

    public void removeTestProvider(String provider, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                clearTestProviderEnabled(provider, opPackageName);
                clearTestProviderLocation(provider, opPackageName);
                clearTestProviderStatus(provider, opPackageName);
                if (this.mMockProviders.remove(provider) != null) {
                    long identity = Binder.clearCallingIdentity();
                    removeProviderLocked(this.mProvidersByName.get(provider));
                    setGeoFencerEnabled(true);
                    Log.d(TAG, "removeTestProvider " + provider + " opPackageName " + opPackageName);
                    LocationProviderInterface realProvider = this.mRealProviders.get(provider);
                    if (realProvider != null) {
                        addProviderLocked(realProvider);
                    }
                    this.mLastLocation.put(provider, null);
                    this.mLastLocationCoarseInterval.put(provider, null);
                    updateProvidersLocked();
                    Binder.restoreCallingIdentity(identity);
                } else {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
            }
        }
    }

    public void setTestProviderLocation(String provider, Location loc, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                MockProvider mockProvider = this.mMockProviders.get(provider);
                if (mockProvider != null) {
                    Location mock = new Location(loc);
                    mock.setIsFromMockProvider(true);
                    if (!TextUtils.isEmpty(loc.getProvider()) && !provider.equals(loc.getProvider())) {
                        EventLog.writeEvent(1397638484, new Object[]{"33091107", Integer.valueOf(Binder.getCallingUid()), provider + "!=" + loc.getProvider()});
                    }
                    long identity = Binder.clearCallingIdentity();
                    mockProvider.setLocation(mock);
                    Binder.restoreCallingIdentity(identity);
                } else {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
            }
        }
    }

    public void clearTestProviderLocation(String provider, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                MockProvider mockProvider = this.mMockProviders.get(provider);
                if (mockProvider != null) {
                    mockProvider.clearLocation();
                } else {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
            }
        }
    }

    public void setTestProviderEnabled(String provider, boolean enabled, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            setTestProviderEnabled(provider, enabled);
        }
    }

    private void setTestProviderEnabled(String provider, boolean enabled) {
        synchronized (this.mLock) {
            MockProvider mockProvider = this.mMockProviders.get(provider);
            if (mockProvider != null) {
                Log.d(TAG, "setTestProviderEnabled " + provider + " enabled " + enabled);
                long identity = Binder.clearCallingIdentity();
                if (enabled) {
                    mockProvider.enable();
                    this.mEnabledProviders.add(provider);
                    this.mDisabledProviders.remove(provider);
                } else {
                    mockProvider.disable();
                    this.mEnabledProviders.remove(provider);
                    this.mDisabledProviders.add(provider);
                }
                updateProvidersLocked();
                Binder.restoreCallingIdentity(identity);
            } else {
                throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
            }
        }
    }

    public void clearTestProviderEnabled(String provider, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                if (this.mMockProviders.get(provider) != null) {
                    long identity = Binder.clearCallingIdentity();
                    this.mEnabledProviders.remove(provider);
                    this.mDisabledProviders.remove(provider);
                    updateProvidersLocked();
                    Binder.restoreCallingIdentity(identity);
                } else {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
            }
        }
    }

    public void setTestProviderStatus(String provider, int status, Bundle extras, long updateTime, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                MockProvider mockProvider = this.mMockProviders.get(provider);
                if (mockProvider != null) {
                    mockProvider.setStatus(status, extras, updateTime);
                } else {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
            }
        }
    }

    public void clearTestProviderStatus(String provider, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                MockProvider mockProvider = this.mMockProviders.get(provider);
                if (mockProvider != null) {
                    mockProvider.clearStatus();
                } else {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
            }
        }
    }

    private void log(String log) {
        if (Log.isLoggable(TAG, 2)) {
            Slog.d(TAG, log);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002a, code lost:
        return;
     */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            synchronized (this.mLock) {
                if (args.length <= 0 || !args[0].equals("--gnssmetrics")) {
                    pw.println("Current Location Manager state:");
                    pw.println("  Location Listeners:");
                    Iterator<Receiver> it = this.mReceivers.values().iterator();
                    while (it.hasNext()) {
                        pw.println("    " + it.next());
                    }
                    pw.println("  Active Records by Provider:");
                    for (Map.Entry<String, ArrayList<UpdateRecord>> entry : this.mRecordsByProvider.entrySet()) {
                        pw.println("    " + entry.getKey() + ":");
                        Iterator it2 = entry.getValue().iterator();
                        while (it2.hasNext()) {
                            pw.println("      " + ((UpdateRecord) it2.next()));
                        }
                    }
                    pw.println("  Active GnssMeasurement Listeners:");
                    for (Identity identity : this.mGnssMeasurementsListeners.values()) {
                        pw.println("    " + identity.mPid + " " + identity.mUid + " " + identity.mPackageName + ": " + isThrottlingExemptLocked(identity));
                    }
                    pw.println("  Active GnssNavigationMessage Listeners:");
                    for (Identity identity2 : this.mGnssNavigationMessageListeners.values()) {
                        pw.println("    " + identity2.mPid + " " + identity2.mUid + " " + identity2.mPackageName + ": " + isThrottlingExemptLocked(identity2));
                    }
                    pw.println("  Overlay Provider Packages:");
                    Iterator<LocationProviderInterface> it3 = this.mProviders.iterator();
                    while (it3.hasNext()) {
                        LocationProviderInterface provider = it3.next();
                        if (provider instanceof LocationProviderProxy) {
                            pw.println("    " + provider.getName() + ": " + ((LocationProviderProxy) provider).getConnectedPackageName());
                        }
                    }
                    pw.println("  Historical Records by Provider:");
                    for (Map.Entry<LocationRequestStatistics.PackageProviderKey, LocationRequestStatistics.PackageStatistics> entry2 : this.mRequestStatistics.statistics.entrySet()) {
                        LocationRequestStatistics.PackageProviderKey key = entry2.getKey();
                        pw.println("    " + key.packageName + ": " + key.providerName + ": " + entry2.getValue());
                    }
                    pw.println("  Last Known Locations:");
                    for (Map.Entry<String, Location> entry3 : this.mLastLocation.entrySet()) {
                        pw.println("    " + entry3.getKey() + ": " + entry3.getValue());
                    }
                    pw.println("  Last Known Locations Coarse Intervals:");
                    for (Map.Entry<String, Location> entry4 : this.mLastLocationCoarseInterval.entrySet()) {
                        pw.println("    " + entry4.getKey() + ": " + entry4.getValue());
                    }
                    this.mGeofenceManager.dump(pw);
                    if (this.mEnabledProviders.size() > 0) {
                        pw.println("  Enabled Providers:");
                        Iterator<String> it4 = this.mEnabledProviders.iterator();
                        while (it4.hasNext()) {
                            pw.println("    " + it4.next());
                        }
                    }
                    if (this.mDisabledProviders.size() > 0) {
                        pw.println("  Disabled Providers:");
                        Iterator<String> it5 = this.mDisabledProviders.iterator();
                        while (it5.hasNext()) {
                            pw.println("    " + it5.next());
                        }
                    }
                    pw.append("  ");
                    this.mBlacklist.dump(pw);
                    if (this.mMockProviders.size() > 0) {
                        pw.println("  Mock Providers:");
                        for (Map.Entry<String, MockProvider> i : this.mMockProviders.entrySet()) {
                            i.getValue().dump(pw, "      ");
                        }
                    }
                    if (!this.mBackgroundThrottlePackageWhitelist.isEmpty()) {
                        pw.println("  Throttling Whitelisted Packages:");
                        Iterator<String> it6 = this.mBackgroundThrottlePackageWhitelist.iterator();
                        while (it6.hasNext()) {
                            pw.println("    " + it6.next());
                        }
                    }
                    pw.append("  fudger: ");
                    this.mLocationFudger.dump(fd, pw, args);
                    if (args.length <= 0 || !"short".equals(args[0])) {
                        Iterator<LocationProviderInterface> it7 = this.mProviders.iterator();
                        while (it7.hasNext()) {
                            LocationProviderInterface provider2 = it7.next();
                            pw.print(provider2.getName() + " Internal State");
                            if (provider2 instanceof LocationProviderProxy) {
                                pw.print(" (" + ((LocationProviderProxy) provider2).getConnectedPackageName() + ")");
                            }
                            pw.println(":");
                            provider2.dump(fd, pw, args);
                        }
                        if (this.mGnssBatchingInProgress) {
                            pw.println("  GNSS batching in progress");
                        }
                        hwLocationPowerTrackerDump(pw);
                        dumpGpsFreezeProxy(pw);
                    }
                } else if (this.mGnssMetricsProvider != null) {
                    pw.append(this.mGnssMetricsProvider.getGnssMetricsAsProtoString());
                }
            }
        }
    }

    private String getCallingAppName(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = this.mPackageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (appInfo == null) {
            return packageName;
        }
        return (String) this.mPackageManager.getApplicationLabel(appInfo);
    }

    private String getPackageNameByPid(int pid) {
        if (pid <= 0) {
            return null;
        }
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (activityManager == null) {
            return null;
        }
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return null;
        }
        String packageName = null;
        Iterator<ActivityManager.RunningAppProcessInfo> it = appProcesses.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ActivityManager.RunningAppProcessInfo appProcess = it.next();
            if (appProcess.pid == pid) {
                packageName = appProcess.processName;
                break;
            }
        }
        int indexProcessFlag = -1;
        if (packageName != null) {
            indexProcessFlag = packageName.indexOf(58);
        }
        return indexProcessFlag > 0 ? packageName.substring(0, indexProcessFlag) : packageName;
    }

    private void printFormatLog(String packageName, int pid, String callingMethodName, String tag) {
        if (!TextUtils.isEmpty(packageName)) {
            String applicationInfo = "<" + getCallingAppName(packageName) + ">[" + getCallingAppName(packageName) + "][" + getPackageNameByPid(pid) + "]:[" + callingMethodName + "]";
            if (tag.equals("gps")) {
                Log.i("ctaifs", applicationInfo + " 定位..GPS定位");
            }
            if (tag.equals("network")) {
                Log.i("ctaifs", applicationInfo + " 定位..Wifi/基站定位");
            }
            if (tag.equals("report_location")) {
                Log.i("ctaifs", applicationInfo + " 读取定位信息");
            }
        }
    }
}
