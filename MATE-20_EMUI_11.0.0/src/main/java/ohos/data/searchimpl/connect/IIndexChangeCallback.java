package ohos.data.searchimpl.connect;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import ohos.data.searchimpl.model.InnerChangedIndexContent;

public interface IIndexChangeCallback extends IInterface {
    public static final String DESCRIPTOR = "com.huawei.nb.searchmanager.callback.IIndexChangeCallback";
    public static final int TRANSACTION_onDataChanged = 1;

    void onDataChanged(String str, InnerChangedIndexContent innerChangedIndexContent) throws RemoteException;

    public static abstract class Stub extends Binder implements IIndexChangeCallback {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, IIndexChangeCallback.DESCRIPTOR);
        }

        public static IIndexChangeCallback asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(IIndexChangeCallback.DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IIndexChangeCallback)) {
                return new Proxy(iBinder);
            }
            return (IIndexChangeCallback) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i == 1) {
                parcel.enforceInterface(IIndexChangeCallback.DESCRIPTOR);
                onDataChanged(parcel.readString(), parcel.readInt() != 0 ? InnerChangedIndexContent.CREATOR.createFromParcel(parcel) : null);
                parcel2.writeNoException();
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString(IIndexChangeCallback.DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IIndexChangeCallback {
            private IBinder mRemote;

            private Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // ohos.data.searchimpl.connect.IIndexChangeCallback
            public void onDataChanged(String str, InnerChangedIndexContent innerChangedIndexContent) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IIndexChangeCallback.DESCRIPTOR);
                    obtain.writeString(str);
                    if (innerChangedIndexContent != null) {
                        obtain.writeInt(1);
                        innerChangedIndexContent.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
