package android.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Objects;

public class NrCellSsbId implements Parcelable {
    public static final Parcelable.Creator<NrCellSsbId> CREATOR = new Parcelable.Creator<NrCellSsbId>() {
        /* class android.telephony.NrCellSsbId.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NrCellSsbId createFromParcel(Parcel in) {
            return new NrCellSsbId(in);
        }

        @Override // android.os.Parcelable.Creator
        public NrCellSsbId[] newArray(int size) {
            return new NrCellSsbId[size];
        }
    };
    private int mArfcn;
    private long mCid;
    private int mNbCellCount;
    private ArrayList<NeighboringCellSsbInfos> mNbCellSsbList = new ArrayList<>();
    private int mPci;
    private int mRsrp;
    private int mSinr;
    private ArrayList<SsbIdInfos> mSsbCellSsbList = new ArrayList<>();
    private int mTimeAdvance;

    public NrCellSsbId() {
    }

    @UnsupportedAppUsage
    public NrCellSsbId(int arfcn, long cid, int pci, int rsrp, int sinr, int timeAdvance, ArrayList<SsbIdInfos> ssbCellSsbList, int nbCellCount, ArrayList<NeighboringCellSsbInfos> nbCellSsbList) {
        this.mArfcn = arfcn;
        this.mCid = cid;
        this.mPci = pci;
        this.mRsrp = rsrp;
        this.mSinr = sinr;
        this.mTimeAdvance = timeAdvance;
        this.mSsbCellSsbList = ssbCellSsbList;
        this.mNbCellCount = nbCellCount;
        this.mNbCellSsbList = nbCellSsbList;
    }

    public NrCellSsbId(Parcel in) {
        this.mArfcn = in.readInt();
        this.mCid = in.readLong();
        this.mPci = in.readInt();
        this.mRsrp = in.readInt();
        this.mSinr = in.readInt();
        this.mTimeAdvance = in.readInt();
        in.readList(this.mSsbCellSsbList, SsbIdInfos.class.getClassLoader());
        this.mNbCellCount = in.readInt();
        in.readList(this.mNbCellSsbList, NeighboringCellSsbInfos.class.getClassLoader());
    }

    @UnsupportedAppUsage
    public void setArfcn(int arfcn) {
        this.mArfcn = arfcn;
    }

    public int getArfcn() {
        return this.mArfcn;
    }

    @UnsupportedAppUsage
    public void setCid(long cid) {
        this.mCid = cid;
    }

    public long getCid() {
        return this.mCid;
    }

    @UnsupportedAppUsage
    public void setPci(int pci) {
        this.mPci = pci;
    }

    public int getPci() {
        return this.mPci;
    }

    @UnsupportedAppUsage
    public void setRsrp(int rsrp) {
        this.mRsrp = rsrp;
    }

    public int getRsrp() {
        return this.mRsrp;
    }

    @UnsupportedAppUsage
    public void setSinr(int sinr) {
        this.mSinr = sinr;
    }

    public int getSinr() {
        return this.mSinr;
    }

    @UnsupportedAppUsage
    public void setTimeAdvance(int timeAdvance) {
        this.mTimeAdvance = timeAdvance;
    }

    public int getTimeAdvance() {
        return this.mTimeAdvance;
    }

    @UnsupportedAppUsage
    public void setSsbCellSsbList(ArrayList<SsbIdInfos> ssbCellSsbList) {
        this.mSsbCellSsbList = ssbCellSsbList;
    }

    public ArrayList<SsbIdInfos> getSsbCellSsbList() {
        return this.mSsbCellSsbList;
    }

    @UnsupportedAppUsage
    public void setNbCellCount(int nbCellCount) {
        this.mNbCellCount = nbCellCount;
    }

    public int getNbCellCount() {
        return this.mNbCellCount;
    }

    @UnsupportedAppUsage
    public void setNbCellSsbList(ArrayList<NeighboringCellSsbInfos> nbCellSsbList) {
        this.mNbCellSsbList = nbCellSsbList;
    }

    public ArrayList<NeighboringCellSsbInfos> getNbCellSsbList() {
        return this.mNbCellSsbList;
    }

    @Override // java.lang.Object
    public String toString() {
        return "[arfcn: **, cid: **, pci: **, rsrp: " + this.mRsrp + ", sinr: " + this.mSinr + ", timeAdvance: **, ssbCellSsbList: " + this.mSsbCellSsbList + ", nbCellCount: " + this.mNbCellCount + ", nbCellSsbList: " + this.mNbCellSsbList + "]";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mArfcn), Long.valueOf(this.mCid), Integer.valueOf(this.mPci), Integer.valueOf(this.mRsrp), Integer.valueOf(this.mSinr), Integer.valueOf(this.mTimeAdvance), this.mSsbCellSsbList, Integer.valueOf(this.mNbCellCount), this.mNbCellSsbList);
    }

    @Override // java.lang.Object
    public boolean equals(Object object) {
        if (!(object instanceof NrCellSsbId)) {
            return false;
        }
        NrCellSsbId other = (NrCellSsbId) object;
        if (this.mArfcn == other.mArfcn && this.mCid == other.mCid && this.mPci == other.mPci && this.mRsrp == other.mRsrp && this.mSinr == other.mSinr && this.mTimeAdvance == other.mTimeAdvance && this.mSsbCellSsbList.size() == other.mSsbCellSsbList.size() && this.mSsbCellSsbList.containsAll(other.mSsbCellSsbList) && this.mNbCellCount == other.mNbCellCount && this.mNbCellSsbList.size() == other.mNbCellSsbList.size() && this.mNbCellSsbList.containsAll(other.mNbCellSsbList)) {
            return true;
        }
        return false;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mArfcn);
        dest.writeLong(this.mCid);
        dest.writeInt(this.mPci);
        dest.writeInt(this.mRsrp);
        dest.writeInt(this.mSinr);
        dest.writeInt(this.mTimeAdvance);
        dest.writeList(this.mSsbCellSsbList);
        dest.writeInt(this.mNbCellCount);
        dest.writeList(this.mNbCellSsbList);
    }
}
