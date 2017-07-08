package com.nxp.intf;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILoaderService extends IInterface {

    public static abstract class Stub extends Binder implements ILoaderService {
        private static final String DESCRIPTOR = "com.nxp.intf.ILoaderService";
        static final int TRANSACTION_appletLoadApplet = 1;
        static final int TRANSACTION_getKeyCertificate = 3;
        static final int TRANSACTION_getListofApplets = 2;
        static final int TRANSACTION_lsExecuteScript = 4;
        static final int TRANSACTION_lsGetVersion = 5;

        private static class Proxy implements ILoaderService {
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

            public int appletLoadApplet(String pkg, String choice) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(choice);
                    this.mRemote.transact(Stub.TRANSACTION_appletLoadApplet, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getListofApplets(String pkg, String[] name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (name == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(name.length);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getListofApplets, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readStringArray(name);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getKeyCertificate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getKeyCertificate, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] lsExecuteScript(String srcIn, String rspOut) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(srcIn);
                    _data.writeString(rspOut);
                    this.mRemote.transact(Stub.TRANSACTION_lsExecuteScript, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] lsGetVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_lsGetVersion, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
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

        public static ILoaderService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILoaderService)) {
                return new Proxy(obj);
            }
            return (ILoaderService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            byte[] _result2;
            switch (code) {
                case TRANSACTION_appletLoadApplet /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = appletLoadApplet(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getListofApplets /*2*/:
                    String[] strArr;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    int _arg1_length = data.readInt();
                    if (_arg1_length < 0) {
                        strArr = null;
                    } else {
                        strArr = new String[_arg1_length];
                    }
                    _result = getListofApplets(_arg0, strArr);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeStringArray(strArr);
                    return true;
                case TRANSACTION_getKeyCertificate /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getKeyCertificate();
                    reply.writeNoException();
                    reply.writeByteArray(_result2);
                    return true;
                case TRANSACTION_lsExecuteScript /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = lsExecuteScript(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeByteArray(_result2);
                    return true;
                case TRANSACTION_lsGetVersion /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = lsGetVersion();
                    reply.writeNoException();
                    reply.writeByteArray(_result2);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int appletLoadApplet(String str, String str2) throws RemoteException;

    byte[] getKeyCertificate() throws RemoteException;

    int getListofApplets(String str, String[] strArr) throws RemoteException;

    byte[] lsExecuteScript(String str, String str2) throws RemoteException;

    byte[] lsGetVersion() throws RemoteException;
}
