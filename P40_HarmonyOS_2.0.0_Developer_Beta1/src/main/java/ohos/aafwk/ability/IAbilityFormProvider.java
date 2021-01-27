package ohos.aafwk.ability;

import ohos.aafwk.content.Intent;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

interface IAbilityFormProvider extends IRemoteBroker {
    public static final int TRANSACTION_ACQUIRE_ABILITY_FORM = 0;
    public static final int TRANSACTION_ACQUIRE_PROVIDER_FORM_INFO = 1;
    public static final int TRANSACTION_FIRE_FORM_EVENT = 4;
    public static final int TRANSACTION_NOTIFY_FORM_DELETE = 2;
    public static final int TRANSACTION_NOTIFY_FORM_UPDATE = 3;

    AbilityForm acquireAbilityForm() throws RemoteException;

    ProviderFormInfo acquireProviderFormInfo(Intent intent) throws RemoteException;

    void fireFormEvent(int i, String str) throws RemoteException;

    void notifyFormDelete(int i) throws RemoteException;

    void notifyFormUpdate(int i) throws RemoteException;

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
            if (i == 0) {
                messageParcel2.writeSequenceable(acquireAbilityForm());
            } else if (i == 1) {
                Intent intent = new Intent();
                if (!messageParcel.readSequenceable(intent)) {
                    return false;
                }
                messageParcel2.writeSequenceable(acquireProviderFormInfo(intent));
            } else if (i == 2) {
                notifyFormDelete(messageParcel.readInt());
            } else if (i == 3) {
                notifyFormUpdate(messageParcel.readInt());
            } else if (i != 4) {
                return IAbilityFormProvider.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            } else {
                fireFormEvent(messageParcel.readInt(), messageParcel.readString());
            }
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

            @Override // ohos.aafwk.ability.IAbilityFormProvider
            public ProviderFormInfo acquireProviderFormInfo(Intent intent) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeSequenceable(intent);
                    this.remote.sendRequest(1, obtain, obtain2, messageOption);
                    return ProviderFormInfo.createFromParcel(obtain2);
                } finally {
                    obtain.reclaim();
                    obtain2.reclaim();
                }
            }

            @Override // ohos.aafwk.ability.IAbilityFormProvider
            public void notifyFormDelete(int i) throws RemoteException {
                communication(i, 2);
            }

            @Override // ohos.aafwk.ability.IAbilityFormProvider
            public void notifyFormUpdate(int i) throws RemoteException {
                communication(i, 3);
            }

            @Override // ohos.aafwk.ability.IAbilityFormProvider
            public void fireFormEvent(int i, String str) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                try {
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    this.remote.sendRequest(4, obtain, obtain2, new MessageOption());
                } finally {
                    obtain.reclaim();
                    obtain2.reclaim();
                }
            }

            private void communication(int i, int i2) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                try {
                    obtain.writeInt(i);
                    this.remote.sendRequest(i2, obtain, obtain2, new MessageOption());
                } finally {
                    obtain.reclaim();
                    obtain2.reclaim();
                }
            }
        }
    }
}
