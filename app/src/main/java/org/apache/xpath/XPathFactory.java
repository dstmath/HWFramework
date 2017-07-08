package org.apache.xpath;

import javax.xml.transform.SourceLocator;
import org.apache.xml.utils.PrefixResolver;

public interface XPathFactory {
    XPath create(String str, SourceLocator sourceLocator, PrefixResolver prefixResolver, int i);
}
