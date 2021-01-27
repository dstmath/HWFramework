package ohos.tools;

public final class ByTrace {
    public static final long BYTRACE_TAG_APP = 4611686018427387904L;

    public static void updateTraceLabel() {
        C0000Bytrace.updateTraceLabel();
    }

    public static void startTrace(String str, float f) {
        C0000Bytrace.startTrace(BYTRACE_TAG_APP, str, f);
    }

    public static void startTrace(String str) {
        C0000Bytrace.startTrace(BYTRACE_TAG_APP, str, -1.0f);
    }

    public static void finishTrace(String str) {
        C0000Bytrace.finishTrace(BYTRACE_TAG_APP, str);
    }

    public static void countTrace(String str, int i) {
        C0000Bytrace.countTrace(BYTRACE_TAG_APP, str, i);
    }
}
