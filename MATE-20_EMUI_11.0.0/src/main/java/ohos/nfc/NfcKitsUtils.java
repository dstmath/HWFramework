package ohos.nfc;

import ohos.utils.Parcel;

public final class NfcKitsUtils {
    public static final String ADAPTER_DESCRIPTOR = "android.nfc.INfcAdapter";
    public static final int BIND_SE_SERVICE = 13;
    public static final String CALLBACK_DESCRIPTOR = "com.nxp.nfc.ISecureElementCallback";
    public static final int CAN_SET_READONLY = 35;
    public static final String CARD_EMULATION_DESCRIPTOR = "android.nfc.INfcCardEmulation";
    public static final String CHANNEL_DESCRIPTOR = "android.se.omapi.ISecureElementChannel";
    public static final int CLOSE_CHANNEL = 19;
    public static final int CLOSE_SESSION_CHANNELS = 28;
    public static final int CLOSE_SE_SESSIONS = 27;
    public static final int GET_AIDS = 41;
    public static final int GET_ATR = 29;
    public static final int GET_INFO_FROM_CONFIG = 24;
    public static final int GET_NFC_INFO = 22;
    public static final int GET_NXP_SERVICE = 12;
    public static final int GET_SECURE_ELEMENTS = 15;
    public static final int GET_SELECT_RESPONSE = 25;
    public static final int IS_CHANNEL_CLOSED = 30;
    public static final int IS_DEFAULT_FOR_AID = 38;
    public static final int IS_LISTEN_MODE_ENABLED = 21;
    public static final int IS_NDEF = 31;
    public static final int IS_SE_PRESENT = 16;
    public static final int IS_SE_SERVICE_CONNECTED = 14;
    public static final int MSG_CONNECT_TAG = 4;
    public static final int MSG_GET_MAX_SEND_LENGTH = 10;
    public static final int MSG_GET_NFC_STATE = 2;
    public static final int MSG_GET_SEND_DATA_TIMEOUT = 8;
    public static final int MSG_IS_NFC_AVAILABLE = 3;
    public static final int MSG_IS_TAG_CONNECT = 6;
    public static final int MSG_RECONNECT_TAG = 5;
    public static final int MSG_RESET_DATA_TIMEOUT = 11;
    public static final int MSG_SEND_RAW_APDU_DATA = 9;
    public static final int MSG_SET_NFC_ENABLED = 1;
    public static final int MSG_SET_SEND_DATA_TIMEOUT = 7;
    public static final int NDEF_READ = 32;
    public static final int NDEF_SET_READONLY = 34;
    public static final int NDEF_WRITE = 33;
    public static final int NFC_DOMAIN_ID = 218109296;
    public static final String NXP_ADAPTER_DESCRIPTOR = "com.nxp.nfc.INxpNfcAdapter";
    public static final int OPEN_CHANNEL = 17;
    public static final int OPEN_SESSION = 26;
    public static final String READER_DESCRIPTOR = "android.se.omapi.ISecureElementReader";
    public static final int REGISTER_AIDS = 39;
    public static final int REMOVE_AIDS = 40;
    public static final String SESSION_DESCRIPTOR = "android.se.omapi.ISecureElementSession";
    public static final int SET_FOREGROUND_SERVICE = 36;
    public static final int SET_LISTEN_MODE = 20;
    public static final int SET_RF_CONFIG = 23;
    public static final String SE_SERVICE_DESCRIPTOR = "android.se.omapi.ISecureElementService";
    public static final int STRICTPOLICY = 1;
    public static final String TAG_DESCRIPTOR = "android.nfc.INfcTag";
    public static final int TRANSMIT_APDU = 18;
    public static final int TRANS_SYNC = 0;
    public static final int UNSET_FOREGROUND_SERVICE = 37;
    public static final int WORKSOURCE = 1;

    public static void writeInterfaceToken(String str, Parcel parcel) {
        parcel.writeInt(1);
        parcel.writeInt(1);
        parcel.writeString(str);
    }
}
