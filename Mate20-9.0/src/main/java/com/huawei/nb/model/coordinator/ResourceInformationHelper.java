package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ResourceInformationHelper extends AEntityHelper<ResourceInformation> {
    private static final ResourceInformationHelper INSTANCE = new ResourceInformationHelper();

    private ResourceInformationHelper() {
    }

    public static ResourceInformationHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ResourceInformation object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String resid = object.getResid();
        if (resid != null) {
            statement.bindString(2, resid);
        } else {
            statement.bindNull(2);
        }
        String serviceName = object.getServiceName();
        if (serviceName != null) {
            statement.bindString(3, serviceName);
        } else {
            statement.bindNull(3);
        }
        String packageName = object.getPackageName();
        if (packageName != null) {
            statement.bindString(4, packageName);
        } else {
            statement.bindNull(4);
        }
        Long versionCode = object.getVersionCode();
        if (versionCode != null) {
            statement.bindLong(5, versionCode.longValue());
        } else {
            statement.bindNull(5);
        }
        String versionName = object.getVersionName();
        if (versionName != null) {
            statement.bindString(6, versionName);
        } else {
            statement.bindNull(6);
        }
        String dependence = object.getDependence();
        if (dependence != null) {
            statement.bindString(7, dependence);
        } else {
            statement.bindNull(7);
        }
        String xpu = object.getXpu();
        if (xpu != null) {
            statement.bindString(8, xpu);
        } else {
            statement.bindNull(8);
        }
        String emuiFamily = object.getEmuiFamily();
        if (emuiFamily != null) {
            statement.bindString(9, emuiFamily);
        } else {
            statement.bindNull(9);
        }
        String productFamily = object.getProductFamily();
        if (productFamily != null) {
            statement.bindString(10, productFamily);
        } else {
            statement.bindNull(10);
        }
        String chipsetVendor = object.getChipsetVendor();
        if (chipsetVendor != null) {
            statement.bindString(11, chipsetVendor);
        } else {
            statement.bindNull(11);
        }
        String chipset = object.getChipset();
        if (chipset != null) {
            statement.bindString(12, chipset);
        } else {
            statement.bindNull(12);
        }
        String product = object.getProduct();
        if (product != null) {
            statement.bindString(13, product);
        } else {
            statement.bindNull(13);
        }
        String productModel = object.getProductModel();
        if (productModel != null) {
            statement.bindString(14, productModel);
        } else {
            statement.bindNull(14);
        }
        String district = object.getDistrict();
        if (district != null) {
            statement.bindString(15, district);
        } else {
            statement.bindNull(15);
        }
        String abTest = object.getAbTest();
        if (abTest != null) {
            statement.bindString(16, abTest);
        } else {
            statement.bindNull(16);
        }
        String supportedAppVersion = object.getSupportedAppVersion();
        if (supportedAppVersion != null) {
            statement.bindString(17, supportedAppVersion);
        } else {
            statement.bindNull(17);
        }
        Integer appVersion = object.getAppVersion();
        if (appVersion != null) {
            statement.bindLong(18, (long) appVersion.intValue());
        } else {
            statement.bindNull(18);
        }
        String interfaceVersion = object.getInterfaceVersion();
        if (interfaceVersion != null) {
            statement.bindString(19, interfaceVersion);
        } else {
            statement.bindNull(19);
        }
        Integer allowUpdate = object.getAllowUpdate();
        if (allowUpdate != null) {
            statement.bindLong(20, (long) allowUpdate.intValue());
        } else {
            statement.bindNull(20);
        }
        String permission = object.getPermission();
        if (permission != null) {
            statement.bindString(21, permission);
        } else {
            statement.bindNull(21);
        }
        Integer forceUpdate = object.getForceUpdate();
        if (forceUpdate != null) {
            statement.bindLong(22, (long) forceUpdate.intValue());
        } else {
            statement.bindNull(22);
        }
        Integer isEncrypt = object.getIsEncrypt();
        if (isEncrypt != null) {
            statement.bindLong(23, (long) isEncrypt.intValue());
        } else {
            statement.bindNull(23);
        }
        String resUrl = object.getResUrl();
        if (resUrl != null) {
            statement.bindString(24, resUrl);
        } else {
            statement.bindNull(24);
        }
        String resPath = object.getResPath();
        if (resPath != null) {
            statement.bindString(25, resPath);
        } else {
            statement.bindNull(25);
        }
        Long resDeletePolicy = object.getResDeletePolicy();
        if (resDeletePolicy != null) {
            statement.bindLong(26, resDeletePolicy.longValue());
        } else {
            statement.bindNull(26);
        }
        Long resNotifyPolicy = object.getResNotifyPolicy();
        if (resNotifyPolicy != null) {
            statement.bindLong(27, resNotifyPolicy.longValue());
        } else {
            statement.bindNull(27);
        }
        Long latestTimestamp = object.getLatestTimestamp();
        if (latestTimestamp != null) {
            statement.bindLong(28, latestTimestamp.longValue());
        } else {
            statement.bindNull(28);
        }
        Integer cycleMinutes = object.getCycleMinutes();
        if (cycleMinutes != null) {
            statement.bindLong(29, (long) cycleMinutes.intValue());
        } else {
            statement.bindNull(29);
        }
        Integer supportDiff = object.getSupportDiff();
        if (supportDiff != null) {
            statement.bindLong(30, (long) supportDiff.intValue());
        } else {
            statement.bindNull(30);
        }
        Integer isPreset = object.getIsPreset();
        if (isPreset != null) {
            statement.bindLong(31, (long) isPreset.intValue());
        } else {
            statement.bindNull(31);
        }
        String reserve1 = object.getReserve1();
        if (reserve1 != null) {
            statement.bindString(32, reserve1);
        } else {
            statement.bindNull(32);
        }
        String reserve2 = object.getReserve2();
        if (reserve2 != null) {
            statement.bindString(33, reserve2);
        } else {
            statement.bindNull(33);
        }
        Integer isExtended = object.getIsExtended();
        if (isExtended != null) {
            statement.bindLong(34, (long) isExtended.intValue());
        } else {
            statement.bindNull(34);
        }
        Long fileSize = object.getFileSize();
        if (fileSize != null) {
            statement.bindLong(35, fileSize.longValue());
        } else {
            statement.bindNull(35);
        }
    }

    public ResourceInformation readObject(Cursor cursor, int offset) {
        return new ResourceInformation(cursor);
    }

    public void setPrimaryKeyValue(ResourceInformation object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, ResourceInformation object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
