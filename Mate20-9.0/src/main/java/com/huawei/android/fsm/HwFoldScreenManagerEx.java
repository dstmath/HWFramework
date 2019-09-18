package com.huawei.android.fsm;

import android.os.Bundle;
import android.os.IBinder;
import com.huawei.android.fsm.IFoldDisplayModeListener;
import com.huawei.android.fsm.IFoldableStateListener;
import java.util.HashMap;
import java.util.Map;

public final class HwFoldScreenManagerEx {
    public static final int DISPLAY_MODE_COORDINATION = 4;
    public static final int DISPLAY_MODE_FULL = 1;
    public static final int DISPLAY_MODE_MAIN = 2;
    public static final int DISPLAY_MODE_SUB = 3;
    public static final int DISPLAY_MODE_UNKNOWN = 0;
    public static final int FOLD_STATE_EXPAND = 1;
    public static final int FOLD_STATE_FOLDED = 2;
    public static final int FOLD_STATE_HALF_FOLDED = 3;
    public static final int FOLD_STATE_UNKNOWN = 0;
    public static final int LISTENER_TYPE_FOLD = 1;
    public static final int LISTENER_TYPE_POSTURE = 2;
    public static final int LISTENER_TYPE_UNKNOWN = 0;
    public static final int POSTURE_FOLDED = 103;
    @Deprecated
    public static final int POSTURE_FULCRUM_MAIN = -1;
    @Deprecated
    public static final int POSTURE_FULCRUM_SUB = -1;
    public static final int POSTURE_FULL = 109;
    public static final int POSTURE_HALF_FOLDED = 106;
    @Deprecated
    public static final int POSTURE_HANDHELD_FOLDED = -1;
    public static final int POSTURE_HANDHELD_FOLDED_MAIN = 104;
    public static final int POSTURE_HANDHELD_FOLDED_SUB = 105;
    @Deprecated
    public static final int POSTURE_HANDHELD_HALF_FOLD = -1;
    public static final int POSTURE_LAY_FLAT_MAIN_UP = 101;
    public static final int POSTURE_LAY_FLAT_SUB_UP = 102;
    public static final int POSTURE_OTHER = 199;
    @Deprecated
    public static final int POSTURE_TWO_FULCRUM_LANDSCAPE = -1;
    @Deprecated
    public static final int POSTURE_TWO_FULCRUM_PORTRAIT = -1;
    public static final int POSTURE_UNKNOWN = 100;
    private static final String TAG = "HwFoldScreenManagerEx";
    private static final Map<FoldDisplayModeListener, IFoldDisplayModeListener> sFoldDisplayModeListenerMap = new HashMap();
    private static final Map<FoldableStateListener, IFoldableStateListener> sFoldableStateListenerMap = new HashMap();

    public interface FoldDisplayModeListener {
        void onScreenDisplayModeChange(int i);
    }

    public interface FoldableStateListener {
        void onStateChange(Bundle bundle);
    }

    public static boolean isFoldable() {
        return HwFoldScreenManager.isFoldable();
    }

    public static int getPosture() {
        return HwFoldScreenManager.getPosture();
    }

    public static int getFoldableState() {
        return HwFoldScreenManager.getFoldableState();
    }

    public static void registerFoldableState(FoldableStateListener listener, int type) {
        if (listener != null && type != 0) {
            HwFoldScreenManager.registerFoldableState(addFoldableStateListener(listener), type);
        }
    }

    public static void unregisterFoldableState(FoldableStateListener listener) {
        if (listener != null) {
            HwFoldScreenManager.unregisterFoldableState(removeFoldableStateListener(listener));
        }
    }

    public static int setDisplayMode(int mode) {
        return HwFoldScreenManager.setDisplayMode(mode);
    }

    public static int getDisplayMode() {
        return HwFoldScreenManager.getDisplayMode();
    }

    public static int lockDisplayMode(int mode) {
        return HwFoldScreenManager.lockDisplayMode(mode);
    }

    public static int unlockDisplayMode() {
        return HwFoldScreenManager.unlockDisplayMode();
    }

    public static void registerFoldDisplayMode(FoldDisplayModeListener listener) {
        if (listener != null) {
            HwFoldScreenManager.registerFoldDisplayMode(addFoldDisplayModeListener(listener));
        }
    }

    public static void unregisterFoldDisplayMode(FoldDisplayModeListener listener) {
        if (listener != null) {
            HwFoldScreenManager.unregisterFoldDisplayMode(removeFoldDisplayModeListener(listener));
        }
    }

    private static IFoldableStateListener addFoldableStateListener(final FoldableStateListener listener) {
        IFoldableStateListener iFoldableStateListener;
        synchronized (sFoldableStateListenerMap) {
            if (!sFoldableStateListenerMap.containsKey(listener)) {
                sFoldableStateListenerMap.put(listener, new IFoldableStateListener.Stub() {
                    public void onStateChange(Bundle extra) {
                        FoldableStateListener.this.onStateChange(extra);
                    }

                    public IBinder asBinder() {
                        return this;
                    }
                });
            }
            iFoldableStateListener = sFoldableStateListenerMap.get(listener);
        }
        return iFoldableStateListener;
    }

    private static IFoldableStateListener removeFoldableStateListener(FoldableStateListener listener) {
        IFoldableStateListener remove;
        synchronized (sFoldableStateListenerMap) {
            remove = sFoldableStateListenerMap.remove(listener);
        }
        return remove;
    }

    private static IFoldDisplayModeListener addFoldDisplayModeListener(final FoldDisplayModeListener listener) {
        IFoldDisplayModeListener iFoldDisplayModeListener;
        synchronized (sFoldDisplayModeListenerMap) {
            if (!sFoldDisplayModeListenerMap.containsKey(listener)) {
                sFoldDisplayModeListenerMap.put(listener, new IFoldDisplayModeListener.Stub() {
                    public void onScreenDisplayModeChange(int displayMode) {
                        FoldDisplayModeListener.this.onScreenDisplayModeChange(displayMode);
                    }

                    public IBinder asBinder() {
                        return this;
                    }
                });
            }
            iFoldDisplayModeListener = sFoldDisplayModeListenerMap.get(listener);
        }
        return iFoldDisplayModeListener;
    }

    private static IFoldDisplayModeListener removeFoldDisplayModeListener(FoldDisplayModeListener listener) {
        IFoldDisplayModeListener remove;
        synchronized (sFoldDisplayModeListenerMap) {
            remove = sFoldDisplayModeListenerMap.remove(listener);
        }
        return remove;
    }
}
