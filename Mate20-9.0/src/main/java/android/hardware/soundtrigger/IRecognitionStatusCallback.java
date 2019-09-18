package android.hardware.soundtrigger;

import android.hardware.soundtrigger.SoundTrigger;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRecognitionStatusCallback extends IInterface {

    public static abstract class Stub extends Binder implements IRecognitionStatusCallback {
        private static final String DESCRIPTOR = "android.hardware.soundtrigger.IRecognitionStatusCallback";
        static final int TRANSACTION_onError = 3;
        static final int TRANSACTION_onGenericSoundTriggerDetected = 2;
        static final int TRANSACTION_onKeyphraseDetected = 1;
        static final int TRANSACTION_onRecognitionPaused = 4;
        static final int TRANSACTION_onRecognitionResumed = 5;

        private static class Proxy implements IRecognitionStatusCallback {
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

            public void onKeyphraseDetected(SoundTrigger.KeyphraseRecognitionEvent recognitionEvent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (recognitionEvent != null) {
                        _data.writeInt(1);
                        recognitionEvent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onGenericSoundTriggerDetected(SoundTrigger.GenericRecognitionEvent recognitionEvent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (recognitionEvent != null) {
                        _data.writeInt(1);
                        recognitionEvent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onError(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onRecognitionPaused() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onRecognitionResumed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRecognitionStatusCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRecognitionStatusCallback)) {
                return new Proxy(obj);
            }
            return (IRecognitionStatusCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: android.hardware.soundtrigger.SoundTrigger$KeyphraseRecognitionEvent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v6, resolved type: android.hardware.soundtrigger.SoundTrigger$GenericRecognitionEvent} */
        /* JADX WARNING: type inference failed for: r1v1 */
        /* JADX WARNING: type inference failed for: r1v12 */
        /* JADX WARNING: type inference failed for: r1v13 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                ? _arg0 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = SoundTrigger.KeyphraseRecognitionEvent.CREATOR.createFromParcel(data);
                        }
                        onKeyphraseDetected(_arg0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = SoundTrigger.GenericRecognitionEvent.CREATOR.createFromParcel(data);
                        }
                        onGenericSoundTriggerDetected(_arg0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onError(data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onRecognitionPaused();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onRecognitionResumed();
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

    void onError(int i) throws RemoteException;

    void onGenericSoundTriggerDetected(SoundTrigger.GenericRecognitionEvent genericRecognitionEvent) throws RemoteException;

    void onKeyphraseDetected(SoundTrigger.KeyphraseRecognitionEvent keyphraseRecognitionEvent) throws RemoteException;

    void onRecognitionPaused() throws RemoteException;

    void onRecognitionResumed() throws RemoteException;
}
