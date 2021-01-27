package ohos.dcall;

import java.util.Objects;
import ohos.ai.engine.pluginlabel.PluginLabelConstants;
import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public final class DisconnectInfo implements Sequenceable {
    static final Sequenceable.Producer<DisconnectInfo> CREATOR = new Sequenceable.Producer<DisconnectInfo>() {
        /* class ohos.dcall.DisconnectInfo.AnonymousClass1 */

        public DisconnectInfo createFromParcel(Parcel parcel) {
            return new DisconnectInfo(parcel.readInt(), parcel.readString(), parcel.readString(), parcel.readString());
        }
    };
    public static final int DISCONNECT_CODE_BUSY = 7;
    public static final int DISCONNECT_CODE_CANCELED = 4;
    public static final int DISCONNECT_CODE_ERROR = 1;
    public static final int DISCONNECT_CODE_LOCAL = 2;
    public static final int DISCONNECT_CODE_MISSED = 5;
    public static final int DISCONNECT_CODE_OTHER = 9;
    public static final int DISCONNECT_CODE_REJECTED = 6;
    public static final int DISCONNECT_CODE_REMOTE = 3;
    public static final int DISCONNECT_CODE_RESTRICTED = 8;
    public static final int DISCONNECT_CODE_UNSPECIFIED = 0;
    public static final int OHOS_DISCONNECT_CODE_ALL_VOICE_CALL_NOT_ALLOW = 27;
    public static final int OHOS_DISCONNECT_CODE_CALL_BARRED_FUCTION_OPEN = 46;
    public static final int OHOS_DISCONNECT_CODE_CALL_FUNCTION_DISABLED = 32;
    public static final int OHOS_DISCONNECT_CODE_CANNOT_MODIFY_CALL_FORWARDING_WHILE_ROAMING = 36;
    public static final int OHOS_DISCONNECT_CODE_CDMA_NOT_SUPPORTED_NUMBER = 47;
    public static final int OHOS_DISCONNECT_CODE_CELLULAR_DATA_DISABLED = 38;
    public static final int OHOS_DISCONNECT_CODE_DIALING = 33;
    public static final int OHOS_DISCONNECT_CODE_EMERGENCY_CALL_NOT_ALLOW = 28;
    public static final int OHOS_DISCONNECT_CODE_EMERGENCY_ONLY = 25;
    public static final int OHOS_DISCONNECT_CODE_FDN_NUMBER_NOT_ALLOW = 49;
    public static final int OHOS_DISCONNECT_CODE_IMEI_REJECTED = 35;
    public static final int OHOS_DISCONNECT_CODE_INCOMING_CALL_MISSED = 62;
    public static final int OHOS_DISCONNECT_CODE_INCOMING_CALL_REJECTED = 63;
    public static final int OHOS_DISCONNECT_CODE_INCOMING_REJECTED_BY_REMOTE = 64;
    public static final int OHOS_DISCONNECT_CODE_INVALID_PHONE_NUMBER = 23;
    public static final int OHOS_DISCONNECT_CODE_LOCAL_HANGUP = 60;
    public static final int OHOS_DISCONNECT_CODE_LOW_BATTERY = 59;
    public static final int OHOS_DISCONNECT_CODE_LOW_BATTERY_DIAL_FAILED = 45;
    public static final int OHOS_DISCONNECT_CODE_MORE_THAN_TWO_CALLS = 31;
    public static final int OHOS_DISCONNECT_CODE_NETWORK_ACCESS_BLOCKED = 26;
    public static final int OHOS_DISCONNECT_CODE_NETWORK_BUSY = 48;
    public static final int OHOS_DISCONNECT_CODE_NORMAL_VOICE_CALL_NOT_ALLOW = 29;
    public static final int OHOS_DISCONNECT_CODE_NOT_EMERGENCY = 44;
    public static final int OHOS_DISCONNECT_CODE_OTASP_CONFIGURING = 30;
    public static final int OHOS_DISCONNECT_CODE_OUTGOING_CALL_FAILED = 58;
    public static final int OHOS_DISCONNECT_CODE_OUTGOING_USER_CANCELED = 39;
    public static final int OHOS_DISCONNECT_CODE_OUT_OF_SERVICE = 24;
    public static final int OHOS_DISCONNECT_CODE_RADIO_OFF = 21;
    public static final int OHOS_DISCONNECT_CODE_REACHED_CELLULAR_DATA_LIMIT = 37;
    public static final int OHOS_DISCONNECT_CODE_REACHED_MAX_CALL_NUMBER = 40;
    public static final int OHOS_DISCONNECT_CODE_REMOTE_HANGUP = 61;
    public static final int OHOS_DISCONNECT_CODE_RINGING = 22;
    public static final int OHOS_DISCONNECT_CODE_SWITCH_TO_ANOTHER_DEVICE = 41;
    public static final int OHOS_DISCONNECT_CODE_UNKNOWN = -1;
    public static final int OHOS_DISCONNECT_CODE_VIDEO_CALL_CHANGED_NUMBER_TO_DIAL = 57;
    public static final int OHOS_DISCONNECT_CODE_VIDEO_CALL_CHANGED_TO_SS = 54;
    public static final int OHOS_DISCONNECT_CODE_VIDEO_CALL_CHANGED_TO_USSD = 55;
    public static final int OHOS_DISCONNECT_CODE_VIDEO_CALL_CHANGED_TO_VOICE_CALL = 56;
    public static final int OHOS_DISCONNECT_CODE_VIDEO_CALL_NOT_ALLOWED_UNDER_TTY_MODE = 42;
    public static final int OHOS_DISCONNECT_CODE_VOICEMAIL_NO_PHONE_NUMBER = 43;
    public static final int OHOS_DISCONNECT_CODE_VOICE_CALL_CHANGED_NUMBER_TO_DIAL = 52;
    public static final int OHOS_DISCONNECT_CODE_VOICE_CALL_CHANGED_TO_SS = 51;
    public static final int OHOS_DISCONNECT_CODE_VOICE_CALL_CHANGED_TO_USSD = 50;
    public static final int OHOS_DISCONNECT_CODE_VOICE_CALL_CHANGED_TO_VIDEO_CALL = 53;
    public static final int OHOS_DISCONNECT_CODE_WIFI_INTERRUPTED = 34;
    private int mCode;
    private String mDescription;
    private String mReason;

    public DisconnectInfo(int i) {
        this(i, null, null, null);
    }

    public DisconnectInfo(int i, String str) {
        this(i, null, null, str);
    }

    public DisconnectInfo(int i, String str, String str2, String str3) {
        this.mCode = i;
        this.mDescription = str2;
        this.mReason = str3;
    }

    public int getCode() {
        return this.mCode;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public String getReason() {
        return this.mReason;
    }

    public int hashCode() {
        return Objects.hashCode(Integer.valueOf(this.mCode)) + Objects.hashCode(this.mDescription) + Objects.hashCode(this.mReason);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof DisconnectInfo)) {
            return false;
        }
        DisconnectInfo disconnectInfo = (DisconnectInfo) obj;
        if (!Objects.equals(Integer.valueOf(this.mCode), Integer.valueOf(disconnectInfo.getCode())) || !Objects.equals(this.mDescription, disconnectInfo.getDescription()) || !Objects.equals(this.mReason, disconnectInfo.getReason())) {
            return false;
        }
        return true;
    }

    public String toString() {
        String str;
        String str2;
        switch (this.mCode) {
            case 0:
                str = PluginLabelConstants.REMOTE_EXCEPTION_DEFAULT;
                break;
            case 1:
                str = "ERROR";
                break;
            case 2:
                str = "LOCAL";
                break;
            case 3:
                str = "REMOTE";
                break;
            case 4:
                str = "CANCELED";
                break;
            case 5:
                str = "MISSED";
                break;
            case 6:
                str = "REJECTED";
                break;
            case 7:
                str = "BUSY";
                break;
            case 8:
                str = "RESTRICTED";
                break;
            case 9:
                str = "OTHER";
                break;
            default:
                str = "invalid code: " + this.mCode;
                break;
        }
        String str3 = this.mDescription;
        if (str3 == null) {
            str2 = "";
        } else {
            str2 = str3.toString();
        }
        String str4 = this.mReason;
        if (str4 == null) {
            str4 = "";
        }
        return "DisconnectInfo [ Code: (" + str + ") Description: (" + str2 + ") Reason: (" + str4 + ") ]";
    }

    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        parcel.writeInt(this.mCode);
        parcel.writeString(this.mDescription);
        parcel.writeString(this.mReason);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        this.mCode = parcel.readInt();
        this.mDescription = parcel.readString();
        this.mReason = parcel.readString();
        return true;
    }
}
