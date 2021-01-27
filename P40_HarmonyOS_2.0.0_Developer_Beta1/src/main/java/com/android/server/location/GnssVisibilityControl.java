package com.android.server.location;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.StatsLog;
import com.android.internal.location.GpsNetInitiatedHandler;
import com.android.internal.notification.SystemNotificationChannels;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/* access modifiers changed from: package-private */
public class GnssVisibilityControl {
    private static final int ARRAY_MAP_INITIAL_CAPACITY_PROXY_APPS_STATE = 5;
    private static final boolean DEBUG = Log.isLoggable("GnssVisibilityControl", 3);
    private static final long LOCATION_ICON_DISPLAY_DURATION_MILLIS = 5000;
    private static final String LOCATION_PERMISSION_NAME = "android.permission.ACCESS_FINE_LOCATION";
    private static final String[] NO_LOCATION_ENABLED_PROXY_APPS = new String[0];
    private static final long ON_GPS_ENABLED_CHANGED_TIMEOUT_MILLIS = 3000;
    private static final String TAG = "GnssVisibilityControl";
    private static final String WAKELOCK_KEY = "GnssVisibilityControl";
    private static final long WAKELOCK_TIMEOUT_MILLIS = 60000;
    private final AppOpsManager mAppOps;
    private final Context mContext;
    private final Handler mHandler;
    private boolean mIsGpsEnabled;
    private final GpsNetInitiatedHandler mNiHandler;
    private PackageManager.OnPermissionsChangedListener mOnPermissionsChangedListener = new PackageManager.OnPermissionsChangedListener() {
        /* class com.android.server.location.$$Lambda$GnssVisibilityControl$cq648s0kLZajRjefdRR_iUZoiQ */

        public final void onPermissionsChanged(int i) {
            GnssVisibilityControl.this.lambda$new$1$GnssVisibilityControl(i);
        }
    };
    private final PackageManager mPackageManager;
    private ArrayMap<String, ProxyAppState> mProxyAppsState = new ArrayMap<>(5);
    private final PowerManager.WakeLock mWakeLock;

    private native boolean native_enable_nfw_location_access(String[] strArr);

    /* access modifiers changed from: private */
    public static final class ProxyAppState {
        private boolean mHasLocationPermission;
        private boolean mIsLocationIconOn;

        private ProxyAppState(boolean hasLocationPermission) {
            this.mHasLocationPermission = hasLocationPermission;
        }
    }

    public /* synthetic */ void lambda$new$1$GnssVisibilityControl(int uid) {
        runOnHandler(new Runnable(uid) {
            /* class com.android.server.location.$$Lambda$GnssVisibilityControl$nmfWkQtbYmj8KoGmFncGZnuzWS0 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                GnssVisibilityControl.this.lambda$new$0$GnssVisibilityControl(this.f$1);
            }
        });
    }

    GnssVisibilityControl(Context context, Looper looper, GpsNetInitiatedHandler niHandler) {
        this.mContext = context;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "GnssVisibilityControl");
        this.mHandler = new Handler(looper);
        this.mNiHandler = niHandler;
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mPackageManager = this.mContext.getPackageManager();
        runOnHandler(new Runnable() {
            /* class com.android.server.location.$$Lambda$GnssVisibilityControl$FLGfeDaxF8J3CE9mTcOXh5j6ow */

            @Override // java.lang.Runnable
            public final void run() {
                GnssVisibilityControl.m16lambda$FLGfeDaxF8J3CE9mTcOXh5j6ow(GnssVisibilityControl.this);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void onGpsEnabledChanged(boolean isEnabled) {
        if (!this.mHandler.runWithScissors(new Runnable(isEnabled) {
            /* class com.android.server.location.$$Lambda$GnssVisibilityControl$WNe_VoiVnZtOTinPJBWWgUSctQ */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                GnssVisibilityControl.this.lambda$onGpsEnabledChanged$2$GnssVisibilityControl(this.f$1);
            }
        }, 3000) && !isEnabled) {
            Log.w("GnssVisibilityControl", "Native call to disable non-framework location access in GNSS HAL may get executed after native_cleanup().");
        }
    }

    public /* synthetic */ void lambda$reportNfwNotification$3$GnssVisibilityControl(String proxyAppPackageName, byte protocolStack, String otherProtocolStackName, byte requestor, String requestorId, byte responseType, boolean inEmergencyMode, boolean isCachedLocation) {
        handleNfwNotification(new NfwNotification(proxyAppPackageName, protocolStack, otherProtocolStackName, requestor, requestorId, responseType, inEmergencyMode, isCachedLocation));
    }

    /* access modifiers changed from: package-private */
    public void reportNfwNotification(String proxyAppPackageName, byte protocolStack, String otherProtocolStackName, byte requestor, String requestorId, byte responseType, boolean inEmergencyMode, boolean isCachedLocation) {
        runOnHandler(new Runnable(proxyAppPackageName, protocolStack, otherProtocolStackName, requestor, requestorId, responseType, inEmergencyMode, isCachedLocation) {
            /* class com.android.server.location.$$Lambda$GnssVisibilityControl$rgPyvoFYNphS9zV3fbeQCNLxa8 */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ byte f$2;
            private final /* synthetic */ String f$3;
            private final /* synthetic */ byte f$4;
            private final /* synthetic */ String f$5;
            private final /* synthetic */ byte f$6;
            private final /* synthetic */ boolean f$7;
            private final /* synthetic */ boolean f$8;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
            }

            @Override // java.lang.Runnable
            public final void run() {
                GnssVisibilityControl.this.lambda$reportNfwNotification$3$GnssVisibilityControl(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void onConfigurationUpdated(GnssConfiguration configuration) {
        runOnHandler(new Runnable(configuration.getProxyApps()) {
            /* class com.android.server.location.$$Lambda$GnssVisibilityControl$YLPk0FuuEUrv7lfRNYvhNb6uKic */
            private final /* synthetic */ List f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                GnssVisibilityControl.this.lambda$onConfigurationUpdated$4$GnssVisibilityControl(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    public void handleInitialize() {
        listenForProxyAppsPackageUpdates();
    }

    private void listenForProxyAppsPackageUpdates() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.location.GnssVisibilityControl.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    char c = 65535;
                    switch (action.hashCode()) {
                        case -810471698:
                            if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
                                c = 2;
                                break;
                            }
                            break;
                        case 172491798:
                            if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                                c = 3;
                                break;
                            }
                            break;
                        case 525384130:
                            if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                                c = 1;
                                break;
                            }
                            break;
                        case 1544582882:
                            if (action.equals("android.intent.action.PACKAGE_ADDED")) {
                                c = 0;
                                break;
                            }
                            break;
                    }
                    if (c == 0 || c == 1 || c == 2 || c == 3) {
                        GnssVisibilityControl.this.handleProxyAppPackageUpdate(intent.getData().getEncodedSchemeSpecificPart(), action);
                    }
                }
            }
        }, UserHandle.ALL, intentFilter, null, this.mHandler);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleProxyAppPackageUpdate(String pkgName, String action) {
        ProxyAppState proxyAppState = this.mProxyAppsState.get(pkgName);
        if (proxyAppState != null) {
            if (DEBUG) {
                Log.d("GnssVisibilityControl", "Proxy app " + pkgName + " package changed: " + action);
            }
            boolean updatedLocationPermission = shouldEnableLocationPermissionInGnssHal(pkgName);
            if (proxyAppState.mHasLocationPermission != updatedLocationPermission) {
                Log.i("GnssVisibilityControl", "Proxy app " + pkgName + " location permission changed. IsLocationPermissionEnabled: " + updatedLocationPermission);
                proxyAppState.mHasLocationPermission = updatedLocationPermission;
                updateNfwLocationAccessProxyAppsInGnssHal();
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: handleUpdateProxyApps */
    public void lambda$onConfigurationUpdated$4$GnssVisibilityControl(List<String> nfwLocationAccessProxyApps) {
        if (isProxyAppListUpdated(nfwLocationAccessProxyApps)) {
            if (!nfwLocationAccessProxyApps.isEmpty()) {
                if (this.mProxyAppsState.isEmpty()) {
                    this.mPackageManager.addOnPermissionsChangeListener(this.mOnPermissionsChangedListener);
                } else {
                    resetProxyAppsState();
                }
                for (String proxyAppPkgName : nfwLocationAccessProxyApps) {
                    this.mProxyAppsState.put(proxyAppPkgName, new ProxyAppState(shouldEnableLocationPermissionInGnssHal(proxyAppPkgName)));
                }
                updateNfwLocationAccessProxyAppsInGnssHal();
            } else if (!this.mProxyAppsState.isEmpty()) {
                this.mPackageManager.removeOnPermissionsChangeListener(this.mOnPermissionsChangedListener);
                resetProxyAppsState();
                updateNfwLocationAccessProxyAppsInGnssHal();
            }
        }
    }

    private void resetProxyAppsState() {
        for (Map.Entry<String, ProxyAppState> entry : this.mProxyAppsState.entrySet()) {
            ProxyAppState proxyAppState = entry.getValue();
            if (proxyAppState.mIsLocationIconOn) {
                this.mHandler.removeCallbacksAndMessages(proxyAppState);
                ApplicationInfo proxyAppInfo = getProxyAppInfo(entry.getKey());
                if (proxyAppInfo != null) {
                    clearLocationIcon(proxyAppState, proxyAppInfo.uid, entry.getKey());
                }
            }
        }
        this.mProxyAppsState.clear();
    }

    private boolean isProxyAppListUpdated(List<String> nfwLocationAccessProxyApps) {
        if (nfwLocationAccessProxyApps.size() != this.mProxyAppsState.size()) {
            return true;
        }
        for (String nfwLocationAccessProxyApp : nfwLocationAccessProxyApps) {
            if (!this.mProxyAppsState.containsKey(nfwLocationAccessProxyApp)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: handleGpsEnabledChanged */
    public void lambda$onGpsEnabledChanged$2$GnssVisibilityControl(boolean isGpsEnabled) {
        if (DEBUG) {
            Log.d("GnssVisibilityControl", "handleGpsEnabledChanged, mIsGpsEnabled: " + this.mIsGpsEnabled + ", isGpsEnabled: " + isGpsEnabled);
        }
        this.mIsGpsEnabled = isGpsEnabled;
        if (!this.mIsGpsEnabled) {
            disableNfwLocationAccess();
        } else {
            setNfwLocationAccessProxyAppsInGnssHal(getLocationPermissionEnabledProxyApps());
        }
    }

    private void disableNfwLocationAccess() {
        setNfwLocationAccessProxyAppsInGnssHal(NO_LOCATION_ENABLED_PROXY_APPS);
    }

    /* access modifiers changed from: private */
    public static class NfwNotification {
        private static final byte NFW_RESPONSE_TYPE_ACCEPTED_LOCATION_PROVIDED = 2;
        private static final byte NFW_RESPONSE_TYPE_ACCEPTED_NO_LOCATION_PROVIDED = 1;
        private static final byte NFW_RESPONSE_TYPE_REJECTED = 0;
        private final boolean mInEmergencyMode;
        private final boolean mIsCachedLocation;
        private final String mOtherProtocolStackName;
        private final byte mProtocolStack;
        private final String mProxyAppPackageName;
        private final byte mRequestor;
        private final String mRequestorId;
        private final byte mResponseType;

        private NfwNotification(String proxyAppPackageName, byte protocolStack, String otherProtocolStackName, byte requestor, String requestorId, byte responseType, boolean inEmergencyMode, boolean isCachedLocation) {
            this.mProxyAppPackageName = proxyAppPackageName;
            this.mProtocolStack = protocolStack;
            this.mOtherProtocolStackName = otherProtocolStackName;
            this.mRequestor = requestor;
            this.mRequestorId = requestorId;
            this.mResponseType = responseType;
            this.mInEmergencyMode = inEmergencyMode;
            this.mIsCachedLocation = isCachedLocation;
        }

        @SuppressLint({"DefaultLocale"})
        public String toString() {
            return String.format("{proxyAppPackageName: %s, protocolStack: %d, otherProtocolStackName: %s, requestor: %d, requestorId: %s, responseType: %s, inEmergencyMode: %b, isCachedLocation: %b}", this.mProxyAppPackageName, Byte.valueOf(this.mProtocolStack), this.mOtherProtocolStackName, Byte.valueOf(this.mRequestor), this.mRequestorId, getResponseTypeAsString(), Boolean.valueOf(this.mInEmergencyMode), Boolean.valueOf(this.mIsCachedLocation));
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getResponseTypeAsString() {
            byte b = this.mResponseType;
            if (b == 0) {
                return "REJECTED";
            }
            if (b == 1) {
                return "ACCEPTED_NO_LOCATION_PROVIDED";
            }
            if (b != 2) {
                return "<Unknown>";
            }
            return "ACCEPTED_LOCATION_PROVIDED";
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isRequestAccepted() {
            return this.mResponseType != 0;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isLocationProvided() {
            return this.mResponseType == 2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isRequestAttributedToProxyApp() {
            return !TextUtils.isEmpty(this.mProxyAppPackageName);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isEmergencyRequestNotification() {
            return this.mInEmergencyMode && !isRequestAttributedToProxyApp();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: handlePermissionsChanged */
    public void lambda$new$0$GnssVisibilityControl(int uid) {
        if (!this.mProxyAppsState.isEmpty()) {
            for (Map.Entry<String, ProxyAppState> entry : this.mProxyAppsState.entrySet()) {
                String proxyAppPkgName = entry.getKey();
                ApplicationInfo proxyAppInfo = getProxyAppInfo(proxyAppPkgName);
                if (proxyAppInfo != null && proxyAppInfo.uid == uid) {
                    boolean isLocationPermissionEnabled = shouldEnableLocationPermissionInGnssHal(proxyAppPkgName);
                    ProxyAppState proxyAppState = entry.getValue();
                    if (isLocationPermissionEnabled != proxyAppState.mHasLocationPermission) {
                        Log.i("GnssVisibilityControl", "Proxy app " + proxyAppPkgName + " location permission changed. IsLocationPermissionEnabled: " + isLocationPermissionEnabled);
                        proxyAppState.mHasLocationPermission = isLocationPermissionEnabled;
                        updateNfwLocationAccessProxyAppsInGnssHal();
                        return;
                    }
                    return;
                }
            }
        }
    }

    private ApplicationInfo getProxyAppInfo(String proxyAppPkgName) {
        try {
            return this.mPackageManager.getApplicationInfo(proxyAppPkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            if (!DEBUG) {
                return null;
            }
            Log.d("GnssVisibilityControl", "Proxy app " + proxyAppPkgName + " is not found.");
            return null;
        }
    }

    private boolean shouldEnableLocationPermissionInGnssHal(String proxyAppPkgName) {
        return isProxyAppInstalled(proxyAppPkgName) && hasLocationPermission(proxyAppPkgName);
    }

    private boolean isProxyAppInstalled(String pkgName) {
        ApplicationInfo proxyAppInfo = getProxyAppInfo(pkgName);
        return proxyAppInfo != null && proxyAppInfo.enabled;
    }

    private boolean hasLocationPermission(String pkgName) {
        return this.mPackageManager.checkPermission(LOCATION_PERMISSION_NAME, pkgName) == 0;
    }

    private void updateNfwLocationAccessProxyAppsInGnssHal() {
        if (this.mIsGpsEnabled) {
            setNfwLocationAccessProxyAppsInGnssHal(getLocationPermissionEnabledProxyApps());
        }
    }

    private void setNfwLocationAccessProxyAppsInGnssHal(String[] locationPermissionEnabledProxyApps) {
        String proxyAppsStr = Arrays.toString(locationPermissionEnabledProxyApps);
        Log.i("GnssVisibilityControl", "Updating non-framework location access proxy apps in the GNSS HAL to: " + proxyAppsStr);
        if (!native_enable_nfw_location_access(locationPermissionEnabledProxyApps)) {
            Log.e("GnssVisibilityControl", "Failed to update non-framework location access proxy apps in the GNSS HAL to: " + proxyAppsStr);
        }
    }

    private String[] getLocationPermissionEnabledProxyApps() {
        int countLocationPermissionEnabledProxyApps = 0;
        for (ProxyAppState proxyAppState : this.mProxyAppsState.values()) {
            if (proxyAppState.mHasLocationPermission) {
                countLocationPermissionEnabledProxyApps++;
            }
        }
        int i = 0;
        String[] locationPermissionEnabledProxyApps = new String[countLocationPermissionEnabledProxyApps];
        for (Map.Entry<String, ProxyAppState> entry : this.mProxyAppsState.entrySet()) {
            String proxyApp = entry.getKey();
            if (entry.getValue().mHasLocationPermission) {
                locationPermissionEnabledProxyApps[i] = proxyApp;
                i++;
            }
        }
        return locationPermissionEnabledProxyApps;
    }

    private void handleNfwNotification(NfwNotification nfwNotification) {
        if (DEBUG) {
            Log.d("GnssVisibilityControl", "Non-framework location access notification: " + nfwNotification);
        }
        if (nfwNotification.isEmergencyRequestNotification()) {
            handleEmergencyNfwNotification(nfwNotification);
            return;
        }
        String proxyAppPkgName = nfwNotification.mProxyAppPackageName;
        ProxyAppState proxyAppState = this.mProxyAppsState.get(proxyAppPkgName);
        boolean isLocationRequestAccepted = nfwNotification.isRequestAccepted();
        boolean isPermissionMismatched = isPermissionMismatched(proxyAppState, nfwNotification);
        logEvent(nfwNotification, isPermissionMismatched);
        if (!nfwNotification.isRequestAttributedToProxyApp()) {
            if (isLocationRequestAccepted) {
                Log.e("GnssVisibilityControl", "ProxyAppPackageName field is not set. AppOps service not notified for notification: " + nfwNotification);
            } else if (DEBUG) {
                Log.d("GnssVisibilityControl", "Non-framework location request rejected. ProxyAppPackageName field is not set in the notification: " + nfwNotification + ". Number of configured proxy apps: " + this.mProxyAppsState.size());
            }
        } else if (proxyAppState == null) {
            Log.w("GnssVisibilityControl", "Could not find proxy app " + proxyAppPkgName + " in the value specified for config parameter: " + GnssConfiguration.CONFIG_NFW_PROXY_APPS + ". AppOps service not notified for notification: " + nfwNotification);
        } else {
            ApplicationInfo proxyAppInfo = getProxyAppInfo(proxyAppPkgName);
            if (proxyAppInfo == null) {
                Log.e("GnssVisibilityControl", "Proxy app " + proxyAppPkgName + " is not found. AppOps service not notified for notification: " + nfwNotification);
                return;
            }
            if (nfwNotification.isLocationProvided()) {
                showLocationIcon(proxyAppState, nfwNotification, proxyAppInfo.uid, proxyAppPkgName);
                this.mAppOps.noteOpNoThrow(1, proxyAppInfo.uid, proxyAppPkgName);
            }
            if (isPermissionMismatched) {
                Log.w("GnssVisibilityControl", "Permission mismatch. Proxy app " + proxyAppPkgName + " location permission is set to " + proxyAppState.mHasLocationPermission + " and GNSS HAL enabled is set to " + this.mIsGpsEnabled + " but GNSS non-framework location access response type is " + nfwNotification.getResponseTypeAsString() + " for notification: " + nfwNotification);
            }
        }
    }

    private boolean isPermissionMismatched(ProxyAppState proxyAppState, NfwNotification nfwNotification) {
        boolean isLocationRequestAccepted = nfwNotification.isRequestAccepted();
        if (proxyAppState == null || !this.mIsGpsEnabled) {
            return isLocationRequestAccepted;
        }
        return proxyAppState.mHasLocationPermission != isLocationRequestAccepted;
    }

    private void showLocationIcon(ProxyAppState proxyAppState, NfwNotification nfwNotification, int uid, String proxyAppPkgName) {
        boolean isLocationIconOn = proxyAppState.mIsLocationIconOn;
        if (isLocationIconOn) {
            this.mHandler.removeCallbacksAndMessages(proxyAppState);
        } else if (!updateLocationIcon(true, uid, proxyAppPkgName)) {
            Log.w("GnssVisibilityControl", "Failed to show Location icon for notification: " + nfwNotification);
            return;
        } else {
            proxyAppState.mIsLocationIconOn = true;
        }
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Location icon on. ");
            sb.append(isLocationIconOn ? "Extending" : "Setting");
            sb.append(" icon display timer. Uid: ");
            sb.append(uid);
            sb.append(", proxyAppPkgName: ");
            sb.append(proxyAppPkgName);
            Log.d("GnssVisibilityControl", sb.toString());
        }
        if (!this.mHandler.postDelayed(new Runnable(proxyAppPkgName) {
            /* class com.android.server.location.$$Lambda$GnssVisibilityControl$3hQO4NR8YgRdTo_ZUTbEKP4TIU */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                GnssVisibilityControl.this.lambda$showLocationIcon$5$GnssVisibilityControl(this.f$1);
            }
        }, proxyAppState, LOCATION_ICON_DISPLAY_DURATION_MILLIS)) {
            clearLocationIcon(proxyAppState, uid, proxyAppPkgName);
            Log.w("GnssVisibilityControl", "Failed to show location icon for the full duration for notification: " + nfwNotification);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: handleLocationIconTimeout */
    public void lambda$showLocationIcon$5$GnssVisibilityControl(String proxyAppPkgName) {
        ApplicationInfo proxyAppInfo = getProxyAppInfo(proxyAppPkgName);
        if (proxyAppInfo != null) {
            clearLocationIcon(this.mProxyAppsState.get(proxyAppPkgName), proxyAppInfo.uid, proxyAppPkgName);
        }
    }

    private void clearLocationIcon(ProxyAppState proxyAppState, int uid, String proxyAppPkgName) {
        updateLocationIcon(false, uid, proxyAppPkgName);
        if (proxyAppState != null) {
            proxyAppState.mIsLocationIconOn = false;
        }
        if (DEBUG) {
            Log.d("GnssVisibilityControl", "Location icon off. Uid: " + uid + ", proxyAppPkgName: " + proxyAppPkgName);
        }
    }

    private boolean updateLocationIcon(boolean displayLocationIcon, int uid, String proxyAppPkgName) {
        if (!displayLocationIcon) {
            this.mAppOps.finishOp(41, uid, proxyAppPkgName);
            this.mAppOps.finishOp(42, uid, proxyAppPkgName);
        } else if (this.mAppOps.startOpNoThrow(41, uid, proxyAppPkgName) != 0) {
            return false;
        } else {
            if (this.mAppOps.startOpNoThrow(42, uid, proxyAppPkgName) != 0) {
                this.mAppOps.finishOp(41, uid, proxyAppPkgName);
                return false;
            }
        }
        sendHighPowerMonitoringBroadcast();
        return true;
    }

    private void sendHighPowerMonitoringBroadcast() {
        this.mContext.sendBroadcastAsUser(new Intent("android.location.HIGH_POWER_REQUEST_CHANGE"), UserHandle.ALL);
    }

    private void handleEmergencyNfwNotification(NfwNotification nfwNotification) {
        boolean isPermissionMismatched = false;
        if (!nfwNotification.isRequestAccepted()) {
            Log.e("GnssVisibilityControl", "Emergency non-framework location request incorrectly rejected. Notification: " + nfwNotification);
            isPermissionMismatched = true;
        }
        if (!this.mNiHandler.getInEmergency()) {
            Log.w("GnssVisibilityControl", "Emergency state mismatch. Device currently not in user initiated emergency session. Notification: " + nfwNotification);
            isPermissionMismatched = true;
        }
        logEvent(nfwNotification, isPermissionMismatched);
        if (nfwNotification.isLocationProvided()) {
            postEmergencyLocationUserNotification(nfwNotification);
        }
    }

    private void postEmergencyLocationUserNotification(NfwNotification nfwNotification) {
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        if (notificationManager == null) {
            Log.w("GnssVisibilityControl", "Could not notify user of emergency location request. Notification: " + nfwNotification);
            return;
        }
        notificationManager.notifyAsUser(null, 0, createEmergencyLocationUserNotification(this.mContext), UserHandle.ALL);
    }

    private static Notification createEmergencyLocationUserNotification(Context context) {
        String firstLineText = context.getString(17040225);
        String secondLineText = context.getString(17040207);
        return new Notification.Builder(context, SystemNotificationChannels.NETWORK_ALERTS).setSmallIcon(17303586).setWhen(0).setOngoing(true).setAutoCancel(true).setColor(context.getColor(17170460)).setDefaults(0).setTicker(firstLineText + " (" + secondLineText + ")").setContentTitle(firstLineText).setContentText(secondLineText).build();
    }

    private void logEvent(NfwNotification notification, boolean isPermissionMismatched) {
        StatsLog.write(131, notification.mProxyAppPackageName, notification.mProtocolStack, notification.mOtherProtocolStackName, notification.mRequestor, notification.mRequestorId, notification.mResponseType, notification.mInEmergencyMode, notification.mIsCachedLocation, isPermissionMismatched);
    }

    private void runOnHandler(Runnable event) {
        this.mWakeLock.acquire(60000);
        if (!this.mHandler.post(runEventAndReleaseWakeLock(event))) {
            this.mWakeLock.release();
        }
    }

    private Runnable runEventAndReleaseWakeLock(Runnable event) {
        return new Runnable(event) {
            /* class com.android.server.location.$$Lambda$GnssVisibilityControl$ezKd0QctWKgyrEvPFQUXWNBxlNg */
            private final /* synthetic */ Runnable f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                GnssVisibilityControl.this.lambda$runEventAndReleaseWakeLock$6$GnssVisibilityControl(this.f$1);
            }
        };
    }

    public /* synthetic */ void lambda$runEventAndReleaseWakeLock$6$GnssVisibilityControl(Runnable event) {
        try {
            event.run();
        } finally {
            this.mWakeLock.release();
        }
    }
}
