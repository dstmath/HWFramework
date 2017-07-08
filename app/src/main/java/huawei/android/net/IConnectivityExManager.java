package huawei.android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IConnectivityExManager extends IInterface {

    public static abstract class Stub extends Binder implements IConnectivityExManager {
        private static final String DESCRIPTOR = "huawei.android.net.IConnectivityExManager";
        static final int TRANSACTION_setSmartKeyguardLevel = 1;
        static final int TRANSACTION_setUseCtrlSocket = 2;

        private static class Proxy implements IConnectivityExManager {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void setSmartKeyguardLevel(String level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(level);
                    this.mRemote.transact(Stub.TRANSACTION_setSmartKeyguardLevel, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUseCtrlSocket(boolean flag) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (flag) {
                        i = Stub.TRANSACTION_setSmartKeyguardLevel;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setUseCtrlSocket, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IConnectivityExManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IConnectivityExManager)) {
                return new Proxy(obj);
            }
            return (IConnectivityExManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0 = false;
            switch (code) {
                case TRANSACTION_setSmartKeyguardLevel /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    setSmartKeyguardLevel(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setUseCtrlSocket /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    setUseCtrlSocket(_arg0);
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void setSmartKeyguardLevel(String str) throws RemoteException;

    void setUseCtrlSocket(boolean z) throws RemoteException;
}
