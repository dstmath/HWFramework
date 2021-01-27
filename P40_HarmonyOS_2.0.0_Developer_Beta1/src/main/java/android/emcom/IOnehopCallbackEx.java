package android.emcom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOnehopCallbackEx extends IInterface {
    String onOnehopCommonCallback(String str) throws RemoteException;

    int onOnehopDataReceived(String str, int i, byte[] bArr, int i2, String str2) throws RemoteException;

    int onOnehopDeviceConnectStateChanged(String str) throws RemoteException;

    int onOnehopDeviceListChanged(OnehopDeviceInfo[] onehopDeviceInfoArr) throws RemoteException;

    int onOnehopSendStateUpdated(String str) throws RemoteException;

    public static class Default implements IOnehopCallbackEx {
        @Override // android.emcom.IOnehopCallbackEx
        public int onOnehopDeviceListChanged(OnehopDeviceInfo[] list) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IOnehopCallbackEx
        public int onOnehopDataReceived(String deviceId, int type, byte[] data, int len, String extInfo) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IOnehopCallbackEx
        public int onOnehopSendStateUpdated(String para) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IOnehopCallbackEx
        public int onOnehopDeviceConnectStateChanged(String para) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IOnehopCallbackEx
        public String onOnehopCommonCallback(String para) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOnehopCallbackEx {
        private static final String DESCRIPTOR = "android.emcom.IOnehopCallbackEx";
        static final int TRANSACTION_onOnehopCommonCallback = 5;
        static final int TRANSACTION_onOnehopDataReceived = 2;
        static final int TRANSACTION_onOnehopDeviceConnectStateChanged = 4;
        static final int TRANSACTION_onOnehopDeviceListChanged = 1;
        static final int TRANSACTION_onOnehopSendStateUpdated = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOnehopCallbackEx asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOnehopCallbackEx)) {
                return new Proxy(obj);
            }
            return (IOnehopCallbackEx) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = onOnehopDeviceListChanged((OnehopDeviceInfo[]) data.createTypedArray(OnehopDeviceInfo.CREATOR));
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = onOnehopDataReceived(data.readString(), data.readInt(), data.createByteArray(), data.readInt(), data.readString());
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result3 = onOnehopSendStateUpdated(data.readString());
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                int _result4 = onOnehopDeviceConnectStateChanged(data.readString());
                reply.writeNoException();
                reply.writeInt(_result4);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                String _result5 = onOnehopCommonCallback(data.readString());
                reply.writeNoException();
                reply.writeString(_result5);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOnehopCallbackEx {
            public static IOnehopCallbackEx sDefaultImpl;
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

            @Override // android.emcom.IOnehopCallbackEx
            public int onOnehopDeviceListChanged(OnehopDeviceInfo[] list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(list, 0);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onOnehopDeviceListChanged(list);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IOnehopCallbackEx
            public int onOnehopDataReceived(String deviceId, int type, byte[] data, int len, String extInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeInt(type);
                    _data.writeByteArray(data);
                    _data.writeInt(len);
                    _data.writeString(extInfo);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onOnehopDataReceived(deviceId, type, data, len, extInfo);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IOnehopCallbackEx
            public int onOnehopSendStateUpdated(String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(para);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onOnehopSendStateUpdated(para);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IOnehopCallbackEx
            public int onOnehopDeviceConnectStateChanged(String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(para);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onOnehopDeviceConnectStateChanged(para);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IOnehopCallbackEx
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

        public static boolean setDefaultImpl(IOnehopCallbackEx impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOnehopCallbackEx getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
