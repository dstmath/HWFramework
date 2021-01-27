package huawei.android.jankshield;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;

public class JankProductInfo implements Parcelable {
    public static final Parcelable.Creator<JankProductInfo> CREATOR = new Parcelable.Creator<JankProductInfo>() {
        /* class huawei.android.jankshield.JankProductInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public JankProductInfo createFromParcel(Parcel in) {
            return new JankProductInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public JankProductInfo[] newArray(int size) {
            return new JankProductInfo[size];
        }
    };
    public static final String DEFAULT_DEVICE_ID = "000000000000000";
    public String productIMEI;
    public String productName;
    public String productSN;
    public String productVersion;

    public JankProductInfo() {
        this.productName = SystemProperties.get("ro.product.name", "NULL");
        this.productSN = SystemProperties.get("ro.serialno", "NULL");
        this.productVersion = getVersionString();
    }

    private JankProductInfo(Parcel in) {
        this.productName = SystemProperties.get("ro.product.name", "NULL");
        this.productSN = SystemProperties.get("ro.serialno", "NULL");
        this.productName = in.readString();
        this.productVersion = in.readString();
        this.productSN = in.readString();
        this.productIMEI = in.readString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flag) {
        dest.writeString(this.productName);
        dest.writeString(this.productVersion);
        dest.writeString(this.productSN);
        dest.writeString(this.productIMEI);
    }

    public static String getVersionString() {
        String[] version = {SystemProperties.get("ro.build.realversion.id", "NULL"), SystemProperties.get("ro.build.cust.id", "NULL"), SystemProperties.get("ro.build.display.id", "NULL")};
        String fullVersionId = Build.DISPLAY;
        for (String s : version) {
            if (!"NULL".equals(s)) {
                return s;
            }
        }
        return fullVersionId;
    }
}
