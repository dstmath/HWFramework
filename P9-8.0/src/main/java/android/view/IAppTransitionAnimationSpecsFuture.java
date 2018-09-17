package android.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAppTransitionAnimationSpecsFuture extends IInterface {

    public static abstract class Stub extends Binder implements IAppTransitionAnimationSpecsFuture {
        private static final String DESCRIPTOR = "android.view.IAppTransitionAnimationSpecsFuture";
        static final int TRANSACTION_get = 1;

        private static class Proxy implements IAppTransitionAnimationSpecsFuture {
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

            public AppTransitionAnimationSpec[] get() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    AppTransitionAnimationSpec[] _result = (AppTransitionAnimationSpec[]) _reply.createTypedArray(AppTransitionAnimationSpec.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAppTransitionAnimationSpecsFuture asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAppTransitionAnimationSpecsFuture)) {
                return new Proxy(obj);
            }
            return (IAppTransitionAnimationSpecsFuture) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    AppTransitionAnimationSpec[] _result = get();
                    reply.writeNoException();
                    reply.writeTypedArray(_result, 1);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    AppTransitionAnimationSpec[] get() throws RemoteException;
}
