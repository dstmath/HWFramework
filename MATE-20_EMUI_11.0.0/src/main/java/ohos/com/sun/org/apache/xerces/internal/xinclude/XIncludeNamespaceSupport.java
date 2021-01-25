package ohos.com.sun.org.apache.xerces.internal.xinclude;

import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;

public class XIncludeNamespaceSupport extends MultipleScopeNamespaceSupport {
    private boolean[] fValidContext;

    public XIncludeNamespaceSupport() {
        this.fValidContext = new boolean[8];
    }

    public XIncludeNamespaceSupport(NamespaceContext namespaceContext) {
        super(namespaceContext);
        this.fValidContext = new boolean[8];
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport, ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
    public void pushContext() {
        super.pushContext();
        int i = this.fCurrentContext + 1;
        boolean[] zArr = this.fValidContext;
        if (i == zArr.length) {
            boolean[] zArr2 = new boolean[(zArr.length * 2)];
            System.arraycopy(zArr, 0, zArr2, 0, zArr.length);
            this.fValidContext = zArr2;
        }
        this.fValidContext[this.fCurrentContext] = true;
    }

    public void setContextInvalid() {
        this.fValidContext[this.fCurrentContext] = false;
    }

    public String getURIFromIncludeParent(String str) {
        int i = this.fCurrentContext - 1;
        while (i > 0 && !this.fValidContext[i]) {
            i--;
        }
        return getURI(str, i);
    }
}
