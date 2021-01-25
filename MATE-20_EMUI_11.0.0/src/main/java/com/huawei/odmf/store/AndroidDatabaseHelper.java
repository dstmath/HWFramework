package com.huawei.odmf.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.huawei.odmf.database.AndroidSQLiteDatabase;
import com.huawei.odmf.database.DataBase;
import com.huawei.odmf.exception.ODMFRuntimeException;
import com.huawei.odmf.model.api.Attribute;
import com.huawei.odmf.model.api.Entity;
import com.huawei.odmf.model.api.ObjectModel;
import com.huawei.odmf.model.api.Relationship;
import com.huawei.odmf.utils.LOG;

public class AndroidDatabaseHelper extends SQLiteOpenHelper implements DatabaseHelper {
    private final ObjectModel model;

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public AndroidDatabaseHelper(Context context, String str, ObjectModel objectModel) {
        super(context, str, (SQLiteDatabase.CursorFactory) null, objectModel != null ? objectModel.getDatabaseVersionCode() : 1);
        this.model = objectModel;
        setWriteAheadLoggingEnabled(true);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        AndroidSQLiteDatabase androidSQLiteDatabase = new AndroidSQLiteDatabase(sQLiteDatabase);
        DatabaseHelperTools.createMetadataTable(androidSQLiteDatabase);
        ObjectModel objectModel = this.model;
        if (objectModel != null) {
            for (Entity entity : objectModel.getEntities().values()) {
                DatabaseHelperTools.createEntityTable(androidSQLiteDatabase, entity);
                DatabaseHelperTools.handleManyToManyRelationship(androidSQLiteDatabase, entity);
            }
        }
        DatabaseHelperTools.generateMetadata(androidSQLiteDatabase, this.model);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        LOG.logW("This database may need to be downgraded. Please pay attention.");
    }

    @Override // com.huawei.odmf.store.DatabaseHelper
    public void clearDatabase(DataBase dataBase) {
        DatabaseHelperTools.dropTables(dataBase);
        onCreate(dataBase.getAndroidSQLiteDatabase());
    }

    @Override // com.huawei.odmf.store.DatabaseHelper
    public void addTable(DataBase dataBase, Entity entity) {
        DatabaseHelperTools.addTable(dataBase, entity);
    }

    @Override // com.huawei.odmf.store.DatabaseHelper
    public void dropTable(DataBase dataBase, String str) {
        DatabaseHelperTools.dropTable(dataBase, str);
    }

    @Override // com.huawei.odmf.store.DatabaseHelper
    public void alterTableName(DataBase dataBase, String str, String str2) {
        DatabaseHelperTools.alterTableName(dataBase, str, str2);
        Entity entity = this.model.getEntity(str);
        if (entity != null) {
            DatabaseHelperTools.rebuildMidTable(dataBase, entity, str2);
            return;
        }
        Entity entity2 = this.model.getEntity(str2);
        if (entity2 != null) {
            DatabaseHelperTools.rebuildMidTable(dataBase, str, entity2);
        }
    }

    @Override // com.huawei.odmf.store.DatabaseHelper
    public void alterTableAddColumn(DataBase dataBase, String str, Attribute attribute) {
        DatabaseHelperTools.alterTableAddColumn(dataBase, str, attribute.getFieldName(), DatabaseTableHelper.getColumnType(attribute.getType()));
    }

    @Override // com.huawei.odmf.store.DatabaseHelper
    public void alterTableAddRelationship(DataBase dataBase, String str, Relationship relationship) {
        DatabaseHelperTools.alterTableAddRelationship(dataBase, str, relationship);
    }

    @Override // com.huawei.odmf.store.DatabaseHelper
    public void setDatabaseEncrypted(byte[] bArr) {
        throw new ODMFRuntimeException("cannot set a key for unencrypted database");
    }

    @Override // com.huawei.odmf.store.DatabaseHelper
    public String getDatabaseVersion(DataBase dataBase) {
        return DatabaseHelperTools.getDatabaseVersion(dataBase);
    }

    @Override // com.huawei.odmf.store.DatabaseHelper
    public void setDatabaseVersions(DataBase dataBase, String str, int i) {
        DatabaseHelperTools.setDatabaseVersions(dataBase, str, i);
    }

    @Override // com.huawei.odmf.store.DatabaseHelper
    public String getEntityVersion(DataBase dataBase, String str) {
        return DatabaseHelperTools.getEntityVersion(dataBase, str);
    }

    @Override // com.huawei.odmf.store.DatabaseHelper
    public void setEntityVersions(DataBase dataBase, String str, String str2, int i) {
        DatabaseHelperTools.setEntityVersions(dataBase, str, str2, i);
    }

    @Override // com.huawei.odmf.store.DatabaseHelper
    public int getDatabaseVersionCode(DataBase dataBase) {
        return DatabaseHelperTools.getDatabaseVersionCode(dataBase);
    }

    @Override // com.huawei.odmf.store.DatabaseHelper
    public int getEntityVersionCode(DataBase dataBase, String str) {
        return DatabaseHelperTools.getEntityVersionCode(dataBase, str);
    }

    @Override // com.huawei.odmf.store.DatabaseHelper
    public void resetMetadata(DataBase dataBase) {
        DatabaseHelperTools.resetMetadata(dataBase, this.model);
    }
}
