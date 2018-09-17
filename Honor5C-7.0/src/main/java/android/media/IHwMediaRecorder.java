package android.media;

public interface IHwMediaRecorder {
    void checkRecordActive();

    void sendStateChangedIntent(int i);
}
