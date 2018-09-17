package huawei.android.pfw;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class HwPFWStartupPackageList implements Parcelable {
    public static final Creator<HwPFWStartupPackageList> CREATOR = new Creator<HwPFWStartupPackageList>() {
        public HwPFWStartupPackageList createFromParcel(Parcel source) {
            return new HwPFWStartupPackageList(source, null);
        }

        public HwPFWStartupPackageList[] newArray(int size) {
            return new HwPFWStartupPackageList[0];
        }
    };
    public static final int STARTUP_LIST_TYPE_FWK_APP_SYSTEM_BLACK = 3;
    public static final int STARTUP_LIST_TYPE_FWK_APP_THIRD_WHITE = 2;
    public static final int STARTUP_LIST_TYPE_FWK_PREBUILT_SYSTEM_BLACK = 1;
    public static final int STARTUP_LIST_TYPE_FWK_PREBUILT_THIRD_WHITE = 0;
    public static final int STARTUP_LIST_TYPE_MUST_CONTROL_APPS = 4;
    private int mListType;
    private List<String> mPackageList;

    /* synthetic */ HwPFWStartupPackageList(Parcel source, HwPFWStartupPackageList -this1) {
        this(source);
    }

    public HwPFWStartupPackageList(int type) {
        this.mPackageList = new ArrayList();
        this.mListType = type;
        this.mPackageList.clear();
    }

    public void setPackageList(int type, List<String> pkgList) {
        this.mListType = type;
        this.mPackageList.clear();
        this.mPackageList.addAll(pkgList);
    }

    public void copyOutPackageList(List<String> pkgList) {
        pkgList.addAll(this.mPackageList);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mListType);
        dest.writeStringList(this.mPackageList);
    }

    private HwPFWStartupPackageList(Parcel source) {
        this.mPackageList = new ArrayList();
        this.mListType = source.readInt();
        source.readStringList(this.mPackageList);
    }
}
