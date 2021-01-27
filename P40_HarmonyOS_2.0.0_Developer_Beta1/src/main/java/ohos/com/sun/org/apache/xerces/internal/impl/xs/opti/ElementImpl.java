package ohos.com.sun.org.apache.xerces.internal.impl.xs.opti;

import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;

public class ElementImpl extends DefaultElement {
    Attr[] attrs;
    int charOffset;
    int col;
    int column;
    String fAnnotation;
    String fSyntheticAnnotation;
    int line;
    int parentRow;
    int row;
    SchemaDOM schemaDOM;

    public ElementImpl(int i, int i2, int i3) {
        this.row = -1;
        this.col = -1;
        this.parentRow = -1;
        this.nodeType = 1;
        this.line = i;
        this.column = i2;
        this.charOffset = i3;
    }

    public ElementImpl(int i, int i2) {
        this(i, i2, -1);
    }

    public ElementImpl(String str, String str2, String str3, String str4, int i, int i2, int i3) {
        super(str, str2, str3, str4, 1);
        this.row = -1;
        this.col = -1;
        this.parentRow = -1;
        this.line = i;
        this.column = i2;
        this.charOffset = i3;
    }

    public ElementImpl(String str, String str2, String str3, String str4, int i, int i2) {
        this(str, str2, str3, str4, i, i2, -1);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public Document getOwnerDocument() {
        return this.schemaDOM;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public Node getParentNode() {
        return this.schemaDOM.relations[this.row][0];
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public boolean hasChildNodes() {
        return this.parentRow != -1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public Node getFirstChild() {
        if (this.parentRow == -1) {
            return null;
        }
        return this.schemaDOM.relations[this.parentRow][1];
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public Node getLastChild() {
        if (this.parentRow == -1) {
            return null;
        }
        int i = 1;
        while (i < this.schemaDOM.relations[this.parentRow].length) {
            if (this.schemaDOM.relations[this.parentRow][i] == null) {
                return this.schemaDOM.relations[this.parentRow][i - 1];
            }
            i++;
        }
        if (i == 1) {
            i++;
        }
        return this.schemaDOM.relations[this.parentRow][i - 1];
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public Node getPreviousSibling() {
        if (this.col == 1) {
            return null;
        }
        return this.schemaDOM.relations[this.row][this.col - 1];
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public Node getNextSibling() {
        if (this.col == this.schemaDOM.relations[this.row].length - 1) {
            return null;
        }
        return this.schemaDOM.relations[this.row][this.col + 1];
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public NamedNodeMap getAttributes() {
        return new NamedNodeMapImpl(this.attrs);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultNode
    public boolean hasAttributes() {
        return this.attrs.length != 0;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultElement
    public String getTagName() {
        return this.rawname;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultElement
    public String getAttribute(String str) {
        int i = 0;
        while (true) {
            Attr[] attrArr = this.attrs;
            if (i >= attrArr.length) {
                return "";
            }
            if (attrArr[i].getName().equals(str)) {
                return this.attrs[i].getValue();
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultElement
    public Attr getAttributeNode(String str) {
        int i = 0;
        while (true) {
            Attr[] attrArr = this.attrs;
            if (i >= attrArr.length) {
                return null;
            }
            if (attrArr[i].getName().equals(str)) {
                return this.attrs[i];
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultElement
    public String getAttributeNS(String str, String str2) {
        int i = 0;
        while (true) {
            Attr[] attrArr = this.attrs;
            if (i >= attrArr.length) {
                return "";
            }
            if (attrArr[i].getLocalName().equals(str2) && nsEquals(this.attrs[i].getNamespaceURI(), str)) {
                return this.attrs[i].getValue();
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultElement
    public Attr getAttributeNodeNS(String str, String str2) {
        int i = 0;
        while (true) {
            Attr[] attrArr = this.attrs;
            if (i >= attrArr.length) {
                return null;
            }
            if (attrArr[i].getName().equals(str2) && nsEquals(this.attrs[i].getNamespaceURI(), str)) {
                return this.attrs[i];
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultElement
    public boolean hasAttribute(String str) {
        int i = 0;
        while (true) {
            Attr[] attrArr = this.attrs;
            if (i >= attrArr.length) {
                return false;
            }
            if (attrArr[i].getName().equals(str)) {
                return true;
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultElement
    public boolean hasAttributeNS(String str, String str2) {
        int i = 0;
        while (true) {
            Attr[] attrArr = this.attrs;
            if (i >= attrArr.length) {
                return false;
            }
            if (attrArr[i].getName().equals(str2) && nsEquals(this.attrs[i].getNamespaceURI(), str)) {
                return true;
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultElement
    public void setAttribute(String str, String str2) {
        int i = 0;
        while (true) {
            Attr[] attrArr = this.attrs;
            if (i >= attrArr.length) {
                return;
            }
            if (attrArr[i].getName().equals(str)) {
                this.attrs[i].setValue(str2);
                return;
            }
            i++;
        }
    }

    public int getLineNumber() {
        return this.line;
    }

    public int getColumnNumber() {
        return this.column;
    }

    public int getCharacterOffset() {
        return this.charOffset;
    }

    public String getAnnotation() {
        return this.fAnnotation;
    }

    public String getSyntheticAnnotation() {
        return this.fSyntheticAnnotation;
    }

    private static boolean nsEquals(String str, String str2) {
        if (str == null) {
            return str2 == null;
        }
        return str.equals(str2);
    }
}
