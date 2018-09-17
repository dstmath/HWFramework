package android.media;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public final class VolumePolicy implements Parcelable {
    public static final int A11Y_MODE_INDEPENDENT_A11Y_VOLUME = 1;
    public static final int A11Y_MODE_MEDIA_A11Y_VOLUME = 0;
    public static final Creator<VolumePolicy> CREATOR = new Creator<VolumePolicy>() {
        public VolumePolicy createFromParcel(Parcel p) {
            boolean z;
            boolean z2 = true;
            boolean z3 = p.readInt() != 0;
            if (p.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            if (p.readInt() == 0) {
                z2 = false;
            }
            return new VolumePolicy(z3, z, z2, p.readInt());
        }

        public VolumePolicy[] newArray(int size) {
            return new VolumePolicy[size];
        }
    };
    public static final VolumePolicy DEFAULT = new VolumePolicy(false, true, true, 400);
    public final boolean doNotDisturbWhenSilent;
    public final int vibrateToSilentDebounce;
    public final boolean volumeDownToEnterSilent;
    public final boolean volumeUpToExitSilent;

    public VolumePolicy(boolean volumeDownToEnterSilent, boolean volumeUpToExitSilent, boolean doNotDisturbWhenSilent, int vibrateToSilentDebounce) {
        this.volumeDownToEnterSilent = volumeDownToEnterSilent;
        this.volumeUpToExitSilent = volumeUpToExitSilent;
        this.doNotDisturbWhenSilent = doNotDisturbWhenSilent;
        this.vibrateToSilentDebounce = vibrateToSilentDebounce;
    }

    public String toString() {
        return "VolumePolicy[volumeDownToEnterSilent=" + this.volumeDownToEnterSilent + ",volumeUpToExitSilent=" + this.volumeUpToExitSilent + ",doNotDisturbWhenSilent=" + this.doNotDisturbWhenSilent + ",vibrateToSilentDebounce=" + this.vibrateToSilentDebounce + "]";
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Boolean.valueOf(this.volumeDownToEnterSilent), Boolean.valueOf(this.volumeUpToExitSilent), Boolean.valueOf(this.doNotDisturbWhenSilent), Integer.valueOf(this.vibrateToSilentDebounce)});
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (!(o instanceof VolumePolicy)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        VolumePolicy other = (VolumePolicy) o;
        if (other.volumeDownToEnterSilent != this.volumeDownToEnterSilent || other.volumeUpToExitSilent != this.volumeUpToExitSilent || other.doNotDisturbWhenSilent != this.doNotDisturbWhenSilent) {
            z = false;
        } else if (other.vibrateToSilentDebounce != this.vibrateToSilentDebounce) {
            z = false;
        }
        return z;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        if (this.volumeDownToEnterSilent) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.volumeUpToExitSilent) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.doNotDisturbWhenSilent) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeInt(this.vibrateToSilentDebounce);
    }
}
