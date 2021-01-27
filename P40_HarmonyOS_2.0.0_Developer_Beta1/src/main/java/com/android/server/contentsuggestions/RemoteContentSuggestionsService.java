package com.android.server.contentsuggestions;

import android.app.contentsuggestions.ClassificationsRequest;
import android.app.contentsuggestions.IClassificationsCallback;
import android.app.contentsuggestions.ISelectionsCallback;
import android.app.contentsuggestions.SelectionsRequest;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.GraphicBuffer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.service.contentsuggestions.IContentSuggestionsService;
import com.android.internal.infra.AbstractMultiplePendingRequestsRemoteService;
import com.android.internal.infra.AbstractRemoteService;
import com.android.server.pm.DumpState;

public class RemoteContentSuggestionsService extends AbstractMultiplePendingRequestsRemoteService<RemoteContentSuggestionsService, IContentSuggestionsService> {
    private static final long TIMEOUT_REMOTE_REQUEST_MILLIS = 2000;

    /* access modifiers changed from: package-private */
    public interface Callbacks extends AbstractRemoteService.VultureCallback<RemoteContentSuggestionsService> {
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    RemoteContentSuggestionsService(Context context, ComponentName serviceName, int userId, Callbacks callbacks, boolean bindInstantServiceAllowed, boolean verbose) {
        super(context, "android.service.contentsuggestions.ContentSuggestionsService", serviceName, userId, callbacks, context.getMainThreadHandler(), bindInstantServiceAllowed ? DumpState.DUMP_CHANGES : 0, verbose, 1);
    }

    /* access modifiers changed from: protected */
    public IContentSuggestionsService getServiceInterface(IBinder service) {
        return IContentSuggestionsService.Stub.asInterface(service);
    }

    /* access modifiers changed from: protected */
    public long getTimeoutIdleBindMillis() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public long getRemoteRequestMillis() {
        return TIMEOUT_REMOTE_REQUEST_MILLIS;
    }

    /* access modifiers changed from: package-private */
    public void provideContextImage(int taskId, GraphicBuffer contextImage, int colorSpaceId, Bundle imageContextRequestExtras) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(taskId, contextImage, colorSpaceId, imageContextRequestExtras) {
            /* class com.android.server.contentsuggestions.$$Lambda$RemoteContentSuggestionsService$VKh1DoMPNSPjPfnVGdsInmxuqzc */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ GraphicBuffer f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ Bundle f$3;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run(IInterface iInterface) {
                RemoteContentSuggestionsService.lambda$provideContextImage$0(this.f$0, this.f$1, this.f$2, this.f$3, (IContentSuggestionsService) iInterface);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void suggestContentSelections(SelectionsRequest selectionsRequest, ISelectionsCallback selectionsCallback) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(selectionsRequest, selectionsCallback) {
            /* class com.android.server.contentsuggestions.$$Lambda$RemoteContentSuggestionsService$yUTbcaYlZCYTmagCkNJ3i2VCkY4 */
            private final /* synthetic */ SelectionsRequest f$0;
            private final /* synthetic */ ISelectionsCallback f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run(IInterface iInterface) {
                RemoteContentSuggestionsService.lambda$suggestContentSelections$1(this.f$0, this.f$1, (IContentSuggestionsService) iInterface);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void classifyContentSelections(ClassificationsRequest classificationsRequest, IClassificationsCallback callback) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(classificationsRequest, callback) {
            /* class com.android.server.contentsuggestions.$$Lambda$RemoteContentSuggestionsService$eoGnQ2MDLLnW1UBX6wxNE1VBLAk */
            private final /* synthetic */ ClassificationsRequest f$0;
            private final /* synthetic */ IClassificationsCallback f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run(IInterface iInterface) {
                RemoteContentSuggestionsService.lambda$classifyContentSelections$2(this.f$0, this.f$1, (IContentSuggestionsService) iInterface);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void notifyInteraction(String requestId, Bundle bundle) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(requestId, bundle) {
            /* class com.android.server.contentsuggestions.$$Lambda$RemoteContentSuggestionsService$Enqw46SYVKFK9F2xX4qUcIu5_3I */
            private final /* synthetic */ String f$0;
            private final /* synthetic */ Bundle f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run(IInterface iInterface) {
                RemoteContentSuggestionsService.lambda$notifyInteraction$3(this.f$0, this.f$1, (IContentSuggestionsService) iInterface);
            }
        });
    }
}
