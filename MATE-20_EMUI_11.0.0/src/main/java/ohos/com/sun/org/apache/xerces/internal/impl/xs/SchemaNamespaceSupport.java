package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport;

public class SchemaNamespaceSupport extends NamespaceSupport {
    public SchemaNamespaceSupport() {
    }

    public SchemaNamespaceSupport(SchemaNamespaceSupport schemaNamespaceSupport) {
        this.fNamespaceSize = schemaNamespaceSupport.fNamespaceSize;
        if (this.fNamespace.length < this.fNamespaceSize) {
            this.fNamespace = new String[this.fNamespaceSize];
        }
        System.arraycopy(schemaNamespaceSupport.fNamespace, 0, this.fNamespace, 0, this.fNamespaceSize);
        this.fCurrentContext = schemaNamespaceSupport.fCurrentContext;
        if (this.fContext.length <= this.fCurrentContext) {
            this.fContext = new int[(this.fCurrentContext + 1)];
        }
        System.arraycopy(schemaNamespaceSupport.fContext, 0, this.fContext, 0, this.fCurrentContext + 1);
    }

    public void setEffectiveContext(String[] strArr) {
        if (strArr != null && strArr.length != 0) {
            pushContext();
            int length = this.fNamespaceSize + strArr.length;
            if (this.fNamespace.length < length) {
                String[] strArr2 = new String[length];
                System.arraycopy(this.fNamespace, 0, strArr2, 0, this.fNamespace.length);
                this.fNamespace = strArr2;
            }
            System.arraycopy(strArr, 0, this.fNamespace, this.fNamespaceSize, strArr.length);
            this.fNamespaceSize = length;
        }
    }

    public String[] getEffectiveLocalContext() {
        int i;
        int i2;
        if (this.fCurrentContext < 3 || (i2 = this.fNamespaceSize - (i = this.fContext[3])) <= 0) {
            return null;
        }
        String[] strArr = new String[i2];
        System.arraycopy(this.fNamespace, i, strArr, 0, i2);
        return strArr;
    }

    public void makeGlobal() {
        if (this.fCurrentContext >= 3) {
            this.fCurrentContext = 3;
            this.fNamespaceSize = this.fContext[3];
        }
    }
}
