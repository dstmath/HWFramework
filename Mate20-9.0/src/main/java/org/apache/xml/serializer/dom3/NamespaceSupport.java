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

        public Prefixes(String[] prefixes2, int size2) {
            this.prefixes = prefixes2;
            this.size = size2;
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
        String[] strArr2 = this.fNamespace;
        int i2 = this.fNamespaceSize;
        this.fNamespaceSize = i2 + 1;
        strArr2[i2] = XML_URI;
        String[] strArr3 = this.fNamespace;
        int i3 = this.fNamespaceSize;
        this.fNamespaceSize = i3 + 1;
        strArr3[i3] = PREFIX_XMLNS;
        String[] strArr4 = this.fNamespace;
        int i4 = this.fNamespaceSize;
        this.fNamespaceSize = i4 + 1;
        strArr4[i4] = XMLNS_URI;
        this.fCurrentContext++;
    }

    public void pushContext() {
        if (this.fCurrentContext + 1 == this.fContext.length) {
            int[] contextarray = new int[(this.fContext.length * 2)];
            System.arraycopy(this.fContext, 0, contextarray, 0, this.fContext.length);
            this.fContext = contextarray;
        }
        int[] contextarray2 = this.fContext;
        int i = this.fCurrentContext + 1;
        this.fCurrentContext = i;
        contextarray2[i] = this.fNamespaceSize;
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
        String[] namespacearray2 = this.fNamespace;
        int i2 = this.fNamespaceSize;
        this.fNamespaceSize = i2 + 1;
        namespacearray2[i2] = prefix;
        String[] strArr = this.fNamespace;
        int i3 = this.fNamespaceSize;
        this.fNamespaceSize = i3 + 1;
        strArr[i3] = uri;
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
        for (int i = this.fNamespaceSize; i > 0; i -= 2) {
            if (this.fNamespace[i - 1].equals(uri) && getURI(this.fNamespace[i - 2]).equals(uri)) {
                return this.fNamespace[i - 2];
            }
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
        if (this.fPrefixes.length < this.fNamespace.length / 2) {
            this.fPrefixes = new String[this.fNamespaceSize];
        }
        boolean unique = true;
        int count = 0;
        for (int i = 2; i < this.fNamespaceSize - 2; i += 2) {
            String prefix = this.fNamespace[i + 2];
            int k = 0;
            while (true) {
                if (k >= count) {
                    break;
                } else if (this.fPrefixes[k] == prefix) {
                    unique = false;
                    break;
                } else {
                    k++;
                }
            }
            if (unique) {
                this.fPrefixes[count] = prefix;
                count++;
            }
            unique = true;
        }
        return new Prefixes(this.fPrefixes, count);
    }
}
