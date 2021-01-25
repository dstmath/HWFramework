package com.huawei.android.fsm;

import android.hardware.display.HwFoldScreenState;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Slog;
import com.huawei.android.fsm.IFoldDisplayModeListener;
import com.huawei.android.fsm.IFoldFsmTipsRequestListener;
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
    public static final String KEY_PRIORITY_FOLD_TIPS = "KEY_PRIORITY_FOLD_TIPS";
    public static final String KEY_TIPS_INT_DISPLAY_MODE = "KEY_TIPS_INT_DISPLAY_MODE";
    public static final String KEY_TIPS_INT_REMOVED_REASON = "KEY_TIPS_INT_REMOVED_REASON";
    public static final String KEY_TIPS_INT_VIEW_TYPE = "KEY_TIPS_INT_VIEW_TYPE";
    public static final String KEY_TIPS_STR_CALLER_NAME = "KEY_TIPS_STR_CALLER_NAME";
    public static final String KEY_TIPS_STR_CAMERA_ID = "KEY_TIPS_STR_CAMERA_ID";
    public static final String KEY_TIPS_TEXT = "KEY_TIPS_TEXT";
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
    public static final int PRIORITY_FOLD_TIPS_AUDIO = 7;
    public static final int PRIORITY_FOLD_TIPS_CALL = 9;
    public static final int PRIORITY_FOLD_TIPS_FRONT_CAMERA = 10;
    public static final int PRIORITY_FOLD_TIPS_OTHER_CALL = 8;
    public static final int PRIORITY_FOLD_TIPS_UNKNOW = 0;
    public static final int REASON_REMOVE_TIPS_FOR_APP_REQUEST = 2;
    public static final int REASON_REMOVE_TIPS_FOR_DISPLAYMODE_CHANGE = 1;
    public static final int REASON_REMOVE_TIPS_FOR_EXCEPTION = 0;
    public static final int REASON_REMOVE_TIPS_FOR_USER_CLOSE = 3;
    public static final int REQ_BROADCAST_TIPS_REMOVED = 4;
    public static final int REQ_REMOVE_TIPS = 1;
    public static final int REQ_SHOW_TIPS = 2;
    public static final int REQ_TIPS_TYPE_UNKNOWN = 0;
    private static final String TAG = "HwFoldScreenManagerEx";
    public static final int TIPS_ID_FOR_CALL = 1;
    public static final int TIPS_ID_FOR_CAMERA = 0;
    public static final int TIPS_ID_FOR_GENERAL = 2;
    public static final int TIPS_TYPE_ANIMATION_FLIP = 1;
    public static final int TIPS_TYPE_DIALOG_FOLD = 0;
    public static final int TIPS_TYPE_DIALOG_INTELLIGENT_FLIP = 2;
    private static Map<FoldDisplayModeListener, IFoldDisplayModeListener> sFoldDisplayModeListenerMap = new HashMap();
    private static Map<FoldFsmTipsRequestListener, IFoldFsmTipsRequestListener> sFoldFsmTipsRequestListenerMap = new HashMap();
    private static Map<FoldableStateListener, IFoldableStateListener> sFoldableStateListenerMap = new HashMap();

    public interface FoldDisplayModeListener {
        void onScreenDisplayModeChange(int i);
    }

    public interface FoldFsmTipsRequestListener {
        void onRequestFsmTips(int i, Bundle bundle);
    }

    public interface FoldableStateListener {
        void onStateChange(Bundle bundle);
    }

    public static boolean isFoldable() {
        return HwFoldScreenManager.isFoldable();
    }

    public static boolean isInwardFoldDevice() {
        return HwFoldScreenState.isInwardFoldDevice();
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
                    /* class com.huawei.android.fsm.HwFoldScreenManagerEx.AnonymousClass1 */

                    @Override // com.huawei.android.fsm.IFoldableStateListener
                    public void onStateChange(Bundle extra) {
                        try {
                            FoldableStateListener.this.onStateChange(extra);
                        } catch (AbstractMethodError e) {
                            Slog.i(HwFoldScreenManagerEx.TAG, "This listener just does not implement: " + FoldableStateListener.this);
                        }
                    }

                    @Override // com.huawei.android.fsm.IFoldableStateListener.Stub, android.os.IInterface
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
                    /* class com.huawei.android.fsm.HwFoldScreenManagerEx.AnonymousClass2 */

                    @Override // com.huawei.android.fsm.IFoldDisplayModeListener
                    public void onScreenDisplayModeChange(int displayMode) {
                        try {
                            FoldDisplayModeListener.this.onScreenDisplayModeChange(displayMode);
                        } catch (AbstractMethodError e) {
                            Slog.i(HwFoldScreenManagerEx.TAG, "This listener just does not implement: " + FoldDisplayModeListener.this);
                        }
                    }

                    @Override // com.huawei.android.fsm.IFoldDisplayModeListener.Stub, android.os.IInterface
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

    public static int reqShowTipsToFsm(int reqTipsType, Bundle data) {
        return HwFoldScreenManager.reqShowTipsToFsm(reqTipsType, data);
    }

    public static void registerFsmTipsRequestListener(FoldFsmTipsRequestListener listener, int type) {
        if (listener != null) {
            HwFoldScreenManager.registerFsmTipsRequestListener(addFoldFsmTipsRequestListener(listener), type);
        }
    }

    public static void unregisterFsmTipsRequestListener(FoldFsmTipsRequestListener listener) {
        if (listener != null) {
            HwFoldScreenManager.unregisterFsmTipsRequestListener(removeFsmTipsRequestListener(listener));
        }
    }

    private static IFoldFsmTipsRequestListener addFoldFsmTipsRequestListener(final FoldFsmTipsRequestListener listener) {
        IFoldFsmTipsRequestListener iFoldFsmTipsRequestListener;
        synchronized (sFoldFsmTipsRequestListenerMap) {
            if (!sFoldFsmTipsRequestListenerMap.containsKey(listener)) {
                sFoldFsmTipsRequestListenerMap.put(listener, new IFoldFsmTipsRequestListener.Stub() {
                    /* class com.huawei.android.fsm.HwFoldScreenManagerEx.AnonymousClass3 */

                    @Override // com.huawei.android.fsm.IFoldFsmTipsRequestListener
                    public void onRequestFsmTips(int reqTipsType, Bundle data) {
                        try {
                            FoldFsmTipsRequestListener.this.onRequestFsmTips(reqTipsType, data);
                        } catch (AbstractMethodError e) {
                            Slog.i(HwFoldScreenManagerEx.TAG, "This listener just does not implement: " + FoldFsmTipsRequestListener.this);
                        }
                    }

                    @Override // com.huawei.android.fsm.IFoldFsmTipsRequestListener.Stub, android.os.IInterface
                    public IBinder asBinder() {
                        return this;
                    }
                });
            }
            iFoldFsmTipsRequestListener = sFoldFsmTipsRequestListenerMap.get(listener);
        }
        return iFoldFsmTipsRequestListener;
    }

    private static IFoldFsmTipsRequestListener removeFsmTipsRequestListener(FoldFsmTipsRequestListener listener) {
        IFoldFsmTipsRequestListener remove;
        synchronized (sFoldFsmTipsRequestListenerMap) {
            remove = sFoldFsmTipsRequestListenerMap.remove(listener);
        }
        return remove;
    }
}
