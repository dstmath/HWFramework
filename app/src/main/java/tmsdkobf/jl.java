package tmsdkobf;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import tmsdk.common.DataEntity;

/* compiled from: Unknown */
public interface jl extends IInterface {

    /* compiled from: Unknown */
    public static abstract class b extends Binder implements jl {
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

    /* compiled from: Unknown */
    public static class a implements jl {
        private IBinder mRemote;
        private int tY;

        a(IBinder iBinder) {
            this.mRemote = iBinder;
            this.tY = Binder.getCallingUid();
        }

        public static jl a(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.tencent.tmsecure.common.ISDKClient");
            return (queryLocalInterface != null && (queryLocalInterface instanceof jl)) ? (jl) queryLocalInterface : new a(iBinder);
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
            if (this.mRemote == aVar.mRemote && this.tY == aVar.tY) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return this.tY;
        }

        public DataEntity sendMessage(DataEntity dataEntity) throws RemoteException {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            obtain.writeInterfaceToken("com.tencent.tmsecure.common.ISDKClient");
            obtain.writeParcelable(dataEntity, 0);
            try {
                this.mRemote.transact(20100405, obtain, obtain2, 0);
                obtain2.readException();
                DataEntity dataEntity2 = (DataEntity) obtain2.readParcelable(DataEntity.class.getClassLoader());
                return dataEntity2;
            } finally {
                obtain.recycle();
                obtain2.recycle();
            }
        }
    }

    DataEntity sendMessage(DataEntity dataEntity) throws RemoteException;
}
