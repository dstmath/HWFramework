package android.speech;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.speech.IRecognitionListener;

public interface IRecognitionService extends IInterface {

    public static abstract class Stub extends Binder implements IRecognitionService {
        private static final String DESCRIPTOR = "android.speech.IRecognitionService";
        static final int TRANSACTION_cancel = 3;
        static final int TRANSACTION_startListening = 1;
        static final int TRANSACTION_stopListening = 2;

        private static class Proxy implements IRecognitionService {
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

            public void startListening(Intent recognizerIntent, IRecognitionListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (recognizerIntent != null) {
                        _data.writeInt(1);
                        recognizerIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void stopListening(IRecognitionListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void cancel(IRecognitionListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRecognitionService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRecognitionService)) {
                return new Proxy(obj);
            }
            return (IRecognitionService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Intent _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Intent) Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        startListening(_arg0, IRecognitionListener.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        stopListening(IRecognitionListener.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        cancel(IRecognitionListener.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void cancel(IRecognitionListener iRecognitionListener) throws RemoteException;

    void startListening(Intent intent, IRecognitionListener iRecognitionListener) throws RemoteException;

    void stopListening(IRecognitionListener iRecognitionListener) throws RemoteException;
}
