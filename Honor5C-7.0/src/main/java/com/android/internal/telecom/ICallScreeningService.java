package com.android.internal.telecom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.ParcelableCall;

public interface ICallScreeningService extends IInterface {

    public static abstract class Stub extends Binder implements ICallScreeningService {
        private static final String DESCRIPTOR = "com.android.internal.telecom.ICallScreeningService";
        static final int TRANSACTION_screenCall = 1;

        private static class Proxy implements ICallScreeningService {
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

            public void screenCall(ICallScreeningAdapter adapter, ParcelableCall call) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (adapter != null) {
                        iBinder = adapter.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (call != null) {
                        _data.writeInt(Stub.TRANSACTION_screenCall);
                        call.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_screenCall, _data, null, Stub.TRANSACTION_screenCall);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICallScreeningService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICallScreeningService)) {
                return new Proxy(obj);
            }
            return (ICallScreeningService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_screenCall /*1*/:
                    ParcelableCall parcelableCall;
                    data.enforceInterface(DESCRIPTOR);
                    ICallScreeningAdapter _arg0 = com.android.internal.telecom.ICallScreeningAdapter.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        parcelableCall = (ParcelableCall) ParcelableCall.CREATOR.createFromParcel(data);
                    } else {
                        parcelableCall = null;
                    }
                    screenCall(_arg0, parcelableCall);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void screenCall(ICallScreeningAdapter iCallScreeningAdapter, ParcelableCall parcelableCall) throws RemoteException;
}
