package com.huawei.odmf.store;

import com.huawei.odmf.database.DataBase;
import com.huawei.odmf.model.api.Attribute;
import com.huawei.odmf.model.api.Entity;
import com.huawei.odmf.model.api.Relationship;

public interface DatabaseHelper {
    void addTable(DataBase dataBase, Entity entity);

    void alterTableAddColumn(DataBase dataBase, String str, Attribute attribute);

    void alterTableAddRelationship(DataBase dataBase, String str, Relationship relationship);

    void alterTableName(DataBase dataBase, String str, String str2);

    void clearDatabase(DataBase dataBase);

    void close();

    void dropTable(DataBase dataBase, String str);

    String getDatabaseVersion(DataBase dataBase);

    int getDatabaseVersionCode(DataBase dataBase);

    String getEntityVersion(DataBase dataBase, String str);

    int getEntityVersionCode(DataBase dataBase, String str);

    void resetMetadata(DataBase dataBase);

    void setDatabaseEncrypted(byte[] bArr);

    void setDatabaseVersions(DataBase dataBase, String str, int i);

    void setEntityVersions(DataBase dataBase, String str, String str2, int i);
}
