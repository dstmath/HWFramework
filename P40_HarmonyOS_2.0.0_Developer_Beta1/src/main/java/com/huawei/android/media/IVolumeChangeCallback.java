package com.huawei.android.media;

public abstract class IVolumeChangeCallback {
    private static final String TAG = "IVolumeChangeCallback";
    private VolumeChangeCallback mCb = new VolumeChangeCallback() {
        /* class com.huawei.android.media.IVolumeChangeCallback.AnonymousClass1 */

        public void onVolumeChange(int device, int stream, String caller, int volume) {
            IVolumeChangeCallback.this.onVolumeChange(device, stream, caller, volume);
        }
    };

    public abstract void onVolumeChange(int i, int i2, String str, int i3);

    public VolumeChangeCallback getVolumeChangeCallback() {
        return this.mCb;
    }
}
