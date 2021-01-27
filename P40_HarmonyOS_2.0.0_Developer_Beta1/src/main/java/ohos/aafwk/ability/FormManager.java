package ohos.aafwk.ability;

import java.util.List;
import ohos.aafwk.ability.FormException;
import ohos.aafwk.content.Intent;
import ohos.agp.components.ComponentProvider;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;

public class FormManager {
    private static final int ADD_FORM = 2;
    private static final int DEATH_RECIPIENT_FLAG = 0;
    private static final String DESCRIPTOR = "OHOS.AppExecFwk.IFormMgr";
    private static final int FLAG_HAS_OBJECT = 1;
    private static final int FLAG_NO_OBJECT = 0;
    private static final int FORMMGR_SERVICE_ID = 403;
    private static final Object INSTANCE_LOCK = new Object();
    private static final boolean JAVA_FORM = false;
    private static final boolean JS_FORM = true;
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 218108160, "FormManager");
    private static final int REQUEST_FORM = 7;
    private static final int UPDATE_FORM = 4;
    private static volatile FormManager instance;
    private static volatile boolean resetFlag = false;
    private IRemoteObject remote;

    public static FormManager getInstance() {
        if (instance == null) {
            synchronized (INSTANCE_LOCK) {
                if (instance == null) {
                    IRemoteObject sysAbility = SysAbilityManager.getSysAbility(403);
                    if (sysAbility == null) {
                        HiLog.warn(LABEL_LOG, "FormManager getInstance failed, remote is null", new Object[0]);
                        return null;
                    }
                    if (!sysAbility.addDeathRecipient(new FormManagerDeathRecipient(), 0)) {
                        HiLog.debug(LABEL_LOG, "FormManager register FormManagerDeathRecipient failed", new Object[0]);
                    }
                    instance = new FormManager(sysAbility);
                }
            }
        }
        return instance;
    }

    public FormManager(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    public Form addForm(Intent intent, IRemoteObject iRemoteObject) throws FormException {
        HiLog.info(LABEL_LOG, "addForm.", new Object[0]);
        if (intent == null || iRemoteObject == null) {
            throw new FormException(FormException.FormError.INPUT_PARAM_INVALID.toString(), "intent or clientStub can not be null");
        }
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
                    reclaimParcel(obtain, obtain2);
                    return null;
                }
                obtain.writeSequenceable(intent);
                obtain.writeRemoteObject(iRemoteObject);
                if (this.remote.sendRequest(2, obtain, obtain2, messageOption)) {
                    int readInt = obtain2.readInt();
                    if (readInt != 0) {
                        throw new FormException(getErrFromResult(readInt));
                    } else if (obtain2.readInt() != 1) {
                        reclaimParcel(obtain, obtain2);
                        return null;
                    } else {
                        Form createFromParcel = Form.createFromParcel(obtain2);
                        reclaimParcel(obtain, obtain2);
                        return createFromParcel;
                    }
                } else {
                    HiLog.error(LABEL_LOG, "FormManager::addForm sendRequest failed", new Object[0]);
                    throw new FormException(FormException.FormError.SEND_FMS_MSG_ERROR);
                }
            } catch (RemoteException e) {
                HiLog.error(LABEL_LOG, "FormManager::addForm, send request to fms failed", new Object[0]);
                String formError = FormException.FormError.SEND_FMS_MSG_ERROR.toString();
                throw new FormException(formError, "send request to fms failed " + e.getMessage());
            } catch (Throwable th) {
                reclaimParcel(obtain, obtain2);
                throw th;
            }
        } else {
            throw new FormException(FormException.FormError.FMS_BINDER_ERROR);
        }
    }

    private FormException.FormError getErrFromResult(int i) {
        FormException.FormError fromErrCode = FormException.FormError.fromErrCode(i);
        return fromErrCode == null ? FormException.FormError.INTERNAL_ERROR : fromErrCode;
    }

    public boolean deleteForm(int i, IRemoteObject iRemoteObject, int i2) throws FormException {
        HiLog.info(LABEL_LOG, "deleteForm.", new Object[0]);
        if (i <= 0 || iRemoteObject == null) {
            throw new FormException(FormException.FormError.INTERNAL_ERROR, "form has no client");
        }
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
                    reclaimParcel(obtain, obtain2);
                    return false;
                } else if (!obtain.writeInt(i)) {
                    reclaimParcel(obtain, obtain2);
                    return false;
                } else {
                    obtain.writeRemoteObject(iRemoteObject);
                    if (this.remote.sendRequest(i2, obtain, obtain2, messageOption)) {
                        int readInt = obtain2.readInt();
                        if (readInt == 0) {
                            reclaimParcel(obtain, obtain2);
                            return true;
                        }
                        HiLog.error(LABEL_LOG, "delete form error, code %{public}d", new Object[]{Integer.valueOf(readInt)});
                        throw new FormException(getErrFromResult(readInt));
                    }
                    HiLog.error(LABEL_LOG, "FormManager::deleteForm sendRequest failed", new Object[0]);
                    throw new FormException(FormException.FormError.SEND_FMS_MSG_ERROR);
                }
            } catch (RemoteException e) {
                HiLog.error(LABEL_LOG, "deleteForm exception %{public}s", new Object[]{e.getMessage()});
                FormException.FormError formError = FormException.FormError.SEND_FMS_MSG_ERROR;
                throw new FormException(formError, "delete form occurs error " + e.getMessage());
            } catch (Throwable th) {
                reclaimParcel(obtain, obtain2);
                throw th;
            }
        } else {
            throw new FormException(FormException.FormError.FMS_BINDER_ERROR);
        }
    }

    public boolean requestForm(int i, IRemoteObject iRemoteObject, Intent intent) throws IllegalArgumentException, RemoteException {
        HiLog.info(LABEL_LOG, "requestForm.", new Object[0]);
        if (i <= 0 || iRemoteObject == null) {
            throw new IllegalArgumentException("formId or clientStub is invalid");
        }
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
                    return false;
                }
                if (!obtain.writeInt(i)) {
                    reclaimParcel(obtain, obtain2);
                    return false;
                }
                obtain.writeSequenceable(intent);
                obtain.writeRemoteObject(iRemoteObject);
                if (!this.remote.sendRequest(7, obtain, obtain2, messageOption)) {
                    HiLog.error(LABEL_LOG, "FormManager::requestForm sendRequest failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                    return false;
                } else if (obtain2.readInt() != 0) {
                    reclaimParcel(obtain, obtain2);
                    return false;
                } else {
                    reclaimParcel(obtain, obtain2);
                    return true;
                }
            } finally {
                reclaimParcel(obtain, obtain2);
            }
        } else {
            throw new RemoteException("failed to get fms");
        }
    }

    public boolean lifecycleUpdate(List<Integer> list, IRemoteObject iRemoteObject, int i) throws IllegalArgumentException, RemoteException {
        HiLog.info(LABEL_LOG, "lifecycleUpdate.", new Object[0]);
        if (list == null || list.isEmpty() || iRemoteObject == null) {
            throw new IllegalArgumentException("formIDs or clientStub is invalid");
        }
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
                    return false;
                }
                if (!obtain.writeInt(list.size())) {
                    reclaimParcel(obtain, obtain2);
                    return false;
                }
                for (Integer num : list) {
                    if (!obtain.writeInt(num.intValue())) {
                        reclaimParcel(obtain, obtain2);
                        return false;
                    }
                }
                obtain.writeRemoteObject(iRemoteObject);
                if (!this.remote.sendRequest(i, obtain, obtain2, messageOption)) {
                    HiLog.error(LABEL_LOG, "FormManager::lifecycleUpdate sendRequest failed", new Object[0]);
                    reclaimParcel(obtain, obtain2);
                    return false;
                } else if (obtain2.readInt() != 0) {
                    reclaimParcel(obtain, obtain2);
                    return false;
                } else {
                    reclaimParcel(obtain, obtain2);
                    return true;
                }
            } finally {
                reclaimParcel(obtain, obtain2);
            }
        } else {
            throw new RemoteException("failed to get fms");
        }
    }

    public boolean updateForm(int i, String str, ComponentProvider componentProvider) throws IllegalArgumentException, RemoteException {
        HiLog.info(LABEL_LOG, "updateForm.", new Object[0]);
        if (i <= 0 || str == null || str.isEmpty() || componentProvider == null) {
            throw new IllegalArgumentException("formId or clientStub is invalid");
        }
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            int applyType = componentProvider.getApplyType();
            if (componentProvider.setApplyType(1)) {
                try {
                    if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
                        return false;
                    }
                    if (!obtain.writeInt(i)) {
                        reclaimParcel(obtain, obtain2);
                        return false;
                    } else if (!obtain.writeString(str)) {
                        reclaimParcel(obtain, obtain2);
                        return false;
                    } else if (!obtain.writeBoolean(false)) {
                        reclaimParcel(obtain, obtain2);
                        return false;
                    } else {
                        obtain.writeSequenceable(componentProvider);
                        if (!this.remote.sendRequest(4, obtain, obtain2, new MessageOption())) {
                            HiLog.error(LABEL_LOG, "FormManager::updateForm sendRequest failed", new Object[0]);
                            reclaimParcel(obtain, obtain2);
                            return false;
                        } else if (obtain2.readInt() != 0) {
                            reclaimParcel(obtain, obtain2);
                            return false;
                        } else if (componentProvider.setApplyType(applyType)) {
                            reclaimParcel(obtain, obtain2);
                            return true;
                        } else {
                            HiLog.error(LABEL_LOG, "updateForm set apply type error. after marshalling actions in remote views.", new Object[0]);
                            throw new RemoteException();
                        }
                    }
                } finally {
                    reclaimParcel(obtain, obtain2);
                }
            } else {
                HiLog.error(LABEL_LOG, "updateForm set apply type error. before marshalling actions in remote views.", new Object[0]);
                throw new RemoteException();
            }
        } else {
            throw new RemoteException("failed to get fms");
        }
    }

    public boolean updateForm(int i, String str, FormBindingData formBindingData) throws IllegalArgumentException, RemoteException {
        HiLog.info(LABEL_LOG, "update js form.", new Object[0]);
        if (i <= 0 || str == null || str.isEmpty() || formBindingData == null) {
            throw new IllegalArgumentException("formId or clientStub is invalid");
        }
        if (resetFlag) {
            resetRemoteObject();
        }
        if (this.remote != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
                    return false;
                }
                if (!obtain.writeInt(i)) {
                    reclaimParcel(obtain, obtain2);
                    return false;
                } else if (!obtain.writeString(str)) {
                    reclaimParcel(obtain, obtain2);
                    return false;
                } else {
                    boolean z = true;
                    if (!obtain.writeBoolean(true)) {
                        reclaimParcel(obtain, obtain2);
                        return false;
                    }
                    obtain.writeSequenceable(formBindingData);
                    if (!this.remote.sendRequest(4, obtain, obtain2, new MessageOption())) {
                        HiLog.error(LABEL_LOG, "FormManager::updateForm sendRequest failed", new Object[0]);
                        reclaimParcel(obtain, obtain2);
                        return false;
                    }
                    if (obtain2.readInt() != 0) {
                        z = false;
                    }
                    reclaimParcel(obtain, obtain2);
                    return z;
                }
            } finally {
                reclaimParcel(obtain, obtain2);
            }
        } else {
            throw new RemoteException("failed to get fms");
        }
    }

    /* access modifiers changed from: package-private */
    public void resetRemoteObject() {
        synchronized (INSTANCE_LOCK) {
            if (resetFlag) {
                IRemoteObject sysAbility = SysAbilityManager.getSysAbility(403);
                if (sysAbility == null) {
                    HiLog.info(LABEL_LOG, "BundleManager reset remoteObject failed, remote is null", new Object[0]);
                    return;
                }
                if (!sysAbility.addDeathRecipient(new FormManagerDeathRecipient(), 0)) {
                    HiLog.info(LABEL_LOG, "BundleManager register BundleManagerDeathRecipient failed", new Object[0]);
                }
                this.remote = sysAbility;
                resetFlag = false;
            }
        }
    }

    private void reclaimParcel(MessageParcel messageParcel, MessageParcel messageParcel2) {
        messageParcel.reclaim();
        messageParcel2.reclaim();
    }

    protected static void resetFlag() {
        resetFlag = true;
    }

    /* access modifiers changed from: private */
    public static class FormManagerDeathRecipient implements IRemoteObject.DeathRecipient {
        private static final int SLEEP_TIME = 1000;
        private int timeout;

        private FormManagerDeathRecipient() {
            this.timeout = 30;
        }

        public void onRemoteDied() {
            IRemoteObject sysAbility = SysAbilityManager.getSysAbility(403);
            while (sysAbility == null) {
                if (this.timeout > 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException unused) {
                        HiLog.info(FormManager.LABEL_LOG, "onRemoteDied::InterruptedException exception", new Object[0]);
                    }
                    sysAbility = SysAbilityManager.getSysAbility(403);
                    this.timeout--;
                } else {
                    HiLog.info(FormManager.LABEL_LOG, "onRemoteDied::getSysAbility failed", new Object[0]);
                    return;
                }
            }
            FormManager.resetFlag();
        }
    }
}
