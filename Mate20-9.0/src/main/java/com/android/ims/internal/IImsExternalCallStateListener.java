package com.android.ims.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.ImsExternalCallState;
import java.util.List;

public interface IImsExternalCallStateListener extends IInterface {

    public static abstract class Stub extends Binder implements IImsExternalCallStateListener {
        private static final String DESCRIPTOR = "com.android.ims.internal.IImsExternalCallStateListener";
        static final int TRANSACTION_onImsExternalCallStateUpdate = 1;

        private static class Proxy implements IImsExternalCallStateListener {
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

            public void onImsExternalCallStateUpdate(List<ImsExternalCallState> externalCallDialogs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(externalCallDialogs);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsExternalCallStateListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsExternalCallStateListener)) {
                return new Proxy(obj);
            }
            return (IImsExternalCallStateListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onImsExternalCallStateUpdate(data.createTypedArrayList(ImsExternalCallState.CREATOR));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onImsExternalCallStateUpdate(List<ImsExternalCallState> list) throws RemoteException;
}
