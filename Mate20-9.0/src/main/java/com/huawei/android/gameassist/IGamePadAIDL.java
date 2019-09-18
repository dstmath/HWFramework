package com.huawei.android.gameassist;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.InputEvent;

public interface IGamePadAIDL extends IInterface {

    public static abstract class Stub extends Binder implements IGamePadAIDL {
        private static final String DESCRIPTOR = "com.huawei.android.gameassist.IGamePadAIDL";
        static final int TRANSACTION_notifyInputEvent = 1;

        private static class Proxy implements IGamePadAIDL {
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

            public void notifyInputEvent(InputEvent inputEvent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (inputEvent != null) {
                        _data.writeInt(1);
                        inputEvent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
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

        public static IGamePadAIDL asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGamePadAIDL)) {
                return new Proxy(obj);
            }
            return (IGamePadAIDL) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            InputEvent _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = (InputEvent) InputEvent.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                notifyInputEvent(_arg0);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void notifyInputEvent(InputEvent inputEvent) throws RemoteException;
}
