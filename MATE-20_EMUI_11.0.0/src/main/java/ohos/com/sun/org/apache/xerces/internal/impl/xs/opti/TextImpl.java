package ohos.com.sun.org.apache.xerces.internal.impl.xs.opti;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Node;

public class TextImpl extends DefaultText {
    int fCol;
    String fData = null;
    int fRow;
    SchemaDOM fSchemaDOM = null;

    public TextImpl(StringBuffer stringBuffer, SchemaDOM schemaDOM, int i, int i2) {
        this.fData = stringBuffer.toString();
        this.fSchemaDOM = schemaDOM;
        this.fRow = i;
        this.fCol = i2;
        this.uri = null;
        this.localpart = null;
        this.prefix = null;
        this.rawname = null;
        this.nodeType = 3;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public Node getParentNode() {
        return this.fSchemaDOM.relations[this.fRow][0];
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public Node getPreviousSibling() {
        if (this.fCol == 1) {
            return null;
        }
        return this.fSchemaDOM.relations[this.fRow][this.fCol - 1];
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public Node getNextSibling() {
        if (this.fCol == this.fSchemaDOM.relations[this.fRow].length - 1) {
            return null;
        }
        return this.fSchemaDOM.relations[this.fRow][this.fCol + 1];
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultText
    public String getData() throws DOMException {
        return this.fData;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultText
    public int getLength() {
        String str = this.fData;
        if (str == null) {
            return 0;
        }
        return str.length();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultText
    public String substringData(int i, int i2) throws DOMException {
        String str = this.fData;
        if (str == null) {
            return null;
        }
        if (i2 < 0 || i < 0 || i > str.length()) {
            throw new DOMException(1, "parameter error");
        }
        int i3 = i2 + i;
        if (i3 >= this.fData.length()) {
            return this.fData.substring(i);
        }
        return this.fData.substring(i, i3);
    }
}
