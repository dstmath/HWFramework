package android.media;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAudioFocusChangeDispatcher extends IInterface {
    void dispatchAudioFocusChange(AudioAttributes audioAttributes, String str, int i, boolean z) throws RemoteException;

    public static class Default implements IAudioFocusChangeDispatcher {
        @Override // android.media.IAudioFocusChangeDispatcher
        public void dispatchAudioFocusChange(AudioAttributes attributes, String clientId, int focusType, boolean action) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAudioFocusChangeDispatcher {
        private static final String DESCRIPTOR = "android.media.IAudioFocusChangeDispatcher";
        static final int TRANSACTION_dispatchAudioFocusChange = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAudioFocusChangeDispatcher asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAudioFocusChangeDispatcher)) {
                return new Proxy(obj);
            }
            return (IAudioFocusChangeDispatcher) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "dispatchAudioFocusChange";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            AudioAttributes _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = AudioAttributes.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                dispatchAudioFocusChange(_arg0, data.readString(), data.readInt(), data.readInt() != 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAudioFocusChangeDispatcher {
            public static IAudioFocusChangeDispatcher sDefaultImpl;
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

            @Override // android.media.IAudioFocusChangeDispatcher
            public void dispatchAudioFocusChange(AudioAttributes attributes, String clientId, int focusType, boolean action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
                    if (attributes != null) {
                        _data.writeInt(1);
                        attributes.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(clientId);
                    _data.writeInt(focusType);
                    if (action) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispatchAudioFocusChange(attributes, clientId, focusType, action);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAudioFocusChangeDispatcher impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAudioFocusChangeDispatcher getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
