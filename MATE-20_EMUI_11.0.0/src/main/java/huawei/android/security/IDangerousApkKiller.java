package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import huawei.android.security.IAppClientMonitor;
import huawei.android.security.IInspectAppObserver;
import java.util.Map;

public interface IDangerousApkKiller extends IInterface {
    Map getInspectAppMap() throws RemoteException;

    void onAppEvent(int i, int i2, int i3, String str, String str2) throws RemoteException;

    void onBehaviorEvent(int i, int i2, int i3) throws RemoteException;

    void regObservInspectUid(String str, IInspectAppObserver iInspectAppObserver) throws RemoteException;

    void registerAppClientMonitor(IAppClientMonitor iAppClientMonitor) throws RemoteException;

    public static abstract class Stub extends Binder implements IDangerousApkKiller {
        private static final String DESCRIPTOR = "huawei.android.security.IDangerousApkKiller";
        static final int TRANSACTION_getInspectAppMap = 1;
        static final int TRANSACTION_onAppEvent = 3;
        static final int TRANSACTION_onBehaviorEvent = 2;
        static final int TRANSACTION_regObservInspectUid = 4;
        static final int TRANSACTION_registerAppClientMonitor = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDangerousApkKiller asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDangerousApkKiller)) {
                return new Proxy(obj);
            }
            return (IDangerousApkKiller) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                Map _result = getInspectAppMap();
                reply.writeNoException();
                reply.writeMap(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onBehaviorEvent(data.readInt(), data.readInt(), data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onAppEvent(data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readString());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                regObservInspectUid(data.readString(), IInspectAppObserver.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                registerAppClientMonitor(IAppClientMonitor.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IDangerousApkKiller {
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

            @Override // huawei.android.security.IDangerousApkKiller
            public Map getInspectAppMap() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readHashMap(getClass().getClassLoader());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IDangerousApkKiller
            public void onBehaviorEvent(int uid, int pid, int behaviorId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(behaviorId);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IDangerousApkKiller
            public void onAppEvent(int event, int uid, int pid, String packageName, String installer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(event);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeString(packageName);
                    _data.writeString(installer);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IDangerousApkKiller
            public void regObservInspectUid(String key, IInspectAppObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IDangerousApkKiller
            public void registerAppClientMonitor(IAppClientMonitor appClientMonitor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(appClientMonitor != null ? appClientMonitor.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
