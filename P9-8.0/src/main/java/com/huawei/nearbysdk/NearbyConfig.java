package com.huawei.nearbysdk;

public class NearbyConfig {
    public static final int AUTH_DOUBLE_CHANNEL = 2;
    public static final int AUTH_SINGLE_CHANNEL = 1;
    public static final int BUSINESSID_HWSHARE = 1;
    public static final int BUSINESSID_SYNERGY = 3;
    public static final int BUSINESSID_WIFISHARE = 2;
    public static final int DEFAULT_METHOD_ERROR_CODE = -1;
    public static final int ERROR_ADV_FAILED = 1006;
    public static final int ERROR_ALREADY_DONE = 1004;
    public static final int ERROR_BLUETOOTH_OFF = 1001;
    public static final int ERROR_CLIENT_CONNECT_FAILED = 4000;
    public static final int ERROR_CONTROL_CHANNEL_CONNECT_FAILED = 2001;
    public static final int ERROR_CONTROL_CHANNEL_DATA_EXCEPTION = 2002;
    public static final int ERROR_CREATE_THREAD_EXCEPTION = 3001;
    public static final int ERROR_DATA_SEND_FAILED = 2005;
    public static final int ERROR_DEVICE_CONNECT_FAILED = 2004;
    public static final int ERROR_INVALID_PARAMETERS = 1003;
    public static final int ERROR_LOCAL_DEVICE_BUSY = 2010;
    public static final int ERROR_MESSAGE_NOT_SUPPORTED = 4001;
    public static final int ERROR_MESSAGE_OPEN_NEARBY_SOCKET_TIMEOUT = 4003;
    public static final int ERROR_MESSAGE_SAME_BID_BTAG = 4002;
    public static final int ERROR_NONE_PUBLISH_AVAILABLE = 5000;
    public static final int ERROR_NOT_SUPPORTED = 2000;
    public static final int ERROR_NO_AVAILABLE_DATA_CHANNEL = 2003;
    public static final int ERROR_P2P_CONNECT_FAILED = 5100;
    public static final int ERROR_P2P_CONNECT_FAILED_LOCAL_BUSY = 5102;
    public static final int ERROR_P2P_CONNECT_FAILED_PEER_BUSY = 5101;
    public static final int ERROR_PEER_DEVICE_BUSY = 2009;
    public static final int ERROR_PUBLISH_CHANNEL_NOT_SUPPORTED = 5001;
    public static final int ERROR_PUBLISH_P2P_CONFLICT_AP_ENABLED = 5052;
    public static final int ERROR_PUBLISH_P2P_DISABLED = 5053;
    public static final int ERROR_PUBLISH_P2P_USED_BY_OTHER_APP = 5051;
    public static final int ERROR_PUBLISH_TIMEOUT = 5002;
    public static final int ERROR_REJECT = 2006;
    public static final int ERROR_REMOTE_DATA_EXCEPTION = 3000;
    public static final int ERROR_SAME_BID_BTAG = 2007;
    public static final int ERROR_SCAN_FAILED = 1005;
    public static final int ERROR_SCREEN_OFF = 1002;
    public static final int ERROR_TIMEOUT = 2008;
    public static final int ERROR_TIME_OUT = 3002;
    public static final int ERROR_TOO_MANY_BLE_ADVERTISE = 1007;
    public static final int ERROR_UNSUPPORT_CHANNLE = 3007;
    public static final int EVENT_CONNECTED = 2;
    public static final int HOTSPOT_VERSION = 0;
    public static final long INIT_AUTH_FAILD_CODE = -1;
    public static final long LISTENER_LOST_ERROR_CODE = -2;
    public static final int NEARBY_SESSION_CREATE_NO_TIME_OUT = -1;
    public static final int NONE_HOTSPOT_VERSION = -1;
    public static final int PROTOCOL_COAP = 4;
    public static final int PROTOCOL_TCP = 1;
    public static final int PROTOCOL_UDP = 2;
    public static final int SECURITY_AUTHENTICATION = 1;
    public static final int SECURITY_BT_PAIRING = 2;
    public static final int SECURITY_DEFAULT = 0;
    public static final int SECURITY_PRIVATE = 3;
    public static final int TRANS_BUFFER_MAX = 1024;
    public static final int TYPECHANNEL_AP = 5;
    public static final int TYPECHANNEL_BLE = 1;
    public static final int TYPECHANNEL_BR = 2;
    public static final int TYPECHANNEL_HWDL = 6;
    public static final int TYPECHANNEL_P2P = 3;
    public static final int TYPECHANNEL_WIFI = 4;

    public enum BusinessTypeEnum {
        AllType(0),
        Data(1),
        Token(2),
        InstantMessage(3),
        Streaming(4);
        
        private int iNum;

        private BusinessTypeEnum(int iNum) {
            this.iNum = 0;
            this.iNum = iNum;
        }

        public int toNumber() {
            return this.iNum;
        }
    }

    public static boolean isVaildSecurityType(int securityType) {
        switch (securityType) {
            case 0:
            case 1:
            case 2:
            case 3:
                return true;
            default:
                return false;
        }
    }
}
