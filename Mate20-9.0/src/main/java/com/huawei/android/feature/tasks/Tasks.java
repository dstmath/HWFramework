package com.huawei.android.feature.tasks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Tasks {
    private Tasks() {
    }

    public static <TResult> TResult await(Task<TResult> task) {
        if (task == null) {
            throw new IllegalArgumentException("Task must not be null");
        } else if (task.isComplete()) {
            return getResult(task);
        } else {
            ap apVar = new ap((byte) 0);
            registerCountDownLatchManager(task, apVar);
            apVar.S.await();
            return getResult(task);
        }
    }

    public static <TResult> TResult await(Task<TResult> task, long j, TimeUnit timeUnit) {
        if (task == null) {
            throw new IllegalArgumentException("Task must not be null");
        } else if (timeUnit == null) {
            throw new IllegalArgumentException("TimeUnit must not be null");
        } else if (task.isComplete()) {
            return getResult(task);
        } else {
            ap apVar = new ap((byte) 0);
            registerCountDownLatchManager(task, apVar);
            if (apVar.S.await(j, timeUnit)) {
                return getResult(task);
            }
            throw new TimeoutException("Timed out waiting for Task");
        }
    }

    private static <TResult> TResult getResult(Task<TResult> task) {
        if (task.isSuccessful()) {
            return task.getResult();
        }
        throw new ExecutionException(task.getException());
    }

    public static <TResult> Task<TResult> makeTask(TResult tresult) {
        al alVar = new al();
        synchronized (alVar.E) {
            if (alVar.L) {
                throw new IllegalStateException("Task is already complete");
            }
            alVar.L = true;
            alVar.M = tresult;
        }
        alVar.K.b(alVar);
        return alVar;
    }

    private static void registerCountDownLatchManager(Task<?> task, ap apVar) {
        task.addOnSuccessListener(TaskExecutors.SExecutor, apVar);
        task.addOnFailureListener(TaskExecutors.SExecutor, apVar);
    }
}
