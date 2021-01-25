package com.huawei.odmf.store;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import com.huawei.nb.coordinator.helper.BusinessTypeEnum;
import com.huawei.odmf.core.DatabaseQueryService;
import com.huawei.odmf.database.DataBase;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFRuntimeException;
import com.huawei.odmf.exception.ODMFSQLiteDatabaseCorruptException;
import com.huawei.odmf.exception.ODMFSQLiteDiskIOException;
import com.huawei.odmf.model.AEntityId;
import com.huawei.odmf.model.api.Attribute;
import com.huawei.odmf.model.api.Entity;
import com.huawei.odmf.model.api.EntityId;
import com.huawei.odmf.model.api.Index;
import com.huawei.odmf.model.api.ObjectModel;
import com.huawei.odmf.model.api.Relationship;
import com.huawei.odmf.utils.JudgeUtils;
import com.huawei.odmf.utils.LOG;
import com.huawei.odmf.utils.SqlUtil;
import com.huawei.odmf.utils.ThreadLocalDateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/* access modifiers changed from: package-private */
public class DatabaseHelperTools {
    private static final String METADATA_COLUMN_NAME = "name";
    private static final String METADATA_COLUMN_VERSION = "version";
    private static final String METADATA_DATABASE_VERSION = "databaseVersion";
    private static final String METADATA_DATABASE_VERSION_CODE = "databaseVersionCode";
    private static final String METADATA_ENTITY_VERSION_CODE_SUFFIX = "_versionCode";
    private static final String METADATA_ENTITY_VERSION_SUFFIX = "_version";
    private static final String METADATA_TABLE_NAME = "odmf_metadata";

    private DatabaseHelperTools() {
    }

    public static void createMetadataTable(DataBase dataBase) {
        ODMFSQLiteTableBuilder oDMFSQLiteTableBuilder = new ODMFSQLiteTableBuilder();
        oDMFSQLiteTableBuilder.setSqliteTableName(METADATA_TABLE_NAME);
        oDMFSQLiteTableBuilder.setPrimaryKey(METADATA_COLUMN_NAME, "TEXT", false);
        oDMFSQLiteTableBuilder.addColumn(METADATA_COLUMN_VERSION, "TEXT");
        oDMFSQLiteTableBuilder.createTable(dataBase);
    }

    public static void generateMetadata(DataBase dataBase, ObjectModel objectModel) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(METADATA_COLUMN_NAME, METADATA_DATABASE_VERSION);
        contentValues.put(METADATA_COLUMN_VERSION, objectModel != null ? objectModel.getDatabaseVersion() : "");
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(METADATA_COLUMN_NAME, METADATA_DATABASE_VERSION_CODE);
        contentValues2.put(METADATA_COLUMN_VERSION, Integer.valueOf(objectModel != null ? objectModel.getDatabaseVersionCode() : 1));
        try {
            dataBase.insertOrThrow(METADATA_TABLE_NAME, null, contentValues);
            dataBase.insertOrThrow(METADATA_TABLE_NAME, null, contentValues2);
            if (objectModel != null) {
                for (String str : objectModel.getEntities().keySet()) {
                    ContentValues contentValues3 = new ContentValues();
                    contentValues3.put(METADATA_COLUMN_NAME, objectModel.getEntity(str).getTableName() + METADATA_ENTITY_VERSION_SUFFIX);
                    contentValues3.put(METADATA_COLUMN_VERSION, objectModel.getEntity(str).getEntityVersion());
                    ContentValues contentValues4 = new ContentValues();
                    contentValues4.put(METADATA_COLUMN_NAME, objectModel.getEntity(str).getTableName() + METADATA_ENTITY_VERSION_CODE_SUFFIX);
                    contentValues4.put(METADATA_COLUMN_VERSION, Integer.valueOf(objectModel.getEntity(str).getEntityVersionCode()));
                    try {
                        dataBase.insertOrThrow(METADATA_TABLE_NAME, null, contentValues3);
                        dataBase.insertOrThrow(METADATA_TABLE_NAME, null, contentValues4);
                    } catch (SQLException e) {
                        LOG.logE("Execute generateMetadata Failed : A SQLiteException occurred when generateMetadata");
                        throw new ODMFRuntimeException(e.toString());
                    }
                }
            }
        } catch (SQLException e2) {
            LOG.logE("Execute generateMetadata Failed : A SQLiteException occurred when generateMetadata");
            throw new ODMFRuntimeException(e2.toString());
        }
    }

    public static void createEntityTable(DataBase dataBase, Entity entity) {
        ODMFSQLiteTableBuilder oDMFSQLiteTableBuilder = new ODMFSQLiteTableBuilder();
        oDMFSQLiteTableBuilder.setSqliteTableName(entity.getTableName());
        ArrayList arrayList = new ArrayList();
        if (entity.getEntityIds().size() > 1) {
            for (EntityId entityId : entity.getEntityIds()) {
                arrayList.add(entityId.getIdName());
            }
            oDMFSQLiteTableBuilder.primaryKey(arrayList);
        } else {
            EntityId entityId2 = (EntityId) entity.getEntityIds().get(0);
            arrayList.add(entityId2.getIdName());
            String columnType = DatabaseTableHelper.getColumnType(entityId2.getType());
            if (entityId2.getGeneratorType().equals(AEntityId.INCREMENT)) {
                oDMFSQLiteTableBuilder.setPrimaryKey(entityId2.getIdName(), columnType, true);
            } else {
                oDMFSQLiteTableBuilder.setPrimaryKey(entityId2.getIdName(), columnType, false);
            }
        }
        addColumns(entity, oDMFSQLiteTableBuilder, arrayList);
        addRelationships(entity, oDMFSQLiteTableBuilder);
        oDMFSQLiteTableBuilder.createTable(dataBase);
        addIndex(dataBase, entity);
    }

    private static void addColumns(Entity entity, ODMFSQLiteTableBuilder oDMFSQLiteTableBuilder, List<String> list) {
        for (Attribute attribute : entity.getAttributes()) {
            int type = attribute.getType();
            if (list.size() != 1 || !list.contains(attribute.getColumnName())) {
                oDMFSQLiteTableBuilder.addColumn(attribute.getColumnName(), DatabaseTableHelper.getColumnType(type));
                if (attribute.isNotNull()) {
                    oDMFSQLiteTableBuilder.setNullable(false);
                }
                if (attribute.isUnique()) {
                    oDMFSQLiteTableBuilder.setUnique("ABORT");
                }
                if (attribute.getDefaultValue() != null) {
                    if (type == 3) {
                        oDMFSQLiteTableBuilder.setDefaultValue(attribute.getDefaultValue().equals("true") ? BusinessTypeEnum.BIZ_TYPE_FENCE : BusinessTypeEnum.BIZ_TYPE_POLICY);
                    } else if (type == 10) {
                        oDMFSQLiteTableBuilder.setDefaultValue(String.valueOf(ThreadLocalDateUtil.parseTime(attribute.getDefaultValue()).getTime()));
                    } else if (type == 9 || type == 12 || type == 13) {
                        oDMFSQLiteTableBuilder.setDefaultValue(String.valueOf(ThreadLocalDateUtil.parseDate(attribute.getDefaultValue()).getTime()));
                    } else {
                        oDMFSQLiteTableBuilder.setDefaultValue(attribute.getDefaultValue());
                    }
                }
            }
        }
    }

    private static void addRelationships(Entity entity, ODMFSQLiteTableBuilder oDMFSQLiteTableBuilder) {
        if (entity.getRelationships() != null) {
            for (Relationship relationship : entity.getRelationships()) {
                if (relationship.getRelationShipType() == 6 && relationship.isMajor()) {
                    oDMFSQLiteTableBuilder.addColumn(relationship.getFieldName(), "INTEGER");
                }
                if (relationship.getRelationShipType() == 2) {
                    oDMFSQLiteTableBuilder.addColumn(relationship.getFieldName(), "INTEGER");
                }
            }
        }
    }

    private static void addIndex(DataBase dataBase, Entity entity) {
        for (Attribute attribute : entity.getAttributes()) {
            if (attribute.hasIndex()) {
                try {
                    dataBase.execSQL("CREATE INDEX " + entity.getTableName() + "_" + attribute.getColumnName() + "_index ON " + entity.getTableName() + " (" + attribute.getColumnName() + ")");
                } catch (SQLException e) {
                    throw new ODMFRuntimeException(e.toString());
                }
            }
        }
        List<Index> indexes = entity.getIndexes();
        if (indexes != null) {
            for (Index index : indexes) {
                try {
                    dataBase.execSQL(SqlUtil.createSqlIndex(entity.getTableName(), index.getIndexName(), index.getCompositeIndexAttributes()));
                } catch (SQLiteException e2) {
                    throw new ODMFRuntimeException(e2.toString());
                }
            }
        }
    }

    public static void handleManyToManyRelationship(DataBase dataBase, Entity entity) {
        for (Relationship relationship : entity.getRelationships()) {
            if (relationship.getRelationShipType() == 0 && relationship.isMajor()) {
                createManyToManyRelationshipTable(dataBase, relationship);
            }
        }
    }

    public static void createManyToManyRelationshipTable(DataBase dataBase, Relationship relationship) {
        if (isTableExists(dataBase, DatabaseTableHelper.getManyToManyMidTableName(relationship))) {
            LOG.logI("Execute createManyToManyRelationshipTable : The mid-table of this many-to-many relationship already exists, return directly.");
            return;
        }
        ODMFSQLiteTableBuilder oDMFSQLiteTableBuilder = new ODMFSQLiteTableBuilder();
        Entity baseEntity = relationship.isMajor() ? relationship.getBaseEntity() : relationship.getRelatedEntity();
        Entity relatedEntity = relationship.isMajor() ? relationship.getRelatedEntity() : relationship.getBaseEntity();
        oDMFSQLiteTableBuilder.setSqliteTableName(DatabaseTableHelper.getManyToManyMidTableName(relationship));
        oDMFSQLiteTableBuilder.addColumn(DatabaseTableHelper.getRelationshipColumnName(baseEntity), "INTEGER");
        oDMFSQLiteTableBuilder.addColumn(DatabaseTableHelper.getRelationshipColumnName(relatedEntity), "INTEGER");
        oDMFSQLiteTableBuilder.createTable(dataBase);
    }

    private static boolean isTableExists(DataBase dataBase, String str) {
        Cursor cursor = null;
        try {
            Cursor rawQuery = dataBase.rawQuery("SELECT COUNT(name) FROM sqlite_master WHERE name = ?", new String[]{str});
            int i = rawQuery.moveToFirst() ? rawQuery.getInt(0) : 0;
            if (rawQuery != null) {
                rawQuery.close();
            }
            return i != 0;
        } catch (SQLException e) {
            LOG.logE("Execute iSTableExists Failed : A SQLException occurred when addTable");
            throw new ODMFRuntimeException("Execute iSTableExists Failed : " + e.toString());
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    public static void addTable(DataBase dataBase, Entity entity) {
        try {
            dataBase.beginTransaction();
            createEntityTable(dataBase, entity);
            handleManyToManyRelationship(dataBase, entity);
            ContentValues contentValues = new ContentValues();
            contentValues.put(METADATA_COLUMN_NAME, entity.getTableName() + METADATA_ENTITY_VERSION_SUFFIX);
            contentValues.put(METADATA_COLUMN_VERSION, entity.getEntityVersion());
            dataBase.insertOrThrow(METADATA_TABLE_NAME, null, contentValues);
            ContentValues contentValues2 = new ContentValues();
            contentValues2.put(METADATA_COLUMN_NAME, entity.getTableName() + METADATA_ENTITY_VERSION_CODE_SUFFIX);
            contentValues2.put(METADATA_COLUMN_VERSION, Integer.valueOf(entity.getEntityVersionCode()));
            dataBase.insertOrThrow(METADATA_TABLE_NAME, null, contentValues2);
            dataBase.setTransactionSuccessful();
            dataBase.endTransaction();
        } catch (SQLException e) {
            LOG.logE("Execute addTable Failed : A SQLiteException occurred when addTable");
            throw new ODMFRuntimeException(e.toString());
        } catch (Throwable th) {
            dataBase.endTransaction();
            throw th;
        }
    }

    public static void dropTable(DataBase dataBase, String str) {
        try {
            dataBase.beginTransaction();
            dataBase.execSQL(String.format(Locale.ENGLISH, "DROP TABLE IF EXISTS %s;", str));
            dataBase.delete(METADATA_TABLE_NAME, "name = ?", new String[]{str + METADATA_ENTITY_VERSION_SUFFIX});
            dataBase.delete(METADATA_TABLE_NAME, "name = ?", new String[]{str + METADATA_ENTITY_VERSION_CODE_SUFFIX});
            dataBase.setTransactionSuccessful();
            dataBase.endTransaction();
        } catch (SQLException e) {
            LOG.logE("Execute dropTable Failed : A SQLiteException occurred when dropTable");
            throw new ODMFRuntimeException(e.toString());
        } catch (Throwable th) {
            dataBase.endTransaction();
            throw th;
        }
    }

    public static void alterTableName(DataBase dataBase, String str, String str2) {
        String[] split = str.split("\\.");
        String[] split2 = str2.split("\\.");
        if (split.length <= 0 || split2.length <= 0) {
            throw new ODMFIllegalArgumentException("The entityName or the tableName format incorrect.");
        }
        String str3 = split[split.length - 1];
        String str4 = split2[split2.length - 1];
        try {
            dataBase.beginTransaction();
            ODMFSQLiteTableBuilder.alterTableName(dataBase, str3, str4);
            ContentValues contentValues = new ContentValues();
            contentValues.put(METADATA_COLUMN_NAME, str4 + METADATA_ENTITY_VERSION_SUFFIX);
            dataBase.update(METADATA_TABLE_NAME, contentValues, "name = ?", new String[]{str3 + METADATA_ENTITY_VERSION_SUFFIX});
            ContentValues contentValues2 = new ContentValues();
            contentValues2.put(METADATA_COLUMN_NAME, str4 + METADATA_ENTITY_VERSION_CODE_SUFFIX);
            dataBase.update(METADATA_TABLE_NAME, contentValues2, "name = ?", new String[]{str3 + METADATA_ENTITY_VERSION_CODE_SUFFIX});
            dataBase.setTransactionSuccessful();
            dataBase.endTransaction();
        } catch (SQLException e) {
            LOG.logE("Execute alterTableName Failed : A SQLException occurred when addTable.");
            throw new ODMFRuntimeException("Execute alterTableName Failed : " + e.toString());
        } catch (Throwable th) {
            dataBase.endTransaction();
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x010b  */
    static void rebuildMidTable(DataBase dataBase, String str, Entity entity) {
        Entity entity2;
        Entity entity3;
        String str2;
        Throwable th;
        SQLException e;
        String[] split = str.split("\\.");
        String str3 = split[split.length - 1];
        for (Relationship relationship : entity.getRelationships()) {
            if (relationship.getRelationShipType() == 0) {
                if (relationship.isMajor()) {
                    str2 = str3 + "_" + relationship.getRelatedEntity().getTableName() + "_" + relationship.getFieldName();
                    entity3 = relationship.getBaseEntity();
                    entity2 = relationship.getRelatedEntity();
                } else {
                    str2 = relationship.getRelatedEntity().getTableName() + "_" + str3 + "_" + relationship.getInverseRelationship().getFieldName();
                    entity3 = relationship.getRelatedEntity();
                    entity2 = relationship.getBaseEntity();
                }
                Cursor cursor = null;
                try {
                    dataBase.beginTransaction();
                    createManyToManyRelationshipTable(dataBase, relationship);
                    Cursor rawQuery = dataBase.rawQuery("SELECT * FROM " + str2, null);
                    while (rawQuery.moveToNext()) {
                        try {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DatabaseTableHelper.getRelationshipColumnName(entity3), rawQuery.getString(0));
                            contentValues.put(DatabaseTableHelper.getRelationshipColumnName(entity2), rawQuery.getString(1));
                            dataBase.insertOrThrow(DatabaseTableHelper.getManyToManyMidTableName(relationship), null, contentValues);
                        } catch (SQLException e2) {
                            e = e2;
                            cursor = rawQuery;
                            try {
                                LOG.logE("Execute rebuildMidTable Failed : A SQLException occurred when rebuild mid table, it may happened when you want to rename the related table.");
                                throw new ODMFRuntimeException("Execute rebuildMidTable Failed : " + e.toString());
                            } catch (Throwable th2) {
                                th = th2;
                                if (cursor != null) {
                                }
                                dataBase.endTransaction();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            cursor = rawQuery;
                            if (cursor != null) {
                                cursor.close();
                            }
                            dataBase.endTransaction();
                            throw th;
                        }
                    }
                    dropTable(dataBase, str2);
                    dataBase.setTransactionSuccessful();
                    if (rawQuery != null) {
                        rawQuery.close();
                    }
                    dataBase.endTransaction();
                } catch (SQLException e3) {
                    e = e3;
                    LOG.logE("Execute rebuildMidTable Failed : A SQLException occurred when rebuild mid table, it may happened when you want to rename the related table.");
                    throw new ODMFRuntimeException("Execute rebuildMidTable Failed : " + e.toString());
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x00bd  */
    static void rebuildMidTable(DataBase dataBase, Entity entity, String str) {
        Throwable th;
        SQLException e;
        String[] split = str.split("\\.");
        String str2 = split[split.length - 1];
        for (Relationship relationship : entity.getRelationships()) {
            if (relationship.getRelationShipType() == 0) {
                String midTableName = getMidTableName(str2, relationship);
                String baseColumnName = getBaseColumnName(str2, relationship);
                String relatedColumnName = getRelatedColumnName(str2, relationship);
                Cursor cursor = null;
                try {
                    dataBase.beginTransaction();
                    ODMFSQLiteTableBuilder oDMFSQLiteTableBuilder = new ODMFSQLiteTableBuilder();
                    oDMFSQLiteTableBuilder.setSqliteTableName(midTableName);
                    oDMFSQLiteTableBuilder.addColumn(baseColumnName, "INTEGER");
                    oDMFSQLiteTableBuilder.addColumn(relatedColumnName, "INTEGER");
                    oDMFSQLiteTableBuilder.createTable(dataBase);
                    Cursor rawQuery = dataBase.rawQuery("SELECT * FROM " + DatabaseTableHelper.getManyToManyMidTableName(relationship), null);
                    while (rawQuery.moveToNext()) {
                        try {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(baseColumnName, rawQuery.getString(0));
                            contentValues.put(relatedColumnName, rawQuery.getString(1));
                            dataBase.insertOrThrow(midTableName, null, contentValues);
                        } catch (SQLException e2) {
                            e = e2;
                            cursor = rawQuery;
                            try {
                                LOG.logE("Execute rebuildMidTable Failed : A SQLException occurred when rebuild mid table, it may happened when you want to rename the related table.");
                                throw new ODMFRuntimeException("Execute rebuildMidTable Failed : " + e.toString());
                            } catch (Throwable th2) {
                                th = th2;
                                if (cursor != null) {
                                }
                                dataBase.endTransaction();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            cursor = rawQuery;
                            if (cursor != null) {
                                cursor.close();
                            }
                            dataBase.endTransaction();
                            throw th;
                        }
                    }
                    dropTable(dataBase, DatabaseTableHelper.getManyToManyMidTableName(relationship));
                    dataBase.setTransactionSuccessful();
                    if (rawQuery != null) {
                        rawQuery.close();
                    }
                    dataBase.endTransaction();
                } catch (SQLException e3) {
                    e = e3;
                    LOG.logE("Execute rebuildMidTable Failed : A SQLException occurred when rebuild mid table, it may happened when you want to rename the related table.");
                    throw new ODMFRuntimeException("Execute rebuildMidTable Failed : " + e.toString());
                }
            }
        }
    }

    private static String getMidTableName(String str, Relationship relationship) {
        if (relationship.isMajor()) {
            return str + "_" + relationship.getRelatedEntity().getTableName() + "_" + relationship.getFieldName();
        }
        return relationship.getRelatedEntity().getTableName() + "_" + str + "_" + relationship.getInverseRelationship().getFieldName();
    }

    private static String getBaseColumnName(String str, Relationship relationship) {
        if (!relationship.isMajor()) {
            return DatabaseTableHelper.getRelationshipColumnName(relationship.getRelatedEntity());
        }
        return "ODMF_" + str + "_" + DatabaseQueryService.getRowidColumnName();
    }

    private static String getRelatedColumnName(String str, Relationship relationship) {
        if (relationship.isMajor()) {
            return DatabaseTableHelper.getRelationshipColumnName(relationship.getRelatedEntity());
        }
        return "ODMF_" + str + "_" + DatabaseQueryService.getRowidColumnName();
    }

    public static void alterTableAddRelationship(DataBase dataBase, String str, Relationship relationship) {
        if (relationship.getRelationShipType() == 6) {
            if (relationship.isMajor()) {
                alterTableAddColumn(dataBase, str, relationship.getFieldName(), "INTEGER");
            } else {
                alterTableAddColumn(dataBase, relationship.getRelatedEntity().getTableName(), relationship.getInverseRelationship().getFieldName(), "INTEGER");
            }
        } else if (relationship.getRelationShipType() == 4) {
            alterTableAddColumn(dataBase, relationship.getRelatedEntity().getTableName(), relationship.getInverseRelationship().getFieldName(), "INTEGER");
        } else if (relationship.getRelationShipType() == 2) {
            alterTableAddColumn(dataBase, relationship.getBaseEntity().getTableName(), relationship.getFieldName(), "INTEGER");
        } else if (relationship.getRelationShipType() == 0) {
            createManyToManyRelationshipTable(dataBase, relationship);
        } else {
            throw new IllegalArgumentException("The relationship type is unsupported.");
        }
    }

    public static void alterTableAddColumn(DataBase dataBase, String str, String str2, String str3) {
        ODMFSQLiteTableBuilder oDMFSQLiteTableBuilder = new ODMFSQLiteTableBuilder();
        oDMFSQLiteTableBuilder.setSqliteTableName(str);
        oDMFSQLiteTableBuilder.addColumn(str2, str3);
        oDMFSQLiteTableBuilder.alterTableAddColumn(dataBase);
    }

    public static void dropTables(DataBase dataBase) {
        Cursor cursor = null;
        try {
            Cursor rawQuery = dataBase.rawQuery("select 'drop table if exists ' || name || ';' from sqlite_master where type='table' and name not like 'android%' and name not like 'sqlite%';and name not like 'odmf_metadata';", null);
            while (rawQuery.moveToNext()) {
                dataBase.execSQL(rawQuery.getString(0));
            }
            if (rawQuery != null) {
                rawQuery.close();
            }
        } catch (SQLException e) {
            LOG.logE("Execute dropTables Failed : A SQLiteException occurred when dropTables");
            throw new ODMFRuntimeException(e.toString());
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x00a9  */
    public static String getDatabaseVersion(DataBase dataBase) {
        Throwable th;
        SQLiteDatabaseCorruptException e;
        SQLiteDiskIOException e2;
        SQLException e3;
        Cursor cursor = null;
        String str = null;
        cursor = null;
        try {
            Cursor query = dataBase.query(METADATA_TABLE_NAME, null, "name = ?", new String[]{METADATA_DATABASE_VERSION}, null, null, null, null);
            try {
                if (query.moveToFirst()) {
                    str = query.getString(query.getColumnIndex(METADATA_COLUMN_VERSION));
                }
                if (query != null) {
                    query.close();
                }
                return str;
            } catch (SQLiteDatabaseCorruptException | com.huawei.hwsqlite.SQLiteDatabaseCorruptException e4) {
                e = e4;
                LOG.logE("Get database version failed : A sqliteDatabase corrupt exception occurred.");
                throw new ODMFSQLiteDatabaseCorruptException("Get database version failed : " + e.getMessage(), e);
            } catch (SQLiteDiskIOException | com.huawei.hwsqlite.SQLiteDiskIOException e5) {
                e2 = e5;
                LOG.logE("Get database version failed : A disk io exception occurred.");
                throw new ODMFSQLiteDiskIOException("Get database version failed : " + e2.getMessage(), e2);
            } catch (SQLException e6) {
                cursor = query;
                e3 = e6;
                LOG.logE("Get database version failed : A SQLException occurred when getDatabaseVersion");
                throw new ODMFRuntimeException("Get database version failed : " + e3.getMessage(), e3);
            } catch (Throwable th2) {
                th = th2;
                cursor = query;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        } catch (SQLiteDatabaseCorruptException e7) {
            e = e7;
            LOG.logE("Get database version failed : A sqliteDatabase corrupt exception occurred.");
            throw new ODMFSQLiteDatabaseCorruptException("Get database version failed : " + e.getMessage(), e);
        } catch (com.huawei.hwsqlite.SQLiteDatabaseCorruptException e8) {
            e = e8;
            LOG.logE("Get database version failed : A sqliteDatabase corrupt exception occurred.");
            throw new ODMFSQLiteDatabaseCorruptException("Get database version failed : " + e.getMessage(), e);
        } catch (SQLiteDiskIOException e9) {
            e2 = e9;
            LOG.logE("Get database version failed : A disk io exception occurred.");
            throw new ODMFSQLiteDiskIOException("Get database version failed : " + e2.getMessage(), e2);
        } catch (com.huawei.hwsqlite.SQLiteDiskIOException e10) {
            e2 = e10;
            LOG.logE("Get database version failed : A disk io exception occurred.");
            throw new ODMFSQLiteDiskIOException("Get database version failed : " + e2.getMessage(), e2);
        } catch (SQLException e11) {
            e3 = e11;
            LOG.logE("Get database version failed : A SQLException occurred when getDatabaseVersion");
            throw new ODMFRuntimeException("Get database version failed : " + e3.getMessage(), e3);
        } catch (Throwable th3) {
            th = th3;
            if (cursor != null) {
            }
            throw th;
        }
    }

    public static void setDatabaseVersions(DataBase dataBase, String str, int i) {
        if (JudgeUtils.checkVersion(str)) {
            ContentValues contentValues = new ContentValues();
            ContentValues contentValues2 = new ContentValues();
            contentValues.put(METADATA_COLUMN_VERSION, str);
            contentValues2.put(METADATA_COLUMN_VERSION, Integer.valueOf(i));
            try {
                dataBase.beginTransaction();
                dataBase.update(METADATA_TABLE_NAME, contentValues, "name = ?", new String[]{METADATA_DATABASE_VERSION});
                dataBase.update(METADATA_TABLE_NAME, contentValues2, "name = ?", new String[]{METADATA_DATABASE_VERSION_CODE});
                dataBase.setTransactionSuccessful();
                dataBase.endTransaction();
            } catch (SQLException e) {
                LOG.logE("Execute setDatabaseVersions Failed : A SQLiteException occurred when setDatabaseVersions");
                throw new ODMFRuntimeException(e.toString());
            } catch (Throwable th) {
                dataBase.endTransaction();
                throw th;
            }
        } else {
            LOG.logE("Execute setDatabaseVersion Failed : The newVersion is invalid.");
            throw new ODMFIllegalArgumentException("The newVersion is invalid.");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0045, code lost:
        if (r10 != null) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0048, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0037, code lost:
        if (r10 != null) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0039, code lost:
        r10.close();
     */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x004d  */
    public static String getEntityVersion(DataBase dataBase, String str) {
        Throwable th;
        Cursor cursor;
        String str2 = null;
        str2 = null;
        Cursor cursor2 = null;
        str2 = null;
        try {
            cursor = dataBase.query(METADATA_TABLE_NAME, null, "name = ?", new String[]{str + METADATA_ENTITY_VERSION_SUFFIX}, null, null, null, null);
            try {
                if (cursor.moveToFirst()) {
                    str2 = cursor.getString(cursor.getColumnIndex(METADATA_COLUMN_VERSION));
                }
            } catch (SQLException unused) {
                try {
                    LOG.logE("Execute getEntityVersion Failed : A SQLiteException occurred when getEntityVersion");
                } catch (Throwable th2) {
                    th = th2;
                    cursor2 = cursor;
                    if (cursor2 != null) {
                    }
                    throw th;
                }
            }
        } catch (SQLException unused2) {
            cursor = null;
            LOG.logE("Execute getEntityVersion Failed : A SQLiteException occurred when getEntityVersion");
        } catch (Throwable th3) {
            th = th3;
            if (cursor2 != null) {
                cursor2.close();
            }
            throw th;
        }
    }

    public static void setEntityVersions(DataBase dataBase, String str, String str2, int i) {
        if (JudgeUtils.checkVersion(str2)) {
            ContentValues contentValues = new ContentValues();
            ContentValues contentValues2 = new ContentValues();
            contentValues.put(METADATA_COLUMN_VERSION, str2);
            contentValues2.put(METADATA_COLUMN_VERSION, Integer.valueOf(i));
            try {
                dataBase.beginTransaction();
                dataBase.update(METADATA_TABLE_NAME, contentValues, "name = ?", new String[]{str + METADATA_ENTITY_VERSION_SUFFIX});
                dataBase.update(METADATA_TABLE_NAME, contentValues2, "name = ?", new String[]{str + METADATA_ENTITY_VERSION_CODE_SUFFIX});
                dataBase.setTransactionSuccessful();
                dataBase.endTransaction();
            } catch (SQLException e) {
                LOG.logE("Execute setEntityVersions Failed : A SQLiteException occurred when setEntityVersions");
                throw new ODMFRuntimeException(e.toString());
            } catch (Throwable th) {
                dataBase.endTransaction();
                throw th;
            }
        } else {
            LOG.logE("Execute setEntityVersion Failed : The newVersion is invalid.");
            throw new ODMFIllegalArgumentException("The newVersion is invalid.");
        }
    }

    public static int getDatabaseVersionCode(DataBase dataBase) {
        com.huawei.hwsqlite.SQLiteDatabaseCorruptException e;
        com.huawei.hwsqlite.SQLiteDiskIOException e2;
        Cursor cursor = null;
        try {
            Cursor query = dataBase.query(METADATA_TABLE_NAME, null, "name = ?", new String[]{METADATA_DATABASE_VERSION_CODE}, null, null, null, null);
            int i = query.moveToFirst() ? query.getInt(query.getColumnIndex(METADATA_COLUMN_VERSION)) : 0;
            if (query != null) {
                query.close();
            }
            return i;
        } catch (SQLiteDatabaseCorruptException e3) {
            e = e3;
            LOG.logE("Get database version code failed : A sqliteDatabase corrupt exception occurred.");
            throw new ODMFSQLiteDatabaseCorruptException("Get database version code failed : " + e.getMessage(), e);
        } catch (com.huawei.hwsqlite.SQLiteDatabaseCorruptException e4) {
            e = e4;
            LOG.logE("Get database version code failed : A sqliteDatabase corrupt exception occurred.");
            throw new ODMFSQLiteDatabaseCorruptException("Get database version code failed : " + e.getMessage(), e);
        } catch (SQLiteDiskIOException e5) {
            e2 = e5;
            LOG.logE("Get database version code failed : A disk io exception occurred.");
            throw new ODMFSQLiteDiskIOException("Get database version code failed : " + e2.getMessage(), e2);
        } catch (com.huawei.hwsqlite.SQLiteDiskIOException e6) {
            e2 = e6;
            LOG.logE("Get database version code failed : A disk io exception occurred.");
            throw new ODMFSQLiteDiskIOException("Get database version code failed : " + e2.getMessage(), e2);
        } catch (SQLException e7) {
            LOG.logE("Get database version code failed : A SQLiteException occurred when getDatabaseVersion");
            throw new ODMFRuntimeException("Get database version code failed : " + e7.getMessage(), e7);
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0044, code lost:
        if (0 == 0) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0047, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0037, code lost:
        if (r1 != null) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0039, code lost:
        r1.close();
     */
    public static int getEntityVersionCode(DataBase dataBase, String str) {
        int i = 0;
        Cursor cursor = null;
        try {
            cursor = dataBase.query(METADATA_TABLE_NAME, null, "name = ?", new String[]{str + METADATA_ENTITY_VERSION_CODE_SUFFIX}, null, null, null, null);
            if (cursor.moveToFirst()) {
                i = cursor.getInt(cursor.getColumnIndex(METADATA_COLUMN_VERSION));
            }
        } catch (SQLException unused) {
            LOG.logE("Execute getEntityVersionCode Failed : A SQLiteException occurred when getEntityVersionCode");
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    public static void resetMetadata(DataBase dataBase, ObjectModel objectModel) {
        com.huawei.hwsqlite.SQLiteDatabaseCorruptException e;
        com.huawei.hwsqlite.SQLiteDiskIOException e2;
        if (objectModel != null) {
            try {
                dataBase.beginTransaction();
                dataBase.delete(METADATA_TABLE_NAME, null, null);
                generateMetadata(dataBase, objectModel);
                dataBase.setTransactionSuccessful();
                dataBase.endTransaction();
            } catch (SQLiteDatabaseCorruptException e3) {
                e = e3;
                LOG.logE("Reset metadata failed : A sqliteDatabase corrupt exception occurred.");
                throw new ODMFSQLiteDatabaseCorruptException("Reset metadata failed : " + e.getMessage(), e);
            } catch (com.huawei.hwsqlite.SQLiteDatabaseCorruptException e4) {
                e = e4;
                LOG.logE("Reset metadata failed : A sqliteDatabase corrupt exception occurred.");
                throw new ODMFSQLiteDatabaseCorruptException("Reset metadata failed : " + e.getMessage(), e);
            } catch (SQLiteDiskIOException e5) {
                e2 = e5;
                LOG.logE("Reset metadata failed : A disk io exception occurred.");
                throw new ODMFSQLiteDiskIOException("Reset metadata failed : " + e2.getMessage(), e2);
            } catch (com.huawei.hwsqlite.SQLiteDiskIOException e6) {
                e2 = e6;
                LOG.logE("Reset metadata failed : A disk io exception occurred.");
                throw new ODMFSQLiteDiskIOException("Reset metadata failed : " + e2.getMessage(), e2);
            } catch (SQLException e7) {
                LOG.logE("Reset metadata failed : A SQLiteException occurred when reset mate data.");
                throw new ODMFRuntimeException("Reset metadata failed : " + e7.getMessage(), e7);
            } catch (Throwable th) {
                dataBase.endTransaction();
                throw th;
            }
        } else {
            LOG.logW("The metadata con not be reset : the model of this database open helper is null.");
        }
    }
}
