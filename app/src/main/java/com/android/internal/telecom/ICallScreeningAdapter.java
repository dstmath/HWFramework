package com.android.internal.telecom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICallScreeningAdapter extends IInterface {

    public static abstract class Stub extends Binder implements ICallScreeningAdapter {
        private static final String DESCRIPTOR = "com.android.internal.telecom.ICallScreeningAdapter";
        static final int TRANSACTION_allowCall = 1;
        static final int TRANSACTION_disallowCall = 2;

        private static class Proxy implements ICallScreeningAdapter {
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

            public void allowCall(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(Stub.TRANSACTION_allowCall, _data, null, Stub.TRANSACTION_allowCall);
                } finally {
                    _data.recycle();
                }
            }

            public void disallowCall(String callId, boolean shouldReject, boolean shouldAddToCallLog, boolean shouldShowNotification) throws RemoteException {
                int i = Stub.TRANSACTION_allowCall;
                Parcel _data = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (shouldReject) {
                        i2 = Stub.TRANSACTION_allowCall;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (shouldAddToCallLog) {
                        i2 = Stub.TRANSACTION_allowCall;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!shouldShowNotification) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_disallowCall, _data, null, Stub.TRANSACTION_allowCall);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICallScreeningAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICallScreeningAdapter)) {
                return new Proxy(obj);
            }
            return (ICallScreeningAdapter) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg3 = false;
            switch (code) {
                case TRANSACTION_allowCall /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    allowCall(data.readString());
                    return true;
                case TRANSACTION_disallowCall /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    boolean _arg1 = data.readInt() != 0;
                    boolean _arg2 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        _arg3 = true;
                    }
                    disallowCall(_arg0, _arg1, _arg2, _arg3);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void allowCall(String str) throws RemoteException;

    void disallowCall(String str, boolean z, boolean z2, boolean z3) throws RemoteException;
}
