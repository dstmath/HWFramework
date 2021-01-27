package ohos.ai.asr;

public class AsrIntent {
    private static final int DEFAULT_TIMEOUT_THRESHOLD_MS = 20000;
    private static final int DEFAULT_VAD_END_WAIT_MS = 2000;
    private static final int DEFAULT_VAD_FRONT_WAIT_MS = 4800;
    private int audioSourceType = AsrAudioSrcType.ASR_SRC_TYPE_FILE.getValue();
    private int engineType = AsrEngineType.ASR_ENGINE_TYPE_LOCAL.getValue();
    private String filePath;
    private int timeoutThresholdMs = DEFAULT_TIMEOUT_THRESHOLD_MS;
    private int vadEndWaitMs = 2000;
    private int vadFrontWaitMs = DEFAULT_VAD_FRONT_WAIT_MS;

    public int getVadEndWaitMs() {
        return this.vadEndWaitMs;
    }

    public void setVadEndWaitMs(int i) {
        this.vadEndWaitMs = i;
    }

    public int getVadFrontWaitMs() {
        return this.vadFrontWaitMs;
    }

    public void setVadFrontWaitMs(int i) {
        this.vadFrontWaitMs = i;
    }

    public int getTimeoutThresholdMs() {
        return this.timeoutThresholdMs;
    }

    public void setTimeoutThresholdMs(int i) {
        this.timeoutThresholdMs = i;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String str) {
        this.filePath = str;
    }

    public int getAudioSourceType() {
        return this.audioSourceType;
    }

    public void setAudioSourceType(AsrAudioSrcType asrAudioSrcType) {
        if (asrAudioSrcType != null) {
            this.audioSourceType = asrAudioSrcType.getValue();
        }
    }

    public int getEngineType() {
        return this.engineType;
    }

    public void setEngineType(AsrEngineType asrEngineType) {
        if (asrEngineType != null) {
            this.engineType = asrEngineType.getValue();
        }
    }

    public enum AsrAudioSrcType {
        ASR_SRC_TYPE_PCM(0),
        ASR_SRC_TYPE_FILE(2);
        
        private final int audioSrcType;

        private AsrAudioSrcType(int i) {
            this.audioSrcType = i;
        }

        public int getValue() {
            return this.audioSrcType;
        }
    }

    public enum AsrEngineType {
        ASR_ENGINE_TYPE_LOCAL(0);
        
        private final int engineType;

        private AsrEngineType(int i) {
            this.engineType = i;
        }

        public int getValue() {
            return this.engineType;
        }
    }
}
