package android.service.chooser;

import android.content.ComponentName;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IChooserTargetService extends IInterface {

    public static abstract class Stub extends Binder implements IChooserTargetService {
        private static final String DESCRIPTOR = "android.service.chooser.IChooserTargetService";
        static final int TRANSACTION_getChooserTargets = 1;

        private static class Proxy implements IChooserTargetService {
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

            public void getChooserTargets(ComponentName targetComponentName, IntentFilter matchedFilter, IChooserTargetResult result) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (targetComponentName != null) {
                        _data.writeInt(Stub.TRANSACTION_getChooserTargets);
                        targetComponentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (matchedFilter != null) {
                        _data.writeInt(Stub.TRANSACTION_getChooserTargets);
                        matchedFilter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (result != null) {
                        iBinder = result.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_getChooserTargets, _data, null, Stub.TRANSACTION_getChooserTargets);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_getChooserTargets /*1*/:
                    ComponentName componentName;
                    IntentFilter intentFilter;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    if (data.readInt() != 0) {
                        intentFilter = (IntentFilter) IntentFilter.CREATOR.createFromParcel(data);
                    } else {
                        intentFilter = null;
                    }
                    getChooserTargets(componentName, intentFilter, android.service.chooser.IChooserTargetResult.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void getChooserTargets(ComponentName componentName, IntentFilter intentFilter, IChooserTargetResult iChooserTargetResult) throws RemoteException;
}
