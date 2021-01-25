package com.huawei.controlcenter.ui.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IControlCenterGesture extends IInterface {
    void dismissControlCenter() throws RemoteException;

    boolean isControlCenterShowing() throws RemoteException;

    void locateControlCenter() throws RemoteException;

    void moveControlCenter(float f) throws RemoteException;

    void preloadControlCenter() throws RemoteException;

    void preloadControlCenterSide(boolean z) throws RemoteException;

    void startControlCenter() throws RemoteException;

    void startControlCenterSide(boolean z) throws RemoteException;

    public static class Default implements IControlCenterGesture {
        @Override // com.huawei.controlcenter.ui.service.IControlCenterGesture
        public void startControlCenter() throws RemoteException {
        }

        @Override // com.huawei.controlcenter.ui.service.IControlCenterGesture
        public void dismissControlCenter() throws RemoteException {
        }

        @Override // com.huawei.controlcenter.ui.service.IControlCenterGesture
        public void preloadControlCenter() throws RemoteException {
        }

        @Override // com.huawei.controlcenter.ui.service.IControlCenterGesture
        public void moveControlCenter(float currentTouch) throws RemoteException {
        }

        @Override // com.huawei.controlcenter.ui.service.IControlCenterGesture
        public void locateControlCenter() throws RemoteException {
        }

        @Override // com.huawei.controlcenter.ui.service.IControlCenterGesture
        public void startControlCenterSide(boolean isLeft) throws RemoteException {
        }

        @Override // com.huawei.controlcenter.ui.service.IControlCenterGesture
        public void preloadControlCenterSide(boolean isLeft) throws RemoteException {
        }

        @Override // com.huawei.controlcenter.ui.service.IControlCenterGesture
        public boolean isControlCenterShowing() throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IControlCenterGesture {
        private static final String DESCRIPTOR = "com.huawei.controlcenter.ui.service.IControlCenterGesture";
        static final int TRANSACTION_dismissControlCenter = 2;
        static final int TRANSACTION_isControlCenterShowing = 8;
        static final int TRANSACTION_locateControlCenter = 5;
        static final int TRANSACTION_moveControlCenter = 4;
        static final int TRANSACTION_preloadControlCenter = 3;
        static final int TRANSACTION_preloadControlCenterSide = 7;
        static final int TRANSACTION_startControlCenter = 1;
        static final int TRANSACTION_startControlCenterSide = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IControlCenterGesture asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IControlCenterGesture)) {
                return new Proxy(obj);
            }
            return (IControlCenterGesture) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg0 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        startControlCenter();
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        dismissControlCenter();
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        preloadControlCenter();
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        moveControlCenter(data.readFloat());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        locateControlCenter();
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        startControlCenterSide(_arg0);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        preloadControlCenterSide(_arg0);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isControlCenterShowing = isControlCenterShowing();
                        reply.writeNoException();
                        reply.writeInt(isControlCenterShowing ? 1 : 0);
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
        public static class Proxy implements IControlCenterGesture {
            public static IControlCenterGesture sDefaultImpl;
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

            @Override // com.huawei.controlcenter.ui.service.IControlCenterGesture
            public void startControlCenter() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startControlCenter();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.controlcenter.ui.service.IControlCenterGesture
            public void dismissControlCenter() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dismissControlCenter();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.controlcenter.ui.service.IControlCenterGesture
            public void preloadControlCenter() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().preloadControlCenter();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.controlcenter.ui.service.IControlCenterGesture
            public void moveControlCenter(float currentTouch) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(currentTouch);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().moveControlCenter(currentTouch);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.controlcenter.ui.service.IControlCenterGesture
            public void locateControlCenter() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().locateControlCenter();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.controlcenter.ui.service.IControlCenterGesture
            public void startControlCenterSide(boolean isLeft) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isLeft ? 1 : 0);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startControlCenterSide(isLeft);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.controlcenter.ui.service.IControlCenterGesture
            public void preloadControlCenterSide(boolean isLeft) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isLeft ? 1 : 0);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().preloadControlCenterSide(isLeft);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.controlcenter.ui.service.IControlCenterGesture
            public boolean isControlCenterShowing() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isControlCenterShowing();
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

        public static boolean setDefaultImpl(IControlCenterGesture impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IControlCenterGesture getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
