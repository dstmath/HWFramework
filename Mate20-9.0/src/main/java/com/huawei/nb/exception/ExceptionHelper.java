package com.huawei.nb.exception;

import com.huawei.nb.utils.logger.DSLog;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public final class ExceptionHelper {
    public static final Throwable TERMINATED = new Termination();

    static final class Termination extends Throwable {
        private static final long serialVersionUID = -4649703670690200604L;

        Termination() {
            super("No further exceptions");
        }

        public Throwable fillInStackTrace() {
            return this;
        }
    }

    private ExceptionHelper() {
        throw new IllegalStateException("No instances!");
    }

    public static void logAndThrowException(String message, Exception exception, Object... args) throws Exception {
        if (!(args == null || args.length == 0)) {
            message = String.format(Locale.US, message, args);
        }
        DSLog.e(message, new Object[0]);
        throw exception;
    }

    public static RuntimeException wrapOrThrow(Throwable error) {
        if (error instanceof Error) {
            throw ((Error) error);
        } else if (error instanceof RuntimeException) {
            return (RuntimeException) error;
        } else {
            return new RuntimeException(error);
        }
    }

    public static <T> boolean addThrowable(AtomicReference<Throwable> field, Throwable exception) {
        Throwable current;
        Throwable update;
        do {
            current = field.get();
            if (current == TERMINATED) {
                return false;
            }
            if (current == null) {
                update = exception;
            } else {
                update = new NBException();
            }
        } while (!field.compareAndSet(current, update));
        return true;
    }

    public static <T> Throwable terminate(AtomicReference<Throwable> field) {
        Throwable current = field.get();
        if (current != TERMINATED) {
            return field.getAndSet(TERMINATED);
        }
        return current;
    }

    public static List<Throwable> flatten(Throwable t) {
        List<Throwable> list = new ArrayList<>();
        ArrayDeque<Throwable> deque = new ArrayDeque<>();
        deque.offer(t);
        while (!deque.isEmpty()) {
            list.add(deque.removeFirst());
        }
        return list;
    }

    public static void throwIfFatal(Throwable t) {
        if (t instanceof VirtualMachineError) {
            throw ((VirtualMachineError) t);
        } else if (t instanceof ThreadDeath) {
            throw ((ThreadDeath) t);
        } else if (t instanceof LinkageError) {
            throw ((LinkageError) t);
        }
    }
}
