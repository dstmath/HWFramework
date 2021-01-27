package com.huawei.nb.searchmanager.callback;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SharedMemory;
import com.huawei.nb.searchmanager.client.model.ChangedIndexContent;

public interface IIndexChangeCallback extends IInterface {
    void onDataChanged(String str, ChangedIndexContent changedIndexContent) throws RemoteException;

    void onDataChangedLarge(String str, SharedMemory sharedMemory, int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IIndexChangeCallback {
        private static final String DESCRIPTOR = "com.huawei.nb.searchmanager.callback.IIndexChangeCallback";
        static final int TRANSACTION_onDataChanged = 1;
        static final int TRANSACTION_onDataChangedLarge = 2;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIndexChangeCallback asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IIndexChangeCallback)) {
                return new Proxy(iBinder);
            }
            return (IIndexChangeCallback) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            ChangedIndexContent changedIndexContent = null;
            SharedMemory sharedMemory = null;
            if (i == 1) {
                parcel.enforceInterface(DESCRIPTOR);
                String readString = parcel.readString();
                if (parcel.readInt() != 0) {
                    changedIndexContent = ChangedIndexContent.CREATOR.createFromParcel(parcel);
                }
                onDataChanged(readString, changedIndexContent);
                return true;
            } else if (i == 2) {
                parcel.enforceInterface(DESCRIPTOR);
                String readString2 = parcel.readString();
                if (parcel.readInt() != 0) {
                    sharedMemory = (SharedMemory) SharedMemory.CREATOR.createFromParcel(parcel);
                }
                onDataChangedLarge(readString2, sharedMemory, parcel.readInt());
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IIndexChangeCallback {
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

            @Override // com.huawei.nb.searchmanager.callback.IIndexChangeCallback
            public void onDataChanged(String str, ChangedIndexContent changedIndexContent) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (changedIndexContent != null) {
                        obtain.writeInt(1);
                        changedIndexContent.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(1, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.callback.IIndexChangeCallback
            public void onDataChangedLarge(String str, SharedMemory sharedMemory, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (sharedMemory != null) {
                        obtain.writeInt(1);
                        sharedMemory.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i);
                    this.mRemote.transact(2, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }
    }
}
