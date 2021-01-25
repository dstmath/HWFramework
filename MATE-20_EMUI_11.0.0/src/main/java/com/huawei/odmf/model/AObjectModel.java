package com.huawei.odmf.model;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.model.api.ObjectModel;
import java.io.File;
import java.util.Map;

public class AObjectModel implements ObjectModel {
    private String databaseName = null;
    private String databaseVersion = null;
    private int databaseVersionCode;
    private Map<String, AEntity> entities = null;
    private String modelName = null;

    public AObjectModel() {
    }

    public AObjectModel(String str, String str2, int i, String str3, Map<String, AEntity> map) {
        this.modelName = str;
        this.databaseVersion = str2;
        this.databaseName = str3;
        this.entities = map;
        this.databaseVersionCode = i;
    }

    @Override // com.huawei.odmf.model.api.ObjectModel
    public String getModelName() {
        return this.modelName;
    }

    public void setModelName(String str) {
        this.modelName = str;
    }

    @Override // com.huawei.odmf.model.api.ObjectModel
    public String getDatabaseVersion() {
        return this.databaseVersion;
    }

    public void setDatabaseVersion(String str) {
        this.databaseVersion = str;
    }

    @Override // com.huawei.odmf.model.api.ObjectModel
    public int getDatabaseVersionCode() {
        return this.databaseVersionCode;
    }

    public void setDatabaseVersionCode(int i) {
        this.databaseVersionCode = i;
    }

    @Override // com.huawei.odmf.model.api.ObjectModel
    public String getDatabaseName() {
        return this.databaseName;
    }

    public void setDatabaseName(String str) {
        this.databaseName = str;
    }

    @Override // com.huawei.odmf.model.api.ObjectModel
    public Map<String, AEntity> getEntities() {
        return this.entities;
    }

    public void setEntities(Map<String, AEntity> map) {
        this.entities = map;
    }

    public static ObjectModel parse(String str, String str2) {
        if (!TextUtils.isEmpty(str) && !TextUtils.isEmpty(str2)) {
            return XmlParser.parseToModel(str, str2);
        }
        throw new ODMFIllegalArgumentException("fileDir or fileName is null");
    }

    public static ObjectModel parse(File file) {
        if (file != null) {
            return XmlParser.parseToModel(file);
        }
        throw new ODMFIllegalArgumentException("file is null");
    }

    public static ObjectModel parse(Context context, String str) {
        if (!TextUtils.isEmpty(str) && context != null) {
            return XmlParser.parseToModel(context, str);
        }
        throw new ODMFIllegalArgumentException("parameter assetsFileName or context error");
    }

    @Override // com.huawei.odmf.model.api.ObjectModel
    public AEntity getEntity(String str) {
        if (!TextUtils.isEmpty(str)) {
            return this.entities.get(str);
        }
        throw new ODMFIllegalArgumentException("entityName is null");
    }

    @Override // com.huawei.odmf.model.api.ObjectModel
    public AEntity getEntity(Class cls) {
        if (cls != null) {
            return this.entities.get(cls.getName());
        }
        throw new ODMFIllegalArgumentException("class is null");
    }

    public String toString() {
        return "AObjectModel{modelName='" + this.modelName + "', databaseVersion=" + this.databaseVersion + ", databaseName='" + this.databaseName + "', entities=" + this.entities + '}';
    }
}
