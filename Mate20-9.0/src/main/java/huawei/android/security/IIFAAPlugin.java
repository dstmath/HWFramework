package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import huawei.android.security.IIFAAPluginCallBack;

public interface IIFAAPlugin extends IInterface {

    public static abstract class Stub extends Binder implements IIFAAPlugin {
        private static final String DESCRIPTOR = "huawei.android.security.IIFAAPlugin";
        static final int TRANSACTION_processCmd = 1;

        private static class Proxy implements IIFAAPlugin {
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

            public void processCmd(IIFAAPluginCallBack callBack, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callBack != null ? callBack.asBinder() : null);
                    _data.writeByteArray(data);
                    this.mRemote.transact(1, _data, _reply, 0);
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

        public static IIFAAPlugin asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIFAAPlugin)) {
                return new Proxy(obj);
            }
            return (IIFAAPlugin) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                processCmd(IIFAAPluginCallBack.Stub.asInterface(data.readStrongBinder()), data.createByteArray());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void processCmd(IIFAAPluginCallBack iIFAAPluginCallBack, byte[] bArr) throws RemoteException;
}
