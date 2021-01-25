package ohos.miscservices.inputmethod.adapter;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.view.inputmethod.InputMethodInfo;
import com.android.internal.view.IInputMethodManager;
import java.util.List;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.InputMethodProperty;

public class InputMethodInfoAdapter {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodInfoAdapter");

    private InputMethodInfoAdapter() {
    }

    public static InputMethodProperty convertToInputMethodProperty(InputMethodInfo inputMethodInfo) {
        if (inputMethodInfo == null) {
            HiLog.error(TAG, "inputMethodInfo is null.", new Object[0]);
            return null;
        }
        InputMethodProperty inputMethodProperty = new InputMethodProperty(inputMethodInfo.getId());
        inputMethodProperty.setPackageName(inputMethodInfo.getPackageName());
        inputMethodProperty.setAbilityName(inputMethodInfo.getServiceName());
        inputMethodProperty.setConfigurationPage(inputMethodInfo.getSettingsActivity());
        inputMethodProperty.setSystemImeFlag(inputMethodInfo.isSystem());
        inputMethodProperty.setDefaultImeId(inputMethodInfo.getIsDefaultResourceId());
        int subtypeCount = inputMethodInfo.getSubtypeCount();
        for (int i = 0; i < subtypeCount; i++) {
            inputMethodProperty.addType(InputMethodSubtypeAdapter.convertToKeyboardType(inputMethodInfo.getSubtypeAt(i)));
        }
        return inputMethodProperty;
    }

    public static InputMethodInfo convertToInputMethodInfo(InputMethodProperty inputMethodProperty) {
        if (inputMethodProperty == null) {
            HiLog.error(TAG, "inputMethodProperty is null.", new Object[0]);
            return null;
        }
        try {
            try {
                List inputMethodList = IInputMethodManager.Stub.asInterface(ServiceManager.getServiceOrThrow("input_method")).getInputMethodList(UserHandle.myUserId());
                if (inputMethodList == null) {
                    return null;
                }
                for (int i = 0; i < inputMethodList.size(); i++) {
                    if (((InputMethodInfo) inputMethodList.get(i)).getId().equals(inputMethodProperty.getId())) {
                        return (InputMethodInfo) inputMethodList.get(i);
                    }
                }
                return null;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } catch (ServiceManager.ServiceNotFoundException e2) {
            throw new IllegalStateException((Throwable) e2);
        }
    }
}
