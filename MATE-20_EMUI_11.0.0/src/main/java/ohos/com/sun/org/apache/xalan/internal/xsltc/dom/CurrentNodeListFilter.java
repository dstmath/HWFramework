package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;

public interface CurrentNodeListFilter {
    boolean test(int i, int i2, int i3, int i4, AbstractTranslet abstractTranslet, DTMAxisIterator dTMAxisIterator);
}
