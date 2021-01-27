package ohos.miscservices.inputmethod;

import ohos.app.Context;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.ivicommon.drivingsafety.model.ControlItemEnum;
import ohos.miscservices.inputmethod.InputMethodController;
import ohos.miscservices.inputmethod.adapter.InputParam;
import ohos.miscservices.inputmethod.implement.InputMethodSystemAbilitySkeleton;
import ohos.miscservices.inputmethod.implement.RemoteInputDataChannelSkeleton;
import ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility;
import ohos.miscservices.inputmethod.internal.IInputMethodAgent;
import ohos.miscservices.inputmethod.internal.InputMethodAgentSkeleton;
import ohos.multimodalinput.event.KeyEvent;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

public class InputMethodController {
    private static final String DESCRIPTOR = "zidaneos.miscservices.inputmethod.interfaces.IInputDataChannelLocal";
    private static final int INPUT_TYPE_SECURITY = 128;
    private static final String REMOTE_INPUT_PROPERTY = "persist.sys.remote.inputmethod";
    public static final int SCREEN_MODE_FULL = 1;
    public static final int SCREEN_MODE_PART = 2;
    public static final int START_IM_NORMAL = 1;
    public static final int STOP_IM_NORMAL = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodController");
    private static volatile InputMethodController sInstance = null;
    private DrivingSafetyController mDrivingSafetyController = new DrivingSafetyController();
    private IInputMethodAgent mInputMethodAgent = InputMethodAgentSkeleton.asInterface(null);
    private InputDataChannel mLocalInputDataChannel;
    private int mPreSelectionEnd = 0;
    private int mPreSelectionStart = 0;
    private IInputMethodSystemAbility mSystemAbility = InputMethodSystemAbilitySkeleton.asInterface(null);
    private TaskDispatcher mTaskDispatcher;

    private InputMethodController() {
    }

    public static InputMethodController getInstance() {
        if (sInstance == null) {
            synchronized (InputMethodController.class) {
                if (sInstance == null) {
                    sInstance = new InputMethodController();
                }
            }
        }
        return sInstance;
    }

    public void setDriveSafetyMode(IDrivingSafety iDrivingSafety) {
        this.mDrivingSafetyController.setDrivingSafety(iDrivingSafety);
    }

    public boolean startInput(int i, boolean z) {
        boolean z2;
        if (!this.mDrivingSafetyController.isDrivingSafety(ControlItemEnum.IME)) {
            HiLog.info(TAG, "current is safety drive mode,can not show inputmethod", new Object[0]);
            return false;
        }
        try {
            HiLog.info(TAG, "startInput: startModes = %{public}d, isFocusViewChanged = %{public}b", Integer.valueOf(i), Boolean.valueOf(z));
            if (z) {
                z2 = this.mSystemAbility.restartInput(i);
            } else {
                z2 = this.mSystemAbility.startInput(i);
            }
            if (z2) {
                HiLog.info(TAG, "show inputmethod success,register driver safety listener", new Object[0]);
                this.mDrivingSafetyController.registerDriveEventListener();
            }
            return z2;
        } catch (RemoteException e) {
            HiLog.error(TAG, "startInput failed,Exception = %s", e.toString());
            return false;
        }
    }

    public void notifyCursorCoordinateChanged(float f, float f2, float f3, float[] fArr) {
        try {
            HiLog.info(TAG, "notifyCursorCoordinateChanged begin.", new Object[0]);
            this.mInputMethodAgent.notifyCursorCoordinateChanged(f, f2, f3, fArr);
        } catch (RemoteException e) {
            HiLog.error(TAG, "notifyCursorCoordinateChanged failed,Exception = %s", e.toString());
        }
    }

    public void setCursorCoordinateNotifyMode(int i) {
        try {
            HiLog.info(TAG, "setCursorCoordinateNotifyMode: mode = %d", Integer.valueOf(i));
            this.mSystemAbility.setCursorCoordinateNotifyMode(i);
        } catch (RemoteException e) {
            HiLog.error(TAG, "setCursorCoordinateNotifyMode failed,Exception = %s", e.toString());
        }
    }

    public boolean isCursorCoordinateSubscribed() {
        try {
            HiLog.info(TAG, "isCursorCoordinateSubscribed begin.", new Object[0]);
            return this.mSystemAbility.isCursorCoordinateSubscribed();
        } catch (RemoteException e) {
            HiLog.error(TAG, "isCursorCoordinateSubscribed failed,Exception = %s", e.toString());
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public class InputDataChannelLocalImpl extends RemoteInputDataChannelSkeleton {
        InputDataChannelLocalImpl(String str) {
            super(str);
        }

        @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
        public boolean insertText(String str) throws RemoteException {
            HiLog.debug(InputMethodController.TAG, "insertText in IMC InputDataChannelLocalImp", new Object[0]);
            if (str == null) {
                HiLog.debug(InputMethodController.TAG, "insertText text is null", new Object[0]);
                return false;
            }
            InputMethodController.this.mTaskDispatcher.asyncDispatch(new Runnable(str) {
                /* class ohos.miscservices.inputmethod.$$Lambda$InputMethodController$InputDataChannelLocalImpl$NoOdu2MmjB17b9sUQ1cAAsWMFq4 */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    InputMethodController.InputDataChannelLocalImpl.this.lambda$insertText$0$InputMethodController$InputDataChannelLocalImpl(this.f$1);
                }
            });
            return true;
        }

        public /* synthetic */ void lambda$insertText$0$InputMethodController$InputDataChannelLocalImpl(String str) {
            InputMethodController.this.mLocalInputDataChannel.insertText(str);
        }

        @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
        public boolean insertRichContent(RichContent richContent) throws RemoteException {
            HiLog.debug(InputMethodController.TAG, "insertRichContent in IMC InputDataChannelLocalImp", new Object[0]);
            if (richContent == null) {
                HiLog.debug(InputMethodController.TAG, "insertRichContent richContent is null", new Object[0]);
                return false;
            }
            InputMethodController.this.mTaskDispatcher.asyncDispatch(new Runnable(richContent) {
                /* class ohos.miscservices.inputmethod.$$Lambda$InputMethodController$InputDataChannelLocalImpl$8RGc9Is2taxX090alPEYVaMujcI */
                private final /* synthetic */ RichContent f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    InputMethodController.InputDataChannelLocalImpl.this.lambda$insertRichContent$1$InputMethodController$InputDataChannelLocalImpl(this.f$1);
                }
            });
            return true;
        }

        public /* synthetic */ void lambda$insertRichContent$1$InputMethodController$InputDataChannelLocalImpl(RichContent richContent) {
            InputMethodController.this.mLocalInputDataChannel.insertRichContent(richContent);
        }

        @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
        public boolean deleteBackward(int i) throws RemoteException {
            HiLog.debug(InputMethodController.TAG, "deleteBackward in IMC InputDataChannelLocalImp", new Object[0]);
            InputMethodController.this.mTaskDispatcher.asyncDispatch(new Runnable(i) {
                /* class ohos.miscservices.inputmethod.$$Lambda$InputMethodController$InputDataChannelLocalImpl$kd_YuE1QAqfdLaUpCoA8sXZACE */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    InputMethodController.InputDataChannelLocalImpl.this.lambda$deleteBackward$2$InputMethodController$InputDataChannelLocalImpl(this.f$1);
                }
            });
            return true;
        }

        public /* synthetic */ void lambda$deleteBackward$2$InputMethodController$InputDataChannelLocalImpl(int i) {
            InputMethodController.this.mLocalInputDataChannel.deleteBackward(i);
        }

        @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
        public boolean deleteForward(int i) throws RemoteException {
            HiLog.debug(InputMethodController.TAG, "deleteForward in IMC InputDataChannelLocalImp", new Object[0]);
            InputMethodController.this.mTaskDispatcher.asyncDispatch(new Runnable(i) {
                /* class ohos.miscservices.inputmethod.$$Lambda$InputMethodController$InputDataChannelLocalImpl$tWJ4wiAjA0djNU3qyuusHkusNs */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    InputMethodController.InputDataChannelLocalImpl.this.lambda$deleteForward$3$InputMethodController$InputDataChannelLocalImpl(this.f$1);
                }
            });
            return true;
        }

        public /* synthetic */ void lambda$deleteForward$3$InputMethodController$InputDataChannelLocalImpl(int i) {
            InputMethodController.this.mLocalInputDataChannel.deleteForward(i);
        }

        @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
        public String getForward(int i) throws RemoteException {
            HiLog.debug(InputMethodController.TAG, "getForward in IMC InputDataChannelLocalImp", new Object[0]);
            InputMethodController.this.mTaskDispatcher.asyncDispatch(new Runnable(i) {
                /* class ohos.miscservices.inputmethod.$$Lambda$InputMethodController$InputDataChannelLocalImpl$6crp0cIv9xwq2nwbA8BuFO0DJQ4 */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    InputMethodController.InputDataChannelLocalImpl.this.lambda$getForward$4$InputMethodController$InputDataChannelLocalImpl(this.f$1);
                }
            });
            return "";
        }

        public /* synthetic */ void lambda$getForward$4$InputMethodController$InputDataChannelLocalImpl(int i) {
            InputMethodController.this.mLocalInputDataChannel.getForward(i);
        }

        @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
        public String getBackward(int i) throws RemoteException {
            HiLog.debug(InputMethodController.TAG, "getBackward in IMC InputDataChannelLocalImp", new Object[0]);
            InputMethodController.this.mTaskDispatcher.asyncDispatch(new Runnable(i) {
                /* class ohos.miscservices.inputmethod.$$Lambda$InputMethodController$InputDataChannelLocalImpl$MbWTtPuAGjJ3daXwJT6kzpnJSoc */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    InputMethodController.InputDataChannelLocalImpl.this.lambda$getBackward$5$InputMethodController$InputDataChannelLocalImpl(this.f$1);
                }
            });
            return "";
        }

        public /* synthetic */ void lambda$getBackward$5$InputMethodController$InputDataChannelLocalImpl(int i) {
            InputMethodController.this.mLocalInputDataChannel.getBackward(i);
        }

        @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
        public boolean markText(int i, int i2) throws RemoteException {
            HiLog.debug(InputMethodController.TAG, "markText in IMC InputDataChannelLocalImp", new Object[0]);
            InputMethodController.this.mTaskDispatcher.asyncDispatch(new Runnable(i, i2) {
                /* class ohos.miscservices.inputmethod.$$Lambda$InputMethodController$InputDataChannelLocalImpl$yvieBbpO3bTTHn7RJA1W9H2wKeo */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    InputMethodController.InputDataChannelLocalImpl.this.lambda$markText$6$InputMethodController$InputDataChannelLocalImpl(this.f$1, this.f$2);
                }
            });
            return true;
        }

        public /* synthetic */ void lambda$markText$6$InputMethodController$InputDataChannelLocalImpl(int i, int i2) {
            InputMethodController.this.mLocalInputDataChannel.markText(i, i2);
        }

        @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
        public boolean unmarkText() throws RemoteException {
            HiLog.debug(InputMethodController.TAG, "unmarkText in IMC InputDataChannelLocalImp", new Object[0]);
            return InputMethodController.this.mLocalInputDataChannel.unmarkText();
        }

        @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
        public boolean replaceMarkedText(String str) throws RemoteException {
            HiLog.debug(InputMethodController.TAG, "replaceMarkedText in IMC InputDataChannelLocalImp", new Object[0]);
            if (str == null) {
                HiLog.debug(InputMethodController.TAG, "replaceMarkedText text is null", new Object[0]);
                return false;
            }
            InputMethodController.this.mTaskDispatcher.asyncDispatch(new Runnable(str) {
                /* class ohos.miscservices.inputmethod.$$Lambda$InputMethodController$InputDataChannelLocalImpl$JHidK8PUgcYrJrzI5Z7XBqsMEdc */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    InputMethodController.InputDataChannelLocalImpl.this.lambda$replaceMarkedText$7$InputMethodController$InputDataChannelLocalImpl(this.f$1);
                }
            });
            return true;
        }

        public /* synthetic */ void lambda$replaceMarkedText$7$InputMethodController$InputDataChannelLocalImpl(String str) {
            InputMethodController.this.mLocalInputDataChannel.replaceMarkedText(str);
        }

        @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
        public EditingText subscribeEditingText(EditingCapability editingCapability) throws RemoteException {
            HiLog.debug(InputMethodController.TAG, "subscribeEditingText in IMC InputDataChannelLocalImp", new Object[0]);
            if (editingCapability != null) {
                return InputMethodController.this.mLocalInputDataChannel.subscribeEditingText(editingCapability);
            }
            HiLog.debug(InputMethodController.TAG, "subscribeEditingText request is null and return a new empty EditingText.", new Object[0]);
            return new EditingText();
        }

        @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
        public void close() throws RemoteException {
            HiLog.debug(InputMethodController.TAG, "close in IMC InputDataChannelLocalImp", new Object[0]);
            InputMethodController.this.mLocalInputDataChannel.close();
        }

        @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
        public boolean sendCustomizedData(String str, PacMap pacMap) throws RemoteException {
            HiLog.debug(InputMethodController.TAG, "sendCustomizedData in IMC InputDataChannelLocalImp", new Object[0]);
            if (str == null) {
                HiLog.debug(InputMethodController.TAG, "sendCustomizedData action is null", new Object[0]);
                return false;
            }
            InputMethodController.this.mTaskDispatcher.asyncDispatch(new Runnable(str, pacMap) {
                /* class ohos.miscservices.inputmethod.$$Lambda$InputMethodController$InputDataChannelLocalImpl$8zrIgIGEwLzyRR6V26WLe9u29Sk */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ PacMap f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    InputMethodController.InputDataChannelLocalImpl.this.lambda$sendCustomizedData$8$InputMethodController$InputDataChannelLocalImpl(this.f$1, this.f$2);
                }
            });
            return true;
        }

        public /* synthetic */ void lambda$sendCustomizedData$8$InputMethodController$InputDataChannelLocalImpl(String str, PacMap pacMap) {
            InputMethodController.this.mLocalInputDataChannel.sendCustomizedData(str, pacMap);
        }

        @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
        public boolean sendKeyEvent(KeyEvent keyEvent) throws RemoteException {
            HiLog.debug(InputMethodController.TAG, "sendKeyEvent in IMC InputDataChannelLocalImp", new Object[0]);
            if (keyEvent == null) {
                HiLog.debug(InputMethodController.TAG, "sendKeyEvent keyEvent is null", new Object[0]);
                return false;
            }
            InputMethodController.this.mTaskDispatcher.asyncDispatch(new Runnable(keyEvent) {
                /* class ohos.miscservices.inputmethod.$$Lambda$InputMethodController$InputDataChannelLocalImpl$iTsUPmHMZQTK33NYHcuJXA8hxP0 */
                private final /* synthetic */ KeyEvent f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    InputMethodController.InputDataChannelLocalImpl.this.lambda$sendKeyEvent$9$InputMethodController$InputDataChannelLocalImpl(this.f$1);
                }
            });
            return true;
        }

        public /* synthetic */ void lambda$sendKeyEvent$9$InputMethodController$InputDataChannelLocalImpl(KeyEvent keyEvent) {
            InputMethodController.this.mLocalInputDataChannel.sendKeyEvent(keyEvent);
        }

        @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
        public boolean sendKeyFunction(int i) throws RemoteException {
            HiLog.debug(InputMethodController.TAG, "sendKeyFunction in IMC InputDataChannelLocalImp", new Object[0]);
            InputMethodController.this.mTaskDispatcher.asyncDispatch(new Runnable(i) {
                /* class ohos.miscservices.inputmethod.$$Lambda$InputMethodController$InputDataChannelLocalImpl$FVRNGY_msqW665dbJnNPKfjKjyI */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    InputMethodController.InputDataChannelLocalImpl.this.lambda$sendKeyFunction$10$InputMethodController$InputDataChannelLocalImpl(this.f$1);
                }
            });
            return true;
        }

        public /* synthetic */ void lambda$sendKeyFunction$10$InputMethodController$InputDataChannelLocalImpl(int i) {
            InputMethodController.this.mLocalInputDataChannel.sendKeyFunction(i);
        }

        @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
        public boolean selectText(int i, int i2) throws RemoteException {
            HiLog.debug(InputMethodController.TAG, "selectText in IMC InputDataChannelLocalImp", new Object[0]);
            InputMethodController.this.mTaskDispatcher.asyncDispatch(new Runnable(i, i2) {
                /* class ohos.miscservices.inputmethod.$$Lambda$InputMethodController$InputDataChannelLocalImpl$W2UgQ0nx8x63wNYg8s7Y7GvPP6o */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    InputMethodController.InputDataChannelLocalImpl.this.lambda$selectText$11$InputMethodController$InputDataChannelLocalImpl(this.f$1, this.f$2);
                }
            });
            return true;
        }

        public /* synthetic */ void lambda$selectText$11$InputMethodController$InputDataChannelLocalImpl(int i, int i2) {
            InputMethodController.this.mLocalInputDataChannel.selectText(i, i2);
        }
    }

    public boolean startRemoteInput(Context context, int i, InputDataChannel inputDataChannel, InputParam inputParam) {
        if (inputParam == null) {
            HiLog.error(TAG, "inputParam get in startRemoteInput in null!", new Object[0]);
            return false;
        } else if (inputParam.getInputType() == 128) {
            HiLog.info(TAG, "Current input type is secure input method type,do not support use remote input method.", new Object[0]);
            return startInput(i, false);
        } else {
            try {
                HiLog.info(TAG, "startRemoteInput: startModes = %d", Integer.valueOf(i));
                HiLog.info(TAG, "startRemoteInput: imeOption = %d", Integer.valueOf(inputParam.getImeOption()));
                this.mLocalInputDataChannel = inputDataChannel;
                this.mTaskDispatcher = context.getUITaskDispatcher();
                this.mSystemAbility.setLocalInputDataChannel(new InputDataChannelLocalImpl(DESCRIPTOR).asObject());
                return this.mSystemAbility.startRemoteInput(i, inputParam.getImeOption());
            } catch (RemoteException e) {
                HiLog.error(TAG, "startRemoteInput failed,Exception = %s", e.toString());
                return false;
            }
        }
    }

    public boolean stopInput(int i) {
        try {
            HiLog.debug(TAG, "stopInput: stopModes = %d", Integer.valueOf(i));
            if (!this.mSystemAbility.stopInput(i)) {
                return false;
            }
            this.mDrivingSafetyController.removeDriveEventListener();
            return true;
        } catch (RemoteException e) {
            HiLog.error(TAG, "stopInput failed,Exception = %s", e.toString());
            return false;
        }
    }

    public int getScreenMode() {
        try {
            HiLog.debug(TAG, "getScreenMode: Now is in full screen mode.", new Object[0]);
            return this.mSystemAbility.getScreenMode();
        } catch (RemoteException e) {
            HiLog.error(TAG, "getScreenMode failed, Exception = %s", e.toString());
            HiLog.debug(TAG, "isFullscreen: Now is not in full screen mode.", new Object[0]);
            return 2;
        }
    }

    public void notifyEditingTextChanged(int i, EditingText editingText) {
        try {
            HiLog.debug(TAG, "notifyEditingTextChanged start", new Object[0]);
            this.mInputMethodAgent.notifyEditingTextChanged(i, editingText);
        } catch (RemoteException e) {
            HiLog.error(TAG, "notifyEditingTextChanged failed, Exception = %s", e.toString());
        }
    }

    public void notifySelectionChanged(int i, int i2) {
        try {
            HiLog.debug(TAG, "InputMethodController: notifySelectionChanged: start character index = %d, end character index = %d", Integer.valueOf(i), Integer.valueOf(i2));
            this.mInputMethodAgent.notifySelectionChanged(this.mPreSelectionStart, this.mPreSelectionEnd, i, i2);
            this.mPreSelectionStart = i;
            this.mPreSelectionEnd = i2;
        } catch (RemoteException e) {
            HiLog.error(TAG, "notifySelectionChanged failed, Exception = %s", e.toString());
        }
    }

    public boolean isAvailable() {
        try {
            HiLog.debug(TAG, "isAvailable start", new Object[0]);
            return this.mSystemAbility.isAvailable();
        } catch (RemoteException e) {
            HiLog.error(TAG, "isAvailable failed, Exception = %s", e.toString());
            return false;
        }
    }

    public int getKeyboardWindowHeight() {
        try {
            HiLog.debug(TAG, "getKeyboardWindowHeight start", new Object[0]);
            return this.mSystemAbility.getKeyboardWindowHeight();
        } catch (RemoteException e) {
            HiLog.error(TAG, "getKeyboardWindowHeight failed, Exception = %s", e.toString());
            return 0;
        }
    }

    public void sendRecommendationInfo(RecommendationInfo[] recommendationInfoArr) {
        try {
            HiLog.debug(TAG, "updateCompletionTexts start", new Object[0]);
            this.mInputMethodAgent.sendRecommendationInfo(recommendationInfoArr);
        } catch (RemoteException e) {
            HiLog.error(TAG, "updateCompletionTexts failed, Exception = %s", e.toString());
        }
    }
}
