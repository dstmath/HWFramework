package org.apache.xml.utils;

import java.util.EmptyStackException;
import java.util.Enumeration;
import org.xml.sax.helpers.NamespaceSupport;

public class NamespaceSupport2 extends NamespaceSupport {
    public static final String XMLNS = "http://www.w3.org/XML/1998/namespace";
    private Context2 currentContext;

    public NamespaceSupport2() {
        reset();
    }

    public void reset() {
        this.currentContext = new Context2(null);
        this.currentContext.declarePrefix("xml", "http://www.w3.org/XML/1998/namespace");
    }

    public void pushContext() {
        Context2 parentContext = this.currentContext;
        this.currentContext = parentContext.getChild();
        if (this.currentContext == null) {
            this.currentContext = new Context2(parentContext);
        } else {
            this.currentContext.setParent(parentContext);
        }
    }

    public void popContext() {
        Context2 parentContext = this.currentContext.getParent();
        if (parentContext == null) {
            throw new EmptyStackException();
        }
        this.currentContext = parentContext;
    }

    public boolean declarePrefix(String prefix, String uri) {
        if (prefix.equals("xml") || prefix.equals("xmlns")) {
            return false;
        }
        this.currentContext.declarePrefix(prefix, uri);
        return true;
    }

    public String[] processName(String qName, String[] parts, boolean isAttribute) {
        String[] name = this.currentContext.processName(qName, isAttribute);
        if (name == null) {
            return null;
        }
        System.arraycopy(name, 0, parts, 0, 3);
        return parts;
    }

    public String getURI(String prefix) {
        return this.currentContext.getURI(prefix);
    }

    public Enumeration getPrefixes() {
        return this.currentContext.getPrefixes();
    }

    public String getPrefix(String uri) {
        return this.currentContext.getPrefix(uri);
    }

    public Enumeration getPrefixes(String uri) {
        return new PrefixForUriEnumerator(this, uri, getPrefixes());
    }

    public Enumeration getDeclaredPrefixes() {
        return this.currentContext.getDeclaredPrefixes();
    }
}
