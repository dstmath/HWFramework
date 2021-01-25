package ohos.miscservices.inputmethod.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ServiceManager;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.view.IInputMethodManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import ohos.aafwk.content.Intent;
import ohos.bundle.AbilityInfo;
import ohos.bundle.BundleManager;
import ohos.bundle.ElementName;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.idn.BasicInfo;
import ohos.idn.DeviceManager;
import ohos.idn.IDeviceChangeListener;
import ohos.miscservices.adapter.utils.AdaptUtil;
import ohos.miscservices.adapter.utils.ReflectUtil;
import ohos.miscservices.inputmethod.InputMethodProperty;
import ohos.miscservices.inputmethod.KeyboardType;
import ohos.miscservices.inputmethod.RemoteInputMethodConnection;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;

public class InputMethodManagerAdapter {
    private static final int CONNECT_ABILITY_DELAY = 2500;
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.adapter.IInputMethodManagerAdapterControl";
    private static final String FAIL = "fail";
    private static final String INPUT_METHOD_SERVICE = "input_method";
    private static final int NOTIFY_CURSOR_CONTEXT_NONE = 0;
    private static final String SUCCESS = "successful";
    private static final int SYS_ABILITY_STUB_ID = 7856;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodManagerAdapter");
    private static volatile InputMethodManagerAdapter sInstance = null;
    private int cursorContextNotifyMode = 0;
    private int imeOption;
    private String mConnectedRemoteId;
    private List<BasicInfo> mDeviceBasicAbilityInfos;
    private IDeviceChangeListener mDeviceChangeListener;
    private DeviceManager mDeviceManager;
    private boolean mFullscreenAdapt = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mHasConnectedRemote = false;
    private String mLocalDeviceId;
    private IRemoteObject mLocalInputDataChannel;
    private ThreadPoolExecutor mPoolExecutor;
    private RemoteInputMethodConnection mRemoteInputMethodConnection;
    private BasicInfo mSelectedBasicAbilityInfo;
    private final IInputMethodManager mService = IInputMethodManager.Stub.asInterface(ServiceManager.getServiceOrThrow(INPUT_METHOD_SERVICE));
    private Callable<Boolean> startRemoteCallable = new Callable() {
        /* class ohos.miscservices.inputmethod.adapter.$$Lambda$InputMethodManagerAdapter$LEXQ68udnbofEzaolZOyUE5SQM */

        @Override // java.util.concurrent.Callable
        public final Object call() {
            return InputMethodManagerAdapter.this.lambda$new$0$InputMethodManagerAdapter();
        }
    };

    public /* synthetic */ Boolean lambda$new$0$InputMethodManagerAdapter() throws Exception {
        return Boolean.valueOf(connectSelectedAbility(this.mSelectedBasicAbilityInfo));
    }

    private InputMethodManagerAdapter() throws ServiceManager.ServiceNotFoundException {
        initThreadPool();
    }

    private void initThreadPool() {
        HiLog.info(TAG, "initThreadPool begin", new Object[0]);
        this.mPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue(1), new RemoteThreadFactory());
        this.mPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
    }

    /* access modifiers changed from: private */
    public static class RemoteThreadFactory implements ThreadFactory {
        private RemoteThreadFactory() {
        }

        @Override // java.util.concurrent.ThreadFactory
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "remoteInputMethodThread_" + Thread.currentThread().getId());
        }
    }

    public static InputMethodManagerAdapter getInstance() {
        HiLog.info(TAG, "InputMethodManagerAdapter getInstance.", new Object[0]);
        if (AdaptUtil.getInputMethodManager().isPresent()) {
            HiLog.info(TAG, "getInstance: just for reversing InputMethodManager instance for double framework.", new Object[0]);
        }
        if (sInstance == null) {
            synchronized (InputMethodManagerAdapter.class) {
                if (sInstance == null) {
                    try {
                        sInstance = new InputMethodManagerAdapter();
                    } catch (ServiceManager.ServiceNotFoundException e) {
                        throw new IllegalStateException((Throwable) e);
                    }
                }
            }
        }
        return sInstance;
    }

    public boolean startInput(int i) throws RemoteException {
        Optional<View> surfaceView = ReflectUtil.getSurfaceView();
        if (!surfaceView.isPresent()) {
            HiLog.error(TAG, "startInput fail because current surfaceView is null.", new Object[0]);
            return false;
        }
        Optional<InputMethodManager> inputMethodManager = AdaptUtil.getInputMethodManager();
        if (!inputMethodManager.isPresent()) {
            HiLog.error(TAG, "startInput failed: current InputMethodManager instance is null.", new Object[0]);
            return false;
        }
        HiLog.info(TAG, "startInput before imm showSoftInput.", new Object[0]);
        if (!inputMethodManager.get().showSoftInput(surfaceView.get(), i)) {
            HiLog.error(TAG, "startInput failed: Current imm showSoftInput failed.", new Object[0]);
            return false;
        }
        HiLog.info(TAG, "startInput success: Current imm showSoftInput success.", new Object[0]);
        return true;
    }

    public boolean restartInput(int i) throws RemoteException {
        Optional<View> surfaceView = ReflectUtil.getSurfaceView();
        if (!surfaceView.isPresent()) {
            HiLog.error(TAG, "restartInput fail because current surfaceView is null.", new Object[0]);
            return false;
        }
        View view = surfaceView.get();
        Optional<InputMethodManager> inputMethodManager = AdaptUtil.getInputMethodManager();
        if (!inputMethodManager.isPresent()) {
            HiLog.error(TAG, "restartInput failed: current InputMethodManager instance is null.", new Object[0]);
            return false;
        }
        InputMethodManager inputMethodManager2 = inputMethodManager.get();
        inputMethodManager2.restartInput(view);
        HiLog.info(TAG, "restartInput before imm showSoftInput.", new Object[0]);
        if (!inputMethodManager2.showSoftInput(view, i)) {
            HiLog.error(TAG, "restartInput failed: Current imm showSoftInput failed.", new Object[0]);
            return false;
        }
        HiLog.info(TAG, "restartInput success: Current imm showSoftInput success.", new Object[0]);
        return true;
    }

    public void setLocalInputDataChannel(IRemoteObject iRemoteObject) throws RemoteException {
        this.mLocalInputDataChannel = iRemoteObject;
    }

    public boolean startRemoteInput(int i, int i2) throws RemoteException {
        this.imeOption = i2;
        queryOnlineBasicAbilityInfos();
        if (this.mDeviceBasicAbilityInfos.isEmpty()) {
            return startInput(i);
        }
        Context context = null;
        Optional<Context> androidContext = AdaptUtil.getAndroidContext();
        if (androidContext.isPresent()) {
            context = androidContext.get();
        }
        selectAbility(context, i);
        return true;
    }

    private void selectAbility(Context context, final int i) throws RemoteException {
        HiLog.debug(TAG, "selectAbility begin", new Object[0]);
        int size = this.mDeviceBasicAbilityInfos.size();
        String[] strArr = new String[size];
        for (int i2 = 0; i2 < size; i2++) {
            strArr[i2] = this.mDeviceBasicAbilityInfos.get(i2).getName();
        }
        AlertDialog create = new AlertDialog.Builder(context, 33948078).setTitle("select the remote devices").setItems(strArr, new DialogInterface.OnClickListener() {
            /* class ohos.miscservices.inputmethod.adapter.InputMethodManagerAdapter.AnonymousClass2 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i < 0 || i > InputMethodManagerAdapter.this.mDeviceBasicAbilityInfos.size() - 1) {
                    HiLog.error(InputMethodManagerAdapter.TAG, "select dialog item error.", new Object[0]);
                    return;
                }
                InputMethodManagerAdapter inputMethodManagerAdapter = InputMethodManagerAdapter.this;
                inputMethodManagerAdapter.mSelectedBasicAbilityInfo = (BasicInfo) inputMethodManagerAdapter.mDeviceBasicAbilityInfos.get(i);
                if (InputMethodManagerAdapter.this.mSelectedBasicAbilityInfo != null) {
                    HiLog.debug(InputMethodManagerAdapter.TAG, "mHasConnectedRemote : %{public}s", Boolean.valueOf(InputMethodManagerAdapter.this.mHasConnectedRemote));
                    InputMethodManagerAdapter inputMethodManagerAdapter2 = InputMethodManagerAdapter.this;
                    inputMethodManagerAdapter2.stopRemoteInput(inputMethodManagerAdapter2.mHasConnectedRemote, InputMethodManagerAdapter.this.mConnectedRemoteId, InputMethodManagerAdapter.this.mSelectedBasicAbilityInfo);
                    FutureTask futureTask = new FutureTask(InputMethodManagerAdapter.this.startRemoteCallable);
                    InputMethodManagerAdapter.this.mPoolExecutor.execute(futureTask);
                    InputMethodManagerAdapter inputMethodManagerAdapter3 = InputMethodManagerAdapter.this;
                    inputMethodManagerAdapter3.handleConnectResult(futureTask, dialogInterface, inputMethodManagerAdapter3.mSelectedBasicAbilityInfo, i);
                } else {
                    InputMethodManagerAdapter.this.dialogDismissStartInput(dialogInterface, i);
                }
                dialogInterface.dismiss();
            }
        }).setNegativeButton(17039360, new DialogInterface.OnClickListener() {
            /* class ohos.miscservices.inputmethod.adapter.InputMethodManagerAdapter.AnonymousClass1 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                if (InputMethodManagerAdapter.this.mHasConnectedRemote) {
                    dialogInterface.dismiss();
                } else {
                    InputMethodManagerAdapter.this.dialogDismissStartInput(dialogInterface, i);
                }
            }
        }).create();
        create.setCanceledOnTouchOutside(false);
        create.show();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConnectResult(FutureTask<Boolean> futureTask, DialogInterface dialogInterface, BasicInfo basicInfo, int i) {
        this.mHandler.post(new Runnable(futureTask, dialogInterface, i, basicInfo) {
            /* class ohos.miscservices.inputmethod.adapter.$$Lambda$InputMethodManagerAdapter$6sOyNdPlGjJ1sOWjgBGIiObu6c4 */
            private final /* synthetic */ FutureTask f$1;
            private final /* synthetic */ DialogInterface f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ BasicInfo f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            @Override // java.lang.Runnable
            public final void run() {
                InputMethodManagerAdapter.this.lambda$handleConnectResult$1$InputMethodManagerAdapter(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    public /* synthetic */ void lambda$handleConnectResult$1$InputMethodManagerAdapter(FutureTask futureTask, DialogInterface dialogInterface, int i, BasicInfo basicInfo) {
        try {
            Boolean bool = (Boolean) futureTask.get(2500, TimeUnit.MILLISECONDS);
            this.mHasConnectedRemote = bool.booleanValue();
            HiLog.info(TAG, "isConnected %{public}s", bool);
            if (!bool.booleanValue()) {
                dialogDismissStartInput(dialogInterface, i);
                this.mConnectedRemoteId = null;
                return;
            }
            this.mConnectedRemoteId = basicInfo.getNodeId();
        } catch (ExecutionException e) {
            HiLog.warn(TAG, "connect ability failed! ExecutionException is %{public}s", e.getMessage());
        } catch (InterruptedException e2) {
            HiLog.warn(TAG, "connect ability failed! InterruptedException is %{public}s", e2.getMessage());
        } catch (TimeoutException e3) {
            HiLog.warn(TAG, "connect ability failed! TimeoutException is %{public}s", e3.getMessage());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dialogDismissStartInput(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
        try {
            if (!startInput(i)) {
                HiLog.error(TAG, "dialog dismiss start input fail.", new Object[0]);
            }
        } catch (RemoteException e) {
            HiLog.error(TAG, "start input remote exception %{public}s", e.getLocalizedMessage());
        }
    }

    private boolean connectSelectedAbility(BasicInfo basicInfo) {
        HiLog.info(TAG, "connectDeviceAbility begin", new Object[0]);
        addSysAbility();
        registerDeviceChangedLister();
        ElementName elementName = new ElementName(basicInfo.getNodeId(), "com.huawei.remoteinputmethod", "RemoteInputMethodAbility");
        initRemoteConnectionInstance();
        Intent param = new Intent().setElement(elementName).setFlags(256).setParam("ClientDeviceId", this.mLocalDeviceId).setParam("ImeOption", this.imeOption);
        HiLog.debug(TAG, "connectAbility begin", new Object[0]);
        Optional<ohos.app.Context> harmonyContext = ReflectUtil.getHarmonyContext();
        if (harmonyContext.isPresent()) {
            return harmonyContext.get().connectAbility(param, this.mRemoteInputMethodConnection);
        }
        HiLog.error(TAG, "Connecting remote input method ability will be failed, the context is null.", new Object[0]);
        return false;
    }

    public IRemoteObject getLocalInputDataChannel() {
        return this.mLocalInputDataChannel;
    }

    private void registerDeviceChangedLister() {
        this.mDeviceChangeListener = new IDeviceChangeListener() {
            /* class ohos.miscservices.inputmethod.adapter.InputMethodManagerAdapter.AnonymousClass3 */

            @Override // ohos.idn.IDeviceChangeListener
            public void onDeviceOnline(BasicInfo basicInfo) {
                HiLog.info(InputMethodManagerAdapter.TAG, "Target remote device online.", new Object[0]);
            }

            @Override // ohos.idn.IDeviceChangeListener
            public void onDeviceOffline(BasicInfo basicInfo) {
                HiLog.info(InputMethodManagerAdapter.TAG, "Target remote device offline.", new Object[0]);
                String nodeId = InputMethodManagerAdapter.this.mSelectedBasicAbilityInfo.getNodeId();
                String nodeId2 = basicInfo.getNodeId();
                if (nodeId.equals(nodeId2) || (InputMethodManagerAdapter.this.mLocalDeviceId != null && InputMethodManagerAdapter.this.mLocalDeviceId.equals(nodeId2))) {
                    InputMethodManagerAdapter.this.mHasConnectedRemote = false;
                    InputMethodManagerAdapter.this.refreshConnection();
                    HiLog.debug(InputMethodManagerAdapter.TAG, "Remove device changed listener %{public}s", Boolean.valueOf(InputMethodManagerAdapter.this.mDeviceManager.removeDeviceChangeListener(DeviceManager.TrustedRange.PUBLIC, InputMethodManagerAdapter.this.mDeviceChangeListener)));
                }
            }
        };
        DeviceManager deviceManager = this.mDeviceManager;
        if (deviceManager == null) {
            HiLog.error(TAG, "registerDeviceChangedLister failed: Current device manager is null.", new Object[0]);
            return;
        }
        HiLog.info(TAG, "Add device changed lister %{public}s", Boolean.valueOf(deviceManager.addDeviceChangeListener(DeviceManager.TrustedRange.PUBLIC, this.mDeviceChangeListener)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshConnection() {
        this.mRemoteInputMethodConnection = new RemoteInputMethodConnection(this.mLocalInputDataChannel, new InputMethodManagerAdapterControlImp(DESCRIPTOR).asObject(), this.mLocalDeviceId);
    }

    private void addSysAbility() {
        HiLog.debug(TAG, "addSysAbility begin", new Object[0]);
        IRemoteObject iRemoteObject = this.mLocalInputDataChannel;
        if (iRemoteObject == null) {
            HiLog.error(TAG, "abilitySkeleton is null!", new Object[0]);
        } else {
            HiLog.debug(TAG, "System ability register successful %{public}s", SysAbilityManager.addSysAbility(SYS_ABILITY_STUB_ID, iRemoteObject, true, 0) == 0 ? SUCCESS : FAIL);
        }
    }

    private void queryOnlineBasicAbilityInfos() {
        this.mDeviceManager = new DeviceManager();
        Optional<BasicInfo> localBasicInfo = this.mDeviceManager.getLocalBasicInfo();
        if (localBasicInfo.isPresent()) {
            this.mLocalDeviceId = localBasicInfo.get().getNodeId();
        } else {
            HiLog.warn(TAG, "Get local device information error: option is not present.", new Object[0]);
        }
        this.mDeviceBasicAbilityInfos = new ArrayList();
        BundleManager instance = BundleManager.getInstance();
        if (instance == null) {
            HiLog.error(TAG, "queryOnlineDeviceAbilities error: bundle manager instance is null.", new Object[0]);
            return;
        }
        for (BasicInfo basicInfo : this.mDeviceManager.getNodesBasicInfo()) {
            String str = this.mLocalDeviceId;
            if (str != null && !str.equals(basicInfo.getNodeId())) {
                Intent element = new Intent().setElement(new ElementName(basicInfo.getNodeId(), "com.huawei.remoteinputmethod", "RemoteInputMethodAbility"));
                List<AbilityInfo> arrayList = new ArrayList();
                try {
                    arrayList = instance.queryAbilityByIntent(element);
                } catch (RemoteException e) {
                    HiLog.error(TAG, "RemoteException %{public}s", e.getLocalizedMessage());
                }
                if (arrayList == null || arrayList.isEmpty()) {
                    HiLog.error(TAG, "The abilityInfoList is empty or null", new Object[0]);
                } else {
                    for (AbilityInfo abilityInfo : arrayList) {
                        HiLog.debug(TAG, "the devices AA is  %{public}s", abilityInfo.getLabel());
                        this.mDeviceBasicAbilityInfos.add(new BasicInfo(basicInfo.getName(), basicInfo.getDeviceType(), abilityInfo.getDeviceId()));
                    }
                }
            }
        }
    }

    private void initRemoteConnectionInstance() {
        if (this.mRemoteInputMethodConnection == null) {
            this.mRemoteInputMethodConnection = new RemoteInputMethodConnection(this.mLocalInputDataChannel, new InputMethodManagerAdapterControlImp(DESCRIPTOR).asObject(), this.mLocalDeviceId);
        }
    }

    public void stopRemoteInput() throws RemoteException {
        Optional<ohos.app.Context> harmonyContext = ReflectUtil.getHarmonyContext();
        if (!harmonyContext.isPresent()) {
            HiLog.error(TAG, "stopRemoteInput error", new Object[0]);
            return;
        }
        HiLog.debug(TAG, "stopRemoteInput: disconnectAbility start", new Object[0]);
        harmonyContext.get().disconnectAbility(this.mRemoteInputMethodConnection);
        this.mHasConnectedRemote = false;
        HiLog.debug(TAG, "stopRemoteInput: disconnectAbility end", new Object[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopRemoteInput(boolean z, String str, BasicInfo basicInfo) {
        if (!z) {
            return;
        }
        if (basicInfo.getNodeId().equals(str)) {
            HiLog.debug(TAG, "remoteDevice has connected.", new Object[0]);
            return;
        }
        try {
            stopRemoteInput();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "stopRemoteInput catch remoteException.", new Object[0]);
        }
    }

    public boolean stopInput(int i) throws RemoteException {
        Optional<View> surfaceView = ReflectUtil.getSurfaceView();
        if (!surfaceView.isPresent()) {
            HiLog.error(TAG, "stopInput failed because current surfaceView is null.", new Object[0]);
            return false;
        }
        IBinder windowToken = surfaceView.get().getWindowToken();
        if (windowToken == null) {
            HiLog.error(TAG, "stopInput failed because current windowToken is null.", new Object[0]);
            return false;
        }
        Optional<InputMethodManager> inputMethodManager = AdaptUtil.getInputMethodManager();
        if (!inputMethodManager.isPresent()) {
            HiLog.error(TAG, "stopInput failed: current InputMethodManager instance is null.", new Object[0]);
            return false;
        } else if (!inputMethodManager.get().hideSoftInputFromWindow(windowToken, i)) {
            HiLog.error(TAG, "hideSoftInputFromWindow fail", new Object[0]);
            return false;
        } else {
            HiLog.debug(TAG, "hideSoftInputFromWindow success", new Object[0]);
            return true;
        }
    }

    public int getScreenMode() {
        Optional<InputMethodManager> inputMethodManager = AdaptUtil.getInputMethodManager();
        int i = 2;
        if (!inputMethodManager.isPresent()) {
            HiLog.error(TAG, "getScreenMode failed: current InputMethodManager instance is null.", new Object[0]);
            return 2;
        }
        if (inputMethodManager.get().isFullscreenMode()) {
            i = 1;
        }
        this.mFullscreenAdapt = true;
        HiLog.debug(TAG, "InputMethodController#getScreenMode works well, adaption successful", new Object[0]);
        return i;
    }

    public boolean isFullscreenAdapt() {
        return this.mFullscreenAdapt;
    }

    public boolean isAvailable() throws RemoteException {
        if (this.mService == null) {
            HiLog.error(TAG, "isAvailable false, the mService is null", new Object[0]);
            return false;
        } else if (!AdaptUtil.getInputMethodManager().isPresent()) {
            HiLog.error(TAG, "isAvailable false, current InputMethodManager instance is null.", new Object[0]);
            return false;
        } else if (!AdaptUtil.getAndroidContext().isPresent()) {
            HiLog.error(TAG, "isAvailable false, the current A context is null.", new Object[0]);
            return false;
        } else if (ReflectUtil.getSurfaceView().isPresent()) {
            return true;
        } else {
            HiLog.error(TAG, "isAvailable false, current surfaceView is null.", new Object[0]);
            return false;
        }
    }

    public int getKeyboardWindowHeight() throws RemoteException {
        Optional<InputMethodManager> inputMethodManager = AdaptUtil.getInputMethodManager();
        if (inputMethodManager.isPresent()) {
            return inputMethodManager.get().getInputMethodWindowVisibleHeight();
        }
        HiLog.error(TAG, "getInputMethodDisplayHeight failed: current InputMethodManager instance is null.", new Object[0]);
        return 0;
    }

    public KeyboardType getCurrentKeyboardType() throws RemoteException {
        HiLog.debug(TAG, "getCurrentKeyboardType start.", new Object[0]);
        Optional<InputMethodManager> inputMethodManager = AdaptUtil.getInputMethodManager();
        if (!inputMethodManager.isPresent()) {
            HiLog.error(TAG, "getCurrentKeyboardType: current InputMethodManager instance is null.", new Object[0]);
            return null;
        }
        InputMethodSubtype currentInputMethodSubtype = inputMethodManager.get().getCurrentInputMethodSubtype();
        HiLog.debug(TAG, "getCurrentKeyboardType:mAospIMM get getCurrentInputMethodSubtype end.", new Object[0]);
        KeyboardType convertToKeyboardType = InputMethodSubtypeAdapter.convertToKeyboardType(currentInputMethodSubtype);
        HiLog.debug(TAG, "getCurrentKeyboardType end .", new Object[0]);
        return convertToKeyboardType;
    }

    public List<InputMethodProperty> listInputMethodEnabled() throws RemoteException {
        HiLog.debug(TAG, "listInputMethodEnabled start.", new Object[0]);
        ArrayList arrayList = new ArrayList();
        Optional<InputMethodManager> inputMethodManager = AdaptUtil.getInputMethodManager();
        if (!inputMethodManager.isPresent()) {
            HiLog.error(TAG, "listInputMethodEnabled: current InputMethodManager instance is null.", new Object[0]);
            return arrayList;
        }
        List<InputMethodInfo> enabledInputMethodList = inputMethodManager.get().getEnabledInputMethodList();
        HiLog.debug(TAG, "listInputMethodEnabled:imm get getEnabledInputMethodList end.", new Object[0]);
        for (InputMethodInfo inputMethodInfo : enabledInputMethodList) {
            arrayList.add(InputMethodInfoAdapter.convertToInputMethodProperty(inputMethodInfo));
        }
        HiLog.debug(TAG, "listInputMethodEnabled end.", new Object[0]);
        return arrayList;
    }

    public List<KeyboardType> listKeyboardType(InputMethodProperty inputMethodProperty) throws RemoteException {
        HiLog.debug(TAG, "listKeyboardType start.", new Object[0]);
        ArrayList arrayList = new ArrayList();
        Optional<InputMethodManager> inputMethodManager = AdaptUtil.getInputMethodManager();
        if (!inputMethodManager.isPresent()) {
            HiLog.error(TAG, "listKeyboardType: current InputMethodManager instance is null.", new Object[0]);
            return arrayList;
        }
        InputMethodInfo convertToInputMethodInfo = InputMethodInfoAdapter.convertToInputMethodInfo(inputMethodProperty);
        HiLog.debug(TAG, "listKeyboardType: input parameter converted end.", new Object[0]);
        List<InputMethodSubtype> enabledInputMethodSubtypeList = inputMethodManager.get().getEnabledInputMethodSubtypeList(convertToInputMethodInfo, true);
        HiLog.debug(TAG, "listKeyboardType:mAospIMM get getEnabledInputMethodSubtypeList end.", new Object[0]);
        for (InputMethodSubtype inputMethodSubtype : enabledInputMethodSubtypeList) {
            arrayList.add(InputMethodSubtypeAdapter.convertToKeyboardType(inputMethodSubtype));
        }
        HiLog.debug(TAG, "listKeyboardType end.", new Object[0]);
        return arrayList;
    }

    public List<InputMethodProperty> listInputMethod() throws RemoteException {
        HiLog.info(TAG, "listInputMethod start.", new Object[0]);
        ArrayList arrayList = new ArrayList();
        Optional<InputMethodManager> inputMethodManager = AdaptUtil.getInputMethodManager();
        if (!inputMethodManager.isPresent()) {
            HiLog.error(TAG, "listInputMethod: current InputMethodManager instance is null.", new Object[0]);
            return arrayList;
        }
        List<InputMethodInfo> inputMethodList = inputMethodManager.get().getInputMethodList();
        HiLog.debug(TAG, "listInputMethod: imm getInputMethodList end.", new Object[0]);
        for (InputMethodInfo inputMethodInfo : inputMethodList) {
            arrayList.add(InputMethodInfoAdapter.convertToInputMethodProperty(inputMethodInfo));
        }
        HiLog.debug(TAG, "listInputMethod end.", new Object[0]);
        return arrayList;
    }

    public void displayOptionalInputMethod() throws RemoteException {
        HiLog.debug(TAG, "displayOptionalInputMethod start.", new Object[0]);
        Optional<InputMethodManager> inputMethodManager = AdaptUtil.getInputMethodManager();
        if (!inputMethodManager.isPresent()) {
            HiLog.error(TAG, "displayOptionalInputMethod failed: current InputMethodManager instance is null.", new Object[0]);
            return;
        }
        inputMethodManager.get().showInputMethodPicker();
        HiLog.debug(TAG, "displayOptionalInputMethod end.", new Object[0]);
    }

    public InputMethodManager getInputMethodManager() {
        return AdaptUtil.getInputMethodManager().get();
    }

    public void setCaretCoordinateNotifyMode(int i) throws RemoteException {
        Optional<InputMethodManager> inputMethodManager = AdaptUtil.getInputMethodManager();
        if (!inputMethodManager.isPresent()) {
            HiLog.error(TAG, "setCursorContextNotifyMode failed: current InputMethodManager instance is null.", new Object[0]);
            return;
        }
        this.cursorContextNotifyMode = i;
        inputMethodManager.get().setUpdateCursorAnchorInfoMode(i);
    }

    public boolean isCaretCoordinateSubscribed() throws RemoteException {
        Optional<InputMethodManager> inputMethodManager = AdaptUtil.getInputMethodManager();
        if (inputMethodManager.isPresent()) {
            return inputMethodManager.get().isCursorAnchorInfoEnabled();
        }
        HiLog.error(TAG, "isCursorContextSubscribed failed: current InputMethodManager instance is null.", new Object[0]);
        return false;
    }
}
