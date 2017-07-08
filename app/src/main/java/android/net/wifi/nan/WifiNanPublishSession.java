package android.net.wifi.nan;

public class WifiNanPublishSession extends WifiNanSession {
    public WifiNanPublishSession(WifiNanManager manager, int sessionId) {
        super(manager, sessionId);
    }

    public void publish(PublishData publishData, PublishSettings publishSettings) {
        this.mManager.publish(this.mSessionId, publishData, publishSettings);
    }
}
