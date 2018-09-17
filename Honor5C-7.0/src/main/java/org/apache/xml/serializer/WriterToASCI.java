package org.apache.xml.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

class WriterToASCI extends Writer implements WriterChain {
    private final OutputStream m_os;

    public WriterToASCI(OutputStream os) {
        this.m_os = os;
    }

    public void write(char[] chars, int start, int length) throws IOException {
        int n = length + start;
        for (int i = start; i < n; i++) {
            this.m_os.write(chars[i]);
        }
    }

    public void write(int c) throws IOException {
        this.m_os.write(c);
    }

    public void write(String s) throws IOException {
        int n = s.length();
        for (int i = 0; i < n; i++) {
            this.m_os.write(s.charAt(i));
        }
    }

    public void flush() throws IOException {
        this.m_os.flush();
    }

    public void close() throws IOException {
        this.m_os.close();
    }

    public OutputStream getOutputStream() {
        return this.m_os;
    }

    public Writer getWriter() {
        return null;
    }
}
