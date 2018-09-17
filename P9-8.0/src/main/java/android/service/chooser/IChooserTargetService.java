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
                    if (result != null) {
                        iBinder = result.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, null, 1);
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
                case 1:
                    ComponentName _arg0;
                    IntentFilter _arg1;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg1 = (IntentFilter) IntentFilter.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    getChooserTargets(_arg0, _arg1, android.service.chooser.IChooserTargetResult.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void getChooserTargets(ComponentName componentName, IntentFilter intentFilter, IChooserTargetResult iChooserTargetResult) throws RemoteException;
}
