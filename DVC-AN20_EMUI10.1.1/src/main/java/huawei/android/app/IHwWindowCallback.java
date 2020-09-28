package huawei.android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.KeyEvent;

public interface IHwWindowCallback extends IInterface {
    void focusedAppChanged() throws RemoteException;

    void handleConfigurationChanged() throws RemoteException;

    long interceptKeyBeforeDispatching(KeyEvent keyEvent, int i) throws RemoteException;

    void screenTurnedOff() throws RemoteException;

    public static class Default implements IHwWindowCallback {
        @Override // huawei.android.app.IHwWindowCallback
        public void handleConfigurationChanged() throws RemoteException {
        }

        @Override // huawei.android.app.IHwWindowCallback
        public long interceptKeyBeforeDispatching(KeyEvent event, int policyFlags) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.app.IHwWindowCallback
        public void screenTurnedOff() throws RemoteException {
        }

        @Override // huawei.android.app.IHwWindowCallback
        public void focusedAppChanged() throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwWindowCallback {
        private static final String DESCRIPTOR = "huawei.android.app.IHwWindowCallback";
        static final int TRANSACTION_focusedAppChanged = 4;
        static final int TRANSACTION_handleConfigurationChanged = 1;
        static final int TRANSACTION_interceptKeyBeforeDispatching = 2;
        static final int TRANSACTION_screenTurnedOff = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwWindowCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwWindowCallback)) {
                return new Proxy(obj);
            }
            return (IHwWindowCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            KeyEvent _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                handleConfigurationChanged();
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = (KeyEvent) KeyEvent.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                long _result = interceptKeyBeforeDispatching(_arg0, data.readInt());
                reply.writeNoException();
                reply.writeLong(_result);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                screenTurnedOff();
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                focusedAppChanged();
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwWindowCallback {
            public static IHwWindowCallback sDefaultImpl;
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

            @Override // huawei.android.app.IHwWindowCallback
            public void handleConfigurationChanged() throws RemoteException {
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
                    Stub.getDefaultImpl().handleConfigurationChanged();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.app.IHwWindowCallback
            public long interceptKeyBeforeDispatching(KeyEvent event, int policyFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(policyFlags);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().interceptKeyBeforeDispatching(event, policyFlags);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.app.IHwWindowCallback
            public void screenTurnedOff() throws RemoteException {
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
                    Stub.getDefaultImpl().screenTurnedOff();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.app.IHwWindowCallback
            public void focusedAppChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().focusedAppChanged();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwWindowCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwWindowCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
