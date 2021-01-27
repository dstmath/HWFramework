package android.media;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public final class VolumePolicy implements Parcelable {
    public static final int A11Y_MODE_INDEPENDENT_A11Y_VOLUME = 1;
    public static final int A11Y_MODE_MEDIA_A11Y_VOLUME = 0;
    public static final Parcelable.Creator<VolumePolicy> CREATOR = new Parcelable.Creator<VolumePolicy>() {
        /* class android.media.VolumePolicy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VolumePolicy createFromParcel(Parcel p) {
            boolean z = true;
            boolean z2 = p.readInt() != 0;
            boolean z3 = p.readInt() != 0;
            if (p.readInt() == 0) {
                z = false;
            }
            return new VolumePolicy(z2, z3, z, p.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public VolumePolicy[] newArray(int size) {
            return new VolumePolicy[size];
        }
    };
    public static final VolumePolicy DEFAULT = new VolumePolicy(false, false, false, 400);
    public final boolean doNotDisturbWhenSilent;
    public final int vibrateToSilentDebounce;
    public final boolean volumeDownToEnterSilent;
    public final boolean volumeUpToExitSilent;

    public VolumePolicy(boolean volumeDownToEnterSilent2, boolean volumeUpToExitSilent2, boolean doNotDisturbWhenSilent2, int vibrateToSilentDebounce2) {
        this.volumeDownToEnterSilent = volumeDownToEnterSilent2;
        this.volumeUpToExitSilent = volumeUpToExitSilent2;
        this.doNotDisturbWhenSilent = doNotDisturbWhenSilent2;
        this.vibrateToSilentDebounce = vibrateToSilentDebounce2;
    }

    public String toString() {
        return "VolumePolicy[volumeDownToEnterSilent=" + this.volumeDownToEnterSilent + ",volumeUpToExitSilent=" + this.volumeUpToExitSilent + ",doNotDisturbWhenSilent=" + this.doNotDisturbWhenSilent + ",vibrateToSilentDebounce=" + this.vibrateToSilentDebounce + "]";
    }

    public int hashCode() {
        return Objects.hash(Boolean.valueOf(this.volumeDownToEnterSilent), Boolean.valueOf(this.volumeUpToExitSilent), Boolean.valueOf(this.doNotDisturbWhenSilent), Integer.valueOf(this.vibrateToSilentDebounce));
    }

    public boolean equals(Object o) {
        if (!(o instanceof VolumePolicy)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        VolumePolicy other = (VolumePolicy) o;
        if (other.volumeDownToEnterSilent == this.volumeDownToEnterSilent && other.volumeUpToExitSilent == this.volumeUpToExitSilent && other.doNotDisturbWhenSilent == this.doNotDisturbWhenSilent && other.vibrateToSilentDebounce == this.vibrateToSilentDebounce) {
            return true;
        }
        return false;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.volumeDownToEnterSilent ? 1 : 0);
        dest.writeInt(this.volumeUpToExitSilent ? 1 : 0);
        dest.writeInt(this.doNotDisturbWhenSilent ? 1 : 0);
        dest.writeInt(this.vibrateToSilentDebounce);
    }
}
