package sun.security.action;

import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.Map;

public class PutAllAction implements PrivilegedAction<Void> {
    private final Map map;
    private final Provider provider;

    public PutAllAction(Provider provider, Map map) {
        this.provider = provider;
        this.map = map;
    }

    public Void run() {
        this.provider.putAll(this.map);
        return null;
    }
}
