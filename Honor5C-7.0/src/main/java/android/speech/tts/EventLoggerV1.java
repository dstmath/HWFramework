package android.speech.tts;

import android.text.TextUtils;

class EventLoggerV1 extends AbstractEventLogger {
    private final SynthesisRequest mRequest;

    EventLoggerV1(SynthesisRequest request, int callerUid, int callerPid, String serviceApp) {
        super(callerUid, callerPid, serviceApp);
        this.mRequest = request;
    }

    protected void logFailure(int statusCode) {
        if (statusCode != -2) {
            EventLogTags.writeTtsSpeakFailure(this.mServiceApp, this.mCallerUid, this.mCallerPid, getUtteranceLength(), getLocaleString(), this.mRequest.getSpeechRate(), this.mRequest.getPitch());
        }
    }

    protected void logSuccess(long audioLatency, long engineLatency, long engineTotal) {
        EventLogTags.writeTtsSpeakSuccess(this.mServiceApp, this.mCallerUid, this.mCallerPid, getUtteranceLength(), getLocaleString(), this.mRequest.getSpeechRate(), this.mRequest.getPitch(), engineLatency, engineTotal, audioLatency);
    }

    private int getUtteranceLength() {
        String utterance = this.mRequest.getText();
        return utterance == null ? 0 : utterance.length();
    }

    private String getLocaleString() {
        StringBuilder sb = new StringBuilder(this.mRequest.getLanguage());
        if (!TextUtils.isEmpty(this.mRequest.getCountry())) {
            sb.append('-');
            sb.append(this.mRequest.getCountry());
            if (!TextUtils.isEmpty(this.mRequest.getVariant())) {
                sb.append('-');
                sb.append(this.mRequest.getVariant());
            }
        }
        return sb.toString();
    }
}
