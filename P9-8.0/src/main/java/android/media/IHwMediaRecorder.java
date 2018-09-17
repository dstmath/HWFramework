package android.media;

public interface IHwMediaRecorder {
    void checkRecordActive(int i);

    void sendStateChangedIntent(int i);

    void showDisableMicrophoneToast();
}
