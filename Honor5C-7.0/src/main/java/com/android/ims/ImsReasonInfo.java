package com.android.ims;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ImsReasonInfo implements Parcelable {
    public static final int CODE_ANSWERED_ELSEWHERE = 1014;
    public static final int CODE_BLACKLISTED_CALL_ID = 506;
    public static final int CODE_CALL_DROP_IWLAN_TO_LTE_UNAVAILABLE = 1100;
    public static final int CODE_CALL_END_CAUSE_CALL_PULL = 1016;
    public static final int CODE_CALL_PULL_OUT_OF_SYNC = 1015;
    public static final int CODE_ECBM_NOT_SUPPORTED = 901;
    public static final int CODE_EMERGENCY_PERM_FAILURE = 364;
    public static final int CODE_EMERGENCY_TEMP_FAILURE = 363;
    public static final int CODE_EPDG_TUNNEL_ESTABLISH_FAILURE = 1400;
    public static final int CODE_EPDG_TUNNEL_LOST_CONNECTION = 1402;
    public static final int CODE_EPDG_TUNNEL_REKEY_FAILURE = 1401;
    public static final int CODE_FDN_BLOCKED = 241;
    public static final int CODE_IWLAN_DPD_FAILURE = 1300;
    public static final int CODE_LOCAL_CALL_BUSY = 142;
    public static final int CODE_LOCAL_CALL_CS_RETRY_REQUIRED = 146;
    public static final int CODE_LOCAL_CALL_DECLINE = 143;
    public static final int CODE_LOCAL_CALL_EXCEEDED = 141;
    public static final int CODE_LOCAL_CALL_RESOURCE_RESERVATION_FAILED = 145;
    public static final int CODE_LOCAL_CALL_TERMINATED = 148;
    public static final int CODE_LOCAL_CALL_VCC_ON_PROGRESSING = 144;
    public static final int CODE_LOCAL_CALL_VOLTE_RETRY_REQUIRED = 147;
    public static final int CODE_LOCAL_HO_NOT_FEASIBLE = 149;
    public static final int CODE_LOCAL_ILLEGAL_ARGUMENT = 101;
    public static final int CODE_LOCAL_ILLEGAL_STATE = 102;
    public static final int CODE_LOCAL_IMS_SERVICE_DOWN = 106;
    public static final int CODE_LOCAL_INTERNAL_ERROR = 103;
    public static final int CODE_LOCAL_LOW_BATTERY = 112;
    public static final int CODE_LOCAL_NETWORK_IP_CHANGED = 124;
    public static final int CODE_LOCAL_NETWORK_NO_LTE_COVERAGE = 122;
    public static final int CODE_LOCAL_NETWORK_NO_SERVICE = 121;
    public static final int CODE_LOCAL_NETWORK_ROAMING = 123;
    public static final int CODE_LOCAL_NOT_REGISTERED = 132;
    public static final int CODE_LOCAL_NO_PENDING_CALL = 107;
    public static final int CODE_LOCAL_POWER_OFF = 111;
    public static final int CODE_LOCAL_SERVICE_UNAVAILABLE = 131;
    public static final int CODE_LOW_BATTERY = 505;
    public static final int CODE_MEDIA_INIT_FAILED = 401;
    public static final int CODE_MEDIA_NOT_ACCEPTABLE = 403;
    public static final int CODE_MEDIA_NO_DATA = 402;
    public static final int CODE_MEDIA_UNSPECIFIED = 404;
    public static final int CODE_MULTIENDPOINT_NOT_SUPPORTED = 902;
    public static final int CODE_REGISTRATION_ERROR = 1000;
    public static final int CODE_SIP_BAD_ADDRESS = 337;
    public static final int CODE_SIP_BAD_REQUEST = 331;
    public static final int CODE_SIP_BUSY = 338;
    public static final int CODE_SIP_CLIENT_ERROR = 342;
    public static final int CODE_SIP_FORBIDDEN = 332;
    public static final int CODE_SIP_GLOBAL_ERROR = 362;
    public static final int CODE_SIP_NOT_ACCEPTABLE = 340;
    public static final int CODE_SIP_NOT_FOUND = 333;
    public static final int CODE_SIP_NOT_REACHABLE = 341;
    public static final int CODE_SIP_NOT_SUPPORTED = 334;
    public static final int CODE_SIP_REDIRECTED = 321;
    public static final int CODE_SIP_REQUEST_CANCELLED = 339;
    public static final int CODE_SIP_REQUEST_TIMEOUT = 335;
    public static final int CODE_SIP_SERVER_ERROR = 354;
    public static final int CODE_SIP_SERVER_INTERNAL_ERROR = 351;
    public static final int CODE_SIP_SERVER_TIMEOUT = 353;
    public static final int CODE_SIP_SERVICE_UNAVAILABLE = 352;
    public static final int CODE_SIP_TEMPRARILY_UNAVAILABLE = 336;
    public static final int CODE_SIP_USER_REJECTED = 361;
    public static final int CODE_SUPP_SVC_CANCELLED = 1202;
    public static final int CODE_SUPP_SVC_FAILED = 1201;
    public static final int CODE_SUPP_SVC_REINVITE_COLLISION = 1203;
    public static final int CODE_TIMEOUT_1XX_WAITING = 201;
    public static final int CODE_TIMEOUT_NO_ANSWER = 202;
    public static final int CODE_TIMEOUT_NO_ANSWER_CALL_UPDATE = 203;
    public static final int CODE_UNSPECIFIED = 0;
    public static final int CODE_USER_DECLINE = 504;
    public static final int CODE_USER_IGNORE = 503;
    public static final int CODE_USER_NOANSWER = 502;
    public static final int CODE_USER_TERMINATED = 501;
    public static final int CODE_USER_TERMINATED_BY_REMOTE = 510;
    public static final int CODE_UT_CB_PASSWORD_MISMATCH = 821;
    public static final int CODE_UT_NETWORK_ERROR = 804;
    public static final int CODE_UT_NOT_SUPPORTED = 801;
    public static final int CODE_UT_NO_CONNECTION = 831;
    public static final int CODE_UT_OPERATION_NOT_ALLOWED = 803;
    public static final int CODE_UT_SERVICE_UNAVAILABLE = 802;
    public static final Creator<ImsReasonInfo> CREATOR = null;
    public static final int EXTRA_CODE_CALL_RETRY_BY_SETTINGS = 3;
    public static final int EXTRA_CODE_CALL_RETRY_NORMAL = 1;
    public static final int EXTRA_CODE_CALL_RETRY_SILENT_REDIAL = 2;
    public static final String EXTRA_MSG_SERVICE_NOT_AUTHORIZED = "Forbidden. Not Authorized for Service";
    public int mCode;
    public int mExtraCode;
    public String mExtraMessage;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.ims.ImsReasonInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.ims.ImsReasonInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsReasonInfo.<clinit>():void");
    }

    public ImsReasonInfo() {
        this.mCode = CODE_UNSPECIFIED;
        this.mExtraCode = CODE_UNSPECIFIED;
        this.mExtraMessage = null;
    }

    public ImsReasonInfo(Parcel in) {
        readFromParcel(in);
    }

    public ImsReasonInfo(int code, int extraCode) {
        this.mCode = code;
        this.mExtraCode = extraCode;
        this.mExtraMessage = null;
    }

    public ImsReasonInfo(int code, int extraCode, String extraMessage) {
        this.mCode = code;
        this.mExtraCode = extraCode;
        this.mExtraMessage = extraMessage;
    }

    public int getCode() {
        return this.mCode;
    }

    public int getExtraCode() {
        return this.mExtraCode;
    }

    public String getExtraMessage() {
        return this.mExtraMessage;
    }

    public String toString() {
        return "ImsReasonInfo :: {" + this.mCode + ", " + this.mExtraCode + ", " + this.mExtraMessage + "}";
    }

    public int describeContents() {
        return CODE_UNSPECIFIED;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mCode);
        out.writeInt(this.mExtraCode);
        out.writeString(this.mExtraMessage);
    }

    private void readFromParcel(Parcel in) {
        this.mCode = in.readInt();
        this.mExtraCode = in.readInt();
        this.mExtraMessage = in.readString();
    }
}
