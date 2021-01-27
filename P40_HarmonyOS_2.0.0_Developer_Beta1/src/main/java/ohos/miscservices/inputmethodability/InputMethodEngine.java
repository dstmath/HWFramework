package ohos.miscservices.inputmethodability;

import java.lang.ref.WeakReference;
import ohos.agp.components.ComponentContainer;
import ohos.agp.window.wmc.AGPInputWindow;
import ohos.agp.window.wmc.AGPWindow;
import ohos.bundle.AbilityInfo;
import ohos.global.resource.ResourceManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.EditorAttribute;
import ohos.miscservices.inputmethod.InputDataChannel;
import ohos.miscservices.inputmethod.RichContent;
import ohos.miscservices.inputmethod.internal.IInputControlChannel;
import ohos.multimodalinput.event.KeyEvent;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.utils.net.Uri;

public class InputMethodEngine {
    private static final int ROW_NUM = 3;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodEngine");
    private WeakReference<InputMethodAbility> mAbilityRefer;
    private int mDisplayMode;
    private EditorAttribute mEditorAttribute;
    private boolean mHasShownWindow;
    private IInputControlChannel mInputControlChannel;
    private InputDataChannel mInputDataChannel;
    private AGPInputWindow mKeyBoardWindow;
    private KeyboardDelegate mKeyboardDelegate;

    public InputMethodEngine(InputMethodAbility inputMethodAbility, KeyboardDelegate keyboardDelegate) {
        this.mAbilityRefer = new WeakReference<>(inputMethodAbility);
        this.mKeyboardDelegate = keyboardDelegate;
        initKeyboardWindow();
    }

    private void initKeyboardWindow() {
        InputMethodAbility inputMethodAbility = this.mAbilityRefer.get();
        if (inputMethodAbility == null) {
            HiLog.error(TAG, "The current local input method ability is null, can not create keyboard window.", new Object[0]);
        } else if (inputMethodAbility.getHostContext() == null) {
            HiLog.error(TAG, "The host context is null, can not create keyboard window.", new Object[0]);
        } else {
            AbilityInfo.AbilitySubType abilitySubType = null;
            if (inputMethodAbility.getAbilityInfo() != null) {
                abilitySubType = inputMethodAbility.getAbilityInfo().getSubType();
            }
            if (abilitySubType != AbilityInfo.AbilitySubType.CA) {
                HiLog.error(TAG, "the ability type is not CA, can not create AGPInputWindow.", new Object[0]);
                return;
            }
            this.mKeyBoardWindow = new AGPInputWindow(inputMethodAbility, "InputMethod", (AGPInputWindow.Callback) null, (int) KeyEvent.KEY_POUND, false);
            this.mKeyBoardWindow.setGravity(128);
            AGPWindow.LayoutParams attributes = this.mKeyBoardWindow.getAttributes();
            if (attributes == null) {
                HiLog.error(TAG, "the AGPInputWindow LayoutParams is null.", new Object[0]);
                return;
            }
            attributes.dimAmount = 0.0f;
            attributes.width = -1;
            attributes.height = -2;
            this.mKeyBoardWindow.setAttributes(attributes);
            ComponentContainer onCreateKeyboardContainer = inputMethodAbility.onCreateKeyboardContainer();
            if (onCreateKeyboardContainer != null) {
                this.mKeyBoardWindow.setContentLayout(onCreateKeyboardContainer);
                return;
            }
            throw new IllegalArgumentException("The initial keyboard must be given and not be null.");
        }
    }

    public IInputControlChannel getInputControlChannel() {
        return this.mInputControlChannel;
    }

    public void initializeInput(IRemoteObject iRemoteObject, int i, IInputControlChannel iInputControlChannel) {
        this.mInputControlChannel = iInputControlChannel;
        AGPInputWindow aGPInputWindow = this.mKeyBoardWindow;
        if (aGPInputWindow == null) {
            HiLog.error(TAG, "The keyboard window is null, set token failed.", new Object[0]);
        } else {
            aGPInputWindow.setToken(iRemoteObject);
        }
    }

    public boolean startInput(IRemoteObject iRemoteObject, EditorAttribute editorAttribute, InputDataChannel inputDataChannel) {
        HiLog.info(TAG, "startInput", new Object[0]);
        this.mInputDataChannel = inputDataChannel;
        this.mEditorAttribute = editorAttribute;
        InputMethodAbility inputMethodAbility = this.mAbilityRefer.get();
        if (inputMethodAbility == null) {
            HiLog.error(TAG, "startInput failed: current ability is null", new Object[0]);
            return false;
        }
        inputMethodAbility.onStartInputInternal();
        return true;
    }

    public boolean showKeyboard(int i) {
        HiLog.info(TAG, "showKeyboard", new Object[0]);
        if (this.mHasShownWindow) {
            HiLog.info(TAG, "keyboard is showing.", new Object[0]);
            return true;
        }
        prepareWindow();
        AGPInputWindow aGPInputWindow = this.mKeyBoardWindow;
        if (aGPInputWindow == null) {
            HiLog.error(TAG, "The keyboard window is null showKeyboard failed.", new Object[0]);
            return false;
        }
        aGPInputWindow.show();
        this.mHasShownWindow = true;
        InputMethodAbility inputMethodAbility = this.mAbilityRefer.get();
        if (inputMethodAbility == null) {
            HiLog.error(TAG, "showKeyboard the mAbility is null", new Object[0]);
            return false;
        }
        inputMethodAbility.onShowKeyboard();
        return true;
    }

    public void updateConfiguration() {
        HiLog.info(TAG, "updateConfiguration", new Object[0]);
        if (this.mHasShownWindow) {
            hideKeyboard(1);
            showKeyboard(1);
        }
    }

    public boolean hideKeyboard(int i) {
        HiLog.info(TAG, "hideKeyboard", new Object[0]);
        AGPInputWindow aGPInputWindow = this.mKeyBoardWindow;
        if (aGPInputWindow == null) {
            HiLog.error(TAG, "The keyboard window is null hideKeyboard failed.", new Object[0]);
            return false;
        }
        aGPInputWindow.hide();
        this.mHasShownWindow = false;
        InputMethodAbility inputMethodAbility = this.mAbilityRefer.get();
        if (inputMethodAbility == null) {
            HiLog.error(TAG, "hideKeyboard the mAbility is null", new Object[0]);
            return false;
        }
        inputMethodAbility.onHideKeyboard();
        return true;
    }

    public InputDataChannel getInputDataChannel() {
        return this.mInputDataChannel;
    }

    public EditorAttribute getEditorAttribute() {
        return this.mEditorAttribute;
    }

    public boolean dispatchKeyBoardEvent(KeyEvent keyEvent) {
        HiLog.info(TAG, "dispatchKeyEvent, keyCode is %{public}s", Integer.valueOf(keyEvent.getKeyCode()));
        if (keyEvent.isKeyDown()) {
            HiLog.info(TAG, "dispatchKeyEvent,key down", new Object[0]);
            return this.mKeyboardDelegate.onKeyDown(keyEvent);
        }
        HiLog.info(TAG, "dispatchKeyEvent,key up", new Object[0]);
        return this.mKeyboardDelegate.onKeyUp(keyEvent);
    }

    public void notifySelectionChanged(int i, int i2, int i3, int i4) {
        HiLog.debug(TAG, "InputMethodAgentSkeleton, notifySelectionChanged start", new Object[0]);
        this.mKeyboardDelegate.onSelectionChanged(i, i2, i3, i4);
    }

    public void notifyEditingTextChanged(int i, EditingText editingText) {
        HiLog.debug(TAG, "InputMethodAgentSkeleton, notifyEditingTextChanged, token:%{public}d", Integer.valueOf(i));
        this.mKeyboardDelegate.onTextChanged(i, editingText);
    }

    public void notifyCursorCoordinateChanged(float f, float f2, float f3, float[] fArr) {
        HiLog.debug(TAG, "InputMethodAgentSkeleton, notifyCursorCoordinateChanged start", new Object[0]);
        float[] fArr2 = {f, f2, 1.0f};
        HiLog.debug(TAG, "InputMethodAgentSkeleton, localPositions: leftPos is %{public}s, topPos is %{public}s", Float.valueOf(fArr2[0]), Float.valueOf(fArr2[1]));
        float[] fArr3 = new float[3];
        for (int i = 0; i < 3; i++) {
            for (int i2 = 0; i2 < 3; i2++) {
                fArr3[i] = fArr3[i] + (fArr[(i * 3) + i2] * fArr2[i2]);
            }
        }
        HiLog.debug(TAG, "InputMethodAgentSkeleton, screenPositions: leftPos is %{public}s, topPos is %{public}s", Float.valueOf(fArr3[0]), Float.valueOf(fArr3[1]));
        this.mKeyboardDelegate.onCursorContextChanged(fArr3[0], fArr3[1], f2 - f3);
    }

    public void setDisplayMode(int i) {
        this.mDisplayMode = i;
    }

    public int getDisplayMode() {
        return this.mDisplayMode;
    }

    public int getScreenOrientation() {
        InputMethodAbility inputMethodAbility = this.mAbilityRefer.get();
        if (inputMethodAbility == null) {
            HiLog.error(TAG, "getScreenOrientation failed.", new Object[0]);
            return 0;
        }
        ResourceManager resourceManager = inputMethodAbility.getResourceManager();
        if (resourceManager != null) {
            return resourceManager.getConfiguration().direction;
        }
        HiLog.error(TAG, "resourceManager is null, getScreenOrientation failed.", new Object[0]);
        return 0;
    }

    public boolean setKeyboardContainer(ComponentContainer componentContainer) {
        AGPInputWindow aGPInputWindow = this.mKeyBoardWindow;
        if (aGPInputWindow == null) {
            HiLog.error(TAG, "The keyboard window is null, set keyboard container failed.", new Object[0]);
            return false;
        }
        aGPInputWindow.setContentLayout(componentContainer);
        return true;
    }

    private void prepareWindow() {
        HiLog.info(TAG, "prepareWindow", new Object[0]);
        try {
            if (this.mInputControlChannel != null) {
                this.mInputControlChannel.reportScreenMode(this.mDisplayMode);
            } else {
                HiLog.warn(TAG, "The input control channel is null, report screen mode failed.", new Object[0]);
            }
        } catch (RemoteException e) {
            HiLog.error(TAG, "reportScreenMode exception: %{public}s", e.getMessage());
        }
        AGPInputWindow aGPInputWindow = this.mKeyBoardWindow;
        if (aGPInputWindow == null) {
            HiLog.error(TAG, "The keyboard window is null, prepareWidow failed.", new Object[0]);
            return;
        }
        AGPWindow.LayoutParams attributes = aGPInputWindow.getAttributes();
        if (attributes == null) {
            HiLog.error(TAG, "the AGPInputWindow LayoutParams is null.", new Object[0]);
        } else if (this.mDisplayMode == 1) {
            HiLog.info(TAG, "the ime wants to be in SCREEN_MODE_FULL mode", new Object[0]);
            attributes.height = -1;
            attributes.width = -1;
            this.mKeyBoardWindow.updateAttributes(attributes);
        } else {
            HiLog.info(TAG, "the ime wants to be in normal mode", new Object[0]);
            attributes.width = -1;
            attributes.height = -2;
            this.mKeyBoardWindow.updateAttributes(attributes);
        }
    }

    public void onStop() {
        AGPInputWindow aGPInputWindow = this.mKeyBoardWindow;
        if (aGPInputWindow == null) {
            HiLog.error(TAG, "The keyboard window is null, destroy window failed.", new Object[0]);
        } else {
            aGPInputWindow.destroy();
        }
    }

    public void setContentPermission(RichContent richContent) {
        Uri dataUri = richContent.getDataUri();
        EditorAttribute editorAttribute = this.mEditorAttribute;
        if (editorAttribute == null) {
            HiLog.error(TAG, "Current EditorAttribute is null, set rich content uri permission failed.", new Object[0]);
            return;
        }
        String clientPackage = editorAttribute.getClientPackage();
        IInputControlChannel iInputControlChannel = this.mInputControlChannel;
        if (iInputControlChannel == null) {
            HiLog.error(TAG, "Current Input controller channel is null, set rich content uri permission failed.", new Object[0]);
            return;
        }
        try {
            richContent.setUriPermission(iInputControlChannel.createUriPermission(dataUri, clientPackage));
        } catch (RemoteException unused) {
            HiLog.error(TAG, "set rich content uri permission RemoteException", new Object[0]);
        }
    }
}
