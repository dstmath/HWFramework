package javax.security.auth.callback;

import java.io.IOException;

public interface CallbackHandler {
    void handle(Callback[] callbackArr) throws IOException, UnsupportedCallbackException;
}
