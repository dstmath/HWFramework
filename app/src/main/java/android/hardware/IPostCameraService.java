package android.hardware;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPostCameraService extends IInterface {

    public static abstract class Stub extends Binder implements IPostCameraService {
        private static final String DESCRIPTOR = "android.hardware.IPostCameraService";
        static final int TRANSACTION_addListener = 2;
        static final int TRANSACTION_connect = 1;
        static final int TRANSACTION_removeListener = 3;

        private static class Proxy implements IPostCameraService {
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

            public int connect(IPostCameraClient client, String clientPackageName, int clientUid, IPostCamera device) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IBinder asBinder;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        asBinder = client.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    _data.writeString(clientPackageName);
                    _data.writeInt(clientUid);
                    if (device != null) {
                        iBinder = device.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_connect, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addListener(ICameraServiceListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addListener, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int removeListener(ICameraServiceListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeListener, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPostCameraService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPostCameraService)) {
                return new Proxy(obj);
            }
            return (IPostCameraService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case TRANSACTION_connect /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = connect(android.hardware.IPostCameraClient.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt(), android.hardware.IPostCamera.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_addListener /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = addListener(android.hardware.ICameraServiceListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_removeListener /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = removeListener(android.hardware.ICameraServiceListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int addListener(ICameraServiceListener iCameraServiceListener) throws RemoteException;

    int connect(IPostCameraClient iPostCameraClient, String str, int i, IPostCamera iPostCamera) throws RemoteException;

    int removeListener(ICameraServiceListener iCameraServiceListener) throws RemoteException;
}
