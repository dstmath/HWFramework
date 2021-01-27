package com.huawei.odmf.user.api;

import android.database.Cursor;
import com.huawei.odmf.core.ManagedObject;
import com.huawei.odmf.core.ObjectId;
import com.huawei.odmf.model.api.Attribute;
import java.util.List;
import java.util.Set;

public interface ObjectContext {
    void beginTransaction();

    void close();

    void commit();

    boolean delete(ManagedObject managedObject);

    void deleteEntityData(Class cls);

    Cursor executeRawQuerySQL(String str);

    void executeRawSQL(String str);

    void exportDatabase(String str, byte[] bArr);

    boolean flush();

    <T extends ManagedObject> List<T> get(Class<T> cls);

    String getDbVersion();

    int getDbVersionCode();

    List<? extends Attribute> getEntityAttributes(String str);

    String getEntityVersion(String str);

    int getEntityVersionCode(String str);

    Set<String> getTableInvolvedInSQL(String str);

    List<? extends ManagedObject> getToManyRelationshipValue(String str, ManagedObject managedObject);

    ManagedObject getToOneRelationshipValue(String str, ManagedObject managedObject);

    boolean inTransaction();

    boolean insert(ManagedObject managedObject);

    Cursor query(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6);

    void registerListener(Object obj, IListener iListener);

    List<? extends ManagedObject> remoteGetToManyRelationshipValue(String str, ObjectId objectId);

    ManagedObject remoteGetToOneRelationshipValue(String str, ObjectId objectId);

    void resetMetadata();

    void rollback();

    void setDbVersions(String str, int i);

    void setEntityVersions(String str, String str2, int i);

    void setOpenQueryCache(boolean z);

    void setQueryCacheNumbers(int i);

    void unregisterListener(Object obj, IListener iListener);

    boolean update(ManagedObject managedObject);

    <T extends ManagedObject> Query<T> where(Class<T> cls);
}
