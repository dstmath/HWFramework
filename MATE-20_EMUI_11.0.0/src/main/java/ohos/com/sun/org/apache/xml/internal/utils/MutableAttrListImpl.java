package ohos.com.sun.org.apache.xml.internal.utils;

import java.io.Serializable;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.helpers.AttributesImpl;

public class MutableAttrListImpl extends AttributesImpl implements Serializable {
    static final long serialVersionUID = 6289452013442934470L;

    public MutableAttrListImpl() {
    }

    public MutableAttrListImpl(Attributes attributes) {
        super(attributes);
    }

    public void addAttribute(String str, String str2, String str3, String str4, String str5) {
        if (str == null) {
            str = "";
        }
        int index = getIndex(str3);
        if (index >= 0) {
            setAttribute(index, str, str2, str3, str4, str5);
        } else {
            MutableAttrListImpl.super.addAttribute(str, str2, str3, str4, str5);
        }
    }

    public void addAttributes(Attributes attributes) {
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            String uri = attributes.getURI(i);
            if (uri == null) {
                uri = "";
            }
            String localName = attributes.getLocalName(i);
            String qName = attributes.getQName(i);
            int index = getIndex(uri, localName);
            if (index >= 0) {
                setAttribute(index, uri, localName, qName, attributes.getType(i), attributes.getValue(i));
            } else {
                addAttribute(uri, localName, qName, attributes.getType(i), attributes.getValue(i));
            }
        }
    }

    public boolean contains(String str) {
        return getValue(str) != null;
    }
}
