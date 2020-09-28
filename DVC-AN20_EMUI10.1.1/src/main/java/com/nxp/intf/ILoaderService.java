package com.nxp.intf;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILoaderService extends IInterface {
    int appletLoadApplet(String str, String str2) throws RemoteException;

    byte[] getKeyCertificate() throws RemoteException;

    int getListofApplets(String str, String[] strArr) throws RemoteException;

    byte[] lsExecuteScript(String str, String str2) throws RemoteException;

    byte[] lsGetVersion() throws RemoteException;

    public static class Default implements ILoaderService {
        @Override // com.nxp.intf.ILoaderService
        public int appletLoadApplet(String pkg, String choice) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.intf.ILoaderService
        public int getListofApplets(String pkg, String[] name) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.intf.ILoaderService
        public byte[] getKeyCertificate() throws RemoteException {
            return null;
        }

        @Override // com.nxp.intf.ILoaderService
        public byte[] lsExecuteScript(String srcIn, String rspOut) throws RemoteException {
            return null;
        }

        @Override // com.nxp.intf.ILoaderService
        public byte[] lsGetVersion() throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILoaderService {
        private static final String DESCRIPTOR = "com.nxp.intf.ILoaderService";
        static final int TRANSACTION_appletLoadApplet = 1;
        static final int TRANSACTION_getKeyCertificate = 3;
        static final int TRANSACTION_getListofApplets = 2;
        static final int TRANSACTION_lsExecuteScript = 4;
        static final int TRANSACTION_lsGetVersion = 5;

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

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String[] _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = appletLoadApplet(data.readString(), data.readString());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                String _arg0 = data.readString();
                int _arg1_length = data.readInt();
                if (_arg1_length < 0) {
                    _arg1 = null;
                } else {
                    _arg1 = new String[_arg1_length];
                }
                int _result2 = getListofApplets(_arg0, _arg1);
                reply.writeNoException();
                reply.writeInt(_result2);
                reply.writeStringArray(_arg1);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                byte[] _result3 = getKeyCertificate();
                reply.writeNoException();
                reply.writeByteArray(_result3);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                byte[] _result4 = lsExecuteScript(data.readString(), data.readString());
                reply.writeNoException();
                reply.writeByteArray(_result4);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                byte[] _result5 = lsGetVersion();
                reply.writeNoException();
                reply.writeByteArray(_result5);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ILoaderService {
            public static ILoaderService sDefaultImpl;
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

            @Override // com.nxp.intf.ILoaderService
            public int appletLoadApplet(String pkg, String choice) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(choice);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().appletLoadApplet(pkg, choice);
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

            @Override // com.nxp.intf.ILoaderService
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
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getListofApplets(pkg, name);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readStringArray(name);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.intf.ILoaderService
            public byte[] getKeyCertificate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getKeyCertificate();
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.intf.ILoaderService
            public byte[] lsExecuteScript(String srcIn, String rspOut) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(srcIn);
                    _data.writeString(rspOut);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().lsExecuteScript(srcIn, rspOut);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.intf.ILoaderService
            public byte[] lsGetVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().lsGetVersion();
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILoaderService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILoaderService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
