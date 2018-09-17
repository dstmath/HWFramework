package android.os;

import android.util.Log;
import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AsyncTask<Params, Progress, Result> {
    private static final /* synthetic */ int[] -android-os-AsyncTask$StatusSwitchesValues = null;
    private static final int CORE_POOL_SIZE = 0;
    private static final int CPU_COUNT = 0;
    private static final boolean IS_ANTUTU = false;
    private static final boolean IS_EMU = false;
    private static final boolean IS_FPGA = false;
    private static final int KEEP_ALIVE_SECONDS = 30;
    private static final String LOG_TAG = "AsyncTask";
    private static final int MAXIMUM_POOL_SIZE = 0;
    private static final int MESSAGE_POST_PROGRESS = 2;
    private static final int MESSAGE_POST_RESULT = 1;
    public static final Executor SERIAL_EXECUTOR = null;
    public static final Executor THREAD_POOL_EXECUTOR = null;
    private static String boardname;
    private static volatile Executor sDefaultExecutor;
    private static InternalHandler sHandler;
    private static final BlockingQueue<Runnable> sPoolWorkQueue = null;
    private static final ThreadFactory sThreadFactory = null;
    private final AtomicBoolean mCancelled;
    private final FutureTask<Result> mFuture;
    private volatile Status mStatus;
    private final AtomicBoolean mTaskInvoked;
    private final WorkerRunnable<Params, Result> mWorker;

    private static abstract class WorkerRunnable<Params, Result> implements Callable<Result> {
        Params[] mParams;

        private WorkerRunnable() {
        }
    }

    /* renamed from: android.os.AsyncTask.3 */
    class AnonymousClass3 extends FutureTask<Result> {
        AnonymousClass3(Callable $anonymous0) {
            super($anonymous0);
        }

        protected void done() {
            try {
                AsyncTask.this.postResultIfNotInvoked(get());
            } catch (InterruptedException e) {
                Log.w(AsyncTask.LOG_TAG, e);
            } catch (ExecutionException e2) {
                throw new RuntimeException("An error occurred while executing doInBackground()", e2.getCause());
            } catch (CancellationException e3) {
                AsyncTask.this.postResultIfNotInvoked(null);
            }
        }
    }

    private static class AsyncTaskResult<Data> {
        final Data[] mData;
        final AsyncTask mTask;

        AsyncTaskResult(AsyncTask task, Data... data) {
            this.mTask = task;
            this.mData = data;
        }
    }

    private static class InternalHandler extends Handler {
        public InternalHandler() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message msg) {
            AsyncTaskResult<?> result = msg.obj;
            switch (msg.what) {
                case AsyncTask.MESSAGE_POST_RESULT /*1*/:
                    result.mTask.finish(result.mData[AsyncTask.MAXIMUM_POOL_SIZE]);
                case AsyncTask.MESSAGE_POST_PROGRESS /*2*/:
                    result.mTask.onProgressUpdate(result.mData);
                default:
            }
        }
    }

    private static class SerialExecutor implements Executor {
        Runnable mActive;
        final ArrayDeque<Runnable> mTasks;

        /* renamed from: android.os.AsyncTask.SerialExecutor.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ Runnable val$r;

            AnonymousClass1(Runnable val$r) {
                this.val$r = val$r;
            }

            public void run() {
                try {
                    this.val$r.run();
                } finally {
                    SerialExecutor.this.scheduleNext();
                }
            }
        }

        private SerialExecutor() {
            this.mTasks = new ArrayDeque();
        }

        public synchronized void execute(Runnable r) {
            this.mTasks.offer(new AnonymousClass1(r));
            if (this.mActive == null) {
                scheduleNext();
            }
        }

        protected synchronized void scheduleNext() {
            Runnable runnable = (Runnable) this.mTasks.poll();
            this.mActive = runnable;
            if (runnable != null) {
                AsyncTask.THREAD_POOL_EXECUTOR.execute(this.mActive);
            }
        }
    }

    public enum Status {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.AsyncTask.Status.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.AsyncTask.Status.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.AsyncTask.Status.<clinit>():void");
        }
    }

    private static /* synthetic */ int[] -getandroid-os-AsyncTask$StatusSwitchesValues() {
        if (-android-os-AsyncTask$StatusSwitchesValues != null) {
            return -android-os-AsyncTask$StatusSwitchesValues;
        }
        int[] iArr = new int[Status.values().length];
        try {
            iArr[Status.FINISHED.ordinal()] = MESSAGE_POST_RESULT;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Status.PENDING.ordinal()] = 3;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Status.RUNNING.ordinal()] = MESSAGE_POST_PROGRESS;
        } catch (NoSuchFieldError e3) {
        }
        -android-os-AsyncTask$StatusSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.AsyncTask.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.AsyncTask.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.AsyncTask.<clinit>():void");
    }

    protected abstract Result doInBackground(Params... paramsArr);

    private static final String getProNameForPid(int pid) {
        String[] outStrings = new String[MESSAGE_POST_RESULT];
        String str = "/proc/" + pid + "/cmdline";
        int[] iArr = new int[MESSAGE_POST_RESULT];
        iArr[MAXIMUM_POOL_SIZE] = 4128;
        Process.readProcFile(str, iArr, outStrings, null, null);
        return outStrings[MAXIMUM_POOL_SIZE];
    }

    private static Handler getHandler() {
        Handler handler;
        synchronized (AsyncTask.class) {
            if (sHandler == null) {
                sHandler = new InternalHandler();
            }
            handler = sHandler;
        }
        return handler;
    }

    public static void setDefaultExecutor(Executor exec) {
        sDefaultExecutor = exec;
    }

    public AsyncTask() {
        this.mStatus = Status.PENDING;
        this.mCancelled = new AtomicBoolean();
        this.mTaskInvoked = new AtomicBoolean();
        this.mWorker = new WorkerRunnable<Params, Result>() {
            public Result call() throws Exception {
                AsyncTask.this.mTaskInvoked.set(true);
                Process.setThreadPriority(10);
                Result result = AsyncTask.this.doInBackground(this.mParams);
                Binder.flushPendingCommands();
                return AsyncTask.this.postResult(result);
            }
        };
        this.mFuture = new AnonymousClass3(this.mWorker);
    }

    private void postResultIfNotInvoked(Result result) {
        if (!this.mTaskInvoked.get()) {
            postResult(result);
        }
    }

    private Result postResult(Result result) {
        Handler handler = getHandler();
        Object[] objArr = new Object[MESSAGE_POST_RESULT];
        objArr[MAXIMUM_POOL_SIZE] = result;
        handler.obtainMessage(MESSAGE_POST_RESULT, new AsyncTaskResult(this, objArr)).sendToTarget();
        return result;
    }

    public final Status getStatus() {
        return this.mStatus;
    }

    protected void onPreExecute() {
    }

    protected void onPostExecute(Result result) {
    }

    protected void onProgressUpdate(Progress... progressArr) {
    }

    protected void onCancelled(Result result) {
        onCancelled();
    }

    protected void onCancelled() {
    }

    public final boolean isCancelled() {
        return this.mCancelled.get();
    }

    public final boolean cancel(boolean mayInterruptIfRunning) {
        this.mCancelled.set(true);
        return this.mFuture.cancel(mayInterruptIfRunning);
    }

    public final Result get() throws InterruptedException, ExecutionException {
        return this.mFuture.get();
    }

    public final Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.mFuture.get(timeout, unit);
    }

    public final AsyncTask<Params, Progress, Result> execute(Params... params) {
        return executeOnExecutor(sDefaultExecutor, params);
    }

    public final AsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec, Params... params) {
        if (this.mStatus != Status.PENDING) {
            switch (-getandroid-os-AsyncTask$StatusSwitchesValues()[this.mStatus.ordinal()]) {
                case MESSAGE_POST_RESULT /*1*/:
                    throw new IllegalStateException("Cannot execute task: the task has already been executed (a task can be executed only once)");
                case MESSAGE_POST_PROGRESS /*2*/:
                    throw new IllegalStateException("Cannot execute task: the task is already running.");
            }
        }
        this.mStatus = Status.RUNNING;
        onPreExecute();
        this.mWorker.mParams = params;
        exec.execute(this.mFuture);
        return this;
    }

    public static void execute(Runnable runnable) {
        sDefaultExecutor.execute(runnable);
    }

    protected final void publishProgress(Progress... values) {
        if (!isCancelled()) {
            getHandler().obtainMessage(MESSAGE_POST_PROGRESS, new AsyncTaskResult(this, values)).sendToTarget();
        }
    }

    private void finish(Result result) {
        if (isCancelled()) {
            onCancelled(result);
        } else {
            if (IS_EMU && IS_ANTUTU) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG, "when async task is finishing,has excepiton:" + e);
                }
            }
            if (IS_FPGA && IS_ANTUTU) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e2) {
                    Log.e(LOG_TAG, "InterruptedException e:" + e2);
                }
            }
            onPostExecute(result);
        }
        this.mStatus = Status.FINISHED;
    }
}
