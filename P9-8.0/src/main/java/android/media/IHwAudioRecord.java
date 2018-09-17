package android.media;

public interface IHwAudioRecord {
    void checkRecordActive(int i);

    boolean isAudioRecordAllowed();

    void sendStateChangedIntent(int i);

    void showDisableMicrophoneToast();
}
