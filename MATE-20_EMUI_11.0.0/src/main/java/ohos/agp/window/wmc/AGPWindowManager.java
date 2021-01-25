package ohos.agp.window.wmc;

import android.content.Context;
import java.util.ArrayList;
import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AGPWindowManager {
    public static final int FLAG_ANDROID_CONTEXT = 10;
    public static final int FLAG_DIALOG_COMMON = 2;
    public static final int FLAG_DIALOG_INPUTMETHOD = 8;
    public static final int FLAG_DIALOG_LIST = 3;
    public static final int FLAG_DIALOG_POPUP = 4;
    public static final int FLAG_DIALOG_PRESENTATION = 7;
    public static final int FLAG_DIALOG_TOAST = 5;
    public static final int FLAG_WINDOW_CUSTOM = 6;
    public static final int FLAG_WINDOW_NORMAL = 1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "AGPWindow");
    private static final Object LOCK = new Object();
    private static volatile AGPWindowManager sInstance;
    private ArrayList<AGPWindow> mWindows = new ArrayList<>();

    public static int getAndroidGravity(int i) {
        if (i == 4) {
            return 3;
        }
        if (i == 8) {
            return 1;
        }
        if (i == 16) {
            return 5;
        }
        if (i == 32) {
            return 48;
        }
        if (i == 64) {
            return 16;
        }
        if (i == 72) {
            return 17;
        }
        if (i != 128) {
            return i;
        }
        return 80;
    }

    public static int getZidaneTextAlignment(int i) {
        if (i == 1) {
            return 8;
        }
        if (i == 3) {
            return 4;
        }
        if (i == 5) {
            return 16;
        }
        if (i == 48) {
            return 32;
        }
        if (i == 80) {
            return 128;
        }
        if (i == 16) {
            return 64;
        }
        if (i != 17) {
            return i;
        }
        return 72;
    }

    static {
        System.loadLibrary("agpwindow.z");
        System.loadLibrary("agp.z");
    }

    private AGPWindowManager() {
    }

    public static AGPWindowManager getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (AGPWindowManager.class) {
            if (sInstance == null) {
                sInstance = new AGPWindowManager();
            }
        }
        return sInstance;
    }

    public AGPWindow createWindowForAndroid(Context context) {
        AGPWindow aGPWindow = new AGPWindow(context);
        addWindow(aGPWindow);
        return aGPWindow;
    }

    public AGPWindow createWindow(ohos.app.Context context) {
        AGPWindow aGPWindow = new AGPWindow(context);
        addWindow(aGPWindow);
        return aGPWindow;
    }

    public AGPWindow createWindow(ohos.app.Context context, int i, int i2) {
        AGPCustomWindow aGPCustomWindow = new AGPCustomWindow(context, i, i2);
        addWindow(aGPCustomWindow);
        return aGPCustomWindow;
    }

    public AGPBaseDialogWindow createDialogWindow(ohos.app.Context context, int i) {
        AGPBaseDialogWindow aGPBaseDialogWindow;
        boolean z = false;
        HiLog.debug(LABEL, "AGPWindowManager createDialogWindow flag=%{public}%d", new Object[]{Integer.valueOf(i)});
        if (i == 4) {
            aGPBaseDialogWindow = new AGPPopupDialogWindow(context, i);
        } else if (i == 5) {
            aGPBaseDialogWindow = new AGPToastWindow(context, i);
            z = true;
        } else if (i != 7) {
            aGPBaseDialogWindow = new AGPCommonDialogWindow(context, i);
        } else {
            aGPBaseDialogWindow = new AGPPresentationWindow(context, i);
        }
        if (!z) {
            addWindow(aGPBaseDialogWindow);
        }
        return aGPBaseDialogWindow;
    }

    public void destroyWindow(AGPWindow aGPWindow) {
        if (aGPWindow == null) {
            HiLog.error(LABEL, "AGPWindowManager destroyWindow failed due to window is null", new Object[0]);
            return;
        }
        synchronized (LOCK) {
            removeWindow(aGPWindow);
            aGPWindow.destroy();
        }
    }

    public Optional<AGPWindow> getTopWindow() {
        synchronized (LOCK) {
            int size = this.mWindows.size();
            if (size > 0) {
                HiLog.debug(LABEL, "AGPWindowManager getTopWindow size=%{public}d", new Object[]{Integer.valueOf(size)});
                return Optional.of(this.mWindows.get(size - 1));
            }
            HiLog.error(LABEL, "AGPWindowManager getTopWindow return null", new Object[0]);
            return Optional.empty();
        }
    }

    public int windowsCount() {
        int size;
        synchronized (LOCK) {
            size = this.mWindows.size();
        }
        return size;
    }

    public void addView(AGPWindow aGPWindow) {
        if (aGPWindow != null) {
            aGPWindow.show();
        }
    }

    private void addWindow(AGPWindow aGPWindow) {
        if (aGPWindow == null) {
            HiLog.error(LABEL, "AGPWindowManager addWindow failed due to window is null", new Object[0]);
            return;
        }
        synchronized (LOCK) {
            this.mWindows.add(aGPWindow);
        }
    }

    private void removeWindow(AGPWindow aGPWindow) {
        if (aGPWindow == null) {
            HiLog.error(LABEL, "AGPWindowManager removeWindow failed due to window is null", new Object[0]);
        } else if (this.mWindows.contains(aGPWindow)) {
            this.mWindows.remove(aGPWindow);
            onWindowChange();
        }
    }

    private void onWindowChange() {
        AGPWindow aGPWindow;
        HiLog.debug(LABEL, "AGPWindowManager onWindowChange", new Object[0]);
        int size = this.mWindows.size();
        if (size > 0) {
            HiLog.debug(LABEL, "AGPWindowManager getTopWindow size=%{public}d", new Object[]{Integer.valueOf(size)});
            aGPWindow = this.mWindows.get(size - 1);
        } else {
            aGPWindow = null;
        }
        if (aGPWindow == null) {
            HiLog.error(LABEL, "not fond window.", new Object[0]);
        } else {
            aGPWindow.notifyBarrierFree();
        }
    }

    public static class BadWindowException extends RuntimeException {
        private static final long serialVersionUID = -7459801466658443342L;

        public BadWindowException() {
            this(null);
        }

        public BadWindowException(String str) {
            super(str);
        }
    }
}
