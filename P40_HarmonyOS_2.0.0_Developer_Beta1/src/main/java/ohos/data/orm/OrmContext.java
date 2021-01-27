package ohos.data.orm;

import java.util.List;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;

public interface OrmContext extends AutoCloseable {
    Double avg(OrmPredicates ormPredicates, String str);

    boolean backup(String str);

    boolean backup(String str, byte[] bArr);

    void beginTransaction();

    void changeEncryptKey(byte[] bArr);

    @Override // java.lang.AutoCloseable
    void close();

    void commit();

    Long count(OrmPredicates ormPredicates);

    int delete(OrmPredicates ormPredicates);

    <T extends OrmObject> boolean delete(T t);

    boolean flush();

    String getAlias();

    <T extends OrmObject> boolean insert(T t);

    boolean isInTransaction();

    Double max(OrmPredicates ormPredicates, String str);

    Double min(OrmPredicates ormPredicates, String str);

    <T extends OrmObject> List<T> query(OrmPredicates ormPredicates);

    ResultSet query(OrmPredicates ormPredicates, String[] strArr);

    void registerContextObserver(OrmContext ormContext, OrmObjectObserver ormObjectObserver);

    void registerEntityObserver(String str, OrmObjectObserver ormObjectObserver);

    void registerObjectObserver(OrmObject ormObject, OrmObjectObserver ormObjectObserver);

    void registerStoreObserver(String str, OrmObjectObserver ormObjectObserver);

    boolean restore(String str);

    boolean restore(String str, byte[] bArr, byte[] bArr2);

    void rollback();

    Double sum(OrmPredicates ormPredicates, String str);

    void unregisterContextObserver(OrmContext ormContext, OrmObjectObserver ormObjectObserver);

    void unregisterEntityObserver(String str, OrmObjectObserver ormObjectObserver);

    void unregisterObjectObserver(OrmObject ormObject, OrmObjectObserver ormObjectObserver);

    void unregisterStoreObserver(String str, OrmObjectObserver ormObjectObserver);

    int update(OrmPredicates ormPredicates, ValuesBucket valuesBucket);

    <T extends OrmObject> boolean update(T t);

    <T extends OrmObject> OrmPredicates where(Class<T> cls);
}
