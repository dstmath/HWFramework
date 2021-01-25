package ohos.ai.asr.taskhandler;

public class AsrCallbackTaskId {
    public static final int TASK_ON_BEGINNING_OF_SPEECH = 3;
    public static final int TASK_ON_BUFFER_RECEIVED = 5;
    public static final int TASK_ON_END = 11;
    public static final int TASK_ON_END_OF_SPEECH = 6;
    public static final int TASK_ON_ERROR = 8;
    public static final int TASK_ON_EVENT = 12;
    public static final int TASK_ON_INIT = 1;
    public static final int TASK_ON_INTERMEDIATE_RESULTS = 10;
    public static final int TASK_ON_LEXICON_UPDATE = 13;
    public static final int TASK_ON_RECORD_END = 7;
    public static final int TASK_ON_RECORD_START = 2;
    public static final int TASK_ON_RESULTS = 9;
    public static final int TASK_ON_RMS_CHANGED = 4;

    private AsrCallbackTaskId() {
    }
}
