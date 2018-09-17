package android.net.wifi.nan;

public class WifiNanSubscribeSession extends WifiNanSession {
    public WifiNanSubscribeSession(WifiNanManager manager, int sessionId) {
        super(manager, sessionId);
    }

    public void subscribe(SubscribeData subscribeData, SubscribeSettings subscribeSettings) {
        this.mManager.subscribe(this.mSessionId, subscribeData, subscribeSettings);
    }
}
