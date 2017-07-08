package android.content;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IIntentSender extends IInterface {

    public static abstract class Stub extends Binder implements IIntentSender {
        private static final String DESCRIPTOR = "android.content.IIntentSender";
        static final int TRANSACTION_send = 1;

        private static class Proxy implements IIntentSender {
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

            public void send(int code, Intent intent, String resolvedType, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(code);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_send);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    if (finishedReceiver != null) {
                        iBinder = finishedReceiver.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(requiredPermission);
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_send);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_send, _data, null, Stub.TRANSACTION_send);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIntentSender asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIntentSender)) {
                return new Proxy(obj);
            }
            return (IIntentSender) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_send /*1*/:
                    Intent intent;
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    String _arg2 = data.readString();
                    IIntentReceiver _arg3 = android.content.IIntentReceiver.Stub.asInterface(data.readStrongBinder());
                    String _arg4 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    send(_arg0, intent, _arg2, _arg3, _arg4, bundle);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void send(int i, Intent intent, String str, IIntentReceiver iIntentReceiver, String str2, Bundle bundle) throws RemoteException;
}
