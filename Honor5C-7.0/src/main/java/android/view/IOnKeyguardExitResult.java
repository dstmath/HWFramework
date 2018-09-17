package android.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOnKeyguardExitResult extends IInterface {

    public static abstract class Stub extends Binder implements IOnKeyguardExitResult {
        private static final String DESCRIPTOR = "android.view.IOnKeyguardExitResult";
        static final int TRANSACTION_onKeyguardExitResult = 1;

        private static class Proxy implements IOnKeyguardExitResult {
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

            public void onKeyguardExitResult(boolean success) throws RemoteException {
                int i = Stub.TRANSACTION_onKeyguardExitResult;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!success) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onKeyguardExitResult, _data, null, Stub.TRANSACTION_onKeyguardExitResult);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOnKeyguardExitResult asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOnKeyguardExitResult)) {
                return new Proxy(obj);
            }
            return (IOnKeyguardExitResult) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0 = false;
            switch (code) {
                case TRANSACTION_onKeyguardExitResult /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    onKeyguardExitResult(_arg0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onKeyguardExitResult(boolean z) throws RemoteException;
}
