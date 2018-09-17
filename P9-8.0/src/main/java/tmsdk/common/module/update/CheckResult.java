package tmsdk.common.module.update;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class CheckResult implements Parcelable {
    public static final Creator<CheckResult> CREATOR = new Creator<CheckResult>() {
        /* renamed from: bH */
        public CheckResult[] newArray(int i) {
            return new CheckResult[i];
        }

        /* renamed from: n */
        public CheckResult createFromParcel(Parcel parcel) {
            return new CheckResult(parcel);
        }
    };
    public String mMessage;
    public String mTitle;
    public List<UpdateInfo> mUpdateInfoList;

    public CheckResult(Parcel parcel) {
        readFromParcel(parcel);
    }

    private void readFromParcel(Parcel parcel) {
        this.mTitle = parcel.readString();
        this.mMessage = parcel.readString();
        this.mUpdateInfoList = new ArrayList();
        parcel.readList(this.mUpdateInfoList, getClass().getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mTitle);
        parcel.writeString(this.mMessage);
        parcel.writeList(this.mUpdateInfoList);
    }
}
