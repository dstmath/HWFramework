package ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;

class WriterOutputBuffer implements OutputBuffer {
    private static int BUFFER_SIZE = 32768;
    private static final int KB = 1024;
    private Writer _writer;

    static {
        if (SecuritySupport.getSystemProperty("os.name").equalsIgnoreCase("solaris")) {
        }
    }

    public WriterOutputBuffer(Writer writer) {
        this._writer = new BufferedWriter(writer, BUFFER_SIZE);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.output.OutputBuffer
    public String close() {
        try {
            this._writer.flush();
            return "";
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.output.OutputBuffer
    public OutputBuffer append(String str) {
        try {
            this._writer.write(str);
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.output.OutputBuffer
    public OutputBuffer append(char[] cArr, int i, int i2) {
        try {
            this._writer.write(cArr, i, i2);
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.output.OutputBuffer
    public OutputBuffer append(char c) {
        try {
            this._writer.write(c);
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }
}
