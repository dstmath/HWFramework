package ohos.com.sun.org.apache.xerces.internal.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;

public final class JAXPNamespaceContextWrapper implements NamespaceContext {
    private final Vector fAllPrefixes = new Vector();
    private int[] fContext = new int[8];
    private int fCurrentContext;
    private ohos.javax.xml.namespace.NamespaceContext fNamespaceContext;
    private List fPrefixes;
    private SymbolTable fSymbolTable;

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public boolean declarePrefix(String str, String str2) {
        return true;
    }

    public JAXPNamespaceContextWrapper(SymbolTable symbolTable) {
        setSymbolTable(symbolTable);
    }

    public void setNamespaceContext(ohos.javax.xml.namespace.NamespaceContext namespaceContext) {
        this.fNamespaceContext = namespaceContext;
    }

    public ohos.javax.xml.namespace.NamespaceContext getNamespaceContext() {
        return this.fNamespaceContext;
    }

    public void setSymbolTable(SymbolTable symbolTable) {
        this.fSymbolTable = symbolTable;
    }

    public SymbolTable getSymbolTable() {
        return this.fSymbolTable;
    }

    public void setDeclaredPrefixes(List list) {
        this.fPrefixes = list;
    }

    public List getDeclaredPrefixes() {
        return this.fPrefixes;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public String getURI(String str) {
        String namespaceURI;
        ohos.javax.xml.namespace.NamespaceContext namespaceContext = this.fNamespaceContext;
        if (namespaceContext == null || (namespaceURI = namespaceContext.getNamespaceURI(str)) == null || "".equals(namespaceURI)) {
            return null;
        }
        SymbolTable symbolTable = this.fSymbolTable;
        return symbolTable != null ? symbolTable.addSymbol(namespaceURI) : namespaceURI.intern();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public String getPrefix(String str) {
        if (this.fNamespaceContext == null) {
            return null;
        }
        if (str == null) {
            str = "";
        }
        String prefix = this.fNamespaceContext.getPrefix(str);
        if (prefix == null) {
            prefix = "";
        }
        SymbolTable symbolTable = this.fSymbolTable;
        return symbolTable != null ? symbolTable.addSymbol(prefix) : prefix.intern();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public Enumeration getAllPrefixes() {
        return Collections.enumeration(new TreeSet(this.fAllPrefixes));
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
        iArr3[i2] = this.fAllPrefixes.size();
        List list = this.fPrefixes;
        if (list != null) {
            this.fAllPrefixes.addAll(list);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public void popContext() {
        Vector vector = this.fAllPrefixes;
        int[] iArr = this.fContext;
        int i = this.fCurrentContext;
        this.fCurrentContext = i - 1;
        vector.setSize(iArr[i]);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public int getDeclaredPrefixCount() {
        List list = this.fPrefixes;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public String getDeclaredPrefixAt(int i) {
        return (String) this.fPrefixes.get(i);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public void reset() {
        this.fCurrentContext = 0;
        this.fContext[this.fCurrentContext] = 0;
        this.fAllPrefixes.clear();
    }
}
