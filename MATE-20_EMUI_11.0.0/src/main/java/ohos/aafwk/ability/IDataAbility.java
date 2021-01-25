package ohos.aafwk.ability;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.global.resource.RawFileDescriptor;
import ohos.utils.PacMap;
import ohos.utils.net.Uri;

public interface IDataAbility {
    int batchInsert(Uri uri, ValuesBucket[] valuesBucketArr) throws DataAbilityRemoteException;

    PacMap call(String str, String str2, PacMap pacMap) throws DataAbilityRemoteException;

    void close();

    int delete(Uri uri, DataAbilityPredicates dataAbilityPredicates) throws DataAbilityRemoteException;

    Uri denormalizeUri(Uri uri) throws DataAbilityRemoteException;

    DataAbilityResult[] executeBatch(ArrayList<DataAbilityOperation> arrayList) throws DataAbilityRemoteException, OperationExecuteException;

    String[] getFileTypes(Uri uri, String str) throws DataAbilityRemoteException;

    String getType(Uri uri) throws DataAbilityRemoteException;

    int insert(Uri uri, ValuesBucket valuesBucket) throws DataAbilityRemoteException;

    void makePersistentUriPermission(Uri uri, int i) throws DataAbilityRemoteException;

    Uri normalizeUri(Uri uri) throws DataAbilityRemoteException;

    void notifyChange(Uri uri);

    FileDescriptor openFile(Uri uri, String str) throws DataAbilityRemoteException, FileNotFoundException;

    RawFileDescriptor openRawFile(Uri uri, String str) throws DataAbilityRemoteException, FileNotFoundException;

    ResultSet query(Uri uri, String[] strArr, DataAbilityPredicates dataAbilityPredicates) throws DataAbilityRemoteException;

    void registerObserver(Uri uri, Object obj);

    void unregisterObserver(Object obj);

    int update(Uri uri, ValuesBucket valuesBucket, DataAbilityPredicates dataAbilityPredicates) throws DataAbilityRemoteException;
}
