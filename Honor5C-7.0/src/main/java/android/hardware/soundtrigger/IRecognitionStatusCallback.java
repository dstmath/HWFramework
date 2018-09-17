package android.hardware.soundtrigger;

import android.hardware.soundtrigger.SoundTrigger.GenericRecognitionEvent;
import android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionEvent;
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

            public void onKeyphraseDetected(KeyphraseRecognitionEvent recognitionEvent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (recognitionEvent != null) {
                        _data.writeInt(Stub.TRANSACTION_onKeyphraseDetected);
                        recognitionEvent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onKeyphraseDetected, _data, null, Stub.TRANSACTION_onKeyphraseDetected);
                } finally {
                    _data.recycle();
                }
            }

            public void onGenericSoundTriggerDetected(GenericRecognitionEvent recognitionEvent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (recognitionEvent != null) {
                        _data.writeInt(Stub.TRANSACTION_onKeyphraseDetected);
                        recognitionEvent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onGenericSoundTriggerDetected, _data, null, Stub.TRANSACTION_onKeyphraseDetected);
                } finally {
                    _data.recycle();
                }
            }

            public void onError(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    this.mRemote.transact(Stub.TRANSACTION_onError, _data, null, Stub.TRANSACTION_onKeyphraseDetected);
                } finally {
                    _data.recycle();
                }
            }

            public void onRecognitionPaused() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onRecognitionPaused, _data, null, Stub.TRANSACTION_onKeyphraseDetected);
                } finally {
                    _data.recycle();
                }
            }

            public void onRecognitionResumed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onRecognitionResumed, _data, null, Stub.TRANSACTION_onKeyphraseDetected);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onKeyphraseDetected /*1*/:
                    KeyphraseRecognitionEvent keyphraseRecognitionEvent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        keyphraseRecognitionEvent = (KeyphraseRecognitionEvent) KeyphraseRecognitionEvent.CREATOR.createFromParcel(data);
                    } else {
                        keyphraseRecognitionEvent = null;
                    }
                    onKeyphraseDetected(keyphraseRecognitionEvent);
                    return true;
                case TRANSACTION_onGenericSoundTriggerDetected /*2*/:
                    GenericRecognitionEvent genericRecognitionEvent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        genericRecognitionEvent = (GenericRecognitionEvent) GenericRecognitionEvent.CREATOR.createFromParcel(data);
                    } else {
                        genericRecognitionEvent = null;
                    }
                    onGenericSoundTriggerDetected(genericRecognitionEvent);
                    return true;
                case TRANSACTION_onError /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onError(data.readInt());
                    return true;
                case TRANSACTION_onRecognitionPaused /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onRecognitionPaused();
                    return true;
                case TRANSACTION_onRecognitionResumed /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    onRecognitionResumed();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onError(int i) throws RemoteException;

    void onGenericSoundTriggerDetected(GenericRecognitionEvent genericRecognitionEvent) throws RemoteException;

    void onKeyphraseDetected(KeyphraseRecognitionEvent keyphraseRecognitionEvent) throws RemoteException;

    void onRecognitionPaused() throws RemoteException;

    void onRecognitionResumed() throws RemoteException;
}
