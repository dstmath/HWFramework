package ohos.data.searchimpl.connect;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SharedMemory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ohos.data.searchimpl.model.InnerIndexData;
import ohos.data.searchimpl.model.InnerRecommendation;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public interface ISearchSession extends IInterface {
    public static final String DESCRIPTOR = "com.huawei.nb.searchmanager.client.ISearchSession";
    public static final int TRANSACTION_coverSearch = 6;
    public static final int TRANSACTION_getSearchHitCount = 2;
    public static final int TRANSACTION_getTopFieldValues = 1;
    public static final int TRANSACTION_groupSearch = 4;
    public static final int TRANSACTION_groupTimeline = 5;
    public static final int TRANSACTION_search = 3;
    public static final int TRANSACTION_searchLarge = 7;

    int getSearchHitCount(String str);

    List<InnerRecommendation> groupSearch(String str, int i);

    List<InnerIndexData> search(String str, int i, int i2);

    List<InnerIndexData> searchLarge(String str, int i, int i2, SharedMemory sharedMemory);

    public static class Proxy implements ISearchSession {
        private static final HiLogLabel LABEL = new HiLogLabel(3, 218109504, "ISearchSession");
        private IBinder mRemote;

        Proxy(IBinder iBinder) {
            this.mRemote = iBinder;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this.mRemote;
        }

        @Override // ohos.data.searchimpl.connect.ISearchSession
        public int getSearchHitCount(String str) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            int i = 0;
            try {
                obtain.writeInterfaceToken(ISearchSession.DESCRIPTOR);
                obtain.writeString(str);
                this.mRemote.transact(2, obtain, obtain2, 0);
                obtain2.readException();
                i = obtain2.readInt();
            } catch (RemoteException e) {
                HiLog.error(LABEL, "getSearchHitCount remote exception, %{public}s", new Object[]{e.getMessage()});
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            return i;
        }

        @Override // ohos.data.searchimpl.connect.ISearchSession
        public List<InnerIndexData> search(String str, int i, int i2) {
            ArrayList arrayList;
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(ISearchSession.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeInt(i);
                obtain.writeInt(i2);
                this.mRemote.transact(3, obtain, obtain2, 0);
                obtain2.readException();
                arrayList = obtain2.createTypedArrayList(InnerIndexData.CREATOR);
            } catch (RemoteException e) {
                HiLog.error(LABEL, "search remote exception, %{public}s", new Object[]{e.getMessage()});
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

        @Override // ohos.data.searchimpl.connect.ISearchSession
        public List<InnerRecommendation> groupSearch(String str, int i) {
            ArrayList arrayList;
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(ISearchSession.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeInt(i);
                this.mRemote.transact(4, obtain, obtain2, 0);
                obtain2.readException();
                arrayList = obtain2.createTypedArrayList(InnerRecommendation.CREATOR);
            } catch (RemoteException e) {
                HiLog.error(LABEL, "groupSearch remote exception, %{public}s", new Object[]{e.getMessage()});
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

        @Override // ohos.data.searchimpl.connect.ISearchSession
        public List<InnerIndexData> searchLarge(String str, int i, int i2, SharedMemory sharedMemory) {
            List<InnerIndexData> list;
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken(ISearchSession.DESCRIPTOR);
                obtain.writeString(str);
                obtain.writeInt(i);
                obtain.writeInt(i2);
                if (sharedMemory != null) {
                    obtain.writeInt(1);
                    sharedMemory.writeToParcel(obtain, 0);
                } else {
                    obtain.writeInt(0);
                }
                this.mRemote.transact(7, obtain, obtain2, 0);
                obtain2.readException();
                list = obtain2.createTypedArrayList(InnerIndexData.CREATOR);
            } catch (RemoteException e) {
                HiLog.error(LABEL, "search large remote exception, %{public}s", new Object[]{e.getMessage()});
                list = Collections.emptyList();
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
            return list;
        }
    }
}
