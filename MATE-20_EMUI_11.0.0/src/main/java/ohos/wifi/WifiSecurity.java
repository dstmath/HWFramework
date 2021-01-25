package ohos.wifi;

import ohos.annotation.SystemApi;

public final class WifiSecurity {
    public static final int EAP = 3;
    public static final int EAP_SUITE_B = 5;
    public static final int INVALID = -1;
    public static final int OPEN = 0;
    public static final int OWE = 6;
    public static final int PSK = 2;
    public static final int SAE = 4;
    @SystemApi
    public static final int WAPI_CERT = 7;
    @SystemApi
    public static final int WAPI_PSK = 8;
    public static final int WEP = 1;

    public enum EapMethod {
        NONE,
        PEAP,
        TLS,
        TTLS,
        PWD,
        SIM,
        AKA,
        AKA_PRIME,
        UNAUTH_TLS
    }

    public enum Phase2Method {
        NONE,
        PAP,
        MSCHAP,
        MSCHAPV2,
        GTC,
        SIM,
        AKA,
        AKA_PRIME
    }

    @SystemApi
    public static final class Wapi {
        public static final int PSK_ASCII = 0;
        public static final int PSK_HEX = 1;
    }
}
