package android.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IInputFilter extends IInterface {

    public static abstract class Stub extends Binder implements IInputFilter {
        private static final String DESCRIPTOR = "android.view.IInputFilter";
        static final int TRANSACTION_filterInputEvent = 3;
        static final int TRANSACTION_install = 1;
        static final int TRANSACTION_uninstall = 2;

        private static class Proxy implements IInputFilter {
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

            public void install(IInputFilterHost host) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (host != null) {
                        iBinder = host.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_install, _data, null, Stub.TRANSACTION_install);
                } finally {
                    _data.recycle();
                }
            }

            public void uninstall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_uninstall, _data, null, Stub.TRANSACTION_install);
                } finally {
                    _data.recycle();
                }
            }

            public void filterInputEvent(InputEvent event, int policyFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(Stub.TRANSACTION_install);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(policyFlags);
                    this.mRemote.transact(Stub.TRANSACTION_filterInputEvent, _data, null, Stub.TRANSACTION_install);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInputFilter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInputFilter)) {
                return new Proxy(obj);
            }
            return (IInputFilter) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_install /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    install(android.view.IInputFilterHost.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_uninstall /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    uninstall();
                    return true;
                case TRANSACTION_filterInputEvent /*3*/:
                    InputEvent inputEvent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        inputEvent = (InputEvent) InputEvent.CREATOR.createFromParcel(data);
                    } else {
                        inputEvent = null;
                    }
                    filterInputEvent(inputEvent, data.readInt());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void filterInputEvent(InputEvent inputEvent, int i) throws RemoteException;

    void install(IInputFilterHost iInputFilterHost) throws RemoteException;

    void uninstall() throws RemoteException;
}
