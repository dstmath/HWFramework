package com.huawei.android.inputmethod;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Singleton;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputConnection;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.InputConnectionWrapper;
import com.huawei.android.inputmethod.IHwInputMethodManager;
import com.huawei.android.inputmethod.IHwSecureInputMethodManager;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwInputMethodManager {
    private static final Singleton<IInputMethodManager> IInputManagerSingleton = new Singleton<IInputMethodManager>() {
        /* class com.huawei.android.inputmethod.HwInputMethodManager.AnonymousClass2 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IInputMethodManager create() {
            return IInputMethodManager.Stub.asInterface(ServiceManager.getService(Context.INPUT_METHOD_SERVICE));
        }
    };
    private static final Singleton<IHwInputMethodManager> IInputMethodManagerSingleton = new Singleton<IHwInputMethodManager>() {
        /* class com.huawei.android.inputmethod.HwInputMethodManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwInputMethodManager create() {
            try {
                return IHwInputMethodManager.Stub.asInterface(HwInputMethodManager.getInputMethodManagerService().getHwInnerService());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    };
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static final Singleton<IInputMethodManager> ISecureInputManagerSingleton = new Singleton<IInputMethodManager>() {
        /* class com.huawei.android.inputmethod.HwInputMethodManager.AnonymousClass4 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IInputMethodManager create() {
            return IInputMethodManager.Stub.asInterface(ServiceManager.getService("input_method_secure"));
        }
    };
    private static final Singleton<IHwSecureInputMethodManager> ISecureInputMethodManagerSingleton = new Singleton<IHwSecureInputMethodManager>() {
        /* class com.huawei.android.inputmethod.HwInputMethodManager.AnonymousClass3 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwSecureInputMethodManager create() {
            try {
                return IHwSecureInputMethodManager.Stub.asInterface(HwInputMethodManager.getSecureInputMethodManagerService().getHwInnerService());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    };
    public static final String SECURITY_INPUT_METHOD_ID = "com.huawei.secime/.SoftKeyboard";
    public static final String SECURITY_INPUT_METHOD_SERVICE = "input_method_secure";
    private static final String TAG = "HwInputMethodManager";
    private static final boolean isSupportedSecIme = IS_CHINA_AREA;

    public static void setDefaultIme(String imeId) {
        try {
            getService().setDefaultIme(imeId);
        } catch (RemoteException e) {
            Log.e(TAG, "setDefaultIme failed: catch RemoteException!");
        }
    }

    public static void setInputSource(boolean isFingerTouch) {
        Log.i(TAG, "setInputSource isFingerTouch = " + isFingerTouch);
        try {
            getService().setInputSource(isFingerTouch);
        } catch (RemoteException e) {
            Log.e(TAG, "setInputSource failed: catch RemoteException!");
        }
    }

    public static void hideSecureInputMethod() {
        Log.i(TAG, "hideSecureInputMethod");
        if (isSupportedSecIme) {
            try {
                getSecureService().hideInputMethod();
            } catch (RemoteException e) {
                Log.e(TAG, "hideSecureInputMethod failed: catch RemoteException!");
            }
        }
    }

    public static void restartInputMethodForMultiDisplay() {
        try {
            getService().restartInputMethodForMultiDisplay();
        } catch (RemoteException e) {
            Log.e(TAG, "restartInputMethod failed: catch RemoteException!");
        }
    }

    public static void onStartInput() {
        try {
            getService().onStartInput();
        } catch (RemoteException e) {
            Log.e(TAG, "onStartInput failed: catch RemoteException!");
        }
    }

    public static void onFinishInput() {
        try {
            getService().onFinishInput();
        } catch (RemoteException e) {
            Log.e(TAG, "onFinishInput failed: catch RemoteException!");
        }
    }

    public static void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) {
        try {
            getService().onUpdateCursorAnchorInfo(cursorAnchorInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "onUpdateCursorAnchorInfo failed: catch RemoteException!");
        }
    }

    public static void registerInputContentListener(HwInputContentListenerEx listener) {
        if (listener == null) {
            Log.e(TAG, "registerInputMethodListener listener is null.");
            return;
        }
        try {
            getService().registerInputContentListener(listener.getInnerListener());
        } catch (RemoteException e) {
            Log.e(TAG, "registerInputContentListener failed: catch RemoteException!");
        }
    }

    public static void unregisterInputContentListener() {
        try {
            getService().unregisterInputContentListener();
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterInputContentListener failed: catch RemoteException!");
        }
    }

    public static void registerInputMethodListener(IHwInputMethodListener listener) {
        if (listener == null) {
            Log.e(TAG, "registerInputMethodListener listener is null.");
            return;
        }
        try {
            getService().registerInputMethodListener(listener);
        } catch (RemoteException e) {
            Log.e(TAG, "registerInputMethodListener failed: catch RemoteException!");
        }
    }

    public static void unregisterInputMethodListener() {
        try {
            getService().unregisterInputMethodListener();
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterInputMethodListener failed: catch RemoteException!");
        }
    }

    public static void onReceivedInputContent(String content) {
        StringBuilder sb = new StringBuilder();
        sb.append("onReceivedInputContent, length = ");
        sb.append(content == null ? "null" : Integer.valueOf(content.length()));
        Log.d(TAG, sb.toString());
        try {
            getService().onReceivedInputContent(content);
        } catch (RemoteException e) {
            Log.e(TAG, "registerInputContentListener failed: catch RemoteException!");
        }
    }

    public static void onShowInputRequested() {
        try {
            getService().onShowInputRequested();
        } catch (RemoteException e) {
            Log.e(TAG, "onShowInputRequested failed: catch RemoteException!");
        }
    }

    public static void onReceivedComposingText(String content) {
        StringBuilder sb = new StringBuilder();
        sb.append("onReceivedComposingText, length = ");
        sb.append(content == null ? "null" : Integer.valueOf(content.length()));
        Log.d(TAG, sb.toString());
        try {
            getService().onReceivedComposingText(content);
        } catch (RemoteException e) {
            Log.e(TAG, "registerInputContentListener failed: catch RemoteException!");
        }
    }

    public static void onContentChanged(String text) {
        try {
            getService().onContentChanged(text);
        } catch (RemoteException e) {
            Log.e(TAG, "onContentChanged failed: catch RemoteException!");
        }
    }

    public static InputConnection getCurInputConnection() {
        try {
            InputBinding binding = getService().getCurInputBinding();
            if (binding != null) {
                return new InputConnectionWrapper(IInputContext.Stub.asInterface(binding.getConnectionToken()), new AtomicBoolean());
            }
            Log.e(TAG, "getCurInputConnection failed: CurInputBinding is null.");
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "getCurInputConnection failed: catch RemoteException!");
            return null;
        }
    }

    public static EditorInfo getCurrentInputStyle() {
        try {
            return getService().getCurrentInputStyle();
        } catch (RemoteException e) {
            Log.e(TAG, "getCurrentInputStyle failed: catch RemoteException!");
            return null;
        }
    }

    private static IHwInputMethodManager getService() {
        return IInputMethodManagerSingleton.get();
    }

    /* access modifiers changed from: private */
    public static IInputMethodManager getInputMethodManagerService() {
        return IInputManagerSingleton.get();
    }

    private static IHwSecureInputMethodManager getSecureService() {
        return ISecureInputMethodManagerSingleton.get();
    }

    /* access modifiers changed from: private */
    public static IInputMethodManager getSecureInputMethodManagerService() {
        return ISecureInputManagerSingleton.get();
    }
}
