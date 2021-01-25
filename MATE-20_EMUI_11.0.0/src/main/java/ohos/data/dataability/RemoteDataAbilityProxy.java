package ohos.data.dataability;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.aafwk.ability.DataAbilityResult;
import ohos.aafwk.ability.IDataAbility;
import ohos.aafwk.ability.IDataAbilityObserver;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.global.resource.RawFileDescriptor;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.tools.Bytrace;
import ohos.utils.PacMap;
import ohos.utils.Parcel;
import ohos.utils.net.Uri;

public class RemoteDataAbilityProxy implements IDataAbility, IRemoteDataAbility {
    private static final long BYTRACE_TAG = 68719476736L;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109536, "RemoteDataAbilityProxy");
    private static long LOCK_TIMEOUT_SECOND = 10;
    private static final int OBSERVER_MAX_NUM = 1000;
    private final ReentrantLock observerLock = new ReentrantLock();
    private HashMap<IDataAbilityObserver, RemoteDataAbilityObserverStub> observerMap = new HashMap<>();
    private IRemoteObject remote;

    @Override // ohos.aafwk.ability.IDataAbility
    public void close() {
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public void makePersistentUriPermission(Uri uri, int i) throws DataAbilityRemoteException {
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public void notifyChange(Uri uri) {
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public FileDescriptor openFile(Uri uri, String str) throws DataAbilityRemoteException, FileNotFoundException {
        return null;
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public RawFileDescriptor openRawFile(Uri uri, String str) throws DataAbilityRemoteException, FileNotFoundException {
        return null;
    }

    public RemoteDataAbilityProxy(IRemoteObject iRemoteObject) {
        if (iRemoteObject != null) {
            this.remote = iRemoteObject;
            return;
        }
        throw new IllegalArgumentException("remote cannot be null.");
    }

    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public int batchInsert(Uri uri, ValuesBucket[] valuesBucketArr) throws DataAbilityRemoteException {
        HiLog.info(LABEL, "batchInsert start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote batchInsert");
            obtain.writeSequenceable(uri);
            int length = valuesBucketArr != null ? valuesBucketArr.length : 0;
            obtain.writeInt(length);
            for (int i = 0; i < length; i++) {
                obtain.writeSequenceable(valuesBucketArr[i]);
            }
            if (!this.remote.sendRequest(2, obtain, obtain2, new MessageOption())) {
                HiLog.error(LABEL, "batchInsert transact fail", new Object[0]);
                obtain2.reclaim();
                obtain.reclaim();
                Bytrace.finishTrace((long) BYTRACE_TAG, "remote batchInsert");
                return 0;
            }
            SqliteExceptionUtils.readExceptionFromParcel(obtain2);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote batchInsert");
            return readInt;
        } catch (RemoteException e) {
            throw new DataAbilityRemoteException(e.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote batchInsert");
            throw th;
        }
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public PacMap call(String str, String str2, PacMap pacMap) throws DataAbilityRemoteException {
        HiLog.info(LABEL, "call start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote call");
            obtain.writeString(str);
            obtain.writeString(str2);
            obtain.writeSequenceable(pacMap);
            PacMap pacMap2 = null;
            if (!this.remote.sendRequest(3, obtain, obtain2, new MessageOption())) {
                HiLog.error(LABEL, "call transact fail", new Object[0]);
            } else {
                SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                if (obtain2.readInt() == 1) {
                    pacMap2 = new PacMap();
                    pacMap2.unmarshalling(obtain2);
                }
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote call");
            return pacMap2;
        } catch (RemoteException e) {
            throw new DataAbilityRemoteException(e.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote call");
            throw th;
        }
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public int delete(Uri uri, DataAbilityPredicates dataAbilityPredicates) throws DataAbilityRemoteException {
        HiLog.info(LABEL, "delete start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote delete");
            obtain.writeSequenceable(uri);
            obtain.writeSequenceable(dataAbilityPredicates);
            if (!this.remote.sendRequest(4, obtain, obtain2, messageOption)) {
                HiLog.error(LABEL, "delete transact fail", new Object[0]);
                obtain2.reclaim();
                obtain.reclaim();
                Bytrace.finishTrace((long) BYTRACE_TAG, "remote delete");
                return 0;
            }
            SqliteExceptionUtils.readExceptionFromParcel(obtain2);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote delete");
            return readInt;
        } catch (RemoteException e) {
            throw new DataAbilityRemoteException(e.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote delete");
            throw th;
        }
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public DataAbilityResult[] executeBatch(ArrayList<DataAbilityOperation> arrayList) throws DataAbilityRemoteException {
        DataAbilityResult[] dataAbilityResultArr;
        HiLog.info(LABEL, "executeBatch start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote executeBatch");
            int size = arrayList != null ? arrayList.size() : 0;
            obtain.writeInt(size);
            for (int i = 0; i < size; i++) {
                obtain.writeSequenceable(arrayList.get(i));
            }
            if (!this.remote.sendRequest(5, obtain, obtain2, new MessageOption())) {
                HiLog.error(LABEL, "executeBatch transact fail", new Object[0]);
                dataAbilityResultArr = new DataAbilityResult[0];
            } else {
                SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                int readInt = obtain2.readInt();
                if (readInt == 0) {
                    dataAbilityResultArr = new DataAbilityResult[0];
                } else {
                    ArrayList arrayList2 = new ArrayList();
                    for (int i2 = 0; i2 < readInt && obtain2.getReadableBytes() > 0; i2++) {
                        arrayList2.add(DataAbilityResult.createFromParcel(obtain2));
                    }
                    dataAbilityResultArr = (DataAbilityResult[]) arrayList2.toArray(new DataAbilityResult[0]);
                }
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote executeBatch");
            return dataAbilityResultArr;
        } catch (RemoteException e) {
            throw new DataAbilityRemoteException(e.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote executeBatch");
            throw th;
        }
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public String[] getFileTypes(Uri uri, String str) throws DataAbilityRemoteException {
        String[] readStringArray;
        HiLog.info(LABEL, "getFileTypes start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote getFileTypes");
            obtain.writeSequenceable(uri);
            obtain.writeString(str);
            if (!this.remote.sendRequest(6, obtain, obtain2, messageOption)) {
                HiLog.error(LABEL, "getFileTypes transact fail", new Object[0]);
                readStringArray = new String[0];
            } else {
                SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                readStringArray = obtain2.readStringArray();
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote getFileTypes");
            return readStringArray;
        } catch (RemoteException e) {
            throw new DataAbilityRemoteException(e.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote getFileTypes");
            throw th;
        }
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public String getType(Uri uri) throws DataAbilityRemoteException {
        String readString;
        HiLog.info(LABEL, "getType start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote getType");
            obtain.writeSequenceable(uri);
            if (!this.remote.sendRequest(7, obtain, obtain2, messageOption)) {
                HiLog.error(LABEL, "getType transact fail", new Object[0]);
                readString = null;
            } else {
                SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                readString = obtain2.readString();
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote getType");
            return readString;
        } catch (RemoteException e) {
            throw new DataAbilityRemoteException(e.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote getType");
            throw th;
        }
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public int insert(Uri uri, ValuesBucket valuesBucket) throws DataAbilityRemoteException {
        HiLog.info(LABEL, "insert start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote insert");
            obtain.writeSequenceable(uri);
            obtain.writeSequenceable(valuesBucket);
            if (!this.remote.sendRequest(8, obtain, obtain2, messageOption)) {
                HiLog.error(LABEL, "insert transact fail", new Object[0]);
                obtain2.reclaim();
                obtain.reclaim();
                Bytrace.finishTrace((long) BYTRACE_TAG, "remote insert");
                return 0;
            }
            SqliteExceptionUtils.readExceptionFromParcel(obtain2);
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote insert");
            return readInt;
        } catch (RemoteException e) {
            throw new DataAbilityRemoteException(e.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote insert");
            throw th;
        }
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public ResultSet query(Uri uri, String[] strArr, DataAbilityPredicates dataAbilityPredicates) throws DataAbilityRemoteException {
        HiLog.info(LABEL, "query start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        Parcel obtain2 = MessageParcel.obtain();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote query");
            obtain.writeSequenceable(uri);
            if (strArr == null) {
                obtain.writeInt(0);
            } else {
                obtain.writeInt(1);
                obtain.writeStringArray(strArr);
            }
            obtain.writeSequenceable(dataAbilityPredicates);
            RemoteResultSet remoteResultSet = null;
            if (!this.remote.sendRequest(9, obtain, obtain2, new MessageOption())) {
                HiLog.error(LABEL, "query transact fail", new Object[0]);
            } else {
                SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                if (obtain2.readInt() != 0) {
                    ResultSetRemoteTransportDescriptor resultSetRemoteTransportDescriptor = new ResultSetRemoteTransportDescriptor();
                    resultSetRemoteTransportDescriptor.unmarshalling(obtain2);
                    remoteResultSet = new RemoteResultSet(resultSetRemoteTransportDescriptor);
                }
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote query");
            return remoteResultSet;
        } catch (RemoteException e) {
            throw new DataAbilityRemoteException(e.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote query");
            throw th;
        }
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public void registerObserver(Uri uri, Object obj) {
        if (obj instanceof IDataAbilityObserver) {
            HiLog.info(LABEL, "registerObserver start, transaction id = %{public}d", new Object[]{10});
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            obtain.writeSequenceable(uri);
            IDataAbilityObserver iDataAbilityObserver = (IDataAbilityObserver) obj;
            if (!this.observerLock.tryLock(LOCK_TIMEOUT_SECOND, TimeUnit.SECONDS)) {
                HiLog.error(LABEL, "Cannot get observerMap's lock.", new Object[0]);
            } else {
                try {
                    if (this.observerMap.containsKey(iDataAbilityObserver)) {
                        HiLog.error(LABEL, "Observer is already registered.", new Object[0]);
                    } else if (this.observerMap.size() > 1000) {
                        HiLog.error(LABEL, "number of observers out of limit,failed to register observer.", new Object[0]);
                    } else {
                        RemoteDataAbilityObserverStub remoteDataAbilityObserverStub = new RemoteDataAbilityObserverStub("RemoteDataAbilityObserver", iDataAbilityObserver);
                        obtain.writeRemoteObject(remoteDataAbilityObserverStub);
                        boolean sendRequest = this.remote.sendRequest(10, obtain, obtain2, messageOption);
                        SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                        if (sendRequest) {
                            this.observerMap.put(iDataAbilityObserver, remoteDataAbilityObserverStub);
                            HiLog.debug(LABEL, "put observer:%{private}s, stub:%{private}s", new Object[]{iDataAbilityObserver, remoteDataAbilityObserverStub});
                        } else {
                            HiLog.error(LABEL, "register transact fail", new Object[0]);
                        }
                        this.observerLock.unlock();
                        obtain2.reclaim();
                        obtain.reclaim();
                        return;
                    }
                } catch (InterruptedException | RemoteException e) {
                    HiLog.error(LABEL, "registerObserver remote exception: %{private}s", new Object[]{e.getMessage()});
                } catch (Throwable th) {
                    this.observerLock.unlock();
                    obtain2.reclaim();
                    obtain.reclaim();
                    throw th;
                }
            }
            this.observerLock.unlock();
            obtain2.reclaim();
            obtain.reclaim();
            return;
        }
        throw new IllegalArgumentException("registerObserver: observer type is incorrect.");
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public void unregisterObserver(Object obj) {
        if (obj instanceof IDataAbilityObserver) {
            HiLog.info(LABEL, "unregisterObserver start", new Object[0]);
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            if (!this.observerLock.tryLock(LOCK_TIMEOUT_SECOND, TimeUnit.SECONDS)) {
                HiLog.error(LABEL, "Cannot get observerMap's lock.", new Object[0]);
            } else {
                try {
                    RemoteDataAbilityObserverStub remoteDataAbilityObserverStub = this.observerMap.get(obj);
                    if (remoteDataAbilityObserverStub == null) {
                        HiLog.error(LABEL, "Observer was not registered.", new Object[0]);
                    } else {
                        obtain.writeRemoteObject(remoteDataAbilityObserverStub);
                        boolean sendRequest = this.remote.sendRequest(11, obtain, obtain2, messageOption);
                        SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                        if (sendRequest) {
                            this.observerMap.remove(obj);
                            HiLog.debug(LABEL, "remove observer:%{private}s", new Object[]{obj});
                        } else {
                            HiLog.error(LABEL, "unregisterObserver failed", new Object[0]);
                        }
                        this.observerLock.unlock();
                        obtain2.reclaim();
                        obtain.reclaim();
                        return;
                    }
                } catch (InterruptedException | RemoteException e) {
                    HiLog.error(LABEL, "unregisterObserver remote exception: %{private}s", new Object[]{e.getMessage()});
                } catch (Throwable th) {
                    this.observerLock.unlock();
                    obtain2.reclaim();
                    obtain.reclaim();
                    throw th;
                }
            }
            this.observerLock.unlock();
            obtain2.reclaim();
            obtain.reclaim();
            return;
        }
        throw new IllegalArgumentException("registerObserver: observer type is incorrect.");
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public int update(Uri uri, ValuesBucket valuesBucket, DataAbilityPredicates dataAbilityPredicates) throws DataAbilityRemoteException {
        HiLog.info(LABEL, "update start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote update");
            obtain.writeSequenceable(uri);
            obtain.writeSequenceable(valuesBucket);
            obtain.writeSequenceable(dataAbilityPredicates);
            boolean sendRequest = this.remote.sendRequest(12, obtain, obtain2, messageOption);
            SqliteExceptionUtils.readExceptionFromParcel(obtain2);
            if (!sendRequest) {
                HiLog.error(LABEL, "update transact fail", new Object[0]);
                obtain2.reclaim();
                obtain.reclaim();
                Bytrace.finishTrace((long) BYTRACE_TAG, "remote update");
                return 0;
            }
            int readInt = obtain2.readInt();
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote update");
            return readInt;
        } catch (RemoteException e) {
            throw new DataAbilityRemoteException(e.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote update");
            throw th;
        }
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public Uri normalizeUri(Uri uri) throws DataAbilityRemoteException {
        Uri readFromParcel;
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote normalizeUri");
            obtain.writeSequenceable(uri);
            boolean sendRequest = this.remote.sendRequest(13, obtain, obtain2, messageOption);
            SqliteExceptionUtils.readExceptionFromParcel(obtain2);
            if (!sendRequest) {
                HiLog.error(LABEL, "normalizeUri transact failed", new Object[0]);
                readFromParcel = null;
            } else {
                readFromParcel = Uri.readFromParcel(obtain2);
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote normalizeUri");
            return readFromParcel;
        } catch (RemoteException e) {
            throw new DataAbilityRemoteException(e.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote normalizeUri");
            throw th;
        }
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public Uri denormalizeUri(Uri uri) throws DataAbilityRemoteException {
        Uri readFromParcel;
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote denormalizeUri");
            obtain.writeSequenceable(uri);
            boolean sendRequest = this.remote.sendRequest(14, obtain, obtain2, messageOption);
            SqliteExceptionUtils.readExceptionFromParcel(obtain2);
            if (!sendRequest) {
                HiLog.error(LABEL, "denormalizeUri transact fail", new Object[0]);
                readFromParcel = null;
            } else {
                readFromParcel = Uri.readFromParcel(obtain2);
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote denormalizeUri");
            return readFromParcel;
        } catch (RemoteException e) {
            throw new DataAbilityRemoteException(e.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote denormalizeUri");
            throw th;
        }
    }
}
