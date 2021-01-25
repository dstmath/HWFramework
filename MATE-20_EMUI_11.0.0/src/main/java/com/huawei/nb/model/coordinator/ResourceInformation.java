package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ResourceInformation extends AManagedObject {
    public static final Parcelable.Creator<ResourceInformation> CREATOR = new Parcelable.Creator<ResourceInformation>() {
        /* class com.huawei.nb.model.coordinator.ResourceInformation.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ResourceInformation createFromParcel(Parcel parcel) {
            return new ResourceInformation(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ResourceInformation[] newArray(int i) {
            return new ResourceInformation[i];
        }
    };
    private String abTest;
    private Integer allowUpdate;
    private Integer appVersion;
    private String chipset;
    private String chipsetVendor;
    private Integer cycleMinutes = 1440;
    private String dependence;
    private String district;
    private String emuiFamily;
    private Long fileSize = 0L;
    private Integer forceUpdate;
    private Long id;
    private String interfaceVersion;
    private Integer isEncrypt;
    private Integer isExtended = 1;
    private Integer isPreset;
    private Long latestTimestamp = 0L;
    private String packageName;
    private String permission;
    private String product;
    private String productFamily;
    private String productModel;
    private Long resDeletePolicy;
    private Long resNotifyPolicy;
    private String resPath;
    private String resUrl;
    private String reserve1;
    private String reserve2;
    private String resid;
    private String serviceName;
    private Integer supportDiff;
    private String supportedAppVersion;
    private Long versionCode;
    private String versionName;
    private String xpu;

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
        return "com.huawei.nb.model.coordinator.ResourceInformation";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public ResourceInformation(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.resid = cursor.getString(2);
        this.serviceName = cursor.getString(3);
        this.packageName = cursor.getString(4);
        this.versionCode = cursor.isNull(5) ? null : Long.valueOf(cursor.getLong(5));
        this.versionName = cursor.getString(6);
        this.dependence = cursor.getString(7);
        this.xpu = cursor.getString(8);
        this.emuiFamily = cursor.getString(9);
        this.productFamily = cursor.getString(10);
        this.chipsetVendor = cursor.getString(11);
        this.chipset = cursor.getString(12);
        this.product = cursor.getString(13);
        this.productModel = cursor.getString(14);
        this.district = cursor.getString(15);
        this.abTest = cursor.getString(16);
        this.supportedAppVersion = cursor.getString(17);
        this.appVersion = cursor.isNull(18) ? null : Integer.valueOf(cursor.getInt(18));
        this.interfaceVersion = cursor.getString(19);
        this.allowUpdate = cursor.isNull(20) ? null : Integer.valueOf(cursor.getInt(20));
        this.permission = cursor.getString(21);
        this.forceUpdate = cursor.isNull(22) ? null : Integer.valueOf(cursor.getInt(22));
        this.isEncrypt = cursor.isNull(23) ? null : Integer.valueOf(cursor.getInt(23));
        this.resUrl = cursor.getString(24);
        this.resPath = cursor.getString(25);
        this.resDeletePolicy = cursor.isNull(26) ? null : Long.valueOf(cursor.getLong(26));
        this.resNotifyPolicy = cursor.isNull(27) ? null : Long.valueOf(cursor.getLong(27));
        this.latestTimestamp = cursor.isNull(28) ? null : Long.valueOf(cursor.getLong(28));
        this.cycleMinutes = cursor.isNull(29) ? null : Integer.valueOf(cursor.getInt(29));
        this.supportDiff = cursor.isNull(30) ? null : Integer.valueOf(cursor.getInt(30));
        this.isPreset = cursor.isNull(31) ? null : Integer.valueOf(cursor.getInt(31));
        this.reserve1 = cursor.getString(32);
        this.reserve2 = cursor.getString(33);
        this.isExtended = cursor.isNull(34) ? null : Integer.valueOf(cursor.getInt(34));
        this.fileSize = !cursor.isNull(35) ? Long.valueOf(cursor.getLong(35)) : l;
    }

    public ResourceInformation(Parcel parcel) {
        super(parcel);
        Long l = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.resid = parcel.readByte() == 0 ? null : parcel.readString();
        this.serviceName = parcel.readByte() == 0 ? null : parcel.readString();
        this.packageName = parcel.readByte() == 0 ? null : parcel.readString();
        this.versionCode = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.versionName = parcel.readByte() == 0 ? null : parcel.readString();
        this.dependence = parcel.readByte() == 0 ? null : parcel.readString();
        this.xpu = parcel.readByte() == 0 ? null : parcel.readString();
        this.emuiFamily = parcel.readByte() == 0 ? null : parcel.readString();
        this.productFamily = parcel.readByte() == 0 ? null : parcel.readString();
        this.chipsetVendor = parcel.readByte() == 0 ? null : parcel.readString();
        this.chipset = parcel.readByte() == 0 ? null : parcel.readString();
        this.product = parcel.readByte() == 0 ? null : parcel.readString();
        this.productModel = parcel.readByte() == 0 ? null : parcel.readString();
        this.district = parcel.readByte() == 0 ? null : parcel.readString();
        this.abTest = parcel.readByte() == 0 ? null : parcel.readString();
        this.supportedAppVersion = parcel.readByte() == 0 ? null : parcel.readString();
        this.appVersion = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.interfaceVersion = parcel.readByte() == 0 ? null : parcel.readString();
        this.allowUpdate = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.permission = parcel.readByte() == 0 ? null : parcel.readString();
        this.forceUpdate = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.isEncrypt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.resUrl = parcel.readByte() == 0 ? null : parcel.readString();
        this.resPath = parcel.readByte() == 0 ? null : parcel.readString();
        this.resDeletePolicy = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.resNotifyPolicy = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.latestTimestamp = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.cycleMinutes = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.supportDiff = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.isPreset = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.reserve1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserve2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.isExtended = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.fileSize = parcel.readByte() != 0 ? Long.valueOf(parcel.readLong()) : l;
    }

    private ResourceInformation(Long l, String str, String str2, String str3, Long l2, String str4, String str5, String str6, String str7, String str8, String str9, String str10, String str11, String str12, String str13, String str14, String str15, Integer num, String str16, Integer num2, String str17, Integer num3, Integer num4, String str18, String str19, Long l3, Long l4, Long l5, Integer num5, Integer num6, Integer num7, String str20, String str21, Integer num8, Long l6) {
        this.id = l;
        this.resid = str;
        this.serviceName = str2;
        this.packageName = str3;
        this.versionCode = l2;
        this.versionName = str4;
        this.dependence = str5;
        this.xpu = str6;
        this.emuiFamily = str7;
        this.productFamily = str8;
        this.chipsetVendor = str9;
        this.chipset = str10;
        this.product = str11;
        this.productModel = str12;
        this.district = str13;
        this.abTest = str14;
        this.supportedAppVersion = str15;
        this.appVersion = num;
        this.interfaceVersion = str16;
        this.allowUpdate = num2;
        this.permission = str17;
        this.forceUpdate = num3;
        this.isEncrypt = num4;
        this.resUrl = str18;
        this.resPath = str19;
        this.resDeletePolicy = l3;
        this.resNotifyPolicy = l4;
        this.latestTimestamp = l5;
        this.cycleMinutes = num5;
        this.supportDiff = num6;
        this.isPreset = num7;
        this.reserve1 = str20;
        this.reserve2 = str21;
        this.isExtended = num8;
        this.fileSize = l6;
    }

    public ResourceInformation() {
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

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(String str) {
        this.serviceName = str;
        setValue();
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String str) {
        this.packageName = str;
        setValue();
    }

    public Long getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(Long l) {
        this.versionCode = l;
        setValue();
    }

    public String getVersionName() {
        return this.versionName;
    }

    public void setVersionName(String str) {
        this.versionName = str;
        setValue();
    }

    public String getDependence() {
        return this.dependence;
    }

    public void setDependence(String str) {
        this.dependence = str;
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

    public String getSupportedAppVersion() {
        return this.supportedAppVersion;
    }

    public void setSupportedAppVersion(String str) {
        this.supportedAppVersion = str;
        setValue();
    }

    public Integer getAppVersion() {
        return this.appVersion;
    }

    public void setAppVersion(Integer num) {
        this.appVersion = num;
        setValue();
    }

    public String getInterfaceVersion() {
        return this.interfaceVersion;
    }

    public void setInterfaceVersion(String str) {
        this.interfaceVersion = str;
        setValue();
    }

    public Integer getAllowUpdate() {
        return this.allowUpdate;
    }

    public void setAllowUpdate(Integer num) {
        this.allowUpdate = num;
        setValue();
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String str) {
        this.permission = str;
        setValue();
    }

    public Integer getForceUpdate() {
        return this.forceUpdate;
    }

    public void setForceUpdate(Integer num) {
        this.forceUpdate = num;
        setValue();
    }

    public Integer getIsEncrypt() {
        return this.isEncrypt;
    }

    public void setIsEncrypt(Integer num) {
        this.isEncrypt = num;
        setValue();
    }

    public String getResUrl() {
        return this.resUrl;
    }

    public void setResUrl(String str) {
        this.resUrl = str;
        setValue();
    }

    public String getResPath() {
        return this.resPath;
    }

    public void setResPath(String str) {
        this.resPath = str;
        setValue();
    }

    public Long getResDeletePolicy() {
        return this.resDeletePolicy;
    }

    public void setResDeletePolicy(Long l) {
        this.resDeletePolicy = l;
        setValue();
    }

    public Long getResNotifyPolicy() {
        return this.resNotifyPolicy;
    }

    public void setResNotifyPolicy(Long l) {
        this.resNotifyPolicy = l;
        setValue();
    }

    public Long getLatestTimestamp() {
        return this.latestTimestamp;
    }

    public void setLatestTimestamp(Long l) {
        this.latestTimestamp = l;
        setValue();
    }

    public Integer getCycleMinutes() {
        return this.cycleMinutes;
    }

    public void setCycleMinutes(Integer num) {
        this.cycleMinutes = num;
        setValue();
    }

    public Integer getSupportDiff() {
        return this.supportDiff;
    }

    public void setSupportDiff(Integer num) {
        this.supportDiff = num;
        setValue();
    }

    public Integer getIsPreset() {
        return this.isPreset;
    }

    public void setIsPreset(Integer num) {
        this.isPreset = num;
        setValue();
    }

    public String getReserve1() {
        return this.reserve1;
    }

    public void setReserve1(String str) {
        this.reserve1 = str;
        setValue();
    }

    public String getReserve2() {
        return this.reserve2;
    }

    public void setReserve2(String str) {
        this.reserve2 = str;
        setValue();
    }

    public Integer getIsExtended() {
        return this.isExtended;
    }

    public void setIsExtended(Integer num) {
        this.isExtended = num;
        setValue();
    }

    public Long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(Long l) {
        this.fileSize = l;
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
        if (this.serviceName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.serviceName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.packageName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.packageName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.versionCode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.versionCode.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.versionName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.versionName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.dependence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.dependence);
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
        if (this.supportedAppVersion != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.supportedAppVersion);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.appVersion != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.appVersion.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.interfaceVersion != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.interfaceVersion);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.allowUpdate != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.allowUpdate.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.permission != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.permission);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.forceUpdate != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.forceUpdate.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.isEncrypt != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.isEncrypt.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.resUrl != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.resUrl);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.resPath != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.resPath);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.resDeletePolicy != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.resDeletePolicy.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.resNotifyPolicy != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.resNotifyPolicy.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.latestTimestamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.latestTimestamp.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.cycleMinutes != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.cycleMinutes.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.supportDiff != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.supportDiff.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.isPreset != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.isPreset.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserve1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserve1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserve2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserve2);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.isExtended != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.isExtended.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.fileSize != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.fileSize.longValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<ResourceInformation> getHelper() {
        return ResourceInformationHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "ResourceInformation { id: " + this.id + ", resid: " + this.resid + ", serviceName: " + this.serviceName + ", packageName: " + this.packageName + ", versionCode: " + this.versionCode + ", versionName: " + this.versionName + ", dependence: " + this.dependence + ", xpu: " + this.xpu + ", emuiFamily: " + this.emuiFamily + ", productFamily: " + this.productFamily + ", chipsetVendor: " + this.chipsetVendor + ", chipset: " + this.chipset + ", product: " + this.product + ", productModel: " + this.productModel + ", district: " + this.district + ", abTest: " + this.abTest + ", supportedAppVersion: " + this.supportedAppVersion + ", appVersion: " + this.appVersion + ", interfaceVersion: " + this.interfaceVersion + ", allowUpdate: " + this.allowUpdate + ", permission: " + this.permission + ", forceUpdate: " + this.forceUpdate + ", isEncrypt: " + this.isEncrypt + ", resUrl: " + this.resUrl + ", resPath: " + this.resPath + ", resDeletePolicy: " + this.resDeletePolicy + ", resNotifyPolicy: " + this.resNotifyPolicy + ", latestTimestamp: " + this.latestTimestamp + ", cycleMinutes: " + this.cycleMinutes + ", supportDiff: " + this.supportDiff + ", isPreset: " + this.isPreset + ", reserve1: " + this.reserve1 + ", reserve2: " + this.reserve2 + ", isExtended: " + this.isExtended + ", fileSize: " + this.fileSize + " }";
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
