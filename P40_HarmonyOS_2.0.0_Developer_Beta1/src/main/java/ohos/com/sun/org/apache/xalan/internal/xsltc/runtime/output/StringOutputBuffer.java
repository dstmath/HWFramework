package ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.output;

class StringOutputBuffer implements OutputBuffer {
    private StringBuffer _buffer = new StringBuffer();

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.output.OutputBuffer
    public String close() {
        return this._buffer.toString();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.output.OutputBuffer
    public OutputBuffer append(String str) {
        this._buffer.append(str);
        return this;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.output.OutputBuffer
    public OutputBuffer append(char[] cArr, int i, int i2) {
        this._buffer.append(cArr, i, i2);
        return this;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.output.OutputBuffer
    public OutputBuffer append(char c) {
        this._buffer.append(c);
        return this;
    }
}
