package org.apache.http.impl.io;

import java.io.IOException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.message.LineFormatter;
import org.apache.http.params.HttpParams;

@Deprecated
public class HttpRequestWriter extends AbstractMessageWriter {
    public HttpRequestWriter(SessionOutputBuffer buffer, LineFormatter formatter, HttpParams params) {
        super(buffer, formatter, params);
    }

    protected void writeHeadLine(HttpMessage message) throws IOException {
        this.sessionBuffer.writeLine(this.lineFormatter.formatRequestLine(this.lineBuf, ((HttpRequest) message).getRequestLine()));
    }
}
