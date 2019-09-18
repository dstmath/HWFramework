package com.huawei.android.hardware.input;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.hardware.input.IHwTHPEventListener;

public interface IHwInputManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwInputManager {
        private static final String DESCRIPTOR = "com.huawei.android.hardware.input.IHwInputManager";
        static final int TRANSACTION_registerListener = 2;
        static final int TRANSACTION_runHwTHPCommand = 1;
        static final int TRANSACTION_setInputEventStrategy = 4;
        static final int TRANSACTION_unregisterListener = 3;

        private static class Proxy implements IHwInputManager {
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

            public String runHwTHPCommand(String command, String parameter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(command);
                    _data.writeString(parameter);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerListener(IHwTHPEventListener listener, IBinder ibinder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeStrongBinder(ibinder);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterListener(IHwTHPEventListener listener, IBinder ibinder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeStrongBinder(ibinder);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInputEventStrategy(boolean isStartInputEventControl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isStartInputEventControl);
                    this.mRemote.transact(4, _data, _reply, 0);
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

        public static IHwInputManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwInputManager)) {
                return new Proxy(obj);
            }
            return (IHwInputManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = runHwTHPCommand(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        registerListener(IHwTHPEventListener.Stub.asInterface(data.readStrongBinder()), data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterListener(IHwTHPEventListener.Stub.asInterface(data.readStrongBinder()), data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        setInputEventStrategy(data.readInt() != 0);
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
    }

    void registerListener(IHwTHPEventListener iHwTHPEventListener, IBinder iBinder) throws RemoteException;

    String runHwTHPCommand(String str, String str2) throws RemoteException;

    void setInputEventStrategy(boolean z) throws RemoteException;

    void unregisterListener(IHwTHPEventListener iHwTHPEventListener, IBinder iBinder) throws RemoteException;
}
