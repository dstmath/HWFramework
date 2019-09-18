package com.huawei.odmf.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.CrossProcessCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.SQLException;
import android.database.StaleDataException;
import android.database.sqlite.SQLiteAccessPermException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import com.huawei.odmf.database.AndroidSQLiteDatabase;
import com.huawei.odmf.database.DataBase;
import com.huawei.odmf.database.ODMFSQLiteDatabase;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFIllegalStateException;
import com.huawei.odmf.exception.ODMFRelatedObjectNotFoundException;
import com.huawei.odmf.exception.ODMFRuntimeException;
import com.huawei.odmf.exception.ODMFSQLiteAccessPermException;
import com.huawei.odmf.exception.ODMFSQLiteCantOpenDatabaseException;
import com.huawei.odmf.exception.ODMFSQLiteDatabaseCorruptException;
import com.huawei.odmf.exception.ODMFSQLiteDiskIOException;
import com.huawei.odmf.exception.ODMFSQLiteFullException;
import com.huawei.odmf.exception.ODMFXmlParserException;
import com.huawei.odmf.model.AEntityHelper;
import com.huawei.odmf.model.ARelationship;
import com.huawei.odmf.model.api.Attribute;
import com.huawei.odmf.model.api.Entity;
import com.huawei.odmf.model.api.ObjectModel;
import com.huawei.odmf.model.api.ObjectModelFactory;
import com.huawei.odmf.model.api.Relationship;
import com.huawei.odmf.predicate.FetchRequest;
import com.huawei.odmf.predicate.SaveRequest;
import com.huawei.odmf.store.AndroidDatabaseHelper;
import com.huawei.odmf.store.DatabaseHelper;
import com.huawei.odmf.store.DatabaseTableHelper;
import com.huawei.odmf.store.ODMFDatabaseHelper;
import com.huawei.odmf.user.api.ObjectContext;
import com.huawei.odmf.utils.CursorUtils;
import com.huawei.odmf.utils.LOG;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class AndroidSqlPersistentStore extends PersistentStore {
    private Context context;
    protected DatabaseHelper databaseHelper;
    protected DataBase db;
    private Map<String, AEntityHelper> helperMap;
    private RelationshipLoader relationshipLoader;
    private final Object statementLock;
    private String uriString;

    AndroidSqlPersistentStore(Context context2, String modelPath, String uriString2, Configuration configuration) {
        this(context2, modelPath, uriString2, configuration, null);
    }

    AndroidSqlPersistentStore(Context context2, String modelFile, String uriString2, Configuration configuration, byte[] key) {
        super(configuration.getPath(), configuration.getDatabaseType(), configuration.getStorageMode(), uriString2);
        this.statementLock = new Object();
        try {
            this.model = ObjectModelFactory.parse(context2, modelFile);
            this.context = context2;
            String databaseName = configuration.getPath();
            if (databaseName == null || databaseName.equals("")) {
                databaseName = this.model.getDatabaseName();
                this.path = databaseName;
            }
            init(configuration.getStorageMode() == 401 ? null : databaseName, key, configuration.isThrowException(), configuration.isDetectDelete());
            this.uriString = uriString2;
        } catch (ODMFIllegalArgumentException | ODMFXmlParserException e) {
            LOG.logE("Create AndroidSqlPersistentStore failed : parser objectModel failed.");
            throw new ODMFRuntimeException("Xml parser failed : " + e.getMessage());
        }
    }

    private void init(String databasePath, byte[] key, boolean throwException, boolean detectDelete) {
        SQLException e;
        SQLException e2;
        SQLException e3;
        SQLException e4;
        RuntimeException e5;
        RuntimeException e6;
        try {
            if (getDatabaseType() == 302) {
                this.databaseHelper = new AndroidDatabaseHelper(this.context, databasePath, getModel());
                this.db = new AndroidSQLiteDatabase(((AndroidDatabaseHelper) this.databaseHelper).getWritableDatabase());
            } else if (getDatabaseType() == 301) {
                this.databaseHelper = new ODMFDatabaseHelper(this.context, databasePath, getModel(), throwException, detectDelete);
                if (key != null && key.length > 0) {
                    this.databaseHelper.setDatabaseEncrypted(key);
                }
                this.db = new ODMFSQLiteDatabase(((ODMFDatabaseHelper) this.databaseHelper).getWritableDatabase());
            } else {
                LOG.logE("Init database failed : incorrect configuration of database.");
                throw new ODMFRuntimeException("The configuration of database is wrong.");
            }
            loadMetadata();
            this.helperMap = new ConcurrentHashMap();
            initHelper(getModel());
            this.relationshipLoader = new RelationshipLoader(this.db, getModel(), this.helperMap);
            if (key != null && key.length > 0) {
                for (int i = 0; i < key.length; i++) {
                    key[i] = 0;
                }
            }
        } catch (SQLiteDatabaseCorruptException e7) {
            e6 = e7;
            LOG.logE("Init database failed : A sqliteDatabase corrupt exception occurred when initializing database.");
            throw new ODMFSQLiteDatabaseCorruptException("Init database failed : " + e6.getMessage(), e6);
        } catch (com.huawei.hwsqlite.SQLiteDatabaseCorruptException e8) {
            e6 = e8;
            LOG.logE("Init database failed : A sqliteDatabase corrupt exception occurred when initializing database.");
            throw new ODMFSQLiteDatabaseCorruptException("Init database failed : " + e6.getMessage(), e6);
        } catch (ODMFSQLiteDatabaseCorruptException e9) {
            e6 = e9;
            LOG.logE("Init database failed : A sqliteDatabase corrupt exception occurred when initializing database.");
            throw new ODMFSQLiteDatabaseCorruptException("Init database failed : " + e6.getMessage(), e6);
        } catch (SQLiteDiskIOException e10) {
            e5 = e10;
            LOG.logE("Init database failed : A disk io exception occurred when initializing database.");
            throw new ODMFSQLiteDiskIOException("Init database failed : " + e5.getMessage(), e5);
        } catch (com.huawei.hwsqlite.SQLiteDiskIOException e11) {
            e5 = e11;
            LOG.logE("Init database failed : A disk io exception occurred when initializing database.");
            throw new ODMFSQLiteDiskIOException("Init database failed : " + e5.getMessage(), e5);
        } catch (ODMFSQLiteDiskIOException e12) {
            e5 = e12;
            LOG.logE("Init database failed : A disk io exception occurred when initializing database.");
            throw new ODMFSQLiteDiskIOException("Init database failed : " + e5.getMessage(), e5);
        } catch (SQLiteFullException e13) {
            e4 = e13;
            LOG.logE("Init database failed : A disk full exception occurred when initializing database.");
            throw new ODMFSQLiteFullException("Init database failed : " + e4.getMessage(), e4);
        } catch (com.huawei.hwsqlite.SQLiteFullException e14) {
            e4 = e14;
            LOG.logE("Init database failed : A disk full exception occurred when initializing database.");
            throw new ODMFSQLiteFullException("Init database failed : " + e4.getMessage(), e4);
        } catch (SQLiteAccessPermException e15) {
            e3 = e15;
            LOG.logE("Init database failed : An access permission exception occurred when initializing database.");
            throw new ODMFSQLiteAccessPermException("Init database failed : " + e3.getMessage(), e3);
        } catch (com.huawei.hwsqlite.SQLiteAccessPermException e16) {
            e3 = e16;
            LOG.logE("Init database failed : An access permission exception occurred when initializing database.");
            throw new ODMFSQLiteAccessPermException("Init database failed : " + e3.getMessage(), e3);
        } catch (SQLiteCantOpenDatabaseException e17) {
            e2 = e17;
            LOG.logE("Init database failed : An cant open exception occurred when initializing database.");
            throw new ODMFSQLiteCantOpenDatabaseException("Init database failed : " + e2.getMessage(), e2);
        } catch (com.huawei.hwsqlite.SQLiteCantOpenDatabaseException e18) {
            e2 = e18;
            LOG.logE("Init database failed : An cant open exception occurred when initializing database.");
            throw new ODMFSQLiteCantOpenDatabaseException("Init database failed : " + e2.getMessage(), e2);
        } catch (SQLiteException e19) {
            e = e19;
            LOG.logE("Init database failed : A SQLite exception occurred when initializing database.");
            throw new ODMFRuntimeException("Init database failed : " + e.getMessage(), e);
        } catch (com.huawei.hwsqlite.SQLiteException e20) {
            e = e20;
            LOG.logE("Init database failed : A SQLite exception occurred when initializing database.");
            throw new ODMFRuntimeException("Init database failed : " + e.getMessage(), e);
        } catch (Exception e21) {
            LOG.logE("Init database failed : A unknown exception occurred when initializing database.");
            throw new ODMFRuntimeException("Init database failed : " + e21.getMessage(), e21);
        } catch (Throwable th) {
            if (key != null && key.length > 0) {
                for (int i2 = 0; i2 < key.length; i2++) {
                    key[i2] = 0;
                }
            }
            throw th;
        }
    }

    private void loadMetadata() {
        setMetadata("databaseVersion", this.databaseHelper.getDatabaseVersion(this.db));
        setMetadata("databaseVersionCode", Integer.valueOf(this.databaseHelper.getDatabaseVersionCode(this.db)));
        if (getModel() == null || getModel().getEntities() == null) {
            LOG.logE("Execute loadMetadata failed : the objectModel or the entities is null.");
            throw new ODMFRuntimeException("Execute loadMetadata failed : the objectModel or the entities is null.");
        }
        for (String entityName : getModel().getEntities().keySet()) {
            String tableName = ((Entity) getModel().getEntities().get(entityName)).getTableName();
            setMetadata(tableName + "_version", this.databaseHelper.getEntityVersion(this.db, tableName));
            setMetadata(tableName + "_versionCode", Integer.valueOf(this.databaseHelper.getEntityVersionCode(this.db, tableName)));
        }
    }

    private void initHelper(ObjectModel model) {
        if (model == null) {
            LOG.logE("Execute initHelper failed : the objectModel is null.");
            throw new ODMFIllegalArgumentException("Execute initHelper failed : the model is null");
        }
        Map<String, ? extends Entity> entities = model.getEntities();
        if (entities == null) {
            LOG.logE("Execute initHelper failed : the entities is null.");
            throw new ODMFIllegalStateException("Execute initHelper failed : the entities in model is null");
        }
        for (Map.Entry<String, ? extends Entity> entry : entities.entrySet()) {
            String key = entry.getKey();
            try {
                AEntityHelper helper = (AEntityHelper) Class.forName(key + "Helper").getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
                helper.setEntity((Entity) entry.getValue());
                this.helperMap.put(key, helper);
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                LOG.logE("Execute initHelper failed : An exception occurred when use reflection to get helper class.");
                throw new ODMFRuntimeException("Execute initHelper failed : " + e.getMessage());
            }
        }
    }

    private void overwriteJoinClause(FetchRequest fetchRequest) {
        String clause = fetchRequest.getJoinClause().toString();
        StringBuilder sb = new StringBuilder("");
        String[] clauseSplit = clause.split("\\s+");
        for (int i = 0; i < clauseSplit.length; i++) {
            if (clauseSplit[i].contains("=")) {
                String[] entitySplit = clauseSplit[i].split("=");
                String entityNameNative = entitySplit[0];
                Entity entityNative = getModel().getEntity(entityNameNative);
                String tableNameNative = entityNative.getTableName();
                String entityNameRelated = entitySplit[1];
                String tableNameRelated = getModel().getEntity(entityNameRelated).getTableName();
                Iterator<? extends Relationship> it = getModel().getEntity(entityNameNative).getRelationships().iterator();
                while (it.hasNext()) {
                    ARelationship aRelationship = (ARelationship) it.next();
                    if (aRelationship.getRelatedEntity().getEntityName().equals(entityNameRelated)) {
                        switch (aRelationship.getRelationShipType()) {
                            case 0:
                                clauseSplit[i - 2] = DatabaseTableHelper.getManyToManyMidTableName(aRelationship);
                                String midTableColumnNative = DatabaseTableHelper.getRelationshipColumnName(entityNative);
                                clauseSplit[i] = tableNameNative + "." + DatabaseQueryService.getRowidColumnName() + " = " + midTableName + "." + midTableColumnNative + " INNER JOIN " + tableNameRelated + " ON " + midTableName + "." + DatabaseTableHelper.getRelationshipColumnName(entityRelated) + " = " + tableNameRelated + "." + DatabaseQueryService.getRowidColumnName();
                                break;
                            case 2:
                                clauseSplit[i] = tableNameNative + "." + aRelationship.getFieldName() + " = " + tableNameRelated + "." + aRelationship.getRelatedColumnName();
                                break;
                            case 4:
                                clauseSplit[i] = tableNameNative + "." + aRelationship.getInverseRelationship().getRelatedColumnName() + " = " + tableNameRelated + "." + aRelationship.getInverseRelationship().getFieldName();
                                break;
                            case 6:
                                if (!aRelationship.isMajor()) {
                                    clauseSplit[i] = tableNameNative + "." + aRelationship.getInverseRelationship().getRelatedColumnName() + " = " + tableNameRelated + "." + aRelationship.getInverseRelationship().getFieldName();
                                    break;
                                } else {
                                    clauseSplit[i] = tableNameNative + "." + aRelationship.getFieldName() + " = " + tableNameRelated + "." + aRelationship.getRelatedColumnName();
                                    break;
                                }
                            default:
                                LOG.logE("Execute overwriteJoinClause failed : the relation type is wrong.");
                                throw new ODMFIllegalStateException("Execute overwriteJoinClause failed : No such relationship type.");
                        }
                    }
                }
                continue;
            }
        }
        for (String append : clauseSplit) {
            sb.append(" ");
            sb.append(append);
        }
        fetchRequest.setJoinClause(sb);
    }

    /* access modifiers changed from: protected */
    public <T extends ManagedObject> List<T> executeFetchRequest(FetchRequest request, ObjectContext context2) {
        SQLException e;
        SQLException e2;
        SQLException e3;
        SQLException e4;
        SQLException e5;
        SQLException e6;
        Cursor cursor;
        if (request == null) {
            LOG.logE("Execute FetchRequest failed : the relation type is wrong.");
            throw new ODMFIllegalArgumentException("Execute FetchRequest failed : The parameter request is null.");
        }
        Cursor cursor2 = null;
        Cursor fastCursor = null;
        CursorWindow window = null;
        List<T> results = new ArrayList<>();
        if (this.model.getEntity(request.getEntityName()) == null) {
            LOG.logE("Execute FetchRequest failed : The entity which the entityName specified is not in the model.");
            throw new ODMFIllegalArgumentException("Execute FetchRequest failed : The entity which the entityName specified is not in the model.");
        }
        AEntityHelper entityHelper = getHelper(request.getEntityName());
        Entity entity = entityHelper.getEntity();
        String tableName = entity.getTableName();
        overwriteJoinClause(request);
        try {
            cursor2 = DatabaseQueryService.query(this.db, tableName + request.getJoinClause().toString(), request);
            boolean useFastCursor = false;
            if (getDatabaseType() == 302) {
                int count = cursor2.getCount();
                if (count == 0) {
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                    if (window != null) {
                        window.close();
                    }
                    return results;
                } else if (cursor2 instanceof CrossProcessCursor) {
                    window = ((CrossProcessCursor) cursor2).getWindow();
                    if (window != null) {
                        if (window.getNumRows() == count) {
                            useFastCursor = true;
                            fastCursor = new FastCursor(window);
                        } else {
                            LOG.logD("Window vs. result size: " + window.getNumRows() + "/" + count);
                        }
                    }
                }
            }
            if (!cursor2.moveToFirst()) {
                if (cursor2 != null) {
                    cursor2.close();
                }
                if (window != null) {
                    window.close();
                }
            } else {
                if (useFastCursor) {
                    cursor = fastCursor;
                } else {
                    cursor = cursor2;
                }
                loadManagedObjectFromCursor(entityHelper, entity, cursor, results, context2);
                if (cursor2 != null) {
                    cursor2.close();
                }
                if (window != null) {
                    window.close();
                }
            }
            return results;
        } catch (StaleDataException e7) {
            LOG.logE("Execute FetchRequest failed : A StaleDataException occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e7.getMessage(), e7);
        } catch (IllegalStateException e8) {
            LOG.logE("Execute FetchRequest failed : A IllegalStateException occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e8.getMessage(), e8);
        } catch (IllegalArgumentException e9) {
            LOG.logE("Execute FetchRequest failed : A IllegalArgumentException occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e9.getMessage(), e9);
        } catch (SQLiteDatabaseCorruptException e10) {
            e6 = e10;
            LOG.logE("Execute FetchRequest failed : A SQLiteDatabaseCorruptException occurred when query");
            throw new ODMFSQLiteDatabaseCorruptException("Execute FetchRequest failed : " + e6.getMessage(), e6);
        } catch (com.huawei.hwsqlite.SQLiteDatabaseCorruptException e11) {
            e6 = e11;
            LOG.logE("Execute FetchRequest failed : A SQLiteDatabaseCorruptException occurred when query");
            throw new ODMFSQLiteDatabaseCorruptException("Execute FetchRequest failed : " + e6.getMessage(), e6);
        } catch (SQLiteDiskIOException e12) {
            e5 = e12;
            LOG.logE("Execute FetchRequest failed : A SQLiteDiskIOException occurred when query");
            throw new ODMFSQLiteDiskIOException("Execute FetchRequest failed : " + e5.getMessage(), e5);
        } catch (com.huawei.hwsqlite.SQLiteDiskIOException e13) {
            e5 = e13;
            LOG.logE("Execute FetchRequest failed : A SQLiteDiskIOException occurred when query");
            throw new ODMFSQLiteDiskIOException("Execute FetchRequest failed : " + e5.getMessage(), e5);
        } catch (SQLiteFullException e14) {
            e4 = e14;
            LOG.logE("Execute FetchRequest failed : A SQLiteFullException occurred when query");
            throw new ODMFSQLiteFullException("Execute FetchRequest failed : " + e4.getMessage(), e4);
        } catch (com.huawei.hwsqlite.SQLiteFullException e15) {
            e4 = e15;
            LOG.logE("Execute FetchRequest failed : A SQLiteFullException occurred when query");
            throw new ODMFSQLiteFullException("Execute FetchRequest failed : " + e4.getMessage(), e4);
        } catch (SQLiteAccessPermException e16) {
            e3 = e16;
            LOG.logE("Execute FetchRequest failed : A SQLiteAccessPermException occurred when query");
            throw new ODMFSQLiteAccessPermException("Execute FetchRequest failed : " + e3.getMessage(), e3);
        } catch (com.huawei.hwsqlite.SQLiteAccessPermException e17) {
            e3 = e17;
            LOG.logE("Execute FetchRequest failed : A SQLiteAccessPermException occurred when query");
            throw new ODMFSQLiteAccessPermException("Execute FetchRequest failed : " + e3.getMessage(), e3);
        } catch (SQLiteCantOpenDatabaseException e18) {
            e2 = e18;
            LOG.logE("Execute FetchRequest failed : A SQLiteCantOpenDatabaseException occurred when query");
            throw new ODMFSQLiteCantOpenDatabaseException("Execute FetchRequest failed : " + e2.getMessage(), e2);
        } catch (com.huawei.hwsqlite.SQLiteCantOpenDatabaseException e19) {
            e2 = e19;
            LOG.logE("Execute FetchRequest failed : A SQLiteCantOpenDatabaseException occurred when query");
            throw new ODMFSQLiteCantOpenDatabaseException("Execute FetchRequest failed : " + e2.getMessage(), e2);
        } catch (SQLiteException e20) {
            e = e20;
            LOG.logE("Execute FetchRequest failed : A SQLiteException occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e.getMessage(), e);
        } catch (com.huawei.hwsqlite.SQLiteException e21) {
            e = e21;
            LOG.logE("Execute FetchRequest failed : A SQLiteException occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e.getMessage(), e);
        } catch (Exception e22) {
            LOG.logE("Execute FetchRequest failed : A unknown exception occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e22.getMessage(), e22);
        } catch (Throwable th) {
            if (cursor2 != null) {
                cursor2.close();
            }
            if (window != null) {
                window.close();
            }
            throw th;
        }
    }

    private <T extends ManagedObject> void loadManagedObjectFromCursor(AEntityHelper helper, Entity entity, Cursor cursor, List<T> results, ObjectContext context2) {
        boolean existCache = CacheConfig.getDefault().isOpenObjectCache();
        do {
            long id = cursor.getLong(DatabaseQueryService.getOdmfRowidIndex());
            if (existCache) {
                ManagedObject cacheObject = PersistentStoreCoordinator.getDefault().getObjectsCache().get(new AObjectId(entity.getEntityName(), Long.valueOf(id), getUriString()));
                if (cacheObject != null) {
                    if (!cacheObject.isDirty()) {
                        results.add(cacheObject);
                    } else if (cacheObject.getLastObjectContext() == context2) {
                        results.add(cacheObject);
                    }
                }
            }
            T obj = (ManagedObject) helper.readObject(cursor, 0);
            obj.setRowId(Long.valueOf(id));
            obj.setState(4);
            obj.setObjectContext(context2);
            obj.setUriString(this.uriString);
            results.add(obj);
        } while (cursor.moveToNext());
    }

    private AEntityHelper getHelper(String entityName) {
        return this.helperMap.get(entityName);
    }

    /* access modifiers changed from: protected */
    public List<ObjectId> executeFetchRequestGetObjectID(FetchRequest request) {
        if (request == null) {
            LOG.logE("Execute FetchRequestGetObjectID failed : The parameter request is null.");
            throw new ODMFIllegalArgumentException("The parameter request is null.");
        }
        List<ObjectId> results = new ArrayList<>();
        Entity entity = getModel().getEntity(request.getEntityName());
        if (entity == null) {
            LOG.logE("Execute FetchRequestGetObjectID failed : he entity which the entityName specified is not in the model.");
            throw new ODMFIllegalArgumentException("Execute FetchRequestGetObjectID failed : The entity which the entityName specified is not in the model");
        }
        String tableName = entity.getTableName();
        overwriteJoinClause(request);
        Cursor cursor = null;
        try {
            Cursor cursor2 = DatabaseQueryService.queryRowID(this.db, tableName + request.getJoinClause().toString(), request);
            while (cursor2.moveToNext()) {
                results.add(createObjectID(entity, Long.valueOf(cursor2.getLong(DatabaseQueryService.getOdmfRowidIndex()))));
            }
            if (cursor2 != null) {
                cursor2.close();
            }
            return results;
        } catch (SQLiteException e) {
            LOG.logE("Execute FetchRequestGetObjectID failed : A SQLiteException occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequestGetObjectID failed : " + e.getMessage());
        } catch (StaleDataException e2) {
            LOG.logE("Execute FetchRequestGetObjectID failed : A StaleDataException occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequestGetObjectID failed : " + e2.getMessage());
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:24:0x009e=Splitter:B:24:0x009e, B:33:0x00de=Splitter:B:33:0x00de} */
    public List<Object> executeFetchRequestWithAggregateFunction(FetchRequest request) {
        SQLException e;
        SQLException e2;
        SQLException e3;
        if (request == null) {
            LOG.logE("Execute FetchRequestWithAggregateFunction failed : The parameter request is null.");
            throw new ODMFIllegalArgumentException("The parameter request is null.");
        } else if (this.model.getEntity(request.getEntityName()) == null) {
            LOG.logE("Execute FetchRequestWithAggregateFunction failed : The entity which the entityName specified is not in the model.");
            throw new ODMFIllegalArgumentException("The entity which the entityName specified is not in the model.");
        } else {
            Entity entity = getHelper(request.getEntityName()).getEntity();
            String tableName = entity.getTableName();
            overwriteJoinClause(request);
            String tableName2 = tableName + request.getJoinClause().toString();
            String[] columns = request.getColumns();
            int[] aggregateOp = request.getAggregateOp();
            if (columns == null || aggregateOp == null) {
                LOG.logE("Execute FetchRequestWithAggregateFunction failed : the querying columns is null or aggregateOp is null.");
                throw new ODMFRuntimeException("Execute FetchRequestWithAggregateFunction failed : the querying columns is null or aggregateOp is null.");
            }
            Cursor cursor = null;
            List<Object> result = new ArrayList<>();
            try {
                Cursor cursor2 = DatabaseQueryService.queryWithAggregateFunction(this.db, tableName2, request);
                cursor2.moveToFirst();
                for (int i = 0; i < columns.length; i++) {
                    result.add(CursorUtils.extractAggregateResult(cursor2.getString(i), aggregateOp[i], entity.getAttribute(columns[i])));
                }
                if (cursor2 != null) {
                    cursor2.close();
                }
                return result;
            } catch (SQLiteDatabaseCorruptException e4) {
                e3 = e4;
                LOG.logE("Execute fetchRequest with aggregateFunction failed : " + e3.getMessage());
                throw new ODMFSQLiteDatabaseCorruptException("End Transaction failed : " + e3.getMessage(), e3);
            } catch (com.huawei.hwsqlite.SQLiteDatabaseCorruptException e5) {
                e3 = e5;
                LOG.logE("Execute fetchRequest with aggregateFunction failed : " + e3.getMessage());
                throw new ODMFSQLiteDatabaseCorruptException("End Transaction failed : " + e3.getMessage(), e3);
            } catch (SQLiteDiskIOException e6) {
                e2 = e6;
                LOG.logE("End Transaction failed : " + e2.getMessage());
                throw new ODMFSQLiteDiskIOException("End Transaction failed : " + e2.getMessage(), e2);
            } catch (com.huawei.hwsqlite.SQLiteDiskIOException e7) {
                e2 = e7;
                LOG.logE("End Transaction failed : " + e2.getMessage());
                throw new ODMFSQLiteDiskIOException("End Transaction failed : " + e2.getMessage(), e2);
            } catch (SQLiteException e8) {
                e = e8;
                LOG.logE("Execute fetchRequest With aggregateFunction failed : A SQLiteException occurred when query.");
                throw new ODMFRuntimeException("Execute fetchRequest With aggregateFunction failed : " + e.getMessage(), e);
            } catch (com.huawei.hwsqlite.SQLiteException e9) {
                e = e9;
                LOG.logE("Execute fetchRequest With aggregateFunction failed : A SQLiteException occurred when query.");
                throw new ODMFRuntimeException("Execute fetchRequest With aggregateFunction failed : " + e.getMessage(), e);
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
    }

    /* access modifiers changed from: protected */
    public Cursor executeFetchRequestGetCursor(FetchRequest request) {
        if (request == null) {
            LOG.logE("Execute FetchRequestGetCursor failed : The parameter request is null.");
            throw new ODMFIllegalArgumentException("Execute FetchRequestGetCursor failed : The parameter request is null");
        }
        Entity entity = getModel().getEntity(request.getEntityName());
        if (entity == null) {
            LOG.logE("Execute FetchRequestGetCursor failed : The entity which the entityName specified is not in the model.");
            throw new ODMFIllegalArgumentException("Execute FetchRequestGetCursor failed : The entity which the entityName specified is not in the model.");
        }
        String tableName = entity.getTableName();
        overwriteJoinClause(request);
        try {
            return DatabaseQueryService.commonquery(this.db, tableName + request.getJoinClause().toString(), request);
        } catch (StaleDataException e) {
            LOG.logE("Execute FetchRequest failed : A StaleDataException occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e.getMessage(), e);
        } catch (IllegalStateException e2) {
            LOG.logE("Execute FetchRequest failed : A IllegalStateException occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e2.getMessage(), e2);
        } catch (IllegalArgumentException e3) {
            LOG.logE("Execute FetchRequest failed : A IllegalArgumentException occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e3.getMessage(), e3);
        } catch (SQLiteDatabaseCorruptException | com.huawei.hwsqlite.SQLiteDatabaseCorruptException e4) {
            LOG.logE("Execute FetchRequest failed : A SQLiteDatabaseCorruptException occurred when query");
            throw new ODMFSQLiteDatabaseCorruptException("Execute FetchRequest failed : " + e4.getMessage(), e4);
        } catch (SQLiteDiskIOException | com.huawei.hwsqlite.SQLiteDiskIOException e5) {
            LOG.logE("Execute FetchRequest failed : A SQLiteDiskIOException occurred when query");
            throw new ODMFSQLiteDiskIOException("Execute FetchRequest failed : " + e5.getMessage(), e5);
        } catch (SQLiteFullException | com.huawei.hwsqlite.SQLiteFullException e6) {
            LOG.logE("Execute FetchRequest failed : A SQLiteFullException occurred when query");
            throw new ODMFSQLiteFullException("Execute FetchRequest failed : " + e6.getMessage(), e6);
        } catch (SQLiteAccessPermException | com.huawei.hwsqlite.SQLiteAccessPermException e7) {
            LOG.logE("Execute FetchRequest failed : A SQLiteAccessPermException occurred when query");
            throw new ODMFSQLiteAccessPermException("Execute FetchRequest failed : " + e7.getMessage(), e7);
        } catch (SQLiteCantOpenDatabaseException | com.huawei.hwsqlite.SQLiteCantOpenDatabaseException e8) {
            LOG.logE("Execute FetchRequest failed : A SQLiteCantOpenDatabaseException occurred when query");
            throw new ODMFSQLiteCantOpenDatabaseException("Execute FetchRequest failed : " + e8.getMessage(), e8);
        } catch (SQLiteException | com.huawei.hwsqlite.SQLiteException e9) {
            LOG.logE("Execute FetchRequest failed : A SQLiteException occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e9.getMessage(), e9);
        } catch (Exception e10) {
            LOG.logE("Execute FetchRequest failed : A unknown exception occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e10.getMessage(), e10);
        }
    }

    /* access modifiers changed from: protected */
    public void executeSaveRequestWithTransaction(SaveRequest request) {
        this.db.beginTransaction();
        try {
            executeSaveRequest(request);
            this.db.setTransactionSuccessful();
            increaseVersion();
        } finally {
            this.db.endTransaction();
        }
    }

    private void executeInsert(List<ManagedObject> insertList) {
        int insertSize = insertList.size();
        for (int i = 0; i < insertSize; i++) {
            ManagedObject object = insertList.get(i);
            AEntityHelper entityHelper = ((AManagedObject) object).getHelper();
            if (!checkEntityHelper(entityHelper)) {
                throw new ODMFIllegalArgumentException("Execute SaveRequest failed : The object is incompatible with the ObjectContext.");
            }
            Entity entity = entityHelper.getEntity();
            Statement statement = entity.getStatements().getInsertStatement(this.db, entity.getTableName(), entity.getAttributes());
            synchronized (this.statementLock) {
                entityHelper.bindValue(statement, object);
                long lastRowID = statement.executeInsert();
                object.setRowId(Long.valueOf(lastRowID));
                object.setUriString(this.uriString);
                if (entity.isKeyAutoIncrement()) {
                    entityHelper.setPrimaryKeyValue(object, lastRowID);
                }
                statement.clearBindings();
            }
            object.setState(4);
        }
        this.relationshipLoader.handleRelationship(insertList);
    }

    private boolean checkEntityHelper(AEntityHelper entityHelper) {
        Entity entity = entityHelper.getEntity();
        if (entity != null) {
            if (entity.getModel() == this.model) {
                return true;
            }
            Entity entity2 = getModel().getEntity(entity.getEntityName());
            if (entity2 != null) {
                entityHelper.setEntity(entity2);
                return true;
            }
        }
        return false;
    }

    private void executeUpdate(List<ManagedObject> updatedList) {
        int updateSize = updatedList.size();
        for (int i = 0; i < updateSize; i++) {
            ManagedObject object = updatedList.get(i);
            long id = object.getRowId().longValue();
            AEntityHelper entityHelper = ((AManagedObject) object).getHelper();
            if (!checkEntityHelper(entityHelper)) {
                throw new ODMFIllegalArgumentException("Execute SaveRequest failed : The object is incompatible with the ObjectContext.");
            }
            Entity entity = entityHelper.getEntity();
            Statement statement = entity.getStatements().getUpdateStatement(this.db, entity.getTableName(), entity.getAttributes());
            synchronized (this.statementLock) {
                entityHelper.bindValue(statement, object);
                statement.bindLong(entity.getAttributes().size() + 1, id);
                statement.execute();
                statement.clearBindings();
            }
            object.setState(4);
        }
        this.relationshipLoader.handleRelationship(updatedList);
    }

    private void executeDelete(List<ManagedObject> deleteList) {
        int deleteSize = deleteList.size();
        for (int i = 0; i < deleteSize; i++) {
            ManagedObject object = deleteList.get(i);
            AEntityHelper entityHelper = ((AManagedObject) object).getHelper();
            if (!checkEntityHelper(entityHelper)) {
                throw new ODMFIllegalArgumentException("Execute SaveRequest failed : The object is incompatible with the ObjectContext.");
            }
            Entity entity = entityHelper.getEntity();
            List<ManagedObject> cascadeDeleteObjects = null;
            if (!entity.getRelationships().isEmpty()) {
                cascadeDeleteObjects = new ArrayList<>();
                this.relationshipLoader.handleCascadeDelete(object, cascadeDeleteObjects);
            }
            Statement statement = entity.getStatements().getDeleteStatement(this.db, entity.getTableName(), entity.getAttributes());
            synchronized (this.statementLock) {
                statement.bindLong(1, object.getRowId().longValue());
                statement.execute();
                statement.clearBindings();
            }
            if (cascadeDeleteObjects != null && !cascadeDeleteObjects.isEmpty()) {
                deleteObjectFromSet(cascadeDeleteObjects, object);
                deleteObjectSetFromSet(cascadeDeleteObjects, deleteList);
                while (!cascadeDeleteObjects.isEmpty()) {
                    List<ManagedObject> moreObjectNeedDelete = new ArrayList<>(cascadeDeleteObjects);
                    moreObjectNeedDelete.addAll(deleteList);
                    for (ManagedObject obj : cascadeDeleteObjects) {
                        long objId = obj.getRowId().longValue();
                        Entity objEntity = ((AManagedObject) obj).getHelper().getEntity();
                        this.relationshipLoader.handleCascadeDelete(obj, moreObjectNeedDelete);
                        Statement statement2 = objEntity.getStatements().getDeleteStatement(this.db, objEntity.getTableName(), objEntity.getAttributes());
                        synchronized (this.statementLock) {
                            statement2.bindLong(1, objId);
                            statement2.executeUpdateDelete();
                            statement2.clearBindings();
                        }
                    }
                    deleteObjectSetFromSet(moreObjectNeedDelete, cascadeDeleteObjects);
                    deleteObjectSetFromSet(moreObjectNeedDelete, deleteList);
                    cascadeDeleteObjects.clear();
                    cascadeDeleteObjects.addAll(moreObjectNeedDelete);
                }
                continue;
            }
            object.setState(0);
        }
    }

    /* access modifiers changed from: protected */
    public void executeSaveRequest(SaveRequest request) {
        if (request == null) {
            LOG.logE("Execute SaveRequest failed : The parameter request is null");
            throw new ODMFIllegalArgumentException("The parameter request is null");
        }
        try {
            executeInsert(request.getInsertedObjects());
            executeUpdate(request.getUpdatedObjects());
            executeDelete(request.getDeletedObjects());
        } catch (SQLiteDatabaseCorruptException | com.huawei.hwsqlite.SQLiteDatabaseCorruptException e) {
            LOG.logE("Execute SaveRequest failed : A SQLiteDatabaseCorruptException occurred when save");
            throw new ODMFSQLiteDatabaseCorruptException("Execute SaveRequest failed : " + e.getMessage(), e);
        } catch (SQLiteDiskIOException | com.huawei.hwsqlite.SQLiteDiskIOException e2) {
            LOG.logE("Execute SaveRequest failed : A SQLiteDiskIOException occurred when save");
            throw new ODMFSQLiteDiskIOException("Execute SaveRequest failed : " + e2.getMessage(), e2);
        } catch (SQLiteFullException | com.huawei.hwsqlite.SQLiteFullException e3) {
            LOG.logE("Execute SaveRequest failed : A SQLiteFullException occurred when save");
            throw new ODMFSQLiteFullException("Execute SaveRequest failed : " + e3.getMessage(), e3);
        } catch (SQLiteAccessPermException | com.huawei.hwsqlite.SQLiteAccessPermException e4) {
            LOG.logE("Execute SaveRequest failed : A SQLiteAccessPermException occurred when save");
            throw new ODMFSQLiteAccessPermException("Execute SaveRequest failed : " + e4.getMessage(), e4);
        } catch (SQLiteCantOpenDatabaseException | com.huawei.hwsqlite.SQLiteCantOpenDatabaseException e5) {
            LOG.logE("Execute SaveRequest failed : A SQLiteCantOpenDatabaseException occurred when save");
            throw new ODMFSQLiteCantOpenDatabaseException("Execute SaveRequest failed : " + e5.getMessage(), e5);
        } catch (SQLiteException | com.huawei.hwsqlite.SQLiteException | IllegalArgumentException e6) {
            LOG.logE("Execute SaveRequest failed : A SQLiteException occurred when save");
            throw new ODMFRuntimeException("Execute SaveRequest failed : " + e6.getMessage());
        } catch (Exception e7) {
            LOG.logE("Execute FetchRequest failed : A unknown exception occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e7.getMessage(), e7);
        }
    }

    private void deleteObjectSetFromSet(List<ManagedObject> deleteFrom, List<ManagedObject> deletes) {
        int size = deletes.size();
        for (int i = 0; i < size; i++) {
            deleteObjectFromSet(deleteFrom, deletes.get(i));
        }
    }

    private void deleteObjectFromSet(List<ManagedObject> deleteFrom, ManagedObject delete) {
        int size = deleteFrom.size();
        int i = 0;
        while (i < size) {
            ManagedObject manageObject = deleteFrom.get(i);
            if (!manageObject.getRowId().equals(delete.getRowId()) || !manageObject.getEntityName().equals(delete.getEntityName())) {
                i++;
            } else {
                deleteFrom.remove(manageObject);
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void beginTransaction() {
        try {
            if (this.db.inTransaction()) {
                LOG.logE("Execute beginTransaction failed : The database is already in a transaction.");
                throw new ODMFIllegalStateException("The database is already in a transaction.");
            } else {
                this.db.beginTransaction();
            }
        } catch (IllegalStateException e) {
            LOG.logE("Execute beginTransaction failed :" + e.getMessage());
            throw new ODMFIllegalStateException("execute beginTransaction failed." + e.getMessage(), e);
        }
    }

    /* access modifiers changed from: protected */
    public boolean inTransaction() {
        try {
            return this.db.inTransaction();
        } catch (IllegalStateException e) {
            LOG.logE("Check inTransaction failed :" + e.getMessage());
            throw new ODMFIllegalStateException("Check inTransaction failed." + e.getMessage(), e);
        }
    }

    /* access modifiers changed from: protected */
    public void rollback() {
        try {
            if (this.db.inTransaction()) {
                this.db.endTransaction();
            } else {
                LOG.logE("Execute rollback failed : The database " + this.db.getPath() + " is not in a transaction.");
                throw new ODMFIllegalStateException("Execute rollback failed : The database " + this.db.getPath() + " is not in a transaction.");
            }
        } catch (IllegalStateException e) {
            LOG.logE("Execute rollback transaction failed : " + e.getMessage());
            throw new ODMFIllegalStateException("Execute rollback transaction failed." + e.getMessage(), e);
        }
    }

    /* access modifiers changed from: protected */
    public void commit() {
        try {
            if (this.db.inTransaction()) {
                this.db.setTransactionSuccessful();
                increaseVersion();
                this.db.endTransaction();
                return;
            }
            LOG.logE("Execute commit failed : The database " + this.db.getPath() + " is not in a transaction.");
            throw new ODMFIllegalStateException("Execute commit failed : The database " + this.db.getPath() + " is not in a transaction");
        } catch (IllegalStateException e) {
            LOG.logE("Execute commit transaction failed :" + e.getMessage());
            throw new ODMFIllegalStateException("Execute rollback commit failed." + e.getMessage(), e);
        }
    }

    public void clearTable(String entityName) {
        com.huawei.hwsqlite.SQLiteException sQLiteException;
        SQLException e;
        SQLException e2;
        if (entityName == null) {
            LOG.logE("Execute deleteEntityData failed : The entityName is null.");
            throw new ODMFIllegalArgumentException("Execute deleteEntityData failed : The entityName is null.");
        }
        Entity entity = getModel().getEntity(entityName);
        if (entity == null) {
            LOG.logE("Execute deleteEntityData failed : The entity which the entityName specified not in current model.");
            throw new ODMFIllegalArgumentException("Execute deleteEntityData failed : The entity which the entityName specified not in current model.");
        }
        String tableName = entity.getTableName();
        boolean hasCorruptException = false;
        Cursor cursor = null;
        try {
            this.db.beginTransaction();
            this.db.delete(tableName, null, null);
            List<? extends Relationship> relationships = entity.getRelationships();
            int relationshipSize = relationships.size();
            for (int i = 0; i < relationshipSize; i++) {
                Relationship relationship = (Relationship) relationships.get(i);
                if (relationship.getRelationShipType() == 0) {
                    this.db.delete(DatabaseTableHelper.getManyToManyMidTableName(relationship), null, null);
                }
            }
            Cursor cursor2 = this.db.rawQuery("SELECT 1 FROM sqlite_master WHERE type='table' AND name='sqlite_sequence'", null);
            if (cursor2.getCount() > 0) {
                this.db.execSQL("UPDATE sqlite_sequence SET seq = 0 WHERE name = '" + tableName + "'");
            }
            this.db.setTransactionSuccessful();
            if (cursor2 != null) {
                cursor2.close();
            }
            this.db.endTransaction(false);
        } catch (SQLiteDatabaseCorruptException e3) {
            e2 = e3;
            hasCorruptException = true;
            LOG.logE("Execute deleteEntityData failed : A SQLiteDatabaseCorruptException occurred : " + e2.getMessage());
            throw new ODMFSQLiteDatabaseCorruptException("End Transaction failed : " + e2.getMessage(), e2);
        } catch (com.huawei.hwsqlite.SQLiteDatabaseCorruptException e4) {
            e2 = e4;
            hasCorruptException = true;
            LOG.logE("Execute deleteEntityData failed : A SQLiteDatabaseCorruptException occurred : " + e2.getMessage());
            throw new ODMFSQLiteDatabaseCorruptException("End Transaction failed : " + e2.getMessage(), e2);
        } catch (SQLiteDiskIOException e5) {
            e = e5;
            hasCorruptException = true;
            LOG.logE("Execute deleteEntityData failed : A SQLiteDiskIOException occurred : " + e.getMessage());
            throw new ODMFSQLiteDiskIOException("End Transaction failed : " + e.getMessage(), e);
        } catch (com.huawei.hwsqlite.SQLiteDiskIOException e6) {
            e = e6;
            hasCorruptException = true;
            LOG.logE("Execute deleteEntityData failed : A SQLiteDiskIOException occurred : " + e.getMessage());
            throw new ODMFSQLiteDiskIOException("End Transaction failed : " + e.getMessage(), e);
        } catch (SQLiteException e7) {
            sQLiteException = e7;
            LOG.logE("Execute deleteEntityData failed : " + sQLiteException.getMessage());
            throw new ODMFRuntimeException("Execute deleteEntityData failed : " + sQLiteException.getMessage());
        } catch (com.huawei.hwsqlite.SQLiteException e8) {
            sQLiteException = e8;
            LOG.logE("Execute deleteEntityData failed : " + sQLiteException.getMessage());
            throw new ODMFRuntimeException("Execute deleteEntityData failed : " + sQLiteException.getMessage());
        } catch (IllegalStateException e9) {
            LOG.logE("Execute deleteEntityData failed : " + e9.getMessage());
            throw new ODMFIllegalStateException("Execute deleteEntityData failed." + e9.getMessage(), e9);
        } catch (RuntimeException e10) {
            LOG.logE("Execute deleteEntityData failed : A RuntimeException occurred : " + e10.getMessage());
            throw new ODMFRuntimeException("Execute delete failed : " + e10.getMessage(), e10);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            this.db.endTransaction(hasCorruptException);
            throw th;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: com.huawei.odmf.core.ManagedObject} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    public ManagedObject getObjectValues(ObjectId objectId) {
        Entity entity = getModel().getEntity(objectId.getEntityName());
        Long id = (Long) objectId.getId();
        AEntityHelper entityHelper = getHelper(objectId.getEntityName());
        ManagedObject managedObject = null;
        Cursor cursor = null;
        try {
            cursor = DatabaseQueryService.query(this.db, entity.getTableName(), new String[]{DatabaseQueryService.getRowidColumnName() + " AS " + DatabaseQueryService.getRowidColumnName(), "*"}, DatabaseQueryService.getRowidColumnName() + "=?", new String[]{String.valueOf(id)});
            if (cursor.moveToNext()) {
                managedObject = entityHelper.readObject(cursor, 0);
                managedObject.setObjectId(objectId);
                managedObject.setRowId((Long) objectId.getId());
            }
            return managedObject;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /* access modifiers changed from: protected */
    public List<ObjectId> getRelationshipObjectId(ObjectId objectId, Relationship relationship) {
        SQLException e;
        SQLException e2;
        Cursor cursor;
        Entity relatedEntity = relationship.getRelatedEntity();
        List<ObjectId> results = new ArrayList<>();
        Cursor cursor2 = null;
        try {
            if (relationship.getRelationShipType() == 2) {
                cursor = this.relationshipLoader.getManyToOneRelationshipCursor(objectId, relationship);
            } else if (relationship.getRelationShipType() == 6) {
                cursor = this.relationshipLoader.getOneToOneRelationshipCursor(objectId, relationship);
            } else if (relationship.getRelationShipType() == 4) {
                cursor = this.relationshipLoader.getOneToManyRelationshipCursor(objectId, relationship);
            } else {
                cursor = this.relationshipLoader.getManyToManyRelationshipCursor(objectId, relationship);
            }
            while (cursor.moveToNext()) {
                String idStr = cursor.getString(0);
                if (idStr != null) {
                    results.add(createObjectID(relatedEntity, Long.valueOf(Long.parseLong(idStr))));
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            if (!relationship.getNotFound().equals(ARelationship.EXCEPTION) || results.size() != 0) {
                return results;
            }
            LOG.logE("Execute getRelationshipObjectId failed : The relevant object not found.");
            throw new ODMFRelatedObjectNotFoundException("Execute getRelationshipObjectId failed : The relevant object not found.");
        } catch (SQLiteDatabaseCorruptException e3) {
            e2 = e3;
            LOG.logE("Execute getRelationshipObjectId failed : A SQLiteException occurred when try to get related ObjectId.");
            throw new ODMFSQLiteDatabaseCorruptException("End Transaction failed : " + e2.getMessage(), e2);
        } catch (com.huawei.hwsqlite.SQLiteDatabaseCorruptException e4) {
            e2 = e4;
            LOG.logE("Execute getRelationshipObjectId failed : A SQLiteException occurred when try to get related ObjectId.");
            throw new ODMFSQLiteDatabaseCorruptException("End Transaction failed : " + e2.getMessage(), e2);
        } catch (SQLiteDiskIOException e5) {
            e = e5;
            LOG.logE("Execute getRelationshipObjectId failed : A SQLiteException occurred when try to get related ObjectId.");
            throw new ODMFSQLiteDiskIOException("End Transaction failed : " + e.getMessage(), e);
        } catch (com.huawei.hwsqlite.SQLiteDiskIOException e6) {
            e = e6;
            LOG.logE("Execute getRelationshipObjectId failed : A SQLiteException occurred when try to get related ObjectId.");
            throw new ODMFSQLiteDiskIOException("End Transaction failed : " + e.getMessage(), e);
        } catch (SQLiteException e7) {
            LOG.logE("Execute getRelationshipObjectId failed : A SQLiteException occurred when try to get related ObjectId.");
            throw new ODMFRuntimeException("Execute getRelationshipObjectId failed : " + e7.getMessage());
        } catch (Throwable th) {
            if (cursor2 != null) {
                cursor2.close();
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void close() {
        try {
            this.databaseHelper.close();
        } catch (SQLiteDatabaseCorruptException | com.huawei.hwsqlite.SQLiteDatabaseCorruptException e) {
            LOG.logE("Close database failed : A SQLiteDatabaseCorruptException occurred when close.");
            throw new ODMFSQLiteDatabaseCorruptException("Close database failed : " + e.getMessage(), e);
        } catch (SQLiteDiskIOException | com.huawei.hwsqlite.SQLiteDiskIOException e2) {
            LOG.logE("Close database failed : A SQLiteDiskIOException occurred when close.");
            throw new ODMFSQLiteDiskIOException("Close database failed : " + e2.getMessage(), e2);
        } catch (RuntimeException e3) {
            LOG.logE("Close database failed : A RuntimeException occurred when close.");
            throw new ODMFRuntimeException("Close database failed : " + e3.getMessage(), e3);
        }
    }

    /* access modifiers changed from: protected */
    public void createEntityForMigration(Entity entity) {
        if (entity == null) {
            LOG.logE("Execute createTable failed : The input parameter entity is null.");
            throw new ODMFIllegalArgumentException("Execute createTable failed : The input parameter entity is null.");
        }
        try {
            this.databaseHelper.addTable(this.db, entity);
        } catch (SQLiteException e) {
            LOG.logE("Execute createTable Failed : A SQLiteException occurred when createTable");
            throw new ODMFRuntimeException("Execute createTable Failed : " + e.getMessage());
        }
    }

    public void dropEntityForMigration(String entityName) {
        if (entityName == null || entityName.equals("")) {
            LOG.logE("Execute dropTable Failed : The input parameter entityName is null.");
            throw new ODMFIllegalArgumentException("Execute dropTable Failed : The input parameter entityName is null.");
        }
        try {
            this.databaseHelper.dropTable(this.db, entityName);
        } catch (SQLiteException e) {
            LOG.logE("Execute dropTable Failed : A SQLiteException occurred when dropTable");
            throw new ODMFRuntimeException("Execute dropTable Failed : " + e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public void renameEntityForMigration(String newName, String oldName) {
        if (newName == null || oldName == null) {
            LOG.logE("Execute renameTable Failed :The newName or the oldName is null.");
            throw new ODMFIllegalArgumentException("Execute renameTable Failed : The newName or the oldName is null.");
        }
        try {
            this.databaseHelper.alterTableName(this.db, oldName, newName);
        } catch (SQLiteException e) {
            LOG.logE("Execute renameTable Failed : A SQLiteException occurred when renameTable");
            throw new ODMFRuntimeException("Execute renameTable Failed : " + e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public void addColumnForMigration(Entity entity, Attribute attribute) {
        if (entity == null || attribute == null) {
            LOG.logE("Execute addColumn Failed : The entity or the attribute is null.");
            throw new ODMFIllegalArgumentException("Execute addColumn Failed : The entity or the attribute is null.");
        }
        try {
            this.databaseHelper.alterTableAddColumn(this.db, entity.getTableName(), attribute);
        } catch (SQLiteException e) {
            LOG.logE("Execute addColumn Failed : A SQLiteException occurred when addColumn");
            throw new ODMFRuntimeException("Execute addColumn Failed : " + e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public void addRelationshipForMigration(Entity entity, Relationship relationship) {
        if (entity == null || relationship == null) {
            LOG.logE("Execute addRelationship Failed : The entity or the relationship is null.");
            throw new ODMFIllegalArgumentException("Execute addRelationship Failed : The entity or the relationship is null.");
        }
        try {
            this.databaseHelper.alterTableAddRelationship(this.db, entity.getTableName(), relationship);
        } catch (SQLiteException e) {
            LOG.logE("Execute addRelationship Failed : A SQLiteException occurred when addRelationship");
            throw new ODMFRuntimeException("Execute addRelationship Failed : " + e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public ManagedObject getToOneRelationshipValue(String fieldName, ManagedObject object, ObjectContext objectContext) {
        return loadToOneRelationshipValue(fieldName, object, objectContext);
    }

    /* access modifiers changed from: protected */
    public List<ManagedObject> getToManyRelationshipValue(String fieldName, ManagedObject object, ObjectContext objectContext) {
        if (fieldName == null || object == null || objectContext == null) {
            LOG.logE("Execute getToManyRelationshipValue Failed : The input parameter is null.");
            throw new ODMFIllegalArgumentException("Execute getToManyRelationshipValue Failed : The input parameter is null.");
        }
        Relationship relationship = getModel().getEntity(object.getEntityName()).getRelationship(fieldName);
        if (relationship == null) {
            LOG.logE("Execute getToManyRelationshipValue Failed : This field name is not in the class.");
            throw new ODMFIllegalArgumentException("Execute getToManyRelationshipValue Failed : This field name is not in the class.");
        }
        List<ObjectId> resultID = getRelationshipObjectId(object.getObjectId(), relationship);
        int resultIdSize = resultID.size();
        if (resultIdSize == 0) {
            return null;
        }
        if (relationship.isLazy()) {
            return new LazyList(resultID, objectContext, relationship.getRelatedEntity().getEntityName(), object);
        }
        ODMFList<ManagedObject> odmfList = new ODMFList<>(objectContext, relationship.getRelatedEntity().getEntityName(), object);
        for (int i = 0; i < resultIdSize; i++) {
            odmfList.addObj(i, getManagedObjectWithCache(resultID.get(i), objectContext));
        }
        return odmfList;
    }

    /* access modifiers changed from: protected */
    public ManagedObject remoteGetToOneRelationshipValue(String fieldName, ObjectId objectID, ObjectContext objectContext) {
        return loadToOneRelationshipValue(fieldName, objectID, objectContext);
    }

    /* access modifiers changed from: protected */
    public List<ManagedObject> remoteGetToManyRelationshipValue(String fieldName, ObjectId objectID, ObjectContext objectContext) {
        if (fieldName == null || objectID == null || objectContext == null) {
            LOG.logE("Execute remoteGetToManyRelationshipValue Failed : The input parameter is null.");
            throw new ODMFIllegalArgumentException("Execute remoteGetToManyRelationshipValue Failed : The input parameter is null.");
        }
        Relationship relationship = getModel().getEntity(objectID.getEntityName()).getRelationship(fieldName);
        if (relationship == null) {
            LOG.logE("Execute remoteGetToManyRelationshipValue Failed : This field name is not in the class.");
            throw new ODMFIllegalArgumentException("Execute remoteGetToManyRelationshipValue Failed : This field name is not in the class.");
        }
        List<ObjectId> resultID = getRelationshipObjectId(objectID, relationship);
        int resultIdSize = resultID.size();
        if (resultIdSize == 0) {
            return null;
        }
        ArrayList<ManagedObject> resultList = new ArrayList<>();
        for (int i = 0; i < resultIdSize; i++) {
            resultList.add(getManagedObjectWithCache(resultID.get(i), objectContext));
        }
        return resultList;
    }

    private ManagedObject loadToOneRelationshipValue(String fieldName, Object object, ObjectContext objectContext) {
        ObjectId id;
        Entity entity;
        if (fieldName == null || object == null || objectContext == null) {
            LOG.logE("Execute loadToOneRelationshipValue Failed : The input parameter is null.");
            throw new ODMFIllegalArgumentException("The input parameter is null.");
        }
        if (object instanceof ObjectId) {
            id = (ObjectId) object;
            entity = getModel().getEntity(id.getEntityName());
        } else if (object instanceof ManagedObject) {
            ManagedObject temp = (ManagedObject) object;
            id = temp.getObjectId();
            entity = getModel().getEntity(temp.getEntityName());
        } else {
            LOG.logE("Execute loadToOneRelationshipValue Failed : The related object neither a ObjectId or a ManagedObject.");
            throw new ODMFIllegalArgumentException("Execute loadToOneRelationshipValue Failed : The related object neither a ObjectId nor a ManagedObject.");
        }
        if (id == null || entity == null) {
            LOG.logE("Execute loadToOneRelationshipValue Failed : The id or the entity of the related object does not exist.");
            throw new ODMFIllegalArgumentException("Execute loadToOneRelationshipValue Failed : The id or the entity of the related object does not exist.");
        }
        Relationship relationship = entity.getRelationship(fieldName);
        if (relationship == null) {
            LOG.logE("Execute loadToOneRelationshipValue Failed : The relationship which the fieldName specified does not exist.");
            throw new ODMFIllegalArgumentException("Execute loadToOneRelationshipValue Failed : The relationship which the fieldName specified does not exist.");
        }
        List<ObjectId> resultIDs = getRelationshipObjectId(id, relationship);
        if (resultIDs.size() == 0) {
            return null;
        }
        ObjectId resultID = resultIDs.get(0);
        ManagedObject managedObject = PersistentStoreCoordinator.getDefault().getObjectFromCache(resultID);
        if (managedObject != null && (!managedObject.isDirty() || managedObject.getLastObjectContext() == objectContext)) {
            return managedObject;
        }
        ManagedObject managedObject2 = getObjectValues(resultID);
        if (managedObject2 == null) {
            return managedObject2;
        }
        managedObject2.setObjectContext(objectContext);
        managedObject2.setState(4);
        managedObject2.setRowId((Long) resultID.getId());
        PersistentStoreCoordinator.getDefault().putObjectIntoCache(managedObject2);
        return managedObject2;
    }

    private ManagedObject getManagedObjectWithCache(ObjectId objectId, ObjectContext objectContext) {
        ManagedObject managedObject = PersistentStoreCoordinator.getDefault().getObjectFromCache(objectId);
        if (managedObject == null) {
            managedObject = getObjectValues(objectId);
            if (managedObject != null) {
                managedObject.setObjectContext(objectContext);
                managedObject.setState(4);
                managedObject.setRowId((Long) objectId.getId());
                PersistentStoreCoordinator.getDefault().putObjectIntoCache(managedObject);
            }
        } else if (managedObject.isDirty() && managedObject.getLastObjectContext() != objectContext) {
            managedObject = getObjectValues(objectId);
            if (managedObject != null) {
                managedObject.setObjectContext(objectContext);
                managedObject.setState(4);
                managedObject.setRowId((Long) objectId.getId());
                PersistentStoreCoordinator.getDefault().putObjectIntoCache(managedObject);
            }
        }
        return managedObject;
    }

    /* access modifiers changed from: protected */
    public Cursor executeRawQuerySQL(String sql) {
        if (sql == null) {
            LOG.logE("Execute RawQuerySQL Failed : The sql is null.");
            throw new ODMFIllegalArgumentException("Execute RawQuerySQL Failed : The sql is null.");
        }
        try {
            return this.db.rawSelect(sql, null, null, false);
        } catch (IllegalStateException e) {
            LOG.logE("Execute rawQuerySQL failed : A IllegalStateException occurred when execute rawQuerySQL.");
            throw new ODMFRuntimeException("Execute rawQuerySQL failed : " + e.getMessage(), e);
        } catch (SQLiteDatabaseCorruptException | com.huawei.hwsqlite.SQLiteDatabaseCorruptException e2) {
            LOG.logE("Execute rawQuerySQL failed : A sqliteDatabase occurred when execute rawQuerySQL.");
            throw new ODMFSQLiteDatabaseCorruptException("Execute rawQuerySQL failed : " + e2.getMessage(), e2);
        } catch (SQLiteDiskIOException | com.huawei.hwsqlite.SQLiteDiskIOException e3) {
            LOG.logE("Execute rawQuerySQL failed : A disk io exception occurred when execute rawQuerySQL.");
            throw new ODMFSQLiteDiskIOException("Execute rawQuerySQL failed : " + e3.getMessage(), e3);
        } catch (SQLiteFullException | com.huawei.hwsqlite.SQLiteFullException e4) {
            LOG.logE("Execute rawQuerySQL failed : A disk full exception occurred when execute rawQuerySQL.");
            throw new ODMFSQLiteFullException("Execute rawQuerySQL failed : " + e4.getMessage(), e4);
        } catch (SQLiteAccessPermException | com.huawei.hwsqlite.SQLiteAccessPermException e5) {
            LOG.logE("Execute rawQuerySQL failed : An access permission exception occurred when execute rawQuerySQL.");
            throw new ODMFSQLiteAccessPermException("Execute rawQuerySQL failed : " + e5.getMessage(), e5);
        } catch (SQLiteCantOpenDatabaseException | com.huawei.hwsqlite.SQLiteCantOpenDatabaseException e6) {
            LOG.logE("Execute rawQuerySQL failed : An cant open exception occurred when execute rawQuerySQL.");
            throw new ODMFSQLiteCantOpenDatabaseException("Execute rawQuerySQL failed : " + e6.getMessage(), e6);
        } catch (SQLiteException | com.huawei.hwsqlite.SQLiteException e7) {
            LOG.logE("Execute rawQuerySQL failed : A SQLite exception occurred when execute rawQuerySQL.");
            throw new ODMFRuntimeException("Execute rawQuerySQL failed : " + e7.getMessage(), e7);
        } catch (Exception e8) {
            LOG.logE("Execute rawQuerySQL failed : A unknown exception occurred when execute rawQuerySQL.");
            throw new ODMFRuntimeException("Execute rawQuerySQL failed : " + e8.getMessage(), e8);
        }
    }

    /* access modifiers changed from: protected */
    public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        try {
            return DatabaseQueryService.query(this.db, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        } catch (IllegalStateException e) {
            LOG.logE("Execute query failed : A IllegalStateException occurred when execute query.");
            throw new ODMFRuntimeException("Execute query failed : " + e.getMessage(), e);
        } catch (SQLiteDatabaseCorruptException | com.huawei.hwsqlite.SQLiteDatabaseCorruptException e2) {
            LOG.logE("Execute query failed : A sqliteDatabase corrupt exception occurred when execute query.");
            throw new ODMFSQLiteDatabaseCorruptException("Execute query failed : " + e2.getMessage(), e2);
        } catch (SQLiteDiskIOException | com.huawei.hwsqlite.SQLiteDiskIOException e3) {
            LOG.logE("Execute query failed : A disk io exception occurred when execute query.");
            throw new ODMFSQLiteDiskIOException("Execute query failed : " + e3.getMessage(), e3);
        } catch (SQLiteFullException | com.huawei.hwsqlite.SQLiteFullException e4) {
            LOG.logE("Execute query failed : A disk full exception occurred when execute query.");
            throw new ODMFSQLiteFullException("Execute query failed : " + e4.getMessage(), e4);
        } catch (SQLiteAccessPermException | com.huawei.hwsqlite.SQLiteAccessPermException e5) {
            LOG.logE("Execute query failed : An access permission exception occurred when execute query.");
            throw new ODMFSQLiteAccessPermException("Execute query failed : " + e5.getMessage(), e5);
        } catch (SQLiteCantOpenDatabaseException | com.huawei.hwsqlite.SQLiteCantOpenDatabaseException e6) {
            LOG.logE("Execute query failed : An cant open exception occurred when execute query.");
            throw new ODMFSQLiteCantOpenDatabaseException("Execute query failed : " + e6.getMessage(), e6);
        } catch (SQLiteException | com.huawei.hwsqlite.SQLiteException e7) {
            LOG.logE("Execute query failed : A SQLite exception occurred when execute query.");
            throw new ODMFRuntimeException("Execute query failed : " + e7.getMessage(), e7);
        } catch (Exception e8) {
            LOG.logE("Execute query failed : A unknown exception occurred when execute query.");
            throw new ODMFRuntimeException("Execute query failed : " + e8.getMessage(), e8);
        }
    }

    /* access modifiers changed from: protected */
    public void executeRawSQL(String sql) {
        if (sql == null) {
            LOG.logE("Execute RawSQL Failed : The sql is null.");
            throw new ODMFIllegalArgumentException("Execute RawSQL Failed : The sql is null.");
        }
        try {
            this.db.execSQL(sql);
        } catch (IllegalStateException e) {
            LOG.logE("Execute rawSQL failed : A IllegalStateException occurred when execute rawSQL.");
            throw new ODMFRuntimeException("Execute rawSQL failed : " + e.getMessage(), e);
        } catch (SQLiteDatabaseCorruptException | com.huawei.hwsqlite.SQLiteDatabaseCorruptException e2) {
            LOG.logE("Execute rawSQL failed : A sqliteDatabase corrupt exception occurred when execute rawSQL.");
            throw new ODMFSQLiteDatabaseCorruptException("Execute rawSQL failed : " + e2.getMessage(), e2);
        } catch (SQLiteDiskIOException | com.huawei.hwsqlite.SQLiteDiskIOException e3) {
            LOG.logE("Execute rawSQL failed: A disk io exception occurred when execute rawSQL.");
            throw new ODMFSQLiteDiskIOException("Execute rawSQL failed : " + e3.getMessage(), e3);
        } catch (SQLiteFullException | com.huawei.hwsqlite.SQLiteFullException e4) {
            LOG.logE("Execute rawSQL failed : A disk full exception occurred when execute rawSQL.");
            throw new ODMFSQLiteFullException("Execute rawSQL failed : " + e4.getMessage(), e4);
        } catch (SQLiteAccessPermException | com.huawei.hwsqlite.SQLiteAccessPermException e5) {
            LOG.logE("Execute rawSQL failed : An access permission exception occurred when execute rawSQL.");
            throw new ODMFSQLiteAccessPermException("Execute rawSQL failed : " + e5.getMessage(), e5);
        } catch (SQLiteCantOpenDatabaseException | com.huawei.hwsqlite.SQLiteCantOpenDatabaseException e6) {
            LOG.logE("Execute rawSQL failed : An cant open exception occurred when execute rawSQL.");
            throw new ODMFSQLiteCantOpenDatabaseException("Execute rawSQL failed : " + e6.getMessage(), e6);
        } catch (SQLiteException | com.huawei.hwsqlite.SQLiteException e7) {
            LOG.logE("Execute rawSQL failed : A SQLite exception occurred when execute rawSQL.");
            throw new ODMFRuntimeException("Execute rawSQL failed : " + e7.getMessage(), e7);
        } catch (Exception e8) {
            LOG.logE("Execute rawSQL failed : A unknown exception occurred when execute rawSQL.");
            throw new ODMFRuntimeException("Execute rawSQL failed : " + e8.getMessage(), e8);
        }
    }

    /* access modifiers changed from: protected */
    public void setDBVersions(String dbVersion, int dbVersionCode) {
        this.databaseHelper.setDatabaseVersions(this.db, dbVersion, dbVersionCode);
    }

    /* access modifiers changed from: protected */
    public void setEntityVersions(String tableName, String entityVersion, int entityVersionCode) {
        this.databaseHelper.setEntityVersions(this.db, tableName, entityVersion, entityVersionCode);
    }

    public void insertDataForMigration(String tableName, ContentValues values) {
        if (tableName == null || values == null) {
            LOG.logE("Execute insertData Failed : The tableName or the values is null.");
            throw new ODMFIllegalArgumentException("The tableName or the values is null.");
        }
        try {
            this.db.insertOrThrow(tableName, null, values);
        } catch (SQLiteException e) {
            LOG.logE("Execute insertData Failed : A SQLiteException occurred when insertData.");
            throw new ODMFRuntimeException("Execute insertData Failed : " + e.getMessage());
        }
    }

    public void exportDatabase(String newDBName, byte[] newKey) {
        String newDBPath;
        byte[] key;
        if (newDBName.startsWith("/")) {
            newDBPath = newDBName;
            newDBName = new File(newDBPath).getName();
        } else {
            newDBPath = new File(this.db.getPath()).getParent() + "/" + newDBName;
        }
        this.context.deleteDatabase(newDBPath);
        String export = String.format("SELECT sqlcipher_export('%s');", new Object[]{newDBName});
        if (newKey == null || newKey.length == 0) {
            key = new byte[0];
        } else {
            key = newKey;
        }
        Cursor cursor = null;
        try {
            this.db.addAttachAlias(newDBName, newDBPath, key);
            cursor = executeRawQuerySQL(export);
            cursor.moveToNext();
            this.db.removeAttachAlias(newDBName);
            LOG.logI("Database " + this.path + " is exported to " + newDBName + ".");
            clearKey(key);
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            LOG.logE("Execute exportDatabase Failed : A exception occurred when export database.");
            throw new ODMFRuntimeException("Execute exportDatabase Failed : " + e.toString());
        } catch (Throwable th) {
            clearKey(key);
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public void resetMetadata() {
        this.databaseHelper.resetMetadata(this.db);
        loadMetadata();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }
}
