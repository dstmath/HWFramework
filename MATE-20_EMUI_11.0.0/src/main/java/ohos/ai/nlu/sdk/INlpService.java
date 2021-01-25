package ohos.ai.nlu.sdk;

import java.util.Optional;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public interface INlpService extends IRemoteBroker {
    String analyzeAssistant(String str) throws RemoteException;

    String analyzeLongText(String str) throws RemoteException;

    String analyzeShortText(String str) throws RemoteException;

    public static abstract class Stub extends RemoteObject implements INlpService {
        private static final String DESCRIPTOR = "com.huawei.hiai.nlu.INLPPluginService";
        private static final int TRANSACTION_ANALYZE_ASSISTANT = 10;
        private static final int TRANSACTION_ANALYZE_LONG_TEXT = 2;
        private static final int TRANSACTION_ANALYZE_SHORT_TEXT = 1;

        public IRemoteObject asObject() {
            return this;
        }

        public Stub() {
            super(DESCRIPTOR);
        }

        public static Optional<INlpService> asInterface(IRemoteObject iRemoteObject) {
            if (iRemoteObject == null) {
                return Optional.empty();
            }
            INlpService queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface instanceof INlpService) {
                return Optional.of(queryLocalInterface);
            }
            return Optional.of(new Proxy(iRemoteObject));
        }

        public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
            if (i == 1) {
                messageParcel.readInterfaceToken();
                String analyzeShortText = analyzeShortText(messageParcel.readString());
                messageParcel2.writeInt(0);
                messageParcel2.writeString(analyzeShortText);
                return true;
            } else if (i == 2) {
                messageParcel.readInterfaceToken();
                String analyzeLongText = analyzeLongText(messageParcel.readString());
                messageParcel2.writeInt(0);
                messageParcel2.writeString(analyzeLongText);
                return true;
            } else if (i == 10) {
                messageParcel.readInterfaceToken();
                String analyzeAssistant = analyzeAssistant(messageParcel.readString());
                messageParcel2.writeInt(0);
                messageParcel2.writeString(analyzeAssistant);
                return true;
            } else if (i != 1598968902) {
                return INlpService.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            } else {
                messageParcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements INlpService {
            private IRemoteObject mRemote;

            Proxy(IRemoteObject iRemoteObject) {
                this.mRemote = iRemoteObject;
            }

            public IRemoteObject asObject() {
                return this.mRemote;
            }

            @Override // ohos.ai.nlu.sdk.INlpService
            public String analyzeShortText(String str) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str);
                    this.mRemote.sendRequest(1, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readString();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.nlu.sdk.INlpService
            public String analyzeLongText(String str) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.sendRequest(2, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readString();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.ai.nlu.sdk.INlpService
            public String analyzeAssistant(String str) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.sendRequest(10, obtain, obtain2, messageOption);
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
