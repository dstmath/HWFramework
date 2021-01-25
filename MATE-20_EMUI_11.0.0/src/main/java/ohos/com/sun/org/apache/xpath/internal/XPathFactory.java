package ohos.com.sun.org.apache.xpath.internal;

import ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver;
import ohos.javax.xml.transform.SourceLocator;

public interface XPathFactory {
    XPath create(String str, SourceLocator sourceLocator, PrefixResolver prefixResolver, int i);
}
