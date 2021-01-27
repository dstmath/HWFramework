package ohos.com.sun.org.apache.xerces.internal.util;

import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.org.xml.sax.AttributeList;
import ohos.org.xml.sax.ext.Attributes2;

public final class AttributesProxy implements AttributeList, Attributes2 {
    private XMLAttributes fAttributes;

    public AttributesProxy(XMLAttributes xMLAttributes) {
        this.fAttributes = xMLAttributes;
    }

    public void setAttributes(XMLAttributes xMLAttributes) {
        this.fAttributes = xMLAttributes;
    }

    public XMLAttributes getAttributes() {
        return this.fAttributes;
    }

    public int getLength() {
        return this.fAttributes.getLength();
    }

    public String getQName(int i) {
        return this.fAttributes.getQName(i);
    }

    public String getURI(int i) {
        String uri = this.fAttributes.getURI(i);
        return uri != null ? uri : XMLSymbols.EMPTY_STRING;
    }

    public String getLocalName(int i) {
        return this.fAttributes.getLocalName(i);
    }

    public String getType(int i) {
        return this.fAttributes.getType(i);
    }

    public String getType(String str) {
        return this.fAttributes.getType(str);
    }

    public String getType(String str, String str2) {
        if (str.equals(XMLSymbols.EMPTY_STRING)) {
            return this.fAttributes.getType(null, str2);
        }
        return this.fAttributes.getType(str, str2);
    }

    public String getValue(int i) {
        return this.fAttributes.getValue(i);
    }

    public String getValue(String str) {
        return this.fAttributes.getValue(str);
    }

    public String getValue(String str, String str2) {
        if (str.equals(XMLSymbols.EMPTY_STRING)) {
            return this.fAttributes.getValue(null, str2);
        }
        return this.fAttributes.getValue(str, str2);
    }

    public int getIndex(String str) {
        return this.fAttributes.getIndex(str);
    }

    public int getIndex(String str, String str2) {
        if (str.equals(XMLSymbols.EMPTY_STRING)) {
            return this.fAttributes.getIndex(null, str2);
        }
        return this.fAttributes.getIndex(str, str2);
    }

    public boolean isDeclared(int i) {
        if (i >= 0 && i < this.fAttributes.getLength()) {
            return Boolean.TRUE.equals(this.fAttributes.getAugmentations(i).getItem(Constants.ATTRIBUTE_DECLARED));
        }
        throw new ArrayIndexOutOfBoundsException(i);
    }

    public boolean isDeclared(String str) {
        int index = getIndex(str);
        if (index != -1) {
            return Boolean.TRUE.equals(this.fAttributes.getAugmentations(index).getItem(Constants.ATTRIBUTE_DECLARED));
        }
        throw new IllegalArgumentException(str);
    }

    public boolean isDeclared(String str, String str2) {
        int index = getIndex(str, str2);
        if (index != -1) {
            return Boolean.TRUE.equals(this.fAttributes.getAugmentations(index).getItem(Constants.ATTRIBUTE_DECLARED));
        }
        throw new IllegalArgumentException(str2);
    }

    public boolean isSpecified(int i) {
        if (i >= 0 && i < this.fAttributes.getLength()) {
            return this.fAttributes.isSpecified(i);
        }
        throw new ArrayIndexOutOfBoundsException(i);
    }

    public boolean isSpecified(String str) {
        int index = getIndex(str);
        if (index != -1) {
            return this.fAttributes.isSpecified(index);
        }
        throw new IllegalArgumentException(str);
    }

    public boolean isSpecified(String str, String str2) {
        int index = getIndex(str, str2);
        if (index != -1) {
            return this.fAttributes.isSpecified(index);
        }
        throw new IllegalArgumentException(str2);
    }

    public String getName(int i) {
        return this.fAttributes.getQName(i);
    }
}
