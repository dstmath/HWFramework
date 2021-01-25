package ohos.com.sun.org.apache.xml.internal.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

interface WriterChain {
    @Override // java.io.Closeable, java.lang.AutoCloseable, ohos.com.sun.org.apache.xml.internal.serializer.WriterChain
    void close() throws IOException;

    @Override // java.io.Flushable, ohos.com.sun.org.apache.xml.internal.serializer.WriterChain
    void flush() throws IOException;

    OutputStream getOutputStream();

    Writer getWriter();

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.WriterChain
    void write(int i) throws IOException;

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.WriterChain
    void write(String str) throws IOException;

    void write(String str, int i, int i2) throws IOException;

    void write(char[] cArr) throws IOException;

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.WriterChain
    void write(char[] cArr, int i, int i2) throws IOException;
}
