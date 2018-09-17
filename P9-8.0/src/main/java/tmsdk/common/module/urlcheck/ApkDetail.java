package tmsdk.common.module.urlcheck;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;

public class ApkDetail implements Parcelable {
    public static Creator<ApkDetail> CREATOR = new Creator<ApkDetail>() {
        /* renamed from: bL */
        public ApkDetail[] newArray(int i) {
            return new ApkDetail[i];
        }

        /* renamed from: p */
        public ApkDetail createFromParcel(Parcel parcel) {
            boolean z = false;
            ApkDetail apkDetail = new ApkDetail();
            apkDetail.apkPackage = parcel.readString();
            apkDetail.apkName = parcel.readString();
            apkDetail.iconUrl = parcel.readString();
            apkDetail.versionCode = parcel.readInt();
            apkDetail.versionName = parcel.readString();
            apkDetail.size = parcel.readLong();
            apkDetail.official = parcel.readInt();
            apkDetail.developer = parcel.readString();
            apkDetail.certMD5 = parcel.readString();
            if (parcel.readInt() != 0) {
                z = true;
            }
            apkDetail.isInSoftwareDB = z;
            apkDetail.description = parcel.readString();
            if (apkDetail.imageUrls == null) {
                apkDetail.imageUrls = new ArrayList();
            }
            parcel.readStringList(apkDetail.imageUrls);
            apkDetail.downloadCount = parcel.readInt();
            apkDetail.source = parcel.readString();
            if (apkDetail.sensitivePermissions == null) {
                apkDetail.sensitivePermissions = new ArrayList();
            }
            parcel.readStringList(apkDetail.sensitivePermissions);
            apkDetail.virsusName = parcel.readString();
            apkDetail.virsusDescription = parcel.readString();
            return apkDetail;
        }
    };
    public String apkName = "";
    public String apkPackage = "";
    public String certMD5 = "";
    public String description = "";
    public String developer = "";
    public int downloadCount = 0;
    public String iconUrl = "";
    public ArrayList<String> imageUrls = null;
    public boolean isInSoftwareDB = false;
    public int official = 0;
    public ArrayList<String> sensitivePermissions = null;
    public long size = 0;
    public String source = "";
    public int versionCode = 0;
    public String versionName = "";
    public String virsusDescription = "";
    public String virsusName = "";

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        int i2 = 0;
        parcel.writeString(this.apkPackage);
        parcel.writeString(this.apkName);
        parcel.writeString(this.iconUrl);
        parcel.writeInt(this.versionCode);
        parcel.writeString(this.versionName);
        parcel.writeLong(this.size);
        parcel.writeInt(this.official);
        parcel.writeString(this.developer);
        parcel.writeString(this.certMD5);
        if (this.isInSoftwareDB) {
            i2 = 1;
        }
        parcel.writeInt(i2);
        parcel.writeString(this.description);
        parcel.writeStringList(this.imageUrls);
        parcel.writeInt(this.downloadCount);
        parcel.writeString(this.source);
        parcel.writeStringList(this.sensitivePermissions);
        parcel.writeString(this.virsusName);
        parcel.writeString(this.virsusDescription);
    }
}
