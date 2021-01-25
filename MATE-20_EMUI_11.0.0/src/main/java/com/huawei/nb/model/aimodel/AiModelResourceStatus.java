package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelResourceStatus extends AManagedObject {
    public static final Parcelable.Creator<AiModelResourceStatus> CREATOR = new Parcelable.Creator<AiModelResourceStatus>() {
        /* class com.huawei.nb.model.aimodel.AiModelResourceStatus.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AiModelResourceStatus createFromParcel(Parcel parcel) {
            return new AiModelResourceStatus(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AiModelResourceStatus[] newArray(int i) {
            return new AiModelResourceStatus[i];
        }
    };
    private String abTest;
    private String chipset;
    private String chipsetVendor;
    private String decryptedKey;
    private String district;
    private String emuiFamily;
    private Long id;
    private String interfaceVersion;
    private String param1;
    private String param2;
    private String product;
    private String productFamily;
    private String productModel;
    private String res_name;
    private String resid;
    private Long status;
    private String supprtAppVerson;
    private String teams;
    private Boolean type;
    private Boolean update;
    private String url;
    private Long version = 0L;
    private String xpu;
    private String zipSha256;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsAiModel";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.aimodel.AiModelResourceStatus";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public AiModelResourceStatus(Cursor cursor) {
        Boolean bool;
        boolean z = false;
        setRowId(Long.valueOf(cursor.getLong(0)));
        Boolean bool2 = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.resid = cursor.getString(2);
        this.status = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.version = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.url = cursor.getString(5);
        this.teams = cursor.getString(6);
        this.zipSha256 = cursor.getString(7);
        this.decryptedKey = cursor.getString(8);
        if (cursor.isNull(9)) {
            bool = null;
        } else {
            bool = Boolean.valueOf(cursor.getInt(9) != 0);
        }
        this.type = bool;
        if (!cursor.isNull(10)) {
            bool2 = Boolean.valueOf(cursor.getInt(10) != 0 ? true : z);
        }
        this.update = bool2;
        this.xpu = cursor.getString(11);
        this.emuiFamily = cursor.getString(12);
        this.productFamily = cursor.getString(13);
        this.chipsetVendor = cursor.getString(14);
        this.chipset = cursor.getString(15);
        this.product = cursor.getString(16);
        this.productModel = cursor.getString(17);
        this.district = cursor.getString(18);
        this.abTest = cursor.getString(19);
        this.supprtAppVerson = cursor.getString(20);
        this.interfaceVersion = cursor.getString(21);
        this.param1 = cursor.getString(22);
        this.param2 = cursor.getString(23);
        this.res_name = cursor.getString(24);
    }

    public AiModelResourceStatus(Parcel parcel) {
        super(parcel);
        Boolean bool;
        Boolean bool2;
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.resid = parcel.readByte() == 0 ? null : parcel.readString();
        this.status = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.version = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.url = parcel.readByte() == 0 ? null : parcel.readString();
        this.teams = parcel.readByte() == 0 ? null : parcel.readString();
        this.zipSha256 = parcel.readByte() == 0 ? null : parcel.readString();
        this.decryptedKey = parcel.readByte() == 0 ? null : parcel.readString();
        boolean z = true;
        if (parcel.readByte() == 0) {
            bool = null;
        } else {
            bool = Boolean.valueOf(parcel.readByte() != 0);
        }
        this.type = bool;
        if (parcel.readByte() == 0) {
            bool2 = null;
        } else {
            bool2 = Boolean.valueOf(parcel.readByte() == 0 ? false : z);
        }
        this.update = bool2;
        this.xpu = parcel.readByte() == 0 ? null : parcel.readString();
        this.emuiFamily = parcel.readByte() == 0 ? null : parcel.readString();
        this.productFamily = parcel.readByte() == 0 ? null : parcel.readString();
        this.chipsetVendor = parcel.readByte() == 0 ? null : parcel.readString();
        this.chipset = parcel.readByte() == 0 ? null : parcel.readString();
        this.product = parcel.readByte() == 0 ? null : parcel.readString();
        this.productModel = parcel.readByte() == 0 ? null : parcel.readString();
        this.district = parcel.readByte() == 0 ? null : parcel.readString();
        this.abTest = parcel.readByte() == 0 ? null : parcel.readString();
        this.supprtAppVerson = parcel.readByte() == 0 ? null : parcel.readString();
        this.interfaceVersion = parcel.readByte() == 0 ? null : parcel.readString();
        this.param1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.param2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.res_name = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private AiModelResourceStatus(Long l, String str, Long l2, Long l3, String str2, String str3, String str4, String str5, Boolean bool, Boolean bool2, String str6, String str7, String str8, String str9, String str10, String str11, String str12, String str13, String str14, String str15, String str16, String str17, String str18, String str19) {
        this.id = l;
        this.resid = str;
        this.status = l2;
        this.version = l3;
        this.url = str2;
        this.teams = str3;
        this.zipSha256 = str4;
        this.decryptedKey = str5;
        this.type = bool;
        this.update = bool2;
        this.xpu = str6;
        this.emuiFamily = str7;
        this.productFamily = str8;
        this.chipsetVendor = str9;
        this.chipset = str10;
        this.product = str11;
        this.productModel = str12;
        this.district = str13;
        this.abTest = str14;
        this.supprtAppVerson = str15;
        this.interfaceVersion = str16;
        this.param1 = str17;
        this.param2 = str18;
        this.res_name = str19;
    }

    public AiModelResourceStatus() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getResid() {
        return this.resid;
    }

    public void setResid(String str) {
        this.resid = str;
        setValue();
    }

    public Long getStatus() {
        return this.status;
    }

    public void setStatus(Long l) {
        this.status = l;
        setValue();
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long l) {
        this.version = l;
        setValue();
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String str) {
        this.url = str;
        setValue();
    }

    public String getTeams() {
        return this.teams;
    }

    public void setTeams(String str) {
        this.teams = str;
        setValue();
    }

    public String getZipSha256() {
        return this.zipSha256;
    }

    public void setZipSha256(String str) {
        this.zipSha256 = str;
        setValue();
    }

    public String getDecryptedKey() {
        return this.decryptedKey;
    }

    public void setDecryptedKey(String str) {
        this.decryptedKey = str;
        setValue();
    }

    public Boolean getType() {
        return this.type;
    }

    public void setType(Boolean bool) {
        this.type = bool;
        setValue();
    }

    public Boolean getUpdate() {
        return this.update;
    }

    public void setUpdate(Boolean bool) {
        this.update = bool;
        setValue();
    }

    public String getXpu() {
        return this.xpu;
    }

    public void setXpu(String str) {
        this.xpu = str;
        setValue();
    }

    public String getEmuiFamily() {
        return this.emuiFamily;
    }

    public void setEmuiFamily(String str) {
        this.emuiFamily = str;
        setValue();
    }

    public String getProductFamily() {
        return this.productFamily;
    }

    public void setProductFamily(String str) {
        this.productFamily = str;
        setValue();
    }

    public String getChipsetVendor() {
        return this.chipsetVendor;
    }

    public void setChipsetVendor(String str) {
        this.chipsetVendor = str;
        setValue();
    }

    public String getChipset() {
        return this.chipset;
    }

    public void setChipset(String str) {
        this.chipset = str;
        setValue();
    }

    public String getProduct() {
        return this.product;
    }

    public void setProduct(String str) {
        this.product = str;
        setValue();
    }

    public String getProductModel() {
        return this.productModel;
    }

    public void setProductModel(String str) {
        this.productModel = str;
        setValue();
    }

    public String getDistrict() {
        return this.district;
    }

    public void setDistrict(String str) {
        this.district = str;
        setValue();
    }

    public String getAbTest() {
        return this.abTest;
    }

    public void setAbTest(String str) {
        this.abTest = str;
        setValue();
    }

    public String getSupprtAppVerson() {
        return this.supprtAppVerson;
    }

    public void setSupprtAppVerson(String str) {
        this.supprtAppVerson = str;
        setValue();
    }

    public String getInterfaceVersion() {
        return this.interfaceVersion;
    }

    public void setInterfaceVersion(String str) {
        this.interfaceVersion = str;
        setValue();
    }

    public String getParam1() {
        return this.param1;
    }

    public void setParam1(String str) {
        this.param1 = str;
        setValue();
    }

    public String getParam2() {
        return this.param2;
    }

    public void setParam2(String str) {
        this.param2 = str;
        setValue();
    }

    public String getRes_name() {
        return this.res_name;
    }

    public void setRes_name(String str) {
        this.res_name = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.id.longValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeLong(1);
        }
        if (this.resid != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.resid);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.status != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.status.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.version != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.version.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.url != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.url);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.teams != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.teams);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.zipSha256 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.zipSha256);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.decryptedKey != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.decryptedKey);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeByte(this.type.booleanValue() ? (byte) 1 : 0);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.update != null) {
            parcel.writeByte((byte) 1);
            parcel.writeByte(this.update.booleanValue() ? (byte) 1 : 0);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.xpu != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.xpu);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.emuiFamily != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.emuiFamily);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.productFamily != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.productFamily);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.chipsetVendor != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.chipsetVendor);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.chipset != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.chipset);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.product != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.product);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.productModel != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.productModel);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.district != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.district);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.abTest != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.abTest);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.supprtAppVerson != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.supprtAppVerson);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.interfaceVersion != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.interfaceVersion);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.param1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.param1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.param2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.param2);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.res_name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.res_name);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<AiModelResourceStatus> getHelper() {
        return AiModelResourceStatusHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "AiModelResourceStatus { id: " + this.id + ", resid: " + this.resid + ", status: " + this.status + ", version: " + this.version + ", url: " + this.url + ", teams: " + this.teams + ", zipSha256: " + this.zipSha256 + ", decryptedKey: " + this.decryptedKey + ", type: " + this.type + ", update: " + this.update + ", xpu: " + this.xpu + ", emuiFamily: " + this.emuiFamily + ", productFamily: " + this.productFamily + ", chipsetVendor: " + this.chipsetVendor + ", chipset: " + this.chipset + ", product: " + this.product + ", productModel: " + this.productModel + ", district: " + this.district + ", abTest: " + this.abTest + ", supprtAppVerson: " + this.supprtAppVerson + ", interfaceVersion: " + this.interfaceVersion + ", param1: " + this.param1 + ", param2: " + this.param2 + ", res_name: " + this.res_name + " }";
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
