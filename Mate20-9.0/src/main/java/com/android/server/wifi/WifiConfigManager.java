package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.IpConfiguration;
import android.net.MacAddress;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.server.LocalServices;
import com.android.server.wifi.ScoringParams;
import com.android.server.wifi.WifiConfigStoreLegacy;
import com.android.server.wifi.WifiConfigurationUtil;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParserException;

public class WifiConfigManager extends AbsWifiConfigManager {
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static boolean HWLOGW_E = true;
    @VisibleForTesting
    public static final int LINK_CONFIGURATION_BSSID_MATCH_LENGTH = 16;
    @VisibleForTesting
    public static final int LINK_CONFIGURATION_MAX_SCAN_CACHE_ENTRIES = 6;
    private static final int MAX_HIDDEN_NETWORKS_NUM = 14;
    private static final int MAX_NO_RESORT_HIDDEN_NETWORKS_NUM = 9;
    public static final int MAX_RX_PACKET_FOR_FULL_SCANS = 16;
    public static final int MAX_TX_PACKET_FOR_FULL_SCANS = 8;
    @VisibleForTesting
    public static final int[] NETWORK_SELECTION_DISABLE_THRESHOLD = {-1, 1, 3, 5, 5, 5, 1, 1, 6, 1, 1, 1, 1, 1, 1, 1, 5};
    @VisibleForTesting
    public static final int[] NETWORK_SELECTION_DISABLE_TIMEOUT_MS = {ScoringParams.Values.MAX_EXPID, 900000, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, 600000, 0, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS};
    @VisibleForTesting
    public static final String PASSWORD_MASK = "*";
    @VisibleForTesting
    public static final int SCAN_CACHE_ENTRIES_MAX_SIZE = 192;
    @VisibleForTesting
    public static final int SCAN_CACHE_ENTRIES_TRIM_SIZE = 128;
    private static final int SCAN_RESULT_MAXIMUM_AGE_MS = 40000;
    @VisibleForTesting
    public static final String SYSUI_PACKAGE_NAME = "com.android.systemui";
    public static final String TAG = "WifiConfigManager";
    private static int mCurrentHiddenNetId = -1;
    private static final WifiConfigurationUtil.WifiConfigurationComparator sScanListComparator = new WifiConfigurationUtil.WifiConfigurationComparator() {
        public int compareNetworksWithSameStatus(WifiConfiguration a, WifiConfiguration b) {
            if (a.numAssociation != b.numAssociation) {
                return Long.compare((long) b.numAssociation, (long) a.numAssociation);
            }
            return Boolean.compare(b.getNetworkSelectionStatus().getSeenInLastQualifiedNetworkSelection(), a.getNetworkSelectionStatus().getSeenInLastQualifiedNetworkSelection());
        }
    };
    private static final WifiConfigurationUtil.WifiConfigurationComparator sScanListTimeComparator = new WifiConfigurationUtil.WifiConfigurationComparator() {
        public int compareNetworksWithSameStatus(WifiConfiguration a, WifiConfiguration b) {
            return Long.compare(0 == b.lastHasInternetTimestamp ? b.lastConnected : b.lastHasInternetTimestamp, 0 == a.lastHasInternetTimestamp ? a.lastConnected : a.lastHasInternetTimestamp);
        }
    };
    private static boolean sVDBG = HWFLOW;
    private static boolean sVVDBG = HWFLOW;
    private final BackupManagerProxy mBackupManagerProxy;
    private final Clock mClock;
    private final ConfigurationMap mConfiguredNetworks;
    private final Context mContext;
    private int mCurrentUserId;
    HwCustWifiAutoJoinController mCust = ((HwCustWifiAutoJoinController) HwCustUtils.createObj(HwCustWifiAutoJoinController.class, new Object[0]));
    private boolean mDeferredUserUnlockRead;
    private final Set<String> mDeletedEphemeralSSIDs;
    private final DeletedEphemeralSsidsStoreData mDeletedEphemeralSsidsStoreData;
    private int mLastPriority;
    private int mLastSelectedNetworkId;
    private long mLastSelectedTimeStamp;
    private OnSavedNetworkUpdateListener mListener;
    private final LocalLog mLocalLog;
    private final int mMaxNumActiveChannelsForPartialScans;
    private final NetworkListStoreData mNetworkListStoreData;
    private int mNextNetworkId;
    private final boolean mOnlyLinkSameCredentialConfigurations;
    private boolean mPendingStoreRead;
    private boolean mPendingUnlockStoreRead;
    private final Map<Integer, ScanDetailCache> mScanDetailCaches;
    private boolean mSimPresent;
    private int mSystemUiUid;
    private final TelephonyManager mTelephonyManager;
    private final UserManager mUserManager;
    private boolean mVerboseLoggingEnabled;
    private final WifiConfigStore mWifiConfigStore;
    private final WifiConfigStoreLegacy mWifiConfigStoreLegacy;
    private final WifiKeyStore mWifiKeyStore;
    private final WifiPermissionsUtil mWifiPermissionsUtil;
    private final WifiPermissionsWrapper mWifiPermissionsWrapper;

    public interface OnSavedNetworkUpdateListener {
        void onSavedNetworkAdded(int i);

        void onSavedNetworkEnabled(int i);

        void onSavedNetworkPermanentlyDisabled(int i, int i2);

        void onSavedNetworkRemoved(int i);

        void onSavedNetworkTemporarilyDisabled(int i, int i2);

        void onSavedNetworkUpdated(int i);
    }

    WifiConfigManager(Context context, Clock clock, UserManager userManager, TelephonyManager telephonyManager, WifiKeyStore wifiKeyStore, WifiConfigStore wifiConfigStore, WifiConfigStoreLegacy wifiConfigStoreLegacy, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper wifiPermissionsWrapper, NetworkListStoreData networkListStoreData, DeletedEphemeralSsidsStoreData deletedEphemeralSsidsStoreData) {
        this.mLocalLog = new LocalLog(ActivityManager.isLowRamDeviceStatic() ? 128 : 256);
        this.mVerboseLoggingEnabled = false;
        this.mCurrentUserId = 0;
        this.mPendingUnlockStoreRead = true;
        this.mPendingStoreRead = true;
        this.mDeferredUserUnlockRead = false;
        this.mSimPresent = false;
        this.mNextNetworkId = 0;
        this.mSystemUiUid = -1;
        this.mLastSelectedNetworkId = -1;
        this.mLastSelectedTimeStamp = -1;
        this.mListener = null;
        this.mLastPriority = -1;
        this.mContext = context;
        this.mClock = clock;
        this.mUserManager = userManager;
        this.mBackupManagerProxy = new BackupManagerProxy();
        this.mTelephonyManager = telephonyManager;
        this.mWifiKeyStore = wifiKeyStore;
        this.mWifiConfigStore = wifiConfigStore;
        this.mWifiConfigStoreLegacy = wifiConfigStoreLegacy;
        this.mWifiPermissionsUtil = wifiPermissionsUtil;
        this.mWifiPermissionsWrapper = wifiPermissionsWrapper;
        this.mConfiguredNetworks = new ConfigurationMap(userManager);
        this.mScanDetailCaches = new HashMap(16, 0.75f);
        this.mDeletedEphemeralSSIDs = new HashSet();
        this.mNetworkListStoreData = networkListStoreData;
        this.mDeletedEphemeralSsidsStoreData = deletedEphemeralSsidsStoreData;
        this.mWifiConfigStore.registerStoreData(this.mNetworkListStoreData);
        this.mWifiConfigStore.registerStoreData(this.mDeletedEphemeralSsidsStoreData);
        this.mOnlyLinkSameCredentialConfigurations = this.mContext.getResources().getBoolean(17957084);
        this.mMaxNumActiveChannelsForPartialScans = this.mContext.getResources().getInteger(17694898);
        try {
            this.mSystemUiUid = this.mContext.getPackageManager().getPackageUidAsUser(SYSUI_PACKAGE_NAME, 1048576, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to resolve SystemUI's UID.");
        }
    }

    @VisibleForTesting
    public static String createDebugTimeStampString(long wallClockMillis) {
        StringBuilder sb = new StringBuilder();
        sb.append("time=");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(wallClockMillis);
        sb.append(String.format("%tm-%td %tH:%tM:%tS.%tL", new Object[]{c, c, c, c, c, c}));
        return sb.toString();
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mVerboseLoggingEnabled = true;
        } else {
            this.mVerboseLoggingEnabled = false;
        }
        this.mWifiConfigStore.enableVerboseLogging(this.mVerboseLoggingEnabled);
        this.mWifiKeyStore.enableVerboseLogging(this.mVerboseLoggingEnabled);
    }

    private void maskPasswordsInWifiConfiguration(WifiConfiguration configuration) {
        if (!TextUtils.isEmpty(configuration.preSharedKey)) {
            configuration.preSharedKey = "*";
        }
        if (configuration.wepKeys != null) {
            for (int i = 0; i < configuration.wepKeys.length; i++) {
                if (!TextUtils.isEmpty(configuration.wepKeys[i])) {
                    configuration.wepKeys[i] = "*";
                }
            }
        }
        if (!TextUtils.isEmpty(configuration.enterpriseConfig.getPassword())) {
            configuration.enterpriseConfig.setPassword("*");
        }
    }

    private void maskRandomizedMacAddressInWifiConfiguration(WifiConfiguration configuration) {
        configuration.setRandomizedMacAddress(MacAddress.fromString("02:00:00:00:00:00"));
    }

    private WifiConfiguration createExternalWifiConfiguration(WifiConfiguration configuration, boolean maskPasswords) {
        WifiConfiguration network = new WifiConfiguration(configuration);
        if (maskPasswords) {
            maskPasswordsInWifiConfiguration(network);
        }
        maskRandomizedMacAddressInWifiConfiguration(network);
        return network;
    }

    private List<WifiConfiguration> getConfiguredNetworks(boolean savedOnly, boolean maskPasswords) {
        List<WifiConfiguration> networks = new ArrayList<>();
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (!savedOnly || !config.ephemeral) {
                networks.add(createExternalWifiConfiguration(config, maskPasswords));
            }
        }
        return networks;
    }

    public List<WifiConfiguration> getConfiguredNetworks() {
        return getConfiguredNetworks(false, true);
    }

    public List<WifiConfiguration> getConfiguredNetworksWithPasswords() {
        return getConfiguredNetworks(false, false);
    }

    public List<WifiConfiguration> getSavedNetworks() {
        return getConfiguredNetworks(true, true);
    }

    public WifiConfiguration getConfiguredNetwork(int networkId) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return null;
        }
        return createExternalWifiConfiguration(config, true);
    }

    public WifiConfiguration getConfiguredNetwork(String configKey) {
        WifiConfiguration config = getInternalConfiguredNetwork(configKey);
        if (config == null) {
            return null;
        }
        return createExternalWifiConfiguration(config, true);
    }

    public WifiConfiguration getConfiguredNetworkWithPassword(int networkId) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return null;
        }
        return createExternalWifiConfiguration(config, false);
    }

    public void enableAllNetworks() {
        boolean networkEnabledStateChanged = false;
        for (WifiConfiguration config : getSavedNetworks()) {
            if (config != null && !config.getNetworkSelectionStatus().isNetworkEnabled() && tryEnableNetwork(config)) {
                networkEnabledStateChanged = true;
            }
        }
        if (networkEnabledStateChanged) {
            WifiNative wifiNative = WifiInjector.getInstance().getWifiNative();
            sendConfiguredNetworksChangedBroadcast();
        }
    }

    public void disableAllNetworksNative() {
        if (WifiInjector.getInstance().getWifiNative() != null) {
            for (WifiConfiguration config : getSavedNetworks()) {
                if (config != null) {
                    config.status = 1;
                }
            }
        }
    }

    public WifiConfiguration getConfiguredNetworkWithoutMasking(int networkId) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return null;
        }
        return new WifiConfiguration(config);
    }

    private Collection<WifiConfiguration> getInternalConfiguredNetworks() {
        return this.mConfiguredNetworks.valuesForCurrentUser();
    }

    private WifiConfiguration getInternalConfiguredNetwork(WifiConfiguration config) {
        WifiConfiguration internalConfig = this.mConfiguredNetworks.getForCurrentUser(config.networkId);
        if (internalConfig != null) {
            return internalConfig;
        }
        WifiConfiguration internalConfig2 = this.mConfiguredNetworks.getByConfigKeyForCurrentUser(config.configKey());
        if (internalConfig2 == null) {
            Log.e(TAG, "Cannot find network with networkId " + config.networkId + " or configKey " + config.configKey());
        }
        return internalConfig2;
    }

    private WifiConfiguration getInternalConfiguredNetwork(int networkId) {
        if (networkId == -1) {
            return null;
        }
        WifiConfiguration internalConfig = this.mConfiguredNetworks.getForCurrentUser(networkId);
        if (internalConfig == null) {
            Log.e(TAG, "Cannot find network with networkId " + networkId);
        }
        return internalConfig;
    }

    private WifiConfiguration getInternalConfiguredNetwork(String configKey) {
        WifiConfiguration internalConfig = this.mConfiguredNetworks.getByConfigKeyForCurrentUser(configKey);
        if (internalConfig == null) {
            Log.e(TAG, "Cannot find network with configKey " + configKey);
        }
        return internalConfig;
    }

    private void sendConfiguredNetworkChangedBroadcast(WifiConfiguration network, int reason) {
        Intent intent = new Intent("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        intent.addFlags(67108864);
        intent.putExtra("multipleChanges", false);
        WifiConfiguration broadcastNetwork = new WifiConfiguration(network);
        maskPasswordsInWifiConfiguration(broadcastNetwork);
        intent.putExtra("wifiConfiguration", broadcastNetwork);
        intent.putExtra("changeReason", reason);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void sendConfiguredNetworksChangedBroadcast() {
        Intent intent = new Intent("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        intent.addFlags(67108864);
        intent.putExtra("multipleChanges", true);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private boolean canModifyNetwork(WifiConfiguration config, int uid) {
        boolean z = true;
        if (uid == 1000) {
            return true;
        }
        if (config.isPasspoint() && uid == 1010) {
            return true;
        }
        if (config.enterpriseConfig != null && uid == 1010 && TelephonyUtil.isSimEapMethod(config.enterpriseConfig.getEapMethod())) {
            return true;
        }
        DevicePolicyManagerInternal dpmi = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        if (dpmi != null && dpmi.isActiveAdminWithPolicy(uid, -2)) {
            return true;
        }
        boolean isCreator = config.creatorUid == uid;
        if (!this.mContext.getPackageManager().hasSystemFeature("android.software.device_admin") || dpmi != null) {
            if (!(dpmi != null && dpmi.isActiveAdminWithPolicy(config.creatorUid, -2))) {
                if (!isCreator && !this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid)) {
                    z = false;
                }
                return z;
            }
            if ((Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_device_owner_configs_lockdown", 0) != 0) || !this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid)) {
                z = false;
            }
            return z;
        }
        Log.w(TAG, "Error retrieving DPMI service.");
        return false;
    }

    private boolean doesUidBelongToCurrentUser(int uid) {
        if (uid == 1000 || uid == this.mSystemUiUid || uid == 1010) {
            return true;
        }
        return WifiConfigurationUtil.doesUidBelongToAnyProfile(uid, this.mUserManager.getProfiles(this.mCurrentUserId));
    }

    private void mergeWithInternalWifiConfiguration(WifiConfiguration internalConfig, WifiConfiguration externalConfig) {
        if (externalConfig.SSID != null) {
            internalConfig.SSID = externalConfig.SSID;
        }
        internalConfig.priority = externalConfig.priority;
        internalConfig.getNetworkSelectionStatus().setNetworkSelectionStatus(externalConfig.getNetworkSelectionStatus().getNetworkSelectionStatus());
        internalConfig.oriSsid = externalConfig.oriSsid;
        if (externalConfig.BSSID != null) {
            internalConfig.BSSID = externalConfig.BSSID.toLowerCase(Locale.ENGLISH);
        }
        internalConfig.isTempCreated = externalConfig.isTempCreated;
        internalConfig.hiddenSSID = externalConfig.hiddenSSID;
        internalConfig.cloudSecurityCheck = externalConfig.cloudSecurityCheck;
        internalConfig.connectToCellularAndWLAN = externalConfig.connectToCellularAndWLAN;
        internalConfig.wifiApType = externalConfig.wifiApType;
        if (externalConfig.preSharedKey != null && !externalConfig.preSharedKey.equals("*")) {
            internalConfig.preSharedKey = externalConfig.preSharedKey;
        }
        if (externalConfig.wepKeys != null) {
            boolean hasWepKey = false;
            for (int i = 0; i < internalConfig.wepKeys.length; i++) {
                if (externalConfig.wepKeys[i] != null && !externalConfig.wepKeys[i].equals("*")) {
                    internalConfig.wepKeys[i] = externalConfig.wepKeys[i];
                    hasWepKey = true;
                }
            }
            if (hasWepKey) {
                internalConfig.wepTxKeyIndex = externalConfig.wepTxKeyIndex;
            }
        }
        if (externalConfig.FQDN != null) {
            internalConfig.FQDN = externalConfig.FQDN;
        }
        if (externalConfig.providerFriendlyName != null) {
            internalConfig.providerFriendlyName = externalConfig.providerFriendlyName;
        }
        if (externalConfig.roamingConsortiumIds != null) {
            internalConfig.roamingConsortiumIds = (long[]) externalConfig.roamingConsortiumIds.clone();
        }
        if (externalConfig.allowedAuthAlgorithms != null && !externalConfig.allowedAuthAlgorithms.isEmpty()) {
            internalConfig.allowedAuthAlgorithms = (BitSet) externalConfig.allowedAuthAlgorithms.clone();
        }
        if (externalConfig.allowedProtocols != null && !externalConfig.allowedProtocols.isEmpty()) {
            internalConfig.allowedProtocols = (BitSet) externalConfig.allowedProtocols.clone();
        }
        if (externalConfig.allowedKeyManagement != null && !externalConfig.allowedKeyManagement.isEmpty()) {
            internalConfig.allowedKeyManagement = (BitSet) externalConfig.allowedKeyManagement.clone();
        }
        if (externalConfig.allowedPairwiseCiphers != null && !externalConfig.allowedPairwiseCiphers.isEmpty()) {
            internalConfig.allowedPairwiseCiphers = (BitSet) externalConfig.allowedPairwiseCiphers.clone();
        }
        if (externalConfig.allowedGroupCiphers != null && !externalConfig.allowedGroupCiphers.isEmpty()) {
            internalConfig.allowedGroupCiphers = (BitSet) externalConfig.allowedGroupCiphers.clone();
        }
        if (externalConfig.getIpConfiguration() != null) {
            IpConfiguration.IpAssignment ipAssignment = externalConfig.getIpAssignment();
            if (ipAssignment != IpConfiguration.IpAssignment.UNASSIGNED) {
                internalConfig.setIpAssignment(ipAssignment);
                if (ipAssignment == IpConfiguration.IpAssignment.STATIC) {
                    internalConfig.setStaticIpConfiguration(new StaticIpConfiguration(externalConfig.getStaticIpConfiguration()));
                }
            }
            IpConfiguration.ProxySettings proxySettings = externalConfig.getProxySettings();
            if (proxySettings != IpConfiguration.ProxySettings.UNASSIGNED) {
                internalConfig.setProxySettings(proxySettings);
                if (proxySettings == IpConfiguration.ProxySettings.PAC || proxySettings == IpConfiguration.ProxySettings.STATIC) {
                    internalConfig.setHttpProxy(new ProxyInfo(externalConfig.getHttpProxy()));
                }
            }
        }
        if (externalConfig.enterpriseConfig != null) {
            internalConfig.enterpriseConfig.copyFromExternal(externalConfig.enterpriseConfig, "*");
        }
        internalConfig.meteredHint = externalConfig.meteredHint;
        internalConfig.meteredOverride = externalConfig.meteredOverride;
    }

    private void setDefaultsInWifiConfiguration(WifiConfiguration configuration) {
        configuration.allowedAuthAlgorithms.set(0);
        configuration.allowedProtocols.set(1);
        configuration.allowedProtocols.set(0);
        configuration.allowedKeyManagement.set(1);
        configuration.allowedKeyManagement.set(2);
        configuration.allowedPairwiseCiphers.set(2);
        configuration.allowedPairwiseCiphers.set(1);
        configuration.allowedGroupCiphers.set(3);
        configuration.allowedGroupCiphers.set(2);
        configuration.allowedGroupCiphers.set(0);
        configuration.allowedGroupCiphers.set(1);
        configuration.setIpAssignment(IpConfiguration.IpAssignment.DHCP);
        configuration.setProxySettings(IpConfiguration.ProxySettings.NONE);
        configuration.status = 1;
        configuration.getNetworkSelectionStatus().setNetworkSelectionStatus(2);
        configuration.getNetworkSelectionStatus().setNetworkSelectionDisableReason(11);
    }

    private WifiConfiguration createNewInternalWifiConfigurationFromExternal(WifiConfiguration externalConfig, int uid) {
        WifiConfiguration newInternalConfig = new WifiConfiguration();
        int i = this.mNextNetworkId;
        this.mNextNetworkId = i + 1;
        newInternalConfig.networkId = i;
        setDefaultsInWifiConfiguration(newInternalConfig);
        mergeWithInternalWifiConfiguration(newInternalConfig, externalConfig);
        newInternalConfig.requirePMF = externalConfig.requirePMF;
        newInternalConfig.noInternetAccessExpected = externalConfig.noInternetAccessExpected;
        newInternalConfig.ephemeral = externalConfig.ephemeral;
        newInternalConfig.useExternalScores = externalConfig.useExternalScores;
        newInternalConfig.shared = externalConfig.shared;
        newInternalConfig.lastUpdateUid = uid;
        newInternalConfig.creatorUid = uid;
        String nameForUid = this.mContext.getPackageManager().getNameForUid(uid);
        newInternalConfig.lastUpdateName = nameForUid;
        newInternalConfig.creatorName = nameForUid;
        String createDebugTimeStampString = createDebugTimeStampString(this.mClock.getWallClockMillis());
        newInternalConfig.updateTime = createDebugTimeStampString;
        newInternalConfig.creationTime = createDebugTimeStampString;
        return newInternalConfig;
    }

    private WifiConfiguration updateExistingInternalWifiConfigurationFromExternal(WifiConfiguration internalConfig, WifiConfiguration externalConfig, int uid) {
        WifiConfiguration newInternalConfig = new WifiConfiguration(internalConfig);
        mergeWithInternalWifiConfiguration(newInternalConfig, externalConfig);
        newInternalConfig.lastUpdateUid = uid;
        newInternalConfig.lastUpdateName = this.mContext.getPackageManager().getNameForUid(uid);
        newInternalConfig.updateTime = createDebugTimeStampString(this.mClock.getWallClockMillis());
        return newInternalConfig;
    }

    private NetworkUpdateResult addOrUpdateNetworkInternal(WifiConfiguration config, int uid) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Adding/Updating network " + config.getPrintableSsid());
        }
        WifiConfiguration newInternalConfig = null;
        WifiConfiguration existingInternalConfig = getInternalConfiguredNetwork(config);
        boolean hasCredentialChanged = true;
        if (existingInternalConfig == null) {
            if (!WifiConfigurationUtil.validate(config, true)) {
                Log.e(TAG, "Cannot add network with invalid config");
                return new NetworkUpdateResult(-1);
            }
            newInternalConfig = createNewInternalWifiConfigurationFromExternal(config, uid);
            existingInternalConfig = getInternalConfiguredNetwork(newInternalConfig.configKey());
        }
        if (existingInternalConfig != null) {
            if (!WifiConfigurationUtil.validate(config, false)) {
                Log.e(TAG, "Cannot update network with invalid config");
                return new NetworkUpdateResult(-1);
            } else if (!canModifyNetwork(existingInternalConfig, uid)) {
                Log.e(TAG, "UID " + uid + " does not have permission to update configuration " + config.configKey());
                return new NetworkUpdateResult(-1);
            } else {
                newInternalConfig = updateExistingInternalWifiConfigurationFromExternal(existingInternalConfig, config, uid);
            }
        }
        if (WifiConfigurationUtil.hasProxyChanged(existingInternalConfig, newInternalConfig) && !canModifyProxySettings(uid)) {
            Log.e(TAG, "UID " + uid + " does not have permission to modify proxy Settings " + config.configKey() + ". Must have NETWORK_SETTINGS, or be device or profile owner.");
            return new NetworkUpdateResult(-1);
        } else if (config.enterpriseConfig != null && config.enterpriseConfig.getEapMethod() != -1 && !config.isPasspoint() && !this.mWifiKeyStore.updateNetworkKeys(newInternalConfig, existingInternalConfig)) {
            return new NetworkUpdateResult(-1);
        } else {
            boolean newNetwork = existingInternalConfig == null;
            boolean hasIpChanged = newNetwork || WifiConfigurationUtil.hasIpChanged(existingInternalConfig, newInternalConfig);
            boolean hasProxyChanged = newNetwork || WifiConfigurationUtil.hasProxyChanged(existingInternalConfig, newInternalConfig);
            if (!newNetwork && !WifiConfigurationUtil.hasCredentialChanged(existingInternalConfig, newInternalConfig)) {
                hasCredentialChanged = false;
            }
            if (hasCredentialChanged) {
                newInternalConfig.getNetworkSelectionStatus().setHasEverConnected(false);
            }
            if (config.isPortalConnect) {
                newInternalConfig.isPortalConnect = config.isPortalConnect;
                WifiInjector.getInstance().getWifiStateMachine().updateLastPortalConnect(config);
            }
            if (config.isHiLinkNetwork) {
                newInternalConfig.isHiLinkNetwork = config.isHiLinkNetwork;
            }
            if (config.enterpriseConfig != null) {
                int eapMethod = config.enterpriseConfig.getEapMethod();
                if (eapMethod == 4 || eapMethod == 5 || eapMethod == 6) {
                    newInternalConfig.enterpriseConfig.setEapSubId(config.enterpriseConfig.getEapSubId());
                }
            }
            if (config.wapiPskTypeBcm != -1) {
                newInternalConfig.wapiPskTypeBcm = config.wapiPskTypeBcm;
            }
            if (!TextUtils.isEmpty(config.wapiAsCertBcm)) {
                newInternalConfig.wapiAsCertBcm = NativeUtil.removeEnclosingQuotes(config.wapiAsCertBcm);
            }
            if (!TextUtils.isEmpty(config.wapiUserCertBcm)) {
                newInternalConfig.wapiUserCertBcm = NativeUtil.removeEnclosingQuotes(config.wapiUserCertBcm);
            }
            try {
                this.mConfiguredNetworks.put(newInternalConfig);
                if (this.mDeletedEphemeralSSIDs.remove(config.SSID) && this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "Removed from ephemeral blacklist: " + config.SSID);
                }
                this.mBackupManagerProxy.notifyDataChanged();
                NetworkUpdateResult result = new NetworkUpdateResult(hasIpChanged, hasProxyChanged, hasCredentialChanged);
                result.setIsNewNetwork(newNetwork);
                result.setNetworkId(newInternalConfig.networkId);
                localLog("addOrUpdateNetworkInternal: added/updated config. netId=" + newInternalConfig.networkId + " configKey=" + newInternalConfig.configKey() + " uid=" + Integer.toString(newInternalConfig.creatorUid) + " name=" + newInternalConfig.creatorName);
                return result;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Failed to add network to config map", e);
                return new NetworkUpdateResult(-1);
            }
        }
    }

    public NetworkUpdateResult addOrUpdateNetwork(WifiConfiguration config, int uid) {
        int i;
        if (!doesUidBelongToCurrentUser(uid)) {
            Log.e(TAG, "UID " + uid + " not visible to the current user");
            return new NetworkUpdateResult(-1);
        } else if (config == null) {
            Log.e(TAG, "Cannot add/update network with null config");
            return new NetworkUpdateResult(-1);
        } else if (this.mPendingStoreRead) {
            Log.e(TAG, "Cannot add/update network before store is read!");
            return new NetworkUpdateResult(-1);
        } else {
            config.defendSsid();
            NetworkUpdateResult result = addOrUpdateNetworkInternal(config, uid);
            if (!result.isSuccess()) {
                Log.e(TAG, "Failed to add/update network " + config.getPrintableSsid());
                return result;
            }
            if (config.hiddenSSID) {
                Log.d(TAG, "set mCurrentHiddenNetId=" + result.getNetworkId() + ", SSID=" + config.SSID);
                mCurrentHiddenNetId = result.getNetworkId();
            }
            WifiConfiguration newConfig = getInternalConfiguredNetwork(result.getNetworkId());
            if (result.isNewNetwork()) {
                i = 0;
            } else {
                i = 2;
            }
            sendConfiguredNetworkChangedBroadcast(newConfig, i);
            if (!config.ephemeral && !config.isPasspoint()) {
                saveToStore(true);
                if (this.mListener != null) {
                    if (result.isNewNetwork()) {
                        this.mListener.onSavedNetworkAdded(newConfig.networkId);
                    } else {
                        this.mListener.onSavedNetworkUpdated(newConfig.networkId);
                    }
                }
            }
            return result;
        }
    }

    private boolean removeNetworkInternal(WifiConfiguration config, int uid) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Removing network " + config.getPrintableSsid());
        }
        if (!(config.isPasspoint() || config.enterpriseConfig == null || config.enterpriseConfig.getEapMethod() == -1)) {
            Log.d(TAG, "removeNetworkInternal: skip remove keys.");
        }
        removeConnectChoiceFromAllNetworks(config.configKey());
        this.mConfiguredNetworks.remove(config.networkId);
        this.mScanDetailCaches.remove(Integer.valueOf(config.networkId));
        this.mBackupManagerProxy.notifyDataChanged();
        localLog("removeNetworkInternal: removed config. netId=" + config.networkId + " configKey=" + config.configKey() + " uid=" + Integer.toString(uid) + " name=" + this.mContext.getPackageManager().getNameForUid(uid));
        return true;
    }

    public boolean removeNetwork(int networkId, int uid) {
        if (!doesUidBelongToCurrentUser(uid)) {
            Log.e(TAG, "UID " + uid + " not visible to the current user");
            return false;
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        if (!canModifyNetwork(config, uid)) {
            Log.e(TAG, "UID " + uid + " does not have permission to delete configuration " + config.configKey());
            return false;
        } else if (!removeNetworkInternal(config, uid)) {
            Log.e(TAG, "Failed to remove network " + config.getPrintableSsid());
            return false;
        } else {
            if (networkId == this.mLastSelectedNetworkId) {
                clearLastSelectedNetwork();
            }
            sendConfiguredNetworkChangedBroadcast(config, 1);
            if (!config.ephemeral && !config.isPasspoint()) {
                saveToStore(true);
                if (this.mListener != null) {
                    this.mListener.onSavedNetworkRemoved(networkId);
                }
            }
            return true;
        }
    }

    public Set<Integer> removeNetworksForApp(ApplicationInfo app) {
        if (app == null || app.packageName == null) {
            return Collections.emptySet();
        }
        Log.d(TAG, "Remove all networks for app " + app);
        Set<Integer> removedNetworks = new ArraySet<>();
        for (WifiConfiguration config : (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0])) {
            if (app.uid == config.creatorUid && app.packageName.equals(config.creatorName)) {
                localLog("Removing network " + config.SSID + ", application \"" + app.packageName + "\" uninstalled from user " + UserHandle.getUserId(app.uid));
                if (removeNetwork(config.networkId, this.mSystemUiUid)) {
                    removedNetworks.add(Integer.valueOf(config.networkId));
                }
            }
        }
        return removedNetworks;
    }

    /* access modifiers changed from: package-private */
    public Set<Integer> removeNetworksForUser(int userId) {
        Log.d(TAG, "Remove all networks for user " + userId);
        Set<Integer> removedNetworks = new ArraySet<>();
        for (WifiConfiguration config : (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0])) {
            if (userId == UserHandle.getUserId(config.creatorUid)) {
                localLog("Removing network " + config.SSID + ", user " + userId + " removed");
                if (removeNetwork(config.networkId, this.mSystemUiUid)) {
                    removedNetworks.add(Integer.valueOf(config.networkId));
                }
            }
        }
        return removedNetworks;
    }

    public boolean removeAllEphemeralOrPasspointConfiguredNetworks() {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Removing all passpoint or ephemeral configured networks");
        }
        boolean didRemove = false;
        for (WifiConfiguration config : (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0])) {
            if (config.isPasspoint()) {
                Log.d(TAG, "Removing passpoint network config " + config.configKey());
                removeNetwork(config.networkId, this.mSystemUiUid);
                didRemove = true;
            } else if (config.ephemeral) {
                Log.d(TAG, "Removing ephemeral network config " + config.configKey());
                removeNetwork(config.networkId, this.mSystemUiUid);
                didRemove = true;
            }
        }
        return didRemove;
    }

    private void setNetworkSelectionEnabled(WifiConfiguration config) {
        WifiConfiguration.NetworkSelectionStatus status = config.getNetworkSelectionStatus();
        status.setNetworkSelectionStatus(0);
        status.setDisableTime(-1);
        status.setNetworkSelectionDisableReason(0);
        status.clearDisableReasonCounter();
        if (this.mListener != null) {
            this.mListener.onSavedNetworkEnabled(config.networkId);
        }
    }

    private void setNetworkSelectionTemporarilyDisabled(WifiConfiguration config, int disableReason) {
        WifiConfiguration.NetworkSelectionStatus status = config.getNetworkSelectionStatus();
        status.setNetworkSelectionStatus(1);
        status.setDisableTime(this.mClock.getElapsedSinceBootMillis());
        status.setNetworkSelectionDisableReason(disableReason);
        if (this.mListener != null) {
            this.mListener.onSavedNetworkTemporarilyDisabled(config.networkId, disableReason);
        }
    }

    private void setNetworkSelectionPermanentlyDisabled(WifiConfiguration config, int disableReason) {
        WifiConfiguration.NetworkSelectionStatus status = config.getNetworkSelectionStatus();
        status.setNetworkSelectionStatus(2);
        status.setDisableTime(-1);
        status.setNetworkSelectionDisableReason(disableReason);
        if (this.mListener != null) {
            this.mListener.onSavedNetworkPermanentlyDisabled(config.networkId, disableReason);
        }
    }

    private void setNetworkStatus(WifiConfiguration config, int status) {
        config.status = status;
        sendConfiguredNetworkChangedBroadcast(config, 2);
    }

    private boolean setNetworkSelectionStatus(WifiConfiguration config, int reason) {
        WifiConfiguration.NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (reason < 0 || reason >= 17) {
            Log.e(TAG, "Invalid Network disable reason " + reason);
            return false;
        }
        if (reason == 0) {
            setNetworkSelectionEnabled(config);
            setNetworkStatus(config, 2);
        } else if (reason < 8) {
            setNetworkSelectionTemporarilyDisabled(config, reason);
            HwWifiCHRService mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
            if (mHwWifiCHRService != null) {
                Bundle data = new Bundle();
                data.putBoolean("protalflag", config.portalNetwork);
                mHwWifiCHRService.uploadDFTEvent(3, data);
                Log.d(TAG, "chr, trigger disable network , reason=" + reason);
                if (2 == reason) {
                    mHwWifiCHRService.updateWifiException(83, "");
                }
                if (3 == reason) {
                    mHwWifiCHRService.updateWifiException(82, "");
                }
                if (4 == reason) {
                    mHwWifiCHRService.updateWifiException(84, "");
                }
            }
        } else if (reason == 16) {
            Log.d(TAG, "setNetworkSelectionTemporarilyDisabled since " + reason);
            setNetworkSelectionTemporarilyDisabled(config, reason);
        } else if (reason == 11) {
            setNetworkSelectionTemporarilyDisabled(config, reason);
            setNetworkStatus(config, 1);
        } else {
            setNetworkSelectionPermanentlyDisabled(config, reason);
            setNetworkStatus(config, 1);
        }
        localLog("setNetworkSelectionStatus: configKey=" + config.configKey() + " networkStatus=" + networkStatus.getNetworkStatusString() + " disableReason=" + networkStatus.getNetworkDisableReasonString() + " at=" + createDebugTimeStampString(this.mClock.getWallClockMillis()));
        saveToStore(false);
        return true;
    }

    private boolean updateNetworkSelectionStatus(WifiConfiguration config, int reason) {
        WifiConfiguration.NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (reason != 0) {
            networkStatus.incrementDisableReasonCounter(reason);
            int disableReasonCounter = networkStatus.getDisableReasonCounter(reason);
            int disableReasonThreshold = NETWORK_SELECTION_DISABLE_THRESHOLD[reason];
            if (disableReasonCounter < disableReasonThreshold) {
                if (this.mVerboseLoggingEnabled || reason < 8 || reason == 16) {
                    Log.d(TAG, "Disable counter for network " + config.getPrintableSsid() + " for reason " + WifiConfiguration.NetworkSelectionStatus.getNetworkDisableReasonString(reason) + " is " + networkStatus.getDisableReasonCounter(reason) + " and threshold is " + disableReasonThreshold);
                }
                return true;
            }
        }
        return setNetworkSelectionStatus(config, reason);
    }

    public boolean updateNetworkSelectionStatus(int networkId, int reason) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        return updateNetworkSelectionStatus(config, reason);
    }

    public boolean updateNetworkNotRecommended(int networkId, boolean notRecommended) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.getNetworkSelectionStatus().setNotRecommended(notRecommended);
        if (this.mVerboseLoggingEnabled) {
            localLog("updateNetworkRecommendation: configKey=" + config.configKey() + " notRecommended=" + notRecommended);
        }
        saveToStore(false);
        return true;
    }

    private boolean tryEnableNetwork(WifiConfiguration config) {
        WifiConfiguration.NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (networkStatus.isNetworkTemporaryDisabled()) {
            long timeDifferenceMs = this.mClock.getElapsedSinceBootMillis() - networkStatus.getDisableTime();
            int disableReason = networkStatus.getNetworkSelectionDisableReason();
            long disableTimeoutMs = (long) NETWORK_SELECTION_DISABLE_TIMEOUT_MS[disableReason];
            if (this.mCust != null && this.mCust.isDeleteReenableAutoJoin() && disableReason == 2) {
                disableTimeoutMs = 2147483647L;
            }
            if (timeDifferenceMs >= disableTimeoutMs) {
                return updateNetworkSelectionStatus(config, 0);
            }
        } else if (networkStatus.isDisabledByReason(12)) {
            return updateNetworkSelectionStatus(config, 0);
        }
        return false;
    }

    public boolean tryEnableNetwork(int networkId) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        return tryEnableNetwork(config);
    }

    public boolean enableNetwork(int networkId, boolean disableOthers, int uid) {
        if (disableOthers) {
            setLastSelectedNetwork(networkId);
        }
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Enabling network " + networkId + " (disableOthers " + disableOthers + ")");
        }
        if (!doesUidBelongToCurrentUser(uid)) {
            Log.e(TAG, "UID " + uid + " not visible to the current user");
            return false;
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null || HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(config, true)) {
            return false;
        }
        if (!canModifyNetwork(config, uid)) {
            Log.e(TAG, "UID " + uid + " does not have permission to update configuration " + config.configKey());
            return false;
        } else if (!updateNetworkSelectionStatus(networkId, 0)) {
            return false;
        } else {
            saveToStore(true);
            return true;
        }
    }

    public boolean disableNetwork(int networkId, int uid) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Disabling network " + networkId);
        }
        if (!doesUidBelongToCurrentUser(uid)) {
            Log.e(TAG, "UID " + uid + " not visible to the current user");
            return false;
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        if (!canModifyNetwork(config, uid)) {
            Log.e(TAG, "UID " + uid + " does not have permission to update configuration " + config.configKey());
            return false;
        }
        int reason = 11;
        int appId = UserHandle.getAppId(uid);
        if (appId == 0 || appId == 1000) {
            reason = 14;
        }
        String packageName = this.mContext.getPackageManager().getNameForUid(uid);
        Log.d(TAG, "updateNetworkSelectionStatus:" + reason + "  " + packageName);
        if (!updateNetworkSelectionStatus(networkId, reason)) {
            return false;
        }
        config.getNetworkSelectionStatus().setNetworkSelectionDisableName(packageName);
        if (networkId == this.mLastSelectedNetworkId) {
            clearLastSelectedNetwork();
        }
        saveToStore(true);
        return true;
    }

    public boolean updateLastConnectUid(int networkId, int uid) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Update network last connect UID for " + networkId);
        }
        if (!doesUidBelongToCurrentUser(uid)) {
            Log.e(TAG, "UID " + uid + " not visible to the current user");
            return false;
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.lastConnectUid = uid;
        return true;
    }

    public boolean updateNetworkAfterConnect(int networkId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Update network after connect for " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.lastConnected = this.mClock.getWallClockMillis();
        config.numAssociation++;
        config.getNetworkSelectionStatus().clearDisableReasonCounter();
        config.getNetworkSelectionStatus().setHasEverConnected(true);
        setNetworkStatus(config, 0);
        saveToStore(false);
        return true;
    }

    public boolean updateNetworkAfterDisconnect(int networkId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Update network after disconnect for " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.lastDisconnected = this.mClock.getWallClockMillis();
        if (config.status == 0) {
            setNetworkStatus(config, 2);
        }
        saveToStore(false);
        return true;
    }

    public boolean setNetworkDefaultGwMacAddress(int networkId, String macAddress) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.defaultGwMacAddress = macAddress;
        return true;
    }

    public boolean setNetworkRandomizedMacAddress(int networkId, MacAddress macAddress) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.setRandomizedMacAddress(macAddress);
        return true;
    }

    public boolean clearNetworkCandidateScanResult(int networkId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Clear network candidate scan result for " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.getNetworkSelectionStatus().setCandidate(null);
        config.getNetworkSelectionStatus().setCandidateScore(Integer.MIN_VALUE);
        config.getNetworkSelectionStatus().setSeenInLastQualifiedNetworkSelection(false);
        return true;
    }

    public boolean setNetworkCandidateScanResult(int networkId, ScanResult scanResult, int score) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Set network candidate scan result " + scanResult + " for " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.getNetworkSelectionStatus().setCandidate(scanResult);
        config.getNetworkSelectionStatus().setCandidateScore(score);
        config.getNetworkSelectionStatus().setSeenInLastQualifiedNetworkSelection(true);
        return true;
    }

    private void removeConnectChoiceFromAllNetworks(String connectChoiceConfigKey) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Removing connect choice from all networks " + connectChoiceConfigKey);
        }
        if (connectChoiceConfigKey != null) {
            for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
                String connectChoice = config.getNetworkSelectionStatus().getConnectChoice();
                if (TextUtils.equals(connectChoice, connectChoiceConfigKey)) {
                    Log.d(TAG, "remove connect choice:" + connectChoice + " from " + config.SSID + " : " + config.networkId);
                    clearNetworkConnectChoice(config.networkId);
                }
            }
        }
    }

    public boolean clearNetworkConnectChoice(int networkId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Clear network connect choice for " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.getNetworkSelectionStatus().setConnectChoice(null);
        config.getNetworkSelectionStatus().setConnectChoiceTimestamp(-1);
        saveToStore(false);
        return true;
    }

    public boolean setNetworkConnectChoice(int networkId, String connectChoiceConfigKey, long timestamp) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Set network connect choice " + connectChoiceConfigKey + " for " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.getNetworkSelectionStatus().setConnectChoice(connectChoiceConfigKey);
        config.getNetworkSelectionStatus().setConnectChoiceTimestamp(timestamp);
        saveToStore(false);
        return true;
    }

    public boolean incrementNetworkNoInternetAccessReports(int networkId) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.numNoInternetAccessReports++;
        return true;
    }

    public boolean setNetworkValidatedInternetAccess(int networkId, boolean validated) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.validatedInternetAccess = validated;
        config.numNoInternetAccessReports = 0;
        saveToStore(false);
        return true;
    }

    public boolean setNetworkNoInternetAccessExpected(int networkId, boolean expected) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.noInternetAccessExpected = expected;
        return true;
    }

    private void clearLastSelectedNetwork() {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Clearing last selected network");
        }
        this.mLastSelectedNetworkId = -1;
        this.mLastSelectedTimeStamp = -1;
    }

    private void setLastSelectedNetwork(int networkId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Setting last selected network to " + networkId);
        }
        this.mLastSelectedNetworkId = networkId;
        this.mLastSelectedTimeStamp = this.mClock.getElapsedSinceBootMillis();
    }

    public int getLastSelectedNetwork() {
        return this.mLastSelectedNetworkId;
    }

    public String getLastSelectedNetworkConfigKey() {
        if (this.mLastSelectedNetworkId == -1) {
            return "";
        }
        WifiConfiguration config = getInternalConfiguredNetwork(this.mLastSelectedNetworkId);
        if (config == null) {
            return "";
        }
        return config.configKey();
    }

    public long getLastSelectedTimeStamp() {
        return this.mLastSelectedTimeStamp;
    }

    public ScanDetailCache getScanDetailCacheForNetwork(int networkId) {
        return this.mScanDetailCaches.get(Integer.valueOf(networkId));
    }

    private ScanDetailCache getOrCreateScanDetailCacheForNetwork(WifiConfiguration config) {
        if (config == null) {
            return null;
        }
        ScanDetailCache cache = getScanDetailCacheForNetwork(config.networkId);
        if (cache == null && config.networkId != -1) {
            cache = new ScanDetailCache(config, SCAN_CACHE_ENTRIES_MAX_SIZE, 128);
            this.mScanDetailCaches.put(Integer.valueOf(config.networkId), cache);
        }
        return cache;
    }

    private void saveToScanDetailCacheForNetwork(WifiConfiguration config, ScanDetail scanDetail) {
        ScanResult scanResult = scanDetail.getScanResult();
        ScanDetailCache scanDetailCache = getOrCreateScanDetailCacheForNetwork(config);
        if (scanDetailCache == null) {
            Log.e(TAG, "Could not allocate scan cache for " + config.getPrintableSsid());
            return;
        }
        if (config.ephemeral) {
            scanResult.untrusted = true;
        }
        scanDetailCache.put(scanDetail);
        attemptNetworkLinking(config);
    }

    public WifiConfiguration getConfiguredNetworkForScanDetail(ScanDetail scanDetail) {
        ScanResult scanResult = scanDetail.getScanResult();
        WifiConfiguration config = null;
        if (scanResult == null) {
            Log.e(TAG, "No scan result found in scan detail");
            return null;
        }
        try {
            config = this.mConfiguredNetworks.getByScanResultForCurrentUser(scanResult);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed to lookup network from config map", e);
        }
        if (config != null && this.mVerboseLoggingEnabled) {
            Log.v(TAG, "getSavedNetworkFromScanDetail Found " + config.configKey() + " for " + scanResult.SSID + "[" + scanResult.capabilities + "]");
        }
        return config;
    }

    public WifiConfiguration getConfiguredNetworkForScanDetailAndCache(ScanDetail scanDetail) {
        WifiConfiguration network = getConfiguredNetworkForScanDetail(scanDetail);
        if (network == null) {
            return null;
        }
        saveToScanDetailCacheForNetwork(network, scanDetail);
        if (scanDetail.getNetworkDetail() != null && scanDetail.getNetworkDetail().getDtimInterval() > 0) {
            network.dtimInterval = scanDetail.getNetworkDetail().getDtimInterval();
        }
        return createExternalWifiConfiguration(network, true);
    }

    public void updateScanDetailCacheFromWifiInfo(WifiInfo info) {
        WifiConfiguration config = getInternalConfiguredNetwork(info.getNetworkId());
        ScanDetailCache scanDetailCache = getScanDetailCacheForNetwork(info.getNetworkId());
        if (!(config == null || scanDetailCache == null)) {
            ScanDetail scanDetail = scanDetailCache.getScanDetail(info.getBSSID());
            if (scanDetail != null) {
                ScanResult result = scanDetail.getScanResult();
                long previousSeen = result.seen;
                int previousRssi = result.level;
                scanDetail.setSeen();
                result.level = info.getRssi();
                long age = result.seen - previousSeen;
                if (previousSeen <= 0 || age <= 0 || age >= 40000 / 2) {
                    ScanDetail scanDetail2 = scanDetail;
                } else {
                    ScanDetailCache scanDetailCache2 = scanDetailCache;
                    ScanDetail scanDetail3 = scanDetail;
                    double alpha = 0.5d - (((double) age) / ((double) 40000));
                    result.level = (int) ((((double) result.level) * (1.0d - alpha)) + (((double) previousRssi) * alpha));
                }
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "Updating scan detail cache freq=" + result.frequency + " BSSID=" + result.BSSID + " RSSI=" + result.level + " for " + config.configKey());
                    return;
                }
                return;
            }
        }
    }

    public void updateScanDetailForNetwork(int networkId, ScanDetail scanDetail) {
        WifiConfiguration network = getInternalConfiguredNetwork(networkId);
        if (network != null) {
            saveToScanDetailCacheForNetwork(network, scanDetail);
        }
    }

    private boolean shouldNetworksBeLinked(WifiConfiguration network1, WifiConfiguration network2, ScanDetailCache scanDetailCache1, ScanDetailCache scanDetailCache2) {
        WifiConfiguration wifiConfiguration = network1;
        WifiConfiguration wifiConfiguration2 = network2;
        if (!this.mOnlyLinkSameCredentialConfigurations || TextUtils.equals(wifiConfiguration.preSharedKey, wifiConfiguration2.preSharedKey)) {
            if (wifiConfiguration.defaultGwMacAddress == null || wifiConfiguration2.defaultGwMacAddress == null) {
                if (!(scanDetailCache1 == null || scanDetailCache2 == null)) {
                    for (String abssid : scanDetailCache1.keySet()) {
                        for (String bbssid : scanDetailCache2.keySet()) {
                            if (abssid.regionMatches(true, 0, bbssid, 0, 16)) {
                                if (this.mVerboseLoggingEnabled) {
                                    Log.v(TAG, "shouldNetworksBeLinked link due to DBDC BSSID match " + wifiConfiguration2.SSID + " and " + wifiConfiguration.SSID + " bssida " + abssid + " bssidb " + bbssid);
                                }
                                return true;
                            }
                        }
                    }
                }
            } else if (wifiConfiguration.defaultGwMacAddress.equals(wifiConfiguration2.defaultGwMacAddress)) {
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "shouldNetworksBeLinked link due to same gw " + wifiConfiguration2.SSID + " and " + wifiConfiguration.SSID + " GW " + wifiConfiguration.defaultGwMacAddress);
                }
                return true;
            }
            return false;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "shouldNetworksBeLinked unlink due to password mismatch");
        }
        return false;
    }

    private void linkNetworks(WifiConfiguration network1, WifiConfiguration network2) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "linkNetworks will link " + network2.configKey() + " and " + network1.configKey());
        }
        if (network2.linkedConfigurations == null) {
            network2.linkedConfigurations = new HashMap();
        }
        if (network1.linkedConfigurations == null) {
            network1.linkedConfigurations = new HashMap();
        }
        network2.linkedConfigurations.put(network1.configKey(), 1);
        network1.linkedConfigurations.put(network2.configKey(), 1);
    }

    private void unlinkNetworks(WifiConfiguration network1, WifiConfiguration network2) {
        if (!(network2.linkedConfigurations == null || network2.linkedConfigurations.get(network1.configKey()) == null)) {
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "unlinkNetworks un-link " + network1.configKey() + " from " + network2.configKey());
            }
            network2.linkedConfigurations.remove(network1.configKey());
        }
        if (network1.linkedConfigurations != null && network1.linkedConfigurations.get(network2.configKey()) != null) {
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "unlinkNetworks un-link " + network2.configKey() + " from " + network1.configKey());
            }
            network1.linkedConfigurations.remove(network2.configKey());
        }
    }

    private void attemptNetworkLinking(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(1)) {
            ScanDetailCache scanDetailCache = getScanDetailCacheForNetwork(config.networkId);
            if (scanDetailCache == null || scanDetailCache.size() <= 6) {
                for (WifiConfiguration linkConfig : getInternalConfiguredNetworks()) {
                    if (!linkConfig.configKey().equals(config.configKey()) && !linkConfig.ephemeral && linkConfig.allowedKeyManagement.get(1)) {
                        ScanDetailCache linkScanDetailCache = getScanDetailCacheForNetwork(linkConfig.networkId);
                        if (linkScanDetailCache == null || linkScanDetailCache.size() <= 6) {
                            if (shouldNetworksBeLinked(config, linkConfig, scanDetailCache, linkScanDetailCache)) {
                                linkNetworks(config, linkConfig);
                            } else {
                                unlinkNetworks(config, linkConfig);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean addToChannelSetForNetworkFromScanDetailCache(Set<Integer> channelSet, ScanDetailCache scanDetailCache, long nowInMillis, long ageInMillis, int maxChannelSetSize) {
        Set<Integer> set;
        if (scanDetailCache != null && scanDetailCache.size() > 0) {
            for (ScanDetail scanDetail : scanDetailCache.values()) {
                ScanResult result = scanDetail.getScanResult();
                boolean valid = nowInMillis - result.seen < ageInMillis;
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "fetchChannelSetForNetwork has " + result.BSSID + " freq " + result.frequency + " age " + (nowInMillis - result.seen) + " ?=" + valid);
                }
                if (valid) {
                    set = channelSet;
                    set.add(Integer.valueOf(result.frequency));
                } else {
                    set = channelSet;
                }
                if (set.size() >= maxChannelSetSize) {
                    return false;
                }
            }
        }
        Set<Integer> set2 = channelSet;
        int i = maxChannelSetSize;
        return true;
    }

    public Set<Integer> fetchChannelSetForNetworkForPartialScan(int networkId, long ageInMillis, int homeChannelFreq) {
        long j;
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return null;
        }
        ScanDetailCache scanDetailCache = getScanDetailCacheForNetwork(networkId);
        if (scanDetailCache == null && config.linkedConfigurations == null) {
            Log.i(TAG, "No scan detail and linked configs associated with networkId " + networkId);
            return null;
        }
        int i = networkId;
        if (this.mVerboseLoggingEnabled) {
            StringBuilder dbg = new StringBuilder();
            dbg.append("fetchChannelSetForNetworkForPartialScan ageInMillis ");
            j = ageInMillis;
            dbg.append(j);
            dbg.append(" for ");
            dbg.append(config.configKey());
            dbg.append(" max ");
            dbg.append(this.mMaxNumActiveChannelsForPartialScans);
            if (scanDetailCache != null) {
                dbg.append(" bssids " + scanDetailCache.size());
            }
            if (config.linkedConfigurations != null) {
                dbg.append(" linked " + config.linkedConfigurations.size());
            }
            Log.v(TAG, dbg.toString());
        } else {
            j = ageInMillis;
        }
        Set<Integer> channelSet = new HashSet<>();
        if (homeChannelFreq > 0) {
            channelSet.add(Integer.valueOf(homeChannelFreq));
            if (channelSet.size() >= this.mMaxNumActiveChannelsForPartialScans) {
                return channelSet;
            }
        }
        long nowInMillis = this.mClock.getWallClockMillis();
        if (addToChannelSetForNetworkFromScanDetailCache(channelSet, scanDetailCache, nowInMillis, j, this.mMaxNumActiveChannelsForPartialScans) && config.linkedConfigurations != null) {
            Iterator it = config.linkedConfigurations.keySet().iterator();
            while (it.hasNext()) {
                String configKey = (String) it.next();
                WifiConfiguration linkedConfig = getInternalConfiguredNetwork(configKey);
                if (linkedConfig != null) {
                    String str = configKey;
                    WifiConfiguration wifiConfiguration = linkedConfig;
                    Iterator it2 = it;
                    if (!addToChannelSetForNetworkFromScanDetailCache(channelSet, getScanDetailCacheForNetwork(linkedConfig.networkId), nowInMillis, j, this.mMaxNumActiveChannelsForPartialScans)) {
                        break;
                    }
                    it = it2;
                }
            }
        }
        return channelSet;
    }

    public List<WifiScanner.PnoSettings.PnoNetwork> retrievePnoNetworkList() {
        List<WifiScanner.PnoSettings.PnoNetwork> pnoList = new ArrayList<>();
        List<WifiConfiguration> networks = new ArrayList<>(getInternalConfiguredNetworks());
        Iterator<WifiConfiguration> iter = networks.iterator();
        while (iter.hasNext()) {
            WifiConfiguration config = iter.next();
            if (config.ephemeral || config.isPasspoint() || config.getNetworkSelectionStatus().isNetworkPermanentlyDisabled() || config.getNetworkSelectionStatus().isNetworkTemporaryDisabled()) {
                iter.remove();
            }
        }
        Collections.sort(networks, sScanListComparator);
        for (WifiConfiguration config2 : networks) {
            pnoList.add(WifiConfigurationUtil.createPnoNetwork(config2));
        }
        return pnoList;
    }

    public List<WifiScanner.ScanSettings.HiddenNetwork> retrieveHiddenNetworkList() {
        List<WifiScanner.ScanSettings.HiddenNetwork> hiddenList = new ArrayList<>();
        List<WifiConfiguration> networks = new ArrayList<>(getInternalConfiguredNetworks());
        List<WifiConfiguration> restoreNetworks = new ArrayList<>();
        List<WifiConfiguration> currentHiddenConfig = new ArrayList<>();
        Iterator<WifiConfiguration> iter = networks.iterator();
        while (iter.hasNext()) {
            WifiConfiguration config = iter.next();
            if (!config.hiddenSSID) {
                iter.remove();
            } else if (mCurrentHiddenNetId == config.networkId) {
                Log.d(TAG, "retrieveHiddenNetworkList: get mCurrentHiddenNetId=" + config.networkId + ", SSID=" + config.SSID);
                mCurrentHiddenNetId = -1;
                currentHiddenConfig.add(0, config);
                iter.remove();
            }
        }
        Collections.sort(networks, sScanListTimeComparator);
        if (!currentHiddenConfig.isEmpty()) {
            networks.addAll(0, currentHiddenConfig);
        }
        int hideNetCount = networks.size();
        if (hideNetCount > 14) {
            for (int i = hideNetCount - 1; i >= 9; i--) {
                restoreNetworks.add(networks.remove(i));
            }
            Collections.sort(restoreNetworks, sScanListComparator);
            while (hideNetCount > 14) {
                WifiConfiguration updateConfig = restoreNetworks.remove(restoreNetworks.size() - 1);
                updateConfig.hiddenSSID = false;
                this.mConfiguredNetworks.put(updateConfig);
                Log.d(TAG, "retrieveHiddenNetworkList: update config:" + updateConfig.SSID + " to hiddenSSID:false");
                hideNetCount += -1;
            }
            for (WifiConfiguration config2 : restoreNetworks) {
                networks.add(config2);
            }
        }
        StringBuffer debugHiddenList = new StringBuffer("");
        for (WifiConfiguration config3 : networks) {
            hiddenList.add(new WifiScanner.ScanSettings.HiddenNetwork(TextUtils.isEmpty(config3.oriSsid) ? config3.SSID : config3.oriSsid));
            debugHiddenList.append(" " + config3.SSID);
        }
        Log.d(TAG, "retrieve sorted hiddenLists_SSID=" + debugHiddenList.toString());
        return hiddenList;
    }

    public boolean wasEphemeralNetworkDeleted(String ssid) {
        return this.mDeletedEphemeralSSIDs.contains(ssid);
    }

    public WifiConfiguration disableEphemeralNetwork(String ssid) {
        if (ssid == null) {
            return null;
        }
        WifiConfiguration foundConfig = null;
        Iterator<WifiConfiguration> it = getInternalConfiguredNetworks().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            WifiConfiguration config = it.next();
            if (config.ephemeral && TextUtils.equals(config.SSID, ssid)) {
                foundConfig = config;
                break;
            }
        }
        this.mDeletedEphemeralSSIDs.add(ssid);
        Log.d(TAG, "Forget ephemeral SSID " + ssid + " num=" + this.mDeletedEphemeralSSIDs.size());
        if (foundConfig != null) {
            Log.d(TAG, "Found ephemeral config in disableEphemeralNetwork: " + foundConfig.networkId);
        }
        return foundConfig;
    }

    public void resetSimNetworks(boolean simPresent) {
        if (this.mVerboseLoggingEnabled) {
            localLog("resetSimNetworks");
        }
        boolean hasSimConfiguration = false;
        if (getInternalConfiguredNetworks().size() == 0) {
            Log.d(TAG, "try to load from config store during resetSimNetworks");
            loadFromStore();
        }
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (TelephonyUtil.isSimConfig(config)) {
                Pair<String, String> currentIdentity = null;
                if (simPresent) {
                    currentIdentity = TelephonyUtil.getSimIdentity(this.mTelephonyManager, new TelephonyUtil(), config);
                }
                if (currentIdentity == null) {
                    Log.d(TAG, "Identity is null");
                    return;
                }
                config.enterpriseConfig.setIdentity((String) currentIdentity.first);
                if (config.enterpriseConfig.getEapMethod() != 0) {
                    config.enterpriseConfig.setAnonymousIdentity("");
                }
                hasSimConfiguration = true;
            }
        }
        this.mSimPresent = simPresent;
        if (hasSimConfiguration) {
            saveToStore(true);
        }
    }

    public boolean isSimPresent() {
        return this.mSimPresent;
    }

    public void enableSimNetworks() {
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (TelephonyUtil.isSimConfig(config)) {
                updateNetworkSelectionStatus(config, 0);
            }
        }
    }

    public boolean needsUnlockedKeyStore() {
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (WifiConfigurationUtil.isConfigForEapNetwork(config)) {
                WifiKeyStore wifiKeyStore = this.mWifiKeyStore;
                if (WifiKeyStore.needsSoftwareBackedKeyStore(config.enterpriseConfig)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void handleUserUnlockOrSwitch(int userId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Loading from store after user switch/unlock for " + userId);
        }
        if (loadFromUserStoreAfterUnlockOrSwitch(userId)) {
            saveToStore(true);
            this.mPendingUnlockStoreRead = false;
        }
    }

    public Set<Integer> handleUserSwitch(int userId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Handling user switch for " + userId);
        }
        if (userId == this.mCurrentUserId) {
            Log.w(TAG, "User already in foreground " + userId);
            return new HashSet();
        } else if (this.mPendingStoreRead) {
            Log.wtf(TAG, "Unexpected user switch before store is read!");
            return new HashSet();
        } else {
            if (this.mUserManager.isUserUnlockingOrUnlocked(this.mCurrentUserId)) {
                saveToStore(true);
            }
            Set<Integer> removedNetworkIds = clearInternalUserData(this.mCurrentUserId);
            this.mConfiguredNetworks.setNewUser(userId);
            this.mCurrentUserId = userId;
            if (this.mUserManager.isUserUnlockingOrUnlocked(this.mCurrentUserId)) {
                handleUserUnlockOrSwitch(this.mCurrentUserId);
            } else {
                this.mPendingUnlockStoreRead = true;
                Log.i(TAG, "Waiting for user unlock to load from store");
            }
            return removedNetworkIds;
        }
    }

    public void handleUserUnlock(int userId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Handling user unlock for " + userId);
        }
        if (this.mPendingStoreRead) {
            Log.w(TAG, "Ignore user unlock until store is read!");
            this.mDeferredUserUnlockRead = true;
            return;
        }
        if (userId == this.mCurrentUserId && this.mPendingUnlockStoreRead) {
            handleUserUnlockOrSwitch(this.mCurrentUserId);
        }
    }

    public void handleUserStop(int userId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Handling user stop for " + userId);
        }
        if (userId == this.mCurrentUserId && this.mUserManager.isUserUnlockingOrUnlocked(this.mCurrentUserId)) {
            saveToStore(true);
            clearInternalUserData(this.mCurrentUserId);
        }
    }

    private void clearInternalData() {
        localLog("clearInternalData: Clearing all internal data");
        this.mConfiguredNetworks.clear();
        this.mDeletedEphemeralSSIDs.clear();
        this.mScanDetailCaches.clear();
        clearLastSelectedNetwork();
    }

    private Set<Integer> clearInternalUserData(int userId) {
        localLog("clearInternalUserData: Clearing user internal data for " + userId);
        Set<Integer> removedNetworkIds = new HashSet<>();
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (!config.shared && WifiConfigurationUtil.doesUidBelongToAnyProfile(config.creatorUid, this.mUserManager.getProfiles(userId))) {
                removedNetworkIds.add(Integer.valueOf(config.networkId));
                localLog("clearInternalUserData: removed config. netId=" + config.networkId + " configKey=" + config.configKey());
                this.mConfiguredNetworks.remove(config.networkId);
            }
        }
        this.mDeletedEphemeralSSIDs.clear();
        this.mScanDetailCaches.clear();
        clearLastSelectedNetwork();
        return removedNetworkIds;
    }

    private void loadInternalDataFromSharedStore(List<WifiConfiguration> configurations) {
        for (WifiConfiguration configuration : configurations) {
            int i = this.mNextNetworkId;
            this.mNextNetworkId = i + 1;
            configuration.networkId = i;
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "Adding network from shared store " + configuration.configKey());
            }
            try {
                this.mConfiguredNetworks.put(configuration);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Failed to add network to config map", e);
            }
        }
    }

    private void loadInternalDataFromUserStore(List<WifiConfiguration> configurations, Set<String> deletedEphemeralSSIDs) {
        for (WifiConfiguration configuration : configurations) {
            int i = this.mNextNetworkId;
            this.mNextNetworkId = i + 1;
            configuration.networkId = i;
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "Adding network from user store " + configuration.configKey());
            }
            try {
                this.mConfiguredNetworks.put(configuration);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Failed to add network to config map", e);
            }
        }
        for (String ssid : deletedEphemeralSSIDs) {
            this.mDeletedEphemeralSSIDs.add(ssid);
        }
    }

    private void loadInternalData(List<WifiConfiguration> sharedConfigurations, List<WifiConfiguration> userConfigurations, Set<String> deletedEphemeralSSIDs) {
        clearInternalData();
        loadInternalDataFromSharedStore(sharedConfigurations);
        loadInternalDataFromUserStore(userConfigurations, deletedEphemeralSSIDs);
        if (this.mConfiguredNetworks.sizeForAllUsers() == 0) {
            Log.w(TAG, "No stored networks found.");
        }
        sendConfiguredNetworksChangedBroadcast();
        this.mPendingStoreRead = false;
    }

    public boolean migrateFromLegacyStore() {
        if (!this.mWifiConfigStoreLegacy.areStoresPresent()) {
            Log.d(TAG, "Legacy store files not found. No migration needed!");
            return true;
        } else if (this.mWifiConfigStore.areStoresPresent()) {
            Log.d(TAG, "New store files found. No migration needed! Remove legacy store files");
            this.mWifiConfigStoreLegacy.removeStores();
            return true;
        } else {
            WifiConfigStoreLegacy.WifiConfigStoreDataLegacy storeData = this.mWifiConfigStoreLegacy.read();
            Log.d(TAG, "Reading from legacy store completed");
            loadInternalData(storeData.getConfigurations(), new ArrayList(), storeData.getDeletedEphemeralSSIDs());
            if (this.mDeferredUserUnlockRead) {
                this.mWifiConfigStore.setUserStore(WifiConfigStore.createUserFile(this.mCurrentUserId));
                this.mDeferredUserUnlockRead = false;
            }
            if (!saveToStore(true)) {
                return false;
            }
            this.mWifiConfigStoreLegacy.removeStores();
            Log.d(TAG, "Migration from legacy store completed");
            return true;
        }
    }

    public boolean loadFromStore() {
        if (this.mDeferredUserUnlockRead) {
            Log.i(TAG, "Handling user unlock before loading from store.");
            this.mWifiConfigStore.setUserStore(WifiConfigStore.createUserFile(this.mCurrentUserId));
            this.mDeferredUserUnlockRead = false;
        }
        if (!this.mWifiConfigStore.areStoresPresent()) {
            Log.d(TAG, "New store files not found. No saved networks loaded!");
            if (!this.mWifiConfigStoreLegacy.areStoresPresent()) {
                this.mPendingStoreRead = false;
            }
            return true;
        }
        try {
            this.mWifiConfigStore.read();
            loadInternalData(this.mNetworkListStoreData.getSharedConfigurations(), this.mNetworkListStoreData.getUserConfigurations(), this.mDeletedEphemeralSsidsStoreData.getSsidList());
            if (HuaweiTelephonyConfigs.isChinaMobile()) {
                initLastPriority();
                Log.d(TAG, "after init mLastPriority is " + this.mLastPriority);
            }
            return true;
        } catch (IOException e) {
            Log.wtf(TAG, "Reading from new store failed. All saved networks are lost!", e);
            return false;
        } catch (XmlPullParserException e2) {
            Log.wtf(TAG, "XML deserialization of store failed. All saved networks are lost!", e2);
            return false;
        }
    }

    private void initLastPriority() {
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
            if (config.priority > this.mLastPriority) {
                this.mLastPriority = config.priority;
            }
        }
    }

    public boolean updatePriority(WifiConfiguration config, int uid) {
        Log.d(TAG, "updatePriority" + config.networkId);
        if (config.networkId == -1) {
            return false;
        }
        if (!WifiConfigurationUtil.isVisibleToAnyProfile(config, this.mUserManager.getProfiles(this.mCurrentUserId))) {
            Log.d(TAG, "updatePriority " + Integer.toString(config.networkId) + ": Network config is not visible to current user.");
            return false;
        }
        if (this.mLastPriority == -1 || this.mLastPriority > 1000000) {
            Log.d(TAG, "Need to reset the priority, mLastPriority:" + this.mLastPriority);
            for (WifiConfiguration config2 : this.mConfiguredNetworks.valuesForCurrentUser()) {
                if (config2.networkId != -1) {
                    config2.priority = 0;
                    addOrUpdateNetwork(config2, uid);
                }
            }
            this.mLastPriority = 0;
        }
        int i = this.mLastPriority + 1;
        this.mLastPriority = i;
        config.priority = i;
        addOrUpdateNetwork(config, uid);
        return true;
    }

    public boolean loadFromUserStoreAfterUnlockOrSwitch(int userId) {
        try {
            this.mWifiConfigStore.switchUserStoreAndRead(WifiConfigStore.createUserFile(userId));
            loadInternalDataFromUserStore(this.mNetworkListStoreData.getUserConfigurations(), this.mDeletedEphemeralSsidsStoreData.getSsidList());
            return true;
        } catch (IOException e) {
            Log.wtf(TAG, "Reading from new store failed. All saved private networks are lost!", e);
            return false;
        } catch (XmlPullParserException e2) {
            Log.wtf(TAG, "XML deserialization of store failed. All saved private networks arelost!", e2);
            return false;
        }
    }

    public boolean saveToStore(boolean forceWrite) {
        if (this.mPendingStoreRead) {
            Log.e(TAG, "Cannot save to store before store is read!");
            return false;
        }
        ArrayList<WifiConfiguration> sharedConfigurations = new ArrayList<>();
        ArrayList<WifiConfiguration> userConfigurations = new ArrayList<>();
        List<Integer> legacyPasspointNetId = new ArrayList<>();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForAllUsers()) {
            if (!config.ephemeral && (!config.isPasspoint() || config.isLegacyPasspointConfig)) {
                if (config.isLegacyPasspointConfig && WifiConfigurationUtil.doesUidBelongToAnyProfile(config.creatorUid, this.mUserManager.getProfiles(this.mCurrentUserId))) {
                    legacyPasspointNetId.add(Integer.valueOf(config.networkId));
                    if (!PasspointManager.addLegacyPasspointConfig(config)) {
                        Log.e(TAG, "Failed to migrate legacy Passpoint config: " + config.FQDN);
                    }
                } else if (config.shared || !WifiConfigurationUtil.doesUidBelongToAnyProfile(config.creatorUid, this.mUserManager.getProfiles(this.mCurrentUserId))) {
                    sharedConfigurations.add(config);
                } else {
                    userConfigurations.add(config);
                }
            }
        }
        for (Integer intValue : legacyPasspointNetId) {
            this.mConfiguredNetworks.remove(intValue.intValue());
        }
        this.mNetworkListStoreData.setSharedConfigurations(sharedConfigurations);
        this.mNetworkListStoreData.setUserConfigurations(userConfigurations);
        this.mDeletedEphemeralSsidsStoreData.setSsidList(this.mDeletedEphemeralSSIDs);
        try {
            this.mWifiConfigStore.write(forceWrite);
            return true;
        } catch (IOException e) {
            Log.wtf(TAG, "Writing to store failed. Saved networks maybe lost!", e);
            return false;
        } catch (XmlPullParserException e2) {
            Log.wtf(TAG, "XML serialization for store failed. Saved networks maybe lost!", e2);
            return false;
        }
    }

    private void localLog(String s) {
        if (this.mLocalLog != null) {
            this.mLocalLog.log(s);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WifiConfigManager");
        pw.println("WifiConfigManager - Log Begin ----");
        this.mLocalLog.dump(fd, pw, args);
        pw.println("WifiConfigManager - Log End ----");
        pw.println("WifiConfigManager - Configured networks Begin ----");
        for (WifiConfiguration network : getInternalConfiguredNetworks()) {
            pw.println(network);
        }
        pw.println("WifiConfigManager - Configured networks End ----");
        pw.println("WifiConfigManager - Next network ID to be allocated " + this.mNextNetworkId);
        pw.println("WifiConfigManager - Last selected network ID " + this.mLastSelectedNetworkId);
    }

    private boolean canModifyProxySettings(int uid) {
        DevicePolicyManagerInternal dpmi = this.mWifiPermissionsWrapper.getDevicePolicyManagerInternal();
        boolean isUidProfileOwner = dpmi != null && dpmi.isActiveAdminWithPolicy(uid, -1);
        boolean isUidDeviceOwner = dpmi != null && dpmi.isActiveAdminWithPolicy(uid, -2);
        boolean hasNetworkSettingsPermission = this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid);
        if (isUidDeviceOwner || isUidProfileOwner || hasNetworkSettingsPermission) {
            return true;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "UID: " + uid + " cannot modify WifiConfiguration proxy settings. ConfigOverride=" + hasNetworkSettingsPermission + " DeviceOwner=" + isUidDeviceOwner + " ProfileOwner=" + isUidProfileOwner);
        }
        return false;
    }

    public void setOnSavedNetworkUpdateListener(OnSavedNetworkUpdateListener listener) {
        this.mListener = listener;
    }

    public void setRecentFailureAssociationStatus(int netId, int reason) {
        WifiConfiguration config = getInternalConfiguredNetwork(netId);
        if (config != null) {
            config.recentFailure.setAssociationStatus(reason);
        }
    }

    public void clearRecentFailureReason(int netId) {
        WifiConfiguration config = getInternalConfiguredNetwork(netId);
        if (config != null) {
            config.recentFailure.clear();
        }
    }
}
