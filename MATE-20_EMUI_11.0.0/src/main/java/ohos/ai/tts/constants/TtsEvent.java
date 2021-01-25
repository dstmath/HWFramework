package ohos.ai.tts.constants;

public class TtsEvent {
    public static final int CREATE_TTS_CLIENT_FAILED = 1;
    public static final int CREATE_TTS_CLIENT_SUCCESS = 0;
    public static final int DESTROY_TTS_CLIENT_FAILED = 3;
    public static final int DESTROY_TTS_CLIENT_SUCCESS = 2;
    public static final int METHOD_EXECUTE_FAILED_REMOTE_EXCEPTION = 101;
    public static final int METHOD_EXECUTE_SUCCESS = 100;

    private TtsEvent() {
    }
}
