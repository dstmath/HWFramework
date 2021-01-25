package ohos.ai.tts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.ai.tts.TtsPluginListener;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public interface TtsPluginService extends IRemoteBroker {
    long getSupportMaxLength(TtsPluginListener ttsPluginListener) throws RemoteException;

    String getVersion(TtsPluginListener ttsPluginListener) throws RemoteException;

    boolean init(InitParams initParams, TtsPluginListener ttsPluginListener) throws RemoteException;

    boolean initOnAppStart(TtsPluginListener ttsPluginListener) throws RemoteException;

    boolean isSpeaking(TtsPluginListener ttsPluginListener) throws RemoteException;

    void release(TtsPluginListener ttsPluginListener) throws RemoteException;

    boolean setAudioType(int i, TtsPluginListener ttsPluginListener) throws RemoteException;

    void setIsSaveTtsData(boolean z, TtsPluginListener ttsPluginListener) throws RemoteException;

    boolean setParams(InitParams initParams, TtsPluginListener ttsPluginListener) throws RemoteException;

    void speakBatchText(List<String> list, String str, TtsPluginListener ttsPluginListener) throws RemoteException;

    void speakLongText(String str, String str2, TtsPluginListener ttsPluginListener) throws RemoteException;

    void speakText(String str, String str2, TtsPluginListener ttsPluginListener) throws RemoteException;

    void stopSpeak(TtsPluginListener ttsPluginListener) throws RemoteException;

    public static abstract class Stub extends RemoteObject implements TtsPluginService {
        private static final String DESCRIPTOR = "com.huawei.hiai.pdk.interfaces.tts.ITtsService";
        static final int TRANSACTION_GET_SUPPORT_MAX_LENGTH = 19;
        static final int TRANSACTION_GET_VERSION = 9;
        static final int TRANSACTION_INIT = 2;
        static final int TRANSACTION_INIT_ON_APP_START = 1;
        static final int TRANSACTION_IS_SPEAKING = 10;
        static final int TRANSACTION_RELEASE = 8;
        static final int TRANSACTION_SET_AUDIO_TYPE = 12;
        static final int TRANSACTION_SET_IS_SAVE_TTS_DATA = 3;
        static final int TRANSACTION_SET_PARAMS = 4;
        static final int TRANSACTION_SPEAKER_LONG_TEXT = 20;
        static final int TRANSACTION_SPEAK_BATCH_TEXT = 6;
        static final int TRANSACTION_SPEAK_TEXT = 5;
        static final int TRANSACTION_STOP_SPEAK = 7;

        public IRemoteObject asObject() {
            return this;
        }

        public Stub(String str) {
            super(str);
        }

        public static Optional<TtsPluginService> asInterface(IRemoteObject iRemoteObject) {
            if (iRemoteObject == null) {
                return Optional.empty();
            }
            TtsPluginService queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface instanceof TtsPluginService) {
                return Optional.of(queryLocalInterface);
            }
            return Optional.of(new Proxy(iRemoteObject));
        }

        public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
            ArrayList arrayList;
            if (i == 12) {
                messageParcel.readInterfaceToken();
                boolean audioType = setAudioType(messageParcel.readInt(), TtsPluginListener.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                messageParcel2.writeInt(0);
                messageParcel2.writeInt(audioType ? 1 : 0);
                return true;
            } else if (i == 1598968902) {
                messageParcel2.writeString(DESCRIPTOR);
                return true;
            } else if (i == 19) {
                messageParcel.readInterfaceToken();
                long supportMaxLength = getSupportMaxLength(TtsPluginListener.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                messageParcel2.writeInt(0);
                messageParcel2.writeLong(supportMaxLength);
                return true;
            } else if (i != 20) {
                switch (i) {
                    case 1:
                        messageParcel.readInterfaceToken();
                        boolean initOnAppStart = initOnAppStart(TtsPluginListener.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        messageParcel2.writeInt(initOnAppStart ? 1 : 0);
                        return true;
                    case 2:
                        messageParcel.readInterfaceToken();
                        boolean init = init(messageParcel.readInt() != 0 ? new InitParams(messageParcel) : null, TtsPluginListener.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        messageParcel2.writeInt(init ? 1 : 0);
                        return true;
                    case 3:
                        messageParcel.readInterfaceToken();
                        setIsSaveTtsData(messageParcel.readInt() != 0, TtsPluginListener.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        return true;
                    case 4:
                        messageParcel.readInterfaceToken();
                        boolean params = setParams(messageParcel.readInt() != 0 ? new InitParams(messageParcel) : null, TtsPluginListener.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        messageParcel2.writeInt(params ? 1 : 0);
                        return true;
                    case 5:
                        messageParcel.readInterfaceToken();
                        speakText(messageParcel.readString(), messageParcel.readString(), TtsPluginListener.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        return true;
                    case 6:
                        messageParcel.readInterfaceToken();
                        int readInt = messageParcel.readInt();
                        if (readInt < 0) {
                            arrayList = null;
                        } else {
                            arrayList = new ArrayList(readInt);
                            while (readInt > 0) {
                                arrayList.add(messageParcel.readString());
                                readInt--;
                            }
                        }
                        speakBatchText(arrayList, messageParcel.readString(), TtsPluginListener.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        return true;
                    case 7:
                        messageParcel.readInterfaceToken();
                        stopSpeak(TtsPluginListener.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        return true;
                    case 8:
                        messageParcel.readInterfaceToken();
                        release(TtsPluginListener.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        return true;
                    case 9:
                        messageParcel.readInterfaceToken();
                        String version = getVersion(TtsPluginListener.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        messageParcel2.writeString(version);
                        return true;
                    case 10:
                        messageParcel.readInterfaceToken();
                        boolean isSpeaking = isSpeaking(TtsPluginListener.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        messageParcel2.writeInt(isSpeaking ? 1 : 0);
                        return true;
                    default:
                        return TtsPluginService.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
                }
            } else {
                messageParcel.readInterfaceToken();
                speakLongText(messageParcel.readString(), messageParcel.readString(), TtsPluginListener.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                messageParcel2.writeInt(0);
                return true;
            }
        }

        private static class Proxy implements TtsPluginService {
            private IRemoteObject mRemote;

            Proxy(IRemoteObject iRemoteObject) {
                this.mRemote = iRemoteObject;
            }

            public IRemoteObject asObject() {
                return this.mRemote;
            }

            @Override // ohos.ai.tts.TtsPluginService
            public boolean initOnAppStart(TtsPluginListener ttsPluginListener) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeRemoteObject(ttsPluginListener != null ? ttsPluginListener.asObject() : null);
                    boolean z = true;
                    this.mRemote.sendRequest(1, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginService
            public boolean init(InitParams initParams, TtsPluginListener ttsPluginListener) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = true;
                    if (initParams != null) {
                        obtain.writeInt(1);
                        initParams.marshalling(obtain);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeRemoteObject(ttsPluginListener != null ? ttsPluginListener.asObject() : null);
                    this.mRemote.sendRequest(2, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginService
            public void setIsSaveTtsData(boolean z, TtsPluginListener ttsPluginListener) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(z ? 1 : 0);
                    obtain.writeRemoteObject(ttsPluginListener != null ? ttsPluginListener.asObject() : null);
                    this.mRemote.sendRequest(3, obtain, obtain2, messageOption);
                    obtain2.readInt();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginService
            public boolean setParams(InitParams initParams, TtsPluginListener ttsPluginListener) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = true;
                    if (initParams != null) {
                        obtain.writeInt(1);
                        initParams.marshalling(obtain);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeRemoteObject(ttsPluginListener != null ? ttsPluginListener.asObject() : null);
                    this.mRemote.sendRequest(4, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginService
            public void speakText(String str, String str2, TtsPluginListener ttsPluginListener) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeRemoteObject(ttsPluginListener != null ? ttsPluginListener.asObject() : null);
                    this.mRemote.sendRequest(5, obtain, obtain2, new MessageOption());
                    obtain2.readInt();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginService
            public void speakBatchText(List<String> list, String str, TtsPluginListener ttsPluginListener) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (list == null) {
                        obtain.writeInt(-1);
                    } else {
                        int size = list.size();
                        obtain.writeInt(size);
                        for (int i = 0; i < size; i++) {
                            obtain.writeString(list.get(i));
                        }
                    }
                    obtain.writeString(str);
                    obtain.writeRemoteObject(ttsPluginListener != null ? ttsPluginListener.asObject() : null);
                    this.mRemote.sendRequest(6, obtain, obtain2, new MessageOption());
                    obtain2.readInt();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginService
            public String getVersion(TtsPluginListener ttsPluginListener) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeRemoteObject(ttsPluginListener != null ? ttsPluginListener.asObject() : null);
                    this.mRemote.sendRequest(9, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readString();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginService
            public void stopSpeak(TtsPluginListener ttsPluginListener) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeRemoteObject(ttsPluginListener != null ? ttsPluginListener.asObject() : null);
                    this.mRemote.sendRequest(7, obtain, obtain2, messageOption);
                    obtain2.readInt();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginService
            public void release(TtsPluginListener ttsPluginListener) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeRemoteObject(ttsPluginListener != null ? ttsPluginListener.asObject() : null);
                    this.mRemote.sendRequest(8, obtain, obtain2, messageOption);
                    obtain2.readInt();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginService
            public boolean isSpeaking(TtsPluginListener ttsPluginListener) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeRemoteObject(ttsPluginListener != null ? ttsPluginListener.asObject() : null);
                    this.mRemote.sendRequest(10, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginService
            public boolean setAudioType(int i, TtsPluginListener ttsPluginListener) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeRemoteObject(ttsPluginListener != null ? ttsPluginListener.asObject() : null);
                    this.mRemote.sendRequest(12, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readInt() != 0;
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginService
            public long getSupportMaxLength(TtsPluginListener ttsPluginListener) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeRemoteObject(ttsPluginListener != null ? ttsPluginListener.asObject() : null);
                    this.mRemote.sendRequest(19, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readLong();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.tts.TtsPluginService
            public void speakLongText(String str, String str2, TtsPluginListener ttsPluginListener) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeRemoteObject(ttsPluginListener != null ? ttsPluginListener.asObject() : null);
                    this.mRemote.sendRequest(20, obtain, obtain2, new MessageOption());
                    obtain2.readInt();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }
        }
    }
}
