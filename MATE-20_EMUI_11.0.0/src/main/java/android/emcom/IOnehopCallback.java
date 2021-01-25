package android.emcom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IOnehopCallback extends IInterface {
    String onOnehopCommonCallback(String str) throws RemoteException;

    void onOnehopDataReceived(String str, int i, byte[] bArr, int i2, String str2) throws RemoteException;

    void onOnehopDeviceConnectStateChanged(String str) throws RemoteException;

    void onOnehopDeviceListChanged(List<OnehopDeviceInfo> list) throws RemoteException;

    void onOnehopSendStateUpdated(String str) throws RemoteException;

    public static class Default implements IOnehopCallback {
        @Override // android.emcom.IOnehopCallback
        public void onOnehopDeviceListChanged(List<OnehopDeviceInfo> list) throws RemoteException {
        }

        @Override // android.emcom.IOnehopCallback
        public void onOnehopDataReceived(String deviceId, int type, byte[] data, int len, String extInfo) throws RemoteException {
        }

        @Override // android.emcom.IOnehopCallback
        public void onOnehopSendStateUpdated(String para) throws RemoteException {
        }

        @Override // android.emcom.IOnehopCallback
        public void onOnehopDeviceConnectStateChanged(String para) throws RemoteException {
        }

        @Override // android.emcom.IOnehopCallback
        public String onOnehopCommonCallback(String para) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOnehopCallback {
        private static final String DESCRIPTOR = "android.emcom.IOnehopCallback";
        static final int TRANSACTION_onOnehopCommonCallback = 5;
        static final int TRANSACTION_onOnehopDataReceived = 2;
        static final int TRANSACTION_onOnehopDeviceConnectStateChanged = 4;
        static final int TRANSACTION_onOnehopDeviceListChanged = 1;
        static final int TRANSACTION_onOnehopSendStateUpdated = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOnehopCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOnehopCallback)) {
                return new Proxy(obj);
            }
            return (IOnehopCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onOnehopDeviceListChanged(data.createTypedArrayList(OnehopDeviceInfo.CREATOR));
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onOnehopDataReceived(data.readString(), data.readInt(), data.createByteArray(), data.readInt(), data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onOnehopSendStateUpdated(data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onOnehopDeviceConnectStateChanged(data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                String _result = onOnehopCommonCallback(data.readString());
                reply.writeNoException();
                reply.writeString(_result);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOnehopCallback {
            public static IOnehopCallback sDefaultImpl;
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

            @Override // android.emcom.IOnehopCallback
            public void onOnehopDeviceListChanged(List<OnehopDeviceInfo> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(list);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onOnehopDeviceListChanged(list);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IOnehopCallback
            public void onOnehopDataReceived(String deviceId, int type, byte[] data, int len, String extInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeInt(type);
                    _data.writeByteArray(data);
                    _data.writeInt(len);
                    _data.writeString(extInfo);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onOnehopDataReceived(deviceId, type, data, len, extInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IOnehopCallback
            public void onOnehopSendStateUpdated(String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(para);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onOnehopSendStateUpdated(para);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IOnehopCallback
            public void onOnehopDeviceConnectStateChanged(String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(para);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onOnehopDeviceConnectStateChanged(para);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IOnehopCallback
            public String onOnehopCommonCallback(String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(para);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onOnehopCommonCallback(para);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOnehopCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOnehopCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
