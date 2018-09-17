package java.net;

import java.io.IOException;

/* compiled from: URLConnection */
class UnknownContentHandler extends ContentHandler {
    static final ContentHandler INSTANCE = new UnknownContentHandler();

    UnknownContentHandler() {
    }

    public Object getContent(URLConnection uc) throws IOException {
        return uc.getInputStream();
    }
}
