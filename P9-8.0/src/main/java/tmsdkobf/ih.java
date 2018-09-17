package tmsdkobf;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import tmsdk.common.DataEntity;

public interface ih extends IInterface {

    public static abstract class b extends Binder implements ih {
        public b() {
            attachInterface(this, "com.tencent.tmsecure.common.ISDKClient");
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean equals(Object obj) {
            return !(obj instanceof b) ? false : super.equals((b) obj);
        }

        public String getInterfaceDescriptor() {
            return "com.tencent.tmsecure.common.ISDKClient";
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i != 20100405) {
                return super.onTransact(i, parcel, parcel2, i2);
            }
            parcel.enforceInterface("com.tencent.tmsecure.common.ISDKClient");
            Parcelable sendMessage = sendMessage((DataEntity) parcel.readParcelable(DataEntity.class.getClassLoader()));
            parcel2.writeNoException();
            parcel2.writeParcelable(sendMessage, 0);
            return true;
        }
    }

    public static class a implements ih {
        private IBinder mRemote;
        private int rx = Binder.getCallingUid();

        a(IBinder iBinder) {
            this.mRemote = iBinder;
        }

        public static ih a(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.tencent.tmsecure.common.ISDKClient");
            return (queryLocalInterface != null && (queryLocalInterface instanceof ih)) ? (ih) queryLocalInterface : new a(iBinder);
        }

        public IBinder asBinder() {
            return this.mRemote;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            if (this.mRemote == aVar.mRemote && this.rx == aVar.rx) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return this.rx;
        }

        public DataEntity sendMessage(DataEntity dataEntity) throws RemoteException {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            obtain.writeInterfaceToken("com.tencent.tmsecure.common.ISDKClient");
            obtain.writeParcelable(dataEntity, 0);
            DataEntity dataEntity2 = null;
            try {
                this.mRemote.transact(20100405, obtain, obtain2, 0);
                obtain2.readException();
                dataEntity2 = (DataEntity) obtain2.readParcelable(DataEntity.class.getClassLoader());
                return dataEntity2;
            } finally {
                obtain.recycle();
                obtain2.recycle();
            }
        }
    }

    DataEntity sendMessage(DataEntity dataEntity) throws RemoteException;
}
