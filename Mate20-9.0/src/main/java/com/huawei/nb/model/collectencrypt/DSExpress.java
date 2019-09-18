package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DSExpress extends AManagedObject {
    public static final Parcelable.Creator<DSExpress> CREATOR = new Parcelable.Creator<DSExpress>() {
        public DSExpress createFromParcel(Parcel in) {
            return new DSExpress(in);
        }

        public DSExpress[] newArray(int size) {
            return new DSExpress[size];
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

    public DSExpress(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DSExpress(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.expressNumber = in.readByte() == 0 ? null : in.readString();
        this.expressCompany = in.readByte() == 0 ? null : in.readString();
        this.companyCode = in.readByte() == 0 ? null : in.readString();
        this.cabinetCompany = in.readByte() == 0 ? null : in.readString();
        this.mState = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.oldState = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.detail = in.readByte() == 0 ? null : in.readString();
        this.updateTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.lastUpdateTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.newestTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.code = in.readByte() == 0 ? null : in.readString();
        this.cabinetLocation = in.readByte() == 0 ? null : in.readString();
        this.latitude = in.readByte() == 0 ? null : in.readString();
        this.longitude = in.readByte() == 0 ? null : in.readString();
        this.appName = in.readByte() == 0 ? null : in.readString();
        this.appPackage = in.readByte() == 0 ? null : in.readString();
        this.source = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.dataSource = in.readByte() == 0 ? null : in.readString();
        this.courierName = in.readByte() == 0 ? null : in.readString();
        this.courierPhone = in.readByte() == 0 ? null : in.readString();
        this.subscribeState = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.sendTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.signTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.signPerson = in.readByte() == 0 ? null : in.readString();
        this.expressFlow = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.extras = in.readByte() == 0 ? null : in.readString();
        this.createTime = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.expand = in.readByte() == 0 ? null : in.readString();
        this.subWithImei = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.reserved0 = in.readByte() == 0 ? null : in.readString();
        this.reserved1 = in.readByte() == 0 ? null : in.readString();
        this.reserved2 = in.readByte() == 0 ? null : in.readString();
        this.reserved3 = in.readByte() == 0 ? null : in.readString();
        this.reserved4 = in.readByte() == 0 ? null : in.readString();
        this.reserved5 = in.readByte() != 0 ? in.readString() : str;
    }

    private DSExpress(Integer id2, String expressNumber2, String expressCompany2, String companyCode2, String cabinetCompany2, Integer mState2, Integer oldState2, String detail2, Long updateTime2, Long lastUpdateTime2, Long newestTime2, String code2, String cabinetLocation2, String latitude2, String longitude2, String appName2, String appPackage2, Integer source2, String dataSource2, String courierName2, String courierPhone2, Integer subscribeState2, Long sendTime2, Long signTime2, String signPerson2, Integer expressFlow2, String extras2, Long createTime2, String expand2, Integer subWithImei2, String reserved02, String reserved12, String reserved22, String reserved32, String reserved42, String reserved52) {
        this.id = id2;
        this.expressNumber = expressNumber2;
        this.expressCompany = expressCompany2;
        this.companyCode = companyCode2;
        this.cabinetCompany = cabinetCompany2;
        this.mState = mState2;
        this.oldState = oldState2;
        this.detail = detail2;
        this.updateTime = updateTime2;
        this.lastUpdateTime = lastUpdateTime2;
        this.newestTime = newestTime2;
        this.code = code2;
        this.cabinetLocation = cabinetLocation2;
        this.latitude = latitude2;
        this.longitude = longitude2;
        this.appName = appName2;
        this.appPackage = appPackage2;
        this.source = source2;
        this.dataSource = dataSource2;
        this.courierName = courierName2;
        this.courierPhone = courierPhone2;
        this.subscribeState = subscribeState2;
        this.sendTime = sendTime2;
        this.signTime = signTime2;
        this.signPerson = signPerson2;
        this.expressFlow = expressFlow2;
        this.extras = extras2;
        this.createTime = createTime2;
        this.expand = expand2;
        this.subWithImei = subWithImei2;
        this.reserved0 = reserved02;
        this.reserved1 = reserved12;
        this.reserved2 = reserved22;
        this.reserved3 = reserved32;
        this.reserved4 = reserved42;
        this.reserved5 = reserved52;
    }

    public DSExpress() {
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

    public String getExpressNumber() {
        return this.expressNumber;
    }

    public void setExpressNumber(String expressNumber2) {
        this.expressNumber = expressNumber2;
        setValue();
    }

    public String getExpressCompany() {
        return this.expressCompany;
    }

    public void setExpressCompany(String expressCompany2) {
        this.expressCompany = expressCompany2;
        setValue();
    }

    public String getCompanyCode() {
        return this.companyCode;
    }

    public void setCompanyCode(String companyCode2) {
        this.companyCode = companyCode2;
        setValue();
    }

    public String getCabinetCompany() {
        return this.cabinetCompany;
    }

    public void setCabinetCompany(String cabinetCompany2) {
        this.cabinetCompany = cabinetCompany2;
        setValue();
    }

    public Integer getMState() {
        return this.mState;
    }

    public void setMState(Integer mState2) {
        this.mState = mState2;
        setValue();
    }

    public Integer getOldState() {
        return this.oldState;
    }

    public void setOldState(Integer oldState2) {
        this.oldState = oldState2;
        setValue();
    }

    public String getDetail() {
        return this.detail;
    }

    public void setDetail(String detail2) {
        this.detail = detail2;
        setValue();
    }

    public Long getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Long updateTime2) {
        this.updateTime = updateTime2;
        setValue();
    }

    public Long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime2) {
        this.lastUpdateTime = lastUpdateTime2;
        setValue();
    }

    public Long getNewestTime() {
        return this.newestTime;
    }

    public void setNewestTime(Long newestTime2) {
        this.newestTime = newestTime2;
        setValue();
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code2) {
        this.code = code2;
        setValue();
    }

    public String getCabinetLocation() {
        return this.cabinetLocation;
    }

    public void setCabinetLocation(String cabinetLocation2) {
        this.cabinetLocation = cabinetLocation2;
        setValue();
    }

    public String getLatitude() {
        return this.latitude;
    }

    public void setLatitude(String latitude2) {
        this.latitude = latitude2;
        setValue();
    }

    public String getLongitude() {
        return this.longitude;
    }

    public void setLongitude(String longitude2) {
        this.longitude = longitude2;
        setValue();
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String appName2) {
        this.appName = appName2;
        setValue();
    }

    public String getAppPackage() {
        return this.appPackage;
    }

    public void setAppPackage(String appPackage2) {
        this.appPackage = appPackage2;
        setValue();
    }

    public Integer getSource() {
        return this.source;
    }

    public void setSource(Integer source2) {
        this.source = source2;
        setValue();
    }

    public String getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(String dataSource2) {
        this.dataSource = dataSource2;
        setValue();
    }

    public String getCourierName() {
        return this.courierName;
    }

    public void setCourierName(String courierName2) {
        this.courierName = courierName2;
        setValue();
    }

    public String getCourierPhone() {
        return this.courierPhone;
    }

    public void setCourierPhone(String courierPhone2) {
        this.courierPhone = courierPhone2;
        setValue();
    }

    public Integer getSubscribeState() {
        return this.subscribeState;
    }

    public void setSubscribeState(Integer subscribeState2) {
        this.subscribeState = subscribeState2;
        setValue();
    }

    public Long getSendTime() {
        return this.sendTime;
    }

    public void setSendTime(Long sendTime2) {
        this.sendTime = sendTime2;
        setValue();
    }

    public Long getSignTime() {
        return this.signTime;
    }

    public void setSignTime(Long signTime2) {
        this.signTime = signTime2;
        setValue();
    }

    public String getSignPerson() {
        return this.signPerson;
    }

    public void setSignPerson(String signPerson2) {
        this.signPerson = signPerson2;
        setValue();
    }

    public Integer getExpressFlow() {
        return this.expressFlow;
    }

    public void setExpressFlow(Integer expressFlow2) {
        this.expressFlow = expressFlow2;
        setValue();
    }

    public String getExtras() {
        return this.extras;
    }

    public void setExtras(String extras2) {
        this.extras = extras2;
        setValue();
    }

    public Long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Long createTime2) {
        this.createTime = createTime2;
        setValue();
    }

    public String getExpand() {
        return this.expand;
    }

    public void setExpand(String expand2) {
        this.expand = expand2;
        setValue();
    }

    public Integer getSubWithImei() {
        return this.subWithImei;
    }

    public void setSubWithImei(Integer subWithImei2) {
        this.subWithImei = subWithImei2;
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
        if (this.expressNumber != null) {
            out.writeByte((byte) 1);
            out.writeString(this.expressNumber);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.expressCompany != null) {
            out.writeByte((byte) 1);
            out.writeString(this.expressCompany);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.companyCode != null) {
            out.writeByte((byte) 1);
            out.writeString(this.companyCode);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.cabinetCompany != null) {
            out.writeByte((byte) 1);
            out.writeString(this.cabinetCompany);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mState != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mState.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.oldState != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.oldState.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.detail != null) {
            out.writeByte((byte) 1);
            out.writeString(this.detail);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.updateTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.updateTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.lastUpdateTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.lastUpdateTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.newestTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.newestTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.code != null) {
            out.writeByte((byte) 1);
            out.writeString(this.code);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.cabinetLocation != null) {
            out.writeByte((byte) 1);
            out.writeString(this.cabinetLocation);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.latitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.latitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.longitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.longitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.appName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.appName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.appPackage != null) {
            out.writeByte((byte) 1);
            out.writeString(this.appPackage);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.source != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.source.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.dataSource != null) {
            out.writeByte((byte) 1);
            out.writeString(this.dataSource);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.courierName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.courierName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.courierPhone != null) {
            out.writeByte((byte) 1);
            out.writeString(this.courierPhone);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.subscribeState != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.subscribeState.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.sendTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.sendTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.signTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.signTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.signPerson != null) {
            out.writeByte((byte) 1);
            out.writeString(this.signPerson);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.expressFlow != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.expressFlow.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.extras != null) {
            out.writeByte((byte) 1);
            out.writeString(this.extras);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.createTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.createTime.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.expand != null) {
            out.writeByte((byte) 1);
            out.writeString(this.expand);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.subWithImei != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.subWithImei.intValue());
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

    public AEntityHelper<DSExpress> getHelper() {
        return DSExpressHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.DSExpress";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("DSExpress { id: ").append(this.id);
        sb.append(", expressNumber: ").append(this.expressNumber);
        sb.append(", expressCompany: ").append(this.expressCompany);
        sb.append(", companyCode: ").append(this.companyCode);
        sb.append(", cabinetCompany: ").append(this.cabinetCompany);
        sb.append(", mState: ").append(this.mState);
        sb.append(", oldState: ").append(this.oldState);
        sb.append(", detail: ").append(this.detail);
        sb.append(", updateTime: ").append(this.updateTime);
        sb.append(", lastUpdateTime: ").append(this.lastUpdateTime);
        sb.append(", newestTime: ").append(this.newestTime);
        sb.append(", code: ").append(this.code);
        sb.append(", cabinetLocation: ").append(this.cabinetLocation);
        sb.append(", latitude: ").append(this.latitude);
        sb.append(", longitude: ").append(this.longitude);
        sb.append(", appName: ").append(this.appName);
        sb.append(", appPackage: ").append(this.appPackage);
        sb.append(", source: ").append(this.source);
        sb.append(", dataSource: ").append(this.dataSource);
        sb.append(", courierName: ").append(this.courierName);
        sb.append(", courierPhone: ").append(this.courierPhone);
        sb.append(", subscribeState: ").append(this.subscribeState);
        sb.append(", sendTime: ").append(this.sendTime);
        sb.append(", signTime: ").append(this.signTime);
        sb.append(", signPerson: ").append(this.signPerson);
        sb.append(", expressFlow: ").append(this.expressFlow);
        sb.append(", extras: ").append(this.extras);
        sb.append(", createTime: ").append(this.createTime);
        sb.append(", expand: ").append(this.expand);
        sb.append(", subWithImei: ").append(this.subWithImei);
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
