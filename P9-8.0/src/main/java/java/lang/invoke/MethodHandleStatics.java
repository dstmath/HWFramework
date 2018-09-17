package java.lang.invoke;

import sun.misc.Unsafe;

class MethodHandleStatics {
    static final Unsafe UNSAFE = Unsafe.getUnsafe();

    private MethodHandleStatics() {
    }

    static InternalError newInternalError(String message) {
        return new InternalError(message);
    }

    static InternalError newInternalError(String message, Throwable cause) {
        return new InternalError(message, cause);
    }

    static InternalError newInternalError(Throwable cause) {
        return new InternalError(cause);
    }

    static RuntimeException newIllegalStateException(String message) {
        return new IllegalStateException(message);
    }

    static RuntimeException newIllegalStateException(String message, Object obj) {
        return new IllegalStateException(message(message, obj));
    }

    static RuntimeException newIllegalArgumentException(String message) {
        return new IllegalArgumentException(message);
    }

    static RuntimeException newIllegalArgumentException(String message, Object obj) {
        return new IllegalArgumentException(message(message, obj));
    }

    static RuntimeException newIllegalArgumentException(String message, Object obj, Object obj2) {
        return new IllegalArgumentException(message(message, obj, obj2));
    }

    static Error uncaughtException(Throwable ex) {
        if (ex instanceof Error) {
            throw ((Error) ex);
        } else if (ex instanceof RuntimeException) {
            throw ((RuntimeException) ex);
        } else {
            throw newInternalError("uncaught exception", ex);
        }
    }

    static Error NYI() {
        throw new AssertionError((Object) "NYI");
    }

    private static String message(String message, Object obj) {
        if (obj != null) {
            return message + ": " + obj;
        }
        return message;
    }

    private static String message(String message, Object obj, Object obj2) {
        if (obj == null && obj2 == null) {
            return message;
        }
        return message + ": " + obj + ", " + obj2;
    }
}
