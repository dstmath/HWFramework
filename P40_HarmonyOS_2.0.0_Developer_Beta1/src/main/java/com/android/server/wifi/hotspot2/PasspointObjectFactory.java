package com.android.server.wifi.hotspot2;

import android.content.Context;
import android.net.wifi.hotspot2.PasspointConfiguration;
import com.android.org.conscrypt.TrustManagerImpl;
import com.android.server.wifi.Clock;
import com.android.server.wifi.SIMAccessor;
import com.android.server.wifi.WifiKeyStore;
import com.android.server.wifi.WifiMetrics;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hotspot2.PasspointConfigSharedStoreData;
import com.android.server.wifi.hotspot2.PasspointConfigUserStoreData;
import com.android.server.wifi.hotspot2.PasspointEventHandler;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;

public class PasspointObjectFactory {
    public PasspointEventHandler makePasspointEventHandler(WifiNative wifiNative, PasspointEventHandler.Callbacks callbacks) {
        return new PasspointEventHandler(wifiNative, callbacks);
    }

    public PasspointProvider makePasspointProvider(PasspointConfiguration config, WifiKeyStore keyStore, SIMAccessor simAccessor, long providerId, int creatorUid, String packageName) {
        return new PasspointProvider(config, keyStore, simAccessor, providerId, creatorUid, packageName);
    }

    public PasspointConfigUserStoreData makePasspointConfigUserStoreData(WifiKeyStore keyStore, SIMAccessor simAccessor, PasspointConfigUserStoreData.DataSource dataSource) {
        return new CustPasspointConfigStoreData(keyStore, simAccessor, dataSource);
    }

    public PasspointConfigSharedStoreData makePasspointConfigSharedStoreData(PasspointConfigSharedStoreData.DataSource dataSource) {
        return new PasspointConfigSharedStoreData(dataSource);
    }

    public AnqpCache makeAnqpCache(Clock clock) {
        return new AnqpCache(clock);
    }

    public ANQPRequestManager makeANQPRequestManager(PasspointEventHandler handler, Clock clock) {
        return new ANQPRequestManager(handler, clock);
    }

    public CertificateVerifier makeCertificateVerifier() {
        return new CertificateVerifier();
    }

    public PasspointProvisioner makePasspointProvisioner(Context context, WifiNative wifiNative, PasspointManager passpointManager, WifiMetrics wifiMetrics) {
        return new PasspointProvisioner(context, wifiNative, this, passpointManager, wifiMetrics);
    }

    public OsuNetworkConnection makeOsuNetworkConnection(Context context) {
        return new OsuNetworkConnection(context);
    }

    public OsuServerConnection makeOsuServerConnection() {
        return new OsuServerConnection(null);
    }

    public WfaKeyStore makeWfaKeyStore() {
        return new WfaKeyStore();
    }

    public SSLContext getSSLContext(String tlsVersion) {
        try {
            return SSLContext.getInstance(tlsVersion);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public TrustManagerImpl getTrustManagerImpl(KeyStore ks) {
        return new TrustManagerImpl(ks);
    }

    public SystemInfo getSystemInfo(Context context, WifiNative wifiNative) {
        return SystemInfo.getInstance(context, wifiNative);
    }
}
