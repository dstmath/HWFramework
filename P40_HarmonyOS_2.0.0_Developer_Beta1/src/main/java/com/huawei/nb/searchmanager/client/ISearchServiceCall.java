package com.huawei.nb.searchmanager.client;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SharedMemory;
import com.huawei.nb.searchmanager.callback.IIndexChangeCallback;
import com.huawei.nb.searchmanager.client.ISearchSession;
import com.huawei.nb.searchmanager.client.model.IndexData;
import com.huawei.nb.searchmanager.client.model.IndexForm;
import com.huawei.nb.searchmanager.client.model.SearchableEntity;
import com.huawei.nb.searchmanager.distribute.DeviceInfo;
import com.huawei.nb.searchmanager.distribute.IDeviceChangeCallback;
import com.huawei.nb.searchmanager.distribute.IRemoteSearchCallback;
import com.huawei.nb.searchmanager.query.bulkcursor.BulkCursorDescriptor;
import java.util.List;
import java.util.Map;

public interface ISearchServiceCall extends IInterface {
    List<String> addFileObserveDirectories(String str, List<String> list, String str2) throws RemoteException;

    IRemoteSearchCallback beginRemoteSearch(DeviceInfo deviceInfo, String str, String str2) throws RemoteException;

    ISearchSession beginSearch(String str, String str2, String str3) throws RemoteException;

    int clearIndex(String str, String str2, Map map, String str3) throws RemoteException;

    int clearIndexForm(String str, String str2) throws RemoteException;

    List<IndexData> delete(String str, String str2, List<IndexData> list, String str3) throws RemoteException;

    int deleteByQuery(String str, String str2, String str3, String str4) throws RemoteException;

    List<String> deleteByTerm(String str, String str2, String str3, List<String> list, String str4) throws RemoteException;

    List<String> deleteFileObserveDirectories(String str, List<String> list, String str2) throws RemoteException;

    int deleteLarge(String str, String str2, SharedMemory sharedMemory, SharedMemory sharedMemory2, String str3) throws RemoteException;

    boolean endRemoteSearch(DeviceInfo deviceInfo, String str, String str2) throws RemoteException;

    void endSearch(String str, String str2, ISearchSession iSearchSession, String str3) throws RemoteException;

    List<Word> executeAnalyzeText(String str, String str2, String str3) throws RemoteException;

    void executeClearData(String str, int i, String str2) throws RemoteException;

    void executeDBCrawl(String str, List<String> list, int i, String str2) throws RemoteException;

    int executeDeleteIndex(String str, List<String> list, List<Attributes> list2, String str2) throws RemoteException;

    void executeFileCrawl(String str, String str2, boolean z, int i, String str3) throws RemoteException;

    int executeInsertIndex(String str, List<SearchIndexData> list, List<Attributes> list2, String str2) throws RemoteException;

    List<SearchIntentItem> executeIntentSearch(String str, String str2, List<String> list, String str3, String str4) throws RemoteException;

    BulkCursorDescriptor executeMultiSearch(String str, Bundle bundle, String str2) throws RemoteException;

    BulkCursorDescriptor executeSearch(String str, String str2, List<String> list, List<Attributes> list2, String str3) throws RemoteException;

    int executeUpdateIndex(String str, List<SearchIndexData> list, List<Attributes> list2, String str2) throws RemoteException;

    int getAccessable(String str, String str2) throws RemoteException;

    List<IndexForm> getIndexForm(String str, String str2) throws RemoteException;

    int getIndexFormVersion(String str, String str2) throws RemoteException;

    List<DeviceInfo> getOnlineDevices(String str) throws RemoteException;

    SearchableEntity getSearchableEntity(String str, String str2) throws RemoteException;

    List<SearchableEntity> getSearchableEntityList(String str) throws RemoteException;

    String grantFilePermission(String str, String str2, String str3, int i, String str4) throws RemoteException;

    List<IndexData> insert(String str, String str2, List<IndexData> list, String str3) throws RemoteException;

    int insertLarge(String str, String str2, SharedMemory sharedMemory, SharedMemory sharedMemory2, String str3) throws RemoteException;

    boolean isIndexCompatible(String str, String str2) throws RemoteException;

    void registerClientDeathBinder(IBinder iBinder, String str) throws RemoteException;

    boolean registerDeviceChangeCallback(String str, IDeviceChangeCallback iDeviceChangeCallback) throws RemoteException;

    void registerIndexChangeListener(String str, String str2, IIndexChangeCallback iIndexChangeCallback, String str3) throws RemoteException;

    boolean registerRemoteSearchCallback(DeviceInfo deviceInfo, IRemoteSearchCallback iRemoteSearchCallback, String str) throws RemoteException;

    String revokeFilePermission(String str, String str2, String str3, int i, String str4) throws RemoteException;

    int setAccessable(String str, boolean z, String str2) throws RemoteException;

    int setIndexForm(String str, int i, List<IndexForm> list, String str2) throws RemoteException;

    int setIndexFormSchema(String str, int i, List<IndexForm> list, int i2, String str2) throws RemoteException;

    void setSearchSwitch(String str, boolean z, String str2) throws RemoteException;

    int setSearchableEntity(SearchableEntity searchableEntity, String str) throws RemoteException;

    void unRegisterClientDeathBinder(IBinder iBinder, String str) throws RemoteException;

    void unRegisterIndexChangeListener(String str, String str2, IIndexChangeCallback iIndexChangeCallback, String str3) throws RemoteException;

    boolean unregisterDeviceChangeCallback(String str) throws RemoteException;

    boolean unregisterRemoteSearchCallback(DeviceInfo deviceInfo, String str) throws RemoteException;

    List<IndexData> update(String str, String str2, List<IndexData> list, String str3) throws RemoteException;

    int updateLarge(String str, String str2, SharedMemory sharedMemory, SharedMemory sharedMemory2, String str3) throws RemoteException;

    public static abstract class Stub extends Binder implements ISearchServiceCall {
        private static final String DESCRIPTOR = "com.huawei.nb.searchmanager.client.ISearchServiceCall";
        static final int TRANSACTION_addFileObserveDirectories = 30;
        static final int TRANSACTION_beginRemoteSearch = 44;
        static final int TRANSACTION_beginSearch = 24;
        static final int TRANSACTION_clearIndex = 23;
        static final int TRANSACTION_clearIndexForm = 14;
        static final int TRANSACTION_delete = 20;
        static final int TRANSACTION_deleteByQuery = 22;
        static final int TRANSACTION_deleteByTerm = 21;
        static final int TRANSACTION_deleteFileObserveDirectories = 31;
        static final int TRANSACTION_deleteLarge = 40;
        static final int TRANSACTION_endRemoteSearch = 45;
        static final int TRANSACTION_endSearch = 25;
        static final int TRANSACTION_executeAnalyzeText = 9;
        static final int TRANSACTION_executeClearData = 3;
        static final int TRANSACTION_executeDBCrawl = 1;
        static final int TRANSACTION_executeDeleteIndex = 7;
        static final int TRANSACTION_executeFileCrawl = 4;
        static final int TRANSACTION_executeInsertIndex = 5;
        static final int TRANSACTION_executeIntentSearch = 8;
        static final int TRANSACTION_executeMultiSearch = 13;
        static final int TRANSACTION_executeSearch = 2;
        static final int TRANSACTION_executeUpdateIndex = 6;
        static final int TRANSACTION_getAccessable = 36;
        static final int TRANSACTION_getIndexForm = 17;
        static final int TRANSACTION_getIndexFormVersion = 16;
        static final int TRANSACTION_getOnlineDevices = 46;
        static final int TRANSACTION_getSearchableEntity = 33;
        static final int TRANSACTION_getSearchableEntityList = 34;
        static final int TRANSACTION_grantFilePermission = 11;
        static final int TRANSACTION_insert = 18;
        static final int TRANSACTION_insertLarge = 38;
        static final int TRANSACTION_isIndexCompatible = 37;
        static final int TRANSACTION_registerClientDeathBinder = 28;
        static final int TRANSACTION_registerDeviceChangeCallback = 47;
        static final int TRANSACTION_registerIndexChangeListener = 26;
        static final int TRANSACTION_registerRemoteSearchCallback = 42;
        static final int TRANSACTION_revokeFilePermission = 12;
        static final int TRANSACTION_setAccessable = 35;
        static final int TRANSACTION_setIndexForm = 15;
        static final int TRANSACTION_setIndexFormSchema = 41;
        static final int TRANSACTION_setSearchSwitch = 10;
        static final int TRANSACTION_setSearchableEntity = 32;
        static final int TRANSACTION_unRegisterClientDeathBinder = 29;
        static final int TRANSACTION_unRegisterIndexChangeListener = 27;
        static final int TRANSACTION_unregisterDeviceChangeCallback = 48;
        static final int TRANSACTION_unregisterRemoteSearchCallback = 43;
        static final int TRANSACTION_update = 19;
        static final int TRANSACTION_updateLarge = 39;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISearchServiceCall asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof ISearchServiceCall)) {
                return new Proxy(iBinder);
            }
            return (ISearchServiceCall) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i != 1598968902) {
                boolean z = false;
                Bundle bundle = null;
                DeviceInfo deviceInfo = null;
                IBinder iBinder = null;
                DeviceInfo deviceInfo2 = null;
                DeviceInfo deviceInfo3 = null;
                SharedMemory sharedMemory = null;
                SharedMemory sharedMemory2 = null;
                SharedMemory sharedMemory3 = null;
                SearchableEntity searchableEntity = null;
                IBinder iBinder2 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        executeDBCrawl(parcel.readString(), parcel.createStringArrayList(), parcel.readInt(), parcel.readString());
                        parcel2.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        BulkCursorDescriptor executeSearch = executeSearch(parcel.readString(), parcel.readString(), parcel.createStringArrayList(), parcel.createTypedArrayList(Attributes.CREATOR), parcel.readString());
                        parcel2.writeNoException();
                        if (executeSearch != null) {
                            parcel2.writeInt(1);
                            executeSearch.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        executeClearData(parcel.readString(), parcel.readInt(), parcel.readString());
                        parcel2.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        executeFileCrawl(parcel.readString(), parcel.readString(), parcel.readInt() != 0, parcel.readInt(), parcel.readString());
                        parcel2.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        int executeInsertIndex = executeInsertIndex(parcel.readString(), parcel.createTypedArrayList(SearchIndexData.CREATOR), parcel.createTypedArrayList(Attributes.CREATOR), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(executeInsertIndex);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        int executeUpdateIndex = executeUpdateIndex(parcel.readString(), parcel.createTypedArrayList(SearchIndexData.CREATOR), parcel.createTypedArrayList(Attributes.CREATOR), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(executeUpdateIndex);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        int executeDeleteIndex = executeDeleteIndex(parcel.readString(), parcel.createStringArrayList(), parcel.createTypedArrayList(Attributes.CREATOR), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(executeDeleteIndex);
                        return true;
                    case TRANSACTION_executeIntentSearch /* 8 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<SearchIntentItem> executeIntentSearch = executeIntentSearch(parcel.readString(), parcel.readString(), parcel.createStringArrayList(), parcel.readString(), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeTypedList(executeIntentSearch);
                        return true;
                    case TRANSACTION_executeAnalyzeText /* 9 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<Word> executeAnalyzeText = executeAnalyzeText(parcel.readString(), parcel.readString(), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeTypedList(executeAnalyzeText);
                        return true;
                    case TRANSACTION_setSearchSwitch /* 10 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        String readString = parcel.readString();
                        if (parcel.readInt() != 0) {
                            z = true;
                        }
                        setSearchSwitch(readString, z, parcel.readString());
                        parcel2.writeNoException();
                        return true;
                    case TRANSACTION_grantFilePermission /* 11 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        String grantFilePermission = grantFilePermission(parcel.readString(), parcel.readString(), parcel.readString(), parcel.readInt(), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeString(grantFilePermission);
                        return true;
                    case TRANSACTION_revokeFilePermission /* 12 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        String revokeFilePermission = revokeFilePermission(parcel.readString(), parcel.readString(), parcel.readString(), parcel.readInt(), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeString(revokeFilePermission);
                        return true;
                    case TRANSACTION_executeMultiSearch /* 13 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        String readString2 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        BulkCursorDescriptor executeMultiSearch = executeMultiSearch(readString2, bundle, parcel.readString());
                        parcel2.writeNoException();
                        if (executeMultiSearch != null) {
                            parcel2.writeInt(1);
                            executeMultiSearch.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_clearIndexForm /* 14 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        int clearIndexForm = clearIndexForm(parcel.readString(), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(clearIndexForm);
                        return true;
                    case TRANSACTION_setIndexForm /* 15 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        int indexForm = setIndexForm(parcel.readString(), parcel.readInt(), parcel.createTypedArrayList(IndexForm.CREATOR), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(indexForm);
                        return true;
                    case TRANSACTION_getIndexFormVersion /* 16 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        int indexFormVersion = getIndexFormVersion(parcel.readString(), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(indexFormVersion);
                        return true;
                    case TRANSACTION_getIndexForm /* 17 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<IndexForm> indexForm2 = getIndexForm(parcel.readString(), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeTypedList(indexForm2);
                        return true;
                    case TRANSACTION_insert /* 18 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<IndexData> insert = insert(parcel.readString(), parcel.readString(), parcel.createTypedArrayList(IndexData.CREATOR), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeTypedList(insert);
                        return true;
                    case TRANSACTION_update /* 19 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<IndexData> update = update(parcel.readString(), parcel.readString(), parcel.createTypedArrayList(IndexData.CREATOR), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeTypedList(update);
                        return true;
                    case TRANSACTION_delete /* 20 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<IndexData> delete = delete(parcel.readString(), parcel.readString(), parcel.createTypedArrayList(IndexData.CREATOR), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeTypedList(delete);
                        return true;
                    case TRANSACTION_deleteByTerm /* 21 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> deleteByTerm = deleteByTerm(parcel.readString(), parcel.readString(), parcel.readString(), parcel.createStringArrayList(), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeStringList(deleteByTerm);
                        return true;
                    case TRANSACTION_deleteByQuery /* 22 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        int deleteByQuery = deleteByQuery(parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(deleteByQuery);
                        return true;
                    case TRANSACTION_clearIndex /* 23 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        int clearIndex = clearIndex(parcel.readString(), parcel.readString(), parcel.readHashMap(getClass().getClassLoader()), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(clearIndex);
                        return true;
                    case TRANSACTION_beginSearch /* 24 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        ISearchSession beginSearch = beginSearch(parcel.readString(), parcel.readString(), parcel.readString());
                        parcel2.writeNoException();
                        if (beginSearch != null) {
                            iBinder2 = beginSearch.asBinder();
                        }
                        parcel2.writeStrongBinder(iBinder2);
                        return true;
                    case TRANSACTION_endSearch /* 25 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        endSearch(parcel.readString(), parcel.readString(), ISearchSession.Stub.asInterface(parcel.readStrongBinder()), parcel.readString());
                        parcel2.writeNoException();
                        return true;
                    case TRANSACTION_registerIndexChangeListener /* 26 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerIndexChangeListener(parcel.readString(), parcel.readString(), IIndexChangeCallback.Stub.asInterface(parcel.readStrongBinder()), parcel.readString());
                        parcel2.writeNoException();
                        return true;
                    case TRANSACTION_unRegisterIndexChangeListener /* 27 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        unRegisterIndexChangeListener(parcel.readString(), parcel.readString(), IIndexChangeCallback.Stub.asInterface(parcel.readStrongBinder()), parcel.readString());
                        parcel2.writeNoException();
                        return true;
                    case TRANSACTION_registerClientDeathBinder /* 28 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerClientDeathBinder(parcel.readStrongBinder(), parcel.readString());
                        parcel2.writeNoException();
                        return true;
                    case TRANSACTION_unRegisterClientDeathBinder /* 29 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        unRegisterClientDeathBinder(parcel.readStrongBinder(), parcel.readString());
                        parcel2.writeNoException();
                        return true;
                    case TRANSACTION_addFileObserveDirectories /* 30 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> addFileObserveDirectories = addFileObserveDirectories(parcel.readString(), parcel.createStringArrayList(), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeStringList(addFileObserveDirectories);
                        return true;
                    case TRANSACTION_deleteFileObserveDirectories /* 31 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> deleteFileObserveDirectories = deleteFileObserveDirectories(parcel.readString(), parcel.createStringArrayList(), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeStringList(deleteFileObserveDirectories);
                        return true;
                    case TRANSACTION_setSearchableEntity /* 32 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            searchableEntity = SearchableEntity.CREATOR.createFromParcel(parcel);
                        }
                        int searchableEntity2 = setSearchableEntity(searchableEntity, parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(searchableEntity2);
                        return true;
                    case TRANSACTION_getSearchableEntity /* 33 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        SearchableEntity searchableEntity3 = getSearchableEntity(parcel.readString(), parcel.readString());
                        parcel2.writeNoException();
                        if (searchableEntity3 != null) {
                            parcel2.writeInt(1);
                            searchableEntity3.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_getSearchableEntityList /* 34 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<SearchableEntity> searchableEntityList = getSearchableEntityList(parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeTypedList(searchableEntityList);
                        return true;
                    case TRANSACTION_setAccessable /* 35 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        String readString3 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            z = true;
                        }
                        int accessable = setAccessable(readString3, z, parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(accessable);
                        return true;
                    case TRANSACTION_getAccessable /* 36 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        int accessable2 = getAccessable(parcel.readString(), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(accessable2);
                        return true;
                    case TRANSACTION_isIndexCompatible /* 37 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean isIndexCompatible = isIndexCompatible(parcel.readString(), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(isIndexCompatible ? 1 : 0);
                        return true;
                    case TRANSACTION_insertLarge /* 38 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        String readString4 = parcel.readString();
                        String readString5 = parcel.readString();
                        SharedMemory sharedMemory4 = parcel.readInt() != 0 ? (SharedMemory) SharedMemory.CREATOR.createFromParcel(parcel) : null;
                        if (parcel.readInt() != 0) {
                            sharedMemory3 = (SharedMemory) SharedMemory.CREATOR.createFromParcel(parcel);
                        }
                        int insertLarge = insertLarge(readString4, readString5, sharedMemory4, sharedMemory3, parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(insertLarge);
                        return true;
                    case TRANSACTION_updateLarge /* 39 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        String readString6 = parcel.readString();
                        String readString7 = parcel.readString();
                        SharedMemory sharedMemory5 = parcel.readInt() != 0 ? (SharedMemory) SharedMemory.CREATOR.createFromParcel(parcel) : null;
                        if (parcel.readInt() != 0) {
                            sharedMemory2 = (SharedMemory) SharedMemory.CREATOR.createFromParcel(parcel);
                        }
                        int updateLarge = updateLarge(readString6, readString7, sharedMemory5, sharedMemory2, parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(updateLarge);
                        return true;
                    case TRANSACTION_deleteLarge /* 40 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        String readString8 = parcel.readString();
                        String readString9 = parcel.readString();
                        SharedMemory sharedMemory6 = parcel.readInt() != 0 ? (SharedMemory) SharedMemory.CREATOR.createFromParcel(parcel) : null;
                        if (parcel.readInt() != 0) {
                            sharedMemory = (SharedMemory) SharedMemory.CREATOR.createFromParcel(parcel);
                        }
                        int deleteLarge = deleteLarge(readString8, readString9, sharedMemory6, sharedMemory, parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(deleteLarge);
                        return true;
                    case TRANSACTION_setIndexFormSchema /* 41 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        int indexFormSchema = setIndexFormSchema(parcel.readString(), parcel.readInt(), parcel.createTypedArrayList(IndexForm.CREATOR), parcel.readInt(), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(indexFormSchema);
                        return true;
                    case TRANSACTION_registerRemoteSearchCallback /* 42 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            deviceInfo3 = DeviceInfo.CREATOR.createFromParcel(parcel);
                        }
                        boolean registerRemoteSearchCallback = registerRemoteSearchCallback(deviceInfo3, IRemoteSearchCallback.Stub.asInterface(parcel.readStrongBinder()), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(registerRemoteSearchCallback ? 1 : 0);
                        return true;
                    case TRANSACTION_unregisterRemoteSearchCallback /* 43 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            deviceInfo2 = DeviceInfo.CREATOR.createFromParcel(parcel);
                        }
                        boolean unregisterRemoteSearchCallback = unregisterRemoteSearchCallback(deviceInfo2, parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(unregisterRemoteSearchCallback ? 1 : 0);
                        return true;
                    case TRANSACTION_beginRemoteSearch /* 44 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        IRemoteSearchCallback beginRemoteSearch = beginRemoteSearch(parcel.readInt() != 0 ? DeviceInfo.CREATOR.createFromParcel(parcel) : null, parcel.readString(), parcel.readString());
                        parcel2.writeNoException();
                        if (beginRemoteSearch != null) {
                            iBinder = beginRemoteSearch.asBinder();
                        }
                        parcel2.writeStrongBinder(iBinder);
                        return true;
                    case TRANSACTION_endRemoteSearch /* 45 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            deviceInfo = DeviceInfo.CREATOR.createFromParcel(parcel);
                        }
                        boolean endRemoteSearch = endRemoteSearch(deviceInfo, parcel.readString(), parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(endRemoteSearch ? 1 : 0);
                        return true;
                    case TRANSACTION_getOnlineDevices /* 46 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<DeviceInfo> onlineDevices = getOnlineDevices(parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeTypedList(onlineDevices);
                        return true;
                    case TRANSACTION_registerDeviceChangeCallback /* 47 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean registerDeviceChangeCallback = registerDeviceChangeCallback(parcel.readString(), IDeviceChangeCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(registerDeviceChangeCallback ? 1 : 0);
                        return true;
                    case TRANSACTION_unregisterDeviceChangeCallback /* 48 */:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean unregisterDeviceChangeCallback = unregisterDeviceChangeCallback(parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(unregisterDeviceChangeCallback ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements ISearchServiceCall {
            private IBinder mRemote;

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public void executeDBCrawl(String str, List<String> list, int i, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeStringList(list);
                    obtain.writeInt(i);
                    obtain.writeString(str2);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public BulkCursorDescriptor executeSearch(String str, String str2, List<String> list, List<Attributes> list2, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeStringList(list);
                    obtain.writeTypedList(list2);
                    obtain.writeString(str3);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0 ? BulkCursorDescriptor.CREATOR.createFromParcel(obtain2) : null;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public void executeClearData(String str, int i, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    obtain.writeString(str2);
                    this.mRemote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public void executeFileCrawl(String str, String str2, boolean z, int i, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeInt(z ? 1 : 0);
                    obtain.writeInt(i);
                    obtain.writeString(str3);
                    this.mRemote.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public int executeInsertIndex(String str, List<SearchIndexData> list, List<Attributes> list2, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeTypedList(list);
                    obtain.writeTypedList(list2);
                    obtain.writeString(str2);
                    this.mRemote.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public int executeUpdateIndex(String str, List<SearchIndexData> list, List<Attributes> list2, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeTypedList(list);
                    obtain.writeTypedList(list2);
                    obtain.writeString(str2);
                    this.mRemote.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public int executeDeleteIndex(String str, List<String> list, List<Attributes> list2, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeStringList(list);
                    obtain.writeTypedList(list2);
                    obtain.writeString(str2);
                    this.mRemote.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public List<SearchIntentItem> executeIntentSearch(String str, String str2, List<String> list, String str3, String str4) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeStringList(list);
                    obtain.writeString(str3);
                    obtain.writeString(str4);
                    this.mRemote.transact(Stub.TRANSACTION_executeIntentSearch, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createTypedArrayList(SearchIntentItem.CREATOR);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public List<Word> executeAnalyzeText(String str, String str2, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeString(str3);
                    this.mRemote.transact(Stub.TRANSACTION_executeAnalyzeText, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createTypedArrayList(Word.CREATOR);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public void setSearchSwitch(String str, boolean z, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(z ? 1 : 0);
                    obtain.writeString(str2);
                    this.mRemote.transact(Stub.TRANSACTION_setSearchSwitch, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public String grantFilePermission(String str, String str2, String str3, int i, String str4) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeString(str3);
                    obtain.writeInt(i);
                    obtain.writeString(str4);
                    this.mRemote.transact(Stub.TRANSACTION_grantFilePermission, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readString();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public String revokeFilePermission(String str, String str2, String str3, int i, String str4) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeString(str3);
                    obtain.writeInt(i);
                    obtain.writeString(str4);
                    this.mRemote.transact(Stub.TRANSACTION_revokeFilePermission, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readString();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public BulkCursorDescriptor executeMultiSearch(String str, Bundle bundle, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (bundle != null) {
                        obtain.writeInt(1);
                        bundle.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeString(str2);
                    this.mRemote.transact(Stub.TRANSACTION_executeMultiSearch, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0 ? BulkCursorDescriptor.CREATOR.createFromParcel(obtain2) : null;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public int clearIndexForm(String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    this.mRemote.transact(Stub.TRANSACTION_clearIndexForm, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public int setIndexForm(String str, int i, List<IndexForm> list, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    obtain.writeTypedList(list);
                    obtain.writeString(str2);
                    this.mRemote.transact(Stub.TRANSACTION_setIndexForm, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public int getIndexFormVersion(String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    this.mRemote.transact(Stub.TRANSACTION_getIndexFormVersion, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public List<IndexForm> getIndexForm(String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    this.mRemote.transact(Stub.TRANSACTION_getIndexForm, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createTypedArrayList(IndexForm.CREATOR);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public List<IndexData> insert(String str, String str2, List<IndexData> list, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeTypedList(list);
                    obtain.writeString(str3);
                    this.mRemote.transact(Stub.TRANSACTION_insert, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createTypedArrayList(IndexData.CREATOR);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public List<IndexData> update(String str, String str2, List<IndexData> list, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeTypedList(list);
                    obtain.writeString(str3);
                    this.mRemote.transact(Stub.TRANSACTION_update, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createTypedArrayList(IndexData.CREATOR);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public List<IndexData> delete(String str, String str2, List<IndexData> list, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeTypedList(list);
                    obtain.writeString(str3);
                    this.mRemote.transact(Stub.TRANSACTION_delete, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createTypedArrayList(IndexData.CREATOR);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public List<String> deleteByTerm(String str, String str2, String str3, List<String> list, String str4) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeString(str3);
                    obtain.writeStringList(list);
                    obtain.writeString(str4);
                    this.mRemote.transact(Stub.TRANSACTION_deleteByTerm, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createStringArrayList();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public int deleteByQuery(String str, String str2, String str3, String str4) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeString(str3);
                    obtain.writeString(str4);
                    this.mRemote.transact(Stub.TRANSACTION_deleteByQuery, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public int clearIndex(String str, String str2, Map map, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeMap(map);
                    obtain.writeString(str3);
                    this.mRemote.transact(Stub.TRANSACTION_clearIndex, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public ISearchSession beginSearch(String str, String str2, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeString(str3);
                    this.mRemote.transact(Stub.TRANSACTION_beginSearch, obtain, obtain2, 0);
                    obtain2.readException();
                    return ISearchSession.Stub.asInterface(obtain2.readStrongBinder());
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public void endSearch(String str, String str2, ISearchSession iSearchSession, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeStrongBinder(iSearchSession != null ? iSearchSession.asBinder() : null);
                    obtain.writeString(str3);
                    this.mRemote.transact(Stub.TRANSACTION_endSearch, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public void registerIndexChangeListener(String str, String str2, IIndexChangeCallback iIndexChangeCallback, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeStrongBinder(iIndexChangeCallback != null ? iIndexChangeCallback.asBinder() : null);
                    obtain.writeString(str3);
                    this.mRemote.transact(Stub.TRANSACTION_registerIndexChangeListener, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public void unRegisterIndexChangeListener(String str, String str2, IIndexChangeCallback iIndexChangeCallback, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeStrongBinder(iIndexChangeCallback != null ? iIndexChangeCallback.asBinder() : null);
                    obtain.writeString(str3);
                    this.mRemote.transact(Stub.TRANSACTION_unRegisterIndexChangeListener, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public void registerClientDeathBinder(IBinder iBinder, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStrongBinder(iBinder);
                    obtain.writeString(str);
                    this.mRemote.transact(Stub.TRANSACTION_registerClientDeathBinder, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public void unRegisterClientDeathBinder(IBinder iBinder, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStrongBinder(iBinder);
                    obtain.writeString(str);
                    this.mRemote.transact(Stub.TRANSACTION_unRegisterClientDeathBinder, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public List<String> addFileObserveDirectories(String str, List<String> list, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeStringList(list);
                    obtain.writeString(str2);
                    this.mRemote.transact(Stub.TRANSACTION_addFileObserveDirectories, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createStringArrayList();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public List<String> deleteFileObserveDirectories(String str, List<String> list, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeStringList(list);
                    obtain.writeString(str2);
                    this.mRemote.transact(Stub.TRANSACTION_deleteFileObserveDirectories, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createStringArrayList();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public int setSearchableEntity(SearchableEntity searchableEntity, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (searchableEntity != null) {
                        obtain.writeInt(1);
                        searchableEntity.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeString(str);
                    this.mRemote.transact(Stub.TRANSACTION_setSearchableEntity, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public SearchableEntity getSearchableEntity(String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    this.mRemote.transact(Stub.TRANSACTION_getSearchableEntity, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0 ? SearchableEntity.CREATOR.createFromParcel(obtain2) : null;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public List<SearchableEntity> getSearchableEntityList(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.transact(Stub.TRANSACTION_getSearchableEntityList, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createTypedArrayList(SearchableEntity.CREATOR);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public int setAccessable(String str, boolean z, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(z ? 1 : 0);
                    obtain.writeString(str2);
                    this.mRemote.transact(Stub.TRANSACTION_setAccessable, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public int getAccessable(String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    this.mRemote.transact(Stub.TRANSACTION_getAccessable, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public boolean isIndexCompatible(String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    boolean z = false;
                    this.mRemote.transact(Stub.TRANSACTION_isIndexCompatible, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public int insertLarge(String str, String str2, SharedMemory sharedMemory, SharedMemory sharedMemory2, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    if (sharedMemory != null) {
                        obtain.writeInt(1);
                        sharedMemory.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (sharedMemory2 != null) {
                        obtain.writeInt(1);
                        sharedMemory2.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeString(str3);
                    this.mRemote.transact(Stub.TRANSACTION_insertLarge, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public int updateLarge(String str, String str2, SharedMemory sharedMemory, SharedMemory sharedMemory2, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    if (sharedMemory != null) {
                        obtain.writeInt(1);
                        sharedMemory.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (sharedMemory2 != null) {
                        obtain.writeInt(1);
                        sharedMemory2.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeString(str3);
                    this.mRemote.transact(Stub.TRANSACTION_updateLarge, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public int deleteLarge(String str, String str2, SharedMemory sharedMemory, SharedMemory sharedMemory2, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    if (sharedMemory != null) {
                        obtain.writeInt(1);
                        sharedMemory.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (sharedMemory2 != null) {
                        obtain.writeInt(1);
                        sharedMemory2.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeString(str3);
                    this.mRemote.transact(Stub.TRANSACTION_deleteLarge, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public int setIndexFormSchema(String str, int i, List<IndexForm> list, int i2, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    obtain.writeTypedList(list);
                    obtain.writeInt(i2);
                    obtain.writeString(str2);
                    this.mRemote.transact(Stub.TRANSACTION_setIndexFormSchema, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public boolean registerRemoteSearchCallback(DeviceInfo deviceInfo, IRemoteSearchCallback iRemoteSearchCallback, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = true;
                    if (deviceInfo != null) {
                        obtain.writeInt(1);
                        deviceInfo.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iRemoteSearchCallback != null ? iRemoteSearchCallback.asBinder() : null);
                    obtain.writeString(str);
                    this.mRemote.transact(Stub.TRANSACTION_registerRemoteSearchCallback, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public boolean unregisterRemoteSearchCallback(DeviceInfo deviceInfo, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = true;
                    if (deviceInfo != null) {
                        obtain.writeInt(1);
                        deviceInfo.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeString(str);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterRemoteSearchCallback, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public IRemoteSearchCallback beginRemoteSearch(DeviceInfo deviceInfo, String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (deviceInfo != null) {
                        obtain.writeInt(1);
                        deviceInfo.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    this.mRemote.transact(Stub.TRANSACTION_beginRemoteSearch, obtain, obtain2, 0);
                    obtain2.readException();
                    return IRemoteSearchCallback.Stub.asInterface(obtain2.readStrongBinder());
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public boolean endRemoteSearch(DeviceInfo deviceInfo, String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = true;
                    if (deviceInfo != null) {
                        obtain.writeInt(1);
                        deviceInfo.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    this.mRemote.transact(Stub.TRANSACTION_endRemoteSearch, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public List<DeviceInfo> getOnlineDevices(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.transact(Stub.TRANSACTION_getOnlineDevices, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createTypedArrayList(DeviceInfo.CREATOR);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public boolean registerDeviceChangeCallback(String str, IDeviceChangeCallback iDeviceChangeCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeStrongBinder(iDeviceChangeCallback != null ? iDeviceChangeCallback.asBinder() : null);
                    boolean z = false;
                    this.mRemote.transact(Stub.TRANSACTION_registerDeviceChangeCallback, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchServiceCall
            public boolean unregisterDeviceChangeCallback(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    boolean z = false;
                    this.mRemote.transact(Stub.TRANSACTION_unregisterDeviceChangeCallback, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
