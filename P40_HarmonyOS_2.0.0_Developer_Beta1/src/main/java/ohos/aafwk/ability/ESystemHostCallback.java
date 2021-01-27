package ohos.aafwk.ability;

import java.util.List;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.interwork.ability.InstantProviderEx;
import ohos.interwork.ui.RemoteViewEx;
import ohos.interwork.utils.PacMapEx;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

class ESystemHostCallback implements IHostCallback {
    private static final int CODE_ON_ACQUIRE = 1;
    private static final int CODE_ON_FORM_UNINSTALLED = 3;
    private static final int CODE_ON_UPDATE = 2;
    private static final int ERR_CODE_OK = 0;
    private static final String ESYSTEM_INTERFACE_TOKEN = "com.huawei.ohos.localability.IFormClient";
    private static final int FLAG_HAS_JAVA_VALUE = 1;
    private static final int FLAG_HAS_JS_VALUE = 2;
    private static final int FLAG_NO_VALUE = 0;
    private static final String KEY_REMOTEVIEW = "REMOTE_VIEW";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108160, "ESystemHostCallback");
    private static final int MAX_FORMS_PER_BUNDLE = 64;
    IRemoteObject remoteClient;

    ESystemHostCallback(IRemoteObject iRemoteObject) {
        this.remoteClient = iRemoteObject;
    }

    static void replyFormRecord(int i, MessageParcel messageParcel, FormRecord formRecord) {
        if (messageParcel.writeInt(0)) {
            if (!marshallingBasicData(messageParcel, i, formRecord)) {
                HiLog.error(LABEL, "marshalling basic data failed", new Object[0]);
            } else if (!formRecord.isJsForm) {
                RemoteViewEx remoteViewEx = ComponentUtils.getRemoteViewEx(FormAdapter.getInstance().getContext(), formRecord.bundleName, formRecord.hapSourceDirs, formRecord.formView);
                if (remoteViewEx == null) {
                    messageParcel.writeInt(0);
                } else if (messageParcel.writeInt(1)) {
                    PacMapEx pacMapEx = new PacMapEx();
                    pacMapEx.putObjectValue(KEY_REMOTEVIEW, remoteViewEx);
                    messageParcel.writePacMapEx(pacMapEx);
                }
            } else if (messageParcel.writeInt(2)) {
                messageParcel.writeParcelableEx(new InstantProviderEx(formRecord.instantProvider));
            }
        }
    }

    private static boolean marshallingBasicData(MessageParcel messageParcel, int i, FormRecord formRecord) {
        if (messageParcel.writeInt(1) && messageParcel.writeInt(i) && messageParcel.writeString(formRecord.bundleName) && messageParcel.writeString(formRecord.abilityName) && messageParcel.writeString(formRecord.formName)) {
            return messageParcel.writeInt(formRecord.eSystemPreviewLayoutId);
        }
        return false;
    }

    @Override // ohos.aafwk.ability.IHostCallback
    public void onAcquire(int i, FormRecord formRecord) {
        handleEvent(1, i, formRecord);
    }

    @Override // ohos.aafwk.ability.IHostCallback
    public void onUpdate(int i, FormRecord formRecord) {
        handleEvent(2, i, formRecord);
    }

    private void handleEvent(int i, int i2, FormRecord formRecord) {
        if (this.remoteClient == null || i2 < 0 || formRecord == null) {
            HiLog.error(LABEL, "invalid param", new Object[0]);
            return;
        }
        boolean z = formRecord.isJsForm;
        RemoteViewEx remoteViewEx = null;
        if (z || (remoteViewEx = ComponentUtils.getRemoteViewEx(FormAdapter.getInstance().getContext(), formRecord.bundleName, formRecord.hapSourceDirs, formRecord.formView)) != null) {
            MessageParcel obtain = MessageParcel.obtain();
            if (!obtain.writeInterfaceToken(ESYSTEM_INTERFACE_TOKEN)) {
                HiLog.error(LABEL, "write interface failed", new Object[0]);
            } else if (!marshallingBasicData(obtain, i2, formRecord)) {
                HiLog.error(LABEL, "marshalling basic data failed", new Object[0]);
            } else {
                if (obtain.writeInt(z ? 2 : 1)) {
                    if (z) {
                        obtain.writeParcelableEx(new InstantProviderEx(formRecord.instantProvider));
                    } else {
                        PacMapEx pacMapEx = new PacMapEx();
                        pacMapEx.putObjectValue(KEY_REMOTEVIEW, remoteViewEx);
                        obtain.writePacMapEx(pacMapEx);
                    }
                    sendImpl(i, obtain, MessageParcel.obtain());
                }
            }
        } else {
            HiLog.error(LABEL, "get RemoteViewEx failed", new Object[0]);
        }
    }

    @Override // ohos.aafwk.ability.IHostCallback
    public void onFormUninstalled(List<Integer> list) {
        if (this.remoteClient == null || list == null || list.isEmpty()) {
            HiLog.error(LABEL, "onFormUninstalled invalid param", new Object[0]);
            return;
        }
        int size = list.size();
        if (size > 64) {
            HiLog.error(LABEL, "onFormUninstalled invalid param", new Object[0]);
            return;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        if (!obtain.writeInterfaceToken(ESYSTEM_INTERFACE_TOKEN)) {
            HiLog.error(LABEL, "write interface failed", new Object[0]);
        } else if (!obtain.writeInt(size)) {
            HiLog.error(LABEL, "onFormUninstalled write form size failed", new Object[0]);
        } else {
            for (Integer num : list) {
                if (!obtain.writeInt(num.intValue())) {
                    HiLog.error(LABEL, "onFormUninstalled write form id item failed", new Object[0]);
                    return;
                }
            }
            sendImpl(3, obtain, obtain2);
        }
    }

    private void sendImpl(int i, MessageParcel messageParcel, MessageParcel messageParcel2) {
        try {
            this.remoteClient.sendRequest(i, messageParcel, messageParcel2, new MessageOption());
        } catch (RemoteException unused) {
            HiLog.info(LABEL, "SendImpl get remote exception..", new Object[0]);
        } catch (Throwable th) {
            messageParcel.reclaim();
            messageParcel2.reclaim();
            throw th;
        }
        messageParcel.reclaim();
        messageParcel2.reclaim();
    }
}
