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
import com.huawei.nb.notification.LocalObservable;
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
import com.huawei.nb.utils.logger.DbLogUtil;
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
    private static final int INVALID_VALUE = -1;
    private static final int QUERY_DLM_ACTION = 0;
    private final ThreadLocal<ErrorInfo> errorInfoThread = new ThreadLocal<>();
    private final OdmfHelper odmfHelper = new OdmfHelper(new AObjectContextAdapter(this));

    public String getApiVersion() {
        return "2.16.2";
    }

    public int getApiVersionCode() {
        return 32;
    }

    public DataServiceProxy(Context context) {
        super(context, DATA_SERVICE_NAME, DATA_SERVICE_ACTION);
    }

    @Override // com.huawei.nb.client.Proxy
    public boolean connect() {
        return connect(null);
    }

    public boolean disconnect() {
        return super.disconnectInner();
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.nb.client.Proxy
    public IDataServiceCall asInterface(IBinder iBinder) {
        return IDataServiceCall.Stub.asInterface(iBinder);
    }

    /* Return type fixed from 'com.huawei.nb.notification.DSLocalObservable' to match base method */
    /* access modifiers changed from: protected */
    @Override // com.huawei.nb.client.Proxy
    public LocalObservable<?, ?, IDataServiceCall> newLocalObservable() {
        return new DSLocalObservable();
    }

    @Override // com.huawei.nb.client.IClient
    public <T extends AManagedObject> List<T> executeQuery(RelationshipQuery relationshipQuery) {
        if (relationshipQuery == null) {
            setAndPrintError(1, "Failed to execute relationship query, error: null input query.", new Object[0]);
            return null;
        }
        return this.odmfHelper.assignObjectContext(executeQueryDirect(relationshipQuery));
    }

    @Override // com.huawei.nb.client.IClient
    public <T extends AManagedObject> T executeSingleQuery(Query query) {
        if (query == null || !query.isValid()) {
            setAndPrintError(1, "Failed to execute single query, error: invalid input query.", new Object[0]);
            return null;
        }
        List<T> executeQuery = executeQuery(query);
        if (executeQuery == null || executeQuery.isEmpty()) {
            return null;
        }
        return executeQuery.get(0);
    }

    @Override // com.huawei.nb.client.IClient
    public <T extends AManagedObject> List<T> executeQuery(Query query) {
        if (query == null || !query.isValid()) {
            setAndPrintError(1, "Failed to execute query, error: invalid input query.", new Object[0]);
            return null;
        }
        List<T> parseCursor = this.odmfHelper.parseCursor(query.getEntityName(), executeCursorQueryDirect(query));
        if (parseCursor == null) {
            setAndPrintError(4, "Failed to execute query, error: service operation failed.", new Object[0]);
        }
        return parseCursor;
    }

    @Override // com.huawei.nb.client.IClient
    public Cursor executeRawQuery(RawQuery rawQuery) {
        if (rawQuery == null) {
            setAndPrintError(1, "Failed to execute raw query, error: null input query.", new Object[0]);
            return null;
        }
        BulkCursorDescriptor executeCursorQueryDirect = executeCursorQueryDirect(rawQuery);
        if (executeCursorQueryDirect != null) {
            return this.odmfHelper.wrapCursor(executeCursorQueryDirect);
        }
        setAndPrintError(4, "Failed to execute raw query, error: service operation failed.", new Object[0]);
        return null;
    }

    private Object executeAggregateQuery(Query query) {
        List executeQueryDirect = executeQueryDirect(query);
        if (executeQueryDirect == null || executeQueryDirect.isEmpty()) {
            return null;
        }
        return executeQueryDirect.get(0);
    }

    private List executeQueryDirect(IQuery iQuery) {
        if (this.remote == null) {
            setAndPrintError(2, "Failed to execute query direct, error: not connected to data service.", new Object[0]);
            return null;
        }
        try {
            ObjectContainer executeQueryDirect = ((IDataServiceCall) this.remote).executeQueryDirect(new QueryContainer(iQuery, this.pkgName));
            if (executeQueryDirect != null) {
                return executeQueryDirect.get();
            }
            setAndPrintError(4, "Failed to execute query direct, error: service operation failed.", new Object[0]);
            return null;
        } catch (RemoteException | RuntimeException e) {
            setAndPrintError(3, "Failed to execute query direct, error: %s.", e.getMessage());
            return null;
        }
    }

    private BulkCursorDescriptor executeCursorQueryDirect(IQuery iQuery) {
        if (this.remote == null) {
            setAndPrintError(2, "Failed to execute cursor query, error: not connected to data service.", new Object[0]);
            return null;
        }
        try {
            return ((IDataServiceCall) this.remote).executeCursorQueryDirect(new QueryContainer(iQuery, this.pkgName));
        } catch (RemoteException | RuntimeException e) {
            setAndPrintError(3, "Failed to execute cursor query, error: %s.", e.getMessage());
            return null;
        }
    }

    @Override // com.huawei.nb.client.IClient
    public long executeCountQuery(Query query) {
        if (query == null || !query.isValid()) {
            setAndPrintError(1, "Failed to execute count query, error: invalid input query.", new Object[0]);
            return -1;
        } else if (2 != query.getAggregateType()) {
            setAndPrintError(1, "Failed to execute count query, error: count query parameter does not match COUNT.", new Object[0]);
            return -1;
        } else {
            Object executeAggregateQuery = executeAggregateQuery(query);
            if (executeAggregateQuery == null) {
                return -1;
            }
            return ((Long) executeAggregateQuery).longValue();
        }
    }

    @Override // com.huawei.nb.client.IClient
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

    @Override // com.huawei.nb.client.IClient
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

    @Override // com.huawei.nb.client.IClient
    public Double executeAvgQuery(Query query) {
        if (query == null || !query.isValid()) {
            setAndPrintError(1, "Failed to execute avg query, error: invalid input query.", new Object[0]);
            return null;
        } else if (3 != query.getAggregateType()) {
            setAndPrintError(1, "Failed to execute avg query, error: average query parameter does not match AVG.", new Object[0]);
            return null;
        } else {
            Object executeAggregateQuery = executeAggregateQuery(query);
            if (executeAggregateQuery instanceof Double) {
                return (Double) executeAggregateQuery;
            }
            return null;
        }
    }

    @Override // com.huawei.nb.client.IClient
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

    @Override // com.huawei.nb.client.IClient
    public Cursor executeFusionQuery(RawQuery rawQuery) {
        return executeRawQuery(rawQuery);
    }

    @Override // com.huawei.nb.client.IClient
    public <T extends AManagedObject> T executeInsert(T t) {
        if (t == null) {
            setAndPrintError(1, "Failed to execute insert, error: null entity to insert.", new Object[0]);
            return null;
        }
        List<T> executeInsert = executeInsert(Arrays.asList(t));
        if (executeInsert == null || executeInsert.isEmpty()) {
            return null;
        }
        return executeInsert.get(0);
    }

    @Override // com.huawei.nb.client.IClient
    public <T extends AManagedObject> List<T> executeInsert(List<T> list) {
        if (list == null || list.isEmpty()) {
            setAndPrintError(1, "Failed to execute insert, error: null or empty entity list to insert.", new Object[0]);
            return null;
        } else if (this.remote == null) {
            setAndPrintError(2, "Failed to execute insert, error: not connected to data service.", new Object[0]);
            return null;
        } else {
            this.odmfHelper.presetUriString(list);
            try {
                ObjectContainer executeInsertDirect = ((IDataServiceCall) this.remote).executeInsertDirect(new ObjectContainer(list.get(0).getClass(), list, this.pkgName));
                if (!(executeInsertDirect == null || executeInsertDirect.get() == null)) {
                    if (!executeInsertDirect.get().isEmpty()) {
                        return this.odmfHelper.assignObjectContext(executeInsertDirect.get());
                    }
                }
                setAndPrintError(4, "Failed to execute insert, error: service operation failed.", new Object[0]);
                return null;
            } catch (RemoteException | RuntimeException e) {
                setAndPrintError(3, "Failed to execute insert, error: %s.", e.getMessage());
                return null;
            }
        }
    }

    public <T extends AManagedObject> boolean executeInsertEfficiently(T t) {
        if (t != null) {
            return executeInsertEfficiently(Arrays.asList(t));
        }
        setAndPrintError(1, "Failed to execute executeInsertEfficiently, error: null entity to insert.", new Object[0]);
        return false;
    }

    public <T extends AManagedObject> boolean executeInsertEfficiently(List<T> list) {
        if (list == null || list.isEmpty()) {
            setAndPrintError(1, "Failed to execute executeInsertEfficiently, error: null or empty entity list to insert.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            setAndPrintError(2, "Failed to execute executeInsertEfficiently, error: not connected to data service.", new Object[0]);
            return false;
        } else {
            try {
                if (((IDataServiceCall) this.remote).executeInsertEfficiently(new ObjectContainer(list.get(0).getClass(), list, this.pkgName)) >= 0) {
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

    @Override // com.huawei.nb.client.IClient
    public <T extends AManagedObject> boolean executeUpdate(T t) {
        if (t != null) {
            return executeUpdate(Arrays.asList(t));
        }
        setAndPrintError(1, "Failed to execute update, error: null entity to update.", new Object[0]);
        return false;
    }

    @Override // com.huawei.nb.client.IClient
    public <T extends AManagedObject> boolean executeUpdate(List<T> list) {
        if (list == null || list.isEmpty()) {
            setAndPrintError(1, "Failed to execute update, error: null or empty entity list to update.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            setAndPrintError(2, "Failed to execute update, error: not connected to data service.", new Object[0]);
            return false;
        } else {
            int i = -1;
            try {
                i = ((IDataServiceCall) this.remote).executeUpdateDirect(new ObjectContainer(list.get(0).getClass(), list, this.pkgName));
            } catch (RemoteException | RuntimeException e) {
                setAndPrintError(3, "Failed to execute update, error: %s.", e.getMessage());
            }
            return i > 0;
        }
    }

    @Override // com.huawei.nb.client.IClient
    public <T extends AManagedObject> boolean executeDelete(T t) {
        if (t != null) {
            return executeDelete(Arrays.asList(t));
        }
        setAndPrintError(1, "Failed to execute delete, error: null entity to delete.", new Object[0]);
        return false;
    }

    @Override // com.huawei.nb.client.IClient
    public <T extends AManagedObject> boolean executeDelete(List<T> list) {
        if (list == null || list.isEmpty()) {
            setAndPrintError(1, "Failed to execute delete, error: null or empty entity list to delete.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            setAndPrintError(2, "Failed to execute delete, error: not connected to data service.", new Object[0]);
            return false;
        } else {
            int i = -1;
            try {
                i = ((IDataServiceCall) this.remote).executeDeleteDirect(new ObjectContainer(list.get(0).getClass(), list, this.pkgName), false);
            } catch (RemoteException | RuntimeException e) {
                setAndPrintError(3, "Failed to execute delete, error:%s.", e.getMessage());
            }
            return i > 0;
        }
    }

    @Override // com.huawei.nb.client.IClient
    public <T extends AManagedObject> boolean executeDeleteAll(Class<T> cls) {
        if (cls == null) {
            setAndPrintError(1, "Failed to execute deleteAll, error: null entity class to delete.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            setAndPrintError(2, "Failed to execute deleteAll, error: not connected to data service.", new Object[0]);
            return false;
        } else {
            int i = -1;
            try {
                i = ((IDataServiceCall) this.remote).executeDeleteDirect(new ObjectContainer(cls, null, this.pkgName), true);
            } catch (RemoteException | RuntimeException e) {
                setAndPrintError(3, "Failed to execute deleteAll, error: %s.", e.getMessage());
            }
            return i >= 0;
        }
    }

    @Override // com.huawei.nb.client.IClient
    public <T extends AManagedObject> boolean subscribe(Class<T> cls, ObserverType observerType, ModelObserver modelObserver) {
        if (cls == null || observerType == null || modelObserver == null) {
            setAndPrintError(2, "Failed to execute subscribe, error: null observer information to subscribe.", new Object[0]);
            return false;
        } else if (observerType != ObserverType.OBSERVER_RECORD || (modelObserver instanceof RecordObserver)) {
            ModelObserverInfo modelObserverInfo = new ModelObserverInfo(observerType, cls, this.pkgName);
            modelObserverInfo.setProxyId(Integer.valueOf(getId()));
            if (this.localObservable instanceof DSLocalObservable) {
                return ((DSLocalObservable) this.localObservable).registerObserver(modelObserverInfo, modelObserver);
            }
            return false;
        } else {
            setAndPrintError(1, "Failed to execute subscribe, error: invalid record observer.", new Object[0]);
            return false;
        }
    }

    @Override // com.huawei.nb.client.IClient
    public <T extends AManagedObject> boolean unSubscribe(Class<T> cls, ObserverType observerType, ModelObserver modelObserver) {
        if (cls == null || observerType == null || modelObserver == null) {
            setAndPrintError(1, "Failed to execute un-subscribe, error: null observer information to un-subscribe.", new Object[0]);
            return false;
        }
        ModelObserverInfo modelObserverInfo = new ModelObserverInfo(observerType, cls, this.pkgName);
        modelObserverInfo.setProxyId(Integer.valueOf(getId()));
        return ((DSLocalObservable) this.localObservable).unregisterObserver(modelObserverInfo, modelObserver);
    }

    public String getDatabaseVersion(String str) {
        if (str == null || str.isEmpty()) {
            setAndPrintError(1, "Failed to get database version, error: invalid database name.", new Object[0]);
            return null;
        } else if (this.remote == null) {
            setAndPrintError(2, "Failed to get database version, error: not connected to data service.", new Object[0]);
            return null;
        } else {
            try {
                return ((IDataServiceCall) this.remote).getDatabaseVersion(str);
            } catch (RemoteException | RuntimeException e) {
                setAndPrintError(3, "Failed to get database version, error: %s.", e.getMessage());
                return null;
            }
        }
    }

    public boolean clearUserData(String str, int i) {
        if (str == null || str.isEmpty()) {
            setAndPrintError(1, "Failed to clear user data, error: invalid database name.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            setAndPrintError(2, "Failed to clear user data, error: not connected to data service.", new Object[0]);
            return false;
        } else {
            try {
                return ((IDataServiceCall) this.remote).clearUserData(str, i);
            } catch (RemoteException | RuntimeException e) {
                setAndPrintError(3, "Failed to clear user data, error: %s.", e.getMessage());
                return false;
            }
        }
    }

    private void setAndPrintError(int i, String str, Object... objArr) {
        String format = String.format(str, objArr);
        DSLog.e(format, new Object[0]);
        this.errorInfoThread.set(new ErrorInfo(i, format));
    }

    public ErrorInfo getErrorInfo() {
        ErrorInfo errorInfo = this.errorInfoThread.get();
        if (errorInfo != null) {
            return errorInfo;
        }
        return new ErrorInfo(0, "");
    }

    public boolean grantDatabaseAuthority(String str, String str2, int i) {
        if (str == null || str2 == null || str.isEmpty() || str2.isEmpty()) {
            setAndPrintError(1, "Failed to grant database authority, error: null input parameter.", new Object[0]);
            return false;
        }
        ZDatabaseAuthorityGrant zDatabaseAuthorityGrant = new ZDatabaseAuthorityGrant();
        zDatabaseAuthorityGrant.setDbName(str);
        zDatabaseAuthorityGrant.setPackageName(str2);
        zDatabaseAuthorityGrant.setAuthority(Integer.valueOf(i));
        if (handleAuthorityGrant(new ObjectContainer(zDatabaseAuthorityGrant.getClass(), Arrays.asList(zDatabaseAuthorityGrant), this.pkgName, str), 2)) {
            return true;
        }
        setAndPrintError(4, "Failed to grant database authority, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean revokeDatabaseAuthority(String str, String str2) {
        if (str == null || str2 == null || str.isEmpty() || str2.isEmpty()) {
            setAndPrintError(1, "Failed to revoke database authority, error: null input parameter.", new Object[0]);
            return false;
        }
        ZDatabaseAuthorityGrant zDatabaseAuthorityGrant = new ZDatabaseAuthorityGrant();
        zDatabaseAuthorityGrant.setDbName(str);
        zDatabaseAuthorityGrant.setPackageName(str2);
        if (handleAuthorityGrant(new ObjectContainer(zDatabaseAuthorityGrant.getClass(), Arrays.asList(zDatabaseAuthorityGrant), this.pkgName, str), 3)) {
            return true;
        }
        setAndPrintError(4, "Failed to revoke database authority, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean grantTableAuthority(Class cls, String str, int i) {
        if (cls == null || str == null) {
            setAndPrintError(1, "Failed to grant table authority, error: null input parameter.", new Object[0]);
            return false;
        }
        String reflectToGetDatabaseName = reflectToGetDatabaseName(cls);
        if (reflectToGetDatabaseName == null) {
            return false;
        }
        ZTableAuthorityGrant zTableAuthorityGrant = new ZTableAuthorityGrant();
        zTableAuthorityGrant.setTableName(cls.getSimpleName());
        zTableAuthorityGrant.setPackageName(str);
        zTableAuthorityGrant.setAuthority(Integer.valueOf(i));
        if (handleAuthorityGrant(new ObjectContainer(zTableAuthorityGrant.getClass(), Arrays.asList(zTableAuthorityGrant), this.pkgName, reflectToGetDatabaseName), 2)) {
            return true;
        }
        setAndPrintError(4, "Failed to grant table authority, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean revokeTableAuthority(Class cls, String str) {
        if (cls == null || str == null) {
            setAndPrintError(1, "Failed to revoke table authority, error: null input parameter.", new Object[0]);
            return false;
        }
        String reflectToGetDatabaseName = reflectToGetDatabaseName(cls);
        if (reflectToGetDatabaseName == null) {
            return false;
        }
        ZTableAuthorityGrant zTableAuthorityGrant = new ZTableAuthorityGrant();
        zTableAuthorityGrant.setTableName(cls.getSimpleName());
        zTableAuthorityGrant.setPackageName(str);
        if (handleAuthorityGrant(new ObjectContainer(zTableAuthorityGrant.getClass(), Arrays.asList(zTableAuthorityGrant), this.pkgName, reflectToGetDatabaseName), 3)) {
            return true;
        }
        setAndPrintError(4, "Failed to revoke table authority, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean grantFieldAuthority(Class cls, String str, String str2, int i) {
        if (cls == null || str2 == null) {
            setAndPrintError(1, "Failed to grant field authority, error: null input parameter.", new Object[0]);
            return false;
        }
        String reflectToGetDatabaseName = reflectToGetDatabaseName(cls);
        if (reflectToGetDatabaseName == null) {
            return false;
        }
        ZFieldAuthorityGrant zFieldAuthorityGrant = new ZFieldAuthorityGrant();
        zFieldAuthorityGrant.setTableName(cls.getSimpleName());
        zFieldAuthorityGrant.setPackageName(str2);
        zFieldAuthorityGrant.setFieldName(str);
        zFieldAuthorityGrant.setAuthority(Integer.valueOf(i));
        if (handleAuthorityGrant(new ObjectContainer(zFieldAuthorityGrant.getClass(), Arrays.asList(zFieldAuthorityGrant), this.pkgName, reflectToGetDatabaseName), 2)) {
            return true;
        }
        setAndPrintError(4, "Failed to grant field authority, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean revokeFieldAuthority(Class cls, String str, String str2) {
        if (cls == null || str2 == null) {
            setAndPrintError(1, "Failed to revoke field authority, error: null input parameter.", new Object[0]);
            return false;
        }
        String reflectToGetDatabaseName = reflectToGetDatabaseName(cls);
        if (reflectToGetDatabaseName == null) {
            return false;
        }
        ZFieldAuthorityGrant zFieldAuthorityGrant = new ZFieldAuthorityGrant();
        zFieldAuthorityGrant.setTableName(cls.getSimpleName());
        zFieldAuthorityGrant.setFieldName(str);
        zFieldAuthorityGrant.setPackageName(str2);
        if (handleAuthorityGrant(new ObjectContainer(zFieldAuthorityGrant.getClass(), Arrays.asList(zFieldAuthorityGrant), this.pkgName, reflectToGetDatabaseName), 3)) {
            return true;
        }
        setAndPrintError(4, "Failed to revoke field authority, error: service operation failed.", new Object[0]);
        return false;
    }

    public <T extends ManagedObject> List<T> executeInsertWithGroup(List<T> list, String str) {
        if (list == null || list.isEmpty() || str == null || str.isEmpty()) {
            setAndPrintError(1, "Failed to insert object with group, error: null or empty entity list to insert.", new Object[0]);
            return null;
        }
        try {
            ObjectContainer executeInsertDirect = ((IDataServiceCall) this.remote).executeInsertDirect(new ObjectContainer(list.get(0).getClass(), list, this.pkgName, null, str));
            if (!(executeInsertDirect == null || executeInsertDirect.get() == null)) {
                if (!executeInsertDirect.get().isEmpty()) {
                    return this.odmfHelper.assignObjectContext(executeInsertDirect.get());
                }
            }
            setAndPrintError(4, "Failed to insert object with group, error: service operation failed.", new Object[0]);
            return null;
        } catch (RemoteException | RuntimeException e) {
            setAndPrintError(3, "Failed to insert object with group, error: %s.", e.getMessage());
            return null;
        }
    }

    public <T extends ManagedObject> boolean moveObjectToGroup(List<T> list, String str) {
        if (list == null || list.isEmpty() || str == null || str.isEmpty()) {
            setAndPrintError(1, "Failed to move object to group, error: null or empty entity list to insert.", new Object[0]);
            return false;
        }
        String simpleName = list.get(0).getClass().getSimpleName();
        ArrayList arrayList = new ArrayList();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ZTupleAuthorityGrant zTupleAuthorityGrant = new ZTupleAuthorityGrant();
            zTupleAuthorityGrant.setTableName(simpleName);
            zTupleAuthorityGrant.setPackageName(this.pkgName);
            zTupleAuthorityGrant.setTupleId(list.get(i).getRowId());
            zTupleAuthorityGrant.setReserved(str);
            arrayList.add(zTupleAuthorityGrant);
        }
        if (handleAuthorityGrant(new ObjectContainer(((ManagedObject) arrayList.get(0)).getClass(), arrayList, this.pkgName, list.get(0).getDatabaseName()), 1)) {
            return true;
        }
        setAndPrintError(4, "Failed to move object to group, error: service operation failed.", new Object[0]);
        return false;
    }

    public <T extends ManagedObject> boolean removeObjectFromGroup(List<T> list) {
        if (list == null || list.isEmpty()) {
            setAndPrintError(1, "Failed to remove object from group, error: null or empty entity list to insert.", new Object[0]);
            return false;
        }
        String simpleName = list.get(0).getClass().getSimpleName();
        ArrayList arrayList = new ArrayList();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ZTupleAuthorityGrant zTupleAuthorityGrant = new ZTupleAuthorityGrant();
            zTupleAuthorityGrant.setTableName(simpleName);
            zTupleAuthorityGrant.setPackageName(this.pkgName);
            zTupleAuthorityGrant.setTupleId(list.get(i).getRowId());
            arrayList.add(zTupleAuthorityGrant);
        }
        if (handleAuthorityGrant(new ObjectContainer(((ManagedObject) arrayList.get(0)).getClass(), arrayList, this.pkgName, list.get(0).getDatabaseName()), 1)) {
            return true;
        }
        setAndPrintError(4, "Failed to remove object from group, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean createGroup(Class cls, String str) {
        if (cls == null || str == null || str.isEmpty()) {
            setAndPrintError(1, "Failed to execute create group, error: null input parameter.", new Object[0]);
            return false;
        }
        ZGroupGrant zGroupGrant = new ZGroupGrant();
        zGroupGrant.setGroupName(str);
        zGroupGrant.setTableName(cls.getSimpleName());
        zGroupGrant.setOwner(this.pkgName);
        zGroupGrant.setIsGroupIdentifier(true);
        if (handleAuthorityGrant(new ObjectContainer(zGroupGrant.getClass(), Arrays.asList(zGroupGrant), this.pkgName, reflectToGetDatabaseName(cls)), 2)) {
            return true;
        }
        setAndPrintError(4, "Failed to create group, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean dropGroup(Class cls, String str) {
        if (cls == null || str == null || str.isEmpty()) {
            setAndPrintError(1, "Failed to drop group, error: null input parameter.", new Object[0]);
            return false;
        }
        ZGroupGrant zGroupGrant = new ZGroupGrant();
        zGroupGrant.setGroupName(str);
        zGroupGrant.setTableName(cls.getSimpleName());
        zGroupGrant.setOwner(this.pkgName);
        zGroupGrant.setIsGroupIdentifier(true);
        if (handleAuthorityGrant(new ObjectContainer(zGroupGrant.getClass(), Arrays.asList(zGroupGrant), this.pkgName, reflectToGetDatabaseName(cls)), 3)) {
            return true;
        }
        setAndPrintError(4, "Failed to drop group, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean addMemberToGroup(Class cls, String str, List<GroupMember> list) {
        if (cls == null || str == null || list == null || str.isEmpty() || list.size() == 0) {
            setAndPrintError(1, "Failed to add member to group, error: null input parameter.", new Object[0]);
            return false;
        }
        ArrayList arrayList = new ArrayList();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ZGroupGrant zGroupGrant = new ZGroupGrant();
            zGroupGrant.setTableName(cls.getSimpleName());
            zGroupGrant.setGroupName(str);
            zGroupGrant.setOwner(this.pkgName);
            zGroupGrant.setIsGroupIdentifier(false);
            if (list.get(i).getMemberPkgName() == null) {
                setAndPrintError(1, "Failed to add member to group, error: the value in members has none.", new Object[0]);
                return false;
            }
            zGroupGrant.setMember(list.get(i).getMemberPkgName());
            zGroupGrant.setAuthority(Integer.valueOf(list.get(i).getAuthority()));
            arrayList.add(zGroupGrant);
        }
        if (handleAuthorityGrant(new ObjectContainer(((ManagedObject) arrayList.get(0)).getClass(), arrayList, this.pkgName, reflectToGetDatabaseName(cls)), 2)) {
            return true;
        }
        setAndPrintError(4, "Failed to add member to group, error: service operation failed.", new Object[0]);
        return false;
    }

    public boolean removeGroupMember(Class cls, String str, List<String> list) {
        if (cls == null || str == null || list == null || str.isEmpty() || list.size() == 0) {
            setAndPrintError(1, "Failed to remove member from group, error: null input parameter.", new Object[0]);
            return false;
        }
        ArrayList arrayList = new ArrayList();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ZGroupGrant zGroupGrant = new ZGroupGrant();
            zGroupGrant.setGroupName(str);
            zGroupGrant.setTableName(cls.getSimpleName());
            zGroupGrant.setOwner(this.pkgName);
            zGroupGrant.setIsGroupIdentifier(false);
            zGroupGrant.setMember(list.get(i));
            arrayList.add(zGroupGrant);
        }
        if (handleAuthorityGrant(new ObjectContainer(((ManagedObject) arrayList.get(0)).getClass(), arrayList, this.pkgName, reflectToGetDatabaseName(cls)), 3)) {
            return true;
        }
        setAndPrintError(4, "Failed to remove member from group, error: service operation failed.", new Object[0]);
        return false;
    }

    private boolean handleAuthorityGrant(ObjectContainer objectContainer, int i) {
        try {
            return ((IDataServiceCall) this.remote).handleAuthorityGrant(objectContainer, i);
        } catch (RemoteException | RuntimeException e) {
            setAndPrintError(3, "Failed to handle authority grant, error: %s.", e.getMessage());
            return false;
        }
    }

    private String reflectToGetDatabaseName(Class cls) {
        try {
            return (String) cls.getMethod("getDatabaseName", new Class[0]).invoke(cls.newInstance(), new Object[0]);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException unused) {
            setAndPrintError(4, "Failed to get database name, error: use reflect to get databaseName failed.", new Object[0]);
            return null;
        }
    }

    public List<DataLifeCycle> queryDataLifeCycleConfig(String str, String str2) {
        if (str == null || str.equals("")) {
            DSLog.e("Failed to query dataLifeCycleConfig, error: param dbName is empty.", new Object[0]);
            return null;
        } else if (str2 == null || str2.equals("")) {
            DSLog.e("Failed to query dataLifeCycleConfig, error: param tableName is empty.", new Object[0]);
            return null;
        } else {
            DSLog.d("query dataLifeCycleConfig for db[%s] table[%s] pkgname[%s].", DbLogUtil.getSafeNameForLog(str), DbLogUtil.getSafeNameForLog(str2), this.pkgName);
            DataLifeCycle dataLifeCycle = new DataLifeCycle();
            dataLifeCycle.setMDBName(str);
            dataLifeCycle.setMTableName(str2);
            ObjectContainer objectContainer = new ObjectContainer(DataLifeCycle.class, Collections.singletonList(dataLifeCycle), this.pkgName);
            if (this.remote != null) {
                try {
                    ObjectContainer handleDataLifeCycleConfig = ((IDataServiceCall) this.remote).handleDataLifeCycleConfig(0, objectContainer);
                    List<DataLifeCycle> list = handleDataLifeCycleConfig != null ? handleDataLifeCycleConfig.get() : null;
                    if (list != null) {
                        return list;
                    }
                    DSLog.e("Failed to query dataLifeCycleConfig, error: query result null with db[%s] table[%s] pkgname[%s].", DbLogUtil.getSafeNameForLog(str), DbLogUtil.getSafeNameForLog(str2), this.pkgName);
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

    public int addDataLifeCycleConfig(String str, String str2, String str3, int i, int i2) {
        DataLifeCycle dataLifeCycle = new DataLifeCycle();
        dataLifeCycle.setMDBName(str);
        dataLifeCycle.setMTableName(str2);
        dataLifeCycle.setMFieldName(str3);
        dataLifeCycle.setMMode(Integer.valueOf(i));
        dataLifeCycle.setMCount(Integer.valueOf(i2));
        ObjectContainer objectContainer = new ObjectContainer(DataLifeCycle.class, Collections.singletonList(dataLifeCycle), this.pkgName);
        if (this.remote != null) {
            try {
                ObjectContainer handleDataLifeCycleConfig = ((IDataServiceCall) this.remote).handleDataLifeCycleConfig(1, objectContainer);
                if (!(handleDataLifeCycleConfig == null || handleDataLifeCycleConfig.get() == null)) {
                    if (!handleDataLifeCycleConfig.get().isEmpty()) {
                        if (handleDataLifeCycleConfig.get().get(0) instanceof Integer) {
                            return ((Integer) handleDataLifeCycleConfig.get().get(0)).intValue();
                        }
                        DSLog.e("Failed to add dataLifeCycleConfig, error: result is not a integer with db[%s] table[%s] fieldName[%s] mode[%s] count[%s] pkgname[%s].", DbLogUtil.getSafeNameForLog(str), DbLogUtil.getSafeNameForLog(str2), DbLogUtil.getSafeNameForLog(str3), Integer.valueOf(i), Integer.valueOf(i2), this.pkgName);
                        return 4;
                    }
                }
                DSLog.e("Failed to add dataLifeCycleConfig, error: add result null with db[%s] table[%s] fieldName[%s] mode[%s] count[%s] pkgname[%s].", DbLogUtil.getSafeNameForLog(str), DbLogUtil.getSafeNameForLog(str2), DbLogUtil.getSafeNameForLog(str3), Integer.valueOf(i), Integer.valueOf(i2), this.pkgName);
                return 4;
            } catch (RemoteException | RuntimeException e) {
                DSLog.e("Failed to add dataLifeCycleConfig, error: %s.", e.getMessage());
                return 3;
            }
        } else {
            DSLog.e("Failed to add dataLifeCycleConfig, error: date service is null.", new Object[0]);
            return 2;
        }
    }

    public int removeDataLifeCycleConfig(String str, String str2, String str3, int i, int i2) {
        DataLifeCycle dataLifeCycle = new DataLifeCycle();
        dataLifeCycle.setMDBName(str);
        dataLifeCycle.setMTableName(str2);
        dataLifeCycle.setMFieldName(str3);
        dataLifeCycle.setMMode(Integer.valueOf(i));
        dataLifeCycle.setMCount(Integer.valueOf(i2));
        ObjectContainer objectContainer = new ObjectContainer(DataLifeCycle.class, Collections.singletonList(dataLifeCycle), this.pkgName);
        if (this.remote != null) {
            try {
                ObjectContainer handleDataLifeCycleConfig = ((IDataServiceCall) this.remote).handleDataLifeCycleConfig(2, objectContainer);
                if (!(handleDataLifeCycleConfig == null || handleDataLifeCycleConfig.get() == null)) {
                    if (!handleDataLifeCycleConfig.get().isEmpty()) {
                        if (handleDataLifeCycleConfig.get().get(0) instanceof Integer) {
                            return ((Integer) handleDataLifeCycleConfig.get().get(0)).intValue();
                        }
                        DSLog.e("Failed to remove dataLifeCycleConfig, error: result is not a integer with db[%s] table[%s] fieldName[%s] mode[%s] count[%s] pkgname[%s].", DbLogUtil.getSafeNameForLog(str), DbLogUtil.getSafeNameForLog(str2), DbLogUtil.getSafeNameForLog(str3), Integer.valueOf(i), Integer.valueOf(i2), this.pkgName);
                        return 4;
                    }
                }
                DSLog.e("Failed to remove dataLifeCycleConfig, error: add result null with db[%s] table[%s] fieldName[%s] mode[%s] count[%s] pkgname[%s].", DbLogUtil.getSafeNameForLog(str), DbLogUtil.getSafeNameForLog(str2), DbLogUtil.getSafeNameForLog(str3), Integer.valueOf(i), Integer.valueOf(i2), this.pkgName);
                return 4;
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
