package ohos.miscservices.inputmethodability;

import ohos.agp.components.ComponentContainer;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.internal.IInputControlChannel;
import ohos.rpc.RemoteException;

public final class KeyboardController {
    public static final int DISPLAY_MODE_FULL = 1;
    public static final int DISPLAY_MODE_PART = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "KeyboardController");
    private InputMethodEngine mInputMethodEngine;

    public KeyboardController(InputMethodEngine inputMethodEngine) {
        if (inputMethodEngine != null) {
            this.mInputMethodEngine = inputMethodEngine;
            return;
        }
        throw new IllegalArgumentException("The given InputMethodEngine for KeyboardController must not be null.");
    }

    public void setDisplayMode(int i) {
        HiLog.info(TAG, "setDisplayMode mode=%{public}d", Integer.valueOf(i));
        this.mInputMethodEngine.setDisplayMode(i);
    }

    public int getDisplayMode() {
        HiLog.info(TAG, "getDisplayMode", new Object[0]);
        return this.mInputMethodEngine.getDisplayMode();
    }

    public void hideKeyboard() {
        HiLog.info(TAG, "hideKeyboard", new Object[0]);
        IInputControlChannel inputControlChannel = this.mInputMethodEngine.getInputControlChannel();
        if (inputControlChannel != null) {
            try {
                inputControlChannel.hideKeyboardSelf(0);
            } catch (RemoteException e) {
                HiLog.error(TAG, "hideKeyboard RemoteException:%{public}s", e.getMessage());
            }
        } else {
            HiLog.error(TAG, "The input control channel is null, hideKeyboard failed.", new Object[0]);
        }
    }

    public boolean toNextInputMethod() {
        HiLog.info(TAG, "toNextInputMethod", new Object[0]);
        IInputControlChannel inputControlChannel = this.mInputMethodEngine.getInputControlChannel();
        if (inputControlChannel != null) {
            try {
                return inputControlChannel.toNextInputMethod();
            } catch (RemoteException e) {
                HiLog.error(TAG, "toNextInputMethod exception: %{public}s", e.getMessage());
            }
        } else {
            HiLog.error(TAG, "The input control channel is null, switch to next input method failed.", new Object[0]);
            return false;
        }
    }

    public int getScreenOrientation() {
        HiLog.info(TAG, "getScreenOrientation", new Object[0]);
        return this.mInputMethodEngine.getScreenOrientation();
    }

    public boolean setKeyboardContainer(ComponentContainer componentContainer) {
        HiLog.info(TAG, "setKeyboardContainer", new Object[0]);
        if (componentContainer != null) {
            return this.mInputMethodEngine.setKeyboardContainer(componentContainer);
        }
        throw new IllegalArgumentException("The given keyboard container can not be null.");
    }
}
