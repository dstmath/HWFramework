package ohos.telephony;

import java.util.Objects;
import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public final class NetworkInformation implements Sequenceable {
    public static final int NETWORK_AVAILABLE = 1;
    public static final int NETWORK_CURRENT = 2;
    public static final int NETWORK_FORBIDDEN = 3;
    public static final int NETWORK_UNKNOWN = 0;
    private String mOperatorName;
    private String mOperatorNumeric;
    private String mRadioTech;
    private int mState;

    public NetworkInformation() {
        this.mState = 0;
        this.mOperatorName = "";
        this.mOperatorNumeric = "";
        this.mState = 0;
        this.mRadioTech = "";
    }

    public NetworkInformation(String str, String str2, int i, String str3) {
        this.mState = 0;
        this.mOperatorName = str;
        this.mOperatorNumeric = str2;
        this.mState = i;
        this.mRadioTech = str3;
    }

    public String getOperatorName() {
        return this.mOperatorName;
    }

    public String getOperatorNumeric() {
        return this.mOperatorNumeric;
    }

    public int getNetworkState() {
        return this.mState;
    }

    public String getRadioTech() {
        return this.mRadioTech;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        parcel.writeString(this.mOperatorName);
        parcel.writeString(this.mOperatorNumeric);
        parcel.writeInt(this.mState);
        parcel.writeString(this.mRadioTech);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        this.mOperatorName = parcel.readString();
        this.mOperatorNumeric = parcel.readString();
        this.mState = parcel.readInt();
        this.mRadioTech = parcel.readString();
        return true;
    }

    public String toString() {
        return NetworkInformation.class.getSimpleName() + ":operatorName = " + this.mOperatorName + ", operatorNumeric = " + this.mOperatorNumeric + ", state = " + this.mState + ", radioTech = " + this.mRadioTech;
    }

    public int hashCode() {
        return Objects.hash(this.mOperatorName, this.mOperatorNumeric, Integer.valueOf(this.mState), this.mRadioTech);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof NetworkInformation)) {
            return false;
        }
        NetworkInformation networkInformation = (NetworkInformation) obj;
        if (!this.mOperatorName.equals(networkInformation.mOperatorName) || !this.mOperatorNumeric.equals(networkInformation.mOperatorNumeric) || this.mState != networkInformation.mState || !this.mRadioTech.equals(networkInformation.mRadioTech)) {
            return false;
        }
        return true;
    }
}
