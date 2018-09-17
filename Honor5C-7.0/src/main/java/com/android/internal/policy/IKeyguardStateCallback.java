package com.android.internal.policy;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IKeyguardStateCallback extends IInterface {

    public static abstract class Stub extends Binder implements IKeyguardStateCallback {
        private static final String DESCRIPTOR = "com.android.internal.policy.IKeyguardStateCallback";
        static final int TRANSACTION_onInputRestrictedStateChanged = 3;
        static final int TRANSACTION_onShowingStateChanged = 1;
        static final int TRANSACTION_onSimSecureStateChanged = 2;

        private static class Proxy implements IKeyguardStateCallback {
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

            public void onShowingStateChanged(boolean showing) throws RemoteException {
                int i = Stub.TRANSACTION_onShowingStateChanged;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!showing) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onShowingStateChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onSimSecureStateChanged(boolean simSecure) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (simSecure) {
                        i = Stub.TRANSACTION_onShowingStateChanged;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onSimSecureStateChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onInputRestrictedStateChanged(boolean inputRestricted) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (inputRestricted) {
                        i = Stub.TRANSACTION_onShowingStateChanged;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onInputRestrictedStateChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IKeyguardStateCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IKeyguardStateCallback)) {
                return new Proxy(obj);
            }
            return (IKeyguardStateCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0 = false;
            switch (code) {
                case TRANSACTION_onShowingStateChanged /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    onShowingStateChanged(_arg0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onSimSecureStateChanged /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    onSimSecureStateChanged(_arg0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onInputRestrictedStateChanged /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    onInputRestrictedStateChanged(_arg0);
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onInputRestrictedStateChanged(boolean z) throws RemoteException;

    void onShowingStateChanged(boolean z) throws RemoteException;

    void onSimSecureStateChanged(boolean z) throws RemoteException;
}
