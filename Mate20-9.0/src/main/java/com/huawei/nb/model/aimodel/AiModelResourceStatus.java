package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelResourceStatus extends AManagedObject {
    public static final Parcelable.Creator<AiModelResourceStatus> CREATOR = new Parcelable.Creator<AiModelResourceStatus>() {
        public AiModelResourceStatus createFromParcel(Parcel in) {
            return new AiModelResourceStatus(in);
        }

        public AiModelResourceStatus[] newArray(int size) {
            return new AiModelResourceStatus[size];
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

    public AiModelResourceStatus(Cursor cursor) {
        Boolean valueOf;
        boolean z = true;
        Boolean bool = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.resid = cursor.getString(2);
        this.status = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.version = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.url = cursor.getString(5);
        this.teams = cursor.getString(6);
        this.zipSha256 = cursor.getString(7);
        this.decryptedKey = cursor.getString(8);
        if (cursor.isNull(9)) {
            valueOf = null;
        } else {
            valueOf = Boolean.valueOf(cursor.getInt(9) != 0);
        }
        this.type = valueOf;
        if (!cursor.isNull(10)) {
            bool = Boolean.valueOf(cursor.getInt(10) == 0 ? false : z);
        }
        this.update = bool;
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public AiModelResourceStatus(Parcel in) {
        super(in);
        Boolean valueOf;
        Boolean valueOf2;
        boolean z = true;
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.resid = in.readByte() == 0 ? null : in.readString();
        this.status = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.version = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.url = in.readByte() == 0 ? null : in.readString();
        this.teams = in.readByte() == 0 ? null : in.readString();
        this.zipSha256 = in.readByte() == 0 ? null : in.readString();
        this.decryptedKey = in.readByte() == 0 ? null : in.readString();
        if (in.readByte() == 0) {
            valueOf = null;
        } else {
            valueOf = Boolean.valueOf(in.readByte() != 0);
        }
        this.type = valueOf;
        if (in.readByte() == 0) {
            valueOf2 = null;
        } else {
            valueOf2 = Boolean.valueOf(in.readByte() == 0 ? false : z);
        }
        this.update = valueOf2;
        this.xpu = in.readByte() == 0 ? null : in.readString();
        this.emuiFamily = in.readByte() == 0 ? null : in.readString();
        this.productFamily = in.readByte() == 0 ? null : in.readString();
        this.chipsetVendor = in.readByte() == 0 ? null : in.readString();
        this.chipset = in.readByte() == 0 ? null : in.readString();
        this.product = in.readByte() == 0 ? null : in.readString();
        this.productModel = in.readByte() == 0 ? null : in.readString();
        this.district = in.readByte() == 0 ? null : in.readString();
        this.abTest = in.readByte() == 0 ? null : in.readString();
        this.supprtAppVerson = in.readByte() == 0 ? null : in.readString();
        this.interfaceVersion = in.readByte() == 0 ? null : in.readString();
        this.param1 = in.readByte() == 0 ? null : in.readString();
        this.param2 = in.readByte() == 0 ? null : in.readString();
        this.res_name = in.readByte() != 0 ? in.readString() : str;
    }

    private AiModelResourceStatus(Long id2, String resid2, Long status2, Long version2, String url2, String teams2, String zipSha2562, String decryptedKey2, Boolean type2, Boolean update2, String xpu2, String emuiFamily2, String productFamily2, String chipsetVendor2, String chipset2, String product2, String productModel2, String district2, String abTest2, String supprtAppVerson2, String interfaceVersion2, String param12, String param22, String res_name2) {
        this.id = id2;
        this.resid = resid2;
        this.status = status2;
        this.version = version2;
        this.url = url2;
        this.teams = teams2;
        this.zipSha256 = zipSha2562;
        this.decryptedKey = decryptedKey2;
        this.type = type2;
        this.update = update2;
        this.xpu = xpu2;
        this.emuiFamily = emuiFamily2;
        this.productFamily = productFamily2;
        this.chipsetVendor = chipsetVendor2;
        this.chipset = chipset2;
        this.product = product2;
        this.productModel = productModel2;
        this.district = district2;
        this.abTest = abTest2;
        this.supprtAppVerson = supprtAppVerson2;
        this.interfaceVersion = interfaceVersion2;
        this.param1 = param12;
        this.param2 = param22;
        this.res_name = res_name2;
    }

    public AiModelResourceStatus() {
    }

    public int describeContents() {
        return 0;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id2) {
        this.id = id2;
        setValue();
    }

    public String getResid() {
        return this.resid;
    }

    public void setResid(String resid2) {
        this.resid = resid2;
        setValue();
    }

    public Long getStatus() {
        return this.status;
    }

    public void setStatus(Long status2) {
        this.status = status2;
        setValue();
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version2) {
        this.version = version2;
        setValue();
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url2) {
        this.url = url2;
        setValue();
    }

    public String getTeams() {
        return this.teams;
    }

    public void setTeams(String teams2) {
        this.teams = teams2;
        setValue();
    }

    public String getZipSha256() {
        return this.zipSha256;
    }

    public void setZipSha256(String zipSha2562) {
        this.zipSha256 = zipSha2562;
        setValue();
    }

    public String getDecryptedKey() {
        return this.decryptedKey;
    }

    public void setDecryptedKey(String decryptedKey2) {
        this.decryptedKey = decryptedKey2;
        setValue();
    }

    public Boolean getType() {
        return this.type;
    }

    public void setType(Boolean type2) {
        this.type = type2;
        setValue();
    }

    public Boolean getUpdate() {
        return this.update;
    }

    public void setUpdate(Boolean update2) {
        this.update = update2;
        setValue();
    }

    public String getXpu() {
        return this.xpu;
    }

    public void setXpu(String xpu2) {
        this.xpu = xpu2;
        setValue();
    }

    public String getEmuiFamily() {
        return this.emuiFamily;
    }

    public void setEmuiFamily(String emuiFamily2) {
        this.emuiFamily = emuiFamily2;
        setValue();
    }

    public String getProductFamily() {
        return this.productFamily;
    }

    public void setProductFamily(String productFamily2) {
        this.productFamily = productFamily2;
        setValue();
    }

    public String getChipsetVendor() {
        return this.chipsetVendor;
    }

    public void setChipsetVendor(String chipsetVendor2) {
        this.chipsetVendor = chipsetVendor2;
        setValue();
    }

    public String getChipset() {
        return this.chipset;
    }

    public void setChipset(String chipset2) {
        this.chipset = chipset2;
        setValue();
    }

    public String getProduct() {
        return this.product;
    }

    public void setProduct(String product2) {
        this.product = product2;
        setValue();
    }

    public String getProductModel() {
        return this.productModel;
    }

    public void setProductModel(String productModel2) {
        this.productModel = productModel2;
        setValue();
    }

    public String getDistrict() {
        return this.district;
    }

    public void setDistrict(String district2) {
        this.district = district2;
        setValue();
    }

    public String getAbTest() {
        return this.abTest;
    }

    public void setAbTest(String abTest2) {
        this.abTest = abTest2;
        setValue();
    }

    public String getSupprtAppVerson() {
        return this.supprtAppVerson;
    }

    public void setSupprtAppVerson(String supprtAppVerson2) {
        this.supprtAppVerson = supprtAppVerson2;
        setValue();
    }

    public String getInterfaceVersion() {
        return this.interfaceVersion;
    }

    public void setInterfaceVersion(String interfaceVersion2) {
        this.interfaceVersion = interfaceVersion2;
        setValue();
    }

    public String getParam1() {
        return this.param1;
    }

    public void setParam1(String param12) {
        this.param1 = param12;
        setValue();
    }

    public String getParam2() {
        return this.param2;
    }

    public void setParam2(String param22) {
        this.param2 = param22;
        setValue();
    }

    public String getRes_name() {
        return this.res_name;
    }

    public void setRes_name(String res_name2) {
        this.res_name = res_name2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        byte b;
        byte b2;
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.id.longValue());
        } else {
            out.writeByte((byte) 0);
            out.writeLong(1);
        }
        if (this.resid != null) {
            out.writeByte((byte) 1);
            out.writeString(this.resid);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.status != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.status.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.version != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.version.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.url != null) {
            out.writeByte((byte) 1);
            out.writeString(this.url);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.teams != null) {
            out.writeByte((byte) 1);
            out.writeString(this.teams);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.zipSha256 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.zipSha256);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.decryptedKey != null) {
            out.writeByte((byte) 1);
            out.writeString(this.decryptedKey);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.type != null) {
            out.writeByte((byte) 1);
            if (this.type.booleanValue()) {
                b2 = 1;
            } else {
                b2 = 0;
            }
            out.writeByte(b2);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.update != null) {
            out.writeByte((byte) 1);
            if (this.update.booleanValue()) {
                b = 1;
            } else {
                b = 0;
            }
            out.writeByte(b);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.xpu != null) {
            out.writeByte((byte) 1);
            out.writeString(this.xpu);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.emuiFamily != null) {
            out.writeByte((byte) 1);
            out.writeString(this.emuiFamily);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.productFamily != null) {
            out.writeByte((byte) 1);
            out.writeString(this.productFamily);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.chipsetVendor != null) {
            out.writeByte((byte) 1);
            out.writeString(this.chipsetVendor);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.chipset != null) {
            out.writeByte((byte) 1);
            out.writeString(this.chipset);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.product != null) {
            out.writeByte((byte) 1);
            out.writeString(this.product);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.productModel != null) {
            out.writeByte((byte) 1);
            out.writeString(this.productModel);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.district != null) {
            out.writeByte((byte) 1);
            out.writeString(this.district);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.abTest != null) {
            out.writeByte((byte) 1);
            out.writeString(this.abTest);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.supprtAppVerson != null) {
            out.writeByte((byte) 1);
            out.writeString(this.supprtAppVerson);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.interfaceVersion != null) {
            out.writeByte((byte) 1);
            out.writeString(this.interfaceVersion);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.param1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.param1);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.param2 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.param2);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.res_name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.res_name);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<AiModelResourceStatus> getHelper() {
        return AiModelResourceStatusHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.aimodel.AiModelResourceStatus";
    }

    public String getDatabaseName() {
        return "dsAiModel";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("AiModelResourceStatus { id: ").append(this.id);
        sb.append(", resid: ").append(this.resid);
        sb.append(", status: ").append(this.status);
        sb.append(", version: ").append(this.version);
        sb.append(", url: ").append(this.url);
        sb.append(", teams: ").append(this.teams);
        sb.append(", zipSha256: ").append(this.zipSha256);
        sb.append(", decryptedKey: ").append(this.decryptedKey);
        sb.append(", type: ").append(this.type);
        sb.append(", update: ").append(this.update);
        sb.append(", xpu: ").append(this.xpu);
        sb.append(", emuiFamily: ").append(this.emuiFamily);
        sb.append(", productFamily: ").append(this.productFamily);
        sb.append(", chipsetVendor: ").append(this.chipsetVendor);
        sb.append(", chipset: ").append(this.chipset);
        sb.append(", product: ").append(this.product);
        sb.append(", productModel: ").append(this.productModel);
        sb.append(", district: ").append(this.district);
        sb.append(", abTest: ").append(this.abTest);
        sb.append(", supprtAppVerson: ").append(this.supprtAppVerson);
        sb.append(", interfaceVersion: ").append(this.interfaceVersion);
        sb.append(", param1: ").append(this.param1);
        sb.append(", param2: ").append(this.param2);
        sb.append(", res_name: ").append(this.res_name);
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
        return "0.0.11";
    }

    public int getDatabaseVersionCode() {
        return 11;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
