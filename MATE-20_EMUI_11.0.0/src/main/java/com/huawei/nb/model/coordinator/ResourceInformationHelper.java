package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ResourceInformationHelper extends AEntityHelper<ResourceInformation> {
    private static final ResourceInformationHelper INSTANCE = new ResourceInformationHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ResourceInformation resourceInformation) {
        return null;
    }

    private ResourceInformationHelper() {
    }

    public static ResourceInformationHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ResourceInformation resourceInformation) {
        Long id = resourceInformation.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String resid = resourceInformation.getResid();
        if (resid != null) {
            statement.bindString(2, resid);
        } else {
            statement.bindNull(2);
        }
        String serviceName = resourceInformation.getServiceName();
        if (serviceName != null) {
            statement.bindString(3, serviceName);
        } else {
            statement.bindNull(3);
        }
        String packageName = resourceInformation.getPackageName();
        if (packageName != null) {
            statement.bindString(4, packageName);
        } else {
            statement.bindNull(4);
        }
        Long versionCode = resourceInformation.getVersionCode();
        if (versionCode != null) {
            statement.bindLong(5, versionCode.longValue());
        } else {
            statement.bindNull(5);
        }
        String versionName = resourceInformation.getVersionName();
        if (versionName != null) {
            statement.bindString(6, versionName);
        } else {
            statement.bindNull(6);
        }
        String dependence = resourceInformation.getDependence();
        if (dependence != null) {
            statement.bindString(7, dependence);
        } else {
            statement.bindNull(7);
        }
        String xpu = resourceInformation.getXpu();
        if (xpu != null) {
            statement.bindString(8, xpu);
        } else {
            statement.bindNull(8);
        }
        String emuiFamily = resourceInformation.getEmuiFamily();
        if (emuiFamily != null) {
            statement.bindString(9, emuiFamily);
        } else {
            statement.bindNull(9);
        }
        String productFamily = resourceInformation.getProductFamily();
        if (productFamily != null) {
            statement.bindString(10, productFamily);
        } else {
            statement.bindNull(10);
        }
        String chipsetVendor = resourceInformation.getChipsetVendor();
        if (chipsetVendor != null) {
            statement.bindString(11, chipsetVendor);
        } else {
            statement.bindNull(11);
        }
        String chipset = resourceInformation.getChipset();
        if (chipset != null) {
            statement.bindString(12, chipset);
        } else {
            statement.bindNull(12);
        }
        String product = resourceInformation.getProduct();
        if (product != null) {
            statement.bindString(13, product);
        } else {
            statement.bindNull(13);
        }
        String productModel = resourceInformation.getProductModel();
        if (productModel != null) {
            statement.bindString(14, productModel);
        } else {
            statement.bindNull(14);
        }
        String district = resourceInformation.getDistrict();
        if (district != null) {
            statement.bindString(15, district);
        } else {
            statement.bindNull(15);
        }
        String abTest = resourceInformation.getAbTest();
        if (abTest != null) {
            statement.bindString(16, abTest);
        } else {
            statement.bindNull(16);
        }
        String supportedAppVersion = resourceInformation.getSupportedAppVersion();
        if (supportedAppVersion != null) {
            statement.bindString(17, supportedAppVersion);
        } else {
            statement.bindNull(17);
        }
        Integer appVersion = resourceInformation.getAppVersion();
        if (appVersion != null) {
            statement.bindLong(18, (long) appVersion.intValue());
        } else {
            statement.bindNull(18);
        }
        String interfaceVersion = resourceInformation.getInterfaceVersion();
        if (interfaceVersion != null) {
            statement.bindString(19, interfaceVersion);
        } else {
            statement.bindNull(19);
        }
        Integer allowUpdate = resourceInformation.getAllowUpdate();
        if (allowUpdate != null) {
            statement.bindLong(20, (long) allowUpdate.intValue());
        } else {
            statement.bindNull(20);
        }
        String permission = resourceInformation.getPermission();
        if (permission != null) {
            statement.bindString(21, permission);
        } else {
            statement.bindNull(21);
        }
        Integer forceUpdate = resourceInformation.getForceUpdate();
        if (forceUpdate != null) {
            statement.bindLong(22, (long) forceUpdate.intValue());
        } else {
            statement.bindNull(22);
        }
        Integer isEncrypt = resourceInformation.getIsEncrypt();
        if (isEncrypt != null) {
            statement.bindLong(23, (long) isEncrypt.intValue());
        } else {
            statement.bindNull(23);
        }
        String resUrl = resourceInformation.getResUrl();
        if (resUrl != null) {
            statement.bindString(24, resUrl);
        } else {
            statement.bindNull(24);
        }
        String resPath = resourceInformation.getResPath();
        if (resPath != null) {
            statement.bindString(25, resPath);
        } else {
            statement.bindNull(25);
        }
        Long resDeletePolicy = resourceInformation.getResDeletePolicy();
        if (resDeletePolicy != null) {
            statement.bindLong(26, resDeletePolicy.longValue());
        } else {
            statement.bindNull(26);
        }
        Long resNotifyPolicy = resourceInformation.getResNotifyPolicy();
        if (resNotifyPolicy != null) {
            statement.bindLong(27, resNotifyPolicy.longValue());
        } else {
            statement.bindNull(27);
        }
        Long latestTimestamp = resourceInformation.getLatestTimestamp();
        if (latestTimestamp != null) {
            statement.bindLong(28, latestTimestamp.longValue());
        } else {
            statement.bindNull(28);
        }
        Integer cycleMinutes = resourceInformation.getCycleMinutes();
        if (cycleMinutes != null) {
            statement.bindLong(29, (long) cycleMinutes.intValue());
        } else {
            statement.bindNull(29);
        }
        Integer supportDiff = resourceInformation.getSupportDiff();
        if (supportDiff != null) {
            statement.bindLong(30, (long) supportDiff.intValue());
        } else {
            statement.bindNull(30);
        }
        Integer isPreset = resourceInformation.getIsPreset();
        if (isPreset != null) {
            statement.bindLong(31, (long) isPreset.intValue());
        } else {
            statement.bindNull(31);
        }
        String reserve1 = resourceInformation.getReserve1();
        if (reserve1 != null) {
            statement.bindString(32, reserve1);
        } else {
            statement.bindNull(32);
        }
        String reserve2 = resourceInformation.getReserve2();
        if (reserve2 != null) {
            statement.bindString(33, reserve2);
        } else {
            statement.bindNull(33);
        }
        Integer isExtended = resourceInformation.getIsExtended();
        if (isExtended != null) {
            statement.bindLong(34, (long) isExtended.intValue());
        } else {
            statement.bindNull(34);
        }
        Long fileSize = resourceInformation.getFileSize();
        if (fileSize != null) {
            statement.bindLong(35, fileSize.longValue());
        } else {
            statement.bindNull(35);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ResourceInformation readObject(Cursor cursor, int i) {
        return new ResourceInformation(cursor);
    }

    public void setPrimaryKeyValue(ResourceInformation resourceInformation, long j) {
        resourceInformation.setId(Long.valueOf(j));
    }
}
