package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DSDigest extends AManagedObject {
    public static final Parcelable.Creator<DSDigest> CREATOR = new Parcelable.Creator<DSDigest>() {
        public DSDigest createFromParcel(Parcel in) {
            return new DSDigest(in);
        }

        public DSDigest[] newArray(int size) {
            return new DSDigest[size];
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

    public DSDigest(Cursor cursor) {
        Integer valueOf;
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        if (cursor.isNull(1)) {
            valueOf = null;
        } else {
            valueOf = Integer.valueOf(cursor.getInt(1));
        }
        this.id = valueOf;
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DSDigest(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.title = in.readByte() == 0 ? null : in.readString();
        this.pageUrl = in.readByte() == 0 ? null : in.readString();
        this.serverId = in.readByte() == 0 ? null : in.readString();
        this.mhtUtl = in.readByte() == 0 ? null : in.readString();
        this.thumbnail = in.readByte() == 0 ? null : in.readString();
        this.htmlPath = in.readByte() == 0 ? null : in.readString();
        this.createdtime = in.readByte() == 0 ? null : in.readString();
        this.comeFrom = in.readByte() == 0 ? null : in.readString();
        this.localUrl = in.readByte() == 0 ? null : in.readString();
        this.excerpt = in.readByte() == 0 ? null : in.readString();
        this.uniqueFlag = in.readByte() == 0 ? null : in.readString();
        this.isLoaded = in.readByte() == 0 ? null : in.readString();
        this.deleted = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.thumbnailUrl = in.readByte() == 0 ? null : in.readString();
        this.isImgLoaded = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.isUpload = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.isDownload = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.syncCount = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.extra = in.readByte() == 0 ? null : in.readString();
        this.pkgName = in.readByte() == 0 ? null : in.readString();
        this.source = in.readByte() == 0 ? null : in.readString();
        this.sourceTime = in.readByte() == 0 ? null : in.readString();
        this.params = in.readByte() == 0 ? null : in.readString();
        this.isMhtHastitle = in.readByte() == 0 ? null : in.readString();
        this.htmlDigest = in.readByte() == 0 ? null : in.readString();
        this.reserved0 = in.readByte() == 0 ? null : in.readString();
        this.reserved1 = in.readByte() == 0 ? null : in.readString();
        this.reserved2 = in.readByte() == 0 ? null : in.readString();
        this.reserved3 = in.readByte() == 0 ? null : in.readString();
        this.reserved4 = in.readByte() == 0 ? null : in.readString();
        this.reserved5 = in.readByte() != 0 ? in.readString() : str;
    }

    private DSDigest(Integer id2, String title2, String pageUrl2, String serverId2, String mhtUtl2, String thumbnail2, String htmlPath2, String createdtime2, String comeFrom2, String localUrl2, String excerpt2, String uniqueFlag2, String isLoaded2, Integer deleted2, String thumbnailUrl2, Integer isImgLoaded2, Integer isUpload2, Integer isDownload2, Integer syncCount2, String extra2, String pkgName2, String source2, String sourceTime2, String params2, String isMhtHastitle2, String htmlDigest2, String reserved02, String reserved12, String reserved22, String reserved32, String reserved42, String reserved52) {
        this.id = id2;
        this.title = title2;
        this.pageUrl = pageUrl2;
        this.serverId = serverId2;
        this.mhtUtl = mhtUtl2;
        this.thumbnail = thumbnail2;
        this.htmlPath = htmlPath2;
        this.createdtime = createdtime2;
        this.comeFrom = comeFrom2;
        this.localUrl = localUrl2;
        this.excerpt = excerpt2;
        this.uniqueFlag = uniqueFlag2;
        this.isLoaded = isLoaded2;
        this.deleted = deleted2;
        this.thumbnailUrl = thumbnailUrl2;
        this.isImgLoaded = isImgLoaded2;
        this.isUpload = isUpload2;
        this.isDownload = isDownload2;
        this.syncCount = syncCount2;
        this.extra = extra2;
        this.pkgName = pkgName2;
        this.source = source2;
        this.sourceTime = sourceTime2;
        this.params = params2;
        this.isMhtHastitle = isMhtHastitle2;
        this.htmlDigest = htmlDigest2;
        this.reserved0 = reserved02;
        this.reserved1 = reserved12;
        this.reserved2 = reserved22;
        this.reserved3 = reserved32;
        this.reserved4 = reserved42;
        this.reserved5 = reserved52;
    }

    public DSDigest() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id2) {
        this.id = id2;
        setValue();
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title2) {
        this.title = title2;
        setValue();
    }

    public String getPageUrl() {
        return this.pageUrl;
    }

    public void setPageUrl(String pageUrl2) {
        this.pageUrl = pageUrl2;
        setValue();
    }

    public String getServerId() {
        return this.serverId;
    }

    public void setServerId(String serverId2) {
        this.serverId = serverId2;
        setValue();
    }

    public String getMhtUtl() {
        return this.mhtUtl;
    }

    public void setMhtUtl(String mhtUtl2) {
        this.mhtUtl = mhtUtl2;
        setValue();
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public void setThumbnail(String thumbnail2) {
        this.thumbnail = thumbnail2;
        setValue();
    }

    public String getHtmlPath() {
        return this.htmlPath;
    }

    public void setHtmlPath(String htmlPath2) {
        this.htmlPath = htmlPath2;
        setValue();
    }

    public String getCreatedtime() {
        return this.createdtime;
    }

    public void setCreatedtime(String createdtime2) {
        this.createdtime = createdtime2;
        setValue();
    }

    public String getComeFrom() {
        return this.comeFrom;
    }

    public void setComeFrom(String comeFrom2) {
        this.comeFrom = comeFrom2;
        setValue();
    }

    public String getLocalUrl() {
        return this.localUrl;
    }

    public void setLocalUrl(String localUrl2) {
        this.localUrl = localUrl2;
        setValue();
    }

    public String getExcerpt() {
        return this.excerpt;
    }

    public void setExcerpt(String excerpt2) {
        this.excerpt = excerpt2;
        setValue();
    }

    public String getUniqueFlag() {
        return this.uniqueFlag;
    }

    public void setUniqueFlag(String uniqueFlag2) {
        this.uniqueFlag = uniqueFlag2;
        setValue();
    }

    public String getIsLoaded() {
        return this.isLoaded;
    }

    public void setIsLoaded(String isLoaded2) {
        this.isLoaded = isLoaded2;
        setValue();
    }

    public Integer getDeleted() {
        return this.deleted;
    }

    public void setDeleted(Integer deleted2) {
        this.deleted = deleted2;
        setValue();
    }

    public String getThumbnailUrl() {
        return this.thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl2) {
        this.thumbnailUrl = thumbnailUrl2;
        setValue();
    }

    public Integer getIsImgLoaded() {
        return this.isImgLoaded;
    }

    public void setIsImgLoaded(Integer isImgLoaded2) {
        this.isImgLoaded = isImgLoaded2;
        setValue();
    }

    public Integer getIsUpload() {
        return this.isUpload;
    }

    public void setIsUpload(Integer isUpload2) {
        this.isUpload = isUpload2;
        setValue();
    }

    public Integer getIsDownload() {
        return this.isDownload;
    }

    public void setIsDownload(Integer isDownload2) {
        this.isDownload = isDownload2;
        setValue();
    }

    public Integer getSyncCount() {
        return this.syncCount;
    }

    public void setSyncCount(Integer syncCount2) {
        this.syncCount = syncCount2;
        setValue();
    }

    public String getExtra() {
        return this.extra;
    }

    public void setExtra(String extra2) {
        this.extra = extra2;
        setValue();
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String pkgName2) {
        this.pkgName = pkgName2;
        setValue();
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source2) {
        this.source = source2;
        setValue();
    }

    public String getSourceTime() {
        return this.sourceTime;
    }

    public void setSourceTime(String sourceTime2) {
        this.sourceTime = sourceTime2;
        setValue();
    }

    public String getParams() {
        return this.params;
    }

    public void setParams(String params2) {
        this.params = params2;
        setValue();
    }

    public String getIsMhtHastitle() {
        return this.isMhtHastitle;
    }

    public void setIsMhtHastitle(String isMhtHastitle2) {
        this.isMhtHastitle = isMhtHastitle2;
        setValue();
    }

    public String getHtmlDigest() {
        return this.htmlDigest;
    }

    public void setHtmlDigest(String htmlDigest2) {
        this.htmlDigest = htmlDigest2;
        setValue();
    }

    public String getReserved0() {
        return this.reserved0;
    }

    public void setReserved0(String reserved02) {
        this.reserved0 = reserved02;
        setValue();
    }

    public String getReserved1() {
        return this.reserved1;
    }

    public void setReserved1(String reserved12) {
        this.reserved1 = reserved12;
        setValue();
    }

    public String getReserved2() {
        return this.reserved2;
    }

    public void setReserved2(String reserved22) {
        this.reserved2 = reserved22;
        setValue();
    }

    public String getReserved3() {
        return this.reserved3;
    }

    public void setReserved3(String reserved32) {
        this.reserved3 = reserved32;
        setValue();
    }

    public String getReserved4() {
        return this.reserved4;
    }

    public void setReserved4(String reserved42) {
        this.reserved4 = reserved42;
        setValue();
    }

    public String getReserved5() {
        return this.reserved5;
    }

    public void setReserved5(String reserved52) {
        this.reserved5 = reserved52;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.title != null) {
            out.writeByte((byte) 1);
            out.writeString(this.title);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.pageUrl != null) {
            out.writeByte((byte) 1);
            out.writeString(this.pageUrl);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.serverId != null) {
            out.writeByte((byte) 1);
            out.writeString(this.serverId);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mhtUtl != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mhtUtl);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.thumbnail != null) {
            out.writeByte((byte) 1);
            out.writeString(this.thumbnail);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.htmlPath != null) {
            out.writeByte((byte) 1);
            out.writeString(this.htmlPath);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.createdtime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.createdtime);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.comeFrom != null) {
            out.writeByte((byte) 1);
            out.writeString(this.comeFrom);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.localUrl != null) {
            out.writeByte((byte) 1);
            out.writeString(this.localUrl);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.excerpt != null) {
            out.writeByte((byte) 1);
            out.writeString(this.excerpt);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.uniqueFlag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.uniqueFlag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isLoaded != null) {
            out.writeByte((byte) 1);
            out.writeString(this.isLoaded);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.deleted != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.deleted.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.thumbnailUrl != null) {
            out.writeByte((byte) 1);
            out.writeString(this.thumbnailUrl);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isImgLoaded != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.isImgLoaded.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isUpload != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.isUpload.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isDownload != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.isDownload.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.syncCount != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.syncCount.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.extra != null) {
            out.writeByte((byte) 1);
            out.writeString(this.extra);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.pkgName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.pkgName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.source != null) {
            out.writeByte((byte) 1);
            out.writeString(this.source);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.sourceTime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.sourceTime);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.params != null) {
            out.writeByte((byte) 1);
            out.writeString(this.params);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isMhtHastitle != null) {
            out.writeByte((byte) 1);
            out.writeString(this.isMhtHastitle);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.htmlDigest != null) {
            out.writeByte((byte) 1);
            out.writeString(this.htmlDigest);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved0 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved0);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved1);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved2 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved2);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved3 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved3);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved4 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved4);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved5 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved5);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<DSDigest> getHelper() {
        return DSDigestHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.DSDigest";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("DSDigest { id: ").append(this.id);
        sb.append(", title: ").append(this.title);
        sb.append(", pageUrl: ").append(this.pageUrl);
        sb.append(", serverId: ").append(this.serverId);
        sb.append(", mhtUtl: ").append(this.mhtUtl);
        sb.append(", thumbnail: ").append(this.thumbnail);
        sb.append(", htmlPath: ").append(this.htmlPath);
        sb.append(", createdtime: ").append(this.createdtime);
        sb.append(", comeFrom: ").append(this.comeFrom);
        sb.append(", localUrl: ").append(this.localUrl);
        sb.append(", excerpt: ").append(this.excerpt);
        sb.append(", uniqueFlag: ").append(this.uniqueFlag);
        sb.append(", isLoaded: ").append(this.isLoaded);
        sb.append(", deleted: ").append(this.deleted);
        sb.append(", thumbnailUrl: ").append(this.thumbnailUrl);
        sb.append(", isImgLoaded: ").append(this.isImgLoaded);
        sb.append(", isUpload: ").append(this.isUpload);
        sb.append(", isDownload: ").append(this.isDownload);
        sb.append(", syncCount: ").append(this.syncCount);
        sb.append(", extra: ").append(this.extra);
        sb.append(", pkgName: ").append(this.pkgName);
        sb.append(", source: ").append(this.source);
        sb.append(", sourceTime: ").append(this.sourceTime);
        sb.append(", params: ").append(this.params);
        sb.append(", isMhtHastitle: ").append(this.isMhtHastitle);
        sb.append(", htmlDigest: ").append(this.htmlDigest);
        sb.append(", reserved0: ").append(this.reserved0);
        sb.append(", reserved1: ").append(this.reserved1);
        sb.append(", reserved2: ").append(this.reserved2);
        sb.append(", reserved3: ").append(this.reserved3);
        sb.append(", reserved4: ").append(this.reserved4);
        sb.append(", reserved5: ").append(this.reserved5);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.14";
    }

    public int getDatabaseVersionCode() {
        return 14;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
