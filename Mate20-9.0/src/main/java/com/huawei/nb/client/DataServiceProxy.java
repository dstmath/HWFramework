package com.huawei.nb.client;

import android.content.Context;
import android.database.Cursor;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.nb.authority.GroupMember;
import com.huawei.nb.container.ObjectContainer;
import com.huawei.nb.model.authority.ZDatabaseAuthorityGrant;
import com.huawei.nb.model.authority.ZFieldAuthorityGrant;
import com.huawei.nb.model.authority.ZGroupGrant;
import com.huawei.nb.model.authority.ZTableAuthorityGrant;
import com.huawei.nb.model.authority.ZTupleAuthorityGrant;
import com.huawei.nb.model.meta.DataLifeCycle;
import com.huawei.nb.notification.DSLocalObservable;
import com.huawei.nb.notification.ModelObserver;
import com.huawei.nb.notification.ModelObserverInfo;
import com.huawei.nb.notification.ObserverType;
import com.huawei.nb.notification.RecordObserver;
import com.huawei.nb.odmfadapter.AObjectContextAdapter;
import com.huawei.nb.odmfadapter.OdmfHelper;
import com.huawei.nb.query.IQuery;
import com.huawei.nb.query.Query;
import com.huawei.nb.query.QueryContainer;
import com.huawei.nb.query.RawQuery;
import com.huawei.nb.query.RelationshipQuery;
import com.huawei.nb.query.bulkcursor.BulkCursorDescriptor;
import com.huawei.nb.service.IDataServiceCall;
import com.huawei.nb.utils.logger.DSLog;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.core.ManagedObject;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DataServiceProxy extends Proxy<IDataServiceCall> implements IClient {
    private static final int ADD_SINGLE_DLM_ACTION = 1;
    private static final String DATA_SERVICE_ACTION = "com.huawei.nb.service.DataService.START";
    private static final String DATA_SERVICE_NAME = "NaturalBase Data Service";
    private static final int DELETE_SINGLE_DLM_ACTION = 2;
    private static final int QUERY_DLM_ACTION = 0;
    private final ThreadLocal<ErrorInfo> errorInfoThread = new ThreadLocal<>();
    private final OdmfHelper odmfHelper = new OdmfHelper(new AObjectContextAdapter(this));

    public DataServiceProxy(Context context) {
        super(context, DATA_SERVICE_NAME, DATA_SERVICE_ACTION);
    }

    public boolean connect() {
        return connect(null);
    }

    public boolean disconnect() {
        return super.disconnectInner();
    }

    /* access modifiers changed from: protected */
    public IDataServiceCall asInterface(IBinder binder) {
        return IDataServiceCall.Stub.asInterface(binder);
    }

    /* access modifiers changed from: protected */
    public DSLocalObservable newLocalObservable() {
        return new DSLocalObservable(this.callbackManager);
    }

    public <T extends AManagedObject> List<T> executeQuery(RelationshipQuery query) {
        if (query == null) {
            setAndPrintError(1, "Failed to execute relationship query, error: null input query.", new Object[0]);
            return null;
        }
        return this.odmfHelper.assignObjectContext(executeQueryDirect(query));
    }

    public <T extends AManagedObject> T executeSingleQuery(Query query) {
        if (query == null || !query.isValid()) {
            setAndPrintError(1, "Failed to execute single query, error: invalid input query.", new Object[0]);
            return null;
        }
        List<T> entities = executeQuery(query);
        if (entities == null || entities.isEmpty()) {
            return null;
        }
        return (AManagedObject) entities.get(0);
    }

    public <T extends AManagedObject> List<T> executeQuery(Query query) {
        if (query == null || !query.isValid()) {
            setAndPrintError(1, "Failed to execute query, error: invalid input query.", new Object[0]);
            return null;
        }
        List entities = this.odmfHelper.parseCursor(query.getEntityName(), executeCursorQueryDirect(query));
        if (entities != null) {
            return entities;
        }
        setAndPrintError(4, "Failed to execute query, error: service operation failed.", new Object[0]);
        return entities;
    }

    public Cursor executeRawQuery(RawQuery query) {
        if (query == null) {
            setAndPrintError(1, "Failed to execute raw query, error: null input query.", new Object[0]);
            return null;
        }
        BulkCursorDescriptor descriptor = executeCursorQueryDirect(query);
        if (descriptor != null) {
            return this.odmfHelper.wrapCursor(descriptor);
        }
        setAndPrintError(4, "Failed to execute raw query, error: service operation failed.", new Object[0]);
        return null;
    }

    private Object executeAggregateQuery(Query query) {
        List result = executeQueryDirect(query);
        if (result == null || result.isEmpty() || !(result.get(0) instanceof List) || ((List) result.get(0)).isEmpty()) {
            return null;
        }
        return ((List) result.get(0)).get(0);
    }

    private List executeQueryDirect(IQuery query) {
        if (this.remote == null) {
            setAndPrintError(2, "Failed to execute query direct, error: not connected to data service.", new Object[0]);
            return null;
        }
        try {
            ObjectContainer oc = ((IDataServiceCall) this.remote).executeQueryDirect(new QueryContainer(query, this.pkgName));
            if (oc != null) {
                return oc.get();
            }
            setAndPrintError(4, "Failed to execute query direct, error: service operation failed.", new Object[0]);
            return null;
        } catch (RemoteException | RuntimeException e) {
            setAndPrintError(3, "Failed to execute query direct, error: %s.", e.getMessage());
            return null;
        }
    }

    private BulkCursorDescriptor executeCursorQueryDirect(IQuery query) {
        if (this.remote == null) {
            setAndPrintError(2, "Failed to execute cursor query, error: not connected to data service.", new Object[0]);
            return null;
        }
        try {
            return ((IDataServiceCall) this.remote).executeCursorQueryDirect(new QueryContainer(query, this.pkgName));
        } catch (RemoteException | RuntimeException e) {
            setAndPrintError(3, "Failed to execute cursor query, error: %s.", e.getMessage());
            return null;
        }
    }

    public long executeCountQuery(Query query) {
        if (query == null || !query.isValid()) {
            setAndPrintError(1, "Failed to execute count query, error: invalid input query.", new Object[0]);
            return -1;
        } else if (2 != query.getAggregateType()) {
            setAndPrintError(1, "Failed to execute count query, error: count query parameter does not match COUNT.", new Object[0]);
            return -1;
        } else {
            Object ret = executeAggregateQuery(query);
            if (ret != null) {
                return ((Long) ret).longValue();
            }
            return -1;
        }
    }

    public Object executeMaxQuery(Query query) {
        if (query == null || !query.isValid()) {
            setAndPrintError(1, "Failed to execute max query, error: invalid input query.", new Object[0]);
            return null;
        } else if (query.getAggregateType() == 0) {
            return executeAggregateQuery(query);
        } else {
            setAndPrintError(1, "Failed to execute max query, error: maximum query parameter does not match MAX.", new Object[0]);
            return null;
        }
    }

    public Object executeMinQuery(Query query) {
        if (query == null || !query.isValid()) {
            setAndPrintError(1, "Failed to execute min query, error: invalid input query.", new Object[0]);
            return null;
        } else if (1 == query.getAggregateType()) {
            return executeAggregateQuery(query);
        } else {
            setAndPrintError(1, "Failed to execute min query, error: minimum query parameter does not match MIN.", new Object[0]);
            return null;
        }
    }

    public Double executeAvgQuery(Query query) {
        if (query == null || !query.isValid()) {
            setAndPrintError(1, "Failed to execute avg query, error: invalid input query.", new Object[0]);
            return null;
        } else if (3 == query.getAggregateType()) {
            return (Double) executeAggregateQuery(query);
        } else {
            setAndPrintError(1, "Failed to execute avg query, error: average query parameter does not match AVG.", new Object[0]);
            return null;
        }
    }

    public Object executeSumQuery(Query query) {
        if (query == null || !query.isValid()) {
            setAndPrintError(1, "Failed to execute sum query, error: invalid input query.", new Object[0]);
            return null;
        } else if (4 == query.getAggregateType()) {
            return executeAggregateQuery(query);
        } else {
            setAndPrintError(1, "Failed to execute sum query, error: sum query parameter does not match SUM.", new Object[0]);
            return null;
        }
    }

    public Cursor executeFusionQuery(RawQuery fusionQuery) {
        return executeRawQuery(fusionQuery);
    }

    public <T extends AManagedObject> T executeInsert(T entity) {
        if (entity == null) {
            setAndPrintError(1, "Failed to execute insert, error: null entity to insert.", new Object[0]);
            return null;
        }
        List<T> insertedEntities = executeInsert(Arrays.asList(new AManagedObject[]{entity}));
        if (insertedEntities == null || insertedEntities.isEmpty()) {
            return null;
        }
        return (AManagedObject) insertedEntities.get(0);
    }

    public <T extends AManagedObject> List<T> executeInsert(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            setAndPrintError(1, "Failed to execute insert, error: null or empty entity list to insert.", new Object[0]);
            return null;
        } else if (this.remote == null) {
            setAndPrintError(2, "Failed to execute insert, error: not connected to data service.", new Object[0]);
            return null;
        } else {
            this.odmfHelper.presetUriString(entities);
            try {
                ObjectContainer oc = ((IDataServiceCall) this.remote).executeInsertDirect(new ObjectContainer<>(((AManagedObject) entities.get(0)).getClass(), entities, this.pkgName));
                if (oc != null && oc.get() != null && !oc.get().isEmpty()) {
                    return this.odmfHelper.assignObjectContext(oc.get());
                }
                setAndPrintError(4, "Failed to execute insert, error: service operation failed.", new Object[0]);
                return null;
            } catch (RemoteException | RuntimeException e) {
                setAndPrintError(3, "Failed to execute insert, error: %s.", e.getMessage());
                return null;
            }
        }
    }

    public <T extends AManagedObject> boolean executeInsertEfficiently(T entity) {
        if (entity == null) {
            setAndPrintError(1, "Failed to execute executeInsertEfficiently, error: null entity to insert.", new Object[0]);
            return false;
        }
        return executeInsertEfficiently(Arrays.asList(new AManagedObject[]{entity}));
    }

    public <T extends AManagedObject> boolean executeInsertEfficiently(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            setAndPrintError(1, "Failed to execute executeInsertEfficiently, error: null or empty entity list to insert.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            setAndPrintError(2, "Failed to execute executeInsertEfficiently, error: not connected to data service.", new Object[0]);
            return false;
        } else {
            try {
                if (((IDataServiceCall) this.remote).executeInsertEfficiently(new ObjectContainer<>(((AManagedObject) entities.get(0)).getClass(), entities, this.pkgName)) >= 0) {
                    return true;
                }
                setAndPrintError(4, "Failed to execute executeInsertEfficiently, error: service operation failed.", new Object[0]);
                return false;
            } catch (RemoteException | RuntimeException e) {
                setAndPrintError(3, "Failed to execute executeInsertEfficiently, error: %s.", e.getMessage());
                return false;
            }
        }
    }

    public <T extends AManagedObject> boolean executeUpdate(T entity) {
        if (entity == null) {
            setAndPrintError(1, "Failed to execute update, error: null entity to update.", new Object[0]);
            return false;
        }
        return executeUpdate(Arrays.asList(new AManagedObject[]{entity}));
    }

    public <T extends AManagedObject> boolean executeUpdate(List<T> entities) {
        boolean z;
        if (entities == null || entities.isEmpty()) {
            setAndPrintError(1, "Failed to execute update, error: null or empty entity list to update.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            setAndPrintError(2, "Failed to execute update, error: not connected to data service.", new Object[0]);
            return false;
        } else {
            int updatedCount = -1;
            try {
                updatedCount = ((IDataServiceCall) this.remote).executeUpdateDirect(new ObjectContainer<>(((AManagedObject) entities.get(0)).getClass(), entities, this.pkgName));
            } catch (RemoteException | RuntimeException e) {
                setAndPrintError(3, "Failed to execute update, error: %s.", e.getMessage());
            }
            if (updatedCount > 0) {
                z = true;
            } else {
                z = false;
            }
            return z;
        }
    }

    public <T extends AManagedObject> boolean executeDelete(T entity) {
        if (entity == null) {
            setAndPrintError(1, "Failed to execute delete, error: null entity to delete.", new Object[0]);
            return false;
        }
        return executeDelete(Arrays.asList(new AManagedObject[]{entity}));
    }

    public <T extends AManagedObject> boolean executeDelete(List<T> entities) {
        boolean z;
        if (entities == null || entities.isEmpty()) {
            setAndPrintError(1, "Failed to execute delete, error: null or empty entity list to delete.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            setAndPrintError(2, "Failed to execute delete, error: not connected to data service.", new Object[0]);
            return false;
        } else {
            int deletedCount = -1;
            try {
                deletedCount = ((IDataServiceCall) this.remote).executeDeleteDirect(new ObjectContainer<>(((AManagedObject) entities.get(0)).getClass(), entities, this.pkgName), false);
            } catch (RemoteException | RuntimeException e) {
                setAndPrintError(3, "Failed to execute delete, error:%s.", e.getMessage());
            }
            if (deletedCount > 0) {
                z = true;
            } else {
                z = false;
            }
            return z;
        }
    }

    public <T extends AManagedObject> boolean executeDeleteAll(Class<T> clazz) {
        boolean z;
        if (clazz == null) {
            setAndPrintError(1, "Failed to execute deleteAll, error: null entity class to delete.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            setAndPrintError(2, "Failed to execute deleteAll, error: not connected to data service.", new Object[0]);
            return false;
        } else {
            int deletedCount = -1;
            try {
                deletedCount = ((IDataServiceCall) this.remote).executeDeleteDirect(new ObjectContainer<>(clazz, null, this.pkgName), true);
            } catch (RemoteException | RuntimeException e) {
                setAndPrintError(3, "Failed to execute deleteAll, error: %s.", e.getMessage());
            }
            if (deletedCount >= 0) {
                z = true;
            } else {
                z = false;
            }
            return z;
        }
    }

    public <T extends AManagedObject> boolean subscribe(Class<T> clazz, ObserverType type, ModelObserver observer) {
        if (clazz == null || type == null || observer == null) {
            setAndPrintError(2, "Failed to execute subscribe, error: null observer information to subscribe.", new Object[0]);
            return false;
        } else if (type != ObserverType.OBSERVER_RECORD || (observer instanceof RecordObserver)) {
            ModelObserverInfo observerInfo = new ModelObserverInfo(type, clazz, this.pkgName);
            observerInfo.setProxyId(Integer.valueOf(getId()));
            return ((DSLocalObservable) this.localObservable).registerObserver(observerInfo, observer);
        } else {
            setAndPrintError(1, "Failed to execute subscribe, error: invalid record observer.", new Object[0]);
            return false;
        }
    }

    public <T extends AManagedObject> boolean unSubscribe(Class<T> clazz, ObserverType type, ModelObserver observer) {
        if (clazz == null || type == null || observer == null) {
            setAndPrintError(1, "Failed to execute un-subscribe, error: null observer information to un-subscribe.", new Object[0]);
            return false;
        }
        ModelObserverInfo observerInfo = new ModelObserverInfo(type, clazz, this.pkgName);
        observerInfo.setProxyId(Integer.valueOf(getId()));
        return ((DSLocalObservable) this.localObservable).unregisterObserver(observerInfo, observer);
    }

    public String getApiVersion() {
        return "2.12.1";
    }

    public int getApiVersionCode() {
        return 21;
    }

    public String getDatabaseVersion(String databaseName) {
        if (databaseName == null || databaseName.isEmpty()) {
            setAndPrintError(1, "Failed to get database version, error: invalid database name.", new Object[0]);
            return null;
        } else if (this.remote == null) {
            setAndPrintError(2, "Failed to get database version, error: not connected to data service.", new Object[0]);
            return null;
        } else {
            try {
                return ((IDataServiceCall) this.remote).getDatabaseVersion(databaseName);
            } catch (RemoteException | RuntimeException e) {
                setAndPrintError(3, "Failed to get database version, error: %s.", e.getMessage());
                return null;
            }
        }
    }

    public boolean clearUserData(String databaseName, int type) {
        if (databaseName == null || databaseName.isEmpty()) {
            setAndPrintError(1, "Failed to clear user data, error: invalid database name.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            setAndPrintError(2, "Failed to clear user data, error: not connected to data service.", new Object[0]);
            return false;
        } else {
            try {
                return ((IDataServiceCall) this.remote).clearUserData(databaseName, type);
            } catch (RemoteException | RuntimeException e) {
                setAndPrintError(3, "Failed to clear user data, error: %s.", e.getMessage());
                return false;
            }
        }
    }

    private void setAndPrintError(int errorCode, String msg, Object... args) {
        String temp = String.format(msg, args);
        DSLog.e(temp, new Object[0]);
        this.errorInfoThread.set(new ErrorInfo(errorCode, temp));
    }

    public ErrorInfo getErrorInfo() {
        ErrorInfo errorInfo = this.errorInfoThread.get();
        return errorInfo != null ? errorInfo : new ErrorInfo(0, "");
    }

    public boolean grantDatabaseAuthority(String databaseName, String grantPkgName, int authority) {
        if (databaseName == null || grantPkgName == null || databaseName.isEmpty() || grantPkgName.isEmpty()) {
            setAndPrintError(1, "Failed to grant database authority, error: null input parameter.", new Object[0]);
            return false;
        }
        ZDatabaseAuthorityGrant databaseAuthorityGrant = new ZDatabaseAuthorityGrant();
        databaseAuthorityGrant.setDbName(databaseName);
        databaseAuthorityGrant.setPackageName(grantPkgName);
        databaseAuthorityGrant.setAuthority(Integer.valueOf(authority));
        if (handleAuthorityGrant(new ObjectContainer<>(databaseAuthorityGrant.getClass(), Arrays.asList(new ManagedObject[]{databaseAuthorityGrant}), this.pkgName, databaseName), 2)) {
            return true;
        }
        setAndPrintError(4, "Failed to grant database authority, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean revokeDatabaseAuthority(String databaseName, String revokePkgName) {
        if (databaseName == null || revokePkgName == null || databaseName.isEmpty() || revokePkgName.isEmpty()) {
            setAndPrintError(1, "Failed to revoke database authority, error: null input parameter.", new Object[0]);
            return false;
        }
        ZDatabaseAuthorityGrant databaseAuthorityGrant = new ZDatabaseAuthorityGrant();
        databaseAuthorityGrant.setDbName(databaseName);
        databaseAuthorityGrant.setPackageName(revokePkgName);
        if (handleAuthorityGrant(new ObjectContainer<>(databaseAuthorityGrant.getClass(), Arrays.asList(new ManagedObject[]{databaseAuthorityGrant}), this.pkgName, databaseName), 3)) {
            return true;
        }
        setAndPrintError(4, "Failed to revoke database authority, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean grantTableAuthority(Class clazz, String grantPkgName, int authority) {
        if (clazz == null || grantPkgName == null) {
            setAndPrintError(1, "Failed to grant table authority, error: null input parameter.", new Object[0]);
            return false;
        }
        String dbName = reflectToGetDatabaseName(clazz);
        if (dbName == null) {
            return false;
        }
        ZTableAuthorityGrant tableAuthorityGrant = new ZTableAuthorityGrant();
        tableAuthorityGrant.setTableName(clazz.getSimpleName());
        tableAuthorityGrant.setPackageName(grantPkgName);
        tableAuthorityGrant.setAuthority(Integer.valueOf(authority));
        if (handleAuthorityGrant(new ObjectContainer<>(tableAuthorityGrant.getClass(), Arrays.asList(new ManagedObject[]{tableAuthorityGrant}), this.pkgName, dbName), 2)) {
            return true;
        }
        setAndPrintError(4, "Failed to grant table authority, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean revokeTableAuthority(Class clazz, String revokePkgName) {
        if (clazz == null || revokePkgName == null) {
            setAndPrintError(1, "Failed to revoke table authority, error: null input parameter.", new Object[0]);
            return false;
        }
        String dbName = reflectToGetDatabaseName(clazz);
        if (dbName == null) {
            return false;
        }
        ZTableAuthorityGrant tableAuthorityGrant = new ZTableAuthorityGrant();
        tableAuthorityGrant.setTableName(clazz.getSimpleName());
        tableAuthorityGrant.setPackageName(revokePkgName);
        if (handleAuthorityGrant(new ObjectContainer<>(tableAuthorityGrant.getClass(), Arrays.asList(new ManagedObject[]{tableAuthorityGrant}), this.pkgName, dbName), 3)) {
            return true;
        }
        setAndPrintError(4, "Failed to revoke table authority, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean grantFieldAuthority(Class clazz, String fieldName, String grantPkgName, int authority) {
        if (clazz == null || grantPkgName == null) {
            setAndPrintError(1, "Failed to grant field authority, error: null input parameter.", new Object[0]);
            return false;
        }
        String dbName = reflectToGetDatabaseName(clazz);
        if (dbName == null) {
            return false;
        }
        ZFieldAuthorityGrant fieldAuthorityGrant = new ZFieldAuthorityGrant();
        fieldAuthorityGrant.setTableName(clazz.getSimpleName());
        fieldAuthorityGrant.setPackageName(grantPkgName);
        fieldAuthorityGrant.setFieldName(fieldName);
        fieldAuthorityGrant.setAuthority(Integer.valueOf(authority));
        if (handleAuthorityGrant(new ObjectContainer<>(fieldAuthorityGrant.getClass(), Arrays.asList(new ManagedObject[]{fieldAuthorityGrant}), this.pkgName, dbName), 2)) {
            return true;
        }
        setAndPrintError(4, "Failed to grant field authority, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean revokeFieldAuthority(Class clazz, String fieldName, String revokePkgName) {
        if (clazz == null || revokePkgName == null) {
            setAndPrintError(1, "Failed to revoke field authority, error: null input parameter.", new Object[0]);
            return false;
        }
        String dbName = reflectToGetDatabaseName(clazz);
        if (dbName == null) {
            return false;
        }
        ZFieldAuthorityGrant fieldAuthorityGrant = new ZFieldAuthorityGrant();
        fieldAuthorityGrant.setTableName(clazz.getSimpleName());
        fieldAuthorityGrant.setFieldName(fieldName);
        fieldAuthorityGrant.setPackageName(revokePkgName);
        if (handleAuthorityGrant(new ObjectContainer<>(fieldAuthorityGrant.getClass(), Arrays.asList(new ManagedObject[]{fieldAuthorityGrant}), this.pkgName, dbName), 3)) {
            return true;
        }
        setAndPrintError(4, "Failed to revoke field authority, error: service operation failed.", new Object[0]);
        return false;
    }

    /* JADX WARNING: type inference failed for: r4v0, types: [java.util.List<T>, java.lang.String] */
    public <T extends ManagedObject> List<T> executeInsertWithGroup(List<T> entities, String groupName) {
        ? r4 = 0;
        if (entities == null || entities.isEmpty() || groupName == null || groupName.isEmpty()) {
            setAndPrintError(1, "Failed to insert object with group, error: null or empty entity list to insert.", new Object[0]);
            return r4;
        }
        try {
            ObjectContainer oc = ((IDataServiceCall) this.remote).executeInsertDirect(new ObjectContainer<>(entities.get(0).getClass(), entities, this.pkgName, r4, groupName));
            if (oc != null && oc.get() != null && !oc.get().isEmpty()) {
                return this.odmfHelper.assignObjectContext(oc.get());
            }
            setAndPrintError(4, "Failed to insert object with group, error: service operation failed.", new Object[0]);
            return r4;
        } catch (RemoteException | RuntimeException e) {
            setAndPrintError(3, "Failed to insert object with group, error: %s.", e.getMessage());
            return r4;
        }
    }

    public <T extends ManagedObject> boolean moveObjectToGroup(List<T> entities, String groupName) {
        if (entities == null || entities.isEmpty() || groupName == null || groupName.isEmpty()) {
            setAndPrintError(1, "Failed to move object to group, error: null or empty entity list to insert.", new Object[0]);
            return false;
        }
        String tbName = ((ManagedObject) entities.get(0)).getClass().getSimpleName();
        List<ManagedObject> objects = new ArrayList<>();
        int size = entities.size();
        for (int i = 0; i < size; i++) {
            ZTupleAuthorityGrant tupleAuthorityGrant = new ZTupleAuthorityGrant();
            tupleAuthorityGrant.setTableName(tbName);
            tupleAuthorityGrant.setPackageName(this.pkgName);
            tupleAuthorityGrant.setTupleId(((ManagedObject) entities.get(i)).getRowId());
            tupleAuthorityGrant.setReserved(groupName);
            objects.add(tupleAuthorityGrant);
        }
        if (handleAuthorityGrant(new ObjectContainer<>(objects.get(0).getClass(), objects, this.pkgName, ((ManagedObject) entities.get(0)).getDatabaseName()), 1)) {
            return true;
        }
        setAndPrintError(4, "Failed to move object to group, error: service operation failed.", new Object[0]);
        return false;
    }

    public <T extends ManagedObject> boolean removeObjectFromGroup(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            setAndPrintError(1, "Failed to remove object from group, error: null or empty entity list to insert.", new Object[0]);
            return false;
        }
        String tbName = ((ManagedObject) entities.get(0)).getClass().getSimpleName();
        List<ManagedObject> objects = new ArrayList<>();
        int size = entities.size();
        for (int i = 0; i < size; i++) {
            ZTupleAuthorityGrant tupleAuthorityGrant = new ZTupleAuthorityGrant();
            tupleAuthorityGrant.setTableName(tbName);
            tupleAuthorityGrant.setPackageName(this.pkgName);
            tupleAuthorityGrant.setTupleId(((ManagedObject) entities.get(i)).getRowId());
            objects.add(tupleAuthorityGrant);
        }
        if (handleAuthorityGrant(new ObjectContainer<>(objects.get(0).getClass(), objects, this.pkgName, ((ManagedObject) entities.get(0)).getDatabaseName()), 1)) {
            return true;
        }
        setAndPrintError(4, "Failed to remove object from group, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean createGroup(Class clazz, String groupName) {
        if (clazz == null || groupName == null || groupName.isEmpty()) {
            setAndPrintError(1, "Failed to execute create group, error: null input parameter.", new Object[0]);
            return false;
        }
        ZGroupGrant group = new ZGroupGrant();
        group.setGroupName(groupName);
        group.setTableName(clazz.getSimpleName());
        group.setOwner(this.pkgName);
        group.setIsGroupIdentifier(true);
        if (handleAuthorityGrant(new ObjectContainer<>(group.getClass(), Arrays.asList(new ManagedObject[]{group}), this.pkgName, reflectToGetDatabaseName(clazz)), 2)) {
            return true;
        }
        setAndPrintError(4, "Failed to create group, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean dropGroup(Class clazz, String groupName) {
        if (clazz == null || groupName == null || groupName.isEmpty()) {
            setAndPrintError(1, "Failed to drop group, error: null input parameter.", new Object[0]);
            return false;
        }
        ZGroupGrant group = new ZGroupGrant();
        group.setGroupName(groupName);
        group.setTableName(clazz.getSimpleName());
        group.setOwner(this.pkgName);
        group.setIsGroupIdentifier(true);
        if (handleAuthorityGrant(new ObjectContainer<>(group.getClass(), Arrays.asList(new ManagedObject[]{group}), this.pkgName, reflectToGetDatabaseName(clazz)), 3)) {
            return true;
        }
        setAndPrintError(4, "Failed to drop group, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean addMemberToGroup(Class clazz, String groupName, List<GroupMember> members) {
        if (clazz == null || groupName == null || members == null || groupName.isEmpty() || members.size() == 0) {
            setAndPrintError(1, "Failed to add member to group, error: null input parameter.", new Object[0]);
            return false;
        }
        List<ManagedObject> objects = new ArrayList<>();
        int size = members.size();
        for (int i = 0; i < size; i++) {
            ZGroupGrant group = new ZGroupGrant();
            group.setTableName(clazz.getSimpleName());
            group.setGroupName(groupName);
            group.setOwner(this.pkgName);
            group.setIsGroupIdentifier(false);
            if (members.get(i).getMemberPkgName() == null) {
                setAndPrintError(1, "Failed to add member to group, error: the value in members has none.", new Object[0]);
                return false;
            }
            group.setMember(members.get(i).getMemberPkgName());
            group.setAuthority(Integer.valueOf(members.get(i).getAuthority()));
            objects.add(group);
        }
        if (handleAuthorityGrant(new ObjectContainer<>(objects.get(0).getClass(), objects, this.pkgName, reflectToGetDatabaseName(clazz)), 2)) {
            return true;
        }
        setAndPrintError(4, "Failed to add member to group, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean removeGroupMember(Class clazz, String groupName, List<String> members) {
        if (clazz == null || groupName == null || members == null || groupName.isEmpty() || members.size() == 0) {
            setAndPrintError(1, "Failed to remove member from group, error: null input parameter.", new Object[0]);
            return false;
        }
        List<ManagedObject> objects = new ArrayList<>();
        int size = members.size();
        for (int i = 0; i < size; i++) {
            ZGroupGrant group = new ZGroupGrant();
            group.setGroupName(groupName);
            group.setTableName(clazz.getSimpleName());
            group.setOwner(this.pkgName);
            group.setIsGroupIdentifier(false);
            group.setMember(members.get(i));
            objects.add(group);
        }
        if (handleAuthorityGrant(new ObjectContainer<>(objects.get(0).getClass(), objects, this.pkgName, reflectToGetDatabaseName(clazz)), 3)) {
            return true;
        }
        setAndPrintError(4, "Failed to remove member from group, error: service operation failed.", new Object[0]);
        return false;
    }

    private boolean handleAuthorityGrant(ObjectContainer container, int operationType) {
        try {
            return ((IDataServiceCall) this.remote).handleAuthorityGrant(container, operationType);
        } catch (RemoteException | RuntimeException e) {
            setAndPrintError(3, "Failed to handle authority grant, error: %s.", e.getMessage());
            return false;
        }
    }

    private String reflectToGetDatabaseName(Class clazz) {
        try {
            return (String) clazz.getMethod("getDatabaseName", new Class[0]).invoke(clazz.newInstance(), new Object[0]);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            setAndPrintError(4, "Failed to get database name, error: use reflect to get databaseName failed.", new Object[0]);
            return null;
        }
    }

    public List<DataLifeCycle> queryDataLifeCycleConfig(String dbName, String tableName) {
        List<DataLifeCycle> responseList;
        if (dbName == null || dbName.equals("")) {
            DSLog.e("Failed to query dataLifeCycleConfig, error: param dbName is empty.", new Object[0]);
            return null;
        } else if (tableName == null || tableName.equals("")) {
            DSLog.e("Failed to query dataLifeCycleConfig, error: param tableName is empty.", new Object[0]);
            return null;
        } else {
            DSLog.d("query dataLifeCycleConfig for db[%s] table[%s] pkgname[%s].", dbName, tableName, this.pkgName);
            DataLifeCycle dlc = new DataLifeCycle();
            dlc.setMDBName(dbName);
            dlc.setMTableName(tableName);
            ObjectContainer oc = new ObjectContainer(DataLifeCycle.class, Collections.singletonList(dlc), this.pkgName);
            if (this.remote != null) {
                try {
                    ObjectContainer result = ((IDataServiceCall) this.remote).handleDataLifeCycleConfig(0, oc);
                    if (result != null) {
                        responseList = result.get();
                    } else {
                        responseList = null;
                    }
                    if (responseList != null) {
                        return responseList;
                    }
                    DSLog.e("Failed to query dataLifeCycleConfig, error: query result null with db[%s] table[%s] pkgname[%s].", dbName, tableName, this.pkgName);
                    return null;
                } catch (RemoteException | RuntimeException e) {
                    DSLog.e("Failed to query dataLifeCycleConfig, error: %s.", e.getMessage());
                    return null;
                }
            } else {
                DSLog.e("Failed to query dataLifeCycleConfig, error: date service is null.", new Object[0]);
                return null;
            }
        }
    }

    public int addDataLifeCycleConfig(String dbName, String tableName, String fieldName, int mode, int count) {
        DataLifeCycle dlc = new DataLifeCycle();
        dlc.setMDBName(dbName);
        dlc.setMTableName(tableName);
        dlc.setMFieldName(fieldName);
        dlc.setMMode(Integer.valueOf(mode));
        dlc.setMCount(Integer.valueOf(count));
        ObjectContainer oc = new ObjectContainer(DataLifeCycle.class, Collections.singletonList(dlc), this.pkgName);
        if (this.remote != null) {
            try {
                ObjectContainer resultObj = ((IDataServiceCall) this.remote).handleDataLifeCycleConfig(1, oc);
                if (resultObj == null || resultObj.get() == null || resultObj.get().isEmpty()) {
                    DSLog.e("Failed to add dataLifeCycleConfig, error: add result null with db[%s] table[%s] fieldName[%s] mode[%s] count[%s] pkgname[%s].", dbName, tableName, fieldName, Integer.valueOf(mode), Integer.valueOf(count), this.pkgName);
                    return 4;
                } else if (resultObj.get().get(0) instanceof Integer) {
                    return ((Integer) resultObj.get().get(0)).intValue();
                } else {
                    DSLog.e("Failed to add dataLifeCycleConfig, error: result is not a integer with db[%s] table[%s] fieldName[%s] mode[%s] count[%s] pkgname[%s].", dbName, tableName, fieldName, Integer.valueOf(mode), Integer.valueOf(count), this.pkgName);
                    return 4;
                }
            } catch (RemoteException | RuntimeException e) {
                DSLog.e("Failed to add dataLifeCycleConfig, error: %s.", e.getMessage());
                return 3;
            }
        } else {
            DSLog.e("Failed to add dataLifeCycleConfig, error: date service is null.", new Object[0]);
            return 2;
        }
    }

    public int removeDataLifeCycleConfig(String dbName, String tableName, String fieldName, int mode, int count) {
        DataLifeCycle dlc = new DataLifeCycle();
        dlc.setMDBName(dbName);
        dlc.setMTableName(tableName);
        dlc.setMFieldName(fieldName);
        dlc.setMMode(Integer.valueOf(mode));
        dlc.setMCount(Integer.valueOf(count));
        ObjectContainer oc = new ObjectContainer(DataLifeCycle.class, Collections.singletonList(dlc), this.pkgName);
        if (this.remote != null) {
            try {
                ObjectContainer resultObj = ((IDataServiceCall) this.remote).handleDataLifeCycleConfig(2, oc);
                if (resultObj == null || resultObj.get() == null || resultObj.get().isEmpty()) {
                    DSLog.e("Failed to remove dataLifeCycleConfig, error: add result null with db[%s] table[%s] fieldName[%s] mode[%s] count[%s] pkgname[%s].", dbName, tableName, fieldName, Integer.valueOf(mode), Integer.valueOf(count), this.pkgName);
                    return 4;
                } else if (resultObj.get().get(0) instanceof Integer) {
                    return ((Integer) resultObj.get().get(0)).intValue();
                } else {
                    DSLog.e("Failed to remove dataLifeCycleConfig, error: result is not a integer with db[%s] table[%s] fieldName[%s] mode[%s] count[%s] pkgname[%s].", dbName, tableName, fieldName, Integer.valueOf(mode), Integer.valueOf(count), this.pkgName);
                    return 4;
                }
            } catch (RemoteException | RuntimeException e) {
                DSLog.e("Failed to remove dataLifeCycleConfig, error: %s.", e.getMessage());
                return 3;
            }
        } else {
            DSLog.e("Failed to remove dataLifeCycleConfig, error: date service is null.", new Object[0]);
            return 2;
        }
    }
}
