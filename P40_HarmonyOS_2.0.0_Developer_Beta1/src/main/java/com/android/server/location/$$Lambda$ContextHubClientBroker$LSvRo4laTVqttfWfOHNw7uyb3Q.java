package com.android.server.location;

import android.hardware.location.IContextHubClientCallback;
import com.android.server.location.ContextHubClientBroker;

/* renamed from: com.android.server.location.-$$Lambda$ContextHubClientBroker$LSvRo4l-aTVqttfWfOHNw7uyb3Q  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ContextHubClientBroker$LSvRo4laTVqttfWfOHNw7uyb3Q implements ContextHubClientBroker.CallbackConsumer {
    public static final /* synthetic */ $$Lambda$ContextHubClientBroker$LSvRo4laTVqttfWfOHNw7uyb3Q INSTANCE = new $$Lambda$ContextHubClientBroker$LSvRo4laTVqttfWfOHNw7uyb3Q();

    private /* synthetic */ $$Lambda$ContextHubClientBroker$LSvRo4laTVqttfWfOHNw7uyb3Q() {
    }

    @Override // com.android.server.location.ContextHubClientBroker.CallbackConsumer
    public final void accept(IContextHubClientCallback iContextHubClientCallback) {
        iContextHubClientCallback.onHubReset();
    }
}
