package com.android.server.wifi;

public class HwWifiCHRConstImpl implements HwWifiCHRConst {
    public static final int AKMP_NOT_VALID = 20;
    public static final String BCM_D11_RXFRAG = "rxfrag";
    public static final String BCM_D11_TXCTL = "txctl";
    public static final String BCM_D11_TXFRAG = "txfrag";
    public static final String BCM_D11_TXFRMSNT = "txfrmsnt";
    public static final String BCM_D11_TXNOACK = "txnoack";
    public static final String BCM_D11_TXNOASSOC = "txnoassoc";
    public static final String BCM_D11_TXNOCTS = "txnocts";
    public static final String BCM_D11_TXPHYERROR = "txphyerror";
    public static final String BCM_D11_TXRTS = "txrts";
    public static final String BCM_RESET = "reset";
    public static final String BCM_RXBADCM = "rxbadcm";
    public static final String BCM_RXBADDS = "rxbadds";
    public static final String BCM_RXBADFCS = "rxbadfcs";
    public static final String BCM_RXBADPLCP = "rxbadplcp";
    public static final String BCM_RXBADPROTO = "rxbadproto";
    public static final String BCM_RXBEACONMBSS = "rxbeaconmbss";
    public static final String BCM_RXBEACONOBSS = "rxbeaconobss";
    public static final String BCM_RXBYTE = "rxbyte";
    public static final String BCM_RXCFRMMCAST = "rxcfrmmcast";
    public static final String BCM_RXCFRMOCAST = "rxcfrmocast";
    public static final String BCM_RXCFRMUCAST = "rxcfrmucast";
    public static final String BCM_RXCRSGLITCH = "rxcrsglitch";
    public static final String BCM_RXCTL = "rxctl";
    public static final String BCM_RXDFRMMCAST = "rxdfrmmcast";
    public static final String BCM_RXDFRMOCAST = "rxdfrmocast";
    public static final String BCM_RXDFRMUCASTMBSS = "rxdfrmucastmbss";
    public static final String BCM_RXFRAME = "rxframe";
    public static final String BCM_RXMFRMMCAST = "rxmfrmmcast";
    public static final String BCM_RXMFRMOCAST = "rxmfrmocast";
    public static final String BCM_RXMFRMUCASTMBSS = "rxmfrmucastmbss";
    public static final String BCM_RXMULTI = "rxmulti";
    public static final String BCM_RXNOBUF = "rxnobuf";
    public static final String BCM_RXNODATA = "rxnondata";
    public static final String BCM_RXSTRT = "rxstrt";
    public static final String BCM_RXWDSFRAME = "rxdfmucastobss";
    public static final String BCM_TXALLFRM = "txallfrm";
    public static final String BCM_TXBYTE = "txbyte";
    public static final String BCM_TXERROR = "txerror";
    public static final String BCM_TXFAIL = "txfail";
    public static final String BCM_TXFRAME = "txframe";
    public static final String BCM_TXNOBUF = "txnobuf";
    public static final String BCM_TXPHYERR = "txphyerr";
    public static final String BCM_TXRETRANS = "txretrans";
    public static final int CIPHER_SUITE_REJECTED = 24;
    public static final int CLASS2_FRAME_FROM_NONAUTH_STA = 6;
    public static final int CLASS3_FRAME_FROM_NONASSOC_STA = 7;
    public static final int DEAUTH_STA_IS_LEFING = 3;
    public static final int DEFAULT_REASON_CODE = -10;
    public static final int DISASSOC_AP_BUSY = 5;
    public static final int DISASSOC_DUE_TO_INACTIVITY = 4;
    public static final int DISASSOC_LOW_ACK = 34;
    public static final int DISASSOC_STA_HAS_LEFT = 8;
    public static final int FOURWAY_HANDSHAKE_TIMEOUT = 15;
    public static final int GROUP_CIPHER_NOT_VALID = 18;
    public static final int GROUP_KEY_UPDATE_TIMEOUT = 16;
    public static final int IEEE_802_1X_AUTH_FAILED = 23;
    public static final int IE_IN_4WAY_DIFFERS = 17;
    public static final int INVALID_IE = 13;
    public static final int INVALID_MICHAEL_MIC_FAILURE = 14;
    public static final int INVALID_RSN_IE_CAPAB = 22;
    public static final int PAIRWISE_CIPHER_NOT_VALID = 19;
    public static final int PREV_AUTH_NOT_VALID = 2;
    public static final int PWR_CAPABILITY_NOT_VALID = 10;
    public static final int STA_REQ_ASSOC_WITHOUT_AUTH = 9;
    public static final int SUPPORTED_CHANNEL_NOT_VALID = 11;
    public static final int TDLS_TEARDOWN_UNREACHABLE = 25;
    public static final int TDLS_TEARDOWN_UNSPECIFIED = 26;
    public static final int UNSPECIFIED = 1;
    public static final int UNSUPPORTED_RSN_IE_VERSION = 21;
    public static final int WEAK_SIGNAL_THRESHOLD = -80;
    public static final int WIFI_ABNORMAL_DISCONNECT = 85;
    public static final int WIFI_ABNORMAL_DISCONNECT_EX = 95;
    public static final int WIFI_ACCESS_INTERNET_FAILED = 87;
    public static final int WIFI_ACCESS_INTERNET_FAILED_EX = 97;
    public static final int WIFI_ACCESS_WEB_SLOWLY = 102;
    public static final int WIFI_CLOSE_FAILED = 81;
    public static final int WIFI_CLOSE_FAILED_EX = 91;
    public static final int WIFI_CONNECT_ASSOC_FAILED = 83;
    public static final int WIFI_CONNECT_ASSOC_FAILED_EX = 93;
    public static final int WIFI_CONNECT_AUTH_FAILED = 82;
    public static final int WIFI_CONNECT_AUTH_FAILED_EX = 92;
    public static final int WIFI_CONNECT_DHCP_FAILED = 84;
    public static final int WIFI_CONNECT_DHCP_FAILED_EX = 94;
    public static final int WIFI_OPEN_FAILED = 80;
    public static final int WIFI_OPEN_FAILED_EX = 90;
    public static final int WIFI_POOR_LEVEL = 103;
    public static final int WIFI_PORTAL_AUTH_MSG_COLLECTE = 124;
    public static final int WIFI_PORTAL_SAMPLES_COLLECTE = 120;
    public static final int WIFI_SCAN_FAILED = 86;
    public static final int WIFI_SCAN_FAILED_EX = 96;
    public static final int WIFI_STABILITY_STAT = 110;
    public static final int WIFI_STATUS_CHANGEDBY_APK = 98;
    public static final int WIFI_USER_CONNECT = 101;
    public static final int WIFI_WIFIPRO_EXCEPTION_EVENT = 122;
    public static final int WIFI_WIFIPRO_STATISTICS_EVENT = 121;
    public static HwWifiCHRConst hwwcci = new HwWifiCHRConstImpl();

    public String getDisconnectReasonCode(int reasonValue) {
        String strReasonCode = "";
        switch (reasonValue) {
            case 2:
                return "PREV_AUTH_NOT_VALID";
            case 3:
                return "DEAUTH_STA_IS_LEFING";
            case 4:
                return "DISASSOC_DUE_TO_INACTIVITY";
            case 5:
                return "DISASSOC_AP_BUSY";
            case 6:
                return "CLASS2_FRAME_FROM_NONAUTH_STA";
            case 7:
                return "CLASS3_FRAME_FROM_NONASSOC_STA";
            case 8:
                return strReasonCode;
            case 9:
                return "STA_REQ_ASSOC_WITHOUT_AUTH";
            case 10:
                return "PWR_CAPABILITY_NOT_VALID";
            case 11:
                return "SUPPORTED_CHANNEL_NOT_VALID";
            case 13:
                return "INVALID_IE";
            case 14:
                return "INVALID_MICHAEL_MIC_FAILURE";
            case 15:
                return "FOURWAY_HANDSHAKE_TIMEOUT";
            case 16:
                return "GROUP_KEY_UPDATE_TIMEOUT";
            case 17:
                return "IE_IN_4WAY_DIFFERS";
            case 18:
                return "GROUP_CIPHER_NOT_VALID";
            case 19:
                return "PAIRWISE_CIPHER_NOT_VALID";
            case 20:
                return "AKMP_NOT_VALID";
            case 21:
                return "UNSUPPORTED_RSN_IE_VERSION";
            case 22:
                return "INVALID_RSN_IE_CAPAB";
            case 23:
                return "IEEE_802_1X_AUTH_FAILED";
            case 24:
                return "CIPHER_SUITE_REJECTED";
            case 25:
                return "TDLS_TEARDOWN_UNREACHABLE";
            case 26:
                return "TDLS_TEARDOWN_UNSPECIFIED";
            case 34:
                return "DISASSOC_LOW_ACK";
            default:
                if (reasonValue > 0) {
                    return "UNSPECIFIED";
                }
                return strReasonCode;
        }
    }

    private HwWifiCHRConstImpl() {
    }

    public static HwWifiCHRConst getDefault() {
        return hwwcci;
    }
}
