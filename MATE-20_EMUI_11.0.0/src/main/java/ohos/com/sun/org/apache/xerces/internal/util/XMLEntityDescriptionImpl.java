package ohos.com.sun.org.apache.xerces.internal.util;

import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityDescription;

public class XMLEntityDescriptionImpl extends XMLResourceIdentifierImpl implements XMLEntityDescription {
    protected String fEntityName;

    public XMLEntityDescriptionImpl() {
    }

    public XMLEntityDescriptionImpl(String str, String str2, String str3, String str4, String str5) {
        setDescription(str, str2, str3, str4, str5);
    }

    public XMLEntityDescriptionImpl(String str, String str2, String str3, String str4, String str5, String str6) {
        setDescription(str, str2, str3, str4, str5, str6);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityDescription
    public void setEntityName(String str) {
        this.fEntityName = str;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityDescription
    public String getEntityName() {
        return this.fEntityName;
    }

    public void setDescription(String str, String str2, String str3, String str4, String str5) {
        setDescription(str, str2, str3, str4, str5, null);
    }

    public void setDescription(String str, String str2, String str3, String str4, String str5, String str6) {
        this.fEntityName = str;
        setValues(str2, str3, str4, str5, str6);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl
    public void clear() {
        super.clear();
        this.fEntityName = null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl
    public int hashCode() {
        int hashCode = super.hashCode();
        String str = this.fEntityName;
        return str != null ? hashCode + str.hashCode() : hashCode;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        String str = this.fEntityName;
        if (str != null) {
            stringBuffer.append(str);
        }
        stringBuffer.append(':');
        if (this.fPublicId != null) {
            stringBuffer.append(this.fPublicId);
        }
        stringBuffer.append(':');
        if (this.fLiteralSystemId != null) {
            stringBuffer.append(this.fLiteralSystemId);
        }
        stringBuffer.append(':');
        if (this.fBaseSystemId != null) {
            stringBuffer.append(this.fBaseSystemId);
        }
        stringBuffer.append(':');
        if (this.fExpandedSystemId != null) {
            stringBuffer.append(this.fExpandedSystemId);
        }
        stringBuffer.append(':');
        if (this.fNamespace != null) {
            stringBuffer.append(this.fNamespace);
        }
        return stringBuffer.toString();
    }
}
