package android.speech.tts;

import android.speech.tts.TextToSpeechService;

abstract class PlaybackQueueItem implements Runnable {
    private final Object mCallerIdentity;
    private final TextToSpeechService.UtteranceProgressDispatcher mDispatcher;

    public abstract void run();

    /* access modifiers changed from: package-private */
    public abstract void stop(int i);

    PlaybackQueueItem(TextToSpeechService.UtteranceProgressDispatcher dispatcher, Object callerIdentity) {
        this.mDispatcher = dispatcher;
        this.mCallerIdentity = callerIdentity;
    }

    /* access modifiers changed from: package-private */
    public Object getCallerIdentity() {
        return this.mCallerIdentity;
    }

    /* access modifiers changed from: protected */
    public TextToSpeechService.UtteranceProgressDispatcher getDispatcher() {
        return this.mDispatcher;
    }
}
