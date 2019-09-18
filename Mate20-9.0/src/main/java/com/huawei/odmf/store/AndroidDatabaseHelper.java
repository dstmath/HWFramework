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

    /* JADX WARNING: Illegal instructions before constructor call */
    public AndroidDatabaseHelper(Context context, String databaseName, ObjectModel model2) {
        super(context, databaseName, null, r0);
        int i;
        if (model2 != null) {
            i = model2.getDatabaseVersionCode();
        } else {
            i = 1;
        }
        this.model = model2;
        setWriteAheadLoggingEnabled(true);
    }

    public void onCreate(SQLiteDatabase androidDb) {
        AndroidSQLiteDatabase androidSQLiteDatabase = new AndroidSQLiteDatabase(androidDb);
        DatabaseHelperTools.createMetadataTable(androidSQLiteDatabase);
        if (this.model != null) {
            for (Entity entity : this.model.getEntities().values()) {
                DatabaseHelperTools.createEntityTable(androidSQLiteDatabase, entity);
                DatabaseHelperTools.handleManyToManyRelationship(androidSQLiteDatabase, entity);
            }
        }
        DatabaseHelperTools.generateMetadata(androidSQLiteDatabase, this.model);
    }

    public void onUpgrade(SQLiteDatabase androidDb, int oldVersion, int newVersion) {
        LOG.logW("This database may need to be upgraded. Please pay attention.");
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LOG.logW("This database may need to be downgraded. Please pay attention.");
    }

    public void clearDatabase(DataBase androidDb) {
        DatabaseHelperTools.dropTables(androidDb);
        onCreate(androidDb.getAndroidSQLiteDatabase());
    }

    public void addTable(DataBase androidDb, Entity entity) {
        DatabaseHelperTools.addTable(androidDb, entity);
    }

    public void dropTable(DataBase androidDb, String tableName) {
        DatabaseHelperTools.dropTable(androidDb, tableName);
    }

    public void alterTableName(DataBase androidDb, String oldEntityName, String newEntityName) {
        DatabaseHelperTools.alterTableName(androidDb, oldEntityName, newEntityName);
        Entity entity = this.model.getEntity(oldEntityName);
        if (entity != null) {
            DatabaseHelperTools.rebuildMidTable(androidDb, entity, newEntityName);
            return;
        }
        Entity entity2 = this.model.getEntity(newEntityName);
        if (entity2 != null) {
            DatabaseHelperTools.rebuildMidTable(androidDb, oldEntityName, entity2);
        }
    }

    public void alterTableAddColumn(DataBase androidDb, String tableName, Attribute attribute) {
        DatabaseHelperTools.alterTableAddColumn(androidDb, tableName, attribute.getFieldName(), DatabaseTableHelper.getColumnType(attribute.getType()));
    }

    public void alterTableAddRelationship(DataBase androidDb, String tableName, Relationship relationship) {
        DatabaseHelperTools.alterTableAddRelationship(androidDb, tableName, relationship);
    }

    public void setDatabaseEncrypted(byte[] key) {
        throw new ODMFRuntimeException("cannot set a key for unencrypted database");
    }

    public String getDatabaseVersion(DataBase androidDb) {
        return DatabaseHelperTools.getDatabaseVersion(androidDb);
    }

    public void setDatabaseVersions(DataBase androidDb, String newVersion, int newVersionCode) {
        DatabaseHelperTools.setDatabaseVersions(androidDb, newVersion, newVersionCode);
    }

    public String getEntityVersion(DataBase androidDb, String tableName) {
        return DatabaseHelperTools.getEntityVersion(androidDb, tableName);
    }

    public void setEntityVersions(DataBase androidDb, String tableName, String newVersion, int newVersionCode) {
        DatabaseHelperTools.setEntityVersions(androidDb, tableName, newVersion, newVersionCode);
    }

    public int getDatabaseVersionCode(DataBase androidDb) {
        return DatabaseHelperTools.getDatabaseVersionCode(androidDb);
    }

    public int getEntityVersionCode(DataBase androidDb, String tableName) {
        return DatabaseHelperTools.getEntityVersionCode(androidDb, tableName);
    }

    public void resetMetadata(DataBase androidDb) {
        DatabaseHelperTools.resetMetadata(androidDb, this.model);
    }
}
