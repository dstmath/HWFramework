package android.rms.iaware.memrepair;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;

public class MemRepairProcPss implements Parcelable {
    public static final Parcelable.Creator<MemRepairProcPss> CREATOR = new Parcelable.Creator<MemRepairProcPss>() {
        /* class android.rms.iaware.memrepair.MemRepairProcPss.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MemRepairProcPss createFromParcel(Parcel source) {
            return new MemRepairProcPss(source);
        }

        @Override // android.os.Parcelable.Creator
        public MemRepairProcPss[] newArray(int size) {
            return new MemRepairProcPss[size];
        }
    };
    private static final int PSS_SETS_MAX_LENGTH = 200;
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
            sb.append("pssSet=");
            sb.append(Arrays.toString(this.mPssSet));
            sb.append(",procState=");
            sb.append(this.mProcState);
            sb.append(",mergeCount=");
            sb.append(this.mMergeCount);
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
        long[] jArr = this.mPssSet;
        if (jArr == null) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(jArr.length);
        dest.writeLongArray(this.mPssSet);
    }
}
