package android.speech.tts;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITextToSpeechCallback extends IInterface {
    void onAudioAvailable(String str, byte[] bArr) throws RemoteException;

    void onBeginSynthesis(String str, int i, int i2, int i3) throws RemoteException;

    void onError(String str, int i) throws RemoteException;

    void onRangeStart(String str, int i, int i2, int i3) throws RemoteException;

    void onStart(String str) throws RemoteException;

    void onStop(String str, boolean z) throws RemoteException;

    void onSuccess(String str) throws RemoteException;

    public static class Default implements ITextToSpeechCallback {
        @Override // android.speech.tts.ITextToSpeechCallback
        public void onStart(String utteranceId) throws RemoteException {
        }

        @Override // android.speech.tts.ITextToSpeechCallback
        public void onSuccess(String utteranceId) throws RemoteException {
        }

        @Override // android.speech.tts.ITextToSpeechCallback
        public void onStop(String utteranceId, boolean isStarted) throws RemoteException {
        }

        @Override // android.speech.tts.ITextToSpeechCallback
        public void onError(String utteranceId, int errorCode) throws RemoteException {
        }

        @Override // android.speech.tts.ITextToSpeechCallback
        public void onBeginSynthesis(String utteranceId, int sampleRateInHz, int audioFormat, int channelCount) throws RemoteException {
        }

        @Override // android.speech.tts.ITextToSpeechCallback
        public void onAudioAvailable(String utteranceId, byte[] audio) throws RemoteException {
        }

        @Override // android.speech.tts.ITextToSpeechCallback
        public void onRangeStart(String utteranceId, int start, int end, int frame) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITextToSpeechCallback {
        private static final String DESCRIPTOR = "android.speech.tts.ITextToSpeechCallback";
        static final int TRANSACTION_onAudioAvailable = 6;
        static final int TRANSACTION_onBeginSynthesis = 5;
        static final int TRANSACTION_onError = 4;
        static final int TRANSACTION_onRangeStart = 7;
        static final int TRANSACTION_onStart = 1;
        static final int TRANSACTION_onStop = 3;
        static final int TRANSACTION_onSuccess = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITextToSpeechCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITextToSpeechCallback)) {
                return new Proxy(obj);
            }
            return (ITextToSpeechCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onStart";
                case 2:
                    return "onSuccess";
                case 3:
                    return "onStop";
                case 4:
                    return "onError";
                case 5:
                    return "onBeginSynthesis";
                case 6:
                    return "onAudioAvailable";
                case 7:
                    return "onRangeStart";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onStart(data.readString());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onSuccess(data.readString());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onStop(data.readString(), data.readInt() != 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onError(data.readString(), data.readInt());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onBeginSynthesis(data.readString(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onAudioAvailable(data.readString(), data.createByteArray());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onRangeStart(data.readString(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ITextToSpeechCallback {
            public static ITextToSpeechCallback sDefaultImpl;
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

            @Override // android.speech.tts.ITextToSpeechCallback
            public void onStart(String utteranceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(utteranceId);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStart(utteranceId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.speech.tts.ITextToSpeechCallback
            public void onSuccess(String utteranceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(utteranceId);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSuccess(utteranceId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.speech.tts.ITextToSpeechCallback
            public void onStop(String utteranceId, boolean isStarted) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(utteranceId);
                    _data.writeInt(isStarted ? 1 : 0);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStop(utteranceId, isStarted);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.speech.tts.ITextToSpeechCallback
            public void onError(String utteranceId, int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(utteranceId);
                    _data.writeInt(errorCode);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onError(utteranceId, errorCode);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.speech.tts.ITextToSpeechCallback
            public void onBeginSynthesis(String utteranceId, int sampleRateInHz, int audioFormat, int channelCount) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(utteranceId);
                    _data.writeInt(sampleRateInHz);
                    _data.writeInt(audioFormat);
                    _data.writeInt(channelCount);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.speech.tts.ITextToSpeechCallback
            public void onAudioAvailable(String utteranceId, byte[] audio) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(utteranceId);
                    _data.writeByteArray(audio);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAudioAvailable(utteranceId, audio);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.speech.tts.ITextToSpeechCallback
            public void onRangeStart(String utteranceId, int start, int end, int frame) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(utteranceId);
                    _data.writeInt(start);
                    _data.writeInt(end);
                    _data.writeInt(frame);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRangeStart(utteranceId, start, end, frame);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITextToSpeechCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITextToSpeechCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
