package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwSecurityDiagnoseCallback extends IInterface {
    void onRootStatus(int i) throws RemoteException;

    public static class Default implements IHwSecurityDiagnoseCallback {
        @Override // huawei.android.security.IHwSecurityDiagnoseCallback
        public void onRootStatus(int rootStatus) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwSecurityDiagnoseCallback {
        private static final String DESCRIPTOR = "huawei.android.security.IHwSecurityDiagnoseCallback";
        static final int TRANSACTION_onRootStatus = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwSecurityDiagnoseCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwSecurityDiagnoseCallback)) {
                return new Proxy(obj);
            }
            return (IHwSecurityDiagnoseCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onRootStatus(data.readInt());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwSecurityDiagnoseCallback {
            public static IHwSecurityDiagnoseCallback sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // huawei.android.security.IHwSecurityDiagnoseCallback
            public void onRootStatus(int rootStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rootStatus);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onRootStatus(rootStatus);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwSecurityDiagnoseCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwSecurityDiagnoseCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
