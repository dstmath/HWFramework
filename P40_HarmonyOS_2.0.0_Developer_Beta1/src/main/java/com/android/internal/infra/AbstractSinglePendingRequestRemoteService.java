package com.android.internal.infra;

import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.IInterface;
import android.util.Slog;
import com.android.internal.infra.AbstractRemoteService;
import com.android.internal.infra.AbstractSinglePendingRequestRemoteService;
import java.io.PrintWriter;

public abstract class AbstractSinglePendingRequestRemoteService<S extends AbstractSinglePendingRequestRemoteService<S, I>, I extends IInterface> extends AbstractRemoteService<S, I> {
    protected AbstractRemoteService.BasePendingRequest<S, I> mPendingRequest;

    public AbstractSinglePendingRequestRemoteService(Context context, String serviceInterface, ComponentName componentName, int userId, AbstractRemoteService.VultureCallback<S> callback, Handler handler, int bindingFlags, boolean verbose) {
        super(context, serviceInterface, componentName, userId, callback, handler, bindingFlags, verbose);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.infra.AbstractRemoteService
    public void handlePendingRequests() {
        if (this.mPendingRequest != null) {
            AbstractRemoteService.BasePendingRequest<S, I> pendingRequest = this.mPendingRequest;
            this.mPendingRequest = null;
            handlePendingRequest(pendingRequest);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.infra.AbstractRemoteService
    public void handleOnDestroy() {
        handleCancelPendingRequest();
    }

    /* access modifiers changed from: protected */
    public AbstractRemoteService.BasePendingRequest<S, I> handleCancelPendingRequest() {
        AbstractRemoteService.BasePendingRequest<S, I> pendingRequest = this.mPendingRequest;
        if (pendingRequest != null) {
            pendingRequest.cancel();
            this.mPendingRequest = null;
        }
        return pendingRequest;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.infra.AbstractRemoteService
    public void handleBindFailure() {
        if (this.mPendingRequest != null) {
            if (this.mVerbose) {
                String str = this.mTag;
                Slog.v(str, "Sending failure to " + this.mPendingRequest);
            }
            this.mPendingRequest.onFailed();
            this.mPendingRequest = null;
        }
    }

    @Override // com.android.internal.infra.AbstractRemoteService
    public void dump(String prefix, PrintWriter pw) {
        super.dump(prefix, pw);
        pw.append((CharSequence) prefix).append("hasPendingRequest=").append((CharSequence) String.valueOf(this.mPendingRequest != null)).println();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.infra.AbstractRemoteService
    public void handlePendingRequestWhileUnBound(AbstractRemoteService.BasePendingRequest<S, I> pendingRequest) {
        if (this.mPendingRequest != null) {
            if (this.mVerbose) {
                String str = this.mTag;
                Slog.v(str, "handlePendingRequestWhileUnBound(): cancelling " + this.mPendingRequest + " to handle " + pendingRequest);
            }
            try {
                this.mPendingRequest.cancel();
            } catch (Exception e) {
                Slog.e(this.mTag, "cancel Exception");
            }
        }
        this.mPendingRequest = pendingRequest;
    }
}
