package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner.PnoSettings.PnoNetwork;
import android.net.wifi.WifiScanner.ScanSettings.HiddenNetwork;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.Log;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.server.LocalServices;
import com.android.server.wifi.WifiConfigStoreLegacy.WifiConfigStoreDataLegacy;
import com.android.server.wifi.WifiConfigurationUtil.WifiConfigurationComparator;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.ScanResultUtil;
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
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParserException;

public class WifiConfigManager extends AbsWifiConfigManager {
    private static final boolean ALLOW_LOCKDOWN_CHECK_BYPASS = true;
    private static final boolean DISALLOW_LOCKDOWN_CHECK_BYPASS = false;
    protected static final boolean HWFLOW;
    private static boolean HWLOGW_E = true;
    public static final int LINK_CONFIGURATION_BSSID_MATCH_LENGTH = 16;
    public static final int LINK_CONFIGURATION_MAX_SCAN_CACHE_ENTRIES = 6;
    private static final int MAX_HIDDEN_NETWORKS_NUM = 14;
    private static final int MAX_NO_RESORT_HIDDEN_NETWORKS_NUM = 9;
    public static final int MAX_RX_PACKET_FOR_FULL_SCANS = 16;
    public static final int MAX_TX_PACKET_FOR_FULL_SCANS = 8;
    public static final int[] NETWORK_SELECTION_DISABLE_THRESHOLD = new int[]{-1, 1, 5, 5, 5, 5, 1, 6, 1, 1, 1, 1, 1, 1, 1, 5};
    public static final int[] NETWORK_SELECTION_DISABLE_TIMEOUT_MS = new int[]{Integer.MAX_VALUE, 900000, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS};
    public static final String PASSWORD_MASK = "*";
    public static final int SCAN_CACHE_ENTRIES_MAX_SIZE = 192;
    public static final int SCAN_CACHE_ENTRIES_TRIM_SIZE = 128;
    private static final int SCAN_RESULT_MAXIMUM_AGE_MS = 40000;
    public static final String SYSUI_PACKAGE_NAME = "com.android.systemui";
    public static final String TAG = "WifiConfigManager";
    private static int mCurrentHiddenNetId = -1;
    private static final WifiConfigurationComparator sScanListComparator = new WifiConfigurationComparator() {
        public int compareNetworksWithSameStatus(WifiConfiguration a, WifiConfiguration b) {
            if (a.numAssociation != b.numAssociation) {
                return Long.compare((long) b.numAssociation, (long) a.numAssociation);
            }
            return Boolean.compare(b.getNetworkSelectionStatus().getSeenInLastQualifiedNetworkSelection(), a.getNetworkSelectionStatus().getSeenInLastQualifiedNetworkSelection());
        }
    };
    private static final WifiConfigurationComparator sScanListTimeComparator = new WifiConfigurationComparator() {
        public int compareNetworksWithSameStatus(WifiConfiguration a, WifiConfiguration b) {
            return Long.compare(0 == b.lastHasInternetTimestamp ? b.lastConnected : b.lastHasInternetTimestamp, 0 == a.lastHasInternetTimestamp ? a.lastConnected : a.lastHasInternetTimestamp);
        }
    };
    private static boolean sVDBG = HWFLOW;
    private static boolean sVVDBG = HWFLOW;
    private int WIFI_CONFIGURE_EMPTY_FLAG;
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

        void onSavedNetworkPermanentlyDisabled(int i);

        void onSavedNetworkRemoved(int i);

        void onSavedNetworkTemporarilyDisabled(int i);

        void onSavedNetworkUpdated(int i);
    }

    static {
        boolean z;
        if (Log.HWINFO) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(TAG, 4);
        } else {
            z = false;
        }
        HWFLOW = z;
    }

    WifiConfigManager(Context context, Clock clock, UserManager userManager, TelephonyManager telephonyManager, WifiKeyStore wifiKeyStore, WifiConfigStore wifiConfigStore, WifiConfigStoreLegacy wifiConfigStoreLegacy, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper wifiPermissionsWrapper, NetworkListStoreData networkListStoreData, DeletedEphemeralSsidsStoreData deletedEphemeralSsidsStoreData) {
        this.mLocalLog = new LocalLog(ActivityManager.isLowRamDeviceStatic() ? 128 : 256);
        this.mVerboseLoggingEnabled = false;
        this.mCurrentUserId = 0;
        this.mPendingUnlockStoreRead = true;
        this.mPendingStoreRead = true;
        this.mDeferredUserUnlockRead = false;
        this.mNextNetworkId = 0;
        this.mSystemUiUid = -1;
        this.WIFI_CONFIGURE_EMPTY_FLAG = 0;
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
        this.mOnlyLinkSameCredentialConfigurations = this.mContext.getResources().getBoolean(17957060);
        this.mMaxNumActiveChannelsForPartialScans = this.mContext.getResources().getInteger(17694879);
        try {
            this.mSystemUiUid = this.mContext.getPackageManager().getPackageUidAsUser(SYSUI_PACKAGE_NAME, 1048576, 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Unable to resolve SystemUI's UID.");
        }
    }

    public static String createDebugTimeStampString(long wallClockMillis) {
        StringBuilder sb = new StringBuilder();
        sb.append("time=");
        Calendar.getInstance().setTimeInMillis(wallClockMillis);
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
            configuration.preSharedKey = PASSWORD_MASK;
        }
        if (configuration.wepKeys != null) {
            for (int i = 0; i < configuration.wepKeys.length; i++) {
                if (!TextUtils.isEmpty(configuration.wepKeys[i])) {
                    configuration.wepKeys[i] = PASSWORD_MASK;
                }
            }
        }
        if (!TextUtils.isEmpty(configuration.enterpriseConfig.getPassword())) {
            configuration.enterpriseConfig.setPassword(PASSWORD_MASK);
        }
    }

    private WifiConfiguration createExternalWifiConfiguration(WifiConfiguration configuration, boolean maskPasswords) {
        WifiConfiguration network = new WifiConfiguration(configuration);
        if (maskPasswords) {
            maskPasswordsInWifiConfiguration(network);
        }
        return network;
    }

    private List<WifiConfiguration> getConfiguredNetworks(boolean savedOnly, boolean maskPasswords) {
        List<WifiConfiguration> networks = new ArrayList();
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
            if (!(config == null || (config.getNetworkSelectionStatus().isNetworkEnabled() ^ 1) == 0 || !tryEnableNetwork(config))) {
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

    private Collection<WifiConfiguration> getInternalConfiguredNetworks() {
        return this.mConfiguredNetworks.valuesForCurrentUser();
    }

    private WifiConfiguration getInternalConfiguredNetwork(WifiConfiguration config) {
        WifiConfiguration internalConfig = this.mConfiguredNetworks.getForCurrentUser(config.networkId);
        if (internalConfig != null) {
            return internalConfig;
        }
        internalConfig = this.mConfiguredNetworks.getByConfigKeyForCurrentUser(config.configKey());
        if (internalConfig == null) {
            Log.e(TAG, "Cannot find network with networkId " + config.networkId + " or configKey " + config.configKey());
        }
        return internalConfig;
    }

    private WifiConfiguration getInternalConfiguredNetwork(int networkId) {
        if (networkId == -1) {
            Log.w(TAG, "Looking up network with invalid networkId -1");
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

    private boolean canModifyNetwork(WifiConfiguration config, int uid, boolean ignoreLockdown) {
        boolean z = true;
        if (config.isPasspoint() && uid == 1010) {
            return true;
        }
        if (config.enterpriseConfig != null && uid == 1010 && TelephonyUtil.isSimEapMethod(config.enterpriseConfig.getEapMethod())) {
            return true;
        }
        DevicePolicyManagerInternal dpmi = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        if (dpmi != null ? dpmi.isActiveAdminWithPolicy(uid, -2) : false) {
            return true;
        }
        boolean isCreator = config.creatorUid == uid;
        if (ignoreLockdown) {
            return this.mWifiPermissionsUtil.checkConfigOverridePermission(uid);
        }
        if (this.mContext.getPackageManager().hasSystemFeature("android.software.device_admin") && dpmi == null) {
            Log.w(TAG, "Error retrieving DPMI service.");
            return false;
        }
        if (dpmi != null ? dpmi.isActiveAdminWithPolicy(config.creatorUid, -2) : false) {
            if (Global.getInt(this.mContext.getContentResolver(), "wifi_device_owner_configs_lockdown", 0) != 0) {
                z = false;
            } else {
                z = this.mWifiPermissionsUtil.checkConfigOverridePermission(uid);
            }
            return z;
        }
        if (!isCreator) {
            z = this.mWifiPermissionsUtil.checkConfigOverridePermission(uid);
        }
        return z;
    }

    private boolean doesUidBelongToCurrentUser(int uid) {
        if (WifiConfigurationUtil.doesUidBelongToAnyProfile(uid, this.mUserManager.getProfiles(this.mCurrentUserId)) || uid == this.mSystemUiUid || uid == 1010) {
            return true;
        }
        return false;
    }

    private void mergeWithInternalWifiConfiguration(WifiConfiguration internalConfig, WifiConfiguration externalConfig) {
        if (externalConfig.SSID != null) {
            internalConfig.SSID = externalConfig.SSID;
        }
        internalConfig.priority = externalConfig.priority;
        internalConfig.getNetworkSelectionStatus().setNetworkSelectionStatus(externalConfig.getNetworkSelectionStatus().getNetworkSelectionStatus());
        internalConfig.oriSsid = externalConfig.oriSsid;
        if (externalConfig.BSSID != null) {
            internalConfig.BSSID = externalConfig.BSSID.toLowerCase();
        }
        internalConfig.isTempCreated = externalConfig.isTempCreated;
        internalConfig.hiddenSSID = externalConfig.hiddenSSID;
        internalConfig.cloudSecurityCheck = externalConfig.cloudSecurityCheck;
        if (!(externalConfig.preSharedKey == null || (externalConfig.preSharedKey.equals(PASSWORD_MASK) ^ 1) == 0)) {
            internalConfig.preSharedKey = externalConfig.preSharedKey;
        }
        if (externalConfig.wepKeys != null) {
            boolean hasWepKey = false;
            int i = 0;
            while (i < internalConfig.wepKeys.length) {
                if (!(externalConfig.wepKeys[i] == null || (externalConfig.wepKeys[i].equals(PASSWORD_MASK) ^ 1) == 0)) {
                    internalConfig.wepKeys[i] = externalConfig.wepKeys[i];
                    hasWepKey = true;
                }
                i++;
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
        if (!(externalConfig.allowedAuthAlgorithms == null || (externalConfig.allowedAuthAlgorithms.isEmpty() ^ 1) == 0)) {
            internalConfig.allowedAuthAlgorithms = (BitSet) externalConfig.allowedAuthAlgorithms.clone();
        }
        if (!(externalConfig.allowedProtocols == null || (externalConfig.allowedProtocols.isEmpty() ^ 1) == 0)) {
            internalConfig.allowedProtocols = (BitSet) externalConfig.allowedProtocols.clone();
        }
        if (!(externalConfig.allowedKeyManagement == null || (externalConfig.allowedKeyManagement.isEmpty() ^ 1) == 0)) {
            internalConfig.allowedKeyManagement = (BitSet) externalConfig.allowedKeyManagement.clone();
        }
        if (!(externalConfig.allowedPairwiseCiphers == null || (externalConfig.allowedPairwiseCiphers.isEmpty() ^ 1) == 0)) {
            internalConfig.allowedPairwiseCiphers = (BitSet) externalConfig.allowedPairwiseCiphers.clone();
        }
        if (!(externalConfig.allowedGroupCiphers == null || (externalConfig.allowedGroupCiphers.isEmpty() ^ 1) == 0)) {
            internalConfig.allowedGroupCiphers = (BitSet) externalConfig.allowedGroupCiphers.clone();
        }
        if (externalConfig.getIpConfiguration() != null) {
            IpAssignment ipAssignment = externalConfig.getIpAssignment();
            if (ipAssignment != IpAssignment.UNASSIGNED) {
                internalConfig.setIpAssignment(ipAssignment);
                if (ipAssignment == IpAssignment.STATIC) {
                    internalConfig.setStaticIpConfiguration(new StaticIpConfiguration(externalConfig.getStaticIpConfiguration()));
                }
            }
            ProxySettings proxySettings = externalConfig.getProxySettings();
            if (proxySettings != ProxySettings.UNASSIGNED) {
                internalConfig.setProxySettings(proxySettings);
                if (proxySettings == ProxySettings.PAC || proxySettings == ProxySettings.STATIC) {
                    internalConfig.setHttpProxy(new ProxyInfo(externalConfig.getHttpProxy()));
                }
            }
        }
        if (externalConfig.enterpriseConfig != null) {
            internalConfig.enterpriseConfig.copyFromExternal(externalConfig.enterpriseConfig, PASSWORD_MASK);
        }
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
        configuration.setIpAssignment(IpAssignment.DHCP);
        configuration.setProxySettings(ProxySettings.NONE);
        configuration.status = 1;
        configuration.getNetworkSelectionStatus().setNetworkSelectionStatus(2);
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
        newInternalConfig.meteredHint = externalConfig.meteredHint;
        newInternalConfig.useExternalScores = externalConfig.useExternalScores;
        newInternalConfig.shared = externalConfig.shared;
        newInternalConfig.lastUpdateUid = uid;
        newInternalConfig.creatorUid = uid;
        String nameForUid = this.mContext.getPackageManager().getNameForUid(uid);
        newInternalConfig.lastUpdateName = nameForUid;
        newInternalConfig.creatorName = nameForUid;
        nameForUid = createDebugTimeStampString(this.mClock.getWallClockMillis());
        newInternalConfig.updateTime = nameForUid;
        newInternalConfig.creationTime = nameForUid;
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
        if (existingInternalConfig == null) {
            newInternalConfig = createNewInternalWifiConfigurationFromExternal(config, uid);
            existingInternalConfig = getInternalConfiguredNetwork(newInternalConfig.configKey());
        }
        if (existingInternalConfig != null) {
            if (canModifyNetwork(existingInternalConfig, uid, false)) {
                newInternalConfig = updateExistingInternalWifiConfigurationFromExternal(existingInternalConfig, config, uid);
            } else {
                Log.e(TAG, "UID " + uid + " does not have permission to update configuration " + config.configKey());
                return new NetworkUpdateResult(-1);
            }
        }
        if (WifiConfigurationUtil.hasProxyChanged(existingInternalConfig, newInternalConfig) && (canModifyProxySettings(uid) ^ 1) != 0) {
            Log.e(TAG, "UID " + uid + " does not have permission to modify proxy Settings " + config.configKey() + ". Must have OVERRIDE_WIFI_CONFIG," + " or be device or profile owner.");
            return new NetworkUpdateResult(-1);
        } else if (config.enterpriseConfig != null && config.enterpriseConfig.getEapMethod() != -1 && (config.isPasspoint() ^ 1) != 0 && !this.mWifiKeyStore.updateNetworkKeys(newInternalConfig, existingInternalConfig)) {
            return new NetworkUpdateResult(-1);
        } else {
            boolean newNetwork = existingInternalConfig == null;
            boolean hasIpChanged = !newNetwork ? WifiConfigurationUtil.hasIpChanged(existingInternalConfig, newInternalConfig) : true;
            boolean hasProxyChanged = !newNetwork ? WifiConfigurationUtil.hasProxyChanged(existingInternalConfig, newInternalConfig) : true;
            boolean hasCredentialChanged = !newNetwork ? WifiConfigurationUtil.hasCredentialChanged(existingInternalConfig, newInternalConfig) : true;
            if (hasCredentialChanged) {
                newInternalConfig.getNetworkSelectionStatus().setHasEverConnected(false);
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
        }
    }

    public NetworkUpdateResult addOrUpdateNetwork(WifiConfiguration config, int uid) {
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
            if (result.isSuccess()) {
                int i;
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
                if (!(config.ephemeral || (config.isPasspoint() ^ 1) == 0)) {
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
            Log.e(TAG, "Failed to add/update network " + config.getPrintableSsid());
            return result;
        }
    }

    private boolean removeNetworkInternal(WifiConfiguration config) {
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
        localLog("removeNetworkInternal: removed config. netId=" + config.networkId + " configKey=" + config.configKey());
        return true;
    }

    public boolean removeNetwork(int networkId, int uid) {
        if (doesUidBelongToCurrentUser(uid)) {
            WifiConfiguration config = getInternalConfiguredNetwork(networkId);
            if (config == null) {
                return false;
            }
            if (!canModifyNetwork(config, uid, false)) {
                Log.e(TAG, "UID " + uid + " does not have permission to delete configuration " + config.configKey());
                return false;
            } else if (removeNetworkInternal(config)) {
                if (networkId == this.mLastSelectedNetworkId) {
                    clearLastSelectedNetwork();
                }
                sendConfiguredNetworkChangedBroadcast(config, 1);
                if (!(config.ephemeral || (config.isPasspoint() ^ 1) == 0)) {
                    saveToStore(true);
                    if (this.mListener != null) {
                        this.mListener.onSavedNetworkRemoved(networkId);
                    }
                }
                return true;
            } else {
                Log.e(TAG, "Failed to remove network " + config.getPrintableSsid());
                return false;
            }
        }
        Log.e(TAG, "UID " + uid + " not visible to the current user");
        return false;
    }

    public Set<Integer> removeNetworksForApp(ApplicationInfo app) {
        int i = 0;
        if (app == null || app.packageName == null) {
            return Collections.emptySet();
        }
        Log.d(TAG, "Remove all networks for app " + app);
        Set<Integer> removedNetworks = new ArraySet();
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0]);
        int length = copiedConfigs.length;
        while (i < length) {
            WifiConfiguration config = copiedConfigs[i];
            if (app.uid == config.creatorUid && (app.packageName.equals(config.creatorName) ^ 1) == 0) {
                localLog("Removing network " + config.SSID + ", application \"" + app.packageName + "\" uninstalled" + " from user " + UserHandle.getUserId(app.uid));
                if (removeNetwork(config.networkId, this.mSystemUiUid)) {
                    removedNetworks.add(Integer.valueOf(config.networkId));
                }
            }
            i++;
        }
        return removedNetworks;
    }

    Set<Integer> removeNetworksForUser(int userId) {
        int i = 0;
        Log.d(TAG, "Remove all networks for user " + userId);
        Set<Integer> removedNetworks = new ArraySet();
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0]);
        int length = copiedConfigs.length;
        while (i < length) {
            WifiConfiguration config = copiedConfigs[i];
            if (userId == UserHandle.getUserId(config.creatorUid)) {
                localLog("Removing network " + config.SSID + ", user " + userId + " removed");
                if (removeNetwork(config.networkId, this.mSystemUiUid)) {
                    removedNetworks.add(Integer.valueOf(config.networkId));
                }
            }
            i++;
        }
        return removedNetworks;
    }

    private void setNetworkSelectionEnabled(WifiConfiguration config) {
        NetworkSelectionStatus status = config.getNetworkSelectionStatus();
        status.setNetworkSelectionStatus(0);
        status.setDisableTime(-1);
        status.setNetworkSelectionDisableReason(0);
        status.clearDisableReasonCounter();
        if (this.mListener != null) {
            this.mListener.onSavedNetworkEnabled(config.networkId);
        }
    }

    private void setNetworkSelectionTemporarilyDisabled(WifiConfiguration config, int disableReason) {
        NetworkSelectionStatus status = config.getNetworkSelectionStatus();
        status.setNetworkSelectionStatus(1);
        status.setDisableTime(this.mClock.getElapsedSinceBootMillis());
        status.setNetworkSelectionDisableReason(disableReason);
        if (this.mListener != null) {
            this.mListener.onSavedNetworkTemporarilyDisabled(config.networkId);
        }
    }

    private void setNetworkSelectionPermanentlyDisabled(WifiConfiguration config, int disableReason) {
        NetworkSelectionStatus status = config.getNetworkSelectionStatus();
        status.setNetworkSelectionStatus(2);
        status.setDisableTime(-1);
        status.setNetworkSelectionDisableReason(disableReason);
        if (this.mListener != null) {
            this.mListener.onSavedNetworkPermanentlyDisabled(config.networkId);
        }
    }

    private void setNetworkStatus(WifiConfiguration config, int status) {
        config.status = status;
        sendConfiguredNetworkChangedBroadcast(config, 2);
    }

    private boolean setNetworkSelectionStatus(WifiConfiguration config, int reason) {
        NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (reason < 0 || reason >= 16) {
            Log.e(TAG, "Invalid Network disable reason " + reason);
            return false;
        }
        if (reason == 0) {
            setNetworkSelectionEnabled(config);
            setNetworkStatus(config, 2);
        } else if (reason < 7) {
            setNetworkSelectionTemporarilyDisabled(config, reason);
            HwWifiCHRStateManager mWiFiCHRManager = HwWifiServiceFactory.getHwWifiCHRStateManager();
            if (mWiFiCHRManager != null) {
                Log.d(TAG, "chr, trigger disable network , reason=" + reason);
                if (2 == reason) {
                    mWiFiCHRManager.updateWifiException(83, "");
                    mWiFiCHRManager.reportHwCHRAccessNetworkEventInfoList(7);
                }
                if (3 == reason) {
                    mWiFiCHRManager.updateWifiException(82, "");
                    mWiFiCHRManager.reportHwCHRAccessNetworkEventInfoList(7);
                }
                if (4 == reason) {
                    mWiFiCHRManager.updateWifiException(84, "");
                    mWiFiCHRManager.reportHwCHRAccessNetworkEventInfoList(7);
                }
            }
        } else if (reason == 15) {
            Log.d(TAG, "setNetworkSelectionTemporarilyDisabled since " + reason);
            setNetworkSelectionTemporarilyDisabled(config, reason);
        } else if (reason == 10) {
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
        NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (reason != 0) {
            networkStatus.incrementDisableReasonCounter(reason);
            int disableReasonCounter = networkStatus.getDisableReasonCounter(reason);
            int disableReasonThreshold = NETWORK_SELECTION_DISABLE_THRESHOLD[reason];
            if (disableReasonCounter < disableReasonThreshold) {
                if (this.mVerboseLoggingEnabled || reason < 7 || reason == 15) {
                    Log.d(TAG, "Disable counter for network " + config.getPrintableSsid() + " for reason " + NetworkSelectionStatus.getNetworkDisableReasonString(reason) + " is " + networkStatus.getDisableReasonCounter(reason) + " and threshold is " + disableReasonThreshold);
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
        NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
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
        } else if (networkStatus.isDisabledByReason(11)) {
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
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Enabling network " + networkId + " (disableOthers " + disableOthers + ")");
        }
        if (doesUidBelongToCurrentUser(uid)) {
            WifiConfiguration config = getInternalConfiguredNetwork(networkId);
            if (config == null || HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(config, true)) {
                return false;
            }
            if (!canModifyNetwork(config, uid, false)) {
                Log.e(TAG, "UID " + uid + " does not have permission to update configuration " + config.configKey());
                return false;
            } else if (!updateNetworkSelectionStatus(networkId, 0)) {
                return false;
            } else {
                if (disableOthers) {
                    setLastSelectedNetwork(networkId);
                }
                saveToStore(true);
                return true;
            }
        }
        Log.e(TAG, "UID " + uid + " not visible to the current user");
        return false;
    }

    public boolean disableNetwork(int networkId, int uid) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Disabling network " + networkId);
        }
        if (doesUidBelongToCurrentUser(uid)) {
            WifiConfiguration config = getInternalConfiguredNetwork(networkId);
            if (config == null) {
                return false;
            }
            if (canModifyNetwork(config, uid, false)) {
                int reason = 10;
                int appId = UserHandle.getAppId(uid);
                if (appId == 0 || appId == 1000) {
                    reason = 13;
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
            Log.e(TAG, "UID " + uid + " does not have permission to update configuration " + config.configKey());
            return false;
        }
        Log.e(TAG, "UID " + uid + " not visible to the current user");
        return false;
    }

    public boolean checkAndUpdateLastConnectUid(int networkId, int uid) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Update network last connect UID for " + networkId);
        }
        if (doesUidBelongToCurrentUser(uid)) {
            WifiConfiguration config = getInternalConfiguredNetwork(networkId);
            if (config == null) {
                return false;
            }
            if (canModifyNetwork(config, uid, true)) {
                config.lastConnectUid = uid;
                return true;
            }
            Log.e(TAG, "UID " + uid + " does not have permission to update configuration " + config.configKey());
            return false;
        }
        Log.e(TAG, "UID " + uid + " not visible to the current user");
        return false;
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
        return (ScanDetailCache) this.mScanDetailCaches.get(Integer.valueOf(networkId));
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
        ScanResult result = scanDetailCache.get(scanResult.BSSID);
        if (result != null) {
            scanResult.blackListTimestamp = result.blackListTimestamp;
            scanResult.numIpConfigFailures = result.numIpConfigFailures;
            scanResult.numConnection = result.numConnection;
        }
        if (config.ephemeral) {
            scanResult.untrusted = true;
        }
        scanDetailCache.put(scanDetail);
        attemptNetworkLinking(config);
    }

    private WifiConfiguration getSavedNetworkForScanDetail(ScanDetail scanDetail) {
        ScanResult scanResult = scanDetail.getScanResult();
        if (scanResult == null) {
            Log.e(TAG, "No scan result found in scan detail");
            return null;
        }
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (ScanResultUtil.doesScanResultMatchWithNetwork(scanResult, config)) {
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "getSavedNetworkFromScanDetail Found " + config.configKey() + " for " + scanResult.SSID + "[" + scanResult.capabilities + "]");
                }
                return config;
            }
        }
        return null;
    }

    public WifiConfiguration getSavedNetworkForScanDetailAndCache(ScanDetail scanDetail) {
        WifiConfiguration network = getSavedNetworkForScanDetail(scanDetail);
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
        if (config != null && scanDetailCache != null) {
            ScanDetail scanDetail = scanDetailCache.getScanDetail(info.getBSSID());
            if (scanDetail != null) {
                ScanResult result = scanDetail.getScanResult();
                long previousSeen = result.seen;
                int previousRssi = result.level;
                scanDetail.setSeen();
                result.level = info.getRssi();
                result.averageRssi(previousRssi, previousSeen, SCAN_RESULT_MAXIMUM_AGE_MS);
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "Updating scan detail cache freq=" + result.frequency + " BSSID=" + result.BSSID + " RSSI=" + result.level + " for " + config.configKey());
                }
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
        if (!this.mOnlyLinkSameCredentialConfigurations || TextUtils.equals(network1.preSharedKey, network2.preSharedKey)) {
            if (network1.defaultGwMacAddress == null || network2.defaultGwMacAddress == null) {
                if (!(scanDetailCache1 == null || scanDetailCache2 == null)) {
                    for (String abssid : scanDetailCache1.keySet()) {
                        for (String bbssid : scanDetailCache2.keySet()) {
                            if (abssid.regionMatches(true, 0, bbssid, 0, 16)) {
                                if (this.mVerboseLoggingEnabled) {
                                    Log.v(TAG, "shouldNetworksBeLinked link due to DBDC BSSID match " + network2.SSID + " and " + network1.SSID + " bssida " + abssid + " bssidb " + bbssid);
                                }
                                return true;
                            }
                        }
                    }
                }
            } else if (network1.defaultGwMacAddress.equals(network2.defaultGwMacAddress)) {
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "shouldNetworksBeLinked link due to same gw " + network2.SSID + " and " + network1.SSID + " GW " + network1.defaultGwMacAddress);
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
        network2.linkedConfigurations.put(network1.configKey(), Integer.valueOf(1));
        network1.linkedConfigurations.put(network2.configKey(), Integer.valueOf(1));
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
                    if (!(linkConfig.configKey().equals(config.configKey()) || linkConfig.ephemeral || !linkConfig.allowedKeyManagement.get(1))) {
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
        if (scanDetailCache != null && scanDetailCache.size() > 0) {
            for (ScanDetail scanDetail : scanDetailCache.values()) {
                ScanResult result = scanDetail.getScanResult();
                boolean valid = nowInMillis - result.seen < ageInMillis;
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "fetchChannelSetForNetwork has " + result.BSSID + " freq " + result.frequency + " age " + (nowInMillis - result.seen) + " ?=" + valid);
                }
                if (valid) {
                    channelSet.add(Integer.valueOf(result.frequency));
                }
                if (channelSet.size() >= maxChannelSetSize) {
                    return false;
                }
            }
        }
        return true;
    }

    public Set<Integer> fetchChannelSetForNetworkForPartialScan(int networkId, long ageInMillis, int homeChannelFreq) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return null;
        }
        ScanDetailCache scanDetailCache = getScanDetailCacheForNetwork(networkId);
        if (scanDetailCache == null && config.linkedConfigurations == null) {
            Log.i(TAG, "No scan detail and linked configs associated with networkId " + networkId);
            return null;
        }
        if (this.mVerboseLoggingEnabled) {
            StringBuilder dbg = new StringBuilder();
            dbg.append("fetchChannelSetForNetworkForPartialScan ageInMillis ").append(ageInMillis).append(" for ").append(config.configKey()).append(" max ").append(this.mMaxNumActiveChannelsForPartialScans);
            if (scanDetailCache != null) {
                dbg.append(" bssids ").append(scanDetailCache.size());
            }
            if (config.linkedConfigurations != null) {
                dbg.append(" linked ").append(config.linkedConfigurations.size());
            }
            Log.v(TAG, dbg.toString());
        }
        Set<Integer> channelSet = new HashSet();
        if (homeChannelFreq > 0) {
            channelSet.add(Integer.valueOf(homeChannelFreq));
            if (channelSet.size() >= this.mMaxNumActiveChannelsForPartialScans) {
                return channelSet;
            }
        }
        long nowInMillis = this.mClock.getWallClockMillis();
        if (addToChannelSetForNetworkFromScanDetailCache(channelSet, scanDetailCache, nowInMillis, ageInMillis, this.mMaxNumActiveChannelsForPartialScans) && config.linkedConfigurations != null) {
            for (String configKey : config.linkedConfigurations.keySet()) {
                WifiConfiguration linkedConfig = getInternalConfiguredNetwork(configKey);
                if (linkedConfig != null) {
                    if (!addToChannelSetForNetworkFromScanDetailCache(channelSet, getScanDetailCacheForNetwork(linkedConfig.networkId), nowInMillis, ageInMillis, this.mMaxNumActiveChannelsForPartialScans)) {
                        break;
                    }
                }
            }
        }
        return channelSet;
    }

    public List<PnoNetwork> retrievePnoNetworkList() {
        List<PnoNetwork> pnoList = new ArrayList();
        List<WifiConfiguration> networks = new ArrayList(getInternalConfiguredNetworks());
        Iterator<WifiConfiguration> iter = networks.iterator();
        while (iter.hasNext()) {
            if (((WifiConfiguration) iter.next()).getNetworkSelectionStatus().isNetworkPermanentlyDisabled()) {
                iter.remove();
            }
        }
        Collections.sort(networks, sScanListComparator);
        int priority = networks.size() - 1;
        for (WifiConfiguration config : networks) {
            pnoList.add(WifiConfigurationUtil.createPnoNetwork(config, priority));
            priority--;
        }
        return pnoList;
    }

    public List<HiddenNetwork> retrieveHiddenNetworkList() {
        WifiConfiguration config;
        List<HiddenNetwork> hiddenList = new ArrayList();
        List<WifiConfiguration> networks = new ArrayList(getInternalConfiguredNetworks());
        List<WifiConfiguration> restoreNetworks = new ArrayList();
        List<WifiConfiguration> currentHiddenConfig = new ArrayList();
        Iterator<WifiConfiguration> iter = networks.iterator();
        while (iter.hasNext()) {
            config = (WifiConfiguration) iter.next();
            if (!config.hiddenSSID || config.getNetworkSelectionStatus().isNetworkPermanentlyDisabled()) {
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
                restoreNetworks.add((WifiConfiguration) networks.remove(i));
            }
            Collections.sort(restoreNetworks, sScanListComparator);
            while (hideNetCount > 14) {
                WifiConfiguration updateConfig = (WifiConfiguration) restoreNetworks.remove(restoreNetworks.size() - 1);
                updateConfig.hiddenSSID = false;
                this.mConfiguredNetworks.put(updateConfig);
                Log.d(TAG, "retrieveHiddenNetworkList: update config:" + updateConfig.SSID + " to hiddenSSID:false");
                hideNetCount--;
            }
            for (WifiConfiguration config2 : restoreNetworks) {
                networks.add(config2);
            }
        }
        StringBuffer debugHiddenList = new StringBuffer("");
        int priority = networks.size() - 1;
        for (WifiConfiguration config22 : networks) {
            hiddenList.add(new HiddenNetwork(TextUtils.isEmpty(config22.oriSsid) ? config22.SSID : config22.oriSsid));
            debugHiddenList.append(" " + config22.SSID);
            priority--;
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
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
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

    public void resetSimNetworks() {
        if (this.mVerboseLoggingEnabled) {
            localLog("resetSimNetworks");
        }
        if ((getInternalConfiguredNetworks() == null || this.WIFI_CONFIGURE_EMPTY_FLAG == getInternalConfiguredNetworks().size()) && !loadFromStore()) {
            Log.e(TAG, "Failed to load from config store during resetSimNetworks");
        }
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (TelephonyUtil.isSimConfig(config)) {
                config.enterpriseConfig.setIdentity(TelephonyUtil.getSimIdentity(this.mTelephonyManager, config));
                if (config.enterpriseConfig.getEapMethod() != 0) {
                    config.enterpriseConfig.setAnonymousIdentity("");
                }
            }
        }
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
        if (userId == this.mCurrentUserId && this.mUserManager.isUserUnlockingOrUnlocked(this.mCurrentUserId)) {
            saveToStore(true);
            clearInternalData();
            this.mCurrentUserId = 0;
        }
    }

    private void clearInternalData() {
        this.mConfiguredNetworks.clear();
        this.mDeletedEphemeralSSIDs.clear();
        this.mScanDetailCaches.clear();
        clearLastSelectedNetwork();
    }

    private Set<Integer> clearInternalUserData(int userId) {
        Set<Integer> removedNetworkIds = new HashSet();
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (!config.shared && WifiConfigurationUtil.doesUidBelongToAnyProfile(config.creatorUid, this.mUserManager.getProfiles(userId))) {
                removedNetworkIds.add(Integer.valueOf(config.networkId));
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
            this.mConfiguredNetworks.put(configuration);
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
            this.mConfiguredNetworks.put(configuration);
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
            WifiConfigStoreDataLegacy storeData = this.mWifiConfigStoreLegacy.read();
            Log.d(TAG, "Reading from legacy store completed");
            List<WifiConfiguration> storeConfigs = storeData.getConfigurations();
            int storeConfigsSize = storeConfigs.size();
            for (int i = 0; i < storeConfigsSize; i++) {
                WifiConfiguration config = (WifiConfiguration) storeConfigs.get(i);
                if (config != null) {
                    int appId = UserHandle.getAppId(config.creatorUid);
                    if (config.BSSID != null && (appId == -1 || appId == 0 || appId == 1000 || appId == 1010)) {
                        Log.w(TAG, "migrateFromLegacyStore creater: " + config.creatorUid + ", ssid: " + config.SSID + ", Bssid:" + ScanResultUtil.getConfusedBssid(config.BSSID));
                        config.BSSID = null;
                    }
                }
            }
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
        if (this.mWifiConfigStore.areStoresPresent()) {
            if (this.mDeferredUserUnlockRead) {
                Log.i(TAG, "Handling user unlock before loading from store.");
                this.mWifiConfigStore.setUserStore(WifiConfigStore.createUserFile(this.mCurrentUserId));
                this.mDeferredUserUnlockRead = false;
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
        Log.d(TAG, "New store files not found. No saved networks loaded!");
        if (!this.mWifiConfigStoreLegacy.areStoresPresent()) {
            this.mPendingStoreRead = false;
        }
        return true;
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
        if (WifiConfigurationUtil.isVisibleToAnyProfile(config, this.mUserManager.getProfiles(this.mCurrentUserId))) {
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
        Log.d(TAG, "updatePriority " + Integer.toString(config.networkId) + ": Network config is not " + "visible to current user.");
        return false;
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
        ArrayList<WifiConfiguration> sharedConfigurations = new ArrayList();
        ArrayList<WifiConfiguration> userConfigurations = new ArrayList();
        List<Integer> legacyPasspointNetId = new ArrayList();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForAllUsers()) {
            if (!config.ephemeral && (!config.isPasspoint() || (config.isLegacyPasspointConfig ^ 1) == 0)) {
                if (config.isLegacyPasspointConfig && WifiConfigurationUtil.doesUidBelongToAnyProfile(config.creatorUid, this.mUserManager.getProfiles(this.mCurrentUserId))) {
                    legacyPasspointNetId.add(Integer.valueOf(config.networkId));
                    if (!PasspointManager.addLegacyPasspointConfig(config)) {
                        Log.e(TAG, "Failed to migrate legacy Passpoint config: " + config.FQDN);
                    }
                } else if (config.shared || (WifiConfigurationUtil.doesUidBelongToAnyProfile(config.creatorUid, this.mUserManager.getProfiles(this.mCurrentUserId)) ^ 1) != 0) {
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
        boolean isUidProfileOwner = dpmi != null ? dpmi.isActiveAdminWithPolicy(uid, -1) : false;
        boolean isUidDeviceOwner = dpmi != null ? dpmi.isActiveAdminWithPolicy(uid, -2) : false;
        boolean hasConfigOverridePermission = this.mWifiPermissionsUtil.checkConfigOverridePermission(uid);
        if (isUidDeviceOwner || isUidProfileOwner || hasConfigOverridePermission) {
            return true;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "UID: " + uid + " cannot modify WifiConfiguration proxy settings." + " ConfigOverride=" + hasConfigOverridePermission + " DeviceOwner=" + isUidDeviceOwner + " ProfileOwner=" + isUidProfileOwner);
        }
        return false;
    }

    public void setOnSavedNetworkUpdateListener(OnSavedNetworkUpdateListener listener) {
        this.mListener = listener;
    }
}
