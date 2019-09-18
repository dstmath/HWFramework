package com.android.internal.util;

import android.os.RemoteException;
import android.util.ExceptionUtils;
import android.util.Log;
import com.android.internal.util.FunctionalUtils;
import java.util.function.Consumer;

public class FunctionalUtils {
    private static final String TAG = "FunctionalUtils";

    @FunctionalInterface
    public interface RemoteExceptionIgnoringConsumer<T> extends Consumer<T> {
        void acceptOrThrow(T t) throws RemoteException;

        void accept(T t) {
            try {
                acceptOrThrow(t);
            } catch (RemoteException e) {
                Log.w(FunctionalUtils.TAG, "RemoteException: acceptOrThrow");
            }
        }
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> extends Consumer<T> {
        void acceptOrThrow(T t) throws Exception;

        void accept(T t) {
            try {
                acceptOrThrow(t);
            } catch (Exception ex) {
                throw ExceptionUtils.propagate(ex);
            }
        }
    }

    @FunctionalInterface
    public interface ThrowingRunnable extends Runnable {
        void runOrThrow() throws Exception;

        void run() {
            try {
                runOrThrow();
            } catch (Exception ex) {
                throw ExceptionUtils.propagate(ex);
            }
        }
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T getOrThrow() throws Exception;
    }

    private FunctionalUtils() {
    }

    public static <T> Consumer<T> uncheckExceptions(ThrowingConsumer<T> action) {
        return action;
    }

    public static <T> Consumer<T> ignoreRemoteException(RemoteExceptionIgnoringConsumer<T> action) {
        return action;
    }

    public static Runnable handleExceptions(ThrowingRunnable r, Consumer<Throwable> handler) {
        return new Runnable(handler) {
            private final /* synthetic */ Consumer f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                FunctionalUtils.lambda$handleExceptions$0(FunctionalUtils.ThrowingRunnable.this, this.f$1);
            }
        };
    }

    static /* synthetic */ void lambda$handleExceptions$0(ThrowingRunnable r, Consumer handler) {
        try {
            r.run();
        } catch (Throwable t) {
            handler.accept(t);
        }
    }
}
