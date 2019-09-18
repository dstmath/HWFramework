package huawei.cust.aidl;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class SimMatchRule implements Parcelable {
    public static final Parcelable.Creator<SimMatchRule> CREATOR = new Parcelable.Creator<SimMatchRule>() {
        public SimMatchRule createFromParcel(Parcel in) {
            return new SimMatchRule(in);
        }

        public SimMatchRule[] newArray(int size) {
            return new SimMatchRule[size];
        }
    };
    private int rule;
    List<SpecialFile> specialFiles = new ArrayList();

    public SimMatchRule() {
    }

    protected SimMatchRule(Parcel in) {
        this.rule = in.readInt();
        this.specialFiles = in.createTypedArrayList(SpecialFile.CREATOR);
    }

    public int getRule() {
        return this.rule;
    }

    public void setRule(int rule2) {
        this.rule = rule2;
    }

    public List<SpecialFile> getSpecialFiles() {
        return this.specialFiles;
    }

    public void setSpecialFiles(List<SpecialFile> specialFiles2) {
        this.specialFiles = specialFiles2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.rule);
        dest.writeTypedList(this.specialFiles);
    }

    public String toString() {
        return "SimMatchRule{rule=" + this.rule + ", specialFiles=" + this.specialFiles + '}';
    }
}
