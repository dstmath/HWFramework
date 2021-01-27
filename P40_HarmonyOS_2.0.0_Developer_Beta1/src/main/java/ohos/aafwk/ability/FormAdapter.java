package ohos.aafwk.ability;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import ohos.aafwk.ability.IAbilityFormProvider;
import ohos.aafwk.content.Intent;
import ohos.agp.components.ComponentProvider;
import ohos.bluetooth.A2dpCodecInfo;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.powermanager.PowerManager;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.system.Parameters;

public class FormAdapter {
    private static final int CODE_FORM_OFFSET = 8585216;
    private static final int ERR_BIND_SUPPLIER_FAILED = 8585226;
    private static final int ERR_CFG_NOT_MATCH_ID = 8585224;
    private static final int ERR_CODE_COMMON = 8585217;
    private static final int ERR_CODE_OK = 0;
    private static final int ERR_DEL_FORM_NOT_SELF = 8585229;
    private static final int ERR_FORM_INVALID_PARAM = 8585223;
    private static final int ERR_INVALID_PARAM = -2;
    private static final int ERR_MAX_INSTANCES_PER_FORM = 8585228;
    private static final int ERR_MAX_SYSTEM_FORMS = 8585227;
    private static final int ERR_NOT_EXIST_ID = 8585225;
    private static final int ERR_SUPPLIER_DEL_FAIL = 8585230;
    private static final String FORM_SUFFIX = "ShellServiceForm";
    private static final String JS_MESSAGE_KEY = "ohos.extra.param.key.message";
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 218108160, "FormAdapter");
    private static int LARGEST_FORM_NUM = A2dpCodecInfo.CODEC_PRIORITY_HIGHEST;
    private static final int MAX_CONFIG_DURATION = 336;
    private static final int MAX_FORMS = 500;
    private static final int MAX_HOUR = 23;
    private static final int MAX_INSTANCE_PER_FORM = 32;
    private static final int MAX_MININUTE = 59;
    private static final long MAX_PERIOD;
    private static final int MIN_CONFIG_DURATION = 1;
    private static final long MIN_PERIOD;
    private static final int MIN_TIME = 0;
    private static final String SCHEME_PACKAGE = "package";
    static final String SYSTEM_PARAM_FORM_UPDATE_TIME = "persist.sys.fms.form.update.time";
    private static final int TIMER_CONFIG_UNIT = 30;
    private static final long TIME_CONVERSION = ((long) ((Parameters.getInt(SYSTEM_PARAM_FORM_UPDATE_TIME, 30) * 60) * 1000));
    private static final String TIME_DELIMETER = ":";
    private static final int UPDATE_AT_CONFIG_COUNT = 2;
    private static Context aContext = null;
    private static int baseId = 1;
    private final Object FORM_LOCK;
    private BundleReceiver bundleReceiver;
    private List<FormHostRecord> clientRecords;
    private int currentId;
    private BitSet formIdSet;
    private boolean formIdSetInited;
    private HashMap<Integer, FormRecord> formRecords;
    private PowerManager pownerManager;

    private native boolean nativeDeleteFormId(int i);

    private native ArrayList<Integer> nativeGetAllFormId();

    private native void nativeInit();

    private native boolean nativeQueryFormId(int i);

    private native boolean nativeSaveFormId(int i);

    static {
        long j = TIME_CONVERSION;
        MIN_PERIOD = 1 * j;
        MAX_PERIOD = j * 336;
        try {
            HiLog.info(LABEL_LOG, "Load form mgr jni so", new Object[0]);
            System.loadLibrary("formmgr_jni.z");
        } catch (UnsatisfiedLinkError unused) {
            HiLog.warn(LABEL_LOG, "ERROR: Could not load formmgr_jni.z.so ", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    public static class Holder {
        private static final FormAdapter INSTANCE = new FormAdapter();

        private Holder() {
        }
    }

    public static FormAdapter getInstance() {
        return Holder.INSTANCE;
    }

    private FormAdapter() {
        this.FORM_LOCK = new Object();
        this.formIdSetInited = false;
        this.formIdSet = new BitSet(LARGEST_FORM_NUM);
        this.currentId = 0;
        this.formRecords = new HashMap<>();
        this.clientRecords = new ArrayList();
        this.pownerManager = new PowerManager();
        this.bundleReceiver = null;
    }

    public void init(Context context) {
        HiLog.info(LABEL_LOG, "FormAdapter init begin", new Object[0]);
        if (context == null) {
            HiLog.error(LABEL_LOG, "init failed, context is null", new Object[0]);
            return;
        }
        aContext = context;
        initReceiver(context);
        nativeInit();
    }

    public int addForm(FormItemInfo formItemInfo, long j, long j2) {
        HiLog.info(LABEL_LOG, "addForm begin here", new Object[0]);
        MessageParcel create = MessageParcel.create(j);
        MessageParcel create2 = MessageParcel.create(j2);
        IRemoteObject readRemoteObject = create.readRemoteObject();
        if (isAddValid(formItemInfo, readRemoteObject)) {
            return handleAddForm(formItemInfo, readRemoteObject, create2);
        }
        HiLog.error(LABEL_LOG, "addForm invalid param", new Object[0]);
        create2.writeInt((int) ERR_FORM_INVALID_PARAM);
        return ERR_FORM_INVALID_PARAM;
    }

    public int deleteForm(int i, boolean z, long j) {
        boolean z2 = true;
        HiLog.info(LABEL_LOG, "deleteForm begin here, formId:%{public}d, isReleaseForm: %{public}b", new Object[]{Integer.valueOf(i), Boolean.valueOf(z)});
        IRemoteObject readRemoteObject = MessageParcel.create(j).readRemoteObject();
        if (i <= 0 || readRemoteObject == null) {
            HiLog.error(LABEL_LOG, "deleteForm invalid param", new Object[0]);
            return ERR_FORM_INVALID_PARAM;
        }
        synchronized (this.FORM_LOCK) {
            Iterator<FormHostRecord> it = this.clientRecords.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                FormHostRecord next = it.next();
                if (readRemoteObject.equals(next.clientStub)) {
                    if (!next.contains(i)) {
                        return ERR_DEL_FORM_NOT_SELF;
                    }
                    HiLog.debug(LABEL_LOG, "deleteForm find target client, and the formHost number is %{private}d before delete", new Object[]{Integer.valueOf(this.clientRecords.size())});
                    next.delForm(i);
                    if (next.isEmpty()) {
                        it.remove();
                    }
                }
            }
            if (!this.formRecords.containsKey(Integer.valueOf(i)) || hasHostLocked(i)) {
                z2 = false;
            } else if (z) {
                HiLog.debug(LABEL_LOG, "no host need this form, reserve form cache, and the form number is %{private}d before delete", new Object[]{Integer.valueOf(this.formRecords.size())});
                return 0;
            } else {
                HiLog.debug(LABEL_LOG, "no host need this form, remove it from cache, and the form number is %{private}d before delete", new Object[]{Integer.valueOf(this.formRecords.size())});
                int notifyProviderFormDelete = notifyProviderFormDelete(i, this.formRecords.get(Integer.valueOf(i)));
                if (notifyProviderFormDelete != 0) {
                    HiLog.error(LABEL_LOG, "notifyProviderFormDelete failed!", new Object[0]);
                    return notifyProviderFormDelete;
                }
                deleteFormId(i);
                this.formRecords.remove(Integer.valueOf(i));
            }
        }
        if (z2) {
            FormTimerManager.getInstance().deleteFormTimer(i);
        }
        return 0;
    }

    public int updateForm(int i, String str, long j) {
        FormBindingData formBindingData;
        HiLog.info(LABEL_LOG, "updateForm begin here, formId:%{public}d", new Object[]{Integer.valueOf(i)});
        if (i <= 0 || str == null || str.isEmpty()) {
            HiLog.error(LABEL_LOG, "update form formId or bundleName is invalid", new Object[0]);
            return -2;
        }
        MessageParcel create = MessageParcel.create(j);
        boolean readBoolean = create.readBoolean();
        ComponentProvider componentProvider = null;
        if (readBoolean) {
            formBindingData = new FormBindingData();
            if (!create.readSequenceable(formBindingData)) {
                HiLog.error(LABEL_LOG, "update form bindingData is invalid", new Object[0]);
                return -2;
            }
        } else {
            ComponentProvider componentProvider2 = new ComponentProvider();
            if (!create.readSequenceable(componentProvider2)) {
                HiLog.error(LABEL_LOG, "updateform remoteComponent is invalid", new Object[0]);
                return -2;
            }
            formBindingData = null;
            componentProvider = componentProvider2;
        }
        synchronized (this.FORM_LOCK) {
            FormRecord formRecord = this.formRecords.get(Integer.valueOf(i));
            if (formRecord == null) {
                HiLog.error(LABEL_LOG, "updateform, not exist such form:%{public}d", new Object[]{Integer.valueOf(i)});
                return -2;
            } else if (!str.equals(formRecord.bundleName)) {
                HiLog.error(LABEL_LOG, "updateform, not match bundleName:%{public}d", new Object[]{str});
                return -2;
            } else if (formRecord.isJsForm != readBoolean) {
                HiLog.error(LABEL_LOG, "updateform, not match js form flag:%{public}d", new Object[]{Boolean.valueOf(readBoolean)});
                return -2;
            } else {
                if (readBoolean) {
                    formRecord.instantProvider.setFormBindingData(formBindingData);
                } else {
                    formRecord.formView = componentProvider;
                }
                for (FormHostRecord formHostRecord : this.clientRecords) {
                    if (formHostRecord.contains(i)) {
                        formHostRecord.setNeedRefresh(i, true);
                    }
                }
                HiLog.debug(LABEL_LOG, "the form number is %{private}d when update, the formHost number is %{private}d when update.", new Object[]{Integer.valueOf(this.formRecords.size()), Integer.valueOf(this.clientRecords.size())});
                if (!isScreenOn()) {
                    HiLog.debug(LABEL_LOG, "screen off, do not initiative refresh", new Object[0]);
                    return 0;
                }
                for (FormHostRecord formHostRecord2 : this.clientRecords) {
                    if (formHostRecord2.isEnableRefresh(i)) {
                        formHostRecord2.onUpdate(i, formRecord);
                        formHostRecord2.setNeedRefresh(i, false);
                    }
                }
                return 0;
            }
        }
    }

    public int enableUpdateForm(List<Integer> list, long j) {
        HiLog.info(LABEL_LOG, "enableUpdateForm", new Object[0]);
        return handleUpdateFormFlag(list, j, true);
    }

    public int disableUpdateForm(List<Integer> list, long j) {
        HiLog.info(LABEL_LOG, "disableUpdateForm", new Object[0]);
        return handleUpdateFormFlag(list, j, false);
    }

    public int requestForm(int i, long j) {
        HiLog.info(LABEL_LOG, "requestForm", new Object[0]);
        MessageParcel create = MessageParcel.create(j);
        Intent intent = new Intent();
        if (!create.readSequenceable(intent)) {
            HiLog.error(LABEL_LOG, "requestForm read intent failed", new Object[0]);
            return -2;
        }
        IRemoteObject readRemoteObject = create.readRemoteObject();
        if (i <= 0 || readRemoteObject == null) {
            HiLog.error(LABEL_LOG, "requestForm invalid param", new Object[0]);
            return -2;
        }
        synchronized (this.FORM_LOCK) {
            for (FormHostRecord formHostRecord : this.clientRecords) {
                if (readRemoteObject.equals(formHostRecord.clientStub)) {
                    if (!formHostRecord.contains(i)) {
                        HiLog.error(LABEL_LOG, "requestForm form is not self-owned", new Object[0]);
                        return ERR_CODE_COMMON;
                    }
                    HiLog.debug(LABEL_LOG, "requestForm find target client", new Object[0]);
                    getInstance().refreshForm(i, intent);
                    return 0;
                }
            }
            HiLog.debug(LABEL_LOG, "requestForm cannot find target client", new Object[0]);
            return -2;
        }
    }

    /* access modifiers changed from: package-private */
    public Context getContext() {
        return aContext;
    }

    /* access modifiers changed from: package-private */
    public boolean isScreenOn() {
        return this.pownerManager.isScreenOn();
    }

    /* access modifiers changed from: package-private */
    public void handleAcquireBack(int i, ProviderFormInfo providerFormInfo, FormHostRecord formHostRecord) {
        if (providerFormInfo != null && formHostRecord != null) {
            RefreshRunnable refreshRunnable = null;
            synchronized (this.FORM_LOCK) {
                FormRecord formRecord = this.formRecords.get(Integer.valueOf(i));
                if (formRecord == null) {
                    HiLog.error(LABEL_LOG, "handleAcquireBack, not exist such form:%{public}d", new Object[]{Integer.valueOf(i)});
                    return;
                }
                if (!providerFormInfo.isJsForm()) {
                    formRecord.formView = providerFormInfo.getComponentProvider();
                } else if (formRecord.instantProvider == null) {
                    HiLog.error(LABEL_LOG, "handleAcquireBack error: js instant provider is null", new Object[0]);
                    return;
                } else {
                    formRecord.instantProvider.setFormBindingData(providerFormInfo.getJsBindingData());
                }
                if (formHostRecord.contains(i)) {
                    formHostRecord.onAcquire(i, formRecord);
                }
                if (formRecord.isEnableUpdate) {
                    if (formRecord.updateDuration > 0) {
                        refreshRunnable = new RefreshRunnable(i, formRecord.updateDuration);
                    } else {
                        refreshRunnable = new RefreshRunnable(i, formRecord.updateAtHour, formRecord.updateAtMin);
                    }
                }
            }
            if (refreshRunnable != null) {
                FormTimerManager.getInstance().addFormTimer(refreshRunnable);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleHostDied(IRemoteObject iRemoteObject) {
        HiLog.info(LABEL_LOG, "remote client died", new Object[0]);
        if (iRemoteObject == null) {
            HiLog.info(LABEL_LOG, "remote client died, invalid param", new Object[0]);
            return;
        }
        synchronized (this.FORM_LOCK) {
            Iterator<FormHostRecord> it = this.clientRecords.iterator();
            while (it.hasNext()) {
                if (iRemoteObject.equals(it.next().clientStub)) {
                    HiLog.info(LABEL_LOG, "find died client, remove it", new Object[0]);
                    it.remove();
                }
            }
            Iterator<Map.Entry<Integer, FormRecord>> it2 = this.formRecords.entrySet().iterator();
            while (it2.hasNext()) {
                if (!hasHostLocked(it2.next().getKey().intValue())) {
                    HiLog.info(LABEL_LOG, "no host need this form, remove it", new Object[0]);
                    it2.remove();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleBundleRemoved(String str) {
        HiLog.debug(LABEL_LOG, "handleBundleRemoved in, bundleName:%{public}s", new Object[]{str});
        if (str == null) {
            HiLog.error(LABEL_LOG, "handleBundleRemoved bundleName invalid", new Object[0]);
            return;
        }
        synchronized (this.FORM_LOCK) {
            ArrayList<Integer> arrayList = new ArrayList();
            Iterator<Map.Entry<Integer, FormRecord>> it = this.formRecords.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, FormRecord> next = it.next();
                if (str.equals(next.getValue().bundleName)) {
                    HiLog.debug(LABEL_LOG, "handleBundleRemoved find matched FormRecord", new Object[0]);
                    int intValue = next.getKey().intValue();
                    arrayList.add(Integer.valueOf(intValue));
                    deleteFormId(intValue);
                    it.remove();
                }
            }
            if (!arrayList.isEmpty()) {
                for (FormHostRecord formHostRecord : this.clientRecords) {
                    ArrayList arrayList2 = new ArrayList();
                    for (Integer num : arrayList) {
                        int intValue2 = num.intValue();
                        if (formHostRecord.contains(intValue2)) {
                            arrayList2.add(Integer.valueOf(intValue2));
                            formHostRecord.delForm(intValue2);
                        }
                    }
                    if (!arrayList2.isEmpty()) {
                        formHostRecord.onFormUninstalled(arrayList2);
                    }
                }
            }
        }
    }

    private void ensureFormIdSetInitialed() {
        if (!this.formIdSetInited) {
            ArrayList<Integer> nativeGetAllFormId = nativeGetAllFormId();
            if (nativeGetAllFormId != null) {
                Iterator<Integer> it = nativeGetAllFormId.iterator();
                while (it.hasNext()) {
                    int intValue = it.next().intValue();
                    if (intValue > 0 && intValue < LARGEST_FORM_NUM) {
                        HiLog.debug(LABEL_LOG, "ensureFormIdSetInitialed, intial formId:%{public}d", new Object[]{Integer.valueOf(intValue)});
                        this.formIdSet.set(intValue);
                    }
                }
            }
            this.formIdSetInited = true;
        }
    }

    private int handleUpdateFormFlag(List<Integer> list, long j, boolean z) {
        IRemoteObject readRemoteObject = MessageParcel.create(j).readRemoteObject();
        if (list == null || list.isEmpty() || readRemoteObject == null) {
            HiLog.error(LABEL_LOG, "handleUpdateFormFlag invalid param", new Object[0]);
            return -2;
        }
        synchronized (this.FORM_LOCK) {
            FormHostRecord formHostRecord = null;
            Iterator<FormHostRecord> it = this.clientRecords.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                FormHostRecord next = it.next();
                if (readRemoteObject.equals(next.clientStub)) {
                    formHostRecord = next;
                    break;
                }
            }
            if (formHostRecord == null) {
                return -2;
            }
            HiLog.debug(LABEL_LOG, "handleUpdateFormFlag find target client", new Object[0]);
            for (Integer num : list) {
                int intValue = num.intValue();
                if (formHostRecord.contains(intValue)) {
                    formHostRecord.setEnableRefresh(intValue, z);
                    if (z) {
                        if (formHostRecord.isNeedRefresh(intValue)) {
                            FormRecord formRecord = this.formRecords.get(Integer.valueOf(intValue));
                            if (formRecord == null) {
                                HiLog.warn(LABEL_LOG, "handleUpdateFormFlag, not exist such form:%{public}d", new Object[]{Integer.valueOf(intValue)});
                            } else {
                                formHostRecord.onUpdate(intValue, formRecord);
                                formHostRecord.setNeedRefresh(intValue, false);
                            }
                        }
                    }
                }
            }
            return 0;
        }
    }

    private boolean hasHostLocked(int i) {
        for (FormHostRecord formHostRecord : this.clientRecords) {
            if (formHostRecord.contains(i)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAddValid(FormItemInfo formItemInfo, IRemoteObject iRemoteObject) {
        if (formItemInfo == null || !formItemInfo.isValidItem() || iRemoteObject == null) {
            return false;
        }
        if (formItemInfo.isJsForm() || ComponentUtils.initLayout(aContext, formItemInfo)) {
            return true;
        }
        HiLog.error(LABEL_LOG, "isAddValid initLayout failed", new Object[0]);
        return false;
    }

    private int handleAddForm(FormItemInfo formItemInfo, IRemoteObject iRemoteObject, MessageParcel messageParcel) {
        synchronized (this.FORM_LOCK) {
            ensureFormIdSetInitialed();
            if (formItemInfo.getFormId() > 0) {
                HiLog.debug(LABEL_LOG, "addForm formId > 0", new Object[0]);
                return handleAddFormById(formItemInfo, iRemoteObject, messageParcel);
            }
            return handleAddFormByInfo(formItemInfo, iRemoteObject, messageParcel);
        }
    }

    private FormHostRecord findOrNewFormHostRecord(FormItemInfo formItemInfo, IRemoteObject iRemoteObject) {
        for (FormHostRecord formHostRecord : this.clientRecords) {
            if (iRemoteObject.equals(formHostRecord.clientStub)) {
                return formHostRecord;
            }
        }
        FormHostRecord createRecord = FormHostRecord.createRecord(formItemInfo.isESystem(), iRemoteObject);
        if (createRecord != null) {
            this.clientRecords.add(createRecord);
        }
        return createRecord;
    }

    private int handleAddFormById(FormItemInfo formItemInfo, IRemoteObject iRemoteObject, MessageParcel messageParcel) {
        HiLog.info(LABEL_LOG, "addForm handleAddFormById", new Object[0]);
        int formId = formItemInfo.getFormId();
        FormRecord formRecord = this.formRecords.get(Integer.valueOf(formId));
        if (formRecord != null) {
            if (formItemInfo.isMatch(formRecord)) {
                return handleAddExistFormRecord(formItemInfo, iRemoteObject, messageParcel, formRecord, formId);
            }
            messageParcel.writeInt((int) ERR_CFG_NOT_MATCH_ID);
            HiLog.error(LABEL_LOG, "formId and item info not match:%{public}d", new Object[]{Integer.valueOf(formId)});
            return ERR_CFG_NOT_MATCH_ID;
        } else if (this.formIdSet.get(formId)) {
            HiLog.debug(LABEL_LOG, "addForm form id in db but not in cache", new Object[0]);
            return handleAddNewFormRecord(formItemInfo, iRemoteObject, messageParcel, formId);
        } else {
            messageParcel.writeInt((int) ERR_NOT_EXIST_ID);
            HiLog.error(LABEL_LOG, "addForm no such form %{public}d", new Object[]{Integer.valueOf(formId)});
            return ERR_NOT_EXIST_ID;
        }
    }

    private int handleAddFormByInfo(FormItemInfo formItemInfo, IRemoteObject iRemoteObject, MessageParcel messageParcel) {
        HiLog.info(LABEL_LOG, "addForm handleAddFormByInfo", new Object[0]);
        int checkEnoughForm = checkEnoughForm(formItemInfo);
        if (checkEnoughForm != 0) {
            messageParcel.writeInt(checkEnoughForm);
            HiLog.error(LABEL_LOG, "too mush forms in system", new Object[0]);
            return checkEnoughForm;
        }
        int generateNextFormId = generateNextFormId();
        if (generateNextFormId >= 0) {
            return handleAddNewFormRecord(formItemInfo, iRemoteObject, messageParcel, generateNextFormId);
        }
        messageParcel.writeInt((int) ERR_CODE_COMMON);
        HiLog.error(LABEL_LOG, "addForm generateNextFormId no invalid formId, too mush forms in system", new Object[0]);
        return ERR_CODE_COMMON;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x003c  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0040  */
    private int handleAddExistFormRecord(FormItemInfo formItemInfo, IRemoteObject iRemoteObject, MessageParcel messageParcel, FormRecord formRecord, int i) {
        int i2;
        HiLog.info(LABEL_LOG, "handleAddExistFormRecord in", new Object[0]);
        FormHostRecord findOrNewFormHostRecord = findOrNewFormHostRecord(formItemInfo, iRemoteObject);
        if (findOrNewFormHostRecord == null) {
            messageParcel.writeInt((int) ERR_CODE_COMMON);
            HiLog.error(LABEL_LOG, "findOrNewFormHostRecord failed, when get matched formRecord", new Object[0]);
            return ERR_CODE_COMMON;
        }
        findOrNewFormHostRecord.addForm(i);
        if (formRecord.isJsForm) {
            if (formRecord.instantProvider == null) {
                i2 = bindSupplier(i, formItemInfo, findOrNewFormHostRecord);
                if (i2 != 0) {
                    messageParcel.writeInt(i2);
                    return i2;
                }
                if (formItemInfo.isESystem()) {
                    ESystemHostCallback.replyFormRecord(i, messageParcel, formRecord);
                } else {
                    OhosHostCallback.replyFormRecord(i, messageParcel, formRecord);
                }
                return 0;
            }
        } else if (formRecord.formView == null) {
            i2 = bindSupplier(i, formItemInfo, findOrNewFormHostRecord);
            if (i2 != 0) {
            }
        }
        i2 = 0;
        if (i2 != 0) {
        }
    }

    private int handleAddNewFormRecord(FormItemInfo formItemInfo, IRemoteObject iRemoteObject, MessageParcel messageParcel, int i) {
        FormHostRecord findOrNewFormHostRecord = findOrNewFormHostRecord(formItemInfo, iRemoteObject);
        if (findOrNewFormHostRecord == null) {
            messageParcel.writeInt((int) ERR_CODE_COMMON);
            HiLog.error(LABEL_LOG, "addForm findOrNewFormHostRecord record failed when no matched formRecord", new Object[0]);
            return ERR_CODE_COMMON;
        }
        findOrNewFormHostRecord.addForm(i);
        FormRecord formRecord = new FormRecord();
        formRecord.bundleName = formItemInfo.getBundleName();
        formRecord.moduleName = formItemInfo.getModuleName();
        formRecord.abilityName = formItemInfo.getAbilityName();
        formRecord.formName = formItemInfo.getFormName();
        formRecord.specification = formItemInfo.getSpecificationId();
        formRecord.isEnableUpdate = formItemInfo.isEnableUpdateFlag();
        if (formRecord.isEnableUpdate) {
            parseUpdateConfig(formRecord, formItemInfo);
        }
        formRecord.formView = null;
        formRecord.isJsForm = formItemInfo.isJsForm();
        if (formRecord.isJsForm) {
            formRecord.instantProvider = new InstantProvider(formItemInfo.getJsComponentName(), formItemInfo.getHapSourceByModuleName(formItemInfo.getAbilityModuleName()));
        }
        formRecord.hapSourceDirs = formItemInfo.getHapSourceDirs();
        formRecord.previewLayoutId = formItemInfo.previewLayoutId;
        formRecord.eSystemPreviewLayoutId = formItemInfo.eSystemPreviewLayoutId;
        this.formRecords.put(Integer.valueOf(i), formRecord);
        if (bindSupplier(i, formItemInfo, findOrNewFormHostRecord) != 0) {
            messageParcel.writeInt((int) ERR_BIND_SUPPLIER_FAILED);
            HiLog.error(LABEL_LOG, "addForm bindSupplier failed", new Object[0]);
            return ERR_BIND_SUPPLIER_FAILED;
        }
        if (formItemInfo.isESystem()) {
            ESystemHostCallback.replyFormRecord(i, messageParcel, formRecord);
        } else {
            OhosHostCallback.replyFormRecord(i, messageParcel, formRecord);
        }
        saveFormId(i);
        return 0;
    }

    private int notifyProviderFormDelete(int i, FormRecord formRecord) {
        if (formRecord == null) {
            HiLog.error(LABEL_LOG, "formRecord is null.", new Object[0]);
            return ERR_CODE_COMMON;
        }
        String str = formRecord.bundleName;
        String str2 = formRecord.abilityName;
        android.content.Intent intent = new android.content.Intent();
        intent.setComponent(new ComponentName(str, str2 + FORM_SUFFIX));
        intent.setFlags(32);
        try {
            if (aContext.bindService(intent, new DeletionConnection(i), 1)) {
                return 0;
            }
            HiLog.error(LABEL_LOG, "bind service failed.", new Object[0]);
            return ERR_SUPPLIER_DEL_FAIL;
        } catch (SecurityException unused) {
            HiLog.error(LABEL_LOG, "bind service exception", new Object[0]);
            return ERR_SUPPLIER_DEL_FAIL;
        }
    }

    private int checkEnoughForm(FormItemInfo formItemInfo) {
        if (this.formRecords.size() >= 500) {
            HiLog.warn(LABEL_LOG, "already exist 500 forms in system", new Object[0]);
            return ERR_MAX_SYSTEM_FORMS;
        }
        int i = 0;
        for (FormRecord formRecord : this.formRecords.values()) {
            if (formItemInfo.isSameFormConfig(formRecord) && (i = i + 1) >= 32) {
                HiLog.warn(LABEL_LOG, "support most 32 instance per form", new Object[0]);
                return ERR_MAX_INSTANCES_PER_FORM;
            }
        }
        return 0;
    }

    private void parseUpdateConfig(FormRecord formRecord, FormItemInfo formItemInfo) {
        int updateDuration = formItemInfo.getUpdateDuration();
        if (updateDuration > 0) {
            parseAsUpdateInterval(formRecord, updateDuration);
        } else {
            parseAsUpdateAt(formRecord, formItemInfo);
        }
    }

    private void parseAsUpdateAt(FormRecord formRecord, FormItemInfo formItemInfo) {
        formRecord.isEnableUpdate = false;
        formRecord.updateDuration = 0;
        String scheduledUpdateTime = formItemInfo.getScheduledUpdateTime();
        HiLog.info(LABEL_LOG, "parseAsUpdateAt updateAt:%{public}s", new Object[]{scheduledUpdateTime});
        if (!scheduledUpdateTime.isEmpty()) {
            String[] split = scheduledUpdateTime.split(TIME_DELIMETER);
            if (split.length != 2) {
                HiLog.error(LABEL_LOG, "parseAsUpdateAt invalid config", new Object[0]);
                return;
            }
            try {
                int parseInt = Integer.parseInt(split[0]);
                int parseInt2 = Integer.parseInt(split[1]);
                if (parseInt < 0 || parseInt > 23 || parseInt2 < 0 || parseInt2 > 59) {
                    HiLog.error(LABEL_LOG, "parseAsUpdateAt time is invalid", new Object[0]);
                    return;
                }
                formRecord.updateAtHour = parseInt;
                formRecord.updateAtMin = parseInt2;
                formRecord.isEnableUpdate = true;
            } catch (NumberFormatException unused) {
                HiLog.error(LABEL_LOG, "parseAsUpdateAt invalid hour or min", new Object[0]);
            }
        }
    }

    private void parseAsUpdateInterval(FormRecord formRecord, int i) {
        if (i <= 1) {
            formRecord.updateDuration = MIN_PERIOD;
        } else if (i >= 336) {
            formRecord.updateDuration = MAX_PERIOD;
        } else {
            formRecord.updateDuration = ((long) i) * TIME_CONVERSION;
        }
    }

    private int generateNextFormId() {
        int i = this.currentId + 1;
        if (i <= 0 || i >= LARGEST_FORM_NUM) {
            i = 1;
        }
        this.currentId = this.formIdSet.nextClearBit(i);
        return this.currentId;
    }

    private void saveFormId(int i) {
        this.formIdSet.set(i);
        nativeSaveFormId(i);
    }

    private void deleteFormId(int i) {
        this.formIdSet.clear(i);
        nativeDeleteFormId(i);
    }

    private int bindSupplier(int i, FormItemInfo formItemInfo, FormHostRecord formHostRecord) {
        HiLogLabel hiLogLabel = LABEL_LOG;
        HiLog.debug(hiLogLabel, "addForm bindService,package:%{public}s, class:%{public}s", new Object[]{formItemInfo.getBundleName(), formItemInfo.getAbilityName() + FORM_SUFFIX});
        FormConnection formConnection = new FormConnection(i, formHostRecord, formItemInfo);
        android.content.Intent intent = new android.content.Intent();
        String bundleName = formItemInfo.getBundleName();
        intent.setComponent(new ComponentName(bundleName, formItemInfo.getAbilityName() + FORM_SUFFIX));
        intent.addFlags(32);
        try {
            if (aContext.bindService(intent, formConnection, 1)) {
                return 0;
            }
            HiLog.error(LABEL_LOG, "bindService failed", new Object[0]);
            return ERR_BIND_SUPPLIER_FAILED;
        } catch (SecurityException unused) {
            HiLog.error(LABEL_LOG, "bindServicebind error", new Object[0]);
            return ERR_BIND_SUPPLIER_FAILED;
        }
    }

    private void reBindSupplier(int i, FormRecord formRecord, Intent intent) {
        if (formRecord != null) {
            HiLogLabel hiLogLabel = LABEL_LOG;
            HiLog.debug(hiLogLabel, "refresh bindService,package:%{public}s, class:%{public}s", new Object[]{formRecord.bundleName, formRecord.abilityName + FORM_SUFFIX});
            RefreshConnection refreshConnection = new RefreshConnection(i, intent);
            android.content.Intent intent2 = new android.content.Intent();
            String str = formRecord.bundleName;
            intent2.setComponent(new ComponentName(str, formRecord.abilityName + FORM_SUFFIX));
            intent2.addFlags(32);
            try {
                if (!aContext.bindService(intent2, refreshConnection, 1)) {
                    HiLog.error(LABEL_LOG, "bindService failed", new Object[0]);
                }
            } catch (SecurityException unused) {
                HiLog.error(LABEL_LOG, "bindServicebind error", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void refreshForm(int i, Intent intent) {
        HiLog.debug(LABEL_LOG, "refresh form,formId:%{public}d", new Object[]{Integer.valueOf(i)});
        if (!isScreenOn()) {
            HiLog.debug(LABEL_LOG, "screen off, do not refresh", new Object[0]);
            return;
        }
        synchronized (this.FORM_LOCK) {
            FormRecord formRecord = this.formRecords.get(Integer.valueOf(i));
            if (formRecord == null) {
                HiLog.error(LABEL_LOG, "not exist such form:%{public}d", new Object[]{Integer.valueOf(i)});
                return;
            }
            HiLog.debug(LABEL_LOG, "the form number is %{private}d before refreshed, the formHost number is %{private}d before refreshed.", new Object[]{Integer.valueOf(this.formRecords.size()), Integer.valueOf(this.clientRecords.size())});
            reBindSupplier(i, formRecord, intent);
        }
    }

    private void initReceiver(Context context) {
        this.bundleReceiver = new BundleReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme(SCHEME_PACKAGE);
        context.registerReceiver(this.bundleReceiver, intentFilter);
    }

    /* access modifiers changed from: package-private */
    public static class RefreshRunnable implements Runnable {
        final int formId;
        final int hour;
        final boolean isUpdateAt;
        final int min;
        final long period;
        long refreshTime = Long.MAX_VALUE;

        RefreshRunnable(int i, long j) {
            this.formId = i;
            this.period = j;
            this.hour = -1;
            this.min = -1;
            this.isUpdateAt = false;
        }

        RefreshRunnable(int i, int i2, int i3) {
            this.formId = i;
            this.hour = i2;
            this.min = i3;
            this.period = -1;
            this.isUpdateAt = true;
        }

        @Override // java.lang.Runnable
        public void run() {
            FormAdapter.getInstance().refreshForm(this.formId, new Intent());
        }
    }

    /* access modifiers changed from: private */
    public static class RefreshConnection implements ServiceConnection {
        private int formId;
        private Intent intent;

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
        }

        RefreshConnection(int i, Intent intent2) {
            this.formId = i;
            this.intent = intent2;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            HiLog.debug(FormAdapter.LABEL_LOG, "refresh onServiceConnected.", new Object[0]);
            IRemoteObject iRemoteObject = (IRemoteObject) IPCAdapter.translateToIRemoteObject(iBinder).orElse(null);
            if (iRemoteObject == null) {
                HiLog.error(FormAdapter.LABEL_LOG, "refresh onServiceConnected failed to get supplier remote object.", new Object[0]);
                return;
            }
            IAbilityFormProvider asProxy = IAbilityFormProvider.FormProviderStub.asProxy(iRemoteObject);
            try {
                if (this.intent.hasParameter(FormAdapter.JS_MESSAGE_KEY)) {
                    asProxy.fireFormEvent(this.formId, this.intent.getStringParam(FormAdapter.JS_MESSAGE_KEY));
                    return;
                }
                asProxy.notifyFormUpdate(this.formId);
                Context context = FormAdapter.getInstance().getContext();
                if (context != null) {
                    context.unbindService(this);
                }
            } catch (RemoteException unused) {
                HiLog.error(FormAdapter.LABEL_LOG, "onServiceConnected acquireAbilityForm exception.", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class DeletionConnection implements ServiceConnection {
        private int formId;

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
        }

        DeletionConnection(int i) {
            this.formId = i;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            HiLog.debug(FormAdapter.LABEL_LOG, "delForm onServiceConnected.", new Object[0]);
            IRemoteObject iRemoteObject = (IRemoteObject) IPCAdapter.translateToIRemoteObject(iBinder).orElse(null);
            if (iRemoteObject == null) {
                HiLog.error(FormAdapter.LABEL_LOG, "delForm onServiceConnected failed to get supplier remote object.", new Object[0]);
                return;
            }
            try {
                IAbilityFormProvider.FormProviderStub.asProxy(iRemoteObject).notifyFormDelete(this.formId);
                Context context = FormAdapter.getInstance().getContext();
                if (context != null) {
                    context.unbindService(this);
                }
            } catch (RemoteException unused) {
                HiLog.error(FormAdapter.LABEL_LOG, "onServiceConnected notifyDeleteAbilityForm exception.", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class FormConnection implements ServiceConnection {
        private FormHostRecord clientRecord;
        private int formId;
        private FormItemInfo info;

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
        }

        public FormConnection(int i, FormHostRecord formHostRecord, FormItemInfo formItemInfo) {
            this.formId = i;
            this.clientRecord = formHostRecord;
            this.info = formItemInfo;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            HiLog.debug(FormAdapter.LABEL_LOG, "addForm onServiceConnected.", new Object[0]);
            IRemoteObject iRemoteObject = (IRemoteObject) IPCAdapter.translateToIRemoteObject(iBinder).orElse(null);
            if (iRemoteObject == null) {
                HiLog.error(FormAdapter.LABEL_LOG, "addForm onServiceConnected failed to get supplier remote object.", new Object[0]);
                return;
            }
            try {
                ProviderFormInfo acquireProviderFormInfo = IAbilityFormProvider.FormProviderStub.asProxy(iRemoteObject).acquireProviderFormInfo(getRequireIntent());
                if (acquireProviderFormInfo == null) {
                    HiLog.error(FormAdapter.LABEL_LOG, "onServiceConnected failed to acquireFormInfoProvider.", new Object[0]);
                    return;
                }
                HiLog.debug(FormAdapter.LABEL_LOG, "addForm get target ProviderFormInfo, call handleAcquireBack.", new Object[0]);
                FormAdapter.getInstance().handleAcquireBack(this.formId, acquireProviderFormInfo, this.clientRecord);
                Context context = FormAdapter.getInstance().getContext();
                if (context != null) {
                    context.unbindService(this);
                }
            } catch (RemoteException unused) {
                HiLog.error(FormAdapter.LABEL_LOG, "onServiceConnected acquireProviderFormInfo exception.", new Object[0]);
            }
        }

        /* access modifiers changed from: package-private */
        public Intent getRequireIntent() {
            Intent intent = new Intent();
            intent.setParam(AbilitySlice.PARAM_FORM_ID_KEY, this.formId);
            intent.setParam(AbilitySlice.PARAM_FORM_NAME_KEY, this.info.getFormName());
            intent.setParam(AbilitySlice.PARAM_FORM_DIMENSION_KEY, this.info.getSpecificationId());
            return intent;
        }
    }

    /* access modifiers changed from: private */
    public static class BundleReceiver extends BroadcastReceiver {
        private BundleReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, android.content.Intent intent) {
            Uri data;
            String schemeSpecificPart;
            if (intent != null && "android.intent.action.PACKAGE_REMOVED".equals(intent.getAction()) && (data = intent.getData()) != null && (schemeSpecificPart = data.getSchemeSpecificPart()) != null && !schemeSpecificPart.isEmpty()) {
                FormAdapter.getInstance().handleBundleRemoved(schemeSpecificPart);
            }
        }
    }
}
