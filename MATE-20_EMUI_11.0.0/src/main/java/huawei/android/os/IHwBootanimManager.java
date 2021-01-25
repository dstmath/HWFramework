package huawei.android.os;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwBootanimManager extends IInterface {
    int getBootAnimSoundSwitch() throws RemoteException;

    boolean isBootOrShutdownSoundCapable() throws RemoteException;

    void switchBootOrShutSound(String str) throws RemoteException;

    public static abstract class Stub extends Binder implements IHwBootanimManager {
        private static final String DESCRIPTOR = "huawei.android.os.IHwBootanimManager";
        static final int TRANSACTION_getBootAnimSoundSwitch = 3;
        static final int TRANSACTION_isBootOrShutdownSoundCapable = 2;
        static final int TRANSACTION_switchBootOrShutSound = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwBootanimManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwBootanimManager)) {
                return new Proxy(obj);
            }
            return (IHwBootanimManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                switchBootOrShutSound(data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean isBootOrShutdownSoundCapable = isBootOrShutdownSoundCapable();
                reply.writeNoException();
                reply.writeInt(isBootOrShutdownSoundCapable ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result = getBootAnimSoundSwitch();
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwBootanimManager {
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

            @Override // huawei.android.os.IHwBootanimManager
            public void switchBootOrShutSound(String openOrClose) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(openOrClose);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.os.IHwBootanimManager
            public boolean isBootOrShutdownSoundCapable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.os.IHwBootanimManager
            public int getBootAnimSoundSwitch() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
