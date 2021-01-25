package ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.output;

interface OutputBuffer {
    OutputBuffer append(char c);

    OutputBuffer append(String str);

    OutputBuffer append(char[] cArr, int i, int i2);

    String close();
}
