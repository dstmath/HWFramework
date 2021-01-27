package com.huawei.odmf.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.CrossProcessCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.SQLException;
import android.database.StaleDataException;
import com.huawei.hwsqlite.SQLiteAccessPermException;
import com.huawei.hwsqlite.SQLiteCantOpenDatabaseException;
import com.huawei.hwsqlite.SQLiteDatabaseCorruptException;
import com.huawei.hwsqlite.SQLiteDiskIOException;
import com.huawei.hwsqlite.SQLiteException;
import com.huawei.hwsqlite.SQLiteFullException;
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
import com.huawei.odmf.utils.StringUtil;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/* access modifiers changed from: package-private */
public class AndroidSqlPersistentStore extends PersistentStore {
    private Context context;
    private DatabaseHelper databaseHelper;
    protected DataBase db;
    private Map<String, AEntityHelper> helperMap;
    private RelationshipLoader relationshipLoader;
    private final Object statementLock;
    private String uriString;

    AndroidSqlPersistentStore(Context context2, String str, String str2, Configuration configuration) {
        this(context2, str, str2, configuration, null);
    }

    AndroidSqlPersistentStore(Context context2, String str, String str2, Configuration configuration, byte[] bArr) {
        super(configuration.getPath(), configuration.getDatabaseType(), configuration.getStorageMode(), str2);
        this.statementLock = new Object();
        try {
            setModel(ObjectModelFactory.parse(context2, str));
            this.context = context2;
            String path = configuration.getPath();
            if (path == null || path.equals("")) {
                path = getModel().getDatabaseName();
                setPath(path);
            }
            init(configuration.getStorageMode() == 401 ? null : path, bArr, configuration.isThrowException(), configuration.isDetectDelete());
            this.uriString = str2;
        } catch (ODMFIllegalArgumentException | ODMFXmlParserException e) {
            LOG.logE("Create AndroidSqlPersistentStore failed : parser objectModel failed.");
            throw new ODMFRuntimeException("Xml parser failed : " + e.getMessage());
        }
    }

    private void init(String str, byte[] bArr, boolean z, boolean z2) {
        SQLiteDatabaseCorruptException e;
        SQLiteDiskIOException e2;
        SQLiteFullException e3;
        SQLiteAccessPermException e4;
        SQLiteCantOpenDatabaseException e5;
        SQLiteException e6;
        try {
            if (getDatabaseType() == 302) {
                this.databaseHelper = new AndroidDatabaseHelper(this.context, str, getModel());
                this.db = new AndroidSQLiteDatabase(((AndroidDatabaseHelper) this.databaseHelper).getWritableDatabase());
            } else if (getDatabaseType() == 301) {
                this.databaseHelper = new ODMFDatabaseHelper(this.context, str, getModel(), z, z2);
                if (bArr != null && bArr.length > 0) {
                    this.databaseHelper.setDatabaseEncrypted(bArr);
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
            if (bArr != null && bArr.length > 0) {
                for (int i = 0; i < bArr.length; i++) {
                    bArr[i] = 0;
                }
            }
        } catch (android.database.sqlite.SQLiteDatabaseCorruptException e7) {
            e = e7;
            LOG.logE("Init database failed : A sqliteDatabase corrupt exception occurred when initializing database.");
            throw new ODMFSQLiteDatabaseCorruptException("Init database failed : " + e.getMessage(), e);
        } catch (SQLiteDatabaseCorruptException e8) {
            e = e8;
            LOG.logE("Init database failed : A sqliteDatabase corrupt exception occurred when initializing database.");
            throw new ODMFSQLiteDatabaseCorruptException("Init database failed : " + e.getMessage(), e);
        } catch (ODMFSQLiteDatabaseCorruptException e9) {
            e = e9;
            LOG.logE("Init database failed : A sqliteDatabase corrupt exception occurred when initializing database.");
            throw new ODMFSQLiteDatabaseCorruptException("Init database failed : " + e.getMessage(), e);
        } catch (android.database.sqlite.SQLiteDiskIOException e10) {
            e2 = e10;
            LOG.logE("Init database failed : A disk io exception occurred when initializing database.");
            throw new ODMFSQLiteDiskIOException("Init database failed : " + e2.getMessage(), e2);
        } catch (SQLiteDiskIOException e11) {
            e2 = e11;
            LOG.logE("Init database failed : A disk io exception occurred when initializing database.");
            throw new ODMFSQLiteDiskIOException("Init database failed : " + e2.getMessage(), e2);
        } catch (ODMFSQLiteDiskIOException e12) {
            e2 = e12;
            LOG.logE("Init database failed : A disk io exception occurred when initializing database.");
            throw new ODMFSQLiteDiskIOException("Init database failed : " + e2.getMessage(), e2);
        } catch (android.database.sqlite.SQLiteFullException e13) {
            e3 = e13;
            LOG.logE("Init database failed : A disk full exception occurred when initializing database.");
            throw new ODMFSQLiteFullException("Init database failed : " + e3.getMessage(), e3);
        } catch (SQLiteFullException e14) {
            e3 = e14;
            LOG.logE("Init database failed : A disk full exception occurred when initializing database.");
            throw new ODMFSQLiteFullException("Init database failed : " + e3.getMessage(), e3);
        } catch (android.database.sqlite.SQLiteAccessPermException e15) {
            e4 = e15;
            LOG.logE("Init database failed : An access permission exception occurred when initializing database.");
            throw new ODMFSQLiteAccessPermException("Init database failed : " + e4.getMessage(), e4);
        } catch (SQLiteAccessPermException e16) {
            e4 = e16;
            LOG.logE("Init database failed : An access permission exception occurred when initializing database.");
            throw new ODMFSQLiteAccessPermException("Init database failed : " + e4.getMessage(), e4);
        } catch (android.database.sqlite.SQLiteCantOpenDatabaseException e17) {
            e5 = e17;
            LOG.logE("Init database failed : An cant open exception occurred when initializing database.");
            throw new ODMFSQLiteCantOpenDatabaseException("Init database failed : " + e5.getMessage(), e5);
        } catch (SQLiteCantOpenDatabaseException e18) {
            e5 = e18;
            LOG.logE("Init database failed : An cant open exception occurred when initializing database.");
            throw new ODMFSQLiteCantOpenDatabaseException("Init database failed : " + e5.getMessage(), e5);
        } catch (android.database.sqlite.SQLiteException e19) {
            e6 = e19;
            LOG.logE("Init database failed : A SQLite exception occurred when initializing database.");
            throw new ODMFRuntimeException("Init database failed : " + e6.getMessage(), e6);
        } catch (SQLiteException e20) {
            e6 = e20;
            LOG.logE("Init database failed : A SQLite exception occurred when initializing database.");
            throw new ODMFRuntimeException("Init database failed : " + e6.getMessage(), e6);
        } catch (Exception e21) {
            LOG.logE("Init database failed : A unknown exception occurred when initializing database.");
            throw new ODMFRuntimeException("Init database failed : " + e21.getMessage(), e21);
        } catch (Throwable th) {
            if (bArr != null && bArr.length > 0) {
                for (int i2 = 0; i2 < bArr.length; i2++) {
                    bArr[i2] = 0;
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
        for (String str : getModel().getEntities().keySet()) {
            String tableName = ((Entity) getModel().getEntities().get(str)).getTableName();
            setMetadata(tableName + "_version", this.databaseHelper.getEntityVersion(this.db, tableName));
            setMetadata(tableName + "_versionCode", Integer.valueOf(this.databaseHelper.getEntityVersionCode(this.db, tableName)));
        }
    }

    private void initHelper(ObjectModel objectModel) {
        if (objectModel != null) {
            Map<String, ? extends Entity> entities = objectModel.getEntities();
            if (entities != null) {
                for (Map.Entry<String, ? extends Entity> entry : entities.entrySet()) {
                    String key = entry.getKey();
                    try {
                        AEntityHelper aEntityHelper = (AEntityHelper) Class.forName(key + "Helper").getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
                        aEntityHelper.setEntity((Entity) entry.getValue());
                        this.helperMap.put(key, aEntityHelper);
                    } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        LOG.logE("Execute initHelper failed : An exception occurred when use reflection to get helper class.");
                        throw new ODMFRuntimeException("Execute initHelper failed : " + e.getMessage());
                    }
                }
                return;
            }
            LOG.logE("Execute initHelper failed : the entities is null.");
            throw new ODMFIllegalStateException("Execute initHelper failed : the entities in model is null");
        }
        LOG.logE("Execute initHelper failed : the objectModel is null.");
        throw new ODMFIllegalArgumentException("Execute initHelper failed : the model is null");
    }

    private void overwriteAllJoinClause(FetchRequest fetchRequest) {
        String sb = fetchRequest.getJoinClause().toString();
        StringBuilder sb2 = new StringBuilder();
        String[] split = sb.split("\\s+");
        for (int i = 0; i < split.length; i++) {
            if (split[i].contains("=")) {
                String[] split2 = split[i].split("=");
                split = overwriteJoinClause(split, i, split2[0], split2[1]);
            }
        }
        for (String str : split) {
            sb2.append(" ");
            sb2.append(str);
        }
        fetchRequest.setJoinClause(sb2);
    }

    private String[] overwriteJoinClause(String[] strArr, int i, String str, String str2) {
        String[] strArr2 = (String[]) Arrays.copyOf(strArr, strArr.length);
        Entity entity = getModel().getEntity(str);
        String tableName = entity.getTableName();
        Entity entity2 = getModel().getEntity(str2);
        String tableName2 = entity2.getTableName();
        for (Relationship relationship : getModel().getEntity(str).getRelationships()) {
            if (relationship.getRelatedEntity().getEntityName().equals(str2)) {
                int relationShipType = relationship.getRelationShipType();
                if (relationShipType == 0) {
                    String manyToManyMidTableName = DatabaseTableHelper.getManyToManyMidTableName(relationship);
                    strArr2[i - 2] = manyToManyMidTableName;
                    String relationshipColumnName = DatabaseTableHelper.getRelationshipColumnName(entity);
                    String relationshipColumnName2 = DatabaseTableHelper.getRelationshipColumnName(entity2);
                    strArr2[i] = tableName + "." + DatabaseQueryService.getRowidColumnName() + " = " + manyToManyMidTableName + "." + relationshipColumnName + " INNER JOIN " + tableName2 + " ON " + manyToManyMidTableName + "." + relationshipColumnName2 + " = " + tableName2 + "." + DatabaseQueryService.getRowidColumnName();
                } else if (relationShipType == 2) {
                    strArr2[i] = overwriteJoinClauseByManyToOne(relationship, tableName, tableName2);
                } else if (relationShipType == 4) {
                    strArr2[i] = overwriteJoinClauseByOneToMany(relationship, tableName, tableName2);
                } else if (relationShipType == 6) {
                    strArr2[i] = overwriteJoinClauseByOneToOne(relationship, tableName, tableName2);
                } else {
                    LOG.logE("Execute overwriteJoinClause failed : the relation type is wrong.");
                    throw new ODMFIllegalStateException("Execute overwriteJoinClause failed : No such relationship type.");
                }
            }
        }
        return strArr2;
    }

    private String overwriteJoinClauseByOneToOne(Relationship relationship, String str, String str2) {
        if (relationship.isMajor()) {
            return str + "." + relationship.getFieldName() + " = " + str2 + "." + relationship.getRelatedColumnName();
        }
        return str + "." + relationship.getInverseRelationship().getRelatedColumnName() + " = " + str2 + "." + relationship.getInverseRelationship().getFieldName();
    }

    private String overwriteJoinClauseByOneToMany(Relationship relationship, String str, String str2) {
        return str + "." + relationship.getInverseRelationship().getRelatedColumnName() + " = " + str2 + "." + relationship.getInverseRelationship().getFieldName();
    }

    private String overwriteJoinClauseByManyToOne(Relationship relationship, String str, String str2) {
        return str + "." + relationship.getFieldName() + " = " + str2 + "." + relationship.getRelatedColumnName();
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public <T extends ManagedObject> List<T> executeFetchRequest(FetchRequest fetchRequest, ObjectContext objectContext) {
        if (fetchRequest == null) {
            LOG.logE("Execute FetchRequest failed : the relation type is wrong.");
            throw new ODMFIllegalArgumentException("Execute FetchRequest failed : The parameter request is null.");
        } else if (getModel().getEntity(fetchRequest.getEntityName()) != null) {
            AEntityHelper helper = getHelper(fetchRequest.getEntityName());
            String tableName = helper.getEntity().getTableName();
            overwriteAllJoinClause(fetchRequest);
            return doObjectQuery(tableName + fetchRequest.getJoinClause().toString(), fetchRequest, helper, objectContext);
        } else {
            LOG.logE("Execute FetchRequest failed : The entity which the entityName specified is not in the model.");
            throw new ODMFIllegalArgumentException("Execute FetchRequest failed : The entity which the entityName specified is not in the model.");
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:1:0x0003 */
    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: java.lang.String */
    /* JADX DEBUG: Multi-variable search result rejected for r5v12, resolved type: java.lang.String */
    /* JADX DEBUG: Multi-variable search result rejected for r5v13, resolved type: java.lang.String */
    /* JADX DEBUG: Multi-variable search result rejected for r5v25, resolved type: java.lang.String */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r5v2 */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x01d7  */
    private <T extends ManagedObject> List<T> doObjectQuery(String str, FetchRequest fetchRequest, AEntityHelper aEntityHelper, ObjectContext objectContext) {
        Throwable th;
        Cursor cursor;
        StaleDataException e;
        IllegalStateException e2;
        IllegalArgumentException e3;
        android.database.sqlite.SQLiteDatabaseCorruptException e4;
        android.database.sqlite.SQLiteDiskIOException e5;
        android.database.sqlite.SQLiteFullException e6;
        android.database.sqlite.SQLiteAccessPermException e7;
        android.database.sqlite.SQLiteCantOpenDatabaseException e8;
        android.database.sqlite.SQLiteException e9;
        Exception e10;
        Cursor cursor2 = null;
        try {
            Cursor query = DatabaseQueryService.query(this.db, str, fetchRequest);
            try {
                Cursor handleCursor = handleCursor(query);
                if (!handleCursor.moveToFirst()) {
                    ArrayList arrayList = new ArrayList();
                    if (query != null) {
                        query.close();
                    }
                    if (!(handleCursor == null || handleCursor == query)) {
                        handleCursor.close();
                    }
                    return arrayList;
                }
                List<T> loadManagedObjectsFromCursor = loadManagedObjectsFromCursor(aEntityHelper, handleCursor, objectContext);
                if (query != null) {
                    query.close();
                }
                if (!(handleCursor == null || handleCursor == query)) {
                    handleCursor.close();
                }
                return loadManagedObjectsFromCursor;
            } catch (StaleDataException e11) {
                e = e11;
                LOG.logE("Execute FetchRequest failed : A StaleDataException occurred when query");
                throw new ODMFRuntimeException("Execute FetchRequest failed : " + e.getMessage(), e);
            } catch (IllegalStateException e12) {
                e2 = e12;
                LOG.logE("Execute FetchRequest failed : A IllegalStateException occurred when query");
                throw new ODMFRuntimeException("Execute FetchRequest failed : " + e2.getMessage(), e2);
            } catch (IllegalArgumentException e13) {
                e3 = e13;
                LOG.logE("Execute FetchRequest failed : A IllegalArgumentException occurred when query");
                throw new ODMFRuntimeException("Execute FetchRequest failed : " + e3.getMessage(), e3);
            } catch (android.database.sqlite.SQLiteDatabaseCorruptException | SQLiteDatabaseCorruptException e14) {
                e4 = e14;
                LOG.logE("Execute FetchRequest failed : A SQLiteDatabaseCorruptException occurred when query");
                throw new ODMFSQLiteDatabaseCorruptException("Execute FetchRequest failed : " + e4.getMessage(), e4);
            } catch (android.database.sqlite.SQLiteDiskIOException | SQLiteDiskIOException e15) {
                e5 = e15;
                LOG.logE("Execute FetchRequest failed : A SQLiteDiskIOException occurred when query");
                throw new ODMFSQLiteDiskIOException("Execute FetchRequest failed : " + e5.getMessage(), e5);
            } catch (android.database.sqlite.SQLiteFullException | SQLiteFullException e16) {
                e6 = e16;
                LOG.logE("Execute FetchRequest failed : A SQLiteFullException occurred when query");
                throw new ODMFSQLiteFullException("Execute FetchRequest failed : " + e6.getMessage(), e6);
            } catch (android.database.sqlite.SQLiteAccessPermException | SQLiteAccessPermException e17) {
                e7 = e17;
                LOG.logE("Execute FetchRequest failed : A SQLiteAccessPermException occurred when query");
                throw new ODMFSQLiteAccessPermException("Execute FetchRequest failed : " + e7.getMessage(), e7);
            } catch (android.database.sqlite.SQLiteCantOpenDatabaseException | SQLiteCantOpenDatabaseException e18) {
                e8 = e18;
                LOG.logE("Execute FetchRequest failed : A SQLiteCantOpenDatabaseException occurred when query");
                throw new ODMFSQLiteCantOpenDatabaseException("Execute FetchRequest failed : " + e8.getMessage(), e8);
            } catch (android.database.sqlite.SQLiteException | SQLiteException e19) {
                e9 = e19;
                LOG.logE("Execute FetchRequest failed : A SQLiteException occurred when query");
                throw new ODMFRuntimeException("Execute FetchRequest failed : " + e9.getMessage(), e9);
            } catch (Exception e20) {
                e10 = e20;
                cursor2 = query;
                str = 0;
                LOG.logE("Execute FetchRequest failed : A unknown exception occurred when query");
                throw new ODMFRuntimeException("Execute FetchRequest failed : " + e10.getMessage(), e10);
            } catch (Throwable th2) {
                th = th2;
                cursor2 = query;
                cursor = null;
                if (cursor2 != null) {
                }
                cursor.close();
                throw th;
            }
        } catch (StaleDataException e21) {
            e = e21;
            LOG.logE("Execute FetchRequest failed : A StaleDataException occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e.getMessage(), e);
        } catch (IllegalStateException e22) {
            e2 = e22;
            LOG.logE("Execute FetchRequest failed : A IllegalStateException occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e2.getMessage(), e2);
        } catch (IllegalArgumentException e23) {
            e3 = e23;
            LOG.logE("Execute FetchRequest failed : A IllegalArgumentException occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e3.getMessage(), e3);
        } catch (android.database.sqlite.SQLiteDatabaseCorruptException e24) {
            e4 = e24;
            LOG.logE("Execute FetchRequest failed : A SQLiteDatabaseCorruptException occurred when query");
            throw new ODMFSQLiteDatabaseCorruptException("Execute FetchRequest failed : " + e4.getMessage(), e4);
        } catch (SQLiteDatabaseCorruptException e25) {
            e4 = e25;
            LOG.logE("Execute FetchRequest failed : A SQLiteDatabaseCorruptException occurred when query");
            throw new ODMFSQLiteDatabaseCorruptException("Execute FetchRequest failed : " + e4.getMessage(), e4);
        } catch (android.database.sqlite.SQLiteDiskIOException e26) {
            e5 = e26;
            LOG.logE("Execute FetchRequest failed : A SQLiteDiskIOException occurred when query");
            throw new ODMFSQLiteDiskIOException("Execute FetchRequest failed : " + e5.getMessage(), e5);
        } catch (SQLiteDiskIOException e27) {
            e5 = e27;
            LOG.logE("Execute FetchRequest failed : A SQLiteDiskIOException occurred when query");
            throw new ODMFSQLiteDiskIOException("Execute FetchRequest failed : " + e5.getMessage(), e5);
        } catch (android.database.sqlite.SQLiteFullException e28) {
            e6 = e28;
            LOG.logE("Execute FetchRequest failed : A SQLiteFullException occurred when query");
            throw new ODMFSQLiteFullException("Execute FetchRequest failed : " + e6.getMessage(), e6);
        } catch (SQLiteFullException e29) {
            e6 = e29;
            LOG.logE("Execute FetchRequest failed : A SQLiteFullException occurred when query");
            throw new ODMFSQLiteFullException("Execute FetchRequest failed : " + e6.getMessage(), e6);
        } catch (android.database.sqlite.SQLiteAccessPermException e30) {
            e7 = e30;
            LOG.logE("Execute FetchRequest failed : A SQLiteAccessPermException occurred when query");
            throw new ODMFSQLiteAccessPermException("Execute FetchRequest failed : " + e7.getMessage(), e7);
        } catch (SQLiteAccessPermException e31) {
            e7 = e31;
            LOG.logE("Execute FetchRequest failed : A SQLiteAccessPermException occurred when query");
            throw new ODMFSQLiteAccessPermException("Execute FetchRequest failed : " + e7.getMessage(), e7);
        } catch (android.database.sqlite.SQLiteCantOpenDatabaseException e32) {
            e8 = e32;
            LOG.logE("Execute FetchRequest failed : A SQLiteCantOpenDatabaseException occurred when query");
            throw new ODMFSQLiteCantOpenDatabaseException("Execute FetchRequest failed : " + e8.getMessage(), e8);
        } catch (SQLiteCantOpenDatabaseException e33) {
            e8 = e33;
            LOG.logE("Execute FetchRequest failed : A SQLiteCantOpenDatabaseException occurred when query");
            throw new ODMFSQLiteCantOpenDatabaseException("Execute FetchRequest failed : " + e8.getMessage(), e8);
        } catch (android.database.sqlite.SQLiteException e34) {
            e9 = e34;
            LOG.logE("Execute FetchRequest failed : A SQLiteException occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e9.getMessage(), e9);
        } catch (SQLiteException e35) {
            e9 = e35;
            LOG.logE("Execute FetchRequest failed : A SQLiteException occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e9.getMessage(), e9);
        } catch (Exception e36) {
            e10 = e36;
            str = 0;
            LOG.logE("Execute FetchRequest failed : A unknown exception occurred when query");
            throw new ODMFRuntimeException("Execute FetchRequest failed : " + e10.getMessage(), e10);
        } catch (Throwable th3) {
            th = th3;
            cursor = str;
            if (cursor2 != null) {
            }
            cursor.close();
            throw th;
        }
    }

    private Cursor handleCursor(Cursor cursor) {
        int count;
        CursorWindow window;
        if (getDatabaseType() != 302 || (count = cursor.getCount()) == 0 || !(cursor instanceof CrossProcessCursor) || (window = ((CrossProcessCursor) cursor).getWindow()) == null) {
            return cursor;
        }
        if (window.getNumRows() == count) {
            return new FastCursor(window);
        }
        LOG.logD("Window vs. result size: " + window.getNumRows() + "/" + count);
        return cursor;
    }

    private <T extends ManagedObject> List<T> loadManagedObjectsFromCursor(AEntityHelper aEntityHelper, Cursor cursor, ObjectContext objectContext) {
        ArrayList arrayList = new ArrayList();
        boolean isOpenObjectCache = CacheConfig.getDefault().isOpenObjectCache();
        do {
            arrayList.add(loadManagedObjectFromCursor(aEntityHelper, cursor, objectContext, isOpenObjectCache));
        } while (cursor.moveToNext());
        return arrayList;
    }

    private <T extends ManagedObject> T loadManagedObjectFromCursor(AEntityHelper aEntityHelper, Cursor cursor, ObjectContext objectContext, boolean z) {
        long j = cursor.getLong(DatabaseQueryService.getOdmfRowidIndex());
        if (z) {
            T t = (T) PersistentStoreCoordinator.getDefault().getObjectsCache().get(new AObjectId(aEntityHelper.getEntity().getEntityName(), Long.valueOf(j), getUriString()));
            if (t != null && (!t.isDirty() || t.getLastObjectContext() == objectContext)) {
                return t;
            }
        }
        T t2 = (T) ((ManagedObject) aEntityHelper.readObject(cursor, 0));
        t2.setRowId(Long.valueOf(j));
        t2.setState(4);
        t2.setObjectContext(objectContext);
        t2.setUriString(this.uriString);
        return t2;
    }

    private AEntityHelper getHelper(String str) {
        return this.helperMap.get(str);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public List<ObjectId> executeFetchRequestGetObjectId(FetchRequest fetchRequest) {
        if (fetchRequest != null) {
            ArrayList arrayList = new ArrayList();
            Entity entity = getModel().getEntity(fetchRequest.getEntityName());
            if (entity != null) {
                String tableName = entity.getTableName();
                overwriteAllJoinClause(fetchRequest);
                Cursor cursor = null;
                try {
                    Cursor queryRowId = DatabaseQueryService.queryRowId(this.db, tableName + fetchRequest.getJoinClause().toString(), fetchRequest);
                    while (queryRowId.moveToNext()) {
                        arrayList.add(createObjectId(entity, Long.valueOf(queryRowId.getLong(DatabaseQueryService.getOdmfRowidIndex()))));
                    }
                    if (queryRowId != null) {
                        queryRowId.close();
                    }
                    return arrayList;
                } catch (android.database.sqlite.SQLiteException e) {
                    LOG.logE("Execute FetchRequestGetObjectID failed : A SQLiteException occurred when query");
                    throw new ODMFRuntimeException("Execute FetchRequestGetObjectID failed : " + e.getMessage());
                } catch (StaleDataException e2) {
                    LOG.logE("Execute FetchRequestGetObjectID failed : A StaleDataException occurred when query");
                    throw new ODMFRuntimeException("Execute FetchRequestGetObjectID failed : " + e2.getMessage());
                } catch (Throwable th) {
                    if (0 != 0) {
                        cursor.close();
                    }
                    throw th;
                }
            } else {
                LOG.logE("Execute FetchRequestGetObjectID failed : The entity which the entityName specified is not in the model.");
                throw new ODMFIllegalArgumentException("Execute FetchRequestGetObjectID failed : The entity which the entityName specified is not in the model");
            }
        } else {
            LOG.logE("Execute FetchRequestGetObjectID failed : The parameter request is null.");
            throw new ODMFIllegalArgumentException("The parameter request is null.");
        }
    }

    @Override // com.huawei.odmf.core.PersistentStore
    public List<Object> executeFetchRequestWithAggregateFunction(FetchRequest fetchRequest) {
        SQLiteDatabaseCorruptException e;
        SQLiteDiskIOException e2;
        if (fetchRequest == null) {
            LOG.logE("Execute FetchRequestWithAggregateFunction failed : The parameter request is null.");
            throw new ODMFIllegalArgumentException("The parameter request is null.");
        } else if (getModel().getEntity(fetchRequest.getEntityName()) != null) {
            Entity entity = getHelper(fetchRequest.getEntityName()).getEntity();
            String tableName = entity.getTableName();
            overwriteAllJoinClause(fetchRequest);
            String str = tableName + fetchRequest.getJoinClause().toString();
            String[] columns = fetchRequest.getColumns();
            int[] aggregateOp = fetchRequest.getAggregateOp();
            if (columns == null || aggregateOp == null) {
                LOG.logE("Execute FetchRequestWithAggregateFunction failed : the querying columns is null or aggregateOp is null.");
                throw new ODMFRuntimeException("Execute FetchRequestWithAggregateFunction failed : the querying columns is null or aggregateOp is null.");
            }
            Cursor cursor = null;
            ArrayList arrayList = new ArrayList();
            try {
                Cursor queryWithAggregateFunction = DatabaseQueryService.queryWithAggregateFunction(this.db, str, fetchRequest);
                queryWithAggregateFunction.moveToFirst();
                for (int i = 0; i < columns.length; i++) {
                    arrayList.add(CursorUtils.extractAggregateResult(queryWithAggregateFunction.getString(i), aggregateOp[i], entity.getAttribute(columns[i])));
                }
                if (queryWithAggregateFunction != null) {
                    queryWithAggregateFunction.close();
                }
                return arrayList;
            } catch (android.database.sqlite.SQLiteDatabaseCorruptException e3) {
                e = e3;
                LOG.logE("Execute fetchRequest with aggregateFunction failed : " + e.getMessage());
                throw new ODMFSQLiteDatabaseCorruptException("End Transaction failed : " + e.getMessage(), e);
            } catch (SQLiteDatabaseCorruptException e4) {
                e = e4;
                LOG.logE("Execute fetchRequest with aggregateFunction failed : " + e.getMessage());
                throw new ODMFSQLiteDatabaseCorruptException("End Transaction failed : " + e.getMessage(), e);
            } catch (android.database.sqlite.SQLiteDiskIOException e5) {
                e2 = e5;
                LOG.logE("Execute fetchRequest with aggregateFunction failed : " + e2.getMessage());
                throw new ODMFSQLiteDiskIOException("Execute fetchRequest with aggregateFunction failed : " + e2.getMessage(), e2);
            } catch (SQLiteDiskIOException e6) {
                e2 = e6;
                LOG.logE("Execute fetchRequest with aggregateFunction failed : " + e2.getMessage());
                throw new ODMFSQLiteDiskIOException("Execute fetchRequest with aggregateFunction failed : " + e2.getMessage(), e2);
            } catch (android.database.sqlite.SQLiteException | SQLiteException e7) {
                LOG.logE("Execute fetchRequest With aggregateFunction failed : A SQLiteException occurred when query.");
                throw new ODMFRuntimeException("Execute fetchRequest With aggregateFunction failed : " + e7.getMessage(), e7);
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
        } else {
            LOG.logE("Execute FetchRequestWithAggregateFunction failed : The entity which the entityName specified is not in the model.");
            throw new ODMFIllegalArgumentException("The entity which the entityName specified is not in the model.");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public Cursor executeFetchRequestGetCursor(FetchRequest fetchRequest) {
        if (fetchRequest != null) {
            String tableName = getEntity(fetchRequest.getEntityName()).getTableName();
            overwriteAllJoinClause(fetchRequest);
            try {
                return DatabaseQueryService.commonQuery(this.db, tableName + fetchRequest.getJoinClause().toString(), fetchRequest);
            } catch (StaleDataException e) {
                LOG.logE("Execute FetchRequest failed : A StaleDataException occurred when query");
                throw new ODMFRuntimeException("Execute FetchRequest failed : " + e.getMessage(), e);
            } catch (IllegalStateException e2) {
                LOG.logE("Execute FetchRequest failed : A IllegalStateException occurred when query");
                throw new ODMFRuntimeException("Execute FetchRequest failed : " + e2.getMessage(), e2);
            } catch (IllegalArgumentException e3) {
                LOG.logE("Execute FetchRequest failed : A IllegalArgumentException occurred when query");
                throw new ODMFRuntimeException("Execute FetchRequest failed : " + e3.getMessage(), e3);
            } catch (android.database.sqlite.SQLiteDatabaseCorruptException | SQLiteDatabaseCorruptException e4) {
                LOG.logE("Execute FetchRequest failed : A SQLiteDatabaseCorruptException occurred when query");
                throw new ODMFSQLiteDatabaseCorruptException("Execute FetchRequest failed : " + e4.getMessage(), e4);
            } catch (android.database.sqlite.SQLiteDiskIOException | SQLiteDiskIOException e5) {
                LOG.logE("Execute FetchRequest failed : A SQLiteDiskIOException occurred when query");
                throw new ODMFSQLiteDiskIOException("Execute FetchRequest failed : " + e5.getMessage(), e5);
            } catch (android.database.sqlite.SQLiteFullException | SQLiteFullException e6) {
                LOG.logE("Execute FetchRequest failed : A SQLiteFullException occurred when query");
                throw new ODMFSQLiteFullException("Execute FetchRequest failed : " + e6.getMessage(), e6);
            } catch (android.database.sqlite.SQLiteAccessPermException | SQLiteAccessPermException e7) {
                LOG.logE("Execute FetchRequest failed : A SQLiteAccessPermException occurred when query");
                throw new ODMFSQLiteAccessPermException("Execute FetchRequest failed : " + e7.getMessage(), e7);
            } catch (android.database.sqlite.SQLiteCantOpenDatabaseException | SQLiteCantOpenDatabaseException e8) {
                LOG.logE("Execute FetchRequest failed : A SQLiteCantOpenDatabaseException occurred when query");
                throw new ODMFSQLiteCantOpenDatabaseException("Execute FetchRequest failed : " + e8.getMessage(), e8);
            } catch (android.database.sqlite.SQLiteException | SQLiteException e9) {
                LOG.logE("Execute FetchRequest failed : A SQLiteException occurred when query");
                throw new ODMFRuntimeException("Execute FetchRequest failed : " + e9.getMessage(), e9);
            } catch (Exception e10) {
                LOG.logE("Execute FetchRequest failed : A unknown exception occurred when query");
                throw new ODMFRuntimeException("Execute FetchRequest failed : " + e10.getMessage(), e10);
            }
        } else {
            LOG.logE("Execute FetchRequestGetCursor failed : The parameter request is null.");
            throw new ODMFIllegalArgumentException("Execute FetchRequestGetCursor failed : The parameter request is null");
        }
    }

    private Entity getEntity(String str) {
        Entity entity = getModel().getEntity(str);
        if (entity != null) {
            return entity;
        }
        LOG.logE("Execute getEntity failed : The entity which the entityName specified is not in the model.");
        throw new ODMFIllegalArgumentException("Execute getEntity failed : The entity which the entityName specified is not in the model.");
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public void executeSaveRequestWithTransaction(SaveRequest saveRequest) {
        this.db.beginTransaction();
        try {
            executeSaveRequest(saveRequest);
            this.db.setTransactionSuccessful();
            increaseVersion();
        } finally {
            this.db.endTransaction();
        }
    }

    private boolean checkEntityHelper(AEntityHelper aEntityHelper) {
        Entity entity = aEntityHelper.getEntity();
        if (entity == null) {
            return false;
        }
        if (entity.getModel() == getModel()) {
            return true;
        }
        Entity entity2 = getModel().getEntity(entity.getEntityName());
        if (entity2 == null) {
            return false;
        }
        aEntityHelper.setEntity(entity2);
        return true;
    }

    private void executeInsert(List<ManagedObject> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ManagedObject managedObject = list.get(i);
            AEntityHelper helper = ((AManagedObject) managedObject).getHelper();
            if (checkEntityHelper(helper)) {
                Entity entity = helper.getEntity();
                Statement insertStatement = entity.getStatements().getInsertStatement(this.db, entity.getTableName(), entity.getAttributes());
                synchronized (this.statementLock) {
                    helper.bindValue(insertStatement, managedObject);
                    long executeInsert = insertStatement.executeInsert();
                    managedObject.setRowId(Long.valueOf(executeInsert));
                    managedObject.setUriString(this.uriString);
                    if (entity.isKeyAutoIncrement()) {
                        helper.setPrimaryKeyValue(managedObject, executeInsert);
                    }
                    insertStatement.clearBindings();
                }
                managedObject.setState(4);
            } else {
                throw new ODMFIllegalArgumentException("Execute SaveRequest failed : The object is incompatible with the ObjectContext.");
            }
        }
        this.relationshipLoader.handleRelationship(list);
    }

    private void executeUpdate(List<ManagedObject> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ManagedObject managedObject = list.get(i);
            long longValue = managedObject.getRowId().longValue();
            AEntityHelper helper = ((AManagedObject) managedObject).getHelper();
            if (checkEntityHelper(helper)) {
                Entity entity = helper.getEntity();
                Statement updateStatement = entity.getStatements().getUpdateStatement(this.db, entity.getTableName(), entity.getAttributes());
                synchronized (this.statementLock) {
                    helper.bindValue(updateStatement, managedObject);
                    updateStatement.bindLong(entity.getAttributes().size() + 1, longValue);
                    updateStatement.execute();
                    updateStatement.clearBindings();
                }
                managedObject.setState(4);
            } else {
                throw new ODMFIllegalArgumentException("Execute SaveRequest failed : The object is incompatible with the ObjectContext.");
            }
        }
        this.relationshipLoader.handleRelationship(list);
    }

    private void executeDelete(List<ManagedObject> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ManagedObject managedObject = list.get(i);
            AEntityHelper helper = ((AManagedObject) managedObject).getHelper();
            if (checkEntityHelper(helper)) {
                Entity entity = helper.getEntity();
                List<ManagedObject> list2 = null;
                if (!entity.getRelationships().isEmpty()) {
                    list2 = new ArrayList<>();
                    this.relationshipLoader.handleCascadeDelete(managedObject, list2);
                }
                Statement deleteStatement = entity.getStatements().getDeleteStatement(this.db, entity.getTableName(), entity.getAttributes());
                synchronized (this.statementLock) {
                    deleteStatement.bindLong(1, managedObject.getRowId().longValue());
                    deleteStatement.execute();
                    deleteStatement.clearBindings();
                }
                managedObject.setState(0);
                if (list2 != null && !list2.isEmpty()) {
                    deleteObjectFromSet(list2, managedObject);
                    deleteObjectSetFromSet(list2, list);
                    while (!list2.isEmpty()) {
                        List<ManagedObject> arrayList = new ArrayList<>(list2);
                        arrayList.addAll(list);
                        for (ManagedObject managedObject2 : list2) {
                            long longValue = managedObject2.getRowId().longValue();
                            Entity entity2 = ((AManagedObject) managedObject2).getHelper().getEntity();
                            this.relationshipLoader.handleCascadeDelete(managedObject2, arrayList);
                            Statement deleteStatement2 = entity2.getStatements().getDeleteStatement(this.db, entity2.getTableName(), entity2.getAttributes());
                            synchronized (this.statementLock) {
                                deleteStatement2.bindLong(1, longValue);
                                deleteStatement2.executeUpdateDelete();
                                deleteStatement2.clearBindings();
                            }
                        }
                        deleteObjectSetFromSet(arrayList, list2);
                        deleteObjectSetFromSet(arrayList, list);
                        list2.clear();
                        list2.addAll(arrayList);
                    }
                    continue;
                }
            } else {
                throw new ODMFIllegalArgumentException("Execute SaveRequest failed : The object is incompatible with the ObjectContext.");
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public void executeSaveRequest(SaveRequest saveRequest) {
        if (saveRequest != null) {
            try {
                executeInsert(saveRequest.getInsertedObjects());
                executeUpdate(saveRequest.getUpdatedObjects());
                executeDelete(saveRequest.getDeletedObjects());
            } catch (android.database.sqlite.SQLiteDatabaseCorruptException | SQLiteDatabaseCorruptException e) {
                LOG.logE("Execute SaveRequest failed : A SQLiteDatabaseCorruptException occurred when save");
                throw new ODMFSQLiteDatabaseCorruptException("Execute SaveRequest failed : " + e.getMessage(), e);
            } catch (android.database.sqlite.SQLiteDiskIOException | SQLiteDiskIOException e2) {
                LOG.logE("Execute SaveRequest failed : A SQLiteDiskIOException occurred when save");
                throw new ODMFSQLiteDiskIOException("Execute SaveRequest failed : " + e2.getMessage(), e2);
            } catch (android.database.sqlite.SQLiteFullException | SQLiteFullException e3) {
                LOG.logE("Execute SaveRequest failed : A SQLiteFullException occurred when save");
                throw new ODMFSQLiteFullException("Execute SaveRequest failed : " + e3.getMessage(), e3);
            } catch (android.database.sqlite.SQLiteAccessPermException | SQLiteAccessPermException e4) {
                LOG.logE("Execute SaveRequest failed : A SQLiteAccessPermException occurred when save");
                throw new ODMFSQLiteAccessPermException("Execute SaveRequest failed : " + e4.getMessage(), e4);
            } catch (android.database.sqlite.SQLiteCantOpenDatabaseException | SQLiteCantOpenDatabaseException e5) {
                LOG.logE("Execute SaveRequest failed : A SQLiteCantOpenDatabaseException occurred when save");
                throw new ODMFSQLiteCantOpenDatabaseException("Execute SaveRequest failed : " + e5.getMessage(), e5);
            } catch (android.database.sqlite.SQLiteException | SQLiteException | IllegalArgumentException e6) {
                LOG.logE("Execute SaveRequest failed : A SQLiteException occurred when save");
                throw new ODMFRuntimeException("Execute SaveRequest failed : " + e6.getMessage());
            } catch (Exception e7) {
                LOG.logE("Execute FetchRequest failed : A unknown exception occurred when query");
                throw new ODMFRuntimeException("Execute FetchRequest failed : " + e7.getMessage(), e7);
            }
        } else {
            LOG.logE("Execute SaveRequest failed : The parameter request is null");
            throw new ODMFIllegalArgumentException("The parameter request is null");
        }
    }

    private void deleteObjectSetFromSet(List<ManagedObject> list, List<ManagedObject> list2) {
        int size = list2.size();
        for (int i = 0; i < size; i++) {
            deleteObjectFromSet(list, list2.get(i));
        }
    }

    private void deleteObjectFromSet(List<ManagedObject> list, ManagedObject managedObject) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ManagedObject managedObject2 = list.get(i);
            if (managedObject2.getRowId().equals(managedObject.getRowId()) && managedObject2.getEntityName().equals(managedObject.getEntityName())) {
                list.remove(managedObject2);
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public void beginTransaction() {
        try {
            if (!this.db.inTransaction()) {
                this.db.beginTransaction();
            } else {
                LOG.logE("Execute beginTransaction failed : The database is already in a transaction.");
                throw new ODMFIllegalStateException("The database is already in a transaction.");
            }
        } catch (IllegalStateException e) {
            LOG.logE("Execute beginTransaction failed :" + e.getMessage());
            throw new ODMFIllegalStateException("execute beginTransaction failed." + e.getMessage(), e);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public boolean inTransaction() {
        try {
            return this.db.inTransaction();
        } catch (IllegalStateException e) {
            LOG.logE("Check inTransaction failed :" + e.getMessage());
            throw new ODMFIllegalStateException("Check inTransaction failed." + e.getMessage(), e);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public void rollback() {
        try {
            if (this.db.inTransaction()) {
                this.db.endTransaction();
                return;
            }
            LOG.logE("Execute rollback failed : The database " + this.db.getPath() + " is not in a transaction.");
            throw new ODMFIllegalStateException("Execute rollback failed : The database " + this.db.getPath() + " is not in a transaction.");
        } catch (IllegalStateException e) {
            LOG.logE("Execute rollback transaction failed : " + e.getMessage());
            throw new ODMFIllegalStateException("Execute rollback transaction failed." + e.getMessage(), e);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
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

    /* JADX WARNING: Removed duplicated region for block: B:43:0x017e  */
    @Override // com.huawei.odmf.core.PersistentStore
    public void clearTable(String str) {
        Throwable th;
        SQLiteDatabaseCorruptException e;
        SQLiteException e2;
        if (str != null) {
            Entity entity = getEntity(str);
            String tableName = entity.getTableName();
            boolean z = false;
            Cursor cursor = null;
            try {
                this.db.beginTransaction();
                this.db.delete(tableName, null, null);
                List<? extends Relationship> relationships = entity.getRelationships();
                int size = relationships.size();
                for (int i = 0; i < size; i++) {
                    Relationship relationship = (Relationship) relationships.get(i);
                    if (relationship.getRelationShipType() == 0) {
                        this.db.delete(DatabaseTableHelper.getManyToManyMidTableName(relationship), null, null);
                    }
                }
                Cursor rawQuery = this.db.rawQuery("SELECT 1 FROM sqlite_master WHERE type='table' AND name='sqlite_sequence'", null);
                if (rawQuery.getCount() > 0) {
                    this.db.execSQL("UPDATE sqlite_sequence SET seq = 0 WHERE name = '" + tableName + "'");
                }
                this.db.setTransactionSuccessful();
                if (rawQuery != null) {
                    rawQuery.close();
                }
                this.db.endTransaction(false);
            } catch (android.database.sqlite.SQLiteDatabaseCorruptException e3) {
                e = e3;
                LOG.logE("Execute deleteEntityData failed : A SQLiteDatabaseCorruptException occurred : " + e.getMessage());
                throw new ODMFSQLiteDatabaseCorruptException("End Transaction failed : " + e.getMessage(), e);
            } catch (SQLiteDatabaseCorruptException e4) {
                e = e4;
                LOG.logE("Execute deleteEntityData failed : A SQLiteDatabaseCorruptException occurred : " + e.getMessage());
                throw new ODMFSQLiteDatabaseCorruptException("End Transaction failed : " + e.getMessage(), e);
            } catch (android.database.sqlite.SQLiteDiskIOException | SQLiteDiskIOException e5) {
                LOG.logE("Execute deleteEntityData failed : A SQLiteDiskIOException occurred : " + e5.getMessage());
                throw new ODMFSQLiteDiskIOException("End Transaction failed : " + e5.getMessage(), e5);
            } catch (android.database.sqlite.SQLiteException e6) {
                e2 = e6;
                LOG.logE("Execute deleteEntityData failed : " + e2.getMessage());
                throw new ODMFRuntimeException("Execute deleteEntityData failed : " + e2.getMessage());
            } catch (SQLiteException e7) {
                e2 = e7;
                LOG.logE("Execute deleteEntityData failed : " + e2.getMessage());
                throw new ODMFRuntimeException("Execute deleteEntityData failed : " + e2.getMessage());
            } catch (IllegalStateException e8) {
                LOG.logE("Execute deleteEntityData failed : " + e8.getMessage());
                throw new ODMFIllegalStateException("Execute deleteEntityData failed." + e8.getMessage(), e8);
            } catch (RuntimeException e9) {
                LOG.logE("Execute deleteEntityData failed : A RuntimeException occurred : " + e9.getMessage());
                throw new ODMFRuntimeException("Execute delete failed : " + e9.getMessage(), e9);
            } catch (Throwable th2) {
                th = th2;
                z = true;
                if (0 != 0) {
                }
                this.db.endTransaction(z);
                throw th;
            }
        } else {
            LOG.logE("Execute deleteEntityData failed : The entityName is null.");
            throw new ODMFIllegalArgumentException("Execute deleteEntityData failed : The entityName is null.");
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x008e  */
    @Override // com.huawei.odmf.core.PersistentStore
    public ManagedObject getObjectValues(ObjectId objectId) {
        Throwable th;
        Entity entity = getModel().getEntity(objectId.getEntityName());
        Long l = (Long) objectId.getId();
        AEntityHelper helper = getHelper(objectId.getEntityName());
        Cursor cursor = null;
        ManagedObject managedObject = null;
        try {
            DataBase dataBase = this.db;
            String tableName = entity.getTableName();
            String[] strArr = {DatabaseQueryService.getRowidColumnName() + " AS " + DatabaseQueryService.getRowidColumnName(), "*"};
            StringBuilder sb = new StringBuilder();
            sb.append(DatabaseQueryService.getRowidColumnName());
            sb.append("=?");
            Cursor query = DatabaseQueryService.query(dataBase, tableName, strArr, sb.toString(), new String[]{String.valueOf(l)});
            try {
                if (query.moveToNext()) {
                    managedObject = (ManagedObject) helper.readObject(query, 0);
                    managedObject.setObjectId(objectId);
                    managedObject.setRowId((Long) objectId.getId());
                }
                if (query != null) {
                    query.close();
                }
                return managedObject;
            } catch (Throwable th2) {
                th = th2;
                cursor = query;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            if (cursor != null) {
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public List<ObjectId> getRelationshipObjectId(ObjectId objectId, Relationship relationship) {
        SQLiteDatabaseCorruptException e;
        SQLiteDiskIOException e2;
        Entity relatedEntity = relationship.getRelatedEntity();
        ArrayList arrayList = new ArrayList();
        Cursor cursor = null;
        try {
            Cursor relationshipCursor = getRelationshipCursor(objectId, relationship);
            if (relationshipCursor != null) {
                while (relationshipCursor.moveToNext()) {
                    String string = relationshipCursor.getString(0);
                    if (string != null) {
                        arrayList.add(createObjectId(relatedEntity, Long.valueOf(Long.parseLong(string))));
                    }
                }
            }
            if (relationshipCursor != null) {
                relationshipCursor.close();
            }
            if (!relationship.getNotFound().equals(ARelationship.EXCEPTION) || arrayList.size() != 0) {
                return arrayList;
            }
            LOG.logE("Execute getRelationshipObjectId failed : The relevant object not found.");
            throw new ODMFRelatedObjectNotFoundException("Execute getRelationshipObjectId failed : The relevant object not found.");
        } catch (android.database.sqlite.SQLiteDatabaseCorruptException e3) {
            e = e3;
            LOG.logE("Execute getRelationshipObjectId failed : A SQLiteException occurred when try to get related ObjectId.");
            throw new ODMFSQLiteDatabaseCorruptException("End Transaction failed : " + e.getMessage(), e);
        } catch (SQLiteDatabaseCorruptException e4) {
            e = e4;
            LOG.logE("Execute getRelationshipObjectId failed : A SQLiteException occurred when try to get related ObjectId.");
            throw new ODMFSQLiteDatabaseCorruptException("End Transaction failed : " + e.getMessage(), e);
        } catch (android.database.sqlite.SQLiteDiskIOException e5) {
            e2 = e5;
            LOG.logE("Execute getRelationshipObjectId failed : A SQLiteException occurred when try to get related ObjectId.");
            throw new ODMFSQLiteDiskIOException("End Transaction failed : " + e2.getMessage(), e2);
        } catch (SQLiteDiskIOException e6) {
            e2 = e6;
            LOG.logE("Execute getRelationshipObjectId failed : A SQLiteException occurred when try to get related ObjectId.");
            throw new ODMFSQLiteDiskIOException("End Transaction failed : " + e2.getMessage(), e2);
        } catch (android.database.sqlite.SQLiteException e7) {
            LOG.logE("Execute getRelationshipObjectId failed : A SQLiteException occurred when try to get related ObjectId.");
            throw new ODMFRuntimeException("Execute getRelationshipObjectId failed : " + e7.getMessage());
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    private Cursor getRelationshipCursor(ObjectId objectId, Relationship relationship) {
        if (relationship.getRelationShipType() == 2) {
            return this.relationshipLoader.getManyToOneRelationshipCursor(objectId, relationship);
        }
        if (relationship.getRelationShipType() == 6) {
            return this.relationshipLoader.getOneToOneRelationshipCursor(objectId, relationship);
        }
        if (relationship.getRelationShipType() == 4) {
            return this.relationshipLoader.getOneToManyRelationshipCursor(objectId, relationship);
        }
        return this.relationshipLoader.getManyToManyRelationshipCursor(objectId, relationship);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public void close() {
        try {
            this.databaseHelper.close();
        } catch (android.database.sqlite.SQLiteDatabaseCorruptException | SQLiteDatabaseCorruptException e) {
            LOG.logE("Close database failed : A SQLiteDatabaseCorruptException occurred when close.");
            throw new ODMFSQLiteDatabaseCorruptException("Close database failed : " + e.getMessage(), e);
        } catch (android.database.sqlite.SQLiteDiskIOException | SQLiteDiskIOException e2) {
            LOG.logE("Close database failed : A SQLiteDiskIOException occurred when close.");
            throw new ODMFSQLiteDiskIOException("Close database failed : " + e2.getMessage(), e2);
        } catch (RuntimeException e3) {
            LOG.logE("Close database failed : A RuntimeException occurred when close.");
            throw new ODMFRuntimeException("Close database failed : " + e3.getMessage(), e3);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public void createEntityForMigration(Entity entity) {
        if (entity != null) {
            try {
                this.databaseHelper.addTable(this.db, entity);
            } catch (android.database.sqlite.SQLiteException e) {
                LOG.logE("Execute createTable Failed : A SQLiteException occurred when createTable");
                throw new ODMFRuntimeException("Execute createTable Failed : " + e.getMessage());
            }
        } else {
            LOG.logE("Execute createTable failed : The input parameter entity is null.");
            throw new ODMFIllegalArgumentException("Execute createTable failed : The input parameter entity is null.");
        }
    }

    @Override // com.huawei.odmf.core.PersistentStore
    public void dropEntityForMigration(String str) {
        if (str == null || str.equals("")) {
            LOG.logE("Execute dropTable Failed : The input parameter entityName is null.");
            throw new ODMFIllegalArgumentException("Execute dropTable Failed : The input parameter is null.");
        }
        try {
            this.databaseHelper.dropTable(this.db, str);
        } catch (android.database.sqlite.SQLiteException e) {
            LOG.logE("Execute dropTable Failed : A SQLiteException occurred when dropTable");
            throw new ODMFRuntimeException("Execute dropTable Failed : " + e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public void renameEntityForMigration(String str, String str2) {
        if (str == null || str2 == null) {
            LOG.logE("Execute renameTable Failed :The newName or the oldName is null.");
            throw new ODMFIllegalArgumentException("Execute renameTable Failed : The newName or the oldName is null.");
        }
        try {
            this.databaseHelper.alterTableName(this.db, str2, str);
        } catch (android.database.sqlite.SQLiteException e) {
            LOG.logE("Execute renameTable Failed : A SQLiteException occurred when renameTable");
            throw new ODMFRuntimeException("Execute renameTable Failed : " + e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public void addColumnForMigration(Entity entity, Attribute attribute) {
        if (entity == null || attribute == null) {
            LOG.logE("Execute addColumn Failed : The entity or the attribute is null.");
            throw new ODMFIllegalArgumentException("Execute addColumn Failed : The entity or the attribute is null.");
        }
        try {
            this.databaseHelper.alterTableAddColumn(this.db, entity.getTableName(), attribute);
        } catch (android.database.sqlite.SQLiteException e) {
            LOG.logE("Execute addColumn Failed : A SQLiteException occurred when addColumn");
            throw new ODMFRuntimeException("Execute addColumn Failed : " + e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public void addRelationshipForMigration(Entity entity, Relationship relationship) {
        if (entity == null || relationship == null) {
            LOG.logE("Execute addRelationship Failed : The entity or the relationship is null.");
            throw new ODMFIllegalArgumentException("Execute addRelationship Failed : The entity or the relationship is null.");
        }
        try {
            this.databaseHelper.alterTableAddRelationship(this.db, entity.getTableName(), relationship);
        } catch (android.database.sqlite.SQLiteException e) {
            LOG.logE("Execute addRelationship Failed : A SQLiteException occurred when addRelationship");
            throw new ODMFRuntimeException("Execute addRelationship Failed : " + e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public ManagedObject getToOneRelationshipValue(String str, ManagedObject managedObject, ObjectContext objectContext) {
        return loadToOneRelationshipValue(str, managedObject, objectContext);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public List<ManagedObject> getToManyRelationshipValue(String str, ManagedObject managedObject, ObjectContext objectContext) {
        if (str == null || managedObject == null || objectContext == null) {
            LOG.logE("Execute getToManyRelationshipValue Failed : The input parameter is null.");
            throw new ODMFIllegalArgumentException("Execute getToManyRelationshipValue Failed : The input parameter is null.");
        }
        Relationship relationship = getModel().getEntity(managedObject.getEntityName()).getRelationship(str);
        if (relationship != null) {
            List<ObjectId> relationshipObjectId = getRelationshipObjectId(managedObject.getObjectId(), relationship);
            int size = relationshipObjectId.size();
            if (size == 0) {
                return null;
            }
            if (relationship.isLazy()) {
                return new LazyList(relationshipObjectId, objectContext, relationship.getRelatedEntity().getEntityName(), managedObject);
            }
            ODMFList oDMFList = new ODMFList(objectContext, relationship.getRelatedEntity().getEntityName(), managedObject);
            for (int i = 0; i < size; i++) {
                oDMFList.addObj(i, getManagedObjectWithCache(relationshipObjectId.get(i), objectContext));
            }
            return oDMFList;
        }
        LOG.logE("Execute getToManyRelationshipValue Failed : This field name is not in the class.");
        throw new ODMFIllegalArgumentException("Execute getToManyRelationshipValue Failed : This field name is not in the class.");
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public ManagedObject remoteGetToOneRelationshipValue(String str, ObjectId objectId, ObjectContext objectContext) {
        return loadToOneRelationshipValue(str, objectId, objectContext);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public List<ManagedObject> remoteGetToManyRelationshipValue(String str, ObjectId objectId, ObjectContext objectContext) {
        if (str == null || objectId == null || objectContext == null) {
            LOG.logE("Execute remoteGetToManyRelationshipValue Failed : The input parameter is null.");
            throw new ODMFIllegalArgumentException("Execute remoteGetToManyRelationshipValue Failed : The input parameter is null.");
        }
        Relationship relationship = getModel().getEntity(objectId.getEntityName()).getRelationship(str);
        if (relationship != null) {
            List<ObjectId> relationshipObjectId = getRelationshipObjectId(objectId, relationship);
            int size = relationshipObjectId.size();
            if (size == 0) {
                return null;
            }
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < size; i++) {
                arrayList.add(getManagedObjectWithCache(relationshipObjectId.get(i), objectContext));
            }
            return arrayList;
        }
        LOG.logE("Execute remoteGetToManyRelationshipValue Failed : This field name is not in the class.");
        throw new ODMFIllegalArgumentException("Execute remoteGetToManyRelationshipValue Failed : This field name is not in the class.");
    }

    private ManagedObject loadToOneRelationshipValue(String str, Object obj, ObjectContext objectContext) {
        ObjectId objectId;
        Entity entity;
        if (str == null || obj == null || objectContext == null) {
            LOG.logE("Execute loadToOneRelationshipValue Failed : The input parameter is null.");
            throw new ODMFIllegalArgumentException("The input parameter is null.");
        }
        if (obj instanceof ObjectId) {
            objectId = (ObjectId) obj;
            entity = getModel().getEntity(objectId.getEntityName());
        } else if (obj instanceof ManagedObject) {
            ManagedObject managedObject = (ManagedObject) obj;
            ObjectId objectId2 = managedObject.getObjectId();
            entity = getModel().getEntity(managedObject.getEntityName());
            objectId = objectId2;
        } else {
            objectId = null;
            entity = null;
        }
        if (objectId == null || entity == null) {
            LOG.logE("Execute loadToOneRelationshipValue Failed : The id or the entity of the related object does not exist.");
            throw new ODMFIllegalArgumentException("Execute loadToOneRelationshipValue Failed : The id or the entity of the related object does not exist.");
        }
        Relationship relationship = entity.getRelationship(str);
        if (relationship != null) {
            List<ObjectId> relationshipObjectId = getRelationshipObjectId(objectId, relationship);
            if (relationshipObjectId.size() == 0) {
                return null;
            }
            ObjectId objectId3 = relationshipObjectId.get(0);
            ManagedObject cacheObject = getCacheObject(objectId3, objectContext);
            if (cacheObject != null) {
                return cacheObject;
            }
            ManagedObject objectValues = getObjectValues(objectId3);
            if (objectValues != null) {
                objectValues.setObjectContext(objectContext);
                objectValues.setState(4);
                objectValues.setRowId((Long) objectId3.getId());
                PersistentStoreCoordinator.getDefault().putObjectIntoCache(objectValues);
            }
            return objectValues;
        }
        LOG.logE("Execute loadToOneRelationshipValue Failed : The relationship which the fieldName specified does not exist.");
        throw new ODMFIllegalArgumentException("Execute loadToOneRelationshipValue Failed : The relationship which the fieldName specified does not exist.");
    }

    private ManagedObject getCacheObject(ObjectId objectId, ObjectContext objectContext) {
        ManagedObject objectFromCache = PersistentStoreCoordinator.getDefault().getObjectFromCache(objectId);
        if (objectFromCache == null) {
            return null;
        }
        if (objectFromCache.isDirty() && objectFromCache.getLastObjectContext() != objectContext) {
            return null;
        }
        return objectFromCache;
    }

    private ManagedObject getManagedObjectWithCache(ObjectId objectId, ObjectContext objectContext) {
        ManagedObject objectFromCache = PersistentStoreCoordinator.getDefault().getObjectFromCache(objectId);
        if (objectFromCache == null) {
            objectFromCache = getObjectValues(objectId);
            if (objectFromCache != null) {
                objectFromCache.setObjectContext(objectContext);
                objectFromCache.setState(4);
                objectFromCache.setRowId((Long) objectId.getId());
                PersistentStoreCoordinator.getDefault().putObjectIntoCache(objectFromCache);
            }
        } else if (!(!objectFromCache.isDirty() || objectFromCache.getLastObjectContext() == objectContext || (objectFromCache = getObjectValues(objectId)) == null)) {
            objectFromCache.setObjectContext(objectContext);
            objectFromCache.setState(4);
            objectFromCache.setRowId((Long) objectId.getId());
            PersistentStoreCoordinator.getDefault().putObjectIntoCache(objectFromCache);
        }
        return objectFromCache;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public Cursor executeRawQuerySQL(String str) {
        if (str != null) {
            try {
                return this.db.rawSelect(str, null, null, false);
            } catch (IllegalStateException e) {
                LOG.logE("Execute rawQuerySQL failed : A IllegalStateException occurred when execute rawQuerySQL.");
                throw new ODMFRuntimeException("Execute rawQuerySQL failed : " + e.getMessage(), e);
            } catch (android.database.sqlite.SQLiteDatabaseCorruptException | SQLiteDatabaseCorruptException e2) {
                LOG.logE("Execute rawQuerySQL failed : A sqliteDatabase occurred when execute rawQuerySQL.");
                throw new ODMFSQLiteDatabaseCorruptException("Execute rawQuerySQL failed : " + e2.getMessage(), e2);
            } catch (android.database.sqlite.SQLiteDiskIOException | SQLiteDiskIOException e3) {
                LOG.logE("Execute rawQuerySQL failed : A disk io exception occurred when execute rawQuerySQL.");
                throw new ODMFSQLiteDiskIOException("Execute rawQuerySQL failed : " + e3.getMessage(), e3);
            } catch (android.database.sqlite.SQLiteFullException | SQLiteFullException e4) {
                LOG.logE("Execute rawQuerySQL failed : A disk full exception occurred when execute rawQuerySQL.");
                throw new ODMFSQLiteFullException("Execute rawQuerySQL failed : " + e4.getMessage(), e4);
            } catch (android.database.sqlite.SQLiteAccessPermException | SQLiteAccessPermException e5) {
                LOG.logE("Execute rawQuerySQL failed : An access permission exception occurred when execute rawQuerySQL.");
                throw new ODMFSQLiteAccessPermException("Execute rawQuerySQL failed : " + e5.getMessage(), e5);
            } catch (android.database.sqlite.SQLiteCantOpenDatabaseException | SQLiteCantOpenDatabaseException e6) {
                LOG.logE("Execute rawQuerySQL failed : An cant open exception occurred when execute rawQuerySQL.");
                throw new ODMFSQLiteCantOpenDatabaseException("Execute rawQuerySQL failed : " + e6.getMessage(), e6);
            } catch (android.database.sqlite.SQLiteException | SQLiteException e7) {
                LOG.logE("Execute rawQuerySQL failed : A SQLite exception occurred when execute rawQuerySQL.");
                throw new ODMFRuntimeException("Execute rawQuerySQL failed : " + e7.getMessage(), e7);
            } catch (Exception e8) {
                LOG.logE("Execute rawQuerySQL failed : A unknown exception occurred when execute rawQuerySQL.");
                throw new ODMFRuntimeException("Execute rawQuerySQL failed : " + e8.getMessage(), e8);
            }
        } else {
            LOG.logE("Execute RawQuerySQL Failed : The sql is null.");
            throw new ODMFIllegalArgumentException("Execute RawQuerySQL Failed : The sql is null.");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public Cursor query(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        try {
            return DatabaseQueryService.query(this.db, z, str, strArr, str2, strArr2, str3, str4, str5, str6);
        } catch (IllegalStateException e) {
            LOG.logE("Execute query failed : A IllegalStateException occurred when execute query.");
            throw new ODMFRuntimeException("Execute query failed : " + e.getMessage(), e);
        } catch (android.database.sqlite.SQLiteDatabaseCorruptException | SQLiteDatabaseCorruptException e2) {
            LOG.logE("Execute query failed : A sqliteDatabase corrupt exception occurred when execute query.");
            throw new ODMFSQLiteDatabaseCorruptException("Execute query failed : " + e2.getMessage(), e2);
        } catch (android.database.sqlite.SQLiteDiskIOException | SQLiteDiskIOException e3) {
            LOG.logE("Execute query failed : A disk io exception occurred when execute query.");
            throw new ODMFSQLiteDiskIOException("Execute query failed : " + e3.getMessage(), e3);
        } catch (android.database.sqlite.SQLiteFullException | SQLiteFullException e4) {
            LOG.logE("Execute query failed : A disk full exception occurred when execute query.");
            throw new ODMFSQLiteFullException("Execute query failed : " + e4.getMessage(), e4);
        } catch (android.database.sqlite.SQLiteAccessPermException | SQLiteAccessPermException e5) {
            LOG.logE("Execute query failed : An access permission exception occurred when execute query.");
            throw new ODMFSQLiteAccessPermException("Execute query failed : " + e5.getMessage(), e5);
        } catch (android.database.sqlite.SQLiteCantOpenDatabaseException | SQLiteCantOpenDatabaseException e6) {
            LOG.logE("Execute query failed : An cant open exception occurred when execute query.");
            throw new ODMFSQLiteCantOpenDatabaseException("Execute query failed : " + e6.getMessage(), e6);
        } catch (android.database.sqlite.SQLiteException | SQLiteException e7) {
            LOG.logE("Execute query failed : A SQLite exception occurred when execute query.");
            throw new ODMFRuntimeException("Execute query failed : " + e7.getMessage(), e7);
        } catch (Exception e8) {
            LOG.logE("Execute query failed : A unknown exception occurred when execute query.");
            throw new ODMFRuntimeException("Execute query failed : " + e8.getMessage(), e8);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public void executeRawSQL(String str) {
        if (str != null) {
            try {
                this.db.execSQL(str);
            } catch (IllegalStateException e) {
                LOG.logE("Execute rawSQL failed : A IllegalStateException occurred when execute rawSQL.");
                throw new ODMFRuntimeException("Execute rawSQL failed : " + e.getMessage(), e);
            } catch (android.database.sqlite.SQLiteDatabaseCorruptException | SQLiteDatabaseCorruptException e2) {
                LOG.logE("Execute rawSQL failed : A sqliteDatabase corrupt exception occurred when execute rawSQL.");
                throw new ODMFSQLiteDatabaseCorruptException("Execute rawSQL failed : " + e2.getMessage(), e2);
            } catch (android.database.sqlite.SQLiteDiskIOException | SQLiteDiskIOException e3) {
                LOG.logE("Execute rawSQL failed: A disk io exception occurred when execute rawSQL.");
                throw new ODMFSQLiteDiskIOException("Execute rawSQL failed : " + e3.getMessage(), e3);
            } catch (android.database.sqlite.SQLiteFullException | SQLiteFullException e4) {
                LOG.logE("Execute rawSQL failed : A disk full exception occurred when execute rawSQL.");
                throw new ODMFSQLiteFullException("Execute rawSQL failed : " + e4.getMessage(), e4);
            } catch (android.database.sqlite.SQLiteAccessPermException | SQLiteAccessPermException e5) {
                LOG.logE("Execute rawSQL failed : An access permission exception occurred when execute rawSQL.");
                throw new ODMFSQLiteAccessPermException("Execute rawSQL failed : " + e5.getMessage(), e5);
            } catch (android.database.sqlite.SQLiteCantOpenDatabaseException | SQLiteCantOpenDatabaseException e6) {
                LOG.logE("Execute rawSQL failed : An cant open exception occurred when execute rawSQL.");
                throw new ODMFSQLiteCantOpenDatabaseException("Execute rawSQL failed : " + e6.getMessage(), e6);
            } catch (android.database.sqlite.SQLiteException | SQLiteException e7) {
                LOG.logE("Execute rawSQL failed : A SQLite exception occurred when execute rawSQL.");
                throw new ODMFRuntimeException("Execute rawSQL failed : " + e7.getMessage(), e7);
            } catch (Exception e8) {
                LOG.logE("Execute rawSQL failed : A unknown exception occurred when execute rawSQL.");
                throw new ODMFRuntimeException("Execute rawSQL failed : " + e8.getMessage(), e8);
            }
        } else {
            LOG.logE("Execute RawSQL Failed : The sql is null.");
            throw new ODMFIllegalArgumentException("Execute RawSQL Failed : The sql is null.");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public void setDbVersions(String str, int i) {
        this.databaseHelper.setDatabaseVersions(this.db, str, i);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.odmf.core.PersistentStore
    public void setEntityVersions(String str, String str2, int i) {
        this.databaseHelper.setEntityVersions(this.db, str, str2, i);
    }

    @Override // com.huawei.odmf.core.PersistentStore
    public void insertDataForMigration(String str, ContentValues contentValues) {
        if (str == null || contentValues == null) {
            LOG.logE("Execute insertData Failed : The tableName or the values is null.");
            throw new ODMFIllegalArgumentException("The tableName or the values is null.");
        }
        try {
            this.db.insertOrThrow(str, null, contentValues);
        } catch (android.database.sqlite.SQLiteException e) {
            LOG.logE("Execute insertData Failed : A SQLiteException occurred when insertData.");
            throw new ODMFRuntimeException("Execute insertData Failed : " + e.getMessage());
        }
    }

    @Override // com.huawei.odmf.core.PersistentStore
    public void exportDatabase(String str, byte[] bArr) {
        String str2;
        if (str.startsWith("/")) {
            str2 = new File(str).getName();
        } else {
            str2 = str;
            str = new File(this.db.getPath()).getParent() + "/" + str;
        }
        this.context.deleteDatabase(str);
        String format = String.format(Locale.ENGLISH, "SELECT sqlcipher_export('%s');", str2);
        if (bArr == null || bArr.length == 0) {
            bArr = new byte[0];
        }
        Cursor cursor = null;
        try {
            this.db.addAttachAlias(str2, str, bArr);
            cursor = executeRawQuerySQL(format);
            cursor.moveToNext();
            this.db.removeAttachAlias(str2);
            LOG.logI("Database " + getPath() + " is exported to " + str2 + ".");
            clearKey(bArr);
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            LOG.logE("Execute exportDatabase Failed : A exception occurred when export database.");
            throw new ODMFRuntimeException("Execute exportDatabase Failed : " + e.toString());
        } catch (Throwable th) {
            clearKey(bArr);
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    @Override // com.huawei.odmf.core.PersistentStore
    public void resetMetadata() {
        this.databaseHelper.resetMetadata(this.db);
        loadMetadata();
    }

    @Override // com.huawei.odmf.core.PersistentStore
    public Set<String> getTableInvolvedInSQL(String str) {
        try {
            return StringUtil.array2Set(this.db.getSQLTables(str, 0, null));
        } catch (android.database.sqlite.SQLiteDatabaseCorruptException | SQLiteDatabaseCorruptException e) {
            LOG.logE("Get table involved in SQL failed : A sqlite database corrupt exception occurred.");
            throw new ODMFSQLiteDatabaseCorruptException("Get table involved in SQL failed : " + e.getMessage(), e);
        } catch (android.database.sqlite.SQLiteDiskIOException | SQLiteDiskIOException e2) {
            LOG.logE("Get table involved in SQL failed : A disk io exception occurred.");
            throw new ODMFSQLiteDiskIOException("Get table involved in SQL failed : " + e2.getMessage(), e2);
        } catch (android.database.sqlite.SQLiteException | SQLiteException e3) {
            LOG.logE("Get table involved in SQL failed : A SQLite exception occurred.");
            throw new ODMFRuntimeException("Get table involved in SQL failed :" + e3.getMessage(), e3);
        } catch (Exception e4) {
            LOG.logE("Get table involved in SQL failed : An unknown exception occurred.");
            throw new ODMFRuntimeException("Get table involved in SQL failed :" + e4.getMessage(), e4);
        }
    }

    @Override // com.huawei.odmf.core.PersistentStore
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.huawei.odmf.core.PersistentStore
    public int hashCode() {
        return super.hashCode();
    }
}
