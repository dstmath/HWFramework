package com.huawei.odmf.model;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.model.api.ObjectModel;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class AObjectModel implements ObjectModel {
    private String databaseName = null;
    private String databaseVersion = null;
    private int databaseVersionCode;
    private Map<String, AEntity> entities = null;
    private String modelName = null;

    public AObjectModel() {
    }

    public AObjectModel(String modelName2, String databaseVersion2, int databaseVersionCode2, String databaseName2, Map<String, AEntity> entities2) {
        this.modelName = modelName2;
        this.databaseVersion = databaseVersion2;
        this.databaseName = databaseName2;
        this.entities = entities2;
        this.databaseVersionCode = databaseVersionCode2;
    }

    public String getModelName() {
        return this.modelName;
    }

    public void setModelName(String modelName2) {
        this.modelName = modelName2;
    }

    public String getDatabaseVersion() {
        return this.databaseVersion;
    }

    public void setDatabaseVersion(String databaseVersion2) {
        this.databaseVersion = databaseVersion2;
    }

    public int getDatabaseVersionCode() {
        return this.databaseVersionCode;
    }

    public void setDatabaseVersionCode(int databaseVersionCode2) {
        this.databaseVersionCode = databaseVersionCode2;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public void setDatabaseName(String databaseName2) {
        this.databaseName = databaseName2;
    }

    public Map<String, AEntity> getEntities() {
        return this.entities;
    }

    public void setEntities(Map<String, AEntity> entities2) {
        this.entities = entities2;
    }

    public static AObjectModel parse(String fileDir, String fileName) {
        if (!TextUtils.isEmpty(fileDir) && !TextUtils.isEmpty(fileName)) {
            return XmlParser.parseToModel(fileDir, fileName);
        }
        throw new ODMFIllegalArgumentException("fileDir or fileName is null");
    }

    public static AObjectModel parse(File file) throws IOException {
        if (file != null) {
            return XmlParser.parseToModel(file);
        }
        throw new ODMFIllegalArgumentException("file is null");
    }

    public static AObjectModel parse(Context context, String assetsFileName) {
        if (!TextUtils.isEmpty(assetsFileName) && context != null) {
            return XmlParser.parseToModel(context, assetsFileName);
        }
        throw new ODMFIllegalArgumentException("parameter assetsFileName or context error");
    }

    public AEntity getEntity(String entityName) {
        if (!TextUtils.isEmpty(entityName)) {
            return this.entities.get(entityName);
        }
        throw new ODMFIllegalArgumentException("entityName is null");
    }

    public AEntity getEntity(Class className) {
        if (className != null) {
            return this.entities.get(className.getName());
        }
        throw new ODMFIllegalArgumentException("class is null");
    }

    public String toString() {
        return "AObjectModel{modelName='" + this.modelName + '\'' + ", databaseVersion=" + this.databaseVersion + ", databaseName='" + this.databaseName + '\'' + ", entities=" + this.entities + '}';
    }
}
