package ohos.ai.nlu.sdk;

import java.util.Optional;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public interface INluCallback extends IRemoteBroker {
    public static final String DESCRIPTOR = "com.huawei.hiai.nlu.INLUPluginCallback";

    void onNluResult(String str) throws RemoteException;

    public static abstract class Stub extends RemoteObject implements INluCallback {
        static final int TRANSACTION_ON_NLU_RESULT = 1;

        public IRemoteObject asObject() {
            return this;
        }

        public Stub() {
            super(INluCallback.DESCRIPTOR);
        }

        public static Optional<INluCallback> asInterface(IRemoteObject iRemoteObject) {
            if (iRemoteObject == null) {
                return Optional.empty();
            }
            INluCallback queryLocalInterface = iRemoteObject.queryLocalInterface(INluCallback.DESCRIPTOR);
            if (queryLocalInterface instanceof INluCallback) {
                return Optional.of(queryLocalInterface);
            }
            return Optional.of(new Proxy(iRemoteObject));
        }

        public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
            if (i == 1) {
                messageParcel.readInterfaceToken();
                onNluResult(messageParcel.readString());
                messageParcel2.writeInt(0);
                return true;
            } else if (i != 1598968902) {
                return INluCallback.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            } else {
                messageParcel2.writeString(INluCallback.DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements INluCallback {
            private IRemoteObject mRemote;

            Proxy(IRemoteObject iRemoteObject) {
                this.mRemote = iRemoteObject;
            }

            public IRemoteObject asObject() {
                return this.mRemote;
            }

            @Override // ohos.ai.nlu.sdk.INluCallback
            public void onNluResult(String str) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInterfaceToken(INluCallback.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.sendRequest(1, obtain, obtain2, messageOption);
                    obtain2.readInt();
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }
        }
    }
}
