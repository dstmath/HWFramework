package ohos.data.orm.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import ohos.app.AbilityContext;
import ohos.app.Context;
import ohos.data.DatabaseHelper;
import ohos.data.PredicatesUtils;
import ohos.data.orm.EntityHelper;
import ohos.data.orm.ObjectId;
import ohos.data.orm.OrmConfig;
import ohos.data.orm.OrmDatabase;
import ohos.data.orm.OrmMigration;
import ohos.data.orm.OrmObject;
import ohos.data.orm.OrmPredicates;
import ohos.data.rdb.RdbOpenCallback;
import ohos.data.rdb.RdbPredicates;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.Statement;
import ohos.data.rdb.StoreConfig;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.hiviewdfx.HiLogLabel;

public class OrmStore {
    private static final String DB_IMPL_SUFFIX = "Impl";
    private static final String GET_HELPER_METHOD_NAME = "getHelper";
    private static final String GET_VERSION_METHOD_NAME = "getVersion";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "OrmStore");
    private final String alias;
    private Context context;
    private final Object helperLock = new Object();
    private Map<String, EntityHelper<OrmObject>> helperMap;
    private final String name;
    private RdbOpenCallback openCallback;
    private final Object statementLock = new Object();
    private Map<String, StatementsLoader> statementMap;
    private RdbStore store;
    private final int version;

    private OrmStore(Context context2, String str, String str2, int i, RdbOpenCallback rdbOpenCallback) {
        this.context = context2;
        this.alias = str;
        this.name = str2;
        this.version = i;
        this.openCallback = rdbOpenCallback;
        this.helperMap = new ConcurrentHashMap(20);
        this.statementMap = new ConcurrentHashMap(20);
    }

    public static <T extends OrmDatabase> OrmStore open(Context context2, OrmConfig ormConfig, Class<T> cls, OrmMigration... ormMigrationArr) {
        Class<?> cls2;
        Object obj;
        String str = cls.getName() + DB_IMPL_SUFFIX;
        ClassLoader classLoader = null;
        try {
            if (context2 instanceof AbilityContext) {
                classLoader = context2.getClassloader();
            }
            if (classLoader != null) {
                cls2 = Class.forName(str, true, classLoader);
            } else {
                cls2 = Class.forName(str);
            }
            if (ormMigrationArr != null) {
                if (ormMigrationArr.length != 0) {
                    obj = cls2.getConstructor(OrmMigration[].class, OrmMigration[].class).newInstance(getUpgradeMigrations(ormMigrationArr), getDowngradeMigrations(ormMigrationArr));
                    OrmStore ormStore = new OrmStore(context2, ormConfig.getAlias(), ormConfig.getName(), ((Integer) cls2.getMethod(GET_VERSION_METHOD_NAME, new Class[0]).invoke(obj, new Object[0])).intValue(), (RdbOpenCallback) cls2.getMethod(GET_HELPER_METHOD_NAME, new Class[0]).invoke(obj, new Object[0]));
                    ormStore.open(ormConfig);
                    return ormStore;
                }
            }
            obj = cls2.newInstance();
            OrmStore ormStore2 = new OrmStore(context2, ormConfig.getAlias(), ormConfig.getName(), ((Integer) cls2.getMethod(GET_VERSION_METHOD_NAME, new Class[0]).invoke(obj, new Object[0])).intValue(), (RdbOpenCallback) cls2.getMethod(GET_HELPER_METHOD_NAME, new Class[0]).invoke(obj, new Object[0]));
            ormStore2.open(ormConfig);
            return ormStore2;
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            throw new IllegalStateException(e.getClass().getName() + "." + str + "." + e.getMessage());
        }
    }

    private static OrmMigration[] getDowngradeMigrations(OrmMigration[] ormMigrationArr) {
        ArrayList arrayList = new ArrayList();
        for (OrmMigration ormMigration : ormMigrationArr) {
            if (ormMigration.getBeginVersion() > ormMigration.getEndVersion()) {
                arrayList.add(ormMigration);
            }
        }
        return (OrmMigration[]) arrayList.toArray(new OrmMigration[0]);
    }

    private static OrmMigration[] getUpgradeMigrations(OrmMigration[] ormMigrationArr) {
        ArrayList arrayList = new ArrayList();
        for (OrmMigration ormMigration : ormMigrationArr) {
            if (ormMigration.getBeginVersion() < ormMigration.getEndVersion()) {
                arrayList.add(ormMigration);
            }
        }
        return (OrmMigration[]) arrayList.toArray(new OrmMigration[0]);
    }

    public void open(OrmConfig ormConfig) {
        this.store = new DatabaseHelper(this.context).getRdbStore(new StoreConfig.Builder().setName(ormConfig.getName()).setStorageMode(StoreConfig.StorageMode.MODE_DISK).setJournalMode(null).setEncryptKey(ormConfig.getEncryptKey()).setReadOnly(false).build(), this.version, this.openCallback);
    }

    public void changeEncryptKey(byte[] bArr) {
        this.store.changeEncryptKey(bArr);
    }

    public void close() {
        RdbStore rdbStore = this.store;
        if (rdbStore != null) {
            rdbStore.close();
        }
        synchronized (this.statementLock) {
            this.statementMap.clear();
        }
        synchronized (this.helperLock) {
            this.helperMap.clear();
        }
    }

    public void save(SaveRequest saveRequest) {
        if (saveRequest != null) {
            RdbStore rdbStore = this.store;
            if (rdbStore != null) {
                rdbStore.beginTransaction();
                try {
                    executeInsert(saveRequest.getInsertedObjects());
                    executeUpdate(saveRequest.getUpdatedObjects());
                    executeDelete(saveRequest.getDeletedObjects());
                    this.store.markAsCommit();
                } finally {
                    this.store.endTransaction();
                }
            } else {
                throw new IllegalStateException("This orm store may already closed.");
            }
        } else {
            throw new IllegalArgumentException("The parameter saveRequest is null");
        }
    }

    private void executeInsert(List<OrmObject> list) {
        for (OrmObject ormObject : list) {
            String name2 = ormObject.getClass().getName();
            EntityHelper<OrmObject> helper = getHelper(name2);
            synchronized (this.statementLock) {
                Statement insertStatement = getStatementLoader(name2).getInsertStatement(this.store, helper);
                helper.bindValue(insertStatement, ormObject);
                long executeAndGetLastInsertRowId = insertStatement.executeAndGetLastInsertRowId();
                ormObject.setRowId(executeAndGetLastInsertRowId);
                ormObject.setObjectId(new ObjectId(this.alias, helper.getTableName(), executeAndGetLastInsertRowId));
                helper.setPrimaryKeyValue(ormObject, executeAndGetLastInsertRowId);
                insertStatement.clearValues();
            }
        }
    }

    private void executeUpdate(List<OrmObject> list) {
        for (OrmObject ormObject : list) {
            long rowId = ormObject.getRowId();
            String name2 = ormObject.getClass().getName();
            EntityHelper<OrmObject> helper = getHelper(name2);
            synchronized (this.statementLock) {
                Statement updateStatement = getStatementLoader(name2).getUpdateStatement(this.store, helper);
                helper.bindValue(updateStatement, ormObject, rowId);
                updateStatement.execute();
                updateStatement.clearValues();
            }
        }
    }

    private void executeDelete(List<OrmObject> list) {
        for (OrmObject ormObject : list) {
            String name2 = ormObject.getClass().getName();
            EntityHelper<OrmObject> helper = getHelper(name2);
            synchronized (this.statementLock) {
                Statement deleteStatement = getStatementLoader(name2).getDeleteStatement(this.store, helper);
                deleteStatement.setLong(1, ormObject.getRowId());
                deleteStatement.execute();
                deleteStatement.clearValues();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0059, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0074, code lost:
        throw new java.lang.IllegalStateException("Execute getHelper failed : " + r5.getMessage());
     */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0059 A[ExcHandler: ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException (r5v3 'e' java.lang.ReflectiveOperationException A[CUSTOM_DECLARE]), Splitter:B:22:0x0058] */
    private EntityHelper<OrmObject> getHelper(String str) {
        EntityHelper<OrmObject> entityHelper;
        Class<?> cls;
        String str2 = str + "Helper";
        synchronized (this.helperLock) {
            entityHelper = this.helperMap.get(str2);
        }
        if (entityHelper != null) {
            return entityHelper;
        }
        ClassLoader classloader = this.context instanceof AbilityContext ? this.context.getClassloader() : null;
        if (classloader != null) {
            cls = Class.forName(str2, true, classloader);
        } else {
            cls = Class.forName(str2);
        }
        EntityHelper<OrmObject> entityHelper2 = (EntityHelper) cls.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
        synchronized (this.helperLock) {
            this.helperMap.put(str2, entityHelper2);
        }
        return entityHelper2;
    }

    private StatementsLoader getStatementLoader(String str) {
        synchronized (this.statementLock) {
            StatementsLoader statementsLoader = this.statementMap.get(str);
            if (statementsLoader != null) {
                return statementsLoader;
            }
            StatementsLoader statementsLoader2 = new StatementsLoader();
            this.statementMap.put(str, statementsLoader2);
            return statementsLoader2;
        }
    }

    public List<OrmObject> executeFetchRequest(OrmPredicates ormPredicates) {
        if (ormPredicates != null) {
            EntityHelper<OrmObject> helper = getHelper(ormPredicates.getEntityName());
            ResultSet resultSet = null;
            try {
                ResultSet query = RdbStoreAdapter.query(this.store, createFromOrmPredicates(ormPredicates));
                if (!query.goToFirstRow()) {
                    ArrayList arrayList = new ArrayList(0);
                    query.close();
                    return arrayList;
                }
                List<OrmObject> loadObjectsFromCursor = loadObjectsFromCursor(helper, query);
                query.close();
                return loadObjectsFromCursor;
            } catch (Throwable th) {
                if (0 != 0) {
                    resultSet.close();
                }
                throw th;
            }
        } else {
            throw new IllegalArgumentException("Execute FetchRequest failed : The parameter ormPredicates is null.");
        }
    }

    public ResultSet executeSharedFetchRequest(OrmPredicates ormPredicates, String[] strArr) {
        if (ormPredicates != null) {
            return RdbStoreAdapter.query(this.store, createFromOrmPredicates(ormPredicates), strArr);
        }
        throw new IllegalArgumentException("Execute Shared FetchRequest failed : The parameter rdbPredicates is null.");
    }

    private List<OrmObject> loadObjectsFromCursor(EntityHelper<OrmObject> entityHelper, ResultSet resultSet) {
        ArrayList arrayList = new ArrayList(20);
        do {
            arrayList.add(loadObjectFromCursor(entityHelper, resultSet));
        } while (resultSet.goToNextRow());
        return arrayList;
    }

    private OrmObject loadObjectFromCursor(EntityHelper<OrmObject> entityHelper, ResultSet resultSet) {
        long j = resultSet.getLong(RdbStoreAdapter.getOdmfRowidIndex());
        OrmObject createInstance = entityHelper.createInstance(resultSet);
        createInstance.setRowId(j);
        createInstance.setObjectId(new ObjectId(this.alias, entityHelper.getTableName(), j));
        return createInstance;
    }

    public int executeDeleteRequest(OrmPredicates ormPredicates) {
        if (ormPredicates != null) {
            return RdbStoreAdapter.delete(this.store, createFromOrmPredicates(ormPredicates));
        }
        throw new IllegalArgumentException("Execute delete Request failed : The parameter ormPredicates is null.");
    }

    public int executeUpdateRequest(OrmPredicates ormPredicates, ValuesBucket valuesBucket) {
        if (ormPredicates != null) {
            return RdbStoreAdapter.update(this.store, createFromOrmPredicates(ormPredicates), valuesBucket);
        }
        throw new IllegalArgumentException("Execute update Request failed : The parameter rdbPredicates is null.");
    }

    public String executeAggregateRequest(OrmPredicates ormPredicates, String str, String str2) {
        if (ormPredicates != null) {
            return RdbStoreAdapter.aggregateQuery(this.store, createFromOrmPredicates(ormPredicates), str, str2);
        }
        throw new IllegalArgumentException("Execute aggregate Request failed : The parameter ormPredicates is null.");
    }

    public void beginTransaction() {
        if (!inTransaction()) {
            this.store.beginTransaction();
            return;
        }
        throw new IllegalStateException("The database is already in a transaction.");
    }

    public boolean inTransaction() {
        return this.store.isInTransaction();
    }

    public void rollback() {
        if (inTransaction()) {
            this.store.endTransaction();
            return;
        }
        throw new IllegalStateException("Execute rollback failed : The database " + this.name + " is not in a transaction.");
    }

    public void commit() {
        if (inTransaction()) {
            this.store.markAsCommit();
            this.store.endTransaction();
            return;
        }
        throw new IllegalStateException("Execute commit failed : The database " + this.name + " is not in a transaction");
    }

    public boolean backup(String str) {
        return this.store.backup(str);
    }

    public boolean restore(String str) {
        return this.store.restore(str);
    }

    public boolean backup(String str, byte[] bArr) {
        return this.store.backup(str, bArr);
    }

    public boolean restore(String str, byte[] bArr, byte[] bArr2) {
        return this.store.restore(str, bArr, bArr2);
    }

    public long count(OrmPredicates ormPredicates) {
        return this.store.count(createFromOrmPredicates(ormPredicates));
    }

    private RdbPredicates createFromOrmPredicates(OrmPredicates ormPredicates) {
        if (ormPredicates != null) {
            RdbPredicates rdbPredicates = new RdbPredicates(getHelper(ormPredicates.getEntityName()).getTableName());
            PredicatesUtils.setWhereClauseAndArgs(rdbPredicates, ormPredicates.getWhereClause(), ormPredicates.getWhereArgs());
            PredicatesUtils.setAttributes(rdbPredicates, ormPredicates.isDistinct(), ormPredicates.getIndex(), ormPredicates.getGroup(), ormPredicates.getOrder(), ormPredicates.getLimit(), ormPredicates.getOffset());
            List<String> joinTableNames = getJoinTableNames(ormPredicates);
            if (joinTableNames != null && !joinTableNames.isEmpty()) {
                rdbPredicates.setJoinTypes(ormPredicates.getJoinTypes());
                rdbPredicates.setJoinConditions(ormPredicates.getJoinConditions());
                rdbPredicates.setJoinCount(ormPredicates.getJoinCount());
                rdbPredicates.setJoinTableNames(joinTableNames);
            }
            return rdbPredicates;
        }
        throw new IllegalArgumentException("ormPredicates cannot be null.");
    }

    private List<String> getJoinTableNames(OrmPredicates ormPredicates) {
        if (ormPredicates.getJoinEntityNames() == null || ormPredicates.getJoinEntityNames().isEmpty()) {
            return new ArrayList();
        }
        return (List) ormPredicates.getJoinEntityNames().stream().map(new Function() {
            /* class ohos.data.orm.impl.$$Lambda$OrmStore$Vm879FzqHHDAbHyduw1PACZDs */

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return OrmStore.this.lambda$getJoinTableNames$0$OrmStore((String) obj);
            }
        }).collect(Collectors.toList());
    }

    public /* synthetic */ String lambda$getJoinTableNames$0$OrmStore(String str) {
        return getHelper(str).getTableName();
    }
}
