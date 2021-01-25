package ohos.event.intentagent;

public final class IntentAgentConstant {
    static final int VALUE_NOT_NULL = 1;
    static final int VALUE_NULL = -1;

    public enum Flags {
        ONE_TIME_FLAG,
        NO_BUILD_FLAG,
        CANCEL_PRESENT_FLAG,
        UPDATE_PRESENT_FLAG,
        CONSTANT_FLAG,
        REPLACE_ELEMENT,
        REPLACE_ACTION,
        REPLACE_URI,
        REPLACE_ENTITIES,
        REPLACE_BUNDLE
    }

    public enum OperationType {
        UNKNOWN_TYPE,
        START_ABILITY,
        START_ABILITIES,
        START_SERVICE,
        SEND_COMMON_EVENT,
        START_FOREGROUND_SERVICE
    }

    private IntentAgentConstant() {
    }
}
