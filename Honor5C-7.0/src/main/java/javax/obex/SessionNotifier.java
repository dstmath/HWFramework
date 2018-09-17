package javax.obex;

import java.io.IOException;

public interface SessionNotifier {
    ObexSession acceptAndOpen(ServerRequestHandler serverRequestHandler) throws IOException;

    ObexSession acceptAndOpen(ServerRequestHandler serverRequestHandler, Authenticator authenticator) throws IOException;
}
