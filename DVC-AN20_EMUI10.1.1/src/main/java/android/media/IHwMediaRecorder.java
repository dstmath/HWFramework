package android.media;

public interface IHwMediaRecorder {
    void sendStateChangedIntent(int i);

    void showDisableMicrophoneToast();
}
