package com.huawei.nb.searchmanager.distribute;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.nb.searchmanager.client.model.IndexData;
import com.huawei.nb.searchmanager.client.model.Recommendation;
import java.util.List;

public interface IRemoteSearchCallback extends IInterface {
    boolean beginRemoteSearch(DeviceInfo deviceInfo, String str) throws RemoteException;

    boolean endRemoteSearch(DeviceInfo deviceInfo, String str) throws RemoteException;

    int getSearchHitCount(String str) throws RemoteException;

    List<String> getTopFieldValues(String str, int i) throws RemoteException;

    List<Recommendation> groupSearch(String str, int i) throws RemoteException;

    List<IndexData> search(String str, int i, int i2) throws RemoteException;

    public static abstract class Stub extends Binder implements IRemoteSearchCallback {
        private static final String DESCRIPTOR = "com.huawei.nb.searchmanager.distribute.IRemoteSearchCallback";
        static final int TRANSACTION_beginRemoteSearch = 5;
        static final int TRANSACTION_endRemoteSearch = 6;
        static final int TRANSACTION_getSearchHitCount = 2;
        static final int TRANSACTION_getTopFieldValues = 1;
        static final int TRANSACTION_groupSearch = 4;
        static final int TRANSACTION_search = 3;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRemoteSearchCallback asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IRemoteSearchCallback)) {
                return new Proxy(iBinder);
            }
            return (IRemoteSearchCallback) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i != 1598968902) {
                DeviceInfo deviceInfo = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> topFieldValues = getTopFieldValues(parcel.readString(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeStringList(topFieldValues);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        int searchHitCount = getSearchHitCount(parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(searchHitCount);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<IndexData> search = search(parcel.readString(), parcel.readInt(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeTypedList(search);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<Recommendation> groupSearch = groupSearch(parcel.readString(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeTypedList(groupSearch);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            deviceInfo = DeviceInfo.CREATOR.createFromParcel(parcel);
                        }
                        boolean beginRemoteSearch = beginRemoteSearch(deviceInfo, parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(beginRemoteSearch ? 1 : 0);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            deviceInfo = DeviceInfo.CREATOR.createFromParcel(parcel);
                        }
                        boolean endRemoteSearch = endRemoteSearch(deviceInfo, parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(endRemoteSearch ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IRemoteSearchCallback {
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

            @Override // com.huawei.nb.searchmanager.distribute.IRemoteSearchCallback
            public List<String> getTopFieldValues(String str, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createStringArrayList();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.distribute.IRemoteSearchCallback
            public int getSearchHitCount(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.distribute.IRemoteSearchCallback
            public List<IndexData> search(String str, int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createTypedArrayList(IndexData.CREATOR);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.distribute.IRemoteSearchCallback
            public List<Recommendation> groupSearch(String str, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    this.mRemote.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createTypedArrayList(Recommendation.CREATOR);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.distribute.IRemoteSearchCallback
            public boolean beginRemoteSearch(DeviceInfo deviceInfo, String str) throws RemoteException {
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
                    this.mRemote.transact(5, obtain, obtain2, 0);
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

            @Override // com.huawei.nb.searchmanager.distribute.IRemoteSearchCallback
            public boolean endRemoteSearch(DeviceInfo deviceInfo, String str) throws RemoteException {
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
                    this.mRemote.transact(6, obtain, obtain2, 0);
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
        }
    }
}
