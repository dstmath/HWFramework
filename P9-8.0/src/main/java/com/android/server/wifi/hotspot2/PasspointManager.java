package com.android.server.wifi.hotspot2;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.server.wifi.Clock;
import com.android.server.wifi.SIMAccessor;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.WifiConfigStore;
import com.android.server.wifi.WifiKeyStore;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hotspot2.NetworkDetail.HSRelease;
import com.android.server.wifi.hotspot2.PasspointConfigStoreData.DataSource;
import com.android.server.wifi.hotspot2.PasspointEventHandler.Callbacks;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants.ANQPElementType;
import com.android.server.wifi.hotspot2.anqp.RawByteElement;
import com.android.server.wifi.util.InformationElementUtil;
import com.android.server.wifi.util.InformationElementUtil.RoamingConsortium;
import com.android.server.wifi.util.InformationElementUtil.Vsa;
import com.android.server.wifi.util.ScanResultUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PasspointManager {
    private static final String TAG = "PasspointManager";
    private static PasspointManager sPasspointManager;
    private final AnqpCache mAnqpCache;
    private final ANQPRequestManager mAnqpRequestManager;
    private final CertificateVerifier mCertVerifier;
    private final PasspointEventHandler mHandler;
    private final WifiKeyStore mKeyStore;
    private final PasspointObjectFactory mObjectFactory;
    private long mProviderIndex;
    private final Map<String, PasspointProvider> mProviders = new HashMap();
    private final SIMAccessor mSimAccessor;
    private final WifiConfigManager mWifiConfigManager;

    private class CallbackHandler implements Callbacks {
        private final Context mContext;

        CallbackHandler(Context context) {
            this.mContext = context;
        }

        public void onANQPResponse(long bssid, Map<ANQPElementType, ANQPElement> anqpElements) {
            ANQPNetworkKey anqpKey = PasspointManager.this.mAnqpRequestManager.onRequestCompleted(bssid, anqpElements != null);
            if (anqpElements != null && anqpKey != null) {
                PasspointManager.this.mAnqpCache.addEntry(anqpKey, anqpElements);
                if (anqpElements.containsKey(ANQPElementType.HSOSUProviders)) {
                    RawByteElement osuProviders = (RawByteElement) anqpElements.get(ANQPElementType.HSOSUProviders);
                    Intent intent = new Intent("android.net.wifi.action.PASSPOINT_OSU_PROVIDERS_LIST");
                    intent.addFlags(67108864);
                    intent.putExtra("android.net.wifi.extra.BSSID_LONG", bssid);
                    intent.putExtra("android.net.wifi.extra.ANQP_ELEMENT_DATA", osuProviders.getPayload());
                    this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.ACCESS_WIFI_STATE");
                }
            }
        }

        public void onIconResponse(long bssid, String fileName, byte[] data) {
            Intent intent = new Intent("android.net.wifi.action.PASSPOINT_ICON");
            intent.addFlags(67108864);
            intent.putExtra("android.net.wifi.extra.BSSID_LONG", bssid);
            intent.putExtra("android.net.wifi.extra.FILENAME", fileName);
            if (data != null) {
                intent.putExtra("android.net.wifi.extra.ICON", Icon.createWithData(data, 0, data.length));
            }
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.ACCESS_WIFI_STATE");
        }

        public void onWnmFrameReceived(WnmData event) {
            Intent intent;
            if (event.isDeauthEvent()) {
                intent = new Intent("android.net.wifi.action.PASSPOINT_DEAUTH_IMMINENT");
                intent.addFlags(67108864);
                intent.putExtra("android.net.wifi.extra.BSSID_LONG", event.getBssid());
                intent.putExtra("android.net.wifi.extra.URL", event.getUrl());
                intent.putExtra("android.net.wifi.extra.ESS", event.isEss());
                intent.putExtra("android.net.wifi.extra.DELAY", event.getDelay());
            } else {
                intent = new Intent("android.net.wifi.action.PASSPOINT_SUBSCRIPTION_REMEDIATION");
                intent.addFlags(67108864);
                intent.putExtra("android.net.wifi.extra.BSSID_LONG", event.getBssid());
                intent.putExtra("android.net.wifi.extra.SUBSCRIPTION_REMEDIATION_METHOD", event.getMethod());
                intent.putExtra("android.net.wifi.extra.URL", event.getUrl());
            }
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.ACCESS_WIFI_STATE");
        }
    }

    private class DataSourceHandler implements DataSource {
        /* synthetic */ DataSourceHandler(PasspointManager this$0, DataSourceHandler -this1) {
            this();
        }

        private DataSourceHandler() {
        }

        public List<PasspointProvider> getProviders() {
            List<PasspointProvider> providers = new ArrayList();
            for (Entry<String, PasspointProvider> entry : PasspointManager.this.mProviders.entrySet()) {
                providers.add((PasspointProvider) entry.getValue());
            }
            return providers;
        }

        public void setProviders(List<PasspointProvider> providers) {
            PasspointManager.this.mProviders.clear();
            for (PasspointProvider provider : providers) {
                PasspointManager.this.mProviders.put(provider.getConfig().getHomeSp().getFqdn(), provider);
            }
        }

        public long getProviderIndex() {
            return PasspointManager.this.mProviderIndex;
        }

        public void setProviderIndex(long providerIndex) {
            PasspointManager.this.mProviderIndex = providerIndex;
        }
    }

    public PasspointManager(Context context, WifiNative wifiNative, WifiKeyStore keyStore, Clock clock, SIMAccessor simAccessor, PasspointObjectFactory objectFactory, WifiConfigManager wifiConfigManager, WifiConfigStore wifiConfigStore) {
        this.mHandler = objectFactory.makePasspointEventHandler(wifiNative, new CallbackHandler(context));
        this.mKeyStore = keyStore;
        this.mSimAccessor = simAccessor;
        this.mObjectFactory = objectFactory;
        this.mAnqpCache = objectFactory.makeAnqpCache(clock);
        this.mAnqpRequestManager = objectFactory.makeANQPRequestManager(this.mHandler, clock);
        this.mCertVerifier = objectFactory.makeCertificateVerifier();
        this.mWifiConfigManager = wifiConfigManager;
        this.mProviderIndex = 0;
        wifiConfigStore.registerStoreData(objectFactory.makePasspointConfigStoreData(this.mKeyStore, this.mSimAccessor, new DataSourceHandler(this, null)));
        sPasspointManager = this;
    }

    public boolean addOrUpdateProvider(PasspointConfiguration config, int uid) {
        if (config == null) {
            Log.e(TAG, "Configuration not provided");
            return false;
        } else if (config.validate()) {
            if (config.getUpdateIdentifier() == Integer.MIN_VALUE && config.getCredential().getCaCertificate() != null) {
                try {
                    this.mCertVerifier.verifyCaCert(config.getCredential().getCaCertificate());
                } catch (GeneralSecurityException e) {
                    Log.e(TAG, "GeneralSecurityException Failed to verify CA certificate: " + e.getMessage());
                    return false;
                } catch (IOException e2) {
                    Log.e(TAG, "IOException Failed to verify CA certificate: " + e2.getMessage());
                    return false;
                } catch (Exception e3) {
                    Log.e(TAG, "Failed to verify CA certificate: " + e3.getMessage());
                    return false;
                }
            }
            PasspointObjectFactory passpointObjectFactory = this.mObjectFactory;
            WifiKeyStore wifiKeyStore = this.mKeyStore;
            SIMAccessor sIMAccessor = this.mSimAccessor;
            long j = this.mProviderIndex;
            this.mProviderIndex = 1 + j;
            PasspointProvider newProvider = passpointObjectFactory.makePasspointProvider(config, wifiKeyStore, sIMAccessor, j, uid);
            if (newProvider.installCertsAndKeys()) {
                if (this.mProviders.containsKey(config.getHomeSp().getFqdn())) {
                    Log.d(TAG, "Replacing configuration for " + config.getHomeSp().getFqdn());
                    ((PasspointProvider) this.mProviders.get(config.getHomeSp().getFqdn())).uninstallCertsAndKeys();
                    this.mProviders.remove(config.getHomeSp().getFqdn());
                }
                this.mProviders.put(config.getHomeSp().getFqdn(), newProvider);
                this.mWifiConfigManager.saveToStore(true);
                Log.d(TAG, "Added/updated Passpoint configuration: " + config.getHomeSp().getFqdn() + " by " + uid);
                return true;
            }
            Log.e(TAG, "Failed to install certificates and keys to keystore");
            return false;
        } else {
            Log.e(TAG, "Invalid configuration");
            return false;
        }
    }

    public boolean removeProvider(String fqdn) {
        if (this.mProviders.containsKey(fqdn)) {
            ((PasspointProvider) this.mProviders.get(fqdn)).uninstallCertsAndKeys();
            this.mProviders.remove(fqdn);
            this.mWifiConfigManager.saveToStore(true);
            Log.d(TAG, "Removed Passpoint configuration: " + fqdn);
            return true;
        }
        Log.e(TAG, "Config doesn't exist");
        return false;
    }

    public List<PasspointConfiguration> getProviderConfigs() {
        List<PasspointConfiguration> configs = new ArrayList();
        for (Entry<String, PasspointProvider> entry : this.mProviders.entrySet()) {
            configs.add(((PasspointProvider) entry.getValue()).getConfig());
        }
        return configs;
    }

    public Pair<PasspointProvider, PasspointMatch> matchProvider(ScanResult scanResult) {
        RoamingConsortium roamingConsortium = InformationElementUtil.getRoamingConsortiumIE(scanResult.informationElements);
        Vsa vsa = InformationElementUtil.getHS2VendorSpecificIE(scanResult.informationElements);
        long bssid = Utils.parseMac(scanResult.BSSID);
        ANQPNetworkKey anqpKey = ANQPNetworkKey.buildKey(scanResult.SSID, bssid, scanResult.hessid, vsa.anqpDomainID);
        ANQPData anqpEntry = this.mAnqpCache.getEntry(anqpKey);
        if (anqpEntry == null) {
            this.mAnqpRequestManager.requestANQPElements(bssid, anqpKey, roamingConsortium.anqpOICount > 0, vsa.hsRelease == HSRelease.R2);
            Log.d(TAG, "ANQP entry not found for: " + anqpKey);
            return null;
        }
        Pair<PasspointProvider, PasspointMatch> bestMatch = null;
        for (Entry<String, PasspointProvider> entry : this.mProviders.entrySet()) {
            PasspointProvider provider = (PasspointProvider) entry.getValue();
            PasspointMatch matchStatus = provider.match(anqpEntry.getElements());
            if (matchStatus == PasspointMatch.HomeProvider) {
                bestMatch = Pair.create(provider, matchStatus);
                break;
            } else if (matchStatus == PasspointMatch.RoamingProvider && bestMatch == null) {
                bestMatch = Pair.create(provider, matchStatus);
            }
        }
        if (bestMatch != null) {
            String str;
            String str2 = TAG;
            String str3 = "Matched %s to %s as %s";
            String[] strArr = new Object[3];
            strArr[0] = scanResult.SSID;
            strArr[1] = ((PasspointProvider) bestMatch.first).getConfig().getHomeSp().getFqdn();
            if (bestMatch.second == PasspointMatch.HomeProvider) {
                str = "Home Provider";
            } else {
                str = "Roaming Provider";
            }
            strArr[2] = str;
            Log.d(str2, String.format(str3, strArr));
        } else {
            Log.d(TAG, "Match not found for " + scanResult.SSID);
        }
        return bestMatch;
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
        for (PasspointProvider provider : providers) {
            Log.d(TAG, "addProviders : " + provider);
        }
        sPasspointManager.addPasspointProviders(providers);
    }

    private void addPasspointProviders(List<PasspointProvider> providers) {
        for (PasspointProvider provider : providers) {
            this.mProviders.put(provider.getConfig().getHomeSp().getFqdn(), provider);
        }
    }

    public void sweepCache() {
        this.mAnqpCache.sweep();
    }

    public void notifyANQPDone(AnqpEvent anqpEvent) {
        this.mHandler.notifyANQPDone(anqpEvent);
    }

    public void notifyIconDone(IconEvent iconEvent) {
        this.mHandler.notifyIconDone(iconEvent);
    }

    public void receivedWnmFrame(WnmData data) {
        this.mHandler.notifyWnmFrameReceived(data);
    }

    public boolean queryPasspointIcon(long bssid, String fileName) {
        return this.mHandler.requestIcon(bssid, fileName);
    }

    public Map<ANQPElementType, ANQPElement> getANQPElements(ScanResult scanResult) {
        Vsa vsa = InformationElementUtil.getHS2VendorSpecificIE(scanResult.informationElements);
        ANQPData anqpEntry = this.mAnqpCache.getEntry(ANQPNetworkKey.buildKey(scanResult.SSID, Utils.parseMac(scanResult.BSSID), scanResult.hessid, vsa.anqpDomainID));
        if (anqpEntry != null) {
            return anqpEntry.getElements();
        }
        return new HashMap();
    }

    public WifiConfiguration getMatchingWifiConfig(ScanResult scanResult) {
        if (scanResult == null || scanResult.BSSID == null) {
            Log.e(TAG, "Attempt to get matching config for a null ScanResult/BSSID");
            return null;
        }
        Pair<PasspointProvider, PasspointMatch> matchedProvider = matchProvider(scanResult);
        if (matchedProvider == null) {
            return null;
        }
        WifiConfiguration config = ((PasspointProvider) matchedProvider.first).getWifiConfig();
        config.SSID = ScanResultUtil.createQuotedSSID(scanResult.SSID);
        if (matchedProvider.second == PasspointMatch.HomeProvider) {
            config.isHomeProviderNetwork = true;
        }
        return config;
    }

    public void dump(PrintWriter pw) {
        pw.println("Dump of PasspointManager");
        pw.println("PasspointManager - Providers Begin ---");
        for (Entry<String, PasspointProvider> entry : this.mProviders.entrySet()) {
            pw.println(entry.getValue());
        }
        pw.println("PasspointManager - Providers End ---");
        pw.println("PasspointManager - Next provider ID to be assigned " + this.mProviderIndex);
        this.mAnqpCache.dump(pw);
    }

    private boolean addWifiConfig(WifiConfiguration wifiConfig) {
        if (wifiConfig == null) {
            return false;
        }
        PasspointConfiguration passpointConfig = PasspointProvider.convertFromWifiConfig(wifiConfig);
        if (passpointConfig == null) {
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
            this.mProviders.put(passpointConfig.getHomeSp().getFqdn(), new PasspointProvider(passpointConfig, wifiKeyStore, sIMAccessor, j, wifiConfig.creatorUid, enterpriseConfig.getCaCertificateAlias(), enterpriseConfig.getClientCertificateAlias(), enterpriseConfig.getClientCertificateAlias()));
            return true;
        }
        Log.e(TAG, "Missing CA Certificate for user credential");
        return false;
    }
}
