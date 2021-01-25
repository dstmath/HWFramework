package com.huawei.nb.searchmanager.client;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SharedMemory;
import com.huawei.nb.searchmanager.client.model.IndexData;
import com.huawei.nb.searchmanager.client.model.Recommendation;
import com.huawei.nb.searchmanager.client.model.Token;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface ISearchSession extends IInterface {
    Map coverSearch(String str, List<String> list, int i) throws RemoteException;

    int getSearchHitCount(String str) throws RemoteException;

    List<String> getTopFieldValues(String str, int i) throws RemoteException;

    List<Recommendation> groupSearch(String str, int i) throws RemoteException;

    List<Recommendation> groupTimeline(String str, String str2, Token token) throws RemoteException;

    List<IndexData> search(String str, int i, int i2) throws RemoteException;

    List<IndexData> searchLarge(String str, int i, int i2, SharedMemory sharedMemory) throws RemoteException;

    public static abstract class Stub extends Binder implements ISearchSession {
        private static final String DESCRIPTOR = "com.huawei.nb.searchmanager.client.ISearchSession";
        static final int TRANSACTION_coverSearch = 6;
        static final int TRANSACTION_getSearchHitCount = 2;
        static final int TRANSACTION_getTopFieldValues = 1;
        static final int TRANSACTION_groupSearch = 4;
        static final int TRANSACTION_groupTimeline = 5;
        static final int TRANSACTION_search = 3;
        static final int TRANSACTION_searchLarge = 7;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISearchSession asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof ISearchSession)) {
                return new Proxy(iBinder);
            }
            return (ISearchSession) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i != 1598968902) {
                Token token = null;
                SharedMemory sharedMemory = null;
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
                        String readString = parcel.readString();
                        String readString2 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            token = Token.CREATOR.createFromParcel(parcel);
                        }
                        List<Recommendation> groupTimeline = groupTimeline(readString, readString2, token);
                        parcel2.writeNoException();
                        parcel2.writeTypedList(groupTimeline);
                        if (token != null) {
                            parcel2.writeInt(1);
                            token.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        Map coverSearch = coverSearch(parcel.readString(), parcel.createStringArrayList(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeMap(coverSearch);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        String readString3 = parcel.readString();
                        int readInt = parcel.readInt();
                        int readInt2 = parcel.readInt();
                        if (parcel.readInt() != 0) {
                            sharedMemory = (SharedMemory) SharedMemory.CREATOR.createFromParcel(parcel);
                        }
                        List<IndexData> searchLarge = searchLarge(readString3, readInt, readInt2, sharedMemory);
                        parcel2.writeNoException();
                        parcel2.writeTypedList(searchLarge);
                        return true;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements ISearchSession {
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

            @Override // com.huawei.nb.searchmanager.client.ISearchSession
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

            @Override // com.huawei.nb.searchmanager.client.ISearchSession
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

            @Override // com.huawei.nb.searchmanager.client.ISearchSession
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

            @Override // com.huawei.nb.searchmanager.client.ISearchSession
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

            @Override // com.huawei.nb.searchmanager.client.ISearchSession
            public List<Recommendation> groupTimeline(String str, String str2, Token token) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    if (token != null) {
                        obtain.writeInt(1);
                        token.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                    ArrayList createTypedArrayList = obtain2.createTypedArrayList(Recommendation.CREATOR);
                    if (obtain2.readInt() != 0) {
                        token.readFromParcel(obtain2);
                    }
                    return createTypedArrayList;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchSession
            public Map coverSearch(String str, List<String> list, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeStringList(list);
                    obtain.writeInt(i);
                    this.mRemote.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readHashMap(getClass().getClassLoader());
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.client.ISearchSession
            public List<IndexData> searchLarge(String str, int i, int i2, SharedMemory sharedMemory) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
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
                    return obtain2.createTypedArrayList(IndexData.CREATOR);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
