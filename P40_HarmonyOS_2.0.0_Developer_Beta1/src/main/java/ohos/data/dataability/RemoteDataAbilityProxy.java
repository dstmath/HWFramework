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
import ohos.data.dataability.impl.IRemoteDataAbility;
import ohos.data.dataability.impl.OldProxyMainVersionException;
import ohos.data.dataability.impl.OldProxyNotSupportException;
import ohos.data.dataability.impl.OldProxyWithCompatibleResponseException;
import ohos.data.dataability.impl.OldProxyWithDefaultReplyException;
import ohos.data.dataability.impl.RemoteDataAbilityObserverStub;
import ohos.data.dataability.impl.RemoteResultSet;
import ohos.data.dataability.impl.ResultSetRemoteTransportDescriptor;
import ohos.data.dataability.impl.SqliteExceptionUtils;
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
        int readInt;
        HiLog.info(LABEL, "batchInsert start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote batchInsert");
            writeVersionToParcel(obtain, 1);
            obtain.writeSequenceable(uri);
            int length = valuesBucketArr != null ? valuesBucketArr.length : 0;
            obtain.writeInt(length);
            for (int i = 0; i < length; i++) {
                obtain.writeSequenceable(valuesBucketArr[i]);
            }
            if (!this.remote.sendRequest(2, obtain, obtain2, new MessageOption())) {
                HiLog.error(LABEL, "batchInsert transact fail", new Object[0]);
            } else {
                try {
                    SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    readInt = obtain2.readInt();
                } catch (OldProxyWithCompatibleResponseException | OldProxyWithDefaultReplyException e) {
                    HiLog.info(LABEL, "batchInsert transact with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e.getMessage()});
                    if (e instanceof OldProxyWithCompatibleResponseException) {
                        SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    }
                    readInt = obtain2.readInt();
                } catch (OldProxyMainVersionException | OldProxyNotSupportException e2) {
                    HiLog.error(LABEL, "batchInsert transact fail with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e2.getMessage()});
                }
                obtain2.reclaim();
                obtain.reclaim();
                Bytrace.finishTrace((long) BYTRACE_TAG, "remote batchInsert");
                return readInt;
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote batchInsert");
            return 0;
        } catch (RemoteException e3) {
            throw new DataAbilityRemoteException(e3.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote batchInsert");
            throw th;
        }
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public PacMap call(String str, String str2, PacMap pacMap) throws DataAbilityRemoteException {
        PacMap createPacMapFromParcel;
        HiLog.info(LABEL, "call start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote call");
            writeVersionToParcel(obtain, 1);
            obtain.writeString(str);
            obtain.writeString(str2);
            obtain.writeSequenceable(pacMap);
            if (!this.remote.sendRequest(3, obtain, obtain2, new MessageOption())) {
                HiLog.error(LABEL, "call transact fail", new Object[0]);
            } else {
                try {
                    SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    createPacMapFromParcel = createPacMapFromParcel(obtain2);
                } catch (OldProxyWithCompatibleResponseException | OldProxyWithDefaultReplyException e) {
                    HiLog.info(LABEL, "call transact with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e.getMessage()});
                    if (e instanceof OldProxyWithCompatibleResponseException) {
                        SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    }
                    createPacMapFromParcel = this.createPacMapFromParcel(obtain2);
                } catch (OldProxyMainVersionException | OldProxyNotSupportException e2) {
                    HiLog.error(LABEL, "call transact fail with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e2.getMessage()});
                }
                obtain2.reclaim();
                obtain.reclaim();
                Bytrace.finishTrace((long) BYTRACE_TAG, "remote call");
                return createPacMapFromParcel;
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote call");
            return null;
        } catch (RemoteException e3) {
            throw new DataAbilityRemoteException(e3.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote call");
            throw th;
        }
    }

    private PacMap createPacMapFromParcel(MessageParcel messageParcel) {
        if (messageParcel.readInt() != 1) {
            return null;
        }
        PacMap pacMap = new PacMap();
        pacMap.unmarshalling(messageParcel);
        return pacMap;
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public int delete(Uri uri, DataAbilityPredicates dataAbilityPredicates) throws DataAbilityRemoteException {
        int readInt;
        HiLog.info(LABEL, "delete start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote delete");
            writeVersionToParcel(obtain, 1);
            obtain.writeSequenceable(uri);
            obtain.writeSequenceable(dataAbilityPredicates);
            if (!this.remote.sendRequest(4, obtain, obtain2, new MessageOption())) {
                HiLog.error(LABEL, "delete transact fail", new Object[0]);
            } else {
                try {
                    SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    readInt = obtain2.readInt();
                } catch (OldProxyWithCompatibleResponseException | OldProxyWithDefaultReplyException e) {
                    HiLog.info(LABEL, "delete transact with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e.getMessage()});
                    if (e instanceof OldProxyWithCompatibleResponseException) {
                        SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    }
                    readInt = obtain2.readInt();
                } catch (OldProxyMainVersionException | OldProxyNotSupportException e2) {
                    HiLog.error(LABEL, "delete transact fail with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e2.getMessage()});
                }
                obtain2.reclaim();
                obtain.reclaim();
                Bytrace.finishTrace((long) BYTRACE_TAG, "remote delete");
                return readInt;
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote delete");
            return 0;
        } catch (RemoteException e3) {
            throw new DataAbilityRemoteException(e3.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote delete");
            throw th;
        }
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public DataAbilityResult[] executeBatch(ArrayList<DataAbilityOperation> arrayList) throws DataAbilityRemoteException {
        DataAbilityResult[] createAbilityResultFromParcel;
        HiLog.info(LABEL, "executeBatch start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote executeBatch");
            writeVersionToParcel(obtain, 1);
            int size = arrayList != null ? arrayList.size() : 0;
            obtain.writeInt(size);
            for (int i = 0; i < size; i++) {
                obtain.writeSequenceable(arrayList.get(i));
            }
            if (!this.remote.sendRequest(5, obtain, obtain2, new MessageOption())) {
                HiLog.error(LABEL, "executeBatch transact fail", new Object[0]);
                createAbilityResultFromParcel = new DataAbilityResult[0];
            } else {
                try {
                    SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    createAbilityResultFromParcel = createAbilityResultFromParcel(obtain2);
                } catch (OldProxyWithCompatibleResponseException | OldProxyWithDefaultReplyException e) {
                    HiLog.info(LABEL, "executeBatch transact with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e.getMessage()});
                    if (e instanceof OldProxyWithCompatibleResponseException) {
                        SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    }
                    createAbilityResultFromParcel = this.createAbilityResultFromParcel(obtain2);
                } catch (OldProxyMainVersionException | OldProxyNotSupportException e2) {
                    HiLog.error(LABEL, "executeBatch transact fail with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e2.getMessage()});
                    createAbilityResultFromParcel = new DataAbilityResult[0];
                }
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote executeBatch");
            return createAbilityResultFromParcel;
        } catch (RemoteException e3) {
            throw new DataAbilityRemoteException(e3.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote executeBatch");
            throw th;
        }
    }

    private DataAbilityResult[] createAbilityResultFromParcel(MessageParcel messageParcel) {
        int readInt = messageParcel.readInt();
        if (readInt == 0) {
            return new DataAbilityResult[0];
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < readInt && messageParcel.getReadableBytes() > 0; i++) {
            arrayList.add(DataAbilityResult.createFromParcel(messageParcel));
        }
        return (DataAbilityResult[]) arrayList.toArray(new DataAbilityResult[0]);
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public String[] getFileTypes(Uri uri, String str) throws DataAbilityRemoteException {
        String[] readStringArray;
        HiLog.info(LABEL, "getFileTypes start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote getFileTypes");
            writeVersionToParcel(obtain, 1);
            obtain.writeSequenceable(uri);
            obtain.writeString(str);
            if (!this.remote.sendRequest(6, obtain, obtain2, new MessageOption())) {
                HiLog.error(LABEL, "getFileTypes transact fail", new Object[0]);
                readStringArray = new String[0];
            } else {
                try {
                    SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    readStringArray = obtain2.readStringArray();
                } catch (OldProxyWithCompatibleResponseException | OldProxyWithDefaultReplyException e) {
                    HiLog.info(LABEL, "getFileTypes transact with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e.getMessage()});
                    if (e instanceof OldProxyWithCompatibleResponseException) {
                        SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    }
                    readStringArray = obtain2.readStringArray();
                } catch (OldProxyMainVersionException | OldProxyNotSupportException e2) {
                    HiLog.error(LABEL, "getFileTypes transact fail with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e2.getMessage()});
                    readStringArray = new String[0];
                }
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote getFileTypes");
            return readStringArray;
        } catch (RemoteException e3) {
            throw new DataAbilityRemoteException(e3.getMessage());
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
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote getType");
            writeVersionToParcel(obtain, 1);
            if (!this.remote.sendRequest(7, obtain, obtain2, new MessageOption())) {
                HiLog.error(LABEL, "getType transact fail", new Object[0]);
            } else {
                try {
                    SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    readString = obtain2.readString();
                } catch (OldProxyWithCompatibleResponseException | OldProxyWithDefaultReplyException e) {
                    HiLog.info(LABEL, "getType transact with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e.getMessage()});
                    if (e instanceof OldProxyWithCompatibleResponseException) {
                        SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    }
                    readString = obtain2.readString();
                } catch (OldProxyMainVersionException | OldProxyNotSupportException e2) {
                    HiLog.error(LABEL, "getType transact fail with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e2.getMessage()});
                }
                obtain2.reclaim();
                obtain.reclaim();
                Bytrace.finishTrace((long) BYTRACE_TAG, "remote getType");
                return readString;
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote getType");
            return null;
        } catch (RemoteException e3) {
            throw new DataAbilityRemoteException(e3.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote getType");
            throw th;
        }
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public int insert(Uri uri, ValuesBucket valuesBucket) throws DataAbilityRemoteException {
        int readInt;
        HiLog.info(LABEL, "insert start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote insert");
            writeVersionToParcel(obtain, 1);
            obtain.writeSequenceable(uri);
            obtain.writeSequenceable(valuesBucket);
            if (!this.remote.sendRequest(8, obtain, obtain2, new MessageOption())) {
                HiLog.error(LABEL, "insert transact fail", new Object[0]);
            } else {
                try {
                    SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    readInt = obtain2.readInt();
                } catch (OldProxyWithCompatibleResponseException | OldProxyWithDefaultReplyException e) {
                    HiLog.info(LABEL, "insert transact with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e.getMessage()});
                    if (e instanceof OldProxyWithCompatibleResponseException) {
                        SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    }
                    readInt = obtain2.readInt();
                } catch (OldProxyMainVersionException | OldProxyNotSupportException e2) {
                    HiLog.error(LABEL, "insert transact fail with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e2.getMessage()});
                }
                obtain2.reclaim();
                obtain.reclaim();
                Bytrace.finishTrace((long) BYTRACE_TAG, "remote insert");
                return readInt;
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote insert");
            return 0;
        } catch (RemoteException e3) {
            throw new DataAbilityRemoteException(e3.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote insert");
            throw th;
        }
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public ResultSet query(Uri uri, String[] strArr, DataAbilityPredicates dataAbilityPredicates) throws DataAbilityRemoteException {
        RemoteResultSet createRemoteResultSetFromParcel;
        HiLog.info(LABEL, "query start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote query");
            writeVersionToParcel(obtain, 1);
            obtain.writeSequenceable(uri);
            if (strArr == null) {
                obtain.writeInt(0);
            } else {
                obtain.writeInt(1);
                obtain.writeStringArray(strArr);
            }
            obtain.writeSequenceable(dataAbilityPredicates);
            if (!this.remote.sendRequest(9, obtain, obtain2, new MessageOption())) {
                HiLog.error(LABEL, "query transact fail", new Object[0]);
            } else {
                try {
                    SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    createRemoteResultSetFromParcel = createRemoteResultSetFromParcel(obtain2);
                } catch (OldProxyWithCompatibleResponseException | OldProxyWithDefaultReplyException e) {
                    HiLog.info(LABEL, "query transact with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e.getMessage()});
                    if (e instanceof OldProxyWithCompatibleResponseException) {
                        SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    }
                    createRemoteResultSetFromParcel = this.createRemoteResultSetFromParcel(obtain2);
                } catch (OldProxyMainVersionException | OldProxyNotSupportException e2) {
                    HiLog.error(LABEL, "query transact fail with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e2.getMessage()});
                }
                obtain2.reclaim();
                obtain.reclaim();
                Bytrace.finishTrace((long) BYTRACE_TAG, "remote query");
                return createRemoteResultSetFromParcel;
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote query");
            return null;
        } catch (RemoteException e3) {
            throw new DataAbilityRemoteException(e3.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote query");
            throw th;
        }
    }

    private RemoteResultSet createRemoteResultSetFromParcel(MessageParcel messageParcel) {
        if (messageParcel.readInt() == 0) {
            return null;
        }
        ResultSetRemoteTransportDescriptor resultSetRemoteTransportDescriptor = new ResultSetRemoteTransportDescriptor();
        resultSetRemoteTransportDescriptor.unmarshalling(messageParcel);
        return new RemoteResultSet(resultSetRemoteTransportDescriptor);
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public void registerObserver(Uri uri, Object obj) {
        if (obj instanceof IDataAbilityObserver) {
            HiLog.info(LABEL, "registerObserver start, transaction id = %{public}d", new Object[]{10});
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            writeVersionToParcel(obtain, 1);
            obtain.writeSequenceable(uri);
            IDataAbilityObserver iDataAbilityObserver = (IDataAbilityObserver) obj;
            if (!checkObserverValidity(iDataAbilityObserver)) {
                this.observerLock.unlock();
                obtain2.reclaim();
                obtain.reclaim();
                return;
            }
            try {
                RemoteDataAbilityObserverStub remoteDataAbilityObserverStub = new RemoteDataAbilityObserverStub("RemoteDataAbilityObserver", iDataAbilityObserver);
                obtain.writeRemoteObject(remoteDataAbilityObserverStub);
                boolean sendRequest = this.remote.sendRequest(10, obtain, obtain2, new MessageOption());
                SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                if (sendRequest) {
                    this.observerMap.put(iDataAbilityObserver, remoteDataAbilityObserverStub);
                    HiLog.debug(LABEL, "put observer:%{private}s, stub:%{private}s", new Object[]{iDataAbilityObserver, remoteDataAbilityObserverStub});
                } else {
                    HiLog.error(LABEL, "register transact fail", new Object[0]);
                }
            } catch (OldProxyWithCompatibleResponseException | OldProxyWithDefaultReplyException e) {
                HiLog.info(LABEL, "registerObserver transact with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e.getMessage()});
                if (e instanceof OldProxyWithCompatibleResponseException) {
                    SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                }
            } catch (OldProxyMainVersionException | OldProxyNotSupportException e2) {
                HiLog.error(LABEL, "registerObserver transact fail with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e2.getMessage()});
            } catch (InterruptedException | RemoteException e3) {
                HiLog.error(LABEL, "registerObserver remote exception: %{private}s", new Object[]{e3.getMessage()});
            } catch (Throwable th) {
                this.observerLock.unlock();
                obtain2.reclaim();
                obtain.reclaim();
                throw th;
            }
            this.observerLock.unlock();
            obtain2.reclaim();
            obtain.reclaim();
            return;
        }
        throw new IllegalArgumentException("registerObserver: observer type is incorrect.");
    }

    private boolean checkObserverValidity(IDataAbilityObserver iDataAbilityObserver) throws InterruptedException {
        if (!this.observerLock.tryLock(LOCK_TIMEOUT_SECOND, TimeUnit.SECONDS)) {
            HiLog.error(LABEL, "Cannot get observerMap's lock.", new Object[0]);
            return false;
        } else if (this.observerMap.containsKey(iDataAbilityObserver)) {
            HiLog.error(LABEL, "Observer is already registered.", new Object[0]);
            return false;
        } else if (this.observerMap.size() <= 1000) {
            return true;
        } else {
            HiLog.error(LABEL, "number of observers out of limit,failed to register observer.", new Object[0]);
            return false;
        }
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public void unregisterObserver(Object obj) {
        if (obj instanceof IDataAbilityObserver) {
            HiLog.info(LABEL, "unregisterObserver start", new Object[0]);
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            writeVersionToParcel(obtain, 1);
            if (!this.observerLock.tryLock(LOCK_TIMEOUT_SECOND, TimeUnit.SECONDS)) {
                HiLog.error(LABEL, "Cannot get observerMap's lock.", new Object[0]);
            } else {
                try {
                    RemoteDataAbilityObserverStub remoteDataAbilityObserverStub = this.observerMap.get(obj);
                    if (remoteDataAbilityObserverStub == null) {
                        HiLog.error(LABEL, "Observer was not registered.", new Object[0]);
                    } else {
                        obtain.writeRemoteObject(remoteDataAbilityObserverStub);
                        boolean sendRequest = this.remote.sendRequest(11, obtain, obtain2, new MessageOption());
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
                } catch (OldProxyWithCompatibleResponseException | OldProxyWithDefaultReplyException e) {
                    HiLog.info(LABEL, "unregisterObserver transact with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e.getMessage()});
                    if (e instanceof OldProxyWithCompatibleResponseException) {
                        SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    }
                } catch (OldProxyMainVersionException | OldProxyNotSupportException e2) {
                    HiLog.error(LABEL, "unregisterObserver transact fail with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e2.getMessage()});
                } catch (InterruptedException | RemoteException e3) {
                    HiLog.error(LABEL, "unregisterObserver remote exception: %{private}s", new Object[]{e3.getMessage()});
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
        int readInt;
        HiLog.info(LABEL, "update start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote update");
            writeVersionToParcel(obtain, 1);
            obtain.writeSequenceable(uri);
            obtain.writeSequenceable(valuesBucket);
            obtain.writeSequenceable(dataAbilityPredicates);
            boolean sendRequest = this.remote.sendRequest(12, obtain, obtain2, new MessageOption());
            SqliteExceptionUtils.readExceptionFromParcel(obtain2);
            if (!sendRequest) {
                HiLog.error(LABEL, "update transact fail", new Object[0]);
            } else {
                try {
                    readInt = obtain2.readInt();
                } catch (OldProxyWithCompatibleResponseException | OldProxyWithDefaultReplyException e) {
                    HiLog.info(LABEL, "update transact with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e.getMessage()});
                    if (e instanceof OldProxyWithCompatibleResponseException) {
                        SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    }
                    readInt = obtain2.readInt();
                } catch (OldProxyMainVersionException | OldProxyNotSupportException e2) {
                    HiLog.error(LABEL, "update transact fail with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e2.getMessage()});
                }
                obtain2.reclaim();
                obtain.reclaim();
                Bytrace.finishTrace((long) BYTRACE_TAG, "remote update");
                return readInt;
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote update");
            return 0;
        } catch (RemoteException e3) {
            throw new DataAbilityRemoteException(e3.getMessage());
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
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote normalizeUri");
            writeVersionToParcel(obtain, 1);
            obtain.writeSequenceable(uri);
            boolean sendRequest = this.remote.sendRequest(13, obtain, obtain2, new MessageOption());
            SqliteExceptionUtils.readExceptionFromParcel(obtain2);
            if (!sendRequest) {
                HiLog.error(LABEL, "normalizeUri transact failed", new Object[0]);
            } else {
                try {
                    readFromParcel = Uri.readFromParcel(obtain2);
                } catch (OldProxyWithCompatibleResponseException | OldProxyWithDefaultReplyException e) {
                    HiLog.info(LABEL, "normalizeUri transact with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e.getMessage()});
                    if (e instanceof OldProxyWithCompatibleResponseException) {
                        SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    }
                    readFromParcel = Uri.readFromParcel(obtain2);
                } catch (OldProxyMainVersionException | OldProxyNotSupportException e2) {
                    HiLog.error(LABEL, "normalizeUri transact fail with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e2.getMessage()});
                }
                obtain2.reclaim();
                obtain.reclaim();
                Bytrace.finishTrace((long) BYTRACE_TAG, "remote normalizeUri");
                return readFromParcel;
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote normalizeUri");
            return null;
        } catch (RemoteException e3) {
            throw new DataAbilityRemoteException(e3.getMessage());
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
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote denormalizeUri");
            writeVersionToParcel(obtain, 1);
            obtain.writeSequenceable(uri);
            boolean sendRequest = this.remote.sendRequest(14, obtain, obtain2, new MessageOption());
            SqliteExceptionUtils.readExceptionFromParcel(obtain2);
            if (!sendRequest) {
                HiLog.error(LABEL, "denormalizeUri transact fail", new Object[0]);
            } else {
                try {
                    readFromParcel = Uri.readFromParcel(obtain2);
                } catch (OldProxyWithCompatibleResponseException | OldProxyWithDefaultReplyException e) {
                    HiLog.info(LABEL, "denormalizeUri transact with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e.getMessage()});
                    if (e instanceof OldProxyWithCompatibleResponseException) {
                        SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    }
                    readFromParcel = Uri.readFromParcel(obtain2);
                } catch (OldProxyMainVersionException | OldProxyNotSupportException e2) {
                    HiLog.error(LABEL, "denormalizeUri transact fail with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e2.getMessage()});
                }
                obtain2.reclaim();
                obtain.reclaim();
                Bytrace.finishTrace((long) BYTRACE_TAG, "remote denormalizeUri");
                return readFromParcel;
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote denormalizeUri");
            return null;
        } catch (RemoteException e3) {
            throw new DataAbilityRemoteException(e3.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote denormalizeUri");
            throw th;
        }
    }

    @Override // ohos.aafwk.ability.IDataAbility
    public boolean reload(Uri uri, PacMap pacMap) throws DataAbilityRemoteException {
        boolean readBoolean;
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            Bytrace.startTrace((long) BYTRACE_TAG, "remote reload");
            writeVersionToParcel(obtain, 1);
            obtain.writeSequenceable(uri);
            obtain.writeSequenceable(pacMap);
            boolean sendRequest = this.remote.sendRequest(15, obtain, obtain2, new MessageOption());
            SqliteExceptionUtils.readExceptionFromParcel(obtain2);
            if (!sendRequest) {
                HiLog.error(LABEL, "reload transact fail", new Object[0]);
            } else {
                try {
                    readBoolean = obtain2.readBoolean();
                } catch (OldProxyWithCompatibleResponseException | OldProxyWithDefaultReplyException e) {
                    HiLog.info(LABEL, "normalizeUri transact with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e.getMessage()});
                    if (e instanceof OldProxyWithCompatibleResponseException) {
                        SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                    }
                    readBoolean = obtain2.readBoolean();
                } catch (OldProxyMainVersionException | OldProxyNotSupportException e2) {
                    HiLog.error(LABEL, "normalizeUri transact fail with new server version as %{private}d : %{private}s", new Object[]{Integer.valueOf(obtain2.readInt()), e2.getMessage()});
                }
                obtain2.reclaim();
                obtain.reclaim();
                Bytrace.finishTrace((long) BYTRACE_TAG, "remote reload");
                return readBoolean;
            }
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote reload");
            return false;
        } catch (RemoteException e3) {
            throw new DataAbilityRemoteException(e3.getMessage());
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            Bytrace.finishTrace((long) BYTRACE_TAG, "remote reload");
            throw th;
        }
    }

    private void writeVersionToParcel(MessageParcel messageParcel, int i) {
        messageParcel.writeInt(1);
        messageParcel.writeInt(i);
    }
}
