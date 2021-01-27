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
import java.util.Set;

public class AObjectContextAdapter implements ObjectContext {
    private IClient client;

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void beginTransaction() {
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void close() {
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void commit() {
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public boolean delete(ManagedObject managedObject) {
        return false;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void deleteEntityData(Class cls) {
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public Cursor executeRawQuerySQL(String str) {
        return null;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void executeRawSQL(String str) {
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void exportDatabase(String str, byte[] bArr) {
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public boolean flush() {
        return false;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public <T extends ManagedObject> List<T> get(Class<T> cls) throws IllegalStateException {
        return null;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public String getDbVersion() {
        return null;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public int getDbVersionCode() {
        return 0;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public List<? extends Attribute> getEntityAttributes(String str) {
        return null;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public String getEntityVersion(String str) {
        return null;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public int getEntityVersionCode(String str) {
        return 0;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public Set<String> getTableInvolvedInSQL(String str) {
        return null;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public boolean inTransaction() {
        return false;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public boolean insert(ManagedObject managedObject) {
        return false;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public Cursor query(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        return null;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void registerListener(Object obj, IListener iListener) {
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public List<? extends ManagedObject> remoteGetToManyRelationshipValue(String str, ObjectId objectId) {
        return null;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public ManagedObject remoteGetToOneRelationshipValue(String str, ObjectId objectId) {
        return null;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void resetMetadata() {
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void rollback() {
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void setDbVersions(String str, int i) {
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void setEntityVersions(String str, String str2, int i) {
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void setOpenQueryCache(boolean z) {
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void setQueryCacheNumbers(int i) {
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public void unregisterListener(Object obj, IListener iListener) {
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public boolean update(ManagedObject managedObject) {
        return false;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public <T extends ManagedObject> Query<T> where(Class<T> cls) {
        return null;
    }

    public AObjectContextAdapter(IClient iClient) {
        this.client = iClient;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public ManagedObject getToOneRelationshipValue(String str, ManagedObject managedObject) {
        List executeQuery;
        if (validateManagedObject(managedObject) && (executeQuery = this.client.executeQuery(new RelationshipQuery(managedObject.getObjectId().getEntityName(), managedObject.getObjectId().getId(), str, RelationshipQuery.RelationType.TO_ONE))) != null && !executeQuery.isEmpty()) {
            return (ManagedObject) executeQuery.get(0);
        }
        return null;
    }

    @Override // com.huawei.odmf.user.api.ObjectContext
    public List<? extends ManagedObject> getToManyRelationshipValue(String str, ManagedObject managedObject) {
        if (!validateManagedObject(managedObject)) {
            return Collections.emptyList();
        }
        return this.client.executeQuery(new RelationshipQuery(managedObject.getObjectId().getEntityName(), managedObject.getObjectId().getId(), str, RelationshipQuery.RelationType.TO_MANY));
    }

    private boolean validateManagedObject(ManagedObject managedObject) {
        return (managedObject == null || managedObject.getObjectId() == null || managedObject.getObjectId().getId() == null || managedObject.getObjectId().getEntityName() == null) ? false : true;
    }
}
