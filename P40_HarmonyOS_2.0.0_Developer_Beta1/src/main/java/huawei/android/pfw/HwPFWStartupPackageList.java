package huawei.android.pfw;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class HwPFWStartupPackageList implements Parcelable {
    public static final Parcelable.Creator<HwPFWStartupPackageList> CREATOR = new Parcelable.Creator<HwPFWStartupPackageList>() {
        /* class huawei.android.pfw.HwPFWStartupPackageList.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwPFWStartupPackageList createFromParcel(Parcel source) {
            return new HwPFWStartupPackageList(source);
        }

        @Override // android.os.Parcelable.Creator
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
