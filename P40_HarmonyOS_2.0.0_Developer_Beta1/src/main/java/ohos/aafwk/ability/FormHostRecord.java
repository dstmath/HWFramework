package ohos.aafwk.ability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;

/* access modifiers changed from: package-private */
public class FormHostRecord {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108160, "FormHostRecord");
    IHostCallback clientImpl;
    IRemoteObject clientStub;
    IRemoteObject.DeathRecipient deathRecipient;
    Map<Integer, Boolean> forms = new HashMap();
    boolean isESystem;
    Map<Integer, Boolean> needRefresh = new HashMap();

    static FormHostRecord createRecord(boolean z, IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            HiLog.error(LABEL, "invalid param", new Object[0]);
            return null;
        }
        FormHostRecord formHostRecord = new FormHostRecord();
        formHostRecord.isESystem = z;
        formHostRecord.clientStub = iRemoteObject;
        formHostRecord.clientImpl = z ? new ESystemHostCallback(iRemoteObject) : new OhosHostCallback(iRemoteObject);
        formHostRecord.deathRecipient = new ClientDeathRecipient(formHostRecord);
        formHostRecord.clientStub.addDeathRecipient(formHostRecord.deathRecipient, 0);
        return formHostRecord;
    }

    /* access modifiers changed from: package-private */
    public void addForm(int i) {
        if (!this.forms.containsKey(Integer.valueOf(i))) {
            this.forms.put(Integer.valueOf(i), true);
        }
    }

    /* access modifiers changed from: package-private */
    public void delForm(int i) {
        this.forms.remove(Integer.valueOf(i));
    }

    /* access modifiers changed from: package-private */
    public boolean isEmpty() {
        return this.forms.isEmpty();
    }

    /* access modifiers changed from: package-private */
    public boolean contains(int i) {
        return this.forms.containsKey(Integer.valueOf(i));
    }

    /* access modifiers changed from: package-private */
    public void setEnableRefresh(int i, boolean z) {
        if (this.forms.containsKey(Integer.valueOf(i))) {
            this.forms.put(Integer.valueOf(i), Boolean.valueOf(z));
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isEnableRefresh(int i) {
        Boolean bool = this.forms.get(Integer.valueOf(i));
        return bool != null && bool.booleanValue();
    }

    /* access modifiers changed from: package-private */
    public void setNeedRefresh(int i, boolean z) {
        this.needRefresh.put(Integer.valueOf(i), Boolean.valueOf(z));
    }

    /* access modifiers changed from: package-private */
    public boolean isNeedRefresh(int i) {
        Boolean bool = this.needRefresh.get(Integer.valueOf(i));
        return bool != null && bool.booleanValue();
    }

    /* access modifiers changed from: package-private */
    public void onAcquire(int i, FormRecord formRecord) {
        IHostCallback iHostCallback = this.clientImpl;
        if (iHostCallback != null) {
            iHostCallback.onAcquire(i, formRecord);
        }
    }

    /* access modifiers changed from: package-private */
    public void onUpdate(int i, FormRecord formRecord) {
        IHostCallback iHostCallback = this.clientImpl;
        if (iHostCallback != null) {
            iHostCallback.onUpdate(i, formRecord);
        }
    }

    /* access modifiers changed from: package-private */
    public void onFormUninstalled(List<Integer> list) {
        IHostCallback iHostCallback = this.clientImpl;
        if (iHostCallback != null) {
            iHostCallback.onFormUninstalled(list);
        }
    }

    private FormHostRecord() {
    }

    private static class ClientDeathRecipient implements IRemoteObject.DeathRecipient {
        private final FormHostRecord owner;

        ClientDeathRecipient(FormHostRecord formHostRecord) {
            this.owner = formHostRecord;
        }

        public void onRemoteDied() {
            FormAdapter.getInstance().handleHostDied(this.owner.clientStub);
        }
    }
}
