package org.apache.xml.serializer.dom3;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class NamespaceSupport {
    static final String PREFIX_XML = "xml".intern();
    static final String PREFIX_XMLNS = "xmlns".intern();
    public static final String XMLNS_URI = SerializerConstants.XMLNS_URI.intern();
    public static final String XML_URI = "http://www.w3.org/XML/1998/namespace".intern();
    protected int[] fContext = new int[8];
    protected int fCurrentContext;
    protected String[] fNamespace = new String[32];
    protected int fNamespaceSize;
    protected String[] fPrefixes = new String[16];

    protected final class Prefixes implements Enumeration {
        private int counter = 0;
        private String[] prefixes;
        private int size = 0;

        public Prefixes(String[] prefixes, int size) {
            this.prefixes = prefixes;
            this.size = size;
        }

        public boolean hasMoreElements() {
            return this.counter < this.size;
        }

        public Object nextElement() {
            if (this.counter < this.size) {
                String[] strArr = NamespaceSupport.this.fPrefixes;
                int i = this.counter;
                this.counter = i + 1;
                return strArr[i];
            }
            throw new NoSuchElementException("Illegal access to Namespace prefixes enumeration.");
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < this.size; i++) {
                buf.append(this.prefixes[i]);
                buf.append(" ");
            }
            return buf.toString();
        }
    }

    public void reset() {
        this.fNamespaceSize = 0;
        this.fCurrentContext = 0;
        this.fContext[this.fCurrentContext] = this.fNamespaceSize;
        String[] strArr = this.fNamespace;
        int i = this.fNamespaceSize;
        this.fNamespaceSize = i + 1;
        strArr[i] = PREFIX_XML;
        strArr = this.fNamespace;
        i = this.fNamespaceSize;
        this.fNamespaceSize = i + 1;
        strArr[i] = XML_URI;
        strArr = this.fNamespace;
        i = this.fNamespaceSize;
        this.fNamespaceSize = i + 1;
        strArr[i] = PREFIX_XMLNS;
        strArr = this.fNamespace;
        i = this.fNamespaceSize;
        this.fNamespaceSize = i + 1;
        strArr[i] = XMLNS_URI;
        this.fCurrentContext++;
    }

    public void pushContext() {
        if (this.fCurrentContext + 1 == this.fContext.length) {
            int[] contextarray = new int[(this.fContext.length * 2)];
            System.arraycopy(this.fContext, 0, contextarray, 0, this.fContext.length);
            this.fContext = contextarray;
        }
        int[] iArr = this.fContext;
        int i = this.fCurrentContext + 1;
        this.fCurrentContext = i;
        iArr[i] = this.fNamespaceSize;
    }

    public void popContext() {
        int[] iArr = this.fContext;
        int i = this.fCurrentContext;
        this.fCurrentContext = i - 1;
        this.fNamespaceSize = iArr[i];
    }

    public boolean declarePrefix(String prefix, String uri) {
        if (prefix == PREFIX_XML || prefix == PREFIX_XMLNS) {
            return false;
        }
        for (int i = this.fNamespaceSize; i > this.fContext[this.fCurrentContext]; i -= 2) {
            if (this.fNamespace[i - 2].equals(prefix)) {
                this.fNamespace[i - 1] = uri;
                return true;
            }
        }
        if (this.fNamespaceSize == this.fNamespace.length) {
            String[] namespacearray = new String[(this.fNamespaceSize * 2)];
            System.arraycopy(this.fNamespace, 0, namespacearray, 0, this.fNamespaceSize);
            this.fNamespace = namespacearray;
        }
        String[] strArr = this.fNamespace;
        int i2 = this.fNamespaceSize;
        this.fNamespaceSize = i2 + 1;
        strArr[i2] = prefix;
        strArr = this.fNamespace;
        i2 = this.fNamespaceSize;
        this.fNamespaceSize = i2 + 1;
        strArr[i2] = uri;
        return true;
    }

    public String getURI(String prefix) {
        for (int i = this.fNamespaceSize; i > 0; i -= 2) {
            if (this.fNamespace[i - 2].equals(prefix)) {
                return this.fNamespace[i - 1];
            }
        }
        return null;
    }

    public String getPrefix(String uri) {
        int i = this.fNamespaceSize;
        while (i > 0) {
            if (this.fNamespace[i - 1].equals(uri) && getURI(this.fNamespace[i - 2]).equals(uri)) {
                return this.fNamespace[i - 2];
            }
            i -= 2;
        }
        return null;
    }

    public int getDeclaredPrefixCount() {
        return (this.fNamespaceSize - this.fContext[this.fCurrentContext]) / 2;
    }

    public String getDeclaredPrefixAt(int index) {
        return this.fNamespace[this.fContext[this.fCurrentContext] + (index * 2)];
    }

    public Enumeration getAllPrefixes() {
        int count = 0;
        if (this.fPrefixes.length < this.fNamespace.length / 2) {
            this.fPrefixes = new String[this.fNamespaceSize];
        }
        boolean unique = true;
        for (int i = 2; i < this.fNamespaceSize - 2; i += 2) {
            String prefix = this.fNamespace[i + 2];
            for (int k = 0; k < count; k++) {
                if (this.fPrefixes[k] == prefix) {
                    unique = false;
                    break;
                }
            }
            if (unique) {
                int count2 = count + 1;
                this.fPrefixes[count] = prefix;
                count = count2;
            }
            unique = true;
        }
        return new Prefixes(this.fPrefixes, count);
    }
}
