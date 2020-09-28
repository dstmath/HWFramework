package huawei.android.security.voicerecognition;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVoiceRecognizeServiceReceiver extends IInterface {
    void onOptCallback(int i, int i2, int i3, int i4) throws RemoteException;

    public static class Default implements IVoiceRecognizeServiceReceiver {
        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeServiceReceiver
        public void onOptCallback(int type, int code, int subCode1, int subCode2) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVoiceRecognizeServiceReceiver {
        private static final String DESCRIPTOR = "huawei.android.security.voicerecognition.IVoiceRecognizeServiceReceiver";
        static final int TRANSACTION_onOptCallback = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceRecognizeServiceReceiver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceRecognizeServiceReceiver)) {
                return new Proxy(obj);
            }
            return (IVoiceRecognizeServiceReceiver) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onOptCallback(data.readInt(), data.readInt(), data.readInt(), data.readInt());
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
        public static class Proxy implements IVoiceRecognizeServiceReceiver {
            public static IVoiceRecognizeServiceReceiver sDefaultImpl;
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

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeServiceReceiver
            public void onOptCallback(int type, int code, int subCode1, int subCode2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(code);
                    _data.writeInt(subCode1);
                    _data.writeInt(subCode2);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onOptCallback(type, code, subCode1, subCode2);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVoiceRecognizeServiceReceiver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVoiceRecognizeServiceReceiver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
