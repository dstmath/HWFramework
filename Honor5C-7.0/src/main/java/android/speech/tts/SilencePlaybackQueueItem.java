package android.speech.tts;

import android.os.ConditionVariable;

class SilencePlaybackQueueItem extends PlaybackQueueItem {
    private final ConditionVariable mCondVar;
    private final long mSilenceDurationMs;

    SilencePlaybackQueueItem(UtteranceProgressDispatcher dispatcher, Object callerIdentity, long silenceDurationMs) {
        super(dispatcher, callerIdentity);
        this.mCondVar = new ConditionVariable();
        this.mSilenceDurationMs = silenceDurationMs;
    }

    public void run() {
        getDispatcher().dispatchOnStart();
        boolean wasStopped = false;
        if (this.mSilenceDurationMs > 0) {
            wasStopped = this.mCondVar.block(this.mSilenceDurationMs);
        }
        if (wasStopped) {
            getDispatcher().dispatchOnStop();
        } else {
            getDispatcher().dispatchOnSuccess();
        }
    }

    void stop(int errorCode) {
        this.mCondVar.open();
    }
}
