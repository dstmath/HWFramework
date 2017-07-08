package android.service.voice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVoiceInteractionService extends IInterface {

    public static abstract class Stub extends Binder implements IVoiceInteractionService {
        private static final String DESCRIPTOR = "android.service.voice.IVoiceInteractionService";
        static final int TRANSACTION_launchVoiceAssistFromKeyguard = 4;
        static final int TRANSACTION_ready = 1;
        static final int TRANSACTION_shutdown = 3;
        static final int TRANSACTION_soundModelsChanged = 2;

        private static class Proxy implements IVoiceInteractionService {
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

            public void ready() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_ready, _data, null, Stub.TRANSACTION_ready);
                } finally {
                    _data.recycle();
                }
            }

            public void soundModelsChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_soundModelsChanged, _data, null, Stub.TRANSACTION_ready);
                } finally {
                    _data.recycle();
                }
            }

            public void shutdown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_shutdown, _data, null, Stub.TRANSACTION_ready);
                } finally {
                    _data.recycle();
                }
            }

            public void launchVoiceAssistFromKeyguard() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_launchVoiceAssistFromKeyguard, _data, null, Stub.TRANSACTION_ready);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceInteractionService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceInteractionService)) {
                return new Proxy(obj);
            }
            return (IVoiceInteractionService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_ready /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    ready();
                    return true;
                case TRANSACTION_soundModelsChanged /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    soundModelsChanged();
                    return true;
                case TRANSACTION_shutdown /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    shutdown();
                    return true;
                case TRANSACTION_launchVoiceAssistFromKeyguard /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    launchVoiceAssistFromKeyguard();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void launchVoiceAssistFromKeyguard() throws RemoteException;

    void ready() throws RemoteException;

    void shutdown() throws RemoteException;

    void soundModelsChanged() throws RemoteException;
}
