package ohos.ai.tts;

import java.util.Optional;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.utils.PacMap;

public interface TtsPluginListener extends IRemoteBroker {
    public static final String DESCRIPTOR = "com.huawei.hiai.pdk.interfaces.tts.ITtsListener";

    void onError(String str, String str2) throws RemoteException;

    void onEvent(int i, PacMap pacMap) throws RemoteException;

    void onFinish(String str) throws RemoteException;

    void onProgress(String str, byte[] bArr, int i) throws RemoteException;

    void onSpeechFinish(String str) throws RemoteException;

    void onSpeechProgressChanged(String str, int i) throws RemoteException;

    void onSpeechStart(String str) throws RemoteException;

    void onStart(String str) throws RemoteException;

    public static abstract class Stub extends RemoteObject implements TtsPluginListener {
        static final int TRANSACTION_ON_ERROR = 4;
        static final int TRANSACTION_ON_EVENT = 8;
        static final int TRANSACTION_ON_FINISH = 3;
        static final int TRANSACTION_ON_PROGRESS = 2;
        static final int TRANSACTION_ON_SPEECH_FINISH = 7;
        static final int TRANSACTION_ON_SPEECH_PROGRESS_CHANGED = 6;
        static final int TRANSACTION_ON_SPEECH_START = 5;
        static final int TRANSACTION_ON_START = 1;

        public IRemoteObject asObject() {
            return this;
        }

        public Stub(String str) {
            super(str);
        }

        public static Optional<TtsPluginListener> asInterface(IRemoteObject iRemoteObject) {
            if (iRemoteObject == null) {
                return Optional.empty();
            }
            TtsPluginListener queryLocalInterface = iRemoteObject.queryLocalInterface(TtsPluginListener.DESCRIPTOR);
            if (queryLocalInterface instanceof TtsPluginListener) {
                return Optional.of(queryLocalInterface);
            }
            return Optional.of(new Proxy(iRemoteObject));
        }

        public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
            byte[] bArr;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        messageParcel.readInterfaceToken();
                        onStart(messageParcel.readString());
                        messageParcel2.writeInt(0);
                        return true;
                    case 2:
                        messageParcel.readInterfaceToken();
                        String readString = messageParcel.readString();
                        int readInt = messageParcel.readInt();
                        if (readInt < 0) {
                            bArr = null;
                        } else {
                            bArr = new byte[readInt];
                        }
                        onProgress(readString, bArr, messageParcel.readInt());
                        messageParcel2.writeInt(0);
                        messageParcel2.writeByteArray(bArr);
                        return true;
                    case 3:
                        messageParcel.readInterfaceToken();
                        onFinish(messageParcel.readString());
                        messageParcel2.writeInt(0);
                        return true;
                    case 4:
                        messageParcel.readInterfaceToken();
                        onError(messageParcel.readString(), messageParcel.readString());
                        messageParcel2.writeInt(0);
                        return true;
                    case 5:
                        messageParcel.readInterfaceToken();
                        onSpeechStart(messageParcel.readString());
                        messageParcel2.writeInt(0);
                        return true;
                    case 6:
                        messageParcel.readInterfaceToken();
                        onSpeechProgressChanged(messageParcel.readString(), messageParcel.readInt());
                        messageParcel2.writeInt(0);
                        return true;
                    case 7:
                        messageParcel.readInterfaceToken();
                        onSpeechFinish(messageParcel.readString());
                        messageParcel2.writeInt(0);
                        return true;
                    case 8:
                        messageParcel.readInterfaceToken();
                        int readInt2 = messageParcel.readInt();
                        PacMap pacMap = new PacMap();
                        onEvent(readInt2, pacMap);
                        messageParcel2.writeInt(0);
                        messageParcel2.writeInt(1);
                        pacMap.marshalling(messageParcel2);
                        return true;
                    default:
                        return TtsPluginListener.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
                }
            } else {
                messageParcel2.writeString(TtsPluginListener.DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements TtsPluginListener {
            private IRemoteObject mRemote;

            Proxy(IRemoteObject iRemoteObject) {
                this.mRemote = iRemoteObject;
            }

            public IRemoteObject asObject() {
                return this.mRemote;
            }

            @Override // ohos.ai.tts.TtsPluginListener
            public void onStart(String str) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(TtsPluginListener.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.sendRequest(1, obtain, obtain2, messageOption);
                    obtain2.readInt();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginListener
            public void onProgress(String str, byte[] bArr, int i) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                try {
                    obtain.writeInterfaceToken(TtsPluginListener.DESCRIPTOR);
                    obtain.writeString(str);
                    if (bArr == null) {
                        obtain.writeInt(-1);
                    } else {
                        obtain.writeInt(bArr.length);
                    }
                    obtain.writeInt(i);
                    this.mRemote.sendRequest(2, obtain, obtain2, new MessageOption());
                    obtain2.readInt();
                    if (bArr != null) {
                        obtain2.readByteArray(bArr);
                        return;
                    }
                    throw new RemoteException("Bad array lengths");
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginListener
            public void onFinish(String str) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(TtsPluginListener.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.sendRequest(3, obtain, obtain2, messageOption);
                    obtain2.readInt();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginListener
            public void onError(String str, String str2) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(TtsPluginListener.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    this.mRemote.sendRequest(4, obtain, obtain2, messageOption);
                    obtain2.readInt();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginListener
            public void onEvent(int i, PacMap pacMap) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(TtsPluginListener.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.sendRequest(8, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    if (obtain2.readInt() != 0) {
                        pacMap.unmarshalling(obtain2);
                    }
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginListener
            public void onSpeechStart(String str) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(TtsPluginListener.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.sendRequest(5, obtain, obtain2, messageOption);
                    obtain2.readInt();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginListener
            public void onSpeechProgressChanged(String str, int i) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(TtsPluginListener.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    this.mRemote.sendRequest(6, obtain, obtain2, messageOption);
                    obtain2.readInt();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginListener
            public void onSpeechFinish(String str) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(TtsPluginListener.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.sendRequest(7, obtain, obtain2, messageOption);
                    obtain2.readInt();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }
        }
    }
}
