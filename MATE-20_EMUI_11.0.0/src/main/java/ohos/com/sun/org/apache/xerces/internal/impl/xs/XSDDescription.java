package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import ohos.com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLSchemaDescription;

public class XSDDescription extends XMLResourceIdentifierImpl implements XMLSchemaDescription {
    public static final short CONTEXT_ATTRIBUTE = 6;
    public static final short CONTEXT_ELEMENT = 5;
    public static final short CONTEXT_IMPORT = 2;
    public static final short CONTEXT_INCLUDE = 0;
    public static final short CONTEXT_INITIALIZE = -1;
    public static final short CONTEXT_INSTANCE = 4;
    public static final short CONTEXT_PREPARSE = 3;
    public static final short CONTEXT_REDEFINE = 1;
    public static final short CONTEXT_XSITYPE = 7;
    protected XMLAttributes fAttributes;
    protected short fContextType;
    protected QName fEnclosedElementName;
    protected String[] fLocationHints;
    protected QName fTriggeringComponent;

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription
    public String getGrammarType() {
        return "http://www.w3.org/2001/XMLSchema";
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLSchemaDescription
    public short getContextType() {
        return this.fContextType;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLSchemaDescription
    public String getTargetNamespace() {
        return this.fNamespace;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLSchemaDescription
    public String[] getLocationHints() {
        return this.fLocationHints;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLSchemaDescription
    public QName getTriggeringComponent() {
        return this.fTriggeringComponent;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLSchemaDescription
    public QName getEnclosingElementName() {
        return this.fEnclosedElementName;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLSchemaDescription
    public XMLAttributes getAttributes() {
        return this.fAttributes;
    }

    public boolean fromInstance() {
        short s = this.fContextType;
        return s == 6 || s == 5 || s == 4 || s == 7;
    }

    public boolean isExternal() {
        short s = this.fContextType;
        return s == 0 || s == 1 || s == 2 || s == 5 || s == 6 || s == 7;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof XMLSchemaDescription)) {
            return false;
        }
        XMLSchemaDescription xMLSchemaDescription = (XMLSchemaDescription) obj;
        if (this.fNamespace != null) {
            return this.fNamespace.equals(xMLSchemaDescription.getTargetNamespace());
        }
        if (xMLSchemaDescription.getTargetNamespace() == null) {
            return true;
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl
    public int hashCode() {
        if (this.fNamespace == null) {
            return 0;
        }
        return this.fNamespace.hashCode();
    }

    public void setContextType(short s) {
        this.fContextType = s;
    }

    public void setTargetNamespace(String str) {
        this.fNamespace = str;
    }

    public void setLocationHints(String[] strArr) {
        int length = strArr.length;
        this.fLocationHints = new String[length];
        System.arraycopy(strArr, 0, this.fLocationHints, 0, length);
    }

    public void setTriggeringComponent(QName qName) {
        this.fTriggeringComponent = qName;
    }

    public void setEnclosingElementName(QName qName) {
        this.fEnclosedElementName = qName;
    }

    public void setAttributes(XMLAttributes xMLAttributes) {
        this.fAttributes = xMLAttributes;
    }

    public void reset() {
        super.clear();
        this.fContextType = -1;
        this.fLocationHints = null;
        this.fTriggeringComponent = null;
        this.fEnclosedElementName = null;
        this.fAttributes = null;
    }

    public XSDDescription makeClone() {
        XSDDescription xSDDescription = new XSDDescription();
        xSDDescription.fAttributes = this.fAttributes;
        xSDDescription.fBaseSystemId = this.fBaseSystemId;
        xSDDescription.fContextType = this.fContextType;
        xSDDescription.fEnclosedElementName = this.fEnclosedElementName;
        xSDDescription.fExpandedSystemId = this.fExpandedSystemId;
        xSDDescription.fLiteralSystemId = this.fLiteralSystemId;
        xSDDescription.fLocationHints = this.fLocationHints;
        xSDDescription.fPublicId = this.fPublicId;
        xSDDescription.fNamespace = this.fNamespace;
        xSDDescription.fTriggeringComponent = this.fTriggeringComponent;
        return xSDDescription;
    }
}
