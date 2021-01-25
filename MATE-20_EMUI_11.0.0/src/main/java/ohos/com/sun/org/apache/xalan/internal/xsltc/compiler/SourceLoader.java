package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.org.xml.sax.InputSource;

public interface SourceLoader {
    InputSource loadSource(String str, String str2, XSLTC xsltc);
}
