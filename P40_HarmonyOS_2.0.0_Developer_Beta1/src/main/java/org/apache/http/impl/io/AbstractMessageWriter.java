package org.apache.http.impl.io;

import java.io.IOException;
import java.util.Iterator;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.io.HttpMessageWriter;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.message.BasicLineFormatter;
import org.apache.http.message.LineFormatter;
import org.apache.http.params.HttpParams;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public abstract class AbstractMessageWriter implements HttpMessageWriter {
    protected final CharArrayBuffer lineBuf;
    protected final LineFormatter lineFormatter;
    protected final SessionOutputBuffer sessionBuffer;

    /* access modifiers changed from: protected */
    public abstract void writeHeadLine(HttpMessage httpMessage) throws IOException;

    public AbstractMessageWriter(SessionOutputBuffer buffer, LineFormatter formatter, HttpParams params) {
        if (buffer != null) {
            this.sessionBuffer = buffer;
            this.lineBuf = new CharArrayBuffer(128);
            this.lineFormatter = formatter != null ? formatter : BasicLineFormatter.DEFAULT;
            return;
        }
        throw new IllegalArgumentException("Session input buffer may not be null");
    }

    @Override // org.apache.http.io.HttpMessageWriter
    public void write(HttpMessage message) throws IOException, HttpException {
        if (message != null) {
            writeHeadLine(message);
            Iterator it = message.headerIterator();
            while (it.hasNext()) {
                this.sessionBuffer.writeLine(this.lineFormatter.formatHeader(this.lineBuf, (Header) it.next()));
            }
            this.lineBuf.clear();
            this.sessionBuffer.writeLine(this.lineBuf);
            return;
        }
        throw new IllegalArgumentException("HTTP message may not be null");
    }
}
