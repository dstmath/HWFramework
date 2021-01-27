package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.IpConfiguration;
import android.net.MacAddress;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.android.server.wifi.ScoringParams;
import com.android.server.wifi.WifiConfigStore;
import com.android.server.wifi.WifiConfigurationUtil;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParserException;

public class WifiConfigManager extends AbsWifiConfigManager implements IHwWifiConfigManagerInner {
    private static final MacAddress DEFAULT_MAC_ADDRESS = MacAddress.fromString("02:00:00:00:00:00");
    @VisibleForTesting
    public static final long DELETED_EPHEMERAL_SSID_EXPIRY_MS = 86400000;
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static boolean HWLOGW_E = true;
    private static final String KEY_MATCH_BSSID = "isMatchBSSID";
    private static final String KEY_RADOMMAC_SSID = "random_mac_ssid";
    @VisibleForTesting
    public static final int LINK_CONFIGURATION_BSSID_MATCH_LENGTH = 16;
    @VisibleForTesting
    public static final int LINK_CONFIGURATION_MAX_SCAN_CACHE_ENTRIES = 6;
    @VisibleForTesting
    public static final long MAX_PNO_SCAN_FREQUENCY_AGE_MS = 2592000000L;
    public static final int MAX_RX_PACKET_FOR_FULL_SCANS = 16;
    public static final int MAX_TX_PACKET_FOR_FULL_SCANS = 8;
    @VisibleForTesting
    public static final int[] NETWORK_SELECTION_DISABLE_THRESHOLD = {-1, 1, 3, 5, 5, 5, 1, 1, 6, 1, 1, 1, 1, 1, 1, 1, 1, 5};
    @VisibleForTesting
    public static final int[] NETWORK_SELECTION_DISABLE_TIMEOUT_MS = {ScoringParams.Values.MAX_EXPID, 900000, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, 600000, 0, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS};
    @VisibleForTesting
    public static final String PASSWORD_MASK = "*";
    private static final int PNO_SCAN_CONFIGKEY_BLACKLIST_INITIAL_SIZE = 5;
    @VisibleForTesting
    public static final int SCAN_CACHE_ENTRIES_MAX_SIZE = 192;
    @VisibleForTesting
    public static final int SCAN_CACHE_ENTRIES_TRIM_SIZE = 128;
    private static final int SCAN_RESULT_MAXIMUM_AGE_MS = 40000;
    @VisibleForTesting
    public static final String SYSUI_PACKAGE_NAME = "com.android.systemui";
    private static final String TAG = "WifiConfigManager";
    private static final int WIFI_PNO_FREQUENCY_CULLING_ENABLED_DEFAULT = 1;
    private static final int WIFI_PNO_RECENCY_SORTING_ENABLED_DEFAULT = 1;
    private static int mCurrentHiddenNetId = -1;
    private static final WifiConfigurationUtil.WifiConfigurationComparator sScanListComparator = new WifiConfigurationUtil.WifiConfigurationComparator() {
        /* class com.android.server.wifi.WifiConfigManager.AnonymousClass1 */

        @Override // com.android.server.wifi.WifiConfigurationUtil.WifiConfigurationComparator
        public int compareNetworksWithSameStatus(WifiConfiguration a, WifiConfiguration b) {
            if (a.numAssociation != b.numAssociation) {
                return Long.compare((long) b.numAssociation, (long) a.numAssociation);
            }
            return Boolean.compare(b.getNetworkSelectionStatus().getSeenInLastQualifiedNetworkSelection(), a.getNetworkSelectionStatus().getSeenInLastQualifiedNetworkSelection());
        }
    };
    private static boolean sVDBG;
    private static boolean sVVDBG;
    private final BackupManagerProxy mBackupManagerProxy;
    private final Clock mClock;
    private final ConfigurationMap mConfiguredNetworks;
    private final Context mContext;
    private int mCurrentUserId;
    HwCustWifiAutoJoinController mCust = ((HwCustWifiAutoJoinController) HwCustUtils.createObj(HwCustWifiAutoJoinController.class, new Object[0]));
    private boolean mDeferredUserUnlockRead;
    private final DeletedEphemeralSsidsStoreData mDeletedEphemeralSsidsStoreData;
    private final Map<String, Long> mDeletedEphemeralSsidsToTimeMap;
    private final FrameworkFacade mFrameworkFacade;
    private HwWifiCHRService mHwWifiCHRService;
    public final IHwWifiConfigManagerEx mHwWifiConfigManagerEx;
    private int mLastSelectedNetworkId;
    private long mLastSelectedTimeStamp;
    private OnSavedNetworkUpdateListener mListener;
    private final LocalLog mLocalLog;
    private final int mMaxNumActiveChannelsForPartialScans;
    private final NetworkListSharedStoreData mNetworkListSharedStoreData;
    private final NetworkListUserStoreData mNetworkListUserStoreData;
    private int mNextNetworkId;
    private final boolean mOnlyLinkSameCredentialConfigurations;
    private boolean mPendingStoreRead;
    private boolean mPendingUnlockStoreRead;
    private final List<String> mPnoConfigKeyBlacklist;
    private boolean mPnoFrequencyCullingEnabled;
    private boolean mPnoRecencySortingEnabled;
    private final Map<String, String> mRandomizedMacAddressMapping;
    protected final Map<String, String> mRandomizedMacAddressMappingPlus;
    protected final Map<String, Integer> mRandomizedMacAddressMatchInfoMap;
    private final RandomizedMacStoreData mRandomizedMacStoreData;
    private final Map<Integer, ScanDetailCache> mScanDetailCaches;
    private int mSystemUiUid;
    private final TelephonyManager mTelephonyManager;
    private final UserManager mUserManager;
    private boolean mVerboseLoggingEnabled;
    private final WifiConfigStore mWifiConfigStore;
    private final WifiInjector mWifiInjector;
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

    static {
        boolean z = HWFLOW;
        sVDBG = z;
        sVVDBG = z;
    }

    WifiConfigManager(Context context, Clock clock, UserManager userManager, TelephonyManager telephonyManager, WifiKeyStore wifiKeyStore, WifiConfigStore wifiConfigStore, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper wifiPermissionsWrapper, WifiInjector wifiInjector, NetworkListSharedStoreData networkListSharedStoreData, NetworkListUserStoreData networkListUserStoreData, DeletedEphemeralSsidsStoreData deletedEphemeralSsidsStoreData, RandomizedMacStoreData randomizedMacStoreData, FrameworkFacade frameworkFacade, Looper looper) {
        this.mLocalLog = new LocalLog(ActivityManager.isLowRamDeviceStatic() ? 128 : 256);
        this.mVerboseLoggingEnabled = false;
        this.mCurrentUserId = 0;
        this.mPendingUnlockStoreRead = true;
        this.mPendingStoreRead = true;
        this.mDeferredUserUnlockRead = false;
        this.mNextNetworkId = 0;
        this.mSystemUiUid = -1;
        this.mLastSelectedNetworkId = -1;
        this.mLastSelectedTimeStamp = -1;
        this.mListener = null;
        this.mPnoFrequencyCullingEnabled = false;
        this.mPnoRecencySortingEnabled = false;
        this.mHwWifiCHRService = null;
        this.mPnoConfigKeyBlacklist = new ArrayList(5);
        this.mContext = context;
        this.mClock = clock;
        this.mUserManager = userManager;
        this.mBackupManagerProxy = new BackupManagerProxy();
        this.mTelephonyManager = telephonyManager;
        this.mWifiKeyStore = wifiKeyStore;
        this.mWifiConfigStore = wifiConfigStore;
        this.mWifiPermissionsUtil = wifiPermissionsUtil;
        this.mWifiPermissionsWrapper = wifiPermissionsWrapper;
        this.mWifiInjector = wifiInjector;
        this.mHwWifiConfigManagerEx = HwWifiServiceFactory.getHwWifiConfigManagerEx(this, this.mContext);
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        this.mConfiguredNetworks = new ConfigurationMap(userManager);
        this.mScanDetailCaches = new HashMap(16, 0.75f);
        this.mDeletedEphemeralSsidsToTimeMap = new HashMap();
        this.mRandomizedMacAddressMapping = new HashMap();
        this.mRandomizedMacAddressMappingPlus = new HashMap();
        this.mRandomizedMacAddressMatchInfoMap = new HashMap();
        this.mNetworkListSharedStoreData = networkListSharedStoreData;
        this.mNetworkListUserStoreData = networkListUserStoreData;
        this.mDeletedEphemeralSsidsStoreData = deletedEphemeralSsidsStoreData;
        this.mRandomizedMacStoreData = randomizedMacStoreData;
        this.mWifiConfigStore.registerStoreData(this.mNetworkListSharedStoreData);
        this.mWifiConfigStore.registerStoreData(this.mNetworkListUserStoreData);
        this.mWifiConfigStore.registerStoreData(this.mDeletedEphemeralSsidsStoreData);
        this.mWifiConfigStore.registerStoreData(this.mRandomizedMacStoreData);
        this.mOnlyLinkSameCredentialConfigurations = this.mContext.getResources().getBoolean(17891593);
        this.mMaxNumActiveChannelsForPartialScans = this.mContext.getResources().getInteger(17694927);
        this.mFrameworkFacade = frameworkFacade;
        this.mFrameworkFacade.registerContentObserver(this.mContext, Settings.Global.getUriFor("wifi_pno_frequency_culling_enabled"), false, new ContentObserver(new Handler(looper)) {
            /* class com.android.server.wifi.WifiConfigManager.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                WifiConfigManager.this.updatePnoFrequencyCullingSetting();
            }
        });
        updatePnoFrequencyCullingSetting();
        this.mFrameworkFacade.registerContentObserver(this.mContext, Settings.Global.getUriFor("wifi_pno_recency_sorting_enabled"), false, new ContentObserver(new Handler(looper)) {
            /* class com.android.server.wifi.WifiConfigManager.AnonymousClass3 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                WifiConfigManager.this.updatePnoRecencySortingSetting();
            }
        });
        updatePnoRecencySortingSetting();
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
        sb.append(String.format("%tm-%td %tH:%tM:%tS.%tL", c, c, c, c, c, c));
        return sb.toString();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int getRandomizedMacAddressMappingSize() {
        return this.mRandomizedMacAddressMapping.size();
    }

    private void uploadRandomMacMatchDft(WifiConfiguration config, boolean isMatchedBssid) {
        if (this.mHwWifiCHRService != null && config != null) {
            Bundle data = new Bundle();
            data.putString(KEY_RADOMMAC_SSID, config.SSID);
            data.putBoolean(KEY_MATCH_BSSID, isMatchedBssid);
            this.mHwWifiCHRService.uploadDFTEvent(19, data);
        }
    }

    /* access modifiers changed from: protected */
    public MacAddress getPersistentMacAddress(WifiConfiguration config) {
        String persistentMacString = this.mRandomizedMacAddressMapping.get(config.getSsidAndSecurityTypeString());
        if (persistentMacString != null) {
            try {
                Log.i(TAG, "Get randomized MAC address from stored value.");
                MacAddress mac = MacAddress.fromString(persistentMacString);
                uploadRandomMacMatchDft(config, false);
                return mac;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error creating randomized MAC address from stored value.");
                this.mRandomizedMacAddressMapping.remove(config.getSsidAndSecurityTypeString());
            }
        } else {
            String macAddr = searchRandomizedMacAddressPlusEntry(config);
            if (macAddr != null) {
                try {
                    Log.i(TAG, "randommac, macAddressPlus contains key, mac: " + StringUtilEx.safeDisplayBssid(macAddr));
                    MacAddress mac2 = MacAddress.fromString(macAddr);
                    uploadRandomMacMatchDft(config, true);
                    return mac2;
                } catch (IllegalArgumentException e2) {
                    Log.e(TAG, "Error creating randomized MAC address from bssid table.");
                }
            }
            uploadRandomMacMatchDft(config, false);
            MacAddress result = WifiConfigurationUtil.calculatePersistentMacForConfiguration(config, WifiConfigurationUtil.obtainMacRandHashFunction(1010));
            if (result == null) {
                result = WifiConfigurationUtil.calculatePersistentMacForConfiguration(config, WifiConfigurationUtil.obtainMacRandHashFunction(1010));
            }
            if (result != null) {
                return result;
            }
            Log.wtf(TAG, "Failed to generate MAC address from KeyStore even after retrying. Using locally generated MAC address instead.");
            return MacAddress.createRandomUnicastAddress();
        }
    }

    /* access modifiers changed from: protected */
    public MacAddress setRandomizedMacToPersistentMac(WifiConfiguration config) {
        MacAddress persistentMac = getPersistentMacAddress(config);
        if (persistentMac == null || persistentMac.equals(config.getRandomizedMacAddress())) {
            return persistentMac;
        }
        WifiConfiguration internalConfig = getInternalConfiguredNetwork(config.networkId);
        if (internalConfig == null) {
            Log.e(TAG, "internalConfig is null.");
            return persistentMac;
        }
        internalConfig.setRandomizedMacAddress(persistentMac);
        return persistentMac;
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePnoFrequencyCullingSetting() {
        boolean z = true;
        if (this.mFrameworkFacade.getIntegerSetting(this.mContext, "wifi_pno_frequency_culling_enabled", 1) != 1) {
            z = false;
        }
        this.mPnoFrequencyCullingEnabled = z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePnoRecencySortingSetting() {
        boolean z = true;
        if (this.mFrameworkFacade.getIntegerSetting(this.mContext, "wifi_pno_recency_sorting_enabled", 1) != 1) {
            z = false;
        }
        this.mPnoRecencySortingEnabled = z;
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
        configuration.setRandomizedMacAddress(DEFAULT_MAC_ADDRESS);
    }

    private WifiConfiguration createExternalWifiConfiguration(WifiConfiguration configuration, boolean maskPasswords, int targetUid) {
        WifiConfiguration network = new WifiConfiguration(configuration);
        if (maskPasswords) {
            maskPasswordsInWifiConfiguration(network);
        }
        if (!(targetUid == 1010 || targetUid == 1000 || targetUid == configuration.creatorUid)) {
            maskRandomizedMacAddressInWifiConfiguration(network);
        }
        return network;
    }

    private List<WifiConfiguration> getConfiguredNetworks(boolean savedOnly, boolean maskPasswords, int targetUid) {
        List<WifiConfiguration> networks = new ArrayList<>();
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (!savedOnly || (!config.ephemeral && !config.isPasspoint())) {
                networks.add(createExternalWifiConfiguration(config, maskPasswords, targetUid));
            }
        }
        return networks;
    }

    public List<WifiConfiguration> getConfiguredNetworks() {
        return getConfiguredNetworks(false, true, 1010);
    }

    public List<WifiConfiguration> getConfiguredNetworksWithPasswords() {
        return getConfiguredNetworks(false, false, 1010);
    }

    @Override // com.android.server.wifi.IHwWifiConfigManagerInner
    public List<WifiConfiguration> getSavedNetworks(int targetUid) {
        return getConfiguredNetworks(true, true, targetUid);
    }

    public WifiConfiguration getConfiguredNetwork(int networkId) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return null;
        }
        return createExternalWifiConfiguration(config, true, 1010);
    }

    public WifiConfiguration getConfiguredNetwork(String configKey) {
        WifiConfiguration config = getInternalConfiguredNetwork(configKey);
        if (config == null) {
            return null;
        }
        return createExternalWifiConfiguration(config, true, 1010);
    }

    public WifiConfiguration getConfiguredNetworkWithPassword(int networkId) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return null;
        }
        return createExternalWifiConfiguration(config, false, 1010);
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
            Log.e(TAG, "Cannot find network with networkId " + config.networkId + " or ssid " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
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
            Log.e(TAG, "Cannot find network with configKey " + StringUtilEx.safeDisplaySsid(configKey));
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
        if (uid == 1000) {
            return true;
        }
        if (config.isPasspoint() && uid == 1010) {
            return true;
        }
        if (config.enterpriseConfig != null && uid == 1010 && TelephonyUtil.isSimEapMethod(config.enterpriseConfig.getEapMethod())) {
            return true;
        }
        DevicePolicyManagerInternal dpmi = this.mWifiPermissionsWrapper.getDevicePolicyManagerInternal();
        if (dpmi != null && dpmi.isActiveAdminWithPolicy(uid, -2)) {
            return true;
        }
        boolean isCreator = config.creatorUid == uid;
        if (!this.mContext.getPackageManager().hasSystemFeature("android.software.device_admin") || dpmi != null) {
            if (dpmi != null && dpmi.isActiveAdminWithPolicy(config.creatorUid, -2)) {
                if ((Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_device_owner_configs_lockdown", 0) != 0) || !this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid)) {
                    return false;
                }
                return true;
            } else if (isCreator || this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid)) {
                return true;
            } else {
                return false;
            }
        } else {
            Log.w(TAG, "Error retrieving DPMI service.");
            return false;
        }
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
        if (externalConfig.BSSID != null) {
            internalConfig.BSSID = externalConfig.BSSID.toLowerCase(Locale.ENGLISH);
        }
        internalConfig.hiddenSSID = externalConfig.hiddenSSID;
        internalConfig.requirePMF = externalConfig.requirePMF;
        if (externalConfig.preSharedKey != null && !externalConfig.preSharedKey.equals("*")) {
            internalConfig.preSharedKey = externalConfig.preSharedKey;
        }
        mergeHwParamsWithInternalWifiConfiguration(internalConfig, externalConfig);
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
        if (externalConfig.allowedGroupManagementCiphers != null && !externalConfig.allowedGroupManagementCiphers.isEmpty()) {
            internalConfig.allowedGroupManagementCiphers = (BitSet) externalConfig.allowedGroupManagementCiphers.clone();
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
        internalConfig.macRandomizationSetting = externalConfig.macRandomizationSetting;
        internalConfig.setCombinationType(externalConfig.isCombinationType());
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

    private WifiConfiguration createNewInternalWifiConfigurationFromExternal(WifiConfiguration externalConfig, int uid, String packageName) {
        MacAddress randomizedMac;
        WifiConfiguration newInternalConfig = new WifiConfiguration();
        int i = this.mNextNetworkId;
        this.mNextNetworkId = i + 1;
        newInternalConfig.networkId = i;
        setDefaultsInWifiConfiguration(newInternalConfig);
        mergeWithInternalWifiConfiguration(newInternalConfig, externalConfig);
        newInternalConfig.requirePMF = externalConfig.requirePMF;
        newInternalConfig.noInternetAccessExpected = externalConfig.noInternetAccessExpected;
        newInternalConfig.ephemeral = externalConfig.ephemeral;
        newInternalConfig.osu = externalConfig.osu;
        newInternalConfig.trusted = externalConfig.trusted;
        newInternalConfig.fromWifiNetworkSuggestion = externalConfig.fromWifiNetworkSuggestion;
        newInternalConfig.fromWifiNetworkSpecifier = externalConfig.fromWifiNetworkSpecifier;
        newInternalConfig.useExternalScores = externalConfig.useExternalScores;
        newInternalConfig.shared = externalConfig.shared;
        newInternalConfig.updateIdentifier = externalConfig.updateIdentifier;
        newInternalConfig.lastUpdateUid = uid;
        newInternalConfig.creatorUid = uid;
        String nameForUid = packageName != null ? packageName : this.mContext.getPackageManager().getNameForUid(uid);
        newInternalConfig.lastUpdateName = nameForUid;
        newInternalConfig.creatorName = nameForUid;
        String createDebugTimeStampString = createDebugTimeStampString(this.mClock.getWallClockMillis());
        newInternalConfig.updateTime = createDebugTimeStampString;
        newInternalConfig.creationTime = createDebugTimeStampString;
        ClientModeImpl clientModeImpl = this.mWifiInjector.getClientModeImpl();
        if (!(clientModeImpl == null || !clientModeImpl.isConnectedMacRandomizationEnabled() || (randomizedMac = getPersistentMacAddress(newInternalConfig)) == null)) {
            newInternalConfig.setRandomizedMacAddress(randomizedMac);
        }
        return newInternalConfig;
    }

    private WifiConfiguration updateExistingInternalWifiConfigurationFromExternal(WifiConfiguration internalConfig, WifiConfiguration externalConfig, int uid, String packageName) {
        WifiConfiguration newInternalConfig = new WifiConfiguration(internalConfig);
        mergeWithInternalWifiConfiguration(newInternalConfig, externalConfig);
        newInternalConfig.lastUpdateUid = uid;
        newInternalConfig.lastUpdateName = packageName != null ? packageName : this.mContext.getPackageManager().getNameForUid(uid);
        newInternalConfig.updateTime = createDebugTimeStampString(this.mClock.getWallClockMillis());
        return newInternalConfig;
    }

    private boolean isAllowWifiConnect(int uid) {
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "isInWhiteAppList: mContext is null");
            return false;
        }
        String packageName = context.getPackageManager().getNameForUid(uid);
        if (packageName != null) {
            return this.mWifiPermissionsUtil.isAllowWifiConnect(uid, packageName);
        }
        Log.e(TAG, "isInWhiteAppList: appName is null");
        return false;
    }

    private NetworkUpdateResult addOrUpdateNetworkInternal(WifiConfiguration config, int uid, String packageName) {
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Adding/Updating network " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
        }
        WifiConfiguration newInternalConfig = null;
        WifiConfiguration existingInternalConfig = getInternalConfiguredNetwork(config);
        boolean hasCredentialChanged = true;
        if (existingInternalConfig == null) {
            if (config.allowedKeyManagement.get(9) || config.allowedKeyManagement.get(8)) {
                config.requirePMF = true;
            }
            if (!WifiConfigurationUtil.validate(config, true)) {
                Log.e(TAG, "Cannot add network with invalid config");
                return new NetworkUpdateResult(-1);
            }
            newInternalConfig = createNewInternalWifiConfigurationFromExternal(config, uid, packageName);
            existingInternalConfig = getInternalConfiguredNetwork(newInternalConfig.configKey());
        }
        if (existingInternalConfig != null) {
            if (!WifiConfigurationUtil.validate(config, false)) {
                Log.e(TAG, "Cannot update network with invalid config");
                return new NetworkUpdateResult(-1);
            } else if (canModifyNetwork(existingInternalConfig, uid) || isAllowWifiConnect(uid)) {
                newInternalConfig = updateExistingInternalWifiConfigurationFromExternal(existingInternalConfig, config, uid, packageName);
            } else {
                Log.e(TAG, "UID " + uid + " does not have permission to update configuration " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
                return new NetworkUpdateResult(-1);
            }
        }
        if (WifiConfigurationUtil.hasProxyChanged(existingInternalConfig, newInternalConfig) && !canModifyProxySettings(uid)) {
            Log.e(TAG, "UID " + uid + " does not have permission to modify proxy Settings " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()) + ". Must have NETWORK_SETTINGS, or be device or profile owner.");
            return new NetworkUpdateResult(-1);
        } else if (WifiConfigurationUtil.hasMacRandomizationSettingsChanged(existingInternalConfig, newInternalConfig) && !this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid) && !this.mWifiPermissionsUtil.checkNetworkSetupWizardPermission(uid)) {
            Log.e(TAG, "UID " + uid + " does not have permission to modify MAC randomization Settings " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()) + ". Must have NETWORK_SETTINGS or NETWORK_SETUP_WIZARD.");
            return new NetworkUpdateResult(-1);
        } else if (!config.isPasspoint() && !config.fromWifiNetworkSuggestion && config.isEnterprise() && !this.mWifiKeyStore.updateNetworkKeys(newInternalConfig, existingInternalConfig)) {
            return new NetworkUpdateResult(-1);
        } else {
            this.mHwWifiConfigManagerEx.partOfAddOrUpdateNetworkInternal(config, newInternalConfig);
            boolean newNetwork = existingInternalConfig == null;
            boolean hasIpChanged = newNetwork || WifiConfigurationUtil.hasIpChanged(existingInternalConfig, newInternalConfig);
            boolean hasProxyChanged = newNetwork || WifiConfigurationUtil.hasProxyChanged(existingInternalConfig, newInternalConfig);
            if (!newNetwork && !WifiConfigurationUtil.hasCredentialChanged(existingInternalConfig, newInternalConfig)) {
                hasCredentialChanged = false;
            }
            if (hasCredentialChanged) {
                newInternalConfig.getNetworkSelectionStatus().setHasEverConnected(false);
            }
            try {
                this.mConfiguredNetworks.put(newInternalConfig);
                if (this.mDeletedEphemeralSsidsToTimeMap.remove(config.SSID) != null && this.mVerboseLoggingEnabled) {
                    Log.i(TAG, "Removed from ephemeral blacklist: " + StringUtilEx.safeDisplaySsid(config.SSID));
                }
                this.mBackupManagerProxy.notifyDataChanged();
                NetworkUpdateResult result = new NetworkUpdateResult(hasIpChanged, hasProxyChanged, hasCredentialChanged);
                result.setIsNewNetwork(newNetwork);
                result.setNetworkId(newInternalConfig.networkId);
                localLog("addOrUpdateNetworkInternal: added/updated config. netId=" + newInternalConfig.networkId + " configKey=" + StringUtilEx.safeDisplaySsid(newInternalConfig.getPrintableSsid()) + " uid=" + Integer.toString(newInternalConfig.creatorUid) + " name=" + newInternalConfig.creatorName);
                return result;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Failed to add network to config map", e);
                return new NetworkUpdateResult(-1);
            }
        }
    }

    private void changeEmpty2Null(String[] wepKeys) {
        if (wepKeys != null) {
            for (int i = 0; i < wepKeys.length; i++) {
                if (wepKeys[i] != null && wepKeys[i].isEmpty()) {
                    wepKeys[i] = null;
                    Log.i(TAG, "the element of wepkey is empty,change it to null,index:" + i);
                }
            }
        }
    }

    public NetworkUpdateResult addOrUpdateNetwork(WifiConfiguration config, int uid, String packageName) {
        int i;
        WifiConfiguration existingConfig;
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
            changeEmpty2Null(config.wepKeys);
            if (!config.isEphemeral() && (existingConfig = getConfiguredNetwork(config.configKey())) != null && existingConfig.isEphemeral()) {
                removeNetwork(existingConfig.networkId, this.mSystemUiUid);
            }
            NetworkUpdateResult result = addOrUpdateNetworkInternal(config, uid, packageName);
            if (!result.isSuccess()) {
                Log.e(TAG, "Failed to add/update network " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
                return result;
            }
            if (config.hiddenSSID) {
                Log.i(TAG, "set mCurrentHiddenNetId=" + result.getNetworkId() + ", SSID=" + StringUtilEx.safeDisplaySsid(config.SSID));
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

    public NetworkUpdateResult addOrUpdateNetwork(WifiConfiguration config, int uid) {
        return addOrUpdateNetwork(config, uid, null);
    }

    private boolean removeNetworkInternal(WifiConfiguration config, int uid) {
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Removing network " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
        }
        if (!config.isPasspoint() && !config.fromWifiNetworkSuggestion && config.isEnterprise()) {
            Log.i(TAG, "removeNetworkInternal: skip remove keys.");
        }
        removeConnectChoiceFromAllNetworks(config.configKey());
        this.mConfiguredNetworks.remove(config.networkId);
        this.mScanDetailCaches.remove(Integer.valueOf(config.networkId));
        this.mBackupManagerProxy.notifyDataChanged();
        localLog("removeNetworkInternal: removed config. netId=" + config.networkId + " configKey=" + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()) + " uid=" + Integer.toString(uid) + " name=" + this.mContext.getPackageManager().getNameForUid(uid));
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
            Log.e(TAG, "UID " + uid + " does not have permission to delete configuration " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
            return false;
        }
        String configKey = config.getSsidAndSecurityTypeString();
        if (!removeNetworkInternal(config, uid)) {
            Log.e(TAG, "Failed to remove network " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
            return false;
        }
        removeWpa3BlackList(configKey);
        if (networkId == this.mLastSelectedNetworkId) {
            clearLastSelectedNetwork();
        }
        sendConfiguredNetworkChangedBroadcast(config, 1);
        if (!config.ephemeral && !config.isPasspoint()) {
            saveToStore(true);
            OnSavedNetworkUpdateListener onSavedNetworkUpdateListener = this.mListener;
            if (onSavedNetworkUpdateListener != null) {
                onSavedNetworkUpdateListener.onSavedNetworkRemoved(networkId);
            }
        }
        return true;
    }

    private String getCreatorPackageName(WifiConfiguration config) {
        String creatorName = config.creatorName;
        if (!creatorName.contains(":")) {
            return creatorName;
        }
        return creatorName.substring(0, creatorName.indexOf(":"));
    }

    public Set<Integer> removeNetworksForApp(ApplicationInfo app) {
        if (app == null || app.packageName == null) {
            return Collections.emptySet();
        }
        Log.i(TAG, "Remove all networks for app " + app);
        Set<Integer> removedNetworks = new ArraySet<>();
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0]);
        for (WifiConfiguration config : copiedConfigs) {
            if (app.uid == config.creatorUid && app.packageName.equals(getCreatorPackageName(config))) {
                localLog("Removing network " + StringUtilEx.safeDisplaySsid(config.SSID) + ", application \"" + app.packageName + "\" uninstalled from user " + UserHandle.getUserId(app.uid));
                if (removeNetwork(config.networkId, this.mSystemUiUid)) {
                    removedNetworks.add(Integer.valueOf(config.networkId));
                }
            }
        }
        return removedNetworks;
    }

    /* access modifiers changed from: package-private */
    public Set<Integer> removeNetworksForUser(int userId) {
        Log.i(TAG, "Remove all networks for user " + userId);
        Set<Integer> removedNetworks = new ArraySet<>();
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0]);
        for (WifiConfiguration config : copiedConfigs) {
            if (userId == UserHandle.getUserId(config.creatorUid)) {
                localLog("Removing network " + StringUtilEx.safeDisplaySsid(config.SSID) + ", user " + userId + " removed");
                if (removeNetwork(config.networkId, this.mSystemUiUid)) {
                    removedNetworks.add(Integer.valueOf(config.networkId));
                }
            }
        }
        return removedNetworks;
    }

    public boolean removeAllEphemeralOrPasspointConfiguredNetworks() {
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Removing all passpoint or ephemeral configured networks");
        }
        boolean didRemove = false;
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0]);
        for (WifiConfiguration config : copiedConfigs) {
            if (config.isPasspoint()) {
                Log.i(TAG, "Removing passpoint network config " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
                removeNetwork(config.networkId, this.mSystemUiUid);
                didRemove = true;
            } else if (config.ephemeral) {
                Log.i(TAG, "Removing ephemeral network config " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
                removeNetwork(config.networkId, this.mSystemUiUid);
                didRemove = true;
            }
        }
        return didRemove;
    }

    public boolean removePasspointConfiguredNetwork(String fqdn) {
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0]);
        for (WifiConfiguration config : copiedConfigs) {
            if (config.isPasspoint() && TextUtils.equals(fqdn, config.FQDN)) {
                Log.i(TAG, "Removing passpoint network config " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
                removeNetwork(config.networkId, this.mSystemUiUid);
                return true;
            }
        }
        return false;
    }

    private void setNetworkSelectionEnabled(WifiConfiguration config) {
        WifiConfiguration.NetworkSelectionStatus status = config.getNetworkSelectionStatus();
        status.setNetworkSelectionStatus(0);
        status.setDisableTime(-1);
        status.setNetworkSelectionDisableReason(0);
        status.clearDisableReasonCounter();
        OnSavedNetworkUpdateListener onSavedNetworkUpdateListener = this.mListener;
        if (onSavedNetworkUpdateListener != null) {
            onSavedNetworkUpdateListener.onSavedNetworkEnabled(config.networkId);
        }
    }

    private void setNetworkSelectionTemporarilyDisabled(WifiConfiguration config, int disableReason) {
        WifiConfiguration.NetworkSelectionStatus status = config.getNetworkSelectionStatus();
        status.setNetworkSelectionStatus(1);
        status.setDisableTime(this.mClock.getElapsedSinceBootMillis());
        status.setNetworkSelectionDisableReason(disableReason);
        OnSavedNetworkUpdateListener onSavedNetworkUpdateListener = this.mListener;
        if (onSavedNetworkUpdateListener != null) {
            onSavedNetworkUpdateListener.onSavedNetworkTemporarilyDisabled(config.networkId, disableReason);
        }
        synchronized (this.mPnoConfigKeyBlacklist) {
            if (!this.mWifiInjector.getClientModeImpl().isScreenOn() && !this.mPnoConfigKeyBlacklist.contains(config.configKey())) {
                Log.i(TAG, "Add the network to pno blacklist:" + StringUtilEx.safeDisplaySsid(config.SSID));
                this.mPnoConfigKeyBlacklist.add(config.configKey());
            }
        }
    }

    private void setNetworkSelectionPermanentlyDisabled(WifiConfiguration config, int disableReason) {
        WifiConfiguration.NetworkSelectionStatus status = config.getNetworkSelectionStatus();
        status.setNetworkSelectionStatus(2);
        status.setDisableTime(-1);
        status.setNetworkSelectionDisableReason(disableReason);
        OnSavedNetworkUpdateListener onSavedNetworkUpdateListener = this.mListener;
        if (onSavedNetworkUpdateListener != null) {
            onSavedNetworkUpdateListener.onSavedNetworkPermanentlyDisabled(config.networkId, disableReason);
        }
    }

    private void setNetworkStatus(WifiConfiguration config, int status) {
        config.status = status;
        sendConfiguredNetworkChangedBroadcast(config, 2);
    }

    private boolean setNetworkSelectionStatus(WifiConfiguration config, int reason) {
        WifiConfiguration.NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (reason < 0 || reason >= 18) {
            Log.e(TAG, "Invalid Network disable reason " + reason);
            return false;
        }
        if (reason == 0) {
            setNetworkSelectionEnabled(config);
            setNetworkStatus(config, 2);
        } else if (reason < 8) {
            setNetworkSelectionTemporarilyDisabled(config, reason);
            HwWifiCHRService mHwWifiCHRService2 = HwWifiServiceFactory.getHwWifiCHRService();
            if (mHwWifiCHRService2 != null) {
                Bundle data = new Bundle();
                data.putBoolean("protalflag", config.portalNetwork);
                mHwWifiCHRService2.uploadDFTEvent(3, data);
                Log.i(TAG, "chr, trigger disable network , reason=" + reason);
                if (2 == reason) {
                    mHwWifiCHRService2.updateWifiException(909009004, "");
                }
                if (3 == reason) {
                    mHwWifiCHRService2.updateWifiException(909009003, "");
                }
                if (4 == reason) {
                    mHwWifiCHRService2.updateWifiException(909009002, "");
                }
            }
        } else if (reason == 17) {
            Log.i(TAG, "setNetworkSelectionTemporarilyDisabled since " + reason);
            setNetworkSelectionTemporarilyDisabled(config, reason);
        } else if (reason == 11) {
            setNetworkSelectionTemporarilyDisabled(config, reason);
            setNetworkStatus(config, 1);
        } else {
            setNetworkSelectionPermanentlyDisabled(config, reason);
            setNetworkStatus(config, 1);
        }
        localLog("setNetworkSelectionStatus: ssid =" + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()) + " networkStatus=" + networkStatus.getNetworkStatusString() + " disableReason=" + networkStatus.getNetworkDisableReasonString() + " at=" + createDebugTimeStampString(this.mClock.getWallClockMillis()));
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
                if (!this.mVerboseLoggingEnabled && reason >= 8 && reason != 17) {
                    return true;
                }
                Log.i(TAG, "Disable counter for network " + config.getPrintableSsid() + " for reason " + WifiConfiguration.NetworkSelectionStatus.getNetworkDisableReasonString(reason) + " is " + networkStatus.getDisableReasonCounter(reason) + " and threshold is " + disableReasonThreshold);
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
            localLog("updateNetworkRecommendation: configKey=" + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()) + " notRecommended=" + notRecommended);
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
            HwCustWifiAutoJoinController hwCustWifiAutoJoinController = this.mCust;
            if (hwCustWifiAutoJoinController != null && hwCustWifiAutoJoinController.isDeleteReenableAutoJoin() && disableReason == 2) {
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
            Log.i(TAG, "Enabling network " + networkId + " (disableOthers " + disableOthers + ")");
        }
        if (!doesUidBelongToCurrentUser(uid)) {
            Log.e(TAG, "UID " + uid + " not visible to the current user");
            return false;
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null || HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(config, true)) {
            return false;
        }
        if (disableOthers) {
            setLastSelectedNetwork(networkId);
        }
        if (!canModifyNetwork(config, uid)) {
            Log.e(TAG, "UID " + uid + " does not have permission to update configuration " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
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
            Log.i(TAG, "Disabling network " + networkId);
        }
        if (!doesUidBelongToCurrentUser(uid)) {
            Log.e(TAG, "UID " + uid + " not visible to the current user");
            return false;
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        if (networkId == this.mLastSelectedNetworkId) {
            clearLastSelectedNetwork();
        }
        if (!canModifyNetwork(config, uid)) {
            Log.e(TAG, "UID " + uid + " does not have permission to update configuration " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
            return false;
        }
        String packageName = this.mHwWifiConfigManagerEx.partOfDisableNetWork(11, uid);
        if (!updateNetworkSelectionStatus(networkId, 11)) {
            return false;
        }
        config.getNetworkSelectionStatus().setNetworkSelectionDisableName(packageName);
        saveToStore(true);
        return true;
    }

    public boolean updateLastConnectUid(int networkId, int uid) {
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Update network last connect UID for " + networkId);
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
            Log.i(TAG, "Update network after connect for " + networkId);
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
            Log.i(TAG, "Update network after disconnect for " + networkId);
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
            Log.i(TAG, "Clear network candidate scan result for " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.getNetworkSelectionStatus().setCandidate((ScanResult) null);
        config.getNetworkSelectionStatus().setCandidateScore((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
        config.getNetworkSelectionStatus().setSeenInLastQualifiedNetworkSelection(false);
        return true;
    }

    public boolean setNetworkCandidateScanResult(int networkId, ScanResult scanResult, int score) {
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Set network candidate scan result " + scanResult + " for " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            Log.e(TAG, "Cannot find network for " + networkId);
            return false;
        }
        config.getNetworkSelectionStatus().setCandidate(scanResult);
        config.getNetworkSelectionStatus().setCandidateScore(score);
        config.getNetworkSelectionStatus().setSeenInLastQualifiedNetworkSelection(true);
        return true;
    }

    private void removeConnectChoiceFromAllNetworks(String connectChoiceConfigKey) {
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Removing connect choice from all networks " + connectChoiceConfigKey);
        }
        if (connectChoiceConfigKey != null) {
            for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
                String connectChoice = config.getNetworkSelectionStatus().getConnectChoice();
                if (TextUtils.equals(connectChoice, connectChoiceConfigKey)) {
                    Log.i(TAG, "remove connect choice:" + connectChoice + " from " + StringUtilEx.safeDisplaySsid(config.SSID) + " : " + config.networkId);
                    clearNetworkConnectChoice(config.networkId);
                }
            }
        }
    }

    public boolean clearNetworkConnectChoice(int networkId) {
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Clear network connect choice for " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.getNetworkSelectionStatus().setConnectChoice((String) null);
        config.getNetworkSelectionStatus().setConnectChoiceTimestamp(-1);
        saveToStore(false);
        return true;
    }

    public boolean setNetworkConnectChoice(int networkId, String connectChoiceConfigKey, long timestamp) {
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Set network connect choice " + connectChoiceConfigKey + " for " + networkId);
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
        if (validated) {
            config.numNoInternetAccessReports = 0;
            Log.i(TAG, "validated is true, clear numNoInternetAccessReports");
        }
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
            Log.i(TAG, "Clearing last selected network");
        }
        this.mLastSelectedNetworkId = -1;
        this.mLastSelectedTimeStamp = -1;
    }

    private void setLastSelectedNetwork(int networkId) {
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Setting last selected network to " + networkId);
        }
        this.mLastSelectedNetworkId = networkId;
        this.mLastSelectedTimeStamp = this.mClock.getElapsedSinceBootMillis();
    }

    public int getLastSelectedNetwork() {
        return this.mLastSelectedNetworkId;
    }

    public String getLastSelectedNetworkConfigKey() {
        WifiConfiguration config;
        int i = this.mLastSelectedNetworkId;
        if (i == -1 || (config = getInternalConfiguredNetwork(i)) == null) {
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
        if (cache != null || config.networkId == -1) {
            return cache;
        }
        ScanDetailCache cache2 = new ScanDetailCache(config, SCAN_CACHE_ENTRIES_MAX_SIZE, 128);
        this.mScanDetailCaches.put(Integer.valueOf(config.networkId), cache2);
        return cache2;
    }

    private void saveToScanDetailCacheForNetwork(WifiConfiguration config, ScanDetail scanDetail) {
        ScanResult scanResult = scanDetail.getScanResult();
        ScanDetailCache scanDetailCache = getOrCreateScanDetailCacheForNetwork(config);
        if (scanDetailCache == null) {
            Log.e(TAG, "Could not allocate scan cache for " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
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
        if (scanResult == null) {
            Log.e(TAG, "No scan result found in scan detail");
            return null;
        }
        WifiConfiguration config = null;
        try {
            config = this.mConfiguredNetworks.getByScanResultForCurrentUser(scanResult);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed to lookup network from config map", e);
        }
        if (config != null && this.mVerboseLoggingEnabled) {
            Log.i(TAG, "getSavedNetworkFromScanDetail Found " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()) + " for " + StringUtilEx.safeDisplaySsid(scanResult.SSID) + "[" + scanResult.capabilities + "]");
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
        return createExternalWifiConfiguration(network, true, 1010);
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
                long age = result.seen - previousSeen;
                if (previousSeen > 0 && age > 0 && age < 40000 / 2) {
                    double alpha = 0.5d - (((double) age) / ((double) 40000));
                    result.level = (int) ((((double) result.level) * (1.0d - alpha)) + (((double) previousRssi) * alpha));
                }
                if (this.mVerboseLoggingEnabled) {
                    Log.i(TAG, "Updating scan detail cache freq=" + result.frequency + " BSSID=" + StringUtilEx.safeDisplaySsid(result.BSSID) + " RSSI=" + result.level + " for " + StringUtilEx.safeDisplaySsid(config.configKey()));
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
                        Iterator<String> it = scanDetailCache2.keySet().iterator();
                        while (true) {
                            if (it.hasNext()) {
                                String bbssid = it.next();
                                if (abssid.regionMatches(true, 0, bbssid, 0, 16)) {
                                    if (this.mVerboseLoggingEnabled) {
                                        Log.i(TAG, "shouldNetworksBeLinked link due to DBDC BSSID match " + StringUtilEx.safeDisplaySsid(network2.SSID) + " and " + StringUtilEx.safeDisplaySsid(network1.SSID) + " bssida " + StringUtilEx.safeDisplayBssid(abssid) + " bssidb " + StringUtilEx.safeDisplayBssid(bbssid));
                                    }
                                    return true;
                                }
                            }
                        }
                    }
                }
            } else if (network1.defaultGwMacAddress.equals(network2.defaultGwMacAddress)) {
                if (this.mVerboseLoggingEnabled) {
                    Log.i(TAG, "shouldNetworksBeLinked link due to same gw " + StringUtilEx.safeDisplaySsid(network2.SSID) + " and " + StringUtilEx.safeDisplaySsid(network1.SSID) + " GW " + StringUtilEx.safeDisplayBssid(network1.defaultGwMacAddress));
                }
                return true;
            }
            return false;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "shouldNetworksBeLinked unlink due to password mismatch");
        }
        return false;
    }

    private void linkNetworks(WifiConfiguration network1, WifiConfiguration network2) {
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "linkNetworks will link " + StringUtilEx.safeDisplaySsid(network2.getPrintableSsid()) + " and " + StringUtilEx.safeDisplaySsid(network1.getPrintableSsid()));
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
                Log.i(TAG, "unlinkNetworks un-link " + StringUtilEx.safeDisplaySsid(network1.getPrintableSsid()) + " from " + StringUtilEx.safeDisplaySsid(network2.getPrintableSsid()));
            }
            network2.linkedConfigurations.remove(network1.configKey());
        }
        if (network1.linkedConfigurations != null && network1.linkedConfigurations.get(network2.configKey()) != null) {
            if (this.mVerboseLoggingEnabled) {
                Log.i(TAG, "unlinkNetworks un-link " + StringUtilEx.safeDisplaySsid(network2.getPrintableSsid()) + " from " + StringUtilEx.safeDisplaySsid(network1.getPrintableSsid()));
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
        if (scanDetailCache != null && scanDetailCache.size() > 0) {
            for (ScanDetail scanDetail : scanDetailCache.values()) {
                ScanResult result = scanDetail.getScanResult();
                boolean valid = nowInMillis - result.seen < ageInMillis;
                if (this.mVerboseLoggingEnabled) {
                    Log.i(TAG, "fetchChannelSetForNetwork has " + StringUtilEx.safeDisplayBssid(result.BSSID) + " freq " + result.frequency + " age " + (nowInMillis - result.seen) + " ?=" + valid);
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
            dbg.append("fetchChannelSetForNetworkForPartialScan ageInMillis ");
            dbg.append(ageInMillis);
            dbg.append(" for ");
            dbg.append(StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
            dbg.append(" max ");
            dbg.append(this.mMaxNumActiveChannelsForPartialScans);
            if (scanDetailCache != null) {
                dbg.append(" bssids " + scanDetailCache.size());
            }
            if (config.linkedConfigurations != null) {
                dbg.append(" linked " + config.linkedConfigurations.size());
            }
            Log.i(TAG, dbg.toString());
        }
        Set<Integer> channelSet = new HashSet<>();
        if (homeChannelFreq > 0) {
            channelSet.add(Integer.valueOf(homeChannelFreq));
            if (channelSet.size() >= this.mMaxNumActiveChannelsForPartialScans) {
                return channelSet;
            }
        }
        long nowInMillis = this.mClock.getWallClockMillis();
        if (addToChannelSetForNetworkFromScanDetailCache(channelSet, scanDetailCache, nowInMillis, ageInMillis, this.mMaxNumActiveChannelsForPartialScans) && config.linkedConfigurations != null) {
            Iterator it = config.linkedConfigurations.keySet().iterator();
            while (it.hasNext() && ((linkedConfig = getInternalConfiguredNetwork((String) it.next())) == null || addToChannelSetForNetworkFromScanDetailCache(channelSet, getScanDetailCacheForNetwork(linkedConfig.networkId), nowInMillis, ageInMillis, this.mMaxNumActiveChannelsForPartialScans))) {
            }
        }
        return channelSet;
    }

    private Set<Integer> fetchChannelSetForNetworkForPnoScan(int networkId, long ageInMillis) {
        ScanDetailCache scanDetailCache;
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null || (scanDetailCache = getScanDetailCacheForNetwork(networkId)) == null) {
            return null;
        }
        if (this.mVerboseLoggingEnabled) {
            StringBuilder sb = new StringBuilder("fetchChannelSetForNetworkForPnoScan ageInMillis ");
            sb.append(ageInMillis);
            sb.append(" for ");
            sb.append(StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
            sb.append(" bssids " + scanDetailCache.size());
            Log.i(TAG, sb.toString());
        }
        Set<Integer> channelSet = new HashSet<>();
        addToChannelSetForNetworkFromScanDetailCache(channelSet, scanDetailCache, this.mClock.getWallClockMillis(), ageInMillis, ScoringParams.Values.MAX_EXPID);
        return channelSet;
    }

    public void clearPnoConfigKeyBlacklist() {
        synchronized (this.mPnoConfigKeyBlacklist) {
            this.mPnoConfigKeyBlacklist.clear();
        }
    }

    public List<WifiScanner.PnoSettings.PnoNetwork> retrievePnoNetworkList() {
        List<WifiScanner.PnoSettings.PnoNetwork> pnoList = new ArrayList<>();
        List<WifiConfiguration> networks = new ArrayList<>(getInternalConfiguredNetworks());
        Iterator<WifiConfiguration> iter = networks.iterator();
        synchronized (this.mPnoConfigKeyBlacklist) {
            while (iter.hasNext()) {
                WifiConfiguration config = iter.next();
                if (config.ephemeral || config.isPasspoint() || config.getNetworkSelectionStatus().isNetworkPermanentlyDisabled() || config.getNetworkSelectionStatus().isNetworkTemporaryDisabled() || this.mPnoConfigKeyBlacklist.contains(config.configKey())) {
                    iter.remove();
                }
            }
        }
        if (networks.isEmpty()) {
            return pnoList;
        }
        Collections.sort(networks, sScanListComparator);
        if (this.mPnoRecencySortingEnabled) {
            WifiConfiguration lastConnectedNetwork = networks.stream().max(Comparator.comparing($$Lambda$WifiConfigManager$IQAd8DT29bH7BRNkSq57y94BdXA.INSTANCE)).get();
            if (lastConnectedNetwork.lastConnected != 0) {
                networks.remove(networks.indexOf(lastConnectedNetwork));
                networks.add(0, lastConnectedNetwork);
            }
        }
        for (WifiConfiguration config2 : networks) {
            WifiScanner.PnoSettings.PnoNetwork pnoNetwork = WifiConfigurationUtil.createPnoNetwork(config2);
            pnoList.add(pnoNetwork);
            if (this.mPnoFrequencyCullingEnabled) {
                Set<Integer> channelSet = fetchChannelSetForNetworkForPnoScan(config2.networkId, MAX_PNO_SCAN_FREQUENCY_AGE_MS);
                if (channelSet != null) {
                    pnoNetwork.frequencies = channelSet.stream().mapToInt($$Lambda$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray();
                }
                if (this.mVerboseLoggingEnabled) {
                    Log.i(TAG, "retrievePnoNetworkList " + StringUtilEx.safeDisplaySsid(pnoNetwork.ssid) + ":" + Arrays.toString(pnoNetwork.frequencies));
                }
            }
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
                Log.i(TAG, "retrieveHiddenNetworkList: get mCurrentHiddenNetId=" + config.networkId + ", SSID=" + StringUtilEx.safeDisplaySsid(config.SSID));
                mCurrentHiddenNetId = -1;
                currentHiddenConfig.add(0, config);
                iter.remove();
            }
        }
        Collections.sort(networks, this.mHwWifiConfigManagerEx.get());
        this.mHwWifiConfigManagerEx.partOfRetrieveHiddenNetworkList(restoreNetworks, networks, currentHiddenConfig);
        StringBuffer debugHiddenList = new StringBuffer("");
        for (WifiConfiguration config2 : networks) {
            hiddenList.add(new WifiScanner.ScanSettings.HiddenNetwork(TextUtils.isEmpty(config2.oriSsid) ? config2.SSID : config2.oriSsid));
            debugHiddenList.append(" " + StringUtilEx.safeDisplaySsid(config2.SSID));
        }
        Log.i(TAG, "retrieve sorted hiddenLists_SSID=" + debugHiddenList.toString());
        return hiddenList;
    }

    public boolean wasEphemeralNetworkDeleted(String ssid) {
        if (!this.mDeletedEphemeralSsidsToTimeMap.containsKey(ssid)) {
            return false;
        }
        if (this.mClock.getWallClockMillis() - this.mDeletedEphemeralSsidsToTimeMap.get(ssid).longValue() <= DELETED_EPHEMERAL_SSID_EXPIRY_MS) {
            return true;
        }
        this.mDeletedEphemeralSsidsToTimeMap.remove(ssid);
        return false;
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
            if ((config.ephemeral || config.isPasspoint()) && TextUtils.equals(config.SSID, ssid)) {
                foundConfig = config;
                break;
            }
        }
        if (foundConfig == null) {
            return null;
        }
        this.mDeletedEphemeralSsidsToTimeMap.put(ssid, Long.valueOf(this.mClock.getWallClockMillis()));
        Log.i(TAG, "Forget ephemeral SSID " + StringUtilEx.safeDisplaySsid(ssid) + " num=" + this.mDeletedEphemeralSsidsToTimeMap.size());
        if (foundConfig.ephemeral) {
            Log.i(TAG, "Found ephemeral config in disableEphemeralNetwork: " + foundConfig.networkId);
        } else if (foundConfig.isPasspoint()) {
            Log.i(TAG, "Found Passpoint config in disableEphemeralNetwork: " + foundConfig.networkId + ", FQDN: " + foundConfig.FQDN);
        }
        removeConnectChoiceFromAllNetworks(foundConfig.configKey());
        return foundConfig;
    }

    @VisibleForTesting
    public void clearDeletedEphemeralNetworks() {
        this.mDeletedEphemeralSsidsToTimeMap.clear();
    }

    public void resetSimNetworks() {
        if (this.mVerboseLoggingEnabled) {
            localLog("resetSimNetworks");
        }
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (TelephonyUtil.isSimConfig(config)) {
                if (config.enterpriseConfig.getEapMethod() == 0) {
                    Pair<String, String> currentIdentity = TelephonyUtil.getSimIdentity(this.mTelephonyManager, new TelephonyUtil(), config, this.mWifiInjector.getCarrierNetworkConfig());
                    if (this.mVerboseLoggingEnabled) {
                        Log.i(TAG, "New identity for config");
                    }
                    if (currentIdentity == null) {
                        Log.i(TAG, "Identity is null");
                    } else {
                        config.enterpriseConfig.setIdentity((String) currentIdentity.first);
                    }
                } else {
                    config.enterpriseConfig.setIdentity("");
                    if (!TelephonyUtil.isAnonymousAtRealmIdentity(config.enterpriseConfig.getAnonymousIdentity())) {
                        config.enterpriseConfig.setAnonymousIdentity("");
                    }
                }
            }
        }
    }

    private void handleUserUnlockOrSwitch(int userId) {
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Loading from store after user switch/unlock for " + userId);
        }
        if (loadFromUserStoreAfterUnlockOrSwitch(userId)) {
            saveToStore(true);
            this.mPendingUnlockStoreRead = false;
        }
    }

    public Set<Integer> handleUserSwitch(int userId) {
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Handling user switch for " + userId);
        }
        int i = this.mCurrentUserId;
        if (userId == i) {
            Log.w(TAG, "User already in foreground " + userId);
            return new HashSet();
        } else if (this.mPendingStoreRead) {
            Log.w(TAG, "User switch before store is read!");
            this.mConfiguredNetworks.setNewUser(userId);
            this.mCurrentUserId = userId;
            this.mDeferredUserUnlockRead = false;
            this.mPendingUnlockStoreRead = true;
            return new HashSet();
        } else {
            if (this.mUserManager.isUserUnlockingOrUnlocked(i)) {
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
            Log.i(TAG, "Handling user unlock for " + userId);
        }
        int i = this.mCurrentUserId;
        if (userId != i) {
            Log.e(TAG, "Ignore user unlock for non current user " + userId);
        } else if (this.mPendingStoreRead) {
            Log.w(TAG, "Ignore user unlock until store is read!");
            this.mDeferredUserUnlockRead = true;
        } else if (this.mPendingUnlockStoreRead) {
            handleUserUnlockOrSwitch(i);
        }
    }

    public void handleUserStop(int userId) {
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Handling user stop for " + userId);
        }
        int i = this.mCurrentUserId;
        if (userId == i && this.mUserManager.isUserUnlockingOrUnlocked(i)) {
            saveToStore(true);
            clearInternalUserData(this.mCurrentUserId);
        }
    }

    private void clearInternalData() {
        localLog("clearInternalData: Clearing all internal data");
        this.mConfiguredNetworks.clear();
        this.mDeletedEphemeralSsidsToTimeMap.clear();
        this.mRandomizedMacAddressMapping.clear();
        this.mRandomizedMacAddressMappingPlus.clear();
        this.mRandomizedMacAddressMatchInfoMap.clear();
        this.mScanDetailCaches.clear();
        clearLastSelectedNetwork();
    }

    private Set<Integer> clearInternalUserData(int userId) {
        localLog("clearInternalUserData: Clearing user internal data for " + userId);
        Set<Integer> removedNetworkIds = new HashSet<>();
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (!config.shared && WifiConfigurationUtil.doesUidBelongToAnyProfile(config.creatorUid, this.mUserManager.getProfiles(userId))) {
                removedNetworkIds.add(Integer.valueOf(config.networkId));
                localLog("clearInternalUserData: removed config. netId=" + config.networkId + " configKey=" + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()));
                this.mConfiguredNetworks.remove(config.networkId);
            }
        }
        this.mDeletedEphemeralSsidsToTimeMap.clear();
        this.mScanDetailCaches.clear();
        clearLastSelectedNetwork();
        return removedNetworkIds;
    }

    private void loadInternalDataFromSharedStore(List<WifiConfiguration> configurations, Map<String, String> macAddressMapping) {
        for (WifiConfiguration configuration : configurations) {
            int i = this.mNextNetworkId;
            this.mNextNetworkId = i + 1;
            configuration.networkId = i;
            if (this.mVerboseLoggingEnabled) {
                Log.i(TAG, "Adding network from shared store " + StringUtilEx.safeDisplaySsid(configuration.getPrintableSsid()));
            }
            try {
                this.mConfiguredNetworks.put(configuration);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Failed to add network to config map", e);
            }
        }
        this.mRandomizedMacAddressMapping.putAll(macAddressMapping);
        this.mRandomizedMacAddressMappingPlus.putAll(this.mRandomizedMacStoreData.getMacMappingPlus());
        generateRandomizedMacAddressMatchInfoMap();
    }

    private void loadInternalDataFromUserStore(List<WifiConfiguration> configurations, Map<String, Long> deletedEphemeralSsidsToTimeMap) {
        for (WifiConfiguration configuration : configurations) {
            int i = this.mNextNetworkId;
            this.mNextNetworkId = i + 1;
            configuration.networkId = i;
            if (this.mVerboseLoggingEnabled) {
                Log.i(TAG, "Adding network from user store " + StringUtilEx.safeDisplaySsid(configuration.getPrintableSsid()));
            }
            try {
                this.mConfiguredNetworks.put(configuration);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Failed to add network to config map", e);
            }
        }
        this.mDeletedEphemeralSsidsToTimeMap.putAll(deletedEphemeralSsidsToTimeMap);
    }

    private void generateRandomizedMacAddresses() {
        ClientModeImpl clientModeImpl = this.mWifiInjector.getClientModeImpl();
        if (clientModeImpl != null && clientModeImpl.isConnectedMacRandomizationEnabled()) {
            for (WifiConfiguration config : getInternalConfiguredNetworks()) {
                if (DEFAULT_MAC_ADDRESS.equals(config.getRandomizedMacAddress())) {
                    setRandomizedMacToPersistentMac(config);
                }
            }
        }
    }

    private void loadInternalData(List<WifiConfiguration> sharedConfigurations, List<WifiConfiguration> userConfigurations, Map<String, Long> deletedEphemeralSsidsToTimeMap, Map<String, String> macAddressMapping) {
        clearInternalData();
        loadInternalDataFromSharedStore(sharedConfigurations, macAddressMapping);
        loadInternalDataFromUserStore(userConfigurations, deletedEphemeralSsidsToTimeMap);
        generateRandomizedMacAddresses();
        if (this.mConfiguredNetworks.sizeForAllUsers() == 0) {
            Log.w(TAG, "No stored networks found.");
        }
        resetSimNetworks();
        sendConfiguredNetworksChangedBroadcast();
        this.mPendingStoreRead = false;
    }

    public boolean loadFromStore() {
        if (this.mDeferredUserUnlockRead) {
            Log.i(TAG, "Handling user unlock before loading from store.");
            List<WifiConfigStore.StoreFile> userStoreFiles = WifiConfigStore.createUserFiles(this.mCurrentUserId);
            if (userStoreFiles == null) {
                Log.wtf(TAG, "Failed to create user store files");
                return false;
            }
            this.mWifiConfigStore.setUserStores(userStoreFiles);
            this.mDeferredUserUnlockRead = false;
        }
        try {
            this.mWifiConfigStore.read();
            loadInternalData(this.mNetworkListSharedStoreData.getConfigurations(), this.mNetworkListUserStoreData.getConfigurations(), this.mDeletedEphemeralSsidsStoreData.getSsidToTimeMap(), this.mRandomizedMacStoreData.getMacMapping());
            if (!HuaweiTelephonyConfigs.isChinaMobile()) {
                return true;
            }
            this.mHwWifiConfigManagerEx.initLastPriority();
            return true;
        } catch (IOException e) {
            Log.wtf(TAG, "Reading from new store failed. All saved networks are lost!", e);
            return false;
        } catch (XmlPullParserException e2) {
            this.mPendingStoreRead = false;
            Log.wtf(TAG, "XML deserialization of store failed. All saved networks are lost!", e2);
            return false;
        }
    }

    private boolean loadFromUserStoreAfterUnlockOrSwitch(int userId) {
        try {
            List<WifiConfigStore.StoreFile> userStoreFiles = WifiConfigStore.createUserFiles(userId);
            if (userStoreFiles == null) {
                Log.e(TAG, "Failed to create user store files");
                return false;
            }
            this.mWifiConfigStore.switchUserStoresAndRead(userStoreFiles);
            loadInternalDataFromUserStore(this.mNetworkListUserStoreData.getConfigurations(), this.mDeletedEphemeralSsidsStoreData.getSsidToTimeMap());
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
        for (Integer num : legacyPasspointNetId) {
            this.mConfiguredNetworks.remove(num.intValue());
        }
        this.mNetworkListSharedStoreData.setConfigurations(sharedConfigurations);
        this.mNetworkListUserStoreData.setConfigurations(userConfigurations);
        this.mDeletedEphemeralSsidsStoreData.setSsidToTimeMap(this.mDeletedEphemeralSsidsToTimeMap);
        this.mRandomizedMacStoreData.setMacMapping(this.mRandomizedMacAddressMapping);
        this.mRandomizedMacStoreData.setMacMappingPlus(this.mRandomizedMacAddressMappingPlus);
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
        LocalLog localLog = this.mLocalLog;
        if (localLog != null) {
            localLog.log(s);
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
        pw.println("WifiConfigManager - PNO scan frequency culling enabled = " + this.mPnoFrequencyCullingEnabled);
        pw.println("WifiConfigManager - PNO scan recency sorting enabled = " + this.mPnoRecencySortingEnabled);
        this.mWifiConfigStore.dump(fd, pw, args);
    }

    private boolean canModifyProxySettings(int uid) {
        DevicePolicyManagerInternal dpmi = this.mWifiPermissionsWrapper.getDevicePolicyManagerInternal();
        boolean isUidProfileOwner = dpmi != null && dpmi.isActiveAdminWithPolicy(uid, -1);
        boolean isUidDeviceOwner = dpmi != null && dpmi.isActiveAdminWithPolicy(uid, -2);
        boolean hasNetworkSettingsPermission = this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid);
        boolean hasNetworkSetupWizardPermission = this.mWifiPermissionsUtil.checkNetworkSetupWizardPermission(uid);
        if (isUidDeviceOwner || isUidProfileOwner || hasNetworkSettingsPermission || hasNetworkSetupWizardPermission) {
            return true;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "UID: " + uid + " cannot modify WifiConfiguration proxy settings. hasNetworkSettings=" + hasNetworkSettingsPermission + " hasNetworkSetupWizard=" + hasNetworkSetupWizardPermission + " DeviceOwner=" + isUidDeviceOwner + " ProfileOwner=" + isUidProfileOwner);
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

    @Override // com.android.server.wifi.IHwWifiConfigManagerInner
    public boolean tryEnableNetwork_2(WifiConfiguration config) {
        return tryEnableNetwork(config);
    }

    @Override // com.android.server.wifi.IHwWifiConfigManagerInner
    public void sendConfiguredNetworksChangedBroadcast_2() {
        sendConfiguredNetworksChangedBroadcast();
    }

    @Override // com.android.server.wifi.IHwWifiConfigManagerInner
    public NetworkUpdateResult addOrUpdateNetwork_2(WifiConfiguration config, int uid) {
        return addOrUpdateNetwork(config, uid);
    }

    @Override // com.android.server.wifi.IHwWifiConfigManagerInner
    public UserManager getManager() {
        return this.mUserManager;
    }

    @Override // com.android.server.wifi.IHwWifiConfigManagerInner
    public int getCurrentUserId() {
        return this.mCurrentUserId;
    }

    @Override // com.android.server.wifi.IHwWifiConfigManagerInner
    public ConfigurationMap getMConfiguredNetworks() {
        return this.mConfiguredNetworks;
    }

    @Override // com.android.server.wifi.IHwWifiConfigManagerInner
    public Collection<WifiConfiguration> getInternalConfiguredNetworks_2() {
        return getInternalConfiguredNetworks();
    }

    @Override // com.android.server.wifi.IHwWifiConfigManagerInner
    public boolean updateNetworkSelectionStatus_2(WifiConfiguration config, int reason) {
        return updateNetworkSelectionStatus(config, reason);
    }

    @Override // com.android.server.wifi.IHwWifiConfigManagerInner
    public WifiConfigurationUtil.WifiConfigurationComparator get() {
        return sScanListComparator;
    }

    public boolean isBssidAlgorithmOn() {
        return false;
    }

    public boolean isRetreatAlgorithmOn() {
        return false;
    }

    public void generateRandomizedMacAddressMatchInfoMap() {
    }

    public void updateRandomizedMacAddressPlus(WifiConfiguration config, String bssid) {
    }

    public String searchRandomizedMacAddressPlusEntry(WifiConfiguration config) {
        return null;
    }

    public void removeWpa3BlackList(String configKey) {
    }
}
