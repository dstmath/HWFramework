package ohos.aafwk.ability;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

interface IAbilityFormProvider extends IRemoteBroker {
    public static final int TRANSACTION_ACQUIRE_ABILITY_FORM = 0;

    AbilityForm acquireAbilityForm() throws RemoteException;

    public static abstract class FormProviderStub extends RemoteObject implements IAbilityFormProvider {
        public IRemoteObject asObject() {
            return this;
        }

        FormProviderStub() {
            super("");
        }

        static IAbilityFormProvider asProxy(IRemoteObject iRemoteObject) {
            if (iRemoteObject == null) {
                return null;
            }
            return new FormProviderProxy(iRemoteObject);
        }

        public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
            if (messageParcel == null || messageParcel2 == null) {
                return false;
            }
            if (i != 0) {
                return IAbilityFormProvider.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            }
            messageParcel2.writeSequenceable(acquireAbilityForm());
            return true;
        }

        /* access modifiers changed from: private */
        public static class FormProviderProxy implements IAbilityFormProvider {
            private IRemoteObject remote;

            FormProviderProxy(IRemoteObject iRemoteObject) {
                this.remote = iRemoteObject;
            }

            public IRemoteObject asObject() {
                return this.remote;
            }

            @Override // ohos.aafwk.ability.IAbilityFormProvider
            public AbilityForm acquireAbilityForm() throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                try {
                    this.remote.sendRequest(0, obtain, obtain2, new MessageOption());
                    return AbilityForm.createFromParcel(obtain2);
                } finally {
                    obtain.reclaim();
                    obtain2.reclaim();
                }
            }
        }
    }
}
