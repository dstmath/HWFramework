package javax.net.ssl;

import java.util.EventObject;

public class SSLSessionBindingEvent extends EventObject {
    private static final long serialVersionUID = 3989172637106345L;
    private String name;

    public SSLSessionBindingEvent(SSLSession session, String name) {
        super(session);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public SSLSession getSession() {
        return (SSLSession) getSource();
    }
}
