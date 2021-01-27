package android.media;

import android.util.Log;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DefaultHwMediaRecorder implements IHwMediaRecorder {
    private static final String TAG = "DefaultHwMediaRecorder";
    private static DefaultHwMediaRecorder mHwMediaRecoder = new DefaultHwMediaRecorder();

    public static DefaultHwMediaRecorder getDefault() {
        if (mHwMediaRecoder == null) {
            mHwMediaRecoder = new DefaultHwMediaRecorder();
        }
        return mHwMediaRecoder;
    }

    @Override // android.media.IHwMediaRecorder
    public void sendStateChangedIntent(int state) {
        Log.w(TAG, "dummy sendStateChangedIntent, state=" + state);
    }

    @Override // android.media.IHwMediaRecorder
    public void showDisableMicrophoneToast() {
        Log.w(TAG, "dummy showDisableMicrophoneToast");
    }
}
