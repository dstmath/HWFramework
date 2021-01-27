package com.huawei.android.media;

import com.huawei.android.media.IDeviceSelectCallback;

@Deprecated
public abstract class DeviceSelectCallback {
    public static final int OUTPUT_A2DP = 2;
    public static final int OUTPUT_HDMI = 4;
    public static final int OUTPUT_NONE = 0;
    public static final int OUTPUT_PRIMARY = 1;
    public static final int OUTPUT_PROXY = 6;
    public static final int OUTPUT_REMOTE_SUBMIX = 5;
    public static final int OUTPUT_USB = 3;
    private IDeviceSelectCallback mDeviceSelectCallback = new IDeviceSelectCallback.Stub() {
        /* class com.huawei.android.media.DeviceSelectCallback.AnonymousClass1 */

        @Override // com.huawei.android.media.IDeviceSelectCallback
        public int selectDevice(int pid, int uid, int content, int usage, int sessionId) {
            return DeviceSelectCallback.this.onSelectDevice(pid, uid, content, usage, sessionId);
        }
    };

    public abstract int onSelectDevice(int i, int i2, int i3, int i4, int i5);

    public final IDeviceSelectCallback getDeviceSelectCb() {
        return this.mDeviceSelectCallback;
    }
}
