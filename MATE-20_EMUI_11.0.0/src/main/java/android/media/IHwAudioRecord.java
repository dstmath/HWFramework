package android.media;

public interface IHwAudioRecord {
    boolean isAudioRecordAllowed();

    void sendStateChangedIntent(int i);

    void showDisableMicrophoneToast();
}
