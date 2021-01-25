package android.hardware.location;

import android.annotation.SystemApi;

@SystemApi
public class ContextHubClientCallback {
    public void onMessageFromNanoApp(ContextHubClient client, NanoAppMessage message) {
    }

    public void onHubReset(ContextHubClient client) {
    }

    public void onNanoAppAborted(ContextHubClient client, long nanoAppId, int abortCode) {
    }

    public void onNanoAppLoaded(ContextHubClient client, long nanoAppId) {
    }

    public void onNanoAppUnloaded(ContextHubClient client, long nanoAppId) {
    }

    public void onNanoAppEnabled(ContextHubClient client, long nanoAppId) {
    }

    public void onNanoAppDisabled(ContextHubClient client, long nanoAppId) {
    }
}
