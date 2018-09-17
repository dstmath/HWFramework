package com.android.internal.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IInputMethodClient extends IInterface {

    public static abstract class Stub extends Binder implements IInputMethodClient {
        private static final String DESCRIPTOR = "com.android.internal.view.IInputMethodClient";
        static final int TRANSACTION_onBindMethod = 2;
        static final int TRANSACTION_onUnbindMethod = 3;
        static final int TRANSACTION_setActive = 4;
        static final int TRANSACTION_setUserActionNotificationSequenceNumber = 5;
        static final int TRANSACTION_setUsingInputMethod = 1;

        private static class Proxy implements IInputMethodClient {
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

            public void setUsingInputMethod(boolean state) throws RemoteException {
                int i = Stub.TRANSACTION_setUsingInputMethod;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!state) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setUsingInputMethod, _data, null, Stub.TRANSACTION_setUsingInputMethod);
                } finally {
                    _data.recycle();
                }
            }

            public void onBindMethod(InputBindResult res) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (res != null) {
                        _data.writeInt(Stub.TRANSACTION_setUsingInputMethod);
                        res.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onBindMethod, _data, null, Stub.TRANSACTION_setUsingInputMethod);
                } finally {
                    _data.recycle();
                }
            }

            public void onUnbindMethod(int sequence, int unbindReason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sequence);
                    _data.writeInt(unbindReason);
                    this.mRemote.transact(Stub.TRANSACTION_onUnbindMethod, _data, null, Stub.TRANSACTION_setUsingInputMethod);
                } finally {
                    _data.recycle();
                }
            }

            public void setActive(boolean active) throws RemoteException {
                int i = Stub.TRANSACTION_setUsingInputMethod;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!active) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setActive, _data, null, Stub.TRANSACTION_setUsingInputMethod);
                } finally {
                    _data.recycle();
                }
            }

            public void setUserActionNotificationSequenceNumber(int sequenceNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sequenceNumber);
                    this.mRemote.transact(Stub.TRANSACTION_setUserActionNotificationSequenceNumber, _data, null, Stub.TRANSACTION_setUsingInputMethod);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInputMethodClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInputMethodClient)) {
                return new Proxy(obj);
            }
            return (IInputMethodClient) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0 = false;
            switch (code) {
                case TRANSACTION_setUsingInputMethod /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    setUsingInputMethod(_arg0);
                    return true;
                case TRANSACTION_onBindMethod /*2*/:
                    InputBindResult inputBindResult;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        inputBindResult = (InputBindResult) InputBindResult.CREATOR.createFromParcel(data);
                    } else {
                        inputBindResult = null;
                    }
                    onBindMethod(inputBindResult);
                    return true;
                case TRANSACTION_onUnbindMethod /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onUnbindMethod(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_setActive /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    setActive(_arg0);
                    return true;
                case TRANSACTION_setUserActionNotificationSequenceNumber /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    setUserActionNotificationSequenceNumber(data.readInt());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onBindMethod(InputBindResult inputBindResult) throws RemoteException;

    void onUnbindMethod(int i, int i2) throws RemoteException;

    void setActive(boolean z) throws RemoteException;

    void setUserActionNotificationSequenceNumber(int i) throws RemoteException;

    void setUsingInputMethod(boolean z) throws RemoteException;
}
