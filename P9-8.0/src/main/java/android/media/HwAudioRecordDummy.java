package android.media;

import android.util.Log;

public class HwAudioRecordDummy implements IHwAudioRecord {
    private static final String TAG = "HwAudioRecordDummy";
    private static IHwAudioRecord mHwAudioRecoder = new HwAudioRecordDummy();

    private HwAudioRecordDummy() {
    }

    public static IHwAudioRecord getDefault() {
        return mHwAudioRecoder;
    }

    public void sendStateChangedIntent(int state) {
        Log.w(TAG, "dummy sendStateChangedIntent, state=" + state);
    }

    public boolean isAudioRecordAllowed() {
        Log.w(TAG, "dummy isAudioRecordAllowed ");
        return true;
    }

    public void checkRecordActive(int audio_source) {
        Log.w(TAG, "dummy checkRecordActive");
    }

    public void showDisableMicrophoneToast() {
        Log.w(TAG, "dummy showDisableMicrophoneToast");
    }
}
