package com.android.server.wifi.hotspot2;

import android.net.wifi.hotspot2.PasspointConfiguration;
import com.android.server.wifi.Clock;
import com.android.server.wifi.SIMAccessor;
import com.android.server.wifi.WifiKeyStore;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hotspot2.PasspointConfigStoreData.DataSource;
import com.android.server.wifi.hotspot2.PasspointEventHandler.Callbacks;

public class PasspointObjectFactory {
    public PasspointEventHandler makePasspointEventHandler(WifiNative wifiNative, Callbacks callbacks) {
        return new PasspointEventHandler(wifiNative, callbacks);
    }

    public PasspointProvider makePasspointProvider(PasspointConfiguration config, WifiKeyStore keyStore, SIMAccessor simAccessor, long providerId, int creatorUid) {
        return new PasspointProvider(config, keyStore, simAccessor, providerId, creatorUid);
    }

    public PasspointConfigStoreData makePasspointConfigStoreData(WifiKeyStore keyStore, SIMAccessor simAccessor, DataSource dataSource) {
        return new CustPasspointConfigStoreData(keyStore, simAccessor, dataSource);
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
}
