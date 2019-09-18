package com.huawei.android.pc;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.pc.IHwPCManager;
import android.util.HwPCUtils;
import android.util.Singleton;
import android.view.InputEvent;
import android.view.PointerIcon;
import com.huawei.android.app.HwRecentTaskInfoEx;
import java.util.List;

public class HwPCManagerEx {
    public static final int GUIDE_EVER_STARTED = 1;
    public static final int GUIDE_NOT_STARTED = 0;
    public static final String GUIDE_STARTED = "guide-started";
    private static final Singleton<IHwPCManager> gDefault = new Singleton<IHwPCManager>() {
        /* access modifiers changed from: protected */
        public IHwPCManager create() {
            return HwPCUtils.getHwPCManager();
        }
    };

    private static IHwPCManager getDefault() {
        return (IHwPCManager) gDefault.get();
    }

    public static int setPCOverScanMode(int mode) throws RemoteException {
        return getDefault().forceDisplayMode(mode);
    }

    public static List<String> getAllSupportPcAppList() throws RemoteException {
        return getDefault().getAllSupportPcAppList();
    }

    public static int getPCDisplayId() throws RemoteException {
        return getDefault().getPCDisplayId();
    }

    public static void execVoiceCmd(Message message) throws RemoteException {
        getDefault().execVoiceCmd(message);
    }

    public static HwRecentTaskInfoEx getHwRecentTaskInfo(int taskId) throws RemoteException {
        return new HwRecentTaskInfoEx(getDefault().getHwRecentTaskInfo(taskId));
    }

    public static void hwRestoreTask(int taskId, float x, float y) throws RemoteException {
        getDefault().hwRestoreTask(taskId, x, y);
    }

    public static void toggleHome() throws RemoteException {
        getDefault().toggleHome();
    }

    public static boolean injectInputEventExternal(InputEvent event, int mode) throws RemoteException {
        return getDefault().injectInputEventExternal(event, mode);
    }

    public static void setPointerIconType(int iconId, boolean keep) throws RemoteException {
        getDefault().setPointerIconType(iconId, keep);
    }

    public static void setCustomPointerIcon(PointerIcon icon, boolean keep) throws RemoteException {
        getDefault().setCustomPointerIcon(icon, keep);
    }

    public static Bitmap getTaskThumbnailEx(int id) throws RemoteException {
        return getDefault().getTaskThumbnailEx(id);
    }

    public static int getPackageSupportPcState(String packageName) throws RemoteException {
        return getDefault().getPackageSupportPcState(packageName);
    }

    public static Bitmap getDisplayBitmap(int displayId, int width, int height) throws RemoteException {
        return getDefault().getDisplayBitmap(displayId, width, height);
    }

    public static void hwResizeTask(int taskId, Rect bounds) throws RemoteException {
        getDefault().hwResizeTask(taskId, bounds);
    }

    public static void registHwSystemUIController(Messenger messenger) throws RemoteException {
        getDefault().registHwSystemUIController(messenger);
    }
}
