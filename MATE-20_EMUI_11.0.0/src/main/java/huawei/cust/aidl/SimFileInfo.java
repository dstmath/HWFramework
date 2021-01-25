package huawei.cust.aidl;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class SimFileInfo implements Parcelable {
    public static final Parcelable.Creator<SimFileInfo> CREATOR = new Parcelable.Creator<SimFileInfo>() {
        /* class huawei.cust.aidl.SimFileInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SimFileInfo createFromParcel(Parcel in) {
            return new SimFileInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public SimFileInfo[] newArray(int size) {
            return new SimFileInfo[size];
        }
    };
    private String gid1;
    private String gid2;
    private String iccid;
    private String imsi;
    private String mccMnc;
    private List<SpecialFile> specialFiles = new ArrayList();
    private String spn;

    public SimFileInfo() {
    }

    protected SimFileInfo(Parcel in) {
        this.mccMnc = in.readString();
        this.imsi = in.readString();
        this.iccid = in.readString();
        this.spn = in.readString();
        this.gid1 = in.readString();
        this.gid2 = in.readString();
        this.specialFiles = in.createTypedArrayList(SpecialFile.CREATOR);
    }

    public String getMccMnc() {
        return this.mccMnc;
    }

    public void setMccMnc(String mccMnc2) {
        this.mccMnc = mccMnc2;
    }

    public String getImsi() {
        return this.imsi;
    }

    public void setImsi(String imsi2) {
        this.imsi = imsi2;
    }

    public String getIccid() {
        return this.iccid;
    }

    public void setIccid(String iccid2) {
        this.iccid = iccid2;
    }

    public String getSpn() {
        return this.spn;
    }

    public void setSpn(String spn2) {
        this.spn = spn2;
    }

    public String getGid1() {
        return this.gid1;
    }

    public void setGid1(String gid12) {
        this.gid1 = gid12;
    }

    public String getGid2() {
        return this.gid2;
    }

    public void setGid2(String gid22) {
        this.gid2 = gid22;
    }

    public List<SpecialFile> getSpecialFiles() {
        return this.specialFiles;
    }

    public void setSpecialFiles(List<SpecialFile> specialFiles2) {
        this.specialFiles = specialFiles2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mccMnc);
        dest.writeString(this.imsi);
        dest.writeString(this.iccid);
        dest.writeString(this.spn);
        dest.writeString(this.gid1);
        dest.writeString(this.gid2);
        dest.writeTypedList(this.specialFiles);
    }

    @Override // java.lang.Object
    public String toString() {
        return "SimFileInfo{mccMnc='" + this.mccMnc + "', imsi='" + givePrintableMsg(this.imsi) + "', iccid='" + givePrintableMsg(this.iccid) + "', spn='" + this.spn + "', gid1='" + this.gid1 + "', gid2='" + this.gid2 + "', specialFiles=" + this.specialFiles + '}';
    }

    private static String givePrintableMsg(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() <= 6) {
            return value;
        }
        return value.substring(0, 6) + "XXXXXXXXXXX";
    }
}
