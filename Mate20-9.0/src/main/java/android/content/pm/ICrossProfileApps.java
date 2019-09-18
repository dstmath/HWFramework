package android.content.pm;

import android.app.IApplicationThread;
import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
import java.util.List;

public interface ICrossProfileApps extends IInterface {

    public static abstract class Stub extends Binder implements ICrossProfileApps {
        private static final String DESCRIPTOR = "android.content.pm.ICrossProfileApps";
        static final int TRANSACTION_getTargetUserProfiles = 2;
        static final int TRANSACTION_startActivityAsUser = 1;

        private static class Proxy implements ICrossProfileApps {
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

            public void startActivityAsUser(IApplicationThread caller, String callingPackage, ComponentName component, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(callingPackage);
                    if (component != null) {
                        _data.writeInt(1);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<UserHandle> getTargetUserProfiles(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(UserHandle.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICrossProfileApps asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICrossProfileApps)) {
                return new Proxy(obj);
            }
            return (ICrossProfileApps) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg2;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg0 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        String _arg1 = data.readString();
                        UserHandle _arg3 = null;
                        if (data.readInt() != 0) {
                            _arg2 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                        }
                        startActivityAsUser(_arg0, _arg1, _arg2, _arg3);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        List<UserHandle> _result = getTargetUserProfiles(data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result);
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

    List<UserHandle> getTargetUserProfiles(String str) throws RemoteException;

    void startActivityAsUser(IApplicationThread iApplicationThread, String str, ComponentName componentName, UserHandle userHandle) throws RemoteException;
}
