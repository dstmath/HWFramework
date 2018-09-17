package android.net.wifi.aware;

import java.util.List;

public class DiscoverySessionCallback {
    public void onPublishStarted(PublishDiscoverySession session) {
    }

    public void onSubscribeStarted(SubscribeDiscoverySession session) {
    }

    public void onSessionConfigUpdated() {
    }

    public void onSessionConfigFailed() {
    }

    public void onSessionTerminated() {
    }

    public void onServiceDiscovered(PeerHandle peerHandle, byte[] serviceSpecificInfo, List<byte[]> list) {
    }

    public void onMessageSendSucceeded(int messageId) {
    }

    public void onMessageSendFailed(int messageId) {
    }

    public void onMessageReceived(PeerHandle peerHandle, byte[] message) {
    }
}
