package org.apache.xml.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

interface WriterChain {
    void close() throws IOException;

    void flush() throws IOException;

    OutputStream getOutputStream();

    Writer getWriter();

    void write(int i) throws IOException;

    void write(String str) throws IOException;

    void write(String str, int i, int i2) throws IOException;

    void write(char[] cArr) throws IOException;

    void write(char[] cArr, int i, int i2) throws IOException;
}
