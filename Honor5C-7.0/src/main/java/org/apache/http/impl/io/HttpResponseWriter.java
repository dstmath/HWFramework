package org.apache.http.impl.io;

import java.io.IOException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.message.LineFormatter;
import org.apache.http.params.HttpParams;

@Deprecated
public class HttpResponseWriter extends AbstractMessageWriter {
    public HttpResponseWriter(SessionOutputBuffer buffer, LineFormatter formatter, HttpParams params) {
        super(buffer, formatter, params);
    }

    protected void writeHeadLine(HttpMessage message) throws IOException {
        this.sessionBuffer.writeLine(this.lineFormatter.formatStatusLine(this.lineBuf, ((HttpResponse) message).getStatusLine()));
    }
}
