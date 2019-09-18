package huawei.android.pfw;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class HwPFWStartupControlScope implements Parcelable {
    public static final Parcelable.Creator<HwPFWStartupControlScope> CREATOR = new Parcelable.Creator<HwPFWStartupControlScope>() {
        public HwPFWStartupControlScope createFromParcel(Parcel source) {
            return new HwPFWStartupControlScope(source);
        }

        public HwPFWStartupControlScope[] newArray(int size) {
            return new HwPFWStartupControlScope[0];
        }
    };
    private List<String> mSystemBlackPackages;
    private List<String> mThirdWhitePackages;

    public HwPFWStartupControlScope() {
        this.mSystemBlackPackages = new ArrayList();
        this.mThirdWhitePackages = new ArrayList();
    }

    public void setScope(List<String> systemBlack, List<String> thirdWhite) {
        clear();
        this.mSystemBlackPackages.addAll(systemBlack);
        this.mThirdWhitePackages.addAll(thirdWhite);
    }

    public void copyOutScope(List<String> systemBlack, List<String> thirdWhite) {
        systemBlack.addAll(this.mSystemBlackPackages);
        thirdWhite.addAll(this.mThirdWhitePackages);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(this.mSystemBlackPackages);
        dest.writeStringList(this.mThirdWhitePackages);
    }

    private HwPFWStartupControlScope(Parcel source) {
        this.mSystemBlackPackages = new ArrayList();
        this.mThirdWhitePackages = new ArrayList();
        source.readStringList(this.mSystemBlackPackages);
        source.readStringList(this.mThirdWhitePackages);
    }

    private void clear() {
        this.mSystemBlackPackages.clear();
        this.mThirdWhitePackages.clear();
    }
}
