package android.aft;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwAftPolicyService extends IInterface {
    void notifyFocusChange(int i, String str) throws RemoteException;

    void notifyIncallModeChange(int i, int i2) throws RemoteException;

    void notifyKeyguardStateChange(boolean z) throws RemoteException;

    void notifyOrientationChange(int i) throws RemoteException;

    public static class Default implements IHwAftPolicyService {
        @Override // android.aft.IHwAftPolicyService
        public void notifyOrientationChange(int orientation) throws RemoteException {
        }

        @Override // android.aft.IHwAftPolicyService
        public void notifyIncallModeChange(int ownerPid, int mode) throws RemoteException {
        }

        @Override // android.aft.IHwAftPolicyService
        public void notifyFocusChange(int focusPid, String focusTitle) throws RemoteException {
        }

        @Override // android.aft.IHwAftPolicyService
        public void notifyKeyguardStateChange(boolean isShowing) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwAftPolicyService {
        private static final String DESCRIPTOR = "android.aft.IHwAftPolicyService";
        static final int TRANSACTION_notifyFocusChange = 3;
        static final int TRANSACTION_notifyIncallModeChange = 2;
        static final int TRANSACTION_notifyKeyguardStateChange = 4;
        static final int TRANSACTION_notifyOrientationChange = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwAftPolicyService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwAftPolicyService)) {
                return new Proxy(obj);
            }
            return (IHwAftPolicyService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "notifyOrientationChange";
            }
            if (transactionCode == 2) {
                return "notifyIncallModeChange";
            }
            if (transactionCode == 3) {
                return "notifyFocusChange";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "notifyKeyguardStateChange";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                notifyOrientationChange(data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                notifyIncallModeChange(data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                notifyFocusChange(data.readInt(), data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                notifyKeyguardStateChange(data.readInt() != 0);
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
        public static class Proxy implements IHwAftPolicyService {
            public static IHwAftPolicyService sDefaultImpl;
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

            @Override // android.aft.IHwAftPolicyService
            public void notifyOrientationChange(int orientation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(orientation);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyOrientationChange(orientation);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.aft.IHwAftPolicyService
            public void notifyIncallModeChange(int ownerPid, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ownerPid);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyIncallModeChange(ownerPid, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.aft.IHwAftPolicyService
            public void notifyFocusChange(int focusPid, String focusTitle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(focusPid);
                    _data.writeString(focusTitle);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyFocusChange(focusPid, focusTitle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.aft.IHwAftPolicyService
            public void notifyKeyguardStateChange(boolean isShowing) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isShowing ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyKeyguardStateChange(isShowing);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwAftPolicyService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwAftPolicyService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
