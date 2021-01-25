package ohos.com.sun.org.apache.xerces.internal.xinclude;

import java.util.Enumeration;
import ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;

public class MultipleScopeNamespaceSupport extends NamespaceSupport {
    protected int fCurrentScope;
    protected int[] fScope;

    public MultipleScopeNamespaceSupport() {
        this.fScope = new int[8];
        this.fCurrentScope = 0;
        this.fScope[0] = 0;
    }

    public MultipleScopeNamespaceSupport(NamespaceContext namespaceContext) {
        super(namespaceContext);
        this.fScope = new int[8];
        this.fCurrentScope = 0;
        this.fScope[0] = 0;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport, ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public Enumeration getAllPrefixes() {
        boolean z;
        if (this.fPrefixes.length < this.fNamespace.length / 2) {
            this.fPrefixes = new String[this.fNamespaceSize];
        }
        int i = 0;
        for (int i2 = this.fContext[this.fScope[this.fCurrentScope]]; i2 <= this.fNamespaceSize - 2; i2 += 2) {
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
        return new NamespaceSupport.Prefixes(this.fPrefixes, i);
    }

    public int getScopeForContext(int i) {
        int i2 = this.fCurrentScope;
        while (i < this.fScope[i2]) {
            i2--;
        }
        return i2;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport, ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public String getPrefix(String str) {
        return getPrefix(str, this.fNamespaceSize, this.fContext[this.fScope[this.fCurrentScope]]);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport, ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public String getURI(String str) {
        return getURI(str, this.fNamespaceSize, this.fContext[this.fScope[this.fCurrentScope]]);
    }

    public String getPrefix(String str, int i) {
        return getPrefix(str, this.fContext[i + 1], this.fContext[this.fScope[getScopeForContext(i)]]);
    }

    public String getURI(String str, int i) {
        return getURI(str, this.fContext[i + 1], this.fContext[this.fScope[getScopeForContext(i)]]);
    }

    public String getPrefix(String str, int i, int i2) {
        if (str == NamespaceContext.XML_URI) {
            return XMLSymbols.PREFIX_XML;
        }
        if (str == NamespaceContext.XMLNS_URI) {
            return XMLSymbols.PREFIX_XMLNS;
        }
        while (i > i2) {
            if (this.fNamespace[i - 1] == str) {
                int i3 = i - 2;
                if (getURI(this.fNamespace[i3]) == str) {
                    return this.fNamespace[i3];
                }
            }
            i -= 2;
        }
        return null;
    }

    public String getURI(String str, int i, int i2) {
        if (str == XMLSymbols.PREFIX_XML) {
            return NamespaceContext.XML_URI;
        }
        if (str == XMLSymbols.PREFIX_XMLNS) {
            return NamespaceContext.XMLNS_URI;
        }
        while (i > i2) {
            if (this.fNamespace[i - 2] == str) {
                return this.fNamespace[i - 1];
            }
            i -= 2;
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport, ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public void reset() {
        this.fCurrentContext = this.fScope[this.fCurrentScope];
        this.fNamespaceSize = this.fContext[this.fCurrentContext];
    }

    public void pushScope() {
        int i = this.fCurrentScope + 1;
        int[] iArr = this.fScope;
        if (i == iArr.length) {
            int[] iArr2 = new int[(iArr.length * 2)];
            System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
            this.fScope = iArr2;
        }
        pushContext();
        int[] iArr3 = this.fScope;
        int i2 = this.fCurrentScope + 1;
        this.fCurrentScope = i2;
        iArr3[i2] = this.fCurrentContext;
    }

    public void popScope() {
        int[] iArr = this.fScope;
        int i = this.fCurrentScope;
        this.fCurrentScope = i - 1;
        this.fCurrentContext = iArr[i];
        popContext();
    }
}
