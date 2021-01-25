package com.huawei.android.app;

import android.util.Log;
import com.huawei.android.view.IHwMultiDisplayPhoneOperateListener;
import java.lang.reflect.InvocationTargetException;

public class HwMultiDisplayPhoneOperateListenerEx {
    private static final String TAG = HwMultiDisplayPhoneOperateListenerEx.class.getSimpleName();
    private Object mInvokeListener;
    private IHwMultiDisplayPhoneOperateListener mService = new IHwMultiDisplayPhoneOperateListener.Stub() {
        /* class com.huawei.android.app.HwMultiDisplayPhoneOperateListenerEx.AnonymousClass1 */

        public void onOperateOnPhone() {
            HwMultiDisplayPhoneOperateListenerEx.this.onOperateOnPhone();
        }
    };

    public HwMultiDisplayPhoneOperateListenerEx(Object invokeListener) {
        this.mInvokeListener = invokeListener;
    }

    public void onOperateOnPhone() {
        if (this.mInvokeListener != null) {
            invokeListenerCall("onOperateOnPhone", null, null);
        }
    }

    public IHwMultiDisplayPhoneOperateListener getInnerListener() {
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
