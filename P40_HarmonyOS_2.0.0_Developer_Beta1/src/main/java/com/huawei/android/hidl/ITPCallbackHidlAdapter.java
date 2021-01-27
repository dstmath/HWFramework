package com.huawei.android.hidl;

import vendor.huawei.hardware.tp.V1_0.ITPCallback;

public class ITPCallbackHidlAdapter {
    private ITPCallback mITPCallback = new ITPCallback.Stub() {
        /* class com.huawei.android.hidl.ITPCallbackHidlAdapter.AnonymousClass1 */

        @Override // vendor.huawei.hardware.tp.V1_0.ITPCallback
        public void notifyTHPEvents(int event, int retval) {
            ITPCallbackHidlAdapter.this.notifyTHPEvents(event, retval);
        }

        @Override // vendor.huawei.hardware.tp.V1_0.ITPCallback
        public void notifyTPEvents(int eventClass, int eventCode, String extraInfo) {
            ITPCallbackHidlAdapter.this.notifyTPEvents(eventClass, eventCode, extraInfo);
        }
    };

    public ITPCallback getITPCallback() {
        return this.mITPCallback;
    }

    public void notifyTHPEvents(int event, int retval) {
    }

    public void notifyTPEvents(int eventClass, int eventCode, String extraInfo) {
    }
}
