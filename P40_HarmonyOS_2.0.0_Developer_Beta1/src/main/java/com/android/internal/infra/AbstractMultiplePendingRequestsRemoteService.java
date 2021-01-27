package com.android.internal.infra;

import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.IInterface;
import android.util.Slog;
import com.android.internal.infra.AbstractMultiplePendingRequestsRemoteService;
import com.android.internal.infra.AbstractRemoteService;
import java.io.PrintWriter;
import java.util.ArrayList;

public abstract class AbstractMultiplePendingRequestsRemoteService<S extends AbstractMultiplePendingRequestsRemoteService<S, I>, I extends IInterface> extends AbstractRemoteService<S, I> {
    private final int mInitialCapacity;
    protected ArrayList<AbstractRemoteService.BasePendingRequest<S, I>> mPendingRequests;

    public AbstractMultiplePendingRequestsRemoteService(Context context, String serviceInterface, ComponentName componentName, int userId, AbstractRemoteService.VultureCallback<S> callback, Handler handler, int bindingFlags, boolean verbose, int initialCapacity) {
        super(context, serviceInterface, componentName, userId, callback, handler, bindingFlags, verbose);
        this.mInitialCapacity = initialCapacity;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.infra.AbstractRemoteService
    public void handlePendingRequests() {
        ArrayList<AbstractRemoteService.BasePendingRequest<S, I>> arrayList = this.mPendingRequests;
        if (arrayList != null) {
            int size = arrayList.size();
            if (this.mVerbose) {
                String str = this.mTag;
                Slog.v(str, "Sending " + size + " pending requests");
            }
            for (int i = 0; i < size; i++) {
                this.mPendingRequests.get(i).run();
            }
            this.mPendingRequests = null;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.infra.AbstractRemoteService
    public void handleOnDestroy() {
        ArrayList<AbstractRemoteService.BasePendingRequest<S, I>> arrayList = this.mPendingRequests;
        if (arrayList != null) {
            int size = arrayList.size();
            if (this.mVerbose) {
                String str = this.mTag;
                Slog.v(str, "Canceling " + size + " pending requests");
            }
            for (int i = 0; i < size; i++) {
                this.mPendingRequests.get(i).cancel();
            }
            this.mPendingRequests = null;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.infra.AbstractRemoteService
    public final void handleBindFailure() {
        ArrayList<AbstractRemoteService.BasePendingRequest<S, I>> arrayList = this.mPendingRequests;
        if (arrayList != null) {
            int size = arrayList.size();
            if (this.mVerbose) {
                String str = this.mTag;
                Slog.v(str, "Sending failure to " + size + " pending requests");
            }
            for (int i = 0; i < size; i++) {
                AbstractRemoteService.BasePendingRequest<S, I> request = this.mPendingRequests.get(i);
                request.onFailed();
                request.finish();
            }
            this.mPendingRequests = null;
        }
    }

    @Override // com.android.internal.infra.AbstractRemoteService
    public void dump(String prefix, PrintWriter pw) {
        super.dump(prefix, pw);
        pw.append((CharSequence) prefix).append("initialCapacity=").append((CharSequence) String.valueOf(this.mInitialCapacity)).println();
        ArrayList<AbstractRemoteService.BasePendingRequest<S, I>> arrayList = this.mPendingRequests;
        pw.append((CharSequence) prefix).append("pendingRequests=").append((CharSequence) String.valueOf(arrayList == null ? 0 : arrayList.size())).println();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.infra.AbstractRemoteService
    public void handlePendingRequestWhileUnBound(AbstractRemoteService.BasePendingRequest<S, I> pendingRequest) {
        if (this.mPendingRequests == null) {
            this.mPendingRequests = new ArrayList<>(this.mInitialCapacity);
        }
        this.mPendingRequests.add(pendingRequest);
        if (this.mVerbose) {
            String str = this.mTag;
            Slog.v(str, "queued " + this.mPendingRequests.size() + " requests; last=" + pendingRequest);
        }
    }
}
