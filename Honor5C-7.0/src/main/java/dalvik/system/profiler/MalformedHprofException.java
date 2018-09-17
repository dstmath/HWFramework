package dalvik.system.profiler;

import java.io.IOException;

public final class MalformedHprofException extends IOException {
    private static final long serialVersionUID = 8558990237047894213L;

    MalformedHprofException(String message) {
        super(message);
    }

    MalformedHprofException(String message, Throwable cause) {
        super(message, cause);
    }

    MalformedHprofException(Throwable cause) {
        super(cause);
    }
}
