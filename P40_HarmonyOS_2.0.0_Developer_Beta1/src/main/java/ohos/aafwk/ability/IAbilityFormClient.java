package ohos.aafwk.ability;

import java.util.function.Supplier;
import ohos.agp.components.ComponentProvider;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

/* access modifiers changed from: package-private */
public interface IAbilityFormClient extends IRemoteBroker {
    public static final int TRANSACTION_SEND_ACTION = 201;
    public static final int TRANSACTION_SEND_LISTENER = 202;

    void sendAction(ComponentProvider componentProvider) throws RemoteException;

    void sendListener(int i) throws RemoteException;

    public static abstract class FormClientStub extends RemoteObject implements IAbilityFormClient {
        private static Supplier<ComponentProvider> DEFAULT_REMOTE_COMPONENTS_BUILDER = $$Lambda$UxQPvovYsqBDdS9sOEPHp_Smk.INSTANCE;

        public IRemoteObject asObject() {
            return this;
        }

        FormClientStub() {
            super("");
        }

        static IAbilityFormClient asProxy(IRemoteObject iRemoteObject) {
            if (iRemoteObject == null) {
                return null;
            }
            return new FormClientProxy(iRemoteObject);
        }

        public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
            if (messageParcel == null || messageParcel2 == null) {
                return false;
            }
            if (i == 201) {
                ComponentProvider componentProvider = DEFAULT_REMOTE_COMPONENTS_BUILDER.get();
                messageParcel.readSequenceable(componentProvider);
                sendAction(componentProvider);
                return true;
            } else if (i != 202) {
                return IAbilityFormClient.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            } else {
                sendListener(messageParcel.readInt());
                return true;
            }
        }

        private static class FormClientProxy implements IAbilityFormClient {
            private static final HiLogLabel LABEL = new HiLogLabel(3, 218108672, "FormClientProxy");
            private IRemoteObject remote;

            FormClientProxy(IRemoteObject iRemoteObject) {
                this.remote = iRemoteObject;
            }

            public IRemoteObject asObject() {
                return this.remote;
            }

            /* JADX INFO: finally extract failed */
            @Override // ohos.aafwk.ability.IAbilityFormClient
            public void sendAction(ComponentProvider componentProvider) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                int applyType = componentProvider.getApplyType();
                if (componentProvider.setApplyType(2)) {
                    try {
                        obtain.writeSequenceable(componentProvider);
                        this.remote.sendRequest(201, obtain, obtain2, messageOption);
                        obtain2.reclaim();
                        obtain.reclaim();
                        if (!componentProvider.setApplyType(applyType)) {
                            HiLog.error(LABEL, "set apply type error. after marshalling actions in remote components.", new Object[0]);
                            throw new RemoteException();
                        }
                    } catch (Throwable th) {
                        obtain2.reclaim();
                        obtain.reclaim();
                        throw th;
                    }
                } else {
                    HiLog.error(LABEL, "set apply type error. before marshalling actions in remote components.", new Object[0]);
                    throw new RemoteException();
                }
            }

            @Override // ohos.aafwk.ability.IAbilityFormClient
            public void sendListener(int i) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInt(i);
                    this.remote.sendRequest(202, obtain, obtain2, messageOption);
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }
        }
    }
}
