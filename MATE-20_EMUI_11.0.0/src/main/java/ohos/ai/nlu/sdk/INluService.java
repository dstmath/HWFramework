package ohos.ai.nlu.sdk;

import java.util.Optional;
import ohos.ai.nlu.sdk.INluCallback;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public interface INluService extends IRemoteBroker {
    String getAppNlpResult(String str, INluCallback iNluCallback) throws RemoteException;

    String getAssistantIntention(String str, INluCallback iNluCallback) throws RemoteException;

    String getCategory(String str, INluCallback iNluCallback) throws RemoteException;

    String getChatIntention(String str, INluCallback iNluCallback) throws RemoteException;

    String getChatNlpResult(String str, INluCallback iNluCallback) throws RemoteException;

    String getEntity(String str, INluCallback iNluCallback) throws RemoteException;

    String getKeywords(String str, INluCallback iNluCallback) throws RemoteException;

    String getWordPos(String str, INluCallback iNluCallback) throws RemoteException;

    String getWordSegment(String str, INluCallback iNluCallback) throws RemoteException;

    int systemInit() throws RemoteException;

    public static abstract class Stub extends RemoteObject implements INluService {
        private static final String DESCRIPTOR = "com.huawei.hiai.nlu.INLUPluginService";
        static final int TRANSACTION_GET_APP_NLP_RESULT = 9;
        static final int TRANSACTION_GET_ASSISTANT_INTENTION = 7;
        static final int TRANSACTION_GET_CATEGORY = 11;
        static final int TRANSACTION_GET_CHAT_INTENTION = 6;
        static final int TRANSACTION_GET_CHAT_NLP_RESULT = 8;
        static final int TRANSACTION_GET_ENTITY = 5;
        static final int TRANSACTION_GET_KEY_WORDS = 10;
        static final int TRANSACTION_GET_WORD_POSITION = 4;
        static final int TRANSACTION_GET_WORD_SEGMENT = 3;
        static final int TRANSACTION_SYSTEM_INIT = 1;

        public IRemoteObject asObject() {
            return this;
        }

        public Stub() {
            super(DESCRIPTOR);
        }

        public static Optional<INluService> asInterface(IRemoteObject iRemoteObject) {
            if (iRemoteObject == null) {
                return Optional.empty();
            }
            INluService queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface instanceof INluService) {
                return Optional.of(queryLocalInterface);
            }
            return Optional.of(new Proxy(iRemoteObject));
        }

        public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
            if (i == 1) {
                messageParcel.readInterfaceToken();
                int systemInit = systemInit();
                messageParcel2.writeInt(0);
                messageParcel2.writeInt(systemInit);
                return true;
            } else if (i != 1598968902) {
                switch (i) {
                    case 3:
                        messageParcel.readInterfaceToken();
                        String wordSegment = getWordSegment(messageParcel.readString(), INluCallback.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        messageParcel2.writeString(wordSegment);
                        return true;
                    case 4:
                        messageParcel.readInterfaceToken();
                        String wordPos = getWordPos(messageParcel.readString(), INluCallback.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        messageParcel2.writeString(wordPos);
                        return true;
                    case 5:
                        messageParcel.readInterfaceToken();
                        String entity = getEntity(messageParcel.readString(), INluCallback.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        messageParcel2.writeString(entity);
                        return true;
                    case 6:
                        messageParcel.readInterfaceToken();
                        String chatIntention = getChatIntention(messageParcel.readString(), INluCallback.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        messageParcel2.writeString(chatIntention);
                        return true;
                    case 7:
                        messageParcel.readInterfaceToken();
                        String assistantIntention = getAssistantIntention(messageParcel.readString(), INluCallback.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        messageParcel2.writeString(assistantIntention);
                        return true;
                    case 8:
                        messageParcel.readInterfaceToken();
                        String chatNlpResult = getChatNlpResult(messageParcel.readString(), INluCallback.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        messageParcel2.writeString(chatNlpResult);
                        return true;
                    case 9:
                        messageParcel.readInterfaceToken();
                        String appNlpResult = getAppNlpResult(messageParcel.readString(), INluCallback.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        messageParcel2.writeString(appNlpResult);
                        return true;
                    case 10:
                        messageParcel.readInterfaceToken();
                        String keywords = getKeywords(messageParcel.readString(), INluCallback.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        messageParcel2.writeString(keywords);
                        return true;
                    case 11:
                        messageParcel.readInterfaceToken();
                        String category = getCategory(messageParcel.readString(), INluCallback.Stub.asInterface(messageParcel.readRemoteObject()).orElse(null));
                        messageParcel2.writeInt(0);
                        messageParcel2.writeString(category);
                        return true;
                    default:
                        return INluService.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
                }
            } else {
                messageParcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements INluService {
            private IRemoteObject remoteObject;

            Proxy(IRemoteObject iRemoteObject) {
                this.remoteObject = iRemoteObject;
            }

            public IRemoteObject asObject() {
                return this.remoteObject;
            }

            @Override // ohos.ai.nlu.sdk.INluService
            public int systemInit() throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.remoteObject.sendRequest(1, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readInt();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.nlu.sdk.INluService
            public String getEntity(String str, INluCallback iNluCallback) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeRemoteObject(iNluCallback != null ? iNluCallback.asObject() : null);
                    this.remoteObject.sendRequest(5, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readString();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.nlu.sdk.INluService
            public String getWordSegment(String str, INluCallback iNluCallback) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeRemoteObject(iNluCallback != null ? iNluCallback.asObject() : null);
                    this.remoteObject.sendRequest(3, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readString();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.nlu.sdk.INluService
            public String getWordPos(String str, INluCallback iNluCallback) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeRemoteObject(iNluCallback != null ? iNluCallback.asObject() : null);
                    this.remoteObject.sendRequest(4, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readString();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.nlu.sdk.INluService
            public String getChatIntention(String str, INluCallback iNluCallback) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeRemoteObject(iNluCallback != null ? iNluCallback.asObject() : null);
                    this.remoteObject.sendRequest(6, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readString();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.nlu.sdk.INluService
            public String getChatNlpResult(String str, INluCallback iNluCallback) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeRemoteObject(iNluCallback != null ? iNluCallback.asObject() : null);
                    this.remoteObject.sendRequest(8, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readString();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.nlu.sdk.INluService
            public String getAppNlpResult(String str, INluCallback iNluCallback) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeRemoteObject(iNluCallback != null ? iNluCallback.asObject() : null);
                    this.remoteObject.sendRequest(9, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readString();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.nlu.sdk.INluService
            public String getKeywords(String str, INluCallback iNluCallback) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeRemoteObject(iNluCallback != null ? iNluCallback.asObject() : null);
                    this.remoteObject.sendRequest(10, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readString();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.nlu.sdk.INluService
            public String getCategory(String str, INluCallback iNluCallback) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeRemoteObject(iNluCallback != null ? iNluCallback.asObject() : null);
                    this.remoteObject.sendRequest(11, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readString();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.nlu.sdk.INluService
            public String getAssistantIntention(String str, INluCallback iNluCallback) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeRemoteObject(iNluCallback != null ? iNluCallback.asObject() : null);
                    this.remoteObject.sendRequest(7, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readString();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }
        }
    }
}
