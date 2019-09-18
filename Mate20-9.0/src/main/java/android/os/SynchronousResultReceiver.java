package android.os;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SynchronousResultReceiver extends ResultReceiver {
    private final CompletableFuture<Result> mFuture;
    private final String mName;

    public static class Result {
        public Bundle bundle;
        public int resultCode;

        public Result(int resultCode2, Bundle bundle2) {
            this.resultCode = resultCode2;
            this.bundle = bundle2;
        }
    }

    public SynchronousResultReceiver() {
        super((Handler) null);
        this.mFuture = new CompletableFuture<>();
        this.mName = null;
    }

    public SynchronousResultReceiver(String name) {
        super((Handler) null);
        this.mFuture = new CompletableFuture<>();
        this.mName = name;
    }

    /* access modifiers changed from: protected */
    public final void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        this.mFuture.complete(new Result(resultCode, resultData));
    }

    public String getName() {
        return this.mName;
    }

    public Result awaitResult(long timeoutMillis) throws TimeoutException {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (timeoutMillis >= 0) {
            try {
                return this.mFuture.get(timeoutMillis, TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                throw new AssertionError("Error receiving response", e);
            } catch (InterruptedException e2) {
                timeoutMillis -= deadline - System.currentTimeMillis();
            }
        }
        throw new TimeoutException();
    }
}
