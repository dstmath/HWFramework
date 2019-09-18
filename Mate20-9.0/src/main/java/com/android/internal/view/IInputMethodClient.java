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
        static final int TRANSACTION_reportFullscreenMode = 6;
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
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onBindMethod(InputBindResult res) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (res != null) {
                        _data.writeInt(1);
                        res.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
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
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setActive(boolean active, boolean fullscreen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(active);
                    _data.writeInt(fullscreen);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setUserActionNotificationSequenceNumber(int sequenceNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sequenceNumber);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void reportFullscreenMode(boolean fullscreen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fullscreen);
                    this.mRemote.transact(6, _data, null, 1);
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
            InputBindResult _arg0;
            if (code != 1598968902) {
                boolean _arg02 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setUsingInputMethod(_arg02);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = InputBindResult.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onBindMethod(_arg0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onUnbindMethod(data.readInt(), data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg03 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setActive(_arg03, _arg02);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        setUserActionNotificationSequenceNumber(data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        reportFullscreenMode(_arg02);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onBindMethod(InputBindResult inputBindResult) throws RemoteException;

    void onUnbindMethod(int i, int i2) throws RemoteException;

    void reportFullscreenMode(boolean z) throws RemoteException;

    void setActive(boolean z, boolean z2) throws RemoteException;

    void setUserActionNotificationSequenceNumber(int i) throws RemoteException;

    void setUsingInputMethod(boolean z) throws RemoteException;
}
