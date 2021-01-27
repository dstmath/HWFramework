package com.nxp.intf;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INxpExtrasService extends IInterface {
    Bundle close(String str, IBinder iBinder) throws RemoteException;

    Bundle getCallingAppPkg(String str, IBinder iBinder) throws RemoteException;

    byte[] getSecureElementUid(String str) throws RemoteException;

    boolean isEnabled() throws RemoteException;

    Bundle open(String str, IBinder iBinder) throws RemoteException;

    Bundle transceive(String str, byte[] bArr) throws RemoteException;

    public static class Default implements INxpExtrasService {
        @Override // com.nxp.intf.INxpExtrasService
        public Bundle open(String pkg, IBinder b) throws RemoteException {
            return null;
        }

        @Override // com.nxp.intf.INxpExtrasService
        public Bundle close(String pkg, IBinder b) throws RemoteException {
            return null;
        }

        @Override // com.nxp.intf.INxpExtrasService
        public Bundle transceive(String pkg, byte[] data_in) throws RemoteException {
            return null;
        }

        @Override // com.nxp.intf.INxpExtrasService
        public Bundle getCallingAppPkg(String pkg, IBinder b) throws RemoteException {
            return null;
        }

        @Override // com.nxp.intf.INxpExtrasService
        public byte[] getSecureElementUid(String pkg) throws RemoteException {
            return null;
        }

        @Override // com.nxp.intf.INxpExtrasService
        public boolean isEnabled() throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INxpExtrasService {
        private static final String DESCRIPTOR = "com.nxp.intf.INxpExtrasService";
        static final int TRANSACTION_close = 2;
        static final int TRANSACTION_getCallingAppPkg = 4;
        static final int TRANSACTION_getSecureElementUid = 5;
        static final int TRANSACTION_isEnabled = 6;
        static final int TRANSACTION_open = 1;
        static final int TRANSACTION_transceive = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INxpExtrasService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INxpExtrasService)) {
                return new Proxy(obj);
            }
            return (INxpExtrasService) iin;
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
                        Bundle _result = open(data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result2 = close(data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result3 = transceive(data.readString(), data.createByteArray());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result4 = getCallingAppPkg(data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result5 = getSecureElementUid(data.readString());
                        reply.writeNoException();
                        reply.writeByteArray(_result5);
                        return true;
                    case TRANSACTION_isEnabled /* 6 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isEnabled = isEnabled();
                        reply.writeNoException();
                        reply.writeInt(isEnabled ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements INxpExtrasService {
            public static INxpExtrasService sDefaultImpl;
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

            @Override // com.nxp.intf.INxpExtrasService
            public Bundle open(String pkg, IBinder b) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(b);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().open(pkg, b);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.intf.INxpExtrasService
            public Bundle close(String pkg, IBinder b) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(b);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().close(pkg, b);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.intf.INxpExtrasService
            public Bundle transceive(String pkg, byte[] data_in) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeByteArray(data_in);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().transceive(pkg, data_in);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.intf.INxpExtrasService
            public Bundle getCallingAppPkg(String pkg, IBinder b) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(b);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCallingAppPkg(pkg, b);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.intf.INxpExtrasService
            public byte[] getSecureElementUid(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSecureElementUid(pkg);
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

            @Override // com.nxp.intf.INxpExtrasService
            public boolean isEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isEnabled, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isEnabled();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INxpExtrasService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INxpExtrasService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
