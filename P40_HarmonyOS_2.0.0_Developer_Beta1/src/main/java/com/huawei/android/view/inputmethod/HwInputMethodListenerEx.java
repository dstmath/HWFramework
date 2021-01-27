package com.huawei.android.view.inputmethod;

import android.util.Log;
import android.view.inputmethod.CursorAnchorInfo;
import com.huawei.android.inputmethod.IHwInputMethodListener;
import java.lang.reflect.InvocationTargetException;

public class HwInputMethodListenerEx {
    private static final String TAG = HwInputMethodListenerEx.class.getSimpleName();
    private Object mInvokeListener;
    private IHwInputMethodListener mService = new IHwInputMethodListener.Stub() {
        /* class com.huawei.android.view.inputmethod.HwInputMethodListenerEx.AnonymousClass1 */

        public void onStartInput() {
            HwInputMethodListenerEx.this.onStartInput();
        }

        public void onFinishInput() {
            HwInputMethodListenerEx.this.onFinishInput();
        }

        public void onShowInputRequested() {
            HwInputMethodListenerEx.this.onShowInputRequested();
        }

        public void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) {
            HwInputMethodListenerEx.this.onUpdateCursorAnchorInfo(cursorAnchorInfo);
        }

        public void onContentChanged(String text) {
            HwInputMethodListenerEx.this.onContentChanged(text);
        }
    };

    public HwInputMethodListenerEx(Object invokeListener) {
        this.mInvokeListener = invokeListener;
    }

    public void onStartInput() {
        if (this.mInvokeListener != null) {
            invokeListenerCall("onStartInput", null, null);
        }
    }

    public void onFinishInput() {
        if (this.mInvokeListener != null) {
            invokeListenerCall("onFinishInput", null, null);
        }
    }

    public void onShowInputRequested() {
        if (this.mInvokeListener != null) {
            invokeListenerCall("onShowInputRequested", null, null);
        }
    }

    public void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) {
        if (this.mInvokeListener != null) {
            invokeListenerCall("onUpdateCursorAnchorInfo", new Class[]{CursorAnchorInfo.class}, new Object[]{cursorAnchorInfo});
        }
    }

    public void onContentChanged(String text) {
        if (this.mInvokeListener != null) {
            invokeListenerCall("onContentChanged", new Class[]{String.class}, new Object[]{text});
        }
    }

    public IHwInputMethodListener getInnerListener() {
        return this.mService;
    }

    private void invokeListenerCall(String methodName, Class[] classesArgs, Object[] objectsArgs) {
        Object obj = this.mInvokeListener;
        if (obj != null) {
            try {
                obj.getClass().getDeclaredMethod(methodName, classesArgs).invoke(this.mInvokeListener, objectsArgs);
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                Log.e("HwInputMethodListenerEx", "invokeListenerCall exception:" + e.toString());
            }
        }
    }
}
