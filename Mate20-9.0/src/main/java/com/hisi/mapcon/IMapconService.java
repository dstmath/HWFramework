package com.hisi.mapcon;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.hisi.mapcon.IMapconServiceCallback;

public interface IMapconService extends IInterface {

    public static abstract class Stub extends Binder implements IMapconService {
        private static final String DESCRIPTOR = "com.hisi.mapcon.IMapconService";
        static final int TRANSACTION_getUtOverWifiApn = 15;
        static final int TRANSACTION_getVoWifiServiceDomain = 12;
        static final int TRANSACTION_getVoWifiServiceState = 10;
        static final int TRANSACTION_notifyImsOff = 6;
        static final int TRANSACTION_notifyImsOn = 5;
        static final int TRANSACTION_notifyRoaming = 16;
        static final int TRANSACTION_notifyWifiOff = 4;
        static final int TRANSACTION_sendMessage = 7;
        static final int TRANSACTION_setDomain = 3;
        static final int TRANSACTION_setVoWifiOff = 2;
        static final int TRANSACTION_setVoWifiOn = 1;
        static final int TRANSACTION_setVoWifiServiceDomain = 11;
        static final int TRANSACTION_setVoWifiServiceOff = 9;
        static final int TRANSACTION_setVoWifiServiceOn = 8;
        static final int TRANSACTION_setupTunnelOverWifi = 13;
        static final int TRANSACTION_teardownTunnelOverWifi = 14;

        private static class Proxy implements IMapconService {
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

            public int setVoWifiOn(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setVoWifiOff(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setDomain(int phoneId, int domain) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(domain);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyWifiOff(IMapconServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int notifyImsOn(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int notifyImsOff(int phoneId, int currentDomain) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(currentDomain);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendMessage(int msgId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(msgId);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setVoWifiServiceOn(int phoneId, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(type);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setVoWifiServiceOff(int phoneId, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(type);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVoWifiServiceState(int phoneId, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(type);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setVoWifiServiceDomain(int phoneId, int type, int domain) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(type);
                    _data.writeInt(domain);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVoWifiServiceDomain(int phoneId, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(type);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setupTunnelOverWifi(int phoneId, int serviceType, String apn, ServerAddr serverAddr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(serviceType);
                    _data.writeString(apn);
                    if (serverAddr != null) {
                        _data.writeInt(1);
                        serverAddr.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int teardownTunnelOverWifi(int phoneId, int type, String apn, ServerAddr serverAddr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(type);
                    _data.writeString(apn);
                    if (serverAddr != null) {
                        _data.writeInt(1);
                        serverAddr.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getUtOverWifiApn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyRoaming(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    this.mRemote.transact(16, _data, _reply, 0);
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

        public static IMapconService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMapconService)) {
                return new Proxy(obj);
            }
            return (IMapconService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                ServerAddr _arg3 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = setVoWifiOn(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = setVoWifiOff(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = setDomain(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        notifyWifiOff(IMapconServiceCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = notifyImsOn(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = notifyImsOff(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        sendMessage(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = setVoWifiServiceOn(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = setVoWifiServiceOff(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getVoWifiServiceState(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = setVoWifiServiceDomain(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = getVoWifiServiceDomain(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        int _arg1 = data.readInt();
                        String _arg2 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = ServerAddr.CREATOR.createFromParcel(data);
                        }
                        int _result11 = setupTunnelOverWifi(_arg0, _arg1, _arg2, _arg3);
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        int _arg12 = data.readInt();
                        String _arg22 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = ServerAddr.CREATOR.createFromParcel(data);
                        }
                        int _result12 = teardownTunnelOverWifi(_arg02, _arg12, _arg22, _arg3);
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        String _result13 = getUtOverWifiApn();
                        reply.writeNoException();
                        reply.writeString(_result13);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        notifyRoaming(data.readInt());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    String getUtOverWifiApn() throws RemoteException;

    int getVoWifiServiceDomain(int i, int i2) throws RemoteException;

    int getVoWifiServiceState(int i, int i2) throws RemoteException;

    int notifyImsOff(int i, int i2) throws RemoteException;

    int notifyImsOn(int i) throws RemoteException;

    void notifyRoaming(int i) throws RemoteException;

    void notifyWifiOff(IMapconServiceCallback iMapconServiceCallback) throws RemoteException;

    void sendMessage(int i) throws RemoteException;

    int setDomain(int i, int i2) throws RemoteException;

    int setVoWifiOff(int i) throws RemoteException;

    int setVoWifiOn(int i) throws RemoteException;

    int setVoWifiServiceDomain(int i, int i2, int i3) throws RemoteException;

    int setVoWifiServiceOff(int i, int i2) throws RemoteException;

    int setVoWifiServiceOn(int i, int i2) throws RemoteException;

    int setupTunnelOverWifi(int i, int i2, String str, ServerAddr serverAddr) throws RemoteException;

    int teardownTunnelOverWifi(int i, int i2, String str, ServerAddr serverAddr) throws RemoteException;
}
