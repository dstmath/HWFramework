package android.rms.iaware.memrepair;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

public class MemRepairProcPss implements Parcelable {
    public static final Creator<MemRepairProcPss> CREATOR = new Creator<MemRepairProcPss>() {
        public MemRepairProcPss createFromParcel(Parcel source) {
            return new MemRepairProcPss(source);
        }

        public MemRepairProcPss[] newArray(int size) {
            return new MemRepairProcPss[size];
        }
    };
    private int mMergeCount;
    private int mProcState;
    private long[] mPssSet;

    public MemRepairProcPss(long[] pssSets, int setsCount, int procState, int mergeCount) {
        if (pssSets != null && pssSets.length > 0 && pssSets.length <= 200 && setsCount > 0 && setsCount <= pssSets.length) {
            this.mPssSet = new long[setsCount];
            System.arraycopy(pssSets, 0, this.mPssSet, 0, setsCount);
        }
        this.mProcState = procState;
        this.mMergeCount = mergeCount;
    }

    protected MemRepairProcPss(Parcel source) {
        this.mProcState = source.readInt();
        this.mMergeCount = source.readInt();
        int pssSize = source.readInt();
        if (pssSize > 0 && pssSize <= 200) {
            this.mPssSet = new long[pssSize];
            source.readLongArray(this.mPssSet);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        if (this.mPssSet != null) {
            sb.append("pssSet=").append(Arrays.toString(this.mPssSet));
            sb.append(",procState=").append(this.mProcState);
            sb.append(",mergeCount=").append(this.mMergeCount);
        }
        sb.append("]");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mProcState);
        dest.writeInt(this.mMergeCount);
        if (this.mPssSet == null) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(this.mPssSet.length);
        dest.writeLongArray(this.mPssSet);
    }
}
