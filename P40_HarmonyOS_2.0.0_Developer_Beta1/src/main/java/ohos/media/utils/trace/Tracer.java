package ohos.media.utils.trace;

import ohos.media.utils.log.Logger;
import ohos.tools.C0000Bytrace;

public class Tracer {
    private final boolean isTracerOn;
    private Logger logger;
    private final long tag;

    public static class Audio {
    }

    public static class Camera {
        public static final int FIRST_FRAME = 100000;
    }

    public static class Image {
    }

    public static class Media {
    }

    Tracer(long j, boolean z) {
        this.tag = j;
        this.isTracerOn = z;
    }

    Tracer(long j, boolean z, Logger logger2) {
        this.tag = j;
        this.isTracerOn = z;
        this.logger = logger2;
    }

    public void startTrace(String str) {
        Logger logger2 = this.logger;
        if (logger2 != null) {
            logger2.debug("startTrace: %{public}s", str);
        }
        if (this.isTracerOn) {
            C0000Bytrace.startTrace(this.tag, str);
        }
    }

    public void startTrace(String str, float f) {
        Logger logger2 = this.logger;
        if (logger2 != null) {
            logger2.debug("startTrace: %{public}s, limit: %{public}f", str, Float.valueOf(f));
        }
        if (this.isTracerOn) {
            C0000Bytrace.startTrace(this.tag, str, f);
        }
    }

    public void startAsyncTrace(int i, String str) {
        Logger logger2 = this.logger;
        if (logger2 != null) {
            logger2.debug("startAsyncTrace: %{public}s, id: %{public}d", str, Integer.valueOf(i));
        }
        if (this.isTracerOn) {
            C0000Bytrace.startAsyncTrace(this.tag, str, i);
        }
    }

    public void startAsyncTrace(int i, String str, float f) {
        Logger logger2 = this.logger;
        if (logger2 != null) {
            logger2.debug("startAsyncTrace: %{public}s, id: %{public}d, limit: %{public}f", str, Integer.valueOf(i), Float.valueOf(f));
        }
        if (this.isTracerOn) {
            C0000Bytrace.startAsyncTrace(this.tag, str, i, f);
        }
    }

    public void finishTrace(String str) {
        if (this.isTracerOn) {
            C0000Bytrace.finishTrace(this.tag, str);
        }
        Logger logger2 = this.logger;
        if (logger2 != null) {
            logger2.debug("finishTrace: %{public}s", str);
        }
    }

    public void finishAsyncTrace(int i, String str) {
        if (this.isTracerOn) {
            C0000Bytrace.finishAsyncTrace(this.tag, str, i);
        }
        Logger logger2 = this.logger;
        if (logger2 != null) {
            logger2.debug("finishAsyncTrace: %{public}s, id: %{public}d", str, Integer.valueOf(i));
        }
    }

    public void countTrace(String str, int i) {
        if (this.isTracerOn) {
            C0000Bytrace.countTrace(this.tag, str, i);
        }
    }
}
