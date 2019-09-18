package huawei.android.security.voicerecognition;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVoiceRecognizeServiceReceiver extends IInterface {

    public static abstract class Stub extends Binder implements IVoiceRecognizeServiceReceiver {
        private static final String DESCRIPTOR = "huawei.android.security.voicerecognition.IVoiceRecognizeServiceReceiver";
        static final int TRANSACTION_onOptCallback = 1;

        private static class Proxy implements IVoiceRecognizeServiceReceiver {
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

            public void onOptCallback(int type, int code, int subCode1, int subCode2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(code);
                    _data.writeInt(subCode1);
                    _data.writeInt(subCode2);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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
    }

    void onOptCallback(int i, int i2, int i3, int i4) throws RemoteException;
}
