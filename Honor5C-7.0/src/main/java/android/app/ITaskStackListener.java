package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITaskStackListener extends IInterface {

    public static abstract class Stub extends Binder implements ITaskStackListener {
        private static final String DESCRIPTOR = "android.app.ITaskStackListener";
        static final int TRANSACTION_onActivityDismissingDockedStack = 6;
        static final int TRANSACTION_onActivityForcedResizable = 5;
        static final int TRANSACTION_onActivityPinned = 2;
        static final int TRANSACTION_onPinnedActivityRestartAttempt = 3;
        static final int TRANSACTION_onPinnedStackAnimationEnded = 4;
        static final int TRANSACTION_onTaskStackChanged = 1;

        private static class Proxy implements ITaskStackListener {
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

            public void onTaskStackChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onTaskStackChanged, _data, null, Stub.TRANSACTION_onTaskStackChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onActivityPinned() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onActivityPinned, _data, null, Stub.TRANSACTION_onTaskStackChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onPinnedActivityRestartAttempt() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onPinnedActivityRestartAttempt, _data, null, Stub.TRANSACTION_onTaskStackChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onPinnedStackAnimationEnded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onPinnedStackAnimationEnded, _data, null, Stub.TRANSACTION_onTaskStackChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onActivityForcedResizable(String packageName, int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(taskId);
                    this.mRemote.transact(Stub.TRANSACTION_onActivityForcedResizable, _data, null, Stub.TRANSACTION_onTaskStackChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onActivityDismissingDockedStack() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onActivityDismissingDockedStack, _data, null, Stub.TRANSACTION_onTaskStackChanged);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITaskStackListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITaskStackListener)) {
                return new Proxy(obj);
            }
            return (ITaskStackListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onTaskStackChanged /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onTaskStackChanged();
                    return true;
                case TRANSACTION_onActivityPinned /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onActivityPinned();
                    return true;
                case TRANSACTION_onPinnedActivityRestartAttempt /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPinnedActivityRestartAttempt();
                    return true;
                case TRANSACTION_onPinnedStackAnimationEnded /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPinnedStackAnimationEnded();
                    return true;
                case TRANSACTION_onActivityForcedResizable /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    onActivityForcedResizable(data.readString(), data.readInt());
                    return true;
                case TRANSACTION_onActivityDismissingDockedStack /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    onActivityDismissingDockedStack();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onActivityDismissingDockedStack() throws RemoteException;

    void onActivityForcedResizable(String str, int i) throws RemoteException;

    void onActivityPinned() throws RemoteException;

    void onPinnedActivityRestartAttempt() throws RemoteException;

    void onPinnedStackAnimationEnded() throws RemoteException;

    void onTaskStackChanged() throws RemoteException;
}
