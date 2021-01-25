package ohos.com.sun.org.apache.xerces.internal.impl.xs.util;

import ohos.com.sun.org.apache.xerces.internal.util.SymbolHash;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObject;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

public final class XSNamedMap4Types extends XSNamedMapImpl {
    private final short fType;

    public XSNamedMap4Types(String str, SymbolHash symbolHash, short s) {
        super(str, symbolHash);
        this.fType = s;
    }

    public XSNamedMap4Types(String[] strArr, SymbolHash[] symbolHashArr, int i, short s) {
        super(strArr, symbolHashArr, i);
        this.fType = s;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMapImpl, ohos.com.sun.org.apache.xerces.internal.xs.XSNamedMap
    public synchronized int getLength() {
        if (this.fLength == -1) {
            int i = 0;
            for (int i2 = 0; i2 < this.fNSNum; i2++) {
                i += this.fMaps[i2].getLength();
            }
            XSObject[] xSObjectArr = new XSObject[i];
            int i3 = 0;
            for (int i4 = 0; i4 < this.fNSNum; i4++) {
                i3 += this.fMaps[i4].getValues(xSObjectArr, i3);
            }
            this.fLength = 0;
            this.fArray = new XSObject[i];
            for (int i5 = 0; i5 < i; i5++) {
                XSTypeDefinition xSTypeDefinition = (XSTypeDefinition) xSObjectArr[i5];
                if (xSTypeDefinition.getTypeCategory() == this.fType) {
                    XSObject[] xSObjectArr2 = this.fArray;
                    int i6 = this.fLength;
                    this.fLength = i6 + 1;
                    xSObjectArr2[i6] = xSTypeDefinition;
                }
            }
        }
        return this.fLength;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMapImpl, ohos.com.sun.org.apache.xerces.internal.xs.XSNamedMap
    public XSObject itemByName(String str, String str2) {
        for (int i = 0; i < this.fNSNum; i++) {
            if (isEqual(str, this.fNamespaces[i])) {
                XSTypeDefinition xSTypeDefinition = (XSTypeDefinition) this.fMaps[i].get(str2);
                if (xSTypeDefinition == null || xSTypeDefinition.getTypeCategory() != this.fType) {
                    return null;
                }
                return xSTypeDefinition;
            }
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMapImpl, ohos.com.sun.org.apache.xerces.internal.xs.XSNamedMap
    public synchronized XSObject item(int i) {
        if (this.fArray == null) {
            getLength();
        }
        if (i >= 0) {
            if (i < this.fLength) {
                return this.fArray[i];
            }
        }
        return null;
    }
}
