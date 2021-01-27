package ohos.dmsdp.sdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISecureFileListener extends IInterface {

    public static class Default implements ISecureFileListener {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }

        @Override // ohos.dmsdp.sdk.ISecureFileListener
        public long getSecureFileSize(String str) throws RemoteException {
            return 0;
        }

        @Override // ohos.dmsdp.sdk.ISecureFileListener
        public byte[] readSecureFile(String str) throws RemoteException {
            return null;
        }

        @Override // ohos.dmsdp.sdk.ISecureFileListener
        public boolean writeSecureFile(String str, byte[] bArr) throws RemoteException {
            return false;
        }
    }

    long getSecureFileSize(String str) throws RemoteException;

    byte[] readSecureFile(String str) throws RemoteException;

    boolean writeSecureFile(String str, byte[] bArr) throws RemoteException;

    public static abstract class Stub extends Binder implements ISecureFileListener {
        private static final String DESCRIPTOR = "com.huawei.dmsdpsdk.ISecureFileListener";
        static final int TRANSACTION_getSecureFileSize = 1;
        static final int TRANSACTION_readSecureFile = 2;
        static final int TRANSACTION_writeSecureFile = 3;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISecureFileListener asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof ISecureFileListener)) {
                return new Proxy(iBinder);
            }
            return (ISecureFileListener) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i == 1) {
                parcel.enforceInterface(DESCRIPTOR);
                long secureFileSize = getSecureFileSize(parcel.readString());
                parcel2.writeNoException();
                parcel2.writeLong(secureFileSize);
                return true;
            } else if (i == 2) {
                parcel.enforceInterface(DESCRIPTOR);
                byte[] readSecureFile = readSecureFile(parcel.readString());
                parcel2.writeNoException();
                parcel2.writeByteArray(readSecureFile);
                return true;
            } else if (i == 3) {
                parcel.enforceInterface(DESCRIPTOR);
                boolean writeSecureFile = writeSecureFile(parcel.readString(), parcel.createByteArray());
                parcel2.writeNoException();
                parcel2.writeInt(writeSecureFile ? 1 : 0);
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ISecureFileListener {
            public static ISecureFileListener sDefaultImpl;
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

            @Override // ohos.dmsdp.sdk.ISecureFileListener
            public long getSecureFileSize(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (!this.mRemote.transact(1, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSecureFileSize(str);
                    }
                    obtain2.readException();
                    long readLong = obtain2.readLong();
                    obtain2.recycle();
                    obtain.recycle();
                    return readLong;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.ISecureFileListener
            public byte[] readSecureFile(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    if (!this.mRemote.transact(2, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().readSecureFile(str);
                    }
                    obtain2.readException();
                    byte[] createByteArray = obtain2.createByteArray();
                    obtain2.recycle();
                    obtain.recycle();
                    return createByteArray;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.ISecureFileListener
            public boolean writeSecureFile(String str, byte[] bArr) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeByteArray(bArr);
                    boolean z = false;
                    if (!this.mRemote.transact(3, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().writeSecureFile(str, bArr);
                    }
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISecureFileListener iSecureFileListener) {
            if (Proxy.sDefaultImpl != null || iSecureFileListener == null) {
                return false;
            }
            Proxy.sDefaultImpl = iSecureFileListener;
            return true;
        }

        public static ISecureFileListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
