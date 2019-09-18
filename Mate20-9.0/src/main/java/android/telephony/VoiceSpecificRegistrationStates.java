package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class VoiceSpecificRegistrationStates implements Parcelable {
    public static final Parcelable.Creator<VoiceSpecificRegistrationStates> CREATOR = new Parcelable.Creator<VoiceSpecificRegistrationStates>() {
        public VoiceSpecificRegistrationStates createFromParcel(Parcel source) {
            return new VoiceSpecificRegistrationStates(source);
        }

        public VoiceSpecificRegistrationStates[] newArray(int size) {
            return new VoiceSpecificRegistrationStates[size];
        }
    };
    public final boolean cssSupported;
    public final int defaultRoamingIndicator;
    public final int roamingIndicator;
    public final int systemIsInPrl;

    VoiceSpecificRegistrationStates(boolean cssSupported2, int roamingIndicator2, int systemIsInPrl2, int defaultRoamingIndicator2) {
        this.cssSupported = cssSupported2;
        this.roamingIndicator = roamingIndicator2;
        this.systemIsInPrl = systemIsInPrl2;
        this.defaultRoamingIndicator = defaultRoamingIndicator2;
    }

    private VoiceSpecificRegistrationStates(Parcel source) {
        this.cssSupported = source.readBoolean();
        this.roamingIndicator = source.readInt();
        this.systemIsInPrl = source.readInt();
        this.defaultRoamingIndicator = source.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBoolean(this.cssSupported);
        dest.writeInt(this.roamingIndicator);
        dest.writeInt(this.systemIsInPrl);
        dest.writeInt(this.defaultRoamingIndicator);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "VoiceSpecificRegistrationStates { mCssSupported=" + this.cssSupported + " mRoamingIndicator=" + this.roamingIndicator + " mSystemIsInPrl=" + this.systemIsInPrl + " mDefaultRoamingIndicator=" + this.defaultRoamingIndicator + "}";
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Boolean.valueOf(this.cssSupported), Integer.valueOf(this.roamingIndicator), Integer.valueOf(this.systemIsInPrl), Integer.valueOf(this.defaultRoamingIndicator)});
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof VoiceSpecificRegistrationStates)) {
            return false;
        }
        VoiceSpecificRegistrationStates other = (VoiceSpecificRegistrationStates) o;
        if (!(this.cssSupported == other.cssSupported && this.roamingIndicator == other.roamingIndicator && this.systemIsInPrl == other.systemIsInPrl && this.defaultRoamingIndicator == other.defaultRoamingIndicator)) {
            z = false;
        }
        return z;
    }
}
