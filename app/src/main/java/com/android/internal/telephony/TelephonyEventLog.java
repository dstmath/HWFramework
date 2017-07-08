package com.android.internal.telephony;

import android.net.ConnectivityMetricsLogger;
import android.os.Bundle;
import android.telephony.ServiceState;
import com.android.ims.ImsReasonInfo;
import com.android.ims.internal.ImsCallSession;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.dataconnection.DataCallResponse;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduPersister;
import java.util.ArrayList;

public class TelephonyEventLog extends ConnectivityMetricsLogger {
    private static final /* synthetic */ int[] -com-android-internal-telephony-Call$StateSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-internal-telephony-PhoneConstants$StateSwitchesValues = null;
    public static final String DATA_KEY_APN = "apn";
    public static final String DATA_KEY_CALLEE = "callee";
    public static final String DATA_KEY_CLIR_MODE = "clirMode";
    public static final String DATA_KEY_DATA_CALL_ACTIVE = "active";
    public static final String DATA_KEY_DATA_CALL_ACTIVES = "actives";
    public static final String DATA_KEY_DATA_CALL_CID = "cid";
    public static final String DATA_KEY_DATA_CALL_CIDS = "cids";
    public static final String DATA_KEY_DATA_CALL_IFNAME = "ifname";
    public static final String DATA_KEY_DATA_CALL_IFNAMES = "ifnames";
    public static final String DATA_KEY_DATA_CALL_RETRY = "retry";
    public static final String DATA_KEY_DATA_CALL_STATUS = "status";
    public static final String DATA_KEY_DATA_CALL_STATUSES = "statuses";
    public static final String DATA_KEY_DATA_CALL_TYPE = "type";
    public static final String DATA_KEY_DATA_CALL_TYPES = "types";
    public static final String DATA_KEY_DATA_DEACTIVATE_REASON = "reason";
    public static final String DATA_KEY_DATA_PROFILE = "profile";
    public static final String DATA_KEY_PARAM1 = "param1";
    public static final String DATA_KEY_PARAM2 = "param2";
    public static final String DATA_KEY_PARTICIPANTS = "participants";
    public static final String DATA_KEY_PHONE_ID = "phoneId";
    public static final String DATA_KEY_PROTOCOL = "protocol";
    public static final String DATA_KEY_RAT = "rat";
    public static final String DATA_KEY_REASONINFO_CODE = "code";
    public static final String DATA_KEY_REASONINFO_EXTRA_CODE = "extra-code";
    public static final String DATA_KEY_REASONINFO_EXTRA_MESSAGE = "extra-message";
    public static final String DATA_KEY_RIL_CALL_RING_RESPONSE = "response";
    public static final String DATA_KEY_RIL_ERROR = "error";
    public static final String DATA_KEY_RIL_HANGUP_GSM_INDEX = "gsmIndex";
    public static final String DATA_KEY_SMS_ACK_PDU = "ackPDU";
    public static final String DATA_KEY_SMS_ERROR_CODE = "errorCode";
    public static final String DATA_KEY_SMS_MESSAGE_REF = "messageRef";
    public static final String DATA_KEY_SRC_TECH = "src-tech";
    public static final String DATA_KEY_TARGET_TECH = "target-tech";
    public static final String DATA_KEY_UTLTE = "UTLTE";
    public static final String DATA_KEY_UTWIFI = "UTWiFi";
    public static final String DATA_KEY_VILTE = "ViLTE";
    public static final String DATA_KEY_VIWIFI = "ViWiFi";
    public static final String DATA_KEY_VOLTE = "VoLTE";
    public static final String DATA_KEY_VOWIFI = "VoWiFi";
    private static final boolean DBG = true;
    public static final int IMS_CONNECTION_STATE_CONNECTED = 1;
    public static final int IMS_CONNECTION_STATE_DISCONNECTED = 3;
    public static final int IMS_CONNECTION_STATE_PROGRESSING = 2;
    public static final int IMS_CONNECTION_STATE_RESUMED = 4;
    public static final int IMS_CONNECTION_STATE_SUSPENDED = 5;
    public static final String SERVICE_STATE_DATA_ALPHA_LONG = "dataAlphaLong";
    public static final String SERVICE_STATE_DATA_ALPNA_SHORT = "dataAlphaShort";
    public static final String SERVICE_STATE_DATA_NUMERIC = "dataOperator";
    public static final String SERVICE_STATE_DATA_RAT = "dataRat";
    public static final String SERVICE_STATE_DATA_REG_STATE = "dataRegSt";
    public static final String SERVICE_STATE_DATA_ROAMING_TYPE = "dataRoamingType";
    public static final String SERVICE_STATE_EMERGENCY_ONLY = "emergencyOnly";
    public static final String SERVICE_STATE_VOICE_ALPHA_LONG = "alphaLong";
    public static final String SERVICE_STATE_VOICE_ALPNA_SHORT = "alphaShort";
    public static final String SERVICE_STATE_VOICE_NUMERIC = "operator";
    public static final String SERVICE_STATE_VOICE_RAT = "rat";
    public static final String SERVICE_STATE_VOICE_REG_STATE = "regSt";
    public static final String SERVICE_STATE_VOICE_ROAMING_TYPE = "roamingType";
    public static final int SETTING_AIRPLANE_MODE = 1;
    public static final int SETTING_CELL_DATA_ENABLED = 2;
    public static final int SETTING_DATA_ROAMING_ENABLED = 3;
    public static final int SETTING_PREFERRED_NETWORK_MODE = 4;
    public static final int SETTING_VI_LTE_ENABLED = 9;
    public static final int SETTING_VI_WIFI_ENABLED = 10;
    public static final int SETTING_VO_LTE_ENABLED = 6;
    public static final int SETTING_VO_WIFI_ENABLED = 7;
    public static final int SETTING_WFC_MODE = 8;
    public static final int SETTING_WIFI_ENABLED = 5;
    private static String TAG = null;
    public static final int TAG_DATA_CALL_LIST = 5;
    public static final int TAG_IMS_CALL_ACCEPT = 2004;
    public static final int TAG_IMS_CALL_HANDOVER = 2025;
    public static final int TAG_IMS_CALL_HANDOVER_FAILED = 2026;
    public static final int TAG_IMS_CALL_HELD = 2015;
    public static final int TAG_IMS_CALL_HOLD = 2007;
    public static final int TAG_IMS_CALL_HOLD_FAILED = 2016;
    public static final int TAG_IMS_CALL_HOLD_RECEIVED = 2017;
    public static final int TAG_IMS_CALL_MERGE = 2009;
    public static final int TAG_IMS_CALL_MERGED = 2023;
    public static final int TAG_IMS_CALL_MERGE_FAILED = 2024;
    public static final int TAG_IMS_CALL_PROGRESSING = 2011;
    public static final int TAG_IMS_CALL_RECEIVE = 2003;
    public static final int TAG_IMS_CALL_REJECT = 2005;
    public static final int TAG_IMS_CALL_RESUME = 2008;
    public static final int TAG_IMS_CALL_RESUMED = 2018;
    public static final int TAG_IMS_CALL_RESUME_FAILED = 2019;
    public static final int TAG_IMS_CALL_RESUME_RECEIVED = 2020;
    public static final int TAG_IMS_CALL_START = 2001;
    public static final int TAG_IMS_CALL_STARTED = 2012;
    public static final int TAG_IMS_CALL_START_CONFERENCE = 2002;
    public static final int TAG_IMS_CALL_START_FAILED = 2013;
    public static final int TAG_IMS_CALL_STATE = 2030;
    public static final int TAG_IMS_CALL_TERMINATE = 2006;
    public static final int TAG_IMS_CALL_TERMINATED = 2014;
    public static final int TAG_IMS_CALL_TTY_MODE_RECEIVED = 2027;
    public static final int TAG_IMS_CALL_UPDATE = 2010;
    public static final int TAG_IMS_CALL_UPDATED = 2021;
    public static final int TAG_IMS_CALL_UPDATE_FAILED = 2022;
    public static final int TAG_IMS_CAPABILITIES = 4;
    public static final int TAG_IMS_CONFERENCE_PARTICIPANTS_STATE_CHANGED = 2028;
    public static final int TAG_IMS_CONNECTION_STATE = 3;
    public static final int TAG_IMS_MULTIPARTY_STATE_CHANGED = 2029;
    public static final int TAG_PHONE_STATE = 8;
    public static final int TAG_RIL_REQUEST = 1001;
    public static final int TAG_RIL_RESPONSE = 1002;
    public static final int TAG_RIL_TIMEOUT_RESPONSE = 1004;
    public static final int TAG_RIL_UNSOL_RESPONSE = 1003;
    public static final int TAG_SERVICE_STATE = 2;
    public static final int TAG_SETTINGS = 1;
    private static final boolean VDBG = false;
    private String mDataOperatorAlphaShort;
    private String mDataOperatorNumeric;
    private int mDataRegState;
    private int mDataRoamingType;
    private boolean mEmergencyOnly;
    private final boolean[] mImsCapabilities;
    int mPhoneId;
    private int mRilDataRadioTechnology;
    private int mRilVoiceRadioTechnology;
    private String mVoiceOperatorAlphaShort;
    private String mVoiceOperatorNumeric;
    private int mVoiceRegState;
    private int mVoiceRoamingType;

    private static /* synthetic */ int[] -getcom-android-internal-telephony-Call$StateSwitchesValues() {
        if (-com-android-internal-telephony-Call$StateSwitchesValues != null) {
            return -com-android-internal-telephony-Call$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.ACTIVE.ordinal()] = TAG_SETTINGS;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.ALERTING.ordinal()] = TAG_SERVICE_STATE;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DIALING.ordinal()] = TAG_IMS_CONNECTION_STATE;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.DISCONNECTED.ordinal()] = TAG_IMS_CAPABILITIES;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.DISCONNECTING.ordinal()] = TAG_DATA_CALL_LIST;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.HOLDING.ordinal()] = SETTING_VO_LTE_ENABLED;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.IDLE.ordinal()] = SETTING_VO_WIFI_ENABLED;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[State.INCOMING.ordinal()] = TAG_PHONE_STATE;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[State.WAITING.ordinal()] = SETTING_VI_LTE_ENABLED;
        } catch (NoSuchFieldError e9) {
        }
        -com-android-internal-telephony-Call$StateSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-PhoneConstants$StateSwitchesValues() {
        if (-com-android-internal-telephony-PhoneConstants$StateSwitchesValues != null) {
            return -com-android-internal-telephony-PhoneConstants$StateSwitchesValues;
        }
        int[] iArr = new int[PhoneConstants.State.values().length];
        try {
            iArr[PhoneConstants.State.IDLE.ordinal()] = TAG_SETTINGS;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PhoneConstants.State.OFFHOOK.ordinal()] = TAG_SERVICE_STATE;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PhoneConstants.State.RINGING.ordinal()] = TAG_IMS_CONNECTION_STATE;
        } catch (NoSuchFieldError e3) {
        }
        -com-android-internal-telephony-PhoneConstants$StateSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.TelephonyEventLog.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.TelephonyEventLog.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.TelephonyEventLog.<clinit>():void");
    }

    public TelephonyEventLog(int phoneId) {
        this.mVoiceRegState = -1;
        this.mDataRegState = -1;
        this.mVoiceRoamingType = -1;
        this.mDataRoamingType = -1;
        this.mRilVoiceRadioTechnology = -1;
        this.mRilDataRadioTechnology = -1;
        this.mEmergencyOnly = false;
        this.mImsCapabilities = new boolean[]{false, false, false, false, false, false};
        this.mPhoneId = phoneId;
    }

    private void writeEvent(int tag, Bundle data) {
        writeEvent(System.currentTimeMillis(), tag, -1, -1, data);
    }

    private void writeEvent(int tag, int param1, int param2) {
        writeEvent(System.currentTimeMillis(), tag, param1, param2, null);
    }

    private void writeEvent(int tag, int param1, int param2, Bundle data) {
        writeEvent(System.currentTimeMillis(), tag, param1, param2, data);
    }

    private void writeEvent(long timestamp, int tag, int param1, int param2, Bundle data) {
        Bundle b = data;
        if (data == null) {
            b = new Bundle();
        }
        b.putInt(DATA_KEY_PHONE_ID, this.mPhoneId);
        b.putInt(DATA_KEY_PARAM1, param1);
        b.putInt(DATA_KEY_PARAM2, param2);
        logEvent(timestamp, TAG_IMS_CAPABILITIES, tag, b);
    }

    public static boolean equals(Object a, Object b) {
        if (a == null) {
            return b == null ? DBG : false;
        } else {
            return a.equals(b);
        }
    }

    public void writeServiceStateChanged(ServiceState serviceState) {
        Bundle b = new Bundle();
        boolean changed = false;
        if (this.mVoiceRegState != serviceState.getVoiceRegState()) {
            this.mVoiceRegState = serviceState.getVoiceRegState();
            b.putInt(SERVICE_STATE_VOICE_REG_STATE, this.mVoiceRegState);
            changed = DBG;
        }
        if (this.mDataRegState != serviceState.getDataRegState()) {
            this.mDataRegState = serviceState.getDataRegState();
            b.putInt(SERVICE_STATE_DATA_REG_STATE, this.mDataRegState);
            changed = DBG;
        }
        if (this.mVoiceRoamingType != serviceState.getVoiceRoamingType()) {
            this.mVoiceRoamingType = serviceState.getVoiceRoamingType();
            b.putInt(SERVICE_STATE_VOICE_ROAMING_TYPE, this.mVoiceRoamingType);
            changed = DBG;
        }
        if (this.mDataRoamingType != serviceState.getDataRoamingType()) {
            this.mDataRoamingType = serviceState.getDataRoamingType();
            b.putInt(SERVICE_STATE_DATA_ROAMING_TYPE, this.mDataRoamingType);
            changed = DBG;
        }
        if (!(equals(this.mVoiceOperatorAlphaShort, serviceState.getVoiceOperatorAlphaShort()) && equals(this.mVoiceOperatorNumeric, serviceState.getVoiceOperatorNumeric()))) {
            this.mVoiceOperatorAlphaShort = serviceState.getVoiceOperatorAlphaShort();
            this.mVoiceOperatorNumeric = serviceState.getVoiceOperatorNumeric();
            b.putString(SERVICE_STATE_VOICE_ALPNA_SHORT, this.mVoiceOperatorAlphaShort);
            b.putString(SERVICE_STATE_VOICE_NUMERIC, this.mVoiceOperatorNumeric);
            changed = DBG;
        }
        if (!(equals(this.mDataOperatorAlphaShort, serviceState.getDataOperatorAlphaShort()) && equals(this.mDataOperatorNumeric, serviceState.getDataOperatorNumeric()))) {
            this.mDataOperatorAlphaShort = serviceState.getDataOperatorAlphaShort();
            this.mDataOperatorNumeric = serviceState.getDataOperatorNumeric();
            b.putString(SERVICE_STATE_DATA_ALPNA_SHORT, this.mDataOperatorAlphaShort);
            b.putString(SERVICE_STATE_DATA_NUMERIC, this.mDataOperatorNumeric);
            changed = DBG;
        }
        if (this.mRilVoiceRadioTechnology != serviceState.getRilVoiceRadioTechnology()) {
            this.mRilVoiceRadioTechnology = serviceState.getRilVoiceRadioTechnology();
            b.putInt(SERVICE_STATE_VOICE_RAT, this.mRilVoiceRadioTechnology);
            changed = DBG;
        }
        if (this.mRilDataRadioTechnology != serviceState.getRilDataRadioTechnology()) {
            this.mRilDataRadioTechnology = serviceState.getRilDataRadioTechnology();
            b.putInt(SERVICE_STATE_DATA_RAT, this.mRilDataRadioTechnology);
            changed = DBG;
        }
        if (this.mEmergencyOnly != serviceState.isEmergencyOnly()) {
            this.mEmergencyOnly = serviceState.isEmergencyOnly();
            b.putBoolean(SERVICE_STATE_EMERGENCY_ONLY, this.mEmergencyOnly);
            changed = DBG;
        }
        if (changed) {
            writeEvent(TAG_SERVICE_STATE, b);
        }
    }

    public void writeSetAirplaneMode(boolean enabled) {
        writeEvent(TAG_SETTINGS, TAG_SETTINGS, enabled ? TAG_SETTINGS : 0);
    }

    public void writeSetCellDataEnabled(boolean enabled) {
        writeEvent(TAG_SETTINGS, TAG_SERVICE_STATE, enabled ? TAG_SETTINGS : 0);
    }

    public void writeSetDataRoamingEnabled(boolean enabled) {
        writeEvent(TAG_SETTINGS, TAG_IMS_CONNECTION_STATE, enabled ? TAG_SETTINGS : 0);
    }

    public void writeSetPreferredNetworkType(int mode) {
        writeEvent(TAG_SETTINGS, TAG_IMS_CAPABILITIES, mode);
    }

    public void writeSetWifiEnabled(boolean enabled) {
        writeEvent(TAG_SETTINGS, TAG_DATA_CALL_LIST, enabled ? TAG_SETTINGS : 0);
    }

    public void writeSetWfcMode(int mode) {
        writeEvent(TAG_SETTINGS, TAG_PHONE_STATE, mode);
    }

    public void writeImsSetFeatureValue(int feature, int network, int value, int status) {
        switch (feature) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                writeEvent(TAG_SETTINGS, SETTING_VO_LTE_ENABLED, value);
            case TAG_SETTINGS /*1*/:
                writeEvent(TAG_SETTINGS, SETTING_VI_LTE_ENABLED, value);
            case TAG_SERVICE_STATE /*2*/:
                writeEvent(TAG_SETTINGS, SETTING_VO_WIFI_ENABLED, value);
            case TAG_IMS_CONNECTION_STATE /*3*/:
                writeEvent(TAG_SETTINGS, SETTING_VI_WIFI_ENABLED, value);
            default:
        }
    }

    public void writeOnImsConnectionState(int state, ImsReasonInfo reasonInfo) {
        writeEvent(TAG_IMS_CONNECTION_STATE, state, -1, imsReasonInfoToBundle(reasonInfo));
    }

    public void writeOnImsCapabilities(boolean[] capabilities) {
        boolean changed = false;
        for (int i = 0; i < capabilities.length; i += TAG_SETTINGS) {
            if (this.mImsCapabilities[i] != capabilities[i]) {
                this.mImsCapabilities[i] = capabilities[i];
                changed = DBG;
            }
        }
        if (changed) {
            Bundle b = new Bundle();
            b.putBoolean(DATA_KEY_VOLTE, capabilities[0]);
            b.putBoolean(DATA_KEY_VILTE, capabilities[TAG_SETTINGS]);
            b.putBoolean(DATA_KEY_VOWIFI, capabilities[TAG_SERVICE_STATE]);
            b.putBoolean(DATA_KEY_VIWIFI, capabilities[TAG_IMS_CONNECTION_STATE]);
            b.putBoolean(DATA_KEY_UTLTE, capabilities[TAG_IMS_CAPABILITIES]);
            b.putBoolean(DATA_KEY_UTWIFI, capabilities[TAG_DATA_CALL_LIST]);
            writeEvent(TAG_IMS_CAPABILITIES, b);
        }
    }

    public void writeRilSetupDataCall(int rilSerial, int radioTechnology, int profile, String apn, String user, String password, int authType, String protocol) {
        Bundle b = new Bundle();
        b.putInt(SERVICE_STATE_VOICE_RAT, radioTechnology);
        b.putInt(DATA_KEY_DATA_PROFILE, profile);
        b.putString(DATA_KEY_APN, apn);
        b.putString(DATA_KEY_PROTOCOL, protocol);
        writeEvent(TAG_RIL_REQUEST, 27, rilSerial, b);
    }

    public void writeRilDeactivateDataCall(int rilSerial, int cid, int reason) {
        Bundle b = new Bundle();
        b.putInt(DATA_KEY_DATA_CALL_CID, cid);
        b.putInt(DATA_KEY_DATA_DEACTIVATE_REASON, reason);
        writeEvent(TAG_RIL_REQUEST, 41, rilSerial, b);
    }

    public void writeRilDataCallList(ArrayList<DataCallResponse> dcsList) {
        Bundle b = new Bundle();
        int[] statuses = new int[dcsList.size()];
        int[] cids = new int[dcsList.size()];
        int[] actives = new int[dcsList.size()];
        String[] types = new String[dcsList.size()];
        String[] ifnames = new String[dcsList.size()];
        for (int i = 0; i < dcsList.size(); i += TAG_SETTINGS) {
            DataCallResponse dcs = (DataCallResponse) dcsList.get(i);
            statuses[i] = dcs.status;
            cids[i] = dcs.cid;
            actives[i] = dcs.active;
            types[i] = dcs.type;
            ifnames[i] = dcs.ifname;
        }
        b.putIntArray(DATA_KEY_DATA_CALL_STATUSES, statuses);
        b.putIntArray(DATA_KEY_DATA_CALL_CIDS, cids);
        b.putIntArray(DATA_KEY_DATA_CALL_ACTIVES, actives);
        b.putStringArray(DATA_KEY_DATA_CALL_TYPES, types);
        b.putStringArray(DATA_KEY_DATA_CALL_IFNAMES, ifnames);
        writeEvent(TAG_DATA_CALL_LIST, -1, -1, b);
    }

    public void writeRilDial(int rilSerial, int clirMode, UUSInfo uusInfo) {
        Bundle b = new Bundle();
        b.putInt(DATA_KEY_CLIR_MODE, clirMode);
        writeEvent(TAG_RIL_REQUEST, SETTING_VI_WIFI_ENABLED, rilSerial, b);
    }

    public void writeRilCallRing(char[] response) {
        Bundle b = new Bundle();
        b.putCharArray(DATA_KEY_RIL_CALL_RING_RESPONSE, response);
        writeEvent(TAG_RIL_UNSOL_RESPONSE, 1018, -1, b);
    }

    public void writeRilHangup(int rilSerial, int req, int gsmIndex) {
        Bundle b = new Bundle();
        b.putInt(DATA_KEY_RIL_HANGUP_GSM_INDEX, gsmIndex);
        writeEvent(TAG_RIL_REQUEST, req, rilSerial, b);
    }

    public void writeRilAnswer(int rilSerial) {
        writeEvent(TAG_RIL_REQUEST, 40, rilSerial, null);
    }

    public void writeRilSrvcc(int rilSrvccState) {
        writeEvent(TAG_RIL_UNSOL_RESPONSE, 1039, rilSrvccState, null);
    }

    public void writeRilSendSms(int rilSerial, int req) {
        writeEvent(TAG_RIL_REQUEST, req, rilSerial, null);
    }

    public void writeRilNewSms(int response) {
        writeEvent(TAG_RIL_UNSOL_RESPONSE, response, -1, null);
    }

    public void writeOnRilSolicitedResponse(int rilSerial, int rilError, int rilRequest, Object ret) {
        Bundle b = new Bundle();
        if (rilError != 0) {
            b.putInt(DATA_KEY_RIL_ERROR, rilError);
        }
        switch (rilRequest) {
            case SETTING_VI_WIFI_ENABLED /*10*/:
            case CharacterSets.ISO_8859_9 /*12*/:
            case UserData.ASCII_CR_INDEX /*13*/:
            case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
            case RadioNVItems.RIL_NV_MIP_PROFILE_MN_HA_SS /*40*/:
            case CallFailCause.TEMPORARY_FAILURE /*41*/:
                writeEvent(TAG_RIL_RESPONSE, rilRequest, rilSerial, b);
            case SmsHeader.ELT_ID_CHARACTER_SIZE_WVG_OBJECT /*25*/:
            case SmsHeader.ELT_ID_EXTENDED_OBJECT_DATA_REQUEST_CMD /*26*/:
            case CallFailCause.USER_NOT_MEMBER_OF_CUG /*87*/:
            case 113:
                if (ret == null) {
                    writeEvent(TAG_RIL_RESPONSE, rilRequest, rilSerial, b);
                    return;
                }
                SmsResponse smsResponse = (SmsResponse) ret;
                b.putInt(DATA_KEY_SMS_MESSAGE_REF, smsResponse.mMessageRef);
                b.putString(DATA_KEY_SMS_ACK_PDU, smsResponse.mAckPdu);
                b.putInt(DATA_KEY_SMS_ERROR_CODE, smsResponse.mErrorCode);
                writeEvent(TAG_RIL_RESPONSE, rilRequest, rilSerial, b);
            case CallFailCause.CALL_FAIL_DESTINATION_OUT_OF_ORDER /*27*/:
                if (ret == null) {
                    writeEvent(TAG_RIL_RESPONSE, rilRequest, rilSerial, b);
                    return;
                }
                DataCallResponse dataCall = (DataCallResponse) ret;
                b.putInt(DATA_KEY_DATA_CALL_STATUS, dataCall.status);
                b.putInt(DATA_KEY_DATA_CALL_RETRY, dataCall.suggestedRetryTime);
                b.putInt(DATA_KEY_DATA_CALL_CID, dataCall.cid);
                b.putInt(DATA_KEY_DATA_CALL_ACTIVE, dataCall.active);
                b.putString(DATA_KEY_DATA_CALL_TYPE, dataCall.type);
                b.putString(DATA_KEY_DATA_CALL_IFNAME, dataCall.ifname);
                writeEvent(TAG_RIL_RESPONSE, rilRequest, rilSerial, b);
            default:
        }
    }

    public void writeOnRilTimeoutResponse(int rilSerial, int rilRequest) {
        writeEvent(TAG_RIL_TIMEOUT_RESPONSE, rilRequest, rilSerial, null);
    }

    public void writePhoneState(PhoneConstants.State phoneState) {
        int state;
        switch (-getcom-android-internal-telephony-PhoneConstants$StateSwitchesValues()[phoneState.ordinal()]) {
            case TAG_SETTINGS /*1*/:
                state = 0;
                break;
            case TAG_SERVICE_STATE /*2*/:
                state = TAG_SERVICE_STATE;
                break;
            case TAG_IMS_CONNECTION_STATE /*3*/:
                state = TAG_SETTINGS;
                break;
            default:
                state = -1;
                break;
        }
        writeEvent(TAG_PHONE_STATE, state, -1);
    }

    public void writeImsCallState(ImsCallSession session, State callState) {
        int state;
        switch (-getcom-android-internal-telephony-Call$StateSwitchesValues()[callState.ordinal()]) {
            case TAG_SETTINGS /*1*/:
                state = TAG_SETTINGS;
                break;
            case TAG_SERVICE_STATE /*2*/:
                state = TAG_IMS_CAPABILITIES;
                break;
            case TAG_IMS_CONNECTION_STATE /*3*/:
                state = TAG_IMS_CONNECTION_STATE;
                break;
            case TAG_IMS_CAPABILITIES /*4*/:
                state = SETTING_VO_WIFI_ENABLED;
                break;
            case TAG_DATA_CALL_LIST /*5*/:
                state = TAG_PHONE_STATE;
                break;
            case SETTING_VO_LTE_ENABLED /*6*/:
                state = TAG_SERVICE_STATE;
                break;
            case SETTING_VO_WIFI_ENABLED /*7*/:
                state = 0;
                break;
            case TAG_PHONE_STATE /*8*/:
                state = TAG_DATA_CALL_LIST;
                break;
            case SETTING_VI_LTE_ENABLED /*9*/:
                state = SETTING_VO_LTE_ENABLED;
                break;
            default:
                state = -1;
                break;
        }
        writeEvent(TAG_IMS_CALL_STATE, getCallId(session), state);
    }

    private void writeImsCallEvent(int tag, ImsCallSession session) {
        writeEvent(tag, getCallId(session), -1);
    }

    private void writeImsCallEvent(int tag, ImsCallSession session, ImsReasonInfo reasonInfo) {
        writeEvent(tag, getCallId(session), -1, imsReasonInfoToBundle(reasonInfo));
    }

    private Bundle imsReasonInfoToBundle(ImsReasonInfo reasonInfo) {
        if (reasonInfo == null) {
            return null;
        }
        Bundle b = new Bundle();
        b.putInt(DATA_KEY_REASONINFO_CODE, reasonInfo.mCode);
        b.putInt(DATA_KEY_REASONINFO_EXTRA_CODE, reasonInfo.mExtraCode);
        b.putString(DATA_KEY_REASONINFO_EXTRA_MESSAGE, reasonInfo.mExtraMessage);
        return b;
    }

    public void writeOnImsCallStart(ImsCallSession session, String callee) {
        Bundle b = new Bundle();
        b.putString(DATA_KEY_CALLEE, callee);
        writeEvent(TAG_IMS_CALL_START, getCallId(session), -1, b);
    }

    public void writeOnImsCallStartConference(ImsCallSession session, String[] participants) {
        Bundle b = new Bundle();
        b.putStringArray(DATA_KEY_PARTICIPANTS, participants);
        writeEvent(TAG_IMS_CALL_START_CONFERENCE, getCallId(session), -1, b);
    }

    public void writeOnImsCallReceive(ImsCallSession session) {
        writeImsCallEvent(TAG_IMS_CALL_RECEIVE, session);
    }

    public void writeOnImsCallAccept(ImsCallSession session) {
        writeImsCallEvent(TAG_IMS_CALL_ACCEPT, session);
    }

    public void writeOnImsCallReject(ImsCallSession session) {
        writeImsCallEvent(TAG_IMS_CALL_REJECT, session);
    }

    public void writeOnImsCallTerminate(ImsCallSession session) {
        writeImsCallEvent(TAG_IMS_CALL_TERMINATE, session);
    }

    public void writeOnImsCallHold(ImsCallSession session) {
        writeImsCallEvent(TAG_IMS_CALL_HOLD, session);
    }

    public void writeOnImsCallResume(ImsCallSession session) {
        writeImsCallEvent(TAG_IMS_CALL_RESUME, session);
    }

    public void writeOnImsCallProgressing(ImsCallSession session) {
        writeImsCallEvent(TAG_IMS_CALL_PROGRESSING, session);
    }

    public void writeOnImsCallStarted(ImsCallSession session) {
        writeImsCallEvent(TAG_IMS_CALL_STARTED, session);
    }

    public void writeOnImsCallStartFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
        writeImsCallEvent(TAG_IMS_CALL_START_FAILED, session, reasonInfo);
    }

    public void writeOnImsCallTerminated(ImsCallSession session, ImsReasonInfo reasonInfo) {
        writeImsCallEvent(TAG_IMS_CALL_TERMINATED, session, reasonInfo);
    }

    public void writeOnImsCallHeld(ImsCallSession session) {
        writeImsCallEvent(TAG_IMS_CALL_HELD, session);
    }

    public void writeOnImsCallHoldReceived(ImsCallSession session) {
        writeImsCallEvent(TAG_IMS_CALL_HOLD_RECEIVED, session);
    }

    public void writeOnImsCallHoldFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
        writeImsCallEvent(TAG_IMS_CALL_HOLD_FAILED, session, reasonInfo);
    }

    public void writeOnImsCallResumed(ImsCallSession session) {
        writeImsCallEvent(TAG_IMS_CALL_RESUMED, session);
    }

    public void writeOnImsCallResumeReceived(ImsCallSession session) {
        writeImsCallEvent(TAG_IMS_CALL_RESUME_RECEIVED, session);
    }

    public void writeOnImsCallResumeFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
        writeImsCallEvent(TAG_IMS_CALL_RESUME_FAILED, session, reasonInfo);
    }

    public void writeOnImsCallHandover(ImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        writeEvent(TAG_IMS_CALL_HANDOVER, getCallId(session), -1, imsHandoverToBundle(srcAccessTech, targetAccessTech, reasonInfo));
    }

    public void writeOnImsCallHandoverFailed(ImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        writeEvent(TAG_IMS_CALL_HANDOVER_FAILED, getCallId(session), -1, imsHandoverToBundle(srcAccessTech, targetAccessTech, reasonInfo));
    }

    private int getCallId(ImsCallSession session) {
        if (session == null) {
            return -1;
        }
        try {
            return Integer.parseInt(session.getCallId());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private Bundle imsHandoverToBundle(int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        Bundle b = new Bundle();
        b.putInt(DATA_KEY_SRC_TECH, srcAccessTech);
        b.putInt(DATA_KEY_TARGET_TECH, targetAccessTech);
        b.putInt(DATA_KEY_REASONINFO_CODE, reasonInfo.mCode);
        b.putInt(DATA_KEY_REASONINFO_EXTRA_CODE, reasonInfo.mExtraCode);
        b.putString(DATA_KEY_REASONINFO_EXTRA_MESSAGE, reasonInfo.mExtraMessage);
        return b;
    }
}
