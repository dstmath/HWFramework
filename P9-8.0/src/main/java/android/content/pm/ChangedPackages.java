package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.List;

public final class ChangedPackages implements Parcelable {
    public static final Creator<ChangedPackages> CREATOR = new Creator<ChangedPackages>() {
        public ChangedPackages createFromParcel(Parcel in) {
            return new ChangedPackages(in);
        }

        public ChangedPackages[] newArray(int size) {
            return new ChangedPackages[size];
        }
    };
    private final List<String> mPackageNames;
    private final int mSequenceNumber;

    public ChangedPackages(int sequenceNumber, List<String> packageNames) {
        this.mSequenceNumber = sequenceNumber;
        this.mPackageNames = packageNames;
    }

    protected ChangedPackages(Parcel in) {
        this.mSequenceNumber = in.readInt();
        this.mPackageNames = in.createStringArrayList();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSequenceNumber);
        dest.writeStringList(this.mPackageNames);
    }

    public int getSequenceNumber() {
        return this.mSequenceNumber;
    }

    public List<String> getPackageNames() {
        return this.mPackageNames;
    }
}
