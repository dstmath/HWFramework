package ohos.com.sun.org.apache.xerces.internal.util;

import java.util.Iterator;
import ohos.javax.xml.namespace.NamespaceContext;

public class NamespaceContextWrapper implements NamespaceContext {
    private ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext fNamespaceContext;

    public NamespaceContextWrapper(NamespaceSupport namespaceSupport) {
        this.fNamespaceContext = namespaceSupport;
    }

    public String getNamespaceURI(String str) {
        if (str != null) {
            return this.fNamespaceContext.getURI(str.intern());
        }
        throw new IllegalArgumentException("Prefix can't be null");
    }

    public String getPrefix(String str) {
        if (str != null) {
            return this.fNamespaceContext.getPrefix(str.intern());
        }
        throw new IllegalArgumentException("URI can't be null.");
    }

    public Iterator getPrefixes(String str) {
        if (str != null) {
            return ((NamespaceSupport) this.fNamespaceContext).getPrefixes(str.intern()).iterator();
        }
        throw new IllegalArgumentException("URI can't be null.");
    }

    public ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext getNamespaceContext() {
        return this.fNamespaceContext;
    }
}
