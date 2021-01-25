package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DSDigest extends AManagedObject {
    public static final Parcelable.Creator<DSDigest> CREATOR = new Parcelable.Creator<DSDigest>() {
        /* class com.huawei.nb.model.collectencrypt.DSDigest.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DSDigest createFromParcel(Parcel parcel) {
            return new DSDigest(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DSDigest[] newArray(int i) {
            return new DSDigest[i];
        }
    };
    private String comeFrom;
    private String createdtime;
    private Integer deleted;
    private String excerpt;
    private String extra;
    private String htmlDigest;
    private String htmlPath;
    private Integer id;
    private Integer isDownload = 0;
    private Integer isImgLoaded = 0;
    private String isLoaded;
    private String isMhtHastitle;
    private Integer isUpload = 0;
    private String localUrl;
    private String mhtUtl;
    private String pageUrl;
    private String params;
    private String pkgName;
    private String reserved0;
    private String reserved1;
    private String reserved2;
    private String reserved3;
    private String reserved4;
    private String reserved5;
    private String serverId;
    private String source;
    private String sourceTime;
    private Integer syncCount = 0;
    private String thumbnail;
    private String thumbnailUrl;
    private String title;
    private String uniqueFlag;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String getDatabaseVersion() {
        return "0.0.14";
    }

    public int getDatabaseVersionCode() {
        return 14;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.DSDigest";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public DSDigest(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.title = cursor.getString(2);
        this.pageUrl = cursor.getString(3);
        this.serverId = cursor.getString(4);
        this.mhtUtl = cursor.getString(5);
        this.thumbnail = cursor.getString(6);
        this.htmlPath = cursor.getString(7);
        this.createdtime = cursor.getString(8);
        this.comeFrom = cursor.getString(9);
        this.localUrl = cursor.getString(10);
        this.excerpt = cursor.getString(11);
        this.uniqueFlag = cursor.getString(12);
        this.isLoaded = cursor.getString(13);
        this.deleted = cursor.isNull(14) ? null : Integer.valueOf(cursor.getInt(14));
        this.thumbnailUrl = cursor.getString(15);
        this.isImgLoaded = cursor.isNull(16) ? null : Integer.valueOf(cursor.getInt(16));
        this.isUpload = cursor.isNull(17) ? null : Integer.valueOf(cursor.getInt(17));
        this.isDownload = cursor.isNull(18) ? null : Integer.valueOf(cursor.getInt(18));
        this.syncCount = !cursor.isNull(19) ? Integer.valueOf(cursor.getInt(19)) : num;
        this.extra = cursor.getString(20);
        this.pkgName = cursor.getString(21);
        this.source = cursor.getString(22);
        this.sourceTime = cursor.getString(23);
        this.params = cursor.getString(24);
        this.isMhtHastitle = cursor.getString(25);
        this.htmlDigest = cursor.getString(26);
        this.reserved0 = cursor.getString(27);
        this.reserved1 = cursor.getString(28);
        this.reserved2 = cursor.getString(29);
        this.reserved3 = cursor.getString(30);
        this.reserved4 = cursor.getString(31);
        this.reserved5 = cursor.getString(32);
    }

    public DSDigest(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.title = parcel.readByte() == 0 ? null : parcel.readString();
        this.pageUrl = parcel.readByte() == 0 ? null : parcel.readString();
        this.serverId = parcel.readByte() == 0 ? null : parcel.readString();
        this.mhtUtl = parcel.readByte() == 0 ? null : parcel.readString();
        this.thumbnail = parcel.readByte() == 0 ? null : parcel.readString();
        this.htmlPath = parcel.readByte() == 0 ? null : parcel.readString();
        this.createdtime = parcel.readByte() == 0 ? null : parcel.readString();
        this.comeFrom = parcel.readByte() == 0 ? null : parcel.readString();
        this.localUrl = parcel.readByte() == 0 ? null : parcel.readString();
        this.excerpt = parcel.readByte() == 0 ? null : parcel.readString();
        this.uniqueFlag = parcel.readByte() == 0 ? null : parcel.readString();
        this.isLoaded = parcel.readByte() == 0 ? null : parcel.readString();
        this.deleted = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.thumbnailUrl = parcel.readByte() == 0 ? null : parcel.readString();
        this.isImgLoaded = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.isUpload = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.isDownload = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.syncCount = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.extra = parcel.readByte() == 0 ? null : parcel.readString();
        this.pkgName = parcel.readByte() == 0 ? null : parcel.readString();
        this.source = parcel.readByte() == 0 ? null : parcel.readString();
        this.sourceTime = parcel.readByte() == 0 ? null : parcel.readString();
        this.params = parcel.readByte() == 0 ? null : parcel.readString();
        this.isMhtHastitle = parcel.readByte() == 0 ? null : parcel.readString();
        this.htmlDigest = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved0 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved3 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved4 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved5 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private DSDigest(Integer num, String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, String str10, String str11, String str12, Integer num2, String str13, Integer num3, Integer num4, Integer num5, Integer num6, String str14, String str15, String str16, String str17, String str18, String str19, String str20, String str21, String str22, String str23, String str24, String str25, String str26) {
        this.id = num;
        this.title = str;
        this.pageUrl = str2;
        this.serverId = str3;
        this.mhtUtl = str4;
        this.thumbnail = str5;
        this.htmlPath = str6;
        this.createdtime = str7;
        this.comeFrom = str8;
        this.localUrl = str9;
        this.excerpt = str10;
        this.uniqueFlag = str11;
        this.isLoaded = str12;
        this.deleted = num2;
        this.thumbnailUrl = str13;
        this.isImgLoaded = num3;
        this.isUpload = num4;
        this.isDownload = num5;
        this.syncCount = num6;
        this.extra = str14;
        this.pkgName = str15;
        this.source = str16;
        this.sourceTime = str17;
        this.params = str18;
        this.isMhtHastitle = str19;
        this.htmlDigest = str20;
        this.reserved0 = str21;
        this.reserved1 = str22;
        this.reserved2 = str23;
        this.reserved3 = str24;
        this.reserved4 = str25;
        this.reserved5 = str26;
    }

    public DSDigest() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String str) {
        this.title = str;
        setValue();
    }

    public String getPageUrl() {
        return this.pageUrl;
    }

    public void setPageUrl(String str) {
        this.pageUrl = str;
        setValue();
    }

    public String getServerId() {
        return this.serverId;
    }

    public void setServerId(String str) {
        this.serverId = str;
        setValue();
    }

    public String getMhtUtl() {
        return this.mhtUtl;
    }

    public void setMhtUtl(String str) {
        this.mhtUtl = str;
        setValue();
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public void setThumbnail(String str) {
        this.thumbnail = str;
        setValue();
    }

    public String getHtmlPath() {
        return this.htmlPath;
    }

    public void setHtmlPath(String str) {
        this.htmlPath = str;
        setValue();
    }

    public String getCreatedtime() {
        return this.createdtime;
    }

    public void setCreatedtime(String str) {
        this.createdtime = str;
        setValue();
    }

    public String getComeFrom() {
        return this.comeFrom;
    }

    public void setComeFrom(String str) {
        this.comeFrom = str;
        setValue();
    }

    public String getLocalUrl() {
        return this.localUrl;
    }

    public void setLocalUrl(String str) {
        this.localUrl = str;
        setValue();
    }

    public String getExcerpt() {
        return this.excerpt;
    }

    public void setExcerpt(String str) {
        this.excerpt = str;
        setValue();
    }

    public String getUniqueFlag() {
        return this.uniqueFlag;
    }

    public void setUniqueFlag(String str) {
        this.uniqueFlag = str;
        setValue();
    }

    public String getIsLoaded() {
        return this.isLoaded;
    }

    public void setIsLoaded(String str) {
        this.isLoaded = str;
        setValue();
    }

    public Integer getDeleted() {
        return this.deleted;
    }

    public void setDeleted(Integer num) {
        this.deleted = num;
        setValue();
    }

    public String getThumbnailUrl() {
        return this.thumbnailUrl;
    }

    public void setThumbnailUrl(String str) {
        this.thumbnailUrl = str;
        setValue();
    }

    public Integer getIsImgLoaded() {
        return this.isImgLoaded;
    }

    public void setIsImgLoaded(Integer num) {
        this.isImgLoaded = num;
        setValue();
    }

    public Integer getIsUpload() {
        return this.isUpload;
    }

    public void setIsUpload(Integer num) {
        this.isUpload = num;
        setValue();
    }

    public Integer getIsDownload() {
        return this.isDownload;
    }

    public void setIsDownload(Integer num) {
        this.isDownload = num;
        setValue();
    }

    public Integer getSyncCount() {
        return this.syncCount;
    }

    public void setSyncCount(Integer num) {
        this.syncCount = num;
        setValue();
    }

    public String getExtra() {
        return this.extra;
    }

    public void setExtra(String str) {
        this.extra = str;
        setValue();
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String str) {
        this.pkgName = str;
        setValue();
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String str) {
        this.source = str;
        setValue();
    }

    public String getSourceTime() {
        return this.sourceTime;
    }

    public void setSourceTime(String str) {
        this.sourceTime = str;
        setValue();
    }

    public String getParams() {
        return this.params;
    }

    public void setParams(String str) {
        this.params = str;
        setValue();
    }

    public String getIsMhtHastitle() {
        return this.isMhtHastitle;
    }

    public void setIsMhtHastitle(String str) {
        this.isMhtHastitle = str;
        setValue();
    }

    public String getHtmlDigest() {
        return this.htmlDigest;
    }

    public void setHtmlDigest(String str) {
        this.htmlDigest = str;
        setValue();
    }

    public String getReserved0() {
        return this.reserved0;
    }

    public void setReserved0(String str) {
        this.reserved0 = str;
        setValue();
    }

    public String getReserved1() {
        return this.reserved1;
    }

    public void setReserved1(String str) {
        this.reserved1 = str;
        setValue();
    }

    public String getReserved2() {
        return this.reserved2;
    }

    public void setReserved2(String str) {
        this.reserved2 = str;
        setValue();
    }

    public String getReserved3() {
        return this.reserved3;
    }

    public void setReserved3(String str) {
        this.reserved3 = str;
        setValue();
    }

    public String getReserved4() {
        return this.reserved4;
    }

    public void setReserved4(String str) {
        this.reserved4 = str;
        setValue();
    }

    public String getReserved5() {
        return this.reserved5;
    }

    public void setReserved5(String str) {
        this.reserved5 = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.id.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.title != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.title);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.pageUrl != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.pageUrl);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.serverId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.serverId);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mhtUtl != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mhtUtl);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.thumbnail != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.thumbnail);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.htmlPath != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.htmlPath);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.createdtime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.createdtime);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.comeFrom != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.comeFrom);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.localUrl != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.localUrl);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.excerpt != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.excerpt);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.uniqueFlag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.uniqueFlag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.isLoaded != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.isLoaded);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.deleted != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.deleted.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.thumbnailUrl != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.thumbnailUrl);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.isImgLoaded != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.isImgLoaded.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.isUpload != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.isUpload.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.isDownload != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.isDownload.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.syncCount != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.syncCount.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.extra != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.extra);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.pkgName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.pkgName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.source != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.source);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.sourceTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.sourceTime);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.params != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.params);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.isMhtHastitle != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.isMhtHastitle);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.htmlDigest != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.htmlDigest);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved0 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved0);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved2);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved3 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved3);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved4 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved4);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved5 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved5);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<DSDigest> getHelper() {
        return DSDigestHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "DSDigest { id: " + this.id + ", title: " + this.title + ", pageUrl: " + this.pageUrl + ", serverId: " + this.serverId + ", mhtUtl: " + this.mhtUtl + ", thumbnail: " + this.thumbnail + ", htmlPath: " + this.htmlPath + ", createdtime: " + this.createdtime + ", comeFrom: " + this.comeFrom + ", localUrl: " + this.localUrl + ", excerpt: " + this.excerpt + ", uniqueFlag: " + this.uniqueFlag + ", isLoaded: " + this.isLoaded + ", deleted: " + this.deleted + ", thumbnailUrl: " + this.thumbnailUrl + ", isImgLoaded: " + this.isImgLoaded + ", isUpload: " + this.isUpload + ", isDownload: " + this.isDownload + ", syncCount: " + this.syncCount + ", extra: " + this.extra + ", pkgName: " + this.pkgName + ", source: " + this.source + ", sourceTime: " + this.sourceTime + ", params: " + this.params + ", isMhtHastitle: " + this.isMhtHastitle + ", htmlDigest: " + this.htmlDigest + ", reserved0: " + this.reserved0 + ", reserved1: " + this.reserved1 + ", reserved2: " + this.reserved2 + ", reserved3: " + this.reserved3 + ", reserved4: " + this.reserved4 + ", reserved5: " + this.reserved5 + " }";
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public int hashCode() {
        return super.hashCode();
    }
}
