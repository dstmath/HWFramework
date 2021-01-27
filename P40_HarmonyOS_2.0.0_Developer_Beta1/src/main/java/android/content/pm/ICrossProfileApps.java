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
    List<UserHandle> getTargetUserProfiles(String str) throws RemoteException;

    void startActivityAsUser(IApplicationThread iApplicationThread, String str, ComponentName componentName, int i, boolean z) throws RemoteException;

    public static class Default implements ICrossProfileApps {
        @Override // android.content.pm.ICrossProfileApps
        public void startActivityAsUser(IApplicationThread caller, String callingPackage, ComponentName component, int userId, boolean launchMainActivity) throws RemoteException {
        }

        @Override // android.content.pm.ICrossProfileApps
        public List<UserHandle> getTargetUserProfiles(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICrossProfileApps {
        private static final String DESCRIPTOR = "android.content.pm.ICrossProfileApps";
        static final int TRANSACTION_getTargetUserProfiles = 2;
        static final int TRANSACTION_startActivityAsUser = 1;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "startActivityAsUser";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "getTargetUserProfiles";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg2;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IApplicationThread _arg0 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                String _arg1 = data.readString();
                if (data.readInt() != 0) {
                    _arg2 = ComponentName.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                startActivityAsUser(_arg0, _arg1, _arg2, data.readInt(), data.readInt() != 0);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                List<UserHandle> _result = getTargetUserProfiles(data.readString());
                reply.writeNoException();
                reply.writeTypedList(_result);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ICrossProfileApps {
            public static ICrossProfileApps sDefaultImpl;
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

            @Override // android.content.pm.ICrossProfileApps
            public void startActivityAsUser(IApplicationThread caller, String callingPackage, ComponentName component, int userId, boolean launchMainActivity) throws RemoteException {
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
                    _data.writeInt(userId);
                    _data.writeInt(launchMainActivity ? 1 : 0);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startActivityAsUser(caller, callingPackage, component, userId, launchMainActivity);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.ICrossProfileApps
            public List<UserHandle> getTargetUserProfiles(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTargetUserProfiles(callingPackage);
                    }
                    _reply.readException();
                    List<UserHandle> _result = _reply.createTypedArrayList(UserHandle.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICrossProfileApps impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICrossProfileApps getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
