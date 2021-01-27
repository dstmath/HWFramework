package ohos.media.utils.trace;

import ohos.media.utils.log.Logger;
import ohos.system.Parameters;
import ohos.tools.C0000Bytrace;

public class TracerFactory {
    public static Tracer getCameraTracer() {
        return new Tracer(C0000Bytrace.BYTRACE_TAG_ZCAMERA, isTracerEnable());
    }

    public static Tracer getCameraTracer(Logger logger) {
        return new Tracer(C0000Bytrace.BYTRACE_TAG_ZCAMERA, isTracerEnable(), logger);
    }

    public static Tracer getAudioTracer() {
        return new Tracer(C0000Bytrace.BYTRACE_TAG_ZAUDIO, isTracerEnable());
    }

    public static Tracer getAudioTracer(Logger logger) {
        return new Tracer(C0000Bytrace.BYTRACE_TAG_ZAUDIO, isTracerEnable(), logger);
    }

    public static Tracer getImageTracer() {
        return new Tracer(C0000Bytrace.BYTRACE_TAG_ZIMAGE, isTracerEnable());
    }

    public static Tracer getImageTracer(Logger logger) {
        return new Tracer(C0000Bytrace.BYTRACE_TAG_ZIMAGE, isTracerEnable(), logger);
    }

    public static Tracer getMediaTracer() {
        return new Tracer(C0000Bytrace.BYTRACE_TAG_ZMEDIA, isTracerEnable());
    }

    public static Tracer getMediaTracer(Logger logger) {
        return new Tracer(C0000Bytrace.BYTRACE_TAG_ZMEDIA, isTracerEnable(), logger);
    }

    private static boolean isTracerEnable() {
        return Parameters.getBoolean("sys.multimedia.tracer.on", false);
    }
}
