package tmsdk.common.module.urlcheck;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdkobf.cf;
import tmsdkobf.cj;
import tmsdkobf.ck;

/* compiled from: Unknown */
public class UrlCheckResultV3 implements Parcelable {
    public static Creator<UrlCheckResultV3> CREATOR;
    public ApkDetail apkDetail;
    public int level;
    public int linkType;
    public int mErrCode;
    public int riskType;
    public String url;
    public WebPageDetail webPageDetail;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.urlcheck.UrlCheckResultV3.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.urlcheck.UrlCheckResultV3.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.urlcheck.UrlCheckResultV3.<clinit>():void");
    }

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

    public UrlCheckResultV3(String str, cj cjVar) {
        this.level = -1;
        this.linkType = -1;
        this.riskType = -1;
        this.webPageDetail = null;
        this.apkDetail = null;
        this.mErrCode = 0;
        this.url = str;
        this.level = cjVar.level;
        this.linkType = cjVar.linkType;
        this.riskType = cjVar.riskType;
        if (cjVar.fj != null) {
            this.webPageDetail = a(cjVar.fj);
        }
        if (cjVar.fk != null) {
            this.apkDetail = a(cjVar.fk);
        }
    }

    private ApkDetail a(cf cfVar) {
        ApkDetail apkDetail = new ApkDetail();
        apkDetail.apkName = cfVar.apkName;
        apkDetail.apkPackage = cfVar.apkPackage;
        apkDetail.iconUrl = cfVar.iconUrl;
        apkDetail.versionCode = cfVar.versionCode;
        apkDetail.versionName = cfVar.versionName;
        apkDetail.size = cfVar.size;
        apkDetail.official = cfVar.official;
        apkDetail.developer = cfVar.developer;
        apkDetail.certMD5 = cfVar.certMD5;
        apkDetail.isInSoftwareDB = cfVar.isInSoftwareDB;
        apkDetail.description = cfVar.description;
        apkDetail.imageUrls = cfVar.imageUrls;
        apkDetail.downloadCount = cfVar.downloadCount;
        apkDetail.source = cfVar.source;
        apkDetail.sensitivePermissions = cfVar.sensitivePermissions;
        apkDetail.virsusName = cfVar.virsusName;
        apkDetail.virsusDescription = cfVar.virsusDescription;
        return apkDetail;
    }

    private WebPageDetail a(ck ckVar) {
        WebPageDetail webPageDetail = new WebPageDetail();
        webPageDetail.title = ckVar.title;
        webPageDetail.description = ckVar.description;
        webPageDetail.webIconUrl = ckVar.webIconUrl;
        webPageDetail.screenshotUrl = ckVar.screenshotUrl;
        webPageDetail.maliceType = ckVar.maliceType;
        webPageDetail.maliceTitle = ckVar.maliceTitle;
        webPageDetail.maliceBody = ckVar.maliceBody;
        webPageDetail.flawName = ckVar.flawName;
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
