package huawei.android.security;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IHwSystemManagerPlugin extends IInterface {
    void setStartComponetBlackList(List<String> list) throws RemoteException;

    int updateAddViewData(Bundle bundle, int i) throws RemoteException;

    public static class Default implements IHwSystemManagerPlugin {
        @Override // huawei.android.security.IHwSystemManagerPlugin
        public int updateAddViewData(Bundle data, int operation) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.IHwSystemManagerPlugin
        public void setStartComponetBlackList(List<String> list) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwSystemManagerPlugin {
        private static final String DESCRIPTOR = "huawei.android.security.IHwSystemManagerPlugin";
        static final int TRANSACTION_setStartComponetBlackList = 2;
        static final int TRANSACTION_updateAddViewData = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwSystemManagerPlugin asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwSystemManagerPlugin)) {
                return new Proxy(obj);
            }
            return (IHwSystemManagerPlugin) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                int _result = updateAddViewData(_arg0, data.readInt());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                setStartComponetBlackList(data.createStringArrayList());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwSystemManagerPlugin {
            public static IHwSystemManagerPlugin sDefaultImpl;
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

            @Override // huawei.android.security.IHwSystemManagerPlugin
            public int updateAddViewData(Bundle data, int operation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(operation);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateAddViewData(data, operation);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwSystemManagerPlugin
            public void setStartComponetBlackList(List<String> pkgs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgs);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setStartComponetBlackList(pkgs);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwSystemManagerPlugin impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwSystemManagerPlugin getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
