package android.view;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.IRemoteAnimationFinishedCallback;

public interface IRemoteAnimationRunner extends IInterface {
    @UnsupportedAppUsage
    void onAnimationCancelled() throws RemoteException;

    @UnsupportedAppUsage
    void onAnimationStart(RemoteAnimationTarget[] remoteAnimationTargetArr, IRemoteAnimationFinishedCallback iRemoteAnimationFinishedCallback) throws RemoteException;

    public static class Default implements IRemoteAnimationRunner {
        @Override // android.view.IRemoteAnimationRunner
        public void onAnimationStart(RemoteAnimationTarget[] apps, IRemoteAnimationFinishedCallback finishedCallback) throws RemoteException {
        }

        @Override // android.view.IRemoteAnimationRunner
        public void onAnimationCancelled() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IRemoteAnimationRunner {
        private static final String DESCRIPTOR = "android.view.IRemoteAnimationRunner";
        static final int TRANSACTION_onAnimationCancelled = 2;
        static final int TRANSACTION_onAnimationStart = 1;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onAnimationStart";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onAnimationCancelled";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onAnimationStart((RemoteAnimationTarget[]) data.createTypedArray(RemoteAnimationTarget.CREATOR), IRemoteAnimationFinishedCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onAnimationCancelled();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IRemoteAnimationRunner {
            public static IRemoteAnimationRunner sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.view.IRemoteAnimationRunner
            public void onAnimationStart(RemoteAnimationTarget[] apps, IRemoteAnimationFinishedCallback finishedCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(apps, 0);
                    _data.writeStrongBinder(finishedCallback != null ? finishedCallback.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAnimationStart(apps, finishedCallback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IRemoteAnimationRunner
            public void onAnimationCancelled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAnimationCancelled();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRemoteAnimationRunner impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IRemoteAnimationRunner getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
