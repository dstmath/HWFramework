package android.speech.tts;

import android.util.EventLog;

public class EventLogTags {
    public static final int TTS_SPEAK_FAILURE = 76002;
    public static final int TTS_SPEAK_SUCCESS = 76001;
    public static final int TTS_V2_SPEAK_FAILURE = 76004;
    public static final int TTS_V2_SPEAK_SUCCESS = 76003;

    private EventLogTags() {
    }

    public static void writeTtsSpeakSuccess(String engine, int callerUid, int callerPid, int length, String locale, int rate, int pitch, long engineLatency, long engineTotal, long audioLatency) {
        EventLog.writeEvent(TTS_SPEAK_SUCCESS, new Object[]{engine, Integer.valueOf(callerUid), Integer.valueOf(callerPid), Integer.valueOf(length), locale, Integer.valueOf(rate), Integer.valueOf(pitch), Long.valueOf(engineLatency), Long.valueOf(engineTotal), Long.valueOf(audioLatency)});
    }

    public static void writeTtsSpeakFailure(String engine, int callerUid, int callerPid, int length, String locale, int rate, int pitch) {
        EventLog.writeEvent(TTS_SPEAK_FAILURE, new Object[]{engine, Integer.valueOf(callerUid), Integer.valueOf(callerPid), Integer.valueOf(length), locale, Integer.valueOf(rate), Integer.valueOf(pitch)});
    }

    public static void writeTtsV2SpeakSuccess(String engine, int callerUid, int callerPid, int length, String requestConfig, long engineLatency, long engineTotal, long audioLatency) {
        EventLog.writeEvent(TTS_V2_SPEAK_SUCCESS, new Object[]{engine, Integer.valueOf(callerUid), Integer.valueOf(callerPid), Integer.valueOf(length), requestConfig, Long.valueOf(engineLatency), Long.valueOf(engineTotal), Long.valueOf(audioLatency)});
    }

    public static void writeTtsV2SpeakFailure(String engine, int callerUid, int callerPid, int length, String requestConfig, int statuscode) {
        EventLog.writeEvent(TTS_V2_SPEAK_FAILURE, new Object[]{engine, Integer.valueOf(callerUid), Integer.valueOf(callerPid), Integer.valueOf(length), requestConfig, Integer.valueOf(statuscode)});
    }
}
