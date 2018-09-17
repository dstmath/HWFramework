package android.speech.tts;

import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;

public abstract class UtteranceProgressListener {
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

    public void onRangeStart(String utteranceId, int start, int end, int frame) {
        onUtteranceRangeStart(utteranceId, start, end);
    }

    @Deprecated
    public void onUtteranceRangeStart(String utteranceId, int start, int end) {
    }

    static UtteranceProgressListener from(final OnUtteranceCompletedListener listener) {
        return new UtteranceProgressListener() {
            public synchronized void onDone(String utteranceId) {
                listener.onUtteranceCompleted(utteranceId);
            }

            public void onError(String utteranceId) {
                listener.onUtteranceCompleted(utteranceId);
            }

            public void onStart(String utteranceId) {
            }

            public void onStop(String utteranceId, boolean interrupted) {
                listener.onUtteranceCompleted(utteranceId);
            }
        };
    }
}
