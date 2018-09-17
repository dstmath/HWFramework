package android.os;

import android.os.Parcelable.Creator;

public class JankProductInfo implements Parcelable {
    public static final Creator<JankProductInfo> CREATOR = new Creator<JankProductInfo>() {
        public JankProductInfo createFromParcel(Parcel in) {
            return new JankProductInfo(in, null);
        }

        public JankProductInfo[] newArray(int size) {
            return new JankProductInfo[size];
        }
    };
    public static final String DEFAULT_DEVICE_ID = "000000000000000";
    public String productIMEI;
    public String productName;
    public String productSN;
    public String productVersion;

    /* synthetic */ JankProductInfo(Parcel in, JankProductInfo -this1) {
        this(in);
    }

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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flag) {
        dest.writeString(this.productName);
        dest.writeString(this.productVersion);
        dest.writeString(this.productSN);
        dest.writeString(this.productIMEI);
    }

    public static String getVersionString() {
        int i = 0;
        String[] version = new String[]{SystemProperties.get("ro.build.realversion.id", "NULL"), SystemProperties.get("ro.build.cust.id", "NULL"), SystemProperties.get("ro.build.display.id", "NULL")};
        String fullVersionId = Build.DISPLAY;
        int length = version.length;
        while (i < length) {
            String s = version[i];
            if (!"NULL".equals(s)) {
                return s;
            }
            i++;
        }
        return fullVersionId;
    }
}
