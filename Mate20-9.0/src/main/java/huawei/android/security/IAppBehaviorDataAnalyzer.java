package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import huawei.android.security.IInspectAppObserver;
import java.util.Map;

public interface IAppBehaviorDataAnalyzer extends IInterface {

    public static abstract class Stub extends Binder implements IAppBehaviorDataAnalyzer {
        private static final String DESCRIPTOR = "huawei.android.security.IAppBehaviorDataAnalyzer";
        static final int TRANSACTION_getInspectAppMap = 1;
        static final int TRANSACTION_onAppEvent = 3;
        static final int TRANSACTION_onBehaviorEvent = 2;
        static final int TRANSACTION_regObservInspectUid = 4;

        private static class Proxy implements IAppBehaviorDataAnalyzer {
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAppBehaviorDataAnalyzer asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAppBehaviorDataAnalyzer)) {
                return new Proxy(obj);
            }
            return (IAppBehaviorDataAnalyzer) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        Map _result = getInspectAppMap();
                        reply.writeNoException();
                        parcel2.writeMap(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        onBehaviorEvent(data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        onAppEvent(data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readString());
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        regObservInspectUid(data.readString(), IInspectAppObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    Map getInspectAppMap() throws RemoteException;

    void onAppEvent(int i, int i2, int i3, String str, String str2) throws RemoteException;

    void onBehaviorEvent(int i, int i2, int i3) throws RemoteException;

    void regObservInspectUid(String str, IInspectAppObserver iInspectAppObserver) throws RemoteException;
}
