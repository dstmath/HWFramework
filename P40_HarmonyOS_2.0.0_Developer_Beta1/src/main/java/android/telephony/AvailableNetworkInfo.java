package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class AvailableNetworkInfo implements Parcelable {
    public static final Parcelable.Creator<AvailableNetworkInfo> CREATOR = new Parcelable.Creator<AvailableNetworkInfo>() {
        /* class android.telephony.AvailableNetworkInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AvailableNetworkInfo createFromParcel(Parcel in) {
            return new AvailableNetworkInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public AvailableNetworkInfo[] newArray(int size) {
            return new AvailableNetworkInfo[size];
        }
    };
    public static final int PRIORITY_HIGH = 1;
    public static final int PRIORITY_LOW = 3;
    public static final int PRIORITY_MED = 2;
    private ArrayList<Integer> mBands;
    private ArrayList<String> mMccMncs;
    private int mPriority;
    private int mSubId;

    public int getSubId() {
        return this.mSubId;
    }

    public int getPriority() {
        return this.mPriority;
    }

    public List<String> getMccMncs() {
        return (List) this.mMccMncs.clone();
    }

    public List<Integer> getBands() {
        return (List) this.mBands.clone();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSubId);
        dest.writeInt(this.mPriority);
        dest.writeStringList(this.mMccMncs);
        dest.writeList(this.mBands);
    }

    private AvailableNetworkInfo(Parcel in) {
        this.mSubId = in.readInt();
        this.mPriority = in.readInt();
        this.mMccMncs = new ArrayList<>();
        in.readStringList(this.mMccMncs);
        this.mBands = new ArrayList<>();
        in.readList(this.mBands, Integer.class.getClassLoader());
    }

    public AvailableNetworkInfo(int subId, int priority, List<String> mccMncs, List<Integer> bands) {
        this.mSubId = subId;
        this.mPriority = priority;
        this.mMccMncs = new ArrayList<>(mccMncs);
        this.mBands = new ArrayList<>(bands);
    }

    public boolean equals(Object o) {
        ArrayList<String> arrayList;
        try {
            AvailableNetworkInfo ani = (AvailableNetworkInfo) o;
            if (o != null && this.mSubId == ani.mSubId && this.mPriority == ani.mPriority && (arrayList = this.mMccMncs) != null && arrayList.equals(ani.mMccMncs) && this.mBands.equals(ani.mBands)) {
                return true;
            }
            return false;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mSubId), Integer.valueOf(this.mPriority), this.mMccMncs, this.mBands);
    }

    public String toString() {
        return "AvailableNetworkInfo: mSubId: " + this.mSubId + " mPriority: " + this.mPriority + " mMccMncs: " + Arrays.toString(this.mMccMncs.toArray()) + " mBands: " + Arrays.toString(this.mBands.toArray());
    }
}
