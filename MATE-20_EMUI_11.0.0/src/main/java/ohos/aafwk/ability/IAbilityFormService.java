package ohos.aafwk.ability;

import ohos.aafwk.ability.AbilityForm;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

/* access modifiers changed from: package-private */
public interface IAbilityFormService extends IRemoteBroker {
    public static final int TRANSACTION_DISABLE_UPDATE_PUSH = 104;
    public static final int TRANSACTION_ENABLE_UPDATE_PUSH = 105;
    public static final int TRANSACTION_ON_TOUCH = 103;
    public static final int TRANSACTION_REGISTER_CLIENT = 100;
    public static final int TRANSACTION_RELEASE = 102;
    public static final int TRANSACTION_REQUEST_LATEST_FORM = 106;
    public static final int TRANSACTION_UNREGISTER_CLIENT = 101;

    void disableUpdatePush(IRemoteObject iRemoteObject) throws RemoteException;

    void enableUpdatePush(IRemoteObject iRemoteObject) throws RemoteException;

    void registerClient(IRemoteObject iRemoteObject) throws RemoteException;

    void release() throws RemoteException;

    AbilityForm.AbilityFormLite requestLatestForm(IRemoteObject iRemoteObject) throws RemoteException;

    void sendOnTouchEvent(int i, ViewsStatus viewsStatus) throws RemoteException;

    void unregisterClient(IRemoteObject iRemoteObject) throws RemoteException;

    public static abstract class FormServiceStub extends RemoteObject implements IAbilityFormService {
        public IRemoteObject asObject() {
            return this;
        }

        FormServiceStub() {
            super("");
        }

        static IAbilityFormService asProxy(IRemoteObject iRemoteObject) {
            if (iRemoteObject == null) {
                return null;
            }
            return new FormServiceProxy(iRemoteObject);
        }

        public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
            if (messageParcel == null || messageParcel2 == null) {
                return false;
            }
            switch (i) {
                case 100:
                    registerClient(messageParcel.readRemoteObject());
                    return true;
                case 101:
                    unregisterClient(messageParcel.readRemoteObject());
                    return true;
                case 102:
                    release();
                    return true;
                case 103:
                    sendOnTouchEvent(messageParcel.readInt(), ViewsStatus.createFromParcel(messageParcel));
                    return true;
                case 104:
                    disableUpdatePush(messageParcel.readRemoteObject());
                    return true;
                case 105:
                    enableUpdatePush(messageParcel.readRemoteObject());
                    return true;
                case 106:
                    messageParcel2.writeSequenceable(requestLatestForm(messageParcel.readRemoteObject()));
                    return true;
                default:
                    return IAbilityFormService.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            }
        }

        private static class FormServiceProxy implements IAbilityFormService {
            private IRemoteObject remote;

            FormServiceProxy(IRemoteObject iRemoteObject) {
                this.remote = iRemoteObject;
            }

            public IRemoteObject asObject() {
                return this.remote;
            }

            @Override // ohos.aafwk.ability.IAbilityFormService
            public void registerClient(IRemoteObject iRemoteObject) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                obtain.writeRemoteObject(iRemoteObject);
                try {
                    this.remote.sendRequest(100, obtain, obtain2, messageOption);
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.aafwk.ability.IAbilityFormService
            public void unregisterClient(IRemoteObject iRemoteObject) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                obtain.writeRemoteObject(iRemoteObject);
                try {
                    this.remote.sendRequest(101, obtain, obtain2, messageOption);
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.aafwk.ability.IAbilityFormService
            public void release() throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                try {
                    this.remote.sendRequest(101, obtain, obtain2, new MessageOption());
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.aafwk.ability.IAbilityFormService
            public void sendOnTouchEvent(int i, ViewsStatus viewsStatus) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeInt(i);
                    obtain.writeSequenceable(viewsStatus);
                    this.remote.sendRequest(103, obtain, obtain2, messageOption);
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.aafwk.ability.IAbilityFormService
            public void disableUpdatePush(IRemoteObject iRemoteObject) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeRemoteObject(iRemoteObject);
                    this.remote.sendRequest(104, obtain, obtain2, messageOption);
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.aafwk.ability.IAbilityFormService
            public void enableUpdatePush(IRemoteObject iRemoteObject) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                try {
                    obtain.writeRemoteObject(iRemoteObject);
                    this.remote.sendRequest(105, obtain, obtain2, messageOption);
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }

            @Override // ohos.aafwk.ability.IAbilityFormService
            public AbilityForm.AbilityFormLite requestLatestForm(IRemoteObject iRemoteObject) throws RemoteException {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption();
                obtain.writeRemoteObject(iRemoteObject);
                try {
                    this.remote.sendRequest(106, obtain, obtain2, messageOption);
                    AbilityForm.AbilityFormLite abilityFormLite = new AbilityForm.AbilityFormLite();
                    obtain2.readSequenceable(abilityFormLite);
                    return abilityFormLite;
                } finally {
                    obtain2.reclaim();
                    obtain.reclaim();
                }
            }
        }
    }
}
