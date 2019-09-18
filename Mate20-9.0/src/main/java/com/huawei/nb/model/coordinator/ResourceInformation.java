package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ResourceInformation extends AManagedObject {
    public static final Parcelable.Creator<ResourceInformation> CREATOR = new Parcelable.Creator<ResourceInformation>() {
        public ResourceInformation createFromParcel(Parcel in) {
            return new ResourceInformation(in);
        }

        public ResourceInformation[] newArray(int size) {
            return new ResourceInformation[size];
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

    public ResourceInformation(Cursor cursor) {
        Long valueOf;
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.resid = cursor.getString(2);
        this.serviceName = cursor.getString(3);
        this.packageName = cursor.getString(4);
        if (cursor.isNull(5)) {
            valueOf = null;
        } else {
            valueOf = Long.valueOf(cursor.getLong(5));
        }
        this.versionCode = valueOf;
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ResourceInformation(Parcel in) {
        super(in);
        Long l = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.resid = in.readByte() == 0 ? null : in.readString();
        this.serviceName = in.readByte() == 0 ? null : in.readString();
        this.packageName = in.readByte() == 0 ? null : in.readString();
        this.versionCode = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.versionName = in.readByte() == 0 ? null : in.readString();
        this.dependence = in.readByte() == 0 ? null : in.readString();
        this.xpu = in.readByte() == 0 ? null : in.readString();
        this.emuiFamily = in.readByte() == 0 ? null : in.readString();
        this.productFamily = in.readByte() == 0 ? null : in.readString();
        this.chipsetVendor = in.readByte() == 0 ? null : in.readString();
        this.chipset = in.readByte() == 0 ? null : in.readString();
        this.product = in.readByte() == 0 ? null : in.readString();
        this.productModel = in.readByte() == 0 ? null : in.readString();
        this.district = in.readByte() == 0 ? null : in.readString();
        this.abTest = in.readByte() == 0 ? null : in.readString();
        this.supportedAppVersion = in.readByte() == 0 ? null : in.readString();
        this.appVersion = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.interfaceVersion = in.readByte() == 0 ? null : in.readString();
        this.allowUpdate = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.permission = in.readByte() == 0 ? null : in.readString();
        this.forceUpdate = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.isEncrypt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.resUrl = in.readByte() == 0 ? null : in.readString();
        this.resPath = in.readByte() == 0 ? null : in.readString();
        this.resDeletePolicy = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.resNotifyPolicy = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.latestTimestamp = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.cycleMinutes = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.supportDiff = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.isPreset = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.reserve1 = in.readByte() == 0 ? null : in.readString();
        this.reserve2 = in.readByte() == 0 ? null : in.readString();
        this.isExtended = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.fileSize = in.readByte() != 0 ? Long.valueOf(in.readLong()) : l;
    }

    private ResourceInformation(Long id2, String resid2, String serviceName2, String packageName2, Long versionCode2, String versionName2, String dependence2, String xpu2, String emuiFamily2, String productFamily2, String chipsetVendor2, String chipset2, String product2, String productModel2, String district2, String abTest2, String supportedAppVersion2, Integer appVersion2, String interfaceVersion2, Integer allowUpdate2, String permission2, Integer forceUpdate2, Integer isEncrypt2, String resUrl2, String resPath2, Long resDeletePolicy2, Long resNotifyPolicy2, Long latestTimestamp2, Integer cycleMinutes2, Integer supportDiff2, Integer isPreset2, String reserve12, String reserve22, Integer isExtended2, Long fileSize2) {
        this.id = id2;
        this.resid = resid2;
        this.serviceName = serviceName2;
        this.packageName = packageName2;
        this.versionCode = versionCode2;
        this.versionName = versionName2;
        this.dependence = dependence2;
        this.xpu = xpu2;
        this.emuiFamily = emuiFamily2;
        this.productFamily = productFamily2;
        this.chipsetVendor = chipsetVendor2;
        this.chipset = chipset2;
        this.product = product2;
        this.productModel = productModel2;
        this.district = district2;
        this.abTest = abTest2;
        this.supportedAppVersion = supportedAppVersion2;
        this.appVersion = appVersion2;
        this.interfaceVersion = interfaceVersion2;
        this.allowUpdate = allowUpdate2;
        this.permission = permission2;
        this.forceUpdate = forceUpdate2;
        this.isEncrypt = isEncrypt2;
        this.resUrl = resUrl2;
        this.resPath = resPath2;
        this.resDeletePolicy = resDeletePolicy2;
        this.resNotifyPolicy = resNotifyPolicy2;
        this.latestTimestamp = latestTimestamp2;
        this.cycleMinutes = cycleMinutes2;
        this.supportDiff = supportDiff2;
        this.isPreset = isPreset2;
        this.reserve1 = reserve12;
        this.reserve2 = reserve22;
        this.isExtended = isExtended2;
        this.fileSize = fileSize2;
    }

    public ResourceInformation() {
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

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(String serviceName2) {
        this.serviceName = serviceName2;
        setValue();
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
        setValue();
    }

    public Long getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(Long versionCode2) {
        this.versionCode = versionCode2;
        setValue();
    }

    public String getVersionName() {
        return this.versionName;
    }

    public void setVersionName(String versionName2) {
        this.versionName = versionName2;
        setValue();
    }

    public String getDependence() {
        return this.dependence;
    }

    public void setDependence(String dependence2) {
        this.dependence = dependence2;
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

    public String getSupportedAppVersion() {
        return this.supportedAppVersion;
    }

    public void setSupportedAppVersion(String supportedAppVersion2) {
        this.supportedAppVersion = supportedAppVersion2;
        setValue();
    }

    public Integer getAppVersion() {
        return this.appVersion;
    }

    public void setAppVersion(Integer appVersion2) {
        this.appVersion = appVersion2;
        setValue();
    }

    public String getInterfaceVersion() {
        return this.interfaceVersion;
    }

    public void setInterfaceVersion(String interfaceVersion2) {
        this.interfaceVersion = interfaceVersion2;
        setValue();
    }

    public Integer getAllowUpdate() {
        return this.allowUpdate;
    }

    public void setAllowUpdate(Integer allowUpdate2) {
        this.allowUpdate = allowUpdate2;
        setValue();
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String permission2) {
        this.permission = permission2;
        setValue();
    }

    public Integer getForceUpdate() {
        return this.forceUpdate;
    }

    public void setForceUpdate(Integer forceUpdate2) {
        this.forceUpdate = forceUpdate2;
        setValue();
    }

    public Integer getIsEncrypt() {
        return this.isEncrypt;
    }

    public void setIsEncrypt(Integer isEncrypt2) {
        this.isEncrypt = isEncrypt2;
        setValue();
    }

    public String getResUrl() {
        return this.resUrl;
    }

    public void setResUrl(String resUrl2) {
        this.resUrl = resUrl2;
        setValue();
    }

    public String getResPath() {
        return this.resPath;
    }

    public void setResPath(String resPath2) {
        this.resPath = resPath2;
        setValue();
    }

    public Long getResDeletePolicy() {
        return this.resDeletePolicy;
    }

    public void setResDeletePolicy(Long resDeletePolicy2) {
        this.resDeletePolicy = resDeletePolicy2;
        setValue();
    }

    public Long getResNotifyPolicy() {
        return this.resNotifyPolicy;
    }

    public void setResNotifyPolicy(Long resNotifyPolicy2) {
        this.resNotifyPolicy = resNotifyPolicy2;
        setValue();
    }

    public Long getLatestTimestamp() {
        return this.latestTimestamp;
    }

    public void setLatestTimestamp(Long latestTimestamp2) {
        this.latestTimestamp = latestTimestamp2;
        setValue();
    }

    public Integer getCycleMinutes() {
        return this.cycleMinutes;
    }

    public void setCycleMinutes(Integer cycleMinutes2) {
        this.cycleMinutes = cycleMinutes2;
        setValue();
    }

    public Integer getSupportDiff() {
        return this.supportDiff;
    }

    public void setSupportDiff(Integer supportDiff2) {
        this.supportDiff = supportDiff2;
        setValue();
    }

    public Integer getIsPreset() {
        return this.isPreset;
    }

    public void setIsPreset(Integer isPreset2) {
        this.isPreset = isPreset2;
        setValue();
    }

    public String getReserve1() {
        return this.reserve1;
    }

    public void setReserve1(String reserve12) {
        this.reserve1 = reserve12;
        setValue();
    }

    public String getReserve2() {
        return this.reserve2;
    }

    public void setReserve2(String reserve22) {
        this.reserve2 = reserve22;
        setValue();
    }

    public Integer getIsExtended() {
        return this.isExtended;
    }

    public void setIsExtended(Integer isExtended2) {
        this.isExtended = isExtended2;
        setValue();
    }

    public Long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(Long fileSize2) {
        this.fileSize = fileSize2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
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
        if (this.serviceName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.serviceName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.packageName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.packageName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.versionCode != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.versionCode.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.versionName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.versionName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.dependence != null) {
            out.writeByte((byte) 1);
            out.writeString(this.dependence);
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
        if (this.supportedAppVersion != null) {
            out.writeByte((byte) 1);
            out.writeString(this.supportedAppVersion);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.appVersion != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.appVersion.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.interfaceVersion != null) {
            out.writeByte((byte) 1);
            out.writeString(this.interfaceVersion);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.allowUpdate != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.allowUpdate.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.permission != null) {
            out.writeByte((byte) 1);
            out.writeString(this.permission);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.forceUpdate != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.forceUpdate.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isEncrypt != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.isEncrypt.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.resUrl != null) {
            out.writeByte((byte) 1);
            out.writeString(this.resUrl);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.resPath != null) {
            out.writeByte((byte) 1);
            out.writeString(this.resPath);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.resDeletePolicy != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.resDeletePolicy.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.resNotifyPolicy != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.resNotifyPolicy.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.latestTimestamp != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.latestTimestamp.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.cycleMinutes != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.cycleMinutes.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.supportDiff != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.supportDiff.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isPreset != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.isPreset.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserve1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserve1);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserve2 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserve2);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isExtended != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.isExtended.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.fileSize != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.fileSize.longValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<ResourceInformation> getHelper() {
        return ResourceInformationHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.coordinator.ResourceInformation";
    }

    public String getDatabaseName() {
        return "dsAiModel";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ResourceInformation { id: ").append(this.id);
        sb.append(", resid: ").append(this.resid);
        sb.append(", serviceName: ").append(this.serviceName);
        sb.append(", packageName: ").append(this.packageName);
        sb.append(", versionCode: ").append(this.versionCode);
        sb.append(", versionName: ").append(this.versionName);
        sb.append(", dependence: ").append(this.dependence);
        sb.append(", xpu: ").append(this.xpu);
        sb.append(", emuiFamily: ").append(this.emuiFamily);
        sb.append(", productFamily: ").append(this.productFamily);
        sb.append(", chipsetVendor: ").append(this.chipsetVendor);
        sb.append(", chipset: ").append(this.chipset);
        sb.append(", product: ").append(this.product);
        sb.append(", productModel: ").append(this.productModel);
        sb.append(", district: ").append(this.district);
        sb.append(", abTest: ").append(this.abTest);
        sb.append(", supportedAppVersion: ").append(this.supportedAppVersion);
        sb.append(", appVersion: ").append(this.appVersion);
        sb.append(", interfaceVersion: ").append(this.interfaceVersion);
        sb.append(", allowUpdate: ").append(this.allowUpdate);
        sb.append(", permission: ").append(this.permission);
        sb.append(", forceUpdate: ").append(this.forceUpdate);
        sb.append(", isEncrypt: ").append(this.isEncrypt);
        sb.append(", resUrl: ").append(this.resUrl);
        sb.append(", resPath: ").append(this.resPath);
        sb.append(", resDeletePolicy: ").append(this.resDeletePolicy);
        sb.append(", resNotifyPolicy: ").append(this.resNotifyPolicy);
        sb.append(", latestTimestamp: ").append(this.latestTimestamp);
        sb.append(", cycleMinutes: ").append(this.cycleMinutes);
        sb.append(", supportDiff: ").append(this.supportDiff);
        sb.append(", isPreset: ").append(this.isPreset);
        sb.append(", reserve1: ").append(this.reserve1);
        sb.append(", reserve2: ").append(this.reserve2);
        sb.append(", isExtended: ").append(this.isExtended);
        sb.append(", fileSize: ").append(this.fileSize);
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
