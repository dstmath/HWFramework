package android.media.tv;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITvRemoteServiceInput extends IInterface {

    public static abstract class Stub extends Binder implements ITvRemoteServiceInput {
        private static final String DESCRIPTOR = "android.media.tv.ITvRemoteServiceInput";
        static final int TRANSACTION_clearInputBridge = 3;
        static final int TRANSACTION_closeInputBridge = 2;
        static final int TRANSACTION_openInputBridge = 1;
        static final int TRANSACTION_sendKeyDown = 5;
        static final int TRANSACTION_sendKeyUp = 6;
        static final int TRANSACTION_sendPointerDown = 7;
        static final int TRANSACTION_sendPointerSync = 9;
        static final int TRANSACTION_sendPointerUp = 8;
        static final int TRANSACTION_sendTimestamp = 4;

        private static class Proxy implements ITvRemoteServiceInput {
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

            public void openInputBridge(IBinder token, String name, int width, int height, int maxPointers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(name);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    _data.writeInt(maxPointers);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void closeInputBridge(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void clearInputBridge(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendTimestamp(IBinder token, long timestamp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeLong(timestamp);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendKeyDown(IBinder token, int keyCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(keyCode);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendKeyUp(IBinder token, int keyCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(keyCode);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendPointerDown(IBinder token, int pointerId, int x, int y) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(pointerId);
                    _data.writeInt(x);
                    _data.writeInt(y);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendPointerUp(IBinder token, int pointerId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(pointerId);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendPointerSync(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITvRemoteServiceInput asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITvRemoteServiceInput)) {
                return new Proxy(obj);
            }
            return (ITvRemoteServiceInput) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    openInputBridge(data.readStrongBinder(), data.readString(), data.readInt(), data.readInt(), data.readInt());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    closeInputBridge(data.readStrongBinder());
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    clearInputBridge(data.readStrongBinder());
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    sendTimestamp(data.readStrongBinder(), data.readLong());
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    sendKeyDown(data.readStrongBinder(), data.readInt());
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    sendKeyUp(data.readStrongBinder(), data.readInt());
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    sendPointerDown(data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt());
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    sendPointerUp(data.readStrongBinder(), data.readInt());
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    sendPointerSync(data.readStrongBinder());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void clearInputBridge(IBinder iBinder) throws RemoteException;

    void closeInputBridge(IBinder iBinder) throws RemoteException;

    void openInputBridge(IBinder iBinder, String str, int i, int i2, int i3) throws RemoteException;

    void sendKeyDown(IBinder iBinder, int i) throws RemoteException;

    void sendKeyUp(IBinder iBinder, int i) throws RemoteException;

    void sendPointerDown(IBinder iBinder, int i, int i2, int i3) throws RemoteException;

    void sendPointerSync(IBinder iBinder) throws RemoteException;

    void sendPointerUp(IBinder iBinder, int i) throws RemoteException;

    void sendTimestamp(IBinder iBinder, long j) throws RemoteException;
}
