package ohos.com.sun.org.apache.xerces.internal.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;

public class NamespaceSupport implements NamespaceContext {
    protected int[] fContext;
    protected int fCurrentContext;
    protected String[] fNamespace;
    protected int fNamespaceSize;
    protected String[] fPrefixes;

    public NamespaceSupport() {
        this.fNamespace = new String[32];
        this.fContext = new int[8];
        this.fPrefixes = new String[16];
    }

    public NamespaceSupport(NamespaceContext namespaceContext) {
        this.fNamespace = new String[32];
        this.fContext = new int[8];
        this.fPrefixes = new String[16];
        pushContext();
        Enumeration allPrefixes = namespaceContext.getAllPrefixes();
        while (allPrefixes.hasMoreElements()) {
            String str = (String) allPrefixes.nextElement();
            declarePrefix(str, namespaceContext.getURI(str));
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public void reset() {
        this.fNamespaceSize = 0;
        this.fCurrentContext = 0;
        String[] strArr = this.fNamespace;
        int i = this.fNamespaceSize;
        this.fNamespaceSize = i + 1;
        strArr[i] = XMLSymbols.PREFIX_XML;
        String[] strArr2 = this.fNamespace;
        int i2 = this.fNamespaceSize;
        this.fNamespaceSize = i2 + 1;
        strArr2[i2] = NamespaceContext.XML_URI;
        String[] strArr3 = this.fNamespace;
        int i3 = this.fNamespaceSize;
        this.fNamespaceSize = i3 + 1;
        strArr3[i3] = XMLSymbols.PREFIX_XMLNS;
        String[] strArr4 = this.fNamespace;
        int i4 = this.fNamespaceSize;
        this.fNamespaceSize = i4 + 1;
        strArr4[i4] = NamespaceContext.XMLNS_URI;
        this.fContext[this.fCurrentContext] = this.fNamespaceSize;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public void pushContext() {
        int i = this.fCurrentContext + 1;
        int[] iArr = this.fContext;
        if (i == iArr.length) {
            int[] iArr2 = new int[(iArr.length * 2)];
            System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
            this.fContext = iArr2;
        }
        int[] iArr3 = this.fContext;
        int i2 = this.fCurrentContext + 1;
        this.fCurrentContext = i2;
        iArr3[i2] = this.fNamespaceSize;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public void popContext() {
        int[] iArr = this.fContext;
        int i = this.fCurrentContext;
        this.fCurrentContext = i - 1;
        this.fNamespaceSize = iArr[i];
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public boolean declarePrefix(String str, String str2) {
        if (str == XMLSymbols.PREFIX_XML || str == XMLSymbols.PREFIX_XMLNS) {
            return false;
        }
        for (int i = this.fNamespaceSize; i > this.fContext[this.fCurrentContext]; i -= 2) {
            String[] strArr = this.fNamespace;
            if (strArr[i - 2] == str) {
                strArr[i - 1] = str2;
                return true;
            }
        }
        int i2 = this.fNamespaceSize;
        String[] strArr2 = this.fNamespace;
        if (i2 == strArr2.length) {
            String[] strArr3 = new String[(i2 * 2)];
            System.arraycopy(strArr2, 0, strArr3, 0, i2);
            this.fNamespace = strArr3;
        }
        String[] strArr4 = this.fNamespace;
        int i3 = this.fNamespaceSize;
        this.fNamespaceSize = i3 + 1;
        strArr4[i3] = str;
        int i4 = this.fNamespaceSize;
        this.fNamespaceSize = i4 + 1;
        strArr4[i4] = str2;
        return true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public String getURI(String str) {
        for (int i = this.fNamespaceSize; i > 0; i -= 2) {
            String[] strArr = this.fNamespace;
            if (strArr[i - 2] == str) {
                return strArr[i - 1];
            }
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public String getPrefix(String str) {
        for (int i = this.fNamespaceSize; i > 0; i -= 2) {
            String[] strArr = this.fNamespace;
            if (strArr[i - 1] == str) {
                int i2 = i - 2;
                if (getURI(strArr[i2]) == str) {
                    return this.fNamespace[i2];
                }
            }
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public int getDeclaredPrefixCount() {
        return (this.fNamespaceSize - this.fContext[this.fCurrentContext]) / 2;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public String getDeclaredPrefixAt(int i) {
        return this.fNamespace[this.fContext[this.fCurrentContext] + (i * 2)];
    }

    public Iterator getPrefixes() {
        boolean z;
        if (this.fPrefixes.length < this.fNamespace.length / 2) {
            this.fPrefixes = new String[this.fNamespaceSize];
        }
        int i = 0;
        int i2 = 2;
        while (i2 < this.fNamespaceSize - 2) {
            i2 += 2;
            String str = this.fNamespace[i2];
            int i3 = 0;
            while (true) {
                if (i3 >= i) {
                    z = true;
                    break;
                } else if (this.fPrefixes[i3] == str) {
                    z = false;
                    break;
                } else {
                    i3++;
                }
            }
            if (z) {
                this.fPrefixes[i] = str;
                i++;
            }
        }
        return new IteratorPrefixes(this.fPrefixes, i);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public Enumeration getAllPrefixes() {
        boolean z;
        if (this.fPrefixes.length < this.fNamespace.length / 2) {
            this.fPrefixes = new String[this.fNamespaceSize];
        }
        int i = 0;
        int i2 = 2;
        while (i2 < this.fNamespaceSize - 2) {
            i2 += 2;
            String str = this.fNamespace[i2];
            int i3 = 0;
            while (true) {
                if (i3 >= i) {
                    z = true;
                    break;
                } else if (this.fPrefixes[i3] == str) {
                    z = false;
                    break;
                } else {
                    i3++;
                }
            }
            if (z) {
                this.fPrefixes[i] = str;
                i++;
            }
        }
        return new Prefixes(this.fPrefixes, i);
    }

    public Vector getPrefixes(String str) {
        Vector vector = new Vector();
        for (int i = this.fNamespaceSize; i > 0; i -= 2) {
            String[] strArr = this.fNamespace;
            if (strArr[i - 1] == str) {
                int i2 = i - 2;
                if (!vector.contains(strArr[i2])) {
                    vector.add(this.fNamespace[i2]);
                }
            }
        }
        return vector;
    }

    public boolean containsPrefix(String str) {
        for (int i = this.fNamespaceSize; i > 0; i -= 2) {
            if (this.fNamespace[i - 2] == str) {
                return true;
            }
        }
        return false;
    }

    public boolean containsPrefixInCurrentContext(String str) {
        for (int i = this.fContext[this.fCurrentContext]; i < this.fNamespaceSize; i += 2) {
            if (this.fNamespace[i] == str) {
                return true;
            }
        }
        return false;
    }

    protected final class IteratorPrefixes implements Iterator {
        private int counter = 0;
        private String[] prefixes;
        private int size = 0;

        public IteratorPrefixes(String[] strArr, int i) {
            this.prefixes = strArr;
            this.size = i;
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.counter < this.size;
        }

        @Override // java.util.Iterator
        public Object next() {
            if (this.counter < this.size) {
                String[] strArr = NamespaceSupport.this.fPrefixes;
                int i = this.counter;
                this.counter = i + 1;
                return strArr[i];
            }
            throw new NoSuchElementException("Illegal access to Namespace prefixes enumeration.");
        }

        @Override // java.lang.Object
        public String toString() {
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < this.size; i++) {
                stringBuffer.append(this.prefixes[i]);
                stringBuffer.append(" ");
            }
            return stringBuffer.toString();
        }

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    protected final class Prefixes implements Enumeration {
        private int counter = 0;
        private String[] prefixes;
        private int size = 0;

        public Prefixes(String[] strArr, int i) {
            this.prefixes = strArr;
            this.size = i;
        }

        @Override // java.util.Enumeration
        public boolean hasMoreElements() {
            return this.counter < this.size;
        }

        @Override // java.util.Enumeration
        public Object nextElement() {
            if (this.counter < this.size) {
                String[] strArr = NamespaceSupport.this.fPrefixes;
                int i = this.counter;
                this.counter = i + 1;
                return strArr[i];
            }
            throw new NoSuchElementException("Illegal access to Namespace prefixes enumeration.");
        }

        @Override // java.lang.Object
        public String toString() {
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < this.size; i++) {
                stringBuffer.append(this.prefixes[i]);
                stringBuffer.append(" ");
            }
            return stringBuffer.toString();
        }
    }
}
