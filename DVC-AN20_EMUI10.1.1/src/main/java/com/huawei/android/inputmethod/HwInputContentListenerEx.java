package com.huawei.android.inputmethod;

import com.huawei.android.inputmethod.IHwInputContentListener;

public class HwInputContentListenerEx {
    private IHwInputContentListener mService = new IHwInputContentListener.Stub() {
        /* class com.huawei.android.inputmethod.HwInputContentListenerEx.AnonymousClass1 */

        @Override // com.huawei.android.inputmethod.IHwInputContentListener
        public void onReceivedInputContent(String content) {
            HwInputContentListenerEx.this.onReceivedInputContent(content);
        }

        @Override // com.huawei.android.inputmethod.IHwInputContentListener
        public void onReceivedComposingText(String content) {
            HwInputContentListenerEx.this.onReceivedComposingText(content);
        }
    };

    public void onReceivedInputContent(String content) {
    }

    public void onReceivedComposingText(String content) {
    }

    public IHwInputContentListener getInnerListener() {
        return this.mService;
    }
}
