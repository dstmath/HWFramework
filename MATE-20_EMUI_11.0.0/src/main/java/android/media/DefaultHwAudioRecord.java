package android.media;

import android.util.Log;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DefaultHwAudioRecord implements IHwAudioRecord {
    private static final String TAG = "DefaultHwAudioRecord";
    private static DefaultHwAudioRecord mHwAudioRecoder = new DefaultHwAudioRecord();

    public static DefaultHwAudioRecord getDefault() {
        return mHwAudioRecoder;
    }

    @Override // android.media.IHwAudioRecord
    public void sendStateChangedIntent(int state) {
        Log.w(TAG, "dummy sendStateChangedIntent, state=" + state);
    }

    @Override // android.media.IHwAudioRecord
    public boolean isAudioRecordAllowed() {
        Log.w(TAG, "dummy isAudioRecordAllowed ");
        return true;
    }

    @Override // android.media.IHwAudioRecord
    public void showDisableMicrophoneToast() {
        Log.w(TAG, "dummy showDisableMicrophoneToast");
    }
}
