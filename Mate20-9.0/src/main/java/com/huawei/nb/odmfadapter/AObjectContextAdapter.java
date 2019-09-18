package com.huawei.nb.odmfadapter;

import android.database.Cursor;
import com.huawei.nb.client.IClient;
import com.huawei.nb.query.RelationshipQuery;
import com.huawei.odmf.core.ManagedObject;
import com.huawei.odmf.core.ObjectId;
import com.huawei.odmf.model.api.Attribute;
import com.huawei.odmf.user.api.IListener;
import com.huawei.odmf.user.api.ObjectContext;
import com.huawei.odmf.user.api.Query;
import java.util.Collections;
import java.util.List;

public class AObjectContextAdapter implements ObjectContext {
    IClient client;

    public AObjectContextAdapter(IClient client2) {
        this.client = client2;
    }

    public boolean insert(ManagedObject object) {
        return false;
    }

    public boolean update(ManagedObject object) {
        return false;
    }

    public boolean delete(ManagedObject object) {
        return false;
    }

    public void beginTransaction() {
    }

    public void commit() {
    }

    public void rollback() {
    }

    public boolean inTransaction() {
        return false;
    }

    public boolean flush() {
        return false;
    }

    public <T extends ManagedObject> List<T> get(Class<T> cls) throws IllegalStateException {
        return null;
    }

    public <T extends ManagedObject> Query<T> where(Class<T> cls) {
        return null;
    }

    public void deleteEntityData(Class clz) {
    }

    public void close() {
    }

    public void setOpenQueryCache(boolean isOpen) {
    }

    public void setQueryCacheNumbers(int queryCacheNumbers) {
    }

    public ManagedObject getToOneRelationshipValue(String fieldName, ManagedObject object) {
        if (!validateManagedObject(object)) {
            return null;
        }
        List retObjects = this.client.executeQuery(new RelationshipQuery(object.getObjectId().getEntityName(), object.getObjectId().getId(), fieldName, RelationshipQuery.RelationType.TO_ONE));
        if (retObjects == null || retObjects.isEmpty()) {
            return null;
        }
        return (ManagedObject) retObjects.get(0);
    }

    public List<? extends ManagedObject> getToManyRelationshipValue(String fieldName, ManagedObject object) {
        if (!validateManagedObject(object)) {
            return Collections.emptyList();
        }
        return this.client.executeQuery(new RelationshipQuery(object.getObjectId().getEntityName(), object.getObjectId().getId(), fieldName, RelationshipQuery.RelationType.TO_MANY));
    }

    public ManagedObject remoteGetToOneRelationshipValue(String fieldName, ObjectId objectId) {
        return null;
    }

    public List<? extends ManagedObject> remoteGetToManyRelationshipValue(String fieldName, ObjectId objectId) {
        return null;
    }

    public void registerListener(Object obj, IListener listener) {
    }

    public void unregisterListener(Object obj, IListener listener) {
    }

    public Cursor executeRawQuerySQL(String sql) {
        return null;
    }

    public void executeRawSQL(String sql) {
    }

    public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return null;
    }

    public String getDBVersion() {
        return null;
    }

    public String getEntityVersion(String s) {
        return null;
    }

    public int getDBVersionCode() {
        return 0;
    }

    public int getEntityVersionCode(String s) {
        return 0;
    }

    public void resetMetadata() {
    }

    public List<? extends Attribute> getEntityAttributes(String entityName) {
        return null;
    }

    public void setEntityVersions(String s1, String s2, int i) {
    }

    public void setDBVersions(String s1, int i) {
    }

    public void exportDatabase(String newDBName, byte[] newKey) {
    }

    private boolean validateManagedObject(ManagedObject object) {
        if (object == null || object.getObjectId() == null || object.getObjectId().getId() == null || object.getObjectId().getEntityName() == null) {
            return false;
        }
        return true;
    }
}
