package com.huawei.android.pc;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.pc.IHwPCManager;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Singleton;
import android.view.InputEvent;
import android.view.PointerIcon;
import com.huawei.android.app.HwRecentTaskInfoEx;
import java.util.ArrayList;
import java.util.List;

public class HwPCManagerEx {
    public static final int GUIDE_EVER_STARTED = 1;
    public static final int GUIDE_NOT_STARTED = 0;
    public static final String GUIDE_STARTED = "guide-started";
    private static final Singleton<IHwPCManager> G_DEFAULT = new Singleton<IHwPCManager>() {
        /* class com.huawei.android.pc.HwPCManagerEx.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public IHwPCManager create() {
            return HwPCUtils.getHwPCManager();
        }
    };
    public static final int MULTI_DISPLAY_MODE_HICAR = 1;
    private static final String TAG = "HwPCManagerEx";

    private static IHwPCManager getDefault() {
        return (IHwPCManager) G_DEFAULT.get();
    }

    public static int setPCOverScanMode(int mode) throws RemoteException {
        if (getDefault() != null) {
            return getDefault().forceDisplayMode(mode);
        }
        Log.i(TAG, "setPCOverScanMode getDefault is null.");
        return -1;
    }

    public static List<String> getAllSupportPcAppList() throws RemoteException {
        if (getDefault() != null) {
            return getDefault().getAllSupportPcAppList();
        }
        Log.i(TAG, "getAllSupportPcAppList getDefault is null.");
        return new ArrayList();
    }

    public static int getPCDisplayId() throws RemoteException {
        if (getDefault() != null) {
            return getDefault().getPCDisplayId();
        }
        Log.i(TAG, "getPCDisplayId getDefault is null.");
        return -1;
    }

    public static void execVoiceCmd(Message message) throws RemoteException {
        if (getDefault() == null) {
            Log.i(TAG, "execVoiceCmd getDefault is null.");
        } else {
            getDefault().execVoiceCmd(message);
        }
    }

    public static HwRecentTaskInfoEx getHwRecentTaskInfo(int taskId) throws RemoteException {
        if (getDefault() != null) {
            return new HwRecentTaskInfoEx(getDefault().getHwRecentTaskInfo(taskId));
        }
        Log.i(TAG, "getHwRecentTaskInfo getDefault is null.");
        return null;
    }

    public static void hwRestoreTask(int taskId, float x, float y) throws RemoteException {
        if (getDefault() == null) {
            Log.i(TAG, "hwRestoreTask getDefault is null.");
        } else {
            getDefault().hwRestoreTask(taskId, x, y);
        }
    }

    public static void toggleHome() throws RemoteException {
        if (getDefault() == null) {
            Log.i(TAG, "toggleHome getDefault is null.");
        } else {
            getDefault().toggleHome();
        }
    }

    public static boolean injectInputEventExternal(InputEvent event, int mode) throws RemoteException {
        if (getDefault() != null) {
            return getDefault().injectInputEventExternal(event, mode);
        }
        Log.i(TAG, "injectInputEventExternal getDefault is null.");
        return false;
    }

    public static void setPointerIconType(int iconId, boolean keep) throws RemoteException {
        if (getDefault() == null) {
            Log.i(TAG, "setPointerIconType getDefault is null.");
        } else {
            getDefault().setPointerIconType(iconId, keep);
        }
    }

    public static void setCustomPointerIcon(PointerIcon icon, boolean keep) throws RemoteException {
        if (getDefault() == null) {
            Log.i(TAG, "setCustomPointerIcon getDefault is null.");
        } else {
            getDefault().setCustomPointerIcon(icon, keep);
        }
    }

    public static Bitmap getTaskThumbnailEx(int id) throws RemoteException {
        if (getDefault() != null) {
            return getDefault().getTaskThumbnailEx(id);
        }
        Log.i(TAG, "getTaskThumbnailEx getDefault is null.");
        return null;
    }

    public static int getPackageSupportPcState(String packageName) throws RemoteException {
        if (getDefault() != null) {
            return getDefault().getPackageSupportPcState(packageName);
        }
        Log.i(TAG, "getPackageSupportPcState getDefault is null.");
        return -1;
    }

    public static Bitmap getDisplayBitmap(int displayId, int width, int height) throws RemoteException {
        if (getDefault() != null) {
            return getDefault().getDisplayBitmap(displayId, width, height);
        }
        Log.i(TAG, "getDisplayBitmap getDefault is null.");
        return null;
    }

    public static void hwResizeTask(int taskId, Rect bounds) throws RemoteException {
        if (getDefault() == null) {
            Log.i(TAG, "hwResizeTask getDefault is null.");
        } else {
            getDefault().hwResizeTask(taskId, bounds);
        }
    }

    public static void registHwSystemUIController(Messenger messenger) throws RemoteException {
        if (getDefault() == null) {
            Log.i(TAG, "registHwSystemUIController getDefault is null.");
        } else {
            getDefault().registHwSystemUIController(messenger);
        }
    }

    public static boolean isInWindowsCastMode() {
        if (getDefault() == null) {
            Log.i(TAG, "isInWindowsCastMode getDefault is null.");
            return false;
        }
        try {
            return getDefault().isInWindowsCastMode();
        } catch (RemoteException e) {
            Log.e(TAG, "isInWindowsCastMode RemoteException.");
            return false;
        }
    }

    public static boolean isInSinkWindowsCastMode() {
        if (getDefault() == null) {
            Log.i(TAG, "isInSinkWindowsCastMode getDefault is null.");
            return false;
        }
        try {
            return getDefault().isInSinkWindowsCastMode();
        } catch (RemoteException e) {
            Log.e(TAG, "isInSinkWindowsCastMode RemoteException.");
            return false;
        }
    }

    public static void setIsInSinkWindowsCastMode(boolean isInCastMode) {
        if (getDefault() == null) {
            Log.i(TAG, "setIsInSinkWindowsCastMode getDefault is null.");
            return;
        }
        try {
            getDefault().setIsInSinkWindowsCastMode(isInCastMode);
        } catch (RemoteException e) {
            Log.e(TAG, "setIsInSinkWindowsCastMode RemoteException.");
        }
    }

    public static void setIsSinkHasKeyboard(boolean isKeyboardExist) {
        if (getDefault() == null) {
            Log.i(TAG, "setIsSinkHasKeyboard getDefault is null.");
            return;
        }
        try {
            getDefault().setIsSinkHasKeyboard(isKeyboardExist);
        } catch (RemoteException e) {
            Log.e(TAG, "setIsSinkHasKeyboard RemoteException.");
        }
    }

    public static int getFocusedDisplayId() {
        if (getDefault() == null) {
            Log.i(TAG, "getFocusedDisplayId getDefault is null.");
            return -1;
        }
        try {
            return getDefault().getFocusedDisplayId();
        } catch (RemoteException e) {
            Log.e(TAG, "getFocusedDisplayId RemoteException.");
            return -1;
        }
    }

    public static boolean isAvoidShowDefaultKeyguard(int displayId) {
        if (getDefault() == null) {
            Log.i(TAG, "isAvoidShowDefaultKeyguard getDefault is null.");
            return false;
        }
        try {
            return getDefault().isAvoidShowDefaultKeyguard(displayId);
        } catch (RemoteException e) {
            Log.e(TAG, "isAvoidShowDefaultKeyguard RemoteException.");
            return false;
        }
    }

    public static void setPadAssistant(boolean isAssistWithPAD) {
        if (getDefault() == null) {
            Log.i(TAG, "setPadAssistant getDefault is null.");
            return;
        }
        try {
            getDefault().setPadAssistant(isAssistWithPAD);
        } catch (RemoteException e) {
            Log.e(TAG, "setPadAssistant RemoteException.");
        }
    }

    public static boolean isPadAssistantMode() {
        if (getDefault() == null) {
            Log.i(TAG, "isPadAssistantMode getDefault is null.");
            return false;
        }
        try {
            return getDefault().isPadAssistantMode();
        } catch (RemoteException e) {
            Log.e(TAG, "isPadAssistantMode RemoteException.");
            return false;
        }
    }

    public static void setIsInBasicMode(boolean isInBasicMode) {
        if (getDefault() == null) {
            Log.i(TAG, "setIsInBasicMode getDefault is null.");
            return;
        }
        try {
            getDefault().setIsInBasicMode(isInBasicMode);
        } catch (RemoteException e) {
            Log.e(TAG, "setIsInBasicMode RemoteException.");
        }
    }

    public static boolean isInBasicMode() {
        if (getDefault() == null) {
            Log.i(TAG, "isInBasicMode getDefault is null.");
            return false;
        }
        try {
            return getDefault().isInBasicMode();
        } catch (RemoteException e) {
            Log.e(TAG, "isInBasicMode RemoteException.");
            return false;
        }
    }

    public static boolean isModeSupportDrag() {
        if (getDefault() == null) {
            Log.i(TAG, "isModeSupportDrag getDefault is null.");
            return false;
        }
        try {
            return getDefault().isModeSupportDrag();
        } catch (RemoteException e) {
            Log.e(TAG, "isModeSupportDrag RemoteException.");
            return false;
        }
    }

    public static void enterFullScreen(int taskId) {
        if (getDefault() == null) {
            Log.i(TAG, "enterFullScreen getDefault is null.");
            return;
        }
        try {
            getDefault().enterFullScreen(taskId);
        } catch (RemoteException e) {
            Log.e(TAG, "enterFullScreen RemoteException.");
        }
    }

    public static void exitFullScreen(int taskId) {
        if (getDefault() == null) {
            Log.i(TAG, "exitFullScreen getDefault is null.");
            return;
        }
        try {
            getDefault().exitFullScreen(taskId);
        } catch (RemoteException e) {
            Log.e(TAG, "exitFullScreen RemoteException.");
        }
    }

    public static boolean isScreenOnAccurately() {
        try {
            if (getDefault() != null) {
                return getDefault().isScreenOnAccurately();
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "isScreenOnAccurately RemoteException");
            return true;
        }
    }

    public static void setDockBarInfo(int mode, Bundle info) {
        if (getDefault() == null) {
            Log.i(TAG, "setDockBarInfo getDefault is null.");
            return;
        }
        try {
            getDefault().setDockBarInfo(mode, info);
        } catch (RemoteException e) {
            Log.e(TAG, "setDockInfo RemoteException");
        }
    }
}
