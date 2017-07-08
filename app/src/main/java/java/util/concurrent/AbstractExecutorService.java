package java.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractExecutorService implements ExecutorService {
    static final /* synthetic */ boolean -assertionsDisabled = false;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.AbstractExecutorService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.AbstractExecutorService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.AbstractExecutorService.<clinit>():void");
    }

    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new FutureTask(runnable, value);
    }

    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new FutureTask(callable);
    }

    public Future<?> submit(Runnable task) {
        if (task == null) {
            throw new NullPointerException();
        }
        RunnableFuture<Void> ftask = newTaskFor(task, null);
        execute(ftask);
        return ftask;
    }

    public <T> Future<T> submit(Runnable task, T result) {
        if (task == null) {
            throw new NullPointerException();
        }
        RunnableFuture<T> ftask = newTaskFor(task, result);
        execute(ftask);
        return ftask;
    }

    public <T> Future<T> submit(Callable<T> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        RunnableFuture<T> ftask = newTaskFor(task);
        execute(ftask);
        return ftask;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private <T> T doInvokeAny(Collection<? extends Callable<T>> tasks, boolean timed, long nanos) throws InterruptedException, ExecutionException, TimeoutException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        int ntasks = tasks.size();
        if (ntasks == 0) {
            throw new IllegalArgumentException();
        }
        long deadline;
        ArrayList<Future<T>> futures = new ArrayList(ntasks);
        ExecutorCompletionService<T> ecs = new ExecutorCompletionService(this);
        if (timed) {
            try {
                deadline = System.nanoTime() + nanos;
            } catch (Throwable th) {
                Throwable th2 = th;
            }
        } else {
            deadline = 0;
        }
        Iterator<? extends Callable<T>> it = tasks.iterator();
        futures.add(ecs.submit((Callable) it.next()));
        ntasks--;
        int active = 1;
        ExecutionException ee = null;
        while (true) {
            Future<T> f = ecs.poll();
            if (f == null) {
                if (ntasks > 0) {
                    ntasks--;
                    futures.add(ecs.submit((Callable) it.next()));
                    active++;
                } else if (active == 0) {
                    break;
                } else if (timed) {
                    f = ecs.poll(nanos, TimeUnit.NANOSECONDS);
                    if (f == null) {
                        break;
                    }
                    nanos = deadline - System.nanoTime();
                } else {
                    f = ecs.take();
                }
            }
            if (f != null) {
                active--;
                try {
                    T t = f.get();
                    cancelAll(futures);
                    return t;
                } catch (ExecutionException eex) {
                    ee = eex;
                } catch (Throwable rex) {
                    ee = new ExecutionException(rex);
                } catch (Throwable th3) {
                    th2 = th3;
                    ee = ee;
                }
            } else {
                ExecutionException ee2;
                ee2 = ee;
                ee = ee2;
            }
        }
        throw new TimeoutException();
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        try {
            return doInvokeAny(tasks, false, 0);
        } catch (TimeoutException e) {
            if (-assertionsDisabled) {
                return null;
            }
            throw new AssertionError();
        }
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return doInvokeAny(tasks, true, unit.toNanos(timeout));
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        ArrayList<Future<T>> futures = new ArrayList(tasks.size());
        try {
            for (Callable<T> t : tasks) {
                RunnableFuture<T> f = newTaskFor(t);
                futures.add(f);
                execute(f);
            }
            int size = futures.size();
            for (int i = 0; i < size; i++) {
                Future<T> f2 = (Future) futures.get(i);
                if (!f2.isDone()) {
                    try {
                        f2.get();
                    } catch (CancellationException e) {
                    } catch (ExecutionException e2) {
                    }
                }
            }
            return futures;
        } catch (Throwable th) {
            cancelAll(futures);
        }
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        long nanos = unit.toNanos(timeout);
        long deadline = System.nanoTime() + nanos;
        ArrayList<Future<T>> futures = new ArrayList(tasks.size());
        int j = 0;
        try {
            for (Callable<T> newTaskFor : tasks) {
                futures.add(newTaskFor(newTaskFor));
            }
            int size = futures.size();
            for (int i = 0; i < size; i++) {
                long j2;
                if (i == 0) {
                    j2 = nanos;
                } else {
                    j2 = deadline - System.nanoTime();
                }
                if (j2 <= 0) {
                    cancelAll(futures, j);
                    return futures;
                }
                execute((Runnable) futures.get(i));
            }
            while (j < size) {
                Future<T> f = (Future) futures.get(j);
                if (!f.isDone()) {
                    try {
                        f.get(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
                    } catch (CancellationException e) {
                    } catch (ExecutionException e2) {
                    } catch (TimeoutException e3) {
                    }
                }
                j++;
            }
            return futures;
        } catch (Throwable th) {
            cancelAll(futures);
        }
    }

    private static <T> void cancelAll(ArrayList<Future<T>> futures) {
        cancelAll(futures, 0);
    }

    private static <T> void cancelAll(ArrayList<Future<T>> futures, int j) {
        int size = futures.size();
        while (j < size) {
            ((Future) futures.get(j)).cancel(true);
            j++;
        }
    }
}
