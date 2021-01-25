package com.android.server.wifi.hotspot2;

import android.app.AppOpsManager;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiSsid;
import android.net.wifi.hotspot2.IProvisioningCallback;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.hotspot2.pps.Credential;
import android.net.wifi.hotspot2.pps.HomeSp;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.server.wifi.Clock;
import com.android.server.wifi.IMSIParameter;
import com.android.server.wifi.SIMAccessor;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.WifiConfigStore;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiKeyStore;
import com.android.server.wifi.WifiMetrics;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.PasspointConfigSharedStoreData;
import com.android.server.wifi.hotspot2.PasspointConfigUserStoreData;
import com.android.server.wifi.hotspot2.PasspointEventHandler;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.hotspot2.anqp.HSOsuProvidersElement;
import com.android.server.wifi.hotspot2.anqp.NAIRealmElement;
import com.android.server.wifi.hotspot2.anqp.OsuProviderInfo;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.util.InformationElementUtil;
import com.android.server.wifi.util.TelephonyUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PasspointManager {
    private static final String TAG = "PasspointManager";
    private static PasspointManager sPasspointManager;
    private final AnqpCache mAnqpCache;
    private final ANQPRequestManager mAnqpRequestManager;
    private final AppOpsManager mAppOps;
    private final Map<String, AppOpsChangedListener> mAppOpsChangedListenerPerApp = new HashMap();
    private final CertificateVerifier mCertVerifier;
    private final Handler mHandler;
    private final WifiKeyStore mKeyStore;
    private final PasspointObjectFactory mObjectFactory;
    private final PasspointEventHandler mPasspointEventHandler;
    private final PasspointProvisioner mPasspointProvisioner;
    private final Map<String, PasspointProvider> mPreconfiguredProviders;
    private long mProviderIndex;
    private final Map<String, PasspointProvider> mProviders;
    private final SIMAccessor mSimAccessor;
    private final SubscriptionManager mSubscriptionManager;
    private final TelephonyManager mTelephonyManager;
    private boolean mVerboseLoggingEnabled = false;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiInjector mWifiInjector;
    private final WifiMetrics mWifiMetrics;

    private class CallbackHandler implements PasspointEventHandler.Callbacks {
        private final Context mContext;

        CallbackHandler(Context context) {
            this.mContext = context;
        }

        @Override // com.android.server.wifi.hotspot2.PasspointEventHandler.Callbacks
        public void onANQPResponse(long bssid, Map<Constants.ANQPElementType, ANQPElement> anqpElements) {
            ANQPNetworkKey anqpKey = PasspointManager.this.mAnqpRequestManager.onRequestCompleted(bssid, anqpElements != null);
            if (anqpElements != null && anqpKey != null) {
                PasspointManager.this.mAnqpCache.addEntry(anqpKey, anqpElements);
            }
        }

        @Override // com.android.server.wifi.hotspot2.PasspointEventHandler.Callbacks
        public void onIconResponse(long bssid, String fileName, byte[] data) {
        }

        @Override // com.android.server.wifi.hotspot2.PasspointEventHandler.Callbacks
        public void onWnmFrameReceived(WnmData event) {
        }
    }

    private class UserDataSourceHandler implements PasspointConfigUserStoreData.DataSource {
        private UserDataSourceHandler() {
        }

        @Override // com.android.server.wifi.hotspot2.PasspointConfigUserStoreData.DataSource
        public List<PasspointProvider> getProviders() {
            List<PasspointProvider> providers = new ArrayList<>();
            for (Map.Entry<String, PasspointProvider> entry : PasspointManager.this.mProviders.entrySet()) {
                providers.add(entry.getValue());
            }
            return providers;
        }

        @Override // com.android.server.wifi.hotspot2.PasspointConfigUserStoreData.DataSource
        public void setProviders(List<PasspointProvider> providers) {
            PasspointManager.this.mProviders.clear();
            for (PasspointProvider provider : providers) {
                PasspointManager.this.mProviders.put(provider.getConfig().getHomeSp().getFqdn(), provider);
                if (provider.getPackageName() != null) {
                    PasspointManager.this.startTrackingAppOpsChange(provider.getPackageName(), provider.getCreatorUid());
                }
            }
        }
    }

    private class SharedDataSourceHandler implements PasspointConfigSharedStoreData.DataSource {
        private SharedDataSourceHandler() {
        }

        @Override // com.android.server.wifi.hotspot2.PasspointConfigSharedStoreData.DataSource
        public long getProviderIndex() {
            return PasspointManager.this.mProviderIndex;
        }

        @Override // com.android.server.wifi.hotspot2.PasspointConfigSharedStoreData.DataSource
        public void setProviderIndex(long providerIndex) {
            PasspointManager.this.mProviderIndex = providerIndex;
        }
    }

    /* access modifiers changed from: private */
    public final class AppOpsChangedListener implements AppOpsManager.OnOpChangedListener {
        private final String mPackageName;
        private final int mUid;

        AppOpsChangedListener(String packageName, int uid) {
            this.mPackageName = packageName;
            this.mUid = uid;
        }

        @Override // android.app.AppOpsManager.OnOpChangedListener
        public void onOpChanged(String op, String packageName) {
            PasspointManager.this.mHandler.post(new Runnable(packageName, op) {
                /* class com.android.server.wifi.hotspot2.$$Lambda$PasspointManager$AppOpsChangedListener$0nwoeLZ16GB6O9PThiDjYN9uQuE */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ String f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    PasspointManager.AppOpsChangedListener.this.lambda$onOpChanged$0$PasspointManager$AppOpsChangedListener(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$onOpChanged$0$PasspointManager$AppOpsChangedListener(String packageName, String op) {
            if (this.mPackageName.equals(packageName) && "android:change_wifi_state".equals(op)) {
                try {
                    PasspointManager.this.mAppOps.checkPackage(this.mUid, this.mPackageName);
                    if (PasspointManager.this.mAppOps.unsafeCheckOpNoThrow("android:change_wifi_state", this.mUid, this.mPackageName) == 1) {
                        Log.i(PasspointManager.TAG, "User disallowed change wifi state for " + packageName);
                        PasspointManager.this.removePasspointProviderWithPackage(this.mPackageName);
                    }
                } catch (SecurityException e) {
                    Log.wtf(PasspointManager.TAG, "Invalid uid/package" + packageName);
                }
            }
        }
    }

    public void removePasspointProviderWithPackage(String packageName) {
        stopTrackingAppOpsChange(packageName);
        for (Map.Entry<String, PasspointProvider> entry : getPasspointProviderWithPackage(packageName).entrySet()) {
            String fqdn = entry.getValue().getConfig().getHomeSp().getFqdn();
            removeProvider(fqdn);
            disconnectIfPasspointNetwork(fqdn);
        }
    }

    private Map<String, PasspointProvider> getPasspointProviderWithPackage(String packageName) {
        return (Map) this.mProviders.entrySet().stream().filter(new Predicate(packageName) {
            /* class com.android.server.wifi.hotspot2.$$Lambda$PasspointManager$Gpr9uNii_VR41_eiXPR6Xaaliag */
            private final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return TextUtils.equals(this.f$0, ((PasspointProvider) ((Map.Entry) obj).getValue()).getPackageName());
            }
        }).collect(Collectors.toMap($$Lambda$PasspointManager$pKR4KzimmVxw_e4yzicg7QJrJ1c.INSTANCE, $$Lambda$PasspointManager$WfXcPLClLXFUI2CymehBp9oUwqE.INSTANCE));
    }

    static /* synthetic */ String lambda$getPasspointProviderWithPackage$1(Map.Entry entry) {
        return (String) entry.getKey();
    }

    static /* synthetic */ PasspointProvider lambda$getPasspointProviderWithPackage$2(Map.Entry entry) {
        return (PasspointProvider) entry.getValue();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startTrackingAppOpsChange(String packageName, int uid) {
        if (!this.mAppOpsChangedListenerPerApp.containsKey(packageName)) {
            AppOpsChangedListener appOpsChangedListener = new AppOpsChangedListener(packageName, uid);
            this.mAppOps.startWatchingMode("android:change_wifi_state", packageName, appOpsChangedListener);
            this.mAppOpsChangedListenerPerApp.put(packageName, appOpsChangedListener);
        }
    }

    private void stopTrackingAppOpsChange(String packageName) {
        AppOpsChangedListener appOpsChangedListener = this.mAppOpsChangedListenerPerApp.remove(packageName);
        if (appOpsChangedListener == null) {
            Log.wtf(TAG, "No app ops listener found for " + packageName);
            return;
        }
        this.mAppOps.stopWatchingMode(appOpsChangedListener);
    }

    private void disconnectIfPasspointNetwork(String fqdn) {
        WifiConfiguration currentConfiguration = this.mWifiInjector.getClientModeImpl().getCurrentWifiConfiguration();
        if (currentConfiguration != null && currentConfiguration.isPasspoint() && TextUtils.equals(currentConfiguration.FQDN, fqdn)) {
            Log.i(TAG, "Disconnect current Passpoint network for " + fqdn + "because the profile was removed");
            this.mWifiInjector.getClientModeImpl().disconnectCommand();
        }
    }

    public PasspointManager(Context context, WifiInjector wifiInjector, Handler handler, WifiNative wifiNative, WifiKeyStore keyStore, Clock clock, SIMAccessor simAccessor, PasspointObjectFactory objectFactory, WifiConfigManager wifiConfigManager, WifiConfigStore wifiConfigStore, WifiMetrics wifiMetrics, TelephonyManager telephonyManager, SubscriptionManager subscriptionManager) {
        this.mPasspointEventHandler = objectFactory.makePasspointEventHandler(wifiNative, new CallbackHandler(context));
        this.mWifiInjector = wifiInjector;
        this.mHandler = handler;
        this.mKeyStore = keyStore;
        this.mSimAccessor = simAccessor;
        this.mObjectFactory = objectFactory;
        this.mProviders = new HashMap();
        this.mPreconfiguredProviders = new HashMap();
        this.mAnqpCache = objectFactory.makeAnqpCache(clock);
        this.mAnqpRequestManager = objectFactory.makeANQPRequestManager(this.mPasspointEventHandler, clock);
        this.mCertVerifier = objectFactory.makeCertificateVerifier();
        this.mWifiConfigManager = wifiConfigManager;
        this.mWifiMetrics = wifiMetrics;
        this.mProviderIndex = 0;
        this.mTelephonyManager = telephonyManager;
        this.mSubscriptionManager = subscriptionManager;
        wifiConfigStore.registerStoreData(objectFactory.makePasspointConfigUserStoreData(this.mKeyStore, this.mSimAccessor, new UserDataSourceHandler()));
        wifiConfigStore.registerStoreData(objectFactory.makePasspointConfigSharedStoreData(new SharedDataSourceHandler()));
        this.mPasspointProvisioner = objectFactory.makePasspointProvisioner(context, wifiNative, this, wifiMetrics);
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        sPasspointManager = this;
    }

    public void initializeProvisioner(Looper looper) {
        this.mPasspointProvisioner.init(looper);
    }

    public void enableVerboseLogging(int verbose) {
        this.mVerboseLoggingEnabled = verbose > 0;
        this.mPasspointProvisioner.enableVerboseLogging(verbose);
    }

    public boolean addOrUpdateProvider(PasspointConfiguration config, int uid, String packageName) {
        this.mWifiMetrics.incrementNumPasspointProviderInstallation();
        if (config == null) {
            Log.e(TAG, "Configuration not provided");
            return false;
        } else if (!config.validate()) {
            Log.e(TAG, "Invalid configuration");
            return false;
        } else {
            X509Certificate[] x509Certificates = config.getCredential().getCaCertificates();
            if (config.getUpdateIdentifier() == Integer.MIN_VALUE && x509Certificates != null) {
                try {
                    for (X509Certificate certificate : x509Certificates) {
                        this.mCertVerifier.verifyCaCert(certificate);
                    }
                } catch (GeneralSecurityException e) {
                    Log.e(TAG, "Exception Failed to verify CA certificate for the first reason");
                    return false;
                } catch (IOException e2) {
                    Log.e(TAG, "Exception Failed to verify CA certificate");
                    return false;
                } catch (Exception e3) {
                    Log.e(TAG, "Failed to verify CA certificate");
                    return false;
                }
            }
            PasspointObjectFactory passpointObjectFactory = this.mObjectFactory;
            WifiKeyStore wifiKeyStore = this.mKeyStore;
            SIMAccessor sIMAccessor = this.mSimAccessor;
            long j = this.mProviderIndex;
            this.mProviderIndex = 1 + j;
            PasspointProvider newProvider = passpointObjectFactory.makePasspointProvider(config, wifiKeyStore, sIMAccessor, j, uid, packageName);
            if (!newProvider.installCertsAndKeys()) {
                Log.e(TAG, "Failed to install certificates and keys to keystore");
                return false;
            }
            if (this.mProviders.containsKey(config.getHomeSp().getFqdn())) {
                Log.d(TAG, "Replacing configuration for " + config.getHomeSp().getFqdn());
                this.mProviders.get(config.getHomeSp().getFqdn()).uninstallCertsAndKeys();
                this.mProviders.remove(config.getHomeSp().getFqdn());
            }
            this.mProviders.put(config.getHomeSp().getFqdn(), newProvider);
            this.mWifiConfigManager.saveToStore(true);
            if (newProvider.getPackageName() != null) {
                startTrackingAppOpsChange(newProvider.getPackageName(), uid);
            }
            Log.d(TAG, "Added/updated Passpoint configuration: " + config.getHomeSp().getFqdn() + " by " + uid);
            this.mWifiMetrics.incrementNumPasspointProviderInstallSuccess();
            return true;
        }
    }

    public int findEapMethodFromNAIRealmMatchedWithCarrier(List<ScanDetail> scanDetails) {
        if (!TelephonyUtil.isSimPresent(this.mSubscriptionManager)) {
            return -1;
        }
        if (scanDetails == null) {
            return -1;
        }
        if (scanDetails.isEmpty()) {
            return -1;
        }
        String mccMnc = this.mTelephonyManager.createForSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId()).getSimOperator();
        if (mccMnc == null) {
            return -1;
        }
        if (mccMnc.length() < 5) {
            return -1;
        }
        String domain = Utils.getRealmForMccMnc(mccMnc);
        if (domain == null) {
            return -1;
        }
        for (ScanDetail scanDetail : scanDetails) {
            if (scanDetail.getNetworkDetail().isInterworking()) {
                ScanResult scanResult = scanDetail.getScanResult();
                InformationElementUtil.RoamingConsortium roamingConsortium = InformationElementUtil.getRoamingConsortiumIE(scanResult.informationElements);
                InformationElementUtil.Vsa vsa = InformationElementUtil.getHS2VendorSpecificIE(scanResult.informationElements);
                try {
                    long bssid = Utils.parseMac(scanResult.BSSID);
                    ANQPNetworkKey anqpKey = ANQPNetworkKey.buildKey(scanResult.SSID, bssid, scanResult.hessid, vsa.anqpDomainID);
                    ANQPData anqpEntry = this.mAnqpCache.getEntry(anqpKey);
                    if (anqpEntry == null) {
                        this.mAnqpRequestManager.requestANQPElements(bssid, anqpKey, roamingConsortium.anqpOICount > 0, vsa.hsRelease == NetworkDetail.HSRelease.R2);
                        Log.d(TAG, "ANQP entry not found for: " + anqpKey);
                    } else {
                        int eapMethod = ANQPMatcher.getCarrierEapMethodFromMatchingNAIRealm(domain, (NAIRealmElement) anqpEntry.getElements().get(Constants.ANQPElementType.ANQPNAIRealm));
                        if (eapMethod != -1) {
                            return eapMethod;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid BSSID provided in the scan result: " + StringUtilEx.safeDisplayBssid(scanResult.BSSID));
                }
            }
        }
        return -1;
    }

    public PasspointConfiguration createEphemeralPasspointConfigForCarrier(int eapMethod) {
        String mccMnc = this.mTelephonyManager.createForSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId()).getSimOperator();
        if (mccMnc == null || mccMnc.length() < 5) {
            Log.e(TAG, "invalid length of mccmnc");
            return null;
        } else if (!Utils.isCarrierEapMethod(eapMethod)) {
            Log.e(TAG, "invalid eapMethod type");
            return null;
        } else {
            String domain = Utils.getRealmForMccMnc(mccMnc);
            if (domain == null) {
                Log.e(TAG, "can't make a home domain name using " + mccMnc);
                return null;
            }
            PasspointConfiguration config = new PasspointConfiguration();
            HomeSp homeSp = new HomeSp();
            homeSp.setFqdn(domain);
            homeSp.setFriendlyName(this.mTelephonyManager.createForSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId()).getSimOperatorName());
            config.setHomeSp(homeSp);
            Credential credential = new Credential();
            credential.setRealm(domain);
            Credential.SimCredential simCredential = new Credential.SimCredential();
            simCredential.setImsi(mccMnc + "*");
            simCredential.setEapType(eapMethod);
            credential.setSimCredential(simCredential);
            config.setCredential(credential);
            if (config.validate()) {
                return config;
            }
            Log.e(TAG, "Transient PasspointConfiguration is not a valid format: " + config);
            return null;
        }
    }

    public boolean hasCarrierProvider(String mccmnc) {
        String domain = Utils.getRealmForMccMnc(mccmnc);
        if (domain == null) {
            Log.e(TAG, "can't make a home domain name using " + mccmnc);
            return false;
        }
        for (Map.Entry<String, PasspointProvider> provider : this.mProviders.entrySet()) {
            if (provider.getValue().getConfig().getCredential().getSimCredential() != null) {
                if (domain.equals(provider.getKey())) {
                    return true;
                }
                IMSIParameter imsiParameter = provider.getValue().getImsiParameter();
                if (imsiParameter != null && imsiParameter.matchesMccMnc(mccmnc)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean installEphemeralPasspointConfigForCarrier(PasspointConfiguration config) {
        if (config == null) {
            Log.e(TAG, "PasspointConfiguration for carrier is null");
            return false;
        } else if (!TelephonyUtil.isSimPresent(this.mSubscriptionManager)) {
            Log.e(TAG, "Sim is not presented on the device");
            return false;
        } else {
            Credential.SimCredential simCredential = config.getCredential().getSimCredential();
            if (simCredential == null || simCredential.getImsi() == null) {
                Log.e(TAG, "This is not for a carrier configuration using EAP-SIM/AKA/AKA'");
                return false;
            } else if (!config.validate()) {
                Log.e(TAG, "It is not a valid format for Passpoint Configuration with EAP-SIM/AKA/AKA'");
                return false;
            } else {
                String imsi = simCredential.getImsi();
                if (imsi.length() < 6) {
                    Log.e(TAG, "Invalid IMSI length: " + imsi.length());
                    return false;
                }
                int index = imsi.indexOf("*");
                if (index == -1) {
                    Log.e(TAG, "missing * in imsi");
                    return false;
                } else if (hasCarrierProvider(imsi.substring(0, index))) {
                    Log.e(TAG, "It is already in the Provider list");
                    return false;
                } else {
                    PasspointObjectFactory passpointObjectFactory = this.mObjectFactory;
                    WifiKeyStore wifiKeyStore = this.mKeyStore;
                    SIMAccessor sIMAccessor = this.mSimAccessor;
                    long j = this.mProviderIndex;
                    this.mProviderIndex = 1 + j;
                    PasspointProvider newProvider = passpointObjectFactory.makePasspointProvider(config, wifiKeyStore, sIMAccessor, j, 1010, null);
                    newProvider.setEphemeral(true);
                    Log.d(TAG, "installed PasspointConfiguration for carrier : " + config.getHomeSp().getFriendlyName());
                    this.mProviders.put(config.getHomeSp().getFqdn(), newProvider);
                    this.mWifiConfigManager.saveToStore(true);
                    return true;
                }
            }
        }
    }

    public boolean removeProvider(String fqdn) {
        this.mWifiMetrics.incrementNumPasspointProviderUninstallation();
        if (!this.mProviders.containsKey(fqdn)) {
            Log.e(TAG, "Config doesn't exist");
            return false;
        }
        this.mProviders.get(fqdn).uninstallCertsAndKeys();
        String packageName = this.mProviders.get(fqdn).getPackageName();
        this.mProviders.remove(fqdn);
        this.mWifiConfigManager.saveToStore(true);
        if (this.mAppOpsChangedListenerPerApp.containsKey(packageName) && getPasspointProviderWithPackage(packageName).size() == 0) {
            stopTrackingAppOpsChange(packageName);
        }
        Log.d(TAG, "Removed Passpoint configuration: " + fqdn);
        this.mWifiMetrics.incrementNumPasspointProviderUninstallSuccess();
        return true;
    }

    public void removeEphemeralProviders() {
        this.mProviders.entrySet().removeIf(new Predicate() {
            /* class com.android.server.wifi.hotspot2.$$Lambda$PasspointManager$Lt6Wq4O4jp8kKNMflQQuqAIcHwk */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return PasspointManager.this.lambda$removeEphemeralProviders$3$PasspointManager((Map.Entry) obj);
            }
        });
    }

    public /* synthetic */ boolean lambda$removeEphemeralProviders$3$PasspointManager(Map.Entry entry) {
        PasspointProvider provider = (PasspointProvider) entry.getValue();
        if (provider == null || !provider.isEphemeral()) {
            return false;
        }
        this.mWifiConfigManager.removePasspointConfiguredNetwork((String) entry.getKey());
        return true;
    }

    public List<PasspointConfiguration> getProviderConfigs() {
        List<PasspointConfiguration> configs = new ArrayList<>();
        for (Map.Entry<String, PasspointProvider> entry : this.mProviders.entrySet()) {
            if (!this.mPreconfiguredProviders.containsKey(entry.getKey())) {
                configs.add(entry.getValue().getConfig());
            }
        }
        return configs;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x005a: APUT  (r3v5 java.lang.Object[]), (2 ??[int, float, short, byte, char]), (r5v8 java.lang.String) */
    public Pair<PasspointProvider, PasspointMatch> matchProvider(ScanResult scanResult) {
        String str;
        List<Pair<PasspointProvider, PasspointMatch>> allMatches = getAllMatchedProviders(scanResult);
        if (allMatches == null) {
            return null;
        }
        Pair<PasspointProvider, PasspointMatch> bestMatch = null;
        Iterator<Pair<PasspointProvider, PasspointMatch>> it = allMatches.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Pair<PasspointProvider, PasspointMatch> match = it.next();
            if (match.second == PasspointMatch.HomeProvider) {
                bestMatch = match;
                break;
            } else if (match.second == PasspointMatch.RoamingProvider && bestMatch == null) {
                bestMatch = match;
            }
        }
        if (bestMatch != null) {
            Object[] objArr = new Object[3];
            objArr[0] = StringUtilEx.safeDisplaySsid(scanResult.SSID);
            objArr[1] = ((PasspointProvider) bestMatch.first).getConfig().getHomeSp().getFqdn();
            if (bestMatch.second == PasspointMatch.HomeProvider) {
                str = "Home Provider";
            } else {
                str = "Roaming Provider";
            }
            objArr[2] = str;
            Log.d(TAG, String.format("Matched %s to %s as %s", objArr));
        } else if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "No service provider found for " + StringUtilEx.safeDisplaySsid(scanResult.SSID));
        }
        return bestMatch;
    }

    public List<Pair<PasspointProvider, PasspointMatch>> getAllMatchedProviders(ScanResult scanResult) {
        String str;
        List<Pair<PasspointProvider, PasspointMatch>> allMatches = new ArrayList<>();
        InformationElementUtil.RoamingConsortium roamingConsortium = InformationElementUtil.getRoamingConsortiumIE(scanResult.informationElements);
        InformationElementUtil.Vsa vsa = InformationElementUtil.getHS2VendorSpecificIE(scanResult.informationElements);
        try {
            long bssid = Utils.parseMac(scanResult.BSSID);
            ANQPNetworkKey anqpKey = ANQPNetworkKey.buildKey(scanResult.SSID, bssid, scanResult.hessid, vsa.anqpDomainID);
            ANQPData anqpEntry = this.mAnqpCache.getEntry(anqpKey);
            char c = 0;
            if (anqpEntry == null) {
                this.mAnqpRequestManager.requestANQPElements(bssid, anqpKey, roamingConsortium.anqpOICount > 0, vsa.hsRelease == NetworkDetail.HSRelease.R2);
                Log.d(TAG, "ANQP entry not found for: " + anqpKey);
                return allMatches;
            }
            for (Map.Entry<String, PasspointProvider> entry : this.mProviders.entrySet()) {
                PasspointProvider provider = entry.getValue();
                PasspointMatch matchStatus = provider.match(anqpEntry.getElements(), roamingConsortium);
                if (matchStatus == PasspointMatch.HomeProvider || matchStatus == PasspointMatch.RoamingProvider) {
                    allMatches.add(Pair.create(provider, matchStatus));
                }
            }
            if (allMatches.size() != 0) {
                for (Pair<PasspointProvider, PasspointMatch> match : allMatches) {
                    Object[] objArr = new Object[3];
                    objArr[c] = StringUtilEx.safeDisplaySsid(scanResult.SSID);
                    objArr[1] = ((PasspointProvider) match.first).getConfig().getHomeSp().getFqdn();
                    if (match.second == PasspointMatch.HomeProvider) {
                        str = "Home Provider";
                    } else {
                        str = "Roaming Provider";
                    }
                    objArr[2] = str;
                    Log.d(TAG, String.format("Matched %s to %s as %s", objArr));
                    c = 0;
                }
            } else if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "No service providers found for " + StringUtilEx.safeDisplaySsid(scanResult.SSID));
            }
            return allMatches;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid BSSID provided in the scan result: " + StringUtilEx.safeDisplayBssid(scanResult.BSSID));
            return allMatches;
        }
    }

    public static boolean addLegacyPasspointConfig(WifiConfiguration config) {
        if (sPasspointManager == null) {
            Log.e(TAG, "PasspointManager have not been initialized yet");
            return false;
        }
        Log.d(TAG, "Installing legacy Passpoint configuration: " + config.FQDN);
        return sPasspointManager.addWifiConfig(config);
    }

    public static void addProviders(List<PasspointProvider> providers) {
        if (sPasspointManager == null) {
            Log.e(TAG, "PasspointManager have not been initialized yet");
            return;
        }
        Iterator<PasspointProvider> it = providers.iterator();
        while (it.hasNext()) {
            Log.d(TAG, "addProviders : " + it.next());
        }
        sPasspointManager.addPasspointProviders(providers);
    }

    private void addPasspointProviders(List<PasspointProvider> providers) {
        for (PasspointProvider provider : providers) {
            this.mProviders.put(provider.getConfig().getHomeSp().getFqdn(), provider);
            this.mPreconfiguredProviders.put(provider.getConfig().getHomeSp().getFqdn(), provider);
        }
    }

    public void sweepCache() {
        this.mAnqpCache.sweep();
    }

    public void notifyANQPDone(AnqpEvent anqpEvent) {
        this.mPasspointEventHandler.notifyANQPDone(anqpEvent);
    }

    public void notifyIconDone(IconEvent iconEvent) {
        this.mPasspointEventHandler.notifyIconDone(iconEvent);
    }

    public void receivedWnmFrame(WnmData data) {
        this.mPasspointEventHandler.notifyWnmFrameReceived(data);
    }

    public boolean queryPasspointIcon(long bssid, String fileName) {
        return this.mPasspointEventHandler.requestIcon(bssid, fileName);
    }

    public Map<Constants.ANQPElementType, ANQPElement> getANQPElements(ScanResult scanResult) {
        InformationElementUtil.Vsa vsa = InformationElementUtil.getHS2VendorSpecificIE(scanResult.informationElements);
        try {
            ANQPData anqpEntry = this.mAnqpCache.getEntry(ANQPNetworkKey.buildKey(scanResult.SSID, Utils.parseMac(scanResult.BSSID), scanResult.hessid, vsa.anqpDomainID));
            if (anqpEntry != null) {
                return anqpEntry.getElements();
            }
            return new HashMap();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid BSSID provided in the scan result: " + StringUtilEx.safeDisplayBssid(scanResult.BSSID));
            return new HashMap();
        }
    }

    public Map<String, Map<Integer, List<ScanResult>>> getAllMatchingFqdnsForScanResults(List<ScanResult> scanResults) {
        if (scanResults == null) {
            Log.e(TAG, "Attempt to get matching config for a null ScanResults");
            return new HashMap();
        }
        Map<String, Map<Integer, List<ScanResult>>> configs = new HashMap<>();
        for (ScanResult scanResult : scanResults) {
            if (scanResult == null) {
                Log.e(TAG, "Attempt to get matching config for a null scanResult");
                return new HashMap();
            } else if (scanResult.isPasspointNetwork()) {
                for (Pair<PasspointProvider, PasspointMatch> matchedProvider : getAllMatchedProviders(scanResult)) {
                    WifiConfiguration config = ((PasspointProvider) matchedProvider.first).getWifiConfig();
                    int type = 0;
                    if (!config.isHomeProviderNetwork) {
                        type = 1;
                    }
                    Map<Integer, List<ScanResult>> scanResultsPerNetworkType = configs.get(config.FQDN);
                    if (scanResultsPerNetworkType == null) {
                        scanResultsPerNetworkType = new HashMap<>();
                        configs.put(config.FQDN, scanResultsPerNetworkType);
                    }
                    List<ScanResult> matchingScanResults = scanResultsPerNetworkType.get(Integer.valueOf(type));
                    if (matchingScanResults == null) {
                        matchingScanResults = new ArrayList<>();
                        scanResultsPerNetworkType.put(Integer.valueOf(type), matchingScanResults);
                    }
                    matchingScanResults.add(scanResult);
                }
            }
        }
        return configs;
    }

    public Map<OsuProvider, List<ScanResult>> getMatchingOsuProviders(List<ScanResult> scanResults) {
        if (scanResults == null) {
            Log.e(TAG, "Attempt to retrieve OSU providers for a null ScanResult");
            return new HashMap();
        }
        Map<OsuProvider, List<ScanResult>> osuProviders = new HashMap<>();
        for (ScanResult scanResult : scanResults) {
            if (scanResult == null) {
                Log.e(TAG, "Attempt to retrieve OSU providers for a null scanResult");
                return new HashMap();
            } else if (scanResult.isPasspointNetwork()) {
                Map<Constants.ANQPElementType, ANQPElement> anqpElements = getANQPElements(scanResult);
                if (anqpElements.containsKey(Constants.ANQPElementType.HSOSUProviders)) {
                    for (OsuProviderInfo info : ((HSOsuProvidersElement) anqpElements.get(Constants.ANQPElementType.HSOSUProviders)).getProviders()) {
                        OsuProvider provider = new OsuProvider((WifiSsid) null, info.getFriendlyNames(), info.getServiceDescription(), info.getServerUri(), info.getNetworkAccessIdentifier(), info.getMethodList(), (Icon) null);
                        List<ScanResult> matchingScanResults = osuProviders.get(provider);
                        if (matchingScanResults == null) {
                            matchingScanResults = new ArrayList<>();
                            osuProviders.put(provider, matchingScanResults);
                        }
                        matchingScanResults.add(scanResult);
                    }
                }
            }
        }
        return osuProviders;
    }

    public Map<OsuProvider, PasspointConfiguration> getMatchingPasspointConfigsForOsuProviders(List<OsuProvider> osuProviders) {
        String osuFriendlyName;
        if (osuProviders == null) {
            Log.e(TAG, "Attempt to retrieve PasspointConfigurations for a null osuProviders");
            return new HashMap();
        }
        Map<OsuProvider, PasspointConfiguration> matchingPasspointConfigs = new HashMap<>();
        List<PasspointConfiguration> passpointConfigurations = getProviderConfigs();
        for (OsuProvider osuProvider : osuProviders) {
            if (osuProvider != null) {
                Map<String, String> friendlyNamesForOsuProvider = osuProvider.getFriendlyNameList();
                if (friendlyNamesForOsuProvider != null) {
                    for (PasspointConfiguration passpointConfiguration : passpointConfigurations) {
                        Map<String, String> serviceFriendlyNamesForPpsMo = passpointConfiguration.getServiceFriendlyNames();
                        if (serviceFriendlyNamesForPpsMo != null) {
                            Iterator<Map.Entry<String, String>> it = serviceFriendlyNamesForPpsMo.entrySet().iterator();
                            while (true) {
                                if (!it.hasNext()) {
                                    break;
                                }
                                Map.Entry<String, String> entry = it.next();
                                String lang = entry.getKey();
                                String friendlyName = entry.getValue();
                                if (!(friendlyName == null || (osuFriendlyName = friendlyNamesForOsuProvider.get(lang)) == null || !friendlyName.equals(osuFriendlyName))) {
                                    matchingPasspointConfigs.put(osuProvider, passpointConfiguration);
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                Log.e(TAG, "Attempt to retrieve PasspointConfigurations for a null osuProvider");
                return new HashMap();
            }
        }
        return matchingPasspointConfigs;
    }

    public List<WifiConfiguration> getWifiConfigsForPasspointProfiles(List<String> fqdnList) {
        Set<String> fqdnSet = new HashSet<>();
        fqdnSet.addAll(fqdnList);
        List<WifiConfiguration> configs = new ArrayList<>();
        for (String fqdn : fqdnSet) {
            PasspointProvider provider = this.mProviders.get(fqdn);
            if (provider != null) {
                configs.add(provider.getWifiConfig());
            }
        }
        return configs;
    }

    public void onPasspointNetworkConnected(String fqdn) {
        PasspointProvider provider = this.mProviders.get(fqdn);
        if (provider == null) {
            Log.e(TAG, "Passpoint network connected without provider: " + fqdn);
        } else if (!provider.getHasEverConnected()) {
            provider.setHasEverConnected(true);
        }
    }

    public void updateMetrics() {
        int numProviders = this.mProviders.size();
        int numConnectedProviders = 0;
        for (Map.Entry<String, PasspointProvider> entry : this.mProviders.entrySet()) {
            if (entry.getValue().getHasEverConnected()) {
                numConnectedProviders++;
            }
        }
        this.mWifiMetrics.updateSavedPasspointProfilesInfo(this.mProviders);
        this.mWifiMetrics.updateSavedPasspointProfiles(numProviders, numConnectedProviders);
    }

    public void dump(PrintWriter pw) {
        pw.println("Dump of PasspointManager");
        pw.println("PasspointManager - Providers Begin ---");
        for (Map.Entry<String, PasspointProvider> entry : this.mProviders.entrySet()) {
            pw.println(entry.getValue());
        }
        pw.println("PasspointManager - Providers End ---");
        pw.println("PasspointManager - Next provider ID to be assigned " + this.mProviderIndex);
        this.mAnqpCache.dump(pw);
    }

    private boolean addWifiConfig(WifiConfiguration wifiConfig) {
        PasspointConfiguration passpointConfig;
        if (wifiConfig == null || (passpointConfig = PasspointProvider.convertFromWifiConfig(wifiConfig)) == null) {
            return false;
        }
        WifiEnterpriseConfig enterpriseConfig = wifiConfig.enterpriseConfig;
        String caCertificateAliasSuffix = enterpriseConfig.getCaCertificateAlias();
        String clientCertAndKeyAliasSuffix = enterpriseConfig.getClientCertificateAlias();
        if (passpointConfig.getCredential().getUserCredential() == null || !TextUtils.isEmpty(caCertificateAliasSuffix)) {
            if (passpointConfig.getCredential().getCertCredential() != null) {
                if (TextUtils.isEmpty(caCertificateAliasSuffix)) {
                    Log.e(TAG, "Missing CA certificate for Certificate credential");
                    return false;
                } else if (TextUtils.isEmpty(clientCertAndKeyAliasSuffix)) {
                    Log.e(TAG, "Missing client certificate and key for certificate credential");
                    return false;
                }
            }
            WifiKeyStore wifiKeyStore = this.mKeyStore;
            SIMAccessor sIMAccessor = this.mSimAccessor;
            long j = this.mProviderIndex;
            this.mProviderIndex = 1 + j;
            this.mProviders.put(passpointConfig.getHomeSp().getFqdn(), new PasspointProvider(passpointConfig, wifiKeyStore, sIMAccessor, j, wifiConfig.creatorUid, null, Arrays.asList(enterpriseConfig.getCaCertificateAlias()), enterpriseConfig.getClientCertificateAlias(), enterpriseConfig.getClientCertificateAlias(), null, false, false));
            return true;
        }
        Log.e(TAG, "Missing CA Certificate for user credential");
        return false;
    }

    public boolean startSubscriptionProvisioning(int callingUid, OsuProvider provider, IProvisioningCallback callback) {
        return this.mPasspointProvisioner.startSubscriptionProvisioning(callingUid, provider, callback);
    }
}
