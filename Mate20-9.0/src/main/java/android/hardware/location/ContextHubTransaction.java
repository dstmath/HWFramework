package android.hardware.location;

import android.annotation.SystemApi;
import android.app.SearchManager;
import android.os.Handler;
import android.os.HandlerExecutor;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SystemApi
public class ContextHubTransaction<T> {
    public static final int RESULT_FAILED_AT_HUB = 5;
    public static final int RESULT_FAILED_BAD_PARAMS = 2;
    public static final int RESULT_FAILED_BUSY = 4;
    public static final int RESULT_FAILED_HAL_UNAVAILABLE = 8;
    public static final int RESULT_FAILED_SERVICE_INTERNAL_FAILURE = 7;
    public static final int RESULT_FAILED_TIMEOUT = 6;
    public static final int RESULT_FAILED_UNINITIALIZED = 3;
    public static final int RESULT_FAILED_UNKNOWN = 1;
    public static final int RESULT_SUCCESS = 0;
    private static final String TAG = "ContextHubTransaction";
    public static final int TYPE_DISABLE_NANOAPP = 3;
    public static final int TYPE_ENABLE_NANOAPP = 2;
    public static final int TYPE_LOAD_NANOAPP = 0;
    public static final int TYPE_QUERY_NANOAPPS = 4;
    public static final int TYPE_UNLOAD_NANOAPP = 1;
    private final CountDownLatch mDoneSignal = new CountDownLatch(1);
    private Executor mExecutor = null;
    private boolean mIsResponseSet = false;
    private OnCompleteListener<T> mListener = null;
    private Response<T> mResponse;
    private int mTransactionType;

    @FunctionalInterface
    public interface OnCompleteListener<L> {
        void onComplete(ContextHubTransaction<L> contextHubTransaction, Response<L> response);
    }

    public static class Response<R> {
        private R mContents;
        private int mResult;

        Response(int result, R contents) {
            this.mResult = result;
            this.mContents = contents;
        }

        public int getResult() {
            return this.mResult;
        }

        public R getContents() {
            return this.mContents;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Result {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    ContextHubTransaction(int type) {
        this.mTransactionType = type;
    }

    public static String typeToString(int type, boolean upperCase) {
        switch (type) {
            case 0:
                return upperCase ? "Load" : "load";
            case 1:
                return upperCase ? "Unload" : "unload";
            case 2:
                return upperCase ? "Enable" : "enable";
            case 3:
                return upperCase ? "Disable" : "disable";
            case 4:
                return upperCase ? "Query" : SearchManager.QUERY;
            default:
                return upperCase ? "Unknown" : "unknown";
        }
    }

    public int getType() {
        return this.mTransactionType;
    }

    public Response<T> waitForResponse(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (this.mDoneSignal.await(timeout, unit)) {
            return this.mResponse;
        }
        throw new TimeoutException("Timed out while waiting for transaction");
    }

    public void setOnCompleteListener(OnCompleteListener<T> listener, Executor executor) {
        synchronized (this) {
            Preconditions.checkNotNull(listener, "OnCompleteListener cannot be null");
            Preconditions.checkNotNull(executor, "Executor cannot be null");
            if (this.mListener == null) {
                this.mListener = listener;
                this.mExecutor = executor;
                if (this.mDoneSignal.getCount() == 0) {
                    this.mExecutor.execute(new Runnable() {
                        public final void run() {
                            ContextHubTransaction.this.mListener.onComplete(ContextHubTransaction.this, ContextHubTransaction.this.mResponse);
                        }
                    });
                }
            } else {
                throw new IllegalStateException("Cannot set ContextHubTransaction listener multiple times");
            }
        }
    }

    public void setOnCompleteListener(OnCompleteListener<T> listener) {
        setOnCompleteListener(listener, new HandlerExecutor(Handler.getMain()));
    }

    /* access modifiers changed from: package-private */
    public void setResponse(Response<T> response) {
        synchronized (this) {
            Preconditions.checkNotNull(response, "Response cannot be null");
            if (!this.mIsResponseSet) {
                this.mResponse = response;
                this.mIsResponseSet = true;
                this.mDoneSignal.countDown();
                if (this.mListener != null) {
                    this.mExecutor.execute(new Runnable() {
                        public final void run() {
                            ContextHubTransaction.this.mListener.onComplete(ContextHubTransaction.this, ContextHubTransaction.this.mResponse);
                        }
                    });
                }
            } else {
                throw new IllegalStateException("Cannot set response of ContextHubTransaction multiple times");
            }
        }
    }
}
