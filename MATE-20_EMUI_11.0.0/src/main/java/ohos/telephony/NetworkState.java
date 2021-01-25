package ohos.telephony;

import java.util.Objects;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class NetworkState implements Sequenceable {
    public static final int NSA_STATE_CONNECTED_DETECT = 3;
    public static final int NSA_STATE_DUAL_CONNECTED = 5;
    public static final int NSA_STATE_IDLE_DETECT = 4;
    public static final int NSA_STATE_NOT_SUPPORT = 1;
    public static final int NSA_STATE_NO_DETECT = 2;
    public static final int NSA_STATE_SA_ATTACHED = 6;
    public static final int REG_STATE_EMERGENCY_CALL_ONLY = 2;
    public static final int REG_STATE_IN_SERVICE = 1;
    public static final int REG_STATE_NO_SERVICE = 0;
    public static final int REG_STATE_POWER_OFF = 3;
    private boolean isCaActive = false;
    private boolean isEmergency = false;
    private boolean isRoaming = false;
    private String longOperatorName = "";
    private int nsaState = 1;
    private String plmnNumeric = "";
    private int regState = 0;
    private String shortOperatorName = "";

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        parcel.writeString(this.longOperatorName);
        parcel.writeString(this.shortOperatorName);
        parcel.writeString(this.plmnNumeric);
        parcel.writeBoolean(this.isRoaming);
        parcel.writeInt(this.regState);
        parcel.writeInt(this.nsaState);
        parcel.writeBoolean(this.isCaActive);
        parcel.writeBoolean(this.isEmergency);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        this.longOperatorName = parcel.readString();
        this.shortOperatorName = parcel.readString();
        this.plmnNumeric = parcel.readString();
        this.isRoaming = parcel.readBoolean();
        this.regState = parcel.readInt();
        this.nsaState = parcel.readInt();
        this.isCaActive = parcel.readBoolean();
        this.isEmergency = parcel.readBoolean();
        return true;
    }

    public String toString() {
        return NetworkState.class.getSimpleName() + ":longOperatorName = " + this.longOperatorName + ", shortOperatorName = " + this.shortOperatorName + ", plmnNumeric = " + this.plmnNumeric + ", isRoaming = " + this.isRoaming + ", regState = " + this.regState + ", nsaState = " + this.nsaState + ", isCaActive = " + this.isCaActive + ", isEmergency = " + this.isEmergency;
    }

    public int hashCode() {
        return Objects.hash(this.longOperatorName, this.shortOperatorName, this.plmnNumeric, Boolean.valueOf(this.isRoaming), Integer.valueOf(this.regState), Integer.valueOf(this.nsaState), Boolean.valueOf(this.isCaActive), Boolean.valueOf(this.isEmergency));
    }

    public String getLongOperatorName() {
        return this.longOperatorName;
    }

    public String getShortOperatorName() {
        return this.shortOperatorName;
    }

    public String getPlmnNumeric() {
        return this.plmnNumeric;
    }

    public boolean isRoaming() {
        return this.isRoaming;
    }

    public int getRegState() {
        return this.regState;
    }

    public int getNsaState() {
        return this.nsaState;
    }

    public boolean isCaActive() {
        return this.isCaActive;
    }

    public boolean isEmergency() {
        return this.isEmergency;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof NetworkState)) {
            return false;
        }
        NetworkState networkState = (NetworkState) obj;
        if (this.longOperatorName.equals(networkState.longOperatorName) && this.shortOperatorName.equals(networkState.shortOperatorName) && this.plmnNumeric.equals(networkState.plmnNumeric) && this.isRoaming == networkState.isRoaming && this.regState == networkState.regState && this.nsaState == networkState.nsaState && this.isCaActive == networkState.isCaActive && this.isEmergency == networkState.isEmergency) {
            return true;
        }
        return false;
    }
}
