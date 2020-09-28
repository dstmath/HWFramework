package android.media;

class AudioRecordRoutingProxy extends AudioRecord {
    public AudioRecordRoutingProxy(long nativeRecordInJavaObj) {
        super(nativeRecordInJavaObj);
    }
}
