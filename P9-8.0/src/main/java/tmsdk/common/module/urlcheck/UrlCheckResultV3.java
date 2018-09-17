package tmsdk.common.module.urlcheck;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdkobf.ct;
import tmsdkobf.cx;
import tmsdkobf.cy;

public class UrlCheckResultV3 implements Parcelable {
    public static Creator<UrlCheckResultV3> CREATOR = new Creator<UrlCheckResultV3>() {
        /* renamed from: bN */
        public UrlCheckResultV3[] newArray(int i) {
            return new UrlCheckResultV3[i];
        }

        /* renamed from: r */
        public UrlCheckResultV3 createFromParcel(Parcel parcel) {
            UrlCheckResultV3 urlCheckResultV3 = new UrlCheckResultV3();
            urlCheckResultV3.url = parcel.readString();
            urlCheckResultV3.level = parcel.readInt();
            urlCheckResultV3.linkType = parcel.readInt();
            urlCheckResultV3.riskType = parcel.readInt();
            if (urlCheckResultV3.linkType != 0) {
                urlCheckResultV3.apkDetail = (ApkDetail) parcel.readParcelable(ApkDetail.class.getClassLoader());
            } else {
                urlCheckResultV3.webPageDetail = (WebPageDetail) parcel.readParcelable(WebPageDetail.class.getClassLoader());
            }
            urlCheckResultV3.mErrCode = parcel.readInt();
            return urlCheckResultV3;
        }
    };
    public ApkDetail apkDetail;
    public int level;
    public int linkType;
    public int mErrCode;
    public int riskType;
    public String url;
    public WebPageDetail webPageDetail;

    private UrlCheckResultV3() {
        this.level = -1;
        this.linkType = -1;
        this.riskType = -1;
        this.webPageDetail = null;
        this.apkDetail = null;
        this.mErrCode = 0;
    }

    public UrlCheckResultV3(int i) {
        this.level = -1;
        this.linkType = -1;
        this.riskType = -1;
        this.webPageDetail = null;
        this.apkDetail = null;
        this.mErrCode = 0;
        this.mErrCode = i;
    }

    public UrlCheckResultV3(String str, cx cxVar) {
        this.level = -1;
        this.linkType = -1;
        this.riskType = -1;
        this.webPageDetail = null;
        this.apkDetail = null;
        this.mErrCode = 0;
        this.url = str;
        this.level = cxVar.level;
        this.linkType = cxVar.linkType;
        this.riskType = cxVar.riskType;
        if (cxVar.gd != null) {
            this.webPageDetail = a(cxVar.gd);
        }
        if (cxVar.ge != null) {
            this.apkDetail = a(cxVar.ge);
        }
    }

    private ApkDetail a(ct ctVar) {
        ApkDetail apkDetail = new ApkDetail();
        apkDetail.apkName = ctVar.apkName;
        apkDetail.apkPackage = ctVar.apkPackage;
        apkDetail.iconUrl = ctVar.iconUrl;
        apkDetail.versionCode = ctVar.versionCode;
        apkDetail.versionName = ctVar.versionName;
        apkDetail.size = ctVar.size;
        apkDetail.official = ctVar.official;
        apkDetail.developer = ctVar.developer;
        apkDetail.certMD5 = ctVar.certMD5;
        apkDetail.isInSoftwareDB = ctVar.isInSoftwareDB;
        apkDetail.description = ctVar.description;
        apkDetail.imageUrls = ctVar.imageUrls;
        apkDetail.downloadCount = ctVar.downloadCount;
        apkDetail.source = ctVar.source;
        apkDetail.sensitivePermissions = ctVar.sensitivePermissions;
        apkDetail.virsusName = ctVar.virsusName;
        apkDetail.virsusDescription = ctVar.virsusDescription;
        return apkDetail;
    }

    private WebPageDetail a(cy cyVar) {
        WebPageDetail webPageDetail = new WebPageDetail();
        webPageDetail.title = cyVar.title;
        webPageDetail.description = cyVar.description;
        webPageDetail.webIconUrl = cyVar.webIconUrl;
        webPageDetail.screenshotUrl = cyVar.screenshotUrl;
        webPageDetail.maliceType = cyVar.maliceType;
        webPageDetail.maliceTitle = cyVar.maliceTitle;
        webPageDetail.maliceBody = cyVar.maliceBody;
        webPageDetail.flawName = cyVar.flawName;
        return webPageDetail;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.url);
        parcel.writeInt(this.level);
        parcel.writeInt(this.linkType);
        parcel.writeInt(this.riskType);
        if (this.linkType != 0) {
            parcel.writeParcelable(this.apkDetail, 0);
        } else {
            parcel.writeParcelable(this.webPageDetail, 0);
        }
        parcel.writeInt(this.mErrCode);
    }
}
