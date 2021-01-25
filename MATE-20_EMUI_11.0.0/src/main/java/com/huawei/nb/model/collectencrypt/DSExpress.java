package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DSExpress extends AManagedObject {
    public static final Parcelable.Creator<DSExpress> CREATOR = new Parcelable.Creator<DSExpress>() {
        /* class com.huawei.nb.model.collectencrypt.DSExpress.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DSExpress createFromParcel(Parcel parcel) {
            return new DSExpress(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DSExpress[] newArray(int i) {
            return new DSExpress[i];
        }
    };
    private String appName;
    private String appPackage;
    private String cabinetCompany;
    private String cabinetLocation;
    private String code;
    private String companyCode;
    private String courierName;
    private String courierPhone;
    private Long createTime = 0L;
    private String dataSource;
    private String detail;
    private String expand;
    private String expressCompany;
    private Integer expressFlow = -1;
    private String expressNumber;
    private String extras;
    private Integer id;
    private Long lastUpdateTime = 0L;
    private String latitude;
    private String longitude;
    private Integer mState = -1;
    private Long newestTime = 0L;
    private Integer oldState = -1;
    private String reserved0;
    private String reserved1;
    private String reserved2;
    private String reserved3;
    private String reserved4;
    private String reserved5;
    private Long sendTime = 0L;
    private String signPerson;
    private Long signTime = 0L;
    private Integer source = 0;
    private Integer subWithImei = 1;
    private Integer subscribeState = 0;
    private Long updateTime = 0L;

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
        return "com.huawei.nb.model.collectencrypt.DSExpress";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public DSExpress(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.expressNumber = cursor.getString(2);
        this.expressCompany = cursor.getString(3);
        this.companyCode = cursor.getString(4);
        this.cabinetCompany = cursor.getString(5);
        this.mState = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.oldState = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        this.detail = cursor.getString(8);
        this.updateTime = cursor.isNull(9) ? null : Long.valueOf(cursor.getLong(9));
        this.lastUpdateTime = cursor.isNull(10) ? null : Long.valueOf(cursor.getLong(10));
        this.newestTime = cursor.isNull(11) ? null : Long.valueOf(cursor.getLong(11));
        this.code = cursor.getString(12);
        this.cabinetLocation = cursor.getString(13);
        this.latitude = cursor.getString(14);
        this.longitude = cursor.getString(15);
        this.appName = cursor.getString(16);
        this.appPackage = cursor.getString(17);
        this.source = cursor.isNull(18) ? null : Integer.valueOf(cursor.getInt(18));
        this.dataSource = cursor.getString(19);
        this.courierName = cursor.getString(20);
        this.courierPhone = cursor.getString(21);
        this.subscribeState = cursor.isNull(22) ? null : Integer.valueOf(cursor.getInt(22));
        this.sendTime = cursor.isNull(23) ? null : Long.valueOf(cursor.getLong(23));
        this.signTime = cursor.isNull(24) ? null : Long.valueOf(cursor.getLong(24));
        this.signPerson = cursor.getString(25);
        this.expressFlow = cursor.isNull(26) ? null : Integer.valueOf(cursor.getInt(26));
        this.extras = cursor.getString(27);
        this.createTime = cursor.isNull(28) ? null : Long.valueOf(cursor.getLong(28));
        this.expand = cursor.getString(29);
        this.subWithImei = !cursor.isNull(30) ? Integer.valueOf(cursor.getInt(30)) : num;
        this.reserved0 = cursor.getString(31);
        this.reserved1 = cursor.getString(32);
        this.reserved2 = cursor.getString(33);
        this.reserved3 = cursor.getString(34);
        this.reserved4 = cursor.getString(35);
        this.reserved5 = cursor.getString(36);
    }

    public DSExpress(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.expressNumber = parcel.readByte() == 0 ? null : parcel.readString();
        this.expressCompany = parcel.readByte() == 0 ? null : parcel.readString();
        this.companyCode = parcel.readByte() == 0 ? null : parcel.readString();
        this.cabinetCompany = parcel.readByte() == 0 ? null : parcel.readString();
        this.mState = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.oldState = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.detail = parcel.readByte() == 0 ? null : parcel.readString();
        this.updateTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.lastUpdateTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.newestTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.code = parcel.readByte() == 0 ? null : parcel.readString();
        this.cabinetLocation = parcel.readByte() == 0 ? null : parcel.readString();
        this.latitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.longitude = parcel.readByte() == 0 ? null : parcel.readString();
        this.appName = parcel.readByte() == 0 ? null : parcel.readString();
        this.appPackage = parcel.readByte() == 0 ? null : parcel.readString();
        this.source = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.dataSource = parcel.readByte() == 0 ? null : parcel.readString();
        this.courierName = parcel.readByte() == 0 ? null : parcel.readString();
        this.courierPhone = parcel.readByte() == 0 ? null : parcel.readString();
        this.subscribeState = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.sendTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.signTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.signPerson = parcel.readByte() == 0 ? null : parcel.readString();
        this.expressFlow = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.extras = parcel.readByte() == 0 ? null : parcel.readString();
        this.createTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.expand = parcel.readByte() == 0 ? null : parcel.readString();
        this.subWithImei = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.reserved0 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved3 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved4 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved5 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private DSExpress(Integer num, String str, String str2, String str3, String str4, Integer num2, Integer num3, String str5, Long l, Long l2, Long l3, String str6, String str7, String str8, String str9, String str10, String str11, Integer num4, String str12, String str13, String str14, Integer num5, Long l4, Long l5, String str15, Integer num6, String str16, Long l6, String str17, Integer num7, String str18, String str19, String str20, String str21, String str22, String str23) {
        this.id = num;
        this.expressNumber = str;
        this.expressCompany = str2;
        this.companyCode = str3;
        this.cabinetCompany = str4;
        this.mState = num2;
        this.oldState = num3;
        this.detail = str5;
        this.updateTime = l;
        this.lastUpdateTime = l2;
        this.newestTime = l3;
        this.code = str6;
        this.cabinetLocation = str7;
        this.latitude = str8;
        this.longitude = str9;
        this.appName = str10;
        this.appPackage = str11;
        this.source = num4;
        this.dataSource = str12;
        this.courierName = str13;
        this.courierPhone = str14;
        this.subscribeState = num5;
        this.sendTime = l4;
        this.signTime = l5;
        this.signPerson = str15;
        this.expressFlow = num6;
        this.extras = str16;
        this.createTime = l6;
        this.expand = str17;
        this.subWithImei = num7;
        this.reserved0 = str18;
        this.reserved1 = str19;
        this.reserved2 = str20;
        this.reserved3 = str21;
        this.reserved4 = str22;
        this.reserved5 = str23;
    }

    public DSExpress() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public String getExpressNumber() {
        return this.expressNumber;
    }

    public void setExpressNumber(String str) {
        this.expressNumber = str;
        setValue();
    }

    public String getExpressCompany() {
        return this.expressCompany;
    }

    public void setExpressCompany(String str) {
        this.expressCompany = str;
        setValue();
    }

    public String getCompanyCode() {
        return this.companyCode;
    }

    public void setCompanyCode(String str) {
        this.companyCode = str;
        setValue();
    }

    public String getCabinetCompany() {
        return this.cabinetCompany;
    }

    public void setCabinetCompany(String str) {
        this.cabinetCompany = str;
        setValue();
    }

    public Integer getMState() {
        return this.mState;
    }

    public void setMState(Integer num) {
        this.mState = num;
        setValue();
    }

    public Integer getOldState() {
        return this.oldState;
    }

    public void setOldState(Integer num) {
        this.oldState = num;
        setValue();
    }

    public String getDetail() {
        return this.detail;
    }

    public void setDetail(String str) {
        this.detail = str;
        setValue();
    }

    public Long getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Long l) {
        this.updateTime = l;
        setValue();
    }

    public Long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    public void setLastUpdateTime(Long l) {
        this.lastUpdateTime = l;
        setValue();
    }

    public Long getNewestTime() {
        return this.newestTime;
    }

    public void setNewestTime(Long l) {
        this.newestTime = l;
        setValue();
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String str) {
        this.code = str;
        setValue();
    }

    public String getCabinetLocation() {
        return this.cabinetLocation;
    }

    public void setCabinetLocation(String str) {
        this.cabinetLocation = str;
        setValue();
    }

    public String getLatitude() {
        return this.latitude;
    }

    public void setLatitude(String str) {
        this.latitude = str;
        setValue();
    }

    public String getLongitude() {
        return this.longitude;
    }

    public void setLongitude(String str) {
        this.longitude = str;
        setValue();
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String str) {
        this.appName = str;
        setValue();
    }

    public String getAppPackage() {
        return this.appPackage;
    }

    public void setAppPackage(String str) {
        this.appPackage = str;
        setValue();
    }

    public Integer getSource() {
        return this.source;
    }

    public void setSource(Integer num) {
        this.source = num;
        setValue();
    }

    public String getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(String str) {
        this.dataSource = str;
        setValue();
    }

    public String getCourierName() {
        return this.courierName;
    }

    public void setCourierName(String str) {
        this.courierName = str;
        setValue();
    }

    public String getCourierPhone() {
        return this.courierPhone;
    }

    public void setCourierPhone(String str) {
        this.courierPhone = str;
        setValue();
    }

    public Integer getSubscribeState() {
        return this.subscribeState;
    }

    public void setSubscribeState(Integer num) {
        this.subscribeState = num;
        setValue();
    }

    public Long getSendTime() {
        return this.sendTime;
    }

    public void setSendTime(Long l) {
        this.sendTime = l;
        setValue();
    }

    public Long getSignTime() {
        return this.signTime;
    }

    public void setSignTime(Long l) {
        this.signTime = l;
        setValue();
    }

    public String getSignPerson() {
        return this.signPerson;
    }

    public void setSignPerson(String str) {
        this.signPerson = str;
        setValue();
    }

    public Integer getExpressFlow() {
        return this.expressFlow;
    }

    public void setExpressFlow(Integer num) {
        this.expressFlow = num;
        setValue();
    }

    public String getExtras() {
        return this.extras;
    }

    public void setExtras(String str) {
        this.extras = str;
        setValue();
    }

    public Long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Long l) {
        this.createTime = l;
        setValue();
    }

    public String getExpand() {
        return this.expand;
    }

    public void setExpand(String str) {
        this.expand = str;
        setValue();
    }

    public Integer getSubWithImei() {
        return this.subWithImei;
    }

    public void setSubWithImei(Integer num) {
        this.subWithImei = num;
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
        if (this.expressNumber != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.expressNumber);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.expressCompany != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.expressCompany);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.companyCode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.companyCode);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.cabinetCompany != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.cabinetCompany);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mState != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mState.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.oldState != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.oldState.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.detail != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.detail);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.updateTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.updateTime.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.lastUpdateTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.lastUpdateTime.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.newestTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.newestTime.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.code != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.code);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.cabinetLocation != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.cabinetLocation);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.latitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.latitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.longitude != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.longitude);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.appName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.appName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.appPackage != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.appPackage);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.source != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.source.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.dataSource != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.dataSource);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.courierName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.courierName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.courierPhone != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.courierPhone);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.subscribeState != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.subscribeState.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.sendTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.sendTime.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.signTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.signTime.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.signPerson != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.signPerson);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.expressFlow != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.expressFlow.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.extras != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.extras);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.createTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.createTime.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.expand != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.expand);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.subWithImei != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.subWithImei.intValue());
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
    public AEntityHelper<DSExpress> getHelper() {
        return DSExpressHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "DSExpress { id: " + this.id + ", expressNumber: " + this.expressNumber + ", expressCompany: " + this.expressCompany + ", companyCode: " + this.companyCode + ", cabinetCompany: " + this.cabinetCompany + ", mState: " + this.mState + ", oldState: " + this.oldState + ", detail: " + this.detail + ", updateTime: " + this.updateTime + ", lastUpdateTime: " + this.lastUpdateTime + ", newestTime: " + this.newestTime + ", code: " + this.code + ", cabinetLocation: " + this.cabinetLocation + ", latitude: " + this.latitude + ", longitude: " + this.longitude + ", appName: " + this.appName + ", appPackage: " + this.appPackage + ", source: " + this.source + ", dataSource: " + this.dataSource + ", courierName: " + this.courierName + ", courierPhone: " + this.courierPhone + ", subscribeState: " + this.subscribeState + ", sendTime: " + this.sendTime + ", signTime: " + this.signTime + ", signPerson: " + this.signPerson + ", expressFlow: " + this.expressFlow + ", extras: " + this.extras + ", createTime: " + this.createTime + ", expand: " + this.expand + ", subWithImei: " + this.subWithImei + ", reserved0: " + this.reserved0 + ", reserved1: " + this.reserved1 + ", reserved2: " + this.reserved2 + ", reserved3: " + this.reserved3 + ", reserved4: " + this.reserved4 + ", reserved5: " + this.reserved5 + " }";
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
