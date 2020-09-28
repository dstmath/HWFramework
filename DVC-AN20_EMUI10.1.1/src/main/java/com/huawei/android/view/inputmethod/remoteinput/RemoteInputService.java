package com.huawei.android.view.inputmethod.remoteinput;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.android.inputmethod.remoteinput.HwRemoteInputManager;
import com.huawei.android.inputmethod.remoteinput.IRemoteInputCallback;
import com.huawei.internal.telephony.PhoneConstantsEx;

public class RemoteInputService {
    private static final boolean IS_DEBUG_TV = SystemProperties.getBoolean("ro.config.remoteinput.debugtv", false);
    private static final boolean IS_REMOTE_INPUT_SUPPORT = SystemProperties.getBoolean("hw_sc.aa_distributed_input", true);
    private static final boolean IS_TV = "tv".equals(SystemProperties.get("ro.build.characteristics", PhoneConstantsEx.APN_TYPE_DEFAULT));
    private static final String REMOTE_INPUT_PERMISSION = "com.huawei.permission.ACCESS_REMOTE_INPUTSERVICE";
    private static final String TAG = "RemoteInputServiceEx";
    private static boolean sIsPermissionAllowed = false;

    private RemoteInputService() throws IllegalAccessException {
        throw new IllegalAccessException("RemoteInputService can not be instantiated");
    }

    public static void setContext(Context context) {
        Log.i(TAG, "set context called");
        if (context == null) {
            Log.e(TAG, "context is null, please check the context");
            return;
        }
        sIsPermissionAllowed = checkPermission(context);
        if (!sIsPermissionAllowed) {
            Log.e(TAG, "remote input permission not allowed");
        } else {
            HwRemoteInputManager.getInstance().setContext(context);
        }
    }

    public static void setText(String text, Bundle style) {
        if (IS_DEBUG_TV) {
            Log.i(TAG, "set text: " + text);
        }
        if (!sIsPermissionAllowed) {
            Log.e(TAG, "remote input permission not allowed");
        } else {
            HwRemoteInputManager.getInstance().setText(text, style);
        }
    }

    public static void notifyFocus(boolean isFocused) {
        Log.i(TAG, "notify focus with hasFocus: " + isFocused);
        if (!sIsPermissionAllowed) {
            Log.e(TAG, "remote input permission not allowed");
        } else {
            HwRemoteInputManager.getInstance().notifyFocus(isFocused);
        }
    }

    public static void setCallBack(RemoteInputCallback callback) {
        Log.i(TAG, "set call back");
        if (!sIsPermissionAllowed) {
            Log.e(TAG, "remote input permission not allowed");
        } else if (callback == null) {
            Log.e(TAG, "call back can not be null");
        } else {
            HwRemoteInputManager.getInstance().setCallBack(new RemoteInputCallbackStub(callback));
        }
    }

    private static boolean checkPermission(Context context) {
        if (context.checkCallingOrSelfPermission(REMOTE_INPUT_PERMISSION) != 0) {
            Log.e(TAG, "permission not requested");
            return false;
        } else if (!IS_REMOTE_INPUT_SUPPORT) {
            Log.e(TAG, "remote input function not supported");
            return false;
        } else if (IS_DEBUG_TV || IS_TV) {
            return true;
        } else {
            Log.e(TAG, "current device is not TV");
            return false;
        }
    }

    private static class RemoteInputCallbackStub extends IRemoteInputCallback.Stub {
        private RemoteInputCallback mRemoteInputCallback;

        RemoteInputCallbackStub(RemoteInputCallback remoteInputCallback) {
            this.mRemoteInputCallback = remoteInputCallback;
        }

        public void setText(String text, Bundle style) {
            if (RemoteInputService.IS_DEBUG_TV) {
                Log.i(RemoteInputService.TAG, "text from remote input servcie to mini keyboard " + text);
            }
            RemoteInputCallback remoteInputCallback = this.mRemoteInputCallback;
            if (remoteInputCallback != null) {
                remoteInputCallback.setText(text);
            }
        }

        public void notifyFocus(boolean isFocused) {
            Log.i(RemoteInputService.TAG, "notify focus from remote input service and do nothing here");
        }
    }
}
