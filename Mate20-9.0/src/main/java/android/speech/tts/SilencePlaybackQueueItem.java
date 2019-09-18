package android.speech.tts;

import android.os.ConditionVariable;
import android.speech.tts.TextToSpeechService;

class SilencePlaybackQueueItem extends PlaybackQueueItem {
    private final ConditionVariable mCondVar = new ConditionVariable();
    private final long mSilenceDurationMs;

    SilencePlaybackQueueItem(TextToSpeechService.UtteranceProgressDispatcher dispatcher, Object callerIdentity, long silenceDurationMs) {
        super(dispatcher, callerIdentity);
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

    /* access modifiers changed from: package-private */
    public void stop(int errorCode) {
        this.mCondVar.open();
    }
}
