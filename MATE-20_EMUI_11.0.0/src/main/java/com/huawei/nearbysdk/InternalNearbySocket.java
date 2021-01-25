package com.huawei.nearbysdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface InternalNearbySocket extends IInterface {
    boolean close() throws RemoteException;

    int getBusinessId() throws RemoteException;

    int getBusinessType() throws RemoteException;

    int getChannelId() throws RemoteException;

    String getIpAddress() throws RemoteException;

    String getLocalIpAddress() throws RemoteException;

    int getPort() throws RemoteException;

    byte getProtocol() throws RemoteException;

    NearbyDevice getRemoteNearbyDevice() throws RemoteException;

    int getSecurityType() throws RemoteException;

    String getServiceUuid() throws RemoteException;

    String getTag() throws RemoteException;

    public static abstract class Stub extends Binder implements InternalNearbySocket {
        private static final String DESCRIPTOR = "com.huawei.nearbysdk.InternalNearbySocket";
        static final int TRANSACTION_close = 12;
        static final int TRANSACTION_getBusinessId = 1;
        static final int TRANSACTION_getBusinessType = 2;
        static final int TRANSACTION_getChannelId = 3;
        static final int TRANSACTION_getIpAddress = 7;
        static final int TRANSACTION_getLocalIpAddress = 6;
        static final int TRANSACTION_getPort = 10;
        static final int TRANSACTION_getProtocol = 8;
        static final int TRANSACTION_getRemoteNearbyDevice = 11;
        static final int TRANSACTION_getSecurityType = 4;
        static final int TRANSACTION_getServiceUuid = 9;
        static final int TRANSACTION_getTag = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static InternalNearbySocket asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof InternalNearbySocket)) {
                return new Proxy(obj);
            }
            return (InternalNearbySocket) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getBusinessId();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getBusinessType();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getChannelId();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getSecurityType();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _result5 = getTag();
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _result6 = getLocalIpAddress();
                        reply.writeNoException();
                        reply.writeString(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        String _result7 = getIpAddress();
                        reply.writeNoException();
                        reply.writeString(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        byte _result8 = getProtocol();
                        reply.writeNoException();
                        reply.writeByte(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _result9 = getServiceUuid();
                        reply.writeNoException();
                        reply.writeString(_result9);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = getPort();
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        NearbyDevice _result11 = getRemoteNearbyDevice();
                        reply.writeNoException();
                        if (_result11 != null) {
                            reply.writeInt(1);
                            _result11.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean close = close();
                        reply.writeNoException();
                        reply.writeInt(close ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements InternalNearbySocket {
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

            @Override // com.huawei.nearbysdk.InternalNearbySocket
            public int getBusinessId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.InternalNearbySocket
            public int getBusinessType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.InternalNearbySocket
            public int getChannelId() throws RemoteException {
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

            @Override // com.huawei.nearbysdk.InternalNearbySocket
            public int getSecurityType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.InternalNearbySocket
            public String getTag() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.InternalNearbySocket
            public String getLocalIpAddress() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.InternalNearbySocket
            public String getIpAddress() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.InternalNearbySocket
            public byte getProtocol() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readByte();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.InternalNearbySocket
            public String getServiceUuid() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.InternalNearbySocket
            public int getPort() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.InternalNearbySocket
            public NearbyDevice getRemoteNearbyDevice() throws RemoteException {
                NearbyDevice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NearbyDevice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.InternalNearbySocket
            public boolean close() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(12, _data, _reply, 0);
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
        }
    }
}
