package android.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

public class NeighboringCellSsbInfos implements Parcelable {
    public static final Parcelable.Creator<NeighboringCellSsbInfos> CREATOR = new Parcelable.Creator<NeighboringCellSsbInfos>() {
        /* class android.telephony.NeighboringCellSsbInfos.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NeighboringCellSsbInfos createFromParcel(Parcel in) {
            return new NeighboringCellSsbInfos(in);
        }

        @Override // android.os.Parcelable.Creator
        public NeighboringCellSsbInfos[] newArray(int size) {
            return new NeighboringCellSsbInfos[size];
        }
    };
    private int mNbArfcn;
    private int mNbPci;
    private int mNbRsrp;
    private int mNbSinr;
    private ArrayList<SsbIdInfos> mNbSsbIdList = new ArrayList<>();

    public NeighboringCellSsbInfos() {
    }

    @UnsupportedAppUsage
    public NeighboringCellSsbInfos(int nbPci, int nbArfcn, int nbRsrp, int nbSinr, ArrayList<SsbIdInfos> nbSsbIdList) {
        this.mNbPci = nbPci;
        this.mNbArfcn = nbArfcn;
        this.mNbRsrp = nbRsrp;
        this.mNbSinr = nbSinr;
        this.mNbSsbIdList = nbSsbIdList;
    }

    public NeighboringCellSsbInfos(Parcel in) {
        this.mNbPci = in.readInt();
        this.mNbArfcn = in.readInt();
        this.mNbRsrp = in.readInt();
        this.mNbSinr = in.readInt();
        in.readList(this.mNbSsbIdList, SsbIdInfos.class.getClassLoader());
    }

    @UnsupportedAppUsage
    public void setNbPci(int nbPci) {
        this.mNbPci = nbPci;
    }

    public int getNbPci() {
        return this.mNbPci;
    }

    @UnsupportedAppUsage
    public void setNbArfcn(int nbArfcn) {
        this.mNbArfcn = nbArfcn;
    }

    public int getNbArfcn() {
        return this.mNbArfcn;
    }

    @UnsupportedAppUsage
    public void setNbRsrp(int nbRsrp) {
        this.mNbRsrp = nbRsrp;
    }

    public int getNbRsrp() {
        return this.mNbRsrp;
    }

    @UnsupportedAppUsage
    public void setNbSinr(int nbSinr) {
        this.mNbSinr = nbSinr;
    }

    public int getNbSinr() {
        return this.mNbSinr;
    }

    @UnsupportedAppUsage
    public void setNbSsbIdList(ArrayList<SsbIdInfos> nbSsbIdList) {
        this.mNbSsbIdList = nbSsbIdList;
    }

    public ArrayList<SsbIdInfos> getNbSsbIdList() {
        return this.mNbSsbIdList;
    }

    @Override // java.lang.Object
    public String toString() {
        return "[nbPci: ***, nbArfcn: ***, nbRsrp: " + this.mNbRsrp + ", nbSinr: " + this.mNbSinr + ", nbSsbIdList: " + this.mNbSsbIdList + "]";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mNbPci);
        dest.writeInt(this.mNbArfcn);
        dest.writeInt(this.mNbRsrp);
        dest.writeInt(this.mNbSinr);
        dest.writeList(this.mNbSsbIdList);
    }
}
