package ohos.aafwk.ability;

import java.util.ArrayList;
import java.util.List;
import ohos.aafwk.utils.log.Log;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

interface IFormHost extends IRemoteBroker {
    public static final String DESCRIPTOR = "ohos.aafwk.ability.IFormHost";
    public static final int MAX_FORM_PER_BUNDLE = 128;
    public static final int TRANSACTION_FORM_ACQUIRED = 1;
    public static final int TRANSACTION_FORM_UNINSTALLED = 3;
    public static final int TRANSACTION_FORM_UPDATE = 2;

    void onAcquired(Form form) throws RemoteException;

    void onFormUninstalled(List<Integer> list) throws RemoteException;

    void onUpdate(Form form) throws RemoteException;

    public static abstract class FormHostStub extends RemoteObject implements IFormHost {
        public IRemoteObject asObject() {
            return this;
        }

        FormHostStub() {
            super("");
        }

        static IFormHost asProxy(IRemoteObject iRemoteObject) {
            if (iRemoteObject == null) {
                return null;
            }
            return new FormHostProxy(iRemoteObject);
        }

        public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
            if (messageParcel == null || messageParcel2 == null) {
                return false;
            }
            if (!IFormHost.DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
                Log.error("addForm IFormHost readToken failed onRemoteRequest token invalidt", new Object[0]);
                return false;
            }
            if (i == 1) {
                Log.info("addForm IFormHost code is acquire", new Object[0]);
                onAcquired(Form.createFromParcel(messageParcel));
            } else if (i == 2) {
                Log.info("IFormHost updte form", new Object[0]);
                onUpdate(Form.createFromParcel(messageParcel));
            } else if (i != 3) {
                return IFormHost.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            } else {
                Log.info("IFormHost form uninstalled.", new Object[0]);
                int readInt = messageParcel.readInt();
                if (readInt <= 0 || readInt > 128) {
                    Log.info("IFormHost form uninstalled, invalid form size:%{public}d", Integer.valueOf(readInt));
                    return false;
                }
                ArrayList arrayList = new ArrayList();
                for (int i2 = 0; i2 < readInt; i2++) {
                    arrayList.add(Integer.valueOf(messageParcel.readInt()));
                }
                onFormUninstalled(arrayList);
            }
            return true;
        }

        private static class FormHostProxy implements IFormHost {
            private IRemoteObject remote;

            FormHostProxy(IRemoteObject iRemoteObject) {
                this.remote = iRemoteObject;
            }

            public IRemoteObject asObject() {
                return this.remote;
            }

            @Override // ohos.aafwk.ability.IFormHost
            public void onAcquired(Form form) throws RemoteException {
                if (form == null) {
                    Log.error("addForm proxy form is null", new Object[0]);
                    return;
                }
                Log.info("addForm proxy onAcquired", new Object[0]);
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption(1);
                if (obtain.writeInterfaceToken(IFormHost.DESCRIPTOR)) {
                    obtain.writeSequenceable(form);
                    try {
                        this.remote.sendRequest(1, obtain, obtain2, messageOption);
                    } finally {
                        obtain.reclaim();
                        obtain2.reclaim();
                    }
                }
            }

            @Override // ohos.aafwk.ability.IFormHost
            public void onUpdate(Form form) throws RemoteException {
                if (form == null) {
                    Log.error("proxy form is null", new Object[0]);
                    return;
                }
                Log.info("proxy onUpdate", new Object[0]);
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption(1);
                if (obtain.writeInterfaceToken(IFormHost.DESCRIPTOR)) {
                    obtain.writeSequenceable(form);
                    try {
                        this.remote.sendRequest(2, obtain, obtain2, messageOption);
                    } finally {
                        obtain.reclaim();
                        obtain2.reclaim();
                    }
                }
            }

            @Override // ohos.aafwk.ability.IFormHost
            public void onFormUninstalled(List<Integer> list) throws RemoteException {
                if (list == null || list.isEmpty()) {
                    Log.error("onFormUninstalled proxy formIds is invalid", new Object[0]);
                    return;
                }
                Log.info("proxy onFormUninstalled", new Object[0]);
                int size = list.size();
                if (size > 128) {
                    Log.error("onFormUninstalled size too big", new Object[0]);
                    return;
                }
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                MessageOption messageOption = new MessageOption(1);
                if (!obtain.writeInterfaceToken(IFormHost.DESCRIPTOR)) {
                    Log.error("write interface failed", new Object[0]);
                } else if (!obtain.writeInt(size)) {
                    Log.error("onFormUninstalled write form size failed", new Object[0]);
                } else {
                    for (Integer num : list) {
                        if (!obtain.writeInt(num.intValue())) {
                            Log.error("onFormUninstalled write form id item failed", new Object[0]);
                            return;
                        }
                    }
                    try {
                        this.remote.sendRequest(3, obtain, obtain2, messageOption);
                    } finally {
                        obtain.reclaim();
                        obtain2.reclaim();
                    }
                }
            }
        }
    }
}
