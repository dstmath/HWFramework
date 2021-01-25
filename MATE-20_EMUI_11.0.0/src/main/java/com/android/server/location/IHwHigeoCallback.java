package com.android.server.location;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwHigeoCallback extends IInterface {
    void onCellBatchingCallback(int i, Bundle bundle) throws RemoteException;

    void onCellFenceCallback(int i, Bundle bundle) throws RemoteException;

    void onGeoFenceCallback(int i, Bundle bundle) throws RemoteException;

    void onHigeoEventCallback(int i, Bundle bundle) throws RemoteException;

    void onMmDataRequest(int i, Bundle bundle) throws RemoteException;

    void onWifiFenceCallback(int i, Bundle bundle) throws RemoteException;

    public static class Default implements IHwHigeoCallback {
        @Override // com.android.server.location.IHwHigeoCallback
        public void onMmDataRequest(int type, Bundle bundle) throws RemoteException {
        }

        @Override // com.android.server.location.IHwHigeoCallback
        public void onHigeoEventCallback(int type, Bundle bundle) throws RemoteException {
        }

        @Override // com.android.server.location.IHwHigeoCallback
        public void onCellBatchingCallback(int type, Bundle bundle) throws RemoteException {
        }

        @Override // com.android.server.location.IHwHigeoCallback
        public void onWifiFenceCallback(int type, Bundle bundle) throws RemoteException {
        }

        @Override // com.android.server.location.IHwHigeoCallback
        public void onCellFenceCallback(int type, Bundle bundle) throws RemoteException {
        }

        @Override // com.android.server.location.IHwHigeoCallback
        public void onGeoFenceCallback(int type, Bundle bundle) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwHigeoCallback {
        private static final String DESCRIPTOR = "com.android.server.location.IHwHigeoCallback";
        static final int TRANSACTION_onCellBatchingCallback = 3;
        static final int TRANSACTION_onCellFenceCallback = 5;
        static final int TRANSACTION_onGeoFenceCallback = 6;
        static final int TRANSACTION_onHigeoEventCallback = 2;
        static final int TRANSACTION_onMmDataRequest = 1;
        static final int TRANSACTION_onWifiFenceCallback = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwHigeoCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwHigeoCallback)) {
                return new Proxy(obj);
            }
            return (IHwHigeoCallback) iin;
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
                        onMmDataRequest(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        onHigeoEventCallback(_arg02, _arg12);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        onCellBatchingCallback(_arg03, _arg13);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg14 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        onWifiFenceCallback(_arg04, _arg14);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        onCellFenceCallback(_arg05, _arg15);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg16 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        onGeoFenceCallback(_arg06, _arg16);
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

        /* access modifiers changed from: private */
        public static class Proxy implements IHwHigeoCallback {
            public static IHwHigeoCallback sDefaultImpl;
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

            @Override // com.android.server.location.IHwHigeoCallback
            public void onMmDataRequest(int type, Bundle bundle) throws RemoteException {
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
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onMmDataRequest(type, bundle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.location.IHwHigeoCallback
            public void onHigeoEventCallback(int type, Bundle bundle) throws RemoteException {
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
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onHigeoEventCallback(type, bundle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.location.IHwHigeoCallback
            public void onCellBatchingCallback(int type, Bundle bundle) throws RemoteException {
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
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCellBatchingCallback(type, bundle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.location.IHwHigeoCallback
            public void onWifiFenceCallback(int type, Bundle bundle) throws RemoteException {
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
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onWifiFenceCallback(type, bundle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.location.IHwHigeoCallback
            public void onCellFenceCallback(int type, Bundle bundle) throws RemoteException {
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
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCellFenceCallback(type, bundle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.location.IHwHigeoCallback
            public void onGeoFenceCallback(int type, Bundle bundle) throws RemoteException {
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
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onGeoFenceCallback(type, bundle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwHigeoCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwHigeoCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
