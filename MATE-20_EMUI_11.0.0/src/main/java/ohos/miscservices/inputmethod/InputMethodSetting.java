package ohos.miscservices.inputmethod;

import java.util.Collections;
import java.util.List;
import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.implement.InputMethodSystemAbilitySkeleton;
import ohos.miscservices.inputmethod.interfaces.IInputMethodSystemAbility;
import ohos.rpc.RemoteException;

@SystemApi
public class InputMethodSetting {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodSetting");
    private static volatile InputMethodSetting sInstance = null;
    private IInputMethodSystemAbility mSystemAbility = InputMethodSystemAbilitySkeleton.asInterface(null);

    private InputMethodSetting() {
    }

    @SystemApi
    public static InputMethodSetting getInstance() {
        if (sInstance == null) {
            synchronized (InputMethodSetting.class) {
                if (sInstance == null) {
                    sInstance = new InputMethodSetting();
                }
            }
        }
        return sInstance;
    }

    @SystemApi
    public KeyboardType getCurrentKeyboardType() {
        try {
            HiLog.debug(TAG, "getCurrentKeyboardType start", new Object[0]);
            return this.mSystemAbility.getCurrentKeyboardType();
        } catch (RemoteException e) {
            HiLog.error(TAG, "getCurrentKeyboardType failed,Exception = %s", e.toString());
            return null;
        }
    }

    @SystemApi
    public List<InputMethodProperty> listInputMethodEnabled() {
        try {
            HiLog.debug(TAG, "listInputMethodEnabled start", new Object[0]);
            return this.mSystemAbility.listInputMethodEnabled();
        } catch (RemoteException e) {
            HiLog.error(TAG, "listInputMethodEnabled failed,Exception = %s", e.toString());
            return Collections.emptyList();
        }
    }

    @SystemApi
    public List<KeyboardType> listKeyboardType(InputMethodProperty inputMethodProperty) {
        try {
            HiLog.debug(TAG, "listKeyboardType start", new Object[0]);
            return this.mSystemAbility.listKeyboardType(inputMethodProperty);
        } catch (RemoteException e) {
            HiLog.error(TAG, "listKeyboardType failed,Exception = %s", e.toString());
            return Collections.emptyList();
        }
    }

    @SystemApi
    public List<InputMethodProperty> listInputMethod() {
        try {
            HiLog.debug(TAG, "listInputMethod start", new Object[0]);
            return this.mSystemAbility.listInputMethod();
        } catch (RemoteException e) {
            HiLog.error(TAG, "listInputMethod failed,Exception = %s", e.toString());
            return Collections.emptyList();
        }
    }

    @SystemApi
    public void displayOptionalInputMethod() {
        try {
            HiLog.debug(TAG, "displayOptionalInputMethod start", new Object[0]);
            this.mSystemAbility.displayOptionalInputMethod();
        } catch (RemoteException e) {
            HiLog.error(TAG, "displayOptionalInputMethod failed,Exception = %s", e.toString());
        }
    }
}
