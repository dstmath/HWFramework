package libcore.net.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkEventDispatcher {
    private static final NetworkEventDispatcher instance = new NetworkEventDispatcher();
    private final List<NetworkEventListener> listeners = new CopyOnWriteArrayList();

    public static NetworkEventDispatcher getInstance() {
        return instance;
    }

    protected NetworkEventDispatcher() {
    }

    public void addListener(NetworkEventListener toAdd) {
        if (toAdd == null) {
            throw new NullPointerException("toAdd == null");
        }
        this.listeners.add(toAdd);
    }

    public void removeListener(NetworkEventListener toRemove) {
        for (NetworkEventListener listener : this.listeners) {
            if (listener == toRemove) {
                this.listeners.remove(listener);
                return;
            }
        }
    }

    public void onNetworkConfigurationChanged() {
        for (NetworkEventListener listener : this.listeners) {
            try {
                listener.onNetworkConfigurationChanged();
            } catch (RuntimeException e) {
                System.logI("Exception thrown during network event propagation", e);
            }
        }
    }
}
