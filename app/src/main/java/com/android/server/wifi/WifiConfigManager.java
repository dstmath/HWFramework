package com.android.server.wifi;

import android.app.admin.DevicePolicyManagerInternal;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.UserInfo;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.NetworkInfo.DetailedState;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.PasspointManagementObjectDefinition;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner.PnoSettings.PnoNetwork;
import android.net.wifi.WpsInfo;
import android.net.wifi.WpsResult;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.LocalServices;
import com.android.server.net.DelayedDiskWrite;
import com.android.server.net.DelayedDiskWrite.Writer;
import com.android.server.net.IpConfigStore;
import com.android.server.wifi.anqp.ANQPElement;
import com.android.server.wifi.anqp.ANQPFactory;
import com.android.server.wifi.anqp.Constants.ANQPElementType;
import com.android.server.wifi.hotspot2.ANQPData;
import com.android.server.wifi.hotspot2.AnqpCache;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.PasspointMatch;
import com.android.server.wifi.hotspot2.SupplicantBridge;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager;
import com.android.server.wifi.hotspot2.pps.Credential;
import com.android.server.wifi.hotspot2.pps.HomeSP;
import com.google.protobuf.nano.Extension;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class WifiConfigManager extends AbsWifiConfigManager {
    private static final /* synthetic */ int[] -android-net-IpConfiguration$IpAssignmentSwitchesValues = null;
    private static final /* synthetic */ int[] -android-net-IpConfiguration$ProxySettingsSwitchesValues = null;
    private static final /* synthetic */ int[] -android-net-NetworkInfo$DetailedStateSwitchesValues = null;
    private static final boolean DBG = true;
    private static final int DEFAULT_MAX_DHCP_RETRIES = 9;
    private static final String DELETED_CONFIG_PSK = "Mjkd86jEMGn79KhKll298Uu7-deleted";
    protected static final boolean HWFLOW = false;
    private static boolean HWLOGW_E = false;
    private static final String IP_CONFIG_FILE = null;
    public static final int MAX_NUM_SCAN_CACHE_ENTRIES = 128;
    public static final int MAX_RX_PACKET_FOR_FULL_SCANS = 16;
    public static final int MAX_RX_PACKET_FOR_PARTIAL_SCANS = 80;
    public static final int MAX_TX_PACKET_FOR_FULL_SCANS = 8;
    public static final int MAX_TX_PACKET_FOR_PARTIAL_SCANS = 40;
    private static final int[] NETWORK_SELECTION_DISABLE_THRESHOLD = null;
    private static final int[] NETWORK_SELECTION_DISABLE_TIMEOUT = null;
    private static final String PPS_FILE = "/data/misc/wifi/PerProviderSubscription.conf";
    public static final boolean ROAM_ON_ANY = false;
    public static final String TAG = "WifiConfigManager";
    private static final String WIFI_VERBOSE_LOGS_KEY = "WIFI_VERBOSE_LOGS";
    private static final PnoListComparator sConnectedPnoListComparator = null;
    private static final PnoListComparator sDisconnectedPnoListComparator = null;
    private static boolean sVDBG;
    private static boolean sVVDBG;
    private ScanDetail mActiveScanDetail;
    private final Object mActiveScanDetailLock;
    public final AtomicInteger mAlwaysEnableScansWhileAssociated;
    private final AnqpCache mAnqpCache;
    public int mBadLinkSpeed24;
    public int mBadLinkSpeed5;
    public AtomicInteger mBandAward5Ghz;
    private Clock mClock;
    private final ConfigurationMap mConfiguredNetworks;
    private Context mContext;
    public AtomicInteger mCurrentNetworkBoost;
    private int mCurrentUserId;
    public Set<String> mDeletedEphemeralSSIDs;
    public final AtomicBoolean mEnableAutoJoinWhenAssociated;
    public final AtomicBoolean mEnableChipWakeUpWhenAssociated;
    public boolean mEnableLinkDebouncing;
    private final boolean mEnableOsuQueries;
    public final AtomicBoolean mEnableRssiPollWhenAssociated;
    public final AtomicInteger mEnableVerboseLogging;
    public boolean mEnableWifiCellularHandoverUserTriggeredAdjustment;
    private FrameworkFacade mFacade;
    public int mGoodLinkSpeed24;
    public int mGoodLinkSpeed5;
    private IpConfigStore mIpconfigStore;
    private final KeyStore mKeyStore;
    private int mLastPriority;
    private String mLastSelectedConfiguration;
    private long mLastSelectedTimeStamp;
    public long mLastUnwantedNetworkDisconnectTimestamp;
    private final LocalLog mLocalLog;
    private HashSet<String> mLostConfigsDbg;
    private final PasspointManagementObjectManager mMOManager;
    public final AtomicInteger mMaxNumActiveChannelsForPartialScans;
    public int mNetworkSwitchingBlackListPeriodMs;
    private boolean mOnlyLinkSameCredentialConfigurations;
    private final SIMAccessor mSIMAccessor;
    private ConcurrentHashMap<Integer, ScanDetailCache> mScanDetailCaches;
    private boolean mShowNetworks;
    private final SupplicantBridge mSupplicantBridge;
    private final SupplicantBridgeCallbacks mSupplicantBridgeCallbacks;
    public AtomicInteger mThresholdMinimumRssi24;
    public AtomicInteger mThresholdMinimumRssi5;
    public final AtomicInteger mThresholdQualifiedRssi24;
    public AtomicInteger mThresholdQualifiedRssi5;
    public AtomicInteger mThresholdSaturatedRssi24;
    public final AtomicInteger mThresholdSaturatedRssi5;
    private final UserManager mUserManager;
    private final WifiConfigStore mWifiConfigStore;
    private final WifiNetworkHistory mWifiNetworkHistory;
    private DelayedDiskWrite mWriter;

    private static class PnoListComparator implements Comparator<WifiConfiguration> {
        public final int ENABLED_NETWORK_SCORE;
        public final int PERMANENTLY_DISABLED_NETWORK_SCORE;
        public final int TEMPORARY_DISABLED_NETWORK_SCORE;

        private PnoListComparator() {
            this.ENABLED_NETWORK_SCORE = 3;
            this.TEMPORARY_DISABLED_NETWORK_SCORE = 2;
            this.PERMANENTLY_DISABLED_NETWORK_SCORE = 1;
        }

        public int compare(WifiConfiguration a, WifiConfiguration b) {
            int configAScore = getPnoNetworkSortScore(a);
            int configBScore = getPnoNetworkSortScore(b);
            if (configAScore == configBScore) {
                return compareConfigurations(a, b);
            }
            return Integer.compare(configBScore, configAScore);
        }

        public int compareConfigurations(WifiConfiguration a, WifiConfiguration b) {
            return 0;
        }

        private int getPnoNetworkSortScore(WifiConfiguration config) {
            if (config.getNetworkSelectionStatus().isNetworkEnabled()) {
                return 3;
            }
            if (config.getNetworkSelectionStatus().isNetworkTemporaryDisabled()) {
                return 2;
            }
            return 1;
        }
    }

    /* renamed from: com.android.server.wifi.WifiConfigManager.3 */
    class AnonymousClass3 implements Writer {
        final /* synthetic */ String val$fqdn;
        final /* synthetic */ HomeSP val$homeSP;

        AnonymousClass3(HomeSP val$homeSP, String val$fqdn) {
            this.val$homeSP = val$homeSP;
            this.val$fqdn = val$fqdn;
        }

        public void onWriteCalled(DataOutputStream out) throws IOException {
            try {
                if (this.val$homeSP != null) {
                    WifiConfigManager.this.mMOManager.addSP(this.val$homeSP);
                } else {
                    WifiConfigManager.this.mMOManager.removeSP(this.val$fqdn);
                }
            } catch (IOException e) {
                WifiConfigManager.this.loge("Could not write /data/misc/wifi/PerProviderSubscription.conf : " + e);
            }
        }
    }

    private class SupplicantBridgeCallbacks implements com.android.server.wifi.hotspot2.SupplicantBridge.SupplicantBridgeCallbacks {
        private SupplicantBridgeCallbacks() {
        }

        public void notifyANQPResponse(ScanDetail scanDetail, Map<ANQPElementType, ANQPElement> anqpElements) {
            WifiConfigManager.this.updateAnqpCache(scanDetail, anqpElements);
            if (anqpElements != null && !anqpElements.isEmpty()) {
                scanDetail.propagateANQPInfo(anqpElements);
                Map<HomeSP, PasspointMatch> matches = WifiConfigManager.this.matchNetwork(scanDetail, WifiConfigManager.ROAM_ON_ANY);
                Log.d(Utils.hs2LogTag(getClass()), scanDetail.getSSID() + " pass 2 matches: " + WifiConfigManager.toMatchString(matches));
                WifiConfigManager.this.cacheScanResultForPasspointConfigs(scanDetail, matches, null);
            }
        }

        public void notifyIconFailed(long bssid) {
            Intent intent = new Intent("android.net.wifi.PASSPOINT_ICON_RECEIVED");
            intent.addFlags(67108864);
            intent.putExtra("bssid", bssid);
            WifiConfigManager.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private static /* synthetic */ int[] -getandroid-net-IpConfiguration$IpAssignmentSwitchesValues() {
        if (-android-net-IpConfiguration$IpAssignmentSwitchesValues != null) {
            return -android-net-IpConfiguration$IpAssignmentSwitchesValues;
        }
        int[] iArr = new int[IpAssignment.values().length];
        try {
            iArr[IpAssignment.DHCP.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[IpAssignment.STATIC.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[IpAssignment.UNASSIGNED.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -android-net-IpConfiguration$IpAssignmentSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getandroid-net-IpConfiguration$ProxySettingsSwitchesValues() {
        if (-android-net-IpConfiguration$ProxySettingsSwitchesValues != null) {
            return -android-net-IpConfiguration$ProxySettingsSwitchesValues;
        }
        int[] iArr = new int[ProxySettings.values().length];
        try {
            iArr[ProxySettings.NONE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ProxySettings.PAC.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ProxySettings.STATIC.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ProxySettings.UNASSIGNED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -android-net-IpConfiguration$ProxySettingsSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getandroid-net-NetworkInfo$DetailedStateSwitchesValues() {
        if (-android-net-NetworkInfo$DetailedStateSwitchesValues != null) {
            return -android-net-NetworkInfo$DetailedStateSwitchesValues;
        }
        int[] iArr = new int[DetailedState.values().length];
        try {
            iArr[DetailedState.AUTHENTICATING.ordinal()] = 10;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DetailedState.BLOCKED.ordinal()] = 11;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DetailedState.CAPTIVE_PORTAL_CHECK.ordinal()] = 12;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DetailedState.CONNECTED.ordinal()] = 1;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[DetailedState.CONNECTING.ordinal()] = 13;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[DetailedState.DISCONNECTED.ordinal()] = 2;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[DetailedState.DISCONNECTING.ordinal()] = 14;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[DetailedState.FAILED.ordinal()] = 15;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[DetailedState.IDLE.ordinal()] = MAX_RX_PACKET_FOR_FULL_SCANS;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[DetailedState.OBTAINING_IPADDR.ordinal()] = 17;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[DetailedState.SCANNING.ordinal()] = 18;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[DetailedState.SUSPENDED.ordinal()] = 19;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[DetailedState.VERIFYING_POOR_LINK.ordinal()] = 20;
        } catch (NoSuchFieldError e13) {
        }
        -android-net-NetworkInfo$DetailedStateSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.WifiConfigManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.WifiConfigManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiConfigManager.<clinit>():void");
    }

    WifiConfigManager(Context context, WifiNative wifiNative, FrameworkFacade facade, Clock clock, UserManager userManager, KeyStore keyStore) {
        this.mEnableAutoJoinWhenAssociated = new AtomicBoolean();
        this.mEnableChipWakeUpWhenAssociated = new AtomicBoolean(DBG);
        this.mEnableRssiPollWhenAssociated = new AtomicBoolean(DBG);
        this.mThresholdSaturatedRssi5 = new AtomicInteger();
        this.mThresholdQualifiedRssi24 = new AtomicInteger();
        this.mEnableVerboseLogging = new AtomicInteger(0);
        this.mAlwaysEnableScansWhileAssociated = new AtomicInteger(0);
        this.mMaxNumActiveChannelsForPartialScans = new AtomicInteger();
        this.mThresholdQualifiedRssi5 = new AtomicInteger();
        this.mThresholdMinimumRssi5 = new AtomicInteger();
        this.mThresholdSaturatedRssi24 = new AtomicInteger();
        this.mThresholdMinimumRssi24 = new AtomicInteger();
        this.mCurrentNetworkBoost = new AtomicInteger();
        this.mBandAward5Ghz = new AtomicInteger();
        this.mLastUnwantedNetworkDisconnectTimestamp = 0;
        this.mDeletedEphemeralSSIDs = new HashSet();
        this.mActiveScanDetailLock = new Object();
        this.mShowNetworks = ROAM_ON_ANY;
        this.mCurrentUserId = 0;
        this.mLastPriority = -1;
        this.mLastSelectedConfiguration = null;
        this.mLastSelectedTimeStamp = -1;
        this.mLostConfigsDbg = new HashSet();
        this.mContext = context;
        this.mFacade = facade;
        this.mClock = clock;
        this.mKeyStore = keyStore;
        this.mUserManager = userManager;
        if (this.mShowNetworks) {
            this.mLocalLog = WifiNative.getLocalLog();
        } else {
            this.mLocalLog = null;
        }
        this.mOnlyLinkSameCredentialConfigurations = this.mContext.getResources().getBoolean(17956896);
        this.mMaxNumActiveChannelsForPartialScans.set(this.mContext.getResources().getInteger(17694778));
        this.mEnableLinkDebouncing = this.mContext.getResources().getBoolean(17956889);
        this.mBandAward5Ghz.set(this.mContext.getResources().getInteger(17694740));
        this.mThresholdMinimumRssi5.set(this.mContext.getResources().getInteger(17694750));
        this.mThresholdQualifiedRssi5.set(this.mContext.getResources().getInteger(17694751));
        this.mThresholdSaturatedRssi5.set(this.mContext.getResources().getInteger(17694752));
        this.mThresholdMinimumRssi24.set(this.mContext.getResources().getInteger(17694753));
        this.mThresholdQualifiedRssi24.set(this.mContext.getResources().getInteger(17694754));
        this.mThresholdSaturatedRssi24.set(this.mContext.getResources().getInteger(17694755));
        this.mEnableWifiCellularHandoverUserTriggeredAdjustment = this.mContext.getResources().getBoolean(17956894);
        this.mBadLinkSpeed24 = this.mContext.getResources().getInteger(17694756);
        this.mBadLinkSpeed5 = this.mContext.getResources().getInteger(17694757);
        this.mGoodLinkSpeed24 = this.mContext.getResources().getInteger(17694758);
        this.mGoodLinkSpeed5 = this.mContext.getResources().getInteger(17694759);
        this.mEnableAutoJoinWhenAssociated.set(this.mContext.getResources().getBoolean(17956895));
        this.mCurrentNetworkBoost.set(this.mContext.getResources().getInteger(17694783));
        this.mNetworkSwitchingBlackListPeriodMs = this.mContext.getResources().getInteger(17694767);
        boolean hs2on = this.mContext.getResources().getBoolean(17956887);
        Log.d(Utils.hs2LogTag(getClass()), "Passpoint is " + (hs2on ? "enabled" : "disabled"));
        this.mConfiguredNetworks = new ConfigurationMap(userManager);
        this.mMOManager = new PasspointManagementObjectManager(new File(PPS_FILE), hs2on);
        this.mEnableOsuQueries = DBG;
        this.mAnqpCache = new AnqpCache(this.mClock);
        this.mSupplicantBridgeCallbacks = new SupplicantBridgeCallbacks();
        this.mSupplicantBridge = new SupplicantBridge(wifiNative, this.mSupplicantBridgeCallbacks);
        this.mScanDetailCaches = new ConcurrentHashMap(MAX_RX_PACKET_FOR_FULL_SCANS, 0.75f, 2);
        this.mSIMAccessor = new SIMAccessor(this.mContext);
        this.mWriter = new DelayedDiskWrite();
        this.mIpconfigStore = new IpConfigStore(this.mWriter);
        this.mWifiNetworkHistory = new WifiNetworkHistory(context, this.mLocalLog, this.mWriter);
        this.mWifiConfigStore = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiConfigStore(wifiNative, this.mKeyStore, this.mLocalLog, this.mShowNetworks, DBG);
    }

    public void trimANQPCache(boolean all) {
        this.mAnqpCache.clear(all, DBG);
    }

    void enableVerboseLogging(int verbose) {
        this.mEnableVerboseLogging.set(verbose);
        if (verbose > 0) {
            sVDBG = DBG;
            this.mShowNetworks = DBG;
        } else {
            sVDBG = ROAM_ON_ANY;
        }
        if (verbose > 1) {
            sVVDBG = DBG;
        } else {
            sVVDBG = ROAM_ON_ANY;
        }
    }

    void loadAndEnableAllNetworks() {
        log("Loading config and enabling all networks ");
        loadConfiguredNetworks();
        enableAllNetworks();
    }

    int getConfiguredNetworksSize() {
        return this.mConfiguredNetworks.sizeForCurrentUser();
    }

    private List<WifiConfiguration> getSavedNetworks(Map<String, String> pskMap) {
        List<WifiConfiguration> networks = new ArrayList();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
            WifiConfiguration newConfig = new WifiConfiguration(config);
            if (!config.ephemeral) {
                if (pskMap != null && config.allowedKeyManagement != null && config.allowedKeyManagement.get(1) && pskMap.containsKey(config.configKey(DBG))) {
                    newConfig.preSharedKey = (String) pskMap.get(config.configKey(DBG));
                }
                networks.add(newConfig);
            }
        }
        return networks;
    }

    private List<WifiConfiguration> getAllConfiguredNetworks() {
        List<WifiConfiguration> networks = new ArrayList();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
            networks.add(new WifiConfiguration(config));
        }
        return networks;
    }

    public List<WifiConfiguration> getSavedNetworks() {
        return getSavedNetworks(null);
    }

    List<WifiConfiguration> getPrivilegedSavedNetworks() {
        List<WifiConfiguration> configurations = getSavedNetworks(getCredentialsByConfigKeyMap());
        for (WifiConfiguration configuration : configurations) {
            try {
                configuration.setPasspointManagementObjectTree(this.mMOManager.getMOTree(configuration.FQDN));
            } catch (IOException ioe) {
                Log.w(TAG, "Failed to parse MO from " + configuration.FQDN + ": " + ioe);
            }
        }
        return configurations;
    }

    public Set<Integer> getHiddenConfiguredNetworkIds() {
        return this.mConfiguredNetworks.getHiddenNetworkIdsForCurrentUser();
    }

    WifiConfiguration getMatchingConfig(ScanResult scanResult) {
        if (scanResult == null) {
            return null;
        }
        for (Entry entry : this.mScanDetailCaches.entrySet()) {
            ScanDetailCache cache = (ScanDetailCache) entry.getValue();
            WifiConfiguration config = getWifiConfiguration(((Integer) entry.getKey()).intValue());
            if (config != null && cache.get(scanResult.BSSID) != null) {
                return config;
            }
        }
        return null;
    }

    private Map<String, String> getCredentialsByConfigKeyMap() {
        return readNetworkVariablesFromSupplicantFile("psk");
    }

    private boolean isLastSelectedConfigNetwork(WifiConfiguration config) {
        if (this.mLastSelectedConfiguration == null || config == null || config.configKey() == null) {
            return ROAM_ON_ANY;
        }
        return config.configKey().equals(this.mLastSelectedConfiguration);
    }

    List<WifiConfiguration> getRecentSavedNetworks(int scanResultAgeMs, boolean copy) {
        List<WifiConfiguration> networks = new ArrayList();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
            boolean dbgTmp = isLastSelectedConfigNetwork(config);
            if (!config.ephemeral) {
                ScanDetailCache cache = getScanDetailCache(config);
                if (cache != null) {
                    config.setVisibility(cache.getVisibility((long) scanResultAgeMs));
                    if (config.visibility == null) {
                        if (dbgTmp) {
                            log("visibility is null. " + this.mLastSelectedConfiguration + "," + scanResultAgeMs);
                        }
                    } else if (config.visibility.rssi5 == WifiConfiguration.INVALID_RSSI && config.visibility.rssi24 == WifiConfiguration.INVALID_RSSI) {
                        if (dbgTmp) {
                            log("rssi5&rssi24 is invaild. " + this.mLastSelectedConfiguration);
                        }
                    } else if (copy) {
                        networks.add(new WifiConfiguration(config));
                    } else {
                        networks.add(config);
                    }
                } else if (dbgTmp) {
                    log("cache is null. " + this.mLastSelectedConfiguration);
                }
            } else if (dbgTmp) {
                log("autoJoinStatus is AUTO_JOIN_DELETED or ephemeral is true. " + this.mLastSelectedConfiguration);
            }
        }
        return networks;
    }

    void updateConfiguration(WifiInfo info) {
        WifiConfiguration config = getWifiConfiguration(info.getNetworkId());
        if (config != null && getScanDetailCache(config) != null) {
            ScanDetail scanDetail = getScanDetailCache(config).getScanDetail(info.getBSSID());
            if (scanDetail != null) {
                ScanResult result = scanDetail.getScanResult();
                long previousSeen = result.seen;
                int previousRssi = result.level;
                scanDetail.setSeen();
                result.level = info.getRssi();
                result.averageRssi(previousRssi, previousSeen, WifiQualifiedNetworkSelector.SCAN_RESULT_MAXIMUNM_AGE);
                if (HWLOGW_E) {
                    loge("updateConfiguration freq=" + result.frequency + " BSSID=" + result.BSSID + " RSSI=" + result.level + " " + config.configKey());
                }
            }
        }
    }

    public WifiConfiguration getWifiConfiguration(int netId) {
        return this.mConfiguredNetworks.getForCurrentUser(netId);
    }

    public WifiConfiguration getWifiConfiguration(String key) {
        return this.mConfiguredNetworks.getByConfigKeyForCurrentUser(key);
    }

    void enableAllNetworks() {
        boolean networkEnabledStateChanged = ROAM_ON_ANY;
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
            if (!(config == null || config.ephemeral || config.getNetworkSelectionStatus().isNetworkEnabled() || !tryEnableQualifiedNetwork(config))) {
                networkEnabledStateChanged = DBG;
            }
        }
        if (networkEnabledStateChanged) {
            saveConfig();
            sendConfiguredNetworksChangedBroadcast();
        }
    }

    private boolean setNetworkPriorityNative(WifiConfiguration config, int priority) {
        return this.mWifiConfigStore.setNetworkPriority(config, priority);
    }

    private boolean setSSIDNative(WifiConfiguration config, String ssid) {
        return this.mWifiConfigStore.setNetworkSSID(config, ssid);
    }

    public boolean updateLastConnectUid(WifiConfiguration config, int uid) {
        if (config == null || config.lastConnectUid == uid) {
            return ROAM_ON_ANY;
        }
        config.lastConnectUid = uid;
        return DBG;
    }

    boolean selectNetwork(WifiConfiguration config, boolean updatePriorities, int uid) {
        if (sVDBG) {
            localLogNetwork("selectNetwork", config.networkId);
        }
        if (config.networkId == -1) {
            return ROAM_ON_ANY;
        }
        if (WifiConfigurationUtil.isVisibleToAnyProfile(config, this.mUserManager.getProfiles(this.mCurrentUserId))) {
            if (this.mLastPriority == -1 || this.mLastPriority > 1000000) {
                if (HWLOGW_E) {
                    loge("Need to reset the priority, mLastPriority:" + this.mLastPriority);
                }
                if (updatePriorities) {
                    for (WifiConfiguration config2 : this.mConfiguredNetworks.valuesForCurrentUser()) {
                        if (config2.networkId != -1) {
                            setNetworkPriorityNative(config2, 0);
                        }
                    }
                }
                this.mLastPriority = 0;
            }
            if (updatePriorities) {
                int i = this.mLastPriority + 1;
                this.mLastPriority = i;
                setNetworkPriorityNative(config, i);
            }
            if (config.isPasspoint()) {
                if (getScanDetailCache(config).size() != 0) {
                    ScanDetail result = getScanDetailCache(config).getFirst();
                    if (result == null) {
                        loge("Could not find scan result for " + config.BSSID);
                    } else {
                        logd("Setting SSID for " + config.networkId + " to" + result.getSSID());
                        setSSIDNative(config, result.getSSID());
                    }
                } else {
                    loge("Could not find bssid for " + config);
                }
            }
            this.mWifiConfigStore.enableHS20(config.isPasspoint());
            if (updatePriorities) {
                saveConfig();
            }
            updateLastConnectUid(config, uid);
            writeKnownNetworkHistory();
            selectNetworkWithoutBroadcast(config.networkId);
            return DBG;
        }
        loge("selectNetwork " + Integer.toString(config.networkId) + ": Network config is not " + "visible to current user.");
        return ROAM_ON_ANY;
    }

    NetworkUpdateResult saveNetwork(WifiConfiguration config, int uid) {
        int i = 0;
        if (config == null || (config.networkId == -1 && config.SSID == null)) {
            return new NetworkUpdateResult(-1);
        }
        if (!WifiConfigurationUtil.isVisibleToAnyProfile(config, this.mUserManager.getProfiles(this.mCurrentUserId))) {
            return new NetworkUpdateResult(-1);
        }
        if (sVDBG) {
            localLogNetwork("WifiConfigManager: saveNetwork netId", config.networkId);
        }
        if (sVDBG) {
            logd("WifiConfigManager saveNetwork, size=" + Integer.toString(this.mConfiguredNetworks.sizeForAllUsers()) + " (for all users)" + " SSID=" + config.SSID + " Uid=" + Integer.toString(config.creatorUid) + "/" + Integer.toString(config.lastUpdateUid));
        }
        if (this.mDeletedEphemeralSSIDs.remove(config.SSID) && HWLOGW_E) {
            loge("WifiConfigManager: removed from ephemeral blacklist: " + config.SSID);
        }
        if (config.networkId == -1) {
        }
        NetworkUpdateResult result = addOrUpdateNetworkNative(config, uid);
        int netId = result.getNetworkId();
        if (sVDBG) {
            localLogNetwork("WifiConfigManager: saveNetwork got it back netId=", netId);
        }
        WifiConfiguration conf = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (conf != null) {
            if (!conf.getNetworkSelectionStatus().isNetworkEnabled()) {
                if (sVDBG) {
                    localLog("WifiConfigManager: re-enabling: " + conf.SSID);
                }
                updateNetworkSelectionStatus(netId, 0);
            }
            if (HWLOGW_E) {
                loge("WifiConfigManager: saveNetwork got config back netId=" + Integer.toString(netId) + " uid=" + Integer.toString(config.creatorUid));
            }
        }
        saveConfig();
        if (!result.isNewNetwork()) {
            i = 2;
        }
        sendConfiguredNetworksChangedBroadcast(conf, i);
        return result;
    }

    void noteRoamingFailure(WifiConfiguration config, int reason) {
        if (config != null) {
            config.lastRoamingFailure = this.mClock.currentTimeMillis();
            config.roamingFailureBlackListTimeMilli = (config.roamingFailureBlackListTimeMilli + 1000) * 2;
            if (config.roamingFailureBlackListTimeMilli > ((long) this.mNetworkSwitchingBlackListPeriodMs)) {
                config.roamingFailureBlackListTimeMilli = (long) this.mNetworkSwitchingBlackListPeriodMs;
            }
            config.lastRoamingFailureReason = reason;
        }
    }

    void saveWifiConfigBSSID(WifiConfiguration config, String bssid) {
        this.mWifiConfigStore.setNetworkBSSID(config, bssid);
    }

    void updateStatus(int netId, DetailedState state) {
        if (netId != -1) {
            WifiConfiguration config = this.mConfiguredNetworks.getForAllUsers(netId);
            if (config != null) {
                switch (-getandroid-net-NetworkInfo$DetailedStateSwitchesValues()[state.ordinal()]) {
                    case Extension.TYPE_DOUBLE /*1*/:
                        config.status = 0;
                        updateNetworkSelectionStatus(netId, 0);
                        break;
                    case Extension.TYPE_FLOAT /*2*/:
                        if (config.status == 0) {
                            config.status = 2;
                            break;
                        }
                        break;
                }
            }
        }
    }

    WifiConfiguration disableEphemeralNetwork(String ssid) {
        if (ssid == null) {
            return null;
        }
        WifiConfiguration foundConfig = this.mConfiguredNetworks.getEphemeralForCurrentUser(ssid);
        this.mDeletedEphemeralSSIDs.add(ssid);
        logd("Forget ephemeral SSID " + ssid + " num=" + this.mDeletedEphemeralSSIDs.size());
        if (foundConfig != null) {
            logd("Found ephemeral config in disableEphemeralNetwork: " + foundConfig.networkId);
        }
        writeKnownNetworkHistory();
        return foundConfig;
    }

    boolean forgetNetwork(int netId) {
        if (this.mShowNetworks) {
            localLogNetwork("forgetNetwork", netId);
        }
        if (removeNetwork(netId)) {
            saveConfig();
            writeKnownNetworkHistory();
            return DBG;
        }
        loge("Failed to forget network " + netId);
        return ROAM_ON_ANY;
    }

    int addOrUpdateNetwork(WifiConfiguration config, int uid) {
        if (config == null || !WifiConfigurationUtil.isVisibleToAnyProfile(config, this.mUserManager.getProfiles(this.mCurrentUserId))) {
            return -1;
        }
        if (this.mShowNetworks) {
            localLogNetwork("addOrUpdateNetwork id=", config.networkId);
        }
        if (config.isPasspoint()) {
            config.SSID = getChecksum(config.FQDN).toString();
            config.enterpriseConfig.setDomainSuffixMatch(config.FQDN);
        }
        NetworkUpdateResult result = addOrUpdateNetworkNative(config, uid);
        if (result.getNetworkId() != -1) {
            WifiConfiguration conf = this.mConfiguredNetworks.getForCurrentUser(result.getNetworkId());
            if (conf != null) {
                int i;
                if (result.isNewNetwork) {
                    i = 0;
                } else {
                    i = 2;
                }
                sendConfiguredNetworksChangedBroadcast(conf, i);
            }
        }
        return result.getNetworkId();
    }

    public int addPasspointManagementObject(String managementObject) {
        try {
            this.mMOManager.addSP(managementObject);
            return 0;
        } catch (IOException e) {
            return -1;
        }
    }

    public int modifyPasspointMo(String fqdn, List<PasspointManagementObjectDefinition> mos) {
        try {
            return this.mMOManager.modifySP(fqdn, mos);
        } catch (IOException e) {
            return -1;
        }
    }

    public boolean queryPasspointIcon(long bssid, String fileName) {
        return this.mSupplicantBridge.doIconQuery(bssid, fileName);
    }

    public int matchProviderWithCurrentNetwork(String fqdn) {
        synchronized (this.mActiveScanDetailLock) {
            ScanDetail scanDetail = this.mActiveScanDetail;
        }
        if (scanDetail == null) {
            return PasspointMatch.None.ordinal();
        }
        HomeSP homeSP = this.mMOManager.getHomeSP(fqdn);
        if (homeSP == null) {
            return PasspointMatch.None.ordinal();
        }
        ANQPData anqpData = this.mAnqpCache.getEntry(scanDetail.getNetworkDetail());
        return homeSP.match(scanDetail.getNetworkDetail(), anqpData != null ? anqpData.getANQPElements() : null, this.mSIMAccessor).ordinal();
    }

    public ArrayList<PnoNetwork> retrieveDisconnectedPnoNetworkList() {
        return retrievePnoNetworkList(sDisconnectedPnoListComparator);
    }

    public ArrayList<PnoNetwork> retrieveConnectedPnoNetworkList() {
        return retrievePnoNetworkList(sConnectedPnoListComparator);
    }

    private static PnoNetwork createPnoNetworkFromWifiConfiguration(WifiConfiguration config, int newPriority) {
        PnoNetwork pnoNetwork = new PnoNetwork(config.SSID);
        pnoNetwork.networkId = config.networkId;
        pnoNetwork.priority = newPriority;
        if (config.hiddenSSID) {
            pnoNetwork.flags = (byte) (pnoNetwork.flags | 1);
        }
        pnoNetwork.flags = (byte) (pnoNetwork.flags | 2);
        pnoNetwork.flags = (byte) (pnoNetwork.flags | 4);
        if (config.allowedKeyManagement.get(1)) {
            pnoNetwork.authBitField = (byte) (pnoNetwork.authBitField | 2);
        } else if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3)) {
            pnoNetwork.authBitField = (byte) (pnoNetwork.authBitField | 4);
        } else {
            pnoNetwork.authBitField = (byte) (pnoNetwork.authBitField | 1);
        }
        return pnoNetwork;
    }

    private ArrayList<PnoNetwork> retrievePnoNetworkList(PnoListComparator pnoListComparator) {
        ArrayList<PnoNetwork> pnoList = new ArrayList();
        ArrayList<WifiConfiguration> wifiConfigurations = new ArrayList(this.mConfiguredNetworks.valuesForCurrentUser());
        Collections.sort(wifiConfigurations, pnoListComparator);
        int priority = wifiConfigurations.size();
        for (WifiConfiguration config : wifiConfigurations) {
            pnoList.add(createPnoNetworkFromWifiConfiguration(config, priority));
            priority--;
        }
        return pnoList;
    }

    boolean removeNetwork(int netId) {
        if (this.mShowNetworks) {
            localLogNetwork("removeNetwork", netId);
        }
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (!removeConfigAndSendBroadcastIfNeeded(config)) {
            return ROAM_ON_ANY;
        }
        if (config.isPasspoint()) {
            writePasspointConfigs(config.FQDN, null);
        }
        return DBG;
    }

    private static Long getChecksum(String source) {
        Checksum csum = new CRC32();
        csum.update(source.getBytes(), 0, source.getBytes().length);
        return Long.valueOf(csum.getValue());
    }

    private boolean removeConfigWithoutBroadcast(WifiConfiguration config) {
        if (config == null) {
            return ROAM_ON_ANY;
        }
        if (this.mWifiConfigStore.removeNetwork(config)) {
            if (config.configKey().equals(this.mLastSelectedConfiguration)) {
                this.mLastSelectedConfiguration = null;
            }
            this.mConfiguredNetworks.remove(config.networkId);
            this.mScanDetailCaches.remove(Integer.valueOf(config.networkId));
            return DBG;
        }
        loge("Failed to remove network " + config.networkId);
        return ROAM_ON_ANY;
    }

    private boolean removeConfigAndSendBroadcastIfNeeded(WifiConfiguration config) {
        if (!removeConfigWithoutBroadcast(config)) {
            return ROAM_ON_ANY;
        }
        String key = config.configKey();
        if (HWLOGW_E) {
            logd("removeNetwork  key=" + key + " config.id=" + config.networkId);
        }
        writeIpAndProxyConfigurations();
        sendConfiguredNetworksChangedBroadcast(config, 1);
        if (!config.ephemeral) {
            removeUserSelectionPreference(key);
        }
        writeKnownNetworkHistory();
        return DBG;
    }

    private void removeUserSelectionPreference(String configKey) {
        Log.d(TAG, "removeUserSelectionPreference: key is " + configKey);
        if (configKey != null) {
            for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
                NetworkSelectionStatus status = config.getNetworkSelectionStatus();
                String connectChoice = status.getConnectChoice();
                if (connectChoice != null && connectChoice.equals(configKey)) {
                    Log.d(TAG, "remove connect choice:" + connectChoice + " from " + config.SSID + " : " + config.networkId);
                    status.setConnectChoice(null);
                    status.setConnectChoiceTimestamp(-1);
                }
            }
        }
    }

    boolean removeNetworksForApp(ApplicationInfo app) {
        int i = 0;
        if (app == null || app.packageName == null) {
            return ROAM_ON_ANY;
        }
        boolean success = DBG;
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForCurrentUser().toArray(new WifiConfiguration[0]);
        int length = copiedConfigs.length;
        while (i < length) {
            WifiConfiguration config = copiedConfigs[i];
            if (app.uid == config.creatorUid && app.packageName.equals(config.creatorName)) {
                if (this.mShowNetworks) {
                    localLog("Removing network " + config.SSID + ", application \"" + app.packageName + "\" uninstalled" + " from user " + UserHandle.getUserId(app.uid));
                }
                success &= removeNetwork(config.networkId);
            }
            i++;
        }
        saveConfig();
        return success;
    }

    boolean removeNetworksForUser(int userId) {
        int i = 0;
        boolean success = DBG;
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0]);
        int length = copiedConfigs.length;
        while (i < length) {
            WifiConfiguration config = copiedConfigs[i];
            if (userId == UserHandle.getUserId(config.creatorUid)) {
                success &= removeNetwork(config.networkId);
                if (this.mShowNetworks) {
                    localLog("Removing network " + config.SSID + ", user " + userId + " removed");
                }
            }
            i++;
        }
        saveConfig();
        return success;
    }

    boolean enableNetwork(WifiConfiguration config, boolean disableOthers, int uid) {
        if (config == null) {
            return ROAM_ON_ANY;
        }
        updateNetworkSelectionStatus(config, 0);
        setLatestUserSelectedConfiguration(config);
        boolean ret = DBG;
        if (disableOthers) {
            ret = selectNetworkWithoutBroadcast(config.networkId);
            if (sVDBG) {
                localLogNetwork("enableNetwork(disableOthers=true, uid=" + uid + ") ", config.networkId);
            }
            updateLastConnectUid(config, uid);
            writeKnownNetworkHistory();
            sendConfiguredNetworksChangedBroadcast();
        } else {
            if (sVDBG) {
                localLogNetwork("enableNetwork(disableOthers=false) ", config.networkId);
            }
            sendConfiguredNetworksChangedBroadcast(config, 2);
        }
        return ret;
    }

    boolean selectNetworkWithoutBroadcast(int netId) {
        return this.mWifiConfigStore.selectNetwork(this.mConfiguredNetworks.getForCurrentUser(netId), this.mConfiguredNetworks.valuesForCurrentUser());
    }

    boolean disableNetworkNative(WifiConfiguration config) {
        return this.mWifiConfigStore.disableNetwork(config);
    }

    void disableAllNetworksNative() {
        this.mWifiConfigStore.disableAllNetworks(this.mConfiguredNetworks.valuesForCurrentUser());
    }

    boolean disableNetwork(int netId) {
        return this.mWifiConfigStore.disableNetwork(this.mConfiguredNetworks.getForCurrentUser(netId));
    }

    boolean updateNetworkSelectionStatus(int netId, int reason) {
        return updateNetworkSelectionStatus(getWifiConfiguration(netId), reason);
    }

    boolean updateNetworkSelectionStatus(WifiConfiguration config, int reason) {
        if (config == null) {
            return ROAM_ON_ANY;
        }
        NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (reason == 0) {
            updateNetworkStatus(config, 0);
            localLog("Enable network:" + config.configKey());
            return DBG;
        }
        networkStatus.incrementDisableReasonCounter(reason);
        localLog("Network:" + config.SSID + "disable counter of " + NetworkSelectionStatus.getNetworkDisableReasonString(reason) + " is: " + networkStatus.getDisableReasonCounter(reason) + "and threshold is: " + NETWORK_SELECTION_DISABLE_THRESHOLD[reason]);
        if (networkStatus.getDisableReasonCounter(reason) >= NETWORK_SELECTION_DISABLE_THRESHOLD[reason]) {
            return updateNetworkStatus(config, reason);
        }
        return DBG;
    }

    public boolean tryEnableQualifiedNetwork(int networkId) {
        WifiConfiguration config = getWifiConfiguration(networkId);
        if (config != null) {
            return tryEnableQualifiedNetwork(config);
        }
        localLog("updateQualifiedNetworkstatus invalid network.");
        return ROAM_ON_ANY;
    }

    private boolean tryEnableQualifiedNetwork(WifiConfiguration config) {
        NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (networkStatus.isNetworkTemporaryDisabled()) {
            long timeDifference = ((this.mClock.elapsedRealtime() - networkStatus.getDisableTime()) / 1000) / 60;
            if (timeDifference < 0 || timeDifference >= ((long) NETWORK_SELECTION_DISABLE_TIMEOUT[networkStatus.getNetworkSelectionDisableReason()])) {
                updateNetworkSelectionStatus(config.networkId, 0);
                return DBG;
            }
        }
        return ROAM_ON_ANY;
    }

    boolean updateNetworkStatus(WifiConfiguration config, int reason) {
        String str = null;
        StringBuilder append = new StringBuilder().append("updateNetworkStatus:");
        if (config != null) {
            str = config.SSID;
        }
        localLog(append.append(str).toString());
        if (config == null) {
            return ROAM_ON_ANY;
        }
        NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (reason < 0 || reason >= 11) {
            localLog("Invalid Network disable reason:" + reason);
            return ROAM_ON_ANY;
        }
        if (reason == 0) {
            if (networkStatus.isNetworkEnabled()) {
                localLog("Need not change Qualified network Selection status since already enabled");
                return ROAM_ON_ANY;
            }
            networkStatus.setNetworkSelectionStatus(0);
            networkStatus.setNetworkSelectionDisableReason(reason);
            networkStatus.setDisableTime(-1);
            networkStatus.clearDisableReasonCounter();
            localLog("Re-enable network: " + config.SSID + " at " + DateFormat.getDateTimeInstance().format(new Date()));
            sendConfiguredNetworksChangedBroadcast(config, 2);
        } else if (networkStatus.isNetworkPermanentlyDisabled()) {
            localLog("Do nothing. Alreay permanent disabled! " + NetworkSelectionStatus.getNetworkDisableReasonString(reason));
            return ROAM_ON_ANY;
        } else if (!networkStatus.isNetworkTemporaryDisabled() || reason >= 6) {
            if (networkStatus.isNetworkEnabled()) {
                disableNetworkNative(config);
                sendConfiguredNetworksChangedBroadcast(config, 2);
                localLog("Disable network " + config.SSID + " reason:" + NetworkSelectionStatus.getNetworkDisableReasonString(reason));
            }
            if (reason < 6) {
                networkStatus.setNetworkSelectionStatus(1);
                networkStatus.setDisableTime(this.mClock.elapsedRealtime());
                if (2 <= reason && 4 >= reason) {
                    HwWifiCHRStateManager mWiFiCHRManager = HwWifiServiceFactory.getHwWifiCHRStateManager();
                    if (mWiFiCHRManager != null) {
                        log("chr, trigger disable network , reason=" + reason);
                        mWiFiCHRManager.updateWifiException(82, "");
                        mWiFiCHRManager.reportHwCHRAccessNetworkEventInfoList(7);
                    }
                }
            } else if (reason == DEFAULT_MAX_DHCP_RETRIES) {
                networkStatus.setNetworkSelectionStatus(1);
                networkStatus.setDisableTime(this.mClock.elapsedRealtime());
            } else {
                networkStatus.setNetworkSelectionStatus(2);
            }
            networkStatus.setNetworkSelectionDisableReason(reason);
            localLog("Network:" + config.SSID + "Configure new status:" + networkStatus.getNetworkStatusString() + " with reason:" + networkStatus.getNetworkDisableReasonString() + " at: " + DateFormat.getDateTimeInstance().format(new Date()));
        } else {
            localLog("Do nothing. Already temporarily disabled! " + NetworkSelectionStatus.getNetworkDisableReasonString(reason));
            return ROAM_ON_ANY;
        }
        return DBG;
    }

    boolean saveConfig() {
        return this.mWifiConfigStore.saveConfig();
    }

    WpsResult startWpsWithPinFromAccessPoint(WpsInfo config) {
        return this.mWifiConfigStore.startWpsWithPinFromAccessPoint(config, this.mConfiguredNetworks.valuesForCurrentUser());
    }

    WpsResult startWpsWithPinFromDevice(WpsInfo config) {
        return this.mWifiConfigStore.startWpsWithPinFromDevice(config, this.mConfiguredNetworks.valuesForCurrentUser());
    }

    WpsResult startWpsPbc(WpsInfo config) {
        return this.mWifiConfigStore.startWpsPbc(config, this.mConfiguredNetworks.valuesForCurrentUser());
    }

    StaticIpConfiguration getStaticIpConfiguration(int netId) {
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (config != null) {
            return config.getStaticIpConfiguration();
        }
        return null;
    }

    void setStaticIpConfiguration(int netId, StaticIpConfiguration staticIpConfiguration) {
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (config != null) {
            config.setStaticIpConfiguration(staticIpConfiguration);
        }
    }

    void setDefaultGwMacAddress(int netId, String macAddress) {
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (config != null) {
            config.defaultGwMacAddress = macAddress;
        }
    }

    ProxyInfo getProxyProperties(int netId) {
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (config != null) {
            return config.getHttpProxy();
        }
        return null;
    }

    boolean isUsingStaticIp(int netId) {
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (config == null || config.getIpAssignment() != IpAssignment.STATIC) {
            return ROAM_ON_ANY;
        }
        return DBG;
    }

    boolean isEphemeral(int netId) {
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        return config != null ? config.ephemeral : ROAM_ON_ANY;
    }

    boolean getMeteredHint(int netId) {
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        return config != null ? config.meteredHint : ROAM_ON_ANY;
    }

    private void sendConfiguredNetworksChangedBroadcast(WifiConfiguration network, int reason) {
        Intent intent = new Intent("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        intent.addFlags(67108864);
        intent.putExtra("multipleChanges", ROAM_ON_ANY);
        intent.putExtra("wifiConfiguration", network);
        intent.putExtra("changeReason", reason);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void sendConfiguredNetworksChangedBroadcast() {
        Intent intent = new Intent("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        intent.addFlags(67108864);
        intent.putExtra("multipleChanges", DBG);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    void loadConfiguredNetworks() {
        Map<String, WifiConfiguration> configs = new HashMap();
        SparseArray<Map<String, String>> networkExtras = new SparseArray();
        this.mLastPriority = this.mWifiConfigStore.loadNetworks(configs, networkExtras);
        readNetworkHistory(configs);
        readPasspointConfig(configs, networkExtras);
        this.mConfiguredNetworks.clear();
        if (this.mScanDetailCaches != null) {
            this.mScanDetailCaches.clear();
        }
        for (Entry<String, WifiConfiguration> entry : configs.entrySet()) {
            String configKey = (String) entry.getKey();
            WifiConfiguration config = (WifiConfiguration) entry.getValue();
            if (configKey.equals(config.configKey())) {
                this.mConfiguredNetworks.put(config);
            } else {
                log("Ignoring network " + config.networkId + " because the configKey loaded " + "from wpa_supplicant.conf is not valid." + " configKey in file is " + configKey + ", currentConfigKey is " + config.configKey());
                this.mWifiConfigStore.removeNetwork(config);
            }
        }
        readIpAndProxyConfigurations();
        sendConfiguredNetworksChangedBroadcast();
        if (this.mShowNetworks) {
            localLog("loadConfiguredNetworks loaded " + this.mConfiguredNetworks.sizeForAllUsers() + " networks (for all users)");
        }
        if (this.mConfiguredNetworks.sizeForAllUsers() == 0) {
            logKernelTime();
            logContents(WifiConfigStore.SUPPLICANT_CONFIG_FILE);
            logContents(WifiConfigStore.SUPPLICANT_CONFIG_FILE_BACKUP);
            logContents(WifiNetworkHistory.NETWORK_HISTORY_CONFIG_FILE);
        }
    }

    private void logContents(String file) {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        localLogAndLogcat("--- Begin " + file + " ---");
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            try {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    if (line.indexOf("serial_number") == -1) {
                        localLogAndLogcat(line);
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e3) {
                    }
                }
                bufferedReader = reader;
            } catch (FileNotFoundException e4) {
                e = e4;
                bufferedReader = reader;
                localLog("Could not open " + file + ", " + e);
                Log.w(TAG, "Could not open " + file + ", " + e);
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e5) {
                    }
                }
                localLogAndLogcat("--- End " + file + " Contents ---");
            } catch (IOException e6) {
                e2 = e6;
                bufferedReader = reader;
                try {
                    localLog("Could not read " + file + ", " + e2);
                    Log.w(TAG, "Could not read " + file + ", " + e2);
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e7) {
                        }
                    }
                    localLogAndLogcat("--- End " + file + " Contents ---");
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e8) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedReader = reader;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            e = e9;
            localLog("Could not open " + file + ", " + e);
            Log.w(TAG, "Could not open " + file + ", " + e);
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            localLogAndLogcat("--- End " + file + " Contents ---");
        } catch (IOException e10) {
            e2 = e10;
            localLog("Could not read " + file + ", " + e2);
            Log.w(TAG, "Could not read " + file + ", " + e2);
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            localLogAndLogcat("--- End " + file + " Contents ---");
        }
        localLogAndLogcat("--- End " + file + " Contents ---");
    }

    private Map<String, String> readNetworkVariablesFromSupplicantFile(String key) {
        return this.mWifiConfigStore.readNetworkVariablesFromSupplicantFile(key);
    }

    private String readNetworkVariableFromSupplicantFile(String configKey, String key) {
        long start = SystemClock.elapsedRealtimeNanos();
        Map<String, String> data = this.mWifiConfigStore.readNetworkVariablesFromSupplicantFile(key);
        long end = SystemClock.elapsedRealtimeNanos();
        if (sVDBG) {
            localLog("readNetworkVariableFromSupplicantFile configKey=[" + configKey + "] key=" + key + " duration=" + (end - start));
        }
        return (String) data.get(configKey);
    }

    boolean needsUnlockedKeyStore() {
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
            if (config.allowedKeyManagement.get(2) && config.allowedKeyManagement.get(3) && needsSoftwareBackedKeyStore(config.enterpriseConfig)) {
                return DBG;
            }
        }
        return ROAM_ON_ANY;
    }

    void readPasspointConfig(Map<String, WifiConfiguration> configs, SparseArray<Map<String, String>> networkExtras) {
        try {
            int matchedConfigs = 0;
            for (HomeSP homeSp : this.mMOManager.loadAllSPs()) {
                String fqdn = homeSp.getFQDN();
                Log.d(TAG, "Looking for " + fqdn);
                for (WifiConfiguration config : configs.values()) {
                    Log.d(TAG, "Testing " + config.SSID);
                    if (config.enterpriseConfig != null) {
                        String configFqdn = (String) ((Map) networkExtras.get(config.networkId)).get(WifiConfigStore.ID_STRING_KEY_FQDN);
                        if (configFqdn != null && configFqdn.equals(fqdn)) {
                            Log.d(TAG, "Matched " + configFqdn + " with " + config.networkId);
                            matchedConfigs++;
                            config.FQDN = fqdn;
                            config.providerFriendlyName = homeSp.getFriendlyName();
                            HashSet<Long> roamingConsortiumIds = homeSp.getRoamingConsortiums();
                            config.roamingConsortiumIds = new long[roamingConsortiumIds.size()];
                            int i = 0;
                            for (Long longValue : roamingConsortiumIds) {
                                config.roamingConsortiumIds[i] = longValue.longValue();
                                i++;
                            }
                            IMSIParameter imsiParameter = homeSp.getCredential().getImsi();
                            config.enterpriseConfig.setPlmn(imsiParameter != null ? imsiParameter.toString() : null);
                            config.enterpriseConfig.setRealm(homeSp.getCredential().getRealm());
                        }
                    }
                }
            }
            Log.d(TAG, "loaded " + matchedConfigs + " passpoint configs");
        } catch (IOException e) {
            loge("Could not read /data/misc/wifi/PerProviderSubscription.conf : " + e);
        }
    }

    public void writePasspointConfigs(String fqdn, HomeSP homeSP) {
        this.mWriter.write(PPS_FILE, new AnonymousClass3(homeSP, fqdn), ROAM_ON_ANY);
    }

    private void readNetworkHistory(Map<String, WifiConfiguration> configs) {
        this.mWifiNetworkHistory.readNetworkHistory(configs, this.mScanDetailCaches, this.mDeletedEphemeralSSIDs);
    }

    public void writeKnownNetworkHistory() {
        List<WifiConfiguration> networks = new ArrayList();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForAllUsers()) {
            networks.add(new WifiConfiguration(config));
        }
        this.mWifiNetworkHistory.writeKnownNetworkHistory(networks, this.mScanDetailCaches, this.mDeletedEphemeralSSIDs);
    }

    public void setAndEnableLastSelectedConfiguration(int netId) {
        if (sVDBG) {
            logd("setLastSelectedConfiguration " + Integer.toString(netId));
        }
        if (netId == -1) {
            this.mLastSelectedConfiguration = null;
            this.mLastSelectedTimeStamp = -1;
            return;
        }
        WifiConfiguration selected = getWifiConfiguration(netId);
        if (selected == null) {
            this.mLastSelectedConfiguration = null;
            this.mLastSelectedTimeStamp = -1;
            return;
        }
        this.mLastSelectedConfiguration = selected.configKey();
        this.mLastSelectedTimeStamp = this.mClock.elapsedRealtime();
        updateNetworkSelectionStatus(netId, 0);
        if (sVDBG) {
            logd("setLastSelectedConfiguration now: " + this.mLastSelectedConfiguration);
        }
    }

    public void setLatestUserSelectedConfiguration(WifiConfiguration network) {
        if (network != null) {
            this.mLastSelectedConfiguration = network.configKey();
            this.mLastSelectedTimeStamp = this.mClock.elapsedRealtime();
        }
    }

    public String getLastSelectedConfiguration() {
        return this.mLastSelectedConfiguration;
    }

    public long getLastSelectedTimeStamp() {
        return this.mLastSelectedTimeStamp;
    }

    public boolean isLastSelectedConfiguration(WifiConfiguration config) {
        if (this.mLastSelectedConfiguration == null || config == null) {
            return ROAM_ON_ANY;
        }
        return this.mLastSelectedConfiguration.equals(config.configKey());
    }

    private void writeIpAndProxyConfigurations() {
        SparseArray<IpConfiguration> networks = new SparseArray();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForAllUsers()) {
            if (!config.ephemeral) {
                networks.put(configKey(config), config.getIpConfiguration());
            }
        }
        this.mIpconfigStore.writeIpAndProxyConfigurations(IP_CONFIG_FILE, networks);
    }

    private void readIpAndProxyConfigurations() {
        SparseArray<IpConfiguration> networks = this.mIpconfigStore.readIpAndProxyConfigurations(IP_CONFIG_FILE);
        if (networks != null && networks.size() != 0) {
            for (int i = 0; i < networks.size(); i++) {
                int id = networks.keyAt(i);
                WifiConfiguration config = this.mConfiguredNetworks.getByConfigKeyIDForAllUsers(id);
                if (config == null || config.ephemeral) {
                    logd("configuration found for missing network, nid=" + id + ", ignored, networks.size=" + Integer.toString(networks.size()));
                } else {
                    config.setIpConfiguration((IpConfiguration) networks.valueAt(i));
                }
            }
        }
    }

    private NetworkUpdateResult addOrUpdateNetworkNative(WifiConfiguration config, int uid) {
        IOException ioe;
        if (sVDBG) {
            localLog("addOrUpdateNetworkNative " + config.getPrintableSsid());
        }
        if (!config.isPasspoint() || this.mMOManager.isEnabled()) {
            WifiConfiguration currentConfig;
            boolean newNetwork = ROAM_ON_ANY;
            boolean existingMO = ROAM_ON_ANY;
            if (config.networkId == -1) {
                config.defendSsid();
                currentConfig = this.mConfiguredNetworks.getByConfigKeyForCurrentUser(config.configKey());
                if (currentConfig != null) {
                    config.networkId = currentConfig.networkId;
                } else {
                    if (this.mMOManager.getHomeSP(config.FQDN) != null) {
                        logd("addOrUpdateNetworkNative passpoint " + config.FQDN + " was found, but no network Id");
                        existingMO = DBG;
                    }
                    newNetwork = DBG;
                }
            } else {
                currentConfig = this.mConfiguredNetworks.getForCurrentUser(config.networkId);
            }
            WifiConfiguration wifiConfiguration = new WifiConfiguration(currentConfig);
            if (!this.mWifiConfigStore.addOrUpdateNetwork(config, currentConfig)) {
                return new NetworkUpdateResult(-1);
            }
            HomeSP homeSP;
            int netId = config.networkId;
            String savedConfigKey = config.configKey();
            if (currentConfig == null) {
                currentConfig = new WifiConfiguration();
                currentConfig.setIpAssignment(IpAssignment.DHCP);
                currentConfig.setProxySettings(ProxySettings.NONE);
                currentConfig.networkId = netId;
                if (config != null) {
                    currentConfig.selfAdded = config.selfAdded;
                    currentConfig.didSelfAdd = config.didSelfAdd;
                    currentConfig.ephemeral = config.ephemeral;
                    currentConfig.meteredHint = config.meteredHint;
                    currentConfig.useExternalScores = config.useExternalScores;
                    currentConfig.lastConnectUid = config.lastConnectUid;
                    currentConfig.lastUpdateUid = config.lastUpdateUid;
                    currentConfig.creatorUid = config.creatorUid;
                    currentConfig.creatorName = config.creatorName;
                    currentConfig.lastUpdateName = config.lastUpdateName;
                    currentConfig.peerWifiConfiguration = config.peerWifiConfiguration;
                    currentConfig.FQDN = config.FQDN;
                    currentConfig.providerFriendlyName = config.providerFriendlyName;
                    currentConfig.roamingConsortiumIds = config.roamingConsortiumIds;
                    currentConfig.validatedInternetAccess = config.validatedInternetAccess;
                    currentConfig.numNoInternetAccessReports = config.numNoInternetAccessReports;
                    currentConfig.updateTime = config.updateTime;
                    currentConfig.creationTime = config.creationTime;
                    currentConfig.shared = config.shared;
                    currentConfig.isTempCreated = config.isTempCreated;
                    currentConfig.cloudSecurityCheck = config.cloudSecurityCheck;
                }
                log("created new config netId=" + Integer.toString(netId) + " uid=" + Integer.toString(currentConfig.creatorUid) + " name=" + currentConfig.creatorName);
            }
            if (existingMO) {
                homeSP = null;
            } else if (config.isPasspoint()) {
                try {
                    if (config.updateIdentifier == null) {
                        Credential credential = new Credential(config.enterpriseConfig, this.mKeyStore, newNetwork ? ROAM_ON_ANY : DBG);
                        HashSet<Long> roamingConsortiumIds = new HashSet();
                        for (long valueOf : config.roamingConsortiumIds) {
                            roamingConsortiumIds.add(Long.valueOf(valueOf));
                        }
                        homeSP = new HomeSP(Collections.emptyMap(), config.FQDN, roamingConsortiumIds, Collections.emptySet(), Collections.emptySet(), Collections.emptyList(), config.providerFriendlyName, null, credential);
                        try {
                            log("created a homeSP object for " + config.networkId + ":" + config.SSID);
                        } catch (IOException e) {
                            ioe = e;
                            Log.e(TAG, "Failed to create Passpoint config: " + ioe);
                            return new NetworkUpdateResult(-1);
                        }
                    }
                    homeSP = null;
                    currentConfig.enterpriseConfig.setRealm(config.enterpriseConfig.getRealm());
                    currentConfig.enterpriseConfig.setPlmn(config.enterpriseConfig.getPlmn());
                } catch (IOException e2) {
                    ioe = e2;
                    homeSP = null;
                    Log.e(TAG, "Failed to create Passpoint config: " + ioe);
                    return new NetworkUpdateResult(-1);
                }
            } else {
                homeSP = null;
            }
            if (uid != -1) {
                if (newNetwork) {
                    currentConfig.creatorUid = uid;
                } else {
                    currentConfig.lastUpdateUid = uid;
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("time=");
            Calendar.getInstance().setTimeInMillis(this.mClock.currentTimeMillis());
            sb.append(String.format("%tm-%td %tH:%tM:%tS.%tL", new Object[]{c, c, c, c, c, c}));
            if (newNetwork) {
                currentConfig.creationTime = sb.toString();
            } else {
                currentConfig.updateTime = sb.toString();
            }
            if (currentConfig.status == 2) {
                updateNetworkSelectionStatus(currentConfig.networkId, 0);
            }
            if (currentConfig.configKey().equals(getLastSelectedConfiguration()) && currentConfig.ephemeral) {
                currentConfig.ephemeral = ROAM_ON_ANY;
                log("remove ephemeral status netId=" + Integer.toString(netId) + " " + currentConfig.configKey());
            }
            if (sVDBG) {
                log("will read network variables netId=" + Integer.toString(netId));
            }
            readNetworkVariables(currentConfig);
            if (savedConfigKey.equals(currentConfig.configKey()) || this.mWifiConfigStore.saveNetworkMetadata(currentConfig)) {
                boolean passwordChanged = ROAM_ON_ANY;
                if (!(newNetwork || config.preSharedKey == null || config.preSharedKey.equals("*"))) {
                    passwordChanged = DBG;
                }
                if (newNetwork || passwordChanged || wasCredentialChange(wifiConfiguration, currentConfig)) {
                    currentConfig.getNetworkSelectionStatus().setHasEverConnected(ROAM_ON_ANY);
                }
                if (config.lastUpdateName != null) {
                    currentConfig.lastUpdateName = config.lastUpdateName;
                }
                if (config.lastUpdateUid != -1) {
                    currentConfig.lastUpdateUid = config.lastUpdateUid;
                }
                this.mConfiguredNetworks.put(currentConfig);
                NetworkUpdateResult result = writeIpAndProxyConfigurationsOnChange(currentConfig, config, newNetwork);
                result.setIsNewNetwork(newNetwork);
                result.setNetworkId(netId);
                if (homeSP != null) {
                    writePasspointConfigs(null, homeSP);
                }
                saveConfig();
                writeKnownNetworkHistory();
                return result;
            }
            loge("Failed to set network metadata. Removing config " + config.networkId);
            this.mWifiConfigStore.removeNetwork(config);
            return new NetworkUpdateResult(-1);
        }
        Log.e(TAG, "Passpoint is not enabled");
        return new NetworkUpdateResult(-1);
    }

    private boolean wasBitSetUpdated(BitSet originalBitSet, BitSet currentBitSet) {
        if (originalBitSet == null || currentBitSet == null) {
            if (!(originalBitSet == null && currentBitSet == null)) {
                return DBG;
            }
        } else if (!originalBitSet.equals(currentBitSet)) {
            return DBG;
        }
        return ROAM_ON_ANY;
    }

    private boolean wasCredentialChange(WifiConfiguration originalConfig, WifiConfiguration currentConfig) {
        if (originalConfig == null || wasBitSetUpdated(originalConfig.allowedKeyManagement, currentConfig.allowedKeyManagement) || wasBitSetUpdated(originalConfig.allowedProtocols, currentConfig.allowedProtocols) || wasBitSetUpdated(originalConfig.allowedAuthAlgorithms, currentConfig.allowedAuthAlgorithms) || wasBitSetUpdated(originalConfig.allowedPairwiseCiphers, currentConfig.allowedPairwiseCiphers) || wasBitSetUpdated(originalConfig.allowedGroupCiphers, currentConfig.allowedGroupCiphers)) {
            return DBG;
        }
        if (!(originalConfig.wepKeys == null || currentConfig.wepKeys == null)) {
            if (originalConfig.wepKeys.length != currentConfig.wepKeys.length) {
                return DBG;
            }
            for (int i = 0; i < originalConfig.wepKeys.length; i++) {
                if (!Objects.equals(originalConfig.wepKeys[i], currentConfig.wepKeys[i])) {
                    return DBG;
                }
            }
        }
        if (originalConfig.hiddenSSID == currentConfig.hiddenSSID && originalConfig.requirePMF == currentConfig.requirePMF && !wasEnterpriseConfigChange(originalConfig.enterpriseConfig, currentConfig.enterpriseConfig)) {
            return ROAM_ON_ANY;
        }
        return DBG;
    }

    protected boolean wasEnterpriseConfigChange(WifiEnterpriseConfig originalEnterpriseConfig, WifiEnterpriseConfig currentEnterpriseConfig) {
        if (originalEnterpriseConfig == null || currentEnterpriseConfig == null) {
            if (!(originalEnterpriseConfig == null && currentEnterpriseConfig == null)) {
                return DBG;
            }
        } else if (originalEnterpriseConfig.getEapMethod() != currentEnterpriseConfig.getEapMethod() || originalEnterpriseConfig.getPhase2Method() != currentEnterpriseConfig.getPhase2Method()) {
            return DBG;
        } else {
            X509Certificate[] originalCaCerts = originalEnterpriseConfig.getCaCertificates();
            X509Certificate[] currentCaCerts = currentEnterpriseConfig.getCaCertificates();
            if (originalCaCerts == null || currentCaCerts == null) {
                if (!(originalCaCerts == null && currentCaCerts == null)) {
                    return DBG;
                }
            } else if (originalCaCerts.length != currentCaCerts.length) {
                return DBG;
            } else {
                for (int i = 0; i < originalCaCerts.length; i++) {
                    if (!originalCaCerts[i].equals(currentCaCerts[i])) {
                        return DBG;
                    }
                }
            }
        }
        return ROAM_ON_ANY;
    }

    public WifiConfiguration getWifiConfigForHomeSP(HomeSP homeSP) {
        WifiConfiguration config = this.mConfiguredNetworks.getByFQDNForCurrentUser(homeSP.getFQDN());
        if (config == null) {
            Log.e(TAG, "Could not find network for homeSP " + homeSP.getFQDN());
        }
        return config;
    }

    public HomeSP getHomeSPForConfig(WifiConfiguration config) {
        WifiConfiguration storedConfig = this.mConfiguredNetworks.getForCurrentUser(config.networkId);
        if (storedConfig == null || !storedConfig.isPasspoint()) {
            return null;
        }
        return this.mMOManager.getHomeSP(storedConfig.FQDN);
    }

    public ScanDetailCache getScanDetailCache(WifiConfiguration config) {
        if (config == null) {
            return null;
        }
        ScanDetailCache cache = (ScanDetailCache) this.mScanDetailCaches.get(Integer.valueOf(config.networkId));
        if (cache == null && config.networkId != -1) {
            cache = new ScanDetailCache(config);
            this.mScanDetailCaches.put(Integer.valueOf(config.networkId), cache);
        }
        return cache;
    }

    public void linkConfiguration(WifiConfiguration config) {
        if (!WifiConfigurationUtil.isVisibleToAnyProfile(config, this.mUserManager.getProfiles(this.mCurrentUserId))) {
            logd("linkConfiguration: Attempting to link config " + config.configKey() + " that is not visible to the current user.");
        } else if ((getScanDetailCache(config) == null || getScanDetailCache(config).size() <= 6) && config.allowedKeyManagement.get(1)) {
            for (WifiConfiguration link : this.mConfiguredNetworks.valuesForCurrentUser()) {
                boolean doLink = ROAM_ON_ANY;
                if (!(link.configKey().equals(config.configKey()) || link.ephemeral || !link.allowedKeyManagement.equals(config.allowedKeyManagement))) {
                    ScanDetailCache linkedScanDetailCache = getScanDetailCache(link);
                    if (linkedScanDetailCache == null || linkedScanDetailCache.size() <= 6) {
                        if (config.defaultGwMacAddress == null || link.defaultGwMacAddress == null) {
                            if (getScanDetailCache(config) != null && getScanDetailCache(config).size() <= 6) {
                                for (String abssid : getScanDetailCache(config).keySet()) {
                                    for (String bbssid : linkedScanDetailCache.keySet()) {
                                        if (sVVDBG) {
                                            logd("linkConfiguration try to link due to DBDC BSSID match " + link.SSID + " and " + config.SSID + " bssida " + abssid + " bssidb " + bbssid);
                                        }
                                        if (abssid.regionMatches(DBG, 0, bbssid, 0, MAX_RX_PACKET_FOR_FULL_SCANS)) {
                                            doLink = DBG;
                                        }
                                    }
                                }
                            }
                        } else if (config.defaultGwMacAddress.equals(link.defaultGwMacAddress)) {
                            if (sVDBG) {
                                logd("linkConfiguration link due to same gw " + link.SSID + " and " + config.SSID + " GW " + config.defaultGwMacAddress);
                            }
                            doLink = DBG;
                        }
                        if (doLink && this.mOnlyLinkSameCredentialConfigurations) {
                            Map<String, String> data = getCredentialsByConfigKeyMap();
                            String apsk = (String) data.get(link.SSID);
                            String bpsk = (String) data.get(config.SSID);
                            if (apsk == null || bpsk == null || TextUtils.isEmpty(apsk) || TextUtils.isEmpty(apsk) || apsk.equals("*") || apsk.equals(DELETED_CONFIG_PSK) || !apsk.equals(bpsk)) {
                                doLink = ROAM_ON_ANY;
                            }
                        }
                        if (doLink) {
                            if (sVDBG) {
                                logd("linkConfiguration: will link " + link.configKey() + " and " + config.configKey());
                            }
                            if (link.linkedConfigurations == null) {
                                link.linkedConfigurations = new HashMap();
                            }
                            if (config.linkedConfigurations == null) {
                                config.linkedConfigurations = new HashMap();
                            }
                            if (link.linkedConfigurations.get(config.configKey()) == null) {
                                link.linkedConfigurations.put(config.configKey(), Integer.valueOf(1));
                            }
                            if (config.linkedConfigurations.get(link.configKey()) == null) {
                                config.linkedConfigurations.put(link.configKey(), Integer.valueOf(1));
                            }
                        } else {
                            if (!(link.linkedConfigurations == null || link.linkedConfigurations.get(config.configKey()) == null)) {
                                if (sVDBG) {
                                    logd("linkConfiguration: un-link " + config.configKey() + " from " + link.configKey());
                                }
                                link.linkedConfigurations.remove(config.configKey());
                            }
                            if (!(config.linkedConfigurations == null || config.linkedConfigurations.get(link.configKey()) == null)) {
                                if (sVDBG) {
                                    logd("linkConfiguration: un-link " + link.configKey() + " from " + config.configKey());
                                }
                                config.linkedConfigurations.remove(link.configKey());
                            }
                        }
                    }
                }
            }
        }
    }

    public HashSet<Integer> makeChannelList(WifiConfiguration config, int age) {
        if (config == null) {
            return null;
        }
        long now_ms = this.mClock.currentTimeMillis();
        HashSet<Integer> channels = new HashSet();
        if (getScanDetailCache(config) == null && config.linkedConfigurations == null) {
            return null;
        }
        ScanResult result;
        if (sVDBG) {
            StringBuilder dbg = new StringBuilder();
            dbg.append("makeChannelList age=").append(Integer.toString(age)).append(" for ").append(config.configKey()).append(" max=").append(this.mMaxNumActiveChannelsForPartialScans);
            if (getScanDetailCache(config) != null) {
                dbg.append(" bssids=").append(getScanDetailCache(config).size());
            }
            if (config.linkedConfigurations != null) {
                dbg.append(" linked=").append(config.linkedConfigurations.size());
            }
            logd(dbg.toString());
        }
        int numChannels = 0;
        if (getScanDetailCache(config) != null && getScanDetailCache(config).size() > 0) {
            for (ScanDetail scanDetail : getScanDetailCache(config).values()) {
                result = scanDetail.getScanResult();
                if (numChannels > this.mMaxNumActiveChannelsForPartialScans.get()) {
                    break;
                }
                if (sVDBG) {
                    logd("has " + result.BSSID + " freq=" + Integer.toString(result.frequency) + " age=" + Long.toString(now_ms - result.seen) + " ?=" + (now_ms - result.seen < ((long) age) ? DBG : ROAM_ON_ANY));
                }
                if (now_ms - result.seen < ((long) age)) {
                    channels.add(Integer.valueOf(result.frequency));
                    numChannels++;
                }
            }
        }
        if (config.linkedConfigurations != null) {
            for (String key : config.linkedConfigurations.keySet()) {
                WifiConfiguration linked = getWifiConfiguration(key);
                if (!(linked == null || getScanDetailCache(linked) == null)) {
                    for (ScanDetail scanDetail2 : getScanDetailCache(linked).values()) {
                        result = scanDetail2.getScanResult();
                        if (sVDBG) {
                            logd("has link: " + result.BSSID + " freq=" + Integer.toString(result.frequency) + " age=" + Long.toString(now_ms - result.seen));
                        }
                        if (numChannels > this.mMaxNumActiveChannelsForPartialScans.get()) {
                            break;
                        }
                        if (now_ms - result.seen < ((long) age)) {
                            channels.add(Integer.valueOf(result.frequency));
                            numChannels++;
                        }
                    }
                }
            }
        }
        return channels;
    }

    private Map<HomeSP, PasspointMatch> matchPasspointNetworks(ScanDetail scanDetail) {
        NetworkDetail networkDetail;
        if (this.mMOManager.isConfigured()) {
            networkDetail = scanDetail.getNetworkDetail();
            if (!networkDetail.hasInterworking()) {
                return null;
            }
            updateAnqpCache(scanDetail, networkDetail.getANQPElements());
            Map<HomeSP, PasspointMatch> matches = matchNetwork(scanDetail, DBG);
            Log.d(Utils.hs2LogTag(getClass()), scanDetail.getSSID() + " pass 1 matches: " + toMatchString(matches));
            return matches;
        }
        if (this.mEnableOsuQueries) {
            networkDetail = scanDetail.getNetworkDetail();
            List<ANQPElementType> querySet = ANQPFactory.buildQueryList(networkDetail, ROAM_ON_ANY, DBG);
            if (networkDetail.queriable(querySet)) {
                querySet = this.mAnqpCache.initiate(networkDetail, querySet);
                if (querySet != null) {
                    this.mSupplicantBridge.startANQP(scanDetail, querySet);
                }
                updateAnqpCache(scanDetail, networkDetail.getANQPElements());
            }
        }
        return null;
    }

    private Map<HomeSP, PasspointMatch> matchNetwork(ScanDetail scanDetail, boolean query) {
        NetworkDetail networkDetail = scanDetail.getNetworkDetail();
        ANQPData anqpData = this.mAnqpCache.getEntry(networkDetail);
        Map aNQPElements = anqpData != null ? anqpData.getANQPElements() : null;
        boolean queried = query ? ROAM_ON_ANY : DBG;
        Collection<HomeSP> homeSPs = this.mMOManager.getLoadedSPs().values();
        Map<HomeSP, PasspointMatch> matches = new HashMap(homeSPs.size());
        Log.d(Utils.hs2LogTag(getClass()), "match nwk " + scanDetail.toKeyString() + ", anqp " + (anqpData != null ? "present" : "missing") + ", query " + query + ", home sps: " + homeSPs.size());
        for (HomeSP homeSP : homeSPs) {
            PasspointMatch match = homeSP.match(networkDetail, aNQPElements, this.mSIMAccessor);
            Log.d(Utils.hs2LogTag(getClass()), " -- " + homeSP.getFQDN() + ": match " + match + ", queried " + queried);
            if ((match == PasspointMatch.Incomplete || this.mEnableOsuQueries) && !queried) {
                List<ANQPElementType> querySet = ANQPFactory.buildQueryList(networkDetail, match == PasspointMatch.Incomplete ? DBG : ROAM_ON_ANY, this.mEnableOsuQueries);
                if (networkDetail.queriable(querySet)) {
                    querySet = this.mAnqpCache.initiate(networkDetail, querySet);
                    if (querySet != null) {
                        this.mSupplicantBridge.startANQP(scanDetail, querySet);
                    }
                }
                queried = DBG;
            }
            matches.put(homeSP, match);
        }
        return matches;
    }

    public Map<ANQPElementType, ANQPElement> getANQPData(NetworkDetail network) {
        ANQPData data = this.mAnqpCache.getEntry(network);
        if (data != null) {
            return data.getANQPElements();
        }
        return null;
    }

    public SIMAccessor getSIMAccessor() {
        return this.mSIMAccessor;
    }

    public void notifyANQPDone(Long bssid, boolean success) {
        this.mSupplicantBridge.notifyANQPDone(bssid, success);
    }

    public void notifyIconReceived(IconEvent iconEvent) {
        Intent intent = new Intent("android.net.wifi.PASSPOINT_ICON_RECEIVED");
        intent.addFlags(67108864);
        intent.putExtra("bssid", iconEvent.getBSSID());
        intent.putExtra("file", iconEvent.getFileName());
        try {
            intent.putExtra("icon", this.mSupplicantBridge.retrieveIcon(iconEvent));
        } catch (IOException e) {
        }
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void updateAnqpCache(ScanDetail scanDetail, Map<ANQPElementType, ANQPElement> anqpElements) {
        NetworkDetail networkDetail = scanDetail.getNetworkDetail();
        if (anqpElements == null) {
            ANQPData data = this.mAnqpCache.getEntry(networkDetail);
            if (data != null) {
                scanDetail.propagateANQPInfo(data.getANQPElements());
            }
            return;
        }
        this.mAnqpCache.update(networkDetail, anqpElements);
    }

    private static String toMatchString(Map<HomeSP, PasspointMatch> matches) {
        StringBuilder sb = new StringBuilder();
        for (Entry<HomeSP, PasspointMatch> entry : matches.entrySet()) {
            sb.append(' ').append(((HomeSP) entry.getKey()).getFQDN()).append("->").append(entry.getValue());
        }
        return sb.toString();
    }

    private void cacheScanResultForPasspointConfigs(ScanDetail scanDetail, Map<HomeSP, PasspointMatch> matches, List<WifiConfiguration> associatedWifiConfigurations) {
        for (Entry<HomeSP, PasspointMatch> entry : matches.entrySet()) {
            PasspointMatch match = (PasspointMatch) entry.getValue();
            if (match == PasspointMatch.HomeProvider || match == PasspointMatch.RoamingProvider) {
                WifiConfiguration config = getWifiConfigForHomeSP((HomeSP) entry.getKey());
                if (config != null) {
                    cacheScanResultForConfig(config, scanDetail, (PasspointMatch) entry.getValue());
                    if (associatedWifiConfigurations != null) {
                        associatedWifiConfigurations.add(config);
                    }
                } else {
                    Log.w(Utils.hs2LogTag(getClass()), "Failed to find config for '" + ((HomeSP) entry.getKey()).getFQDN() + "'");
                }
            }
        }
    }

    private void cacheScanResultForConfig(WifiConfiguration config, ScanDetail scanDetail, PasspointMatch passpointMatch) {
        ScanResult scanResult = scanDetail.getScanResult();
        ScanDetailCache scanDetailCache = getScanDetailCache(config);
        if (scanDetailCache == null) {
            Log.w(TAG, "Could not allocate scan cache for " + config.SSID);
            return;
        }
        ScanResult result = scanDetailCache.get(scanResult.BSSID);
        if (result != null) {
            scanResult.blackListTimestamp = result.blackListTimestamp;
            scanResult.numIpConfigFailures = result.numIpConfigFailures;
            scanResult.numConnection = result.numConnection;
            scanResult.isAutoJoinCandidate = result.isAutoJoinCandidate;
        }
        if (config.ephemeral) {
            scanResult.untrusted = DBG;
        }
        if (scanDetailCache.size() > 192) {
            long now_dbg = 0;
            if (sVVDBG) {
                logd(" Will trim config " + config.configKey() + " size " + scanDetailCache.size());
                for (ScanDetail sd : scanDetailCache.values()) {
                    logd("     " + sd.getBSSIDString() + " " + sd.getSeen());
                }
                now_dbg = SystemClock.elapsedRealtimeNanos();
            }
            scanDetailCache.trim(MAX_NUM_SCAN_CACHE_ENTRIES);
            if (sVVDBG) {
                logd(" Finished trimming config, time(ns) " + (SystemClock.elapsedRealtimeNanos() - now_dbg));
                for (ScanDetail sd2 : scanDetailCache.values()) {
                    logd("     " + sd2.getBSSIDString() + " " + sd2.getSeen());
                }
            }
        }
        if (passpointMatch != null) {
            scanDetailCache.put(scanDetail, passpointMatch, getHomeSPForConfig(config));
        } else {
            scanDetailCache.put(scanDetail);
        }
        linkConfiguration(config);
    }

    private boolean isEncryptionWep(String encryption) {
        return encryption.contains("WEP");
    }

    private boolean isEncryptionPsk(String encryption) {
        return encryption.contains("PSK");
    }

    private boolean isEncryptionEap(String encryption) {
        return encryption.contains("EAP");
    }

    public boolean isOpenNetwork(String encryption) {
        if (isEncryptionWep(encryption) || isEncryptionPsk(encryption) || isEncryptionEap(encryption)) {
            return ROAM_ON_ANY;
        }
        return DBG;
    }

    public boolean isOpenNetwork(ScanResult scan) {
        return isOpenNetwork(scan.capabilities);
    }

    public boolean isOpenNetwork(WifiConfiguration config) {
        return isOpenNetwork(config.configKey());
    }

    public List<WifiConfiguration> getSavedNetworkFromScanDetail(ScanDetail scanDetail) {
        ScanResult scanResult = scanDetail.getScanResult();
        if (scanResult == null) {
            return null;
        }
        List<WifiConfiguration> savedWifiConfigurations = new ArrayList();
        String ssid = "\"" + scanResult.SSID + "\"";
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
            if (config.SSID != null && config.SSID.equals(ssid)) {
                localLog("getSavedNetworkFromScanDetail(): try " + config.configKey() + " SSID=" + config.SSID + " " + scanResult.SSID + " " + scanResult.capabilities);
                String scanResultEncrypt = scanResult.capabilities;
                String configEncrypt = config.configKey();
                int index = configEncrypt.indexOf(config.SSID);
                if (-1 != index) {
                    configEncrypt = configEncrypt.substring(config.SSID.length() + index);
                }
                if ((isEncryptionWep(scanResultEncrypt) && isEncryptionWep(configEncrypt)) || ((isEncryptionPsk(scanResultEncrypt) && isEncryptionPsk(configEncrypt)) || ((isEncryptionEap(scanResultEncrypt) && isEncryptionEap(configEncrypt)) || (isOpenNetwork(scanResultEncrypt) && isOpenNetwork(configEncrypt))))) {
                    savedWifiConfigurations.add(config);
                }
            }
        }
        return savedWifiConfigurations;
    }

    public List<WifiConfiguration> updateSavedNetworkWithNewScanDetail(ScanDetail scanDetail, boolean isConnectingOrConnected) {
        if (scanDetail.getScanResult() == null) {
            return null;
        }
        NetworkDetail networkDetail = scanDetail.getNetworkDetail();
        List<WifiConfiguration> associatedWifiConfigurations = new ArrayList();
        if (networkDetail.hasInterworking() && !isConnectingOrConnected) {
            Map<HomeSP, PasspointMatch> matches = matchPasspointNetworks(scanDetail);
            if (matches != null) {
                cacheScanResultForPasspointConfigs(scanDetail, matches, associatedWifiConfigurations);
            }
        }
        List<WifiConfiguration> savedConfigurations = getSavedNetworkFromScanDetail(scanDetail);
        if (savedConfigurations != null) {
            for (WifiConfiguration config : savedConfigurations) {
                cacheScanResultForConfig(config, scanDetail, null);
                associatedWifiConfigurations.add(config);
            }
        }
        if (associatedWifiConfigurations.size() == 0) {
            return null;
        }
        return associatedWifiConfigurations;
    }

    public void handleUserSwitch(int userId) {
        this.mCurrentUserId = userId;
        Set<WifiConfiguration> ephemeralConfigs = new HashSet();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
            if (config.ephemeral) {
                ephemeralConfigs.add(config);
            }
        }
        if (!ephemeralConfigs.isEmpty()) {
            for (WifiConfiguration config2 : ephemeralConfigs) {
                removeConfigWithoutBroadcast(config2);
            }
            saveConfig();
            writeKnownNetworkHistory();
        }
        for (WifiConfiguration network : this.mConfiguredNetworks.handleUserSwitch(this.mCurrentUserId)) {
            disableNetworkNative(network);
        }
        enableAllNetworks();
        sendConfiguredNetworksChangedBroadcast();
    }

    public int getCurrentUserId() {
        return this.mCurrentUserId;
    }

    public boolean isCurrentUserProfile(int userId) {
        boolean z = DBG;
        if (userId == this.mCurrentUserId) {
            return DBG;
        }
        UserInfo parent = this.mUserManager.getProfileParent(userId);
        if (parent == null || parent.id != this.mCurrentUserId) {
            z = ROAM_ON_ANY;
        }
        return z;
    }

    private NetworkUpdateResult writeIpAndProxyConfigurationsOnChange(WifiConfiguration currentConfig, WifiConfiguration newConfig, boolean isNewNetwork) {
        boolean ipChanged = ROAM_ON_ANY;
        boolean proxyChanged = ROAM_ON_ANY;
        switch (-getandroid-net-IpConfiguration$IpAssignmentSwitchesValues()[newConfig.getIpAssignment().ordinal()]) {
            case Extension.TYPE_DOUBLE /*1*/:
                if (currentConfig.getIpAssignment() != newConfig.getIpAssignment()) {
                    ipChanged = DBG;
                    break;
                }
                break;
            case Extension.TYPE_FLOAT /*2*/:
                if (currentConfig.getIpAssignment() == newConfig.getIpAssignment()) {
                    if (!Objects.equals(currentConfig.getStaticIpConfiguration(), newConfig.getStaticIpConfiguration())) {
                        ipChanged = DBG;
                        break;
                    }
                    ipChanged = ROAM_ON_ANY;
                    break;
                }
                ipChanged = DBG;
                break;
            case Extension.TYPE_INT64 /*3*/:
                break;
            default:
                loge("Ignore invalid ip assignment during write");
                break;
        }
        switch (-getandroid-net-IpConfiguration$ProxySettingsSwitchesValues()[newConfig.getProxySettings().ordinal()]) {
            case Extension.TYPE_DOUBLE /*1*/:
                if (currentConfig.getProxySettings() != newConfig.getProxySettings()) {
                    proxyChanged = DBG;
                    break;
                }
                break;
            case Extension.TYPE_FLOAT /*2*/:
            case Extension.TYPE_INT64 /*3*/:
                ProxyInfo newHttpProxy = newConfig.getHttpProxy();
                ProxyInfo currentHttpProxy = currentConfig.getHttpProxy();
                if (newHttpProxy == null) {
                    if (currentHttpProxy == null) {
                        proxyChanged = ROAM_ON_ANY;
                        break;
                    }
                    proxyChanged = DBG;
                    break;
                } else if (!newHttpProxy.equals(currentHttpProxy)) {
                    proxyChanged = DBG;
                    break;
                } else {
                    proxyChanged = ROAM_ON_ANY;
                    break;
                }
            case Extension.TYPE_UINT64 /*4*/:
                break;
            default:
                loge("Ignore invalid proxy configuration during write");
                break;
        }
        if (ipChanged) {
            currentConfig.setIpAssignment(newConfig.getIpAssignment());
            currentConfig.setStaticIpConfiguration(newConfig.getStaticIpConfiguration());
            log("IP config changed SSID = " + currentConfig.SSID);
            if (currentConfig.getStaticIpConfiguration() != null) {
                log(" static configuration: " + currentConfig.getStaticIpConfiguration().toString());
            }
        }
        if (proxyChanged) {
            currentConfig.setProxySettings(newConfig.getProxySettings());
            currentConfig.setHttpProxy(newConfig.getHttpProxy());
            log("proxy changed SSID = " + currentConfig.SSID);
            if (currentConfig.getHttpProxy() != null) {
                log(" proxyProperties: " + currentConfig.getHttpProxy().toString());
            }
        }
        if (ipChanged || proxyChanged || isNewNetwork) {
            if (sVDBG) {
                logd("writeIpAndProxyConfigurationsOnChange: " + currentConfig.SSID + " -> " + newConfig.SSID + " path: " + IP_CONFIG_FILE);
            }
            writeIpAndProxyConfigurations();
        }
        return new NetworkUpdateResult(ipChanged, proxyChanged);
    }

    private void readNetworkVariables(WifiConfiguration config) {
        this.mWifiConfigStore.readNetworkVariables(config);
    }

    public WifiConfiguration wifiConfigurationFromScanResult(ScanResult result) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + result.SSID + "\"";
        if (sVDBG) {
            logd("WifiConfiguration from scan results " + config.SSID + " cap " + result.capabilities);
        }
        if (result.capabilities.contains("PSK") || result.capabilities.contains("EAP") || result.capabilities.contains("WEP")) {
            if (result.capabilities.contains("PSK")) {
                config.allowedKeyManagement.set(1);
            }
            if (result.capabilities.contains("EAP")) {
                config.allowedKeyManagement.set(2);
                config.allowedKeyManagement.set(3);
            }
            if (result.capabilities.contains("WEP")) {
                config.allowedKeyManagement.set(0);
                config.allowedAuthAlgorithms.set(0);
                config.allowedAuthAlgorithms.set(1);
            }
        } else {
            config.allowedKeyManagement.set(0);
        }
        return config;
    }

    public WifiConfiguration wifiConfigurationFromScanResult(ScanDetail scanDetail) {
        return wifiConfigurationFromScanResult(scanDetail.getScanResult());
    }

    private static int configKey(WifiConfiguration config) {
        return config.configKey().hashCode();
    }

    void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WifiConfigManager");
        pw.println("mLastPriority " + this.mLastPriority);
        pw.println("Configured networks");
        for (WifiConfiguration conf : getAllConfiguredNetworks()) {
            pw.println(conf);
        }
        pw.println();
        if (this.mLostConfigsDbg != null && this.mLostConfigsDbg.size() > 0) {
            pw.println("LostConfigs: ");
            for (String s : this.mLostConfigsDbg) {
                pw.println(s);
            }
        }
        if (this.mLocalLog != null) {
            pw.println("WifiConfigManager - Log Begin ----");
            this.mLocalLog.dump(fd, pw, args);
            pw.println("WifiConfigManager - Log End ----");
        }
        if (this.mMOManager.isConfigured()) {
            pw.println("Begin dump of ANQP Cache");
            this.mAnqpCache.dump(pw);
            pw.println("End dump of ANQP Cache");
        }
    }

    public String getConfigFile() {
        return IP_CONFIG_FILE;
    }

    protected void logd(String s) {
        Log.d(TAG, s);
    }

    protected void loge(String s) {
        loge(s, ROAM_ON_ANY);
    }

    protected void loge(String s, boolean stack) {
        if (stack) {
            Log.e(TAG, s + " stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        } else {
            Log.e(TAG, s);
        }
    }

    private void logKernelTime() {
        long kernelTimeMs = System.nanoTime() / 1000000;
        StringBuilder builder = new StringBuilder();
        builder.append("kernel time = ").append(kernelTimeMs / 1000).append(".").append(kernelTimeMs % 1000).append("\n");
        localLog(builder.toString());
    }

    protected void log(String s) {
        Log.d(TAG, s);
    }

    private void localLog(String s) {
        if (this.mLocalLog != null) {
            this.mLocalLog.log(s);
        }
    }

    private void localLogAndLogcat(String s) {
        localLog(s);
        Log.d(TAG, s);
    }

    private void localLogNetwork(String s, int netId) {
        if (this.mLocalLog != null) {
            WifiConfiguration config;
            synchronized (this.mConfiguredNetworks) {
                config = this.mConfiguredNetworks.getForAllUsers(netId);
            }
            if (config != null) {
                this.mLocalLog.log(s + " " + config.getPrintableSsid() + " " + netId + " status=" + config.status + " key=" + config.configKey());
            } else {
                this.mLocalLog.log(s + " " + netId);
            }
        }
    }

    static boolean needsSoftwareBackedKeyStore(WifiEnterpriseConfig config) {
        if (TextUtils.isEmpty(config.getClientCertificateAlias())) {
            return ROAM_ON_ANY;
        }
        return DBG;
    }

    public boolean isSimConfig(WifiConfiguration config) {
        return this.mWifiConfigStore.isSimConfig(config);
    }

    public void resetSimNetworks() {
        this.mWifiConfigStore.resetSimNetworks(this.mConfiguredNetworks.valuesForCurrentUser());
    }

    boolean isNetworkConfigured(WifiConfiguration config) {
        boolean z = DBG;
        if (config.networkId != -1) {
            if (this.mConfiguredNetworks.getForCurrentUser(config.networkId) == null) {
                z = ROAM_ON_ANY;
            }
            return z;
        }
        if (this.mConfiguredNetworks.getByConfigKeyForCurrentUser(config.configKey()) == null) {
            z = ROAM_ON_ANY;
        }
        return z;
    }

    boolean canModifyNetwork(int uid, int networkId, boolean onlyAnnotate) {
        boolean z = DBG;
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(networkId);
        if (config == null) {
            loge("canModifyNetwork: cannot find config networkId " + networkId);
            return ROAM_ON_ANY;
        }
        boolean isUidDeviceOwner;
        DevicePolicyManagerInternal dpmi = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        if (dpmi != null) {
            isUidDeviceOwner = dpmi.isActiveAdminWithPolicy(uid, -2);
        } else {
            isUidDeviceOwner = ROAM_ON_ANY;
        }
        if (isUidDeviceOwner) {
            return DBG;
        }
        boolean isCreator = config.creatorUid == uid ? DBG : ROAM_ON_ANY;
        if (onlyAnnotate) {
            if (!isCreator) {
                z = checkConfigOverridePermission(uid);
            }
            return z;
        } else if (this.mContext.getPackageManager().hasSystemFeature("android.software.device_admin") && dpmi == null) {
            return ROAM_ON_ANY;
        } else {
            boolean isConfigEligibleForLockdown;
            if (dpmi != null) {
                isConfigEligibleForLockdown = dpmi.isActiveAdminWithPolicy(config.creatorUid, -2);
            } else {
                isConfigEligibleForLockdown = ROAM_ON_ANY;
            }
            if (isConfigEligibleForLockdown) {
                boolean isLockdownFeatureEnabled;
                if (Global.getInt(this.mContext.getContentResolver(), "wifi_device_owner_configs_lockdown", 0) != 0) {
                    isLockdownFeatureEnabled = DBG;
                } else {
                    isLockdownFeatureEnabled = ROAM_ON_ANY;
                }
                if (isLockdownFeatureEnabled) {
                    z = ROAM_ON_ANY;
                } else {
                    z = checkConfigOverridePermission(uid);
                }
                return z;
            }
            if (!isCreator) {
                z = checkConfigOverridePermission(uid);
            }
            return z;
        }
    }

    boolean canModifyNetwork(int uid, WifiConfiguration config, boolean onlyAnnotate) {
        if (config == null) {
            loge("canModifyNetowrk recieved null configuration");
            return ROAM_ON_ANY;
        }
        int netid;
        if (config.networkId != -1) {
            netid = config.networkId;
        } else {
            WifiConfiguration test = this.mConfiguredNetworks.getByConfigKeyForCurrentUser(config.configKey());
            if (test == null) {
                return ROAM_ON_ANY;
            }
            netid = test.networkId;
        }
        return canModifyNetwork(uid, netid, onlyAnnotate);
    }

    boolean checkConfigOverridePermission(int uid) {
        boolean z = ROAM_ON_ANY;
        try {
            if (this.mFacade.checkUidPermission("android.permission.OVERRIDE_WIFI_CONFIG", uid) == 0) {
                z = DBG;
            }
            return z;
        } catch (RemoteException e) {
            return ROAM_ON_ANY;
        }
    }

    void handleBadNetworkDisconnectReport(int netId, WifiInfo info) {
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (config != null && ((!info.is24GHz() || info.getRssi() > -73) && (!info.is5GHz() || info.getRssi() > -70))) {
            Log.d(TAG, "updateNetworkSelectionStatus(DISABLED_BAD_LINK)");
            updateNetworkSelectionStatus(config, 1);
        }
        this.mLastUnwantedNetworkDisconnectTimestamp = this.mClock.currentTimeMillis();
    }

    int getMaxDhcpRetries() {
        return this.mFacade.getIntegerSetting(this.mContext, "wifi_max_dhcp_retry_count", DEFAULT_MAX_DHCP_RETRIES);
    }

    void clearBssidBlacklist() {
        this.mWifiConfigStore.clearBssidBlacklist();
    }

    void blackListBssid(String bssid) {
        this.mWifiConfigStore.blackListBssid(bssid);
    }

    public boolean isBssidBlacklisted(String bssid) {
        return this.mWifiConfigStore.isBssidBlacklisted(bssid);
    }

    public boolean getEnableAutoJoinWhenAssociated() {
        return this.mEnableAutoJoinWhenAssociated.get();
    }

    public void setEnableAutoJoinWhenAssociated(boolean enabled) {
        this.mEnableAutoJoinWhenAssociated.set(enabled);
    }

    public void setActiveScanDetail(ScanDetail activeScanDetail) {
        synchronized (this.mActiveScanDetailLock) {
            this.mActiveScanDetail = activeScanDetail;
        }
    }

    public boolean wasEphemeralNetworkDeleted(String ssid) {
        return this.mDeletedEphemeralSSIDs.contains(ssid);
    }

    public void updateWifiConfigByWifiPro(WifiConfiguration config, boolean uiOnly) {
    }
}
