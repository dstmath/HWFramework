package android.swing;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.KeyEvent;

public interface IHwSwingService extends IInterface {
    boolean dispatchUnhandledKey(KeyEvent keyEvent, String str) throws RemoteException;

    void notifyFingersTouching(boolean z) throws RemoteException;

    void notifyFocusChange(String str, String str2) throws RemoteException;

    void notifyRotationChange(int i) throws RemoteException;

    public static class Default implements IHwSwingService {
        @Override // android.swing.IHwSwingService
        public boolean dispatchUnhandledKey(KeyEvent event, String pkgName) throws RemoteException {
            return false;
        }

        @Override // android.swing.IHwSwingService
        public void notifyRotationChange(int rotation) throws RemoteException {
        }

        @Override // android.swing.IHwSwingService
        public void notifyFingersTouching(boolean isTouching) throws RemoteException {
        }

        @Override // android.swing.IHwSwingService
        public void notifyFocusChange(String focusWindowTitle, String pkgName) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwSwingService {
        private static final String DESCRIPTOR = "android.swing.IHwSwingService";
        static final int TRANSACTION_dispatchUnhandledKey = 1;
        static final int TRANSACTION_notifyFingersTouching = 3;
        static final int TRANSACTION_notifyFocusChange = 4;
        static final int TRANSACTION_notifyRotationChange = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwSwingService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwSwingService)) {
                return new Proxy(obj);
            }
            return (IHwSwingService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "dispatchUnhandledKey";
            }
            if (transactionCode == 2) {
                return "notifyRotationChange";
            }
            if (transactionCode == 3) {
                return "notifyFingersTouching";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "notifyFocusChange";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            KeyEvent _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = KeyEvent.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                boolean dispatchUnhandledKey = dispatchUnhandledKey(_arg0, data.readString());
                reply.writeNoException();
                reply.writeInt(dispatchUnhandledKey ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                notifyRotationChange(data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                notifyFingersTouching(data.readInt() != 0);
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                notifyFocusChange(data.readString(), data.readString());
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
        public static class Proxy implements IHwSwingService {
            public static IHwSwingService sDefaultImpl;
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

            @Override // android.swing.IHwSwingService
            public boolean dispatchUnhandledKey(KeyEvent event, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().dispatchUnhandledKey(event, pkgName);
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

            @Override // android.swing.IHwSwingService
            public void notifyRotationChange(int rotation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rotation);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyRotationChange(rotation);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.swing.IHwSwingService
            public void notifyFingersTouching(boolean isTouching) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isTouching ? 1 : 0);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyFingersTouching(isTouching);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.swing.IHwSwingService
            public void notifyFocusChange(String focusWindowTitle, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(focusWindowTitle);
                    _data.writeString(pkgName);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyFocusChange(focusWindowTitle, pkgName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwSwingService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwSwingService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
