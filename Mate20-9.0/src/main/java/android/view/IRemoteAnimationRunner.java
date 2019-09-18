package android.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.IRemoteAnimationFinishedCallback;

public interface IRemoteAnimationRunner extends IInterface {

    public static abstract class Stub extends Binder implements IRemoteAnimationRunner {
        private static final String DESCRIPTOR = "android.view.IRemoteAnimationRunner";
        static final int TRANSACTION_onAnimationCancelled = 2;
        static final int TRANSACTION_onAnimationStart = 1;

        private static class Proxy implements IRemoteAnimationRunner {
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

            public void onAnimationStart(RemoteAnimationTarget[] apps, IRemoteAnimationFinishedCallback finishedCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(apps, 0);
                    _data.writeStrongBinder(finishedCallback != null ? finishedCallback.asBinder() : null);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onAnimationCancelled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRemoteAnimationRunner asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRemoteAnimationRunner)) {
                return new Proxy(obj);
            }
            return (IRemoteAnimationRunner) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onAnimationStart((RemoteAnimationTarget[]) data.createTypedArray(RemoteAnimationTarget.CREATOR), IRemoteAnimationFinishedCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onAnimationCancelled();
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

    void onAnimationCancelled() throws RemoteException;

    void onAnimationStart(RemoteAnimationTarget[] remoteAnimationTargetArr, IRemoteAnimationFinishedCallback iRemoteAnimationFinishedCallback) throws RemoteException;
}
