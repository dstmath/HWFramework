package ohos.aafwk.ability;

import java.util.List;
import ohos.aafwk.ability.AbilitySlice;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

/* access modifiers changed from: package-private */
public class OhosHostCallback implements IHostCallback {
    private static final int ERR_CODE_OK = 0;
    private static final int FLAG_HAS_VALUE = 1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108160, "OhosHostCallback");
    private final IFormHost remoteClient;

    OhosHostCallback(IRemoteObject iRemoteObject) {
        this.remoteClient = AbilitySlice.FormHostClient.asProxy(iRemoteObject);
    }

    static void replyFormRecord(int i, MessageParcel messageParcel, FormRecord formRecord) {
        if (messageParcel.writeInt(0) && messageParcel.writeInt(1)) {
            HiLog.info(LABEL, "addForm replyFormRecord, formId:%{public}d", new Object[]{Integer.valueOf(i)});
            messageParcel.writeSequenceable(generateForm(i, formRecord));
        }
    }

    @Override // ohos.aafwk.ability.IHostCallback
    public void onAcquire(int i, FormRecord formRecord) {
        if (this.remoteClient == null || i < 0 || formRecord == null) {
            HiLog.error(LABEL, "invalid param", new Object[0]);
            return;
        }
        try {
            this.remoteClient.onAcquired(generateForm(i, formRecord));
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "onAcquire remote exception.", new Object[0]);
        }
    }

    @Override // ohos.aafwk.ability.IHostCallback
    public void onUpdate(int i, FormRecord formRecord) {
        if (this.remoteClient == null || i < 0 || formRecord == null) {
            HiLog.error(LABEL, "onUpdate invalid param", new Object[0]);
            return;
        }
        try {
            this.remoteClient.onUpdate(generateForm(i, formRecord));
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "onUpdate remote exception.", new Object[0]);
        }
    }

    @Override // ohos.aafwk.ability.IHostCallback
    public void onFormUninstalled(List<Integer> list) {
        if (this.remoteClient == null || list == null || list.isEmpty()) {
            HiLog.error(LABEL, "onFormUninstalled invalid param", new Object[0]);
            return;
        }
        try {
            this.remoteClient.onFormUninstalled(list);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "onFormUninstalled remote exception.", new Object[0]);
        }
    }

    private static Form generateForm(int i, FormRecord formRecord) {
        Form form = new Form();
        form.formId = i;
        form.bundleName = formRecord.bundleName;
        form.abilityName = formRecord.abilityName;
        form.formName = formRecord.formName;
        form.previewID = formRecord.previewLayoutId;
        if (formRecord.isJsForm) {
            form.setJsForm(true);
            form.setInstantProvider(formRecord.instantProvider);
        } else {
            form.setRemoteComponent(formRecord.formView);
        }
        return form;
    }
}
