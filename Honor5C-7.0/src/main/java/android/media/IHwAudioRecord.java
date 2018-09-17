package android.media;

public interface IHwAudioRecord {
    void checkRecordActive();

    boolean isAudioRecordAllowed();

    void sendStateChangedIntent(int i);
}
