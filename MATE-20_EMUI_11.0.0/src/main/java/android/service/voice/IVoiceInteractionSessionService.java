package android.service.voice;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVoiceInteractionSessionService extends IInterface {
    void newSession(IBinder iBinder, Bundle bundle, int i) throws RemoteException;

    public static class Default implements IVoiceInteractionSessionService {
        @Override // android.service.voice.IVoiceInteractionSessionService
        public void newSession(IBinder token, Bundle args, int startFlags) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVoiceInteractionSessionService {
        private static final String DESCRIPTOR = "android.service.voice.IVoiceInteractionSessionService";
        static final int TRANSACTION_newSession = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceInteractionSessionService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceInteractionSessionService)) {
                return new Proxy(obj);
            }
            return (IVoiceInteractionSessionService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "newSession";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IBinder _arg0 = data.readStrongBinder();
                if (data.readInt() != 0) {
                    _arg1 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                newSession(_arg0, _arg1, data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IVoiceInteractionSessionService {
            public static IVoiceInteractionSessionService sDefaultImpl;
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

            @Override // android.service.voice.IVoiceInteractionSessionService
            public void newSession(IBinder token, Bundle args, int startFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(startFlags);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().newSession(token, args, startFlags);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVoiceInteractionSessionService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVoiceInteractionSessionService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
