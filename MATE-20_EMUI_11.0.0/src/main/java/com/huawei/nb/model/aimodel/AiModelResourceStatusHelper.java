package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelResourceStatusHelper extends AEntityHelper<AiModelResourceStatus> {
    private static final AiModelResourceStatusHelper INSTANCE = new AiModelResourceStatusHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, AiModelResourceStatus aiModelResourceStatus) {
        return null;
    }

    private AiModelResourceStatusHelper() {
    }

    public static AiModelResourceStatusHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, AiModelResourceStatus aiModelResourceStatus) {
        Long id = aiModelResourceStatus.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String resid = aiModelResourceStatus.getResid();
        if (resid != null) {
            statement.bindString(2, resid);
        } else {
            statement.bindNull(2);
        }
        Long status = aiModelResourceStatus.getStatus();
        if (status != null) {
            statement.bindLong(3, status.longValue());
        } else {
            statement.bindNull(3);
        }
        Long version = aiModelResourceStatus.getVersion();
        if (version != null) {
            statement.bindLong(4, version.longValue());
        } else {
            statement.bindNull(4);
        }
        String url = aiModelResourceStatus.getUrl();
        if (url != null) {
            statement.bindString(5, url);
        } else {
            statement.bindNull(5);
        }
        String teams = aiModelResourceStatus.getTeams();
        if (teams != null) {
            statement.bindString(6, teams);
        } else {
            statement.bindNull(6);
        }
        String zipSha256 = aiModelResourceStatus.getZipSha256();
        if (zipSha256 != null) {
            statement.bindString(7, zipSha256);
        } else {
            statement.bindNull(7);
        }
        String decryptedKey = aiModelResourceStatus.getDecryptedKey();
        if (decryptedKey != null) {
            statement.bindString(8, decryptedKey);
        } else {
            statement.bindNull(8);
        }
        Boolean type = aiModelResourceStatus.getType();
        long j = 1;
        if (type != null) {
            statement.bindLong(9, type.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(9);
        }
        Boolean update = aiModelResourceStatus.getUpdate();
        if (update != null) {
            if (!update.booleanValue()) {
                j = 0;
            }
            statement.bindLong(10, j);
        } else {
            statement.bindNull(10);
        }
        String xpu = aiModelResourceStatus.getXpu();
        if (xpu != null) {
            statement.bindString(11, xpu);
        } else {
            statement.bindNull(11);
        }
        String emuiFamily = aiModelResourceStatus.getEmuiFamily();
        if (emuiFamily != null) {
            statement.bindString(12, emuiFamily);
        } else {
            statement.bindNull(12);
        }
        String productFamily = aiModelResourceStatus.getProductFamily();
        if (productFamily != null) {
            statement.bindString(13, productFamily);
        } else {
            statement.bindNull(13);
        }
        String chipsetVendor = aiModelResourceStatus.getChipsetVendor();
        if (chipsetVendor != null) {
            statement.bindString(14, chipsetVendor);
        } else {
            statement.bindNull(14);
        }
        String chipset = aiModelResourceStatus.getChipset();
        if (chipset != null) {
            statement.bindString(15, chipset);
        } else {
            statement.bindNull(15);
        }
        String product = aiModelResourceStatus.getProduct();
        if (product != null) {
            statement.bindString(16, product);
        } else {
            statement.bindNull(16);
        }
        String productModel = aiModelResourceStatus.getProductModel();
        if (productModel != null) {
            statement.bindString(17, productModel);
        } else {
            statement.bindNull(17);
        }
        String district = aiModelResourceStatus.getDistrict();
        if (district != null) {
            statement.bindString(18, district);
        } else {
            statement.bindNull(18);
        }
        String abTest = aiModelResourceStatus.getAbTest();
        if (abTest != null) {
            statement.bindString(19, abTest);
        } else {
            statement.bindNull(19);
        }
        String supprtAppVerson = aiModelResourceStatus.getSupprtAppVerson();
        if (supprtAppVerson != null) {
            statement.bindString(20, supprtAppVerson);
        } else {
            statement.bindNull(20);
        }
        String interfaceVersion = aiModelResourceStatus.getInterfaceVersion();
        if (interfaceVersion != null) {
            statement.bindString(21, interfaceVersion);
        } else {
            statement.bindNull(21);
        }
        String param1 = aiModelResourceStatus.getParam1();
        if (param1 != null) {
            statement.bindString(22, param1);
        } else {
            statement.bindNull(22);
        }
        String param2 = aiModelResourceStatus.getParam2();
        if (param2 != null) {
            statement.bindString(23, param2);
        } else {
            statement.bindNull(23);
        }
        String res_name = aiModelResourceStatus.getRes_name();
        if (res_name != null) {
            statement.bindString(24, res_name);
        } else {
            statement.bindNull(24);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public AiModelResourceStatus readObject(Cursor cursor, int i) {
        return new AiModelResourceStatus(cursor);
    }

    public void setPrimaryKeyValue(AiModelResourceStatus aiModelResourceStatus, long j) {
        aiModelResourceStatus.setId(Long.valueOf(j));
    }
}
