package android.hardware.soundtrigger;

import android.hardware.soundtrigger.SoundTrigger;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRecognitionStatusCallback extends IInterface {
    void onError(int i) throws RemoteException;

    void onGenericSoundTriggerDetected(SoundTrigger.GenericRecognitionEvent genericRecognitionEvent) throws RemoteException;

    void onKeyphraseDetected(SoundTrigger.KeyphraseRecognitionEvent keyphraseRecognitionEvent) throws RemoteException;

    void onRecognitionPaused() throws RemoteException;

    void onRecognitionResumed() throws RemoteException;

    public static class Default implements IRecognitionStatusCallback {
        @Override // android.hardware.soundtrigger.IRecognitionStatusCallback
        public void onKeyphraseDetected(SoundTrigger.KeyphraseRecognitionEvent recognitionEvent) throws RemoteException {
        }

        @Override // android.hardware.soundtrigger.IRecognitionStatusCallback
        public void onGenericSoundTriggerDetected(SoundTrigger.GenericRecognitionEvent recognitionEvent) throws RemoteException {
        }

        @Override // android.hardware.soundtrigger.IRecognitionStatusCallback
        public void onError(int status) throws RemoteException {
        }

        @Override // android.hardware.soundtrigger.IRecognitionStatusCallback
        public void onRecognitionPaused() throws RemoteException {
        }

        @Override // android.hardware.soundtrigger.IRecognitionStatusCallback
        public void onRecognitionResumed() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IRecognitionStatusCallback {
        private static final String DESCRIPTOR = "android.hardware.soundtrigger.IRecognitionStatusCallback";
        static final int TRANSACTION_onError = 3;
        static final int TRANSACTION_onGenericSoundTriggerDetected = 2;
        static final int TRANSACTION_onKeyphraseDetected = 1;
        static final int TRANSACTION_onRecognitionPaused = 4;
        static final int TRANSACTION_onRecognitionResumed = 5;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onKeyphraseDetected";
            }
            if (transactionCode == 2) {
                return "onGenericSoundTriggerDetected";
            }
            if (transactionCode == 3) {
                return "onError";
            }
            if (transactionCode == 4) {
                return "onRecognitionPaused";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "onRecognitionResumed";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            SoundTrigger.KeyphraseRecognitionEvent _arg0;
            SoundTrigger.GenericRecognitionEvent _arg02;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = SoundTrigger.KeyphraseRecognitionEvent.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onKeyphraseDetected(_arg0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = SoundTrigger.GenericRecognitionEvent.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                onGenericSoundTriggerDetected(_arg02);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onError(data.readInt());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onRecognitionPaused();
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                onRecognitionResumed();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IRecognitionStatusCallback {
            public static IRecognitionStatusCallback sDefaultImpl;
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

            @Override // android.hardware.soundtrigger.IRecognitionStatusCallback
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
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onKeyphraseDetected(recognitionEvent);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.soundtrigger.IRecognitionStatusCallback
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
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onGenericSoundTriggerDetected(recognitionEvent);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.soundtrigger.IRecognitionStatusCallback
            public void onError(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onError(status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.soundtrigger.IRecognitionStatusCallback
            public void onRecognitionPaused() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRecognitionPaused();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.soundtrigger.IRecognitionStatusCallback
            public void onRecognitionResumed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRecognitionResumed();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRecognitionStatusCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IRecognitionStatusCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
