package android.speech.tts;

import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;

public abstract class UtteranceProgressListener {

    /* renamed from: android.speech.tts.UtteranceProgressListener.1 */
    static class AnonymousClass1 extends UtteranceProgressListener {
        final /* synthetic */ OnUtteranceCompletedListener val$listener;

        AnonymousClass1(OnUtteranceCompletedListener val$listener) {
            this.val$listener = val$listener;
        }

        public synchronized void onDone(String utteranceId) {
            this.val$listener.onUtteranceCompleted(utteranceId);
        }

        public void onError(String utteranceId) {
            this.val$listener.onUtteranceCompleted(utteranceId);
        }

        public void onStart(String utteranceId) {
        }

        public void onStop(String utteranceId, boolean interrupted) {
            this.val$listener.onUtteranceCompleted(utteranceId);
        }
    }

    public abstract void onDone(String str);

    @Deprecated
    public abstract void onError(String str);

    public abstract void onStart(String str);

    public void onError(String utteranceId, int errorCode) {
        onError(utteranceId);
    }

    public void onStop(String utteranceId, boolean interrupted) {
    }

    public void onBeginSynthesis(String utteranceId, int sampleRateInHz, int audioFormat, int channelCount) {
    }

    public void onAudioAvailable(String utteranceId, byte[] audio) {
    }

    static UtteranceProgressListener from(OnUtteranceCompletedListener listener) {
        return new AnonymousClass1(listener);
    }
}
