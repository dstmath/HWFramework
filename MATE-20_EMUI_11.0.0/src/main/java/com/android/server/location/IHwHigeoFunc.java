package com.android.server.location;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.server.location.IHwHigeoCallback;

public interface IHwHigeoFunc extends IInterface {
    boolean registerHigeoCallback(IHwHigeoCallback iHwHigeoCallback) throws RemoteException;

    boolean sendCellBatchingData(int i, Bundle bundle) throws RemoteException;

    boolean sendCellFenceData(int i, Bundle bundle) throws RemoteException;

    int sendGeoFenceData(int i, Bundle bundle) throws RemoteException;

    boolean sendHigeoData(int i, Bundle bundle) throws RemoteException;

    boolean sendMmData(int i, Bundle bundle) throws RemoteException;

    int sendWifiFenceData(int i, Bundle bundle) throws RemoteException;

    public static class Default implements IHwHigeoFunc {
        @Override // com.android.server.location.IHwHigeoFunc
        public boolean sendMmData(int type, Bundle bundle) throws RemoteException {
            return false;
        }

        @Override // com.android.server.location.IHwHigeoFunc
        public boolean sendHigeoData(int type, Bundle bundle) throws RemoteException {
            return false;
        }

        @Override // com.android.server.location.IHwHigeoFunc
        public boolean sendCellBatchingData(int type, Bundle bundle) throws RemoteException {
            return false;
        }

        @Override // com.android.server.location.IHwHigeoFunc
        public int sendWifiFenceData(int type, Bundle bundle) throws RemoteException {
            return 0;
        }

        @Override // com.android.server.location.IHwHigeoFunc
        public boolean sendCellFenceData(int type, Bundle bundle) throws RemoteException {
            return false;
        }

        @Override // com.android.server.location.IHwHigeoFunc
        public int sendGeoFenceData(int type, Bundle bundle) throws RemoteException {
            return 0;
        }

        @Override // com.android.server.location.IHwHigeoFunc
        public boolean registerHigeoCallback(IHwHigeoCallback higeoCallback) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwHigeoFunc {
        private static final String DESCRIPTOR = "com.android.server.location.IHwHigeoFunc";
        static final int TRANSACTION_registerHigeoCallback = 7;
        static final int TRANSACTION_sendCellBatchingData = 3;
        static final int TRANSACTION_sendCellFenceData = 5;
        static final int TRANSACTION_sendGeoFenceData = 6;
        static final int TRANSACTION_sendHigeoData = 2;
        static final int TRANSACTION_sendMmData = 1;
        static final int TRANSACTION_sendWifiFenceData = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwHigeoFunc asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwHigeoFunc)) {
                return new Proxy(obj);
            }
            return (IHwHigeoFunc) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            Bundle _arg12;
            Bundle _arg13;
            Bundle _arg14;
            Bundle _arg15;
            Bundle _arg16;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        boolean sendMmData = sendMmData(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(sendMmData ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        boolean sendHigeoData = sendHigeoData(_arg02, _arg12);
                        reply.writeNoException();
                        reply.writeInt(sendHigeoData ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        boolean sendCellBatchingData = sendCellBatchingData(_arg03, _arg13);
                        reply.writeNoException();
                        reply.writeInt(sendCellBatchingData ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg14 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        int _result = sendWifiFenceData(_arg04, _arg14);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        boolean sendCellFenceData = sendCellFenceData(_arg05, _arg15);
                        reply.writeNoException();
                        reply.writeInt(sendCellFenceData ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg16 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        int _result2 = sendGeoFenceData(_arg06, _arg16);
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerHigeoCallback = registerHigeoCallback(IHwHigeoCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerHigeoCallback ? 1 : 0);
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
        public static class Proxy implements IHwHigeoFunc {
            public static IHwHigeoFunc sDefaultImpl;
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

            @Override // com.android.server.location.IHwHigeoFunc
            public boolean sendMmData(int type, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    boolean _result = true;
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendMmData(type, bundle);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.location.IHwHigeoFunc
            public boolean sendHigeoData(int type, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    boolean _result = true;
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendHigeoData(type, bundle);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.location.IHwHigeoFunc
            public boolean sendCellBatchingData(int type, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    boolean _result = true;
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendCellBatchingData(type, bundle);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.location.IHwHigeoFunc
            public int sendWifiFenceData(int type, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendWifiFenceData(type, bundle);
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

            @Override // com.android.server.location.IHwHigeoFunc
            public boolean sendCellFenceData(int type, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    boolean _result = true;
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendCellFenceData(type, bundle);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.location.IHwHigeoFunc
            public int sendGeoFenceData(int type, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendGeoFenceData(type, bundle);
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

            @Override // com.android.server.location.IHwHigeoFunc
            public boolean registerHigeoCallback(IHwHigeoCallback higeoCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(higeoCallback != null ? higeoCallback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerHigeoCallback(higeoCallback);
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

        public static boolean setDefaultImpl(IHwHigeoFunc impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwHigeoFunc getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
