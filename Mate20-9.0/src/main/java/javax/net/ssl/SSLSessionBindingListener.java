package javax.net.ssl;

import java.util.EventListener;

public interface SSLSessionBindingListener extends EventListener {
    void valueBound(SSLSessionBindingEvent sSLSessionBindingEvent);

    void valueUnbound(SSLSessionBindingEvent sSLSessionBindingEvent);
}
