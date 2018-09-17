package android.speech.tts;

abstract class PlaybackQueueItem implements Runnable {
    private final Object mCallerIdentity;
    private final UtteranceProgressDispatcher mDispatcher;

    public abstract void run();

    abstract void stop(int i);

    PlaybackQueueItem(UtteranceProgressDispatcher dispatcher, Object callerIdentity) {
        this.mDispatcher = dispatcher;
        this.mCallerIdentity = callerIdentity;
    }

    Object getCallerIdentity() {
        return this.mCallerIdentity;
    }

    protected UtteranceProgressDispatcher getDispatcher() {
        return this.mDispatcher;
    }
}
