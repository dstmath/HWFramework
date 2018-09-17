package android.service.chooser;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IChooserTargetResult extends IInterface {

    public static abstract class Stub extends Binder implements IChooserTargetResult {
        private static final String DESCRIPTOR = "android.service.chooser.IChooserTargetResult";
        static final int TRANSACTION_sendResult = 1;

        private static class Proxy implements IChooserTargetResult {
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

            public void sendResult(List<ChooserTarget> targets) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(targets);
                    this.mRemote.transact(Stub.TRANSACTION_sendResult, _data, null, Stub.TRANSACTION_sendResult);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IChooserTargetResult asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IChooserTargetResult)) {
                return new Proxy(obj);
            }
            return (IChooserTargetResult) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_sendResult /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    sendResult(data.createTypedArrayList(ChooserTarget.CREATOR));
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void sendResult(List<ChooserTarget> list) throws RemoteException;
}
