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
