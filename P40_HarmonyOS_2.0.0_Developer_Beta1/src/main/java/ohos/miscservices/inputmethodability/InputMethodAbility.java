package ohos.miscservices.inputmethodability;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.DirectionalLayout;
import ohos.global.configuration.Configuration;
import ohos.global.resource.ResourceManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethodability.implement.InputMethodInterfaceAdapterSkeleton;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public abstract class InputMethodAbility extends Ability {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodAbility");
    private InputMethodEngine mInputMethodEngine;
    private int mPreDirection;

    /* access modifiers changed from: protected */
    public abstract KeyboardDelegate onCreateKeyboardInterface();

    /* access modifiers changed from: protected */
    public void onStart(Intent intent) {
        InputMethodAbility.super.onStart(intent);
        HiLog.info(TAG, "onStart begin.", new Object[0]);
        KeyboardDelegate onCreateKeyboardInterface = onCreateKeyboardInterface();
        if (onCreateKeyboardInterface != null) {
            this.mInputMethodEngine = new InputMethodEngine(this, onCreateKeyboardInterface);
            ResourceManager resourceManager = getResourceManager();
            if (resourceManager != null) {
                this.mPreDirection = resourceManager.getConfiguration().direction;
            }
            HiLog.debug(TAG, "preDirection is %{public}d.", Integer.valueOf(this.mPreDirection));
            return;
        }
        throw new IllegalArgumentException("Custom input method ability must create a not null KeyboardDelegate.");
    }

    /* access modifiers changed from: protected */
    public IRemoteObject onConnect(Intent intent) {
        InputMethodAbility.super.onConnect(intent);
        IRemoteObject iRemoteObject = null;
        try {
            iRemoteObject = InputMethodInterfaceAdapterSkeleton.asInterface(null).onAbilityConnected(this, this.mInputMethodEngine);
            if (iRemoteObject == null) {
                HiLog.error(TAG, "The inputMethodInterface is null.", new Object[0]);
            } else {
                HiLog.info(TAG, "get inputMethodInterface successed.", new Object[0]);
            }
        } catch (RemoteException e) {
            HiLog.error(TAG, "onAbilityConnected failed, Exception = %s", e.toString());
        }
        return iRemoteObject;
    }

    /* access modifiers changed from: protected */
    public void onDisconnect(Intent intent) {
        HiLog.info(TAG, "onDisconnect.", new Object[0]);
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        InputMethodAbility.super.onStop();
        this.mInputMethodEngine.onStop();
        this.mInputMethodEngine = null;
    }

    /* access modifiers changed from: protected */
    public void onCommand(Intent intent, boolean z, int i) {
        HiLog.info(TAG, "onCommand.", new Object[0]);
    }

    public void onConfigurationUpdated(Configuration configuration) {
        InputMethodAbility.super.onConfigurationUpdated(configuration);
        if (configuration.direction != this.mPreDirection) {
            this.mPreDirection = configuration.direction;
            HiLog.info(TAG, "preDirection is %{public}d.", Integer.valueOf(this.mPreDirection));
            onScreenOrientationChanged(this.mPreDirection);
            this.mInputMethodEngine.updateConfiguration();
        }
    }

    /* access modifiers changed from: protected */
    public ComponentContainer onCreateKeyboardContainer() {
        HiLog.info(TAG, "onCreateKeyboardContainer.", new Object[0]);
        return new DirectionalLayout(this);
    }

    /* access modifiers changed from: protected */
    public void onBeginInput(KeyboardController keyboardController, TextInputClient textInputClient) {
        HiLog.info(TAG, "onBeginInput.", new Object[0]);
    }

    /* access modifiers changed from: protected */
    public void onEndInput() {
        HiLog.info(TAG, "onEndInput.", new Object[0]);
    }

    /* access modifiers changed from: protected */
    public void onShowKeyboard() {
        HiLog.info(TAG, "onShowKeyboard.", new Object[0]);
    }

    /* access modifiers changed from: protected */
    public void onScreenOrientationChanged(int i) {
        HiLog.info(TAG, "onScreenOrientationChanged.", new Object[0]);
    }

    /* access modifiers changed from: protected */
    public void onHideKeyboard() {
        HiLog.info(TAG, "onHideKeyboard.", new Object[0]);
    }

    /* access modifiers changed from: package-private */
    public void onStartInputInternal() {
        HiLog.info(TAG, "onStartInputInternal", new Object[0]);
        onBeginInput(new KeyboardController(this.mInputMethodEngine), new TextInputClient(this.mInputMethodEngine));
    }
}
