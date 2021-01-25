package ohos.data.searchimpl.connect;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ohos.data.search.SearchResult;
import ohos.data.searchimpl.connect.ISearchSession;
import ohos.data.searchimpl.model.InnerIndexData;
import ohos.data.searchimpl.model.InnerIndexForm;
import ohos.data.searchimpl.model.InnerSearchableEntity;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public interface IHOSPSearchServiceCall extends IInterface {
    public static final String DESCRIPTOR = "com.huawei.searchservice.service.IHOSPSearchServiceCall";
    public static final int TRANSACTION_beginSearch = 11;
    public static final int TRANSACTION_clearIndex = 10;
    public static final int TRANSACTION_clearIndexForm = 4;
    public static final int TRANSACTION_delete = 7;
    public static final int TRANSACTION_deleteByQuery = 9;
    public static final int TRANSACTION_deleteByTerm = 8;
    public static final int TRANSACTION_endSearch = 12;
    public static final int TRANSACTION_getIndexForm = 2;
    public static final int TRANSACTION_getIndexFormVersion = 3;
    public static final int TRANSACTION_getSearchableEntity = 18;
    public static final int TRANSACTION_getSearchableEntityList = 19;
    public static final int TRANSACTION_insert = 5;
    public static final int TRANSACTION_registerClientDeathBinder = 15;
    public static final int TRANSACTION_registerIndexChangeListener = 13;
    public static final int TRANSACTION_setIndexForm = 1;
    public static final int TRANSACTION_setSearchableEntity = 17;
    public static final int TRANSACTION_unRegisterClientDeathBinder = 16;
    public static final int TRANSACTION_unRegisterIndexChangeListener = 14;
    public static final int TRANSACTION_update = 6;

    ISearchSession beginSearch(String str, String str2, String str3);

    int clearIndex(String str, String str2, Map<String, List<String>> map, String str3);

    int clearIndexForm(String str, String str2);

    List<InnerIndexData> delete(String str, String str2, List<InnerIndexData> list, String str3);

    int deleteByQuery(String str, String str2, String str3, String str4);

    List<String> deleteByTerm(String str, String str2, String str3, List<String> list, String str4);

    void endSearch(String str, String str2, ISearchSession iSearchSession, String str3);

    List<InnerIndexForm> getIndexForm(String str, String str2);

    int getIndexFormVersion(String str, String str2);

    InnerSearchableEntity getSearchableEntity(String str, String str2);

    List<InnerSearchableEntity> getSearchableEntityList(String str);

    List<InnerIndexData> insert(String str, String str2, List<InnerIndexData> list, String str3);

    void registerClientDeathBinder(IBinder iBinder, String str);

    void registerIndexChangeListener(String str, String str2, IIndexChangeCallback iIndexChangeCallback, String str3);

    int setIndexForm(String str, int i, List<InnerIndexForm> list, String str2);

    int setSearchableEntity(InnerSearchableEntity innerSearchableEntity, String str);

    void unRegisterClientDeathBinder(IBinder iBinder, String str);

    void unRegisterIndexChangeListener(String str, String str2, IIndexChangeCallback iIndexChangeCallback, String str3);

    List<InnerIndexData> update(String str, String str2, List<InnerIndexData> list, String str3);

    public static class Proxy implements IHOSPSearchServiceCall {
        private static final HiLogLabel LABEL = new HiLogLabel(3, 218109504, "IHSOPSearchServiceCall");
        private IBinder mRemote;

        public Proxy(IBinder iBinder) {
            this.mRemote = iBinder;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this.mRemote;
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public int setIndexForm(String str, int i, List<InnerIndexForm> list, String str2) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            int i2 = 0;
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeInt(i);
                obtain.writeTypedList(list);
                obtain.writeString(str2);
                this.mRemote.transact(1, obtain, obtain2, 0);
                obtain2.readException();
                i2 = obtain2.readInt();
            } catch (RemoteException e) {
                HiLog.error(LABEL, "set indexForm remote exception, %{public}s", new Object[]{e.getMessage()});
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            return i2;
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public List<InnerIndexForm> getIndexForm(String str, String str2) {
            ArrayList arrayList;
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeString(str2);
                this.mRemote.transact(2, obtain, obtain2, 0);
                obtain2.readException();
                arrayList = obtain2.createTypedArrayList(InnerIndexForm.CREATOR);
            } catch (RemoteException e) {
                HiLog.error(LABEL, "get indexForm remote exception, %{public}s", new Object[]{e.getMessage()});
                arrayList = null;
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            return arrayList;
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public int getIndexFormVersion(String str, String str2) {
            int i;
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeString(str2);
                this.mRemote.transact(3, obtain, obtain2, 0);
                obtain2.readException();
                i = obtain2.readInt();
            } catch (RemoteException e) {
                HiLog.error(LABEL, "get indexForm version remote exception, %{public}s", new Object[]{e.getMessage()});
                i = -1;
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            return i;
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public int clearIndexForm(String str, String str2) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            int i = 0;
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeString(str2);
                this.mRemote.transact(4, obtain, obtain2, 0);
                obtain2.readException();
                i = obtain2.readInt();
            } catch (RemoteException e) {
                HiLog.error(LABEL, "clear indexForm remote exception, %{public}s", new Object[]{e.getMessage()});
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            return i;
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public List<InnerIndexData> insert(String str, String str2, List<InnerIndexData> list, String str3) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeString(str2);
                obtain.writeTypedList(list);
                obtain.writeString(str3);
                this.mRemote.transact(5, obtain, obtain2, 0);
                obtain2.readException();
                list = obtain2.createTypedArrayList(InnerIndexData.CREATOR);
            } catch (RemoteException e) {
                HiLog.error(LABEL, "insert index remote exception, %{public}s", new Object[]{e.getMessage()});
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            return list;
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public List<InnerIndexData> update(String str, String str2, List<InnerIndexData> list, String str3) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeString(str2);
                obtain.writeTypedList(list);
                obtain.writeString(str3);
                this.mRemote.transact(6, obtain, obtain2, 0);
                obtain2.readException();
                list = obtain2.createTypedArrayList(InnerIndexData.CREATOR);
            } catch (RemoteException e) {
                HiLog.error(LABEL, "update index remote exception, %{public}s", new Object[]{e.getMessage()});
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            return list;
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public List<InnerIndexData> delete(String str, String str2, List<InnerIndexData> list, String str3) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeString(str2);
                obtain.writeTypedList(list);
                obtain.writeString(str3);
                this.mRemote.transact(7, obtain, obtain2, 0);
                obtain2.readException();
                list = obtain2.createTypedArrayList(InnerIndexData.CREATOR);
            } catch (RemoteException e) {
                HiLog.error(LABEL, "delete index remote exception, %{public}s", new Object[]{e.getMessage()});
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            return list;
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public List<String> deleteByTerm(String str, String str2, String str3, List<String> list, String str4) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeString(str2);
                obtain.writeString(str3);
                obtain.writeStringList(list);
                obtain.writeString(str4);
                this.mRemote.transact(8, obtain, obtain2, 0);
                obtain2.readException();
                list = obtain2.createStringArrayList();
            } catch (RemoteException e) {
                HiLog.error(LABEL, "delete index by term remote exception, %{public}s", new Object[]{e.getMessage()});
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            return list;
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public int deleteByQuery(String str, String str2, String str3, String str4) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            int i = 0;
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeString(str2);
                obtain.writeString(str3);
                obtain.writeString(str4);
                this.mRemote.transact(9, obtain, obtain2, 0);
                obtain2.readException();
                i = obtain2.readInt();
            } catch (RemoteException e) {
                HiLog.error(LABEL, "delete index by query remote exception, %{public}s", new Object[]{e.getMessage()});
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            return i;
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public int clearIndex(String str, String str2, Map<String, List<String>> map, String str3) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            int i = 0;
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeString(str2);
                obtain.writeMap(map);
                obtain.writeString(str3);
                this.mRemote.transact(10, obtain, obtain2, 0);
                obtain2.readException();
                i = obtain2.readInt();
            } catch (RemoteException e) {
                HiLog.error(LABEL, "clear index remote exception, %{public}s", new Object[]{e.getMessage()});
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            return i;
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public ISearchSession beginSearch(String str, String str2, String str3) {
            ISearchSession.Proxy proxy;
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeString(str2);
                obtain.writeString(str3);
                this.mRemote.transact(11, obtain, obtain2, 0);
                obtain2.readException();
                proxy = new ISearchSession.Proxy(obtain2.readStrongBinder());
            } catch (RemoteException e) {
                HiLog.error(LABEL, "begin search session remote exception, %{public}s", new Object[]{e.getMessage()});
                proxy = null;
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            return proxy;
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public void endSearch(String str, String str2, ISearchSession iSearchSession, String str3) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeString(str2);
                obtain.writeStrongBinder(iSearchSession != null ? iSearchSession.asBinder() : null);
                obtain.writeString(str3);
                this.mRemote.transact(12, obtain, obtain2, 0);
                obtain2.readException();
            } catch (RemoteException e) {
                HiLog.error(LABEL, "end search session remote exception, %{public}s", new Object[]{e.getMessage()});
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public void registerIndexChangeListener(String str, String str2, IIndexChangeCallback iIndexChangeCallback, String str3) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeString(str2);
                obtain.writeStrongBinder(iIndexChangeCallback != null ? iIndexChangeCallback.asBinder() : null);
                obtain.writeString(str3);
                this.mRemote.transact(13, obtain, obtain2, 0);
                obtain2.readException();
            } catch (RemoteException e) {
                HiLog.error(LABEL, "register index change listener remote exception, %{public}s", new Object[]{e.getMessage()});
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public void unRegisterIndexChangeListener(String str, String str2, IIndexChangeCallback iIndexChangeCallback, String str3) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeString(str2);
                obtain.writeStrongBinder(iIndexChangeCallback != null ? iIndexChangeCallback.asBinder() : null);
                obtain.writeString(str3);
                this.mRemote.transact(14, obtain, obtain2, 0);
                obtain2.readException();
            } catch (RemoteException e) {
                HiLog.error(LABEL, "unregister index change listener remote exception, %{public}s", new Object[]{e.getMessage()});
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public void registerClientDeathBinder(IBinder iBinder, String str) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeStrongBinder(iBinder);
                obtain.writeString(str);
                this.mRemote.transact(15, obtain, obtain2, 0);
                obtain2.readException();
            } catch (RemoteException e) {
                HiLog.error(LABEL, "register client death binder remote exception, %{public}s", new Object[]{e.getMessage()});
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public void unRegisterClientDeathBinder(IBinder iBinder, String str) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeStrongBinder(iBinder);
                obtain.writeString(str);
                this.mRemote.transact(16, obtain, obtain2, 0);
                obtain2.readException();
            } catch (RemoteException e) {
                HiLog.error(LABEL, "unRegister client death binder remote exception, %{public}s", new Object[]{e.getMessage()});
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public int setSearchableEntity(InnerSearchableEntity innerSearchableEntity, String str) {
            int retCode;
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                if (innerSearchableEntity != null) {
                    obtain.writeInt(1);
                    innerSearchableEntity.writeToParcel(obtain, 0);
                } else {
                    obtain.writeInt(0);
                }
                obtain.writeString(str);
                this.mRemote.transact(17, obtain, obtain2, 0);
                obtain2.readException();
                retCode = obtain2.readInt();
            } catch (RemoteException e) {
                HiLog.error(LABEL, "setSearchableEntity remote exception, %{public}s", new Object[]{e.getMessage()});
                retCode = SearchResult.IPC_EXCEPTION.getRetCode();
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            return retCode;
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public InnerSearchableEntity getSearchableEntity(String str, String str2) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            InnerSearchableEntity innerSearchableEntity = null;
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeString(str2);
                this.mRemote.transact(18, obtain, obtain2, 0);
                obtain2.readException();
                if (obtain2.readInt() != 0) {
                    innerSearchableEntity = InnerSearchableEntity.CREATOR.createFromParcel(obtain2);
                }
            } catch (RemoteException e) {
                HiLog.error(LABEL, "getSearchableEntity remote exception, %{public}s", new Object[]{e.getMessage()});
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            return innerSearchableEntity;
        }

        @Override // ohos.data.searchimpl.connect.IHOSPSearchServiceCall
        public List<InnerSearchableEntity> getSearchableEntityList(String str) {
            ArrayList arrayList;
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(IHOSPSearchServiceCall.DESCRIPTOR);
                obtain.writeString(str);
                this.mRemote.transact(19, obtain, obtain2, 0);
                obtain2.readException();
                arrayList = obtain2.createTypedArrayList(InnerSearchableEntity.CREATOR);
            } catch (RemoteException e) {
                HiLog.error(LABEL, "getSearchableEntityList remote exception, %{public}s", new Object[]{e.getMessage()});
                arrayList = null;
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            return arrayList;
        }
    }
}
