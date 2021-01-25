package android.service.chooser;

import android.content.ComponentName;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.service.chooser.IChooserTargetResult;

public interface IChooserTargetService extends IInterface {
    void getChooserTargets(ComponentName componentName, IntentFilter intentFilter, IChooserTargetResult iChooserTargetResult) throws RemoteException;

    public static class Default implements IChooserTargetService {
        @Override // android.service.chooser.IChooserTargetService
        public void getChooserTargets(ComponentName targetComponentName, IntentFilter matchedFilter, IChooserTargetResult result) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IChooserTargetService {
        private static final String DESCRIPTOR = "android.service.chooser.IChooserTargetService";
        static final int TRANSACTION_getChooserTargets = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IChooserTargetService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IChooserTargetService)) {
                return new Proxy(obj);
            }
            return (IChooserTargetService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "getChooserTargets";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg0;
            IntentFilter _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ComponentName.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                if (data.readInt() != 0) {
                    _arg1 = IntentFilter.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                getChooserTargets(_arg0, _arg1, IChooserTargetResult.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IChooserTargetService {
            public static IChooserTargetService sDefaultImpl;
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

            @Override // android.service.chooser.IChooserTargetService
            public void getChooserTargets(ComponentName targetComponentName, IntentFilter matchedFilter, IChooserTargetResult result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (targetComponentName != null) {
                        _data.writeInt(1);
                        targetComponentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (matchedFilter != null) {
                        _data.writeInt(1);
                        matchedFilter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(result != null ? result.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getChooserTargets(targetComponentName, matchedFilter, result);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IChooserTargetService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IChooserTargetService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
