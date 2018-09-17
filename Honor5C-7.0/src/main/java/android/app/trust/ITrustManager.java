package android.app.trust;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITrustManager extends IInterface {

    public static abstract class Stub extends Binder implements ITrustManager {
        private static final String DESCRIPTOR = "android.app.trust.ITrustManager";
        static final int TRANSACTION_isDeviceLocked = 7;
        static final int TRANSACTION_isDeviceSecure = 8;
        static final int TRANSACTION_isTrustUsuallyManaged = 9;
        static final int TRANSACTION_registerTrustListener = 3;
        static final int TRANSACTION_reportEnabledTrustAgentsChanged = 2;
        static final int TRANSACTION_reportKeyguardShowingChanged = 5;
        static final int TRANSACTION_reportUnlockAttempt = 1;
        static final int TRANSACTION_setDeviceLockedForUser = 6;
        static final int TRANSACTION_unregisterTrustListener = 4;

        private static class Proxy implements ITrustManager {
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

            public void reportUnlockAttempt(boolean successful, int userId) throws RemoteException {
                int i = Stub.TRANSACTION_reportUnlockAttempt;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!successful) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_reportUnlockAttempt, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportEnabledTrustAgentsChanged(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_reportEnabledTrustAgentsChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerTrustListener(ITrustListener trustListener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (trustListener != null) {
                        iBinder = trustListener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerTrustListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterTrustListener(ITrustListener trustListener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (trustListener != null) {
                        iBinder = trustListener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterTrustListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportKeyguardShowingChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_reportKeyguardShowingChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDeviceLockedForUser(int userId, boolean locked) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (locked) {
                        i = Stub.TRANSACTION_reportUnlockAttempt;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setDeviceLockedForUser, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isDeviceLocked(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_isDeviceLocked, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isDeviceSecure(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_isDeviceSecure, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isTrustUsuallyManaged(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_isTrustUsuallyManaged, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITrustManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITrustManager)) {
                return new Proxy(obj);
            }
            return (ITrustManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg1 = 0;
            boolean _result;
            switch (code) {
                case TRANSACTION_reportUnlockAttempt /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    reportUnlockAttempt(data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_reportEnabledTrustAgentsChanged /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    reportEnabledTrustAgentsChanged(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerTrustListener /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerTrustListener(android.app.trust.ITrustListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterTrustListener /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterTrustListener(android.app.trust.ITrustListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_reportKeyguardShowingChanged /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    reportKeyguardShowingChanged();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setDeviceLockedForUser /*6*/:
                    boolean _arg12;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg12 = true;
                    }
                    setDeviceLockedForUser(_arg0, _arg12);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isDeviceLocked /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isDeviceLocked(data.readInt());
                    reply.writeNoException();
                    if (_result) {
                        _arg1 = TRANSACTION_reportUnlockAttempt;
                    }
                    reply.writeInt(_arg1);
                    return true;
                case TRANSACTION_isDeviceSecure /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isDeviceSecure(data.readInt());
                    reply.writeNoException();
                    if (_result) {
                        _arg1 = TRANSACTION_reportUnlockAttempt;
                    }
                    reply.writeInt(_arg1);
                    return true;
                case TRANSACTION_isTrustUsuallyManaged /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isTrustUsuallyManaged(data.readInt());
                    reply.writeNoException();
                    if (_result) {
                        _arg1 = TRANSACTION_reportUnlockAttempt;
                    }
                    reply.writeInt(_arg1);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean isDeviceLocked(int i) throws RemoteException;

    boolean isDeviceSecure(int i) throws RemoteException;

    boolean isTrustUsuallyManaged(int i) throws RemoteException;

    void registerTrustListener(ITrustListener iTrustListener) throws RemoteException;

    void reportEnabledTrustAgentsChanged(int i) throws RemoteException;

    void reportKeyguardShowingChanged() throws RemoteException;

    void reportUnlockAttempt(boolean z, int i) throws RemoteException;

    void setDeviceLockedForUser(int i, boolean z) throws RemoteException;

    void unregisterTrustListener(ITrustListener iTrustListener) throws RemoteException;
}
